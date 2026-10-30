package com.handoffiq.dto;

import java.util.List;

public class HandoverSummaryResponse {

    private Long handoverId;
    private String projectOverview;
    private String workCompleted;
    private String workInProgress;
    private List<String> pendingTasks;
    private List<String> upcomingDeadlines;
    private List<String> importantDecisions;
    private List<String> knownIssuesAndBlockers;
    private List<String> relevantDocuments;
    private List<String> recommendedNextSteps;
    private String formattedMarkdown;
    private boolean fallbackUsed;

    public HandoverSummaryResponse() {}

    public Long getHandoverId() { return handoverId; }
    public void setHandoverId(Long handoverId) { this.handoverId = handoverId; }
    public String getProjectOverview() { return projectOverview; }
    public void setProjectOverview(String projectOverview) { this.projectOverview = projectOverview; }
    public String getWorkCompleted() { return workCompleted; }
    public void setWorkCompleted(String workCompleted) { this.workCompleted = workCompleted; }
    public String getWorkInProgress() { return workInProgress; }
    public void setWorkInProgress(String workInProgress) { this.workInProgress = workInProgress; }
    public List<String> getPendingTasks() { return pendingTasks; }
    public void setPendingTasks(List<String> pendingTasks) { this.pendingTasks = pendingTasks; }
    public List<String> getUpcomingDeadlines() { return upcomingDeadlines; }
    public void setUpcomingDeadlines(List<String> upcomingDeadlines) { this.upcomingDeadlines = upcomingDeadlines; }
    public List<String> getImportantDecisions() { return importantDecisions; }
    public void setImportantDecisions(List<String> importantDecisions) { this.importantDecisions = importantDecisions; }
    public List<String> getKnownIssuesAndBlockers() { return knownIssuesAndBlockers; }
    public void setKnownIssuesAndBlockers(List<String> knownIssuesAndBlockers) { this.knownIssuesAndBlockers = knownIssuesAndBlockers; }
    public List<String> getRelevantDocuments() { return relevantDocuments; }
    public void setRelevantDocuments(List<String> relevantDocuments) { this.relevantDocuments = relevantDocuments; }
    public List<String> getRecommendedNextSteps() { return recommendedNextSteps; }
    public void setRecommendedNextSteps(List<String> recommendedNextSteps) { this.recommendedNextSteps = recommendedNextSteps; }
    public String getFormattedMarkdown() { return formattedMarkdown; }
    public void setFormattedMarkdown(String formattedMarkdown) { this.formattedMarkdown = formattedMarkdown; }
    public boolean isFallbackUsed() { return fallbackUsed; }
    public void setFallbackUsed(boolean fallbackUsed) { this.fallbackUsed = fallbackUsed; }
}
