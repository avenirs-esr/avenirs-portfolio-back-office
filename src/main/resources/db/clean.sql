\connect template1;
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'avenirs_api' AND pid <> pg_backend_pid();
DROP DATABASE IF EXISTS avenirs_back_office;
DROP ROLE IF EXISTS avenirs_back_office_admin;
DROP ROLE IF EXISTS avenirs_back_office_admin_role;
