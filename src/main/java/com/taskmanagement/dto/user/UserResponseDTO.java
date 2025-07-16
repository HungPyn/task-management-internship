package com.taskmanagement.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taskmanagement.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDTO {

    private String id;
    private String username;
    private String fullName;
    private Role role;
    private Instant created;
}
