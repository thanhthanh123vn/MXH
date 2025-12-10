package social_mate.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import social_mate.entity.User;
import social_mate.entity.UserPrincipal;
import social_mate.entity.enums.AuthProvider;
import social_mate.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserDetailService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub

		try {
			
			String[] parts = username.split(":", 2);
			AuthProvider provider;
			String email;

			// Trường hợp 1: Key tổng hợp (từ JWT)
			// ví dụ: "GOOGLE:user@gmail.com"
			if (parts.length == 2) {
				provider = AuthProvider.valueOf(parts[0]);
				email = parts[1];
			}
			// Trường hợp 2: Email thuần (từ form login)
			// ví dụ: "user@gmail.com"
			else {
				provider = AuthProvider.DEFAULT;
				email = username; // username chính là email
			}
			
			System.out.println(email+" "+ provider);

			// Tìm user bằng cả email và provider
			User user = userRepository.findByEmailAndAuthProvider(email, provider)
					.orElseThrow(() -> new UsernameNotFoundException(
							"User not found with email: " + email + " and provider: " + provider));

			return new UserPrincipal(user);

		} catch (Exception e) {
			// Bắt các lỗi tiềm ẩn như AuthProvider.valueOf thất bại
			throw new UsernameNotFoundException("Cannot find user with username: " + username, e);
		}

	}

}
