package com.example.rollingapptask.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileRequest {
    @NotBlank
    @Size(min = 3, max = 40)
    private String name;

    @NotBlank
    @Size(min = 3, max = 15)
    private String username;

    @NotBlank
    @Size(max = 40)
    @Email
    private String email;

    @Size(max = 100)
    private String bio;

    @Size(max = 100)
    private String website;

    @Size(max = 100)
    private String location;
} 