package social_mate.dto.response;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
public class PostResponseDto {
    private Long id;

    private String content;

    private List<String> mediaUrls;
    private String backgroundColor;
    private Instant createdAt;
    private UserResponseDto user;
    private boolean deleted;

    private Long originalPostId;
    private List<MediaResponse> media;
    private PostResponseDto originalPost;
    private long likeCount; // Tổng số lượt like
    private boolean liked;
    @Getter
    @Setter
    public static class MediaResponse {
        private String url;
        private String type;     // image, video, raw
        private String fileName; // Tên file hiển thị
    }
}