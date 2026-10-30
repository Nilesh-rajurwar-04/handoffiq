package com.handoffiq.controller;

import com.handoffiq.dto.*;
import com.handoffiq.security.UserPrincipal;
import com.handoffiq.service.HandoverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/handovers")
@Tag(name = "Handovers", description = "Employee handover workflows and Handover Readiness Score engine")
public class HandoverController {

    private final HandoverService handoverService;

    public HandoverController(HandoverService handoverService) {
        this.handoverService = handoverService;
    }

    @PostMapping
    @Operation(summary = "Initiate a new employee handover package")
    public ResponseEntity<HandoverResponse> createHandover(
            @Valid @RequestBody CreateHandoverRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        HandoverResponse response = handoverService.createHandover(request, principal.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "List all employee handovers with optional status and project filtering")
    public ResponseEntity<List<HandoverResponse>> getAllHandovers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long employeeId) {
        List<HandoverResponse> handovers = handoverService.getAllHandovers(status, projectId, employeeId);
        return ResponseEntity.ok(handovers);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed handover package by ID with full readiness metrics")
    public ResponseEntity<HandoverResponse> getHandoverById(@PathVariable Long id) {
        HandoverResponse response = handoverService.getHandoverById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/readiness")
    @Operation(summary = "Get transparent Handover Readiness Score (0-100) and criteria checklist")
    public ResponseEntity<ReadinessScoreResponse> getReadinessScore(@PathVariable Long id) {
        ReadinessScoreResponse score = handoverService.getReadinessScore(id);
        return ResponseEntity.ok(score);
    }

    @PostMapping("/{id}/accept")
    @Operation(summary = "Receiving employee formal review and acknowledgement")
    public ResponseEntity<HandoverResponse> acceptHandover(
            @PathVariable Long id,
            @Valid @RequestBody HandoverAcknowledgeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        HandoverResponse response = handoverService.acknowledgeHandover(id, request, principal.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Finalize and complete handover (guarded by Handover Readiness Score check)")
    public ResponseEntity<HandoverResponse> completeHandover(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        HandoverResponse response = handoverService.completeHandover(id, principal.getId());
        return ResponseEntity.ok(response);
    }
}
