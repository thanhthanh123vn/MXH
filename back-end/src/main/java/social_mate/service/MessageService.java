package social_mate.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import social_mate.config.WebSocketEventListener;
import social_mate.dto.request.MessageFileRequestDto;
import social_mate.dto.request.MessageTextRequestDto;
import social_mate.dto.response.ConversationResponseDto;
import social_mate.dto.response.MessageResponseDto;
import social_mate.entity.*;
import social_mate.entity.enums.MessageStatus;
import social_mate.entity.enums.MessageType;
import social_mate.mapper.MessageMapper;
import social_mate.repository.ConversationRepository;
import social_mate.repository.MessageRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final Cloudinary cloudinary;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MessageMapper messageMapper;
    private final WebSocketEventListener webSocketEventListener;
    private final ConversationService conversationService;


    @Transactional
    public void createMessageText(MessageTextRequestDto messageTextRequestDto, UserPrincipal userPrincipal) {
        User sender = userPrincipal.getUser();
        Conversation conversation = getValidConversation(messageTextRequestDto.getConversationId(), sender.getId());
        Message message = new Message();
        message.setSender(sender);
        message.setConversation(conversation);
        message.setMessageType(MessageType.TEXT);
        message.setContent(messageTextRequestDto.getContent());
        message.setMessageStatus(MessageStatus.SENT);
        Message savedMessage= messageRepository.save(message);

        //set lastMessage, senderIdLastMessage and senderNameLastMessage for conversation.
        updateConversationLastMessage(conversation, sender, messageTextRequestDto.getContent());

        sendToWebSocket(savedMessage, conversation);

        updateMessageStatusBasedOnRecipients(savedMessage, conversation, sender.getId());

    }

    @Transactional
    public void createMessageFile(MessageFileRequestDto messageFileRequestDto, UserPrincipal userPrincipal) {
        User sender = userPrincipal.getUser();
        Conversation conversation = getValidConversation(messageFileRequestDto.getConversationId(), sender.getId());
        try {

            MultipartFile file = messageFileRequestDto.getFile();

            String fileName = file.getOriginalFilename() + "_" + UUID.randomUUID().toString();
            Map params = ObjectUtils.asMap(
                    "folder", "messages",
                    "public_id", fileName,
                    "resource_type", "auto"
            );
            Map result = cloudinary.uploader().upload(file.getBytes(), params);

            Message message = new Message();
            message.setSender(sender);
            message.setConversation(conversation);
            message.setMessageType(MessageType.FILE);
            message.setFileName(file.getOriginalFilename());
            message.setFileUrl((String) result.get("url"));
            message.setMessageStatus(MessageStatus.SENT);
            Message savedMessage=messageRepository.save(message);

            //set lastMessage, senderIdLastMessage and senderNameLastMessage for conversation.

            updateConversationLastMessage(conversation, sender, file.getOriginalFilename());

            sendToWebSocket(savedMessage, conversation);
            updateMessageStatusBasedOnRecipients(savedMessage, conversation, sender.getId());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendToWebSocket(Message message, Conversation conversation) {
        MessageResponseDto messageResponseDto=messageMapper.toMessageResponseDto(message);
        simpMessagingTemplate.convertAndSend("/topic/conversation/"+conversation.getId(),messageResponseDto);

        List<User> participants=conversation.getParticipants().stream().map(Participant::getUser).collect(Collectors.toList());

        for(User user:participants){
            ConversationResponseDto conversationResponseDto=conversationService.mapToConversationResponseDto(conversation,user);

            simpMessagingTemplate.convertAndSend("/topic/user/"+user.getId()+"/conversation",conversationResponseDto);
        }

    }

    private void updateConversationLastMessage(Conversation conversation, User sender, String lastMessage) {
        conversation.setLastMessage(lastMessage);
        conversation.setSenderIdLastMessage(sender.getId());
        conversation.setSenderNameLastMessage(sender.getUsername());
        conversationRepository.save(conversation);

    }

    private Conversation getValidConversation(Long conversationId, Long userId) {

        return conversationRepository.findByIdAndUserId(conversationId, userId).orElseThrow(() -> new RuntimeException("conversation invalid"));
    }

    private void updateMessageStatusBasedOnRecipients(Message message, Conversation conversation, Long senderId) {
        // Get all participants except sender
        List<Long> recipientIds = conversation.getParticipants().stream()
                .map(p -> p.getUser().getId())
                .filter(id -> !id.equals(senderId))
                .collect(Collectors.toList());

        // Check if any recipient is online
        boolean anyOnline = recipientIds.stream()
                .anyMatch(webSocketEventListener::isUserOnline);

        if (anyOnline) {
            message.setMessageStatus(MessageStatus.RECEIVED);
            messageRepository.save(message);

            // Notify about status update
            simpMessagingTemplate.convertAndSend(
                    "/topic/conversation/" + conversation.getId() + "/status",
                    new MessageStatusService.MessageStatusUpdate(message.getId(), MessageStatus.RECEIVED)
            );
        }

        // Check if any recipient is viewing the conversation
        boolean anyViewing = recipientIds.stream()
                .anyMatch(id -> webSocketEventListener.isUserInConversation(conversation.getId(), id));

        if (anyViewing) {
            message.setMessageStatus(MessageStatus.SEEN);
            messageRepository.save(message);

            simpMessagingTemplate.convertAndSend(
                    "/topic/conversation/" + conversation.getId() + "/status",
                    new MessageStatusService.MessageStatusUpdate(message.getId(), MessageStatus.SEEN)
            );
        }
    }
}
