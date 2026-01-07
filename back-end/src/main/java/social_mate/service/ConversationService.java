package social_mate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social_mate.dto.request.ConversationDirectRequestDto;
import social_mate.dto.request.ConversationGroupRequestDto;
import social_mate.dto.response.ConversationResponseDto;
import social_mate.dto.response.MessageResponseDto;
import social_mate.entity.*;
import social_mate.entity.enums.ConversationType;
import social_mate.entity.enums.ParticipantRole;
import social_mate.mapper.MessageMapper;
import social_mate.repository.ConversationRepository;
import social_mate.repository.MessageRepository;
import social_mate.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;

    public List<MessageResponseDto> getConversationById(Long conversationId, Integer page, Integer size, UserPrincipal userPrincipal) {

        conversationRepository.findByIdAndUserId(conversationId, userPrincipal.getUser().getId()).orElseThrow(() -> new RuntimeException("conversation not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        List<Message> messages = messageRepository.findByConversationId(conversationId, pageable);

        return messages.stream().map(messageMapper::toMessageResponseDto).collect(Collectors.toList());

    }
    public List<ConversationResponseDto> getConversationsByUserId(UserPrincipal userPrincipal) {
        User currentUser = userPrincipal.getUser();
        Long userId = currentUser.getId();

        List<Conversation> conversations = conversationRepository.findConversationsByUserId(userId);

        return conversations.stream().map(conversation -> mapToConversationResponseDto(conversation, currentUser)).collect(Collectors.toList());

    }

    public ConversationResponseDto mapToConversationResponseDto(Conversation conversation, User currentUser) {

        String conversationName;
        String conversationAvatar;

        if (ConversationType.GROUP.equals(conversation.getConversationType())) {
            conversationName = conversation.getConversationName();
            conversationAvatar = conversation.getConversationAvatar();
        } else {
            User partner = conversation.getParticipants().stream().map(Participant::getUser).filter(user -> !user.getId().equals(currentUser.getId())).findFirst().orElse(null);

            if (partner != null) {

                conversationName = partner.getUsername();
                conversationAvatar = partner.getAvatar();
            } else {
                conversationName = "unknown";
                conversationAvatar = null;
            }

        }

        String senderNameLastMessage = conversation.getSenderNameLastMessage();
        if (currentUser.getId().equals(conversation.getSenderIdLastMessage())) {
            senderNameLastMessage = "you";
        }

        Long unSeenCount = messageRepository.countUnseenMessages(conversation.getId(), currentUser.getId());

        return new ConversationResponseDto(
                conversation.getId(),
                conversation.getLastMessage(),
                senderNameLastMessage,
                conversationName,
                conversationAvatar,
                conversation.getUpdatedAt(),
                unSeenCount
        );
    }

    @Transactional
    public void createConversationGroup(ConversationGroupRequestDto conversationGroupRequestDto, UserPrincipal userPrincipal) {
        User currentUser = userPrincipal.getUser();

        Set<Long> uniqueMemberIds = new HashSet<>(conversationGroupRequestDto.getMemberIds());
        List<User> members = userRepository.findAllById(uniqueMemberIds);

        Conversation conversation = new Conversation();
        conversation.setConversationName(conversationGroupRequestDto.getConversationName());
        conversation.setConversationAvatar("https://cdn-icons-png.flaticon.com/512/5857/5857278.png");
        conversation.setConversationType(ConversationType.GROUP);

        List<Participant> participants = new ArrayList<>();

        Participant adminParticipant = new Participant();
        adminParticipant.setUser(currentUser);
        adminParticipant.setConversation(conversation);
        adminParticipant.setParticipantRole(ParticipantRole.ADMIN);
        participants.add(adminParticipant);

        for (User member : members) {
            Participant participant = new Participant();
            participant.setUser(member);
            participant.setConversation(conversation);
            participant.setParticipantRole(ParticipantRole.MEMBER);
            participants.add(participant);
        }

        conversation.setParticipants(participants);
        conversationRepository.save(conversation);
    }

    @Transactional
    public Long createConversationDirect(ConversationDirectRequestDto conversationDirectRequestDto, UserPrincipal userPrincipal) {
        User currentUser = userPrincipal.getUser();

        User partner = userRepository.findById(conversationDirectRequestDto.getPartnerId()).orElseThrow(() -> new RuntimeException("partner not found"));

        // 1. Tạo Conversation
        Conversation conversation = new Conversation();
        conversation.setConversationType(ConversationType.DIRECT);
        // Direct thì Name và Avatar để null
        conversation.setConversationName(null);
        conversation.setConversationAvatar(null);

        // 2. Tạo Participants (Role NONE)
        List<Participant> participants = new ArrayList<>();

        // Current User
        Participant p1 = new Participant();
        p1.setUser(currentUser);
        p1.setConversation(conversation);
        p1.setParticipantRole(ParticipantRole.NONE);
        participants.add(p1);

        // Partner
        Participant p2 = new Participant();
        p2.setUser(partner);
        p2.setConversation(conversation);
        p2.setParticipantRole(ParticipantRole.NONE);
        participants.add(p2);

        conversation.setParticipants(participants);
        // 3. Lưu
       Conversation conversationSaved=   conversationRepository.save(conversation);

        return conversationSaved.getId();
    }

    public Long findExistingDirectConversationId(UserPrincipal userPrincipal, Long partnerId) {
        Long currentUserId = userPrincipal.getUser().getId();
        return conversationRepository.findExistingDirectConversation(currentUserId, partnerId)
                .map(Conversation::getId)
                .orElse(null);
    }

}
