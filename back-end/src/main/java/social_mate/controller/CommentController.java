package social_mate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import social_mate.dto.request.CommentRequestDto;
import social_mate.dto.response.CommentResponseDto;
import social_mate.entity.UserPrincipal;
import social_mate.service.CommentService;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {


    private final CommentService commentService;


    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getComments(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<CommentResponseDto> comments = commentService.getComments(postId, userPrincipal);
        return ResponseEntity.ok(comments);
    }


    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Long postId,
            @RequestBody @Valid CommentRequestDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        CommentResponseDto newComment = commentService.createComment(postId, request, userPrincipal);
        return ResponseEntity.status(HttpStatus.CREATED).body(newComment);
    }


    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @RequestBody @Valid CommentRequestDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        CommentResponseDto updatedComment = commentService.updateComment(commentId, request, userPrincipal);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        commentService.deleteComment(commentId, userPrincipal);
        return ResponseEntity.ok(Collections.singletonMap("message", "Xóa bình luận thành công"));
    }
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<?> toggleLike(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        commentService.toggleCommentLike(commentId, userPrincipal);
        return ResponseEntity.ok(Collections.singletonMap("message", "Thao tác thành công"));
    }
}