package com.tick.market.dto;

import com.tick.market.entity.AuditStatus;
import java.time.Instant;

/**
 * DTO returned upon submission to the ingestion endpoint (POST /api/v1/events).
 */
public class IngestResponse {

    private AuditStatus status;
    private String eventId;
    private Instant receivedAt;
    private String message;

    public IngestResponse() {
    }

    public IngestResponse(AuditStatus status, String eventId, Instant receivedAt, String message) {
        this.status = status;
        this.eventId = eventId;
        this.receivedAt = receivedAt;
        this.message = message;
    }

    public static IngestResponse accepted(String eventId, Instant receivedAt) {
        return new IngestResponse(AuditStatus.ACCEPTED, eventId, receivedAt, "Event successfully ingested");
    }

    public static IngestResponse duplicate(String eventId, Instant receivedAt) {
        return new IngestResponse(AuditStatus.DUPLICATE, eventId, receivedAt, "Event already exists");
    }

    public AuditStatus getStatus() {
        return status;
    }

    public void setStatus(AuditStatus status) {
        this.status = status;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
