-- JobMate seed: job_portal_job
-- Taxonomy is required before the UI can create jobs. Jobs 1–2 are OPEN (public list).
-- company_id 1 / employer_id 2 match the company and employer seeds.

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

INSERT INTO job_categories (id, name, slug, description, active, created_at, updated_at)
SELECT 1, 'Engineering', 'engineering', 'Software and infrastructure roles', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM job_categories WHERE name = 'Engineering');

INSERT INTO job_categories (id, name, slug, description, parent_id, active, created_at, updated_at)
SELECT 2, 'Backend', 'backend', 'API and service engineering', 1, TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM job_categories WHERE name = 'Backend');

INSERT INTO job_categories (id, name, slug, description, parent_id, active, created_at, updated_at)
SELECT 3, 'Frontend', 'frontend', 'Web client engineering', 1, TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM job_categories WHERE name = 'Frontend');

INSERT INTO job_categories (id, name, slug, description, active, created_at, updated_at)
SELECT 4, 'Design', 'design', 'Product and visual design', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM job_categories WHERE name = 'Design');

INSERT INTO job_skills (id, name, slug, active, created_at, updated_at)
SELECT 1, 'Java', 'java', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM job_skills WHERE name = 'Java');

INSERT INTO job_skills (id, name, slug, active, created_at, updated_at)
SELECT 2, 'Spring Boot', 'spring-boot', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM job_skills WHERE name = 'Spring Boot');

INSERT INTO job_skills (id, name, slug, active, created_at, updated_at)
SELECT 3, 'PostgreSQL', 'postgresql', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM job_skills WHERE name = 'PostgreSQL');

INSERT INTO job_skills (id, name, slug, active, created_at, updated_at)
SELECT 4, 'React', 'react', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM job_skills WHERE name = 'React');

INSERT INTO job_skills (id, name, slug, active, created_at, updated_at)
SELECT 5, 'Apache Kafka', 'apache-kafka', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM job_skills WHERE name = 'Apache Kafka');

INSERT INTO job_skills (id, name, slug, active, created_at, updated_at)
SELECT 6, 'Docker', 'docker', TRUE, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM job_skills WHERE name = 'Docker');

INSERT INTO job_tags (id, name, slug, created_at, updated_at)
SELECT 1, 'Remote', 'remote', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM job_tags WHERE name = 'Remote');

INSERT INTO job_tags (id, name, slug, created_at, updated_at)
SELECT 2, 'Urgent', 'urgent', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM job_tags WHERE name = 'Urgent');

INSERT INTO job_tags (id, name, slug, created_at, updated_at)
SELECT 3, 'Internship', 'internship', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM job_tags WHERE name = 'Internship');

INSERT INTO jobs (
  id, title, description, requirements, responsibilities, benefits,
  company_id, employer_id, category_id,
  address, city, state, country, zip_code,
  min_salary, max_salary,
  job_type, work_mode, experience_level, status, opening, active,
  application_deadline, published_at, created_at, updated_at
)
SELECT
  1,
  'Senior Java Backend Engineer',
  'Build and operate Spring Cloud services for a job marketplace: gateway JWT, JPA, and Feign integrations.',
  'Java 21, Spring Boot, PostgreSQL, REST. Experience with Spring Security and OpenFeign is a plus.',
  'Own service APIs, reviews, and production-quality local Docker Compose demos.',
  'Health insurance, learning budget, hybrid office in Bengaluru.',
  1, 2, 2,
  'Koramangala', 'Bengaluru', 'Karnataka', 'India', '560034',
  1800000, 2800000,
  'FULL_TIME', 'HYBRID', 'SENIOR_LEVEL', 'OPEN', 2, TRUE,
  CURRENT_DATE + INTERVAL '45 days', NOW(), NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = 'Senior Java Backend Engineer' AND employer_id = 2);

INSERT INTO jobs (
  id, title, description, requirements, responsibilities, benefits,
  company_id, employer_id, category_id,
  address, city, state, country, zip_code,
  min_salary, max_salary,
  job_type, work_mode, experience_level, status, opening, active,
  application_deadline, published_at, created_at, updated_at
)
SELECT
  2,
  'React Frontend Developer',
  'Work on the JobMate SPA: job search, resume editor, and employer dashboards talking to the API gateway.',
  'React, JavaScript, REST clients. Vite experience preferred.',
  'Ship UI against existing backend contracts; no invented endpoints.',
  'Remote-friendly, flexible hours.',
  1, 2, 3,
  NULL, 'Bengaluru', 'Karnataka', 'India', NULL,
  1200000, 2000000,
  'FULL_TIME', 'REMOTE', 'MID_LEVEL', 'OPEN', 1, TRUE,
  CURRENT_DATE + INTERVAL '30 days', NOW(), NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = 'React Frontend Developer' AND employer_id = 2);

INSERT INTO jobs (
  id, title, description, requirements, responsibilities, benefits,
  company_id, employer_id, category_id,
  city, country, job_type, work_mode, experience_level, status, opening, active,
  created_at, updated_at
)
SELECT
  3,
  'Platform Intern (draft)',
  'Draft listing — not visible on the public job list.',
  'Curiosity about Java and SQL. Currently enrolled in a CS program.',
  'Shadow backend reviews and write tests.',
  'Stipend.',
  1, 2, 2,
  'Bengaluru', 'India', 'INTERNSHIP', 'ON_SITE', 'ENTRY_LEVEL', 'DRAFT', 1, TRUE,
  NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM jobs WHERE title = 'Platform Intern (draft)' AND employer_id = 2);

SELECT jobmate_sync_id_seq('job_categories');
SELECT jobmate_sync_id_seq('job_skills');
SELECT jobmate_sync_id_seq('job_tags');
SELECT jobmate_sync_id_seq('jobs');
