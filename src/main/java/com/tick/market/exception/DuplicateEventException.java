package com.tick.market.exception;

/**
 * Exception thrown when an event with the given eventId has already been persisted.
 * Maps to HTTP 409 Conflict.
 */
public class DuplicateEventException extends RuntimeException {

    private final String eventId;

    public DuplicateEventException(String eventId) {
        super(String.format("Duplicate event: an event with eventId '%s' has already been ingested", eventId));
        this.eventId = eventId;
    }

    public DuplicateEventException(String eventId, Throwable cause) {
        super(String.format("Duplicate event: an event with eventId '%s' has already been ingested", eventId), cause);
        this.eventId = eventId;
    }

    public String getEventId() {
        return eventId;
    }
}
