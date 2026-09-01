# API notes

The HTTP API is the gateway. There is no `/v1` prefix, no pagination on list endpoints, and no generated OpenAPI document. These notes are a contract sketch, not a DTO catalog.

Runnable walkthrough: [demo.http](http/demo.http). Every gateway route: [JobMate.postman_collection.json](http/JobMate.postman_collection.json) (import into Postman).

## Gateway

Public (no JWT): `/auth/**` → user-service.

Anonymous-or-optional JWT (missing `Authorization` is anonymous; a supplied Bearer token is validated and identity headers are injected; malformed/invalid token → 401):

| Method | Path | Notes |
|---|---|---|
| GET | `/api/jobs` | Numeric-id detail is `GET /api/jobs/{id}` (`id` digits only). Optional JWT lets the posting employer or an admin see a **DRAFT** by id. |
| GET | `/api/job-categories`, `/api/job-skills`, `/api/job-tags` | List |
| GET | `/api/job-categories/{id}`, `/api/job-skills/{id}`, `/api/job-tags/{id}` | Numeric id |

These optional-auth GETs do **not** include `/api/jobs/my`, `/api/jobs/admin`, `/api/jobs/company/{companyId}`, or `/api/jobs/search/natural`. Writes stay protected.

Protected (`Authorization: Bearer <access-token>`). The gateway validates the JWT, then replaces `X-User-Id`, `X-User-Email`, and `X-User-Role` from claims (`email`, `authorities`, `userId`) before forwarding:

| Prefix | Service |
|---|---|
| `/api/users/**` | user-service |
| `/api/companies/**` | company-service |
| `/api/jobs/**`, `/api/job-categories/**`, `/api/job-skills/**`, `/api/job-tags/**` | job-service |
| `/api/applications/**` | application-service |
| `/api/resumes/**` | resume-service |
| `/api/preferences/**` | preferences |
| `/api/ai/**` | AI service |

Access tokens are issued by user-service, expire after 24 hours, and include the user's role in `authorities`. Self-registration as `ROLE_ADMIN` is rejected. Passwords must be at least 8 characters. Suspended and deleted accounts cannot log in, reset a password, or change a password.

Public password recovery (no JWT, no SMTP): `POST /auth/forgot-password` `{ "email" }` always returns a generic message. Unknown, suspended, and deleted emails do not issue a token. Active accounts store a SHA-256 hash of a one-hour, single-use opaque token. When `app.password-reset.expose-token` is true (default for this local project; env `PASSWORD_RESET_EXPOSE_TOKEN`), the raw token is in `resetToken`; otherwise it is omitted. `POST /auth/reset-password` `{ "token", "newPassword" }` encodes the new password and clears the token. Invalid token: `INVALID_RESET_TOKEN`. Expired: `RESET_TOKEN_EXPIRED`. Authenticated `POST /api/users/change-password` `{ "currentPassword", "newPassword" }` checks the current password (`INVALID_CREDENTIALS` if wrong).

## Admin and taxonomy (gateway role checks)

These paths are matched before the prefix routes. Direct service ports do not apply these checks.

Admin only (`ROLE_ADMIN`):

| Method | Path |
|---|---|
| GET | `/api/users` |
| PATCH | `/api/users/{userId}/suspend` |
| PATCH | `/api/users/{userId}/activate` |
| DELETE | `/api/users/{userId}/delete` |
| PATCH | `/api/companies/{id}/verify` |
| PATCH | `/api/companies/{id}/deactivate` |
| GET | `/api/jobs/admin` |

Employer (`ROLE_EMPLOYER`):

| Method | Path |
|---|---|
| GET | `/api/jobs/my` |

Taxonomy writes (`ROLE_ADMIN` or `ROLE_EMPLOYER`). Taxonomy **GET** list/detail is the optional-JWT public table above:

| Method | Path |
|---|---|
| POST | `/api/job-categories` |
| PUT, DELETE | `/api/job-categories/{id}` |
| POST | `/api/job-skills` |
| PUT, DELETE | `/api/job-skills/{id}` |
| POST | `/api/job-tags` |
| PUT, DELETE | `/api/job-tags/{id}` |

## Main resources

### User

- `GET` / `PUT /api/users/profile`
- `POST /api/users/change-password` (JWT; identity from `X-User-Email`)
- `GET /api/users/{userId}` (any authenticated caller; application-service uses this over Feign)
- Admin list, suspend, activate, and soft-delete as in the table above

### Company

- `POST /api/companies`
- `GET /api/companies/{id}`
- `GET /api/companies/my`
- `GET /api/companies` — optional `companyType`, `industryType`, `companyStatus`
- `PUT /api/companies/{id}` — owner
- `PATCH /api/companies/{id}` — owner delete (not `DELETE`)
- Admin verify and deactivate as in the table above

### Job

