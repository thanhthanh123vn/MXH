package social_mate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDto {
    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private UserResponseDto user;
    private Long parentId;
    private long likeCount;
    private boolean liked; // User hiện tại đã like chưa
    private List<CommentResponseDto> replies;
}