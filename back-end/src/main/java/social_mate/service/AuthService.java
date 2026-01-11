package social_mate.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Random;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import social_mate.dto.request.GoogleTokenRequestDto;
import social_mate.dto.request.LoginRequestDto;
import social_mate.dto.request.OtpRequestDto;
import social_mate.dto.request.RegisterRequestDto;
import social_mate.dto.response.TokenResponseDto;
import social_mate.dto.response.UserResponseDto;
import social_mate.entity.User;
import social_mate.entity.UserPrincipal;
import social_mate.entity.enums.AuthProvider;
import social_mate.entity.oauth2.GoogleOAuth2UserInfo;
import social_mate.entity.oauth2.OAuth2UserInfo;
import social_mate.exception.EmailAlreadyExistsException;
import social_mate.exception.ExpiredOtpException;
import social_mate.exception.NotMatchingOtpException;
import social_mate.exception.NotMatchingRefreshTokenException;
import social_mate.mapper.UserMapper;
import social_mate.repository.UserRepository;
import social_mate.security.jwt.JwtAuthenticationService;
import social_mate.security.oauth2.OAuth2UserService;
import social_mate.util.SendEmail;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final SendEmail sendEmail;
	private final PasswordEncoder passwordEncoder;
	private final HttpSession session;
	private final UserMapper userMapper;
	private final AuthenticationManager authenticationManager;
	private final JwtAuthenticationService jwtService;
	private final UserDetailService userDetailService;
	private final GoogleIdTokenVerifier googleIdTokenVerifier;
	private final OAuth2UserService oAuth2UserService;

	private static final int OTP_EXP = 5;

	@Transactional
	public void register(RegisterRequestDto registerRequestDto) {

		// check exist email
		if (userRepository.findByEmailAndAuthProvider(registerRequestDto.getEmail(), AuthProvider.DEFAULT).isPresent()) {
			throw new EmailAlreadyExistsException("Email already exists");
		}

		// generate random OTP (6 numbers)

		Random random = new Random();

		int otpValue = 100000 + random.nextInt(900000);
		String otp = String.valueOf(otpValue);

		// send email with otp to registerRequestDto.getEmail()

		sendEmail.sendOtpToEmail(registerRequestDto.getEmail(), otp);

		// save session include requestDto and OTP and exp OTP

		session.setAttribute("register_request", registerRequestDto);
		session.setAttribute("otp", otp);
		session.setAttribute("otp_exp", System.currentTimeMillis() + OTP_EXP * 60 * 1000);

	}

	@Transactional
	public UserResponseDto verifyRegister(OtpRequestDto otpRequestDto) {

		// check OTP from session with otpRequestDto.getOtp()

		String sessionOtp = (String) session.getAttribute("otp");
		Long otpExp = (Long) session.getAttribute("otp_exp");
		RegisterRequestDto registerRequestDto = (RegisterRequestDto) session.getAttribute("register_request");

		// if otp wrong throw exception "NotMatchingOtpException"

		if (!sessionOtp.equals(otpRequestDto.getOtp())) {
			throw new NotMatchingOtpException("invalid OTP code");
		}

		// if otp expired throw exception "ExpriedOtpException"
		if (System.currentTimeMillis() > otpExp) {
			throw new ExpiredOtpException("OTP has expired. Please request a new one.");
		}
		// if otp right save user from session registerRequestDto
		User newUser = userMapper.toUser(registerRequestDto);
		String encodedPassword = passwordEncoder.encode(registerRequestDto.getPassword());
		newUser.setPassword(encodedPassword);
		newUser.setAuthProvider(AuthProvider.DEFAULT);

		User userSaved = userRepository.save(newUser);
		// Tạo UserDetail mặc định ngay sau khi tạo User
		userDetailService.createUserDetailForUser(userSaved);
		clearSession();

		return userMapper.toUserResponseDto(userSaved);

	}

	public TokenResponseDto authenticate(LoginRequestDto loginRequestDto) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword()));

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();

		String accessToken = jwtService.generateToken(userDetails);
		String refreshToken = jwtService.generateRefreshToken(userDetails);

		return new TokenResponseDto(accessToken, refreshToken);

	}

	public TokenResponseDto refreshToken(String refreshToken) {

		String userEmail = jwtService.extractUsername(refreshToken);

		UserDetails userDetails = userDetailService.loadUserByUsername(userEmail);

		if (!jwtService.isTokenValid(refreshToken, userDetails)) {
			throw new NotMatchingRefreshTokenException("refresh token not maching");
		}

		String newAccessToken = jwtService.generateToken(userDetails);

		return new TokenResponseDto(newAccessToken, refreshToken);

	}
	
    @Transactional
    public TokenResponseDto verifyGoogleIdTokenAndLogin(GoogleTokenRequestDto googleTokenRequestDto) throws GeneralSecurityException, IOException {
        // 1. Xác thực idToken với Google
        GoogleIdToken idToken = googleIdTokenVerifier.verify(googleTokenRequestDto.getIdToken());
        if (idToken == null) {
            throw new IllegalArgumentException("Invalid ID token");
        }

        // 2. Trích xuất thông tin người dùng từ payload
        GoogleIdToken.Payload payload = idToken.getPayload();

        // 3. Sử dụng logic xử lý user hiện có của bạn
        // Chúng ta cần điều chỉnh OAuth2UserService để có thể gọi logic này từ bên ngoài
        // (Xem bước 2 bên dưới)
        
        // Tạo một đối tượng OAuth2UserInfo thủ công từ payload
        // Gói payload vào một Map để tương thích với factory
        Map<String, Object> attributes = new java.util.HashMap<>(payload);
        attributes.put("sub", payload.getSubject()); // Đảm bảo providerId (sub) có trong map
        attributes.put("picture", payload.get("picture"));
        attributes.put("name", payload.get("name"));
        
        // Giả sử GoogleOAuth2UserInfo của bạn có constructor nhận Map
        OAuth2UserInfo oAuth2UserInfo = new GoogleOAuth2UserInfo(attributes); 

        // 4. Gọi logic xử lý user đã được refactor
        User user = oAuth2UserService.processOAuth2User(AuthProvider.GOOGLE.toString(), oAuth2UserInfo);

        // 5. Tạo UserPrincipal (cần thiết cho việc tạo JWT)
        // Chúng ta dùng 'payload' (Map) làm attributes
        UserPrincipal userPrincipal = new UserPrincipal(user, payload);

        // 6. Tạo JWT
        String accessToken = jwtService.generateToken(userPrincipal);
        String refreshToken = jwtService.generateRefreshToken(userPrincipal);

        // 7. Trả về token cho client
        return new TokenResponseDto(accessToken, refreshToken);
    }

	private void clearSession() {
		// TODO Auto-generated method stub

		session.removeAttribute("otp");
		session.removeAttribute("register_request");
		session.removeAttribute("otp_exp");

	}

}
