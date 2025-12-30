package social_mate.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import social_mate.dto.request.CommentRequestDto;
import social_mate.dto.response.CommentResponseDto;
import social_mate.dto.response.UserResponseDto;
import social_mate.entity.post.Comment;
import social_mate.entity.User;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "user", target = "user")
    CommentResponseDto toResponse(Comment comment);

    UserResponseDto toUserDto(User user);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "post", ignore = true)
    Comment toEntity(CommentRequestDto request);


}