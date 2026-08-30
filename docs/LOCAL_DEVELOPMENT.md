# Local development

## Environment

Create `docker/.env` from `docker/.env.example`:

```dotenv
DB_PASSWORD=
JWT_SECRET=
GEMINI_API_KEY=
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=
```

`JWT_SECRET` must contain at least 32 bytes. Gemini and mail values are optional unless those integrations are started.

For IDE runs, set `DB_PASSWORD` and `JWT_SECRET` in each relevant process environment. Config Server looks for `job-portal-config/` relative to the process working directory (module, repo root, or `cloud/`). Override only if needed with `CONFIG_REPOSITORY_PATH` (Compose uses `file:/config`).

## Start databases

From `docker/`:

```bash
docker compose up -d userdb companydb jobdb applicationdb preferencedb resumedb
docker compose ps
```

| Database | Host address | Database name |
|---|---|---|
| User | `localhost:5433` | `job_portal_user` |
| Company | `localhost:5434` | `job_portal_company` |
| Job | `localhost:5435` | `job_portal_job` |
| Application | `localhost:5436` | `job_portal_application` |
| Preferences | `localhost:5439` | `job_portal_preference` |
| Resume | `localhost:5440` | `job_portal_resume` |

## Build

The parent reactor has no wrapper:

```bash
mvn clean install
```

Each executable module has `mvnw` and `mvnw.cmd` for module-level work.

## Start services

Start in this order:

1. `job-portal-service-registry` — port 8761
2. `job-portal-config-server` — port 8888
3. `job-portal-api-gateway` — port 5007
4. `job-portal-user-service` — port 5001
5. Remaining services as required by the flow

Config Server should expose:

```text
http://localhost:8888/job-portal-user-service/default
```

Use the gateway for API calls:

```text
http://localhost:5007
```

Direct service ports are for local debugging only.

## Kafka

For services running in Compose:

```bash
docker compose --profile kafka up -d
```

Containers talk to Kafka at `kafka:29092`. Host-run application and notification services should use `KAFKA_BOOTSTRAP_SERVERS=localhost:9092`.

Host-only broker (IDE processes, no notification container):

```bash
docker compose -f docker-compose.dev.yaml up -d
```

Do not start both Kafka definitions at once; they both bind host port 9092.

If `MAIL_PASSWORD` is unset, notification-service will fail the send and the Kafka consumer will log the error. Application status is still saved.

## Common failures

**JWT startup failure:** set the same `JWT_SECRET` for user-service and gateway.

**Datasource connection refused:** confirm the service is using the host port from the table, not PostgreSQL's internal `5432`.

**Config not found:** `curl http://localhost:8888/job-portal-resume-service/default` should list `propertySources`. Restart Config Server after changing search paths. Docker Compose already sets `CONFIG_REPOSITORY_PATH=file:/config`.

**Feign service unavailable:** verify the target service is registered in Eureka with the application name expected by the Feign client.
