package social_mate.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import social_mate.dto.request.NotificationRequestDto;
import social_mate.dto.response.NotificationResponseDto;
import social_mate.entity.Notification;
import social_mate.entity.UserPrincipal;
import social_mate.repository.NotificationRepository;
import social_mate.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


    @GetMapping()
    private ResponseEntity<List<NotificationResponseDto>> getNotifications(@AuthenticationPrincipal UserPrincipal userPrincipal) {

        return ResponseEntity.status(200).body(notificationService.getNotificationsByOwner(userPrincipal));
    }
    @PostMapping()
    private ResponseEntity<NotificationResponseDto> createNotification(@RequestBody NotificationRequestDto notificationRequestDto, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        NotificationResponseDto responseDto = notificationService.createNotification(notificationRequestDto, userPrincipal);

        return ResponseEntity.status(201).body(responseDto);
    }

    //get number unread return Integer
    @GetMapping("/count-unread")
    public ResponseEntity<Long> countUnread(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        long count = notificationService.countUnread(userPrincipal);
        return ResponseEntity.ok(count);
    }
    //update =
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        notificationService.markAllAsRead(userPrincipal);
        return ResponseEntity.noContent().build();
    }

}
