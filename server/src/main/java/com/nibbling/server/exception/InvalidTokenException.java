package com.nibbling.server.exception;

// Thrown when an emailed link's token is unknown, expired, or already used.
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
