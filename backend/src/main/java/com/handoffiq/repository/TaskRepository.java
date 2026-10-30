package com.handoffiq.repository;

import com.handoffiq.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectId(Long projectId);
    List<Task> findByAssignedToId(Long userId);
    List<Task> findByAssignedToIdAndStatus(Long userId, String status);
    List<Task> findByStatus(String status);
    List<Task> findByPriority(String priority);
    
    long countByStatus(String status);
    long countByAssignedToIdAndStatus(Long userId, String status);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.dueDate < :today AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    long countOverdueTasks(@Param("today") LocalDate today);

    @Query("SELECT t FROM Task t WHERE t.dueDate < :today AND t.status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY t.dueDate ASC")
    List<Task> findOverdueTasks(@Param("today") LocalDate today);

    @Query("SELECT t FROM Task t WHERE t.assignedTo IS NULL AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Task> findUnassignedTasks();

    @Query("SELECT t FROM Task t WHERE t.status = 'BLOCKED'")
    List<Task> findBlockedTasks();

    @Query("SELECT t FROM Task t WHERE t.assignedTo.id = :userId AND t.dueDate = :today AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<Task> findDueTodayByUserId(@Param("userId") Long userId, @Param("today") LocalDate today);

    @Query("SELECT t FROM Task t WHERE t.assignedTo.id = :userId AND t.status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY CASE WHEN t.dueDate < :today THEN 1 ELSE 2 END, t.dueDate ASC")
    List<Task> findPriorityTodayByUserId(@Param("userId") Long userId, @Param("today") LocalDate today);

    List<Task> findByRelatedMeetingId(Long meetingId);
    List<Task> findByRelatedDecisionId(Long decisionId);
}
