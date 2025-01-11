package com.example.rollingapptask.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class UserProfile {
    private Long id;
    private String username;
    private String name;
    private Instant joinedAt;
    private Long pollCount;
    private Long voteCount;
} 