# payment-orchestrator

A Spring Boot service that records payment intents and orchestrates them through
their lifecycle. Today it exposes a single idempotent endpoint for creating a
payment; the status machine (`CREATED → AUTHORIZED → CAPTURED → SETTLED`, plus
`REFUNDED` / `FAILED`) is modelled but not yet driven.

## Stack

| | |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.1 (Web MVC, Data JPA, Validation) |
| Database | PostgreSQL 17 (via Docker Compose) |
| Build | Maven (wrapper included) |

## Running locally

Start the database:

```bash
docker compose up -d
```

Postgres listens on **host port 5434** (5432 and 5433 are assumed taken by local
Homebrew instances), with database/user/password all `payment` /
`payment_orchestrator`.

Then run the app:

```bash
./mvnw spring-boot:run
```

It starts on `http://localhost:8080`. Hibernate is set to `ddl-auto=update`, so
the `payments` table is created on first boot — no migration step yet.

Other useful commands:

```bash
./mvnw test          # run tests (needs the database up — contextLoads boots the full app)
./mvnw package       # build the executable jar into target/
docker compose down  # stop the database (add -v to also drop the data volume)
```

## Configuration

Everything in `src/main/resources/application.properties` that matters can be
overridden by environment variable:

| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5434/payment_orchestrator` |
| `DB_USER` | `payment` |
| `DB_PASSWORD` | `payment` |

## API

### `POST /payments`

Creates a payment. Requires an `Idempotency-Key` header: replaying the same key
returns the payment that key already created instead of creating a second one.

Request:

```bash
curl -X POST http://localhost:8080/payments \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: 8f1c0b2a-0f4b-4a1e-9a7a-1b2c3d4e5f60' \
  -d '{
        "merchantId": "merchant_42",
        "customerId": "customer_7",
        "amount": 2500,
        "currency": "USD"
      }'
```

`amount` is in the currency's minor units (2500 = $25.00) and must be positive;
`merchantId`, `customerId` and `currency` must be non-blank.

Response `200 OK`:

```json
{
  "paymentId": "3b2f7d1e-5c8a-4f6b-9e0d-7a1c2b3d4e5f",
  "merchantId": "merchant_42",
  "customerId": "customer_7",
  "amount": 2500,
  "currency": "USD",
  "status": "CREATED"
}
```

## Layout

```
src/main/java/com/tejashvi/payment/
├── PaymentOrchestratorApplication.java
├── controller/PaymentController.java   # HTTP layer
├── service/PaymentService.java         # idempotency check + persistence
├── repository/PaymentRepository.java   # Spring Data JPA
├── domain/Payment.java                 # entity; status, timestamps, idempotency key
├── domain/PaymentStatus.java
└── dto/                                # request/response records
```

## Known gaps

These are deliberate for now, and worth knowing before building on top:

- **Schema management.** `ddl-auto=update` is fine while the schema is forming;
  switch to `validate` and introduce Flyway or Liquibase before any real
  deployment.
- **Idempotency is check-then-write.** Two concurrent requests with the same key
  can both pass the lookup and race to insert. The unique constraint on
  `idempotency_key` stops the duplicate row, but the loser surfaces as a
  constraint violation rather than a replayed response.
- **No error handling.** There is no `@ControllerAdvice`, so validation failures
  and constraint violations fall through to Spring's default error responses. A
  missing `Idempotency-Key` header is a 400 with no explanation.
- **No lifecycle transitions.** `PaymentStatus` beyond `CREATED` is unused —
  there is no authorize, capture, or refund path, and no PSP integration.
- **No read endpoint.** Once created, a payment can only be fetched from the
  database directly.
- **Thin tests.** Only `contextLoads` exists, and it needs a live database.
# payment-orchestrator
