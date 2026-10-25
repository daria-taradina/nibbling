package com.nibbling.server.service;

import com.nibbling.server.entity.Attempt;
import com.nibbling.server.entity.AttemptStatus;
import com.nibbling.server.repository.AttemptRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// Auto-submits attempts left open past the end of their puzzle day, so streaks
// and leaderboards are correct without waiting for the player to come back.
// If the server was asleep at midnight, QuizService does the same lazily.
@Component
public class AttemptSweeper {

    private static final Logger log = LoggerFactory.getLogger(AttemptSweeper.class);

    private final AttemptRepository attempts;
    private final QuizService quizService;
    private final PuzzleDay puzzleDay;

    public AttemptSweeper(AttemptRepository attempts, QuizService quizService, PuzzleDay puzzleDay) {
        this.attempts = attempts;
        this.quizService = quizService;
        this.puzzleDay = puzzleDay;
    }

    @Scheduled(cron = "0 5 0 * * *", zone = "${app.puzzle-zone:America/Los_Angeles}")
    public void closeOverdueAttempts() {
        for (Attempt a : attempts.findByStatusAndDeadlineAtBefore(AttemptStatus.IN_PROGRESS, puzzleDay.now())) {
            try {
                quizService.closeIfOverdue(a.getId());
            } catch (RuntimeException e) {
                log.warn("Could not auto-submit attempt {}: {}", a.getId(), e.getMessage());
            }
        }
    }
}
