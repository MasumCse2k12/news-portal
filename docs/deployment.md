# Deployment Guide

## Prerequisites

| Tool | Version |
|---|---|
| Java | 25+ |
| Gradle | 8.10+ (or use `./gradlew` wrapper) |
| Node.js | 24+ |
| Docker | 24+ |
| Docker Compose | v2 |

## Local Development

```bash
# 1. Start all infrastructure containers
docker-compose -f infrastructure/docker-compose.yml \
    --env-file infrastructure/news-portal.env up -d

# 2. Verify all containers are running
docker-compose -f infrastructure/docker-compose.yml ps

# 3. Backend — Flyway migrations run automatically on startup
cd backend
./gradlew bootRun --args='--spring.profiles.active=dev'

# 4. Frontend
cd frontend
npm install && npm run dev
```

## Environment File

All credentials and runtime config live in `infrastructure/news-portal.env`.
Copy and fill in all `CHANGE_ME` values before deploying.

```bash
cp infrastructure/news-portal.env infrastructure/news-portal.env.local
# edit news-portal.env.local with real values
```

Key variables:

| Variable | Description |
|---|---|
| `SERVER_HOST` | Backend bind address (`0.0.0.0` = all interfaces) |
| `SERVER_PORT` | Backend port (default `8080`) |
| `VITE_API_BASE_URL` | Full API URL baked into the frontend bundle at build time |
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | PostgreSQL connection |
| `JWT_SECRET` | 256-bit hex — generate with `openssl rand -hex 32` |
| `MINIO_PUBLIC_URL` | Public-facing URL for media files |
| `CORS_ORIGINS` | Comma-separated allowed origins |

## Database Setup

Run once on the production DB server as the `postgres` superuser:

```bash
psql -U postgres -f infrastructure/scripts/db-setup.sql
```

This creates the `newsportal` database and `newsportal_user` with least-privilege access.
Flyway runs all migrations automatically on first application startup.

## Gradle Build

```bash
cd backend

# Run tests
./gradlew test

# Build production JAR
./gradlew bootJar -x test

# Run DB migrations manually
./gradlew flywayMigrate \
    -Dflyway.url=jdbc:postgresql://localhost:5432/newsportal \
    -Dflyway.user=newsportal_user \
    -Dflyway.password=YOUR_PASSWORD
```

> **First-time setup:** If `gradle/wrapper/gradle-wrapper.jar` is missing,
> run `gradle wrapper --gradle-version 8.10` once inside `backend/` and commit the result.

## Docker Build

```bash
# Backend (multi-stage: gradle:8.10-jdk25-alpine builder + eclipse-temurin:25-jre-alpine runtime)
docker build -t news-portal-api:latest ./backend

# Frontend (multi-stage: node:24-alpine builder + nginx:alpine runtime)
# VITE_API_BASE_URL is baked in at build time
docker build \
    --build-arg VITE_API_BASE_URL=http://YOUR_SERVER_IP:8084/api/v1 \
    -t news-portal-web:latest ./frontend

# Full stack with env file
docker-compose -f infrastructure/docker-compose.yml \
    --env-file infrastructure/news-portal.env \
    up --build
```

## CI/CD Pipeline (Jenkins)

### Jenkins Setup

1. **JDK 25** — Manage Jenkins → Global Tool Configuration → JDK → add `jdk-25`
2. **Secret file credential** — Manage Jenkins → Credentials → Global → Add Credentials
   - Kind: **Secret file**
   - File: upload `news-portal.env`
   - ID: **`NEWS_PORTAL_ENV`**
3. **Docker registry credential** — ID: `docker-registry-creds` (username/password)

### Branch Strategy

```
feature/* → develop → staging → main → production
```

### Pipeline Stages

| Stage | Trigger | Description |
|---|---|---|
| Checkout | always | Clone repo |
| Start Test Services | always | `docker run` Elasticsearch + RabbitMQ for tests |
| Backend: Test | always | `./gradlew test`, JUnit + JaCoCo reports |
| Stop Test Services | always | Remove test containers |
| Backend: Code Quality | develop / main | SonarQube scan |
| Backend: Security Scan | main only | OWASP dependency check |
| Backend: Build JAR | always | `./gradlew bootJar` → `build/libs/news-portal-api.jar` |
| Frontend: Build | always | `npm ci && npm run build` |
| Docker: Build & Push | develop / main | Builds both images, pushes to registry |
| DB Migration | develop / main | `./gradlew flywayMigrate` using `NEWS_PORTAL_ENV` credentials |
| Deploy: Staging | develop | `docker-compose up` on staging server |
| Deploy: Production | main | Manual approval gate → `docker-compose up` on production |

### Test Services (Docker)

The pipeline spins up throwaway containers per build:

| Service | Image | Port |
|---|---|---|
| Elasticsearch | `elasticsearch:8.11.4` | `9200` |
| RabbitMQ | `rabbitmq:3.13-alpine` | `5672` |

Container names are suffixed with `${BUILD_NUMBER}` (e.g. `np-es-42`) to allow parallel builds. They are always removed in `post { always { ... } }` even if the build fails.

## Production Infrastructure Checklist

- [ ] `news-portal.env` uploaded to Jenkins as Secret file `NEWS_PORTAL_ENV`
- [ ] `db-setup.sql` executed on production PostgreSQL
- [ ] `JWT_SECRET` set to a strong random value (`openssl rand -hex 32`)
- [ ] `VITE_API_BASE_URL` set to the public-facing API URL
- [ ] Cloudflare DNS + WAF enabled
- [ ] SSL certificate (Let's Encrypt via Certbot)
- [ ] PostgreSQL: daily backups to S3/MinIO
- [ ] Redis: AOF persistence enabled
- [ ] MinIO: replication configured
- [ ] Elasticsearch: snapshot to S3 daily
- [ ] Prometheus + Grafana running
- [ ] Alertmanager configured (email/Slack)
- [ ] Log aggregation (ELK or Loki)
- [ ] Health checks configured in Docker/K8s

## Environment Profiles

| Profile | DB | Cache | Description |
|---|---|---|---|
| dev | Local PostgreSQL | Local Redis | Full local stack via Docker |
| test | H2 in-memory | Disabled | Unit tests — no external services needed |
| prod | Production PostgreSQL | Production Redis | Live traffic |

## Flyway Migration Rules

1. NEVER modify an existing migration file after it's committed
2. Always add new `Vn__description.sql` files
3. Use descriptive names: `V12__add_seo_columns_to_articles.sql`
4. Test migrations on staging before applying to production
5. Keep a rollback plan for every migration
