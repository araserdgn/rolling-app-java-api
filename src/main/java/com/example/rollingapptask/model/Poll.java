package com.example.rollingapptask.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "polls")
@Data
@NoArgsConstructor
public class Poll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 140)
    private String question;

    @OneToMany(
        mappedBy = "poll",
        cascade = CascadeType.ALL,
        fetch = FetchType.EAGER,
        orphanRemoval = true
    )
    private List<Choice> choices = new ArrayList<>();

    @NotNull
    private Instant expirationDateTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PollStatus status = PollStatus.ACTIVE;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public void addChoice(Choice choice) {
        choices.add(choice);
        choice.setPoll(this);
    }

    public void removeChoice(Choice choice) {
        choices.remove(choice);
        choice.setPoll(null);
    }

    public boolean isExpired() {
        return getExpirationDateTime().isBefore(Instant.now()) || status == PollStatus.EXPIRED;
    }
} 