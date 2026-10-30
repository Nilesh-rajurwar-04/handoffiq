package com.handoffiq.ai;

import com.handoffiq.dto.*;
import com.handoffiq.entity.*;
import com.handoffiq.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service("fallbackAiService")
public class RuleBasedFallbackAiService implements AiServiceProvider {

    private static final Logger log = LoggerFactory.getLogger(RuleBasedFallbackAiService.class);
    private final UserRepository userRepository;

    public RuleBasedFallbackAiService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public MeetingAnalysisResponse analyzeMeeting(String rawNotes, Long projectId) {
        log.info("Executing rule-based NLP meeting analysis engine on text ({} chars)", rawNotes != null ? rawNotes.length() : 0);
        MeetingAnalysisResponse response = new MeetingAnalysisResponse();
        response.setFallbackUsed(true);

        if (rawNotes == null || rawNotes.trim().isEmpty()) {
            response.setMeetingSummary("No notes provided for analysis.");
            response.setImportantDiscussionPoints(Collections.emptyList());
            response.setDecisions(Collections.emptyList());
            response.setActionItems(Collections.emptyList());
            response.setPriority("MEDIUM");
            response.setRisksAndUnresolvedQuestions(Collections.emptyList());
            return response;
        }

        List<User> activeUsers = userRepository.findByIsActiveTrue();
        String[] sentences = rawNotes.split("[\\.\\n\\r]+");
        List<String> discussionPoints = new ArrayList<>();
        List<MeetingAnalysisResponse.DecisionItem> decisions = new ArrayList<>();
        List<MeetingAnalysisResponse.ActionItem> actionItems = new ArrayList<>();
        List<String> risks = new ArrayList<>();

        for (String rawSentence : sentences) {
            String sentence = rawSentence.trim();
            if (sentence.isEmpty()) continue;

            String lower = sentence.toLowerCase();

            // Detect decisions
            if (lower.contains("approved") || lower.contains("decided") || lower.contains("agreed") || lower.contains("finalized") || lower.contains("resolved")) {
                decisions.add(new MeetingAnalysisResponse.DecisionItem(
                        sentence,
                        "Decision agreed during meeting discussion",
                        "Organizational alignment"
                ));
            }
            // Detect risks or pending/blockers
            else if (lower.contains("pending") || lower.contains("risk") || lower.contains("blocker") || lower.contains("unresolved") || lower.contains("issue") || lower.contains("delay") || lower.contains("concern")) {
                risks.add(sentence);
            }

            // Detect action items: look for modal verbs or active assignment patterns
            boolean isAction = lower.contains("will") || lower.contains("shall") || lower.contains("assigned to") || lower.contains("to complete") || lower.contains("responsible for") || lower.contains("needs to") || lower.contains("must");
            
            // Check if any registered user is named in the sentence
            User matchedUser = null;
            for (User u : activeUsers) {
                String first = u.getFullName().split(" ")[0].toLowerCase();
                if (lower.contains(first) || lower.contains(u.getFullName().toLowerCase())) {
                    matchedUser = u;
                    break;
                }
            }

            if (isAction || matchedUser != null) {
                String suggestedDeadline = extractDeadline(sentence);
                String priority = (lower.contains("urgent") || lower.contains("asap") || lower.contains("critical")) ? "CRITICAL" :
                                  (lower.contains("high") || lower.contains("important")) ? "HIGH" : "MEDIUM";

                MeetingAnalysisResponse.ActionItem item = new MeetingAnalysisResponse.ActionItem(
                        sentence,
                        "Derived action item from meeting notes discussion",
                        matchedUser != null ? matchedUser.getFullName() : "Unassigned (Pending Review)",
                        matchedUser != null ? matchedUser.getId() : null,
                        suggestedDeadline,
                        priority
                );
                actionItems.add(item);
            } else {
                discussionPoints.add(sentence);
            }
        }

        // Generate synthetic summary
        StringBuilder summary = new StringBuilder();
        summary.append("During this session, ").append(discussionPoints.size()).append(" discussion topics were covered, ");
        summary.append(decisions.size()).append(" architectural/product decisions were established, and ");
        summary.append(actionItems.size()).append(" discrete action items were formulated.");
        if (!risks.isEmpty()) {
            summary.append(" Attention was flagged for ").append(risks.size()).append(" pending items or potential risks.");
        }

        response.setMeetingSummary(summary.toString());
        response.setImportantDiscussionPoints(discussionPoints.isEmpty() ? List.of("General progress review and alignment sync") : discussionPoints);
        response.setDecisions(decisions);
        response.setActionItems(actionItems);
        response.setRisksAndUnresolvedQuestions(risks.isEmpty() ? List.of("Verify cross-module dependencies and deployment schedule") : risks);
        response.setPriority(risks.size() > 1 ? "HIGH" : "MEDIUM");

        return response;
    }

