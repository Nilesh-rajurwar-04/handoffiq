package com.handoffiq.dto;

import jakarta.validation.constraints.NotBlank;

public class KnowledgeSearchRequest {

    @NotBlank(message = "Search query is required")
    private String query;

    private String category;
    private Long projectId;

    public KnowledgeSearchRequest() {}

    public KnowledgeSearchRequest(String query, String category, Long projectId) {
        this.query = query;
        this.category = category;
        this.projectId = projectId;
    }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
}
