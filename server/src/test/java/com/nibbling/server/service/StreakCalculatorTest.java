package com.nibbling.server.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StreakCalculatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 19);

    private static LocalDate d(int daysAgo) {
        return TODAY.minusDays(daysAgo);
    }

    @Test
    void noDaysIsZero() {
        var s = StreakCalculator.compute(List.of(), TODAY);
        assertEquals(0, s.current());
        assertEquals(0, s.longest());
    }

    @Test
    void runEndingTodayCounts() {
        var s = StreakCalculator.compute(List.of(d(0), d(1), d(2)), TODAY);
        assertEquals(3, s.current());
        assertEquals(3, s.longest());
    }

    @Test
    void streakSurvivesUntilTodayEnds() {
        // Finished yesterday and the day before, haven't played yet today: still alive.
        var s = StreakCalculator.compute(List.of(d(1), d(2)), TODAY);
        assertEquals(2, s.current());
    }

    @Test
    void missingYesterdayBreaksIt() {
        var s = StreakCalculator.compute(List.of(d(2), d(3), d(4)), TODAY);
        assertEquals(0, s.current());
        assertEquals(3, s.longest());
    }

    @Test
    void longestCanBeInThePast() {
        var s = StreakCalculator.compute(List.of(d(0), d(1), d(5), d(6), d(7), d(8)), TODAY);
        assertEquals(2, s.current());
        assertEquals(4, s.longest());
    }
}
