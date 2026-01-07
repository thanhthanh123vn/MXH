package social_mate.mapper;

import org.mapstruct.Mapper;
import social_mate.dto.response.ConversationResponseDto;
import social_mate.entity.Conversation;


@Mapper(componentModel = "spring")
public interface ConversationMapper {
	
	ConversationResponseDto toConversationResponseDto(Conversation conversation);
}
