package com.tick.market.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tick.market.dto.EventIngestRequest;
import com.tick.market.dto.IngestResponse;
import com.tick.market.entity.AuditOperation;
import com.tick.market.entity.AuditStatus;
import com.tick.market.entity.EventType;
import com.tick.market.entity.MarketEvent;
import com.tick.market.exception.DuplicateEventException;
import com.tick.market.exception.InvalidEventPayloadException;
import com.tick.market.repository.MarketEventRepository;
import com.tick.market.validator.EventPayloadValidator;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class EventIngestionServiceTest {

    @Mock
    private MarketEventRepository marketEventRepository;

    @Mock
    private AuditService auditService;

    private EventPayloadValidator eventPayloadValidator;
    private EventIngestionService eventIngestionService;

    @BeforeEach
    void setUp() {
        eventPayloadValidator = new EventPayloadValidator();
        eventIngestionService = new EventIngestionServiceImpl(
            marketEventRepository,
            auditService,
            eventPayloadValidator
        );
    }

    private EventIngestRequest createSampleRequest(String eventId) {
        return new EventIngestRequest(
            eventId,
            "AAPL",
            EventType.TRADE,
            new BigDecimal("185.50"),
            new BigDecimal("100"),
            Instant.now()
        );
    }

    @Test
    @DisplayName("Should ingest event successfully on first submission")
    void shouldIngestEventSuccessfully() {
        EventIngestRequest request = createSampleRequest("evt-100");
        MarketEvent savedEvent = new MarketEvent(
            request.getEventId(),
            request.getSymbol(),
            request.getEventType(),
            request.getPrice(),
            request.getQuantity(),
            request.getEventTime(),
            Instant.now()
        );
        savedEvent.setId(1L);

        when(marketEventRepository.existsByEventId("evt-100")).thenReturn(false);
        when(marketEventRepository.saveAndFlush(any(MarketEvent.class))).thenReturn(savedEvent);

        IngestResponse response = eventIngestionService.ingestEvent(request);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(AuditStatus.ACCEPTED);
        assertThat(response.getEventId()).isEqualTo("evt-100");

        verify(marketEventRepository).saveAndFlush(any(MarketEvent.class));
        verify(auditService).recordAudit(
            eq("evt-100"),
            eq(AuditOperation.INGEST_EVENT),
            eq(AuditStatus.ACCEPTED),
            any()
        );
    }

    @Test
    @DisplayName("Should reject duplicate event at application level")
    void shouldRejectDuplicateEventAtApplicationLevel() {
        EventIngestRequest request = createSampleRequest("evt-duplicate");

        when(marketEventRepository.existsByEventId("evt-duplicate")).thenReturn(true);

        assertThatThrownBy(() -> eventIngestionService.ingestEvent(request))
            .isInstanceOf(DuplicateEventException.class)
            .hasMessageContaining("evt-duplicate");

        verify(marketEventRepository, never()).saveAndFlush(any());
        verify(auditService).recordAudit(
            eq("evt-duplicate"),
            eq(AuditOperation.INGEST_EVENT),
            eq(AuditStatus.DUPLICATE),
            any()
        );
    }

    @Test
    @DisplayName("Should handle database unique constraint violation under concurrent submissions")
    void shouldHandleDatabaseUniqueConstraintViolation() {
        EventIngestRequest request = createSampleRequest("evt-concurrent");

        when(marketEventRepository.existsByEventId("evt-concurrent")).thenReturn(false);
        when(marketEventRepository.saveAndFlush(any(MarketEvent.class)))
            .thenThrow(new DataIntegrityViolationException("Unique constraint uq_market_events_event_id violated"));

        assertThatThrownBy(() -> eventIngestionService.ingestEvent(request))
            .isInstanceOf(DuplicateEventException.class)
            .hasMessageContaining("evt-concurrent");

        verify(auditService).recordAudit(
            eq("evt-concurrent"),
            eq(AuditOperation.INGEST_EVENT),
            eq(AuditStatus.DUPLICATE),
            any()
        );
    }

    @Test
    @DisplayName("Should reject invalid event payload before persistence")
    void shouldRejectInvalidEventPayload() {
        EventIngestRequest invalidRequest = new EventIngestRequest(
            "",
            "AAPL",
            EventType.TRADE,
            new BigDecimal("-10"),
            new BigDecimal("100"),
            Instant.now()
        );

        assertThatThrownBy(() -> eventIngestionService.ingestEvent(invalidRequest))
            .isInstanceOf(InvalidEventPayloadException.class);

        verify(marketEventRepository, never()).existsByEventId(any());
        verify(marketEventRepository, never()).saveAndFlush(any());
    }
}
