package com.tick.market.service;

import com.tick.market.dto.EventResponse;
import com.tick.market.dto.PagedResponse;
import com.tick.market.entity.EventType;
import com.tick.market.entity.MarketEvent;
import com.tick.market.exception.EventNotFoundException;
import com.tick.market.repository.MarketEventRepository;
import com.tick.market.specification.MarketEventSpecification;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of EventQueryService enforcing deterministic pagination,
 * result capping, and dynamic filtering.
 */
@Service
@Transactional(readOnly = true)
public class EventQueryServiceImpl implements EventQueryService {

    private final MarketEventRepository marketEventRepository;

    @Value("${tick.pagination.default-page:0}")
    private int defaultPage = 0;

    @Value("${tick.pagination.default-size:20}")
    private int defaultSize = 20;

    @Value("${tick.pagination.max-size:100}")
    private int maxSize = 100;

    public EventQueryServiceImpl(MarketEventRepository marketEventRepository) {
        this.marketEventRepository = marketEventRepository;
    }

    @Override
    public PagedResponse<EventResponse> queryEvents(
        String symbol,
        EventType eventType,
        Instant from,
        Instant to,
        Integer page,
        Integer size
    ) {
        int targetPage = (page == null || page < 0) ? defaultPage : page;
        int targetSize = (size == null || size <= 0) ? defaultSize : Math.min(size, maxSize);

        // Deterministic ordering: primary by eventTime DESC, secondary by id DESC
        Sort sort = Sort.by(Sort.Direction.DESC, "eventTime")
                        .and(Sort.by(Sort.Direction.DESC, "id"));

        Pageable pageable = PageRequest.of(targetPage, targetSize, sort);

        Specification<MarketEvent> spec = MarketEventSpecification.withFilters(symbol, eventType, from, to);

        Page<MarketEvent> eventPage = marketEventRepository.findAll(spec, pageable);

        return PagedResponse.from(eventPage, EventResponse::from);
    }

    @Override
    public EventResponse getEventById(String eventId) {
        return marketEventRepository.findByEventId(eventId)
                .map(EventResponse::from)
                .orElseThrow(() -> new EventNotFoundException(eventId));
    }
}
