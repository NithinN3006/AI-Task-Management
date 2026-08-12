package com.aitask.backend.repository;

import com.aitask.backend.model.Priority;
import com.aitask.backend.model.Status;
import com.aitask.backend.model.Task;
import com.aitask.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserAndDeletedAtIsNull(User user);
    List<Task> findByUserAndStatusAndDeletedAtIsNull(User user, Status status);
    List<Task> findByUserAndPriorityAndDeletedAtIsNull(User user, Priority priority);
    List<Task> findByUserAndStatusAndPriorityAndDeletedAtIsNull(User user, Status status, Priority priority);
    
    Optional<Task> findByIdAndUserAndDeletedAtIsNull(Long id, User user);
    Optional<Task> findByIdAndUser(Long id, User user);
}
