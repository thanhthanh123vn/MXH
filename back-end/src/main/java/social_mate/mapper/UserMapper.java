package social_mate.mapper;

import org.mapstruct.Mapper;

import social_mate.dto.request.RegisterRequestDto;
import social_mate.dto.response.UserResponseDto;
import social_mate.entity.User;



@Mapper(componentModel = "spring")
public interface UserMapper {
	
	
	 UserResponseDto toUserResponseDto(User user);
	 
	 User toUser(RegisterRequestDto registerRequestDto);
	 

}
