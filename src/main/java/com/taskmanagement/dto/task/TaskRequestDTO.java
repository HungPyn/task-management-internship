package com.taskmanagement.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskRequestDTO {

    @NotBlank(message = "Title must not be blank")
    private String title;

    private String description;

    @NotBlank(message = "Status must not be blank")
    @Pattern(
            regexp = "PENDING|IN_PROGRESS|COMPLETED",
            message = "Invalid status value"
    )
    private String status;

    @NotNull(message = "Created time must not be null")
    private Instant created;

    private Instant updated;

    @NotNull(message = "Deadline must not be null")
    private Instant deadline;

    private String userId;
}
