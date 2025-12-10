package social_mate.entity.oauth2;

import java.util.Map;

public class GoogleOAuth2UserInfo extends OAuth2UserInfo{
	
	

	public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
		super(attributes);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getProviderId() {
		// TODO Auto-generated method stub
		return (String) attributes.get("sub");
	}

	@Override
	public String getEmail() {
		// TODO Auto-generated method stub
		return (String) attributes.get("email");
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return (String) attributes.get("name");
	}

	@Override
	public String getAvatar() {
		// TODO Auto-generated method stub
		return (String) attributes.get("picture");
	}

}
