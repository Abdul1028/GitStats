package org.gitstats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"org.gitstats", "org.gitstats.backend", "org.gitstats.backend.config", "org.gitstats.backend.controller", "org.gitstats.backend.service", "org.gitstats.backend.dto"})
public class GitStatsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GitStatsApplication.class, args);
    }

}
