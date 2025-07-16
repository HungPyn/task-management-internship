package com.taskmanagement.service;

import com.taskmanagement.dto.auth.LoginRequest;
import com.taskmanagement.dto.auth.LoginResponse;
import com.taskmanagement.dto.auth.RegisterRequest;
import com.taskmanagement.dto.user.UserResponseDTO;

public interface AuthService {

    UserResponseDTO register(RegisterRequest dto);

    LoginResponse login(LoginRequest dto);

}
