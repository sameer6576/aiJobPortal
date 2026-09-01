-- JobMate seed: job_portal_resume
-- candidate_id 3 = seeker@jobmate.local

CREATE OR REPLACE FUNCTION jobmate_sync_id_seq(p_table text) RETURNS void AS $$
DECLARE
  seq text;
  max_id bigint;
BEGIN
  EXECUTE format('SELECT COALESCE(MAX(id), 0) FROM %I', p_table) INTO max_id;
  IF max_id < 1 THEN
    RETURN;
  END IF;
  seq := pg_get_serial_sequence(p_table, 'id');
  IF seq IS NOT NULL THEN
    PERFORM setval(seq, max_id);
    RETURN;
  END IF;
  IF EXISTS (SELECT 1 FROM pg_class WHERE relkind = 'S' AND relname = p_table || '_seq') THEN
    PERFORM setval(p_table || '_seq', max_id);
  ELSIF EXISTS (SELECT 1 FROM pg_class WHERE relkind = 'S' AND relname = 'hibernate_sequence') THEN
    PERFORM setval('hibernate_sequence', max_id, true);
  END IF;
END;
$$ LANGUAGE plpgsql;

INSERT INTO resumes (
  id, candidate_id, title, template, visibility, is_default, is_active, completion_score,
  summary, first_name, last_name, headline, email, phone, city, country,
  linkedin_url, github_url, created_at, updated_at
)
SELECT
  1, 3, 'Arjun Mehta — Java Backend',
  'PROFESSIONAL', 'PUBLIC', TRUE, TRUE, 80,
  'Backend engineer with Java, Spring Boot, and PostgreSQL. Comfortable with REST APIs and Docker Compose.',
  'Arjun', 'Mehta', 'Java Backend Engineer',
  'seeker@jobmate.local', '+910000000003', 'Bengaluru', 'India',
  'https://www.linkedin.com/in/arjun-mehta-jobmate',
  'https://github.com/arjun-mehta-jobmate',
  NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM resumes WHERE candidate_id = 3 AND title = 'Arjun Mehta — Java Backend');

INSERT INTO educations (
  id, resume_id, institution_name, degree, field_of_study, grade,
  start_date, end_date, is_currently_studying, display_order, created_at, updated_at
)
SELECT 1, 1, 'Visvesvaraya Technological University', 'B.E.', 'Computer Science', '8.2 CGPA',
       DATE '2018-08-01', DATE '2022-06-30', FALSE, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM educations WHERE resume_id = 1 AND institution_name = 'Visvesvaraya Technological University');

INSERT INTO work_experiences (
  id, resume_id, company_name, job_title, employment_type, location,
  start_date, end_date, is_current_job, description, display_order, created_at, updated_at
)
SELECT 1, 1, 'Harbor Software', 'Software Engineer', 'FULL_TIME', 'Bengaluru',
       DATE '2022-07-01', NULL, TRUE,
       'Implemented Spring Boot services, JPA repositories, and REST endpoints used by internal tools.',
       0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM work_experiences WHERE resume_id = 1 AND company_name = 'Harbor Software');

INSERT INTO projects (
  id, resume_id, title, description, project_url, start_date, end_date,
  display_order, is_ongoing, created_at, updated_at
)
SELECT 1, 1, 'Inventory API',
       'Spring Boot + PostgreSQL inventory service with JWT auth and Docker Compose.',
       'https://github.com/arjun-mehta-jobmate/inventory-api',
       DATE '2023-01-01', DATE '2023-06-01', 0, FALSE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM projects WHERE resume_id = 1 AND title = 'Inventory API');

INSERT INTO resume_skills (id, resume_id, skill_name, proficiency_level, years_of_experience, display_order, created_at, updated_at)
SELECT 1, 1, 'Java', 'ADVANCED', 3, 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM resume_skills WHERE resume_id = 1 AND skill_name = 'Java');

INSERT INTO resume_skills (id, resume_id, skill_name, proficiency_level, years_of_experience, display_order, created_at, updated_at)
SELECT 2, 1, 'Spring Boot', 'INTERMEDIATE', 2, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM resume_skills WHERE resume_id = 1 AND skill_name = 'Spring Boot');

INSERT INTO resume_skills (id, resume_id, skill_name, proficiency_level, years_of_experience, display_order, created_at, updated_at)
SELECT 3, 1, 'PostgreSQL', 'INTERMEDIATE', 2, 2, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM resume_skills WHERE resume_id = 1 AND skill_name = 'PostgreSQL');

INSERT INTO languages (id, resume_id, language_name, proficiency, display_order, created_at, updated_at)
SELECT 1, 1, 'English', 'PROFESSIONAL', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM languages WHERE resume_id = 1 AND language_name = 'English');

INSERT INTO languages (id, resume_id, language_name, proficiency, display_order, created_at, updated_at)
SELECT 2, 1, 'Hindi', 'NATIVE', 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM languages WHERE resume_id = 1 AND language_name = 'Hindi');

INSERT INTO certifications (
  id, resume_id, name, issuing_organization, issue_date, credential_id, display_order, created_at, updated_at
)
SELECT 1, 1, 'Oracle Certified Professional: Java SE', 'Oracle', DATE '2023-03-15', 'OCP-JAVA-DEMO', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM certifications WHERE resume_id = 1 AND name = 'Oracle Certified Professional: Java SE');

INSERT INTO awards (
  id, resume_id, title, issued_by, award_date, description, display_order, created_at, updated_at
)
SELECT 1, 1, 'Hackathon Winner', 'Harbor Software', DATE '2024-02-10', 'Internal hackathon — API performance track.', 0, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM awards WHERE resume_id = 1 AND title = 'Hackathon Winner');

SELECT jobmate_sync_id_seq('resumes');
SELECT jobmate_sync_id_seq('educations');
SELECT jobmate_sync_id_seq('work_experiences');
SELECT jobmate_sync_id_seq('projects');
SELECT jobmate_sync_id_seq('resume_skills');
SELECT jobmate_sync_id_seq('languages');
SELECT jobmate_sync_id_seq('certifications');
SELECT jobmate_sync_id_seq('awards');
