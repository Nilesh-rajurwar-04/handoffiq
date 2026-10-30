package com.handoffiq.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_chunks")
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private KnowledgeDocument document;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(name = "chunk_text", columnDefinition = "LONGTEXT", nullable = false)
    private String chunkText;

    @Column(name = "embedding_ref")
    private String embeddingRef;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public DocumentChunk() {}

    public DocumentChunk(KnowledgeDocument document, Integer chunkIndex, String chunkText) {
        this.document = document;
        this.chunkIndex = chunkIndex;
        this.chunkText = chunkText;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public KnowledgeDocument getDocument() { return document; }
    public void setDocument(KnowledgeDocument document) { this.document = document; }

    public Integer getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(Integer chunkIndex) { this.chunkIndex = chunkIndex; }

    public String getChunkText() { return chunkText; }
    public void setChunkText(String chunkText) { this.chunkText = chunkText; }

    public String getEmbeddingRef() { return embeddingRef; }
    public void setEmbeddingRef(String embeddingRef) { this.embeddingRef = embeddingRef; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
