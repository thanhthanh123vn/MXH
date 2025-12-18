package social_mate.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import social_mate.dto.request.PostRequestDto;
import social_mate.dto.response.PostResponseDto;
import social_mate.entity.User;
import social_mate.entity.UserPrincipal;
import social_mate.entity.post.Post;
import social_mate.entity.post.PostMedia;
import social_mate.mapper.PostMapper;
import social_mate.repository.PostMediaRepository;
import social_mate.repository.PostRepository;
import social_mate.repository.UserRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMediaRepository mediaRepository;
    private final PostMapper postMapper;



    private final CloudinaryService cloudinaryService;


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
        newPost.setPrivacyMode(1);

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

            // 2. Chờ tất cả upload xong và thu thập kết quả (Blocking main thread)
            List<PostMedia> mediaEntities = futures.stream()
                    .map(CompletableFuture::join) // join() sẽ ném unchecked exception nếu có lỗi
                    .collect(Collectors.toList());

            // 3. Lưu Batch vào Database ở luồng chính (Đảm bảo Transaction)
            if (!mediaEntities.isEmpty()) {
                savedMediaList = mediaRepository.saveAll(mediaEntities);
            }
        }

        savedPost.setMedia(savedMediaList);
        return postMapper.toPostResponseDto(savedPost);
    }
    public List<PostResponseDto> getMyPosts(UserPrincipal userPrincipal) {

        // 1. Lấy thông tin user hiện tại từ UserPrincipal
        User currentUser = userPrincipal.getUser();

        // 2. Gọi Repository để lấy bài viết của user
        List<Post> posts = postRepository.findAllByUserOrderByCreatedAtDesc(currentUser);

        // 3. Convert sang DTO
        List<PostResponseDto> postResponseDtos = new ArrayList<>();

        for (Post post : posts) {

                PostResponseDto postResponseDto = postMapper.toPostResponseDto(post);
                postResponseDtos.add(postResponseDto);

        }

        // Gợi ý: Có thể dùng Stream API cho ngắn gọn hơn:
        // return posts.stream().map(postMapper::toPostResponseDto).toList();

        return postResponseDtos;
    }
    @Transactional
    public PostResponseDto updatePost(Long postId, PostRequestDto dto, UserPrincipal userPrincipal ) {
        // 1. Tìm bài viết
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        // 2. Kiểm tra quyền chủ sở hữu
        if (!post.getUser().getId().equals(userPrincipal.getUser().getId())) {
            throw new RuntimeException("Bạn không có quyền sửa bài viết này");
        }

        // 3. Cập nhật nội dung text
        post.setContent(dto.getContent());


        if (dto.getMediaUrls() != null) {
            // Xóa media cũ
            mediaRepository.deleteAllByPost(post);

            // Lưu media mới
            List<PostMedia> newMediaList = new ArrayList<>();
            for (String url : dto.getMediaUrls()) {
                PostMedia media = new PostMedia();
                media.setPost(post);
                media.setUrl(url);


                media.setType("image");
                mediaRepository.save(media);
                newMediaList.add(media);
            }
            // Cập nhật lại list media cho object post để mapper hiển thị đúng
            post.setMedia(newMediaList);
        }

        // 5. Lưu và trả về
        Post updatedPost = postRepository.save(post);
        return postMapper.toPostResponseDto(updatedPost);
    }

    // --- CHỨC NĂNG XÓA BÀI VIẾT (SOFT DELETE) ---

    public void deletePost(Long postId, UserPrincipal userPrincipal) {
        // 1. Tìm bài viết
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        // 2. Kiểm tra quyền chủ sở hữu
        if (!post.getUser().getId().equals(userPrincipal.getUser().getId())) {
            throw new RuntimeException("Bạn không có quyền xóa bài viết này");
        }

        // 3. Thực hiện xóa mềm (Soft Delete)
        post.setDeleted(true);
        postRepository.save(post);
    }


}