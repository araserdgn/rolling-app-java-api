package com.example.rollingapptask.repository;

import com.example.rollingapptask.model.Vote;
import com.example.rollingapptask.model.ChoiceVoteCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    @Query("SELECT v FROM Vote v where v.user.id = :userId and v.poll.id in :pollIds")
    List<Vote> findByUserIdAndPollIdIn(@Param("userId") Long userId, @Param("pollIds") List<Long> pollIds);

    @Query("SELECT v FROM Vote v where v.user.id = :userId and v.poll.id = :pollId")
    Vote findByUserIdAndPollId(@Param("userId") Long userId, @Param("pollId") Long pollId);

    @Query("SELECT COUNT(v.id) from Vote v where v.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT v.choice.id as choiceId, count(v.id) as voteCount FROM Vote v WHERE v.poll.id = :pollId GROUP BY v.choice.id")
    List<ChoiceVoteCount> countByPollIdGroupByChoiceId(@Param("pollId") Long pollId);

    Page<Vote> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT v.user.id) FROM Vote v")
    long countDistinctUsers();

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.choice.id = :choiceId")
    long countByChoiceId(@Param("choiceId") Long choiceId);
} 