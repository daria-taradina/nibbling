package com.nibbling.server.dto;

import java.util.List;

// tier is null when the attempt was auto-submitted with no answers (a Miss).
public record ResultView(
        int correctCount,
        int total,
        double score,
        String tier,
        int xp,
        String submitReason,
        List<ReviewView> review
) {}
