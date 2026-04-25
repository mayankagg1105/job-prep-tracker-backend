package com.example.job_prep_tracker.security;
import com.example.job_prep_tracker.entity.User;
import com.example.job_prep_tracker.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;

@Component
public class OAuth2LoginSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;

    public OAuth2LoginSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String googleId = oauthUser.getAttribute("sub");

        userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User();
            user.setEmail(email);
            user.setGoogleId(googleId);
            user.setCreatedAt(LocalDateTime.from(Instant.now()));
            return userRepository.save(user);
        });

        // redirect to frontend
//        response.sendRedirect("http://localhost:5173/dashboard");
        response.sendRedirect("https://job-prep-tracker-frontend.onrender.com/dashboard");
    }
}
