package org.gitstats.backend.service;

import org.gitstats.backend.dto.GitHubUserDTO;
import org.gitstats.backend.dto.GitHubRepoDTO;
import org.gitstats.backend.dto.GitHubEventDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.client.HttpGraphQlClient;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.function.Function;

@Service
public class GitHubService {

    private final RestClient restClient;
    private final HttpGraphQlClient graphQlClient;

    @Value("${github.api.base-url:https://api.github.com}")
    private String githubApiBaseUrl;

    public GitHubService(
        RestClient.Builder restClientBuilder,
        HttpGraphQlClient graphQlClient
    ) {
        this.restClient = restClientBuilder.build();
        this.graphQlClient = graphQlClient;
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

    public List<GitHubEventDTO> getPublicEvents(String username) {
        // API defaults to 30 events, max 100 per page
        String url = githubApiBaseUrl + "/users/" + username + "/events/public?per_page=100";
        try {
            List<GitHubEventDTO> events = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<GitHubEventDTO>>() {});
            return events != null ? events : List.of();
        } catch (Exception e) {
            System.err.println("Error fetching public events for " + username + ": " + e.getMessage());
            // Consider logging and returning empty list or throwing
            throw new RuntimeException("Failed to fetch public events from GitHub", e);
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

    public GitHubUserDTO getAuthenticatedUserInfo(String token) {
        String url = githubApiBaseUrl + "/user";
        try {
            return restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + token)
                    .header("User-Agent", "GitStatsApp")
                    .retrieve()
                    .body(GitHubUserDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error fetching authenticated user info: " + e.getMessage());
            throw new RuntimeException("Failed to fetch authenticated user info from GitHub", e);
        }
    }

    public List<GitHubRepoDTO> getAuthenticatedUserRepos(String token) {
        String url = githubApiBaseUrl + "/user/repos?per_page=100&affiliation=owner,collaborator";
        try {
            List<GitHubRepoDTO> repos = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + token)
                    .header("User-Agent", "GitStatsApp")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<GitHubRepoDTO>>() {});
            return repos != null ? repos : List.of();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error fetching authenticated user repos: " + e.getMessage());
            throw new RuntimeException("Failed to fetch authenticated user repos from GitHub", e);
        }
    }

    public List<GitHubEventDTO> getAuthenticatedUserEvents(String token) {
        GitHubUserDTO user = getAuthenticatedUserInfo(token);
        String username = user.getLogin();
        if (username == null) {
            throw new IllegalStateException("Could not extract username from authenticated user info");
        }
        String url = githubApiBaseUrl + "/users/" + username + "/events?per_page=100";
        try {
            List<GitHubEventDTO> events = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + token)
                    .header("User-Agent", "GitStatsApp")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<GitHubEventDTO>>() {});
            return events != null ? events : List.of();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error fetching authenticated user events: " + e.getMessage());
            throw new RuntimeException("Failed to fetch authenticated user events from GitHub", e);
        }
    }

    public Object getContributionData(String token) {
        GitHubUserDTO user = getAuthenticatedUserInfo(token);
        String username = user.getLogin();
        if (username == null) {
            throw new IllegalStateException("Could not extract username from authenticated user info");
        }
        OffsetDateTime to = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime from = to.minusDays(365);
        Map<String, Object> variables = Map.of(
            "username", username,
            "from", from.toString(),
            "to", to.toString()
        );
        try {
            return graphQlClient
                    .mutate()
                    .header("Authorization", "Bearer " + token)
                    .header("User-Agent", "GitStatsApp")
                    .build()
                    .documentName("contributions")
                    .variables(variables)
                    .retrieve("user.contributionsCollection")
                    .toEntity(Object.class)
                    .block();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error fetching contribution data: " + e.getMessage());
            throw new RuntimeException("Failed to fetch contribution data from GitHub", e);
        }
    }
} 