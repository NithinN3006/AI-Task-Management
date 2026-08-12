package com.aitask.backend.dto;

import com.aitask.backend.model.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaskCreateRequest {
    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Priority priority;

    private LocalDateTime dueDate;
}
