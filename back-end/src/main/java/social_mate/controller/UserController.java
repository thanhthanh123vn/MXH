package social_mate.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import social_mate.dto.response.UserResponseDto;
import social_mate.entity.UserPrincipal;
import social_mate.service.UserService;

@RestController
@RequestMapping("api/v1/user")
@RequiredArgsConstructor
public class UserController {
	
	private final UserService userService;
	
	@GetMapping("/me")
	public ResponseEntity<UserResponseDto> getMe(@AuthenticationPrincipal UserPrincipal userPrincipal){
		
		UserResponseDto userResponse=userService.getMe(userPrincipal);
		
		return ResponseEntity.status(200).body(userResponse);
		
		
	}
	

}
