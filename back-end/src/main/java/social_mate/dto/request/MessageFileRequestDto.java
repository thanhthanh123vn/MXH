package social_mate.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
public class MessageFileRequestDto {

    private Long conversationId;
    private MultipartFile file;
}
