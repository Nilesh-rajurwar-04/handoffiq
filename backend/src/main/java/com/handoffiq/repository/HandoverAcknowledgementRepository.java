package com.handoffiq.repository;

import com.handoffiq.entity.HandoverAcknowledgement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HandoverAcknowledgementRepository extends JpaRepository<HandoverAcknowledgement, Long> {
    List<HandoverAcknowledgement> findByHandoverId(Long handoverId);
    Optional<HandoverAcknowledgement> findByHandoverIdAndAcknowledgerId(Long handoverId, Long acknowledgerId);
}
