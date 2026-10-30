package com.handoffiq.controller;

import com.handoffiq.dto.*;
import com.handoffiq.security.UserPrincipal;
import com.handoffiq.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Task management, status updates, outcome tracking, and carry-forward")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @Operation(summary = "Create a new action item or project task")
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        TaskResponse response = taskService.createTask(request, principal.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get tasks with multi-parameter filtering")
    public ResponseEntity<List<TaskResponse>> getTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) Boolean myTasksOnly,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long effectiveAssigneeId = assigneeId;
        if (Boolean.TRUE.equals(myTasksOnly) && principal != null) {
            effectiveAssigneeId = principal.getId();
        }
        List<TaskResponse> tasks = taskService.getAllTasks(status, priority, projectId, effectiveAssigneeId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task details by ID")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        TaskResponse response = taskService.getTaskById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update task details, ownership, or deadlines")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        TaskResponse response = taskService.updateTask(id, request, principal.getId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update task status (TODO, IN_PROGRESS, BLOCKED, COMPLETED, CANCELLED)")
    public ResponseEntity<TaskResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody TaskStatusUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        TaskResponse response = taskService.updateStatus(id, request, principal.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/outcome")
    @Operation(summary = "Record task completion outcome and detailed notes")
    public ResponseEntity<TaskResponse> recordOutcome(
            @PathVariable Long id,
            @Valid @RequestBody TaskOutcomeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        TaskResponse response = taskService.recordOutcome(id, request, principal.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/carry-forward")
    @Operation(summary = "Carry forward unresolved task to another meeting or handover")
    public ResponseEntity<TaskResponse> carryForward(
            @PathVariable Long id,
            @Valid @RequestBody CarryForwardRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        TaskResponse response = taskService.carryForwardTask(id, request, principal.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/reassign")
    @Operation(summary = "Reassign task to another employee with documented reason")
    public ResponseEntity<TaskResponse> reassignTask(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserPrincipal principal) {
        Long newAssigneeId = Long.valueOf(body.get("newAssigneeId").toString());
        String reason = body.getOrDefault("reason", "Direct reassignment").toString();
        TaskResponse response = taskService.reassignTask(id, newAssigneeId, reason, principal.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/comments")
    @Operation(summary = "Post a comment or discussion note on a task")
    public ResponseEntity<Void> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal principal) {
        String comment = body.getOrDefault("comment", "");
        taskService.addComment(id, comment, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
