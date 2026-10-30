package com.handoffiq.service;

import com.handoffiq.dto.*;
import com.handoffiq.entity.*;
import com.handoffiq.exception.BadRequestException;
import com.handoffiq.exception.ResourceNotFoundException;
import com.handoffiq.logging.StructuredLogger;
import com.handoffiq.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final MeetingRepository meetingRepository;
    private final DecisionRepository decisionRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final TaskCommentRepository taskCommentRepository;
    private final TaskStatusHistoryRepository taskStatusHistoryRepository;
    private final TaskOutcomeRepository taskOutcomeRepository;
    private final TaskCarryForwardHistoryRepository taskCarryForwardRepository;
    private final StructuredLogger structuredLogger;

    public TaskService(
            TaskRepository taskRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            MeetingRepository meetingRepository,
            DecisionRepository decisionRepository,
            TaskAssignmentRepository taskAssignmentRepository,
            TaskCommentRepository taskCommentRepository,
            TaskStatusHistoryRepository taskStatusHistoryRepository,
            TaskOutcomeRepository taskOutcomeRepository,
            TaskCarryForwardHistoryRepository taskCarryForwardRepository,
            StructuredLogger structuredLogger) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.meetingRepository = meetingRepository;
        this.decisionRepository = decisionRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.taskCommentRepository = taskCommentRepository;
        this.taskStatusHistoryRepository = taskStatusHistoryRepository;
        this.taskOutcomeRepository = taskOutcomeRepository;
        this.taskCarryForwardRepository = taskCarryForwardRepository;
        this.structuredLogger = structuredLogger;
    }

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request, Long userId) {
        long start = System.currentTimeMillis();

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.getProjectId()));

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        User assignee = null;
        if (request.getAssignedToId() != null) {
            assignee = userRepository.findById(request.getAssignedToId()).orElse(null);
        }

        Meeting meeting = null;
        if (request.getRelatedMeetingId() != null) {
            meeting = meetingRepository.findById(request.getRelatedMeetingId()).orElse(null);
        }

        Decision decision = null;
        if (request.getRelatedDecisionId() != null) {
            decision = decisionRepository.findById(request.getRelatedDecisionId()).orElse(null);
        }

        Task task = new Task();
        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription());
        task.setProject(project);
        task.setAssignedTo(assignee);
        task.setCreatedBy(creator);
        task.setPriority(request.getPriority() != null ? request.getPriority().toUpperCase() : "MEDIUM");
        task.setStatus("TODO");
        task.setStartDate(request.getStartDate());
        task.setDueDate(request.getDueDate());
        task.setRelatedMeeting(meeting);
        task.setRelatedDecision(decision);

        Task saved = taskRepository.save(task);

        // Record initial status history
        taskStatusHistoryRepository.save(new TaskStatusHistory(saved, null, "TODO", creator, "Initial task creation"));

        // If assigned, record initial assignment
        if (assignee != null) {
            taskAssignmentRepository.save(new TaskAssignment(saved, null, assignee, creator, "Initial task assignment"));
        }

        structuredLogger.logDomainEvent("TASK_CREATE", userId, "SUCCESS", System.currentTimeMillis() - start, "Task created: " + saved.getTitle());
        return mapToTaskResponse(saved);
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request, Long userId) {
        long start = System.currentTimeMillis();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (request.getTitle() != null) task.setTitle(request.getTitle().trim());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getPriority() != null) task.setPriority(request.getPriority().toUpperCase());
        if (request.getStartDate() != null) task.setStartDate(request.getStartDate());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());

        // Check if assignee changed
        if (request.getAssignedToId() != null && (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(request.getAssignedToId()))) {
            User newAssignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getAssignedToId()));
            User previous = task.getAssignedTo();
            task.setAssignedTo(newAssignee);
            taskAssignmentRepository.save(new TaskAssignment(task, previous, newAssignee, user, "Reassigned via task update"));
        }

        // Check status change
        if (request.getStatus() != null && !request.getStatus().equalsIgnoreCase(task.getStatus())) {
            String oldStatus = task.getStatus();
            task.setStatus(request.getStatus().toUpperCase());
            if ("COMPLETED".equalsIgnoreCase(request.getStatus())) {
                task.setCompletedAt(LocalDateTime.now());
                if (request.getCompletionNotes() != null) task.setCompletionNotes(request.getCompletionNotes());
                if (request.getCompletionOutcome() != null) task.setCompletionOutcome(request.getCompletionOutcome());
            }
            taskStatusHistoryRepository.save(new TaskStatusHistory(task, oldStatus, task.getStatus(), user, "Status updated"));
        }

        Task updated = taskRepository.save(task);
        structuredLogger.logDomainEvent("TASK_UPDATE", userId, "SUCCESS", System.currentTimeMillis() - start, "Task updated: " + updated.getTitle());
        return mapToTaskResponse(updated);
    }

    @Transactional
    public TaskResponse updateStatus(Long taskId, TaskStatusUpdateRequest request, Long userId) {
        long start = System.currentTimeMillis();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        String oldStatus = task.getStatus();
        String newStatus = request.getStatus().toUpperCase();

        task.setStatus(newStatus);
        if ("COMPLETED".equalsIgnoreCase(newStatus)) {
            task.setCompletedAt(LocalDateTime.now());
        }

        taskStatusHistoryRepository.save(new TaskStatusHistory(task, oldStatus, newStatus, user, request.getRemarks()));
        Task updated = taskRepository.save(task);

        structuredLogger.logDomainEvent("TASK_STATUS_CHANGE", userId, "SUCCESS", System.currentTimeMillis() - start, "Task #" + taskId + " status changed to " + newStatus);
        return mapToTaskResponse(updated);
    }

    @Transactional
    public TaskResponse recordOutcome(Long taskId, TaskOutcomeRequest request, Long userId) {
        long start = System.currentTimeMillis();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        task.setStatus("COMPLETED");
        task.setCompletionOutcome(request.getOutcomeType());
        task.setCompletionNotes(request.getDetailedNotes());
        task.setCompletedAt(LocalDateTime.now());
        taskRepository.save(task);

        // Update or insert TaskOutcome record
        TaskOutcome outcome = taskOutcomeRepository.findByTaskId(taskId)
                .orElseGet(() -> new TaskOutcome(task, request.getOutcomeType(), request.getDetailedNotes(), user));
        outcome.setOutcomeType(request.getOutcomeType());
        outcome.setDetailedNotes(request.getDetailedNotes());
        outcome.setRecordedBy(user);
        taskOutcomeRepository.save(outcome);

        taskStatusHistoryRepository.save(new TaskStatusHistory(task, "IN_PROGRESS", "COMPLETED", user, "Outcome recorded: " + request.getOutcomeType()));

        structuredLogger.logDomainEvent("TASK_OUTCOME_RECORD", userId, "SUCCESS", System.currentTimeMillis() - start, "Outcome recorded for Task #" + taskId + ": " + request.getOutcomeType());
        return mapToTaskResponse(task);
    }

    @Transactional
    public TaskResponse carryForwardTask(Long taskId, CarryForwardRequest request, Long userId) {
        long start = System.currentTimeMillis();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Meeting toMeeting = null;
        if (request.getToMeetingId() != null) {
            toMeeting = meetingRepository.findById(request.getToMeetingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Target meeting not found: " + request.getToMeetingId()));
            task.setRelatedMeeting(toMeeting);
        }

        Meeting fromMeeting = task.getRelatedMeeting();

        TaskCarryForwardHistory carryRecord = new TaskCarryForwardHistory(
                task,
                fromMeeting,
                toMeeting,
                request.getReason(),
                user
        );
        carryRecord.setToHandoverId(request.getToHandoverId());
        taskCarryForwardRepository.save(carryRecord);

        // Keep status as TODO or IN_PROGRESS, preserving original ID and history
        if ("COMPLETED".equalsIgnoreCase(task.getStatus()) || "CANCELLED".equalsIgnoreCase(task.getStatus())) {
            task.setStatus("IN_PROGRESS");
        }
        Task updated = taskRepository.save(task);

        structuredLogger.logDomainEvent("TASK_CARRY_FORWARD", userId, "SUCCESS", System.currentTimeMillis() - start, "Carried forward Task #" + taskId + ". Reason: " + request.getReason());
        return mapToTaskResponse(updated);
    }

    @Transactional
    public TaskResponse reassignTask(Long taskId, Long newAssigneeId, String reason, Long userId) {
        long start = System.currentTimeMillis();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        User newAssignee = userRepository.findById(newAssigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + newAssigneeId));

        User assigner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        User previous = task.getAssignedTo();
        task.setAssignedTo(newAssignee);
        taskRepository.save(task);

        taskAssignmentRepository.save(new TaskAssignment(task, previous, newAssignee, assigner, reason));

        structuredLogger.logDomainEvent("TASK_REASSIGN", userId, "SUCCESS", System.currentTimeMillis() - start, "Reassigned Task #" + taskId + " to " + newAssignee.getFullName());
        return mapToTaskResponse(task);
    }

    @Transactional
    public void addComment(Long taskId, String commentText, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        taskCommentRepository.save(new TaskComment(task, author, commentText.trim()));
    }

    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return mapToTaskResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks(String status, String priority, Long projectId, Long assigneeId) {
        List<Task> tasks;
        if (projectId != null) {
            tasks = taskRepository.findByProjectId(projectId);
        } else if (assigneeId != null) {
            tasks = taskRepository.findByAssignedToId(assigneeId);
        } else {
            tasks = taskRepository.findAll();
        }

        return tasks.stream()
                .filter(t -> status == null || status.equalsIgnoreCase(t.getStatus()))
                .filter(t -> priority == null || priority.equalsIgnoreCase(t.getPriority()))
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    public TaskResponse mapToTaskResponse(Task t) {
        TaskResponse resp = new TaskResponse();
        resp.setId(t.getId());
        resp.setTitle(t.getTitle());
        resp.setDescription(t.getDescription());
        resp.setProjectId(t.getProject() != null ? t.getProject().getId() : null);
        resp.setProjectName(t.getProject() != null ? t.getProject().getName() : null);
        resp.setProjectCode(t.getProject() != null ? t.getProject().getCode() : null);
        resp.setAssignedToId(t.getAssignedTo() != null ? t.getAssignedTo().getId() : null);
        resp.setAssignedToName(t.getAssignedTo() != null ? t.getAssignedTo().getFullName() : "Unassigned");
        resp.setAssignedToEmail(t.getAssignedTo() != null ? t.getAssignedTo().getEmail() : null);
        resp.setAssignedToAvatar(t.getAssignedTo() != null ? t.getAssignedTo().getAvatarUrl() : null);
        resp.setCreatedById(t.getCreatedBy() != null ? t.getCreatedBy().getId() : null);
        resp.setCreatedByName(t.getCreatedBy() != null ? t.getCreatedBy().getFullName() : null);
        resp.setPriority(t.getPriority());
        resp.setStatus(t.getStatus());
        resp.setStartDate(t.getStartDate());
        resp.setDueDate(t.getDueDate());
        resp.setRelatedMeetingId(t.getRelatedMeeting() != null ? t.getRelatedMeeting().getId() : null);
        resp.setRelatedMeetingTitle(t.getRelatedMeeting() != null ? t.getRelatedMeeting().getTitle() : null);
        resp.setRelatedDecisionId(t.getRelatedDecision() != null ? t.getRelatedDecision().getId() : null);
        resp.setRelatedDecisionTitle(t.getRelatedDecision() != null ? t.getRelatedDecision().getTitle() : null);
        resp.setCompletionNotes(t.getCompletionNotes());
        resp.setCompletionOutcome(t.getCompletionOutcome());
        resp.setCompletedAt(t.getCompletedAt());
        resp.setCreatedAt(t.getCreatedAt());
        resp.setUpdatedAt(t.getUpdatedAt());

        // Comments
        List<TaskComment> comments = taskCommentRepository.findByTaskIdOrderByCreatedAtAsc(t.getId());
        resp.setComments(comments.stream().map(c -> new TaskResponse.CommentDto(
                c.getId(),
                c.getAuthor().getId(),
                c.getAuthor().getFullName(),
                c.getAuthor().getAvatarUrl(),
                c.getCommentText(),
                c.getCreatedAt()
        )).collect(Collectors.toList()));

        // Status History
        List<TaskStatusHistory> history = taskStatusHistoryRepository.findByTaskIdOrderByCreatedAtAsc(t.getId());
        resp.setStatusHistory(history.stream().map(h -> new TaskResponse.StatusHistoryDto(
                h.getId(),
                h.getOldStatus(),
                h.getNewStatus(),
                h.getChangedBy() != null ? h.getChangedBy().getFullName() : "System",
                h.getRemarks(),
                h.getCreatedAt()
        )).collect(Collectors.toList()));

        // Carry Forward History
        List<TaskCarryForwardHistory> carry = taskCarryForwardRepository.findByTaskIdOrderByCarriedAtDesc(t.getId());
        resp.setCarryForwardHistory(carry.stream().map(cf -> new TaskResponse.CarryForwardDto(
                cf.getId(),
                cf.getFromMeeting() != null ? cf.getFromMeeting().getId() : null,
                cf.getFromMeeting() != null ? cf.getFromMeeting().getTitle() : null,
                cf.getToMeeting() != null ? cf.getToMeeting().getId() : null,
                cf.getToMeeting() != null ? cf.getToMeeting().getTitle() : null,
                cf.getReason(),
                cf.getCarriedBy() != null ? cf.getCarriedBy().getFullName() : null,
                cf.getCarriedAt()
        )).collect(Collectors.toList()));

        return resp;
    }
}
