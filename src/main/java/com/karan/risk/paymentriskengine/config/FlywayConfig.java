package com.karan.risk.paymentriskengine.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Explicit Flyway configuration.
 *
 * Spring Boot's Flyway autoconfiguration is not triggering reliably with
 * Spring Boot 4.1.1 + Flyway 12.x. This explicit bean guarantees Flyway runs
 * at startup, before Hibernate validates the schema.
 *
 * The initMethod = "migrate" ensures migrations are applied as soon as the
 * bean is created.
 */
@Configuration
public class FlywayConfig {

    private static final Logger log = LoggerFactory.getLogger(FlywayConfig.class);

    @Bean(initMethod = "migrate")
    public Flyway flyway(DataSource dataSource) {
        log.info("Initializing Flyway manually — migrations from classpath:db/migration");

        Flyway flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .baselineVersion("0")
            .load();

        log.info("Flyway configured. Pending migrations will run on bean init.");
        return flyway;
    }
}
