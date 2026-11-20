package com.nibbling.server.dto;

import java.util.List;

// Per-question breakdown, only sent once an attempt is submitted.
public record ReviewView(
        long questionId,
        List<String> selectedKeys,
        List<String> correctKeys,
        boolean correct,
        String explanationMd
) {}
