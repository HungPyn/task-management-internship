package com.taskmanagement.service.impl;

import com.taskmanagement.dto.auth.LoginRequest;
import com.taskmanagement.dto.auth.LoginResponse;
import com.taskmanagement.dto.auth.RegisterRequest;
import com.taskmanagement.dto.user.UserResponseDTO;
import com.taskmanagement.entity.User;
import com.taskmanagement.enums.Role;
import com.taskmanagement.ex.AppException;
import com.taskmanagement.ex.ExceptionCode;
import com.taskmanagement.mapper.UserMapper;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.security.CustomUserDetail;
import com.taskmanagement.security.JwtTokenProvider;
import com.taskmanagement.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final JwtTokenProvider jwtTokenProvider;

        @Override
        public UserResponseDTO register(RegisterRequest dto) {

            if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
                log.warn("Attempt to register with existing username: {}", dto.getUsername());
                throw new AppException(ExceptionCode.USER_FOUND);
            }
            Instant created = Instant.now();
            Role role = Role.fromString(dto.getRole());
            String password = passwordEncoder.encode(dto.getPassword());
            User user = new User();
            user.setUsername(dto.getUsername());
            user.setFullName(dto.getFullName());
            user.setPassword(password);
            user.setCreated(created);
            user.setRole(role);
            userRepository.save(user);
            log.info("Registering new user: {}, role: {}", dto.getUsername(), role);

            UserResponseDTO dtoResponse = userMapper.toUserResponseDTO(user);
            if (!role.equals(Role.ADMIN)) {
                dtoResponse.setRole(null);
            }
            return dtoResponse;
        }

    @Override
    public LoginResponse login(LoginRequest dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new AppException(ExceptionCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {

            log.warn("Incorrect password for user: {}", dto.getUsername());
            throw new AppException(ExceptionCode.PASSWORD_INCORRECT);
        }

        String token = jwtTokenProvider.generateToken(new CustomUserDetail(user));
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(token);
        log.info("Generated JWT for user: {}", dto.getUsername());
        return loginResponse;
    }
}
