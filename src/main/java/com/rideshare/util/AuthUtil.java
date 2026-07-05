package com.rideshare.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.rideshare.entity.User;
import com.rideshare.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthUtil {
	
	@Autowired
	private UserRepository userRepository;
	
	public User getCurrentUser() {
		
		String email = SecurityContextHolder
				.getContext()
				.getAuthentication()
				.getName();
		
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("User not found"));
		
	}

}
