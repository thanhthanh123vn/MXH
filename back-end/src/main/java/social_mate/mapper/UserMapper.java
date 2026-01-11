package social_mate.mapper;

import org.mapstruct.Mapper;

import org.mapstruct.Mapping;
import social_mate.dto.request.RegisterRequestDto;
import social_mate.dto.response.ProfileResponseDto;
import social_mate.dto.response.UserResponseDto;
import social_mate.dto.response.UserSearchResponseDto;
import social_mate.entity.User;
import social_mate.entity.UserDetail;


@Mapper(componentModel = "spring")
public interface UserMapper {
	
	
	 UserResponseDto toUserResponseDto(User user);
	 
	 User toUser(RegisterRequestDto registerRequestDto);

	@Mapping(target = "friendStatus", ignore = true)
	UserSearchResponseDto toUserSearchResponseDto(User user);

	@Mapping(source = "user.id", target = "id")
	@Mapping(source = "user.username", target = "username")
	@Mapping(source = "user.email", target = "email")
	@Mapping(source = "user.avatar", target = "avatar")
	@Mapping(source = "detail.bio", target = "bio")
	@Mapping(source = "detail.jobTitle", target = "jobTitle")
	@Mapping(source = "detail.coverPhoto", target = "coverPhoto")
	@Mapping(source = "detail.address", target = "address")
	@Mapping(target = "totalFriends", ignore = true)
	@Mapping(target = "totalPosts", ignore = true)
	ProfileResponseDto toProfileResponse(User user, UserDetail detail);
}
