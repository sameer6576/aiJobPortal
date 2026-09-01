# Run the stack in three modes

This file is the canonical startup guide. Pick **one** mode per machine session. Do not mix them: they compete for Eureka (`8761`), Config Server (`8888`), gateway (`5007` vs Compose `5050`), and Postgres host ports.

Environment topology and IDE troubleshooting: [LOCAL_DEVELOPMENT.md](LOCAL_DEVELOPMENT.md). Reviewer API walkthrough: [DEMO.md](DEMO.md). Tests: [TESTING.md](TESTING.md).

| Mode | Java processes | PostgreSQL | Kafka (optional) | Gateway URL |
|---|---|---|---|---|
| 1. Native | IntelliJ | Homebrew / local installer on `5432` | Local broker on `9092` | `http://localhost:5007` |
| 2. Hybrid | IntelliJ | Compose DB containers | `docker-compose.dev.yaml` or local broker | `http://localhost:5007` |
| 3. Full Compose | None (images) | Compose | `--profile kafka` | `http://localhost:5050` |

Companion frontend:
[ai-job-portal-frontend](https://github.com/sameer6576/ai-job-portal-frontend).
Run `npm run dev` for modes 1–2 (proxy target `5007`) or
`npm run dev:docker` for mode 3 (proxy target `5050`). The frontend does not
need backend secrets.

Commands assume the repo root is `job-portal-system`. Unix examples use bash. From `docker/` on Windows PowerShell, the same `docker compose` lines apply; PowerShell equivalents are shown where the Unix form differs.

---

## Shared once

### Secrets

```bash
cd docker
cp .env.example .env
```

PowerShell:

```powershell
Set-Location docker
Copy-Item .env.example .env
```

Edit `docker/.env`: set `DB_PASSWORD` (Postgres `postgres` role) and `JWT_SECRET` (at least 32 bytes). Optional: `GEMINI_API_KEY`, `MAIL_USERNAME`, `MAIL_PASSWORD`.

Never commit `docker/.env`, `docker/local-native.env`, or `docker/local-hybrid.env`.

### Build (modes 1 and 2)

From the repo root (parent `pom.xml`, no wrapper):

```bash
mvn clean install -DskipTests
```

Use a module `./mvnw` / `mvnw.cmd` if you only rebuild one service.

### IntelliJ env (modes 1 and 2)

Install EnvFile or set environment variables on each run configuration.

| Process | Required env |
|---|---|
| Service registry | none |
| Config Server | none |
| API gateway | `JWT_SECRET` |
| User, company, job, application, preference, resume | topology file below + `DB_PASSWORD` |
| AI | `GEMINI_API_KEY` if you call Gemini |
| Notification | `MAIL_*` if you want SMTP; Kafka must be up |

Start order for every IntelliJ run:

1. `job-portal-service-registry` — `8761`
2. `job-portal-config-server` — `8888`
3. `job-portal-api-gateway` — `5007`
4. `job-portal-user-service` — `5001`
5. Company `5002`, job `5003`, application `5004`, preferences `5005`, resume `5009`, AI `5010`
6. Notification `5011` only if Kafka is running

Working directory for Config Server should be the repo root (`job-portal-system`) so it finds `job-portal-config/`.

---

## Mode 1 — Everything from IntelliJ (no Docker)

Postgres and (optional) Kafka run on the host. No Compose.

### 1. PostgreSQL

Install and start PostgreSQL (Homebrew example):

```bash
brew services start postgresql@17
pg_isready -h localhost -p 5432
```

On Windows, start the PostgreSQL Windows service (or the installer stack) so something listens on `5432`, then use `pg_isready` / `psql` from the PostgreSQL `bin` directory if it is on `PATH`.

If `postmaster.pid` is stale and nothing listens on `5432`, remove the pid file only after you confirm the port is free, then start the service again. Do not `initdb` over an existing data directory.

Create databases (skip any that already exist):

```bash
export PATH="$(brew --prefix postgresql@17)/bin:$PATH"
psql -h localhost -p 5432 -U postgres -d postgres <<'SQL'
CREATE DATABASE job_portal_user;
CREATE DATABASE job_portal_company;
CREATE DATABASE job_portal_job;
CREATE DATABASE job_portal_application;
CREATE DATABASE job_portal_preference;
CREATE DATABASE job_portal_resume;
SQL
```

PowerShell (SQL file or `-c` per database):

```powershell
psql -h localhost -p 5432 -U postgres -d postgres -c "CREATE DATABASE job_portal_user;"
# repeat for company, job, application, preference, resume
```

Set the `postgres` password to the same value as `DB_PASSWORD`:

```bash
psql -h localhost -p 5432 -U postgres -c "ALTER USER postgres PASSWORD 'your-local-password';"
```

### 2. Topology env

```bash
cp docker/local-native.env.example docker/local-native.env
```

```powershell
Copy-Item docker\local-native.env.example docker\local-native.env
```

Set `DB_PASSWORD` and `JWT_SECRET` in `docker/local-native.env` (same `JWT_SECRET` as you will use on the gateway).

Point EnvFile at `docker/local-native.env` for every DB-backed service. Point gateway at the same `JWT_SECRET`.

### 3. Run the apps

Build, then start the IntelliJ configurations in the order above.

Check Config Server:

```bash
curl -s http://localhost:8888/job-portal-user-service/default | head
```

```powershell
curl.exe -s http://localhost:8888/job-portal-user-service/default
```

`propertySources` must be non-empty.

### 4. Optional Kafka + notification (still no Docker)

Stop any container bound to `9092`. Then either Homebrew Kafka or the Apache tarball (KRaft, no ZooKeeper). Confirm something listens on `9092`.

Start notification-service. Application-service already defaults to `KAFKA_BOOTSTRAP_SERVERS=localhost:9092`.

### 5. Call the API

Gateway: **`http://localhost:5007`**

Eureka: `http://localhost:8761`

Config: `http://localhost:8888`

### Stop

Stop IntelliJ run configurations. Stop Kafka if you started it. Optionally stop the local PostgreSQL service if you want Postgres down too.

---

## Mode 2 — IntelliJ apps + Docker databases

Java in the IDE. Postgres (and optionally Kafka) in Docker. Do **not** also run native Postgres on `5432` with `local-native.env` in this session.

### 1. Secrets

`docker/.env` must exist (`DB_PASSWORD`, `JWT_SECRET`). Compose injects `DB_PASSWORD` into the DB containers on **first** volume create. Changing `.env` later does not change an existing volume password.

### 2. Start databases

```bash
cd docker
docker compose up -d userdb companydb jobdb applicationdb preferencedb resumedb
docker compose ps
pg_isready -h localhost -p 5433
pg_isready -h localhost -p 5434
```

| Host | Database |
|---|---|
| `localhost:5433` | `job_portal_user` |
| `localhost:5434` | `job_portal_company` |
| `localhost:5435` | `job_portal_job` |
| `localhost:5436` | `job_portal_application` |
| `localhost:5439` | `job_portal_preference` |
| `localhost:5440` | `job_portal_resume` |

### 3. Topology env

```bash
cp docker/local-hybrid.env.example docker/local-hybrid.env
```

```powershell
Copy-Item docker\local-hybrid.env.example docker\local-hybrid.env
```

Set `DB_PASSWORD` and `JWT_SECRET` to match `docker/.env` (and the password baked into the DB volumes).

EnvFile → `docker/local-hybrid.env` on DB-backed services. Gateway: same `JWT_SECRET`.

### 4. Run the apps

Same IntelliJ order as mode 1. Gateway: **`http://localhost:5007`**.

### 5. Optional Kafka for IntelliJ notification-service

Do not start this if you will use `--profile kafka` (that broker also binds `9092`).

```bash
cd docker
docker compose -f docker-compose.dev.yaml up -d
```

Then start notification-service from IntelliJ (`KAFKA_BOOTSTRAP_SERVERS=localhost:9092` is the default).

### Stop

```bash
cd docker
docker compose stop userdb companydb jobdb applicationdb preferencedb resumedb
docker compose -f docker-compose.dev.yaml down
```

Stop IntelliJ processes.

---

## Mode 3 — Full Docker Compose

No IntelliJ for the backend. Images must already exist as `sameer9599/job-portal-*:latest` (local or pulled) **or** you must rebuild them with Jib (`jib:dockerBuild`). Compose does not compile source.

From `docker/`:

```bash
cd docker
cp .env.example .env   # if not already done
# edit .env
docker compose up -d
docker compose ps
```

Gateway on the host: **`http://localhost:5050`** (maps container `5007` → host `5050`).

Eureka: `http://localhost:8761`. Config: `http://localhost:8888`.

Containers use `SPRING_DATASOURCE_URL` like `jdbc:postgresql://companydb:5432/job_portal_company`. Do not set `DB_HOST=localhost` on those containers.

### Compose profiles

Default `docker compose up -d` starts discovery, config-server, gateway, domain services, AI, and the six Postgres containers. It does **not** start Kafka or notification-service.

`kafka` and `notification-service` are declared with `profiles: ["kafka"]`. They start only with:

```bash
cd docker
docker compose --profile kafka up -d
```

Notification then uses `KAFKA_BOOTSTRAP_SERVERS=kafka:29092`. Do not also run `docker-compose.dev.yaml`.

**Warning:** `application-service` is configured with `KAFKA_BOOTSTRAP_SERVERS=kafka:29092` in Compose even when profile `kafka` is not started. A status change still persists in the database. The Kafka publish can fail after about 3 seconds (`max.block.ms` default 3000 in `KafkaProducerConfig`) because hostname `kafka` is not there.

To get email events, start with `--profile kafka` **and** set SMTP in `docker/.env`. Application status is saved even if Kafka or SMTP fails.

### Discovery and Config Server health checks

Compose `discovery` and `config-server` health checks call `http://localhost:8761/actuator/health` and `http://localhost:8888/actuator/health`. The **source** POMs for `job-portal-service-registry` and `job-portal-config-server` do not include `spring-boot-starter-actuator`. Whether a prebuilt `sameer9599/job-portal-*` image serves those actuator paths is unverified. If those checks never become healthy, inspect the image and logs rather than assuming the current source tree matches the image.

Gateway and user-service source POMs do include actuator; Compose does not health-check those containers the same way.

### Rebuild images (when you change code)

From the repo root, after `common-lib` and dependents are installed:

```bash
mvn -pl common-lib install -DskipTests
# then jib for the modules you changed, e.g.
# mvn -pl cloud/job-portal-api-gateway,services/job-portal-user-service -am -DskipTests jib:dockerBuild
```

Recreate those services:

```bash
cd docker
docker compose up -d --force-recreate gateway user-service
```

### Stop

```bash
cd docker
docker compose --profile kafka down
```

Add `-v` only if you intend to wipe Postgres volumes.

---

## Verify

| Check | Mode 1 / 2 | Mode 3 |
|---|---|---|
| Eureka UI | `http://localhost:8761` | same |
| Config | `curl http://localhost:8888/job-portal-user-service/default` | same |
| Gateway | `http://localhost:5007` | `http://localhost:5050` |
| API walkthrough | [DEMO.md](DEMO.md), [demo.http](http/demo.http), Postman [JobMate.postman_collection.json](http/JobMate.postman_collection.json) | set `baseUrl` / `@gateway` to `http://localhost:5050` |

---

## If it fails

- Placeholder `DB_HOST` / `USER_DB_PORT`: IntelliJ is missing `local-native.env` or `local-hybrid.env`.
- `Connection refused` on `5432`: native Postgres is down (mode 1).
- `Connection refused` on `5434` / `5436`: hybrid DBs are not up, or you loaded native env while expecting Docker ports.
- `password authentication failed`: IntelliJ `DB_PASSWORD` ≠ volume or local role. Postgres Docker only reads `POSTGRES_PASSWORD` on first volume init.
- `JWT_SECRET must contain at least 32 bytes`: gateway or user-service missing the secret.
- Port already in use: another mode still running.
- Mode 3 status update slow / Kafka error in application-service logs: profile `kafka` not started; DB status can still persist (see warning above).
- Mode 3 discovery/config never healthy: actuator health URLs vs image contents (see above).

More detail: [LOCAL_DEVELOPMENT.md](LOCAL_DEVELOPMENT.md).
