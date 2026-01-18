package social_mate.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import social_mate.entity.enums.NotificationType;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequestDto {
    private Long ownerId;
    private String title;
    private String content;
    private Long linkedResourceId;
    private NotificationType notificationType;
    private boolean isRead;

}