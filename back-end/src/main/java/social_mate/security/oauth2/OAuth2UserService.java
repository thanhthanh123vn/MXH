package social_mate.security.oauth2;

import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import social_mate.entity.User;
import social_mate.entity.UserPrincipal;
import social_mate.entity.enums.AuthProvider;
import social_mate.entity.oauth2.OAuth2UserInfo;
import social_mate.entity.oauth2.OAuth2UserInfoFactory;
import social_mate.exception.OAuth2AuthenticationProcessingException;
import social_mate.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        // 1. Trích xuất thông tin
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo =
                OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        // 2. Gọi logic xử lý đã được refactor
        User user = processOAuth2User(registrationId, oAuth2UserInfo);

        return new UserPrincipal(user, oAuth2User.getAttributes());
    }

    /**
     * Tách logic này ra và để public (hoặc ít nhất là package-private)
     * để MobileAuthService có thể tái sử dụng.
     */
    public User processOAuth2User(String registrationId, OAuth2UserInfo oAuth2UserInfo) {

        Optional<User> userOptional = userRepository.findByProviderId(oAuth2UserInfo.getProviderId());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();

            AuthProvider authProvider = AuthProvider.valueOf(registrationId.toUpperCase());
            if (!user.getAuthProvider().equals(authProvider)) {
                throw new OAuth2AuthenticationProcessingException(
                        "Looks like you're signed up with " + user.getAuthProvider() +
                        " account. Please use your " + user.getAuthProvider() + " account to login."
                );
            }
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(registrationId, oAuth2UserInfo);
        }

        return user;
    }

    // Giữ nguyên các hàm private helper
    private User registerNewUser(String registrationId, OAuth2UserInfo oauth2UserInfo) {
        User user = new User();

        AuthProvider authProvider = AuthProvider.valueOf(registrationId.toUpperCase());

        user.setAuthProvider(authProvider);
        user.setProviderId(oauth2UserInfo.getProviderId());
        user.setUsername(oauth2UserInfo.getUsername());
        user.setEmail(oauth2UserInfo.getEmail());
        user.setAvatar(oauth2UserInfo.getAvatar());

        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        existingUser.setUsername(oAuth2UserInfo.getUsername());
        existingUser.setAvatar(oAuth2UserInfo.getAvatar());
        return userRepository.save(existingUser);
    }
}
