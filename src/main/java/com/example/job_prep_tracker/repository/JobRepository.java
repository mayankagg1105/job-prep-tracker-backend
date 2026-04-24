package com.example.job_prep_tracker.repository;

import com.example.job_prep_tracker.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {
}
