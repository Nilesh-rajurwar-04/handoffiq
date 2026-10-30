package com.handoffiq.controller;

import com.handoffiq.entity.Project;
import com.handoffiq.entity.ProjectMember;
import com.handoffiq.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "Project management, knowledge timelines, and continuity mapping")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @Operation(summary = "List all active enterprise projects")
    public ResponseEntity<List<Project>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project details by ID")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        Project project = projectService.getProjectById(id);
        return ResponseEntity.ok(project);
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "Get project members and designated roles")
    public ResponseEntity<List<ProjectMember>> getProjectMembers(@PathVariable Long id) {
        List<ProjectMember> members = projectService.getProjectMembers(id);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/{id}/continuity")
    @Operation(summary = "Project Continuity View: Show overall progress, pending responsibilities, previous owners, and current owners in one screen")
    public ResponseEntity<Map<String, Object>> getProjectContinuityView(@PathVariable Long id) {
        Map<String, Object> continuity = projectService.getProjectContinuityView(id);
        return ResponseEntity.ok(continuity);
    }

    @GetMapping("/{id}/timeline")
    @Operation(summary = "Knowledge Timeline: Chronological stream of meetings, decisions, tasks, and handovers")
    public ResponseEntity<List<Map<String, Object>>> getKnowledgeTimeline(@PathVariable Long id) {
        List<Map<String, Object>> timeline = projectService.getKnowledgeTimeline(id);
        return ResponseEntity.ok(timeline);
    }
}
