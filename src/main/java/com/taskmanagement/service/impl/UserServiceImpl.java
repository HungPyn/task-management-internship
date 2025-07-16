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
import com.taskmanagement.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthHelper authHelper;

    @Override
    public PaginatedResponse<UserResponseDTO> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> usersPage = userRepository.findAllByOrderByCreatedDesc(pageable);

        List<UserResponseDTO> dtoList = usersPage.getContent()
                .stream()
                .map(userMapper::toUserResponseDTO)
                .toList();

        return new PaginatedResponse<>(
                dtoList,
                usersPage.getNumber(),
                (int) usersPage.getTotalElements(),
                usersPage.getTotalPages(),
                usersPage.hasNext()
        );
    }

    @Override
    public UserResponseDTO findById(String id) {
        if (!authHelper.isAdmin() && !authHelper.getCurrentUserId().equals(id)) {
            log.warn("Unauthorized access attempt by user ID: {}", authHelper.getCurrentUserId());
            throw new AppException(ExceptionCode.UNAUTHORIZED);
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ExceptionCode.USER_NOT_FOUND));

        UserResponseDTO dto = userMapper.toUserResponseDTO(user);

        if (!authHelper.isAdmin()) {
            dto.setRole(null);
        }

        return dto;
    }



    @Override
    public UserResponseDTO create(UserRequestDTO dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            log.warn("Attempt to create with existing username: {}", dto.getUsername());
            throw new AppException(ExceptionCode.USER_FOUND);
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.fromString(dto.getRole()));
        user.setCreated(Instant.now());

        userRepository.save(user);
        log.info("Created new user: {}, role: {}", dto.getUsername(), user.getRole());

        return userMapper.toUserResponseDTO(user);
    }

    @Override
    public UserResponseDTO update(String id, UserRequestDTO dto) {
        if (!authHelper.isAdmin() && !authHelper.getCurrentUserId().equals(id)) {
            log.warn("Unauthorized update attempt by user ID: {}", authHelper.getCurrentUserId());
            throw new AppException(ExceptionCode.UNAUTHORIZED);
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ExceptionCode.USER_NOT_FOUND));

        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());
        if (authHelper.isAdmin()) {
            user.setRole(Role.fromString(dto.getRole()));
        }

        userRepository.save(user);
        log.info("User ID: {} updated by user ID: {}", id, authHelper.getCurrentUserId());

        return userMapper.toUserResponseDTO(user);
    }



    @Override
    public void delete(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ExceptionCode.USER_NOT_FOUND));

        userRepository.delete(user);
        log.info("Deleted user ID: {}, username: {}", id, user.getUsername());
    }
}
