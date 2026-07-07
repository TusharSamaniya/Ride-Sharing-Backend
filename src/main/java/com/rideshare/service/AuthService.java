package com.rideshare.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rideshare.dto.auth.AuthResponse;
import com.rideshare.dto.auth.LoginRequest;
import com.rideshare.dto.auth.RegisterRequest;
import com.rideshare.entity.User;
import com.rideshare.repository.UserRepository;
import com.rideshare.security.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private AuthenticationManager authenticationManager;
	
	public AuthResponse register(RegisterRequest request) {
		if(userRepository.existsByEmail(request.getEmail())) throw new RuntimeException("Email is already registered");
		
		//Build and save User entity — password is BCrypt hashed
		var user = User.builder()
				.firstName(request.getFirstName())
				.lastName(request.getLastName())
				.email(request.getEmail())
				.password(passwordEncoder.encode(request.getPassword()))
				.phoneNumber(request.getPhoneNumber())
				.role(request.getRole())
				.isActive(true)
				.build();
		userRepository.save(user);
		
		// Build Spring Security UserDetails from saved user
		var userDetails = org.springframework.security.core.userdetails.User
				.withUsername(user.getEmail())
				.password(user.getPassword())
				.authorities("ROLE_" + user.getRole().name())
				.build();
		
		// AuthenticationManager internally calls:
        // 1. UserDetailsService.loadUserByUsername(email)
        // 2. passwordEncoder.matches(rawPassword, storedHashedPassword)
        // If either fails it throws BadCredentialsException → 401
		return AuthResponse.builder()
		        .accessToken(jwtUtil.generateToken(userDetails))
		        .refreshToken(jwtUtil.generateRefreshToken(userDetails))
		        .tokenType("Bearer")           // ← add this line, it was missing
		        .userId(user.getId())
		        .email(user.getEmail())        // ← add this line, it was missing
		        .firstName(user.getFirstName())
		        .role(user.getRole().name())
		        .build();
		
	}
	
	public AuthResponse login(LoginRequest request) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						request.getEmail(),
						request.getPassword()
						)
				);
		
		//If we reach here, credentials are correct
		var user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new RuntimeException("User not found"));
		
		var userDetails = org.springframework.security.core.userdetails.User
				.withUsername(user.getEmail())
				.password(user.getPassword())
				.authorities("ROLE_" + user.getRole().name())
				.build();
		
		return AuthResponse.builder()
				.accessToken(jwtUtil.generateToken(userDetails))
				.refreshToken(jwtUtil.generateRefreshToken(userDetails))
				.userId(user.getId())
				.firstName(user.getFirstName())
                .role(user.getRole().name())
                .build();
	
		
	}
	

}
