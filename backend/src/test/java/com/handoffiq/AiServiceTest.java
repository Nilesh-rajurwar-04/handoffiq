package com.handoffiq;

import com.handoffiq.ai.RuleBasedFallbackAiService;
import com.handoffiq.dto.AiAssistantResponse;
import com.handoffiq.dto.CatchUpBriefResponse;
import com.handoffiq.dto.MeetingAnalysisResponse;
import com.handoffiq.entity.Task;
import com.handoffiq.entity.User;
import com.handoffiq.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AiServiceTest {

    private RuleBasedFallbackAiService fallbackAiService;
    private List<User> activeUsers = new ArrayList<>();

    @BeforeEach
    void setUp() {
        activeUsers.clear();
        User rahul = new User();
        rahul.setId(4L);
        rahul.setFullName("Rahul Sharma");

        User priya = new User();
        priya.setId(5L);
        priya.setFullName("Priya Patel");

        activeUsers.add(rahul);
        activeUsers.add(priya);

        UserRepository userRepo = (UserRepository) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{UserRepository.class},
                (proxy, method, args) -> {
                    if ("findByIsActiveTrue".equals(method.getName())) return activeUsers;
                    return null;
                }
        );

        fallbackAiService = new RuleBasedFallbackAiService(userRepo);
    }

    @Test
    @DisplayName("Should extract summary, decisions, action items and deadlines from meeting notes")
    void testMeetingAnalysis() {
        String notes = "We discussed the new employee onboarding portal. Rahul will complete the login module by Friday. " +
                "Priya will prepare the database documentation. The manager approved the new authentication design. " +
                "The API integration is still pending.";

        MeetingAnalysisResponse response = fallbackAiService.analyzeMeeting(notes, 1L);

        assertNotNull(response);
        assertTrue(response.isFallbackUsed());
        assertFalse(response.getMeetingSummary().isEmpty());

        // Check decisions
        assertFalse(response.getDecisions().isEmpty());
        assertTrue(response.getDecisions().stream().anyMatch(d -> d.getTitle().toLowerCase().contains("approved")));

        // Check action items
        assertFalse(response.getActionItems().isEmpty());
        assertTrue(response.getActionItems().stream().anyMatch(a -> a.getTitle().contains("Rahul")));

        // Check risks / pending
        assertFalse(response.getRisksAndUnresolvedQuestions().isEmpty());
        assertTrue(response.getRisksAndUnresolvedQuestions().stream().anyMatch(r -> r.toLowerCase().contains("pending")));
    }

    @Test
    @DisplayName("Should ground Handoff Assistant answers in actual database records")
    void testAssistantGroundedAnswer() {
        User user = new User();
        user.setId(5L);
        user.setFullName("Priya Patel");

        Task task = new Task();
        task.setId(9L);
        task.setTitle("Refactor Auth Controller");
        task.setStatus("IN_PROGRESS");
        task.setPriority("HIGH");
        task.setAssignedTo(user);

        AiAssistantResponse response = fallbackAiService.answerQuestion(
                "What tasks are pending for me?",
                user,
                null,
                List.of(),
                List.of(task),
                List.of(),
                List.of(),
                List.of()
        );

        assertNotNull(response);
        assertTrue(response.getAnswer().contains("Refactor Auth Controller"));
        assertTrue(response.getAnswer().contains("#9"));
        assertEquals("USER_TASKS", response.getMatchedIntent());
    }
}
