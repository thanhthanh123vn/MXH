package social_mate.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import social_mate.dto.request.PostRequestDto;
import social_mate.dto.response.PostResponseDto;
import social_mate.entity.post.Post;
import social_mate.entity.post.PostMedia;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface PostMapper {


    @Mapping(source = "id", target = "id")
    @Mapping(source = "media", target = "mediaUrls")
    @Mapping(source = "media", target = "media", qualifiedByName = "mapMediaToDto")
    PostResponseDto toPostResponseDto(Post post);



    @Mapping(target = "media", ignore = true)
    Post toPost(PostRequestDto postRequestDto);


    default String mapPostMediaToString(PostMedia postMedia) {
        if (postMedia == null) {
            return null;
        }
        return postMedia.getUrl();
    }
    @Named("mapMediaToDto")
    default List<PostResponseDto.MediaResponse> mapMediaToDto(List<PostMedia> mediaList) {
        if (mediaList == null) return null;

        return mediaList.stream().map(m -> {
            PostResponseDto.MediaResponse dto = new PostResponseDto.MediaResponse();

            dto.setUrl(m.getUrl());
            dto.setType(m.getType());
            dto.setFileName(m.getFileName());
            return dto;
        }).collect(Collectors.toList());
    }
}