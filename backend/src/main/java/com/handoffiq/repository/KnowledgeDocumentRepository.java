package com.handoffiq.repository;

import com.handoffiq.entity.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeDocumentRepository extends JpaRepository<KnowledgeDocument, Long> {
    List<KnowledgeDocument> findByProjectId(Long projectId);
    List<KnowledgeDocument> findByCategory(String category);
    List<KnowledgeDocument> findAllByOrderByCreatedAtDesc();

    @Query("SELECT d FROM KnowledgeDocument d WHERE LOWER(d.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.contentText) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<KnowledgeDocument> searchDocuments(@Param("keyword") String keyword);
}
