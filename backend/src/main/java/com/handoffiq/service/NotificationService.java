package com.handoffiq.service;

import com.handoffiq.dto.NotificationResponse;
import com.handoffiq.entity.Handover;
import com.handoffiq.entity.Notification;
import com.handoffiq.entity.Task;
import com.handoffiq.entity.User;
import com.handoffiq.exception.ResourceNotFoundException;
import com.handoffiq.repository.HandoverRepository;
import com.handoffiq.repository.NotificationRepository;
import com.handoffiq.repository.TaskRepository;
import com.handoffiq.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final TaskRepository taskRepository;
    private final HandoverRepository handoverRepository;
    private final UserRepository userRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            TaskRepository taskRepository,
            HandoverRepository handoverRepository,
            UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.taskRepository = taskRepository;
        this.handoverRepository = handoverRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        notif.setIsRead(true);
        notificationRepository.save(notif);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    /**
     * Intelligent Follow-up Detection Engine:
     * Returns actionable follow-ups across the organization for managers.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getPendingFollowUps() {
        LocalDate today = LocalDate.now();

        List<Task> overdueTasks = taskRepository.findOverdueTasks(today);
        List<Task> unassignedTasks = taskRepository.findUnassignedTasks();
        List<Task> blockedTasks = taskRepository.findBlockedTasks();
        List<Handover> pendingHandovers = handoverRepository.findByStatus("AWAITING_ACCEPTANCE");

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("overdueTasksCount", overdueTasks.size());
        result.put("overdueTasks", overdueTasks);
        result.put("unassignedTasksCount", unassignedTasks.size());
        result.put("unassignedTasks", unassignedTasks);
        result.put("blockedTasksCount", blockedTasks.size());
        result.put("blockedTasks", blockedTasks);
        result.put("pendingHandoversCount", pendingHandovers.size());
        result.put("pendingHandovers", pendingHandovers);

        return result;
    }

    /**
     * Periodic background scanner for overdue and blocked tasks.
     * Runs every 10 minutes to verify alerts.
     */
    @Scheduled(fixedRate = 600000)
    @Transactional
    public void scanAndGenerateIntelligentAlerts() {
        log.info("Running scheduled intelligent task follow-up & alert scanner");
        LocalDate today = LocalDate.now();

        // 1. Overdue task alerts
        List<Task> overdueTasks = taskRepository.findOverdueTasks(today);
        for (Task t : overdueTasks) {
            if (t.getAssignedTo() != null) {
                String title = "Overdue Task Alert: #" + t.getId();
                String msg = "Task '" + t.getTitle() + "' was due on " + t.getDueDate() + " and is still pending.";
                // Send if not already alerted
                createAlertIfNotExists(t.getAssignedTo(), title, msg, "OVERDUE_TASK", "TASK", t.getId());
            }
        }

        // 2. Blocked task alerts
        List<Task> blockedTasks = taskRepository.findBlockedTasks();
        for (Task t : blockedTasks) {
            if (t.getCreatedBy() != null) {
                String title = "Blocked Task Notification: #" + t.getId();
                String msg = "Task '" + t.getTitle() + "' has been marked as BLOCKED and requires escalation.";
                createAlertIfNotExists(t.getCreatedBy(), title, msg, "BLOCKED_TASK", "TASK", t.getId());
            }
        }
    }

    private void createAlertIfNotExists(User user, String title, String msg, String type, String refType, Long refId) {
        notificationRepository.save(new Notification(user, title, msg, type, refType, refId));
    }

    private NotificationResponse mapToResponse(Notification n) {
        NotificationResponse resp = new NotificationResponse();
        resp.setId(n.getId());
        resp.setUserId(n.getUser() != null ? n.getUser().getId() : null);
        resp.setTitle(n.getTitle());
        resp.setMessage(n.getMessage());
        resp.setNotificationType(n.getNotificationType());
        resp.setIsRead(n.getIsRead());
        resp.setReferenceType(n.getReferenceType());
        resp.setReferenceId(n.getReferenceId());
        resp.setCreatedAt(n.getCreatedAt());
        return resp;
    }
}
