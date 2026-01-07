package social_mate.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ConversationGroupRequestDto {

    private String conversationName;
    private List<Long> memberIds;

}
