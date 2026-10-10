package com.tick.market.service;

import com.tick.market.dto.EventIngestRequest;
import com.tick.market.dto.IngestResponse;
import com.tick.market.entity.AuditOperation;
import com.tick.market.entity.AuditStatus;
import com.tick.market.entity.MarketEvent;
import com.tick.market.exception.DuplicateEventException;
import com.tick.market.repository.MarketEventRepository;
import com.tick.market.validator.EventPayloadValidator;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Core event ingestion service implementation.
 * Enforces dual-layer idempotency (application check + PostgreSQL unique constraint translation),
 * transactional boundaries, and persistent audit trail recording.
 */
@Service
public class EventIngestionServiceImpl implements EventIngestionService {

    private static final Logger log = LoggerFactory.getLogger(EventIngestionServiceImpl.class);

    private final MarketEventRepository marketEventRepository;
    private final AuditService auditService;
    private final EventPayloadValidator eventPayloadValidator;

    public EventIngestionServiceImpl(
        MarketEventRepository marketEventRepository,
        AuditService auditService,
        EventPayloadValidator eventPayloadValidator
    ) {
        this.marketEventRepository = marketEventRepository;
        this.auditService = auditService;
        this.eventPayloadValidator = eventPayloadValidator;
    }

    @Override
    @Transactional
    public IngestResponse ingestEvent(EventIngestRequest request) {
        // 1. Domain payload validation
        eventPayloadValidator.validate(request);

        String eventId = request.getEventId();

        // 2. Application-level idempotency check
        if (marketEventRepository.existsByEventId(eventId)) {
            log.warn("Application-level duplicate detected for eventId={}", eventId);
            auditService.recordAudit(
                eventId,
                AuditOperation.INGEST_EVENT,
                AuditStatus.DUPLICATE,
                "Event rejected: duplicate eventId already exists"
            );
            throw new DuplicateEventException(eventId);
        }

        Instant receivedAt = Instant.now();
        MarketEvent event = new MarketEvent(
            eventId,
            request.getSymbol().trim().toUpperCase(),
            request.getEventType(),
            request.getPrice(),
            request.getQuantity(),
            request.getEventTime(),
            receivedAt
        );

        try {
            // 3. Persistent write
            MarketEvent savedEvent = marketEventRepository.saveAndFlush(event);

            // 4. Record audit entry
            auditService.recordAudit(
                eventId,
                AuditOperation.INGEST_EVENT,
                AuditStatus.ACCEPTED,
                "Event successfully ingested and stored"
            );

            log.info("Successfully ingested event: id={}, eventId={}, symbol={}", savedEvent.getId(), eventId, savedEvent.getSymbol());
            return IngestResponse.accepted(savedEvent.getEventId(), savedEvent.getReceivedAt());

        } catch (DataIntegrityViolationException ex) {
            // 5. Database-level idempotency enforcement (concurrent duplicates)
            log.warn("Database unique constraint violation for eventId={}: {}", eventId, ex.getMessage());
            auditService.recordAudit(
                eventId,
                AuditOperation.INGEST_EVENT,
                AuditStatus.DUPLICATE,
                "Event rejected: unique constraint violation under concurrent ingestion"
            );
            throw new DuplicateEventException(eventId, ex);
        }
    }
}
