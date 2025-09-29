
# Balance Predictor — Microservices Starter

Services:
- **ledger-service** (8082): Pending events (H2)
- **plaid-service** (8083): Mock Plaid balance
- **balance-service** (8080): Computes projection by calling the other two

## Run locally
In three terminals:
```
cd ledger-service && mvn spring-boot:run
cd plaid-service && mvn spring-boot:run
cd balance-service && mvn spring-boot:run
```

Swagger:
- http://localhost:8082/swagger-ui.html
- http://localhost:8083/swagger-ui.html
- http://localhost:8080/swagger-ui.html

## Docker Compose
```
docker compose up --build
```

## Try it
```
curl -X POST "http://localhost:8083/api/plaid/balance?amount=3000"
curl -H "Content-Type: application/json"   -d '[{"date":"2025-09-28","amount":-120.50,"description":"Electric bill"},
       {"date":"2025-09-29","amount":-150.00,"description":"Citi"},
       {"date":"2025-09-29","amount":-100.00,"description":"Water"}]'   "http://localhost:8082/api/ledger/events?userId=1"
curl "http://localhost:8080/api/balance/running?userId=1"
```
