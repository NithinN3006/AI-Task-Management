package com.aitask.backend.dto;

import com.aitask.backend.model.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskStatusUpdateRequest {
    @NotNull
    private Status status;
}
