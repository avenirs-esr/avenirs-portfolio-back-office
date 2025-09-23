SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'template1'
  AND pid <> pg_backend_pid();

CREATE ROLE avenirs_back_office_admin_role SUPERUSER;
CREATE ROLE avenirs_back_office_admin PASSWORD 'ENC(35gvyDVeasprFzTNQvTjl1HAMjTb5Zcu)' NOSUPERUSER CREATEDB CREATEROLE INHERIT LOGIN;
GRANT avenirs_back_office_admin_role to avenirs_back_office_admin;

CREATE DATABASE avenirs_back_office OWNER avenirs_back_office_admin;
GRANT ALL PRIVILEGES ON DATABASE avenirs_back_office TO avenirs_back_office_admin_role;
\c avenirs_back_office
CREATE SCHEMA IF NOT EXISTS dev AUTHORIZATION avenirs_back_office_admin;
ALTER USER avenirs_back_office_admin SET search_path TO dev, public;
CREATE EXTENSION IF NOT EXISTS citext;
CREATE EXTENSION IF NOT EXISTS pgcrypto;
