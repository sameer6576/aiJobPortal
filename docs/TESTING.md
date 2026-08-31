# Testing

Automated tests live in the Maven reactor. CI and local verify use the same command. Startup of the live stack is [RUN_MODES.md](RUN_MODES.md). API walkthrough is [DEMO.md](DEMO.md) and [demo.http](http/demo.http).

## Commands

From the repo root (`job-portal-system`). There is no root Maven wrapper.

Full reactor (what GitHub Actions runs):

```bash
mvn -B verify
```

Targeted modules (from the same root; `-am` builds required reactor modules such as `common-lib`):

```bash
mvn -B -pl services/job-portal-user-service -am verify
mvn -B -pl services/job-portal-application-service -am verify
mvn -B -pl services/job-portal-job-service -am verify
```

Several modules together:

```bash
mvn -B -pl services/job-portal-user-service,services/job-portal-application-service,services/job-portal-job-service -am verify
```

Module wrappers (`mvnw` / `mvnw.cmd`) can run tests inside a single executable module after that module’s dependencies are installed.

## CI

[`.github/workflows/build.yml`](../.github/workflows/build.yml) runs only `mvn -B verify` on Temurin 21. It does not build Jib images, start Compose, or call Gemini/Kafka/SMTP.

## What exists

### Domain / slice tests

| Class | Module | What it covers |
|---|---|---|
| `AuthServiceImplTest` | `services/job-portal-user-service` | Signup puts the requested role on the JWT; signup rejects `ROLE_ADMIN` self-registration; login rejects `SUSPENDED` users (no token). Mockito unit tests, no Spring context. |
| `UserServiceImplTest` | `services/job-portal-user-service` | `updateProfile` persists a supplied phone while leaving an omitted full name unchanged. Mockito unit test. |
| `ApplicationServiceImplTest` | `services/job-portal-application-service` | Apply sets `AUTO_SHORTLISTED` when AI score is high; Gemini failure leaves `NOT_SCREENED`; duplicate apply fails before AI/job calls; draft jobs cannot be applied to; `getApplicationById` rejects an unrelated user. Mockito unit tests. |
| `JobSpecificationTest` | `services/job-portal-job-service` | `@DataJpaTest` on H2: `JobSpecification` filters by category and `minOpenings` (`opening` column). |

### Context-load smoke tests (11)

Each is `@SpringBootTest` with a `contextLoads()` method:

| Class | Module |
|---|---|
| `JobPortalServiceRegistryApplicationTests` | `cloud/job-portal-service-registry` |
| `JobPortalConfigServerApplicationTests` | `cloud/job-portal-config-server` |
| `JobPortalApiGatewayApplicationTests` | `cloud/job-portal-api-gateway` |
| `JobPortalUserServiceApplicationTests` | `services/job-portal-user-service` |
| `JobPortalCompanyServiceApplicationTests` | `services/job-portal-company-service` |
| `JobPortalJobServiceApplicationTests` | `services/job-portal-job-service` |
| `JobPortalApplicationServiceApplicationTests` | `services/job-portal-application-service` |
| `JobPortalPreferencesApplicationTests` | `services/job-portal-preferences` |
| `JobPortalResumeServiceApplicationTests` | `services/job-portal-resume-service` |
| `JobPortalAiServiceApplicationTests` | `services/job-portal-ai-service` |
| `JobPortalNotificationServiceApplicationTests` | `services/job-portal-notification-service` |

These assert that the Spring context starts with **test** configuration, not that HTTP, Kafka, or Gemini work end to end.

## H2 / `ddl-auto` vs runtime

Runtime Config Server YAML (`job-portal-config/`) uses PostgreSQL and `spring.jpa.hibernate.ddl-auto: update`. See [LOCAL_DEVELOPMENT.md](LOCAL_DEVELOPMENT.md).

Tests that start a datasource use **in-memory H2** (`MODE=PostgreSQL`) and **`ddl-auto: create-drop`**:

- Domain services: `src/test/resources/application.yaml` (user, company, job, application, preferences, resume) disable Config Server and Eureka, set a test JWT secret where needed.
- `JobSpecificationTest` repeats H2 + `create-drop` via `@TestPropertySource`.

Do not assume test schema or H2 dialect match production Postgres `update` behavior for every mapping.

## Untested or only lightly covered

These areas have little or no automated coverage in this repo:

- HTTP through the gateway (JWT filter, role rules, routing)
- Feign clients and service-to-service calls
- Company, resume, preferences, AI, and notification **business** logic (smoke context only)
- Kafka publish/consume, SMTP, Gemini live calls
- Duplicate-apply and status flows as HTTP
- Docker Compose, Jib images, actuator health used by Compose
- Flyway-style migrations (none exist; schema is Hibernate)

Manual API checks use [demo.http](http/demo.http) and [JobMate.postman_collection.json](http/JobMate.postman_collection.json) against a running gateway ([DEMO.md](DEMO.md)).
