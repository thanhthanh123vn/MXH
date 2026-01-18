package social_mate.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import social_mate.dto.request.AddFriendRequestDto;
import social_mate.dto.request.NotificationRequestDto;
import social_mate.dto.response.FriendResponseDto;
import social_mate.entity.Friend;
import social_mate.entity.User;
import social_mate.entity.UserPrincipal;
import social_mate.entity.enums.FriendshipStatus;
import social_mate.entity.enums.NotificationType;
import social_mate.mapper.FriendMapper;
import social_mate.repository.FriendRepository;
import social_mate.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final FriendMapper mapper;
    private final NotificationService notificationService;

    public List<FriendResponseDto> getFriends(UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            throw new IllegalStateException("UserPrincipal không được null");
        }


        User currentUser = userPrincipal.getUser();
        Long userId = currentUser.getId();
        List<User> friends = friendRepository.getFriends(userId);
        return friends.stream()
                .map(mapper::toFriendResponse)
                .toList();
    }

    public List<FriendResponseDto> getFriendRequests(UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            throw new IllegalStateException("UserPrincipal không được null");
        }
        User currentUser = userPrincipal.getUser();
        Long userId = currentUser.getId();

        List<User> users = friendRepository.findFriendRequests(userId);

        return users.stream()
                .map(mapper::toFriendResponse)
                .toList();
    }

    @Transactional
    public void sendFriendRequest(AddFriendRequestDto request, UserPrincipal userPrincipal) throws BadRequestException {

        User sender = userPrincipal.getUser();
        Long senderId = sender.getId();
        Long receiverId = request.getReceiverid();

        if (senderId.equals(receiverId)) {
            throw new BadRequestException("Cannot add yourself");
        }
        Optional<Friend> existingOpt =
                friendRepository.findFriendBetweenUsers(senderId, receiverId);

        if (existingOpt.isPresent()) {
            Friend existing = existingOpt.get();

            switch (existing.getStatus()) {
                case PENDING -> {
                    throw new RuntimeException("Friend request already pending");
                }
                case ACCEPTED -> {
                    throw new RuntimeException("You are already friends");
                }
                case BLOCKED -> throw new RuntimeException("user blocked");

                case REJECTED, CANCELLED -> {
                    // cho phép gửi lại
                    existing.setSenderId(senderId);
                    existing.setReceiverId(receiverId);
                    existing.setStatus(FriendshipStatus.PENDING);
                    friendRepository.save(existing);

                    // [1] Gửi thông báo: Lời mời kết bạn (isAccepted = false)
                    sendNotificationTypeFriend(receiverId, userPrincipal, false);
                    return;
                }
            }
        }

        // chưa từng có quan hệ -> tạo mới
        Friend friend = new Friend();
        friend.setSenderId(senderId);
        friend.setReceiverId(receiverId);
        friend.setStatus(FriendshipStatus.PENDING);
        friendRepository.save(friend);

        // [2] Gửi thông báo: Lời mời kết bạn (isAccepted = false)
        sendNotificationTypeFriend(receiverId, userPrincipal, false);
    }

    @Transactional
    public void acceptFriendRequest(Long senderId, UserPrincipal userPrincipal) {

        Long receiverId = userPrincipal.getUser().getId(); // Người đang bấm chấp nhận (B)

        Friend friend = friendRepository
                .findBySenderIdAndReceiverIdAndStatus(
                        senderId, // Người gửi lời mời ban đầu (A)
                        receiverId,
                        FriendshipStatus.PENDING
                )
                .orElseThrow(() ->
                        new RuntimeException("Friend request not found")
                );

        friend.setStatus(FriendshipStatus.ACCEPTED);
        friendRepository.save(friend);

        // [3] Gửi thông báo: Đã chấp nhận (isAccepted = true)
        // Người nhận thông báo là senderId (A)
        // Người thực hiện hành động là userPrincipal (B)
        sendNotificationTypeFriend(senderId, userPrincipal, true);
    }

    @Transactional
    public void rejectFriendRequest(Long senderId, UserPrincipal userPrincipal) {
        Long receiverId = userPrincipal.getUser().getId();

        Friend friend = friendRepository
                .findBySenderIdAndReceiverIdAndStatus(
                        senderId,
                        receiverId,
                        FriendshipStatus.PENDING
                )
                .orElseThrow(() ->
                        new RuntimeException("Friend request not found")
                );

        friend.setStatus(FriendshipStatus.REJECTED);
        friendRepository.save(friend);
    }

    // block user
    public void blockUser(Long targetUserId, UserPrincipal principal) {
        Long currentUserId = principal.getUser().getId();

        if (currentUserId.equals(targetUserId)) {
            throw new RuntimeException("Cannot block yourself");
        }

        Optional<Friend> friendOpt =
                friendRepository.findFriendBetweenUsers(currentUserId, targetUserId);

        if (friendOpt.isPresent()) {
            Friend f = friendOpt.get();

            if (f.getStatus() == FriendshipStatus.BLOCKED) {
                throw new RuntimeException("User already blocked");
            }


            f.setStatus(FriendshipStatus.BLOCKED);
            f.setSenderId(currentUserId);
            f.setReceiverId(targetUserId);
            friendRepository.save(f);
            return;
        }

        // chưa có record -> tạo mới
        Friend friend = new Friend();
        friend.setSenderId(currentUserId);
        friend.setReceiverId(targetUserId);
        friend.setStatus(FriendshipStatus.BLOCKED);

        friendRepository.save(friend);
    }

    //unblocked
    public void unblockUser(Long targetUserId, UserPrincipal principal) {
        Long currentUserId = principal.getUser().getId();
        if (currentUserId.equals(targetUserId)) {
            throw new RuntimeException("Cannot unblock yourself");
        }
        // tìm record
        Friend friend = friendRepository
                .findFriendBetweenUsers(currentUserId, targetUserId)
                .orElseThrow(() -> new RuntimeException("Block record not found"));
// kiểm tra xem có phải đúng trạng thái block không
        if (friend.getStatus() != FriendshipStatus.BLOCKED) {
            throw new RuntimeException("User is not blocked");
        }
// mình người block mới dc unblock người khác
        if (friend.getSenderId() != currentUserId) {
            throw new RuntimeException("You are not allowed to unblock this user");
        }

        friendRepository.delete(friend);
    }

    //unfriend
    public void unfriend(Long friendUserId, UserPrincipal principal) throws BadRequestException {

        Long currentUserId = principal.getUser().getId();

        // 1. Không cho tự hủy chính mình
        if (currentUserId.equals(friendUserId)) {
            throw new BadRequestException("Cannot unfriend yourself");
        }

        // 2. Tìm quan hệ bạn bè
        Friend friend = friendRepository
                .findFriendBetweenUsers(currentUserId, friendUserId)
                .orElseThrow(() ->
                        new RuntimeException("Friend relationship not found")
                );

        // 3. unfriend khi là bạn
        if (friend.getStatus() != FriendshipStatus.ACCEPTED) {
            throw new BadRequestException("You are not friends");
        }

        // 4. delete record
        friendRepository.delete(friend);
    }

    // ========================================================================
    // LOGIC GỬI THÔNG BÁO KẾT BẠN
    // ========================================================================

    /**
     * Tạo thông báo kết bạn.
     *
     * @param targetUserId   ID người nhận thông báo.
     * @param actorPrincipal Người thực hiện hành động (Gửi hoặc Chấp nhận).
     * @param isAccepted     false: Gửi lời mời, true: Chấp nhận lời mời.
     */
    private void sendNotificationTypeFriend(Long targetUserId, UserPrincipal actorPrincipal, boolean isAccepted) {
        String actorName = actorPrincipal.getUser().getUsername(); // Hoặc getFullName nếu có

        NotificationRequestDto requestDto = new NotificationRequestDto();

        // 1. Người nhận thông báo
        requestDto.setOwnerId(targetUserId);

        // 2. ID để điều hướng (Click vào thông báo sẽ sang trang cá nhân người thực hiện)
        requestDto.setLinkedResourceId(actorPrincipal.getUser().getId());

        // 3. Loại thông báo (Dùng Enum FRIENDS như bạn đã cung cấp)
        requestDto.setNotificationType(NotificationType.FRIENDS);

        if (isAccepted) {
            // TRƯỜNG HỢP: Đã chấp nhận kết bạn
            // Hiển thị FE: [User A] + [đã chấp nhận lời mời kết bạn] + : + [Giờ đây hai bạn có thể nhắn tin...]
            requestDto.setTitle("đã chấp nhận lời mời kết bạn");
            requestDto.setContent("Giờ đây hai bạn đã trở thành bạn bè.");
        } else {
            // TRƯỜNG HỢP: Gửi lời mời mới
            // Hiển thị FE: [User A] + [đã gửi lời mời kết bạn] + : + [Nhấn để xem trang cá nhân...]
            requestDto.setTitle("đã gửi lời mời kết bạn");
            requestDto.setContent("Nhấn vào đây để phản hồi.");
        }

        // Gọi service để lưu và bắn socket
        notificationService.createNotification(requestDto, actorPrincipal);
    }
}
