package com.example.rollingapptask.payload;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VoteRequest {
    @NotNull
    private Long choiceId;
} 