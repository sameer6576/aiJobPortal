# Security

## Supported model

User-service verifies credentials with BCrypt and issues a signed HMAC JWT (24-hour expiry). The API Gateway verifies signature and expiration on protected routes, then derives `X-User-Id`, `X-User-Email`, and `X-User-Role` from JWT claims.

The system is stateless. It does not implement server sessions, refresh-token rotation, token revocation, OAuth2, or OIDC.

Password reset does not send mail. User-service issues an opaque random token, stores only its SHA-256 hash (`passwordResetTokenHash`) with a one-hour expiry, and consumes it on successful reset. Passwords are BCrypt-encoded and must not be logged. For local/demo, `app.password-reset.expose-token` (default `true`, `PASSWORD_RESET_EXPOSE_TOKEN`) may return the raw token in the forgot-password JSON; set it false when that leak is not wanted.

## Secret and configuration names

Values are environment variables. Do not commit filled `.env` files. Names used by Config Server, Compose, and services include:

Secrets: `JWT_SECRET`, `DB_PASSWORD`, `GEMINI_API_KEY`, `MAIL_USERNAME`, `MAIL_PASSWORD`.

Config (not secrets, but environment-specific): `MAIL_HOST`, `MAIL_PORT`, `DB_HOST`, `DB_USERNAME`, `USER_DB_PORT`, `COMPANY_DB_PORT`, `JOB_DB_PORT`, `APPLICATION_DB_PORT`, `PREFERENCE_DB_PORT`, `RESUME_DB_PORT`, `CONFIG_REPOSITORY_PATH`, `KAFKA_BOOTSTRAP_SERVERS`, `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.

`JWT_SECRET` must be identical in user-service and gateway and contain at least 32 bytes. Those two processes fail at startup if it is absent or too short.

If this project has previously been pushed or shared, rotate former JWT, database, mail, and Gemini values. Removing a value from the latest tree does not remove it from Git history.

## Current risks

### Gateway-only identity

Most business services do not independently validate JWTs. Controllers trust `X-User-Id`, `X-User-Email`, and `X-User-Role`. Exposing a service port allows a caller to forge those headers.

Only the gateway should be reachable in a deployed environment. Local Compose service ports are for debugging.

### Identity header replacement

On protected gateway routes, inbound `X-User-Id`, `X-User-Email`, and `X-User-Role` are removed and then set from the validated JWT. Forged identity headers on a gateway request are ignored. `/auth/**` is not JWT-filtered. Optional-auth public job/taxonomy GETs also strip those headers when no token is present. Direct service ports still accept those headers.

### Authorization coverage

At the gateway, `ROLE_ADMIN` is required for `GET /api/users`, user suspend/activate/delete, company verify/deactivate, and `GET /api/jobs/admin`. `GET /api/jobs/my` requires `ROLE_EMPLOYER`. Job category, skill, and tag writes require `ROLE_ADMIN` or `ROLE_EMPLOYER`. `GET /api/jobs`, `GET /api/jobs/{numeric id}`, and taxonomy GET list/detail are optional-JWT: missing token is anonymous (identity headers stripped); a valid Bearer token injects identity so an owner or admin can read a draft by id; a malformed or invalid supplied token is 401. Other `/api/**` routes need a valid JWT.

At the service (when called with trusted headers):

- Company `PUT` and owner delete (`PATCH /api/companies/{id}`) require the owner. Verify and deactivate have no service-level role check.
- Job update, publish, close, and delete require the posting employer. Draft `GET /api/jobs/{id}` is hidden (`NOT_FOUND`) unless that employer or a role string containing `ROLE_ADMIN`.
- Resume and nested education/experience/project/skill/language operations require the resume owner.
- Application GET by id and skills-gap are limited to the candidate or employer on that row. Company list and notes require the employer. Job-scoped application list requires that job's employer. Status and star require the employer; withdraw and delete require the candidate.
- `GET /api/users/{userId}` is readable to any authenticated caller because application-service uses it over Feign.

Direct service ports do not apply the gateway role checks. Taxonomy writes and admin user/company/job paths are therefore reachable without `ROLE_ADMIN` if the port is exposed.

### Token lifecycle

Access tokens expire after 24 hours and cannot be revoked earlier. There is no refresh token or session inventory. A stolen token works until expiry or `JWT_SECRET` rotation (which invalidates all tokens).

### Gemini

Resume text, job text, and related fields may be sent to Gemini when AI features run. There is no separate consent flow, retention policy, or prompt-injection control in this codebase. Treat Gemini as an external processor; do not send data you are not willing to leave the host.

### SMTP

Notification-service sends application-status mail with `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, and `MAIL_PASSWORD`. Use application-specific credentials, not a personal mailbox password. Status is saved even if Kafka or SMTP fails. Mail content is not encrypted beyond TLS if the SMTP session uses it (port 587 / STARTTLS in the local examples).

### Schema

Persistent services use Hibernate `ddl-auto: update` in `job-portal-config/`. That is local convenience, not a locked-down production schema. There are no Flyway or Liquibase migrations. Tests use `create-drop`.

## Hardening gaps (not implemented)

Before exposing this system outside a local environment, these remain open:

1. Restrict network access so only the gateway can reach business APIs.
2. Add service-level JWT or mTLS if services run on an untrusted network (Feign still sends identity headers).
3. Rate limiting on authentication and AI routes.
4. Audit logs that omit tokens, passwords, and resume content.
5. Replace `ddl-auto: update` with versioned migrations.
6. Token revocation or shorter-lived access tokens if the 24-hour window is too long.
7. Outbox or equivalent if notification delivery must be reliable.

The gateway already replaces inbound identity headers and already requires `ROLE_ADMIN` (and employer/admin for taxonomy writes) on the listed paths. That is not a complete authorization story for exposed service ports.

## Reporting

Do not open a public issue containing a credential, token, personal record, or exploit payload. Revoke exposed credentials before sharing diagnostic details.
