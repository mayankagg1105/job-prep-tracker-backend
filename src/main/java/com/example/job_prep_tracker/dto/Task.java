package com.example.job_prep_tracker.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Task {
    private String subject;
    private String companyName;
    private String taskType;
    private LocalDateTime deadline;
    private String meetingLink;
    private String emailSnippet;

    public static Object builder() {
        return null;
    }
}
