package com.taskmanagement.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.dto.user.UserRequestDTO;
import com.taskmanagement.dto.user.UserResponseDTO;
import com.taskmanagement.security.AuthHelper;
import com.taskmanagement.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "springdoc.api-docs.enabled=false",
        "springdoc.swagger-ui.enabled=false"
})
class CurrentUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthHelper authHelper;

    // ------------------- GET USER -------------------
    @Test
    @WithMockUser(username = "uuid-123", authorities = "USER")
    void testGetUserById_Success() throws Exception {
        String userId = "uuid-123";

        UserResponseDTO responseDTO = new UserResponseDTO(
                userId,
                "hung123",
                "Hung Vu",
                null,
                Instant.parse("2024-01-01T00:00:00Z")
        );

        when(authHelper.getCurrentUserId()).thenReturn(userId);
        when(authHelper.isAdmin()).thenReturn(false);
        when(userService.findById(userId)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/user/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(userId)))
                .andExpect(jsonPath("$.data.username", is("hung123")))
                .andExpect(jsonPath("$.data.fullName", is("Hung Vu")));
    }

    @Test
    void testGetUserById_Fail_Forbidden() throws Exception {
        mockMvc.perform(get("/api/user/uuid-123"))
                .andExpect(status().isForbidden());
    }

    // ------------------- UPDATE USER -------------------
    @Test
    @WithMockUser(username = "uuid-123", authorities = "USER")
    void testUpdateUser_Success() throws Exception {
        String userId = "uuid-123";
        UserRequestDTO request = new UserRequestDTO("hung123", "Hung123", "Hung Vu", "USER");

        UserResponseDTO responseDTO = new UserResponseDTO(
                userId,
                "hung123",
                "Hung Vu",
                null,
                Instant.parse("2024-01-01T00:00:00Z")
        );

        when(authHelper.getCurrentUserId()).thenReturn(userId);
        when(authHelper.isAdmin()).thenReturn(false);
        when(userService.update(userId, request)).thenReturn(responseDTO);

        mockMvc.perform(put("/api/user/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id", is(userId)))
                .andExpect(jsonPath("$.data.username", is("hung123")))
                .andExpect(jsonPath("$.data.fullName", is("Hung Vu")));
    }

    @Test
    @WithMockUser(username = "uuid-123", authorities = "USER")
    void testUpdateUser_Fail_InvalidPassword() throws Exception {
        String userId = "uuid-123";
        UserRequestDTO request = new UserRequestDTO("hung123", "123", "Hung Vu", "USER");

        mockMvc.perform(put("/api/user/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.data.errors[0].field", is("password")));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testUpdateUser_Fail_FullNameBlank() throws Exception {
        String userId = "550e8400-e29b-41d4-a716-446655440000";

        UserRequestDTO request = new UserRequestDTO(
                "hung123",
                "Hung123",
                "",
                "USER"
        );

        mockMvc.perform(put("/api/user/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // HTTP 200 vì controller luôn return SuccessResponse
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.data.errors[0].field", is("fullName")))
                .andExpect(jsonPath("$.data.errors[0].message", is("full name is not empty")));
    }

    @Test
    void testUpdateUser_Fail_Forbidden() throws Exception {
        UserRequestDTO request = new UserRequestDTO("hung123", "Hung123", "Hung Vu", "USER");

        mockMvc.perform(put("/api/user/uuid-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden()); // Không có quyền USER
    }
}
