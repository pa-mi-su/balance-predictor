# Balance Predictor

[![CI](https://github.com/pa-mi-su/balance-predictor/actions/workflows/ci.yml/badge.svg)](https://github.com/pa-mi-su/balance-predictor/actions/workflows/ci.yml)
[![Docker](https://img.shields.io/badge/docker-ready-blue)]()
[![License: MIT](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-17-orange)]()
[![Spring Boot](https://img.shields.io/badge/springboot-3.3.3-brightgreen)]()
[![Spring Cloud](https://img.shields.io/badge/springcloud-2023.0.3-blue)]()

A **microservices-based balance forecasting system** built with **Java 17, Spring Boot, Spring Cloud (Eureka), Docker, PostgreSQL, and Flyway**.
It demonstrates real-world enterprise patterns like **service discovery**, **API aggregation**, **database-backed persistence**, and **containerized deployment**.

---

## 🚀 Features

- **API Gateway (NEW)** → Central entry point for all services using **Spring Cloud Gateway**, routing through **Eureka Discovery**.
- **Balance Service** → Aggregates transactions and balances from Ledger & Plaid to compute projected balances.
- **Ledger Service (DB Layer Revamp)**
  - Fully migrated from in-memory storage to **PostgreSQL 16**
  - Uses **Spring Data JPA + Flyway migrations** for schema management
  - Persists all debit/credit events in a durable relational store.
- **Plaid Service (Mock)** → Simulated external bank API providing mock account balance data.
- **Eureka Server** → Service registry and discovery hub.
- **Swagger UI** → Interactive documentation for each service.
- **Docker Compose** → One-command startup of the entire platform.
- **Postman Collection (Gateway Edition)** → One-click testing of all API endpoints.
- **CI/CD (GitHub Actions)** → Builds, tests, and publishes multi-service Docker artifacts.

---

## 🧠 Idempotency Contract

The **Ledger Service** ensures **idempotent inserts** of user events — meaning **the same event cannot be stored twice**, even if re-sent.

### How It Works

1. **Application-Level Guard**
   - Before inserting, `LedgerService.addEvents()` checks for an existing record using
     `existsByUserIdAndDateAndAmountAndDescription()`.
   - This prevents most duplicates during normal operations.

2. **Database-Level Guarantee**
   - A **unique constraint** in the schema enforces idempotency at the DB level:
     ```sql
     CREATE UNIQUE INDEX uq_pending_events_natural
       ON pending_events(user_id, event_date, amount, description);
     ```
   - Any duplicate insert attempt triggers a `DataIntegrityViolationException`, which the app safely ignores.

3. **Safe Retries**
   - Reposting the same batch of events will not create new rows.
   - Example:
     ```bash
     # Add the same events twice
     curl -s -H "Content-Type: application/json"        -d '[{"date":"2025-10-05","amount":500.00,"description":"Paycheck"}]'        "http://localhost:8081/api/ledger/events?userId=1"
     ```
     Both requests return the same set of records, without duplicates.

4. **Post-Restart Persistence**
   - Data persists in Postgres — restarting `ledger-service` or any container does not clear events.

### Why It Matters

This guarantees that:

- API retries (due to gateway/network issues) **do not create double entries**
- Re-running an import job **is safe and consistent**
- Client operations remain **idempotent by design**

---

## 🧭 System Flows (What happens, where)

### 0) Startup & Discovery
1. **Eureka Server** starts (8761).
2. **Postgres** starts (health-checked).
3. **Ledger Service** (8082) starts → runs **Flyway** → registers with **Eureka** as `LEDGER-SERVICE`.
4. **Plaid Service** (8083) starts → registers as `PLAID-SERVICE` (mock).
5. **Balance Service** (8080) starts → registers as `BALANCE-SERVICE`.
6. **API Gateway** (8081) starts → registers as `API-GATEWAY` and enables discovery-based routing.

### 1) Health Checks
- **Direct:** `:8080/8082/8083/8761` `/actuator/health`.
- **Via Gateway:** `/balance-service/actuator/health`, `/ledger-service/actuator/health`, `/plaid-service/actuator/health` + gateway’s own `/actuator/health`.

### 2) Add Events (write path)
- **Call:** `POST /api/ledger/events?userId={id}` (gateway → ledger).
- **Ledger Service:**
  - App-level dedupe via repository `existsByUserIdAndDateAndAmountAndDescription`.
  - Persist via JPA to Postgres.
  - DB-level dedupe via unique index `(user_id, event_date, amount, description)`.
  - Return ordered events for the user.

### 3) Get Events (read path)
- **Call:** `GET /api/ledger/events?userId={id}` (gateway → ledger).
- **Ledger Service:** `findByUserIdOrderByDateAscIdAsc` → Postgres → returns events.

### 4) Projected Balance (aggregation)
- **Call:** `GET /api/balance/running?userId={id}` (gateway → balance).
- **Balance Service:**
  - Calls **Plaid** for current balance.
  - Calls **Ledger** for events.
  - Computes projected balance and returns it.

### 5) Eureka + Gateway routing
- Gateway forwards to `lb://{service}` using Eureka registrations (no hardcoded host:port).
- If a backend is down/unavailable, gateway returns `5xx/503` until it’s `UP` again.

### 6) Persistence & Idempotency Guarantees
- **Durability:** Events live in Postgres; container restarts do not lose data.
- **Idempotency:** Reposting the same `(userId, date, amount, description)` never duplicates a row (app guard + unique index).
- **Ordering:** Reads sorted by `(event_date ASC, id ASC)`.

### 7) Service Responsibilities (at a glance)
- **Eureka Server:** registry only.
- **API Gateway:** single entry point, routing, health.
- **Ledger Service:** owns event storage & listing (JPA + Flyway).
- **Plaid Service (mock):** supplies current balance.
- **Balance Service:** orchestrates Plaid + Ledger to compute projected balance.

---
balance-predictor/
├─ eureka-server/         # Service discovery (registry)
├─ api-gateway/           # Single entrypoint (Spring Cloud Gateway)
├─ ledger-service/        # Persists user events (PostgreSQL + JPA + Flyway)
├─ plaid-service/         # Mock provider for current balance
├─ balance-service/       # Aggregates Plaid + Ledger (projection calc)
├─ docker-compose.yml     # All containers + Postgres
├─ postman/               # Gateway collection
└─ pom.xml                # Root BOM/parent (version alignment)
---

## 💳 Plaid Sandbox Integration (October 2025)

The **Plaid Service** now integrates with the **Plaid Sandbox API** instead of static mock data.  
This enables real Plaid workflows including **account linking, token exchange, and live balance retrieval** — all within the sandbox environment.

### ✅ What’s New

- **Real Plaid Connectivity**
  - Uses Plaid’s official sandbox endpoints (`https://sandbox.plaid.com`).
  - Requires valid `PLAID_CLIENT_ID` and `PLAID_SECRET` from your Plaid Dashboard.
  - Automatically registered in Eureka and accessible via the API Gateway.

- **Endpoints**
  | Method | Endpoint | Description |
  |---------|-----------|-------------|
  | `POST` | `/api/plaid/sandbox/public_token/create` | Generates a sandbox `public_token` (simulates linking a bank account). |
  | `POST` | `/api/plaid/item/public_token/exchange` | Exchanges the `public_token` for a persistent `access_token`. |
  | `GET`  | `/api/plaid/balance?userId={id}` | Fetches live sandbox balances using the stored `access_token`. |

- **Environment Variables**
  ```bash
  PLAID_ENV=sandbox
  PLAID_CLIENT_ID=your_client_id_here
  PLAID_SECRET=your_secret_here
  PLAID_WEBHOOK_URL=
  ```

- **Example Usage**
  ```bash
  # 1. Create a sandbox public token
  curl -s -X POST "http://localhost:8081/api/plaid/sandbox/public_token/create"     -H "Content-Type: application/json"     -d '{"userId":1}'

  # 2. Exchange it for an access token
  curl -s -X POST "http://localhost:8081/api/plaid/item/public_token/exchange"     -H "Content-Type: application/json"     -d '{"userId":1,"publicToken":"public-sandbox-..."}'

  # 3. Fetch live sandbox balances
  curl -s "http://localhost:8081/api/plaid/balance?userId=1" | jq
  ```

- **Results**
  - Returns Plaid’s full sandbox dataset, including:
    - Plaid Checking / Savings / Credit / Mortgage / 401k accounts
    - Realistic balances and transaction-ready data

---

## 📜 License

This project is licensed under the **MIT License**.
