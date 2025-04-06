package org.gitstats.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS configured in WebConfig
            .cors(withDefaults())
            // Disable CSRF for stateless APIs (if applicable, adjust if using sessions)
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize
                // Allow unauthenticated GET requests to public user info, repos, and language endpoints
                .requestMatchers(HttpMethod.GET,
                    "/api/users/{username}",
                    "/api/users/{username}/repos",
                    "/api/users/{username}/languages"
                ).permitAll()
                 // Allow unauthenticated OPTIONS requests (used for CORS preflight)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // All other requests require authentication (we'll configure login later)
                .anyRequest().authenticated()
            )
            // For now, add basic form login to handle the .anyRequest().authenticated() part
            // We will replace this with proper OAuth2 login later
            .formLogin(withDefaults());
            // Or use httpBasic for simple testing if preferred
            // .httpBasic(withDefaults());

        return http.build();
    }

    // We might need a CorsConfigurationSource bean later if the WebMvcConfigurer approach
    // doesn't integrate perfectly with Spring Security's CORS handling, but let's try this first.
} 