package com.example.rollingapptask.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PollAnalyticsResponse {
    private long totalPolls;
    private long totalVotes;
    private long totalParticipants;
    private List<PollTrend> trends;
    private Map<String, Long> pollsByStatus;
    private double averageVotesPerPoll;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PollTrend {
        private Instant date;
        private long pollCount;
        private long voteCount;
    }
} 