package com.handoffiq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class CreateTaskRequest {

    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private Long assignedToId;
    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH, CRITICAL
    private LocalDate startDate;
    private LocalDate dueDate;
    private Long relatedMeetingId;
    private Long relatedDecisionId;

    public CreateTaskRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Long getRelatedMeetingId() { return relatedMeetingId; }
    public void setRelatedMeetingId(Long relatedMeetingId) { this.relatedMeetingId = relatedMeetingId; }

    public Long getRelatedDecisionId() { return relatedDecisionId; }
    public void setRelatedDecisionId(Long relatedDecisionId) { this.relatedDecisionId = relatedDecisionId; }
}
