package com.tick.market.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * JPA entity representing a simulated market event persisted in TICK.
 */
@Entity
@Table(
    name = "market_events",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_market_events_event_id", columnNames = {"event_id"})
    }
)
public class MarketEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 128)
    private String eventId;

    @Column(name = "symbol", nullable = false, length = 32)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 32)
    private EventType eventType;

    @Column(name = "price", precision = 19, scale = 4)
    private BigDecimal price;

    @Column(name = "quantity", precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "event_time", nullable = false)
    private Instant eventTime;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public MarketEvent() {
    }

    public MarketEvent(
        String eventId,
        String symbol,
        EventType eventType,
        BigDecimal price,
        BigDecimal quantity,
        Instant eventTime,
        Instant receivedAt
    ) {
        this.eventId = eventId;
        this.symbol = symbol;
        this.eventType = eventType;
        this.price = price;
        this.quantity = quantity;
        this.eventTime = eventTime;
        this.receivedAt = receivedAt != null ? receivedAt : Instant.now();
    }

    @PrePersist
    protected void onPrePersist() {
        Instant now = Instant.now();
        if (this.receivedAt == null) {
            this.receivedAt = now;
        }
        if (this.createdAt == null) {
            this.createdAt = now;
        }
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarketEvent that = (MarketEvent) o;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return "MarketEvent{" +
                "id=" + id +
                ", eventId='" + eventId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", eventType=" + eventType +
                ", price=" + price +
                ", quantity=" + quantity +
                ", eventTime=" + eventTime +
                ", receivedAt=" + receivedAt +
                ", createdAt=" + createdAt +
                '}';
    }
}
