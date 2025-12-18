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


    private List<MediaResponse> media;

    @Getter
    @Setter
    public static class MediaResponse {
        private String url;
        private String type;     // image, video, raw
        private String fileName; // Tên file hiển thị
    }
}