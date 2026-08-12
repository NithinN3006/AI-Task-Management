package com.aitask.backend.dto;

import com.aitask.backend.model.Priority;
import com.aitask.backend.model.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaskUpdateRequest {
    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Priority priority;
    
    @NotNull
    private Status status;

    private LocalDateTime dueDate;
}
