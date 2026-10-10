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
import java.time.Instant;
import java.util.Objects;

/**
 * JPA entity capturing operational audit trails for event ingestion attempts.
 */
@Entity
@Table(name = "audit_records")
public class AuditRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 128)
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation", nullable = false, length = 64)
    private AuditOperation operation;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private AuditStatus status;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;

    public AuditRecord() {
    }

    public AuditRecord(String eventId, AuditOperation operation, AuditStatus status, String message) {
        this.eventId = eventId;
        this.operation = operation;
        this.status = status;
        this.message = message;
        this.recordedAt = Instant.now();
    }

    @PrePersist
    protected void onPrePersist() {
        if (this.recordedAt == null) {
            this.recordedAt = Instant.now();
        }
    }

    public static AuditRecord of(String eventId, AuditOperation operation, AuditStatus status, String message) {
        return new AuditRecord(eventId, operation, status, message);
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

    public AuditOperation getOperation() {
        return operation;
    }

    public void setOperation(AuditOperation operation) {
        this.operation = operation;
    }

    public AuditStatus getStatus() {
        return status;
    }

    public void setStatus(AuditStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditRecord that = (AuditRecord) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AuditRecord{" +
                "id=" + id +
                ", eventId='" + eventId + '\'' +
                ", operation=" + operation +
                ", status=" + status +
                ", message='" + message + '\'' +
                ", recordedAt=" + recordedAt +
                '}';
    }
}
