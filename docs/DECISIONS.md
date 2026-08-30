# Technical decisions

## 1. Spring MVC Gateway

**Context:** The services use Spring MVC and blocking JPA/Feign calls.

**Decision:** Use Spring Cloud Gateway Server MVC with functional routes.

**Consequence:** The edge stack matches the blocking application model. This is not a reactive gateway and should not be presented as WebFlux.

## 2. Database per service

**Context:** User, company, job, resume, application, and preference data have separate ownership and lifecycles.

**Decision:** Give each persistent service its own PostgreSQL database.

**Consequence:** Services cannot join across databases. Application responses use Feign calls and can become chatty. A modular monolith would be cheaper to operate at this project scale, but the split makes boundaries and consistency trade-offs explicit.

## 3. Feign for reads, Kafka for status notifications

**Context:** Creating and presenting an application requires current job, resume, company, and candidate details. Email delivery does not need to block the status endpoint.

**Decision:** Use Feign for required domain reads and Kafka for application-status notifications.

**Consequence:** Required services must be available during synchronous operations. Email can be retried independently, although dead-letter handling and an outbox are not implemented yet.

## 4. AI as a stateless adapter

**Context:** Gemini supports content generation and structured screening, while authoritative user and job data belong to domain services.

**Decision:** Keep prompts and model parsing in AI service. Do not give AI service a database or ownership of domain records.

**Consequence:** Application and job services own orchestration. Gemini outages do not roll back an application row; screening fields stay `NOT_SCREENED`.

## 5. Native Config Server for local evaluation

**Context:** A public Git-backed configuration repository previously contained environment-specific credentials and made a local clone depend on another repository.

**Decision:** Use Spring Cloud Config's native backend and keep sanitized configuration in `job-portal-config/`.

**Consequence:** A clone is self-contained and secrets remain environment variables. A private Git backend can be added as a deployment profile when there is a real deployment environment.

## 6. Keep the current JWT issuer and gateway

**Context:** User-service already authenticates credentials and the gateway validates the resulting JWT.

**Decision:** Harden this model rather than introduce Keycloak, an authorization server, or session storage.

**Consequence:** The system remains stateless and simple to demonstrate. Refresh tokens, revocation, internal service authentication, and identity-provider federation remain explicit future work. An internal mesh would validate JWT or mTLS so Feign cannot be spoofed on an open network.

## 7. Path-based admin at the gateway

**Context:** Admin operations live under `/api/users`, `/api/companies`, and `/api/jobs`, not `/api/admin/**`.

**Decision:** Require `ROLE_ADMIN` on those specific gateway paths after JWT validation. Signup tokens include the user's role.

**Consequence:** A seeker or employer token cannot call those routes through the gateway. Services remain header-trusting if their ports are exposed.
