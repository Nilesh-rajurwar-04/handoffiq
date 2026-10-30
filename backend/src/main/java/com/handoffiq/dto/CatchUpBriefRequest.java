package com.handoffiq.dto;

public class CatchUpBriefRequest {

    private Long projectId;
    private Integer daysLookback;
    private Long specificMeetingId;

    public CatchUpBriefRequest() {}

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public Integer getDaysLookback() { return daysLookback; }
    public void setDaysLookback(Integer daysLookback) { this.daysLookback = daysLookback; }
    public Long getSpecificMeetingId() { return specificMeetingId; }
    public void setSpecificMeetingId(Long specificMeetingId) { this.specificMeetingId = specificMeetingId; }
}
