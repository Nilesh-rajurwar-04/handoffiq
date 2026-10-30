package com.handoffiq.dto;

import java.util.List;

public class MeetingAnalysisResponse {

    private String meetingSummary;
    private List<String> importantDiscussionPoints;
    private List<DecisionItem> decisions;
    private List<ActionItem> actionItems;
    private String priority; // LOW, MEDIUM, HIGH, CRITICAL
    private List<String> risksAndUnresolvedQuestions;
    private String rawAiResponse;
    private boolean fallbackUsed;

    public MeetingAnalysisResponse() {}

    public static class DecisionItem {
        private String title;
        private String rationale;
        private String context;

        public DecisionItem() {}
        public DecisionItem(String title, String rationale, String context) {
            this.title = title;
            this.rationale = rationale;
            this.context = context;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getRationale() { return rationale; }
        public void setRationale(String rationale) { this.rationale = rationale; }
        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }
    }

    public static class ActionItem {
        private String title;
        private String description;
        private String suggestedOwnerName;
        private Long suggestedOwnerId; // matched with DB user
        private String suggestedDeadline;
        private String priority;
        private boolean isConfirmed;

        public ActionItem() {}
        public ActionItem(String title, String description, String suggestedOwnerName, Long suggestedOwnerId, String suggestedDeadline, String priority) {
            this.title = title;
            this.description = description;
            this.suggestedOwnerName = suggestedOwnerName;
            this.suggestedOwnerId = suggestedOwnerId;
            this.suggestedDeadline = suggestedDeadline;
            this.priority = priority;
            this.isConfirmed = false;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getSuggestedOwnerName() { return suggestedOwnerName; }
        public void setSuggestedOwnerName(String suggestedOwnerName) { this.suggestedOwnerName = suggestedOwnerName; }
        public Long getSuggestedOwnerId() { return suggestedOwnerId; }
        public void setSuggestedOwnerId(Long suggestedOwnerId) { this.suggestedOwnerId = suggestedOwnerId; }
        public String getSuggestedDeadline() { return suggestedDeadline; }
        public void setSuggestedDeadline(String suggestedDeadline) { this.suggestedDeadline = suggestedDeadline; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        public boolean isConfirmed() { return isConfirmed; }
        public void setConfirmed(boolean confirmed) { isConfirmed = confirmed; }
    }

    public String getMeetingSummary() { return meetingSummary; }
    public void setMeetingSummary(String meetingSummary) { this.meetingSummary = meetingSummary; }
    public List<String> getImportantDiscussionPoints() { return importantDiscussionPoints; }
    public void setImportantDiscussionPoints(List<String> importantDiscussionPoints) { this.importantDiscussionPoints = importantDiscussionPoints; }
    public List<DecisionItem> getDecisions() { return decisions; }
    public void setDecisions(List<DecisionItem> decisions) { this.decisions = decisions; }
    public List<ActionItem> getActionItems() { return actionItems; }
    public void setActionItems(List<ActionItem> actionItems) { this.actionItems = actionItems; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public List<String> getRisksAndUnresolvedQuestions() { return risksAndUnresolvedQuestions; }
    public void setRisksAndUnresolvedQuestions(List<String> risksAndUnresolvedQuestions) { this.risksAndUnresolvedQuestions = risksAndUnresolvedQuestions; }
    public String getRawAiResponse() { return rawAiResponse; }
    public void setRawAiResponse(String rawAiResponse) { this.rawAiResponse = rawAiResponse; }
    public boolean isFallbackUsed() { return fallbackUsed; }
    public void setFallbackUsed(boolean fallbackUsed) { this.fallbackUsed = fallbackUsed; }
}
