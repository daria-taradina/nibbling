package com.nibbling.server.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "daily_challenge_questions")
public class DailyChallengeQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "puzzle_date", nullable = false)
    private DailyChallenge challenge;

    // 1-based order shown to the player.
    @Column(name = "position", nullable = false)
    private int position;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    protected DailyChallengeQuestion() {
        // required by JPA
    }

    public DailyChallengeQuestion(DailyChallenge challenge, int position, Question question) {
        this.challenge = challenge;
        this.position = position;
        this.question = question;
    }

    public DailyChallenge getChallenge() { return challenge; }
    public int getPosition() { return position; }
    public Question getQuestion() { return question; }
}
