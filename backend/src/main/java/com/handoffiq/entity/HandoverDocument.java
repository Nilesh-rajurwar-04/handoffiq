package com.handoffiq.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "handover_documents", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"handover_id", "document_id"})
})
public class HandoverDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handover_id", nullable = false)
    private Handover handover;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(length = 255)
    private String notes;

    public HandoverDocument() {}

    public HandoverDocument(Handover handover, Long documentId, String notes) {
        this.handover = handover;
        this.documentId = documentId;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Handover getHandover() { return handover; }
    public void setHandover(Handover handover) { this.handover = handover; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
