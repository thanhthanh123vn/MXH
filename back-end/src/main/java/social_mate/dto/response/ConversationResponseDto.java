package social_mate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import social_mate.entity.enums.ConversationType;

import java.time.Instant;
@Getter
@Setter
@AllArgsConstructor
public class ConversationResponseDto {
	
	private Long id;
	private String lastMessage;
	private String senderNameLastMessage;
    private String conversationName;
    private String conversationAvatar;
	private Instant updated_at;
	private Long messages_unseen;
	private Long senderIdLastMessage;
	private ConversationType  conversationType;
}
