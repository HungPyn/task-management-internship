package com.taskmanagement.service;

import com.taskmanagement.dto.PaginatedResponse;
import com.taskmanagement.dto.task.TaskRequestDTO;
import com.taskmanagement.dto.task.TaskResponseDTO;
import com.taskmanagement.enums.Status;

import java.util.UUID;

public interface TaskService {

    public PaginatedResponse<TaskResponseDTO> getTasks(int page, int size, Status status, String sortBy, String direction);

    TaskResponseDTO create(TaskRequestDTO dto);

    TaskResponseDTO update(String id, TaskRequestDTO dto);

    void delete(String taskId);
}
