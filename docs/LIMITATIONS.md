# Limitations

Recruiter-readable inventory of what JobMate **does not try to be** and what is **unfinished in the current code**. Cross-links: [Architecture](ARCHITECTURE.md), [Decisions](DECISIONS.md), [Security](SECURITY.md), [API notes](API.md), [Portfolio](PORTFOLIO.md).

This system and its companion React client are intended for local portfolio
review. They are not a deployed marketplace and do not claim production
operations, Kubernetes, or scale.

## Intentional non-goals

| Area | What is out of scope |
|---|---|
| Client | A separate React SPA exists, but there is no mobile app, server-side rendering, or public SEO rendering. |
| Identity product | No OAuth2/OIDC, Keycloak, refresh tokens, token revocation, or session store. Access tokens expire (24 hours in API notes); they cannot be revoked early. |
| Documents | Resumes are structured JSON/JPA records. No PDF/DOC upload or parsing. |
| Search | Job list filters and Gemini-interpreted natural language. No Elasticsearch/OpenSearch, ranking, or recruiter resume search. |
| Marketplace extras | No payments, job alerts (`/api/ai/alert-suggestion` was removed), recommendations-as-a-product, or multi-tenant SaaS billing. |
| API catalog | No generated OpenAPI/Swagger. Routes are documented in [API.md](API.md) and generated Postman (`docs/http/gen_postman.mjs`). |
| Platform | No Kubernetes manifests, service mesh, or production deployment runbook. Docker Compose and Jib exist for **local** images and processes. |
| Versioning / lists | No `/v1` prefix. List endpoints are not paginated. |
| Config backend | Native filesystem Config Server for a self-contained clone, not a Git-backed production config service. |

A modular monolith would be cheaper to operate at this project size; the service split is for boundary practice ([Decisions](DECISIONS.md) §2).

## Current engineering gaps

### Authorization is gateway-first

Most business services trust `X-User-Id`, `X-User-Email`, and `X-User-Role`. The gateway is the intended public entry: it verifies JWT and **replaces** those headers from claims. **Direct service ports skip JWT and gateway role checks** (admin user/company/job paths, taxonomy writes). `GET /api/users/{userId}` remains callable by any authenticated gateway user because application-service uses it over Feign.

There is no service-to-service JWT or mTLS. Feign callers can be spoofed if those ports are reachable on an untrusted network.

### AI is fail-open on the hiring write path

Gemini outage or Feign failure: apply still persists with `NOT_SCREENED`; cover-letter endpoint returns the stored application. Status updates persist if Kafka or SMTP fail. Skills-gap (and some direct AI routes) can still fail the HTTP call. The frontend exposes optional user instructions for content-generation endpoints; there is no prompt-injection control, rate limit, or data-retention policy for content sent to Gemini.

### Messaging is not transactional

`ApplicationStatusChangedEvent` is published after the database write. There is **no outbox**, no dead-letter consumer in-repo, and no retry policy beyond what the Kafka/Spring stack does by default. Email is best-effort.

### Schema and tests

Hibernate `ddl-auto: update` on the six domain databases. **No versioned migrations.** Tests: a small set of domain unit tests (signup roles, suspended login, job filters, screening, draft apply rejection) plus Spring context-load smoke tests. Not an integration suite through the gateway.

Duplicate application and saved-job checks are service prechecks without matching database unique constraints, so concurrent requests can race. Application status has no transition matrix; `appliedAt` / `withdrawnAt` are not currently populated, and full application responses expose employer notes to an authorized candidate reader.

### Operational

- No rate limiting on auth or AI.
- No audit log suitable for production (and logs must not include tokens, passwords, or resume dumps).
- Password reset has **no SMTP**; recovery is hash-stored tokens plus an optional local `resetToken` field.
- Compose service ports are for debugging; they are not a hardened network layout.
- Notification and AI are optional; the core apply path does not require them.

## What to say in a review

Honest summary: **edge JWT + header-derived identity, six Postgres databases, Feign for reads, optional Kafka mail, optional Gemini**, all intended to be started from [RUN_MODES.md](RUN_MODES.md) and walked in [DEMO.md](DEMO.md). Gaps above are known, not accidental marketing omissions.
