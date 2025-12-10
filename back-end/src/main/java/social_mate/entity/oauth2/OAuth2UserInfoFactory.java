package social_mate.entity.oauth2;

import java.util.Map;

import social_mate.entity.enums.AuthProvider;



public class OAuth2UserInfoFactory {

	public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {

		if (registrationId.equals(AuthProvider.GOOGLE.toString().toLowerCase())) {

			return new GoogleOAuth2UserInfo(attributes);
		}
		//more provider ...


		return null;

	}

}
