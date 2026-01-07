package social_mate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import social_mate.dto.request.MessageFileRequestDto;
import social_mate.dto.request.MessageTextRequestDto;
import social_mate.entity.UserPrincipal;
import social_mate.service.MessageService;

import java.util.Collections;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @PostMapping("/text")
    public ResponseEntity<?> createMessageText(@RequestBody MessageTextRequestDto messageTextRequestDto, @AuthenticationPrincipal UserPrincipal userPrincipal) {

        messageService.createMessageText(messageTextRequestDto, userPrincipal);

        return ResponseEntity.status(201).body(Collections.singletonMap("message", "create message successfully"));
    }

    @PostMapping("/file")
    public ResponseEntity<?> createMessageFile(@ModelAttribute MessageFileRequestDto messageFileRequestDto, @AuthenticationPrincipal UserPrincipal userPrincipal) {

        messageService.createMessageFile(messageFileRequestDto, userPrincipal);

        return ResponseEntity.status(201).body(Collections.singletonMap("message", "create message successfully"));
    }

}
