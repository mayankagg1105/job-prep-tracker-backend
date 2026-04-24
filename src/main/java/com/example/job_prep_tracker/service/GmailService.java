package com.example.job_prep_tracker.service;

import com.example.job_prep_tracker.entity.Task;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class GmailService {

    public static void sendReminder(Task task) {
        // For now, just log to console
        System.out.println("===== REMINDER =====");
        System.out.println("Task: " + task.getTaskType() + " with " + task.getCompanyName());
        System.out.println("When: " + task.getDeadline().format(
                DateTimeFormatter.ofPattern("EEEE, MMMM d 'at' h:mm a")
        ));
        System.out.println("Subject: " + task.getSubject());
        if (task.getMeetingLink() != null) {
            System.out.println("Link: " + task.getMeetingLink());
        }
        System.out.println("====================");
    }
}