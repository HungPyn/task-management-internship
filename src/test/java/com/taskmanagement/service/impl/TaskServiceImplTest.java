package com.taskmanagement.service.impl;

import com.taskmanagement.dto.PaginatedResponse;
import com.taskmanagement.dto.task.TaskRequestDTO;
import com.taskmanagement.dto.task.TaskResponseDTO;
import com.taskmanagement.entity.Task;
import com.taskmanagement.entity.User;
import com.taskmanagement.enums.Status;
import com.taskmanagement.ex.AppException;
import com.taskmanagement.ex.ExceptionCode;
import com.taskmanagement.mapper.TaskMapper;
import com.taskmanagement.repository.TaskRepository;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.security.AuthHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceImplTest {

    @InjectMocks
    private TaskServiceImpl taskService;

    @Mock private TaskRepository taskRepository;
    @Mock private UserRepository userRepository;
    @Mock private TaskMapper taskMapper;
    @Mock private AuthHelper authHelper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetTasks_AsAdmin() {
        when(authHelper.isAdmin()).thenReturn(true);

        List<Task> taskList = List.of(new Task());
        Page<Task> page = new PageImpl<>(taskList);
        when(taskRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(taskMapper.toTaskResponseDTO(any())).thenReturn(new TaskResponseDTO());

        PaginatedResponse<TaskResponseDTO> result = taskService.getTasks(0, 10, null, "created", "desc");

        assertEquals(1, result.getItems().size());
    }

    @Test
    void testGetTasks_AsUser() {
        when(authHelper.isAdmin()).thenReturn(false);
        when(authHelper.getCurrentUserId()).thenReturn("user1");

        List<Task> taskList = List.of(new Task());
        Page<Task> page = new PageImpl<>(taskList);
        when(taskRepository.findByUserId(eq("user1"), any(Pageable.class))).thenReturn(page);
        when(taskMapper.toTaskResponseDTO(any())).thenReturn(new TaskResponseDTO());

        PaginatedResponse<TaskResponseDTO> result = taskService.getTasks(0, 10, null, "created", "desc");

        assertEquals(1, result.getItems().size());
    }

    @Test
    void testCreate_AsAdmin_Success() {
        Instant now = Instant.now();
        TaskRequestDTO dto = new TaskRequestDTO("Title", "Desc", "PENDING", now, now, now, "user123");

        User user = new User();
        user.setId("user123");

        when(authHelper.isAdmin()).thenReturn(true);
        when(userRepository.findById("user123")).thenReturn(Optional.of(user));
        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toTaskResponseDTO(any())).thenReturn(new TaskResponseDTO());

        TaskResponseDTO response = taskService.create(dto);
        assertNotNull(response);
    }

    @Test
    void testCreate_AsAdmin_Fail_MissingUserId() {
        TaskRequestDTO dto = new TaskRequestDTO("Title", "Desc", "PENDING", null, null, null, null);
        when(authHelper.isAdmin()).thenReturn(true);

        AppException ex = assertThrows(AppException.class, () -> taskService.create(dto));
        assertEquals(ExceptionCode.USER_ID_REQUIRED, ex.getCode());
    }

    @Test
    void testCreate_AsUser_Success() {
        Instant now = Instant.now();
        TaskRequestDTO dto = new TaskRequestDTO("Title", "Desc", "PENDING", now, now, now, null);

        User user = new User();
        user.setId("user-id");

        when(authHelper.isAdmin()).thenReturn(false);
        when(authHelper.getCurrentUserId()).thenReturn("user-id");
        when(userRepository.findById("user-id")).thenReturn(Optional.of(user));
        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(taskMapper.toTaskResponseDTO(any())).thenReturn(new TaskResponseDTO());

        TaskResponseDTO result = taskService.create(dto);
        assertNotNull(result);
    }

    @Test
    void testUpdate_Success_UserOwnsTask() {
        Task task = new Task();
        User user = new User(); user.setId("user-id");
        task.setUser(user);

        Instant now = Instant.now();
        TaskRequestDTO dto = new TaskRequestDTO("Updated", "Desc", "COMPLETED", now, now, now, null);

        when(taskRepository.findById("task-id")).thenReturn(Optional.of(task));
        when(authHelper.getCurrentUserId()).thenReturn("user-id");
        when(authHelper.isAdmin()).thenReturn(false);
        when(taskRepository.save(any())).thenReturn(task);
        when(taskMapper.toTaskResponseDTO(any())).thenReturn(new TaskResponseDTO());

        TaskResponseDTO result = taskService.update("task-id", dto);
        assertNotNull(result);
    }

    @Test
    void testUpdate_Fail_Unauthorized() {
        Task task = new Task();
        User user = new User(); user.setId("owner-id");
        task.setUser(user);

        Instant now = Instant.now();
        TaskRequestDTO dto = new TaskRequestDTO("Updated", "Desc", "COMPLETED", now, now, now, null);

        when(taskRepository.findById("task-id")).thenReturn(Optional.of(task));
        when(authHelper.getCurrentUserId()).thenReturn("other-id");
        when(authHelper.isAdmin()).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> taskService.update("task-id", dto));
        assertEquals(ExceptionCode.UNAUTHORIZED, ex.getCode());
    }

    @Test
    void testDelete_Success_Admin() {
        Task task = new Task();
        User user = new User(); user.setId("u1");
        task.setUser(user);

        when(taskRepository.findById("task-id")).thenReturn(Optional.of(task));
        when(authHelper.getCurrentUserId()).thenReturn("admin");
        when(authHelper.isAdmin()).thenReturn(true);

        assertDoesNotThrow(() -> taskService.delete("task-id"));
        verify(taskRepository).delete(task);
    }

    @Test
    void testDelete_Fail_NotOwner() {
        Task task = new Task();
        User user = new User(); user.setId("owner-id");
        task.setUser(user);

        when(taskRepository.findById("task-id")).thenReturn(Optional.of(task));
        when(authHelper.getCurrentUserId()).thenReturn("user2");
        when(authHelper.isAdmin()).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () -> taskService.delete("task-id"));
        assertEquals(ExceptionCode.UNAUTHORIZED, ex.getCode());
    }
}
