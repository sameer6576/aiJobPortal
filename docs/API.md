# API notes

The API is exposed through the gateway. There is no generated OpenAPI document. A clone walkthrough is in [demo.http](http/demo.http).

## Authentication

Public endpoints:

| Method | Path |
|---|---|
| POST | `/auth/signup` |
| POST | `/auth/login` |

Configured `/api/**` gateway routes require:

```http
Authorization: Bearer <access-token>
```

Signup JWT includes the user's role in `authorities`. Self-registration as `ROLE_ADMIN` is rejected.

Admin-only through the gateway (`ROLE_ADMIN`):

| Method | Path |
|---|---|
| GET | `/api/users` |
| PATCH | `/api/users/{userId}/suspend` |
| PATCH | `/api/users/{userId}/activate` |
| DELETE | `/api/users/{userId}/delete` |
| PATCH | `/api/companies/{id}/verify` |
| PATCH | `/api/companies/{id}/deactivate` |
| GET | `/api/jobs/admin` |

Employer or admin through the gateway:

| Method | Path |
|---|---|
| POST/PUT/DELETE | `/api/job-categories` |
| POST/PUT/DELETE | `/api/job-skills` |
| POST/PUT/DELETE | `/api/job-tags` |

Passwords must be at least 8 characters. Suspended and deleted accounts cannot log in. Access tokens expire after 24 hours.

## Main resources

### User

- `GET /api/users/profile`
- `PUT /api/users/profile`
- `GET /api/users/{userId}`
- `GET /api/users`
- suspend, activate, and soft-delete endpoints (admin through the gateway)

### Company

- `POST /api/companies`
- `GET /api/companies/{id}`
- `GET /api/companies/my`
- `GET /api/companies`
- update, verify, deactivate, and delete endpoints

### Job

- `POST /api/jobs`
- `GET /api/jobs/{id}` (HTTP 200; drafts are hidden unless the employer or an admin)
- `GET /api/jobs` with query filters (`categoryId` matches the category, openings use the `opening` column)
- `GET /api/jobs/company/{companyId}` (open jobs only)
- `POST /api/jobs/search/natural` with `{ "query": "..." }`
- `GET /api/jobs/admin` (admin through the gateway)
- `PATCH /api/jobs/{id}/publish`
- `PATCH /api/jobs/{id}/close`
- categories, skills, and tags under their own resources

### Resume

- `POST /api/resumes`
- `GET /api/resumes/{resumeId}`
- `GET /api/resumes/my`
- personal information, summary, default selection, and deletion
- nested education, work experience, project, skill, and language resources

Resumes are structured records; PDF or DOC upload is not implemented.

### Application

- `POST /api/applications`
- `GET /api/applications/{applicationId}`
- `GET /api/applications/my`
- `GET /api/applications/company`
- status, withdrawal, starring, deletion, and employer notes
- `POST /api/applications/{applicationId}/cover-letter`
- `GET /api/applications/{applicationId}/skills-gap`

`POST /api/applications` stores `aiScore` and `aiShortListStatus` when Gemini screening succeeds (`>=80` AUTO_SHORTLISTED, `>=50` REVIEW_RECOMMENDED, otherwise LOW_MATCH). Gemini failure leaves `NOT_SCREENED` and does not reject the apply. Cover-letter generation is also fail-open.

`GET /api/applications/{applicationId}` is limited to the candidate or employer on that row. `GET /api/applications/job/{jobId}` is limited to the job's employer.

### Preferences

- save, list, check, and remove saved jobs under `/api/preferences/saved-jobs`

### AI

The AI service exposes:

- application cover-letter, screening-score, and skills-gap prompts
- job description, requirements, salary, skills, responsibilities, benefits, and tag assistance
- resume summary, experience bullet, improvement, and career feedback assistance
- search enhancement

Application-service and job-service assemble job/resume context and call these over Feign. Direct `/api/ai/**` calls still accept the same payloads in the request body. `GET /api/ai/{prompt}` and `/api/ai/alert-suggestion` are removed.

## Errors

Failed requests return a JSON body (not a wrapper around successes):

```json
{ "code": "NOT_FOUND", "message": "Job not found with ID: 12" }
```

HTTP status comes from the exception type (`404`, `403`, `409`, `400`, `401`, `503`). Unexpected failures log the cause and return `INTERNAL_ERROR` with `"An unexpected error occurred"`.

Stable codes: `NOT_FOUND`, `FORBIDDEN`, `CONFLICT`, `BAD_REQUEST`, `UNAUTHORIZED`, `VALIDATION_FAILED`, `INTERNAL_ERROR`, `AI_UNAVAILABLE`, `ALREADY_APPLIED`, `JOB_NOT_OPEN`, `EMAIL_REGISTERED`, `ADMIN_SELF_SIGNUP`, `ACCOUNT_DISABLED`, `INVALID_CREDENTIALS`.

## Current API constraints

- No `/v1` version prefix
- No pagination on list endpoints
- No generated OpenAPI specification
- Direct service ports still trust identity headers
- Resume AI is `POST /api/ai/resume/...`
