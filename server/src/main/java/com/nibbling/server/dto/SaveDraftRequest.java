package com.nibbling.server.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

// The full draft every time (questionId -> selected option keys). Last write wins.
public record SaveDraftRequest(@NotNull Map<Long, List<String>> answers) {}
