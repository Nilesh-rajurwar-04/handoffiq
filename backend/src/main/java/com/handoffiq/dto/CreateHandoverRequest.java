package com.handoffiq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public class CreateHandoverRequest {

    @NotBlank(message = "Handover title is required")
    private String title;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotNull(message = "Current employee ID is required")
    private Long currentEmployeeId;

    @NotNull(message = "Receiving employee ID is required")
    private Long receivingEmployeeId;

    @NotBlank(message = "Handover reason is required")
    private String reason;

    @NotNull(message = "Handover target date is required")
    private LocalDate handoverDate;

    private String specialInstructions;
    private List<Long> taskIds;
    private List<Long> documentIds;

    public CreateHandoverRequest() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Long getCurrentEmployeeId() { return currentEmployeeId; }
    public void setCurrentEmployeeId(Long currentEmployeeId) { this.currentEmployeeId = currentEmployeeId; }
    public Long getReceivingEmployeeId() { return receivingEmployeeId; }
    public void setReceivingEmployeeId(Long receivingEmployeeId) { this.receivingEmployeeId = receivingEmployeeId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDate getHandoverDate() { return handoverDate; }
    public void setHandoverDate(LocalDate handoverDate) { this.handoverDate = handoverDate; }
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
    public List<Long> getTaskIds() { return taskIds; }
    public void setTaskIds(List<Long> taskIds) { this.taskIds = taskIds; }
    public List<Long> getDocumentIds() { return documentIds; }
    public void setDocumentIds(List<Long> documentIds) { this.documentIds = documentIds; }
}
