package social_mate.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import social_mate.dto.response.FriendResponseDto;
import social_mate.entity.Friend;
import social_mate.entity.User;

@Mapper(componentModel = "spring")
public interface FriendMapper {
    @Mapping(target = "name", source = "username")
    @Mapping(target = "avatar", source = "avatar")
    FriendResponseDto toFriendResponse(User user);

    Friend toFriend(Friend friend);
}
