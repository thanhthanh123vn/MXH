package social_mate.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MessageTextRequestDto {

    private Long conversationId;
    private String content;

}
