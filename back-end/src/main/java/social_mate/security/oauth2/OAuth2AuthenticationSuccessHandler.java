package social_mate.security.oauth2;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import social_mate.entity.UserPrincipal;
import social_mate.security.jwt.JwtAuthenticationService;
import social_mate.security.jwt.JwtProperties;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	
	private final JwtAuthenticationService jwtService;
	
	@Value("${app.fontend-domain}")
	private String fontEndDomain;
	private final JwtProperties jwtProperties;
    
//    // Giả sử bạn định nghĩa thời gian sống của token trong application.properties
//    @Value("${application.security.jwt.expiration}")
//    private long accessTokenExpiration;
//    @Value("${application.security.jwt.refresh-token.expiration}")
//    private long refreshTokenExpiration;


	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		
		String targetUrl = determineTargetUrl(request, response, authentication);
		
		if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

		clearAuthenticationAttributes(request);
		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}

	protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String redirectUri = fontEndDomain; // Chỉ cần redirect về trang chủ hoặc dashboard

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

		String accessToken = jwtService.generateToken(userPrincipal);
		String refreshToken = jwtService.generateRefreshToken(userPrincipal);
		
        // Tạo cookie cho Access Token
        addCookie(response, "access_token", accessToken, jwtProperties.getAccessTokenExpiration());

        // Tạo cookie cho Refresh Token
        addCookie(response, "refresh_token", refreshToken, jwtProperties.getRefreshTokenExpiration());
		
		return redirectUri;
	}

    /**
     * Helper để tạo và thêm cookie vào response
     */
    private void addCookie(HttpServletResponse response, String name, String value, long maxAgeInMilliseconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/"); // Path "/" có nghĩa là cookie sẽ được gửi cho mọi request trên domain
        cookie.setMaxAge((int) TimeUnit.MILLISECONDS.toSeconds(maxAgeInMilliseconds));
        // cookie.setSecure(true); // <-- BẮT BUỘC bật cờ này ở môi trường Production (khi dùng HTTPS)
        response.addCookie(cookie);
    }
}