package com.taskmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.dto.auth.LoginRequest;
import com.taskmanagement.dto.auth.LoginResponse;
import com.taskmanagement.dto.auth.RegisterRequest;
import com.taskmanagement.dto.user.UserResponseDTO;
import com.taskmanagement.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "springdoc.api-docs.enabled=false",
        "springdoc.swagger-ui.enabled=false"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    // -------------------- SUCCESS --------------------
    @Test
    void testRegister_Success() throws Exception {
        RegisterRequest request = new RegisterRequest("hung123", "Hung123", "Vu Van Hung", "USER");

        UserResponseDTO responseDTO = new UserResponseDTO(
                "550e8400-e29b-41d4-a716-446655440000",
                "hung123",
                "Vu Van Hung",
                null,
                Instant.parse("2024-01-01T00:00:00Z")
        );

        when(authService.register(request)).thenReturn(responseDTO);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is("550e8400-e29b-41d4-a716-446655440000")))
                .andExpect(jsonPath("$.data.username", is("hung123")))
                .andExpect(jsonPath("$.data.fullName", is("Vu Van Hung")));
    }

    // -------------------- FAIL CASES --------------------

    @Test
    void testRegister_Fail_UsernameBlank() throws Exception {
        RegisterRequest request = new RegisterRequest("", "Hung123", "Hung Vu", "USER");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.data.errors[0].field", is("username")))
                .andExpect(jsonPath("$.data.errors[0].message", is("username is not empty")));
    }

    @Test
    void testRegister_Fail_PasswordBlank() throws Exception {
        RegisterRequest request = new RegisterRequest("hung123", "", "Hung Vu", "USER");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.data.errors[0].field", is("password")))
                .andExpect(jsonPath("$.data.errors[0].message", is("password is not empty")));
    }

    @Test
    void testRegister_Fail_PasswordTooShort() throws Exception {
        RegisterRequest request = new RegisterRequest("hung123", "H1", "Hung Vu", "USER");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.data.errors[0].field", is("password")))
                .andExpect(jsonPath("$.data.errors[0].message", is("password must be between 6 and 16 characters")));
    }

    @Test
    void testRegister_Fail_PasswordNoDigit() throws Exception {
        RegisterRequest request = new RegisterRequest("hung123", "abcdef", "Hung Vu", "USER");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.data.errors[0].field", is("password")))
                .andExpect(jsonPath("$.data.errors[0].message", is("password must contain at least one letter and one digit")));
    }

    @Test
    void testRegister_Fail_FullNameBlank() throws Exception {
        RegisterRequest request = new RegisterRequest("hung123", "Hung123", "", "USER");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.data.errors[0].field", is("fullName")))
                .andExpect(jsonPath("$.data.errors[0].message", is("full name is not empty")));
    }

    @Test
    void testRegister_Fail_RoleBlank() throws Exception {
        RegisterRequest request = new RegisterRequest("hung123", "Hung123", "Hung Vu", "");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.data.errors[0].field", is("role")))
                .andExpect(jsonPath("$.data.errors[0].message", is("role is not empty")));
    }

    // -------------------- LOGIN --------------------

    @Test
    void testLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest("hung123", "123456");

        LoginResponse response = new LoginResponse();
        response.setToken("mocked-token");

        when(authService.login(request)).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token", is("mocked-token")));
    }
}
