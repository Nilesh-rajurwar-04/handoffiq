package com.handoffiq.dto;

import jakarta.validation.constraints.NotBlank;

public class TaskStatusUpdateRequest {

    @NotBlank(message = "New status is required")
    private String status; // TODO, IN_PROGRESS, BLOCKED, COMPLETED, CANCELLED

    private String remarks;

    public TaskStatusUpdateRequest() {}

    public TaskStatusUpdateRequest(String status, String remarks) {
        this.status = status;
        this.remarks = remarks;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
