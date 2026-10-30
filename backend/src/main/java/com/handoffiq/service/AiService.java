package com.handoffiq.service;

import com.handoffiq.ai.AiServiceFactory;
import com.handoffiq.ai.AiServiceProvider;
import com.handoffiq.dto.*;
import com.handoffiq.entity.*;
import com.handoffiq.exception.ResourceNotFoundException;
import com.handoffiq.logging.LoggingMdcFilter;
import com.handoffiq.logging.StructuredLogger;
import com.handoffiq.repository.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AiService {

    private final AiServiceFactory aiServiceFactory;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final MeetingRepository meetingRepository;
    private final TaskRepository taskRepository;
    private final DecisionRepository decisionRepository;
    private final HandoverRepository handoverRepository;
    private final HandoverTaskRepository handoverTaskRepository;
    private final KnowledgeDocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final AiInteractionLogRepository aiInteractionLogRepository;
    private final StructuredLogger structuredLogger;

    public AiService(
            AiServiceFactory aiServiceFactory,
            UserRepository userRepository,
            ProjectRepository projectRepository,
            MeetingRepository meetingRepository,
            TaskRepository taskRepository,
            DecisionRepository decisionRepository,
            HandoverRepository handoverRepository,
            HandoverTaskRepository handoverTaskRepository,
            KnowledgeDocumentRepository documentRepository,
            DocumentChunkRepository chunkRepository,
            AiInteractionLogRepository aiInteractionLogRepository,
            StructuredLogger structuredLogger) {
        this.aiServiceFactory = aiServiceFactory;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.meetingRepository = meetingRepository;
        this.taskRepository = taskRepository;
        this.decisionRepository = decisionRepository;
        this.handoverRepository = handoverRepository;
        this.handoverTaskRepository = handoverTaskRepository;
        this.documentRepository = documentRepository;
        this.chunkRepository = chunkRepository;
        this.aiInteractionLogRepository = aiInteractionLogRepository;
        this.structuredLogger = structuredLogger;
    }

    private String getCorrelationId() {
        String id = MDC.get(LoggingMdcFilter.MDC_CORRELATION_ID);
        return id != null ? id : "corr-ai";
    }

    @Transactional
    public MeetingAnalysisResponse analyzeMeetingNotes(MeetingAnalysisRequest request, Long userId) {
        long start = System.currentTimeMillis();
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
        AiServiceProvider provider = aiServiceFactory.getProvider();

        MeetingAnalysisResponse response;
        boolean success = true;
        try {
            response = provider.analyzeMeeting(request.getRawNotes(), request.getProjectId());

            // Validate suggested owners against real DB users
            List<User> activeUsers = userRepository.findByIsActiveTrue();
            if (response.getActionItems() != null) {
                for (MeetingAnalysisResponse.ActionItem item : response.getActionItems()) {
                    if (item.getSuggestedOwnerName() != null && item.getSuggestedOwnerId() == null) {
                        String nameToMatch = item.getSuggestedOwnerName().toLowerCase();
                        for (User u : activeUsers) {
                            String firstName = u.getFullName().split(" ")[0].toLowerCase();
                            if (nameToMatch.contains(firstName) || nameToMatch.contains(u.getFullName().toLowerCase())) {
                                item.setSuggestedOwnerId(u.getId());
                                item.setSuggestedOwnerName(u.getFullName());
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            success = false;
            structuredLogger.logDomainEvent("AI_MEETING_ANALYSIS", userId, "ERROR", System.currentTimeMillis() - start, "AI analysis error: " + ex.getMessage());
            throw ex;
        } finally {
            long duration = System.currentTimeMillis() - start;
            AiInteractionLog logEntry = new AiInteractionLog(
                    user,
                    "MEETING_ANALYSIS",
                    duration,
                    success ? "SUCCESS" : "FAILURE",
                    provider.getClass().getSimpleName().contains("Fallback"),
                    getCorrelationId()
            );
            aiInteractionLogRepository.save(logEntry);
            structuredLogger.logDomainEvent("AI_MEETING_ANALYSIS", userId, success ? "SUCCESS" : "FAILURE", duration, "Processed meeting analysis in " + duration + "ms");
        }

        return response;
    }

    @Transactional
    public CatchUpBriefResponse generateCatchUpBrief(CatchUpBriefRequest request, Long userId) {
        long start = System.currentTimeMillis();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Project project = null;
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId()).orElse(null);
        } else {
            List<Project> allProjects = projectRepository.findAll();
            if (!allProjects.isEmpty()) project = allProjects.get(0);
        }

        List<Meeting> meetings = project != null ? 
                meetingRepository.findByProjectIdOrderByScheduledStartDesc(project.getId()) :
                meetingRepository.findTop5ByOrderByScheduledStartDesc();

        List<Task> userTasks = taskRepository.findByAssignedToId(userId);
        List<Task> projectTasks = project != null ? taskRepository.findByProjectId(project.getId()) : userTasks;
        List<Decision> decisions = project != null ? decisionRepository.findByProjectIdOrderByDecidedAtDesc(project.getId()) : decisionRepository.findAllByOrderByDecidedAtDesc();

        AiServiceProvider provider = aiServiceFactory.getProvider();
        CatchUpBriefResponse response;
        boolean success = true;

        try {
            response = provider.generateCatchUpBrief(user, project, meetings, userTasks, projectTasks, decisions);
        } catch (Exception ex) {
            success = false;
            throw ex;
        } finally {
            long duration = System.currentTimeMillis() - start;
            AiInteractionLog logEntry = new AiInteractionLog(
                    user,
                    "CATCH_UP_BRIEF",
                    duration,
                    success ? "SUCCESS" : "FAILURE",
                    provider.getClass().getSimpleName().contains("Fallback"),
                    getCorrelationId()
            );
            aiInteractionLogRepository.save(logEntry);
            structuredLogger.logDomainEvent("AI_CATCH_UP_BRIEF", userId, success ? "SUCCESS" : "FAILURE", duration, "Generated catch-up brief in " + duration + "ms");
        }

        return response;
    }

    @Transactional
    public HandoverSummaryResponse generateHandoverSummary(HandoverSummaryRequest request, Long userId) {
        long start = System.currentTimeMillis();
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;

        Handover handover = handoverRepository.findById(request.getHandoverId())
                .orElseThrow(() -> new ResourceNotFoundException("Handover not found: " + request.getHandoverId()));

        List<HandoverTask> hTasks = handoverTaskRepository.findByHandoverId(handover.getId());
        List<Task> tasks = hTasks.stream().map(HandoverTask::getTask).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toList());
        List<Decision> decisions = handover.getProject() != null ? decisionRepository.findByProjectIdOrderByDecidedAtDesc(handover.getProject().getId()) : List.of();
        List<KnowledgeDocument> docs = handover.getProject() != null ? documentRepository.findByProjectId(handover.getProject().getId()) : documentRepository.findAll();

        AiServiceProvider provider = aiServiceFactory.getProvider();
        HandoverSummaryResponse response;
        boolean success = true;

        try {
            response = provider.generateHandoverSummary(handover, tasks, decisions, docs);

            // Persist the generated summary on the Handover entity
            handover.setAiGeneratedSummary(response.getFormattedMarkdown() != null ? response.getFormattedMarkdown() : response.getProjectOverview());
            if (response.getWorkCompleted() != null) handover.setWorkCompletedNotes(response.getWorkCompleted());
            if (response.getWorkInProgress() != null) handover.setWorkInProgressNotes(response.getWorkInProgress());
            if (response.getKnownIssuesAndBlockers() != null && !response.getKnownIssuesAndBlockers().isEmpty()) {
                handover.setBlockersNotes(String.join("\n", response.getKnownIssuesAndBlockers()));
            }
            if (response.getRecommendedNextSteps() != null && !response.getRecommendedNextSteps().isEmpty()) {
                handover.setRecommendedStepsNotes(String.join("\n", response.getRecommendedNextSteps()));
            }
            handoverRepository.save(handover);

        } catch (Exception ex) {
            success = false;
            throw ex;
        } finally {
            long duration = System.currentTimeMillis() - start;
            AiInteractionLog logEntry = new AiInteractionLog(
                    user,
                    "HANDOVER_SUMMARY",
                    duration,
                    success ? "SUCCESS" : "FAILURE",
                    provider.getClass().getSimpleName().contains("Fallback"),
                    getCorrelationId()
            );
            aiInteractionLogRepository.save(logEntry);
            structuredLogger.logDomainEvent("AI_HANDOVER_SUMMARY", userId, success ? "SUCCESS" : "FAILURE", duration, "Generated handover summary in " + duration + "ms");
        }

        return response;
    }

    @Transactional
    public AiAssistantResponse askAssistant(AiAssistantRequest request, Long userId) {
        long start = System.currentTimeMillis();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Project project = null;
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId()).orElse(null);
        }

        List<Meeting> meetings = project != null ?
                meetingRepository.findByProjectIdOrderByScheduledStartDesc(project.getId()) :
                meetingRepository.findAllByOrderByScheduledStartDesc();

        List<Task> tasks = project != null ?
                taskRepository.findByProjectId(project.getId()) :
                taskRepository.findAll();

        List<Decision> decisions = project != null ?
                decisionRepository.findByProjectIdOrderByDecidedAtDesc(project.getId()) :
                decisionRepository.findAllByOrderByDecidedAtDesc();

        List<Handover> handovers = project != null ?
                handoverRepository.findByProjectId(project.getId()) :
                handoverRepository.findAllByOrderByCreatedAtDesc();

        List<KnowledgeDocument> docs = project != null ?
                documentRepository.findByProjectId(project.getId()) :
                documentRepository.findAll();

        AiServiceProvider provider = aiServiceFactory.getProvider();
        AiAssistantResponse response;
        boolean success = true;

        try {
            response = provider.answerQuestion(request.getQuestion(), user, project, meetings, tasks, decisions, handovers, docs);
        } catch (Exception ex) {
            success = false;
            throw ex;
        } finally {
            long duration = System.currentTimeMillis() - start;
            AiInteractionLog logEntry = new AiInteractionLog(
                    user,
                    "ASSISTANT_CHAT",
                    duration,
                    success ? "SUCCESS" : "FAILURE",
                    provider.getClass().getSimpleName().contains("Fallback"),
                    getCorrelationId()
            );
            aiInteractionLogRepository.save(logEntry);
            structuredLogger.logDomainEvent("AI_ASSISTANT_QUERY", userId, success ? "SUCCESS" : "FAILURE", duration, "Handoff assistant answered query in " + duration + "ms");
        }

        return response;
    }

    @Transactional
    public KnowledgeSearchResponse searchKnowledge(KnowledgeSearchRequest request, Long userId) {
        long start = System.currentTimeMillis();
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;

        List<KnowledgeDocument> documents = request.getProjectId() != null ?
                documentRepository.findByProjectId(request.getProjectId()) :
                documentRepository.findAll();

        List<DocumentChunk> chunks = chunkRepository.searchChunks(request.getQuery());

        AiServiceProvider provider = aiServiceFactory.getProvider();
        KnowledgeSearchResponse response;
        boolean success = true;

        try {
            response = provider.searchKnowledge(request.getQuery(), documents, chunks);
        } catch (Exception ex) {
            success = false;
            throw ex;
        } finally {
            long duration = System.currentTimeMillis() - start;
            AiInteractionLog logEntry = new AiInteractionLog(
                    user,
                    "KNOWLEDGE_SEARCH",
                    duration,
                    success ? "SUCCESS" : "FAILURE",
                    provider.getClass().getSimpleName().contains("Fallback"),
                    getCorrelationId()
            );
            aiInteractionLogRepository.save(logEntry);
            structuredLogger.logDomainEvent("AI_KNOWLEDGE_SEARCH", userId, success ? "SUCCESS" : "FAILURE", duration, "Knowledge search processed in " + duration + "ms");
        }

        return response;
    }
}
