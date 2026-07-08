package com.rideshare.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rideshare.dto.user.UpdateProfileRequest;
import com.rideshare.dto.user.UserProfileDto;
import com.rideshare.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
	
	
	private final UserService userService;
	
	@GetMapping("/me")
	public ResponseEntity<UserProfileDto> getMyProfile(){
		return ResponseEntity.ok(userService.getMyProfile());
	}
	
	@PutMapping("/me")
	public ResponseEntity<UserProfileDto> updateMyProfile(
			@Valid @RequestBody UpdateProfileRequest request){
		return ResponseEntity.ok(userService.updateMyProfile(request));
	}
	
	@DeleteMapping("/me")
	public ResponseEntity<String> deactivateAccount(){
		return ResponseEntity.ok(userService.deactivateMyAccount());
	}

}
