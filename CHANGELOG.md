# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Initial project scaffolding with Spring Boot 3 and Maven
- Project documentation: README, architecture roadmap, and ADR placeholders
- MIT License
- `.gitignore`, `.editorconfig`, and `.env.example` for consistent dev setup

### Added
- **Module 1: Payment Intake API**
    - `POST /api/v1/payments` endpoint with Bean Validation
    - `Payment` JPA entity with UUID PK and `BigDecimal` amount
    - `PaymentStatus` enum (RECEIVED, APPROVED, REVIEW, DECLINED)
    - `PaymentRepository` extending `JpaRepository`
    - `PaymentService` with `@Transactional` boundary
    - `GlobalExceptionHandler` for structured error responses
    - Flyway migration `V1__create_payments_table.sql`
    - `application.yaml` config with env-var placeholders

### Added
- **Module 2: Rule Engine + Risk Scoring**
    - `Rule` interface (Strategy pattern) for pluggable rules
    - `RuleEngine` orchestrator with fail-safe evaluation
    - `RuleContext` and `RuleResult` records
    - `VelocityRule` — sliding window transaction counter
    - `AmountThresholdRule` — tiered amount scoring
    - `GeoAnomalyRule` — high-risk country + home mismatch detection
    - `BlacklistRule` — sender/receiver blacklist checks
    - `DecisionEngine` — configurable APPROVE/REVIEW/DECLINE thresholds
    - `RiskScore` record for full audit trail
    - Wired into `PaymentServiceImpl` — every payment now scored
    - Flyway migration `V2` adds `ip_country` column

### Added
- **Module 3: Audit Trail + Admin API + Tests**
    - `RuleHit` JPA entity — persisted record of every rule evaluation
    - `RuleHitRepository` + `RuleHitService` for audit persistence
    - `PaymentServiceImpl` now persists rule hits atomically with payment
    - `AdminController` with 3 endpoints:
        - `GET /api/v1/admin/payments/{id}` — payment detail with rule hits
        - `GET /api/v1/admin/payments/flagged` — REVIEW + DECLINED list
        - `POST /api/v1/admin/payments/{id}/override` — manual override
    - `PaymentNotFoundException` + 404 handler
    - `OverrideRequest` DTO with mandatory reason (compliance requirement)
    - Flyway migration `V3__create_rule_hits_table.sql` with FK + indexes
    - **22 unit tests** across `DecisionEngine`, `AmountThresholdRule`, `RuleEngine`, `VelocityRule`, `RuleHitServiceImpl`
    - JDBC timezone enforced to UTC (production-grade consistency)

### Changed
- Nothing yet

### Fixed
- Nothing yet

<!--
## [0.1.0] - 2025-XX-XX
### Added
- Payment intake API (POST /api/v1/payments)
- Request validation and error handling
### Changed
### Fixed
-->
