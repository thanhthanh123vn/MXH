package social_mate.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import social_mate.dto.request.GoogleTokenRequestDto;
import social_mate.dto.request.LoginRequestDto;
import social_mate.dto.request.OtpRequestDto;
import social_mate.dto.request.RegisterRequestDto;
import social_mate.dto.response.TokenResponseDto;
import social_mate.dto.response.UserResponseDto;
import social_mate.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<?> register(@RequestBody RegisterRequestDto registerRequestDto) {

		Map<String, String> response = new HashMap<>();

		authService.register(registerRequestDto);

		response.put("message", "otp send to your emal");

		return ResponseEntity.status(200).body(response);

	}

	@PostMapping("/verify-register")
	public ResponseEntity<UserResponseDto> verifyRigister(@RequestBody OtpRequestDto otpRequestDto) {

		UserResponseDto newUser = authService.verifyRegister(otpRequestDto);

		return ResponseEntity.status(201).body(newUser);

	}

	@PostMapping("/login")
	public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {

		TokenResponseDto response = authService.authenticate(loginRequestDto);

		return ResponseEntity.status(200).body(response);
	}

	@PostMapping("/refresh-token")
	public ResponseEntity<TokenResponseDto> refreshToken(@CookieValue String refreshToken) {

		TokenResponseDto response = authService.refreshToken(refreshToken);

		return ResponseEntity.status(200).body(response);

	}

	@PostMapping("/login-with-google")
	public ResponseEntity<TokenResponseDto> loginWithGoogle(@Valid @RequestBody GoogleTokenRequestDto idToken)
			throws GeneralSecurityException, IOException {

		TokenResponseDto response = authService.verifyGoogleIdTokenAndLogin(idToken);

		return ResponseEntity.status(200).body(response);

	}

}
