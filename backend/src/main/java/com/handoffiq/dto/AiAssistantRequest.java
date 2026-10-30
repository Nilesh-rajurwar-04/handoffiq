package com.handoffiq.dto;

import jakarta.validation.constraints.NotBlank;

public class AiAssistantRequest {

    @NotBlank(message = "Question or prompt is required")
    private String question;

    private Long projectId;

    public AiAssistantRequest() {}

    public AiAssistantRequest(String question, Long projectId) {
        this.question = question;
        this.projectId = projectId;
    }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
}
