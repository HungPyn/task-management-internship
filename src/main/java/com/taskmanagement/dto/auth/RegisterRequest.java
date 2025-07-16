package com.taskmanagement.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "username is not empty")
    private String username;

    @NotBlank(message = "password is not empty")
    @Size(min = 6, max = 16, message = "password must be between 6 and 16 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{6,16}$",
            message = "password must contain at least one letter and one digit")
    private String password;

    @NotBlank(message = "full name is not empty")
    private String fullName;

    @NotBlank(message = "role is not empty")
    private String role;
}
