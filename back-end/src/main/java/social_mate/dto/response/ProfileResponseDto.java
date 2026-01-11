package social_mate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import social_mate.entity.enums.FriendViewStatus;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class ProfileResponseDto {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private String bio;
    private String jobTitle;
    private String coverPhoto;
    private String address;
    private int totalFriends;
    private int totalPosts;
    private FriendViewStatus friendViewStatus;
}
