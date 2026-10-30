package com.handoffiq.controller;

import com.handoffiq.dto.DashboardSummaryResponse;
import com.handoffiq.security.UserPrincipal;
import com.handoffiq.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Executive telemetry, analytics, and priority metrics")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping({"/summary", "/stats", "/metrics"})
    @Operation(summary = "Get full executive dashboard summary with KPIs and chart feeds")
    public ResponseEntity<DashboardSummaryResponse> getSummary(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        DashboardSummaryResponse summary = dashboardService.getDashboardSummary(userId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/activities")
    @Operation(summary = "Get recent activities")
    public ResponseEntity<List<DashboardSummaryResponse.ActivityLogDto>> getActivities(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        DashboardSummaryResponse summary = dashboardService.getDashboardSummary(userId);
        return ResponseEntity.ok(summary.getRecentActivities());
    }
}
