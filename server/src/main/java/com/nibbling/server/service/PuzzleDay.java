package com.nibbling.server.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

// Defines "today". The puzzle changes at midnight in one fixed zone
// (America/Los_Angeles by default) for everyone, like Wordle. Attempts end at
// the next midnight in that zone, which is DST-safe because we ask the zone.
@Component
public class PuzzleDay {

    private final ZoneId zone;
    private final Clock clock;

    public PuzzleDay(@Value("${app.puzzle-zone:America/Los_Angeles}") String zone, Clock clock) {
        this.zone = ZoneId.of(zone);
        this.clock = clock;
    }

    public Instant now() {
        return clock.instant();
    }

    public LocalDate today() {
        return LocalDate.now(clock.withZone(zone));
    }

    // The moment the given puzzle day ends (start of the next day in the zone).
    public Instant deadlineFor(LocalDate puzzleDate) {
        return puzzleDate.plusDays(1).atStartOfDay(zone).toInstant();
    }
}
