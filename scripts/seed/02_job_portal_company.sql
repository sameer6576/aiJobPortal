-- JobMate seed: job_portal_company
-- owner_id 2 = employer@jobmate.local from 01_job_portal_user.sql

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

INSERT INTO companies (
  id, name, slug, tagline, description, website, founded_year, email, phone,
  company_size, company_type, industry_type, status, is_verified, active,
  registration_number, owner_id, created_at, updated_at
)
SELECT
  1,
  'Nimbus Labs',
  'nimbus-labs',
  'Build software that ships',
  'Product engineering studio hiring Java and frontend developers. Local JobMate demo company.',
  'https://nimbus.example',
  2018,
  'jobs@nimbus.example',
  '+910000000010',
  'MEDIUM',
  'PRIVATE',
  'TECHNOLOGY',
  'ACTIVE',
  TRUE,
  TRUE,
  'CIN-JOBMATE-001',
  2,
  NOW(),
  NOW()
WHERE NOT EXISTS (SELECT 1 FROM companies WHERE owner_id = 2 OR name = 'Nimbus Labs');

SELECT jobmate_sync_id_seq('companies');
