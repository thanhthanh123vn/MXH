package social_mate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import social_mate.entity.enums.ConversationType;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;

@Entity
@Table(name = "conversations")
@Getter
@Setter
public class Conversation extends AbstractEntity<Conversation> {

	@Column(name = "conversation_type")
	@Enumerated(EnumType.STRING)
	private ConversationType conversationType;
	@Column(name = "last_message")
	private String lastMessage;
	
	@Column(name= "sender_id_last_message")
	private Long senderIdLastMessage;

    @Column(name = "sender_name_last_message")
    private String senderNameLastMessage;

	//add attribute "messages_unseen"
	
	@Column(name = "conversation_name")
	private String conversationName;
	
	@Column(name = "conversation_avatar")
	private String conversationAvatar;
	

	@OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Message> messages = new ArrayList<>();

	@OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<Participant> participants = new ArrayList<>();



}
