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

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostMediaRepository mediaRepository;
    private final PostMapper postMapper;



    private final CloudinaryService cloudinaryService;

    public PostResponseDto createPost(PostRequestDto dto, List<MultipartFile> files, UserPrincipal userPrincipal) {

        if (userPrincipal == null) {
            throw new IllegalStateException("UserPrincipal không được null");
        }

        User userCurrent = userPrincipal.getUser();

        Post newPost = postMapper.toPost(dto);
        newPost.setUser(userCurrent);

        // Mặc định các trường chưa có
        newPost.setDeleted(false);
        newPost.setPrivacyMode(1); // Ví dụ: 1 là Public

        Post savedPost = postRepository.save(newPost);

        // Xử lý upload file nếu có
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                try {
                    // Upload lên Cloudinary
                    Map result = cloudinaryService.uploadFile(file);

                    String url = (String) result.get("secure_url");
                    String resourceType = (String) result.get("resource_type"); // "image" hoặc "video"

                    // Lưu vào PostMedia
                    PostMedia media = new PostMedia();
                    media.setPost(savedPost);
                    media.setUrl(url);
                    media.setType(resourceType); // Lưu loại file (image/video)
                    mediaRepository.save(media);

                } catch (IOException e) {
                    throw new RuntimeException("Lỗi upload file: " + e.getMessage());
                }
            }
        }



        return postMapper.toPostResponseDto(savedPost);
    }
    public List<PostResponseDto> getAllPosts() {
        List<Post> posts = postRepository.findAll();
        List<PostResponseDto> postResponseDtos = new ArrayList<>();
        for (Post post : posts) {
            PostResponseDto postResponseDto = postMapper.toPostResponseDto(post);
            postResponseDtos.add(postResponseDto);

        }
        return postResponseDtos;
    }
    @Transactional
    public PostResponseDto updatePost(Long postId, PostRequestDto dto, UserPrincipal userPrincipal) {
        // 1. Tìm bài viết
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        // 2. Kiểm tra quyền chủ sở hữu
        if (!post.getUser().getId().equals(userPrincipal.getUser().getId())) {
            throw new RuntimeException("Bạn không có quyền sửa bài viết này");
        }

        // 3. Cập nhật nội dung text
        post.setContent(dto.getContent());

        // 4. Xử lý Media (Ảnh/Video)
        // Cách đơn giản nhất: Xóa hết cái cũ, lưu lại list cái mới từ DTO gửi lên
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
