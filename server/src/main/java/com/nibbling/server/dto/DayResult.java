package com.nibbling.server.dto;

import java.time.LocalDate;

// One day the player has an attempt for. status: SUBMITTED | IN_PROGRESS.
// tier is null for a SUBMITTED day with no answers (a Miss).
// Days with no attempt at all are simply not listed (the client marks them).
public record DayResult(LocalDate date, String status, String tier) {}
