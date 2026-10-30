package com.handoffiq.repository;

import com.handoffiq.entity.Decision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DecisionRepository extends JpaRepository<Decision, Long> {
    List<Decision> findByProjectIdOrderByDecidedAtDesc(Long projectId);
    List<Decision> findByMeetingId(Long meetingId);
    List<Decision> findAllByOrderByDecidedAtDesc();
}
