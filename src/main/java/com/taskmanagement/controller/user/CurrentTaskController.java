package com.taskmanagement.controller.user;

import com.taskmanagement.dto.task.TaskRequestDTO;
import com.taskmanagement.dto.user.UserRequestDTO;
import com.taskmanagement.enums.Status;
import com.taskmanagement.retresponse.SuccessResponse;
import com.taskmanagement.service.TaskService;
import com.taskmanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user/tasks")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('USER')")
public class CurrentTaskController {

    private final TaskService taskService;

    @GetMapping
    public SuccessResponse getTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Status status,
            @RequestParam(defaultValue = "created") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        var result = taskService.getTasks(page, size, status, sortBy, direction);
        return new SuccessResponse(result);
    }

    @GetMapping("/{id}")
    public SuccessResponse getTaskById(@PathVariable String id) {
        return new SuccessResponse();
    }

    @PostMapping
    public SuccessResponse createTask(@RequestBody @Valid TaskRequestDTO dto) {
        var response =taskService.create(dto);
        return new SuccessResponse(response);
    }

    @PutMapping("/{id}")
    public SuccessResponse updateTask(@PathVariable String id,@RequestBody @Valid TaskRequestDTO dto) {
        var response = taskService.update(id,dto);
        return new SuccessResponse(response);
    }
}
