package com.taskmanagement.service;

import com.taskmanagement.dto.PaginatedResponse;
import com.taskmanagement.dto.user.UserRequestDTO;
import com.taskmanagement.dto.user.UserResponseDTO;
import org.springframework.stereotype.Service;

import java.util.UUID;

public interface UserService {

    PaginatedResponse<UserResponseDTO> findAll(int page, int size);

    UserResponseDTO findById(String id);

    UserResponseDTO create(UserRequestDTO dto);

    UserResponseDTO update(String id, UserRequestDTO dto);

    void delete(String id);
}
