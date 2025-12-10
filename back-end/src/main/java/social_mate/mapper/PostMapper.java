package social_mate.mapper;

import org.mapstruct.Mapper;
import social_mate.dto.request.PostRequestDto;
import social_mate.dto.request.RegisterRequestDto;
import social_mate.dto.response.PostResponseDto;
import social_mate.dto.response.UserResponseDto;
import social_mate.entity.User;
import social_mate.entity.post.Post;
@Mapper(componentModel = "spring")
public  interface  PostMapper {
    PostResponseDto toPostResponseDto(Post post);

    Post toPost(PostRequestDto postRequestDto);
    

}
