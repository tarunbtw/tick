package com.tick.market.entity;

/**
 * Outcome status recorded for an ingestion or query audit log.
 */
public enum AuditStatus {
    ACCEPTED,
    DUPLICATE,
    REJECTED,
    FAILED
}
