package com.taskmanagement.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.dto.PaginatedResponse;
import com.taskmanagement.dto.task.TaskRequestDTO;
import com.taskmanagement.dto.task.TaskResponseDTO;
import com.taskmanagement.enums.Status;
import com.taskmanagement.security.AuthHelper;
import com.taskmanagement.service.TaskService;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "springdoc.api-docs.enabled=false",
        "springdoc.swagger-ui.enabled=false"
})
class TaskAdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private TaskService taskService;
    @MockBean private AuthHelper authHelper;

    private final Instant now = Instant.now();

    // ------------------- GET LIST -------------------
    @Test
    @WithMockUser(authorities = "ADMIN")
    void testGetTasks_Success() throws Exception {
        PaginatedResponse<TaskResponseDTO> mockResponse = new PaginatedResponse<>(
                List.of(new TaskResponseDTO()), 0, 1, 1, false
        );
        when(taskService.getTasks(anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/admin/tasks")
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)));
    }

    @Test
    void testGetTasks_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/tasks"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testGetTasks_Forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/tasks"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testGetTasks_InvalidStatus() throws Exception {
        mockMvc.perform(get("/api/admin/tasks")
                        .param("status", "INVALID")) // not in enum
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", containsString("Failed to convert")));
    }

    // ------------------- CREATE -------------------
    @Test
    @WithMockUser(authorities = "ADMIN")
    void testCreateTask_Success() throws Exception {
        TaskRequestDTO request = new TaskRequestDTO("New Task", "Desc", "PENDING", now, now, now, null);
        TaskResponseDTO response = new TaskResponseDTO();

        when(taskService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/admin/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testCreateTask_InvalidInput() throws Exception {
        TaskRequestDTO request = new TaskRequestDTO("", "Desc", "PENDING", now, now, now, null); // invalid: empty title

        mockMvc.perform(post("/api/admin/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.data.errors[0].field", is("title")));
    }

    // ------------------- UPDATE -------------------
    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUpdateTask_Success() throws Exception {
        TaskRequestDTO request = new TaskRequestDTO("Updated", "Desc", "COMPLETED", now, now, now, null);
        TaskResponseDTO response = new TaskResponseDTO();

        when(taskService.update(eq("task-id"), any())).thenReturn(response);

        mockMvc.perform(put("/api/admin/tasks/{id}", "task-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUpdateTask_InvalidStatus() throws Exception {
        TaskRequestDTO request = new TaskRequestDTO("Updated", "Desc", "INVALID", now, now, now, null);

        mockMvc.perform(put("/api/admin/tasks/{id}", "task-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(400)));
    }

    // ------------------- DELETE -------------------
    @Test
    @WithMockUser(authorities = "ADMIN")
    void testDeleteTask_Success() throws Exception {
        doNothing().when(taskService).delete("task-id");

        mockMvc.perform(delete("/api/admin/tasks/{id}", "task-id"))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteTask_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/admin/tasks/{id}", "task-id"))
                .andExpect(status().isForbidden());
    }
}
