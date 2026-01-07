package social_mate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Nhớ thêm Transactional
import social_mate.dto.request.CommentRequestDto;
import social_mate.dto.response.CommentResponseDto;
import social_mate.entity.User;
import social_mate.entity.UserPrincipal;
import social_mate.entity.post.Comment;
import social_mate.entity.post.Post;
import social_mate.mapper.CommentMapper;
import social_mate.repository.CommentRepository;
import social_mate.repository.PostRepository;
import social_mate.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Transactional
    public CommentResponseDto createComment(Long postId, CommentRequestDto request, UserPrincipal userPrincipal) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại"));


        User user = userPrincipal.getUser();

        Comment comment = commentMapper.toEntity(request);
        comment.setPost(post);
        comment.setUser(user);

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toResponse(savedComment);
    }

    public List<CommentResponseDto> getComments(Long postId, UserPrincipal userPrincipal) {

        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);

        return comments.stream()
                .map(commentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long commentId, UserPrincipal userPrincipal) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Bình luận không tồn tại"));

        Long currentUserId = userPrincipal.getUser().getId();
        Long commentOwnerId = comment.getUser().getId();
        Long postOwnerId = comment.getPost().getUser().getId();

        // Cho phép chủ comment HOẶC chủ bài viết xóa comment
        if (!currentUserId.equals(commentOwnerId) && !currentUserId.equals(postOwnerId)) {
            throw new RuntimeException("Bạn không có quyền xóa bình luận này");
        }

        commentRepository.delete(comment);
    }

    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentRequestDto commentRequest, UserPrincipal userPrincipal) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Bình luận không tồn tại"));

        Long currentUserId = userPrincipal.getUser().getId();
        Long commentOwnerId = comment.getUser().getId();

        if (!currentUserId.equals(commentOwnerId)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa bình luận này");
        }

        comment.setContent(commentRequest.getContent());

        Comment updatedComment = commentRepository.save(comment);
        return commentMapper.toResponse(updatedComment);
    }
}