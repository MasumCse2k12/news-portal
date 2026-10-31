-- =============================================================
-- News Portal — PostgreSQL Setup Script
-- Run ONCE on the production DB server as a superuser (postgres)
-- Usage: psql -U postgres -f db-setup.sql
--
-- To RESET an existing database (re-run migrations from scratch):
--   psql -U postgres -c "DROP DATABASE IF EXISTS news_portal;"
--   psql -U postgres -f db-setup.sql
-- =============================================================

CREATE USER my_news_portal WITH PASSWORD 'my_news_portal';
CREATE DATABASE news_portal OWNER my_news_portal
    ENCODING   = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE   = 'en_US.UTF-8'
    TEMPLATE   = template0;

-- Connect to the new database and grant schema privileges
\c news_portal

-- Ensure the app user can create and use objects in the public schema
-- (required explicitly in PostgreSQL 15+)
GRANT ALL ON SCHEMA public TO my_news_portal;
ALTER SCHEMA public OWNER TO my_news_portal;

-- Done — Flyway will create all tables on first app startup
