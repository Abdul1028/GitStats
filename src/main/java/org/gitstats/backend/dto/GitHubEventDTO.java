package org.gitstats.backend.dto;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore fields we don't map
public class GitHubEventDTO {

    private String id;
    private String type;

    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

    private RepoInfo repo;
    // private ActorInfo actor; // Could add if needed
    // private PayloadInfo payload; // Could add specific payload details

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RepoInfo {
        private Long id;
        private String name;
        // private String url;
    }

    // Potential inner classes for Actor and Payload if more detail is needed
    /*
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ActorInfo {
        private Long id;
        private String login;
        @JsonProperty("display_login")
        private String displayLogin;
        @JsonProperty("avatar_url")
        private String avatarUrl;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PayloadInfo {
        // Fields depend heavily on the event type (PushEvent, CreateEvent, etc.)
        // Example for PushEvent:
        // private Long push_id;
        // private Integer size;
        // private Integer distinct_size;
        // private String ref;
        // private String head;
        // private String before;
        // private List<CommitInfo> commits;
    }
    */
} 