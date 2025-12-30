package social_mate.service;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import social_mate.dto.request.AddFriendRequestDto;
import social_mate.dto.response.FriendResponseDto;
import social_mate.entity.Friend;
import social_mate.entity.User;
import social_mate.entity.UserPrincipal;
import social_mate.entity.enums.FriendshipStatus;
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
    public void sendFriendRequest(AddFriendRequestDto request, UserPrincipal userPrincipal) throws BadRequestException {

        User sender = userPrincipal.getUser();
        Long senderId = sender.getId();
        Long receiverId = request.getReceiverid();

        if (senderId.equals(receiverId)) {
            // xử lí lại theo custom exeption tự handle
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
    }

    public void acceptFriendRequest(Long senderId, UserPrincipal userPrincipal) {

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

        friend.setStatus(FriendshipStatus.ACCEPTED);
        friendRepository.save(friend);
    }
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
}
