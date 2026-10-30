package com.handoffiq.dto;

import jakarta.validation.constraints.NotBlank;

public class HandoverAcknowledgeRequest {

    @NotBlank(message = "Acknowledgement comments are required")
    private String comments;

    public HandoverAcknowledgeRequest() {}

    public HandoverAcknowledgeRequest(String comments) {
        this.comments = comments;
    }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
}
