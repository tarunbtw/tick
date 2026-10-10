package com.tick.market.service;

import com.tick.market.dto.EventResponse;
import com.tick.market.dto.PagedResponse;
import com.tick.market.entity.EventType;
import java.time.Instant;

/**
 * Service contract for querying stored market events with safe filtering and deterministic pagination.
 */
public interface EventQueryService {

    /**
     * Queries stored market events matching the provided filters with bounded pagination
     * and deterministic sorting (eventTime DESC, id DESC).
     *
     * @param symbol    optional instrument symbol
     * @param eventType optional event type
     * @param from      optional lower time bound
     * @param to        optional upper time bound
     * @param page      zero-based page index (defaults to 0 if invalid)
     * @param size      requested page size (clamped between 1 and 100)
     * @return PagedResponse containing matching EventResponse items and pagination metadata
     */
    PagedResponse<EventResponse> queryEvents(
        String symbol,
        EventType eventType,
        Instant from,
        Instant to,
        Integer page,
        Integer size
    );

    /**
     * Looks up an individual market event by its unique eventId.
     *
     * @param eventId the event identifier
     * @return EventResponse representing the found event
     * @throws com.tick.market.exception.EventNotFoundException if not found
     */
    EventResponse getEventById(String eventId);
}
