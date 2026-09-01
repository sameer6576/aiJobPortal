-- JobMate seed: job_portal_application
-- candidate 3 / resume 1 / job 1 / company 1 / employer 2

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

INSERT INTO applications (
  id, candidate_id, company_id, employer_id, resume_id, job_id,
  status, cover_letter, expected_salary, is_starred,
  ai_score, ai_short_list_status, applied_at, created_at, updated_at
)
SELECT
  1, 3, 1, 2, 1, 1,
  'REVIEWING',
  'I have shipped Spring Boot services with JPA and PostgreSQL and would like to work on Nimbus Labs backend roles.',
  2000000, FALSE,
  72, 'REVIEW_RECOMMENDED', NOW(), NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM applications WHERE candidate_id = 3 AND job_id = 1);

INSERT INTO application_notes (id, application_id, added_by_user_id, content, created_at)
SELECT 1, 1, 2, 'Strong Java overlap. Schedule a screening call.', NOW()
WHERE NOT EXISTS (
  SELECT 1 FROM application_notes WHERE application_id = 1 AND added_by_user_id = 2
);

SELECT jobmate_sync_id_seq('applications');
SELECT jobmate_sync_id_seq('application_notes');
