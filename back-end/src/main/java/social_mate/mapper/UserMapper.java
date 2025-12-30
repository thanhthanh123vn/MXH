package social_mate.mapper;

import org.mapstruct.Mapper;

import org.mapstruct.Mapping;
import social_mate.dto.request.RegisterRequestDto;
import social_mate.dto.response.UserResponseDto;
import social_mate.dto.response.UserSearchResponseDto;
import social_mate.entity.User;



@Mapper(componentModel = "spring")
public interface UserMapper {
	
	
	 UserResponseDto toUserResponseDto(User user);
	 
	 User toUser(RegisterRequestDto registerRequestDto);

	@Mapping(target = "friendStatus", ignore = true)
	UserSearchResponseDto toUserSearchResponseDto(User user);

}
