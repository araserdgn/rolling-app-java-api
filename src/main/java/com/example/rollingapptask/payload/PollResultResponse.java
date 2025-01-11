package com.example.rollingapptask.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PollResultResponse {
    private Long pollId;
    private String question;
    private List<ChoiceResult> choices;
    private long totalVotes;
    private Instant expirationDateTime;
    private boolean isExpired;
    private UserSummary createdBy;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChoiceResult {
        private Long id;
        private String text;
        private long voteCount;
        private double percentage;
    }
} 