package com.handoffiq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.handoffiq.dto.HandoverResponse;
import com.handoffiq.dto.ReadinessChecklistItem;
import com.handoffiq.dto.ReadinessScoreResponse;
import com.handoffiq.entity.*;
import com.handoffiq.exception.HandoverIncompleteException;
import com.handoffiq.logging.StructuredLogger;
import com.handoffiq.repository.*;
import com.handoffiq.service.HandoverService;
import com.handoffiq.service.ReadinessScorerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class HandoverWorkflowTest {

    private HandoverService handoverService;
    private Handover testHandover;
    private User manager;
    private User currentEmp;
    private User receivingEmp;
    private ReadinessScoreResponse simulatedScore;
    private List<HandoverTask> simulatedTasks = new ArrayList<>();

    @BeforeEach
    void setUp() {
        manager = new User();
        manager.setId(2L);
        manager.setFullName("Sarah Jenkins");

        currentEmp = new User();
        currentEmp.setId(4L);
        currentEmp.setFullName("Rahul Sharma");

        receivingEmp = new User();
        receivingEmp.setId(5L);
        receivingEmp.setFullName("Priya Patel");

        testHandover = new Handover();
        testHandover.setId(1L);
        testHandover.setTitle("Nexus Handover");
        testHandover.setCurrentEmployee(currentEmp);
        testHandover.setReceivingEmployee(receivingEmp);
        testHandover.setStatus("AWAITING_REVIEW");

        simulatedTasks.clear();

        HandoverRepository handoverRepo = (HandoverRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{HandoverRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) return Optional.of(testHandover);
                    if ("save".equals(method.getName())) return testHandover;
                    return null;
                }
        );

        HandoverTaskRepository htRepo = (HandoverTaskRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{HandoverTaskRepository.class},
                (proxy, method, args) -> {
                    if ("findByHandoverId".equals(method.getName())) return simulatedTasks;
                    return List.of();
                }
        );

        HandoverDocumentRepository hdRepo = (HandoverDocumentRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{HandoverDocumentRepository.class},
                (proxy, method, args) -> List.of()
        );

        HandoverAcknowledgementRepository haRepo = (HandoverAcknowledgementRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{HandoverAcknowledgementRepository.class},
                (proxy, method, args) -> List.of()
        );

        ProjectRepository projRepo = (ProjectRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{ProjectRepository.class},
                (proxy, method, args) -> null
        );

        UserRepository userRepo = (UserRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{UserRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) return Optional.of(manager);
                    return null;
                }
        );

        TaskRepository taskRepo = (TaskRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{TaskRepository.class},
                (proxy, method, args) -> args[0]
        );

        KnowledgeDocumentRepository kdRepo = (KnowledgeDocumentRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{KnowledgeDocumentRepository.class},
                (proxy, method, args) -> null
        );

        TaskAssignmentRepository taRepo = (TaskAssignmentRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{TaskAssignmentRepository.class},
                (proxy, method, args) -> null
        );

        ReadinessScorerService scorer = new ReadinessScorerService(htRepo, hdRepo, haRepo, null, new ObjectMapper()) {
            @Override
            public ReadinessScoreResponse calculateScore(Handover handover) {
                return simulatedScore;
            }

            @Override
            public String serializeChecklist(List<ReadinessChecklistItem> checklist) {
                return "[]";
            }
        };

        StructuredLogger logger = new StructuredLogger();

        handoverService = new HandoverService(
                handoverRepo, htRepo, hdRepo, haRepo, projRepo,
                userRepo, taskRepo, kdRepo, taRepo, scorer, logger
        );
    }

    @Test
    @DisplayName("Should prevent completion and throw HandoverIncompleteException if readiness score is not ready")
    void testPreventCompletionOnLowReadiness() {
        simulatedScore = new ReadinessScoreResponse(
                1L, 45, "CRITICAL", false, List.of(), List.of("Tasks unassigned"), List.of("Assign tasks")
        );

        assertThrows(HandoverIncompleteException.class, () -> {
            handoverService.completeHandover(1L, 2L);
        });
    }

    @Test
    @DisplayName("Should successfully finalize handover and reassign tasks when readiness score is satisfied")
    void testCompleteHandoverSuccess() {
        simulatedScore = new ReadinessScoreResponse(
                1L, 95, "EXCELLENT", true, List.of(), List.of(), List.of()
        );

        Task inFlightTask = new Task();
        inFlightTask.setId(10L);
        inFlightTask.setTitle("Auth Controller Refactor");
        inFlightTask.setStatus("IN_PROGRESS");
        inFlightTask.setAssignedTo(currentEmp);

        HandoverTask ht = new HandoverTask(testHandover, inFlightTask, currentEmp, receivingEmp, "IN_PROGRESS", "Transfer");
        simulatedTasks.add(ht);

        HandoverResponse response = handoverService.completeHandover(1L, 2L);

        assertNotNull(response);
        assertEquals("COMPLETED", testHandover.getStatus());
        assertEquals(receivingEmp, inFlightTask.getAssignedTo()); // Task reassigned to Priya!
    }
}
