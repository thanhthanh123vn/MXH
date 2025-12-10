package social_mate.entity.oauth2;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class OAuth2UserInfo {
	
	protected Map<String , Object> attributes;
	
	public abstract String getProviderId();
	public abstract String getEmail();
	public abstract String getUsername();
	public abstract String getAvatar();
	

}
