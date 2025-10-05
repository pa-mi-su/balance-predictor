# Balance Predictor

[![CI](https://github.com/pa-mi-su/balance-predictor/actions/workflows/ci.yml/badge.svg)](https://github.com/pa-mi-su/balance-predictor/actions/workflows/ci.yml)
[![Docker](https://img.shields.io/badge/docker-ready-blue)]()
[![License:
MIT](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-17-orange)]() [![Spring
Boot](https://img.shields.io/badge/springboot-3.3.3-brightgreen)]()
[![Spring
Cloud](https://img.shields.io/badge/springcloud-2023.0.3-blue)]()

A **microservices-based balance forecasting system** built with **Java
17, Spring Boot, Spring Cloud (Eureka), Docker, PostgreSQL, and
Flyway**.\
It demonstrates real-world enterprise patterns like **service
discovery**, **API aggregation**, **database-backed persistence**, and
**containerized deployment**.

------------------------------------------------------------------------

## 🚀 Features

-   **API Gateway (NEW)** → Central entry point for all services using
    **Spring Cloud Gateway**, routing through **Eureka Discovery**.
-   **Balance Service** → Aggregates transactions and balances from
    Ledger & Plaid to compute projected balances.
-   **Ledger Service (DB Layer Revamp)** →
    -   Fully migrated from in-memory storage to **PostgreSQL 16**.\
    -   Uses **Spring Data JPA + Flyway migrations** for schema
        management.\
    -   Persists all debit/credit events in a durable relational store.
-   **Plaid Service (Mock)** → Simulated external bank API providing
    mock account balance data.
-   **Eureka Server** → Service registry and discovery hub.
-   **Swagger UI** → Interactive documentation for each service.
-   **Docker Compose** → One-command startup of the entire platform.
-   **Postman Collection (Gateway Edition)** → One-click testing of all
    API endpoints.
-   **CI/CD (GitHub Actions)** → Builds, tests, and publishes
    multi-service Docker artifacts.

------------------------------------------------------------------------

## 🧩 DB Layer Upgrade (October 2025)

The **Ledger Service** now has a full persistence layer and migration
support.

### ✅ Highlights

-   Added **PostgreSQL 16 (bpdb)** container in Docker Compose.

-   Added **Flyway migrations** (`V1__create_pending_events.sql`) for
    schema bootstrap.

-   Introduced **`PendingEvent` JPA entity** and
    **`PendingEventRepository`**.

-   Updated **LedgerService** and **LedgerController** to perform real
    DB reads/writes.

-   Removed all legacy **in-memory model classes**.

-   Added healthchecks for `bp-postgres` and service dependency
    ordering.

-   Updated Maven dependencies to include:

    ``` xml
    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>org.flywaydb</groupId>
      <artifactId>flyway-core</artifactId>
    </dependency>
    ```

-   Verified schema migration with Flyway at container startup:

        Database: jdbc:postgresql://postgres:5432/bpdb (PostgreSQL 16.10)
        Successfully applied 1 migration to schema "public", now at version v1

### 🧠 DB Entity Example

``` java
@Entity
@Table(name = "pending_events")
public class PendingEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private LocalDate date;
    private BigDecimal amount;
    private String description;
}
```

------------------------------------------------------------------------

## 🛠️ Tech Stack

-   **Java 17**
-   **Spring Boot 3.3.3**
-   **Spring Cloud 2023.0.3**
-   **PostgreSQL 16**
-   **Spring Data JPA**
-   **Flyway 10**
-   **Springdoc OpenAPI**
-   **Docker / Docker Compose**
-   **GitHub Actions**
-   **Postman**

------------------------------------------------------------------------

## 📦 Getting Started

### 1. Clone & Build

``` bash
git clone git@github.com:pa-mi-su/balance-predictor.git
cd balance-predictor
```

### 2. Run the Full Stack

``` bash
docker compose down -v --remove-orphans
docker compose build --no-cache
docker compose up -d
```

### 3. Verify Services

``` bash
docker compose ps
curl -s http://localhost:8761/actuator/health   # Eureka
curl -s http://localhost:8082/actuator/health   # Ledger
curl -s http://localhost:8080/actuator/health   # Balance
curl -s http://localhost:8083/actuator/health   # Plaid
curl -s http://localhost:8081/actuator/health   # Gateway
```

------------------------------------------------------------------------

## 🗄️ Database Configuration

The database service (`bp-postgres`) is included in
`docker-compose.yml`.

  ---------------------------------------------------------------------------------------------------
  Env Var                   Description                      Value
  ------------------------- -------------------------------- ----------------------------------------
  `POSTGRES_DB`             Database name                    `bpdb`

  `POSTGRES_USER`           Username                         `bpuser`

  `POSTGRES_PASSWORD`       Password                         `bppass`

  `SPRING_DATASOURCE_URL`   JDBC URL                         `jdbc:postgresql://postgres:5432/bpdb`
  ---------------------------------------------------------------------------------------------------

------------------------------------------------------------------------

## 🌉 API Gateway Overview

  Route Prefix        Target Service           Description
  ------------------- ------------------------ ------------------------
  `/api/balance/**`   `lb://balance-service`   Aggregated projections
  `/api/ledger/**`    `lb://ledger-service`    DB-backed transactions
  `/api/plaid/**`     `lb://plaid-service`     Mock Plaid endpoints

Each route uses `StripPrefix=1` for clean forwarding.\
Runs on **port 8081**.

------------------------------------------------------------------------

## 💡 Example End-to-End Flow

``` bash
# 1. Get Plaid mock balance
curl -s "http://localhost:8081/api/plaid/balance?userId=1"

# 2. Add Ledger events (DB write)
curl -s -H "Content-Type: application/json"   -d '[{"date":"2025-10-05","amount":-60.0,"description":"Dinner"},{"date":"2025-10-06","amount":500.0,"description":"Paycheck"}]'   "http://localhost:8081/api/ledger/events?userId=1"

# 3. Fetch persisted Ledger events (DB read)
curl -s "http://localhost:8081/api/ledger/events?userId=1"

# 4. Compute projected balance
curl -s "http://localhost:8081/api/balance/running?userId=1"
```

------------------------------------------------------------------------

## 📊 Postman Collection

File: `postman/BalancePredictor-Gateway.postman_collection.json`

Includes one-click: - Health checks - Ledger DB CRUD tests - Projected
balance aggregation - End-to-end flow (Plaid → Ledger → Balance)

Import into Postman and run the "End-to-End Flow" folder to verify full
system behavior.

------------------------------------------------------------------------

## 📚 Swagger UIs

  -----------------------------------------------------------------------------------------
  Service                                         URL
  ----------------------------------------------- -----------------------------------------
  Balance                                         <http://localhost:8080/swagger-ui.html>

  Ledger                                          <http://localhost:8082/swagger-ui.html>

  Plaid                                           <http://localhost:8083/swagger-ui.html>

  Eureka                                          <http://localhost:8761>
  -----------------------------------------------------------------------------------------

------------------------------------------------------------------------

## 🧱 Architecture Overview

              ┌────────────────┐
              │  API Gateway   │  (8081)
              └──────┬─────────┘
                     │
         ┌───────────┼──────────────┐
         │           │              │
    ┌────┴────┐ ┌────┴────┐ ┌──────┴────┐
    │ Balance │ │ Ledger  │ │  Plaid     │
    │ Service │ │ Service │ │  Service   │
    │ (8080)  │ │ (8082)  │ │ (8083)    │
    └─────────┘ └─────────┘ └───────────┘
           │             │
           └─────────────┘
               PostgreSQL (bpdb)

------------------------------------------------------------------------

## 🤖 CI/CD

GitHub Actions: - Lint, compile, and test on PRs. - Build `.jar`
artifacts for all services. - Optional Docker image publishing workflow.

------------------------------------------------------------------------

## 📂 Project Layout

    balance-predictor/
    ├── api-gateway/         # Spring Cloud Gateway
    ├── balance-service/     # Aggregates ledger + plaid
    ├── ledger-service/      # DB-backed ledger (JPA + Flyway)
    │   └── src/main/resources/db/migration/V1__create_pending_events.sql
    ├── plaid-service/       # Mock bank API
    ├── eureka-server/       # Discovery service
    ├── docker-compose.yml   # Full-stack orchestration
    ├── postman/             # Postman test suite
    └── README.md

------------------------------------------------------------------------

## 📜 License

This project is licensed under the **MIT License**.
