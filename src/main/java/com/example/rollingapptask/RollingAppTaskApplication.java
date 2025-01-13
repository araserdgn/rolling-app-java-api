package com.example.rollingapptask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RollingAppTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(RollingAppTaskApplication.class, args);
    }
} 