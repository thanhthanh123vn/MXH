package social_mate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class
CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper; // Inject Mapper vào đây
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public CommentResponseDto createComment(Long postId, CommentRequestDto request, UserPrincipal userPrincipal) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại"));
        User user = userRepository.findById(userPrincipal.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Comment comment = commentMapper.toEntity(request);

        comment.setPost(post);
        comment.setUser(user);

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toResponse(savedComment);
    }

    public List<CommentResponseDto> getComments(Long postId, UserPrincipal userPrincipal) {

        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        if (userPrincipal == null) {
            throw new IllegalStateException("UserPrincipal không được null");
        }
        User userCurrent = userPrincipal.getUser();

        return comments.stream()
                .map(commentMapper::toResponse)
                .collect(Collectors.toList());
    }

    public boolean deleteComment(Long commentId, UserPrincipal userPrincipal) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Bình luận không tồn tại"));

        Long currentUserId = userPrincipal.getUser().getId();
        Long commentOwnerId = comment.getUser().getId();

        Long postOwnerId = comment.getPost().getUser().getId();

        if (!currentUserId.equals(commentOwnerId) && !currentUserId.equals(postOwnerId)) {
            throw new RuntimeException("Bạn không có quyền xóa bình luận này");
        }

        // 4. Thực hiện xóa
        commentRepository.delete(comment);
        return true;
    }

    public CommentResponseDto updateComment(Long commentId, CommentRequestDto commentRequest, UserPrincipal userPrincipal) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Bình luận không tồn tại"));

        if (userPrincipal == null) {
            throw new IllegalStateException("User chưa đăng nhập");
        }


        Long currentUserId = userPrincipal.getUser().getId();
        Long commentOwnerId = comment.getUser().getId();

        if (!currentUserId.equals(commentOwnerId)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa bình luận này");
        }

        comment.setContent(commentRequest.getContent());

        comment.setCreatedAt(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);

        return commentMapper.toResponse(updatedComment);
    }
}