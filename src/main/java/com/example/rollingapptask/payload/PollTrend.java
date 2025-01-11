package com.example.rollingapptask.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PollTrend {
    private Instant date;
    private long pollCount;
    private long voteCount;
} 