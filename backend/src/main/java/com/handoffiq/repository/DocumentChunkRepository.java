package com.handoffiq.repository;

import com.handoffiq.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    List<DocumentChunk> findByDocumentIdOrderByChunkIndexAsc(Long documentId);
    void deleteByDocumentId(Long documentId);

    @Query("SELECT c FROM DocumentChunk c WHERE LOWER(c.chunkText) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<DocumentChunk> searchChunks(@Param("keyword") String keyword);
}
