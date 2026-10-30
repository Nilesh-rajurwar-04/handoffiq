package com.handoffiq.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "handover_tasks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"handover_id", "task_id"})
})
public class HandoverTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handover_id", nullable = false)
    private Handover handover;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "previous_owner_id")
    private User previousOwner;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "new_owner_id")
    private User newOwner;

    @Column(name = "task_status_at_handover", length = 30)
    private String taskStatusAtHandover;

    @Column(length = 255)
    private String notes;

    public HandoverTask() {}

    public HandoverTask(Handover handover, Task task, User previousOwner, User newOwner, String taskStatusAtHandover, String notes) {
        this.handover = handover;
        this.task = task;
        this.previousOwner = previousOwner;
        this.newOwner = newOwner;
        this.taskStatusAtHandover = taskStatusAtHandover;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Handover getHandover() { return handover; }
    public void setHandover(Handover handover) { this.handover = handover; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public User getPreviousOwner() { return previousOwner; }
    public void setPreviousOwner(User previousOwner) { this.previousOwner = previousOwner; }

    public User getNewOwner() { return newOwner; }
    public void setNewOwner(User newOwner) { this.newOwner = newOwner; }

    public String getTaskStatusAtHandover() { return taskStatusAtHandover; }
    public void setTaskStatusAtHandover(String taskStatusAtHandover) { this.taskStatusAtHandover = taskStatusAtHandover; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
