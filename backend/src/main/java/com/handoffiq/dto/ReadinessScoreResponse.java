package com.handoffiq.dto;

import java.util.List;

public class ReadinessScoreResponse {

    private Long handoverId;
    private int overallScore; // 0 - 100
    private String statusCategory; // EXCELLENT (90-100), GOOD (75-89), NEEDS_ATTENTION (50-74), CRITICAL (<50)
    private boolean isReadyForCompletion;
    private List<ReadinessChecklistItem> checklist;
    private List<String> missingRequirements;
    private List<String> recommendedActions;

    public ReadinessScoreResponse() {}

    public ReadinessScoreResponse(Long handoverId, int overallScore, String statusCategory, boolean isReadyForCompletion, List<ReadinessChecklistItem> checklist, List<String> missingRequirements, List<String> recommendedActions) {
        this.handoverId = handoverId;
        this.overallScore = overallScore;
        this.statusCategory = statusCategory;
        this.isReadyForCompletion = isReadyForCompletion;
        this.checklist = checklist;
        this.missingRequirements = missingRequirements;
        this.recommendedActions = recommendedActions;
    }

    public Long getHandoverId() { return handoverId; }
    public void setHandoverId(Long handoverId) { this.handoverId = handoverId; }
    public int getOverallScore() { return overallScore; }
    public void setOverallScore(int overallScore) { this.overallScore = overallScore; }
    public String getStatusCategory() { return statusCategory; }
    public void setStatusCategory(String statusCategory) { this.statusCategory = statusCategory; }
    public boolean isReadyForCompletion() { return isReadyForCompletion; }
    public void setReadyForCompletion(boolean readyForCompletion) { isReadyForCompletion = readyForCompletion; }
    public List<ReadinessChecklistItem> getChecklist() { return checklist; }
    public void setChecklist(List<ReadinessChecklistItem> checklist) { this.checklist = checklist; }
    public List<String> getMissingRequirements() { return missingRequirements; }
    public void setMissingRequirements(List<String> missingRequirements) { this.missingRequirements = missingRequirements; }
    public List<String> getRecommendedActions() { return recommendedActions; }
    public void setRecommendedActions(List<String> recommendedActions) { this.recommendedActions = recommendedActions; }
}
