package com.handoffiq.repository;

import com.handoffiq.entity.TaskCarryForwardHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskCarryForwardHistoryRepository extends JpaRepository<TaskCarryForwardHistory, Long> {
    List<TaskCarryForwardHistory> findByTaskIdOrderByCarriedAtDesc(Long taskId);
    List<TaskCarryForwardHistory> findByFromMeetingId(Long fromMeetingId);
    List<TaskCarryForwardHistory> findByToMeetingId(Long toMeetingId);
}
