package org.gitstats.backend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gitstats.backend.dto.GitHubEventDTO;
import org.gitstats.backend.dto.GitHubRepoDTO;
import org.gitstats.backend.dto.GitHubUserDTO;
import org.gitstats.backend.service.GitHubService;
import org.springframework.beans.factory.annotation.Autowired;
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
            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("login", principal.getAttribute("login"));
            userDetails.put("name", principal.getAttribute("name"));
            userDetails.put("avatarUrl", principal.getAttribute("avatar_url"));
            return ResponseEntity.ok(userDetails);
        } else {
            return ResponseEntity.noContent().build();
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