package social_mate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import social_mate.dto.request.PostRequestDto;
import social_mate.dto.response.PostResponseDto;
import social_mate.entity.UserPrincipal;
import social_mate.service.PostService;

@Controller
@RequestMapping("/api/v1/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    @PostMapping
    public ResponseEntity<PostResponseDto> createPost(PostRequestDto postRequestDto, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        PostResponseDto postResponseDto = postService.createPost(postRequestDto, userPrincipal);
        return ResponseEntity.status(201).body(postResponseDto);
    }
}
