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
import com.taskmanagement.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final AuthHelper authHelper;

    // ============================= GET TASKS =============================

    @Override
    public PaginatedResponse<TaskResponseDTO> getTasks(int page, int size, Status status, String sortBy, String direction) {
        if (authHelper.isAdmin()) {
            return getTasksForAdmin(page, size, status, sortBy, direction);
        } else {
            return getTasksForUser(page, size, status, sortBy, direction);
        }
    }

    private PaginatedResponse<TaskResponseDTO> getTasksForAdmin(int page, int size, Status status, String sortBy, String direction) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        Page<Task> taskPage = (status != null)
                ? taskRepository.findByStatus(status, pageable)
                : taskRepository.findAll(pageable);
        return toPaginatedResponse(taskPage, "ADMIN");
    }

    private PaginatedResponse<TaskResponseDTO> getTasksForUser(int page, int size, Status status, String sortBy, String direction) {
        String currentUserId = authHelper.getCurrentUserId();
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        Page<Task> taskPage = (status != null)
                ? taskRepository.findByUserIdAndStatus(currentUserId, status, pageable)
                : taskRepository.findByUserId(currentUserId, pageable);
        return toPaginatedResponse(taskPage, currentUserId.toString());
    }

    private Pageable buildPageable(int page, int size, String sortBy, String direction) {
        List<String> allowedSortFields = List.of("created", "deadline");
        if (!allowedSortFields.contains(sortBy)) {
            log.warn("Invalid sort field '{}'. Defaulting to 'created'.", sortBy);
            sortBy = "created";
        }

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        return PageRequest.of(page, size, sort);
    }

    private PaginatedResponse<TaskResponseDTO> toPaginatedResponse(Page<Task> taskPage, String ownerIdentifier) {
        List<TaskResponseDTO> dtoList = taskPage.getContent().stream()
                .map(taskMapper::toTaskResponseDTO)
                .toList();

        log.info("Fetched tasks for '{}': page={}, totalElements={}, totalPages={}",
                ownerIdentifier, taskPage.getNumber(), taskPage.getTotalElements(), taskPage.getTotalPages());

        return new PaginatedResponse<>(
                dtoList,
                taskPage.getNumber(),
                (int) taskPage.getTotalElements(),
                taskPage.getTotalPages(),
                taskPage.hasNext()
        );
    }

    // ============================= CREATE =============================

    @Override
    public TaskResponseDTO create(TaskRequestDTO dto) {
        String targetUserId;

        if (authHelper.isAdmin()) {
            if (dto.getUserId() == null) {
                log.warn("Admin attempted to create a task without providing userId.");
                throw new AppException(ExceptionCode.USER_ID_REQUIRED);
            }
            targetUserId = dto.getUserId();
            log.info("Admin is creating a task for userId: {}", targetUserId);
        } else {
            if (dto.getUserId() != null && !dto.getUserId().equals(authHelper.getCurrentUserId())) {
                log.warn("User {} attempted to create a task for a different userId: {}", authHelper.getCurrentUserId(), dto.getUserId());
                throw new AppException(ExceptionCode.UNAUTHORIZED);
            }
            targetUserId = authHelper.getCurrentUserId();
            log.info("User (id: {}) is creating a task for themselves.", targetUserId);
        }

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", targetUserId);
                    return new AppException(ExceptionCode.USER_NOT_FOUND);
                });

        Task task = new Task();
        task.setUser(user);
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setDeadline(dto.getDeadline());
        task.setCreated(Instant.now());
        task.setStatus(Status.fromString(dto.getStatus()));

        taskRepository.save(task);

        log.info("Task successfully created for user: {}, title: '{}'", user.getUsername(), dto.getTitle());

        return taskMapper.toTaskResponseDTO(task);
    }


    // ============================= UPDATE =============================

    @Override
    public TaskResponseDTO update(String taskId, TaskRequestDTO dto) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.warn("Task not found with id: {}", taskId);
                    return new AppException(ExceptionCode.TASK_NOT_FOUND);
                });

        String currentUserId = authHelper.getCurrentUserId();
        boolean isAdmin = authHelper.isAdmin();

        if (!isAdmin) {
            if (dto.getUserId() != null && !dto.getUserId().equals(currentUserId)) {
                log.warn("User {} attempted to update task {} with a different userId: {}", currentUserId, taskId, dto.getUserId());
                throw new AppException(ExceptionCode.UNAUTHORIZED);
            }

            if (!task.getUser().getId().equals(currentUserId)) {
                log.warn("Unauthorized update attempt by userId: {} for taskId: {}", currentUserId, taskId);
                throw new AppException(ExceptionCode.UNAUTHORIZED);
            }
        }

        if (isAdmin && dto.getUserId() != null && !dto.getUserId().equals(task.getUser().getId())) {
            User newUser = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> {
                        log.warn("User not found with id: {}", dto.getUserId());
                        return new AppException(ExceptionCode.USER_NOT_FOUND);
                    });
            task.setUser(newUser);
            log.info("Admin reassigned taskId: {} to userId: {}", taskId, dto.getUserId());
        }

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setDeadline(dto.getDeadline());
        task.setStatus(Status.fromString(dto.getStatus()));

        taskRepository.save(task);

        log.info("Task updated successfully. taskId: {}, updatedBy: {}", taskId, currentUserId);

        return taskMapper.toTaskResponseDTO(task);
    }

    // ============================= DELETE =============================

    @Override
    public void delete(String taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.warn("Task not found with id: {}", taskId);
                    return new AppException(ExceptionCode.TASK_NOT_FOUND);
                });

        String currentUserId = authHelper.getCurrentUserId();
        boolean isAdmin = authHelper.isAdmin();

        if (!isAdmin && !task.getUser().getId().equals(currentUserId)) {
            log.warn("Unauthorized delete attempt by userId: {} for taskId: {}", currentUserId, taskId);
            throw new AppException(ExceptionCode.UNAUTHORIZED);
        }

        taskRepository.delete(task);
        log.info("Task deleted successfully. taskId: {}, deletedBy: {}", taskId, currentUserId);
    }
}
