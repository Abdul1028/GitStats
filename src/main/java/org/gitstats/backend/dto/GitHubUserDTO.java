package org.gitstats.backend.dto;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class GitHubUserDTO {

    private String login;

    @JsonProperty("avatar_url")
    private String avatarUrl;

    private String name;
    private String bio;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    private int followers;
    private int following;

    // Add other fields as needed from the plan.md (e.g., public_repos)
    // @JsonProperty("public_repos")
    // private int publicRepos;

    public String getLogin() {
        return this.login;
    }
} 