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

@Service("geminiAiService")
public class GeminiAiService implements AiServiceProvider {

    private static final Logger log = LoggerFactory.getLogger(GeminiAiService.class);

    @Value("${handoffiq.ai.gemini.api-key:}")
    private String apiKey;

    @Value("${handoffiq.ai.gemini.model:gemini-1.5-flash}")
    private String model;

    @Value("${handoffiq.ai.timeout-ms:15000}")
    private long timeoutMs;

    private final RuleBasedFallbackAiService fallbackService;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public GeminiAiService(RuleBasedFallbackAiService fallbackService, ObjectMapper objectMapper) {
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
        return apiKey != null && !apiKey.trim().isEmpty() && !apiKey.equals("YOUR_GEMINI_API_KEY") && this.httpClient != null;
    }

    private String callGeminiApi(String prompt) throws Exception {
        if (!isApiKeyConfigured()) {
            throw new IllegalStateException("Gemini API key is not configured. Falling back to rule-based engine.");
        }

        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;

        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> parts = Map.of("parts", List.of(textPart));
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(parts),
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "responseMimeType", "application/json"
                )
        );

        String jsonPayload = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofMillis(timeoutMs))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        long start = System.currentTimeMillis();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        long duration = System.currentTimeMillis() - start;

        if (response.statusCode() != 200) {
            log.error("Gemini API returned status {}: {}", response.statusCode(), response.body());
            throw new RuntimeException("Gemini API error: HTTP " + response.statusCode());
        }

        log.info("Gemini API call succeeded in {}ms", duration);
        JsonNode root = objectMapper.readTree(response.body());
        JsonNode candidate = root.path("candidates").get(0);
        return candidate.path("content").path("parts").get(0).path("text").asText();
    }

    @Override
    public MeetingAnalysisResponse analyzeMeeting(String rawNotes, Long projectId) {
        if (!isApiKeyConfigured()) {
            log.info("Gemini API key not configured; delegating meeting analysis to fallback engine");
            return fallbackService.analyzeMeeting(rawNotes, projectId);
        }

        try {
            String prompt = "You are an enterprise AI meeting intelligence engine. Analyze the following meeting notes and output strict JSON with this schema:\n" +
                    "{\n" +
                    "  \"meetingSummary\": \"Concise paragraph summary\",\n" +
                    "  \"importantDiscussionPoints\": [\"string\"],\n" +
                    "  \"decisions\": [{\"title\": \"string\", \"rationale\": \"string\", \"context\": \"string\"}],\n" +
                    "  \"actionItems\": [{\"title\": \"string\", \"description\": \"string\", \"suggestedOwnerName\": \"string\", \"suggestedDeadline\": \"YYYY-MM-DD\", \"priority\": \"LOW|MEDIUM|HIGH|CRITICAL\"}],\n" +
                    "  \"priority\": \"LOW|MEDIUM|HIGH|CRITICAL\",\n" +
                    "  \"risksAndUnresolvedQuestions\": [\"string\"]\n" +
                    "}\n\nNotes to analyze:\n" + rawNotes;

            String jsonText = callGeminiApi(prompt);
            MeetingAnalysisResponse resp = objectMapper.readValue(jsonText, MeetingAnalysisResponse.class);
            resp.setFallbackUsed(false);
            return resp;
        } catch (Exception ex) {
            log.warn("Gemini meeting analysis failed ({}); falling back to rule-based engine", ex.getMessage());
            return fallbackService.analyzeMeeting(rawNotes, projectId);
        }
    }

    @Override
    public HandoverSummaryResponse generateHandoverSummary(Handover handover, List<Task> tasks, List<Decision> decisions, List<KnowledgeDocument> docs) {
        if (!isApiKeyConfigured()) {
            return fallbackService.generateHandoverSummary(handover, tasks, decisions, docs);
        }

        try {
            String prompt = "Generate a comprehensive employee handover summary for handover #" + handover.getId() +
                    " for project " + (handover.getProject() != null ? handover.getProject().getName() : "") +
                    " from " + handover.getCurrentEmployee().getFullName() + " to " + handover.getReceivingEmployee().getFullName() +
                    ". Return strict JSON matching HandoverSummaryResponse fields.";

            String jsonText = callGeminiApi(prompt);
            HandoverSummaryResponse resp = objectMapper.readValue(jsonText, HandoverSummaryResponse.class);
            resp.setFallbackUsed(false);
            return resp;
        } catch (Exception ex) {
            log.warn("Gemini handover summary failed ({}); using fallback", ex.getMessage());
            return fallbackService.generateHandoverSummary(handover, tasks, decisions, docs);
        }
    }

    @Override
    public CatchUpBriefResponse generateCatchUpBrief(User user, Project project, List<Meeting> recentMeetings, List<Task> userTasks, List<Task> projectTasks, List<Decision> decisions) {
        if (!isApiKeyConfigured()) {
            return fallbackService.generateCatchUpBrief(user, project, recentMeetings, userTasks, projectTasks, decisions);
        }

        try {
            String prompt = "Generate a personalized Catch-up Brief for employee " + user.getFullName() +
                    " on project " + (project != null ? project.getName() : "All") + ". Return strict JSON.";
            String jsonText = callGeminiApi(prompt);
            CatchUpBriefResponse resp = objectMapper.readValue(jsonText, CatchUpBriefResponse.class);
            resp.setFallbackUsed(false);
            return resp;
        } catch (Exception ex) {
            log.warn("Gemini catch-up brief failed ({}); using fallback", ex.getMessage());
            return fallbackService.generateCatchUpBrief(user, project, recentMeetings, userTasks, projectTasks, decisions);
        }
    }

    @Override
    public AiAssistantResponse answerQuestion(String question, User user, Project project, List<Meeting> meetings, List<Task> tasks, List<Decision> decisions, List<Handover> handovers, List<KnowledgeDocument> documents) {
        if (!isApiKeyConfigured()) {
            return fallbackService.answerQuestion(question, user, project, meetings, tasks, decisions, handovers, documents);
        }

        try {
            String prompt = "You are Handoff Assistant. Answer the question accurately using project database context: " + question + ". Return strict JSON.";
            String jsonText = callGeminiApi(prompt);
            AiAssistantResponse resp = objectMapper.readValue(jsonText, AiAssistantResponse.class);
            resp.setFallbackUsed(false);
            return resp;
        } catch (Exception ex) {
            log.warn("Gemini assistant chat failed ({}); using fallback", ex.getMessage());
            return fallbackService.answerQuestion(question, user, project, meetings, tasks, decisions, handovers, documents);
        }
    }

    @Override
    public KnowledgeSearchResponse searchKnowledge(String query, List<KnowledgeDocument> documents, List<DocumentChunk> chunks) {
        if (!isApiKeyConfigured()) {
            return fallbackService.searchKnowledge(query, documents, chunks);
        }

        try {
            String prompt = "Synthesize an answer for query: '" + query + "' from repository documents. Return strict JSON.";
            String jsonText = callGeminiApi(prompt);
            KnowledgeSearchResponse resp = objectMapper.readValue(jsonText, KnowledgeSearchResponse.class);
            resp.setFallbackUsed(false);
            return resp;
        } catch (Exception ex) {
            log.warn("Gemini knowledge search failed ({}); using fallback", ex.getMessage());
            return fallbackService.searchKnowledge(query, documents, chunks);
        }
    }
}
