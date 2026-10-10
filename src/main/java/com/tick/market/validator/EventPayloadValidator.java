package com.tick.market.validator;

import com.tick.market.dto.EventIngestRequest;
import com.tick.market.exception.InvalidEventPayloadException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;

/**
 * Validates domain and business logic constraints on incoming MarketEvent payloads.
 */
@Component
public class EventPayloadValidator {

    private static final Duration MAX_FUTURE_SKEW = Duration.ofMinutes(10);
    private static final Instant MIN_ALLOWABLE_TIMESTAMP = Instant.parse("2000-01-01T00:00:00Z");

    /**
     * Validates the business constraints of an EventIngestRequest.
     *
     * @param request the request payload to validate
     * @throws InvalidEventPayloadException if any business invariant is violated
     */
    public void validate(EventIngestRequest request) {
        if (request == null) {
            throw new InvalidEventPayloadException("Request body cannot be null");
        }

        validateEventId(request.getEventId());
        validateSymbol(request.getSymbol());
        validateEventType(request);
        validateNumericValues(request);
        validateTimestamp(request.getEventTime());
    }

    private void validateEventId(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            throw new InvalidEventPayloadException("eventId is required and cannot be blank");
        }
        if (eventId.length() > 128) {
            throw new InvalidEventPayloadException("eventId length cannot exceed 128 characters");
        }
    }

    private void validateSymbol(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            throw new InvalidEventPayloadException("symbol is required and cannot be blank");
        }
        if (symbol.length() > 32) {
            throw new InvalidEventPayloadException("symbol length cannot exceed 32 characters");
        }
    }

    private void validateEventType(EventIngestRequest request) {
        if (request.getEventType() == null) {
            throw new InvalidEventPayloadException("eventType is required and must be a valid market event type");
        }
    }

    private void validateNumericValues(EventIngestRequest request) {
        BigDecimal price = request.getPrice();
        if (price != null && price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidEventPayloadException("price must be strictly positive when provided");
        }

        BigDecimal quantity = request.getQuantity();
        if (quantity != null && quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidEventPayloadException("quantity must be strictly positive when provided");
        }
    }

    private void validateTimestamp(Instant eventTime) {
        if (eventTime == null) {
            throw new InvalidEventPayloadException("eventTime is required");
        }

        Instant now = Instant.now();
        if (eventTime.isAfter(now.plus(MAX_FUTURE_SKEW))) {
            throw new InvalidEventPayloadException(
                String.format("eventTime '%s' cannot be more than 10 minutes in the future", eventTime)
            );
        }

        if (eventTime.isBefore(MIN_ALLOWABLE_TIMESTAMP)) {
            throw new InvalidEventPayloadException(
                String.format("eventTime '%s' cannot be before year 2000", eventTime)
            );
        }
    }
}
