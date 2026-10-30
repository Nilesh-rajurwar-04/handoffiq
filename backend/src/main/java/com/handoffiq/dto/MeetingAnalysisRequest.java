package com.handoffiq.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class MeetingAnalysisRequest {

    @NotBlank(message = "Raw meeting notes are required for analysis")
    private String rawNotes;

    private Long meetingId;
    private Long projectId;

    public MeetingAnalysisRequest() {}

    public MeetingAnalysisRequest(String rawNotes, Long meetingId, Long projectId) {
        this.rawNotes = rawNotes;
        this.meetingId = meetingId;
        this.projectId = projectId;
    }

    public String getRawNotes() { return rawNotes; }
    public void setRawNotes(String rawNotes) { this.rawNotes = rawNotes; }
    public Long getMeetingId() { return meetingId; }
    public void setMeetingId(Long meetingId) { this.meetingId = meetingId; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
}
