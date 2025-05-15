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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

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
            // Configure CSRF with cookie-based token repository
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/health-check")
            )
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
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                    .baseUri("/oauth2/authorization")
                    .authorizationRequestRepository(new HttpSessionOAuth2AuthorizationRequestRepository())
                )
                .redirectionEndpoint(redirection -> redirection
                    .baseUri("/login/oauth2/code/*")
                )
                .defaultSuccessUrl(frontendUrl, true)
            )
            // Configure session management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                .maximumSessions(1)
                .expiredUrl(frontendUrl)
            )
            // Configure request cache
            .requestCache(cache -> cache
                .requestCache(new HttpSessionRequestCache())
            )
            // Configure logout
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/api/logout"))
                .logoutSuccessUrl(frontendUrl)
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                .permitAll()
            );

        return http.build();
    }

    // We might need a CorsConfigurationSource bean later if the WebMvcConfigurer approach
    // doesn't integrate perfectly with Spring Security's CORS handling, but let's try this first.
} 