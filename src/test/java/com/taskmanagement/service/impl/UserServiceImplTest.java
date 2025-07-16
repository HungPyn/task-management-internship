package com.taskmanagement.service.impl;

import com.taskmanagement.dto.PaginatedResponse;
import com.taskmanagement.dto.user.UserRequestDTO;
import com.taskmanagement.dto.user.UserResponseDTO;
import com.taskmanagement.entity.User;
import com.taskmanagement.enums.Role;
import com.taskmanagement.ex.AppException;
import com.taskmanagement.ex.ExceptionCode;
import com.taskmanagement.mapper.UserMapper;
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

class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthHelper authHelper;

    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll_Success() {
        List<User> users = List.of(new User());
        Page<User> page = new PageImpl<>(users);
        when(userRepository.findAllByOrderByCreatedDesc(any(Pageable.class))).thenReturn(page);
        when(userMapper.toUserResponseDTO(any())).thenReturn(new UserResponseDTO());

        PaginatedResponse<UserResponseDTO> result = userService.findAll(0, 10);

        assertEquals(1, result.getItems().size());
        assertEquals(1, result.getTotalItems());
    }

    @Test
    void testFindById_Success_Admin() {
        String userId = "uuid";
        User user = new User();
        user.setId(userId);
        when(authHelper.isAdmin()).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDTO(user)).thenReturn(new UserResponseDTO(userId, "username", "name", Role.USER, Instant.now()));

        UserResponseDTO dto = userService.findById(userId);

        assertEquals(userId, dto.getId());
    }

    @Test
    void testFindById_Success_UserOwnsData() {
        String userId = "uuid";
        when(authHelper.getCurrentUserId()).thenReturn(userId);
        when(authHelper.isAdmin()).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(userMapper.toUserResponseDTO(any())).thenReturn(new UserResponseDTO(userId, "user", "full", Role.USER, Instant.now()));

        UserResponseDTO dto = userService.findById(userId);

        assertEquals(userId, dto.getId());
    }

    @Test
    void testFindById_Fail_Unauthorized() {
        when(authHelper.isAdmin()).thenReturn(false);
        when(authHelper.getCurrentUserId()).thenReturn("user1");

        AppException ex = assertThrows(AppException.class, () -> userService.findById("user2"));

        assertEquals(ExceptionCode.UNAUTHORIZED, ex.getCode());
    }

    @Test
    void testCreate_Success() {
        UserRequestDTO request = new UserRequestDTO("user", "Password1", "Full Name", "USER");
        when(userRepository.findByUsername("user")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password1")).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(new User());
        when(userMapper.toUserResponseDTO(any())).thenReturn(new UserResponseDTO("uuid", "user", "Full Name", Role.USER, Instant.now()));

        UserResponseDTO result = userService.create(request);

        assertEquals("user", result.getUsername());
    }

    @Test
    void testCreate_Fail_UsernameExists() {
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(new User()));

        UserRequestDTO request = new UserRequestDTO("user", "Password1", "Full Name", "USER");

        AppException ex = assertThrows(AppException.class, () -> userService.create(request));

        assertEquals(ExceptionCode.USER_FOUND, ex.getCode());
    }

    @Test
    void testUpdate_Success_Admin() {
        String id = "uuid";
        UserRequestDTO dto = new UserRequestDTO("user", "pass123", "Updated Name", "ADMIN");
        User user = new User();
        user.setId(id);

        when(authHelper.isAdmin()).thenReturn(true);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("pass123")).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(user);
        when(userMapper.toUserResponseDTO(any())).thenReturn(new UserResponseDTO(id, "user", "Updated Name", Role.ADMIN, Instant.now()));

        UserResponseDTO result = userService.update(id, dto);

        assertEquals("Updated Name", result.getFullName());
    }

    @Test
    void testUpdate_Fail_Unauthorized() {
        String id = "uuid";
        when(authHelper.isAdmin()).thenReturn(false);
        when(authHelper.getCurrentUserId()).thenReturn("another-id");

        UserRequestDTO dto = new UserRequestDTO("user", "pass", "name", "USER");

        AppException ex = assertThrows(AppException.class, () -> userService.update(id, dto));
        assertEquals(ExceptionCode.UNAUTHORIZED, ex.getCode());
    }

    @Test
    void testDelete_Success() {
        String id = "uuid";
        User user = new User();
        user.setId(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> userService.delete(id));
        verify(userRepository).delete(user);
    }

    @Test
    void testDelete_Fail_UserNotFound() {
        when(userRepository.findById("notfound")).thenReturn(Optional.empty());

        AppException ex = assertThrows(AppException.class, () -> userService.delete("notfound"));
        assertEquals(ExceptionCode.USER_NOT_FOUND, ex.getCode());
    }
}
