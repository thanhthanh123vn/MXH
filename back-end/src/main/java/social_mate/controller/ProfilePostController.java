package social_mate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import social_mate.dto.response.ProfilePostResponseDto;
import social_mate.service.ProfilePostService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}/posts")
@RequiredArgsConstructor
public class ProfilePostController {

    private final ProfilePostService profilePostService;

    @GetMapping()
    public ResponseEntity<List<ProfilePostResponseDto>> getUserPosts(@PathVariable Long userId) {
        List<ProfilePostResponseDto> posts = profilePostService.getProfilePosts(userId);
        return ResponseEntity.ok(posts);
    }
}
