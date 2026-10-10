package com.tick.market.repository;

import com.tick.market.entity.MarketEvent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for MarketEvent entity supporting CRUD,
 * existence checks by eventId, and dynamic specification queries.
 */
@Repository
public interface MarketEventRepository extends JpaRepository<MarketEvent, Long>, JpaSpecificationExecutor<MarketEvent> {

    /**
     * Look up a market event by its unique producer event identifier.
     *
     * @param eventId the idempotency key identifier
     * @return an Optional containing the MarketEvent if found
     */
    Optional<MarketEvent> findByEventId(String eventId);

    /**
     * Check whether an event has already been ingested.
     *
     * @param eventId the idempotency key identifier
     * @return true if an event with this eventId already exists, false otherwise
     */
    boolean existsByEventId(String eventId);
}
