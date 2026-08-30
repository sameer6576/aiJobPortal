# Contributing

## Development

1. Use JDK 21 and Maven 3.9 or a module wrapper.
2. Copy `docker/.env.example` to `docker/.env`.
3. Start the databases described in `docs/LOCAL_DEVELOPMENT.md`.
4. Build from the repository root.
5. Start Eureka, Config Server, Gateway, then the services needed for the change.

## Change guidelines

- Keep service ownership explicit; do not add cross-database entity relationships.
- Derive caller identity from verified gateway claims, not request-body identifiers.
- Do not commit credentials, access tokens, personal records, generated files, or IDE state.
- Add validation and ownership checks with new write endpoints.
- Prefer focused domain tests over additional context-load tests.
- Document new environment variables and API behavior.
- Keep pull requests limited to one behavior or infrastructure concern.

## Verification

Before requesting review:

```bash
mvn test
```

Also run the relevant flow through the gateway. If a change affects Kafka, mail, or Gemini, describe the local configuration used without including secret values.
