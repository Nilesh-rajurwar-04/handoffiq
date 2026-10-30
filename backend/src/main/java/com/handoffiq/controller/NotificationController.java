package com.handoffiq.controller;

import com.handoffiq.dto.NotificationResponse;
import com.handoffiq.security.UserPrincipal;
import com.handoffiq.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications & Follow-Ups", description = "In-app notifications and intelligent task follow-up engine")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @Operation(summary = "Get notifications for currently authenticated user")
    public ResponseEntity<List<NotificationResponse>> getNotifications(@AuthenticationPrincipal UserPrincipal principal) {
        List<NotificationResponse> notifications = notificationService.getUserNotifications(principal.getId());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get count of unread notifications")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        long count = notificationService.getUnreadCount(principal.getId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAsRead(id, principal.getId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAllAsRead(principal.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/pending-follow-ups")
    @Operation(summary = "Get organization-wide pending follow-ups (overdue tasks, unassigned tasks, blocked tasks, pending handovers)")
    public ResponseEntity<Map<String, Object>> getPendingFollowUps() {
        Map<String, Object> followUps = notificationService.getPendingFollowUps();
        return ResponseEntity.ok(followUps);
    }
}
