package com.tick.market.service;

import com.tick.market.dto.EventIngestRequest;
import com.tick.market.dto.IngestResponse;

/**
 * Service contract for deterministic, idempotent market event ingestion.
 */
public interface EventIngestionService {

    /**
     * Ingests a simulated market event.
     * Enforces validation, application-level idempotency, and handles concurrent unique constraint violations.
     *
     * @param request the incoming market event payload
     * @return an IngestResponse confirming acceptance
     * @throws com.tick.market.exception.DuplicateEventException if eventId already exists
     * @throws com.tick.market.exception.InvalidEventPayloadException if validation fails
     */
    IngestResponse ingestEvent(EventIngestRequest request);
}
