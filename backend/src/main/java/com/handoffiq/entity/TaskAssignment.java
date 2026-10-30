package com.handoffiq.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_assignments")
public class TaskAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "previous_assignee_id")
    private User previousAssignee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "new_assignee_id")
    private User newAssignee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_by_id", nullable = false)
    private User assignedBy;

    @Column(length = 255)
    private String reason;

    @Column(name = "assigned_at", updatable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    public TaskAssignment() {}

    public TaskAssignment(Task task, User previousAssignee, User newAssignee, User assignedBy, String reason) {
        this.task = task;
        this.previousAssignee = previousAssignee;
        this.newAssignee = newAssignee;
        this.assignedBy = assignedBy;
        this.reason = reason;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public User getPreviousAssignee() { return previousAssignee; }
    public void setPreviousAssignee(User previousAssignee) { this.previousAssignee = previousAssignee; }

    public User getNewAssignee() { return newAssignee; }
    public void setNewAssignee(User newAssignee) { this.newAssignee = newAssignee; }

    public User getAssignedBy() { return assignedBy; }
    public void setAssignedBy(User assignedBy) { this.assignedBy = assignedBy; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}
