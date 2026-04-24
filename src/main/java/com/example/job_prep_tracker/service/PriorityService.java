package com.example.job_prep_tracker.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class PriorityService {

    public String calculatePriority(LocalDateTime deadline) {
        long hoursUntil = ChronoUnit.HOURS.between(LocalDateTime.now(), deadline);

        if (hoursUntil < 24) return "HIGH";
        if (hoursUntil < 72) return "MEDIUM";
        return "LOW";
    }
}