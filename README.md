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

## 🧩 DB Layer Upgrade (October 2025)

The **Ledger Service** now has a full persistence layer and migration support.

### ✅ Highlights

- Added **PostgreSQL 16 (bpdb)** container in Docker Compose  
- Added **Flyway migrations** (`V1__create_pending_events.sql`) for schema bootstrap  
- Introduced **`PendingEvent` JPA entity** and **`PendingEventRepository`**  
- Updated **LedgerService** and **LedgerController** to perform real DB reads/writes  
- Removed all legacy **in-memory model classes**  
- Added health checks for `bp-postgres` and service dependency ordering  
- Updated Maven dependencies to include PostgreSQL & Flyway  
- Verified schema migration with Flyway at container startup:

  ```
  Database: jdbc:postgresql://postgres:5432/bpdb (PostgreSQL 16.10)
  Successfully applied 1 migration to schema "public", now at version v1
  ```

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

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot 3.3.3**
- **Spring Cloud 2023.0.3**
- **PostgreSQL 16**
- **Spring Data JPA**
- **Flyway 10**
- **Springdoc OpenAPI**
- **Docker / Docker Compose**
- **GitHub Actions**
- **Postman**

---

## 📦 Getting Started

### 1. Clone & Build

```bash
git clone git@github.com:pa-mi-su/balance-predictor.git
cd balance-predictor
```

### 2. Run the Full Stack

```bash
docker compose down -v --remove-orphans
docker compose build --no-cache
docker compose up -d
```

### 3. Verify Services

```bash
docker compose ps
curl -s http://localhost:8761/actuator/health   # Eureka
curl -s http://localhost:8082/actuator/health   # Ledger
curl -s http://localhost:8080/actuator/health   # Balance
curl -s http://localhost:8083/actuator/health   # Plaid
curl -s http://localhost:8081/actuator/health   # Gateway
```

---

## 💡 Example End-to-End Flow

```bash
# 1. Get Plaid mock balance
curl -s "http://localhost:8081/api/plaid/balance?userId=1"

# 2. Add Ledger events (DB write)
curl -s -H "Content-Type: application/json"   -d '[{"date":"2025-10-05","amount":-60.0,"description":"Dinner"},
       {"date":"2025-10-06","amount":500.0,"description":"Paycheck"}]'   "http://localhost:8081/api/ledger/events?userId=1"

# 3. Fetch persisted Ledger events (DB read)
curl -s "http://localhost:8081/api/ledger/events?userId=1"

# 4. Compute projected balance
curl -s "http://localhost:8081/api/balance/running?userId=1"
```

---

## 📊 Postman Collection

File: `postman/BalancePredictor-Gateway.postman_collection.json`

Includes one-click tests for:
- Health checks  
- Ledger DB CRUD  
- Projected balance aggregation  
- End-to-end workflow (Plaid → Ledger → Balance)

---

## 📚 Swagger UIs

| Service | URL |
|----------|-----|
| Balance | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) |
| Ledger | [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html) |
| Plaid | [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html) |
| Eureka | [http://localhost:8761](http://localhost:8761) |

---

## 🤖 CI/CD

GitHub Actions:
- Builds & tests all modules on PRs  
- Packages Docker images and JARs on push  
- Runs full Maven verify lifecycle  

---

## 📂 Project Layout

```
balance-predictor/
├── api-gateway/
├── balance-service/
├── ledger-service/
│   └── src/main/resources/db/migration/V1__create_pending_events.sql
├── plaid-service/
├── eureka-server/
├── docker-compose.yml
├── postman/
└── README.md
```

---

## 📜 License

This project is licensed under the **MIT License**.
