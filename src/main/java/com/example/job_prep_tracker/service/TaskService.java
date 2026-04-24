package com.example.job_prep_tracker.service;

import com.example.job_prep_tracker.entity.Task;
import com.example.job_prep_tracker.entity.User;
import com.example.job_prep_tracker.repository.TaskRepository;
import com.example.job_prep_tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    @Autowired
    private GmailTaskExtractor gmailExtractor;  // Use your extractor

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public int syncEmailsForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        try {
            // Use your GmailTaskExtractor
            List<Task> newTasks = gmailExtractor.extractTasks(
                    user.getAccessToken(),
                    user.getId()
            );

            // Filter out already processed emails
            List<Task> tasksToSave = newTasks.stream()
                    .filter(task -> !taskRepository.existsByGmailMessageId(task.getGmailMessageId()))
                    .toList();

            taskRepository.saveAll(tasksToSave);
            return tasksToSave.size();

        } catch (Exception e) {
            throw new RuntimeException("Failed to sync Gmail: " + e.getMessage(), e);
        }
    }

    public List<Task> getTasksForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return taskRepository.findByUserIdAndStatusOrderByDeadlineAsc(
                user.getId(),
                "PENDING"
        );
    }

    @Transactional
    public void markComplete(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setStatus("COMPLETED");
        task.setCompletedAt(LocalDateTime.now());
        taskRepository.save(task);
    }
}