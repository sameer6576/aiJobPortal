-- JobMate seed: job_portal_user
-- Run after Hibernate has created tables (start user-service once).
-- Login password for all seeded users: Demo@1234
-- IDs are fixed so company/job/resume/application rows can reference them.

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

-- $2a$10$... is Spring BCryptPasswordEncoder for Demo@1234
INSERT INTO users (
  id, full_name, email, password, phone, role, status, created_at, updated_at
)
SELECT 1, 'JobMate Admin', 'admin@jobmate.local',
       '$2a$10$T5PC.V6aR1tjnmxibEMUfO4CipsiPB5SJezMS1UQ3j1WzPUA0ITQ6',
       '+910000000001', 'ROLE_ADMIN', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@jobmate.local');

INSERT INTO users (
  id, full_name, email, password, phone, role, status, created_at, updated_at
)
SELECT 2, 'Priya Sharma', 'employer@jobmate.local',
       '$2a$10$T5PC.V6aR1tjnmxibEMUfO4CipsiPB5SJezMS1UQ3j1WzPUA0ITQ6',
       '+910000000002', 'ROLE_EMPLOYER', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'employer@jobmate.local');

INSERT INTO users (
  id, full_name, email, password, phone, role, status, created_at, updated_at
)
SELECT 3, 'Arjun Mehta', 'seeker@jobmate.local',
       '$2a$10$T5PC.V6aR1tjnmxibEMUfO4CipsiPB5SJezMS1UQ3j1WzPUA0ITQ6',
       '+910000000003', 'ROLE_JOB_SEEKER', 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'seeker@jobmate.local');

SELECT jobmate_sync_id_seq('users');
