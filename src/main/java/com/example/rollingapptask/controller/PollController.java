package com.example.rollingapptask.controller;

import com.example.rollingapptask.model.*;
import com.example.rollingapptask.payload.*;
import com.example.rollingapptask.repository.PollRepository;
import com.example.rollingapptask.repository.UserRepository;
import com.example.rollingapptask.repository.VoteRepository;
import com.example.rollingapptask.security.CurrentUser;
import com.example.rollingapptask.security.UserPrincipal;
import com.example.rollingapptask.service.PollService;
import com.example.rollingapptask.util.AppConstants;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;

@RestController
@RequestMapping("/api/polls")
public class PollController {

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PollService pollService;

    @GetMapping
    public PagedResponse<PollResponse> getPolls(@CurrentUser UserPrincipal currentUser,
                                              @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                              @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return pollService.getAllPolls(currentUser, page, size);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createPoll(@Valid @RequestBody PollRequest pollRequest, 
                                      @CurrentUser UserPrincipal currentUser) {
        Poll poll = pollService.createPoll(pollRequest, currentUser);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{pollId}")
                .buildAndExpand(poll.getId()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "Poll Created Successfully"));
    }

    @GetMapping("/{pollId}")
    public PollResponse getPollById(@CurrentUser UserPrincipal currentUser,
                                  @PathVariable Long pollId) {
        return pollService.getPollById(pollId, currentUser);
    }

    @PostMapping("/{pollId}/votes")
    @PreAuthorize("hasRole('USER')")
    public PollResponse castVote(@CurrentUser UserPrincipal currentUser,
                               @PathVariable Long pollId,
                               @Valid @RequestBody VoteRequest voteRequest) {
        return pollService.castVoteAndGetUpdatedPoll(pollId, voteRequest, currentUser);
    }

    @GetMapping("/user/polls")
    @PreAuthorize("hasRole('USER')")
    public PagedResponse<PollResponse> getPollsCreatedBy(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return pollService.getPollsCreatedBy(currentUser.getId(), page, size);
    }

    @GetMapping("/user/votes")
    @PreAuthorize("hasRole('USER')")
    public PagedResponse<PollResponse> getPollsVotedBy(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        return pollService.getPollsVotedBy(currentUser.getId(), page, size);
    }

    @GetMapping("/{pollId}/results")
    @PreAuthorize("hasRole('USER')")
    public PollResultResponse getPollResults(@PathVariable Long pollId) {
        return pollService.getPollResults(pollId);
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasRole('USER')")
    public PollAnalyticsResponse getPollAnalytics(
        @CurrentUser UserPrincipal currentUser,
        @RequestParam(required = false) Instant startDate,
        @RequestParam(required = false) Instant endDate) {
        return pollService.getPollAnalytics(currentUser, startDate, endDate);
    }
    @DeleteMapping("/{pollId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deletePoll(@PathVariable Long pollId, @CurrentUser UserPrincipal currentUser) {
        pollService.deletePoll(pollId, currentUser);
        return ResponseEntity.ok()
                .body(new ApiResponse(true, "Poll deleted successfully"));
    }
} 