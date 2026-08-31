/**
 * Generates docs/http/JobMate.postman_collection.json from gateway routes + request DTOs.
 * Run: node docs/http/gen_postman.mjs
 */
import { writeFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));

const seekerAuth = [
  { key: "Authorization", value: "Bearer {{seekerToken}}", type: "text" },
];
const employerAuth = [
  { key: "Authorization", value: "Bearer {{employerToken}}", type: "text" },
];
const adminAuth = [
  { key: "Authorization", value: "Bearer {{adminToken}}", type: "text" },
];

function jsonBody(obj) {
  return {
    mode: "raw",
    raw: JSON.stringify(obj, null, 2),
    options: { raw: { language: "json" } },
  };
}

function captureId(tokenVar) {
  return {
    listen: "test",
    script: {
      type: "text/javascript",
      exec: [
        "const json = pm.response.json();",
        "const id = json.id ?? json.userId;",
        `if (id != null) pm.collectionVariables.set("${tokenVar}", String(id));`,
      ],
    },
  };
}

function req(name, method, path, { auth, body, query, description, event } = {}) {
  const item = {
    name,
    request: {
      method,
      header: [
        ...(auth ?? []),
        ...(body ? [{ key: "Content-Type", value: "application/json" }] : []),
      ],
      url: {
        raw: `{{baseUrl}}${path}`,
        host: ["{{baseUrl}}"],
        path: path.replace(/^\//, "").split("/"),
      },
    },
  };
  if (description) item.request.description = description;
  if (query?.length) {
    item.request.url.query = query;
    const qs = query
      .filter((q) => q.disabled !== true)
      .map((q) => `${q.key}=${q.value}`)
      .join("&");
    if (qs) item.request.url.raw += `?${qs}`;
  }
  if (body !== undefined) {
    item.request.body =
      typeof body === "string"
        ? {
            mode: "raw",
            raw: body,
            options: { raw: { language: "json" } },
          }
        : jsonBody(body);
  }
  if (event) item.event = Array.isArray(event) ? event : [event];
  return item;
}

function loginTests(tokenVar) {
  return {
    listen: "test",
    script: {
      type: "text/javascript",
      exec: [
        "const json = pm.response.json();",
        `if (json.jwt) pm.collectionVariables.set("${tokenVar}", json.jwt);`,
        "if (json.userId) pm.collectionVariables.set(\"userId\", String(json.userId));",
      ],
    },
  };
}

const signupSeeker = {
  fullName: "Sam Seeker",
  email: "sam.seeker@example.com",
  password: "SeekPass1",
  phone: "9000000001",
  role: "ROLE_JOB_SEEKER",
};

const signupEmployer = {
  fullName: "Erin Employer",
  email: "erin.employer@example.com",
  password: "EmpPass1",
  phone: "9000000002",
  role: "ROLE_EMPLOYER",
};

const companyBody = {
  name: "Acme Labs",
  tagline: "Hiring engineers who ship",
  description: "Product studio building marketplace software.",
  logoUrl: "https://cdn.example.com/acme-logo.png",
  coverImageUrl: "https://cdn.example.com/acme-cover.png",
  website: "https://acme.example",
  email: "jobs@acme.example",
  phone: "08040000000",
  foundedYear: 2018,
  companySize: "SMALL",
  companyType: "PRIVATE",
  industryType: "TECHNOLOGY",
  registrationNumber: "U72900KA2018PTC000001",
  socialLinks: [
    { platform: "LINKEDIN", url: "https://linkedin.com/company/acme-labs" },
    { platform: "GITHUB", url: "https://github.com/acme-labs" },
  ],
};

const jobBody = {
  title: "Java backend engineer",
  description: "Build job application APIs and screening pipelines.",
  requirements: "Java 21, Spring Boot, PostgreSQL",
  responsibilities: "Own application-service APIs and Feign clients.",
  benefits: "Hybrid work, learning budget",
  categoryId: "{{categoryId}}",
  skillIds: ["{{jobSkillId}}"],
  tagIds: ["{{jobTagId}}"],
  address: "Koramangala",
  city: "Bengaluru",
  state: "Karnataka",
  country: "India",
  zipCode: "560034",
  minSalary: 1800000,
  maxSalary: 2800000,
  jobType: "FULL_TIME",
  workMode: "HYBRID",
  experienceLevel: "MID_LEVEL",
  openings: 2,
  applicationDeadline: "2026-12-31",
  expiresAt: "2027-01-31",
};

const collection = {
  info: {
    name: "JobMate",
    description:
      "Gateway API. Payloads include every DTO field.\n\n1. baseUrl: http://localhost:5007 (IntelliJ) or http://localhost:5050 (Compose).\n2. Run Auth signups then logins — Tests store JWTs.\n3. adminToken only works for an existing ROLE_ADMIN; signup cannot create admins.\n4. Do not send X-User-Id; the gateway sets identity from the JWT.\n5. After create company/job/resume, copy ids into collection variables (or edit the Tests tab).",
    schema: "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
  },
  variable: [
    { key: "baseUrl", value: "http://localhost:5007" },
    { key: "seekerToken", value: "" },
    { key: "employerToken", value: "" },
    { key: "adminToken", value: "" },
    { key: "userId", value: "1" },
    { key: "companyId", value: "1" },
    { key: "categoryId", value: "1" },
    { key: "jobSkillId", value: "1" },
    { key: "jobTagId", value: "1" },
    { key: "jobId", value: "1" },
    { key: "resumeId", value: "1" },
    { key: "educationId", value: "1" },
    { key: "workExperienceId", value: "1" },
    { key: "projectId", value: "1" },
    { key: "resumeSkillId", value: "1" },
    { key: "languageId", value: "1" },
    { key: "applicationId", value: "1" },
    { key: "noteId", value: "1" },
    { key: "savedJobId", value: "1" },
  ],
  item: [
    {
      name: "Auth",
      item: [
        req("Signup job seeker", "POST", "/auth/signup", {
          body: signupSeeker,
          description: "SignupRequest: fullName, email, password (min 8), phone, role",
        }),
        req("Signup employer", "POST", "/auth/signup", {
          body: signupEmployer,
        }),
        {
          ...req("Login job seeker", "POST", "/auth/login", {
            body: { email: signupSeeker.email, password: signupSeeker.password },
            description: "LoginRequest: email, password. Tests save seekerToken.",
          }),
          event: [loginTests("seekerToken")],
        },
        {
          ...req("Login employer", "POST", "/auth/login", {
            body: { email: signupEmployer.email, password: signupEmployer.password },
          }),
          event: [loginTests("employerToken")],
        },
        {
          ...req("Login admin", "POST", "/auth/login", {
            body: { email: "admin@example.com", password: "AdminPass1" },
            description: "Use an existing ROLE_ADMIN. Signup cannot create admins.",
          }),
          event: [loginTests("adminToken")],
        },
      ],
    },
    {
      name: "Users",
      item: [
        req("Get my profile", "GET", "/api/users/profile", { auth: seekerAuth }),
        req("Update my profile", "PUT", "/api/users/profile", {
          auth: seekerAuth,
          body: {
            fullName: "Sam Seeker",
            phone: "9000000099",
            profileImage: "https://cdn.example.com/avatars/sam.png",
          },
          description: "UpdateUserRequest: fullName, phone, profileImage",
        }),
        req("Get user by id", "GET", "/api/users/{{userId}}", { auth: employerAuth }),
        req("List users (admin)", "GET", "/api/users", { auth: adminAuth }),
        req("Suspend user (admin)", "PATCH", "/api/users/{{userId}}/suspend", { auth: adminAuth }),
        req("Activate user (admin)", "PATCH", "/api/users/{{userId}}/activate", { auth: adminAuth }),
        req("Delete user (admin)", "DELETE", "/api/users/{{userId}}/delete", { auth: adminAuth }),
      ],
    },
    {
      name: "Companies",
      item: [
        req("Create company", "POST", "/api/companies", {
          auth: employerAuth,
          body: companyBody,
          event: captureId("companyId"),
          description:
            "CompanyRequest: name, tagline, description, logoUrl, coverImageUrl, website, email, phone, foundedYear, companySize, companyType, industryType, registrationNumber, socialLinks[{platform,url}]",
        }),
        req("Get company by id", "GET", "/api/companies/{{companyId}}", { auth: seekerAuth }),
        req("Get my company", "GET", "/api/companies/my", { auth: employerAuth }),
        req("List companies", "GET", "/api/companies", {
          auth: seekerAuth,
          query: [
            { key: "companyType", value: "PRIVATE" },
            { key: "industryType", value: "TECHNOLOGY" },
            { key: "companyStatus", value: "ACTIVE", disabled: true },
          ],
        }),
        req("Update company", "PUT", "/api/companies/{{companyId}}", {
          auth: employerAuth,
          body: companyBody,
        }),
        req("Verify company (admin)", "PATCH", "/api/companies/{{companyId}}/verify", { auth: adminAuth }),
        req("Deactivate company (admin)", "PATCH", "/api/companies/{{companyId}}/deactivate", { auth: adminAuth }),
        req("Delete company", "PATCH", "/api/companies/{{companyId}}", { auth: employerAuth }),
      ],
    },
    {
      name: "Job taxonomy",
      item: [
        req("Create category", "POST", "/api/job-categories", {
          auth: employerAuth,
          body: {
            name: "Engineering",
            description: "Software and infrastructure roles",
            iconUrl: "https://cdn.example.com/icons/eng.svg",
            parentId: null,
          },
          event: captureId("categoryId"),
          description: "JobCategoryRequest: name, description, iconUrl, parentId",
        }),
        req("List categories", "GET", "/api/job-categories", { auth: seekerAuth }),
        req("Get category", "GET", "/api/job-categories/{{categoryId}}", { auth: seekerAuth }),
        req("Update category", "PUT", "/api/job-categories/{{categoryId}}", {
          auth: employerAuth,
          body: {
            name: "Engineering",
            description: "Updated description",
            iconUrl: "https://cdn.example.com/icons/eng.svg",
            parentId: null,
          },
        }),
        req("Delete category", "DELETE", "/api/job-categories/{{categoryId}}", { auth: employerAuth }),
        req("Create job skill", "POST", "/api/job-skills", {
          auth: employerAuth,
          body: { name: "Java", category: "PROGRAMMING_LANGUAGE" },
          event: captureId("jobSkillId"),
          description: "JobSkillRequest: name, category (SkillCategory enum)",
        }),
        req("List job skills", "GET", "/api/job-skills", { auth: seekerAuth }),
        req("Get job skill", "GET", "/api/job-skills/{{jobSkillId}}", { auth: seekerAuth }),
        req("Update job skill", "PUT", "/api/job-skills/{{jobSkillId}}", {
          auth: employerAuth,
          body: { name: "Java", category: "PROGRAMMING_LANGUAGE" },
        }),
        req("Delete job skill", "DELETE", "/api/job-skills/{{jobSkillId}}", { auth: employerAuth }),
        req("Create job tag", "POST", "/api/job-tags", {
          auth: employerAuth,
          body: { name: "Remote-friendly" },
          event: captureId("jobTagId"),
        }),
        req("List job tags", "GET", "/api/job-tags", { auth: seekerAuth }),
        req("Get job tag", "GET", "/api/job-tags/{{jobTagId}}", { auth: seekerAuth }),
        req("Update job tag", "PUT", "/api/job-tags/{{jobTagId}}", {
          auth: employerAuth,
          body: { name: "Remote-friendly" },
        }),
        req("Delete job tag", "DELETE", "/api/job-tags/{{jobTagId}}", { auth: employerAuth }),
      ],
    },
    {
      name: "Jobs",
      item: [
        req("Create job", "POST", "/api/jobs", {
          auth: employerAuth,
          body: jobBody,
          event: captureId("jobId"),
          description:
            "JobRequest: title, description, requirements, responsibilities, benefits, categoryId, skillIds, tagIds, address, city, state, country, zipCode, minSalary, maxSalary, jobType, workMode, experienceLevel, openings, applicationDeadline, expiresAt",
        }),
        req("Get job", "GET", "/api/jobs/{{jobId}}", { auth: seekerAuth }),
        req("List / search jobs", "GET", "/api/jobs", {
          auth: seekerAuth,
          query: [
            { key: "keyword", value: "java" },
            { key: "id", value: "", disabled: true },
            { key: "skillIds", value: "{{jobSkillId}}" },
            { key: "tagIds", value: "{{jobTagId}}" },
            { key: "companyId", value: "{{companyId}}" },
            { key: "categoryId", value: "{{categoryId}}" },
            { key: "location", value: "Bengaluru" },
            { key: "minSalary", value: "1000000" },
            { key: "maxSalary", value: "4000000" },
            { key: "jobType", value: "FULL_TIME" },
            { key: "workMode", value: "HYBRID" },
            { key: "experienceLevel", value: "MID_LEVEL" },
            { key: "status", value: "OPEN" },
            { key: "minOpenings", value: "1" },
            { key: "maxOpenings", value: "10" },
          ],
          description: "JobSearchRequest as query params (GET @ModelAttribute)",
        }),
        req("Natural language search", "POST", "/api/jobs/search/natural", {
          auth: seekerAuth,
          body: { query: "remote java jobs in bangalore" },
        }),
        req("Jobs by company", "GET", "/api/jobs/company/{{companyId}}", { auth: seekerAuth }),
        req("List all jobs (admin)", "GET", "/api/jobs/admin", { auth: adminAuth }),
        req("Update job", "PUT", "/api/jobs/{{jobId}}", {
          auth: employerAuth,
          body: jobBody,
        }),
        req("Publish job", "PATCH", "/api/jobs/{{jobId}}/publish", { auth: employerAuth }),
        req("Close job", "PATCH", "/api/jobs/{{jobId}}/close", { auth: employerAuth }),
        req("Delete job", "DELETE", "/api/jobs/{{jobId}}", { auth: employerAuth }),
      ],
    },
    {
      name: "Resumes",
      item: [
        req("Create resume", "POST", "/api/resumes", {
          auth: seekerAuth,
          body: {
            title: "Sam backend resume",
            template: "PROFESSIONAL",
            visibility: "PRIVATE",
            isDefault: true,
          },
          event: captureId("resumeId"),
          description: "CreateResumeRequest: title, template, visibility, isDefault",
        }),
        req("Get resume", "GET", "/api/resumes/{{resumeId}}", { auth: seekerAuth }),
        req("My resumes", "GET", "/api/resumes/my", { auth: seekerAuth }),
        req("Update personal info", "PUT", "/api/resumes/{{resumeId}}/personal-info", {
          auth: seekerAuth,
          body: {
            firstName: "Sam",
            lastName: "Seeker",
            headline: "Java backend engineer",
            email: "sam.seeker@example.com",
            phone: "9000000001",
            city: "Bengaluru",
            country: "India",
            linkedinUrl: "https://linkedin.com/in/sam-seeker",
            githubUrl: "https://github.com/sam-seeker",
            portfolioUrl: "https://sam.example",
            websiteUrl: "https://sam.example",
          },
          description: "PersonalInfoResponse used as request body",
        }),
        req("Update summary", "PUT", "/api/resumes/{{resumeId}}/summary", {
          auth: seekerAuth,
          body: JSON.stringify(
            "Backend engineer with 4 years building Spring Boot APIs, PostgreSQL, and Kafka consumers."
          ),
          description: "Raw JSON string body (@RequestBody String summary)",
        }),
        req("Set default resume", "PUT", "/api/resumes/{{resumeId}}/default", { auth: seekerAuth }),
        req("Delete resume", "DELETE", "/api/resumes/{{resumeId}}", { auth: seekerAuth }),
        req("Add education", "POST", "/api/resumes/{{resumeId}}/educations", {
          auth: seekerAuth,
          event: captureId("educationId"),
          body: {
            institutionName: "NITK Surathkal",
            degree: "B.Tech",
            fieldOfStudy: "Computer Science",
            grade: "8.2 CGPA",
            startDate: "2016-08-01",
            endDate: "2020-05-31",
            isCurrentlyStudying: false,
            description: "Coursework in distributed systems.",
            displayOrder: 1,
          },
        }),
        req("List educations", "GET", "/api/resumes/{{resumeId}}/educations", { auth: seekerAuth }),
        req("Update education", "PUT", "/api/resumes/{{resumeId}}/educations/{{educationId}}", {
          auth: seekerAuth,
          body: {
            institutionName: "NITK Surathkal",
            degree: "B.Tech",
            fieldOfStudy: "Computer Science",
            grade: "8.4 CGPA",
            startDate: "2016-08-01",
            endDate: "2020-05-31",
            isCurrentlyStudying: false,
            description: "Updated notes.",
            displayOrder: 1,
          },
        }),
        req("Delete education", "DELETE", "/api/resumes/{{resumeId}}/educations/{{educationId}}", {
          auth: seekerAuth,
        }),
        req("Add work experience", "POST", "/api/resumes/{{resumeId}}/work-experiences", {
          auth: seekerAuth,
          event: captureId("workExperienceId"),
          body: {
            companyName: "Acme Labs",
            companyLogoUrl: "https://cdn.example.com/acme-logo.png",
            jobTitle: "Software engineer",
            employmentType: "FULL_TIME",
            location: "Bengaluru",
            startDate: "2021-06-01",
            endDate: null,
            isCurrentJob: true,
            technologies: ["Java", "Spring Boot", "PostgreSQL"],
            displayOrder: 1,
            description: "Built REST APIs for hiring workflows.",
          },
        }),
        req("List work experiences", "GET", "/api/resumes/{{resumeId}}/work-experiences", { auth: seekerAuth }),
        req("Update work experience", "PUT", "/api/resumes/{{resumeId}}/work-experiences/{{workExperienceId}}", {
          auth: seekerAuth,
          body: {
            companyName: "Acme Labs",
            companyLogoUrl: "https://cdn.example.com/acme-logo.png",
            jobTitle: "Senior software engineer",
            employmentType: "FULL_TIME",
            location: "Bengaluru",
            startDate: "2021-06-01",
            endDate: null,
            isCurrentJob: true,
            technologies: ["Java", "Spring Boot", "Kafka"],
            displayOrder: 1,
            description: "Owned application-service.",
          },
        }),
        req("Delete work experience", "DELETE", "/api/resumes/{{resumeId}}/work-experiences/{{workExperienceId}}", {
          auth: seekerAuth,
        }),
        req("Add project", "POST", "/api/resumes/{{resumeId}}/projects", {
          auth: seekerAuth,
          event: captureId("projectId"),
          body: {
            title: "JobMate",
            description: "Spring Cloud job portal",
            technologies: ["Java", "Spring Cloud", "PostgreSQL"],
            projectUrl: "https://jobmate.example",
            sourceCodeUrl: "https://github.com/example/jobmate",
            startDate: "2025-01-01",
            endDate: null,
            isOngoing: true,
            displayOrder: 1,
          },
        }),
        req("List projects", "GET", "/api/resumes/{{resumeId}}/projects", { auth: seekerAuth }),
        req("Update project", "PUT", "/api/resumes/{{resumeId}}/projects/{{projectId}}", {
          auth: seekerAuth,
          body: {
            title: "JobMate",
            description: "Updated description",
            technologies: ["Java", "Spring Cloud"],
            projectUrl: "https://jobmate.example",
            sourceCodeUrl: "https://github.com/example/jobmate",
            startDate: "2025-01-01",
            endDate: null,
            isOngoing: true,
            displayOrder: 1,
          },
        }),
        req("Delete project", "DELETE", "/api/resumes/{{resumeId}}/projects/{{projectId}}", { auth: seekerAuth }),
        req("Add resume skill", "POST", "/api/resumes/{{resumeId}}/skills", {
          auth: seekerAuth,
          event: captureId("resumeSkillId"),
          body: {
            skillName: "Java",
            proficiencyLevel: "ADVANCED",
            yearsOfExperience: 4,
            displayOrder: 1,
          },
        }),
        req("List resume skills", "GET", "/api/resumes/{{resumeId}}/skills", { auth: seekerAuth }),
        req("Update resume skill", "PUT", "/api/resumes/{{resumeId}}/skills/{{resumeSkillId}}", {
          auth: seekerAuth,
          body: {
            skillName: "Java",
            proficiencyLevel: "EXPERT",
            yearsOfExperience: 5,
            displayOrder: 1,
          },
        }),
        req("Delete resume skill", "DELETE", "/api/resumes/{{resumeId}}/skills/{{resumeSkillId}}", {
          auth: seekerAuth,
        }),
        req("Add language", "POST", "/api/resumes/{{resumeId}}/languages", {
          auth: seekerAuth,
          event: captureId("languageId"),
          body: {
            languageName: "English",
            proficiency: "PROFESSIONAL",
            displayOrder: 1,
          },
        }),
        req("List languages", "GET", "/api/resumes/{{resumeId}}/languages", { auth: seekerAuth }),
        req("Update language", "PUT", "/api/resumes/{{resumeId}}/languages/{{languageId}}", {
          auth: seekerAuth,
          body: {
            languageName: "English",
            proficiency: "FLUENT",
            displayOrder: 1,
          },
        }),
        req("Delete language", "DELETE", "/api/resumes/{{resumeId}}/languages/{{languageId}}", {
          auth: seekerAuth,
        }),
      ],
    },
    {
      name: "Applications",
      item: [
        req("Apply", "POST", "/api/applications", {
          auth: seekerAuth,
          event: captureId("applicationId"),
          body: {
            jobId: "{{jobId}}",
            resumeId: "{{resumeId}}",
            coverLetter: "I am excited to apply for this Java backend role.",
            expectedSalary: 2200000,
            availableFrom: "2026-10-01",
          },
          description: "CreateApplicationRequest: jobId, resumeId, coverLetter, expectedSalary, availableFrom",
        }),
        req("Get application", "GET", "/api/applications/{{applicationId}}", { auth: seekerAuth }),
        req("My applications", "GET", "/api/applications/my", { auth: seekerAuth }),
        req("Company applications", "GET", "/api/applications/company", {
          auth: employerAuth,
          query: [
            { key: "jobId", value: "{{jobId}}" },
            { key: "status", value: "PENDING" },
            { key: "isStarred", value: "false" },
            { key: "aiShortListStatus", value: "NOT_SCREENED" },
            { key: "minAiScore", value: "50" },
            { key: "sortBy", value: "aiScore" },
          ],
          description: "CompanyApplicationFilterRequest as query params",
        }),
        req("Applications for job", "GET", "/api/applications/job/{{jobId}}", { auth: employerAuth }),
        req("Update status", "PATCH", "/api/applications/{{applicationId}}/status", {
          auth: employerAuth,
          body: { status: "REVIEWING" },
        }),
        req("Withdraw", "PATCH", "/api/applications/{{applicationId}}/withdraw", {
          auth: seekerAuth,
          body: { name: "Sam Seeker", reason: "Accepted another offer" },
        }),
        req("Toggle star", "PATCH", "/api/applications/{{applicationId}}/star", { auth: employerAuth }),
        req("Generate cover letter (application)", "POST", "/api/applications/{{applicationId}}/cover-letter", {
          auth: seekerAuth,
          description: "No body; server loads job + resume.",
        }),
        req("Skills gap (application)", "GET", "/api/applications/{{applicationId}}/skills-gap", {
          auth: seekerAuth,
        }),
        req("Add note", "POST", "/api/applications/{{applicationId}}/notes", {
          auth: employerAuth,
          event: captureId("noteId"),
          body: { content: "Strong Spring Boot background. Schedule a screen." },
        }),
        req("List notes", "GET", "/api/applications/{{applicationId}}/notes", { auth: employerAuth }),
        req("Delete note", "DELETE", "/api/applications/{{applicationId}}/notes/{{noteId}}", {
          auth: employerAuth,
        }),
        req("Delete application", "DELETE", "/api/applications/{{applicationId}}", { auth: seekerAuth }),
      ],
    },
    {
      name: "Preferences",
      item: [
        req("Save job", "POST", "/api/preferences/saved-jobs", {
          auth: seekerAuth,
          event: captureId("savedJobId"),
          body: { jobId: "{{jobId}}" },
        }),
        req("My saved jobs", "GET", "/api/preferences/saved-jobs", { auth: seekerAuth }),
        req("Is job saved", "GET", "/api/preferences/saved-jobs/check", {
          auth: seekerAuth,
          query: [{ key: "jobId", value: "{{jobId}}" }],
        }),
        req("Unsave job", "DELETE", "/api/preferences/saved-jobs/{{savedJobId}}", { auth: seekerAuth }),
      ],
    },
    {
      name: "AI",
      item: [
        req("Job describe", "POST", "/api/ai/job/describe", {
          auth: employerAuth,
          body: {
            jobTitle: "Java backend engineer",
            skills: ["Java", "Spring Boot", "PostgreSQL"],
            experienceLevel: "MID_LEVEL",
            jobType: "FULL_TIME",
            workMode: "HYBRID",
            category: "Engineering",
            additionalContext: "Marketplace domain, Feign between services",
          },
        }),
        req("Job requirements", "GET", "/api/ai/job/requirements", {
          auth: employerAuth,
          query: [
            { key: "title", value: "Java backend engineer" },
            { key: "category", value: "Engineering" },
          ],
        }),
        req("Salary suggestion", "POST", "/api/ai/job/salary-suggestion", {
          auth: employerAuth,
          body: {
            title: "Java backend engineer",
            skills: ["Java", "Spring Boot"],
            experienceLevel: "MID_LEVEL",
            jobType: "FULL_TIME",
            location: "Bengaluru",
          },
        }),
        req("Skills recommendation", "GET", "/api/ai/job/skills-recommendation", {
          auth: employerAuth,
          query: [
            { key: "title", value: "Java backend engineer" },
            { key: "category", value: "Engineering" },
          ],
        }),
        req("Job responsibilities", "GET", "/api/ai/job/responsibilities", {
          auth: employerAuth,
          query: [
            { key: "title", value: "Java backend engineer" },
            { key: "category", value: "Engineering" },
          ],
        }),
        req("Job benefits", "GET", "/api/ai/job/benefits", {
          auth: employerAuth,
          query: [
            { key: "title", value: "Java backend engineer" },
            { key: "category", value: "Engineering" },
            { key: "jobType", value: "FULL_TIME" },
          ],
        }),
        req("Tags recommendation", "GET", "/api/ai/job/tags-recommendation", {
          auth: employerAuth,
          query: [
            { key: "title", value: "Java backend engineer" },
            { key: "description", value: "Spring Boot APIs" },
          ],
        }),
        req("Resume summary", "POST", "/api/ai/resume/summary", {
          auth: seekerAuth,
          body: {
            targetJobTitle: "Java backend engineer",
            workExperiences: [
              {
                jobTitle: "Software engineer",
                companyName: "Acme Labs",
                description: "Built REST APIs",
              },
            ],
            skills: ["Java", "Spring Boot"],
            educations: [
              {
                degree: "B.Tech",
                fieldOfStudy: "Computer Science",
                institutionName: "NITK",
              },
            ],
            yearsOfExperience: 4,
          },
        }),
        req("Experience bullets", "POST", "/api/ai/resume/experience-bullets", {
          auth: seekerAuth,
          body: {
            jobTitle: "Software engineer",
            company: "Acme Labs",
            rawDescription: "Worked on APIs and databases",
            achievementsHint: "Reduced p95 latency",
          },
        }),
        req("Resume improvements", "POST", "/api/ai/resume/improvements", {
          auth: seekerAuth,
          body: {
            resumeContent: "Java developer with Spring Boot experience.",
            targetJobTitle: "Java backend engineer",
          },
        }),
        req("Career feedback", "POST", "/api/ai/resume/career-feedback", {
          auth: seekerAuth,
          body: {
            resumeContent: "Java developer with Spring Boot experience.",
            targetJobTitle: "Staff engineer",
          },
        }),
        req("Cover letter (AI)", "POST", "/api/ai/application/cover-letter", {
          auth: seekerAuth,
          body: {
            jobTitle: "Java backend engineer",
            jobDescription: "Build application APIs.",
            candidateName: "Sam Seeker",
            candidateSummary: "4 years Spring Boot",
            candidateSkills: ["Java", "Spring Boot"],
            candidateExperience: ["Built REST APIs at Acme"],
            targetCompanyName: "Acme Labs",
          },
        }),
        req("Screening score (AI)", "POST", "/api/ai/application/screening-core", {
          auth: employerAuth,
          body: {
            jobTitle: "Java backend engineer",
            experienceLevel: "MID_LEVEL",
            requiredSkills: ["Java", "Spring Boot"],
            responsibilities: "Own APIs",
            candidateSummary: "4 years backend",
            candidateSkills: ["Java", "PostgreSQL"],
            candidateExperience: ["Spring Boot at Acme"],
          },
        }),
        req("Skills gap (AI)", "POST", "/api/ai/application/skills-gap", {
          auth: seekerAuth,
          body: {
            jobTitle: "Java backend engineer",
            candidateSkills: ["Java", "Spring Boot"],
            requiredSkills: ["Java", "Kafka", "Kubernetes"],
          },
        }),
        req("Search enhance", "POST", "/api/ai/search/enhance", {
          auth: seekerAuth,
          body: { query: "remote java jobs in bangalore" },
        }),
      ],
    },
  ],
};

const out = join(__dirname, "JobMate.postman_collection.json");
writeFileSync(out, JSON.stringify(collection, null, 2) + "\n");
console.log("Wrote", out);
