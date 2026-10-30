package com.handoffiq.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "handover_acknowledgements")
public class HandoverAcknowledgement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handover_id", nullable = false)
    private Handover handover;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "acknowledger_id", nullable = false)
    private User acknowledger;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @Column(name = "acknowledged_at", updatable = false)
    private LocalDateTime acknowledgedAt = LocalDateTime.now();

    public HandoverAcknowledgement() {}

    public HandoverAcknowledgement(Handover handover, User acknowledger, String comments) {
        this.handover = handover;
        this.acknowledger = acknowledger;
        this.comments = comments;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Handover getHandover() { return handover; }
    public void setHandover(Handover handover) { this.handover = handover; }

    public User getAcknowledger() { return acknowledger; }
    public void setAcknowledger(User acknowledger) { this.acknowledger = acknowledger; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
}
