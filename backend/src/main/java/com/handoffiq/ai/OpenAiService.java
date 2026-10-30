package com.handoffiq.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handoffiq.dto.*;
import com.handoffiq.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service("openAiService")
public class OpenAiService implements AiServiceProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAiService.class);

    @Value("${handoffiq.ai.openai.api-key:}")
    private String apiKey;

    @Value("${handoffiq.ai.openai.model:gpt-4o-mini}")
    private String model;

    @Value("${handoffiq.ai.timeout-ms:15000}")
    private long timeoutMs;

    private final RuleBasedFallbackAiService fallbackService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public OpenAiService(RuleBasedFallbackAiService fallbackService, ObjectMapper objectMapper) {
        this.fallbackService = fallbackService;
        this.objectMapper = objectMapper;
        HttpClient client = null;
        try {
            client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
        } catch (Throwable t) {
            log.warn("Could not initialize java.net.http.HttpClient: {}", t.getMessage());
        }
        this.httpClient = client;
    }

    private boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty() && !apiKey.equals("YOUR_OPENAI_API_KEY") && this.httpClient != null;
    }

    private String callOpenAiApi(String systemPrompt, String userPrompt) throws Exception {
        if (!isApiKeyConfigured()) {
            throw new IllegalStateException("OpenAI API key is not configured. Falling back to rule-based engine.");
        }

        String endpoint = "https://api.openai.com/v1/chat/completions";

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "response_format", Map.of("type", "json_object"),
                "temperature", 0.2
        );

        String jsonPayload = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofMillis(timeoutMs))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        long start = System.currentTimeMillis();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        long duration = System.currentTimeMillis() - start;

        if (response.statusCode() != 200) {
            log.error("OpenAI API returned status {}: {}", response.statusCode(), response.body());
            throw new RuntimeException("OpenAI API error: HTTP " + response.statusCode());
        }

        log.info("OpenAI API call succeeded in {}ms", duration);
        JsonNode root = objectMapper.readTree(response.body());
        return root.path("choices").get(0).path("message").path("content").asText();
    }

    @Override
    public MeetingAnalysisResponse analyzeMeeting(String rawNotes, Long projectId) {
        if (!isApiKeyConfigured()) {
            return fallbackService.analyzeMeeting(rawNotes, projectId);
        }

        try {
            String systemPrompt = "You are an enterprise AI meeting intelligence engine. Output strict JSON with meetingSummary, importantDiscussionPoints, decisions, actionItems, priority, risksAndUnresolvedQuestions.";
            String jsonText = callOpenAiApi(systemPrompt, rawNotes);
            MeetingAnalysisResponse resp = objectMapper.readValue(jsonText, MeetingAnalysisResponse.class);
            resp.setFallbackUsed(false);
            return resp;
        } catch (Exception ex) {
            log.warn("OpenAI meeting analysis failed ({}); using fallback", ex.getMessage());
            return fallbackService.analyzeMeeting(rawNotes, projectId);
        }
    }

    @Override
    public HandoverSummaryResponse generateHandoverSummary(Handover handover, List<Task> tasks, List<Decision> decisions, List<KnowledgeDocument> docs) {
        if (!isApiKeyConfigured()) {
            return fallbackService.generateHandoverSummary(handover, tasks, decisions, docs);
        }

        try {
            String systemPrompt = "Generate an executive employee handover report in strict JSON format.";
            String userPrompt = "Handover #" + handover.getId() + " - " + handover.getTitle() + " for project " + (handover.getProject() != null ? handover.getProject().getName() : "");
            String jsonText = callOpenAiApi(systemPrompt, userPrompt);
            HandoverSummaryResponse resp = objectMapper.readValue(jsonText, HandoverSummaryResponse.class);
            resp.setFallbackUsed(false);
            return resp;
        } catch (Exception ex) {
            log.warn("OpenAI handover summary failed ({}); using fallback", ex.getMessage());
            return fallbackService.generateHandoverSummary(handover, tasks, decisions, docs);
        }
    }

    @Override
    public CatchUpBriefResponse generateCatchUpBrief(User user, Project project, List<Meeting> recentMeetings, List<Task> userTasks, List<Task> projectTasks, List<Decision> decisions) {
        if (!isApiKeyConfigured()) {
            return fallbackService.generateCatchUpBrief(user, project, recentMeetings, userTasks, projectTasks, decisions);
        }

        try {
            String systemPrompt = "Generate a personalized Catch-up Brief in JSON format for " + user.getFullName();
            String userPrompt = "Project: " + (project != null ? project.getName() : "All");
            String jsonText = callOpenAiApi(systemPrompt, userPrompt);
            CatchUpBriefResponse resp = objectMapper.readValue(jsonText, CatchUpBriefResponse.class);
            resp.setFallbackUsed(false);
            return resp;
        } catch (Exception ex) {
            log.warn("OpenAI catch-up brief failed ({}); using fallback", ex.getMessage());
            return fallbackService.generateCatchUpBrief(user, project, recentMeetings, userTasks, projectTasks, decisions);
        }
    }

    @Override
    public AiAssistantResponse answerQuestion(String question, User user, Project project, List<Meeting> meetings, List<Task> tasks, List<Decision> decisions, List<Handover> handovers, List<KnowledgeDocument> documents) {
        if (!isApiKeyConfigured()) {
            return fallbackService.answerQuestion(question, user, project, meetings, tasks, decisions, handovers, documents);
        }

        try {
            String systemPrompt = "You are Handoff Assistant. Answer questions accurately grounded in database context. Return strict JSON.";
            String jsonText = callOpenAiApi(systemPrompt, question);
            AiAssistantResponse resp = objectMapper.readValue(jsonText, AiAssistantResponse.class);
            resp.setFallbackUsed(false);
            return resp;
        } catch (Exception ex) {
            log.warn("OpenAI assistant chat failed ({}); using fallback", ex.getMessage());
            return fallbackService.answerQuestion(question, user, project, meetings, tasks, decisions, handovers, documents);
        }
    }

    @Override
    public KnowledgeSearchResponse searchKnowledge(String query, List<KnowledgeDocument> documents, List<DocumentChunk> chunks) {
        if (!isApiKeyConfigured()) {
            return fallbackService.searchKnowledge(query, documents, chunks);
        }

        try {
            String systemPrompt = "Synthesize an answer from documents. Return JSON.";
            String jsonText = callOpenAiApi(systemPrompt, query);
            KnowledgeSearchResponse resp = objectMapper.readValue(jsonText, KnowledgeSearchResponse.class);
            resp.setFallbackUsed(false);
            return resp;
        } catch (Exception ex) {
            log.warn("OpenAI knowledge search failed ({}); using fallback", ex.getMessage());
            return fallbackService.searchKnowledge(query, documents, chunks);
        }
    }
}
