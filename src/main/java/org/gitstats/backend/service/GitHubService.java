package org.gitstats.backend.service;

import org.gitstats.backend.dto.GitHubUserDTO;
import org.gitstats.backend.dto.GitHubRepoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;

@Service
public class GitHubService {

    private final RestClient restClient;

    @Value("${github.api.base-url:https://api.github.com}")
    private String githubApiBaseUrl;

    public GitHubService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public GitHubUserDTO getPublicUserInfo(String username) {
        String url = githubApiBaseUrl + "/users/" + username;
        try {
            return restClient.get()
                    .uri(url)
                    // Consider adding an Accept header for the specific API version if needed
                    // .header("Accept", "application/vnd.github.v3+json")
                    .retrieve()
                    .body(GitHubUserDTO.class);
        } catch (Exception e) {
            // Handle exceptions appropriately (e.g., user not found, API rate limit)
            // For now, rethrow or return null/Optional.empty()
            // Consider logging the error
            System.err.println("Error fetching user info for " + username + ": " + e.getMessage());
            // You might want to throw a custom exception here
            throw new RuntimeException("Failed to fetch user info from GitHub", e);
        }
    }

    public List<GitHubRepoDTO> getPublicRepos(String username) {
        String url = githubApiBaseUrl + "/users/" + username + "/repos?per_page=100";
        // You might also want to add sorting, e.g., &sort=updated or &sort=pushed
        try {
            List<GitHubRepoDTO> repos = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<GitHubRepoDTO>>() {});
            return repos != null ? repos : List.of();
        } catch (Exception e) {
            System.err.println("Error fetching repos for " + username + ": " + e.getMessage());
            throw new RuntimeException("Failed to fetch repos from GitHub", e);
        }
    }

    // Helper method to calculate language statistics
    public Map<String, Long> calculateLanguageStats(List<GitHubRepoDTO> repos) {
        if (repos == null || repos.isEmpty()) {
            return Map.of();
        }
        return repos.stream()
                .map(GitHubRepoDTO::getLanguage)
                .filter(lang -> lang != null && !lang.isBlank())
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ));
    }

    // Method to get languages directly (combines fetch and calculate)
    public Map<String, Long> getLanguageStats(String username) {
        List<GitHubRepoDTO> repos = getPublicRepos(username);
        return calculateLanguageStats(repos);
    }

    // Add methods for other API calls here (events, etc.)
} 