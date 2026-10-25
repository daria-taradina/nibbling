package com.nibbling.server.dto;

import java.util.List;
import java.util.Map;

// Optional: the final answers, saved just before grading so a debounced draft
// save that has not landed yet cannot be lost. Omit to grade the saved draft.
public record SubmitRequest(Map<Long, List<String>> answers) {}
