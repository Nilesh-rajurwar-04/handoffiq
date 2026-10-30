package com.handoffiq.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_carry_forward_history")
public class TaskCarryForwardHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_meeting_id")
    private Meeting fromMeeting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_meeting_id")
    private Meeting toMeeting;

    @Column(name = "from_handover_id")
    private Long fromHandoverId;

    @Column(name = "to_handover_id")
    private Long toHandoverId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reason;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "carried_by_id", nullable = false)
    private User carriedBy;

    @Column(name = "carried_at", updatable = false)
    private LocalDateTime carriedAt = LocalDateTime.now();

    public TaskCarryForwardHistory() {}

    public TaskCarryForwardHistory(Task task, Meeting fromMeeting, Meeting toMeeting, String reason, User carriedBy) {
        this.task = task;
        this.fromMeeting = fromMeeting;
        this.toMeeting = toMeeting;
        this.reason = reason;
        this.carriedBy = carriedBy;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public Meeting getFromMeeting() { return fromMeeting; }
    public void setFromMeeting(Meeting fromMeeting) { this.fromMeeting = fromMeeting; }

    public Meeting getToMeeting() { return toMeeting; }
    public void setToMeeting(Meeting toMeeting) { this.toMeeting = toMeeting; }

    public Long getFromHandoverId() { return fromHandoverId; }
    public void setFromHandoverId(Long fromHandoverId) { this.fromHandoverId = fromHandoverId; }

    public Long getToHandoverId() { return toHandoverId; }
    public void setToHandoverId(Long toHandoverId) { this.toHandoverId = toHandoverId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public User getCarriedBy() { return carriedBy; }
    public void setCarriedBy(User carriedBy) { this.carriedBy = carriedBy; }

    public LocalDateTime getCarriedAt() { return carriedAt; }
    public void setCarriedAt(LocalDateTime carriedAt) { this.carriedAt = carriedAt; }
}
