# Security

## Supported model

User-service verifies credentials with BCrypt and issues a signed JWT. The API Gateway verifies the signature and expiration before forwarding protected routes. User ID, email, and authorities are JWT claims used to build downstream identity headers.

The system is stateless. It does not implement server sessions, refresh-token rotation, token revocation, OAuth2, or OIDC.

## Secret handling

Required secrets are environment variables:

- `JWT_SECRET`
- `DB_PASSWORD`
- `GEMINI_API_KEY`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`

`JWT_SECRET` must be identical in user-service and gateway and contain at least 32 bytes. User-service and gateway fail at startup if it is absent or too short.

Do not commit `.env` files. If this project has previously been pushed or shared, rotate all former JWT, database, mail, and Gemini values. Removing a value from the latest tree does not remove it from Git history.

## Current risks

### Gateway-only identity

Most business services do not independently validate JWTs. Their controllers trust `X-User-Id`, `X-User-Email`, and `X-User-Role`. Exposing a service port allows a caller to forge those headers.

Only the gateway should be reachable in a deployed environment. Local Compose service ports are for debugging.

### Identity header replacement

The gateway removes caller-supplied `X-User-Id`, `X-User-Email`, and `X-User-Role` and then sets them from the validated JWT. Forged identity headers on a gateway request are ignored. Direct service ports still accept those headers.

### Authorization coverage

The gateway requires `ROLE_ADMIN` for user administration, company verify/deactivate, and `GET /api/jobs/admin`. Job category, skill, and tag writes require `ROLE_ADMIN` or `ROLE_EMPLOYER`. Company updates, resume nested GETs, and application notes require the owner or employer. Application GET by id is limited to the candidate or employer on that application. `GET /api/users/{userId}` remains readable to any authenticated caller because application-service uses it over Feign.

Direct service ports do not apply the gateway role checks.

### Token lifecycle

Access tokens expire but cannot be revoked before expiry. There is no refresh token or session inventory. Keep access-token lifetime short and rotate the HMAC key after a suspected compromise.

### External services

Resume and candidate content may be sent to Gemini. Production use would require consent, data minimization, retention review, rate limits, and prompt-injection controls.

Email credentials must be application-specific credentials rather than an account password.

## Deployment expectations

Before exposing this system outside a local environment:

1. Restrict network access so only the gateway can reach business APIs.
2. The gateway already replaces inbound identity headers from the JWT.
3. The gateway already requires `ROLE_ADMIN` on the listed administrative paths.
4. Add service-level JWT or mTLS authentication if services run on an untrusted network.
5. Add rate limiting to authentication and AI routes.
6. Add audit logs without tokens, passwords, or resume content.
7. Replace `ddl-auto: update` with versioned migrations.

## Reporting

Do not open a public issue containing a credential, token, personal record, or exploit payload. Revoke exposed credentials before sharing diagnostic details.
