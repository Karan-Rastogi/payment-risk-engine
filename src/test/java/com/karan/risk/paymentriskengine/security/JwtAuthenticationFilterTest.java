package com.karan.risk.paymentriskengine.security;

import com.karan.risk.paymentriskengine.config.JwtProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private static final String SECRET =
        "test-secret-key-for-unit-tests-must-be-at-least-32-chars-long";

    private JwtService jwtService;
    private UserDetailsService userDetailsService;
    private JwtAuthenticationFilter filter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    private UserDetails analyst;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties(SECRET, 3600_000L, "test-issuer");
        jwtService = new JwtService(props);

        analyst = User.withUsername("analyst")
            .password("ignored")
            .authorities(List.of(new SimpleGrantedAuthority("ROLE_ANALYST")))
            .build();

        userDetailsService = mock(UserDetailsService.class);
        when(userDetailsService.loadUserByUsername("analyst")).thenReturn(analyst);

        filter = new JwtAuthenticationFilter(jwtService, userDetailsService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Missing Authorization header -> context stays empty, chain continues")
    void missingHeader_contextEmpty() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    @DisplayName("Non-Bearer header -> context stays empty")
    void nonBearerHeader_contextEmpty() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic abc123");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    @DisplayName("Valid token -> context populated with user")
    void validToken_contextPopulated() throws Exception {
        String token = jwtService.generateToken(analyst);
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("analyst");
        assertThat(auth.getAuthorities())
            .extracting("authority")
            .contains("ROLE_ANALYST");

        verify(filterChain).doFilter(request, response);
        verify(userDetailsService).loadUserByUsername("analyst");
    }

    @Test
    @DisplayName("Tampered token -> context stays empty, chain continues")
    void tamperedToken_contextEmpty() throws Exception {
        String token = jwtService.generateToken(analyst);
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + tampered);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Malformed token -> context stays empty, no exception")
    void malformedToken_noException() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer not-a-real-jwt");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
