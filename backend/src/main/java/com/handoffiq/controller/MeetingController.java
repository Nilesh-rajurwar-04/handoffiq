package com.handoffiq.controller;

import com.handoffiq.dto.CreateMeetingRequest;
import com.handoffiq.dto.MeetingResponse;
import com.handoffiq.security.UserPrincipal;
import com.handoffiq.service.MeetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meetings")
@Tag(name = "Meetings", description = "Meeting lifecycle management and participant collaboration")
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @PostMapping
    @Operation(summary = "Create a new meeting")
    public ResponseEntity<MeetingResponse> createMeeting(
            @Valid @RequestBody CreateMeetingRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        MeetingResponse response = meetingService.createMeeting(request, principal.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "List all meetings, optionally filtered by project")
    public ResponseEntity<List<MeetingResponse>> getMeetings(@RequestParam(required = false) Long projectId) {
        List<MeetingResponse> meetings = projectId != null ?
                meetingService.getMeetingsByProject(projectId) :
                meetingService.getAllMeetings();
        return ResponseEntity.ok(meetings);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed meeting record by ID")
    public ResponseEntity<MeetingResponse> getMeetingById(@PathVariable Long id) {
        MeetingResponse response = meetingService.getMeetingById(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/notes")
    @Operation(summary = "Update raw notes and AI summary on a meeting")
    public ResponseEntity<MeetingResponse> updateNotes(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal principal) {
        String rawNotes = body.get("rawNotes");
        String aiSummary = body.get("aiSummary");
        MeetingResponse response = meetingService.updateNotes(id, rawNotes, aiSummary, principal.getId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Transition meeting lifecycle status (SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED)")
    public ResponseEntity<MeetingResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal principal) {
        String status = body.getOrDefault("status", "COMPLETED");
        MeetingResponse response = meetingService.updateStatus(id, status, principal.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Delete meeting record (Authorized roles only)")
    public ResponseEntity<Void> deleteMeeting(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        meetingService.deleteMeeting(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
