-- =============================================================
-- News Portal — PostgreSQL Setup Script
-- Run once on the production DB server as a superuser (postgres)
-- Usage: psql -U postgres -f db-setup.sql
-- =============================================================

-- 1. Create application user
CREATE USER newsportal_user WITH
    PASSWORD 'CHANGE_ME_STRONG_PASSWORD'
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    LOGIN;

-- 2. Create database owned by application user
CREATE DATABASE newsportal
    WITH OWNER      = newsportal_user
         ENCODING   = 'UTF8'
         LC_COLLATE = 'en_US.UTF-8'
         LC_CTYPE   = 'en_US.UTF-8'
         TEMPLATE   = template0;

-- 3. Connect to the new database
\c newsportal

-- 4. Restrict public schema access, grant only to app user
REVOKE ALL ON SCHEMA public FROM PUBLIC;
GRANT ALL ON SCHEMA public TO newsportal_user;

-- 5. Grant object-level privileges (covers tables Flyway will create)
ALTER DEFAULT PRIVILEGES FOR ROLE newsportal_user IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO newsportal_user;

ALTER DEFAULT PRIVILEGES FOR ROLE newsportal_user IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO newsportal_user;

-- 6. Optional: create a read-only reporting user
-- CREATE USER newsportal_readonly WITH PASSWORD 'CHANGE_ME_READONLY_PASSWORD' LOGIN;
-- GRANT CONNECT ON DATABASE newsportal TO newsportal_readonly;
-- GRANT USAGE ON SCHEMA public TO newsportal_readonly;
-- ALTER DEFAULT PRIVILEGES FOR ROLE newsportal_user IN SCHEMA public
--     GRANT SELECT ON TABLES TO newsportal_readonly;

-- Done — Flyway will run migrations on first app startup
