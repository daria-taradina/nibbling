package com.nibbling.server.dto;

import java.time.Instant;

// state is SUBMITTED if the deadline passed while the player was answering;
// the client should then refetch GET /api/challenges/today for the result.
public record SaveDraftResponse(String state, Instant deadlineAt) {}
