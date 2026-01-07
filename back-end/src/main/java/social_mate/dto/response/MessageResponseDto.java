package social_mate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import social_mate.entity.User;
import social_mate.entity.enums.MessageStatus;
import social_mate.entity.enums.MessageType;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class MessageResponseDto {

    private Long id;
    private MessageType messageType;
    private String content;
    private String fileName;
    private String fileUrl;
    private MessageStatus messageStatus;
    private String senderName;
    private String senderAvatar;
    private Instant createdAt;
}
