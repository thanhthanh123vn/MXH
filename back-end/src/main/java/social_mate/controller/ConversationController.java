package social_mate.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

import lombok.RequiredArgsConstructor;
import social_mate.dto.request.ConversationDirectRequestDto;
import social_mate.dto.request.ConversationGroupRequestDto;
import social_mate.dto.response.ConversationResponseDto;
import social_mate.dto.response.MessageResponseDto;
import social_mate.entity.UserPrincipal;
import social_mate.service.ConversationService;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping()
    public ResponseEntity<List<ConversationResponseDto>> getConversationsByUserId(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<ConversationResponseDto> response = conversationService.getConversationsByUserId(userPrincipal);

        return ResponseEntity.status(200).body(response);

    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<List<MessageResponseDto>> getConversationById(@PathVariable Long conversationId, @RequestParam(defaultValue = "0") Integer page,@RequestParam(defaultValue = "10") Integer size, @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<MessageResponseDto> messages = conversationService.getConversationById(conversationId, page, size, userPrincipal);

        return ResponseEntity.status(200).body(messages);

    }

    @PostMapping("/group")
    public ResponseEntity<?> createConversationGroup(@RequestBody ConversationGroupRequestDto conversationGroupRequestDto, @AuthenticationPrincipal UserPrincipal userPrincipal) {

        conversationService.createConversationGroup(conversationGroupRequestDto, userPrincipal);

        return ResponseEntity.status(201).body(Collections.singletonMap("message", "conversation group created successfully"));

    }

    @PostMapping("/direct")
    public ResponseEntity<?> createConversationDirect(@RequestBody ConversationDirectRequestDto request,
                                                      @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long newId = conversationService.createConversationDirect(request, userPrincipal);
        return ResponseEntity.status(201).body(Collections.singletonMap("conversationId", newId));
    }

    @GetMapping("/direct/check/{partnerId}")
    public ResponseEntity<?> checkExistingDirect(@PathVariable Long partnerId,
                                                 @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Long conversationId = conversationService.findExistingDirectConversationId(userPrincipal, partnerId);
        return ResponseEntity.ok(Collections.singletonMap("conversationId", conversationId));
    }

}