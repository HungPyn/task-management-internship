package com.taskmanagement.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.dto.PaginatedResponse;
import com.taskmanagement.dto.task.TaskRequestDTO;
import com.taskmanagement.dto.task.TaskResponseDTO;
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
class CurrentTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private AuthHelper authHelper;

    private final Instant now = Instant.now();

    // ------------------- GET TASK LIST -------------------

    @Test
    @WithMockUser(authorities = "USER")
    void testGetTasks_Success() throws Exception {
        PaginatedResponse<TaskResponseDTO> mockResponse = new PaginatedResponse<>(
                List.of(new TaskResponseDTO()), 0, 1, 1, false
        );

        when(taskService.getTasks(anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/user/tasks")
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "COMPLETED")
                        .param("sortBy", "created")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.page", is(0)))
                .andExpect(jsonPath("$.data.totalItems", is(1)))
                .andExpect(jsonPath("$.data.totalPages", is(1)))
                .andExpect(jsonPath("$.data.hasNext", is(false)));
    }

    @Test
    void testGetTasks_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/user/tasks"))
                .andExpect(status().isForbidden()); // default security response
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testGetTasks_Forbidden() throws Exception {
        mockMvc.perform(get("/api/user/tasks"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testGetTasks_InvalidStatusParam() throws Exception {
        mockMvc.perform(get("/api/user/tasks")
                        .param("status", "DONE")) // not valid enum
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", containsString("Invalid status")));
    }

    // ------------------- CREATE TASK -------------------

    @Test
    @WithMockUser(authorities = "USER")
    void testCreateTask_Success() throws Exception {
        TaskRequestDTO request = new TaskRequestDTO("Test task", "Desc", "COMPLETED", now, now, now, null);
        TaskResponseDTO response = new TaskResponseDTO();

        when(taskService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/user/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void testCreateTask_Unauthorized() throws Exception {
        TaskRequestDTO request = new TaskRequestDTO("Test task", "Desc", "COMPLETED", now, now, now, null);

        mockMvc.perform(post("/api/user/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testCreateTask_Forbidden() throws Exception {
        TaskRequestDTO request = new TaskRequestDTO("Test task", "Desc", "COMPLETED", now, now, now, null);

        mockMvc.perform(post("/api/user/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testCreateTask_Fail_InvalidField() throws Exception {
        TaskRequestDTO request = new TaskRequestDTO("", "Desc", "COMPLETED", now, now, now, null);

        mockMvc.perform(post("/api/user/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Dữ liệu không hợp lệ")))
                .andExpect(jsonPath("$.data.errors[0].field", is("title")));
    }

    // ------------------- UPDATE TASK -------------------

    @Test
    @WithMockUser(authorities = "USER")
    void testUpdateTask_Success() throws Exception {
        TaskRequestDTO request = new TaskRequestDTO("Updated", "Desc", "COMPLETED", now, now, now, null);
        TaskResponseDTO response = new TaskResponseDTO();

        when(taskService.update(eq("task-id"), any())).thenReturn(response);

        mockMvc.perform(put("/api/user/tasks/{id}", "task-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void testUpdateTask_Unauthorized() throws Exception {
        TaskRequestDTO request = new TaskRequestDTO("Updated", "Desc", "COMPLETED", now, now, now, null);

        mockMvc.perform(put("/api/user/tasks/{id}", "task-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden()); // bị block bởi security
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void testUpdateTask_Forbidden() throws Exception {
        TaskRequestDTO request = new TaskRequestDTO("Updated", "Desc", "COMPLETED", now, now, now, null);

        mockMvc.perform(put("/api/user/tasks/{id}", "task-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void testUpdateTask_Fail_InvalidStatus() throws Exception {
        TaskRequestDTO request = new TaskRequestDTO("Updated", "Desc", "INVALID_STATUS", now, now, now, null);

        mockMvc.perform(put("/api/user/tasks/{id}", "task-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Dữ liệu không hợp lệ")))
                .andExpect(jsonPath("$.data.errors[0].field", is("status")))
                .andExpect(jsonPath("$.data.errors[0].message", containsString("Invalid status")));
    }

}
