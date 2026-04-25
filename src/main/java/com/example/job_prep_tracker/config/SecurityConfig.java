package com.example.job_prep_tracker.config;

import com.example.job_prep_tracker.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;

    // ✅ ONLY inject UserService (no circular dependency now)
    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    // ✅ Bean for OAuth2AuthorizedClientService
    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
            ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/error",
                                "/login/**",
                                "/oauth2/**"
                        ).permitAll()
                        .requestMatchers("/api/**", "/job/**").authenticated()
                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth -> oauth
                        // ✅ Injected properly via method
                        .successHandler(authenticationSuccessHandler(authorizedClientService(null)))
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"Unauthorized\"}");
                        })
                );

        return http.build();
    }

    // ✅ METHOD INJECTION (no constructor dependency)
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler(
            OAuth2AuthorizedClientService authorizedClientService) {

        return (request, response, authentication) -> {

            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            String email = oauth2User.getAttribute("email");
            String googleId = oauth2User.getAttribute("sub");

            OAuth2AuthorizedClient client =
                    authorizedClientService.loadAuthorizedClient(
                            "google",
                            authentication.getName()
                    );

            if (client != null) {
                userService.saveOAuthUser(email, googleId, client);
            }

            // ⚠️ CHANGE THIS for production
            response.sendRedirect("http://localhost:5173/dashboard");
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}