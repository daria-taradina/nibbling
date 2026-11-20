package com.nibbling.server.repository;

import com.nibbling.server.entity.DailyChallengeQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DailyChallengeQuestionRepository extends JpaRepository<DailyChallengeQuestion, Long> {

    @Query("select dcq from DailyChallengeQuestion dcq " +
            "where dcq.challenge.puzzleDate = :date order by dcq.position")
    List<DailyChallengeQuestion> findForDate(@Param("date") LocalDate date);

    // [questionId, last puzzle date it was used] for every question used so far.
    @Query("select dcq.question.id, max(dcq.challenge.puzzleDate) from DailyChallengeQuestion dcq " +
            "group by dcq.question.id")
    List<Object[]> lastUsedDates();
}
