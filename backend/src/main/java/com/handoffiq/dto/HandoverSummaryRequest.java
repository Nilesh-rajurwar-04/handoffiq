package com.handoffiq.dto;

import jakarta.validation.constraints.NotNull;

public class HandoverSummaryRequest {

    @NotNull(message = "Handover ID is required")
    private Long handoverId;

    public HandoverSummaryRequest() {}

    public HandoverSummaryRequest(Long handoverId) {
        this.handoverId = handoverId;
    }

    public Long getHandoverId() { return handoverId; }
    public void setHandoverId(Long handoverId) { this.handoverId = handoverId; }
}
