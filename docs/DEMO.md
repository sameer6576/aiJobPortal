# Demo

This document defines the current portfolio walkthrough. A complete runnable request file will be added with later authorization, Kafka, and AI integration hardening.

## Environment

- Eureka: `http://localhost:8761`
- Config Server: `http://localhost:8888`
- Gateway: `http://localhost:5007`
- PostgreSQL: Docker Compose ports listed in `LOCAL_DEVELOPMENT.md`

## Walkthrough

1. Open Eureka and confirm the gateway and required services are registered.
2. Sign up an employer and a job seeker through `/auth/signup`.
3. Log in both users and retain their access tokens.
4. Create the employer's company.
5. Create and publish a job.
6. Create the seeker's structured resume.
7. Submit an application.
8. Verify a duplicate application is rejected.
9. Change application status as the employer.
10. If Kafka and mail are configured, inspect notification-service for delivery.
11. Call a Gemini endpoint with non-sensitive sample content if an API key is configured.

## Expected limitations

- Application creation does not automatically run Gemini screening yet.
- Kafka and notification are not included in the core Compose startup.
- Several authorization checks and consistent error responses remain hardening work.

The walkthrough intentionally uses the gateway. Direct service calls bypass the current authentication boundary and are not part of the demo.