    private String extractDeadline(String sentence) {
        String lower = sentence.toLowerCase();
        LocalDate now = LocalDate.now();
        if (lower.contains("by friday")) {
            return now.plusDays((5 - now.getDayOfWeek().getValue() + 7) % 7).toString();
        } else if (lower.contains("by tomorrow")) {
            return now.plusDays(1).toString();
        } else if (lower.contains("by next week") || lower.contains("next week")) {
            return now.plusDays(7).toString();
        } else if (lower.contains("by end of sprint") || lower.contains("end of sprint")) {
            return now.plusDays(10).toString();
        }
        return now.plusDays(5).toString(); // default 5 day SLA
    }

    @Override
    public HandoverSummaryResponse generateHandoverSummary(Handover handover, List<Task> tasks, List<Decision> decisions, List<KnowledgeDocument> docs) {
        HandoverSummaryResponse response = new HandoverSummaryResponse();
        response.setHandoverId(handover.getId());
        response.setFallbackUsed(true);

        String currentName = handover.getCurrentEmployee() != null ? handover.getCurrentEmployee().getFullName() : "Outgoing Employee";
        String recvName = handover.getReceivingEmployee() != null ? handover.getReceivingEmployee().getFullName() : "Incoming Employee";
        String projName = handover.getProject() != null ? handover.getProject().getName() : "Assigned Project";

        response.setProjectOverview("Strategic responsibility transition for " + projName + " from " + currentName + " to " + recvName + ". Reason: " + handover.getReason());

        long completedCount = tasks.stream().filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus())).count();
        long inProgressCount = tasks.stream().filter(t -> "IN_PROGRESS".equalsIgnoreCase(t.getStatus())).count();
        long blockedCount = tasks.stream().filter(t -> "BLOCKED".equalsIgnoreCase(t.getStatus())).count();
        long todoCount = tasks.stream().filter(t -> "TODO".equalsIgnoreCase(t.getStatus())).count();

        response.setWorkCompleted("A total of " + completedCount + " tasks have been successfully concluded with documented outcomes.");
        response.setWorkInProgress(inProgressCount + " tasks are actively in development and require continuity from " + recvName + ".");

        List<String> pending = tasks.stream()
                .filter(t -> !"COMPLETED".equalsIgnoreCase(t.getStatus()) && !"CANCELLED".equalsIgnoreCase(t.getStatus()))
                .map(t -> "[" + t.getPriority() + "] " + t.getTitle() + " (Status: " + t.getStatus() + ", Due: " + (t.getDueDate() != null ? t.getDueDate().toString() : "TBD") + ")")
                .collect(Collectors.toList());
        response.setPendingTasks(pending);

        List<String> deadlines = tasks.stream()
                .filter(t -> t.getDueDate() != null && !"COMPLETED".equalsIgnoreCase(t.getStatus()))
                .sorted(Comparator.comparing(Task::getDueDate))
                .limit(5)
                .map(t -> t.getTitle() + " is due on " + t.getDueDate())
                .collect(Collectors.toList());
        response.setUpcomingDeadlines(deadlines);

        List<String> decList = decisions.stream()
                .map(d -> d.getTitle() + ": " + (d.getRationale() != null ? d.getRationale() : "Approved architectural standard"))
                .collect(Collectors.toList());
        response.setImportantDecisions(decList);

        List<String> blockers = tasks.stream()
                .filter(t -> "BLOCKED".equalsIgnoreCase(t.getStatus()))
                .map(t -> "Task #" + t.getId() + " (" + t.getTitle() + ") is currently BLOCKED. Review completion notes for remediation.")
                .collect(Collectors.toList());
        if (blockers.isEmpty()) {
            blockers.add("No critical task blockers detected at time of handover generation.");
        }
        response.setKnownIssuesAndBlockers(blockers);

        List<String> docList = docs.stream()
                .map(d -> d.getTitle() + " (" + d.getCategory() + ") - " + d.getFileName())
                .collect(Collectors.toList());
        response.setRelevantDocuments(docList);

        List<String> nextSteps = List.of(
                "Review open pull requests and verify staging deployment credentials.",
                "Execute local test suites for " + projName + " to confirm development environment readiness.",
                "Acknowledge the handover once all repository and secret credentials are confirmed.",
                "Schedule a 30-minute shadow pairing session to walk through in-flight tasks."
        );
        response.setRecommendedNextSteps(nextSteps);

        StringBuilder md = new StringBuilder();
        md.append("## Automated Handover Intelligence Report\n\n");
        md.append("**Project**: ").append(projName).append("\n");
        md.append("**From**: ").append(currentName).append(" | **To**: ").append(recvName).append("\n\n");
        md.append("### Executive Overview\n").append(response.getProjectOverview()).append("\n\n");
        md.append("### In-Flight Responsibilities\n");
        for (String p : pending) {
            md.append("- ").append(p).append("\n");
        }
        md.append("\n### Known Blockers & Issues\n");
        for (String b : blockers) {
            md.append("- ").append(b).append("\n");
        }
        md.append("\n### Recommended Immediate Actions\n");
        for (String s : nextSteps) {
            md.append("1. ").append(s).append("\n");
        }

        response.setFormattedMarkdown(md.toString());
        return response;
    }

    @Override
    public CatchUpBriefResponse generateCatchUpBrief(User user, Project project, List<Meeting> recentMeetings, List<Task> userTasks, List<Task> projectTasks, List<Decision> decisions) {
        CatchUpBriefResponse response = new CatchUpBriefResponse();
        response.setFallbackUsed(true);
        response.setBriefTitle("Project Catch-up Brief: " + (project != null ? project.getName() : "All Projects"));
        response.setProjectName(project != null ? project.getName() : "Enterprise Portfolio");
        response.setEmployeeName(user.getFullName());
        response.setGeneratedAt(LocalDate.now().format(DateTimeFormatter.ISO_DATE));

        response.setExecutiveSummary("Personalized intelligence brief synthesized from " + recentMeetings.size() + " recent meetings, " +
                decisions.size() + " active architectural decisions, and " + userTasks.size() + " assigned action items.");

        List<CatchUpBriefResponse.MeetingDigestItem> whatHappened = recentMeetings.stream()
                .map(m -> new CatchUpBriefResponse.MeetingDigestItem(
                        m.getId(),
                        m.getTitle(),
                        m.getScheduledStart().toLocalDate().toString(),
                        m.getAiSummary() != null ? m.getAiSummary() : (m.getAgenda() != null ? m.getAgenda() : "Discussion on project milestones")
                ))
                .collect(Collectors.toList());
        response.setWhatHappened(whatHappened);

        List<CatchUpBriefResponse.DecisionDigestItem> keyDecisions = decisions.stream()
                .map(d -> new CatchUpBriefResponse.DecisionDigestItem(
                        d.getId(),
                        d.getTitle(),
                        d.getRationale() != null ? d.getRationale() : "Approved organizational decision",
                        d.getMeeting() != null ? d.getMeeting().getId() : null
                ))
                .collect(Collectors.toList());
        response.setKeyDecisions(keyDecisions);

        response.setCurrentDevelopments(List.of(
                "Sprint deliverables are tracking towards the upcoming release milestone.",
                "Cross-module API contract updates are in progress between backend and gateway teams."
        ));

        List<CatchUpBriefResponse.TaskDigestItem> responsibilities = userTasks.stream()
                .filter(t -> !"COMPLETED".equalsIgnoreCase(t.getStatus()))
                .map(t -> new CatchUpBriefResponse.TaskDigestItem(
                        t.getId(),
                        t.getTitle(),
                        t.getPriority(),
                        t.getStatus(),
                        t.getDueDate() != null ? t.getDueDate().toString() : "TBD",
                        user.getFullName()
                ))
                .collect(Collectors.toList());
        response.setEmployeeResponsibilities(responsibilities);

        List<CatchUpBriefResponse.TaskDigestItem> pendingTeam = projectTasks.stream()
                .filter(t -> !"COMPLETED".equalsIgnoreCase(t.getStatus()))
                .limit(8)
                .map(t -> new CatchUpBriefResponse.TaskDigestItem(
                        t.getId(),
                        t.getTitle(),
                        t.getPriority(),
                        t.getStatus(),
                        t.getDueDate() != null ? t.getDueDate().toString() : "TBD",
                        t.getAssignedTo() != null ? t.getAssignedTo().getFullName() : "Unassigned"
                ))
                .collect(Collectors.toList());
        response.setPendingTeamTasks(pendingTeam);

        List<String> deadlines = userTasks.stream()
                .filter(t -> t.getDueDate() != null && !"COMPLETED".equalsIgnoreCase(t.getStatus()))
                .sorted(Comparator.comparing(Task::getDueDate))
                .map(t -> t.getTitle() + " (Due: " + t.getDueDate() + ")")
                .collect(Collectors.toList());
        response.setApproachingDeadlines(deadlines.isEmpty() ? List.of("No immediate deadlines assigned within the next 48 hours.") : deadlines);

        response.setRecommendedNextSteps(List.of(
                "Prioritize your highest priority open task (" + (responsibilities.isEmpty() ? "Check unassigned tasks" : responsibilities.get(0).getTitle()) + ").",
                "Review the latest decisions in the Decision Traceability log.",
                "Post a quick update on your active tasks in the team sync channel."
        ));

        return response;
    }

    @Override
    public AiAssistantResponse answerQuestion(String question, User user, Project project, List<Meeting> meetings, List<Task> tasks, List<Decision> decisions, List<Handover> handovers, List<KnowledgeDocument> documents) {
        AiAssistantResponse response = new AiAssistantResponse();
        response.setFallbackUsed(true);

        if (question == null || question.trim().isEmpty()) {
            response.setAnswer("Please enter a question about your project, meetings, tasks, or handovers.");
            response.setConfidenceScore(100);
            return response;
        }

        String q = question.toLowerCase();
        List<AiAssistantResponse.SourceReference> sources = new ArrayList<>();

        if (q.contains("last meeting") || q.contains("previous meeting") || q.contains("what happened in the meeting")) {
            if (!meetings.isEmpty()) {
                Meeting lastMeeting = meetings.get(0);
                sources.add(new AiAssistantResponse.SourceReference("MEETING", lastMeeting.getId(), lastMeeting.getTitle(), lastMeeting.getAiSummary()));
                response.setAnswer("In the most recent meeting **" + lastMeeting.getTitle() + "** on " + lastMeeting.getScheduledStart().toLocalDate() + ":\n\n" +
                        (lastMeeting.getAiSummary() != null ? lastMeeting.getAiSummary() : lastMeeting.getAgenda()));
                response.setConfidenceScore(95);
                response.setMatchedIntent("MEETING_RECAP");
            } else {
                response.setAnswer("No meeting records were found for the selected project.");
                response.setConfidenceScore(70);
            }
        } else if (q.contains("pending for me") || q.contains("my tasks") || q.contains("what should i work on") || q.contains("my pending")) {
            List<Task> userTasks = tasks.stream()
                    .filter(t -> t.getAssignedTo() != null && t.getAssignedTo().getId().equals(user.getId()) && !"COMPLETED".equalsIgnoreCase(t.getStatus()))
                    .collect(Collectors.toList());

            if (!userTasks.isEmpty()) {
                StringBuilder ans = new StringBuilder("Here are your current active and pending tasks:\n\n");
                for (Task t : userTasks) {
                    sources.add(new AiAssistantResponse.SourceReference("TASK", t.getId(), t.getTitle(), "Status: " + t.getStatus() + ", Priority: " + t.getPriority()));
                    ans.append("- **Task #").append(t.getId()).append("**: ").append(t.getTitle())
                            .append(" [Priority: ").append(t.getPriority())
                            .append(", Status: ").append(t.getStatus())
                            .append(", Due: ").append(t.getDueDate() != null ? t.getDueDate() : "TBD").append("]\n");
                }
                ans.append("\n**Recommended focus**: Start with Task #").append(userTasks.get(0).getId()).append(" (").append(userTasks.get(0).getTitle()).append(").");
                response.setAnswer(ans.toString());
                response.setConfidenceScore(98);
                response.setMatchedIntent("USER_TASKS");
            } else {
                response.setAnswer("Great job! You have no pending tasks assigned to you right now.");
                response.setConfidenceScore(95);
            }
        } else if (q.contains("decision") || q.contains("decisions")) {
            if (!decisions.isEmpty()) {
                StringBuilder ans = new StringBuilder("Here are key architectural and project decisions on record:\n\n");
                for (Decision d : decisions.stream().limit(5).collect(Collectors.toList())) {
                    sources.add(new AiAssistantResponse.SourceReference("DECISION", d.getId(), d.getTitle(), d.getRationale()));
                    ans.append("- **").append(d.getTitle()).append("**: ").append(d.getRationale() != null ? d.getRationale() : "Approved").append("\n");
                }
                response.setAnswer(ans.toString());
                response.setConfidenceScore(92);
                response.setMatchedIntent("DECISION_LOOKUP");
            } else {
                response.setAnswer("No recorded decisions found for this project context.");
                response.setConfidenceScore(75);
            }
        } else if (q.contains("handover") || q.contains("handover report")) {
            if (!handovers.isEmpty()) {
                Handover latest = handovers.get(0);
                sources.add(new AiAssistantResponse.SourceReference("HANDOVER", latest.getId(), latest.getTitle(), latest.getReason()));
                response.setAnswer("### Latest Handover: " + latest.getTitle() + "\n\n" +
                        "- **From**: " + latest.getCurrentEmployee().getFullName() + "\n" +
                        "- **To**: " + latest.getReceivingEmployee().getFullName() + "\n" +
                        "- **Status**: " + latest.getStatus() + " (Readiness Score: " + latest.getReadinessScore() + "/100)\n" +
                        "- **Reason**: " + latest.getReason() + "\n\n" +
                        (latest.getAiGeneratedSummary() != null ? latest.getAiGeneratedSummary() : "Handover details documented."));
                response.setConfidenceScore(95);
                response.setMatchedIntent("HANDOVER_INSPECTION");
            } else {
                response.setAnswer("No employee handovers found on record.");
                response.setConfidenceScore(80);
            }
        } else if (q.contains("unresolved") || q.contains("blocker") || q.contains("blocked")) {
            List<Task> blocked = tasks.stream().filter(t -> "BLOCKED".equalsIgnoreCase(t.getStatus())).collect(Collectors.toList());
            if (!blocked.isEmpty()) {
                StringBuilder ans = new StringBuilder("Here are the currently unresolved/blocked tasks requiring attention:\n\n");
                for (Task t : blocked) {
                    sources.add(new AiAssistantResponse.SourceReference("TASK", t.getId(), t.getTitle(), "Blocked task"));
                    ans.append("- **Task #").append(t.getId()).append("**: ").append(t.getTitle())
                            .append(" (Assigned to: ").append(t.getAssignedTo() != null ? t.getAssignedTo().getFullName() : "Unassigned").append(")\n");
                }
                response.setAnswer(ans.toString());
                response.setConfidenceScore(94);
                response.setMatchedIntent("BLOCKER_DETECTION");
            } else {
                response.setAnswer("There are no tasks currently flagged as BLOCKED in this project.");
                response.setConfidenceScore(95);
            }
        } else {
            // General query fallback grounded in documents and tasks
            List<KnowledgeDocument> matchedDocs = documents.stream()
                    .filter(d -> d.getTitle().toLowerCase().contains(q) || (d.getContentText() != null && d.getContentText().toLowerCase().contains(q)))
                    .collect(Collectors.toList());

            if (!matchedDocs.isEmpty()) {
                KnowledgeDocument doc = matchedDocs.get(0);
                sources.add(new AiAssistantResponse.SourceReference("DOCUMENT", doc.getId(), doc.getTitle(), doc.getDescription()));
                response.setAnswer("Based on knowledge document **" + doc.getTitle() + "**:\n\n" +
                        (doc.getContentText() != null ? doc.getContentText().substring(0, Math.min(doc.getContentText().length(), 400)) + "..." : doc.getDescription()));
                response.setConfidenceScore(85);
                response.setMatchedIntent("KNOWLEDGE_DOC_MATCH");
            } else {
                response.setAnswer("I searched the project database, meetings, tasks, and documents, but could not find specific information matching: '" + question + "'. Please refine your question or ask about recent meetings, pending tasks, or handovers.");
                response.setConfidenceScore(50);
                response.setMatchedIntent("UNKNOWN");
            }
        }

        response.setSourceReferences(sources);
        return response;
    }

    @Override
    public KnowledgeSearchResponse searchKnowledge(String query, List<KnowledgeDocument> documents, List<DocumentChunk> chunks) {
        KnowledgeSearchResponse response = new KnowledgeSearchResponse();
        response.setQuery(query);
        response.setFallbackUsed(true);

        String q = query != null ? query.toLowerCase() : "";
        List<KnowledgeSearchResponse.SearchResultItem> results = new ArrayList<>();
        List<KnowledgeSearchResponse.SourceCitation> citations = new ArrayList<>();

        for (KnowledgeDocument doc : documents) {
            boolean titleMatch = doc.getTitle().toLowerCase().contains(q);
            boolean descMatch = doc.getDescription() != null && doc.getDescription().toLowerCase().contains(q);
            boolean contentMatch = doc.getContentText() != null && doc.getContentText().toLowerCase().contains(q);

            if (titleMatch || descMatch || contentMatch || q.isEmpty()) {
                String snippet = doc.getContentText() != null ? 
                        doc.getContentText().substring(0, Math.min(doc.getContentText().length(), 180)) + "..." : doc.getDescription();
                
                results.add(new KnowledgeSearchResponse.SearchResultItem(
                        doc.getId(),
                        doc.getTitle(),
                        doc.getCategory(),
                        doc.getFileName(),
                        snippet,
                        doc.getProject() != null ? doc.getProject().getId() : null,
                        doc.getProject() != null ? doc.getProject().getName() : "Enterprise Global"
                ));

                citations.add(new KnowledgeSearchResponse.SourceCitation(
                        doc.getTitle(),
                        doc.getCategory(),
                        "/api/documents/" + doc.getId()
                ));
            }
        }

        StringBuilder answer = new StringBuilder();
        if (!results.isEmpty()) {
            answer.append("Synthesized knowledge from ").append(results.size()).append(" matching repository documents:\n\n");
            for (KnowledgeSearchResponse.SearchResultItem item : results.stream().limit(3).collect(Collectors.toList())) {
                answer.append("• **").append(item.getTitle()).append("** (").append(item.getCategory()).append("): ").append(item.getSnippet()).append("\n");
            }
        } else {
            answer.append("No knowledge base documents directly matched the search query '").append(query).append("'. Try searching with broader keywords like 'SSO', 'Kafka', 'Rate Limiting', or 'SOP'.");
        }

        response.setSynthesizedAnswer(answer.toString());
        response.setResults(results);
        response.setCitations(citations);
        return response;
    }
}
