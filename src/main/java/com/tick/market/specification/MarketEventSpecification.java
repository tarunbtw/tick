package com.tick.market.specification;

import com.tick.market.entity.EventType;
import com.tick.market.entity.MarketEvent;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

/**
 * Dynamic JPA criteria specifications for querying and filtering MarketEvent records.
 */
public final class MarketEventSpecification {

    private MarketEventSpecification() {
        // Prevent instantiation of utility specification class
    }

    /**
     * Builds a composite JPA Specification matching the supplied query parameters.
     * All supplied parameters are combined with logical AND. Null parameters are omitted.
     *
     * @param symbol    the instrument symbol to match (case-insensitive)
     * @param eventType the specific EventType to match
     * @param from      inclusive start timestamp (eventTime >= from)
     * @param to        inclusive end timestamp (eventTime <= to)
     * @return composite Specification
     */
    public static Specification<MarketEvent> withFilters(
        String symbol,
        EventType eventType,
        Instant from,
        Instant to
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (symbol != null && !symbol.isBlank()) {
                predicates.add(
                    cb.equal(cb.upper(root.get("symbol")), symbol.trim().toUpperCase())
                );
            }

            if (eventType != null) {
                predicates.add(cb.equal(root.get("eventType"), eventType));
            }

            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventTime"), from));
            }

            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventTime"), to));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
