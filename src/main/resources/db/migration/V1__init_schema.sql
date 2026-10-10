-- V1__init_schema.sql: Initial schema for TICK (Market Events and Audit Records)

-- 1. Market Events Table
CREATE TABLE IF NOT EXISTS market_events (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(128) NOT NULL,
    symbol VARCHAR(32) NOT NULL,
    event_type VARCHAR(32) NOT NULL,
    price NUMERIC(19, 4),
    quantity NUMERIC(19, 4),
    event_time TIMESTAMPTZ NOT NULL,
    received_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Enforce idempotency at database level
    CONSTRAINT uq_market_events_event_id UNIQUE (event_id)
);

-- Indexes for efficient querying, filtering, and deterministic sorting
CREATE INDEX IF NOT EXISTS idx_market_events_symbol_time 
    ON market_events (symbol, event_time DESC);

CREATE INDEX IF NOT EXISTS idx_market_events_type_time 
    ON market_events (event_type, event_time DESC);

CREATE INDEX IF NOT EXISTS idx_market_events_event_time 
    ON market_events (event_time DESC);

-- 2. Audit Records Table
CREATE TABLE IF NOT EXISTS audit_records (
    id BIGSERIAL PRIMARY KEY,
    event_id VARCHAR(128) NOT NULL,
    operation VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    message TEXT,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for auditing by event ID and time
CREATE INDEX IF NOT EXISTS idx_audit_records_event_id 
    ON audit_records (event_id);

CREATE INDEX IF NOT EXISTS idx_audit_records_recorded_at 
    ON audit_records (recorded_at DESC);
