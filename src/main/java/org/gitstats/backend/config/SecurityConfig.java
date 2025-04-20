package org.gitstats.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS configured in WebConfig
            .cors(withDefaults())
            // Disable CSRF for stateless APIs (if applicable, adjust if using sessions)
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize
                // Allow unauthenticated GET requests to public data endpoints
                .requestMatchers(HttpMethod.GET,
                    "/api/users/{username}",
                    "/api/users/{username}/repos",
                    "/api/users/{username}/languages",
                    "/api/users/{username}/events"
                ).permitAll()
                 // Allow unauthenticated OPTIONS requests (used for CORS preflight)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // Authenticated user endpoint
                .requestMatchers("/api/user/me").authenticated()
                // Any other request must be authenticated
                .anyRequest().authenticated()
            )
            // Configure exception handling for authentication
            .exceptionHandling(exceptions -> exceptions
                // For unauthenticated requests, return 401 Unauthorized instead of redirecting
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            // Replace formLogin with oauth2Login
            // .formLogin(withDefaults());
            .oauth2Login(oauth2 -> oauth2
                // Always redirect to the configured frontend app root on successful login
                .defaultSuccessUrl(frontendUrl, true)
                // We could add custom failure handler later if needed
                // .failureUrl("/login?error")
            )
            // --- Refined Logout Configuration --- 
            .logout(logout -> logout
                 // Specify the URL to redirect to after logout
                .logoutSuccessUrl(frontendUrl)
                 // Invalidate session and clear authentication on logout
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                 // Delete cookies on logout
                .deleteCookies("JSESSIONID") 
                // Explicitly permit all access to the default /logout URL (GET or POST)
                .permitAll() 
            );

        return http.build();
    }

    // We might need a CorsConfigurationSource bean later if the WebMvcConfigurer approach
    // doesn't integrate perfectly with Spring Security's CORS handling, but let's try this first.
} 