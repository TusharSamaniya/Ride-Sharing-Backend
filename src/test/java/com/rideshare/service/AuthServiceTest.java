package com.rideshare.service;

import com.rideshare.dto.auth.LoginRequest;
import com.rideshare.dto.auth.RegisterRequest;
import com.rideshare.dto.auth.AuthResponse;
import com.rideshare.entity.User;
import com.rideshare.entity.UserRole;
import com.rideshare.repository.UserRepository;
import com.rideshare.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// @ExtendWith tells JUnit to use Mockito
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // @Mock creates a FAKE version of each dependency
    // No real database, no real JWT — all fake controlled objects
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    // @InjectMocks creates a REAL AuthService
    // but injects the fake mocks above into it
    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private User savedUser;

    // @BeforeEach runs before every single test method
    // Use it to set up common test data
    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("Rahul");
        registerRequest.setLastName("Sharma");
        registerRequest.setEmail("rahul@test.com");
        registerRequest.setPassword("password123");
        registerRequest.setPhoneNumber("9876543210");
        registerRequest.setRole(UserRole.RIDER);

        savedUser = new User();
        savedUser.setId(1L);
        savedUser.setFirstName("Rahul");
        savedUser.setLastName("Sharma");
        savedUser.setEmail("rahul@test.com");
        savedUser.setPassword("hashedPassword");
        savedUser.setRole(UserRole.RIDER);
        savedUser.setIsActive(true);
    }

    // ── TEST 1 ────────────────────────────────────────────
    // Test: register with a new email → should succeed
    // ─────────────────────────────────────────────────────
    @Test
    void register_withNewEmail_shouldReturnAuthResponse() {

        // ARRANGE — set up what the fake objects will return
        // when userRepository.existsByEmail() is called → return false
        // meaning: email does not exist yet
        when(userRepository.existsByEmail("rahul@test.com"))
                .thenReturn(false);

        // when passwordEncoder.encode() is called → return fake hash
        when(passwordEncoder.encode("password123"))
                .thenReturn("hashedPassword");

        // when userRepository.save() is called → return our savedUser
        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        // when jwtUtil.generateToken() is called → return fake token
        when(jwtUtil.generateToken(any()))
                .thenReturn("fake.access.token");

        when(jwtUtil.generateRefreshToken(any()))
                .thenReturn("fake.refresh.token");

        // ACT — call the actual method we are testing
        AuthResponse response = authService.register(registerRequest);

        // ASSERT — check the result is what we expected
        assertThat(response).isNotNull();
        assertThat(response.getEmail())
                .isEqualTo("rahul@test.com");
        assertThat(response.getFirstName())
                .isEqualTo("Rahul");
        assertThat(response.getAccessToken())
                .isEqualTo("fake.access.token");
        assertThat(response.getRole())
                .isEqualTo("RIDER");

        // Verify that save() was called exactly once
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ── TEST 2 ────────────────────────────────────────────
    // Test: register with existing email → should throw error
    // ─────────────────────────────────────────────────────
    @Test
    void register_withExistingEmail_shouldThrowException() {

        // ARRANGE — email already exists in database
        when(userRepository.existsByEmail("rahul@test.com"))
                .thenReturn(true);

        // ASSERT + ACT — expect an exception to be thrown
        assertThatThrownBy(() ->
                authService.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already registered");

        // Verify that save() was NEVER called
        // because registration should have failed
        verify(userRepository, never()).save(any());
    }

    // ── TEST 3 ────────────────────────────────────────────
    // Test: login with correct credentials → should succeed
    // ─────────────────────────────────────────────────────
    @Test
    void login_withCorrectCredentials_shouldReturnAuthResponse() {

        // ARRANGE
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("rahul@test.com");
        loginRequest.setPassword("password123");

        // authenticationManager.authenticate() does not throw
        // means credentials are correct
        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);

        when(userRepository.findByEmail("rahul@test.com"))
                .thenReturn(Optional.of(savedUser));

        when(jwtUtil.generateToken(any()))
                .thenReturn("fake.access.token");

        when(jwtUtil.generateRefreshToken(any()))
                .thenReturn("fake.refresh.token");

        // ACT
        AuthResponse response = authService.login(loginRequest);

        // ASSERT
        assertThat(response).isNotNull();
        assertThat(response.getEmail())
                .isEqualTo("rahul@test.com");
        assertThat(response.getAccessToken())
                .isNotNull();
    }
}