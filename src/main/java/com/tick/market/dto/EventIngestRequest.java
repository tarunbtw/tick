package com.tick.market.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tick.market.entity.EventType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Request payload for ingesting a simulated market event into TICK.
 */
public class EventIngestRequest {

    @NotBlank(message = "eventId is required and must not be blank")
    @Size(max = 128, message = "eventId must not exceed 128 characters")
    private String eventId;

    @NotBlank(message = "symbol is required and must not be blank")
    @Size(min = 1, max = 32, message = "symbol length must be between 1 and 32 characters")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "symbol must only contain alphanumeric characters, dots, underscores, or hyphens")
    private String symbol;

    @NotNull(message = "eventType is required and must be a valid market event type")
    private EventType eventType;

    @DecimalMin(value = "0.0", inclusive = false, message = "price must be strictly positive when provided")
    @Digits(integer = 15, fraction = 4, message = "price can have up to 15 integer digits and 4 decimal places")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = false, message = "quantity must be strictly positive when provided")
    @Digits(integer = 15, fraction = 4, message = "quantity can have up to 15 integer digits and 4 decimal places")
    private BigDecimal quantity;

    @NotNull(message = "eventTime is required and must be an ISO-8601 UTC timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant eventTime;

    public EventIngestRequest() {
    }

    public EventIngestRequest(
        String eventId,
        String symbol,
        EventType eventType,
        BigDecimal price,
        BigDecimal quantity,
        Instant eventTime
    ) {
        this.eventId = eventId;
        this.symbol = symbol;
        this.eventType = eventType;
        this.price = price;
        this.quantity = quantity;
        this.eventTime = eventTime;
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

    @Override
    public String toString() {
        return "EventIngestRequest{" +
                "eventId='" + eventId + '\'' +
                ", symbol='" + symbol + '\'' +
                ", eventType=" + eventType +
                ", price=" + price +
                ", quantity=" + quantity +
                ", eventTime=" + eventTime +
                '}';
    }
}
