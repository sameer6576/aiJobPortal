# Local development

Step-by-step commands for native IntelliJ, hybrid, and full Compose: [RUN_MODES.md](RUN_MODES.md).

## Environment

Secrets (`docker/.env`) and **where Postgres lives** are process environment. Config Server does not default `DB_HOST` or `*_DB_PORT`. Host-run services fail at placeholder resolution if those are missing.

Create `docker/.env` from `docker/.env.example` (Compose and shared secrets):

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

For IntelliJ, also copy **one** topology file and fill `DB_PASSWORD` / `JWT_SECRET` (or load `docker/.env` plus the topology file):

| File | Meaning |
|---|---|
| `docker/local-native.env.example` → `local-native.env` | All Java apps in IntelliJ; Homebrew Postgres on `localhost:5432` |
| `docker/local-hybrid.env.example` → `local-hybrid.env` | IntelliJ apps; Compose DB containers on `5433`–`5440` |

Services do not load `.env` files themselves.

- **Compose:** uses `docker/.env` automatically. Each app container sets `SPRING_DATASOURCE_URL` (`userdb:5432`, `companydb:5432`, …). `DB_HOST` / `*_DB_PORT` are not required inside those containers.
- **IDE:** EnvFile (or run-configuration env) with secrets plus one topology file. Registry and Config Server do not need DB vars. Gateway needs `JWT_SECRET`. Each DB-backed service needs `DB_HOST`, `DB_PASSWORD`, and its `*_DB_PORT`.
- **Terminal:** `set -a && source docker/.env && source docker/local-native.env && set +a` (or `local-hybrid.env`).

Config Server looks for `job-portal-config/` relative to the process working directory (module, repo root). Override with `CONFIG_REPOSITORY_PATH` if needed (Compose uses `file:/config`).

`job-portal-config/` inside this repo is the only source of truth. A second checkout next to `job-portal-system/` can shadow it.

## Databases

### Native (IntelliJ only)

One PostgreSQL on `localhost:5432` (Homebrew, installer, etc.). Create:

```text
job_portal_user
job_portal_company
job_portal_job
job_portal_application
job_portal_preference
job_portal_resume
```

Load `docker/local-native.env`. The `postgres` role password must match `DB_PASSWORD`. Hibernate `ddl-auto: update` creates tables on first start.

Major version may differ from Compose (`postgres:16`).

### Hybrid (IntelliJ + Compose DBs)

```bash
cd docker
docker compose up -d userdb companydb jobdb applicationdb preferencedb resumedb
docker compose ps
```

Load `docker/local-hybrid.env`.

| Database | Host address | Database name |
|---|---|---|
| User | `localhost:5433` | `job_portal_user` |
| Company | `localhost:5434` | `job_portal_company` |
| Job | `localhost:5435` | `job_portal_job` |
| Application | `localhost:5436` | `job_portal_application` |
| Preferences | `localhost:5439` | `job_portal_preference` |
| Resume | `localhost:5440` | `job_portal_resume` |

Do not run native Postgres on `5432` *and* these containers if you mix env files by mistake.

Schema is managed with `ddl-auto: update`. Drop a database by hand for a clean slate.

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

Load `local-native.env` or `local-hybrid.env` on every DB-backed run configuration (user, company, job, application, preference, resume). Gateway only needs `JWT_SECRET`.

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

**Could not resolve placeholder `DB_HOST` / `USER_DB_PORT` (etc.):** that JVM has no topology env. Load `docker/local-native.env` or `docker/local-hybrid.env` (plus `DB_PASSWORD`). Compose app containers do not need these; they set `SPRING_DATASOURCE_URL`.

**`JWT_SECRET must contain at least 32 bytes` on startup:** the variable is missing or too short in that process. Set the same `JWT_SECRET` for user-service and gateway.

**Datasource connection refused:** the URL resolved, but nothing is listening. Native → Postgres on `5432`. Hybrid → the matching Compose DB container (e.g. `5434` for company).

**Config not found, or wrong values served:** `curl http://localhost:8888/job-portal-resume-service/default` should list `propertySources` and the path it read them from. An empty list means the working directory does not match any search location; stale ports or passwords mean a second `job-portal-config` checkout is shadowing the one in the repo. Restart Config Server after changing search paths. Docker Compose already sets `CONFIG_REPOSITORY_PATH=file:/config`.

**Feign service unavailable:** verify the target service is registered in Eureka with the application name expected by the Feign client.
