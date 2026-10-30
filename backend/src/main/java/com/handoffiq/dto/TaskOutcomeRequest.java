package com.handoffiq.dto;

import jakarta.validation.constraints.NotBlank;

public class TaskOutcomeRequest {

    @NotBlank(message = "Outcome type is required")
    private String outcomeType; // Completed successfully, Documentation prepared, API integration completed, Issue identified, Work partially completed

    @NotBlank(message = "Detailed outcome notes are required")
    private String detailedNotes;

    public TaskOutcomeRequest() {}

    public TaskOutcomeRequest(String outcomeType, String detailedNotes) {
        this.outcomeType = outcomeType;
        this.detailedNotes = detailedNotes;
    }

    public String getOutcomeType() { return outcomeType; }
    public void setOutcomeType(String outcomeType) { this.outcomeType = outcomeType; }

    public String getDetailedNotes() { return detailedNotes; }
    public void setDetailedNotes(String detailedNotes) { this.detailedNotes = detailedNotes; }
}
