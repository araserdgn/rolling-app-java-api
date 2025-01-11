package com.example.rollingapptask.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChoiceRequest {
    @NotBlank
    @Size(max = 40)
    private String text;
} 