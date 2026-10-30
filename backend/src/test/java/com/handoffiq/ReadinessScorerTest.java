package com.handoffiq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.handoffiq.dto.ReadinessScoreResponse;
import com.handoffiq.entity.*;
import com.handoffiq.repository.*;
import com.handoffiq.service.ReadinessScorerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReadinessScorerTest {

    private ReadinessScorerService readinessScorerService;
    private List<HandoverTask> tasksToReturn = new ArrayList<>();
    private List<HandoverDocument> docsToReturn = new ArrayList<>();
    private List<HandoverAcknowledgement> acksToReturn = new ArrayList<>();
    private List<Decision> decisionsToReturn = new ArrayList<>();

    @BeforeEach
    void setUp() {
        tasksToReturn.clear();
        docsToReturn.clear();
        acksToReturn.clear();
        decisionsToReturn.clear();

        HandoverTaskRepository taskRepo = (HandoverTaskRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{HandoverTaskRepository.class},
                (proxy, method, args) -> {
                    if ("findByHandoverId".equals(method.getName())) return tasksToReturn;
                    return null;
                }
        );

        HandoverDocumentRepository docRepo = (HandoverDocumentRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{HandoverDocumentRepository.class},
                (proxy, method, args) -> {
                    if ("findByHandoverId".equals(method.getName())) return docsToReturn;
                    return null;
                }
        );

        HandoverAcknowledgementRepository ackRepo = (HandoverAcknowledgementRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{HandoverAcknowledgementRepository.class},
                (proxy, method, args) -> {
                    if ("findByHandoverId".equals(method.getName())) return acksToReturn;
                    return null;
                }
        );

        DecisionRepository decRepo = (DecisionRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{DecisionRepository.class},
                (proxy, method, args) -> {
                    if ("findByProjectIdOrderByDecidedAtDesc".equals(method.getName())) return decisionsToReturn;
                    return null;
                }
        );

        readinessScorerService = new ReadinessScorerService(taskRepo, docRepo, ackRepo, decRepo, new ObjectMapper());
    }

    @Test
    @DisplayName("Should calculate 100/100 score when all 7 criteria are fully satisfied")
    void testPerfectReadinessScore() {
        Handover handover = new Handover();
        handover.setId(10L);
        handover.setTitle("Complete Lead Handover");
        Project project = new Project();
        project.setId(1L);
        project.setName("Project Nexus");
        handover.setProject(project);

        User current = new User();
        current.setId(4L);
        current.setFullName("Rahul Sharma");
        User receiver = new User();
        receiver.setId(5L);
        receiver.setFullName("Priya Patel");
        handover.setCurrentEmployee(current);
        handover.setReceivingEmployee(receiver);
        handover.setBlockersNotes("All known dependencies cleared.");

        Task task = new Task();
        task.setId(101L);
        task.setTitle("API Auth Refactor");
        task.setDescription("Comprehensive controller refactor to use JWT bearer tokens.");
        task.setDueDate(LocalDate.now().plusDays(5));
        task.setStatus("IN_PROGRESS");
        task.setAssignedTo(receiver);

        HandoverTask ht = new HandoverTask(handover, task, current, receiver, "IN_PROGRESS", "Assigned");
        HandoverDocument hd = new HandoverDocument(handover, 1L, "Runbook");
        HandoverAcknowledgement ack = new HandoverAcknowledgement(handover, receiver, "Ready to take over");
        Decision decision = new Decision();
        decision.setId(1L);
        decision.setTitle("JWT OAuth2 Standard");
        decision.setRationale("Approved enterprise auth protocol");

        tasksToReturn.add(ht);
        docsToReturn.add(hd);
        acksToReturn.add(ack);
        decisionsToReturn.add(decision);

        ReadinessScoreResponse response = readinessScorerService.calculateScore(handover);

        assertNotNull(response);
        assertEquals(100, response.getOverallScore());
        assertEquals("EXCELLENT", response.getStatusCategory());
        assertTrue(response.isReadyForCompletion());
        assertTrue(response.getMissingRequirements().isEmpty());
    }

    @Test
    @DisplayName("Should calculate low score and block completion when mandatory criteria are missing")
    void testIncompleteReadinessScore() {
        Handover handover = new Handover();
        handover.setId(20L);
        handover.setTitle("Draft Handover");
        Project project = new Project();
        project.setId(2L);
        handover.setProject(project);

        User current = new User();
        current.setId(4L);
        User receiver = new User();
        receiver.setId(5L);
        handover.setCurrentEmployee(current);
        handover.setReceivingEmployee(receiver);

        Task task = new Task();
        task.setId(201L);
        task.setTitle("Unassigned task");
        task.setDescription("");
        task.setStatus("TODO");
        task.setAssignedTo(null);

        HandoverTask ht = new HandoverTask(handover, task, current, null, "TODO", "Pending owner");
        tasksToReturn.add(ht);

        ReadinessScoreResponse response = readinessScorerService.calculateScore(handover);

        assertNotNull(response);
        assertTrue(response.getOverallScore() < 50);
        assertFalse(response.isReadyForCompletion());
        assertFalse(response.getMissingRequirements().isEmpty());
    }
}
