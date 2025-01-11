package com.example.rollingapptask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan(basePackages = {"com.example.rollingapptask.model"})
@EnableJpaRepositories(basePackages = {"com.example.rollingapptask.repository"})
@EnableScheduling
public class RollingAppTaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(RollingAppTaskApplication.class, args);
    }
} 