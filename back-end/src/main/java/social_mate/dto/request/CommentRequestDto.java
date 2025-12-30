package social_mate.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentRequestDto {

    private Long id;
    private Long postId;
    private Long userId;
    @NotBlank(message = "Nội dung bình luận không được để trống")
    private String content;

}