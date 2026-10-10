package com.tick.market.exception;

/**
 * Exception thrown when a queried event cannot be found by its identifier.
 * Maps to HTTP 404 Not Found.
 */
public class EventNotFoundException extends RuntimeException {

    private final String eventId;

    public EventNotFoundException(String eventId) {
        super(String.format("Event not found with eventId: '%s'", eventId));
        this.eventId = eventId;
    }

    public String getEventId() {
        return eventId;
    }
}
