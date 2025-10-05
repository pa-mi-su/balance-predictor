# Balance Predictor

[![CI](https://github.com/pa-mi-su/balance-predictor/actions/workflows/ci.yml/badge.svg)](https://github.com/pa-mi-su/balance-predictor/actions/workflows/ci.yml)
[![Docker](https://img.shields.io/badge/docker-ready-blue)]()
[![License: MIT](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)
[![Java](https://img.shields.io/badge/java-17-orange)]()
[![Spring Boot](https://img.shields.io/badge/springboot-3.3.3-brightgreen)]()
[![Spring Cloud](https://img.shields.io/badge/springcloud-2023.0.3-blue)]()

A **microservices-based balance forecasting system** built with **Java 17, Spring Boot, Spring Cloud (Eureka), Docker, and Docker Compose**.  
It demonstrates real-world patterns like **service discovery**, **API aggregation**, and **containerized deployment**.

---

## 🚀 Features
- **API Gateway (NEW)** → Central entry point for all services using **Spring Cloud Gateway**, routing requests to backend services through **Eureka Discovery**.
- **Balance Service** → Aggregates data from Ledger & Plaid services and calculates projected balances.
- **Ledger Service** → Stores and returns user debit/credit events.
- **Plaid Service (Mock)** → Simulates a bank API returning current balance information.
- **Eureka Server** → Service registry for discovery & load balancing.
- **Swagger UI** → Interactive API documentation for all services.
- **Docker Compose Setup** → Run the entire system with a single command.
- **CI/CD** → GitHub Actions builds & tests modules and uploads artifacts.
- **Postman Collection** → One-click API testing for all endpoints.

---

## 🛠️ Tech Stack
- **Java 17**
- **Spring Boot 3.3.3**
- **Spring Cloud 2023.0.3 (Eureka Server + Client, LoadBalancer, Gateway)**
- **Spring WebFlux (WebClient)** with `@LoadBalanced` for service discovery
- **Docker & Docker Compose** for containerization
- **Springdoc OpenAPI** for Swagger UI
- **GitHub Actions** for CI/CD pipelines
- **Postman** for API testing

---

## 📦 Getting Started

### 1. Clone the repo
```bash
git clone git@github.com:pa-mi-su/balance-predictor.git
cd balance-predictor
```

### 2. Build & Run with Docker Compose
```bash
docker compose down -v --remove-orphans
docker compose build --no-cache
docker compose up -d
```

### 3. Verify services are healthy
```bash
docker compose ps
curl -s http://localhost:8761/actuator/health   # Eureka Server
curl -s http://localhost:8080/actuator/health   # Balance Service
curl -s http://localhost:8082/actuator/health   # Ledger Service
curl -s http://localhost:8083/actuator/health   # Plaid Service
curl -s http://localhost:8081/actuator/health   # API Gateway
```

---

## 🌉 API Gateway Overview

The **API Gateway** (Spring Cloud Gateway) runs on **port 8081** and provides a unified access layer to all backend services.  
It uses **Eureka service discovery** to dynamically route traffic without hardcoded URLs.

### Default Routes

| Service | Gateway Route | Target Service (via Eureka) |
|----------|----------------|-----------------------------|
| Balance Service | `/api/balance/**` | `lb://balance-service` |
| Ledger Service  | `/api/ledger/**`  | `lb://ledger-service`  |
| Plaid Service   | `/api/plaid/**`   | `lb://plaid-service`   |

Each route applies a `StripPrefix=1` filter, so `/api/balance/foo` maps to `/foo` on the target service.

---

### Example API Gateway Requests

#### ✅ Health Checks
```bash
# Gateway itself
curl -s http://localhost:8081/actuator/health

# Via Gateway → Plaid
curl -s http://localhost:8081/plaid-service/actuator/health

# Via Gateway → Ledger
curl -s http://localhost:8081/ledger-service/actuator/health

# Via Gateway → Balance
curl -s http://localhost:8081/balance-service/actuator/health
```

#### ✅ End-to-End Flow
```bash
# Get mock Plaid balance
curl -s "http://localhost:8081/api/plaid/balance?userId=1"

# Add ledger events (POST)
curl -s -H "Content-Type: application/json"   -d '[{"date":"2025-10-04","amount":-50.0,"description":"Test debit"}]'   "http://localhost:8081/api/ledger/events?userId=1"

# Get projected balance
curl -s "http://localhost:8081/api/balance/running?userId=1"
```

---

## 📖 API Usage (Direct Services)

> You can still hit services directly on their own ports if needed.

### ✅ Add Ledger Events
```bash
curl -s -H "Content-Type: application/json"   -d '[{"date":"2025-09-29","amount":-50.00,"description":"Test debit"}]'   "http://localhost:8082/api/ledger/events?userId=1"
```

### ✅ Get Current Balance (Plaid Mock)
```bash
curl -s "http://localhost:8083/api/plaid/balance?userId=1"
```

### ✅ Get Projected Balance (Aggregated)
```bash
curl -s "http://localhost:8080/api/balance/running?userId=1"
```

---

## 📚 Swagger UIs
- Balance → [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)  
- Ledger → [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)  
- Plaid → [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html)  
- Eureka → [http://localhost:8761](http://localhost:8761)  

---

## 🧪 Example Workflow
1. Add some debit/credit events in Ledger Service.  
2. Check current balance from Plaid Service (mock).  
3. Get aggregated projected balance from Balance Service.  
4. Optionally, route all requests through the API Gateway on `:8081`.  
5. View service registration in the **Eureka Dashboard** at `http://localhost:8761`.

---

## 🔗 Networking
All services are attached to the custom Docker network **`bpnet`**.  
Services resolve each other by **logical service ID** via **Eureka** (`ledger-service`, `plaid-service`, `balance-service`, `api-gateway`).

---

## 🧰 Postman Collection (Gateway Edition)
We provide an updated **Postman collection** configured for gateway-based routing.

### Import Instructions:
1. Import `postman/BalancePredictor-Gateway.postman_collection.json` into Postman.  
2. Verify environment variables:
   - `gateway_base = http://localhost:8081`
   - `eureka_base = http://localhost:8761`
   - `userId = 1`
3. Run the **“Health”** and **“Balance Service (via gateway)”** requests to confirm full routing.

---

## 🤖 CI/CD
GitHub Actions workflow:
- Runs `mvn clean verify` for PRs into `dev`, `uat`, `main`, `prod`.  
- Runs `mvn package -DskipTests` on pushes/tags for artifact builds.  
- Uploads `.jar` artifacts for each service for deployment.  

---

## 📂 Project Structure
```
balance-predictor/
├── api-gateway/         # Spring Cloud Gateway service
├── balance-service/     # Aggregator service
├── ledger-service/      # Transaction storage
├── plaid-service/       # Mock bank API
├── eureka-server/       # Service discovery
├── docker-compose.yml   # Multi-service orchestration
├── pom.xml              # Root aggregator POM
└── README.md
```

---

## 📜 License
This project is licensed under the MIT License.
