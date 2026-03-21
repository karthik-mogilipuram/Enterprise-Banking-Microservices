package com.enterprise.banking.auth.service;

import com.enterprise.banking.auth.exception.UserAlreadyExistsException;
import com.enterprise.banking.auth.model.Role;
import com.enterprise.banking.auth.model.User;
import com.enterprise.banking.auth.model.dto.AuthResponse;
import com.enterprise.banking.auth.model.dto.LoginRequest;
import com.enterprise.banking.auth.model.dto.RegisterRequest;
import com.enterprise.banking.auth.model.dto.UserProfileResponse;
import com.enterprise.banking.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(testUserId)
                .email("Karthik@test.com")
                .passwordHash("encoded_password")
                .fullName("Karthik Mogilipuram")
                .phone("1234567890")
                .role(Role.USER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void register_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Karthik Mogilipuram")
                .email("Karthik@test.com")
                .password("password123")
                .phone("1234567890")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtService.generateAccessToken(any(), anyString(), anyString())).thenReturn("access_token");
        when(jwtService.generateRefreshToken(any(), anyString())).thenReturn("refresh_token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("Karthik@test.com", response.getEmail());
        assertEquals("Karthik Mogilipuram", response.getFullName());
        assertEquals("access_token", response.getAccessToken());
        assertEquals("refresh_token", response.getRefreshToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Karthik Mogilipuram")
                .email("Karthik@test.com")
                .password("password123")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_Success() {
        LoginRequest request = LoginRequest.builder()
                .email("Karthik@test.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
        when(jwtService.generateAccessToken(any(), anyString(), anyString())).thenReturn("access_token");
        when(jwtService.generateRefreshToken(any(), anyString())).thenReturn("refresh_token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("Karthik@test.com", response.getEmail());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void getProfile_Success() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        UserProfileResponse response = authService.getProfile(testUserId);

        assertNotNull(response);
        assertEquals(testUserId, response.getId());
        assertEquals("Karthik@test.com", response.getEmail());
        assertEquals("Karthik Mogilipuram", response.getFullName());
        assertEquals(Role.USER, response.getRole());
    }

    @Test
    void getProfile_NotFound_ThrowsException() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.getProfile(testUserId));
    }
}