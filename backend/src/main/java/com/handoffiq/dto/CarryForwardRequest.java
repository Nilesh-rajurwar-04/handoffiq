package com.handoffiq.dto;

import jakarta.validation.constraints.NotBlank;

public class CarryForwardRequest {

    private Long toMeetingId;
    private Long toHandoverId;

    @NotBlank(message = "Reason for carry-forward is required")
    private String reason;

    public CarryForwardRequest() {}

    public CarryForwardRequest(Long toMeetingId, Long toHandoverId, String reason) {
        this.toMeetingId = toMeetingId;
        this.toHandoverId = toHandoverId;
        this.reason = reason;
    }

    public Long getToMeetingId() { return toMeetingId; }
    public void setToMeetingId(Long toMeetingId) { this.toMeetingId = toMeetingId; }

    public Long getToHandoverId() { return toHandoverId; }
    public void setToHandoverId(Long toHandoverId) { this.toHandoverId = toHandoverId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
