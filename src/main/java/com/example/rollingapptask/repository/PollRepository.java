package com.example.rollingapptask.repository;

import com.example.rollingapptask.model.Poll;
import com.example.rollingapptask.model.PollStatus;
import com.example.rollingapptask.payload.PollAnalyticsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface PollRepository extends JpaRepository<Poll, Long> {
    @Query("SELECT p FROM Poll p WHERE p.createdBy.id = :userId")
    Page<Poll> findByCreatedBy(@Param("userId") Long userId, Pageable pageable);

    List<Poll> findByIdIn(List<Long> pollIds);

    @Query("SELECT p FROM Poll p WHERE p.expirationDateTime <= ?1 AND p.status = 'ACTIVE'")
    List<Poll> findExpiredPolls(Instant now);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.poll.id = ?1")
    long countVotesByPollId(Long pollId);

    @Query("SELECT new com.example.rollingapptask.payload.PollAnalyticsResponse$PollTrend(DATE(p.createdAt), COUNT(p), COUNT(v)) " +
           "FROM Poll p LEFT JOIN Vote v ON v.poll = p " +
           "WHERE p.createdAt BETWEEN ?1 AND ?2 " +
           "GROUP BY DATE(p.createdAt)")
    List<PollAnalyticsResponse.PollTrend> findPollTrends(Instant startDate, Instant endDate);

    long countByStatus(PollStatus status);
} 