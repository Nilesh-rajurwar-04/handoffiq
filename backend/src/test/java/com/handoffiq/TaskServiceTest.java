package com.handoffiq;

import com.handoffiq.dto.*;
import com.handoffiq.entity.*;
import com.handoffiq.logging.StructuredLogger;
import com.handoffiq.repository.*;
import com.handoffiq.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TaskServiceTest {

    private TaskService taskService;
    private User testUser;
    private Project testProject;
    private Task testTask;

    private boolean taskSaved = false;
    private boolean outcomeSaved = false;
    private boolean carryForwardSaved = false;

    @BeforeEach
    void setUp() {
        taskSaved = false;
        outcomeSaved = false;
        carryForwardSaved = false;

        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Sarah Jenkins");

        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Project Nexus");

        testTask = new Task();
        testTask.setId(10L);
        testTask.setTitle("Auth Refactor");
        testTask.setProject(testProject);
        testTask.setCreatedBy(testUser);
        testTask.setStatus("TODO");

        TaskRepository taskRepo = (TaskRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{TaskRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) return Optional.of(testTask);
                    if ("save".equals(method.getName())) {
                        taskSaved = true;
                        return args[0];
                    }
                    return null;
                }
        );

        ProjectRepository projRepo = (ProjectRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{ProjectRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) return Optional.of(testProject);
                    return null;
                }
        );

        UserRepository userRepo = (UserRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{UserRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) return Optional.of(testUser);
                    return null;
                }
        );

        Meeting meeting = new Meeting();
        meeting.setId(99L);
        meeting.setTitle("Next Sprint Sync");

        MeetingRepository meetRepo = (MeetingRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{MeetingRepository.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) return Optional.of(meeting);
                    return null;
                }
        );

        DecisionRepository decRepo = (DecisionRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{DecisionRepository.class},
                (proxy, method, args) -> null
        );

        TaskAssignmentRepository assignRepo = (TaskAssignmentRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{TaskAssignmentRepository.class},
                (proxy, method, args) -> List.of()
        );

        TaskCommentRepository commentRepo = (TaskCommentRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{TaskCommentRepository.class},
                (proxy, method, args) -> List.of()
        );

        TaskStatusHistoryRepository statusRepo = (TaskStatusHistoryRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{TaskStatusHistoryRepository.class},
                (proxy, method, args) -> List.of()
        );

        TaskOutcomeRepository outcomeRepo = (TaskOutcomeRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{TaskOutcomeRepository.class},
                (proxy, method, args) -> {
                    if ("findByTaskId".equals(method.getName())) return Optional.empty();
                    if ("save".equals(method.getName())) {
                        outcomeSaved = true;
                        return args[0];
                    }
                    return null;
                }
        );

        TaskCarryForwardHistoryRepository carryRepo = (TaskCarryForwardHistoryRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{TaskCarryForwardHistoryRepository.class},
                (proxy, method, args) -> {
                    if ("save".equals(method.getName())) {
                        carryForwardSaved = true;
                        return args[0];
                    }
                    if ("findByTaskIdOrderByCarriedAtDesc".equals(method.getName())) return List.of();
                    return null;
                }
        );

        StructuredLogger logger = new StructuredLogger();

        taskService = new TaskService(
                taskRepo, projRepo, userRepo, meetRepo, decRepo,
                assignRepo, commentRepo, statusRepo, outcomeRepo, carryRepo, logger
        );
    }

    @Test
    @DisplayName("Should create task and save entity")
    void testCreateTask() {
        CreateTaskRequest req = new CreateTaskRequest();
        req.setTitle("Auth Refactor");
        req.setProjectId(1L);
        req.setPriority("HIGH");

        TaskResponse res = taskService.createTask(req, 1L);

        assertNotNull(res);
        assertEquals("Auth Refactor", res.getTitle());
        assertTrue(taskSaved);
    }

    @Test
    @DisplayName("Should record task outcome and mark task completed")
    void testRecordOutcome() {
        TaskOutcomeRequest req = new TaskOutcomeRequest("Completed successfully", "Unit tests passed with 100% coverage");
        TaskResponse res = taskService.recordOutcome(10L, req, 1L);

        assertNotNull(res);
        assertEquals("COMPLETED", testTask.getStatus());
        assertEquals("Completed successfully", testTask.getCompletionOutcome());
        assertTrue(outcomeSaved);
    }

    @Test
    @DisplayName("Should carry forward task without creating duplicate and preserve history")
    void testCarryForward() {
        CarryForwardRequest req = new CarryForwardRequest(99L, null, "Requires architectural sign-off");
        TaskResponse res = taskService.carryForwardTask(10L, req, 1L);

        assertNotNull(res);
        assertTrue(carryForwardSaved);
    }
}
