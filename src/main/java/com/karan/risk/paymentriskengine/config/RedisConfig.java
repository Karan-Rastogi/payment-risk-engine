package com.karan.risk.paymentriskengine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis configuration.
 *
 * Spring Boot auto-configures the RedisConnectionFactory based on
 * spring.data.redis.* properties in application.yaml. We only define
 * the StringRedisTemplate bean — used for sorted set operations
 * in velocity counters.
 */
@Configuration
public class RedisConfig {

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
