package com.tick.market.dto;

import com.tick.market.entity.EventType;
import com.tick.market.entity.MarketEvent;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO representing an individual market event returned by the API.
 */
public class EventResponse {

    private Long id;
    private String eventId;
    private String symbol;
    private EventType eventType;
    private BigDecimal price;
    private BigDecimal quantity;
    private Instant eventTime;
    private Instant receivedAt;

    public EventResponse() {
    }

    public EventResponse(
        Long id,
        String eventId,
        String symbol,
        EventType eventType,
        BigDecimal price,
        BigDecimal quantity,
        Instant eventTime,
        Instant receivedAt
    ) {
        this.id = id;
        this.eventId = eventId;
        this.symbol = symbol;
        this.eventType = eventType;
        this.price = price;
        this.quantity = quantity;
        this.eventTime = eventTime;
        this.receivedAt = receivedAt;
    }

    public static EventResponse from(MarketEvent event) {
        if (event == null) {
            return null;
        }
        return new EventResponse(
            event.getId(),
            event.getEventId(),
            event.getSymbol(),
            event.getEventType(),
            event.getPrice(),
            event.getQuantity(),
            event.getEventTime(),
            event.getReceivedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public Instant getEventTime() {
        return eventTime;
    }

    public void setEventTime(Instant eventTime) {
        this.eventTime = eventTime;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }
}
