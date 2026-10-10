package com.tick.market.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tick.market.entity.EventType;
import com.tick.market.entity.MarketEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class MarketEventRepositoryTest {

    @Autowired
    private MarketEventRepository marketEventRepository;

    @Autowired
    private TestEntityManager entityManager;

    private MarketEvent buildEvent(String eventId, String symbol) {
        return new MarketEvent(
            eventId,
            symbol,
            EventType.TRADE,
            new BigDecimal("150.00"),
            new BigDecimal("25"),
            Instant.parse("2026-10-10T10:00:00Z"),
            Instant.now()
        );
    }

    @Test
    @DisplayName("Should save and find event by eventId")
    void shouldSaveAndFindByEventId() {
        MarketEvent event = buildEvent("repo-evt-01", "NVDA");
        marketEventRepository.saveAndFlush(event);

        Optional<MarketEvent> found = marketEventRepository.findByEventId("repo-evt-01");

        assertThat(found).isPresent();
        assertThat(found.get().getSymbol()).isEqualTo("NVDA");
        assertThat(found.get().getEventType()).isEqualTo(EventType.TRADE);
        assertThat(found.get().getId()).isNotNull();
    }

    @Test
    @DisplayName("Should return true for existsByEventId when record exists, false otherwise")
    void shouldCheckExistenceByEventId() {
        MarketEvent event = buildEvent("repo-evt-02", "TSLA");
        marketEventRepository.saveAndFlush(event);

        assertThat(marketEventRepository.existsByEventId("repo-evt-02")).isTrue();
        assertThat(marketEventRepository.existsByEventId("non-existent-id")).isFalse();
    }

    @Test
    @DisplayName("Should enforce uniqueness on eventId and throw DataIntegrityViolationException")
    void shouldEnforceUniqueConstraintOnEventId() {
        MarketEvent event1 = buildEvent("duplicate-id", "AAPL");
        marketEventRepository.saveAndFlush(event1);

        MarketEvent event2 = buildEvent("duplicate-id", "GOOGL");

        assertThatThrownBy(() -> marketEventRepository.saveAndFlush(event2))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}
