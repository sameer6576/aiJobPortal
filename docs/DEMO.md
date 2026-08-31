# Demo

This document is the walkthrough. Runnable requests are in [demo.http](http/demo.http). A full Postman collection (every gateway endpoint) is [JobMate.postman_collection.json](http/JobMate.postman_collection.json).

## Environment

- Eureka: `http://localhost:8761`
- Config Server: `http://localhost:8888`
- Gateway: `http://localhost:5007` (IntelliJ) or `http://localhost:5050` (full Compose). How to start each stack: [RUN_MODES.md](RUN_MODES.md).
- PostgreSQL: Docker Compose ports listed in `LOCAL_DEVELOPMENT.md`

## Walkthrough

1. Open Eureka and confirm the gateway and required services are registered.
2. Sign up an employer and a job seeker through `/auth/signup`.
3. Log in both users and retain their access tokens.
4. Create the employer's company.
5. Optional: `POST /api/ai/job/describe`, then create and publish a job.
6. Create the seeker's structured resume with skills.
7. Submit an application. The response includes `aiScore` and `aiShortListStatus` when Gemini is configured; otherwise `NOT_SCREENED`.
8. Verify a duplicate application is rejected.
9. `POST /api/applications/{id}/cover-letter` as the seeker.
10. Employer `GET /api/applications/company?minAiScore=50`.
11. Change application status as the employer.
12. If `docker compose --profile kafka up -d` and mail are configured, inspect notification-service logs for delivery. Status is saved even if Kafka or SMTP fails.
13. `POST /api/jobs/search/natural` with `{ "query": "remote java jobs in bangalore" }`.

## Expected limitations

- Screening and cover-letter generation are fail-open: a Gemini outage does not block apply or the cover-letter endpoint.
- Gateway `ROLE_ADMIN` is required for user list/suspend/activate/delete, company verify/deactivate, and `GET /api/jobs/admin`. Job category/skill/tag writes need `ROLE_ADMIN` or `ROLE_EMPLOYER`. Direct service ports skip that check.
- Kafka publish is not in the same transaction as the status update.
- There is no job-alert product; `/api/ai/alert-suggestion` was removed.

The walkthrough uses the gateway. Direct service calls skip JWT verification.
