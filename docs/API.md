# API notes

The API is exposed through the gateway. There is no generated OpenAPI document in the current repository.

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

Signup accepts `ROLE_JOB_SEEKER` or `ROLE_EMPLOYER`. Self-registration as `ROLE_ADMIN` is rejected.

## Main resources

### User

- `GET /api/users/profile`
- `PUT /api/users/profile`
- `GET /api/users/{userId}`
- `GET /api/users`
- suspend, activate, and soft-delete endpoints

### Company

- `POST /api/companies`
- `GET /api/companies/{id}`
- `GET /api/companies/my`
- `GET /api/companies`
- update, verify, deactivate, and delete endpoints

### Job

- `POST /api/jobs`
- `GET /api/jobs/{id}`
- `GET /api/jobs` with query filters
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

An application can be submitted once per candidate/job pair.

### Preferences

- save, list, check, and remove saved jobs under `/api/preferences/saved-jobs`

### AI

The AI service exposes:

- application cover-letter, screening-score, and skills-gap prompts
- job description, requirements, salary, skills, responsibilities, benefits, and tag assistance
- resume summary, experience bullet, improvement, and career feedback assistance
- search enhancement

These endpoints currently accept domain context in their request body. Automatic screening during application creation is not implemented.

## Current API constraints

- No `/v1` version prefix
- No consistent global error contract
- No pagination on list endpoints
- No generated OpenAPI specification
- Authorization is not complete on administrative and record-by-ID endpoints
- Resume AI uses request bodies on GET endpoints; this will be corrected in a later hardening change
