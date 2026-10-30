package com.handoffiq.dto;

import java.util.List;

public class CatchUpBriefResponse {

    private String briefTitle;
    private String projectName;
    private String employeeName;
    private String generatedAt;
    private String executiveSummary;
    private List<MeetingDigestItem> whatHappened;
    private List<DecisionDigestItem> keyDecisions;
    private List<String> currentDevelopments;
    private List<TaskDigestItem> employeeResponsibilities;
    private List<TaskDigestItem> pendingTeamTasks;
    private List<String> approachingDeadlines;
    private List<String> recommendedNextSteps;
    private boolean fallbackUsed;

    public CatchUpBriefResponse() {}

    public static class MeetingDigestItem {
        private Long meetingId;
        private String meetingTitle;
        private String meetingDate;
        private String summary;

        public MeetingDigestItem() {}
        public MeetingDigestItem(Long meetingId, String meetingTitle, String meetingDate, String summary) {
            this.meetingId = meetingId;
            this.meetingTitle = meetingTitle;
            this.meetingDate = meetingDate;
            this.summary = summary;
        }

        public Long getMeetingId() { return meetingId; }
        public void setMeetingId(Long meetingId) { this.meetingId = meetingId; }
        public String getMeetingTitle() { return meetingTitle; }
        public void setMeetingTitle(String meetingTitle) { this.meetingTitle = meetingTitle; }
        public String getMeetingDate() { return meetingDate; }
        public void setMeetingDate(String meetingDate) { this.meetingDate = meetingDate; }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
    }

    public static class DecisionDigestItem {
        private Long decisionId;
        private String title;
        private String rationale;
        private Long meetingId;

        public DecisionDigestItem() {}
        public DecisionDigestItem(Long decisionId, String title, String rationale, Long meetingId) {
            this.decisionId = decisionId;
            this.title = title;
            this.rationale = rationale;
            this.meetingId = meetingId;
        }

        public Long getDecisionId() { return decisionId; }
        public void setDecisionId(Long decisionId) { this.decisionId = decisionId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getRationale() { return rationale; }
        public void setRationale(String rationale) { this.rationale = rationale; }
        public Long getMeetingId() { return meetingId; }
        public void setMeetingId(Long meetingId) { this.meetingId = meetingId; }
    }

    public static class TaskDigestItem {
        private Long taskId;
        private String title;
        private String priority;
        private String status;
        private String dueDate;
        private String assigneeName;

        public TaskDigestItem() {}
        public TaskDigestItem(Long taskId, String title, String priority, String status, String dueDate, String assigneeName) {
            this.taskId = taskId;
            this.title = title;
            this.priority = priority;
            this.status = status;
            this.dueDate = dueDate;
            this.assigneeName = assigneeName;
        }

        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getDueDate() { return dueDate; }
        public void setDueDate(String dueDate) { this.dueDate = dueDate; }
        public String getAssigneeName() { return assigneeName; }
        public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }
    }

    public String getBriefTitle() { return briefTitle; }
    public void setBriefTitle(String briefTitle) { this.briefTitle = briefTitle; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }
    public String getExecutiveSummary() { return executiveSummary; }
    public void setExecutiveSummary(String executiveSummary) { this.executiveSummary = executiveSummary; }
    public List<MeetingDigestItem> getWhatHappened() { return whatHappened; }
    public void setWhatHappened(List<MeetingDigestItem> whatHappened) { this.whatHappened = whatHappened; }
    public List<DecisionDigestItem> getKeyDecisions() { return keyDecisions; }
    public void setKeyDecisions(List<DecisionDigestItem> keyDecisions) { this.keyDecisions = keyDecisions; }
    public List<String> getCurrentDevelopments() { return currentDevelopments; }
    public void setCurrentDevelopments(List<String> currentDevelopments) { this.currentDevelopments = currentDevelopments; }
    public List<TaskDigestItem> getEmployeeResponsibilities() { return employeeResponsibilities; }
    public void setEmployeeResponsibilities(List<TaskDigestItem> employeeResponsibilities) { this.employeeResponsibilities = employeeResponsibilities; }
    public List<TaskDigestItem> getPendingTeamTasks() { return pendingTeamTasks; }
    public void setPendingTeamTasks(List<TaskDigestItem> pendingTeamTasks) { this.pendingTeamTasks = pendingTeamTasks; }
    public List<String> getApproachingDeadlines() { return approachingDeadlines; }
    public void setApproachingDeadlines(List<String> approachingDeadlines) { this.approachingDeadlines = approachingDeadlines; }
    public List<String> getRecommendedNextSteps() { return recommendedNextSteps; }
    public void setRecommendedNextSteps(List<String> recommendedNextSteps) { this.recommendedNextSteps = recommendedNextSteps; }
    public boolean isFallbackUsed() { return fallbackUsed; }
    public void setFallbackUsed(boolean fallbackUsed) { this.fallbackUsed = fallbackUsed; }
}
