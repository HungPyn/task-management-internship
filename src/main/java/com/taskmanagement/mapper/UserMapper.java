package com.taskmanagement.mapper;

import com.taskmanagement.dto.user.UserResponseDTO;
import com.taskmanagement.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel ="spring")
public interface UserMapper {

    UserResponseDTO toUserResponseDTO(User dto);
}
