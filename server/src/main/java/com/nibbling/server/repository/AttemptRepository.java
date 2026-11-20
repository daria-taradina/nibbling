package com.nibbling.server.repository;

import com.nibbling.server.entity.Attempt;
import com.nibbling.server.entity.AttemptStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttemptRepository extends JpaRepository<Attempt, UUID> {

    Optional<Attempt> findByUserIdAndPuzzleDate(UUID userId, LocalDate puzzleDate);

    List<Attempt> findByUserIdAndStatusAndDeadlineAtBefore(UUID userId, AttemptStatus status, Instant cutoff);

    List<Attempt> findByStatusAndDeadlineAtBefore(AttemptStatus status, Instant cutoff);

    // ---- stats (XP, streak, calendar) ----

    @Query("select coalesce(sum(a.xp), 0L) from Attempt a where a.userId = :userId and a.status = :status")
    long totalXp(@Param("userId") UUID userId, @Param("status") AttemptStatus status);

    // Days the player actually completed (a tier was earned); auto-submitted empty days don't count.
    @Query("select a.puzzleDate from Attempt a where a.userId = :userId and a.status = :status and a.tier is not null")
    List<LocalDate> completedDates(@Param("userId") UUID userId, @Param("status") AttemptStatus status);

    @Query("select min(a.puzzleDate) from Attempt a where a.userId = :userId")
    Optional<LocalDate> firstPlayedOn(@Param("userId") UUID userId);

    List<Attempt> findByUserIdAndPuzzleDateBetweenOrderByPuzzleDate(UUID userId, LocalDate from, LocalDate to);

    // Row-locking reads for anything that changes an attempt (save draft, submit,
    // auto-submit). They serialize per attempt, so the player's Submit and the
    // nightly sweep can never grade the same attempt twice.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Attempt a where a.userId = :userId and a.puzzleDate = :date")
    Optional<Attempt> findForUpdate(@Param("userId") UUID userId, @Param("date") LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Attempt a where a.id = :id")
    Optional<Attempt> findForUpdateById(@Param("id") UUID id);

    // Idempotent start: a double tap (or two tabs) creates exactly one attempt.
    @Modifying
    @Query(nativeQuery = true, value =
            "insert into attempts (id, user_id, puzzle_date, status, started_at, deadline_at, version) " +
            "values (gen_random_uuid(), :userId, :date, 'IN_PROGRESS', :startedAt, :deadlineAt, 0) " +
            "on conflict (user_id, puzzle_date) do nothing")
    int insertIfAbsent(@Param("userId") UUID userId,
                       @Param("date") LocalDate date,
                       @Param("startedAt") Instant startedAt,
                       @Param("deadlineAt") Instant deadlineAt);
}
