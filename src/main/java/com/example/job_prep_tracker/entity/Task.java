package com.example.job_prep_tracker.entity;

import jakarta.persistence.*;
import lombok.Data;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@Data
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;



    private String companyName;
    private String taskType; // INTERVIEW, TEST, ASSIGNMENT
    private String subject;
    private LocalDateTime deadline;
    private String status = "PENDING";
    private String priority;
    private String meetingLink;
    @Column(length = 500)
    private String emailSnippet;
    private LocalDateTime remindedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(unique = true)
    private String gmailMessageId;
}