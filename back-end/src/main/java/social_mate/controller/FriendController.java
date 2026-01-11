package social_mate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import social_mate.dto.request.AddFriendRequestDto;
import social_mate.dto.request.RespondFriendRequestDto;
import social_mate.dto.response.FriendResponseDto;
import social_mate.dto.response.FriendStatusResponseDto;
import social_mate.entity.UserPrincipal;
import social_mate.entity.enums.FriendViewStatus;
import social_mate.service.FriendService;

import java.util.List;

@RestController
@RequestMapping("api/v1/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @GetMapping()
    public ResponseEntity<List<FriendResponseDto>> getFriends(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<FriendResponseDto> friends = friendService.getFriends(userPrincipal);
        return ResponseEntity.ok(friends);
    }

    @GetMapping("/requests")
    public ResponseEntity<List<FriendResponseDto>> getFriendRequests(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        List<FriendResponseDto> friends = friendService.getFriendRequests(userPrincipal);
        return ResponseEntity.ok(friends);
    }

    @PostMapping()
    public ResponseEntity<FriendStatusResponseDto> addFriend(
            @RequestBody @Valid AddFriendRequestDto request,
            @AuthenticationPrincipal UserPrincipal principal) throws BadRequestException {
        friendService.sendFriendRequest(request, principal);
        return ResponseEntity.ok(
                FriendStatusResponseDto.builder()
                        .userId(request.getReceiverid())
                        .friendStatus(FriendViewStatus.SENT_REQUEST)
                        .build()
        );
    }

    //  Accept
    @PutMapping("/accept/{targetUserId}")
    public ResponseEntity<FriendStatusResponseDto> acceptFriendRequest(
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        friendService.acceptFriendRequest(targetUserId, principal);
        return ResponseEntity.ok(
                FriendStatusResponseDto.builder()
                        .userId(targetUserId)
                        .friendStatus(FriendViewStatus.FRIEND)
                        .build()
        );
    }


    // Reject
    @PutMapping("/reject/{targetUserId}")
    public ResponseEntity<FriendStatusResponseDto> rejectFriendRequest(
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        friendService.rejectFriendRequest(targetUserId, userPrincipal);
        return ResponseEntity.ok(
                FriendStatusResponseDto.builder()
                        .userId(targetUserId)
                        .friendStatus(FriendViewStatus.NONE)
                        .build()
        );
    }
    //block
    @PostMapping("/block/{targetUserId}")
    public ResponseEntity<FriendStatusResponseDto> blockUser(
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        friendService.blockUser(targetUserId, userPrincipal);

        return ResponseEntity.ok(
                FriendStatusResponseDto.builder()
                        .userId(targetUserId)
                        .friendStatus(FriendViewStatus.BLOCKED)
                        .build()
        );
    }
    // unblock
    @DeleteMapping("/unblock/{targetUserId}")
    public ResponseEntity<FriendStatusResponseDto> unblock(
            @PathVariable Long targetUserId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        friendService.unblockUser(targetUserId, userPrincipal);
        return ResponseEntity.ok(
                FriendStatusResponseDto.builder()
                        .userId(targetUserId)
                        .friendStatus(FriendViewStatus.NONE)
                        .build()
        );
    }
    @DeleteMapping("unfriend/{friendUserId}")
    public ResponseEntity<FriendStatusResponseDto> unfriend(
            @PathVariable Long friendUserId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) throws BadRequestException {
        friendService.unfriend(friendUserId, userPrincipal);
        return ResponseEntity.ok(
                FriendStatusResponseDto.builder()
                        .userId(friendUserId)
                        .friendStatus(FriendViewStatus.NONE)
                        .build()
        );
    }
}
