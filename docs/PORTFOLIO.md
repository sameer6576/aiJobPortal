# JobMate — portfolio notes

Backend-only job-marketplace API for local evaluation. Not a product launch, not Kubernetes, not production ops. Companion: [README](../README.md), [Architecture](ARCHITECTURE.md), [Decisions](DECISIONS.md), [Limitations](LIMITATIONS.md).

## Product

JobMate models the server path from account creation to an employer’s application decision. A job seeker signs up, maintains a **structured** resume (sections in the database, not a parsed PDF), saves jobs, and applies to **OPEN** postings. An employer owns a company, publishes jobs, lists applicants (including optional Gemini scores), writes notes, and changes status. Optional Kafka + SMTP notify the candidate when status changes. Gemini can draft job copy, interpret a natural-language search into filters, score an apply, generate a cover letter, and report a skills gap. The public edge is a Spring MVC API gateway that verifies JWTs issued by user-service.

Scope stops there: no web UI, no recruiter resume search product, no payments, no SEO site, no file ingest, no search index, no job-alert product.

## Why this shape (and the cheaper alternative)

A **modular monolith** with one Postgres and in-process modules would be cheaper to run and easier to operate at this size. This repo splits six databases and eight application processes anyway so that **ownership, Feign chatter, header trust, and dual-write to Kafka are visible and discussable**, not hidden behind a single schema. That is practice for service boundaries, not a claim that microservices were required.

## Stack vs what was implemented

**Platform (Spring / Docker):** Java 21, Spring Boot 4.0.5, Spring Cloud 2025.1.1 — MVC Gateway, Eureka, native Config Server, OpenFeign, Security, Data JPA. Compose runs six Postgres 16 instances plus optional Kafka. Jib builds images. Hibernate `ddl-auto: update` creates/alters tables locally. There are no Flyway migrations.

**Author-owned in this repo:** domain models and APIs; user-service credential check (BCrypt) and JWT issuance; gateway filters (JWT, strip/set `X-User-*`, path-based `ROLE_ADMIN` / employer taxonomy); Feign clients and application/job orchestration; stateless Gemini adapter and fail-open screening/cover-letter; Kafka event publish on status change and SMTP consumer; sanitized `job-portal-config/`; Compose and env topology files; focused tests plus `docs/http/gen_postman.mjs`.

Spring did not write the job/application rules. Docker did not define the bounded contexts.

## Capabilities (what you can show)

1. Signup/login with role in the JWT; self-signup as `ROLE_ADMIN` rejected; `SUSPENDED` / `DELETED` cannot log in.
2. Employer company CRUD-style ownership; gateway admin verify/deactivate.
3. Jobs: draft/publish/close, category/skill/tag writes for employer or admin, list filters, drafts hidden from public company lists.
4. Structured resumes with nested education, experience, projects, skills, languages.
5. Apply to `OPEN` jobs only; duplicate apply rejected; GET by id limited to candidate or employer; employer notes.
6. Gemini: apply-time screening stored as score + shortlist enum; cover letter on the application; skills-gap; `POST /api/jobs/search/natural`.
7. Saved jobs in preferences-service.
8. Optional status email via Kafka topic `application.status.changed` (status still saved if Kafka/SMTP fail).

## 15-minute demo

Use **[DEMO.md](DEMO.md)** with [demo.http](http/demo.http) or [JobMate.postman_collection.json](http/JobMate.postman_collection.json). Start the stack from **[RUN_MODES.md](RUN_MODES.md)** (one mode only).

Suggested path: Eureka up → signup employer + seeker → company → (optional AI job describe) create/publish job → resume with skills → apply (score or `NOT_SCREENED`) → duplicate apply 409 → cover letter → employer company list with `minAiScore` → status change → optional Kafka logs → natural-language search.

Gateway: `5007` (IntelliJ) or `5050` (full Compose). Direct service ports skip JWT.

## Defensible decisions

Documented in [DECISIONS.md](DECISIONS.md):

| Choice | Why |
|---|---|
| MVC gateway | Same blocking model as JPA/Feign; not WebFlux. |
| Native Config Server | Clone is self-contained; secrets stay env vars. |
| JWT at user-service + gateway | Simple to demo; no Keycloak/OIDC in this repo. |
| Feign for apply assembly, Kafka for email | Apply needs consistent reads; mail must not block the status HTTP call. |
| AI without a database | Gemini is an adapter; domain rows stay in application/job/resume. |
| Path-based admin at the gateway | Admin APIs are not under `/api/admin/**`; role is still enforced at the edge. |

## Non-goals and gaps

See **[LIMITATIONS.md](LIMITATIONS.md)** for the full list. Do not claim OpenAPI generation, refresh tokens, file parsing, a search index, Kubernetes, or production deployment.

## Talking points (interview)

**Gateway vs service auth.** The gateway verifies the HMAC JWT, drops inbound `X-User-Id` / `X-User-Email` / `X-User-Role`, and sets them from claims. Admin and some write paths are role-gated **only on gateway routes**. Most services do not re-validate the JWT. That is an explicit local-debug trade-off, not “zero-trust mesh.”

**Fail-open AI.** Apply and cover-letter persist if Gemini or the AI Feign call fails (screening → `NOT_SCREENED`; cover letter returns the stored row). Skills-gap and some direct `/api/ai/**` calls surface the AI error. Natural-language search also fail-opens at the job-service orchestration layer. Product choice: hiring workflow is not blocked by a model outage.

**No outbox.** Application status is committed, then Kafka is published. Those are not one transaction. Lost events or duplicates are possible. An outbox would be the next step if email were a hard requirement.

**Header-trusting direct ports.** Compose/IDE still expose service ports for debugging. A caller who can hit those ports can spoof `X-User-*`. Evaluation traffic should go through the gateway. Internal Feign is the same header model: on an open network, another process could spoof service-to-service calls unless JWT/mTLS were added later.

**DB-per-service cost.** Six databases force you to talk about consistency and chattiness. A monolith would be cheaper; the split is the teaching artifact.
