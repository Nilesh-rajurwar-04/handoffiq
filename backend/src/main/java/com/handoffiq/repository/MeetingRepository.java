package com.handoffiq.repository;

import com.handoffiq.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByProjectIdOrderByScheduledStartDesc(Long projectId);
    List<Meeting> findAllByOrderByScheduledStartDesc();
    List<Meeting> findTop5ByOrderByScheduledStartDesc();
    long countByStatus(String status);
    
    @Query("SELECT m FROM Meeting m WHERE m.project.id IN :projectIds ORDER BY m.scheduledStart DESC")
    List<Meeting> findByProjectIds(List<Long> projectIds);
}
