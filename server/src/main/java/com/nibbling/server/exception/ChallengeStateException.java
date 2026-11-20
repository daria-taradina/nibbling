package com.nibbling.server.exception;

// Action not allowed in the attempt's current state (e.g. editing after submit).
public class ChallengeStateException extends RuntimeException {
    public ChallengeStateException(String message) {
        super(message);
    }
}
