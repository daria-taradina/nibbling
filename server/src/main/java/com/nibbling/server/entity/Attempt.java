package com.nibbling.server.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// A user's one attempt at one day's challenge. Rows are created with a native
// "insert ... on conflict do nothing" (see AttemptRepository), so two taps on
// Start can never create two attempts or throw.
//
// Lifecycle: IN_PROGRESS (answers are a saved draft) -> SUBMITTED (graded, frozen).
// No finished attempt for a day = Miss. tier == null on a SUBMITTED attempt also
// means Miss (auto-submitted with zero answers).
@Entity
@Table(name = "attempts",
        uniqueConstraints = @UniqueConstraint(name = "uq_attempt_user_day",
                columnNames = {"user_id", "puzzle_date"}))
public class Attempt {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "puzzle_date", nullable = false)
    private LocalDate puzzleDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 12, nullable = false)
    private AttemptStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "submit_reason", length = 10)
    private SubmitReason submitReason;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "deadline_at", nullable = false)
    private Instant deadlineAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "correct_count")
    private Integer correctCount;

    @Column(name = "total")
    private Integer total;

    @Column(name = "score")
    private Double score;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", length = 10)
    private Tier tier;

    @Column(name = "xp")
    private Integer xp;

    // Guards the race between the user pressing Submit and the nightly sweep:
    // whichever commits second fails instead of grading (and awarding XP) twice.
    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected Attempt() {
        // required by JPA
    }

    public void markSubmitted(SubmitReason reason, Instant at, int correctCount, int total,
                              double score, Tier tier, int xp) {
        this.status = AttemptStatus.SUBMITTED;
        this.submitReason = reason;
        this.submittedAt = at;
        this.correctCount = correctCount;
        this.total = total;
        this.score = score;
        this.tier = tier;
        this.xp = xp;
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public LocalDate getPuzzleDate() { return puzzleDate; }
    public AttemptStatus getStatus() { return status; }
    public SubmitReason getSubmitReason() { return submitReason; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getDeadlineAt() { return deadlineAt; }
    public Instant getSubmittedAt() { return submittedAt; }
    public Integer getCorrectCount() { return correctCount; }
    public Integer getTotal() { return total; }
    public Double getScore() { return score; }
    public Tier getTier() { return tier; }
    public Integer getXp() { return xp; }
}
