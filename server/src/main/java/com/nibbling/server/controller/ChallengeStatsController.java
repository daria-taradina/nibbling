package com.nibbling.server.controller;

import com.nibbling.server.dto.CalendarResponse;
import com.nibbling.server.dto.StatsResponse;
import com.nibbling.server.security.UserPrincipal;
import com.nibbling.server.service.StatsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/challenges")
public class ChallengeStatsController {

    private final StatsService statsService;

    public ChallengeStatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    // XP, level, streaks and this week's results (home page cards).
    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> stats(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(statsService.stats(principal.getId()));
    }

    // Per-day results for one month, e.g. /api/challenges/calendar?month=2026-10 (default: this month).
    @GetMapping("/calendar")
    public ResponseEntity<CalendarResponse> calendar(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return ResponseEntity.ok(statsService.calendar(principal.getId(), month));
    }
}
