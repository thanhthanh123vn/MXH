package social_mate.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import social_mate.entity.enums.MessageStatus;
import social_mate.entity.enums.MessageType;

@Entity
@Table(name = "messages")
@Setter
@Getter
public class Message extends AbstractEntity<Message> {

	@Column(name = "message_type")
	@Enumerated(EnumType.STRING)
	private MessageType messageType;

	@Column(name = "content")
	private String content;

	@Column(name = "file_name")
	private String fileName;
	@Column(name = "file_url")
	private String fileUrl;

	@Column(name = "status_message")
	@Enumerated(EnumType.STRING)
	private MessageStatus  messageStatus;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "conversation_id", nullable = false)
	private Conversation conversation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sender_id", nullable = false)
	private User sender;
	
}
