package com.karan.risk.paymentriskengine.security;

import com.karan.risk.paymentriskengine.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // 32+ character secret for HS256
        JwtProperties props = new JwtProperties(
            "test-secret-key-for-unit-tests-must-be-at-least-32-chars-long",
            3600_000L,   // 1 hour
            "payment-risk-engine-test"
        );
        jwtService = new JwtService(props);

        userDetails = User.withUsername("analyst")
            .password("ignored")
            .authorities(List.of(new SimpleGrantedAuthority("ROLE_ANALYST")))
            .build();
    }

    @Test
    @DisplayName("generateToken produces a non-null token")
    void generateToken_producesToken() {
        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3);  // header.payload.signature
    }

    @Test
    @DisplayName("extractUsername returns the subject")
    void extractUsername_returnsSubject() {
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("analyst");
    }

    @Test
    @DisplayName("isTokenValid returns true for matching user")
    void isTokenValid_matchingUser_returnsTrue() {
        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid returns false for different user")
    void isTokenValid_differentUser_returnsFalse() {
        String token = jwtService.generateToken(userDetails);

        UserDetails other = User.withUsername("admin")
            .password("ignored")
            .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
            .build();

        assertThat(jwtService.isTokenValid(token, other)).isFalse();
    }

    @Test
    @DisplayName("isTokenValid returns false for tampered token")
    void isTokenValid_tamperedToken_returnsFalse() {
        String token = jwtService.generateToken(userDetails);
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThat(jwtService.isTokenValid(tampered, userDetails)).isFalse();
    }

    @Test
    @DisplayName("expired token is rejected")
    void isTokenValid_expiredToken_returnsFalse() throws InterruptedException {
        // 1 ms expiry — token expires almost immediately
        JwtProperties shortLivedProps = new JwtProperties(
            "test-secret-key-for-unit-tests-must-be-at-least-32-chars-long",
            1L,
            "payment-risk-engine-test"
        );
        JwtService shortLivedService = new JwtService(shortLivedProps);
        String token = shortLivedService.generateToken(userDetails);

        Thread.sleep(50);

        assertThat(shortLivedService.isTokenValid(token, userDetails)).isFalse();
    }
}
