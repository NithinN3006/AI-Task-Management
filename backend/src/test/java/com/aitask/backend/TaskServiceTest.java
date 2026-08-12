package com.aitask.backend;

import com.aitask.backend.dto.TaskCreateRequest;
import com.aitask.backend.dto.TaskDto;
import com.aitask.backend.model.Action;
import com.aitask.backend.model.Priority;
import com.aitask.backend.model.Task;
import com.aitask.backend.model.User;
import com.aitask.backend.repository.TaskRepository;
import com.aitask.backend.repository.UserRepository;
import com.aitask.backend.service.LedgerService;
import com.aitask.backend.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LedgerService ledgerService;

    private TaskService taskService;

    private User mockUser;
    private Task mockTask;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(taskRepository, userRepository, ledgerService);

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        mockTask = new Task();
        mockTask.setId(10L);
        mockTask.setTitle("Test Task");
        mockTask.setUser(mockUser);
    }

    @Test
    void testCreateTask() {
        TaskCreateRequest request = new TaskCreateRequest();
        request.setTitle("New Task");
        request.setPriority(Priority.HIGH);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setId(20L);
            return t;
        });

        TaskDto result = taskService.createTask("test@example.com", request);

        assertNotNull(result);
        assertEquals("New Task", result.getTitle());
        assertEquals(Priority.HIGH, result.getPriority());
        assertEquals(20L, result.getId());

        verify(ledgerService).recordAction(any(Task.class), eq(Action.CREATED));
    }

    @Test
    void testDeleteTask() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(taskRepository.findByIdAndUserAndDeletedAtIsNull(10L, mockUser)).thenReturn(Optional.of(mockTask));
        when(taskRepository.save(any(Task.class))).thenReturn(mockTask);

        taskService.deleteTask("test@example.com", 10L);

        assertNotNull(mockTask.getDeletedAt());
        verify(ledgerService).recordAction(mockTask, Action.DELETED);
    }
}
