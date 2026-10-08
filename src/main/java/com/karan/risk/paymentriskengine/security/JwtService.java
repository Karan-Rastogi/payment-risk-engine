package com.karan.risk.paymentriskengine.security;

import com.karan.risk.paymentriskengine.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT token generation and validation.
 *
 * Uses HMAC-SHA256 (HS256) with a secret key from JwtProperties.
 * Tokens contain:
 *   - sub: username
 *   - roles: list of granted authorities
 *   - iss: issuer
 *   - iat / exp: issued-at and expiration times
 *
 * Stateless — no server-side session storage.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = buildSigningKey(properties.secret());
        log.info("JwtService initialized. Issuer={}, ExpirationMs={}",
            properties.issuer(), properties.expirationMs());
    }

    /**
     * Generate a JWT for the given user.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        List<String> roles = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();
        claims.put("roles", roles);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + properties.expirationMs());

        return Jwts.builder()
            .claims(claims)
            .subject(userDetails.getUsername())
            .issuer(properties.issuer())
            .issuedAt(now)
            .expiration(expiry)
            .signWith(signingKey)
            .compact();
    }

    /**
     * Extract username (subject) from token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Check if token is valid for the given user.
     * Verifies signature, expiry, and username match.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception ex) {
            log.warn("JWT validation failed: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Extract a specific claim from the token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .requireIssuer(properties.issuer())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private static SecretKey buildSigningKey(String secret) {
        // If secret is Base64-encoded, decode; else use raw bytes.
        // For simplicity, we use raw bytes of the configured string.
        byte[] keyBytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                "JWT secret must be at least 32 bytes for HS256. Current: " + keyBytes.length);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
