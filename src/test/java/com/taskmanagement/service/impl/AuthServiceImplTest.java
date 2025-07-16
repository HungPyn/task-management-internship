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
import com.taskmanagement.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest("hung123", "123a56", "Vu Van Hung", "USER");

        when(userRepository.findByUsername("hung123")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123a56")).thenReturn("encoded-password");

        UUID userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        User savedUser = new User();
        savedUser.setId(userId.toString());
        savedUser.setUsername("hung123");
        savedUser.setFullName("Vu Van Hung");
        savedUser.setPassword("encoded-password");
        savedUser.setCreated(Instant.parse("2024-01-01T00:00:00Z"));
        savedUser.setRole(Role.USER);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponseDTO responseDTO = new UserResponseDTO(
                userId.toString(), "hung123", "Vu Van Hung", null, Instant.parse("2024-01-01T00:00:00Z")
        );

        when(userMapper.toUserResponseDTO(any(User.class))).thenReturn(responseDTO);

        UserResponseDTO result = authService.register(request);

        assertEquals("hung123", result.getUsername());
        assertEquals("Vu Van Hung", result.getFullName());
        assertEquals(userId.toString(), result.getId());
    }


    @Test
    void testRegister_UsernameExists() {
        RegisterRequest request = new RegisterRequest("hung123", "123a56", "Vu Van Hung", "USER");

        when(userRepository.findByUsername("hung123")).thenReturn(Optional.of(new User()));

        AppException ex = assertThrows(AppException.class, () -> authService.register(request));
        assertEquals(ExceptionCode.USER_FOUND, ex.getCode());
    }

    @Test
    void testLogin_Success() {
        LoginRequest request = new LoginRequest("hung123", "mypassword");

        User user = new User();
        user.setUsername("hung123");
        user.setPassword("encoded-password");

        when(userRepository.findByUsername("hung123")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("mypassword", "encoded-password")).thenReturn(true);
        when(jwtTokenProvider.generateToken(any())).thenReturn("mocked-jwt");

        LoginResponse response = authService.login(request);

        assertEquals("mocked-jwt", response.getToken());
    }

    @Test
    void testLogin_UserNotFound() {
        when(userRepository.findByUsername("notfound_user")).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () ->
                authService.login(new LoginRequest("notfound_user", "pass")));

        assertEquals(ExceptionCode.USER_NOT_FOUND, ex.getCode());
    }

    @Test
    void testLogin_IncorrectPassword() {
        User user = new User();
        user.setUsername("hung123");
        user.setPassword("encoded");

        when(userRepository.findByUsername("hung123")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpass", "encoded")).thenReturn(false);

        AppException ex = assertThrows(AppException.class, () ->
                authService.login(new LoginRequest("hung123", "wrongpass")));

        assertEquals(ExceptionCode.PASSWORD_INCORRECT, ex.getCode());
    }
}
