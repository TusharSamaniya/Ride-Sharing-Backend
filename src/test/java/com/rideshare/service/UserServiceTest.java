package com.rideshare.service;

import com.rideshare.dto.user.UpdateProfileRequest;
import com.rideshare.dto.user.UserProfileDto;
import com.rideshare.entity.User;
import com.rideshare.entity.UserRole;
import com.rideshare.repository.UserRepository;
import com.rideshare.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private UserService userService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setFirstName("Rahul");
        currentUser.setLastName("Sharma");
        currentUser.setEmail("rahul@test.com");
        currentUser.setPhoneNumber("9876543210");
        currentUser.setRole(UserRole.RIDER);
        currentUser.setIsActive(true);
    }

    // ── TEST 1 ────────────────────────────────────────────
    // Test: getMyProfile returns correct user data
    // ─────────────────────────────────────────────────────
    @Test
    void getMyProfile_shouldReturnUserProfile() {

        // ARRANGE
        when(authUtil.getCurrentUser()).thenReturn(currentUser);

        // ACT
        UserProfileDto result = userService.getMyProfile();

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getEmail())
                .isEqualTo("rahul@test.com");
        assertThat(result.getFirstName())
                .isEqualTo("Rahul");
        assertThat(result.getRole())
                .isEqualTo("RIDER");
    }

    // ── TEST 2 ────────────────────────────────────────────
    // Test: updateProfile updates only provided fields
    // ─────────────────────────────────────────────────────
    @Test
    void updateMyProfile_shouldUpdateOnlyProvidedFields() {

        // ARRANGE
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName("Rahul Updated");
        // lastName and phoneNumber are null — should not change

        when(authUtil.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.save(any(User.class)))
                .thenReturn(currentUser);

        // ACT
        UserProfileDto result =
                userService.updateMyProfile(request);

        // ASSERT
        // firstName should be updated
        assertThat(currentUser.getFirstName())
                .isEqualTo("Rahul Updated");

        // lastName should remain the same
        assertThat(currentUser.getLastName())
                .isEqualTo("Sharma");

        // save() should be called once
        verify(userRepository, times(1)).save(currentUser);
    }

    // ── TEST 3 ────────────────────────────────────────────
    // Test: deactivateMyAccount sets isActive to false
    // ─────────────────────────────────────────────────────
    @Test
    void deactivateMyAccount_shouldSetIsActiveFalse() {

        // ARRANGE
        when(authUtil.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.save(any(User.class)))
                .thenReturn(currentUser);

        // ACT
        String result = userService.deactivateMyAccount();

        // ASSERT
        assertThat(currentUser.getIsActive()).isFalse();
        assertThat(result)
                .contains("deactivated");
    }
}