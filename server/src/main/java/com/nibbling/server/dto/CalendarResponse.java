package com.nibbling.server.dto;

import java.time.LocalDate;
import java.util.List;

// month is "yyyy-MM". Misses before firstPlayedOn should not be drawn.
public record CalendarResponse(String month, LocalDate today, LocalDate firstPlayedOn, List<DayResult> days) {}
