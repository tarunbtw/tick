package com.tick.market.specification;

import static org.assertj.core.api.Assertions.assertThat;

import com.tick.market.entity.EventType;
import com.tick.market.entity.MarketEvent;
import com.tick.market.repository.MarketEventRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class MarketEventSpecificationTest {

    @Autowired
    private MarketEventRepository marketEventRepository;

    @BeforeEach
    void setUp() {
        marketEventRepository.deleteAll();

        marketEventRepository.save(new MarketEvent(
            "spec-01",
            "AAPL",
            EventType.TRADE,
            new BigDecimal("220.00"),
            new BigDecimal("100"),
            Instant.parse("2026-10-10T10:00:00Z"),
            Instant.now()
        ));

        marketEventRepository.save(new MarketEvent(
            "spec-02",
            "AAPL",
            EventType.QUOTE,
            new BigDecimal("220.50"),
            new BigDecimal("500"),
            Instant.parse("2026-10-10T11:00:00Z"),
            Instant.now()
        ));

        marketEventRepository.save(new MarketEvent(
            "spec-03",
            "GOOGL",
            EventType.TRADE,
            new BigDecimal("175.00"),
            new BigDecimal("50"),
            Instant.parse("2026-10-10T12:00:00Z"),
            Instant.now()
        ));

        marketEventRepository.flush();
    }

    @Test
    @DisplayName("Should filter events by symbol")
    void shouldFilterBySymbol() {
        Specification<MarketEvent> spec = MarketEventSpecification.withFilters("AAPL", null, null, null);
        List<MarketEvent> results = marketEventRepository.findAll(spec);

        assertThat(results).hasSize(2)
            .allMatch(e -> e.getSymbol().equalsIgnoreCase("AAPL"));
    }

    @Test
    @DisplayName("Should filter events by eventType")
    void shouldFilterByEventType() {
        Specification<MarketEvent> spec = MarketEventSpecification.withFilters(null, EventType.QUOTE, null, null);
        List<MarketEvent> results = marketEventRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getEventId()).isEqualTo("spec-02");
    }

    @Test
    @DisplayName("Should filter events by time range")
    void shouldFilterByTimeRange() {
        Instant from = Instant.parse("2026-10-10T10:30:00Z");
        Instant to = Instant.parse("2026-10-10T12:30:00Z");

        Specification<MarketEvent> spec = MarketEventSpecification.withFilters(null, null, from, to);
        List<MarketEvent> results = marketEventRepository.findAll(spec);

        assertThat(results).hasSize(2)
            .extracting(MarketEvent::getEventId)
            .containsExactlyInAnyOrder("spec-02", "spec-03");
    }

    @Test
    @DisplayName("Should filter by composite conditions")
    void shouldFilterByCompositeConditions() {
        Instant from = Instant.parse("2026-10-10T09:00:00Z");
        Instant to = Instant.parse("2026-10-10T11:30:00Z");

        Specification<MarketEvent> spec = MarketEventSpecification.withFilters(
            "AAPL",
            EventType.TRADE,
            from,
            to
        );
        List<MarketEvent> results = marketEventRepository.findAll(spec);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getEventId()).isEqualTo("spec-01");
    }
}
