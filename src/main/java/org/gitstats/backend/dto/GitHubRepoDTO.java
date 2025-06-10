package org.gitstats.backend.dto;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class GitHubRepoDTO {

    private Long id;
    private String name;
    private String description;

    @JsonProperty("html_url")
    private String htmlUrl;

    @JsonProperty("stargazers_count")
    private int stargazersCount;

    @JsonProperty("forks_count")
    private int forksCount;

    private String language; // Primary language

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    // Add other fields if needed, e.g., updated_at, pushed_at
    // @JsonProperty("updated_at")
    // private OffsetDateTime updatedAt;
    // @JsonProperty("pushed_at")
    // private OffsetDateTime pushedAt;

    public String getLanguage() {
        return this.language;
    }
} 