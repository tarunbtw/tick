# TICK

Simulated market event ingestion service built with Spring Boot and PostgreSQL.

## Overview

TICK is an event ingestion service designed around the core backend requirements of financial data systems: deterministic validation, idempotency, transactional writes, and bounded queries.

Market events are simulated and not connected to live exchange feeds.

## Tech Stack

- Java 17
- Spring Boot 3.3.4 (Spring Web, Spring Data JPA, Validation)
- PostgreSQL 16
- Flyway (schema migrations)
- JUnit 5 and Mockito
- Docker and Docker Compose

## Architecture and Key Decisions

### 1. Dual-Layer Idempotency
`eventId` is the producer-assigned idempotency key. Duplicate submissions must never produce multiple records.
- **Application check**: `EventIngestionService` checks for record existence before saving.
- **Database constraint**: PostgreSQL enforces `UNIQUE (event_id)` on the `market_events` table. Concurrent requests racing past the application check are stopped by PostgreSQL. The service catches the constraint violation (`DataIntegrityViolationException`) and returns `409 Conflict`.

### 2. Transaction Boundaries
- Transaction management lives in the service layer (`@Transactional`).
- Audit logs use `Propagation.REQUIRES_NEW` so that audit records are committed even if the primary ingestion operation fails or rolls back.

### 3. Querying and Pagination
- Repeated queries must yield deterministic results. Results are ordered by `eventTime DESC, id DESC`.
- Page size is clamped between 1 and 100 (default 20) to prevent unbounded memory allocation.
- Dynamic filtering uses Spring Data JPA specifications across instrument symbol, event type, and time bounds.

## Project Structure

```text
src/
├── main/
│   ├── java/com/tick/market/
│   │   ├── config/            # Jackson and Web MVC configuration
│   │   ├── controller/        # REST endpoints
│   │   ├── dto/               # Request, response, error payloads
│   │   ├── entity/            # JPA entities and enums
│   │   ├── exception/         # Exceptions and global ControllerAdvice
│   │   ├── repository/        # Spring Data repositories
│   │   ├── service/           # Ingestion, query, and audit business logic
│   │   ├── specification/     # Dynamic JPA query specifications
│   │   ├── validator/         # Domain payload validation rules
│   │   └── TickApplication.java
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-test.yml
│       └── db/migration/      # Flyway SQL migrations
└── test/
    └── java/com/tick/market/  # Unit, MockMvc, and DataJpaTest suites
```

## API Specification

### Ingest Event

```http
POST /api/v1/events
Content-Type: application/json
```

Request body:

```json
{
  "eventId": "evt-001",
  "symbol": "AAPL",
  "eventType": "TRADE",
  "price": 225.42,
  "quantity": 100,
  "eventTime": "2026-10-10T08:30:00Z"
}
```

Response (`201 Created`):

```json
{
  "status": "ACCEPTED",
  "eventId": "evt-001",
  "receivedAt": "2026-10-10T08:30:01.123456Z",
  "message": "Event successfully ingested"
}
```

Error responses:
- `400 Bad Request`: Payload validation failure (missing required fields, invalid symbol, negative price/quantity, or invalid timestamp).
- `409 Conflict`: Event with the same `eventId` has already been ingested.

### Query Events

```http
GET /api/v1/events
```

Query parameters:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `symbol` | String | No | Case-insensitive instrument symbol (e.g. `AAPL`) |
| `eventType` | String | No | Event classification (`TRADE`, `QUOTE`, `PRICE_UPDATE`, `ORDER_CANCEL`, `HALT`) |
| `from` | ISO-8601 | No | Inclusive start timestamp UTC (e.g. `2026-10-10T00:00:00Z`) |
| `to` | ISO-8601 | No | Inclusive end timestamp UTC (e.g. `2026-10-10T23:59:59Z`) |
| `page` | Integer | No | Zero-based page index (default: 0) |
| `size` | Integer | No | Page size clamped between 1 and 100 (default: 20) |

Response (`200 OK`):

```json
{
  "content": [
    {
      "id": 1,
      "eventId": "evt-001",
      "symbol": "AAPL",
      "eventType": "TRADE",
      "price": 225.42,
      "quantity": 100.0,
      "eventTime": "2026-10-10T08:30:00Z",
      "receivedAt": "2026-10-10T08:30:01.123456Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "isFirst": true,
  "isLast": true
}
```

### Get Event by ID

```http
GET /api/v1/events/{eventId}
```

Returns `200 OK` with the event object, or `404 Not Found` if missing.

## Database Schema

Migrations are managed via Flyway (`V1__init_schema.sql`).

### `market_events`
- `id`: BIGSERIAL PRIMARY KEY
- `event_id`: VARCHAR(128) NOT NULL UNIQUE
- `symbol`: VARCHAR(32) NOT NULL
- `event_type`: VARCHAR(32) NOT NULL
- `price`: NUMERIC(19, 4)
- `quantity`: NUMERIC(19, 4)
- `event_time`: TIMESTAMPTZ NOT NULL
- `received_at`: TIMESTAMPTZ NOT NULL
- `created_at`: TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP

Indexes:
- `idx_market_events_symbol_time (symbol, event_time DESC)`
- `idx_market_events_type_time (event_type, event_time DESC)`
- `idx_market_events_event_time (event_time DESC)`

### `audit_records`
- `id`: BIGSERIAL PRIMARY KEY
- `event_id`: VARCHAR(128) NOT NULL
- `operation`: VARCHAR(64) NOT NULL
- `status`: VARCHAR(32) NOT NULL
- `message`: TEXT
- `recorded_at`: TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP

Indexes:
- `idx_audit_records_event_id (event_id)`
- `idx_audit_records_recorded_at (recorded_at DESC)`

## Local Development

### Requirements
- JDK 17
- Docker and Docker Compose
- Maven 3.8+

### 1. Start PostgreSQL

```bash
docker compose up -d
```

Starts PostgreSQL on port `5432` with database `tick_db` and user `tick_user`.

### 2. Run the Service

```bash
mvn spring-boot:run
```

The application runs on `http://localhost:8080`.

### 3. Run Tests

```bash
mvn test
```

Unit and slice tests run against an in-memory database using the `test` profile.
