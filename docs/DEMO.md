# Demo (about 15 minutes)

Reviewer walkthrough for **both roles** (`ROLE_EMPLOYER` and `ROLE_JOB_SEEKER`) through the gateway. Runnable clone of most steps: [demo.http](http/demo.http). Every gateway route: [JobMate.postman_collection.json](http/JobMate.postman_collection.json). Path notes: [API.md](API.md). How to start the stack: [RUN_MODES.md](RUN_MODES.md).

There is no documented admin seed and no admin self-signup (`ROLE_ADMIN` is rejected at signup). Stay on employer and seeker.

## Prerequisites

1. One run mode is up ([RUN_MODES.md](RUN_MODES.md)). Do not mix IntelliJ and full Compose.
2. `docker/.env` (or IntelliJ env) has `JWT_SECRET` (≥ 32 bytes) and `DB_PASSWORD`. Optional: `GEMINI_API_KEY`, SMTP (`MAIL_*`), Kafka.
3. You will create, in order: employer + seeker accounts, **company**, **job category**, **job**, then **publish** the job. A job without a category cannot be created as in [demo.http](http/demo.http). Apply only works for `OPEN` jobs.
4. Gateway base URL (set `@gateway` in `demo.http` or Postman `baseUrl`):
   - IntelliJ (modes 1 and 2): **`http://localhost:5007`**
   - Full Compose (mode 3): **`http://localhost:5050`**

Optional integrations:

- **Gemini:** screening, cover letter, skills-gap, natural search, and `POST /api/ai/job/describe` work when `GEMINI_API_KEY` is set on AI-service. Without it, apply still succeeds with `aiShortListStatus=NOT_SCREENED`; cover-letter is fail-open.
- **Kafka + SMTP:** `docker compose --profile kafka up -d` (mode 3) or a host broker (modes 1–2) plus mail vars. Status is saved even if publish or SMTP fails. In full Compose **without** profile `kafka`, application-service still targets `kafka:29092`; the status HTTP call can take ~3s and log a Kafka failure while the DB row is updated. See [RUN_MODES.md](RUN_MODES.md).

## Environment

- Eureka: `http://localhost:8761`
- Config Server: `http://localhost:8888`
- Gateway: `http://localhost:5007` (IntelliJ) or `http://localhost:5050` (full Compose)
- PostgreSQL ports: [LOCAL_DEVELOPMENT.md](LOCAL_DEVELOPMENT.md)

Use `Authorization: Bearer <token>` on `/api/**`. Call the gateway, not service ports (direct ports skip JWT).

## Walkthrough

### 1. Health of the mesh (~1 min)

Open Eureka and confirm gateway, user, company, job, resume, application, and preferences (plus AI if you will use Gemini) are registered.

### 2. Sign up and log in both roles (~2 min)

`POST /auth/signup` for a seeker (`ROLE_JOB_SEEKER`) and an employer (`ROLE_EMPLOYER`), then `POST /auth/login` for each. Keep both access tokens. Passwords must be at least 8 characters. Example bodies: [demo.http](http/demo.http).

### 3. Employer: company, category, job, publish (~3 min)

1. `POST /api/companies` as the employer (required before a job with that company).
2. `POST /api/job-categories` as the employer (`ROLE_EMPLOYER` or `ROLE_ADMIN` on the gateway).
3. Optional: `POST /api/ai/job/describe` (Gemini).
4. `POST /api/jobs` with `categoryId` from the category, then `PATCH /api/jobs/{id}/publish`.

### 4. Seeker: structured resume (~2 min)

Resumes are structured records (not PDF upload). `POST /api/resumes`, then add nested data used for screening, at least a skill: `POST /api/resumes/{id}/skills` (see [demo.http](http/demo.http)). Education, work experience, and projects are optional for a short demo.

### 5. Seeker: save job, apply, duplicate apply (~3 min)

1. `POST /api/preferences/saved-jobs` with `{ "jobId": <publishedJobId> }`. List with `GET /api/preferences/saved-jobs`.
2. `POST /api/applications` with `{ "jobId", "resumeId" }`. Response includes `aiScore` and `aiShortListStatus` when Gemini succeeds (`>=80` AUTO_SHORTLISTED, `>=50` REVIEW_RECOMMENDED, else LOW_MATCH); otherwise `NOT_SCREENED`.
3. Repeat the same apply; expect conflict / already applied (`ALREADY_APPLIED`).
4. Optional: `POST /api/applications/{id}/cover-letter` and `GET /api/applications/{id}/skills-gap` as the seeker.

### 6. Seeker: natural-language search (~1 min)

`POST /api/jobs/search/natural` with `{ "query": "remote java jobs in bangalore" }` (seeker token). Uses AI-service when Gemini is configured.

### 7. Employer: filter, notes, status (~3 min)

1. `GET /api/applications/company?minAiScore=50` (employer token).
2. `POST /api/applications/{id}/notes` with `{ "content": "..." }`; `GET /api/applications/{id}/notes`.
3. `PATCH /api/applications/{id}/status` with `{ "status": "REVIEWING" }` (or another valid status). If Kafka and mail are up, check notification-service logs. Status is stored even when Kafka/SMTP fail.

## Expected limitations

- Screening and cover-letter generation are fail-open: a Gemini outage does not block apply or the cover-letter endpoint.
- Gateway `ROLE_ADMIN` is required for user list/suspend/activate/delete, company verify/deactivate, and `GET /api/jobs/admin`. Job category/skill/tag writes need `ROLE_ADMIN` or `ROLE_EMPLOYER`. Direct service ports skip that check.
- Kafka publish is not in the same transaction as the status update.
- There is no job-alert product; `/api/ai/alert-suggestion` was removed.
