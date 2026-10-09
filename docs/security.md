# Security Architecture

## Overview

The Payment Risk Engine uses JWT-based authentication and role-based authorization. Two mechanisms protect different endpoint types:

1. **API key authentication (planned):** Payment intake from partner banks.
2. **JWT authentication:** Admin endpoints accessed by analysts.

## Authentication Flow

The login endpoint accepts a username and password, authenticates the user against the database, and returns a signed JWT.

```http
POST /api/v1/auth/login
Content-Type: application/json
```

Request body:

```json
{
  "username": "analyst",
  "password": "analyst123"
}
```

Authentication sequence:

1. `AuthController.login()`
2. `AuthenticationManager.authenticate()`
3. `DaoAuthenticationProvider`
4. `AppUserDetailsService.loadUserByUsername()`
5. Database lookup:

   ```sql
   SELECT * FROM users WHERE username = ?
   ```

6. `BCryptPasswordEncoder.matches(rawPassword, storedHash)` verifies the password.
7. If the credentials match, Spring Security creates an `Authentication` object with the user's authorities.
8. `JwtService.generateToken(authentication)` creates the JWT.
9. The API returns the token.

Example response:

```json
{
  "token": "eyJ...",
  "type": "Bearer",
  "expiresIn": 86400
}
```

> **Security note:** The username and password above are illustrative development credentials only. Do not use hard-coded or default credentials in production.

## Authorization Flow

For a protected endpoint, the client sends the token in the `Authorization` header:

```http
Authorization: Bearer <token>
```

The request is processed as follows:

1. `JwtAuthenticationFilter.doFilterInternal()` extracts the bearer token.
2. `JwtService.extractUsername()` reads the token subject.
3. The application loads the corresponding `UserDetails` and validates the token signature, expiration, and username.
4. The filter sets the `SecurityContext` with the authenticated principal and authorities.
5. Authorization rules declared in `SecurityConfig` evaluate the request.
6. If the user's role is permitted, the request proceeds. Otherwise, the API returns `403 Forbidden`.

Requests with missing or invalid authentication return `401 Unauthorized`.

## Role Matrix

| Endpoint | `ANALYST` | `SENIOR_ANALYST` | `ADMIN` |
| --- | :---: | :---: | :---: |
| `GET /api/v1/admin/payments/{id}` | Yes | Yes | Yes |
| `GET /api/v1/admin/payments/flagged` | Yes | Yes | Yes |
| `POST /api/v1/admin/payments/{id}/override` | No | Yes | Yes |

## Token Structure

Example JWT payload, shown decoded for documentation purposes:

```json
{
  "roles": ["ROLE_ANALYST"],
  "sub": "analyst",
  "iss": "payment-risk-engine",
  "iat": 1791488857,
  "exp": 1791575257
}
```

Claims:

- `sub`: Username (subject).
- `roles`: Role authorities. Spring Security commonly uses the `ROLE_` prefix for role-based authorities; ensure the token-generation and authorization code agree on the representation.
- `iss`: Token issuer.
- `iat`: Issued-at time, as Unix seconds.
- `exp`: Expiration time, as Unix seconds.

## Configuration

Example Spring configuration:

```yaml
spring:
  security:
    jwt:
      secret: ${JWT_SECRET}
      expiration-ms: 86400000
      issuer: payment-risk-engine
```

Keep the actual signing secret outside source control and supply it through a secret manager or environment-specific configuration. The property name `expiration-ms` indicates milliseconds; `86400000` is 24 hours.

## Production Requirements

- **Secret management:** Store `JWT_SECRET` in AWS Secrets Manager, HashiCorp Vault, or an equivalent secret manager.
- **Short-lived access tokens:** Prefer access-token lifetimes of approximately 15 minutes to 1 hour, based on risk and usability requirements.
- **Refresh tokens:** Implement a secure refresh flow, including rotation and reuse detection where appropriate.
- **Token revocation:** Use a Redis-backed denylist or another revocation mechanism if immediate invalidation is required. Ensure entries expire when the corresponding token expires.
- **HTTPS only:** Require TLS for all authentication and API traffic.
- **Login rate limiting:** Rate-limit `/api/v1/auth/login` and consider progressive delays or temporary lockouts.
- **Credential handling:** Never log passwords, access tokens, refresh tokens, or signing secrets.
- **Authorization checks:** Enforce permissions server-side for every protected endpoint; do not rely on client-side role checks.

## Error Responses

### 401 Unauthorized

Returned when authentication is missing or invalid.

```json
{
  "timestamp": "2026-10-08T20:06:56Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required"
}
```

### 403 Forbidden

Returned when the user is authenticated but lacks the required permissions.

```json
{
  "timestamp": "2026-10-08T20:07:39Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Insufficient permissions"
}
```

## Future Enhancements

- **API key authentication:** Add API key authentication for partner-bank payment intake at `/api/v1/payments`. Store only securely hashed keys where practical, support rotation, and scope keys to the minimum required permissions.
- **Refresh tokens:** Use short-lived access tokens with a secure refresh-token flow.
- **Token revocation:** Add a Redis-backed denylist for logout and other invalidation events when immediate revocation is needed.
- **Multi-factor authentication (MFA):** Require TOTP or another suitable second factor for privileged admin roles.
- **Audit logging:** Record authentication events, including login successes and failures, token revocation, and security-sensitive administrative actions. Avoid recording credentials or raw tokens.
- **Rate limiting:** Protect login and other sensitive endpoints against brute-force and abuse.

## Implementation Checklist

- [x] Load the JWT signing secret from a secret manager.
- [x] Validate the signature, issuer, expiration, and expected claims.
- [x] Confirm role names and `ROLE_` prefix handling are consistent.
- [x] Apply authorization rules to every protected endpoint.
- [ ] Use HTTPS and configure secure token handling.
- [ ] Add rate limiting and security-event auditing.
- [x] Add tests for successful login, invalid credentials, expired tokens, and each role/endpoint combination.
