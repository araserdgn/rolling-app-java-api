package com.example.rollingapptask.controller;

import com.example.rollingapptask.exception.ResourceNotFoundException;
import com.example.rollingapptask.model.User;
import com.example.rollingapptask.payload.*;
import com.example.rollingapptask.repository.UserRepository;
import com.example.rollingapptask.security.CurrentUser;
import com.example.rollingapptask.security.UserPrincipal;
import com.example.rollingapptask.service.PollService;
import com.example.rollingapptask.service.UserService;
import com.example.rollingapptask.util.AppConstants;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PollService pollService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public UserProfile getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        return userService.getUserProfile(currentUser.getId());
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public UserProfile updateProfile(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody UserProfileRequest profileRequest) {
        return userService.updateProfile(currentUser.getId(), profileRequest);
    }

    @GetMapping("/user/checkUsernameAvailability")
    public UserIdentityAvailability checkUsernameAvailability(@RequestParam(value = "username") String username) {
        Boolean isAvailable = !userRepository.existsByUsername(username);
        return new UserIdentityAvailability(isAvailable);
    }

    @GetMapping("/user/checkEmailAvailability")
    public UserIdentityAvailability checkEmailAvailability(@RequestParam(value = "email") String email) {
        Boolean isAvailable = !userRepository.existsByEmail(email);
        return new UserIdentityAvailability(isAvailable);
    }

    @GetMapping("/{username}")
    public UserProfile getUserProfile(@PathVariable(value = "username") String username) {
        return userService.getUserProfileByUsername(username);
    }

    @GetMapping("/{username}/polls")
    public PagedResponse<PollResponse> getPollsCreatedBy(
            @PathVariable(value = "username") String username,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return pollService.getPollsCreatedByUsername(username, page, size);
    }

    @GetMapping("/{username}/votes")
    public PagedResponse<PollResponse> getPollsVotedBy(
            @PathVariable(value = "username") String username,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return pollService.getPollsVotedByUsername(username, page, size);
    }

} 