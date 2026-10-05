# Payment Risk Engine

Real-time fraud detection & risk scoring system for payment transactions.
Built with Spring Boot 3, PostgreSQL, Redis, Kafka.

## 🎯 Problem

Banks and payment processors handle millions of transactions per day, and even a
0.1% fraud rate translates to massive financial losses and regulatory penalties.
Traditional rule-based systems are too slow and too rigid to catch modern fraud
patterns. This project simulates a **real-time payment risk engine** that evaluates
every transaction in milliseconds and decides whether to approve, flag for review,
or decline it — the same core problem JP Morgan, Visa, and Stripe solve at scale.

## 🏗️ Architecture

![Payment Risk Engine Architecture](docs/image.png)

> **Note:** Architecture diagram coming soon. Placeholder image pending.

## ⚙️ Tech Stack

- **Language:** Java 17
- **Framework:** Spring Boot 3.x
- **Database:** PostgreSQL (transaction & audit persistence)
- **Cache:** Redis (real-time velocity counters)
- **Messaging:** Apache Kafka (async event publishing)
- **Build Tool:** Maven
- **Containerization:** Docker + docker-compose
- **CI/CD:** GitHub Actions
- **Testing:** JUnit 5, Mockito, Testcontainers
- **Monitoring:** Spring Actuator, Micrometer, Prometheus

## 🚀 Features

- [x] Payment intake API (REST endpoint with validation)
- [x] Project scaffolding & docs
- [x] Rule engine (velocity, geo, amount, blacklist)
- [x] Risk scoring + decision engine
- [x] Audit log & persistence layer
- [x] Admin API (flagged transactions, manual overrides)
- [x] Unit tests (JUnit 5 + Mockito + AssertJ)
- [x] Redis-based velocity counters
- [x] Kafka event publishing
- [x] Admin dashboard (flagged transactions, overrides)
- [x] Docker & docker-compose setup
- [ ] CI pipeline with GitHub Actions
- [ ] Swagger / OpenAPI docs
- [ ] Load testing & benchmarks

## 📚 API Docs

Swagger UI will be available at:
`http://localhost:8080/swagger-ui.html`

*(Link will be live once the API module is complete.)*

## 🧪 How to Run

## 🐳 Local Infrastructure

The entire local stack runs via Docker Compose:

| Service | Port | Purpose |
| :--- | :--- | :--- |
| PostgreSQL 16 | 5432 | Transaction & audit persistence |
| Redis 7 | 6379 | Velocity counters (sorted sets) |
| Kafka 7.6 | 9092 | Event publishing (`payment.events` topic) |
| Zookeeper | 2181 | Kafka coordination |

Start all services:
```bash
docker-compose up -d

## 🧪 Testing Strategy

### Unit Tests (Current — 22 tests)

Located in `src/test/java/`:

| Test Class | Coverage |
| :--- | :--- |
| `DecisionEngineTest` | Threshold boundaries (0/29/30/69/70/100) |
| `AmountThresholdRuleTest` | Tier boundaries with `BigDecimal` |
| `VelocityRuleTest` | Sliding window, sender isolation |
| `RuleEngineTest` | Aggregation, cap at 100, fail-safe behaviour |
| `RuleHitServiceImplTest` | Audit persistence (Mockito) |

**Characteristics:**
- No infrastructure required — run in milliseconds
- Cover business logic, not framework wiring
- Run on every commit
- Command: `./mvnw test`

### Integration Tests (Module 6 — Planned)

Will use **Testcontainers** to spin up real PostgreSQL per test run:
- Full Spring context load
- JPA repository queries against real DB
- Flyway migrations
- End-to-end API tests via `MockMvc`

**Why not H2?** H2 and PostgreSQL differ in edge cases (JSON, indexes, constraints). Testcontainers gives production-equivalent behaviour.

**Why was the default `contextLoads` removed?** It required a manually-managed local PostgreSQL. That's not reproducible in CI. Testcontainers solves this properly.

### Prerequisites
- Java 17+
- Docker & Docker Compose
- Maven (or use the included `./mvnw` wrapper)

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/Karan-Rastogi/payment-risk-engine.git
cd payment-risk-engine

# 2. Copy environment file
cp .env.example .env

# 3. Start dependencies (PostgreSQL, Redis, Kafka)
docker-compose up -d

# 4. Run the Spring Boot application
./mvnw spring-boot:run

```
## 🤝 Contributing

This is a personal learning project, but suggestions and feedback are welcome.
Please open an issue first to discuss any major changes.

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](./LICENSE) file.

## 👤 Author

**KARAN RASTOGI**
- LinkedIn:
- Email: krnrastogi18@gmail.com
- GitHub: [@Karan-Rastogi](https://github.com/Karan-Rastogi)

---

⭐ If you find this project useful or interesting, feel free to star the repo!
