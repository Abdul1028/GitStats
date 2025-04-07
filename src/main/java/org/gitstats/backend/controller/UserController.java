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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/user/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            String login = principal.getAttribute("login");
            if (login == null) {
                 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not extract login from principal");
            }
            try {
                // Fetch full user details using the GitHubService
                GitHubUserDTO userInfo = gitHubService.getPublicUserInfo(login);
                return ResponseEntity.ok(userInfo);
            } catch (HttpClientErrorException.NotFound e) {
                // Handle case where user might not be found via public API despite being authenticated
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("GitHub user details not found for: " + login);
            } catch (RuntimeException e) {
                System.err.println("Error fetching authenticated user details: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching user data: " + e.getMessage());
            }
        } else {
            // Consider returning UNAUTHORIZED if principal is null, though SecurityConfig might handle this
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
    }

    @GetMapping("/user/repos")
    public ResponseEntity<?> getAuthenticatedUserRepos(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        try {
            List<GitHubRepoDTO> repos = gitHubService.getAuthenticatedUserRepos();
            return ResponseEntity.ok(repos);
        } catch (RuntimeException e) {
            System.err.println("Error fetching authenticated repos: " + e.getMessage());
            return ResponseEntity.status(500).body("Error fetching repository data: " + e.getMessage());
        }
    }

    @GetMapping("/user/languages")
    public ResponseEntity<?> getAuthenticatedUserLanguages(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        try {
            List<GitHubRepoDTO> repos = gitHubService.getAuthenticatedUserRepos();
            Map<String, Long> languageStats = gitHubService.calculateLanguageStats(repos);
            return ResponseEntity.ok(languageStats);
        } catch (RuntimeException e) {
            System.err.println("Error fetching authenticated languages: " + e.getMessage());
            return ResponseEntity.status(500).body("Error fetching language data: " + e.getMessage());
        }
    }

    @GetMapping("/user/events")
    public ResponseEntity<?> getAuthenticatedUserEvents(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        try {
            List<GitHubEventDTO> events = gitHubService.getAuthenticatedUserEvents();
            return ResponseEntity.ok(events);
        } catch (RuntimeException e) {
            System.err.println("Error fetching authenticated events: " + e.getMessage());
            return ResponseEntity.status(500).body("Error fetching event data: " + e.getMessage());
        }
    }

    @GetMapping("/user/contributions")
    public ResponseEntity<?> getAuthenticatedUserContributions(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        try {
            Object contributionData = gitHubService.getContributionData();
            // We might need to map this Object to a specific DTO later
            return ResponseEntity.ok(contributionData);
        } catch (RuntimeException e) {
            System.err.println("Error fetching contribution data: " + e.getMessage());
            return ResponseEntity.status(500).body("Error fetching contribution data: " + e.getMessage());
        }
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<?> getPublicUserInfo(@PathVariable String username) {
        try {
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

    // Add other endpoints as needed
} 