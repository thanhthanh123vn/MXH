package social_mate.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import social_mate.dto.response.UserResponseDto;
import social_mate.dto.response.UserSearchResponseDto;
import social_mate.entity.Friend;
import social_mate.entity.User;
import social_mate.entity.UserPrincipal;
import social_mate.entity.enums.FriendViewStatus;
import social_mate.mapper.UserMapper;
import social_mate.repository.FriendRepository;
import social_mate.repository.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    // Inject UserMapper để thực hiện chuyển đổi
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;


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


    /**
     * lấy danh sách user theo keyword tìm kiếm dựa vào username
     * <p>
     * * @param keyword userPrincipal
     *
     * @return list user search theo keyword gồm thông tin theo UserSearchResponseDto
     */
    public List<UserSearchResponseDto> searchUsers(String keyword, UserPrincipal userPrincipal) {
        Long currentUserId = userPrincipal.getUser().getId();
        List<User> users = userRepository.searchUsers(keyword, currentUserId);
        return users.stream().map(user -> {
            UserSearchResponseDto res = userMapper.toUserSearchResponseDto(user);
            Optional<Friend> friendOpt = friendRepository.findFriendBetweenUsers(currentUserId, user.getId());
            res.setFriendStatus(mapFriendStatus(friendOpt, currentUserId));
            return res;
        }).toList();
    }


    private FriendViewStatus mapFriendStatus(Optional<Friend> friendOpt, Long currentUserId) {
        if (friendOpt.isEmpty()) {
            return FriendViewStatus.NONE;
        }

        Friend f = friendOpt.get();
        switch (f.getStatus()) {
            case BLOCKED:
                return FriendViewStatus.BLOCKED;

            case ACCEPTED:
                return FriendViewStatus.FRIEND;

            case PENDING:
                return Objects.equals(f.getSenderId(), currentUserId)
                        ? FriendViewStatus.SENT_REQUEST
                        : FriendViewStatus.RECEIVED_REQUEST;

            default:
                return FriendViewStatus.NONE;
        }
    }

    public List<UserResponseDto> getUsersByUsername(String username) {
        return userRepository.findByUsernameContainingIgnoreCase(username).stream().map(userMapper::toUserResponseDto).collect(Collectors.toList());
    }
}