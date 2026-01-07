package social_mate.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import social_mate.config.WebSocketEventListener;
import social_mate.dto.request.TypingRequestDto;

@Controller
@RequiredArgsConstructor
public class WebsocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketEventListener  webSocketEventListener;

    @MessageMapping("/conversation/{conversationId}/typing")
    public void handleTyping(@DestinationVariable Long conversationId, @Payload TypingRequestDto typingRequestDto) {

        messagingTemplate.convertAndSend("/topic/conversation/"+ conversationId+"/typing", typingRequestDto);

    }
    @MessageMapping("/conversation/{conversationId}/leave")
    public void handleLeaveConversation(@DestinationVariable Long conversationId, SimpMessageHeaderAccessor headerAccessor) {
        // 1. Lấy Session ID hiện tại của kết nối
        String sessionId = headerAccessor.getSessionId();

        // 2. Tra cứu UserId từ Map đã lưu lúc Connect
        Long userId = webSocketEventListener.getUserIdBySessionId(sessionId);

        // 3. Thực hiện logic remove
        if (userId != null) {
            webSocketEventListener.removeUserFromConversation(conversationId, userId);
        } else {
            System.out.println("not found user");
        }
    }
}
