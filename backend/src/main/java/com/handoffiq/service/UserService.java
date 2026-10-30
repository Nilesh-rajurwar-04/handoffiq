package com.handoffiq.service;

import com.handoffiq.dto.UserProfileResponse;
import com.handoffiq.entity.Task;
import com.handoffiq.entity.User;
import com.handoffiq.exception.ResourceNotFoundException;
import com.handoffiq.repository.TaskRepository;
import com.handoffiq.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public UserService(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToProfileResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> getActiveEmployees() {
        return userRepository.findByIsActiveTrue().stream()
                .map(this::mapToProfileResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToProfileResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserProfileResponse> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getActiveEmployees();
        }
        return userRepository.searchUsers(query.trim()).stream()
                .map(this::mapToProfileResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getEmployeeWorkloadIndicators() {
        List<User> users = userRepository.findByIsActiveTrue();
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> workloadList = new ArrayList<>();

        for (User u : users) {
            List<Task> userTasks = taskRepository.findByAssignedToId(u.getId());
            long activeCount = userTasks.stream()
                    .filter(t -> !"COMPLETED".equalsIgnoreCase(t.getStatus()) && !"CANCELLED".equalsIgnoreCase(t.getStatus()))
                    .count();
            long overdueCount = userTasks.stream()
                    .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(today) && !"COMPLETED".equalsIgnoreCase(t.getStatus()) && !"CANCELLED".equalsIgnoreCase(t.getStatus()))
                    .count();
            long completedCount = userTasks.stream()
                    .filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus()))
                    .count();

            String level = activeCount >= 5 ? "OVERLOADED" :
                           activeCount >= 3 ? "HIGH" :
                           activeCount >= 1 ? "OPTIMAL" : "LIGHT";

            Map<String, Object> map = new LinkedHashMap<>();
            map.put("userId", u.getId());
            map.put("fullName", u.getFullName());
            map.put("email", u.getEmail());
            map.put("avatarUrl", u.getAvatarUrl());
            map.put("designation", u.getDesignation());
            map.put("department", u.getDepartment() != null ? u.getDepartment().getName() : "General");
            map.put("activeTasks", activeCount);
            map.put("overdueTasks", overdueCount);
            map.put("completedTasks", completedCount);
            map.put("workloadLevel", level);
            workloadList.add(map);
        }

        return workloadList;
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        UserProfileResponse resp = new UserProfileResponse();
        resp.setId(user.getId());
        resp.setFullName(user.getFullName());
        resp.setEmail(user.getEmail());
        resp.setRole(user.getRole() != null ? user.getRole().getName() : "ROLE_EMPLOYEE");
        resp.setDepartmentId(user.getDepartment() != null ? user.getDepartment().getId() : null);
        resp.setDepartmentName(user.getDepartment() != null ? user.getDepartment().getName() : "None");
        resp.setDesignation(user.getDesignation());
        resp.setAvatarUrl(user.getAvatarUrl());
        resp.setPhone(user.getPhone());
        resp.setIsActive(user.getIsActive());
        resp.setCreatedAt(user.getCreatedAt());
        return resp;
    }
}
