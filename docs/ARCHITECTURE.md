# Architecture

## Scope

JobMate models the backend workflow from account creation to an employer's application decision. It is not a full job marketplace: recruiter resume search, payments, public SEO pages, file parsing, and large-scale search ranking are outside the current scope.

## Runtime components

```mermaid
flowchart TB
  Client[HTTPClient] --> Gateway[APIGateway]
  Gateway --> User[UserService]
  Gateway --> Company[CompanyService]
  Gateway --> Job[JobService]
  Gateway --> Resume[ResumeService]
  Gateway --> Application[ApplicationService]
  Gateway --> Preferences[PreferencesService]
  Gateway --> AI[AIService]
  Application --> User
  Application --> Company
  Application --> Job
  Application --> Resume
  Job --> Company
  Application --> Kafka[KafkaTopic]
  Kafka --> Notification[NotificationService]
  Notification --> SMTP[SMTP]
  AI --> Gemini[GeminiAPI]
  Gateway --> Eureka[Eureka]
  User --> Config[ConfigServer]
  Company --> Config
  Job --> Config
  Resume --> Config
  Application --> Config
  Preferences --> Config
  AI --> Config
```

The API Gateway is the public boundary. It validates an HMAC-signed JWT issued by user-service and derives identity headers for downstream services. Direct service ports exist for local debugging and must not be exposed in a production network.

## Data ownership

| Service | Owned data |
|---|---|
| user-service | Users, credentials, roles, account status |
| company-service | Companies and social links |
| job-service | Jobs, categories, skills, and tags |
| resume-service | Resumes, personal details, experience, education, projects, skills, languages |
| application-service | Applications and employer notes |
| preferences | Saved jobs |
| AI and notification | Stateless |

Services store foreign identifiers rather than cross-database relationships. For example, an application stores candidate, employer, company, job, and resume IDs, then resolves views through Feign clients.

## Synchronous collaboration

- job-service calls company-service to resolve the employer's company.
- application-service calls user, company, job, and resume services.
- Eureka service names are used by OpenFeign and the gateway load balancer.

This keeps ownership clear but makes application response assembly dependent on several services. Timeouts, retries, and graceful degradation remain future reliability work.

## Asynchronous collaboration

`ApplicationEventPublisher` sends `ApplicationStatusChangedEvent` to `application.status.changed`. `NotificationKafkaConsumer` consumes it and sends an HTML email with JavaMail.

The database update and Kafka publish are not atomic. An outbox would be appropriate if notification delivery became a strict business requirement.

## Configuration

The Config Server uses the native backend and reads `job-portal-config/`. Docker mounts that directory read-only at `/config`; host execution can override `CONFIG_REPOSITORY_PATH`.

Compose is the port and database-name reference. Host-run services connect to the published PostgreSQL ports; container-run services override datasource URLs with Docker service names.

## AI boundary

AI service is a stateless Gemini adapter. It contains prompts and typed response parsing but does not own user, job, resume, or application data. Application-time orchestration is not implemented yet; current endpoints accept the context they need in the request body.

This boundary keeps external-model concerns separate from transactional services and makes model failure visible rather than part of a database transaction.
