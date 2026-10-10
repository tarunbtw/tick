# TICK

A Spring Boot service for ingesting, validating, storing, and querying simulated market events.

## Overview

TICK models a small backend service for market-style event data. It focuses on reliable ingestion rather than trading or market-data distribution.

The service is designed around:

- request validation
- idempotent event ingestion
- transactional database operations
- event filtering and pagination
- audit records
- automated tests

Market events are simulated and are not sourced from a live exchange.

## Stack

- Java 17
- Spring Boot 3.3.4
- PostgreSQL 16
- Flyway (Database Migrations)
- JUnit 5 / Mockito
- Docker & Docker Compose

## Project Structure

The application follows a conventional Spring Boot structure:

```text
src/
├── main/
│   ├── java/
│   │   └── com/tick/market/
│   │       ├── config/           # Spring & Jackson configuration
│   │       ├── controller/       # REST API endpoints
│   │       ├── dto/              # Request, response, error DTOs
│   │       ├── entity/           # JPA entities & domain enums
│   │       ├── exception/        # Custom exceptions & global advice
│   │       ├── repository/       # Spring Data JPA repositories
│   │       ├── service/          # Business logic & transaction boundaries
│   │       ├── specification/   # Dynamic query specifications
│   │       └── validator/        # Custom validation logic
│   └── resources/
│       ├── application.yml       # Application properties
│       ├── application-dev.yml   # Dev PostgreSQL configuration
│       └── db/migration/         # Flyway SQL migration scripts
└── test/
    └── java/
        └── com/tick/market/      # Unit, integration, and web slice tests
```

The package structure separates API/controllers, services, persistence, domain models, and configuration.

## API

### Create an event

```http
POST /api/v1/events
Content-Type: application/json
```

Example request:

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

The `eventId` is used as the idempotency key. Submitting the same event more than once must not create duplicate event records and returns `409 Conflict`.

### Query events

```http
GET /api/v1/events
```

Supported query filters:

| Parameter | Type | Description |
|-----------|------|-------------|
| `symbol` | `string` | Filter by instrument symbol (e.g. `AAPL`) |
| `eventType` | `string` | Filter by event type (`TRADE`, `QUOTE`, etc.) |
| `from` | `ISO-8601` | Minimum timestamp inclusive (e.g. `2026-10-10T00:00:00Z`) |
| `to` | `ISO-8601` | Maximum timestamp inclusive (e.g. `2026-10-10T23:59:59Z`) |
| `page` | `integer` | Page index (0-indexed, default `0`) |
| `size` | `integer` | Page size (1–100, default `20`) |

Example:

```http
GET /api/v1/events?symbol=AAPL&eventType=TRADE&page=0&size=20
```

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

### Get event by ID

```http
GET /api/v1/events/{eventId}
```

## Idempotency

TICK treats `eventId` as a unique identifier for an incoming event.

Idempotency is enforced at two levels:

1. **Application level**: The ingestion service queries the repository for an existing event identifier before saving.
2. **Database level**: PostgreSQL enforces a `UNIQUE` constraint on the `event_id` column.

The database constraint is required because an application-level existence check alone is not sufficient to prevent duplicates under concurrent requests. If concurrent requests race past the application check, the database constraint rejects the second insert, which the service captures and translates into a `409 Conflict` response.

## Transactions

Event persistence and its associated audit operation are handled within an appropriate transaction boundary using Spring's `@Transactional`.

The goal is to prevent partially committed state when a write operation fails.

## Data

The initial persistence model contains two main concepts:

### Market Event

Stores the simulated market event and its associated timestamps.

- `id`: Auto-generated primary key
- `event_id`: Unique producer-supplied event identifier
- `symbol`: Instrument identifier (e.g., `AAPL`)
- `event_type`: Type of market event (`TRADE`, `QUOTE`, `PRICE_UPDATE`, `ORDER_CANCEL`, `HALT`)
- `price`: Event price
- `quantity`: Event quantity
- `event_time`: Event producer timestamp (UTC)
- `received_at`: Service receipt timestamp (UTC)
- `created_at`: Row creation timestamp

Composite and selective indexes (`symbol, event_time`, `event_type, event_time`, `event_time`) optimize query performance.

### Audit Record

Stores information about the ingestion operation, including its result, error message, and timestamp:

- `id`: Primary key
- `event_id`: Targeted event identifier
- `operation`: Action performed (`INGEST_EVENT`)
- `status`: Outcome status (`ACCEPTED`, `DUPLICATE`, `REJECTED`, `FAILED`)
- `message`: Context or failure details
- `recorded_at`: Timestamp of the audit event

## Testing

The test suite covers the behavior that matters to the service, including:

- request validation (positive numbers, ISO timestamps, required fields)
- duplicate event handling (idempotency checks)
- concurrent duplicate submissions (database unique constraint translation)
- transactional behavior and atomicity
- filtering by symbol, event type, and date range
- pagination bounds and stable result ordering

## Running Locally

### Requirements

- JDK 17
- Maven 3.8+
- Docker & Docker Compose

### 1. Start Database

Start PostgreSQL via Docker Compose:

```bash
docker compose up -d
```

### 2. Run the Application

Run the Spring Boot application using Maven:

```bash
mvn spring-boot:run
```

The service starts on `http://localhost:8080`.

### 3. Run Tests

Execute the automated test suite:

```bash
mvn test
```

## Development Notes

The project intentionally keeps the API and infrastructure small.

The main implementation concerns are:

- keeping validation separate from persistence logic
- defining clear transaction boundaries in the service layer
- enforcing uniqueness in PostgreSQL
- avoiding unbounded query results through strict pagination caps
- keeping API error responses consistent
- testing failure and concurrency cases

## Status

TICK is under active development. Core domain entities, Flyway migrations, ingestion and query services, validation rules, REST controllers, and test harnesses are implemented.
