package com.aitask.backend.service;

import com.aitask.backend.dto.TaskCreateRequest;
import com.aitask.backend.dto.TaskDto;
import com.aitask.backend.dto.TaskStatusUpdateRequest;
import com.aitask.backend.dto.TaskUpdateRequest;
import com.aitask.backend.model.Action;
import com.aitask.backend.model.Priority;
import com.aitask.backend.model.Status;
import com.aitask.backend.model.Task;
import com.aitask.backend.model.User;
import com.aitask.backend.repository.TaskRepository;
import com.aitask.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final LedgerService ledgerService;

    public List<TaskDto> getTasks(String userEmail, Status status, Priority priority) {
        User user = getUser(userEmail);
        
        List<Task> tasks;
        if (status != null && priority != null) {
            tasks = taskRepository.findByUserAndStatusAndPriorityAndDeletedAtIsNull(user, status, priority);
        } else if (status != null) {
            tasks = taskRepository.findByUserAndStatusAndDeletedAtIsNull(user, status);
        } else if (priority != null) {
            tasks = taskRepository.findByUserAndPriorityAndDeletedAtIsNull(user, priority);
        } else {
            tasks = taskRepository.findByUserAndDeletedAtIsNull(user);
        }

        return tasks.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public TaskDto getTaskById(String userEmail, Long taskId) {
        User user = getUser(userEmail);
        Task task = getTask(taskId, user);
        return mapToDto(task);
    }

    @Transactional
    public TaskDto createTask(String userEmail, TaskCreateRequest request) {
        User user = getUser(userEmail);

        Task task = Task.builder()
                .user(user)
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .status(Status.TODO) // default
                .dueDate(request.getDueDate())
                .build();

        task = taskRepository.save(task);
        ledgerService.recordAction(task, Action.CREATED);
        
        return mapToDto(task);
    }

    @Transactional
    public TaskDto updateTask(String userEmail, Long taskId, TaskUpdateRequest request) {
        User user = getUser(userEmail);
        Task task = getTask(taskId, user);

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(request.getPriority());
        task.setStatus(request.getStatus());
        task.setDueDate(request.getDueDate());

        task = taskRepository.save(task);
        ledgerService.recordAction(task, Action.UPDATED);

        return mapToDto(task);
    }

    @Transactional
    public TaskDto updateTaskStatus(String userEmail, Long taskId, TaskStatusUpdateRequest request) {
        User user = getUser(userEmail);
        Task task = getTask(taskId, user);

        task.setStatus(request.getStatus());
        task = taskRepository.save(task);
        ledgerService.recordAction(task, Action.STATUS_CHANGED);

        return mapToDto(task);
    }

    @Transactional
    public void deleteTask(String userEmail, Long taskId) {
        User user = getUser(userEmail);
        Task task = getTask(taskId, user);

        task.setDeletedAt(LocalDateTime.now());
        task = taskRepository.save(task);
        ledgerService.recordAction(task, Action.DELETED);
    }

    public void verifyTaskOwnership(String userEmail, Long taskId) {
        User user = getUser(userEmail);
        taskRepository.findByIdAndUser(taskId, user)
                .orElseThrow(() -> new RuntimeException("Task not found or access denied"));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Task getTask(Long taskId, User user) {
        return taskRepository.findByIdAndUserAndDeletedAtIsNull(taskId, user)
                .orElseThrow(() -> new RuntimeException("Task not found or access denied"));
    }

    private TaskDto mapToDto(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority())
                .status(task.getStatus())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
