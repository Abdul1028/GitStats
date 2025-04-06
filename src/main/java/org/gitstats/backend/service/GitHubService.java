package org.gitstats.backend.service;

import org.gitstats.backend.dto.GitHubUserDTO;
import org.gitstats.backend.dto.GitHubRepoDTO;
import org.gitstats.backend.dto.GitHubEventDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
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
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final HttpGraphQlClient graphQlClient;

    @Value("${github.api.base-url:https://api.github.com}")
    private String githubApiBaseUrl;

    public GitHubService(
        RestClient.Builder restClientBuilder, 
        OAuth2AuthorizedClientService authorizedClientService,
        HttpGraphQlClient graphQlClient
    ) {
        this.restClient = restClientBuilder.build();
        this.authorizedClientService = authorizedClientService;
        this.graphQlClient = graphQlClient;
    }

    private OAuth2AuthenticationToken getAuthenticationToken() {
        return (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
    }
    
    private String getAccessToken() {
        OAuth2AuthenticationToken authentication = getAuthenticationToken();
        if (authentication == null) {
            throw new IllegalStateException("User not authenticated");
        }
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(), 
                authentication.getName()
        );
        if (client == null || client.getAccessToken() == null) {
            throw new IllegalStateException("Could not find OAuth2 access token");
        }
        return client.getAccessToken().getTokenValue();
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

    public List<GitHubRepoDTO> getAuthenticatedUserRepos() {
        String url = githubApiBaseUrl + "/user/repos?per_page=100&affiliation=owner,collaborator"; // Fetch owned and collaborated repos
        String token = getAccessToken();

        try {
            List<GitHubRepoDTO> repos = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<GitHubRepoDTO>>() {});
            return repos != null ? repos : List.of();
        } catch (Exception e) {
            System.err.println("Error fetching authenticated user repos: " + e.getMessage());
            throw new RuntimeException("Failed to fetch authenticated user repos from GitHub", e);
        }
    }

    // New method for authenticated user events
    public List<GitHubEventDTO> getAuthenticatedUserEvents() {
        OAuth2AuthenticationToken authentication = getAuthenticationToken();
        String username = authentication.getPrincipal().getAttribute("login"); // Get username from token
        if (username == null) {
             throw new IllegalStateException("Could not extract username from authenticated principal");
        }
        // Note: This endpoint might require specific scopes depending on event types needed.
        String url = githubApiBaseUrl + "/users/" + username + "/events?per_page=100"; 
        String token = getAccessToken();
        
        try {
            List<GitHubEventDTO> events = restClient.get()
                    .uri(url)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<GitHubEventDTO>>() {});
            return events != null ? events : List.of();
        } catch (Exception e) {
            System.err.println("Error fetching authenticated user events: " + e.getMessage());
            throw new RuntimeException("Failed to fetch authenticated user events from GitHub", e);
        }
    }

    // --- GraphQL Methods --- 
    public Object getContributionData() { // Return type will be refined based on response structure
        OAuth2AuthenticationToken authentication = getAuthenticationToken();
        String username = authentication.getPrincipal().getAttribute("login");
        String token = getAccessToken();

        // Define time range (e.g., last 365 days)
        OffsetDateTime to = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime from = to.minusDays(365);

        // Variables for the GraphQL query
        Map<String, Object> variables = Map.of(
            "username", username,
            "from", from.toString(),
            "to", to.toString()
        );

        // Execute the GraphQL query defined in contributions.graphql
        // Add Authorization header dynamically
        return graphQlClient
                .mutate()
                .header("Authorization", "Bearer " + token)
                .build()
                .documentName("contributions") // Refers to contributions.graphql
                .variables(variables)
                .retrieve("user.contributionsCollection") // Path to the desired data
                .toEntity(Object.class) // Use a specific DTO later
                .block(); // Use block() for simplicity, consider reactive approach later
    }
} 