package social_mate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Nhớ thêm Transactional
import social_mate.dto.request.CommentRequestDto;
import social_mate.dto.response.CommentResponseDto;
import social_mate.entity.User;
import social_mate.entity.UserPrincipal;
import social_mate.entity.post.Comment;
import social_mate.entity.post.CommentLike;
import social_mate.entity.post.Post;
import social_mate.mapper.CommentMapper;
import social_mate.repository.CommentLikeRepository;
import social_mate.repository.CommentRepository;
import social_mate.repository.PostRepository;
import social_mate.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentLikeRepository commentLikeRepository;
    @Transactional
    public CommentResponseDto createComment(Long postId, CommentRequestDto request, UserPrincipal userPrincipal) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại"));
        User user = userPrincipal.getUser();

        Comment comment = commentMapper.toEntity(request);
        comment.setPost(post);
        comment.setUser(user);

        // --- XỬ LÝ TRẢ LỜI BÌNH LUẬN ---
        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Bình luận cha không tồn tại"));
            comment.setParentComment(parent);
        }

        Comment savedComment = commentRepository.save(comment);

        // Trả về DTO
        CommentResponseDto response = commentMapper.toResponse(savedComment);
        response.setLikeCount(0);
        response.setLiked(false);
        if (savedComment.getParentComment() != null) {
            response.setParentId(savedComment.getParentComment().getId());
        }
        return response;
    }

    // --- LẤY DANH SÁCH BÌNH LUẬN (ĐỆ QUY ĐỂ LẤY REPLIES) ---
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getComments(Long postId, UserPrincipal userPrincipal) {
        User currentUser = (userPrincipal != null) ? userPrincipal.getUser() : null;

        // Chỉ lấy các comment gốc (parent = null)
        List<Comment> rootComments = commentRepository.findByPostIdAndParentCommentIsNullOrderByCreatedAtAsc(postId);

        return rootComments.stream()
                .map(comment -> mapToDtoWithReplies(comment, currentUser))
                .collect(Collectors.toList());
    }

    // Hàm helper để map và lấy replies đệ quy + thông tin Like
    private CommentResponseDto mapToDtoWithReplies(Comment comment, User currentUser) {
        CommentResponseDto dto = commentMapper.toResponse(comment);

        // Map thông tin Like
        dto.setLikeCount(commentLikeRepository.countByComment(comment));
        if (currentUser != null) {
            dto.setLiked(commentLikeRepository.existsByUserAndComment(currentUser, comment));
        }

        // Map Parent ID
        if (comment.getParentComment() != null) {
            dto.setParentId(comment.getParentComment().getId());
        }

        // Map Replies (Đệ quy)
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            List<CommentResponseDto> replyDtos = comment.getReplies().stream()
                    .map(reply -> mapToDtoWithReplies(reply, currentUser)) // Gọi đệ quy
                    .collect(Collectors.toList());
            dto.setReplies(replyDtos);
        } else {
            dto.setReplies(new ArrayList<>());
        }

        return dto;
    }
    // Hàm map đơn lẻ và set thông tin like
    private CommentResponseDto mapToResponse(Comment comment, User currentUser) {
        CommentResponseDto dto = commentMapper.toResponse(comment);

        // Set Parent ID
        if (comment.getParentComment() != null) {
            dto.setParentId(comment.getParentComment().getId());
        }

        // Set Like info
        dto.setLikeCount(commentLikeRepository.countByComment(comment));
        dto.setLiked(commentLikeRepository.existsByUserAndComment(currentUser, comment));

        return dto;
    }
    @Transactional
    public void toggleCommentLike(Long commentId, UserPrincipal userPrincipal) {
        User user = userPrincipal.getUser();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Bình luận không tồn tại"));

        Optional<CommentLike> existingLike = commentLikeRepository.findByUserAndComment(user, comment);

        if (existingLike.isPresent()) {
            commentLikeRepository.delete(existingLike.get());
        } else {
            CommentLike newLike = new CommentLike();
            newLike.setUser(user);
            newLike.setComment(comment);
            commentLikeRepository.save(newLike); // Like
        }
    }


    @Transactional
    public void deleteComment(Long commentId, UserPrincipal userPrincipal) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Bình luận không tồn tại"));

        Long currentUserId = userPrincipal.getUser().getId();
        Long commentOwnerId = comment.getUser().getId();
        Long postOwnerId = comment.getPost().getUser().getId();


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