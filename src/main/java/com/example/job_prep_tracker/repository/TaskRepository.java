package com.example.job_prep_tracker.repository;

import com.example.job_prep_tracker.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    boolean existsByGmailMessageId(String gmailMessageId);

    List<Task> findByUserIdAndStatusOrderByDeadlineAsc(UUID userId, String status);

    List<Task> findByStatusAndDeadlineBetween(
            String status,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Task> findByStatusAndDeadlineBetweenAndRemindedAtIsNull(
            String status,
            LocalDateTime start,
            LocalDateTime end
    );
}
