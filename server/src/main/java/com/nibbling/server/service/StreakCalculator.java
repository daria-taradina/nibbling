package com.nibbling.server.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Pure streak math (no Spring, no database).
//
// A day counts when the player completed it. The current streak is the run of
// consecutive completed days ending today, or ending yesterday if today isn't
// done yet: a streak only breaks once a whole day has passed without finishing.
public final class StreakCalculator {

    public record Streaks(int current, int longest) {}

    private StreakCalculator() {}

    public static Streaks compute(Collection<LocalDate> completedDays, LocalDate today) {
        Set<LocalDate> done = new HashSet<>(completedDays);

        int current = 0;
        LocalDate cursor = done.contains(today) ? today : today.minusDays(1);
        while (done.contains(cursor)) {
            current++;
            cursor = cursor.minusDays(1);
        }

        List<LocalDate> sorted = new ArrayList<>(done);
        Collections.sort(sorted);
        int longest = 0;
        int run = 0;
        LocalDate previous = null;
        for (LocalDate day : sorted) {
            run = (previous != null && previous.plusDays(1).equals(day)) ? run + 1 : 1;
            longest = Math.max(longest, run);
            previous = day;
        }
        return new Streaks(current, longest);
    }
}
