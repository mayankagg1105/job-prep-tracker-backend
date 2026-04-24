package com.example.job_prep_tracker.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.persistence.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(generator = "UUID")
    private UUID id;
    private String email;
    private String googleId;
    @Column(length = 2048)
    private String accessToken;
    @Column(length = 512)
    private String refreshToken;
    private LocalDateTime tokenExpiresAt;
    private LocalDateTime createdAt = LocalDateTime.now();
}