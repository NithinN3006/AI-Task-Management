package com.aitask.backend.controller;

import com.aitask.backend.dto.*;
import com.aitask.backend.model.Priority;
import com.aitask.backend.model.Status;
import com.aitask.backend.service.LedgerService;
import com.aitask.backend.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final LedgerService ledgerService;

    @GetMapping
    public ResponseEntity<List<TaskDto>> getTasks(
            Authentication authentication,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Priority priority) {
        
        String email = authentication.getName();
        return ResponseEntity.ok(taskService.getTasks(email, status, priority));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTask(
            Authentication authentication,
            @PathVariable Long id) {
        
        String email = authentication.getName();
        return ResponseEntity.ok(taskService.getTaskById(email, id));
    }

    @PostMapping
    public ResponseEntity<TaskDto> createTask(
            Authentication authentication,
            @Valid @RequestBody TaskCreateRequest request) {
        
        String email = authentication.getName();
        return ResponseEntity.ok(taskService.createTask(email, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody TaskUpdateRequest request) {
        
        String email = authentication.getName();
        return ResponseEntity.ok(taskService.updateTask(email, id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskDto> updateTaskStatus(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody TaskStatusUpdateRequest request) {
        
        String email = authentication.getName();
        return ResponseEntity.ok(taskService.updateTaskStatus(email, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteTask(
            Authentication authentication,
            @PathVariable Long id) {
        
        String email = authentication.getName();
        taskService.deleteTask(email, id);
        return ResponseEntity.ok(new MessageResponse("Task deleted successfully"));
    }

    // Phase 6 endpoints
    @GetMapping("/{id}/history")
    public ResponseEntity<List<LedgerHistoryResponse>> getTaskHistory(
            Authentication authentication,
            @PathVariable Long id) {
        
        String email = authentication.getName();
        taskService.verifyTaskOwnership(email, id);
        return ResponseEntity.ok(ledgerService.getHistory(id));
    }

    @GetMapping("/{id}/verify")
    public ResponseEntity<LedgerVerifyResponse> verifyTaskLedger(
            Authentication authentication,
            @PathVariable Long id) {
        
        String email = authentication.getName();
        taskService.verifyTaskOwnership(email, id);
        return ResponseEntity.ok(ledgerService.verifyLedger(id));
    }
}
