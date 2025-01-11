package com.example.rollingapptask.service;

import com.example.rollingapptask.exception.BadRequestException;
import com.example.rollingapptask.exception.ResourceNotFoundException;
import com.example.rollingapptask.exception.UnauthorizedException;
import com.example.rollingapptask.model.*;
import com.example.rollingapptask.payload.PagedResponse;
import com.example.rollingapptask.payload.PollRequest;
import com.example.rollingapptask.payload.PollResponse;
import com.example.rollingapptask.payload.VoteRequest;
import com.example.rollingapptask.repository.PollRepository;
import com.example.rollingapptask.repository.UserRepository;
import com.example.rollingapptask.repository.VoteRepository;
import com.example.rollingapptask.security.UserPrincipal;
import com.example.rollingapptask.util.AppConstants;
import com.example.rollingapptask.util.ModelMapper;
import com.example.rollingapptask.payload.PollResultResponse;
import com.example.rollingapptask.payload.PollAnalyticsResponse;
import com.example.rollingapptask.payload.PollTrend;
import com.example.rollingapptask.payload.UserSummary;
import com.example.rollingapptask.model.PollStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

@Service
public class PollService {

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private UserRepository userRepository;

    public PagedResponse<PollResponse> getAllPolls(UserPrincipal currentUser, int page, int size) {
        validatePageNumberAndSize(page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Poll> polls = pollRepository.findAll(pageable);

        if(polls.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), polls.getNumber(),
                    polls.getSize(), polls.getTotalElements(), polls.getTotalPages(), polls.isLast());
        }

        List<Long> pollIds = polls.map(Poll::getId).getContent();
        Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(pollIds);
        Map<Long, Long> pollUserVoteMap = getPollUserVoteMap(currentUser, pollIds);
        Map<Long, User> creatorMap = getPollCreatorMap(polls.getContent());

        List<PollResponse> pollResponses = polls.map(poll -> {
            return ModelMapper.mapPollToPollResponse(poll,
                    choiceVoteCountMap,
                    creatorMap.get(poll.getCreatedBy().getId()),
                    pollUserVoteMap == null ? null : pollUserVoteMap.get(poll.getId()));
        }).getContent();

