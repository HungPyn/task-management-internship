package com.taskmanagement.controller.admin;

import com.taskmanagement.dto.user.UserRequestDTO;
import com.taskmanagement.retresponse.SuccessResponse;
import com.taskmanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public SuccessResponse getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var result = userService.findAll(page, size);
        return new SuccessResponse(result);
    }
    @GetMapping("/{id}")
    public SuccessResponse getUserById(@PathVariable String id) {
        var user = userService.findById(id);
        return new SuccessResponse(user);
    }

    @PostMapping
    public SuccessResponse createUser(@RequestBody @Valid UserRequestDTO dto) {
        var response = userService.create(dto);
        return new SuccessResponse(response);
    }

    @PutMapping("/{id}")
    public SuccessResponse updateUser(
            @PathVariable String id,
            @RequestBody @Valid UserRequestDTO dto) {
        var response = userService.update(id, dto);
        return new SuccessResponse(response);
    }

    @DeleteMapping("/{id}")
    public SuccessResponse deleteUser(@PathVariable String id) {
        userService.delete(id);
        return new SuccessResponse();
    }
}

