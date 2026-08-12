package com.aitask.backend.dto;

import com.aitask.backend.model.Priority;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AiGenerateResponse {
    private String description;
    private Priority priority;
    private Integer estimatedHours;
    private boolean aiGenerated;
}
