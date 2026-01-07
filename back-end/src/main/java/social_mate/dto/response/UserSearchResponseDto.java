package social_mate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import social_mate.entity.enums.FriendViewStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSearchResponseDto {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private FriendViewStatus friendStatus;
}
