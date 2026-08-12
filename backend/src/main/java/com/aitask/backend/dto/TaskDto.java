package com.aitask.backend.dto;

import com.aitask.backend.model.Priority;
import com.aitask.backend.model.Status;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class TaskDto {
    private Long id;
    private String title;
    private String description;
    private Priority priority;
    private LocalDateTime dueDate;
    private Status status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
