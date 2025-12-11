package social_mate.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import social_mate.dto.request.PostRequestDto;
import social_mate.dto.response.PostResponseDto;
import social_mate.entity.post.Post;
import social_mate.entity.post.PostMedia;

@Mapper(componentModel = "spring")
public interface PostMapper {


    @Mapping(source = "media", target = "mediaUrls")
    PostResponseDto toPostResponseDto(Post post);



    @Mapping(target = "media", ignore = true)
    Post toPost(PostRequestDto postRequestDto);


    default String mapPostMediaToString(PostMedia postMedia) {
        if (postMedia == null) {
            return null;
        }
        return postMedia.getUrl();
    }
}