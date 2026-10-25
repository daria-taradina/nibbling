package com.nibbling.server.dto;

import java.time.LocalDate;
import java.util.List;

// today is the puzzle date in Los Angeles; the client should not work it out itself.
// week is Monday..Sunday of the current week. level = totalXp / xpPerLevel + 1.
public record StatsResponse(
        LocalDate today,
        int totalXp,
        int level,
        int xpIntoLevel,
        int xpPerLevel,
        int currentStreak,
        int longestStreak,
        LocalDate firstPlayedOn,
        List<DayResult> week
) {}
