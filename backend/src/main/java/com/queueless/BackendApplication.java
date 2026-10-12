package com.queueless;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// This is the starting point of the backend.
// @SpringBootApplication = "start Spring Boot and scan this package (com.queueless) for our classes".
// @EnableScheduling      = "allow methods marked @Scheduled to run automatically" (used for 10-minute reminders).
@SpringBootApplication
@EnableScheduling
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
