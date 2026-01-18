package social_mate.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import social_mate.dto.request.PostRequestDto;
import social_mate.dto.response.PostResponseDto;
import social_mate.entity.UserPrincipal;
import social_mate.service.PostService;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final ObjectMapper objectMapper; // 1. Inject ObjectMapper


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseDto> createPost(
            @RequestPart("post") String postJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal UserPrincipal userPrincipal) throws JsonProcessingException {

        // 3. Tự convert String sang Object
        PostRequestDto postRequestDto = objectMapper.readValue(postJson, PostRequestDto.class);

        // Gọi service như bình thường
        PostResponseDto postResponseDto = postService.createPost(postRequestDto, files, userPrincipal);

        return ResponseEntity.status(201).body(postResponseDto);
    }

    @GetMapping()
    public ResponseEntity<List<PostResponseDto>> getMyPosts(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        // Gọi hàm getMyPosts vừa viết ở Service
        List<PostResponseDto> posts = postService.getMyPosts(userPrincipal);

        return ResponseEntity.ok(posts);
    }
    @PutMapping("/{id}")
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable Long id,
            @RequestBody PostRequestDto postRequestDto,
            @AuthenticationPrincipal UserPrincipal userPrincipal)  {

        PostResponseDto response = postService.updatePost(id, postRequestDto, userPrincipal);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        postService.deletePost(id, userPrincipal);
        // Trả về JSON thay vì String thuần túy
        return ResponseEntity.ok(Collections.singletonMap("message", "Xóa bài viết thành công"));
    }
    @PostMapping("/{postId}/like")
    public ResponseEntity<?> toggleLike(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        postService.toggleLike(postId, userPrincipal);

        // Trả về map đơn giản hoặc gọi lại service để lấy số like mới nhất nếu muốn update realtime UI
        return ResponseEntity.ok(Collections.singletonMap("message", "Success"));
    }

    // --- Lấy chi tiết 1 bài viết ---

    @GetMapping("/feed")
    public ResponseEntity<List<PostResponseDto>> getNewsFeed(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<PostResponseDto> posts = postService.getFriendPosts(userPrincipal);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponseDto> getPostById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        PostResponseDto post = postService.getPostById(id, userPrincipal);
        return ResponseEntity.ok(post);
    }


}