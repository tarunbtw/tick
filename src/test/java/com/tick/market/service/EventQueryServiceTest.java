package com.tick.market.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tick.market.dto.EventResponse;
import com.tick.market.dto.PagedResponse;
import com.tick.market.entity.EventType;
import com.tick.market.entity.MarketEvent;
import com.tick.market.exception.EventNotFoundException;
import com.tick.market.repository.MarketEventRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class EventQueryServiceTest {

    @Mock
    private MarketEventRepository marketEventRepository;

    @Captor
    private ArgumentCaptor<Pageable> pageableCaptor;

    private EventQueryService eventQueryService;

    @BeforeEach
    void setUp() {
        eventQueryService = new EventQueryServiceImpl(marketEventRepository);
    }

    private MarketEvent createSampleEntity(String eventId) {
        MarketEvent event = new MarketEvent(
            eventId,
            "MSFT",
            EventType.TRADE,
            new BigDecimal("420.00"),
            new BigDecimal("50"),
            Instant.now(),
            Instant.now()
        );
        event.setId(10L);
        return event;
    }

    @Test
    @DisplayName("Should query events and clamp page size to maximum allowed bound")
    void shouldQueryEventsAndClampPageSize() {
        MarketEvent entity = createSampleEntity("evt-clamp");
        when(marketEventRepository.findAll(any(Specification.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(entity)));

        PagedResponse<EventResponse> response = eventQueryService.queryEvents(
            "MSFT",
            EventType.TRADE,
            null,
            null,
            -1,  // Invalid page should clamp to default 0
            500  // Excessive size should clamp to max 100
        );

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getEventId()).isEqualTo("evt-clamp");

        verify(marketEventRepository).findAll(any(Specification.class), pageableCaptor.capture());
        Pageable capturedPageable = pageableCaptor.getValue();
        assertThat(capturedPageable.getPageNumber()).isZero();
        assertThat(capturedPageable.getPageSize()).isEqualTo(100);
        assertThat(capturedPageable.getSort().getOrderFor("eventTime")).isNotNull();
    }

    @Test
    @DisplayName("Should retrieve event by eventId when found")
    void shouldRetrieveEventByIdWhenFound() {
        MarketEvent entity = createSampleEntity("evt-found");
        when(marketEventRepository.findByEventId("evt-found")).thenReturn(Optional.of(entity));

        EventResponse response = eventQueryService.getEventById("evt-found");

        assertThat(response).isNotNull();
        assertThat(response.getEventId()).isEqualTo("evt-found");
        assertThat(response.getSymbol()).isEqualTo("MSFT");
    }

    @Test
    @DisplayName("Should throw EventNotFoundException when eventId does not exist")
    void shouldThrowEventNotFoundExceptionWhenNotFound() {
        when(marketEventRepository.findByEventId("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventQueryService.getEventById("non-existent"))
            .isInstanceOf(EventNotFoundException.class)
            .hasMessageContaining("non-existent");
    }
}
