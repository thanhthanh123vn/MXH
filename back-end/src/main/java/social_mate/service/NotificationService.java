package social_mate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social_mate.dto.request.NotificationRequestDto;
import social_mate.dto.response.NotificationResponseDto;
import social_mate.entity.Notification;
import social_mate.entity.User;
import social_mate.entity.UserPrincipal;
import social_mate.repository.NotificationRepository;
import social_mate.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Transactional
    public NotificationResponseDto createNotification(NotificationRequestDto requestDto, UserPrincipal userPrincipal) {
        // 1. Convert DTO -> Entity
        Notification notification = mapToEntity(requestDto, userPrincipal);

        // 2. Lưu xuống DB
        Notification savedNotification = notificationRepository.save(notification);

        // 3. Map sang Response
        NotificationResponseDto responseDto = mapToResponseDto(savedNotification, userPrincipal.getUser());

        // 4. (Optional) Gửi WebSocket Realtime
        // Giả sử bạn có endpoint socket là /topic/notifications/{userId}
         simpMessagingTemplate.convertAndSend("/topic/notifications/" + requestDto.getOwnerId(), responseDto);

        return responseDto;
    }

    public List<NotificationResponseDto> getNotificationsByOwner(UserPrincipal userPrincipal) {
        long currentUserId = userPrincipal.getUser().getId();

        List<Notification> notifications = notificationRepository.findTop10ByOwnerIdOrderByIdDesc(currentUserId);

        if (notifications.isEmpty()) {
            return List.of();
        }

        Set<Long> senderIds = notifications.stream()
                .map(Notification::getSenderId)
                .collect(Collectors.toSet());

        List<User> senders = userRepository.findAllById(senderIds);

        Map<Long, User> senderMap = senders.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return notifications.stream().map(notification -> {
            User sender = senderMap.getOrDefault(notification.getSenderId(), new User());
            return mapToResponseDto(notification, sender);
        }).collect(Collectors.toList());
    }

    public long countUnread(UserPrincipal userPrincipal) {
        return notificationRepository.countByOwnerIdAndIsReadFalse(userPrincipal.getUser().getId());
    }

    @Transactional
    public void markAllAsRead(UserPrincipal userPrincipal) {
        List<Notification> unreadList = notificationRepository.findByOwnerIdAndIsReadFalse(userPrincipal.getUser().getId());

        if (!unreadList.isEmpty()) {
            unreadList.forEach(n -> n.setRead(true));

            notificationRepository.saveAll(unreadList);
        }
    }

    // ================= PRIVATE METHODS (MAPPER) =================

    private Notification mapToEntity(NotificationRequestDto dto, UserPrincipal userPrincipal) {
        Notification notification = new Notification();
        notification.setSenderId(userPrincipal.getUser().getId());
        notification.setOwnerId(dto.getOwnerId());
        notification.setTitle(dto.getTitle());
        notification.setContent(dto.getContent());
        notification.setLinkedResourceId(dto.getLinkedResourceId());
        notification.setNotificationType(dto.getNotificationType());
        notification.setRead(false);

        return notification;
    }

    private NotificationResponseDto mapToResponseDto(Notification notification, User sender) {
        // Xử lý Null Safety cho User
        String senderName = (sender.getUsername() != null) ? sender.getUsername() : "Unknown User";
        String senderAvatar = (sender.getAvatar() != null) ? sender.getAvatar() : "";

        // Quan trọng: Thứ tự tham số phải KHỚP 100% với Constructor của NotificationResponseDto
        return new NotificationResponseDto(
                notification.getSenderId(),
                senderName,
                senderAvatar,
                notification.getTitle(),
                notification.getContent(),
                notification.getLinkedResourceId(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getNotificationType()
        );
    }


}