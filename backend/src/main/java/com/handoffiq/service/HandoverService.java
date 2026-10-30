package com.handoffiq.service;

import com.handoffiq.dto.*;
import com.handoffiq.entity.*;
import com.handoffiq.exception.BadRequestException;
import com.handoffiq.exception.HandoverIncompleteException;
import com.handoffiq.exception.ResourceNotFoundException;
import com.handoffiq.logging.StructuredLogger;
import com.handoffiq.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HandoverService {

    private final HandoverRepository handoverRepository;
    private final HandoverTaskRepository handoverTaskRepository;
    private final HandoverDocumentRepository handoverDocumentRepository;
    private final HandoverAcknowledgementRepository acknowledgementRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final KnowledgeDocumentRepository documentRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final ReadinessScorerService readinessScorerService;
    private final StructuredLogger structuredLogger;

    public HandoverService(
            HandoverRepository handoverRepository,
            HandoverTaskRepository handoverTaskRepository,
            HandoverDocumentRepository handoverDocumentRepository,
            HandoverAcknowledgementRepository acknowledgementRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            TaskRepository taskRepository,
            KnowledgeDocumentRepository documentRepository,
            TaskAssignmentRepository taskAssignmentRepository,
            ReadinessScorerService readinessScorerService,
            StructuredLogger structuredLogger) {
        this.handoverRepository = handoverRepository;
        this.handoverTaskRepository = handoverTaskRepository;
        this.handoverDocumentRepository = handoverDocumentRepository;
        this.acknowledgementRepository = acknowledgementRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.documentRepository = documentRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.readinessScorerService = readinessScorerService;
        this.structuredLogger = structuredLogger;
    }

    @Transactional
    public HandoverResponse createHandover(CreateHandoverRequest request, Long initiatorId) {
        long start = System.currentTimeMillis();

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.getProjectId()));

        User currentEmployee = userRepository.findById(request.getCurrentEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Current employee not found: " + request.getCurrentEmployeeId()));

        User receivingEmployee = userRepository.findById(request.getReceivingEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiving employee not found: " + request.getReceivingEmployeeId()));

        User initiator = userRepository.findById(initiatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Initiator user not found: " + initiatorId));

        Handover handover = new Handover();
        handover.setTitle(request.getTitle().trim());
        handover.setProject(project);
        handover.setCurrentEmployee(currentEmployee);
        handover.setReceivingEmployee(receivingEmployee);
        handover.setInitiatedBy(initiator);
        handover.setReason(request.getReason());
        handover.setHandoverDate(request.getHandoverDate());
        handover.setSpecialInstructions(request.getSpecialInstructions());
        handover.setStatus("AWAITING_REVIEW");

        Handover initialSaved = handoverRepository.save(handover);
        final Handover saved = initialSaved;

        // Link tasks
        if (request.getTaskIds() != null) {
            for (Long tId : request.getTaskIds()) {
                taskRepository.findById(tId).ifPresent(task -> {
                    HandoverTask ht = new HandoverTask(
                            saved,
                            task,
                            task.getAssignedTo(),
                            receivingEmployee,
                            task.getStatus(),
                            "Assigned for transfer in handover #" + saved.getId()
                    );
                    handoverTaskRepository.save(ht);
                });
            }
        }

        // Link documents
        if (request.getDocumentIds() != null) {
            for (Long dId : request.getDocumentIds()) {
                HandoverDocument hd = new HandoverDocument(saved, dId, "Linked knowledge document");
                handoverDocumentRepository.save(hd);
            }
        }

        // Calculate initial Readiness Score
        ReadinessScoreResponse score = readinessScorerService.calculateScore(saved);
        saved.setReadinessScore(score.getOverallScore());
        saved.setReadinessChecklist(readinessScorerService.serializeChecklist(score.getChecklist()));
        Handover finalSaved = handoverRepository.save(saved);

        structuredLogger.logDomainEvent("HANDOVER_INITIATE", initiatorId, "SUCCESS", System.currentTimeMillis() - start, "Handover initiated: " + finalSaved.getTitle());
        return mapToHandoverResponse(finalSaved, score);
    }

    @Transactional(readOnly = true)
    public List<HandoverResponse> getAllHandovers() {
        return getAllHandovers(null, null, null);
    }

    @Transactional(readOnly = true)
    public List<HandoverResponse> getAllHandovers(String status, Long projectId, Long employeeId) {
        List<Handover> handovers;
        if (projectId != null) {
            handovers = handoverRepository.findByProjectId(projectId);
        } else if (employeeId != null) {
            handovers = handoverRepository.findByParticipantUserId(employeeId);
        } else {
            handovers = handoverRepository.findAllByOrderByCreatedAtDesc();
        }

        return handovers.stream()
                .filter(h -> status == null || status.equalsIgnoreCase(h.getStatus()))
                .map(h -> mapToHandoverResponse(h, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HandoverResponse getHandoverById(Long id) {
        Handover handover = handoverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Handover not found with id: " + id));
        ReadinessScoreResponse score = readinessScorerService.calculateScore(handover);
        return mapToHandoverResponse(handover, score);
    }

    @Transactional(readOnly = true)
    public ReadinessScoreResponse getReadinessScore(Long id) {
        Handover handover = handoverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Handover not found with id: " + id));
        return readinessScorerService.calculateScore(handover);
    }

    @Transactional
    public HandoverResponse acknowledgeHandover(Long id, HandoverAcknowledgeRequest request, Long userId) {
        long start = System.currentTimeMillis();

        Handover handover = handoverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Handover not found with id: " + id));

        User acknowledger = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Save acknowledgement record
        acknowledgementRepository.save(new HandoverAcknowledgement(handover, acknowledger, request.getComments()));

        handover.setAcknowledgedAt(LocalDateTime.now());
        handover.setAcknowledgementNotes(request.getComments());
        handover.setStatus("ACCEPTED");

        // Recalculate Readiness Score
        ReadinessScoreResponse score = readinessScorerService.calculateScore(handover);
        handover.setReadinessScore(score.getOverallScore());
        handover.setReadinessChecklist(readinessScorerService.serializeChecklist(score.getChecklist()));
        Handover saved = handoverRepository.save(handover);

        structuredLogger.logDomainEvent("HANDOVER_ACKNOWLEDGE", userId, "SUCCESS", System.currentTimeMillis() - start, "Handover acknowledged: " + saved.getTitle());
        return mapToHandoverResponse(saved, score);
    }

    @Transactional
    public HandoverResponse completeHandover(Long id, Long userId) {
        long start = System.currentTimeMillis();

        Handover handover = handoverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Handover not found with id: " + id));

        User manager = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check readiness score & mandatory criteria
        ReadinessScoreResponse score = readinessScorerService.calculateScore(handover);
        if (!score.isReadyForCompletion()) {
            throw new HandoverIncompleteException(
                    "Handover cannot be completed. Readiness score is " + score.getOverallScore() + "/100 and mandatory conditions are incomplete.",
                    score.getOverallScore(),
                    score.getMissingRequirements()
            );
        }

        // Execute task reassignments across all handover tasks
        List<HandoverTask> hTasks = handoverTaskRepository.findByHandoverId(id);
        for (HandoverTask ht : hTasks) {
            Task t = ht.getTask();
            if (t != null && ht.getNewOwner() != null && !"COMPLETED".equalsIgnoreCase(t.getStatus())) {
                User oldOwner = t.getAssignedTo();
                t.setAssignedTo(ht.getNewOwner());
                taskRepository.save(t);

                taskAssignmentRepository.save(new TaskAssignment(
                        t,
                        oldOwner,
                        ht.getNewOwner(),
                        manager,
                        "Transferred via completed Handover #" + handover.getId()
                ));
            }
        }

        handover.setStatus("COMPLETED");
        handover.setCompletedAt(LocalDateTime.now());
        handover.setReadinessScore(score.getOverallScore());
        handover.setReadinessChecklist(readinessScorerService.serializeChecklist(score.getChecklist()));
        Handover saved = handoverRepository.save(handover);

        structuredLogger.logDomainEvent("HANDOVER_COMPLETE", userId, "SUCCESS", System.currentTimeMillis() - start, "Handover marked completed: " + saved.getTitle());
        return mapToHandoverResponse(saved, score);
    }

    public HandoverResponse mapToHandoverResponse(Handover h, ReadinessScoreResponse scoreReport) {
        HandoverResponse resp = new HandoverResponse();
        resp.setId(h.getId());
        resp.setTitle(h.getTitle());
        resp.setProjectId(h.getProject() != null ? h.getProject().getId() : null);
        resp.setProjectName(h.getProject() != null ? h.getProject().getName() : null);
        resp.setProjectCode(h.getProject() != null ? h.getProject().getCode() : null);
        resp.setCurrentEmployeeId(h.getCurrentEmployee() != null ? h.getCurrentEmployee().getId() : null);
        resp.setCurrentEmployeeName(h.getCurrentEmployee() != null ? h.getCurrentEmployee().getFullName() : null);
        resp.setCurrentEmployeeEmail(h.getCurrentEmployee() != null ? h.getCurrentEmployee().getEmail() : null);
        resp.setCurrentEmployeeAvatar(h.getCurrentEmployee() != null ? h.getCurrentEmployee().getAvatarUrl() : null);
        resp.setReceivingEmployeeId(h.getReceivingEmployee() != null ? h.getReceivingEmployee().getId() : null);
        resp.setReceivingEmployeeName(h.getReceivingEmployee() != null ? h.getReceivingEmployee().getFullName() : null);
        resp.setReceivingEmployeeEmail(h.getReceivingEmployee() != null ? h.getReceivingEmployee().getEmail() : null);
        resp.setReceivingEmployeeAvatar(h.getReceivingEmployee() != null ? h.getReceivingEmployee().getAvatarUrl() : null);
        resp.setInitiatedById(h.getInitiatedBy() != null ? h.getInitiatedBy().getId() : null);
        resp.setInitiatedByName(h.getInitiatedBy() != null ? h.getInitiatedBy().getFullName() : null);
        resp.setReason(h.getReason());
        resp.setHandoverDate(h.getHandoverDate());
        resp.setSpecialInstructions(h.getSpecialInstructions());
        resp.setStatus(h.getStatus());
        resp.setReadinessScore(h.getReadinessScore());
        resp.setReadinessChecklist(h.getReadinessChecklist());
        resp.setAiGeneratedSummary(h.getAiGeneratedSummary());
        resp.setWorkCompletedNotes(h.getWorkCompletedNotes());
        resp.setWorkInProgressNotes(h.getWorkInProgressNotes());
        resp.setBlockersNotes(h.getBlockersNotes());
        resp.setRecommendedStepsNotes(h.getRecommendedStepsNotes());
        resp.setAcknowledgedAt(h.getAcknowledgedAt());
        resp.setAcknowledgementNotes(h.getAcknowledgementNotes());
        resp.setCompletedAt(h.getCompletedAt());
        resp.setCreatedAt(h.getCreatedAt());
        resp.setUpdatedAt(h.getUpdatedAt());

        // Tasks
        List<HandoverTask> hTasks = handoverTaskRepository.findByHandoverId(h.getId());
        resp.setTasks(hTasks.stream().map(ht -> {
            HandoverResponse.HandoverTaskDto dto = new HandoverResponse.HandoverTaskDto();
            dto.setId(ht.getId());
            if (ht.getTask() != null) {
                dto.setTaskId(ht.getTask().getId());
                dto.setTaskTitle(ht.getTask().getTitle());
                dto.setTaskPriority(ht.getTask().getPriority());
                dto.setTaskStatus(ht.getTask().getStatus());
                dto.setDueDate(ht.getTask().getDueDate());
            }
            if (ht.getPreviousOwner() != null) {
                dto.setPreviousOwnerId(ht.getPreviousOwner().getId());
                dto.setPreviousOwnerName(ht.getPreviousOwner().getFullName());
            }
            if (ht.getNewOwner() != null) {
                dto.setNewOwnerId(ht.getNewOwner().getId());
                dto.setNewOwnerName(ht.getNewOwner().getFullName());
            }
            dto.setNotes(ht.getNotes());
            return dto;
        }).collect(Collectors.toList()));

        // Documents
        List<HandoverDocument> hDocs = handoverDocumentRepository.findByHandoverId(h.getId());
        resp.setDocuments(hDocs.stream().map(hd -> {
            HandoverResponse.HandoverDocumentDto dto = new HandoverResponse.HandoverDocumentDto();
            dto.setDocumentId(hd.getDocumentId());
            dto.setNotes(hd.getNotes());
            documentRepository.findById(hd.getDocumentId()).ifPresent(doc -> {
                dto.setDocumentTitle(doc.getTitle());
                dto.setFileName(doc.getFileName());
                dto.setFileType(doc.getFileType());
                dto.setCategory(doc.getCategory());
            });
            return dto;
        }).collect(Collectors.toList()));

        if (scoreReport != null) {
            resp.setReadinessReport(scoreReport);
        }

        return resp;
    }
}
