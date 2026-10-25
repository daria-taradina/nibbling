package com.nibbling.server.service;

import com.nibbling.server.dto.CalendarResponse;
import com.nibbling.server.dto.DayResult;
import com.nibbling.server.dto.StatsResponse;
import com.nibbling.server.entity.Attempt;
import com.nibbling.server.entity.AttemptStatus;
import com.nibbling.server.repository.AttemptRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

// Everything about progress is derived from submitted attempts (nothing extra is
// stored), so changing the XP numbers or level curve never needs a migration.
@Service
public class StatsService {

    private final PuzzleDay puzzleDay;
    private final AttemptRepository attempts;
    private final QuizService quizService;
    private final int xpPerLevel;

    public StatsService(PuzzleDay puzzleDay,
                        AttemptRepository attempts,
                        QuizService quizService,
                        @Value("${quiz.level.xp-per-level:300}") int xpPerLevel) {
        this.puzzleDay = puzzleDay;
        this.attempts = attempts;
        this.quizService = quizService;
        this.xpPerLevel = xpPerLevel;
    }

    @Transactional
    public StatsResponse stats(UUID userId) {
        quizService.closeOverdueFor(userId);
        LocalDate today = puzzleDay.today();

        int totalXp = (int) attempts.totalXp(userId, AttemptStatus.SUBMITTED);
        StreakCalculator.Streaks streaks =
                StreakCalculator.compute(attempts.completedDates(userId, AttemptStatus.SUBMITTED), today);

        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return new StatsResponse(
                today,
                totalXp,
                totalXp / xpPerLevel + 1,
                totalXp % xpPerLevel,
                xpPerLevel,
                streaks.current(),
                streaks.longest(),
                attempts.firstPlayedOn(userId).orElse(null),
                dayResults(userId, weekStart, weekStart.plusDays(6)));
    }

    // month == null means the current puzzle month.
    @Transactional
    public CalendarResponse calendar(UUID userId, YearMonth month) {
        quizService.closeOverdueFor(userId);
        LocalDate today = puzzleDay.today();
        YearMonth shown = month != null ? month : YearMonth.from(today);
        return new CalendarResponse(
                shown.toString(),
                today,
                attempts.firstPlayedOn(userId).orElse(null),
                dayResults(userId, shown.atDay(1), shown.atEndOfMonth()));
    }

    private List<DayResult> dayResults(UUID userId, LocalDate from, LocalDate to) {
        return attempts.findByUserIdAndPuzzleDateBetweenOrderByPuzzleDate(userId, from, to).stream()
                .map(StatsService::toDay)
                .toList();
    }

    private static DayResult toDay(Attempt a) {
        return new DayResult(a.getPuzzleDate(), a.getStatus().name(),
                a.getTier() == null ? null : a.getTier().name());
    }
}
