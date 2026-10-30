package com.handoffiq.ai;

import com.handoffiq.dto.*;
import com.handoffiq.entity.*;

import java.util.List;

public interface AiServiceProvider {
    MeetingAnalysisResponse analyzeMeeting(String rawNotes, Long projectId);
    HandoverSummaryResponse generateHandoverSummary(Handover handover, List<Task> tasks, List<Decision> decisions, List<KnowledgeDocument> docs);
    CatchUpBriefResponse generateCatchUpBrief(User user, Project project, List<Meeting> recentMeetings, List<Task> userTasks, List<Task> projectTasks, List<Decision> decisions);
    AiAssistantResponse answerQuestion(String question, User user, Project project, List<Meeting> meetings, List<Task> tasks, List<Decision> decisions, List<Handover> handovers, List<KnowledgeDocument> documents);
    KnowledgeSearchResponse searchKnowledge(String query, List<KnowledgeDocument> documents, List<DocumentChunk> chunks);
}
