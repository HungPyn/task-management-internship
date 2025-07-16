package com.taskmanagement.controller;

import com.taskmanagement.dto.auth.LoginRequest;
import com.taskmanagement.dto.auth.RegisterRequest;
import com.taskmanagement.retresponse.SuccessResponse;
import com.taskmanagement.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public SuccessResponse register(@RequestBody @Valid RegisterRequest dto) {
        var data = authService.register(dto);
        return new SuccessResponse(data);
    }

    @PostMapping("/login")
    public SuccessResponse login(@RequestBody @Valid LoginRequest dto) {
        var data = authService.login(dto);
        return new SuccessResponse(data);
    }
}
