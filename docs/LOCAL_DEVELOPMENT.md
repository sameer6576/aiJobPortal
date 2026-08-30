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

Services read secrets only from the process environment (`${DB_PASSWORD}`, `${JWT_SECRET}`, …). They do not load `.env` files.

- **Compose:** uses `docker/.env` automatically.
- **IDE:** point a shared EnvFile / environment-file setting at `docker/.env`, or set the same keys on a run-configuration template. Config Server does not need `DB_PASSWORD`.
- **Terminal:** `set -a && source docker/.env && set +a` then start processes from that shell.

Config Server looks for `job-portal-config/` relative to the process working directory, covering the module directory, the repo root, and the folder above the repo. Override with `CONFIG_REPOSITORY_PATH` if you run it from somewhere else (Compose uses `file:/config`).

`job-portal-config/` inside the repo is the only source of truth. A second checkout of the config repo next to `job-portal-system/` will shadow it and hand out stale ports and passwords, so keep only one copy on disk.

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

Those host ports are the defaults baked into the Config Server files. Point a service somewhere else with `DB_HOST`, `DB_USERNAME`, or the per-service port variables (`USER_DB_PORT`, `COMPANY_DB_PORT`, `JOB_DB_PORT`, `APPLICATION_DB_PORT`, `PREFERENCE_DB_PORT`, `RESUME_DB_PORT`). Compose bypasses all of this by setting `SPRING_DATASOURCE_URL` directly on each container.

Schema is managed with `ddl-auto: update`, so data survives restarts. Drop and recreate a database by hand when you want a clean slate.

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

## Full stack in Compose

```bash
cd docker
docker compose up -d
```

The gateway is published on `http://localhost:5050` in Compose, not 5007. Run either the IDE stack or the Compose stack, not both: they compete for the database host ports and for Eureka on 8761.

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

Kafka is optional. With no broker reachable, application-service gives up on the publish after ~3 seconds (`spring.kafka.producer.properties.max.block.ms`), logs the failure, and the status change is still persisted.

If `MAIL_PASSWORD` is unset, notification-service will fail the send and the Kafka consumer will log the error. Application status is still saved.

## Common failures

**`JWT_SECRET must contain at least 32 bytes` on startup:** the variable is missing or too short in that process. Set the same value for user-service and gateway.

**Datasource connection refused:** confirm the service is using the host port from the table, not PostgreSQL's internal `5432`.

**Config not found, or wrong values served:** `curl http://localhost:8888/job-portal-resume-service/default` should list `propertySources` and the path it read them from. An empty list means the working directory does not match any search location; stale ports or passwords mean a second `job-portal-config` checkout is shadowing the one in the repo. Restart Config Server after changing search paths. Docker Compose already sets `CONFIG_REPOSITORY_PATH=file:/config`.

**Feign service unavailable:** verify the target service is registered in Eureka with the application name expected by the Feign client.
