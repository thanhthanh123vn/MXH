package social_mate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import social_mate.entity.enums.NotificationType;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
public class NotificationResponseDto {
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String title;
    private String content;
    private Long linkedResourceId;
    private boolean isRead;
    private Instant createdAt;
    private NotificationType notificationType;
}
