package social_mate.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GoogleTokenRequestDto {

	@NotBlank(message = "idToken is required")
	private String idToken;

}
