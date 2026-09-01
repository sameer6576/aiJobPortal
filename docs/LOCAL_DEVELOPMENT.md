# Local development

Step-by-step commands for native IntelliJ, hybrid, and full Compose: **[RUN_MODES.md](RUN_MODES.md)**. This page covers environment and config topology, IDE startup, and troubleshooting.

API notes: [API.md](API.md). Reviewer flow: [DEMO.md](DEMO.md). Tests: [TESTING.md](TESTING.md). Runnable requests: [demo.http](http/demo.http). Full Postman collection: [JobMate.postman_collection.json](http/JobMate.postman_collection.json).

## Environment and config topology

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

```powershell
Copy-Item docker\.env.example docker\.env
```

For IntelliJ, also copy **one** topology file and fill `DB_PASSWORD` / `JWT_SECRET` (or load `docker/.env` plus the topology file):

| File | Meaning |
|---|---|
| `docker/local-native.env.example` → `local-native.env` | All Java apps in IntelliJ; local Postgres on `localhost:5432` |
| `docker/local-hybrid.env.example` → `local-hybrid.env` | IntelliJ apps; Compose DB containers on `5433`–`5440` |

```powershell
Copy-Item docker\local-native.env.example docker\local-native.env
# or
Copy-Item docker\local-hybrid.env.example docker\local-hybrid.env
```

Services do not load `.env` files themselves.

- **Compose:** uses `docker/.env` automatically. Each app container sets `SPRING_DATASOURCE_URL` (`userdb:5432`, `companydb:5432`, …). `DB_HOST` / `*_DB_PORT` are not required inside those containers.
- **IDE:** EnvFile (or run-configuration env) with secrets plus one topology file. Registry and Config Server do not need DB vars. Gateway needs `JWT_SECRET`. Each DB-backed service needs `DB_HOST`, `DB_PASSWORD`, and its `*_DB_PORT`.
- **Terminal (bash):** `set -a && source docker/.env && source docker/local-native.env && set +a` (or `local-hybrid.env`).
- **Terminal (PowerShell):** set the same keys on the process (`$env:DB_PASSWORD = '...'`, `$env:JWT_SECRET = '...'`, plus topology vars from the copied file). Do not paste real secrets into committed files.

### Frontend environment

The companion React app only needs the gateway address. Use `npm run dev`
(`VITE_GATEWAY_URL=http://localhost:5007`) for native/hybrid mode or
`npm run dev:docker` (`http://localhost:5050`) for full Compose. It does not
read `docker/.env`, and backend secrets must never be copied into `VITE_*`
variables because those values are exposed to the browser.

Config Server looks for `job-portal-config/` relative to the process working directory (module, repo root). Override with `CONFIG_REPOSITORY_PATH` if needed (Compose uses `file:/config`).

`job-portal-config/` inside this repo is the only source of truth. A second checkout next to `job-portal-system/` can shadow it.

Runtime Hibernate for domain services is `ddl-auto: update` in `job-portal-config/` (tables created or evolved on start). Tests use H2 and `create-drop`; see [TESTING.md](TESTING.md). There are no Flyway/Liquibase migrations.

## Databases

### Native (IntelliJ only)

One PostgreSQL on `localhost:5432` (Homebrew, Windows installer, etc.). Create:

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

Commands: [RUN_MODES.md](RUN_MODES.md) mode 2.

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

Drop a database by hand for a clean slate; `ddl-auto: update` will recreate schema on next start.

## Build

The parent reactor has no wrapper:

```bash
mvn clean install
```

Each executable module has `mvnw` and `mvnw.cmd` for module-level work.

## IDE startup

Start in this order (ports are the process listen ports):

1. `job-portal-service-registry` — `8761`
2. `job-portal-config-server` — `8888`
3. `job-portal-api-gateway` — `5007`
4. `job-portal-user-service` — `5001`
5. Remaining services as required by the flow (company `5002`, job `5003`, application `5004`, preferences `5005`, resume `5009`, AI `5010`)

Load `local-native.env` or `local-hybrid.env` on every DB-backed run configuration (user, company, job, application, preference, resume). Gateway only needs `JWT_SECRET`. Config Server working directory: repo root.

Config Server should expose:

```text
http://localhost:8888/job-portal-user-service/default
```

Use the gateway for API calls:

```text
http://localhost:5007
```

Full Compose publishes the gateway at `http://localhost:5050`. Exact mode commands and “do not mix stacks”: [RUN_MODES.md](RUN_MODES.md).

Direct service ports are for local debugging only; they skip gateway JWT checks.

## Kafka and mail

Optional. With no broker reachable, application-service gives up on the publish after ~3 seconds (`spring.kafka.producer.properties.max.block.ms`, default 3000 in `KafkaProducerConfig`), logs the failure, and the status change is still persisted.

Host-run application and notification services should use `KAFKA_BOOTSTRAP_SERVERS=localhost:9092`. Compose containers use `kafka:29092`.

**Full Compose:** `application-service` is always set to `kafka:29092`, including when you did not pass `--profile kafka`. Status updates can then take ~3s and log a publish failure while the row in Postgres is still updated. Details: [RUN_MODES.md](RUN_MODES.md) mode 3.

If `MAIL_PASSWORD` is unset, notification-service will fail the send and the Kafka consumer will log the error. Application status is still saved.

Do not start both `docker-compose.dev.yaml` Kafka and Compose profile `kafka` at once; they both bind host port `9092`.

## Common failures

**Could not resolve placeholder `DB_HOST` / `USER_DB_PORT` (etc.):** that JVM has no topology env. Load `docker/local-native.env` or `docker/local-hybrid.env` (plus `DB_PASSWORD`). Compose app containers do not need these; they set `SPRING_DATASOURCE_URL`.

**`JWT_SECRET must contain at least 32 bytes` on startup:** the variable is missing or too short in that process. Set the same `JWT_SECRET` for user-service and gateway.

**Datasource connection refused:** the URL resolved, but nothing is listening. Native → Postgres on `5432`. Hybrid → the matching Compose DB container (e.g. `5434` for company).

**Config not found, or wrong values served:** `curl http://localhost:8888/job-portal-resume-service/default` (PowerShell: `curl.exe`) should list `propertySources` and the path it read them from. An empty list means the working directory does not match any search location; stale ports or passwords mean a second `job-portal-config` checkout is shadowing the one in the repo. Restart Config Server after changing search paths. Docker Compose already sets `CONFIG_REPOSITORY_PATH=file:/config`.

**Feign service unavailable:** verify the target service is registered in Eureka with the application name expected by the Feign client.

**Compose discovery/config stuck unhealthy:** health checks hit `/actuator/health`. Registry and Config Server source POMs do not include actuator; prebuilt image behavior is unverified. See [RUN_MODES.md](RUN_MODES.md) mode 3.
