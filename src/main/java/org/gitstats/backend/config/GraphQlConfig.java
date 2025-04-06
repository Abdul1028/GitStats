package org.gitstats.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GraphQlConfig {

    @Value("${github.graphql.endpoint:https://api.github.com/graphql}")
    private String githubGraphqlEndpoint;

    @Bean
    public HttpGraphQlClient graphQlClient() {
        // Create a WebClient for the GraphQL endpoint
        WebClient webClient = WebClient.builder()
                .baseUrl(githubGraphqlEndpoint)
                .build();
        
        // We will need to enhance this client later in the GitHubService 
        // to add the Authorization header dynamically per request.
        return HttpGraphQlClient.builder(webClient).build();
    }
} 