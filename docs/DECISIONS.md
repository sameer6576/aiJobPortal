# Technical decisions

## 1. Spring MVC Gateway

**Context:** The services use Spring MVC and blocking JPA/Feign calls.

**Decision:** Use Spring Cloud Gateway Server MVC with functional routes (`RouteConfig`).

**Consequence:** The edge stack matches the blocking application model. This is not a reactive gateway and should not be presented as WebFlux.

## 2. Database per service

**Context:** User, company, job, resume, application, and preference data have separate ownership and lifecycles.

**Decision:** Give each persistent service its own PostgreSQL database.

**Consequence:** Services cannot join across databases. Application responses use Feign calls and can become chatty. A modular monolith would be cheaper to operate at this project scale, but the split makes boundaries and consistency trade-offs explicit.

## 3. Feign for reads, Kafka for status notifications

**Context:** Creating and presenting an application requires current job, resume, company, and candidate details. Email delivery does not need to block the status endpoint.

**Decision:** Use Feign for required domain reads and Kafka for application-status notifications (`application.status.changed`).

**Consequence:** Required services must be available during synchronous operations. Email can be retried independently, although dead-letter handling and an outbox are not implemented. The database update and Kafka publish are not atomic.

## 4. AI as a stateless adapter

**Context:** Gemini supports content generation and structured screening, while authoritative user and job data belong to domain services.

**Decision:** Keep prompts and model parsing in AI service. Do not give AI service a database or ownership of domain records.

**Consequence:** Application and job services own orchestration and call AI over Feign. Shared request/response types live in `common-lib`. How Gemini failures are handled differs by feature (see ADR 10).

## 5. Native Config Server for local evaluation

**Context:** A public Git-backed configuration repository previously contained environment-specific credentials and made a local clone depend on another repository.

**Decision:** Use Spring Cloud Config's native backend and keep sanitized configuration in `job-portal-config/`.

**Consequence:** A clone is self-contained and secrets remain environment variables. A private Git backend can be added as a deployment profile when there is a real deployment environment.

## 6. Keep the current JWT issuer and gateway

**Context:** User-service already authenticates credentials and the gateway validates the resulting JWT.

**Decision:** Harden this model rather than introduce Keycloak, an authorization server, or session storage. User-service issues HMAC JWTs; the gateway is the JWT boundary for public HTTP.

**Consequence:** The system remains stateless and simple to demonstrate. Refresh tokens, revocation, internal service authentication, and identity-provider federation remain explicit future work. An internal mesh would validate JWT or mTLS so Feign cannot be spoofed on an open network.

## 7. Path-based admin at the gateway

**Context:** Admin operations live under `/api/users`, `/api/companies`, and `/api/jobs`, not `/api/admin/**`.

**Decision:** Require `ROLE_ADMIN` on those specific gateway paths after JWT validation. Require `ROLE_ADMIN` or `ROLE_EMPLOYER` on taxonomy write paths. Signup tokens include the user's role.

**Consequence:** A seeker or employer token cannot call those routes through the gateway. Services remain header-trusting if their ports are exposed. Taxonomy GET remains JWT-only at the gateway.

## 8. Jib images for Compose

**Context:** Local full-stack runs use Docker Compose, which pulls named application images rather than building from Dockerfiles in the compose file.

**Decision:** Package each cloud and business module with the Jib Maven plugin (`jib:dockerBuild`) to images such as `sameer9599/job-portal-*-service:latest` (and gateway, config-server, service-registry). Compose references those tags.

**Consequence:** Changing Java code for a Compose run requires a Jib rebuild of the affected modules. Jib here is local packaging convenience, not a published production release process or Kubernetes deploy story.

## 9. Hibernate `ddl-auto: update` locally, no migrations

**Context:** There is no Flyway or Liquibase module. Persistent services need a schema that can appear after a clone without a separate migration runner.

**Decision:** Set `spring.jpa.hibernate.ddl-auto: update` in `job-portal-config/` for user, company, job, resume, application, and preference services. Tests use `create-drop`.

**Consequence:** Local databases evolve with entity changes. Schema history is not versioned. `update` can fail or leave leftover columns on incompatible changes. This is not a production schema strategy; versioned migrations would replace it before any shared environment.

## 10. Differentiated AI fail-open

**Context:** Gemini is optional for a local demo, but not every AI call is equally blocking. Callers already distinguish apply, cover letter, skills-gap, and search.

**Decision:** Catch Gemini/Feign failures only where a domain row or search result can still be useful:

- Apply screening: store the application, set `aiScore` null and `aiShortListStatus` `NOT_SCREENED`.
- Cover letter: return the stored application; do not update the letter.
- Natural-language job search: fall back to `keyword` on the original query.
- Skills-gap: do not catch; propagate the AI error (`AI_UNAVAILABLE` when Gemini is down).

Score bands when screening succeeds: `>=80` `AUTO_SHORTLISTED`, `>=50` `REVIEW_RECOMMENDED`, otherwise `LOW_MATCH`.

**Consequence:** Apply, cover-letter, and NL search remain usable without Gemini. Skills-gap and other uncaught AI endpoints fail when the model or AI service is unavailable. Callers must not assume a single fail-open policy.
