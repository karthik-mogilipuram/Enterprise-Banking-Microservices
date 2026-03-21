package com.enterprise.banking.auth.controller;

import com.enterprise.banking.auth.model.Role;
import com.enterprise.banking.auth.model.dto.AuthResponse;
import com.enterprise.banking.auth.model.dto.LoginRequest;
import com.enterprise.banking.auth.model.dto.RegisterRequest;
import com.enterprise.banking.auth.model.dto.UserProfileResponse;
import com.enterprise.banking.auth.security.JwtAuthFilter;
import com.enterprise.banking.auth.service.AuthService;
import com.enterprise.banking.auth.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private JwtAuthFilter jwtAuthFilter;

    private final UUID testUserId = UUID.randomUUID();

    @Test
    void register_ValidRequest_Returns201() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Venkat Ramana")
                .email("venkat@test.com")
                .password("password123")
                .phone("1234567890")
                .build();

        AuthResponse response = AuthResponse.builder()
                .userId(testUserId)
                .email("venkat@test.com")
                .fullName("Venkat Ramana")
                .role(Role.USER)
                .accessToken("access_token")
                .refreshToken("refresh_token")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("venkat@test.com"))
                .andExpect(jsonPath("$.fullName").value("Venkat Ramana"))
                .andExpect(jsonPath("$.accessToken").value("access_token"));
    }

    @Test
    void register_InvalidEmail_Returns400() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Venkat Ramana")
                .email("invalid-email")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_MissingPassword_Returns400() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Venkat Ramana")
                .email("venkat@test.com")
                .build();

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ValidRequest_Returns200() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("venkat@test.com")
                .password("password123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .userId(testUserId)
                .email("venkat@test.com")
                .fullName("Venkat Ramana")
                .role(Role.USER)
                .accessToken("access_token")
                .refreshToken("refresh_token")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("venkat@test.com"))
                .andExpect(jsonPath("$.accessToken").value("access_token"));
    }

    @Test
    void refresh_EmptyToken_Returns400() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getProfile_Authenticated_Returns200() throws Exception {
        UserProfileResponse profile = UserProfileResponse.builder()
                .id(testUserId)
                .email("venkat@test.com")
                .fullName("Venkat Ramana")
                .phone("1234567890")
                .role(Role.USER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(authService.getProfile(any(UUID.class))).thenReturn(profile);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk());
    }

    @Test
    void getProfile_Unauthenticated_Returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}