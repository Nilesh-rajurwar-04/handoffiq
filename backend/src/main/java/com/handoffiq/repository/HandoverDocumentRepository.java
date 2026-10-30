package com.handoffiq.repository;

import com.handoffiq.entity.HandoverDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HandoverDocumentRepository extends JpaRepository<HandoverDocument, Long> {
    List<HandoverDocument> findByHandoverId(Long handoverId);
}
