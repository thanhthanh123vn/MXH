package social_mate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class ProfilePostResponseDto {
    private Long id;

    private String backgroundColor;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;
    private List<String> mediaUrls;
    private int totalLikes;
    private UserResponseDto user;
    private ProfilePostResponseDto originalPost;
}
