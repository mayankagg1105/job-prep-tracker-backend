package com.example.job_prep_tracker.scheduler;

import com.example.job_prep_tracker.entity.Task; // Use Entity, not DTO for DB operations
import com.example.job_prep_tracker.repository.TaskRepository;
import com.example.job_prep_tracker.service.GmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling // Required to enable scheduling tasks [cite: 1207]
public class ReminderScheduler {

    @Autowired
    private TaskRepository taskRepository; // Inject the repository [cite: 1210]

    @Autowired
    private GmailService emailService; // Inject the email service [cite: 1211]

    @Scheduled(cron = "0 0 9,15 * * *") // Runs at 9 AM and 3 PM [cite: 183, 1213-1221]
    public void sendReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderWindow = now.plusHours(24);

        // Use the injected instance 'taskRepository' (lowercase)
        List<Task> upcomingTasks = taskRepository
                .findByStatusAndDeadlineBetweenAndRemindedAtIsNull(
                        "PENDING",
                        now,
                        reminderWindow
                );

        upcomingTasks.forEach(task -> {
            try {
                GmailService.sendReminder(task);
                task.setRemindedAt(now);
            } catch (Exception e) {
                // Log error but continue with other tasks [cite: 1238-1240]
            }
        });

        taskRepository.saveAll(upcomingTasks);
    }
}