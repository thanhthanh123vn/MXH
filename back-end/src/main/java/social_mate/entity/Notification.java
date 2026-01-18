package social_mate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import social_mate.entity.enums.NotificationType;

@Entity
@Table(name = "Notifications")
@Getter
@Setter
public class Notification extends AbstractEntity<Notification>{

    @Column(name="sender_id")
    private Long senderId;

    @Column(name="owner_id")
    private Long ownerId;

    @Column(name="title")
    private String title;

    @Column(name="content")
    private String content;

    @Column(name="is_read")
    private boolean isRead = false;

    @Column(name="notification_type", length = 20)
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Column(name="linked_resource_id")
    private Long linkedResourceId;



}
