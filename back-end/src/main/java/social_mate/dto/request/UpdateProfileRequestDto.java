package social_mate.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequestDto {
    // users
    private String username;
    private String avatar;

    // user_details
    private String bio;
    private String jobTitle;
    private String coverPhoto;
    private String address;
}