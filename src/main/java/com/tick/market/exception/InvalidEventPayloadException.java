package com.tick.market.exception;

/**
 * Exception thrown when domain validation rules are violated by an incoming event payload.
 * Maps to HTTP 400 Bad Request.
 */
public class InvalidEventPayloadException extends RuntimeException {

    public InvalidEventPayloadException(String message) {
        super(message);
    }

    public InvalidEventPayloadException(String message, Throwable cause) {
        super(message, cause);
    }
}
