package com.handoffiq.repository;

import com.handoffiq.entity.AiInteractionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiInteractionLogRepository extends JpaRepository<AiInteractionLog, Long> {
    List<AiInteractionLog> findByFeatureOrderByCreatedAtDesc(String feature);
    List<AiInteractionLog> findTop20ByOrderByCreatedAtDesc();
}
