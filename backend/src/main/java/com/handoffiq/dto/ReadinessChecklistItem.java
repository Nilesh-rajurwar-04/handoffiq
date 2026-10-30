package com.handoffiq.dto;

public class ReadinessChecklistItem {

    private String criterion;
    private String description;
    private int weight;
    private int maxScore;
    private int earnedScore;
    private boolean isPassed;
    private boolean isMandatory;
    private String statusDetails;
    private String correctiveAction;

    public ReadinessChecklistItem() {}

    public ReadinessChecklistItem(String criterion, String description, int weight, int maxScore, int earnedScore, boolean isPassed, boolean isMandatory, String statusDetails, String correctiveAction) {
        this.criterion = criterion;
        this.description = description;
        this.weight = weight;
        this.maxScore = maxScore;
        this.earnedScore = earnedScore;
        this.isPassed = isPassed;
        this.isMandatory = isMandatory;
        this.statusDetails = statusDetails;
        this.correctiveAction = correctiveAction;
    }

    public String getCriterion() { return criterion; }
    public void setCriterion(String criterion) { this.criterion = criterion; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
    public int getMaxScore() { return maxScore; }
    public void setMaxScore(int maxScore) { this.maxScore = maxScore; }
    public int getEarnedScore() { return earnedScore; }
    public void setEarnedScore(int earnedScore) { this.earnedScore = earnedScore; }
    public boolean isPassed() { return isPassed; }
    public void setPassed(boolean passed) { isPassed = passed; }
    public boolean isMandatory() { return isMandatory; }
    public void setMandatory(boolean mandatory) { isMandatory = mandatory; }
    public String getStatusDetails() { return statusDetails; }
    public void setStatusDetails(String statusDetails) { this.statusDetails = statusDetails; }
    public String getCorrectiveAction() { return correctiveAction; }
    public void setCorrectiveAction(String correctiveAction) { this.correctiveAction = correctiveAction; }
}
