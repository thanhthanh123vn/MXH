package social_mate.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import social_mate.dto.response.UserResponseDto;
import social_mate.entity.User;
import social_mate.entity.UserPrincipal;
import social_mate.mapper.UserMapper;

@Service
@RequiredArgsConstructor
public class UserService {

	// Inject UserMapper để thực hiện chuyển đổi
	private final UserMapper userMapper;

	/**
	 * Lấy thông tin DTO của người dùng đã được xác thực.
	 * * @param userPrincipal Đối tượng principal chứa thông tin người dùng.
	 * @return UserResponseDto chứa thông tin public của người dùng.
	 */
	public UserResponseDto getMe(UserPrincipal userPrincipal) {
		if (userPrincipal == null) {
			// Điều này không nên xảy ra nếu được gọi từ controller đã check
			// Hoặc bạn có thể ném một exception cụ thể
			throw new IllegalStateException("UserPrincipal không được null");
		}

		// Lấy User entity từ principal
		User currentUser = userPrincipal.getUser();

		// Chuyển đổi User entity sang DTO và trả về
		return userMapper.toUserResponseDto(currentUser);
	}


	// Trong tương lai, bạn có thể thêm các phương thức khác tại đây
	// ví dụ: updateUserProfile, getUserById, v.v.
}