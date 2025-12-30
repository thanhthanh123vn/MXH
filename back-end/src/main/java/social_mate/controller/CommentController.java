package social_mate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import social_mate.dto.request.CommentRequestDto;
import social_mate.dto.request.PostRequestDto;
import social_mate.dto.response.CommentResponseDto;
import social_mate.entity.User;
import social_mate.entity.UserPrincipal;
import social_mate.entity.post.Comment;
import social_mate.mapper.CommentMapper;
import social_mate.repository.CommentRepository;
import social_mate.service.CommentService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {
    @Autowired
    private final CommentService commentService;
    private final CommentMapper commentMapper; // Inject Mapper vào đây


    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getComments(@PathVariable Long postId, @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<CommentResponseDto> comments = commentService.getComments(postId, userPrincipal);

        return ResponseEntity.ok(comments);
    }

    @PostMapping("/{postId}/comment")
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Long postId,
            @RequestBody @Valid CommentRequestDto request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {

        CommentResponseDto newComment = commentService.createComment(postId, request, userPrincipal);

        return ResponseEntity.status(HttpStatus.CREATED).body(newComment);
    }
}