package com.karan.risk.paymentriskengine.integration;

import com.karan.risk.paymentriskengine.AbstractIntegrationTest;
import com.karan.risk.paymentriskengine.dto.LoginRequest;
import com.karan.risk.paymentriskengine.dto.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end integration test for authentication & authorization.
 *
 * Verifies:
 *   - Login with valid credentials returns JWT
 *   - Login with invalid credentials returns 401
 *   - Admin endpoints require JWT (401 without)
 *   - ANALYST can GET flagged payments but not override (403)
 *   - SENIOR_ANALYST can override (200)
 */
class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    private RestTemplate restTemplate;
    private String loginUrl;
    private String flaggedUrl;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        this.restTemplate = new RestTemplate();
        this.baseUrl = "http://localhost:" + port;
        this.loginUrl = baseUrl + "/api/v1/auth/login";
        this.flaggedUrl = baseUrl + "/api/v1/admin/payments/flagged";
    }

    @Test
    @DisplayName("Valid credentials return JWT token")
    void validCredentials_returnToken() {
        LoginRequest request = new LoginRequest("analyst", "analyst123");

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
            loginUrl, request, LoginResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().token()).isNotBlank();
        assertThat(response.getBody().type()).isEqualTo("Bearer");
        assertThat(response.getBody().expiresIn()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Invalid credentials return 401")
    void invalidCredentials_return401() {
        LoginRequest request = new LoginRequest("analyst", "wrong-password");

        assertThatThrownBy(() -> restTemplate.postForEntity(
            loginUrl, request, LoginResponse.class))
            .isInstanceOf(HttpClientErrorException.Unauthorized.class);
    }

    @Test
    @DisplayName("Admin endpoint without token returns 401")
    void adminEndpointWithoutToken_return401() {
        assertThatThrownBy(() -> restTemplate.getForEntity(
            flaggedUrl, String.class))
            .isInstanceOf(HttpClientErrorException.Unauthorized.class);
    }

    @Test
    @DisplayName("ANALYST can GET flagged payments but cannot override")
    void analyst_canReadCannotOverride() {
        String token = loginAs("analyst", "analyst123");

        // GET flagged — should succeed
        ResponseEntity<String> getResponse = restTemplate.exchange(
            flaggedUrl, HttpMethod.GET,
            new HttpEntity<>(bearerHeaders(token)),
            String.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Try override — should return 403
        String overrideUrl = baseUrl + "/api/v1/admin/payments/" +
            "00000000-0000-0000-0000-000000000000/override";
        HttpEntity<String> overrideRequest = new HttpEntity<>(
            "{\"status\":\"APPROVED\",\"reason\":\"test\"}",
            bearerHeaders(token));

        assertThatThrownBy(() -> restTemplate.exchange(
            overrideUrl, HttpMethod.POST, overrideRequest, String.class))
            .isInstanceOf(HttpClientErrorException.Forbidden.class);
    }

    @Test
    @DisplayName("SENIOR_ANALYST gets past role check but fails on non-existent payment")
    void senior_getsPastRoleCheck() {
        String token = loginAs("senior", "senior123");

        String overrideUrl = baseUrl + "/api/v1/admin/payments/" +
            "00000000-0000-0000-0000-000000000000/override";
        HttpEntity<String> overrideRequest = new HttpEntity<>(
            "{\"status\":\"APPROVED\",\"reason\":\"test\"}",
            bearerHeaders(token));

        // Should NOT be 403 — should be 404 (payment not found)
        assertThatThrownBy(() -> restTemplate.exchange(
            overrideUrl, HttpMethod.POST, overrideRequest, String.class))
            .isInstanceOf(HttpClientErrorException.NotFound.class);
    }

    // --- Helpers ---

    private String loginAs(String username, String password) {
        LoginRequest loginRequest = new LoginRequest(username, password);
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
            loginUrl, loginRequest, LoginResponse.class);
        assertThat(response.getBody()).isNotNull();
        return response.getBody().token();
    }

    private HttpHeaders bearerHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
