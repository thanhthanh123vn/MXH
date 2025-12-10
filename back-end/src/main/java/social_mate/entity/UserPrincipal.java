package social_mate.entity;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class UserPrincipal implements UserDetails, OAuth2User {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private User user;
	private Map<String, Object> attributes;

	public UserPrincipal(User user) {
		this.user = user;
	}
	public User getUser() {
		return user;
	}

	public UserPrincipal(User user, Map<String, Object> atttributes) {
		this.user = user;
		this.attributes = atttributes;
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return user.getAuthProvider().toString() + ":" + user.getEmail();
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Map<String, Object> getAttributes() {
		// TODO Auto-generated method stub
		return attributes;
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return user.getProviderId();
	}

}
