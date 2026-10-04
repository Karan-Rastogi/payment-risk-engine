package com.karan.risk.paymentriskengine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Development-only security config.
 *
 * Active only when the "dev" Spring profile is set.
 * All endpoints are public — this lets local development and Postman
 * testing work without authentication overhead.
 *
 * DO NOT use this in production. Production JWT auth comes in Module 5.
 */
@Configuration
@Profile("dev")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .httpBasic(basic -> basic.disable());
        return http.build();
    }
}
