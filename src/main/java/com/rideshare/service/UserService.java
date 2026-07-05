package com.rideshare.service;

import com.rideshare.dto.user.*;
import com.rideshare.entity.User;
import com.rideshare.repository.UserRepository;
import com.rideshare.util.AuthUtil;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

	@Autowired
    private UserRepository userRepository;
	@Autowired
    private AuthUtil authUtil;

    // GET /api/users/me
    public UserProfileDto getMyProfile() {
        User user = authUtil.getCurrentUser();
        return convertToDto(user);
    }

    // PUT /api/users/me
    public UserProfileDto updateMyProfile(UpdateProfileRequest request) {
        User user = authUtil.getCurrentUser();

        // Only update fields that the client actually sent (not null)
        // This way client can update just one field without touching others
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        // save() updates the existing record because user already has an ID
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    // DELETE /api/users/me
    // We do NOT delete the row. We just set isActive = false (soft delete)
    public String deactivateMyAccount() {
        User user = authUtil.getCurrentUser();
        user.setIsActive(false);
        userRepository.save(user);
        return "Account deactivated successfully";
    }

    // Private helper — converts User entity to UserProfileDto
    // We do this manually to keep it simple
    private UserProfileDto convertToDto(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .profilePictureUrl(user.getProfilePictureUrl())
                .build();
    }
}