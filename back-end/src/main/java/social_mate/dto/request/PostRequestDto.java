package social_mate.dto.request;

import lombok.Getter;
import lombok.Setter;
import social_mate.entity.post.PostMedia;

import java.util.List;
@Getter
@Setter
public class PostRequestDto {

    private String content;

    private List<String> mediaUrls;

}
