package com.example.rollingapptask.service;

import com.example.rollingapptask.exception.ResourceNotFoundException;
import com.example.rollingapptask.model.User;
import com.example.rollingapptask.payload.UserProfile;
import com.example.rollingapptask.payload.UserProfileRequest;
import com.example.rollingapptask.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public UserProfile getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        long pollCount = userRepository.countPollsByUserId(userId);
        long voteCount = userRepository.countVotesByUserId(userId);

        return new UserProfile(
            user.getId(),
            user.getUsername(),
            user.getName(),
            user.getCreatedAt(),
            pollCount,
            voteCount
        );
    }

    public UserProfile updateProfile(Long userId, UserProfileRequest profileRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setName(profileRequest.getName());
        user.setUsername(profileRequest.getUsername());
        user.setEmail(profileRequest.getEmail());
        // Diğer alanları da güncelleyebilirsiniz

        user = userRepository.save(user);

        long pollCount = userRepository.countPollsByUserId(userId);
        long voteCount = userRepository.countVotesByUserId(userId);

        return new UserProfile(
            user.getId(),
            user.getUsername(),
            user.getName(),
            user.getCreatedAt(),
            pollCount,
            voteCount
        );
    }

    public UserProfile getUserProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        long pollCount = userRepository.countPollsByUserId(user.getId());
        long voteCount = userRepository.countVotesByUserId(user.getId());

        return new UserProfile(
            user.getId(),
            user.getUsername(),
            user.getName(),
            user.getCreatedAt(),
            pollCount,
            voteCount
        );
    }
} 