package com.tick.market.controller;

import com.tick.market.dto.EventIngestRequest;
import com.tick.market.dto.EventResponse;
import com.tick.market.dto.IngestResponse;
import com.tick.market.dto.PagedResponse;
import com.tick.market.entity.EventType;
import com.tick.market.service.EventIngestionService;
import com.tick.market.service.EventQueryService;
import jakarta.validation.Valid;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing endpoints for market event ingestion, querying, and retrieval.
 */
@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventIngestionService eventIngestionService;
    private final EventQueryService eventQueryService;

    public EventController(
        EventIngestionService eventIngestionService,
        EventQueryService eventQueryService
    ) {
        this.eventIngestionService = eventIngestionService;
        this.eventQueryService = eventQueryService;
    }

    /**
     * Ingests a new market event.
     * Enforces field validation, application-level idempotency, and transactional storage.
     *
     * @param request the market event payload
     * @return 201 Created with IngestResponse
     */
    @PostMapping
    public ResponseEntity<IngestResponse> ingestEvent(@Valid @RequestBody EventIngestRequest request) {
        IngestResponse response = eventIngestionService.ingestEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Queries stored market events with dynamic filtering and deterministic pagination.
     *
     * @param symbol    optional instrument symbol
     * @param eventType optional event type
     * @param from      optional start timestamp (inclusive ISO-8601 UTC)
     * @param to        optional end timestamp (inclusive ISO-8601 UTC)
     * @param page      zero-based page number (default: 0)
     * @param size      number of elements per page (default: 20, max: 100)
     * @return 200 OK with PagedResponse containing EventResponse items
     */
    @GetMapping
    public ResponseEntity<PagedResponse<EventResponse>> queryEvents(
        @RequestParam(required = false) String symbol,
        @RequestParam(required = false) EventType eventType,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
        @RequestParam(required = false, defaultValue = "0") Integer page,
        @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        PagedResponse<EventResponse> response = eventQueryService.queryEvents(
            symbol,
            eventType,
            from,
            to,
            page,
            size
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves an individual stored market event by its producer eventId.
     *
     * @param eventId the event identifier
     * @return 200 OK with EventResponse, or 404 Not Found
     */
    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable String eventId) {
        EventResponse response = eventQueryService.getEventById(eventId);
        return ResponseEntity.ok(response);
    }
}
