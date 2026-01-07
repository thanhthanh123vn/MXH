package social_mate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social_mate.entity.Message;
import social_mate.entity.enums.MessageStatus;
import social_mate.repository.MessageRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageStatusService {

    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Async // Chạy async để không block quá trình connect socket
    @Transactional
    public void markPendingMessagesAsReceived(Long userId) {
        List<Message> pendingMessages = messageRepository.findPendingMessagesForUser(userId);

        if (!pendingMessages.isEmpty()) {
            for (Message msg : pendingMessages) {
                msg.setMessageStatus(MessageStatus.RECEIVED);
            }
            messageRepository.saveAll(pendingMessages);

            pendingMessages.forEach(msg -> {
                simpMessagingTemplate.convertAndSend(
                        "/topic/conversation/" + msg.getConversation().getId() + "/status",
                        new MessageStatusUpdate(msg.getId(), MessageStatus.RECEIVED)
                );
            });
        }
    }

    @Transactional
    public void updateMessageStatusToSeen(Long conversationId, Long readerId) {
        List<Message> messages = messageRepository.findUnseenMessagesInConversation(conversationId, readerId);

        if (!messages.isEmpty()) {
            messages.forEach(m -> m.setMessageStatus(MessageStatus.SEEN));
            messageRepository.saveAll(messages);

            messages.forEach(msg -> {
                simpMessagingTemplate.convertAndSend(
                        "/topic/conversation/" + conversationId + "/status",
                        new MessageStatusUpdate(msg.getId(), MessageStatus.SEEN)
                );
            });
        }
    }

    // DTO class giữ nguyên
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class MessageStatusUpdate {
        private Long messageId;
        private MessageStatus status;

    }
}