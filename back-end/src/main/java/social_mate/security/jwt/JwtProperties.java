package social_mate.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ConfigurationProperties(prefix = "jwt")
@RequiredArgsConstructor
@Getter
public class JwtProperties {
	private final String secretKey; 
	
	private final Long accessTokenExpiration;
	
	private final Long refreshTokenExpiration;

}
