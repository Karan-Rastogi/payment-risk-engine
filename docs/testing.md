# Testing Strategy

## Two-Layer Test Approach

### Unit Tests (39 tests)

**Tools:** JUnit 5, Mockito, AssertJ
**Speed:** Milliseconds per test
**Infrastructure:** None required

Cover business logic in isolation:
- Rule evaluation (thresholds, boundaries)
- Decision engine (score → status mapping)
- JWT token generation and validation
- Event publishing (with mocked Kafka)
- Audit persistence (with mocked repositories)

### Integration Tests (9 tests)

**Tools:** Testcontainers, Spring Boot Test, RestTemplate
**Speed:** 60-90 seconds (Docker container startup)
**Infrastructure:** Real PostgreSQL, Redis, Kafka containers

Cover end-to-end flows:
- HTTP request → controller → service → rules → DB → Kafka
- JWT login → token validation → role-based access
- Velocity rule with real Redis sorted sets
- Event publishing with real Kafka topics

## Why Testcontainers

Real infrastructure catches bugs mocks miss:
- Flyway migration errors
- JPA query problems
- Redis sorted set behavior
- Kafka serialization issues
- HTTP status codes and headers

**Real examples from this project:**
1. Empty blacklist config in test profile — BlacklistRule silently not firing
2. Velocity score (20) below review threshold (30) — required two payments to trigger REVIEW

## Running Locally

Requires Docker Desktop running.

```bash
./mvnw clean verify                              # all tests
./mvnw test                                      # unit only
./mvnw test -Dtest=PaymentIntakeIntegrationTest  # one integration class
