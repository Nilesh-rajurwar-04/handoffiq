package com.handoffiq.dto;

import java.time.LocalDateTime;
import java.util.List;

public class MeetingResponse {

    private Long id;
    private String title;
    private Long projectId;
    private String projectName;
    private String projectCode;
    private Long createdById;
    private String createdByName;
    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;
    private String status;
    private String location;
    private String agenda;
    private String rawNotes;
    private String aiSummary;
    private String keyDiscussionPoints;
    private List<ParticipantDto> participants;
    private List<DecisionDto> decisions;
    private List<TaskSummaryDto> tasks;
    private LocalDateTime createdAt;

    public MeetingResponse() {}

    public static class ParticipantDto {
        private Long userId;
        private String fullName;
        private String email;
        private String avatarUrl;
        private String attendanceStatus;

        public ParticipantDto() {}
        public ParticipantDto(Long userId, String fullName, String email, String avatarUrl, String attendanceStatus) {
            this.userId = userId;
            this.fullName = fullName;
            this.email = email;
            this.avatarUrl = avatarUrl;
            this.attendanceStatus = attendanceStatus;
        }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getAvatarUrl() { return avatarUrl; }
        public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
        public String getAttendanceStatus() { return attendanceStatus; }
        public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }
    }

    public static class DecisionDto {
        private Long id;
        private String title;
        private String rationale;
        private String status;
        private String decidedByName;

        public DecisionDto() {}
        public DecisionDto(Long id, String title, String rationale, String status, String decidedByName) {
            this.id = id;
            this.title = title;
            this.rationale = rationale;
            this.status = status;
            this.decidedByName = decidedByName;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getRationale() { return rationale; }
        public void setRationale(String rationale) { this.rationale = rationale; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getDecidedByName() { return decidedByName; }
        public void setDecidedByName(String decidedByName) { this.decidedByName = decidedByName; }
    }

    public static class TaskSummaryDto {
        private Long id;
        private String title;
        private String status;
        private String priority;
        private String assigneeName;

        public TaskSummaryDto() {}
        public TaskSummaryDto(Long id, String title, String status, String priority, String assigneeName) {
            this.id = id;
            this.title = title;
            this.status = status;
            this.priority = priority;
            this.assigneeName = assigneeName;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        public String getAssigneeName() { return assigneeName; }
        public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }
    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }
    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
    public LocalDateTime getScheduledStart() { return scheduledStart; }
    public void setScheduledStart(LocalDateTime scheduledStart) { this.scheduledStart = scheduledStart; }
    public LocalDateTime getScheduledEnd() { return scheduledEnd; }
    public void setScheduledEnd(LocalDateTime scheduledEnd) { this.scheduledEnd = scheduledEnd; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getAgenda() { return agenda; }
    public void setAgenda(String agenda) { this.agenda = agenda; }
    public String getRawNotes() { return rawNotes; }
    public void setRawNotes(String rawNotes) { this.rawNotes = rawNotes; }
    public String getAiSummary() { return aiSummary; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
    public String getKeyDiscussionPoints() { return keyDiscussionPoints; }
    public void setKeyDiscussionPoints(String keyDiscussionPoints) { this.keyDiscussionPoints = keyDiscussionPoints; }
    public List<ParticipantDto> getParticipants() { return participants; }
    public void setParticipants(List<ParticipantDto> participants) { this.participants = participants; }
    public List<DecisionDto> getDecisions() { return decisions; }
    public void setDecisions(List<DecisionDto> decisions) { this.decisions = decisions; }
    public List<TaskSummaryDto> getTasks() { return tasks; }
    public void setTasks(List<TaskSummaryDto> tasks) { this.tasks = tasks; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
