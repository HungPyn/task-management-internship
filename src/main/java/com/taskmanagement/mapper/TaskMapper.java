package com.taskmanagement.mapper;

import com.taskmanagement.dto.task.TaskResponseDTO;
import com.taskmanagement.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {

    @Mapping(target = "username",source = "user.username")
    TaskResponseDTO toTaskResponseDTO(Task task);
}
