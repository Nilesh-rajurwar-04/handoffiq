package com.handoffiq.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class HandoverResponse {

    private Long id;
    private String title;
    private Long projectId;
    private String projectName;
    private String projectCode;
    private Long currentEmployeeId;
    private String currentEmployeeName;
    private String currentEmployeeEmail;
    private String currentEmployeeAvatar;
    private Long receivingEmployeeId;
    private String receivingEmployeeName;
    private String receivingEmployeeEmail;
    private String receivingEmployeeAvatar;
    private Long initiatedById;
    private String initiatedByName;
    private String reason;
    private LocalDate handoverDate;
    private String specialInstructions;
    private String status; // DRAFT, AWAITING_REVIEW, AWAITING_ACCEPTANCE, ACCEPTED, COMPLETED
    private Integer readinessScore;
    private String readinessChecklist;
    private String aiGeneratedSummary;
    private String workCompletedNotes;
    private String workInProgressNotes;
    private String blockersNotes;
    private String recommendedStepsNotes;
    private LocalDateTime acknowledgedAt;
    private String acknowledgementNotes;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<HandoverTaskDto> tasks;
    private List<HandoverDocumentDto> documents;
    private ReadinessScoreResponse readinessReport;

    public HandoverResponse() {}

    public static class HandoverTaskDto {
        private Long id;
        private Long taskId;
        private String taskTitle;
        private String taskPriority;
        private String taskStatus;
        private LocalDate dueDate;
        private Long previousOwnerId;
        private String previousOwnerName;
        private Long newOwnerId;
        private String newOwnerName;
        private String notes;

        public HandoverTaskDto() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }
        public String getTaskTitle() { return taskTitle; }
        public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }
        public String getTaskPriority() { return taskPriority; }
        public void setTaskPriority(String taskPriority) { this.taskPriority = taskPriority; }
        public String getTaskStatus() { return taskStatus; }
        public void setTaskStatus(String taskStatus) { this.taskStatus = taskStatus; }
        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
        public Long getPreviousOwnerId() { return previousOwnerId; }
        public void setPreviousOwnerId(Long previousOwnerId) { this.previousOwnerId = previousOwnerId; }
        public String getPreviousOwnerName() { return previousOwnerName; }
        public void setPreviousOwnerName(String previousOwnerName) { this.previousOwnerName = previousOwnerName; }
        public Long getNewOwnerId() { return newOwnerId; }
        public void setNewOwnerId(Long newOwnerId) { this.newOwnerId = newOwnerId; }
        public String getNewOwnerName() { return newOwnerName; }
        public void setNewOwnerName(String newOwnerName) { this.newOwnerName = newOwnerName; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class HandoverDocumentDto {
        private Long documentId;
        private String documentTitle;
        private String fileName;
        private String fileType;
        private String category;
        private String notes;

        public HandoverDocumentDto() {}

        public Long getDocumentId() { return documentId; }
        public void setDocumentId(Long documentId) { this.documentId = documentId; }
        public String getDocumentTitle() { return documentTitle; }
        public void setDocumentTitle(String documentTitle) { this.documentTitle = documentTitle; }
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
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
    public Long getCurrentEmployeeId() { return currentEmployeeId; }
    public void setCurrentEmployeeId(Long currentEmployeeId) { this.currentEmployeeId = currentEmployeeId; }
    public String getCurrentEmployeeName() { return currentEmployeeName; }
    public void setCurrentEmployeeName(String currentEmployeeName) { this.currentEmployeeName = currentEmployeeName; }
    public String getCurrentEmployeeEmail() { return currentEmployeeEmail; }
    public void setCurrentEmployeeEmail(String currentEmployeeEmail) { this.currentEmployeeEmail = currentEmployeeEmail; }
    public String getCurrentEmployeeAvatar() { return currentEmployeeAvatar; }
    public void setCurrentEmployeeAvatar(String currentEmployeeAvatar) { this.currentEmployeeAvatar = currentEmployeeAvatar; }
    public Long getReceivingEmployeeId() { return receivingEmployeeId; }
    public void setReceivingEmployeeId(Long receivingEmployeeId) { this.receivingEmployeeId = receivingEmployeeId; }
    public String getReceivingEmployeeName() { return receivingEmployeeName; }
    public void setReceivingEmployeeName(String receivingEmployeeName) { this.receivingEmployeeName = receivingEmployeeName; }
    public String getReceivingEmployeeEmail() { return receivingEmployeeEmail; }
    public void setReceivingEmployeeEmail(String receivingEmployeeEmail) { this.receivingEmployeeEmail = receivingEmployeeEmail; }
    public String getReceivingEmployeeAvatar() { return receivingEmployeeAvatar; }
    public void setReceivingEmployeeAvatar(String receivingEmployeeAvatar) { this.receivingEmployeeAvatar = receivingEmployeeAvatar; }
    public Long getInitiatedById() { return initiatedById; }
    public void setInitiatedById(Long initiatedById) { this.initiatedById = initiatedById; }
    public String getInitiatedByName() { return initiatedByName; }
    public void setInitiatedByName(String initiatedByName) { this.initiatedByName = initiatedByName; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDate getHandoverDate() { return handoverDate; }
    public void setHandoverDate(LocalDate handoverDate) { this.handoverDate = handoverDate; }
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getReadinessScore() { return readinessScore; }
    public void setReadinessScore(Integer readinessScore) { this.readinessScore = readinessScore; }
    public String getReadinessChecklist() { return readinessChecklist; }
    public void setReadinessChecklist(String readinessChecklist) { this.readinessChecklist = readinessChecklist; }
    public String getAiGeneratedSummary() { return aiGeneratedSummary; }
    public void setAiGeneratedSummary(String aiGeneratedSummary) { this.aiGeneratedSummary = aiGeneratedSummary; }
    public String getWorkCompletedNotes() { return workCompletedNotes; }
    public void setWorkCompletedNotes(String workCompletedNotes) { this.workCompletedNotes = workCompletedNotes; }
    public String getWorkInProgressNotes() { return workInProgressNotes; }
    public void setWorkInProgressNotes(String workInProgressNotes) { this.workInProgressNotes = workInProgressNotes; }
    public String getBlockersNotes() { return blockersNotes; }
    public void setBlockersNotes(String blockersNotes) { this.blockersNotes = blockersNotes; }
    public String getRecommendedStepsNotes() { return recommendedStepsNotes; }
    public void setRecommendedStepsNotes(String recommendedStepsNotes) { this.recommendedStepsNotes = recommendedStepsNotes; }
    public LocalDateTime getAcknowledgedAt() { return acknowledgedAt; }
    public void setAcknowledgedAt(LocalDateTime acknowledgedAt) { this.acknowledgedAt = acknowledgedAt; }
    public String getAcknowledgementNotes() { return acknowledgementNotes; }
    public void setAcknowledgementNotes(String acknowledgementNotes) { this.acknowledgementNotes = acknowledgementNotes; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<HandoverTaskDto> getTasks() { return tasks; }
    public void setTasks(List<HandoverTaskDto> tasks) { this.tasks = tasks; }
    public List<HandoverDocumentDto> getDocuments() { return documents; }
    public void setDocuments(List<HandoverDocumentDto> documents) { this.documents = documents; }
    public ReadinessScoreResponse getReadinessReport() { return readinessReport; }
    public void setReadinessReport(ReadinessScoreResponse readinessReport) { this.readinessReport = readinessReport; }
}
