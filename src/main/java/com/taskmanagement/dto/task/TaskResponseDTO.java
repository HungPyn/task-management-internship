package com.taskmanagement.dto.task;

import com.taskmanagement.enums.Status;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
public class TaskResponseDTO {

    private String id;
    private String title;
    private String description;
    private Status status;
    private Instant created;
    private Instant updated;
    private Instant deadline;
    private String username;

}
