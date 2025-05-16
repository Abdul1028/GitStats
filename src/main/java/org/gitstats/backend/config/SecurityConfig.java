package org.gitstats.backend.config;

import org.gitstats.backend.security.JwtAuthenticationFilter;
import org.gitstats.backend.security.OAuth2SuccessHandler;
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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final OAuth2SuccessHandler oauth2SuccessHandler;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, OAuth2SuccessHandler oauth2SuccessHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.oauth2SuccessHandler = oauth2SuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS configured in WebConfig
            .cors(withDefaults())
            // Disable CSRF as we're using JWT
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize
                // Allow unauthenticated GET requests to public data endpoints
                .requestMatchers(HttpMethod.GET,
                    "/api/health-check",
                    "/api/users/{username}",
                    "/api/users/{username}/repos",
                    "/api/users/{username}/languages",
                    "/api/users/{username}/events",
                    "/login/oauth2/code/github",
                    "/oauth2/authorization/github"
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
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            // Configure OAuth2 login
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                    .baseUri("/oauth2/authorization")
                    .authorizationRequestRepository(new HttpSessionOAuth2AuthorizationRequestRepository())
                )
                .redirectionEndpoint(redirection -> redirection
                    .baseUri("/login/oauth2/code/*")
                )
                .successHandler(oauth2SuccessHandler)
            )
            // Configure session management to be stateless
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // Add JWT filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
} 