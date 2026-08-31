# Contributing

This repository is the JobMate **backend**. Changes should stay inside a service’s ownership boundary and be evaluable through the API gateway.

## Development

1. Use JDK 21 and Maven 3.9 or a module wrapper.
2. Copy `docker/.env.example` to `docker/.env`. Do not put secret values in Git, docs, logs, or PR descriptions.
3. Start databases as in [Local development](docs/LOCAL_DEVELOPMENT.md). Pick one stack from [Run modes](docs/RUN_MODES.md).
4. Build from the repository root (`mvn` — there is no root wrapper).
5. Start Eureka, Config Server, Gateway, then only the services the change needs.

## Ownership boundaries

- Each persistent service owns its database. Do not add JPA associations or joins across services.
- Store foreign identifiers, then load related views with Feign. Do not share tables.
- AI service stays stateless: prompts and parsing only. User, job, resume, and application rows stay in their services.
- Notification service consumes Kafka and sends mail; it does not own application state.
- Prefer a focused PR: one behavior or one infrastructure concern.

## Identity

Caller identity is **only** the gateway-derived headers `X-User-Id`, `X-User-Email`, and `X-User-Role` after JWT verification.

- Never take user id, email, or role from the request body, query, or path as the authenticated principal.
- Never trust client-supplied `X-User-*` headers on the gateway; the gateway strips them and sets claims from the JWT ([Security](docs/SECURITY.md)).
- Direct service ports still trust those headers. Do not document or ship a flow that bypasses the gateway for authorization.

## Secrets and generated artifacts

Do not commit credentials, tokens, personal records, `docker/.env`, IDE run configs with secrets, or generated binaries. Document new env **names** in [Local development](docs/LOCAL_DEVELOPMENT.md) without values.

## Tests

From the repository root:

```bash
mvn -B verify
```

- Add focused domain tests with new write/auth behavior (ownership, status, screening fail-open, filters).
- Prefer those over extra context-load tests.
- If Kafka, mail, or Gemini is involved, say which local config was used — no secret values.

## Gateway flow

Before review, exercise the changed path **through the gateway** (`5007` host / `5050` Compose), not a service port. Confirm JWT, role gates, and identity headers behave as in [API notes](docs/API.md) and [Demo](docs/DEMO.md).

## Docs and Postman

If you add or change a gateway route, DTO, error code, or env var, update:

- [API notes](docs/API.md) and, if the walkthrough changes, [Demo](docs/DEMO.md)
- `docs/http/demo.http` when the 15-minute path changes
- Postman: edit `docs/http/gen_postman.mjs` and regenerate with `node docs/http/gen_postman.mjs` (do not hand-edit `JobMate.postman_collection.json` as the source of truth)
- [Limitations](docs/LIMITATIONS.md) / [Decisions](docs/DECISIONS.md) if the contract or a trade-off changed

There is no generated OpenAPI document; the Postman generator is the full-route catalog.
