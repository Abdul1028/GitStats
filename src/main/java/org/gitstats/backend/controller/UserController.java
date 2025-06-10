package org.gitstats.backend.controller;

import java.util.List;
import java.util.Map;

import org.gitstats.backend.dto.GitHubEventDTO;
import org.gitstats.backend.dto.GitHubRepoDTO;
import org.gitstats.backend.dto.GitHubUserDTO;
import org.gitstats.backend.service.GitHubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

@RestController
@RequestMapping("/api")
public class UserController {

    private final GitHubService gitHubService;

    @Autowired
    public UserController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    // Helper method to extract token
    private String extractToken(String authHeader) {
        System.out.println("check");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            System.out.println("Token extracted: " + authHeader.substring(7));
            return authHeader.substring(7);

        }
        return null;
    }

    @GetMapping("/user/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        System.out.println("hitt");
        String token = extractToken(authHeader);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
        }
        try {
            GitHubUserDTO userInfo = gitHubService.getAuthenticatedUserInfo(token);
            return ResponseEntity.ok(userInfo);
        } catch (HttpClientErrorException.Unauthorized e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching user data: " + e.getMessage());
        }
    }

    @GetMapping("/user/repos")
    public ResponseEntity<?> getAuthenticatedUserRepos(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = extractToken(authHeader);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
        }
        try {
            List<GitHubRepoDTO> repos = gitHubService.getAuthenticatedUserRepos(token);
            return ResponseEntity.ok(repos);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching repository data: " + e.getMessage());
        }
    }

    @GetMapping("/user/languages")
    public ResponseEntity<?> getAuthenticatedUserLanguages(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = extractToken(authHeader);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
        }
        try {
            List<GitHubRepoDTO> repos = gitHubService.getAuthenticatedUserRepos(token);
            Map<String, Long> languageStats = gitHubService.calculateLanguageStats(repos);
            return ResponseEntity.ok(languageStats);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching language data: " + e.getMessage());
        }
    }

    @GetMapping("/user/events")
    public ResponseEntity<?> getAuthenticatedUserEvents(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = extractToken(authHeader);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
        }
        try {
            List<GitHubEventDTO> events = gitHubService.getAuthenticatedUserEvents(token);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching event data: " + e.getMessage());
        }
    }

    @GetMapping("/user/contributions")
    public ResponseEntity<?> getAuthenticatedUserContributions(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = extractToken(authHeader);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
        }
        try {
            Object contributionData = gitHubService.getContributionData(token);
            return ResponseEntity.ok(contributionData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching contribution data: " + e.getMessage());
        }
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<?> getPublicUserInfo(@PathVariable String username) {
        try {
            System.err.println("hit in");
            GitHubUserDTO userInfo = gitHubService.getPublicUserInfo(username);
            return ResponseEntity.ok(userInfo);
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.status(404).body("GitHub user not found: " + username);
        } catch (RuntimeException e) {
            // Log the exception details
            return ResponseEntity.status(500).body("Error fetching data from GitHub: " + e.getMessage());
        }
    }

    @GetMapping("/users/{username}/repos")
    public ResponseEntity<?> getPublicRepos(@PathVariable String username) {
        try {
            List<GitHubRepoDTO> repos = gitHubService.getPublicRepos(username);
            return ResponseEntity.ok(repos);
        } catch (HttpClientErrorException.NotFound e) {
            // Although unlikely for the repos endpoint itself, the user might not exist
            return ResponseEntity.status(404).body("GitHub user not found or no access: " + username);
        } catch (RuntimeException e) {
            // Log the exception details
            return ResponseEntity.status(500).body("Error fetching repo data from GitHub: " + e.getMessage());
        }
    }

    @GetMapping("/users/{username}/languages")
    public ResponseEntity<?> getLanguageStats(@PathVariable String username) {
        try {
            Map<String, Long> languageStats = gitHubService.getLanguageStats(username);
            return ResponseEntity.ok(languageStats);
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.status(404).body("GitHub user not found: " + username);
        } catch (RuntimeException e) {
            // Log the exception details
            return ResponseEntity.status(500).body("Error fetching language data from GitHub: " + e.getMessage());
        }
    }

    @GetMapping("/users/{username}/events")
    public ResponseEntity<?> getPublicEvents(@PathVariable String username) {
        try {
            List<GitHubEventDTO> events = gitHubService.getPublicEvents(username);
            return ResponseEntity.ok(events);
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.status(404).body("GitHub user not found: " + username);
        } catch (RuntimeException e) {
            // Log the exception details
            return ResponseEntity.status(500).body("Error fetching event data from GitHub: " + e.getMessage());
        }
    }

    @GetMapping("/health-check")
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> response = Map.of(
            "status", "UP",
            "message", "API is running successfully",
            "timestamp", java.time.LocalDateTime.now().toString()
        );
        return ResponseEntity.ok(response);
    }

    // Add other endpoints as needed
} 