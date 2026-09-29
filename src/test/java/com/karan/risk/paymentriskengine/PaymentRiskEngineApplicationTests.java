package com.karan.risk.paymentriskengine;

import org.junit.jupiter.api.Test;

/**
 * Sanity test for the main application class.
 *
 * NOTE: The default @SpringBootTest contextLoads() test has been intentionally
 * removed because it requires a live PostgreSQL instance and network access.
 * A proper integration test using Testcontainers will be added in Module 6,
 * which spins up an ephemeral PostgreSQL container for each test run.
 */
class PaymentRiskEngineApplicationTests {

    @Test
    void applicationClassLoads() {
        // Verify the main class exists and is loadable
        RiskEngineApplication.class.getName();
    }
}
