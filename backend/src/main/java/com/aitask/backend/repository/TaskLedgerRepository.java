package com.aitask.backend.repository;

import com.aitask.backend.model.TaskLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskLedgerRepository extends JpaRepository<TaskLedger, Long> {
    List<TaskLedger> findByTaskIdOrderByTimestampAsc(Long taskId);
    Optional<TaskLedger> findTopByTaskIdOrderByTimestampDesc(Long taskId);
}
