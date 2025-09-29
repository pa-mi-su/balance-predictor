# Balance Predictor

[![Build](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Docker](https://img.shields.io/badge/docker-ready-blue)]()
[![License](https://img.shields.io/badge/license-MIT-green)]()
[![Java](https://img.shields.io/badge/java-17-orange)]()
[![Spring Boot](https://img.shields.io/badge/springboot-3.3.3-brightgreen)]()

A **microservices-based balance forecasting system** built with **Java 17, Spring Boot, Docker, and Docker Compose**.
This project demonstrates real-world patterns like service-to-service communication, API aggregation, and containerized deployment.

---

## 🚀 Features
- **Balance Service** → Aggregates data from Ledger & Plaid services and calculates projected balances.
- **Ledger Service** → Stores and returns user debit/credit events.
- **Plaid Service (Mock)** → Simulates a bank API returning current balance information.
- **Swagger UI** → Interactive API documentation for all services.
- **Docker Compose Setup** → Run the entire system with a single command.

---

## 🛠️ Tech Stack
- **Java 17**
- **Spring Boot 3.3.3**
- **Spring WebFlux (WebClient)** for async HTTP calls
- **Docker & Docker Compose** for containerization
- **Springdoc OpenAPI** for Swagger UI

---

## 📦 Getting Started

### 1. Clone the repo
```bash
git clone git@github.com:pa-mi-su/balance-predictor.git
cd balance-predictor
```

### 2. Build & Run with Docker Compose
```bash
docker compose up -d --build
```

### 3. Verify services are healthy
```bash
docker compose ps
curl -s http://localhost:8080/actuator/health   # Balance Service
curl -s http://localhost:8082/actuator/health   # Ledger Service
curl -s http://localhost:8083/actuator/health   # Plaid Service
```

---

## 📖 API Usage

### ✅ Add Ledger Events (Debits / Credits)
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

---

## 🧪 Example Workflow
1. Add some debit/credit events in Ledger Service.
2. Check current balance from Plaid Service (mock).
3. Get aggregated projected balance from Balance Service.

---

## 📂 Project Structure
```
balance-predictor/
├── balance-service/   # Aggregator service
├── ledger-service/    # Transaction storage
├── plaid-service/     # Mock bank API
├── docker-compose.yml # Multi-service orchestration
└── README.md
```

---

## 📜 License
This project is licensed under the MIT License.
