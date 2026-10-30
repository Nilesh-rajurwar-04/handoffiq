package com.handoffiq.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handoffiq.dto.ReadinessChecklistItem;
import com.handoffiq.dto.ReadinessScoreResponse;
import com.handoffiq.entity.*;
import com.handoffiq.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReadinessScorerService {

    private static final Logger log = LoggerFactory.getLogger(ReadinessScorerService.class);
    private final HandoverTaskRepository handoverTaskRepository;
    private final HandoverDocumentRepository handoverDocumentRepository;
    private final HandoverAcknowledgementRepository acknowledgementRepository;
    private final DecisionRepository decisionRepository;
    private final ObjectMapper objectMapper;

    public ReadinessScorerService(
            HandoverTaskRepository handoverTaskRepository,
            HandoverDocumentRepository handoverDocumentRepository,
            HandoverAcknowledgementRepository acknowledgementRepository,
            DecisionRepository decisionRepository,
            ObjectMapper objectMapper) {
        this.handoverTaskRepository = handoverTaskRepository;
        this.handoverDocumentRepository = handoverDocumentRepository;
        this.acknowledgementRepository = acknowledgementRepository;
        this.decisionRepository = decisionRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public ReadinessScoreResponse calculateScore(Handover handover) {
        log.info("Calculating Handover Readiness Score for Handover #{} ('{}')", handover.getId(), handover.getTitle());

        List<HandoverTask> handoverTasks = handoverTaskRepository.findByHandoverId(handover.getId());
        List<HandoverDocument> handoverDocs = handoverDocumentRepository.findByHandoverId(handover.getId());
        List<HandoverAcknowledgement> acks = acknowledgementRepository.findByHandoverId(handover.getId());
        Long projectId = handover.getProject() != null ? handover.getProject().getId() : null;
        List<Decision> decisions = projectId != null ? decisionRepository.findByProjectIdOrderByDecidedAtDesc(projectId) : List.of();

        List<ReadinessChecklistItem> checklist = new ArrayList<>();
        List<String> missingRequirements = new ArrayList<>();
        List<String> recommendedActions = new ArrayList<>();
        int totalScore = 0;

        // 1. Pending Task Reassignment (Weight: 25 points) - MANDATORY
        int pendingTasksCount = 0;
        int assignedTasksCount = 0;
        for (HandoverTask ht : handoverTasks) {
            Task t = ht.getTask();
            if (t != null && !"COMPLETED".equalsIgnoreCase(t.getStatus()) && !"CANCELLED".equalsIgnoreCase(t.getStatus())) {
                pendingTasksCount++;
                if (ht.getNewOwner() != null || t.getAssignedTo() != null) {
                    assignedTasksCount++;
                }
            }
        }
        boolean taskReassignedPassed = (pendingTasksCount == 0) || (assignedTasksCount == pendingTasksCount);
        int taskScore = pendingTasksCount == 0 ? 25 : (int) Math.round(((double) assignedTasksCount / pendingTasksCount) * 25);
        totalScore += taskScore;
        checklist.add(new ReadinessChecklistItem(
                "Task Ownership Continuity",
                "Every pending and in-flight task must have an assigned receiving owner.",
                25, 25, taskScore, taskReassignedPassed, true,
                assignedTasksCount + " of " + pendingTasksCount + " pending tasks have designated owners.",
                taskReassignedPassed ? "Verified" : "Assign designated new owners to all unassigned pending tasks before finalizing."
        ));
        if (!taskReassignedPassed) {
            missingRequirements.add((pendingTasksCount - assignedTasksCount) + " pending task(s) lack a designated receiving owner.");
            recommendedActions.add("Assign new owners for in-flight tasks in the Handover Tasks tab.");
        }

        // 2. Task Description Completeness (Weight: 15 points)
        int validDescCount = 0;
        for (HandoverTask ht : handoverTasks) {
            Task t = ht.getTask();
            if (t != null && t.getDescription() != null && t.getDescription().trim().length() >= 10) {
                validDescCount++;
            }
        }
        int totalTasks = handoverTasks.size();
        boolean descPassed = totalTasks == 0 || (validDescCount == totalTasks);
        int descScore = totalTasks == 0 ? 15 : (int) Math.round(((double) validDescCount / totalTasks) * 15);
        totalScore += descScore;
        checklist.add(new ReadinessChecklistItem(
                "Task Specification Quality",
                "Task descriptions must provide sufficient context for seamless handover.",
                15, 15, descScore, descPassed, false,
                validDescCount + " of " + totalTasks + " tasks have detailed specifications.",
                descPassed ? "Verified" : "Add context, technical specifications, and expected outcomes to brief task descriptions."
        ));
        if (!descPassed) {
            missingRequirements.add((totalTasks - validDescCount) + " task(s) have incomplete or missing descriptions.");
            recommendedActions.add("Expand task descriptions with clear acceptance criteria.");
        }

        // 3. Documented Deadlines (Weight: 15 points)
        int deadlineCount = 0;
        for (HandoverTask ht : handoverTasks) {
            Task t = ht.getTask();
            if (t != null && t.getDueDate() != null) {
                deadlineCount++;
            }
        }
        boolean deadlinePassed = totalTasks == 0 || (deadlineCount == totalTasks);
        int deadlineScore = totalTasks == 0 ? 15 : (int) Math.round(((double) deadlineCount / totalTasks) * 15);
        totalScore += deadlineScore;
        checklist.add(new ReadinessChecklistItem(
                "Delivery Timelines & Deadlines",
                "Target due dates must be documented for scheduling and SLA tracking.",
                15, 15, deadlineScore, deadlinePassed, false,
                deadlineCount + " of " + totalTasks + " tasks have defined due dates.",
                deadlinePassed ? "Verified" : "Set target due dates for all open handover tasks."
        ));
        if (!deadlinePassed) {
            missingRequirements.add((totalTasks - deadlineCount) + " task(s) do not have target due dates specified.");
            recommendedActions.add("Review project roadmap and set clear deadlines for unscheduled tasks.");
        }

        // 4. Architectural & Product Decisions Included (Weight: 10 points)
        boolean decisionsPassed = !decisions.isEmpty();
        int decisionScore = decisionsPassed ? 10 : 0;
        totalScore += decisionScore;
        checklist.add(new ReadinessChecklistItem(
                "Decision Traceability",
                "Important project and architectural decisions must be accessible for context.",
                10, 10, decisionScore, decisionsPassed, false,
                decisions.size() + " documented decisions found in project knowledge log.",
                decisionsPassed ? "Verified" : "Log key architectural decisions in the Decisions module to prevent lost rationale."
        ));
        if (!decisionsPassed) {
            missingRequirements.add("No architectural or project decisions have been linked or logged for this project.");
            recommendedActions.add("Document key architectural choices and design rationale in the project log.");
        }

        // 5. Relevant Supporting Documents Attached (Weight: 10 points)
        boolean docsPassed = !handoverDocs.isEmpty();
        int docScore = docsPassed ? 10 : 0;
        totalScore += docScore;
        checklist.add(new ReadinessChecklistItem(
                "Knowledge & Runbook Assets",
                "Technical specifications, SOPs, or runbooks must be attached to the handover.",
                10, 10, docScore, docsPassed, false,
                handoverDocs.size() + " supporting knowledge document(s) attached.",
                docsPassed ? "Verified" : "Attach relevant architecture RFCs, runbooks, or handover SOPs."
        ));
        if (!docsPassed) {
            missingRequirements.add("No supporting documentation or operational runbooks attached to this handover package.");
            recommendedActions.add("Attach at least one technical runbook or SOP from the Knowledge Base.");
        }

        // 6. Blockers & Known Issues Documented (Weight: 10 points)
        long blockedTasks = handoverTasks.stream()
                .filter(ht -> ht.getTask() != null && "BLOCKED".equalsIgnoreCase(ht.getTask().getStatus()))
                .count();
        boolean blockersDocumented = (blockedTasks == 0) || (handover.getBlockersNotes() != null && !handover.getBlockersNotes().trim().isEmpty());
        int blockerScore = blockersDocumented ? 10 : 0;
        totalScore += blockerScore;
        checklist.add(new ReadinessChecklistItem(
                "Risk & Blocker Mitigation",
                "All known impediments or blocked tasks must have explicit notes or mitigation steps.",
                10, 10, blockerScore, blockersDocumented, false,
                blockedTasks > 0 ? (blockedTasks + " blocked task(s) flagged with documentation.") : "No blocked tasks currently active.",
                blockersDocumented ? "Verified" : "Provide detailed blocker notes and escalation paths for blocked tasks."
        ));
        if (!blockersDocumented) {
            missingRequirements.add(blockedTasks + " task(s) are marked BLOCKED without documented blocker notes.");
            recommendedActions.add("Fill in the Blockers & Known Issues section with resolution plans.");
        }

        // 7. Receiving Employee Acknowledgement (Weight: 15 points) - MANDATORY
        boolean ackPassed = !acks.isEmpty() || handover.getAcknowledgedAt() != null;
        int ackScore = ackPassed ? 15 : 0;
        totalScore += ackScore;
        checklist.add(new ReadinessChecklistItem(
                "Receiver Formal Sign-Off",
                "Receiving employee must review the package and submit formal acknowledgement.",
                15, 15, ackScore, ackPassed, true,
                ackPassed ? "Acknowledged by " + (handover.getReceivingEmployee() != null ? handover.getReceivingEmployee().getFullName() : "receiver") + "." : "Awaiting recipient review and sign-off.",
                ackPassed ? "Verified" : "Receiving employee must click 'Acknowledge Handover' after inspecting responsibilities."
        ));
        if (!ackPassed) {
            missingRequirements.add("Receiving employee (" + (handover.getReceivingEmployee() != null ? handover.getReceivingEmployee().getFullName() : "Receiver") + ") has not yet submitted formal acknowledgement.");
            recommendedActions.add("Request the receiving employee to review documents and submit acknowledgement.");
        }

        // Final Score Categorization & Readiness Assessment
        String category = totalScore >= 90 ? "EXCELLENT" :
                          totalScore >= 75 ? "GOOD" :
                          totalScore >= 50 ? "NEEDS_ATTENTION" : "CRITICAL";

        // Must satisfy all mandatory requirements (Task reassignment and Receiver acknowledgement) AND score at least 80% to be ready for completion
        boolean isReadyForCompletion = taskReassignedPassed && ackPassed && totalScore >= 80;

        return new ReadinessScoreResponse(
                handover.getId(),
                totalScore,
                category,
                isReadyForCompletion,
                checklist,
                missingRequirements,
                recommendedActions
        );
    }

    public String serializeChecklist(List<ReadinessChecklistItem> checklist) {
        try {
            return objectMapper.writeValueAsString(checklist);
        } catch (Exception ex) {
            log.error("Failed to serialize readiness checklist: {}", ex.getMessage());
            return "[]";
        }
    }

    public List<ReadinessChecklistItem> deserializeChecklist(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<ReadinessChecklistItem>>() {});
        } catch (Exception ex) {
            log.error("Failed to deserialize readiness checklist: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }
}
