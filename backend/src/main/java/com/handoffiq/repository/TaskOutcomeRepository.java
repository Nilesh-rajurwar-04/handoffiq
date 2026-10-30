package com.handoffiq.repository;

import com.handoffiq.entity.TaskOutcome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskOutcomeRepository extends JpaRepository<TaskOutcome, Long> {
    Optional<TaskOutcome> findByTaskId(Long taskId);
}
