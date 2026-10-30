package com.handoffiq.controller;

import com.handoffiq.dto.*;
import com.handoffiq.security.UserPrincipal;
import com.handoffiq.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI Intelligence", description = "AI Meeting Intelligence, Catch-up Briefs, and Grounded Assistant")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/meeting-analysis")
    @Operation(summary = "Analyze raw meeting notes to extract structured summaries, decisions, action items, and risks")
    public ResponseEntity<MeetingAnalysisResponse> analyzeMeeting(
            @Valid @RequestBody MeetingAnalysisRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        MeetingAnalysisResponse response = aiService.analyzeMeetingNotes(request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/catch-up-brief")
    @Operation(summary = "Generate personalized catch-up brief for employees returning or joining a project")
    public ResponseEntity<CatchUpBriefResponse> generateCatchUpBrief(
            @RequestBody CatchUpBriefRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        CatchUpBriefResponse response = aiService.generateCatchUpBrief(request, principal.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/handover-summary")
    @Operation(summary = "Generate comprehensive AI handover briefing report")
    public ResponseEntity<HandoverSummaryResponse> generateHandoverSummary(
            @Valid @RequestBody HandoverSummaryRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        HandoverSummaryResponse response = aiService.generateHandoverSummary(request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assistant")
    @Operation(summary = "Query context-aware Handoff Assistant grounded in actual database knowledge")
    public ResponseEntity<AiAssistantResponse> askAssistant(
            @Valid @RequestBody AiAssistantRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        AiAssistantResponse response = aiService.askAssistant(request, principal.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/knowledge-search")
    @Operation(summary = "Perform semantic/keyword knowledge search with AI-synthesized answer and source citations")
    public ResponseEntity<KnowledgeSearchResponse> searchKnowledge(
            @Valid @RequestBody KnowledgeSearchRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        KnowledgeSearchResponse response = aiService.searchKnowledge(request, userId);
        return ResponseEntity.ok(response);
    }
}
