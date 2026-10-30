package com.handoffiq.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_outcomes")
public class TaskOutcome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false, unique = true)
    private Task task;

    @Column(name = "outcome_type", nullable = false, length = 100)
    private String outcomeType;

    @Column(name = "detailed_notes", columnDefinition = "TEXT", nullable = false)
    private String detailedNotes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recorded_by_id", nullable = false)
    private User recordedBy;

    @Column(name = "recorded_at", updatable = false)
    private LocalDateTime recordedAt = LocalDateTime.now();

    public TaskOutcome() {}

    public TaskOutcome(Task task, String outcomeType, String detailedNotes, User recordedBy) {
        this.task = task;
        this.outcomeType = outcomeType;
        this.detailedNotes = detailedNotes;
        this.recordedBy = recordedBy;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public String getOutcomeType() { return outcomeType; }
    public void setOutcomeType(String outcomeType) { this.outcomeType = outcomeType; }

    public String getDetailedNotes() { return detailedNotes; }
    public void setDetailedNotes(String detailedNotes) { this.detailedNotes = detailedNotes; }

    public User getRecordedBy() { return recordedBy; }
    public void setRecordedBy(User recordedBy) { this.recordedBy = recordedBy; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
