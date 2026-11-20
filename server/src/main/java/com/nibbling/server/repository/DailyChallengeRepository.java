package com.nibbling.server.repository;

import com.nibbling.server.entity.DailyChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface DailyChallengeRepository extends JpaRepository<DailyChallenge, LocalDate> {

    // Returns 1 if THIS call created the day, 0 if it already existed. If two
    // requests race, the loser waits for the winner's transaction (questions
    // included) to commit, so a 0 means the day is complete.
    @Modifying
    @Query(nativeQuery = true, value =
            "insert into daily_challenges (puzzle_date, type) values (:date, 'QUIZ') on conflict do nothing")
    int insertIfAbsent(@Param("date") LocalDate date);
}
