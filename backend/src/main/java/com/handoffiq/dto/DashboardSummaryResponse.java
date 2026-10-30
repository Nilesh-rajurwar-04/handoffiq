package com.handoffiq.dto;

import java.util.List;
import java.util.Map;

public class DashboardSummaryResponse {

    private long totalEmployees;
    private long activeProjects;
    private long totalMeetings;
    private long pendingTasks;
    private long completedTasks;
    private long overdueTasks;
    private long activeHandovers;
    private List<TaskResponse> myPriorityToday;
    private List<ChartDataPoint> taskStatusDistribution;
    private List<ChartDataPoint> pendingVsCompleted;
    private List<WeeklyActivityDataPoint> weeklyTaskActivity;
    private List<DepartmentWorkloadDataPoint> departmentWorkload;
    private List<ActivityLogDto> recentActivities;

    public DashboardSummaryResponse() {}

    public static class ChartDataPoint {
        private String name;
        private long value;
        private String color;

        public ChartDataPoint() {}
        public ChartDataPoint(String name, long value, String color) {
            this.name = name;
            this.value = value;
            this.color = color;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public long getValue() { return value; }
        public void setValue(long value) { this.value = value; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }

    public static class WeeklyActivityDataPoint {
        private String day;
        private int created;
        private int completed;

        public WeeklyActivityDataPoint() {}
        public WeeklyActivityDataPoint(String day, int created, int completed) {
            this.day = day;
            this.created = created;
            this.completed = completed;
        }

        public String getDay() { return day; }
        public void setDay(String day) { this.day = day; }
        public int getCreated() { return created; }
        public void setCreated(int created) { this.created = created; }
        public int getCompleted() { return completed; }
        public void setCompleted(int completed) { this.completed = completed; }
    }

    public static class DepartmentWorkloadDataPoint {
        private String department;
        private int activeTasks;
        private int completedTasks;
        private int employees;

        public DepartmentWorkloadDataPoint() {}
        public DepartmentWorkloadDataPoint(String department, int activeTasks, int completedTasks, int employees) {
            this.department = department;
            this.activeTasks = activeTasks;
            this.completedTasks = completedTasks;
            this.employees = employees;
        }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public int getActiveTasks() { return activeTasks; }
        public void setActiveTasks(int activeTasks) { this.activeTasks = activeTasks; }
        public int getCompletedTasks() { return completedTasks; }
        public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }
        public int getEmployees() { return employees; }
        public void setEmployees(int employees) { this.employees = employees; }
    }

    public static class ActivityLogDto {
        private Long id;
        private String userName;
        private String userAvatar;
        private String action;
        private String entityType;
        private Long entityId;
        private String details;
        private String timestamp;

        public ActivityLogDto() {}
        public ActivityLogDto(Long id, String userName, String userAvatar, String action, String entityType, Long entityId, String details, String timestamp) {
            this.id = id;
            this.userName = userName;
            this.userAvatar = userAvatar;
            this.action = action;
            this.entityType = entityType;
            this.entityId = entityId;
            this.details = details;
            this.timestamp = timestamp;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
        public String getUserAvatar() { return userAvatar; }
        public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getEntityType() { return entityType; }
        public void setEntityType(String entityType) { this.entityType = entityType; }
        public Long getEntityId() { return entityId; }
        public void setEntityId(Long entityId) { this.entityId = entityId; }
        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    }

    public long getTotalEmployees() { return totalEmployees; }
    public void setTotalEmployees(long totalEmployees) { this.totalEmployees = totalEmployees; }
    public long getActiveProjects() { return activeProjects; }
    public void setActiveProjects(long activeProjects) { this.activeProjects = activeProjects; }
    public long getTotalMeetings() { return totalMeetings; }
    public void setTotalMeetings(long totalMeetings) { this.totalMeetings = totalMeetings; }
    public long getPendingTasks() { return pendingTasks; }
    public void setPendingTasks(long pendingTasks) { this.pendingTasks = pendingTasks; }
    public long getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(long completedTasks) { this.completedTasks = completedTasks; }
    public long getOverdueTasks() { return overdueTasks; }
    public void setOverdueTasks(long overdueTasks) { this.overdueTasks = overdueTasks; }
    public long getActiveHandovers() { return activeHandovers; }
    public void setActiveHandovers(long activeHandovers) { this.activeHandovers = activeHandovers; }
    public List<TaskResponse> getMyPriorityToday() { return myPriorityToday; }
    public void setMyPriorityToday(List<TaskResponse> myPriorityToday) { this.myPriorityToday = myPriorityToday; }
    public List<ChartDataPoint> getTaskStatusDistribution() { return taskStatusDistribution; }
    public void setTaskStatusDistribution(List<ChartDataPoint> taskStatusDistribution) { this.taskStatusDistribution = taskStatusDistribution; }
    public List<ChartDataPoint> getPendingVsCompleted() { return pendingVsCompleted; }
    public void setPendingVsCompleted(List<ChartDataPoint> pendingVsCompleted) { this.pendingVsCompleted = pendingVsCompleted; }
    public List<WeeklyActivityDataPoint> getWeeklyTaskActivity() { return weeklyTaskActivity; }
    public void setWeeklyTaskActivity(List<WeeklyActivityDataPoint> weeklyTaskActivity) { this.weeklyTaskActivity = weeklyTaskActivity; }
    public List<DepartmentWorkloadDataPoint> getDepartmentWorkload() { return departmentWorkload; }
    public void setDepartmentWorkload(List<DepartmentWorkloadDataPoint> departmentWorkload) { this.departmentWorkload = departmentWorkload; }
    public List<ActivityLogDto> getRecentActivities() { return recentActivities; }
    public void setRecentActivities(List<ActivityLogDto> recentActivities) { this.recentActivities = recentActivities; }
}
