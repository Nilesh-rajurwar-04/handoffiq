package com.handoffiq.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private Long projectId;
    private String projectName;
    private String projectCode;
    private Long assignedToId;
    private String assignedToName;
    private String assignedToEmail;
    private String assignedToAvatar;
    private Long createdById;
    private String createdByName;
    private String priority;
    private String status;
    private LocalDate startDate;
    private LocalDate dueDate;
    private Long relatedMeetingId;
    private String relatedMeetingTitle;
    private Long relatedDecisionId;
    private String relatedDecisionTitle;
    private String completionNotes;
    private String completionOutcome;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentDto> comments;
    private List<StatusHistoryDto> statusHistory;
    private List<CarryForwardDto> carryForwardHistory;

    public TaskResponse() {}

    public static class CommentDto {
        private Long id;
        private Long authorId;
        private String authorName;
        private String authorAvatar;
        private String commentText;
        private LocalDateTime createdAt;

        public CommentDto() {}
        public CommentDto(Long id, Long authorId, String authorName, String authorAvatar, String commentText, LocalDateTime createdAt) {
            this.id = id;
            this.authorId = authorId;
            this.authorName = authorName;
            this.authorAvatar = authorAvatar;
            this.commentText = commentText;
            this.createdAt = createdAt;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getAuthorId() { return authorId; }
        public void setAuthorId(Long authorId) { this.authorId = authorId; }
        public String getAuthorName() { return authorName; }
        public void setAuthorName(String authorName) { this.authorName = authorName; }
        public String getAuthorAvatar() { return authorAvatar; }
        public void setAuthorAvatar(String authorAvatar) { this.authorAvatar = authorAvatar; }
        public String getCommentText() { return commentText; }
        public void setCommentText(String commentText) { this.commentText = commentText; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class StatusHistoryDto {
        private Long id;
        private String oldStatus;
        private String newStatus;
        private String changedByName;
        private String remarks;
        private LocalDateTime createdAt;

        public StatusHistoryDto() {}
        public StatusHistoryDto(Long id, String oldStatus, String newStatus, String changedByName, String remarks, LocalDateTime createdAt) {
            this.id = id;
            this.oldStatus = oldStatus;
            this.newStatus = newStatus;
            this.changedByName = changedByName;
            this.remarks = remarks;
            this.createdAt = createdAt;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getOldStatus() { return oldStatus; }
        public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }
        public String getNewStatus() { return newStatus; }
        public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
        public String getChangedByName() { return changedByName; }
        public void setChangedByName(String changedByName) { this.changedByName = changedByName; }
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }

    public static class CarryForwardDto {
        private Long id;
        private Long fromMeetingId;
        private String fromMeetingTitle;
        private Long toMeetingId;
        private String toMeetingTitle;
        private String reason;
        private String carriedByName;
        private LocalDateTime carriedAt;

        public CarryForwardDto() {}
        public CarryForwardDto(Long id, Long fromMeetingId, String fromMeetingTitle, Long toMeetingId, String toMeetingTitle, String reason, String carriedByName, LocalDateTime carriedAt) {
            this.id = id;
            this.fromMeetingId = fromMeetingId;
            this.fromMeetingTitle = fromMeetingTitle;
            this.toMeetingId = toMeetingId;
            this.toMeetingTitle = toMeetingTitle;
            this.reason = reason;
            this.carriedByName = carriedByName;
            this.carriedAt = carriedAt;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getFromMeetingId() { return fromMeetingId; }
        public void setFromMeetingId(Long fromMeetingId) { this.fromMeetingId = fromMeetingId; }
        public String getFromMeetingTitle() { return fromMeetingTitle; }
        public void setFromMeetingTitle(String fromMeetingTitle) { this.fromMeetingTitle = fromMeetingTitle; }
        public Long getToMeetingId() { return toMeetingId; }
        public void setToMeetingId(Long toMeetingId) { this.toMeetingId = toMeetingId; }
        public String getToMeetingTitle() { return toMeetingTitle; }
        public void setToMeetingTitle(String toMeetingTitle) { this.toMeetingTitle = toMeetingTitle; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getCarriedByName() { return carriedByName; }
        public void setCarriedByName(String carriedByName) { this.carriedByName = carriedByName; }
        public LocalDateTime getCarriedAt() { return carriedAt; }
        public void setCarriedAt(LocalDateTime carriedAt) { this.carriedAt = carriedAt; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }
    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }
    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }
    public String getAssignedToEmail() { return assignedToEmail; }
    public void setAssignedToEmail(String assignedToEmail) { this.assignedToEmail = assignedToEmail; }
    public String getAssignedToAvatar() { return assignedToAvatar; }
    public void setAssignedToAvatar(String assignedToAvatar) { this.assignedToAvatar = assignedToAvatar; }
    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }
    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public Long getRelatedMeetingId() { return relatedMeetingId; }
    public void setRelatedMeetingId(Long relatedMeetingId) { this.relatedMeetingId = relatedMeetingId; }
    public String getRelatedMeetingTitle() { return relatedMeetingTitle; }
    public void setRelatedMeetingTitle(String relatedMeetingTitle) { this.relatedMeetingTitle = relatedMeetingTitle; }
    public Long getRelatedDecisionId() { return relatedDecisionId; }
    public void setRelatedDecisionId(Long relatedDecisionId) { this.relatedDecisionId = relatedDecisionId; }
    public String getRelatedDecisionTitle() { return relatedDecisionTitle; }
    public void setRelatedDecisionTitle(String relatedDecisionTitle) { this.relatedDecisionTitle = relatedDecisionTitle; }
    public String getCompletionNotes() { return completionNotes; }
    public void setCompletionNotes(String completionNotes) { this.completionNotes = completionNotes; }
    public String getCompletionOutcome() { return completionOutcome; }
    public void setCompletionOutcome(String completionOutcome) { this.completionOutcome = completionOutcome; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<CommentDto> getComments() { return comments; }
    public void setComments(List<CommentDto> comments) { this.comments = comments; }
    public List<StatusHistoryDto> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(List<StatusHistoryDto> statusHistory) { this.statusHistory = statusHistory; }
    public List<CarryForwardDto> getCarryForwardHistory() { return carryForwardHistory; }
    public void setCarryForwardHistory(List<CarryForwardDto> carryForwardHistory) { this.carryForwardHistory = carryForwardHistory; }
}
