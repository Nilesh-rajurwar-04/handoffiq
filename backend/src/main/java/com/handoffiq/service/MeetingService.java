package com.handoffiq.service;

import com.handoffiq.dto.CreateMeetingRequest;
import com.handoffiq.dto.MeetingResponse;
import com.handoffiq.entity.*;
import com.handoffiq.exception.BadRequestException;
import com.handoffiq.exception.ResourceNotFoundException;
import com.handoffiq.logging.StructuredLogger;
import com.handoffiq.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final MeetingParticipantRepository participantRepository;
    private final DecisionRepository decisionRepository;
    private final TaskRepository taskRepository;
    private final StructuredLogger structuredLogger;

    public MeetingService(
            MeetingRepository meetingRepository,
            ProjectRepository projectRepository,
            UserRepository userRepository,
            MeetingParticipantRepository participantRepository,
            DecisionRepository decisionRepository,
            TaskRepository taskRepository,
            StructuredLogger structuredLogger) {
        this.meetingRepository = meetingRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
        this.decisionRepository = decisionRepository;
        this.taskRepository = taskRepository;
        this.structuredLogger = structuredLogger;
    }

    @Transactional
    public MeetingResponse createMeeting(CreateMeetingRequest request, Long userId) {
        long start = System.currentTimeMillis();

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.getProjectId()));

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Meeting meeting = new Meeting();
        meeting.setTitle(request.getTitle().trim());
        meeting.setProject(project);
        meeting.setCreatedBy(creator);
        meeting.setScheduledStart(request.getScheduledStart());
        meeting.setScheduledEnd(request.getScheduledEnd());
        meeting.setLocation(request.getLocation());
        meeting.setAgenda(request.getAgenda());
        meeting.setRawNotes(request.getRawNotes());
        meeting.setStatus("SCHEDULED");

        Meeting saved = meetingRepository.save(meeting);

        // Add creator as participant
        participantRepository.save(new MeetingParticipant(saved, creator, "ATTENDED"));

        // Add invited participants
        if (request.getParticipantIds() != null) {
            for (Long pId : request.getParticipantIds()) {
                if (!pId.equals(userId)) {
                    userRepository.findById(pId).ifPresent(pUser ->
                            participantRepository.save(new MeetingParticipant(saved, pUser, "INVITED")));
                }
            }
        }

        structuredLogger.logDomainEvent("MEETING_CREATE", userId, "SUCCESS", System.currentTimeMillis() - start, "Meeting created: " + saved.getTitle());
        return mapToMeetingResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MeetingResponse> getAllMeetings() {
        return meetingRepository.findAllByOrderByScheduledStartDesc().stream()
                .map(this::mapToMeetingResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MeetingResponse> getMeetingsByProject(Long projectId) {
        return meetingRepository.findByProjectIdOrderByScheduledStartDesc(projectId).stream()
                .map(this::mapToMeetingResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MeetingResponse getMeetingById(Long id) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + id));
        return mapToMeetingResponse(meeting);
    }

    @Transactional
    public MeetingResponse updateNotes(Long id, String rawNotes, String aiSummary, Long userId) {
        long start = System.currentTimeMillis();
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + id));

        if (rawNotes != null) meeting.setRawNotes(rawNotes);
        if (aiSummary != null) meeting.setAiSummary(aiSummary);

        Meeting updated = meetingRepository.save(meeting);
        structuredLogger.logDomainEvent("MEETING_UPDATE_NOTES", userId, "SUCCESS", System.currentTimeMillis() - start, "Meeting notes updated: " + updated.getTitle());
        return mapToMeetingResponse(updated);
    }

    @Transactional
    public MeetingResponse updateStatus(Long id, String status, Long userId) {
        long start = System.currentTimeMillis();
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + id));

        meeting.setStatus(status.toUpperCase());
        Meeting updated = meetingRepository.save(meeting);
        structuredLogger.logDomainEvent("MEETING_UPDATE_STATUS", userId, "SUCCESS", System.currentTimeMillis() - start, "Meeting status changed to " + status + " for " + updated.getTitle());
        return mapToMeetingResponse(updated);
    }

    @Transactional
    public void deleteMeeting(Long id, Long userId) {
        long start = System.currentTimeMillis();
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meeting not found with id: " + id));

        meetingRepository.delete(meeting);
        structuredLogger.logDomainEvent("MEETING_DELETE", userId, "SUCCESS", System.currentTimeMillis() - start, "Deleted meeting #" + id);
    }

    private MeetingResponse mapToMeetingResponse(Meeting m) {
        MeetingResponse resp = new MeetingResponse();
        resp.setId(m.getId());
        resp.setTitle(m.getTitle());
        resp.setProjectId(m.getProject() != null ? m.getProject().getId() : null);
        resp.setProjectName(m.getProject() != null ? m.getProject().getName() : null);
        resp.setProjectCode(m.getProject() != null ? m.getProject().getCode() : null);
        resp.setCreatedById(m.getCreatedBy() != null ? m.getCreatedBy().getId() : null);
        resp.setCreatedByName(m.getCreatedBy() != null ? m.getCreatedBy().getFullName() : null);
        resp.setScheduledStart(m.getScheduledStart());
        resp.setScheduledEnd(m.getScheduledEnd());
        resp.setStatus(m.getStatus());
        resp.setLocation(m.getLocation());
        resp.setAgenda(m.getAgenda());
        resp.setRawNotes(m.getRawNotes());
        resp.setAiSummary(m.getAiSummary());
        resp.setKeyDiscussionPoints(m.getKeyDiscussionPoints());
        resp.setCreatedAt(m.getCreatedAt());

        // Participants
        List<MeetingParticipant> participants = participantRepository.findByMeetingId(m.getId());
        List<MeetingResponse.ParticipantDto> participantDtos = participants.stream().map(p ->
                new MeetingResponse.ParticipantDto(
                        p.getUser().getId(),
                        p.getUser().getFullName(),
                        p.getUser().getEmail(),
                        p.getUser().getAvatarUrl(),
                        p.getAttendanceStatus()
                )).collect(Collectors.toList());
        resp.setParticipants(participantDtos);

        // Decisions
        List<Decision> decisions = decisionRepository.findByMeetingId(m.getId());
        List<MeetingResponse.DecisionDto> decisionDtos = decisions.stream().map(d ->
                new MeetingResponse.DecisionDto(
                        d.getId(),
                        d.getTitle(),
                        d.getRationale(),
                        d.getStatus(),
                        d.getDecidedBy() != null ? d.getDecidedBy().getFullName() : "Decided"
                )).collect(Collectors.toList());
        resp.setDecisions(decisionDtos);

        // Tasks
        List<Task> tasks = taskRepository.findByRelatedMeetingId(m.getId());
        List<MeetingResponse.TaskSummaryDto> taskDtos = tasks.stream().map(t ->
                new MeetingResponse.TaskSummaryDto(
                        t.getId(),
                        t.getTitle(),
                        t.getStatus(),
                        t.getPriority(),
                        t.getAssignedTo() != null ? t.getAssignedTo().getFullName() : "Unassigned"
                )).collect(Collectors.toList());
        resp.setTasks(taskDtos);

        return resp;
    }
}
