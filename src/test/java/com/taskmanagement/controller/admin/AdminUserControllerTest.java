package com.taskmanagement.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.dto.PaginatedResponse;
import com.taskmanagement.dto.user.UserRequestDTO;
import com.taskmanagement.dto.user.UserResponseDTO;
import com.taskmanagement.enums.Role;
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
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "springdoc.api-docs.enabled=false",
        "springdoc.swagger-ui.enabled=false"
})
@WithMockUser(authorities = "ADMIN")
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    // ---------------- SUCCESS CASES ----------------

    @Test
    void testGetAllUsers_Success() throws Exception {
        UserResponseDTO dto = new UserResponseDTO("id", "username", "Full Name", Role.USER, Instant.now());
        PaginatedResponse<UserResponseDTO> paginated = new PaginatedResponse<>(List.of(dto), 0, 1, 1, false);

        when(userService.findAll(0, 10)).thenReturn(paginated);

        mockMvc.perform(get("/api/admin/users?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].username", is("username")));
    }

    @Test
    void testGetUserById_Success() throws Exception {
        String userId = "123";
        UserResponseDTO dto = new UserResponseDTO(userId, "username", "Full Name", Role.ADMIN, Instant.now());

        when(userService.findById(userId)).thenReturn(dto);

        mockMvc.perform(get("/api/admin/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username", is("username")));
    }

    @Test
    void testCreateUser_Success() throws Exception {
        UserRequestDTO request = new UserRequestDTO("username", "Hung123", "Full Name", "USER");
        UserResponseDTO response = new UserResponseDTO("id", "username", "Full Name", Role.USER, Instant.now());

        when(userService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username", is("username")))
                .andExpect(jsonPath("$.data.fullName", is("Full Name")));
    }

    @Test
    void testUpdateUser_Success() throws Exception {
        String userId = "123";
        UserRequestDTO request = new UserRequestDTO("username", "Hung123", "Updated Name", "ADMIN");
        UserResponseDTO response = new UserResponseDTO(userId, "username", "Updated Name", Role.ADMIN, Instant.now());

        when(userService.update(any(), any())).thenReturn(response);

        mockMvc.perform(put("/api/admin/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName", is("Updated Name")));
    }

    @Test
    void testDeleteUser_Success() throws Exception {
        mockMvc.perform(delete("/api/admin/users/123"))
                .andExpect(status().isOk());
    }

    // ---------------- FAIL CASES ----------------

    @Test
    void testCreateUser_Fail_UsernameBlank() throws Exception {
        UserRequestDTO request = new UserRequestDTO("", "Hung123", "Full Name", "USER");

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.data.errors[0].field", is("username")));
    }

    @Test
    void testCreateUser_Fail_PasswordInvalid() throws Exception {
        UserRequestDTO request = new UserRequestDTO("username", "123", "Full Name", "USER");

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.data.errors[0].field", is("password")));
    }

    @Test
    void testUpdateUser_Fail_FullNameBlank() throws Exception {
        String userId = "123";
        UserRequestDTO request = new UserRequestDTO("username", "Hung123", "", "ADMIN");

        mockMvc.perform(put("/api/admin/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.data.errors[0].field", is("fullName")));
    }
}
