package com.aitask.backend.controller;

import com.aitask.backend.dto.AiGenerateRequest;
import com.aitask.backend.dto.AiGenerateResponse;
import com.aitask.backend.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/generate-task-details")
    public ResponseEntity<AiGenerateResponse> generateTaskDetails(
            @Valid @RequestBody AiGenerateRequest request) {
        
        // This endpoint does not necessarily require task ownership checks since it just hits the AI.
        // It's still protected by the Spring Security filter chain.
        return ResponseEntity.ok(aiService.generateTaskDetails(request.getTitle()));
    }
}
