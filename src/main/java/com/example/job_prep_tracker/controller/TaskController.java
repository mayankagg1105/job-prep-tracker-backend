package com.example.job_prep_tracker.controller;

import com.example.job_prep_tracker.entity.Task;
import com.example.job_prep_tracker.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "https://job-prep-tracker-seven.vercel.app", allowCredentials = "true")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping("/hello")
    public String hello(){
        return "hello mayank and anmol ";
    }

    @PostMapping("/sync")
    public Map<String, Object> syncGmail(@AuthenticationPrincipal OAuth2User principal) {
        String email = principal.getAttribute("email");
        int tasksFound = taskService.syncEmailsForUser(email);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("tasksFound", tasksFound);
        return response;
    }

    @GetMapping
    public List<Task> getTasks(@AuthenticationPrincipal OAuth2User principal) {
        String email = principal.getAttribute("email");
        return taskService.getTasksForUser(email);
    }

    @PatchMapping("/{taskId}/complete")
    public void completeTask(@PathVariable UUID taskId) {
        taskService.markComplete(taskId);
    }
}