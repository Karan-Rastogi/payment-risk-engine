package com.karan.risk.paymentriskengine;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared Testcontainers configuration.
 *
 * Spins up real PostgreSQL, Redis, and Kafka containers once per test suite.
 * Containers are shared via singleton pattern — Spring context cache reuses them.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    @SuppressWarnings("resource")
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"))
            .withDatabaseName("riskengine")
            .withUsername("postgres")
            .withPassword("postgres")
            .withEnv("TZ", "UTC")
            .withEnv("PGTZ", "UTC");
    }

    @Bean
    @ServiceConnection(name = "redis")
    @SuppressWarnings("resource")
    public GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);
    }

    @Bean
    @SuppressWarnings("resource")
    public KafkaContainer kafkaContainer() {
        return new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));
    }
}
