package com.karan.risk.paymentriskengine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class SmokeIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ApplicationContext context;

    @Test
    @DisplayName("Spring context loads with real PostgreSQL, Redis, Kafka")
    void contextLoads() {
        assertThat(context).isNotNull();
        assertThat(context.getBeanDefinitionCount()).isGreaterThan(50);
    }
}
