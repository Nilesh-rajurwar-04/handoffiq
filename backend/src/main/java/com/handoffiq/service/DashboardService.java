package com.handoffiq.service;

import com.handoffiq.dto.DashboardSummaryResponse;
import com.handoffiq.dto.TaskResponse;
import com.handoffiq.entity.ActivityLog;
import com.handoffiq.entity.Department;
import com.handoffiq.entity.Task;
import com.handoffiq.entity.User;
import com.handoffiq.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final MeetingRepository meetingRepository;
    private final TaskRepository taskRepository;
    private final HandoverRepository handoverRepository;
    private final DepartmentRepository departmentRepository;
    private final ActivityLogRepository activityLogRepository;
    private final TaskService taskService;

    public DashboardService(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            MeetingRepository meetingRepository,
            TaskRepository taskRepository,
            HandoverRepository handoverRepository,
            DepartmentRepository departmentRepository,
            ActivityLogRepository activityLogRepository,
            TaskService taskService) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.meetingRepository = meetingRepository;
        this.taskRepository = taskRepository;
        this.handoverRepository = handoverRepository;
        this.departmentRepository = departmentRepository;
        this.activityLogRepository = activityLogRepository;
        this.taskService = taskService;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary(Long currentUserId) {
        LocalDate today = LocalDate.now();

        DashboardSummaryResponse response = new DashboardSummaryResponse();

        // 1. High-level KPI counts
        response.setTotalEmployees(userRepository.countByIsActiveTrue());
        response.setActiveProjects(projectRepository.countByStatus("ACTIVE"));
        response.setTotalMeetings(meetingRepository.count());
        
        long completed = taskRepository.countByStatus("COMPLETED");
        long todo = taskRepository.countByStatus("TODO");
        long inProgress = taskRepository.countByStatus("IN_PROGRESS");
        long blocked = taskRepository.countByStatus("BLOCKED");
        long cancelled = taskRepository.countByStatus("CANCELLED");
        long pending = todo + inProgress + blocked;

        response.setCompletedTasks(completed);
        response.setPendingTasks(pending);
        response.setOverdueTasks(taskRepository.countOverdueTasks(today));
        response.setActiveHandovers(handoverRepository.countByStatus("AWAITING_ACCEPTANCE") + handoverRepository.countByStatus("AWAITING_REVIEW") + handoverRepository.countByStatus("DRAFT"));

        // 2. My Priority Today
        List<Task> priorityTasks = currentUserId != null ? 
                taskRepository.findPriorityTodayByUserId(currentUserId, today) : List.of();
        List<TaskResponse> myPriorityList = priorityTasks.stream()
                .limit(5)
                .map(taskService::mapToTaskResponse)
                .collect(Collectors.toList());
        response.setMyPriorityToday(myPriorityList);

        // 3. Task Status Distribution (for Pie/Donut Chart)
        List<DashboardSummaryResponse.ChartDataPoint> statusDist = List.of(
                new DashboardSummaryResponse.ChartDataPoint("Completed", completed, "#10B981"),
                new DashboardSummaryResponse.ChartDataPoint("In Progress", inProgress, "#3B82F6"),
                new DashboardSummaryResponse.ChartDataPoint("To Do", todo, "#8B5CF6"),
                new DashboardSummaryResponse.ChartDataPoint("Blocked", blocked, "#EF4444"),
                new DashboardSummaryResponse.ChartDataPoint("Cancelled", cancelled, "#9CA3AF")
        );
        response.setTaskStatusDistribution(statusDist);

        // 4. Pending vs Completed
        List<DashboardSummaryResponse.ChartDataPoint> pendingVsComp = List.of(
                new DashboardSummaryResponse.ChartDataPoint("Completed", completed, "#10B981"),
                new DashboardSummaryResponse.ChartDataPoint("Pending Execution", pending, "#F59E0B")
        );
        response.setPendingVsCompleted(pendingVsComp);

        // 5. Weekly Task Activity (Mon-Fri trend)
        List<DashboardSummaryResponse.WeeklyActivityDataPoint> weeklyActivity = List.of(
                new DashboardSummaryResponse.WeeklyActivityDataPoint("Mon", 4, 3),
                new DashboardSummaryResponse.WeeklyActivityDataPoint("Tue", 6, 5),
                new DashboardSummaryResponse.WeeklyActivityDataPoint("Wed", 5, 4),
                new DashboardSummaryResponse.WeeklyActivityDataPoint("Thu", 8, 7),
                new DashboardSummaryResponse.WeeklyActivityDataPoint("Fri", 6, 8)
        );
        response.setWeeklyTaskActivity(weeklyActivity);

        // 6. Department Workload
        List<DashboardSummaryResponse.DepartmentWorkloadDataPoint> deptWorkload = new ArrayList<>();
        List<Department> departments = departmentRepository.findAll();
        for (Department d : departments) {
            List<User> deptUsers = userRepository.findByDepartmentId(d.getId());
            int activeCount = 0;
            int compCount = 0;
            for (User u : deptUsers) {
                List<Task> uTasks = taskRepository.findByAssignedToId(u.getId());
                activeCount += (int) uTasks.stream().filter(t -> !"COMPLETED".equalsIgnoreCase(t.getStatus()) && !"CANCELLED".equalsIgnoreCase(t.getStatus())).count();
                compCount += (int) uTasks.stream().filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus())).count();
            }
            deptWorkload.add(new DashboardSummaryResponse.DepartmentWorkloadDataPoint(
                    d.getName(), activeCount, compCount, deptUsers.size()
            ));
        }
        response.setDepartmentWorkload(deptWorkload);

        // 7. Recent Activities
        List<ActivityLog> logs = activityLogRepository.findTop20ByOrderByCreatedAtDesc();
        List<DashboardSummaryResponse.ActivityLogDto> activityDtos = logs.stream().map(l ->
                new DashboardSummaryResponse.ActivityLogDto(
                        l.getId(),
                        l.getUser() != null ? l.getUser().getFullName() : "System",
                        l.getUser() != null ? l.getUser().getAvatarUrl() : null,
                        l.getAction(),
                        l.getEntityType(),
                        l.getEntityId(),
                        l.getDetails(),
                        l.getCreatedAt() != null ? l.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")) : "Just now"
                )).collect(Collectors.toList());
        response.setRecentActivities(activityDtos);

        return response;
    }
}
