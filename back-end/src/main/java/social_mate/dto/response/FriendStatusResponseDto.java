package social_mate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import social_mate.entity.enums.FriendViewStatus;

@Getter
@Builder
@AllArgsConstructor
public class FriendStatusResponseDto {
    private Long userId;
    private FriendViewStatus friendStatus;
}
