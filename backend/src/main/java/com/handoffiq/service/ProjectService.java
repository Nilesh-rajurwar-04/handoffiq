package com.handoffiq.service;

import com.handoffiq.entity.*;
import com.handoffiq.exception.ResourceNotFoundException;
import com.handoffiq.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final MeetingRepository meetingRepository;
    private final TaskRepository taskRepository;
    private final DecisionRepository decisionRepository;
    private final HandoverRepository handoverRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            ProjectMemberRepository projectMemberRepository,
            MeetingRepository meetingRepository,
            TaskRepository taskRepository,
            DecisionRepository decisionRepository,
            HandoverRepository handoverRepository,
            TaskAssignmentRepository taskAssignmentRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.meetingRepository = meetingRepository;
        this.taskRepository = taskRepository;
        this.decisionRepository = decisionRepository;
        this.handoverRepository = handoverRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<ProjectMember> getProjectMembers(Long projectId) {
        return projectMemberRepository.findByProjectId(projectId);
    }

    /**
     * Project Continuity View:
     * Shows overall progress, pending responsibilities, previous owners, and current owners in one screen.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getProjectContinuityView(Long projectId) {
        Project project = getProjectById(projectId);
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        List<Handover> handovers = handoverRepository.findByProjectId(projectId);

        long totalTasks = tasks.size();
        long completedTasks = tasks.stream().filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus())).count();
        long inProgressTasks = tasks.stream().filter(t -> "IN_PROGRESS".equalsIgnoreCase(t.getStatus())).count();
        long blockedTasks = tasks.stream().filter(t -> "BLOCKED".equalsIgnoreCase(t.getStatus())).count();
        long todoTasks = tasks.stream().filter(t -> "TODO".equalsIgnoreCase(t.getStatus())).count();

        int progressPercentage = totalTasks > 0 ? (int) Math.round(((double) completedTasks / totalTasks) * 100) : 0;

        List<Map<String, Object>> continuityTasks = new ArrayList<>();
        for (Task t : tasks) {
            Map<String, Object> taskMap = new LinkedHashMap<>();
            taskMap.put("taskId", t.getId());
            taskMap.put("title", t.getTitle());
            taskMap.put("status", t.getStatus());
            taskMap.put("priority", t.getPriority());
            taskMap.put("dueDate", t.getDueDate());
            taskMap.put("currentOwner", t.getAssignedTo() != null ? t.getAssignedTo().getFullName() : "Unassigned");
            taskMap.put("currentOwnerAvatar", t.getAssignedTo() != null ? t.getAssignedTo().getAvatarUrl() : null);

            // Fetch previous assignments
            List<TaskAssignment> assignments = taskAssignmentRepository.findByTaskIdOrderByAssignedAtDesc(t.getId());
            if (!assignments.isEmpty() && assignments.get(0).getPreviousAssignee() != null) {
                taskMap.put("previousOwner", assignments.get(0).getPreviousAssignee().getFullName());
                taskMap.put("reassignedReason", assignments.get(0).getReason());
                taskMap.put("reassignedAt", assignments.get(0).getAssignedAt());
            } else {
                taskMap.put("previousOwner", "Original Assignee");
                taskMap.put("reassignedReason", "Initial creation");
                taskMap.put("reassignedAt", t.getCreatedAt());
            }

            continuityTasks.add(taskMap);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("project", project);
        result.put("progressPercentage", progressPercentage);
        result.put("metrics", Map.of(
                "totalTasks", totalTasks,
                "completedTasks", completedTasks,
                "inProgressTasks", inProgressTasks,
                "blockedTasks", blockedTasks,
                "todoTasks", todoTasks,
                "handoversCount", handovers.size()
        ));
        result.put("tasksContinuity", continuityTasks);
        result.put("handovers", handovers);

        return result;
    }

    /**
     * Knowledge Timeline:
     * Chronological unified stream of meetings, decisions, tasks, and handovers.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getKnowledgeTimeline(Long projectId) {
        List<Map<String, Object>> timeline = new ArrayList<>();

        // 1. Meetings
        List<Meeting> meetings = meetingRepository.findByProjectIdOrderByScheduledStartDesc(projectId);
        for (Meeting m : meetings) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", "MEETING");
            item.put("id", m.getId());
            item.put("title", m.getTitle());
            item.put("timestamp", m.getScheduledStart());
            item.put("actor", m.getCreatedBy() != null ? m.getCreatedBy().getFullName() : "Organizer");
            item.put("details", m.getAiSummary() != null ? m.getAiSummary() : m.getAgenda());
            item.put("status", m.getStatus());
            timeline.add(item);
        }

        // 2. Decisions
        List<Decision> decisions = decisionRepository.findByProjectIdOrderByDecidedAtDesc(projectId);
        for (Decision d : decisions) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", "DECISION");
            item.put("id", d.getId());
            item.put("title", d.getTitle());
            item.put("timestamp", d.getDecidedAt());
            item.put("actor", d.getDecidedBy() != null ? d.getDecidedBy().getFullName() : "Lead");
            item.put("details", d.getRationale());
            item.put("status", d.getStatus());
            timeline.add(item);
        }

        // 3. Handovers
        List<Handover> handovers = handoverRepository.findByProjectId(projectId);
        for (Handover h : handovers) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("type", "HANDOVER");
            item.put("id", h.getId());
            item.put("title", h.getTitle());
            item.put("timestamp", h.getCreatedAt());
            item.put("actor", h.getCurrentEmployee().getFullName() + " -> " + h.getReceivingEmployee().getFullName());
            item.put("details", h.getReason() + " (Readiness: " + h.getReadinessScore() + "/100)");
            item.put("status", h.getStatus());
            timeline.add(item);
        }

        // 4. Tasks (completed or critical)
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        for (Task t : tasks) {
            if ("COMPLETED".equalsIgnoreCase(t.getStatus()) || "BLOCKED".equalsIgnoreCase(t.getStatus())) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("type", "TASK");
                item.put("id", t.getId());
                item.put("title", t.getTitle());
                item.put("timestamp", t.getUpdatedAt() != null ? t.getUpdatedAt() : t.getCreatedAt());
                item.put("actor", t.getAssignedTo() != null ? t.getAssignedTo().getFullName() : "Team");
                item.put("details", t.getCompletionOutcome() != null ? t.getCompletionOutcome() : ("Task status changed to " + t.getStatus()));
                item.put("status", t.getStatus());
                timeline.add(item);
            }
        }

        // Sort descending by timestamp
        timeline.sort((a, b) -> {
            Object t1 = a.get("timestamp");
            Object t2 = b.get("timestamp");
            if (t1 == null && t2 == null) return 0;
            if (t1 == null) return 1;
            if (t2 == null) return -1;
            return t2.toString().compareTo(t1.toString());
        });

        return timeline;
    }
}
