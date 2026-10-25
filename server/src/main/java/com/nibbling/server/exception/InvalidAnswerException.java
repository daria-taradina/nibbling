package com.nibbling.server.exception;

// Answers that don't belong to today's quiz, or submitting with nothing answered.
public class InvalidAnswerException extends RuntimeException {
    public InvalidAnswerException(String message) {
        super(message);
    }
}
