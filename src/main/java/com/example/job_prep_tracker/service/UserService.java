package com.example.job_prep_tracker.service;

import com.example.job_prep_tracker.entity.User;
import com.example.job_prep_tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public User saveOAuthUser(String email, String googleId, OAuth2AuthorizedClient client) {
        User user = userRepository.findByEmail(email)
                .orElse(new User());

        user.setEmail(email);
        user.setGoogleId(googleId);

        // Extract tokens
        OAuth2AccessToken accessToken = client.getAccessToken();
        OAuth2RefreshToken refreshToken = client.getRefreshToken();

        user.setAccessToken(accessToken.getTokenValue());

        if (refreshToken != null) {
            user.setRefreshToken(refreshToken.getTokenValue());
        }

        // Set expiration
        if (accessToken.getExpiresAt() != null) {
            Instant expiresAt = accessToken.getExpiresAt();
            user.setTokenExpiresAt(
                    LocalDateTime.ofInstant(expiresAt, ZoneId.systemDefault())
            );
        }

        return userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}