        return new PagedResponse<>(pollResponses, polls.getNumber(),
                polls.getSize(), polls.getTotalElements(), polls.getTotalPages(), polls.isLast());
    }

    public Poll createPoll(PollRequest pollRequest, UserPrincipal currentUser) {
        Poll poll = new Poll();
        poll.setQuestion(pollRequest.getQuestion());

        pollRequest.getChoices().forEach(choiceRequest -> {
            poll.addChoice(new Choice(choiceRequest.getText()));
        });

        poll.setExpirationDateTime(pollRequest.getExpirationDateTime());
        
        User user = userRepository.findById(currentUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));
        poll.setCreatedBy(user);

        return pollRepository.save(poll);
    }

    public PollResponse getPollById(Long pollId, UserPrincipal currentUser) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll", "id", pollId));

        List<ChoiceVoteCount> votes = voteRepository.countByPollIdGroupByChoiceId(pollId);
        Map<Long, Long> choiceVotesMap = votes.stream()
                .collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));

        User creator = userRepository.findById(poll.getCreatedBy().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", poll.getCreatedBy().getId()));

        Vote userVote = null;
        if(currentUser != null) {
            userVote = voteRepository.findByUserIdAndPollId(currentUser.getId(), pollId);
        }

        return ModelMapper.mapPollToPollResponse(poll, choiceVotesMap,
                creator, userVote != null ? userVote.getChoice().getId(): null);
    }

    public PollResponse castVoteAndGetUpdatedPoll(Long pollId, VoteRequest voteRequest, UserPrincipal currentUser) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll", "id", pollId));

        if(poll.getExpirationDateTime().isBefore(Instant.now())) {
            throw new BadRequestException("Sorry! This Poll has already expired");
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        Choice selectedChoice = poll.getChoices().stream()
                .filter(choice -> choice.getId().equals(voteRequest.getChoiceId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Choice", "id", voteRequest.getChoiceId()));

        Vote vote = new Vote();
        vote.setPoll(poll);
        vote.setUser(user);
        vote.setChoice(selectedChoice);

        try {
            vote = voteRepository.save(vote);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Sorry! You have already cast your vote in this poll");
        }

        List<ChoiceVoteCount> votes = voteRepository.countByPollIdGroupByChoiceId(pollId);
        Map<Long, Long> choiceVotesMap = votes.stream()
                .collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));

        return ModelMapper.mapPollToPollResponse(poll, choiceVotesMap, user, vote.getChoice().getId());
    }

    public PagedResponse<PollResponse> getPollsCreatedBy(Long userId, int page, int size) {
        validatePageNumberAndSize(page, size);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Poll> polls = pollRepository.findByCreatedBy(userId, pageable);

        if (polls.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), polls.getNumber(),
                    polls.getSize(), polls.getTotalElements(), polls.getTotalPages(), polls.isLast());
        }

        List<Long> pollIds = polls.map(Poll::getId).getContent();
        Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(pollIds);
        Map<Long, Long> pollUserVoteMap = getPollUserVoteMap(new UserPrincipal(user.getId(), user.getName(), 
                user.getUsername(), user.getEmail(), user.getPassword(), null), pollIds);

        List<PollResponse> pollResponses = polls.map(poll -> {
            return ModelMapper.mapPollToPollResponse(poll,
                    choiceVoteCountMap,
                    user,
                    pollUserVoteMap == null ? null : pollUserVoteMap.get(poll.getId()));
        }).getContent();

        return new PagedResponse<>(pollResponses, polls.getNumber(),
                polls.getSize(), polls.getTotalElements(), polls.getTotalPages(), polls.isLast());
    }

    public PagedResponse<PollResponse> getPollsVotedBy(Long userId, int page, int size) {
        validatePageNumberAndSize(page, size);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Vote> votes = voteRepository.findByUserId(userId, pageable);

        if (votes.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), votes.getNumber(),
                    votes.getSize(), votes.getTotalElements(), votes.getTotalPages(), votes.isLast());
        }

        List<Long> pollIds = votes.map(vote -> vote.getPoll().getId()).getContent();
        Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(pollIds);
        Map<Long, Long> pollUserVoteMap = getPollUserVoteMap(new UserPrincipal(user.getId(), user.getName(),
                user.getUsername(), user.getEmail(), user.getPassword(), null), pollIds);
        Map<Long, User> creatorMap = getPollCreatorMap(votes.map(Vote::getPoll).getContent());

        List<PollResponse> pollResponses = votes.map(vote -> {
            return ModelMapper.mapPollToPollResponse(vote.getPoll(),
                    choiceVoteCountMap,
                    creatorMap.get(vote.getPoll().getCreatedBy().getId()),
                    pollUserVoteMap == null ? null : pollUserVoteMap.get(vote.getPoll().getId()));
        }).getContent();

        return new PagedResponse<>(pollResponses, votes.getNumber(),
                votes.getSize(), votes.getTotalElements(), votes.getTotalPages(), votes.isLast());
    }

    private void validatePageNumberAndSize(int page, int size) {
        if(page < 0) {
            throw new BadRequestException("Page number cannot be less than zero.");
        }

        if(size > AppConstants.MAX_PAGE_SIZE) {
            throw new BadRequestException("Page size must not be greater than " + AppConstants.MAX_PAGE_SIZE);
        }
    }

    private Map<Long, Long> getChoiceVoteCountMap(List<Long> pollIds) {
        List<ChoiceVoteCount> votes = voteRepository.countByPollIdGroupByChoiceId(pollIds.get(0));

        return votes.stream()
                .collect(Collectors.toMap(ChoiceVoteCount::getChoiceId, ChoiceVoteCount::getVoteCount));
    }

    private Map<Long, Long> getPollUserVoteMap(UserPrincipal currentUser, List<Long> pollIds) {
        Map<Long, Long> userVoteMap = null;
        if(currentUser != null) {
            List<Vote> userVotes = voteRepository.findByUserIdAndPollIdIn(currentUser.getId(), pollIds);

            userVoteMap = userVotes.stream()
                    .collect(Collectors.toMap(vote -> vote.getPoll().getId(), vote -> vote.getChoice().getId()));
        }
        return userVoteMap;
    }

    private Map<Long, User> getPollCreatorMap(List<Poll> polls) {
        List<Long> creatorIds = polls.stream()
                .map(poll -> poll.getCreatedBy().getId())
                .distinct()
                .collect(Collectors.toList());

        List<User> creators = userRepository.findByIdIn(creatorIds);
        return creators.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    @Scheduled(fixedRate = 300000) // Her 5 dakikada bir çalışır
    public void checkExpiredPolls() {
        List<Poll> expiredPolls = pollRepository.findExpiredPolls(Instant.now());
        expiredPolls.forEach(poll -> {
            poll.setStatus(PollStatus.EXPIRED);
            pollRepository.save(poll);
        });
    }

    public PollResultResponse getPollResults(Long pollId) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll", "id", pollId));

        // Toplam oy sayısını al
        long totalVotes = voteRepository.countByPollIdGroupByChoiceId(pollId).stream()
                .mapToLong(ChoiceVoteCount::getVoteCount)
                .sum();

        // Her seçenek için oy sayısı ve yüzdesini hesapla
        List<PollResultResponse.ChoiceResult> choiceResults = poll.getChoices().stream()
                .map(choice -> {
                    long voteCount = voteRepository.countByChoiceId(choice.getId());
                    double percentage = totalVotes > 0 ? (double) voteCount / totalVotes * 100 : 0.0;
                    
                    return new PollResultResponse.ChoiceResult(
                        choice.getId(),
                        choice.getText(),
                        voteCount,
                        percentage
                    );
                })
                .collect(Collectors.toList());

        // Poll yaratıcısının bilgilerini al
        User creator = userRepository.findById(poll.getCreatedBy().getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", poll.getCreatedBy().getId()));
        UserSummary creatorSummary = new UserSummary(creator.getId(), creator.getUsername(), creator.getName());

        return new PollResultResponse(
            poll.getId(),
            poll.getQuestion(),
            choiceResults,
            totalVotes,
            poll.getExpirationDateTime(),
            poll.isExpired(),
            creatorSummary
        );
    }

    public PollAnalyticsResponse getPollAnalytics(UserPrincipal currentUser, Instant startDate, Instant endDate) {
        // Varsayılan tarih aralığı: son 30 gün
        if (startDate == null) {
            startDate = Instant.now().minus(30, ChronoUnit.DAYS);
        }
        if (endDate == null) {
            endDate = Instant.now();
        }

        // Toplam istatistikleri hesapla
        long totalPolls = pollRepository.count();
        long totalVotes = voteRepository.count();
        long totalParticipants = voteRepository.countDistinctUsers();
        double averageVotesPerPoll = totalPolls > 0 ? (double) totalVotes / totalPolls : 0.0;

        // Poll durumlarına göre sayıları hesapla
        Map<String, Long> pollsByStatus = Arrays.stream(PollStatus.values())
                .collect(Collectors.toMap(
                    PollStatus::name,
                    status -> pollRepository.countByStatus(status)
                ));

        // Trend verilerini al
        List<PollAnalyticsResponse.PollTrend> trends = pollRepository.findPollTrends(startDate, endDate);

        return new PollAnalyticsResponse(
            totalPolls,
            totalVotes,
            totalParticipants,
            trends,
            pollsByStatus,
            averageVotesPerPoll
        );
    }

    public void deletePoll(Long pollId, UserPrincipal currentUser) {
        Poll poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll", "id", pollId));

        // Poll'u sadece oluşturan kullanıcı silebilir
        if (!poll.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new UnauthorizedException("You don't have permission to delete this poll");
        }

        // Poll'a ait oylar otomatik silinecek (CascadeType.ALL sayesinde)
        pollRepository.delete(poll);
    }

    public PagedResponse<PollResponse> getPollsCreatedByUsername(String username, int page, int size) {
        validatePageNumberAndSize(page, size);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Poll> polls = pollRepository.findByCreatedBy(user.getId(), pageable);

        if (polls.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), polls.getNumber(),
                    polls.getSize(), polls.getTotalElements(), polls.getTotalPages(), polls.isLast());
        }

        List<Long> pollIds = polls.map(Poll::getId).getContent();
        Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(pollIds);
        Map<Long, Long> pollUserVoteMap = getPollUserVoteMap(new UserPrincipal(user.getId(), user.getName(), 
                user.getUsername(), user.getEmail(), user.getPassword(), null), pollIds);

        List<PollResponse> pollResponses = polls.map(poll -> {
            return ModelMapper.mapPollToPollResponse(poll,
                    choiceVoteCountMap,
                    user,
                    pollUserVoteMap == null ? null : pollUserVoteMap.get(poll.getId()));
        }).getContent();

        return new PagedResponse<>(pollResponses, polls.getNumber(),
                polls.getSize(), polls.getTotalElements(), polls.getTotalPages(), polls.isLast());
    }

    public PagedResponse<PollResponse> getPollsVotedByUsername(String username, int page, int size) {
        validatePageNumberAndSize(page, size);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "createdAt");
        Page<Vote> votes = voteRepository.findByUserId(user.getId(), pageable);

        if (votes.getNumberOfElements() == 0) {
            return new PagedResponse<>(Collections.emptyList(), votes.getNumber(),
                    votes.getSize(), votes.getTotalElements(), votes.getTotalPages(), votes.isLast());
        }

        List<Long> pollIds = votes.map(vote -> vote.getPoll().getId()).getContent();
        Map<Long, Long> choiceVoteCountMap = getChoiceVoteCountMap(pollIds);
        Map<Long, Long> pollUserVoteMap = getPollUserVoteMap(new UserPrincipal(user.getId(), user.getName(),
                user.getUsername(), user.getEmail(), user.getPassword(), null), pollIds);
        Map<Long, User> creatorMap = getPollCreatorMap(votes.map(Vote::getPoll).getContent());

        List<PollResponse> pollResponses = votes.map(vote -> {
            return ModelMapper.mapPollToPollResponse(vote.getPoll(),
                    choiceVoteCountMap,
                    creatorMap.get(vote.getPoll().getCreatedBy().getId()),
                    pollUserVoteMap == null ? null : pollUserVoteMap.get(vote.getPoll().getId()));
        }).getContent();

        return new PagedResponse<>(pollResponses, votes.getNumber(),
                votes.getSize(), votes.getTotalElements(), votes.getTotalPages(), votes.isLast());
    }
} 