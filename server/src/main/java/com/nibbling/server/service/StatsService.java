package com.nibbling.server.service;

import com.nibbling.server.dto.CalendarResponse;
import com.nibbling.server.dto.DayResult;
import com.nibbling.server.dto.StatsResponse;
import com.nibbling.server.entity.Attempt;
import com.nibbling.server.entity.AttemptStatus;
import com.nibbling.server.repository.AttemptRepository;
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

    // XP required to reach each level. Index = level - 1, so level 1 needs 0 XP,
    // level 2 needs 100, level 3 needs 300, etc. Level 5 is the cap.
    //   Hatchling (1) -> Learner (2) -> Coder (3) -> Developer (4) -> Code Master (5)
    private static final int[] LEVEL_THRESHOLDS = {0, 100, 300, 600, 1000};

    private final PuzzleDay puzzleDay;
    private final AttemptRepository attempts;
    private final QuizService quizService;

    public StatsService(PuzzleDay puzzleDay,
                        AttemptRepository attempts,
                        QuizService quizService) {
        this.puzzleDay = puzzleDay;
        this.attempts = attempts;
        this.quizService = quizService;
    }

    @Transactional
    public StatsResponse stats(UUID userId) {
        quizService.closeOverdueFor(userId);
        LocalDate today = puzzleDay.today();

        int totalXp = (int) attempts.totalXp(userId, AttemptStatus.SUBMITTED);
        StreakCalculator.Streaks streaks =
                StreakCalculator.compute(attempts.completedDates(userId, AttemptStatus.SUBMITTED), today);

        int level = levelFor(totalXp);
        int floorXp = LEVEL_THRESHOLDS[level - 1];
        int ceilXp = level < LEVEL_THRESHOLDS.length ? LEVEL_THRESHOLDS[level] : Integer.MAX_VALUE;
        int xpPerLevel = ceilXp == Integer.MAX_VALUE ? LEVEL_THRESHOLDS[LEVEL_THRESHOLDS.length - 1] - floorXp
                : ceilXp - floorXp;

        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        return new StatsResponse(
                today,
                totalXp,
                level,
                totalXp - floorXp,
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

    // Returns 1-based level (capped at the last tier).
    private static int levelFor(int totalXp) {
        int level = 1;
        for (int i = 1; i < LEVEL_THRESHOLDS.length; i++) {
            if (totalXp >= LEVEL_THRESHOLDS[i]) level = i + 1;
            else break;
        }
        return level;
    }
}
