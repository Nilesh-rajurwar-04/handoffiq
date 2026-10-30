package com.handoffiq.dto;

import java.time.LocalDate;

public class UpdateTaskRequest {

    private String title;
    private String description;
    private Long assignedToId;
    private String priority;
    private String status;
    private LocalDate startDate;
    private LocalDate dueDate;
    private String completionNotes;
    private String completionOutcome;
    private Long relatedMeetingId;
    private Long relatedDecisionId;

    public UpdateTaskRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public String getCompletionNotes() { return completionNotes; }
    public void setCompletionNotes(String completionNotes) { this.completionNotes = completionNotes; }
    public String getCompletionOutcome() { return completionOutcome; }
    public void setCompletionOutcome(String completionOutcome) { this.completionOutcome = completionOutcome; }
    public Long getRelatedMeetingId() { return relatedMeetingId; }
    public void setRelatedMeetingId(Long relatedMeetingId) { this.relatedMeetingId = relatedMeetingId; }
    public Long getRelatedDecisionId() { return relatedDecisionId; }
    public void setRelatedDecisionId(Long relatedDecisionId) { this.relatedDecisionId = relatedDecisionId; }
}
