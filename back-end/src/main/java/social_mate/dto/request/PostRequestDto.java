package social_mate.dto.request;

import lombok.Getter;
import lombok.Setter;
import social_mate.entity.post.PostMedia;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PostRequestDto {

    private String content;
    private boolean delete;
    private List<String> mediaUrls;
    private Long originalPostId;

    private Integer privacyMode;
    private Map<String, Object> metaData;
}
