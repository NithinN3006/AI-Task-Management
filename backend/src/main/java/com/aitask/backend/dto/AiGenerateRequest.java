package com.aitask.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiGenerateRequest {
    @NotBlank
    private String title;
}
