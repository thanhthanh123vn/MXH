package social_mate.dto.response;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
@Getter
@Setter
public class PostResponseDto {

    private String content;

    private List<String> mediaUrls;
}
