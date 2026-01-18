package social_mate.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import social_mate.dto.request.NotificationRequestDto;
import social_mate.dto.request.PostRequestDto;
import social_mate.dto.response.PostResponseDto;
import social_mate.entity.User;
import social_mate.entity.UserPrincipal;
import social_mate.entity.enums.NotificationType;
import social_mate.entity.post.Post;
import social_mate.entity.post.PostLike;
import social_mate.entity.post.PostMedia;
import social_mate.mapper.PostMapper;
import social_mate.repository.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMediaRepository mediaRepository;
    private final PostMapper postMapper;
    private final NotificationService notificationService;
    private final CloudinaryService cloudinaryService;
    private final PostLikeRepository postLikeRepository;
    private final FriendRepository friendRepository;

    @Transactional
    public PostResponseDto createPost(PostRequestDto dto, List<MultipartFile> files, UserPrincipal userPrincipal) {

        // 1. Kiểm tra User
        if (userPrincipal == null) {
            throw new IllegalStateException("UserPrincipal không được null");
        }
        User userCurrent = userPrincipal.getUser();

        // 2. Tạo và lưu Post
        Post newPost = postMapper.toPost(dto);
        if (dto.getPrivacyMode() != null) {
            newPost.setPrivacyMode(dto.getPrivacyMode());
        } else {
            newPost.setPrivacyMode(1);
        }
        if (dto.getMetaData() != null && dto.getMetaData().containsKey("backgroundColor")) {
            newPost.setBackgroundColor((String) dto.getMetaData().get("backgroundColor"));
        }
        newPost.setUser(userCurrent);
        newPost.setDeleted(false);
        // Set OriginalPostId nếu có (share)
        if (dto.getOriginalPostId() != null) {
            newPost.setOriginalPostId(dto.getOriginalPostId());
        }

        Post savedPost = postRepository.save(newPost);
        List<PostMedia> savedMediaList = new ArrayList<>();

        if (files != null && !files.isEmpty()) {
            // 1. Upload song song (Multithreading)
            List<CompletableFuture<PostMedia>> futures = files.stream()
                    .map(file -> CompletableFuture.supplyAsync(() -> {
                        try {
                            // Upload lên Cloudinary
                            Map result = cloudinaryService.uploadFile(file);
                            String url = (String) result.get("secure_url");
                            String resourceType = (String) result.get("resource_type");

                            // Tạo object nhưng chưa lưu
                            PostMedia media = new PostMedia();
                            media.setUrl(url);
                            media.setType(resourceType);
                            media.setFileName(file.getOriginalFilename());
                            media.setPost(savedPost); // Set quan hệ với Post

                            return media;
                        } catch (IOException e) {
                            throw new RuntimeException("Lỗi upload file: " + file.getOriginalFilename(), e);
                        }
                    }))
                    .collect(Collectors.toList());

            // 2. Chờ tất cả upload xong và thu thập kết quả
            List<PostMedia> mediaEntities = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            // 3. Lưu Batch vào Database
            if (!mediaEntities.isEmpty()) {
                savedMediaList = mediaRepository.saveAll(mediaEntities);
            }
        }
        savedPost.setMedia(savedMediaList);

        // --- GỬI THÔNG BÁO CHO BẠN BÈ (Chỉ khi đăng bài mới, không phải share, và public/friend) ---
        if (newPost.getOriginalPostId() == null && (newPost.getPrivacyMode() == 1 || newPost.getPrivacyMode() == 2)) {
            sendNotificationToFriends(savedPost, userPrincipal);
        }

        return postMapper.toPostResponseDto(savedPost);
    }


    public List<PostResponseDto> getMyPosts(UserPrincipal userPrincipal) {
        User currentUser = userPrincipal.getUser();
        List<Post> posts = postRepository.findAllByUserAndDeletedFalseOrderByCreatedAtDesc(currentUser);
        List<PostResponseDto> postResponseDtos = new ArrayList<>();

        for (Post post : posts) {
            PostResponseDto dto = postMapper.toPostResponseDto(post);
            if (post.getOriginalPostId() != null) {
                Post origin = postRepository.findById(post.getOriginalPostId()).orElse(null);
                if (origin != null) {
                    PostResponseDto originDto = postMapper.toPostResponseDto(origin);
                    dto.setOriginalPost(originDto);
                }
            }

            dto.setLikeCount(postLikeRepository.countByPost(post));
            dto.setLiked(postLikeRepository.existsByUserAndPost(currentUser, post));

            postResponseDtos.add(dto);
        }
        return postResponseDtos;
    }

    @Transactional
    public PostResponseDto updatePost(Long postId, PostRequestDto dto, UserPrincipal userPrincipal ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        if (!post.getUser().getId().equals(userPrincipal.getUser().getId())) {
            throw new RuntimeException("Bạn không có quyền sửa bài viết này");
        }

        post.setContent(dto.getContent());

        if (dto.getMediaUrls() != null) {
            mediaRepository.deleteAllByPost(post);
            List<PostMedia> newMediaList = new ArrayList<>();
            for (String url : dto.getMediaUrls()) {
                PostMedia media = new PostMedia();
                media.setPost(post);
                media.setUrl(url);
                media.setType("image");
                mediaRepository.save(media);
                newMediaList.add(media);
            }
            post.setMedia(newMediaList);
        }

        Post updatedPost = postRepository.save(post);
        return postMapper.toPostResponseDto(updatedPost);
    }


    public void deletePost(Long postId, UserPrincipal userPrincipal) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        if (!post.getUser().getId().equals(userPrincipal.getUser().getId())) {
            throw new RuntimeException("Bạn không có quyền xóa bài viết này");
        }

        post.setDeleted(true);
        postRepository.save(post);
    }

    @Transactional
    public void toggleLike(Long postId, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

      
        Optional<PostLike> existingLike = postLikeRepository.findByUserAndPost(user, post);

        if (existingLike.isPresent()) {
            // Nếu like rồi -> Xóa like (Unlike)
            postLikeRepository.delete(existingLike.get());
        } else {
            // Nếu chưa like -> Tạo like mới
            PostLike newLike = new PostLike();
            newLike.setUser(user);
            newLike.setPost(post);
            postLikeRepository.save(newLike);

            // --- SỬA LỖI Ở ĐÂY ---
            // Gọi hàm thông báo Like (chỉ báo cho chủ bài viết)
            sendNotificationTypeLike(post, userPrincipal);
        }
    }

    @Transactional(readOnly = true)
    public PostResponseDto getPostById(Long id, UserPrincipal userPrincipal) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        PostResponseDto dto = postMapper.toPostResponseDto(post);

        if (post.getOriginalPostId() != null) {
            Post origin = postRepository.findById(post.getOriginalPostId()).orElse(null);
            if (origin != null) {
                dto.setOriginalPost(postMapper.toPostResponseDto(origin));
            }
        }

        dto.setLikeCount(postLikeRepository.countByPost(post));

        if (userPrincipal != null) {
            User currentUser = userPrincipal.getUser();
            dto.setLiked(postLikeRepository.existsByUserAndPost(currentUser, post));
        } else {
            dto.setLiked(false);
        }

        return dto;
    }

    @Transactional(readOnly = true)
    public List<PostResponseDto> getFriendPosts(UserPrincipal userPrincipal) {
        User currentUser = userPrincipal.getUser();

        // 1. Lấy danh sách bạn bè
        List<User> friends = friendRepository.getFriends(currentUser.getId());

        // 2. Thêm chính bản thân vào
        List<User> authorList = new ArrayList<>(friends);
        authorList.add(currentUser);

        // 3. Query DB
        List<Post> allPosts = postRepository.findByUserInAndDeletedFalseOrderByCreatedAtDesc(authorList);

        // 4. Lọc Privacy và Map DTO
        List<Post> visiblePosts = allPosts.stream()
                .filter(post -> {
                    if (post.getUser().getId().equals(currentUser.getId())) return true;
                    Integer privacy = post.getPrivacyMode();
                    return privacy == null || privacy == 1 || privacy == 2;
                })
                .collect(Collectors.toList());

        return mapToPostResponseDtoList(visiblePosts, currentUser);
    }

    // --- Helper Method: Tách logic Map DTO ---
    private List<PostResponseDto> mapToPostResponseDtoList(List<Post> posts, User currentUser) {
        List<PostResponseDto> postResponseDtos = new ArrayList<>();

        for (Post post : posts) {
            PostResponseDto dto = postMapper.toPostResponseDto(post);

            if (post.getOriginalPostId() != null) {
                Post origin = postRepository.findById(post.getOriginalPostId()).orElse(null);
                if (origin != null) {
                    PostResponseDto originDto = postMapper.toPostResponseDto(origin);
                    dto.setOriginalPost(originDto);
                }
            }

            dto.setLikeCount(postLikeRepository.countByPost(post));
            if (currentUser != null) {
                dto.setLiked(postLikeRepository.existsByUserAndPost(currentUser, post));
            } else {
                dto.setLiked(false);
            }

            postResponseDtos.add(dto);
        }
        return postResponseDtos;
    }

    // ========================================================================
    // --- PHẦN NOTIFICATION (Đã sửa lại cho đúng logic) ---
    // ========================================================================

    // 1. Hàm gửi thông báo LIKE (Chỉ gửi cho chủ bài viết)
    private void sendNotificationTypeLike(Post post, UserPrincipal actorPrincipal) {
        User actor = actorPrincipal.getUser();       // Người bấm like
        User owner = post.getUser();                 // Chủ bài viết

        // Nếu tự like bài mình thì KHÔNG gửi thông báo
        if (actor.getId().equals(owner.getId())) {
            return;
        }

        NotificationRequestDto requestDto = new NotificationRequestDto();
        requestDto.setOwnerId(owner.getId()); // Người nhận là chủ bài viết
        requestDto.setTitle("đã thích bài viết của bạn");
        requestDto.setNotificationType(NotificationType.LIKE);
        requestDto.setLinkedResourceId(post.getId());

        String contentPreview;
        if (post.getContent() != null && !post.getContent().trim().isEmpty()) {
            contentPreview = post.getContent().length() > 50
                    ? post.getContent().substring(0, 50) + "..."
                    : post.getContent();
        } else {
            contentPreview = "Nhấn để xem chi tiết bài viết.";
        }

        requestDto.setContent(contentPreview);
        notificationService.createNotification(requestDto, actorPrincipal);
    }

    // 2. Hàm gửi thông báo POST MỚI (Gửi cho tất cả bạn bè)
    private void sendNotificationToFriends(Post post, UserPrincipal actorPrincipal) {
        CompletableFuture.runAsync(() -> {
            try {
                User actor = actorPrincipal.getUser();

                List<User> friends = friendRepository.getFriends(actor.getId());

                for (User friend : friends) {
                    NotificationRequestDto requestDto = new NotificationRequestDto();
                    requestDto.setOwnerId(friend.getId()); // Người nhận là bạn bè
                    requestDto.setTitle("đã đăng một bài viết mới");

                    String contentPreview = "Hãy xem bài viết mới của " + actor.getUsername();
                    if (post.getContent() != null && !post.getContent().isEmpty()) {
                        contentPreview = post.getContent().length() > 50
                                ? post.getContent().substring(0, 50) + "..."
                                : post.getContent();
                    }
                    requestDto.setContent(contentPreview);

                    requestDto.setNotificationType(NotificationType.POST);
                    requestDto.setLinkedResourceId(post.getId());

                    notificationService.createNotification(requestDto, actorPrincipal);
                }
            } catch (Exception e) {
                System.err.println("Lỗi gửi thông báo bài viết mới: " + e.getMessage());
            }
        });
    }
}