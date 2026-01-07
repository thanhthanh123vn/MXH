package social_mate.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import social_mate.dto.request.MessageTextRequestDto;
import social_mate.dto.response.MessageResponseDto;
import social_mate.entity.Message;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    Message toMessage(MessageTextRequestDto messageTextRequestDto);

    @Mapping(source = "sender.username", target = "senderName")
    @Mapping(source = "sender.avatar", target = "senderAvatar")
    MessageResponseDto toMessageResponseDto(Message message);
}
