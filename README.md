# Balance Predictor

[![Build](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Docker](https://img.shields.io/badge/docker-ready-blue)]()
[![License](https://img.shields.io/badge/license-MIT-green)]()
[![Java](https://img.shields.io/badge/java-17-orange)]()
[![Spring Boot](https://img.shields.io/badge/springboot-3.3.3-brightgreen)]()
[![Spring Cloud](https://img.shields.io/badge/springcloud-2023.0.3-blue)]()

A **microservices-based balance forecasting system** built with **Java 17, Spring Boot, Spring Cloud (Eureka), Docker, and Docker Compose**.  
It demonstrates real-world patterns like **service discovery**, **API aggregation**, and **containerized deployment**.

---

## 🚀 Features
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
- **Spring Cloud 2023.0.3 (Eureka Server + Client, LoadBalancer)**
- **Spring WebFlux (WebClient)** for async HTTP calls
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
- Eureka → [http://localhost:8761](http://localhost:8761)  

---

## 🧪 Example Workflow
1. Add some debit/credit events in Ledger Service.  
2. Check current balance from Plaid Service (mock).  
3. Get aggregated projected balance from Balance Service.  
4. View service registration in the Eureka Dashboard at `http://localhost:8761`.  

---

## 🔗 Networking
All services are attached to the custom Docker network **`bpnet`**.  
This allows them to resolve each other by container name (`ledger-service`, `plaid-service`, `eureka-server`) instead of `localhost`.

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
├── balance-service/     # Aggregator service
├── ledger-service/      # Transaction storage
├── plaid-service/       # Mock bank API
├── eureka-server/       # Service discovery
├── docker-compose.yml   # Multi-service orchestration
├── pom.xml              # Root aggregator POM
└── README.md
```

---

## 🧰 Postman Collection
We provide a ready-to-use **Postman Collection** with all endpoints pre-configured.

1. Import `postman/BalancePredictor.postman_collection.json` into Postman.  
2. Use the predefined environment (`localhost`) or update to match your deployed environment.  
3. Run the **Collection Runner** for end-to-end testing.  

---

## 📜 License
This project is licensed under the MIT License.
