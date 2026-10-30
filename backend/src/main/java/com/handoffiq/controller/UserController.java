package com.handoffiq.controller;

import com.handoffiq.dto.UserProfileResponse;
import com.handoffiq.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Employees", description = "Employee directory and workload distribution metrics")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "List all active employees")
    public ResponseEntity<List<UserProfileResponse>> getUsers(@RequestParam(required = false) String search) {
        List<UserProfileResponse> users = (search != null && !search.trim().isEmpty()) ?
                userService.searchUsers(search) : userService.getActiveEmployees();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee profile by ID")
    public ResponseEntity<UserProfileResponse> getUserById(@PathVariable Long id) {
        UserProfileResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/workload")
    @Operation(summary = "Get employee workload indicator (active vs overdue tasks per employee)")
    public ResponseEntity<List<Map<String, Object>>> getWorkloadIndicators() {
        List<Map<String, Object>> workload = userService.getEmployeeWorkloadIndicators();
        return ResponseEntity.ok(workload);
    }
}