- `POST /api/jobs`
- `GET /api/jobs/{id}` — anonymous OK; drafts return `NOT_FOUND` unless the employer or an admin (optional JWT)
- `GET /api/jobs` — anonymous OK; optional `keyword`, `companyId`, `categoryId` (category entity id), `location` (city/state/country contains), `minSalary`, `maxSalary`, `jobType`, `workMode`, `experienceLevel`, `status` (defaults to `OPEN`), `minOpenings`, `maxOpenings` (column `opening`). `skillIds` and `tagIds` are on the request type but are not applied
- `GET /api/jobs/my` — employer JWT; every status for that employer id, newest first (no company-id parameter)
- `GET /api/jobs/company/{companyId}` — open jobs only; JWT required
- `POST /api/jobs/search/natural` with `{ "query": "..." }` — AI mapping fail-opens to a keyword search
- `GET /api/jobs/admin`
- `PUT /api/jobs/{id}`, `PATCH /api/jobs/{id}/publish`, `PATCH /api/jobs/{id}/close`, `DELETE /api/jobs/{id}` — posting employer
- Categories, skills, and tags under `/api/job-categories`, `/api/job-skills`, `/api/job-tags`

### Resume

Structured JSON records. PDF or DOC upload is not implemented.

- `POST /api/resumes`
- `GET /api/resumes/{resumeId}`, `GET /api/resumes/my`
- `PUT /api/resumes/{resumeId}` — `{ "title" }` (non-blank, max 150); owner only
- `PUT /api/resumes/{resumeId}/personal-info`, `/summary`, `/default`
- `DELETE /api/resumes/{resumeId}`
- Nested owner-scoped resources: `/educations`, `/work-experiences`, `/projects`, `/skills`, `/languages`, `/awards`, `/certifications`. Award and certification deletes return `204`.

### Application

- `POST /api/applications`
- `GET /api/applications/{applicationId}` — candidate or employer on that row
- `GET /api/applications/my`
- `GET /api/applications/company` — employer; optional `jobId`, `status`, `isStarred`, `aiShortListStatus`, `minAiScore`; `sortBy` `AI_SCORE_DESC` / `AI_SCORE_ASC` (otherwise `appliedAt` descending)
- `GET /api/applications/job/{jobId}` — that job's employer
- `PATCH /api/applications/{applicationId}/status`, `/star` — employer
- `PATCH /api/applications/{applicationId}/withdraw`, `DELETE /api/applications/{applicationId}` — candidate
- Notes: `/api/applications/{applicationId}/notes` — employer
- `POST /api/applications/{applicationId}/cover-letter`
- `GET /api/applications/{applicationId}/skills-gap`

On apply, when Gemini screening succeeds, `aiScore` is stored and `aiShortListStatus` is `AUTO_SHORTLISTED` (`>=80`), `REVIEW_RECOMMENDED` (`>=50`), or `LOW_MATCH`. Gemini failure leaves `aiScore` null and `NOT_SCREENED`; the application is still created. Cover-letter generation is fail-open: Gemini failure returns the stored application without updating the letter. Skills-gap does not fail open; AI errors propagate (`AI_UNAVAILABLE` when Gemini is down).

### Preferences

Under `/api/preferences/saved-jobs`: `POST`, `GET`, `GET /check?jobId=`, `DELETE /{savedJobId}`.

### AI

Authenticated `/api/ai/**`. Application-service and job-service assemble context and call these over Feign; the same payloads work if the body is sent directly.

- `POST /api/ai/application/cover-letter`, `/screening-core`, `/skills-gap`
- Job: `POST /api/ai/job/describe`, `/salary-suggestion`; `GET /api/ai/job/requirements`, `/skills-recommendation`, `/responsibilities`, `/benefits`, `/tags-recommendation`
- Resume: `POST /api/ai/resume/summary`, `/experience-bullets`, `/improvements`, `/career-feedback`
- `POST /api/ai/search/enhance`

Optional user instructions are accepted as `additionalContext` by cover-letter,
job-description, resume-summary, resume-improvement, and career-feedback
requests. Work-experience bullets use `achievementsHint` for the same purpose.
Structured candidate/job fields remain the primary context; instructions must
not be treated as permission to invent experience or qualifications.

`GET /api/ai/{prompt}` and `/api/ai/alert-suggestion` are not present.

## Errors

Service failures use a body (successes are not wrapped):

```json
{ "code": "NOT_FOUND", "message": "Job not found with ID: 12" }
```

HTTP status follows the exception type (`404`, `403`, `409`, `400`, `401`, `503`). Unexpected failures log the cause and return `INTERNAL_ERROR` with `"An unexpected error occurred"`.

Stable codes: `NOT_FOUND`, `FORBIDDEN`, `CONFLICT`, `BAD_REQUEST`, `UNAUTHORIZED`, `VALIDATION_FAILED`, `INTERNAL_ERROR`, `AI_UNAVAILABLE`, `ALREADY_APPLIED`, `JOB_NOT_OPEN`, `EMAIL_REGISTERED`, `ADMIN_SELF_SIGNUP`, `ACCOUNT_DISABLED`, `INVALID_CREDENTIALS`, `INVALID_RESET_TOKEN`, `RESET_TOKEN_EXPIRED`.

Gateway missing/invalid JWT and role denials use Spring `ResponseStatusException` (typically `401` / `403`). They are not this `{code,message}` body.

Direct service ports still trust identity headers and skip gateway role checks.

## Current API constraints

- No `/v1` version prefix
- No pagination on list endpoints
- No generated OpenAPI specification
- Resume AI is `POST /api/ai/resume/...`
