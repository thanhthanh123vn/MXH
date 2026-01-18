package social_mate.config;


import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import org.springframework.stereotype.Component;

import org.springframework.web.socket.messaging.SessionConnectEvent;

import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import social_mate.dto.response.UserStatusResponseDto;
import social_mate.service.MessageStatusService;

import java.util.HashSet;
import java.util.List;

import java.util.Map;

import java.util.Set;

import java.util.concurrent.ConcurrentHashMap;


@Component

@RequiredArgsConstructor

@Slf4j

public class WebSocketEventListener {


    private final MessageStatusService messageStatusService;


// Maps

    private final Map<Long, Set<String>> onlineUsers = new ConcurrentHashMap<>();

    private final Map<Long, Set<Long>> conversationSubscribers = new ConcurrentHashMap<>();

    private final Map<String, Long> sessionToUser = new ConcurrentHashMap<>();
    private final SimpMessageSendingOperations messagingTemplate;


    @EventListener

    public void handleWebSocketConnectListener(SessionConnectEvent event) {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = headerAccessor.getSessionId();


        Long userId = getUserIdFromHeaders(headerAccessor);


        if (userId != null) {

            sessionToUser.put(sessionId, userId);

            onlineUsers.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);


            log.info("USER ONLINE: ID={}, Session={}", userId, sessionId);


            messageStatusService.markPendingMessagesAsReceived(userId);

            messagingTemplate.convertAndSend("/topic/user-status", new UserStatusResponseDto(userId, "ONLINE"));
        } else {

            log.warn("Connection rejected: Could not determine UserId for session {}", sessionId);

        }

    }


    @EventListener

    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = headerAccessor.getSessionId();

        Long userId = sessionToUser.remove(sessionId);


        if (userId != null) {

            Set<String> sessions = onlineUsers.get(userId);

            if (sessions != null) {

                sessions.remove(sessionId);

                if (sessions.isEmpty()) {

                    onlineUsers.remove(userId);

                    log.info("USER OFFLINE: ID={}", userId);
                    messagingTemplate.convertAndSend("/topic/user-status", new UserStatusResponseDto(userId, "OFFLINE"));
                }

            }

            conversationSubscribers.values().forEach(sub -> sub.remove(userId));

        }

    }


    @EventListener

    public void handleSubscribeEvent(SessionSubscribeEvent event) {

        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String destination = headerAccessor.getDestination();

        String sessionId = headerAccessor.getSessionId();

        Long userId = sessionToUser.get(sessionId);


        if (userId != null && destination != null && destination.startsWith("/topic/conversation/")) {

            String idPart = destination.replace("/topic/conversation/", "");

            if (!idPart.contains("/")) {

                try {

                    Long conversationId = Long.parseLong(idPart);

                    conversationSubscribers.computeIfAbsent(conversationId, k -> ConcurrentHashMap.newKeySet()).add(userId);


                    log.info("User {} is viewing conversation {}", userId, conversationId);


// User đang xem -> Đánh dấu là SEEN ngay lập tức

                    messageStatusService.updateMessageStatusToSeen(conversationId, userId);

                } catch (NumberFormatException e) {

// ignore

                }

            }

        }

    }

    public void removeUserFromConversation(Long conversationId, Long userId) {

        Set<Long> subscribers = conversationSubscribers.get(conversationId);

        if (subscribers != null) {

            boolean removed = subscribers.remove(userId);

            if (removed) {

                log.info("User {} left conversation {}", userId, conversationId);

            }

// Dọn dẹp map nếu rỗng để tiết kiệm bộ nhớ

            if (subscribers.isEmpty()) {

                conversationSubscribers.remove(conversationId);

            }

        }

    }


    public boolean isUserOnline(Long userId) {

        boolean isOnline = onlineUsers.containsKey(userId);

        log.debug("Check isUserOnline ID={}: {}", userId, isOnline);

        return isOnline;

    }


    public boolean isUserInConversation(Long conversationId, Long userId) {

        Set<Long> subscribers = conversationSubscribers.get(conversationId);

        return subscribers != null && subscribers.contains(userId);

    }


    private Long getUserIdFromHeaders(StompHeaderAccessor headerAccessor) {


        List<String> userIdHeaders = headerAccessor.getNativeHeader("userId");

        if (userIdHeaders != null && !userIdHeaders.isEmpty()) {

            try {

                return Long.parseLong(userIdHeaders.get(0));

            } catch (NumberFormatException e) {

                log.error("Invalid userId in header: {}", userIdHeaders.get(0));

            }

        }


        return null;

    }

    public Long getUserIdBySessionId(String sessionId) {

        return sessionToUser.get(sessionId);

    }

    public Set<Long> getOnlineUserIds() {
        return new HashSet<>(onlineUsers.keySet());
    }
}