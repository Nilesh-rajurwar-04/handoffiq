package com.handoffiq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HandoffIqApplication {

    public static void main(String[] args) {
        SpringApplication.run(HandoffIqApplication.class, args);
    }
}
