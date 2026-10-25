package com.nibbling.server.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// state: NOT_STARTED | IN_PROGRESS | SUBMITTED
//  - NOT_STARTED: no questions yet (they appear once the player taps Start)
//  - IN_PROGRESS: questions + the saved draft in "answers" (questionId -> option keys)
//  - SUBMITTED:   questions + answers + result (with correct answers and explanations)
public record TodayResponse(
        LocalDate puzzleDate,
        String type,
        String state,
        Instant deadlineAt,
        int questionCount,
        List<QuestionView> questions,
        Map<Long, List<String>> answers,
        ResultView result
) {}
