package com.example.rollingapptask.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSummary {
    private Long id;
    private String username;
    private String name;
} 