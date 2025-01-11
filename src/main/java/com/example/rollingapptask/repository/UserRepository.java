package com.example.rollingapptask.repository;

import com.example.rollingapptask.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT COUNT(p) FROM Poll p WHERE p.createdBy.id = :userId")
    long countPollsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.user.id = :userId")
    long countVotesByUserId(@Param("userId") Long userId);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    List<User> findByIdIn(Collection<Long> userIds);

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);
} 