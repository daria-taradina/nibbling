package com.nibbling.server.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

// "Which challenge is today's?" One row per puzzle day. The day is defined in
// the app.puzzle-zone time zone (America/Los_Angeles), not UTC.
@Entity
@Table(name = "daily_challenges")
public class DailyChallenge {

    @Id
    @Column(name = "puzzle_date", nullable = false)
    private LocalDate puzzleDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 10, nullable = false)
    private ChallengeType type;

    protected DailyChallenge() {
        // required by JPA
    }

    public LocalDate getPuzzleDate() { return puzzleDate; }
    public ChallengeType getType() { return type; }
}
