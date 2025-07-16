package com.taskmanagement.controller.user;

import com.taskmanagement.dto.user.UserRequestDTO;
import com.taskmanagement.retresponse.SuccessResponse;
import com.taskmanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('USER')")
public class CurrentUserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public SuccessResponse getUserById(@PathVariable String id) {
        var user = userService.findById(id);
        return new SuccessResponse(user);
    }

    @PutMapping("/{id}")
    public SuccessResponse updateUser(
            @PathVariable String id,
            @RequestBody @Valid UserRequestDTO dto) {
        var response = userService.update(id, dto);
        return new SuccessResponse(response);
    }
}
