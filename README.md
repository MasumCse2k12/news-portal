# News Portal — Bangladesh & International News

A full-stack online news portal covering local (Bangladesh), national, and international news.
Modelled after leading portals like Prothom Alo, BBC, Al Jazeera, and CNN.

## Tech Stack

| Layer | Technology |
|---|---|
| Layer | Technology |
|---|---|
| Backend | Java 25 + Spring Boot 4.1 (Monolith) |
| Build | Gradle 8.10 (Kotlin DSL) |
| Frontend | React 19 + Vite 6 + Redux Toolkit + TanStack Query |
| Styling | Tailwind CSS 4 |
| Database | PostgreSQL 16 (primary), Redis 7 (cache) |
| Search | Elasticsearch 8 (full-text, bilingual) |
| Media Storage | MinIO (S3-compatible) |
| Message Queue | RabbitMQ 3 |
| Migration | Flyway 11 |
| CI/CD | Jenkins (Gradle + Docker test services) |
| Containerization | Docker + Docker Compose |
| Runtime | Node.js 24 (frontend build), eclipse-temurin:25 (backend) |
| Reverse Proxy | Nginx |
| Monitoring | Prometheus + Grafana |

## Project Structure

```
news-portal/
├── docs/                        # Architecture, DB design, API docs
├── backend/                     # Spring Boot monolith
│   └── src/main/java/com/newsportal/
│       ├── config/              # Spring configs (Security, RabbitMQ, MinIO, Redis)
│       ├── common/              # Shared: BaseEntity, ApiResponse, ExceptionHandler
│       ├── security/            # JWT filter, UserDetailsService
│       ├── domain/              # Feature packages (article, user, category, etc.)
│       │   ├── article/         # Entity, Repository, Service, Controller, DTOs
│       │   ├── category/
│       │   ├── tag/
│       │   ├── comment/
│       │   ├── media/
│       │   ├── user/
│       │   ├── subscription/
│       │   └── auditlog/
│       ├── messaging/           # RabbitMQ publishers & consumers
│       ├── storage/             # MinIO service
│       ├── search/              # Elasticsearch service
│       └── scheduler/           # Spring Batch / scheduled jobs
├── frontend/                    # React 19 + Vite 6 SPA
│   └── src/
│       ├── components/          # Reusable UI components
│       ├── pages/               # Route-level pages (public, cms, admin)
│       ├── store/               # Redux Toolkit slices
│       ├── services/            # Axios API client
│       ├── hooks/               # Custom React hooks
│       └── i18n/                # Bengali + English translations
├── infrastructure/
│   ├── docker-compose.yml       # Full local dev stack
│   ├── nginx/nginx.conf         # Reverse proxy config
│   └── jenkins/Jenkinsfile      # CI/CD pipeline
└── scripts/                     # Helper shell scripts
```

## Quick Start (Local Development)

### Prerequisites
- Docker Desktop
- Java 25+
- Gradle 8.10+ (or use `./gradlew` wrapper — run `gradle wrapper --gradle-version 8.10` once inside `backend/`)
- Node.js 24+, npm

### 1. Start infrastructure services
```bash
docker-compose -f infrastructure/docker-compose.yml up -d
```

### 2. Run backend
```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=dev'
```

### 3. Run frontend
```bash
cd frontend
npm install
npm run dev
```

### 4. Access services
| Service | URL |
|---|---|
| Reader Portal | http://localhost:3000 |
| CMS (Admin/Editor) | http://localhost:3000/cms |
| Backend API | http://localhost:8080/api/v1 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| MinIO Console | http://localhost:9001 |
| RabbitMQ Management | http://localhost:15672 |
| Elasticsearch | http://localhost:9200 |

## Environment Variables

All config lives in `infrastructure/news-portal.env`. Fill in all `CHANGE_ME` values before running.
Key variables:
- `SERVER_HOST`, `SERVER_PORT` — backend bind address and port
- `VITE_API_BASE_URL` — full API URL baked into the frontend bundle at build time
- `DB_URL`, `DB_USER`, `DB_PASSWORD`
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`
- `MINIO_ENDPOINT`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, `MINIO_PUBLIC_URL`
- `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USER`, `RABBITMQ_PASSWORD`
- `JWT_SECRET` — generate with `openssl rand -hex 32`
- `ELASTICSEARCH_URIS`
- `MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD`
- `CORS_ORIGINS`

## Documentation

- [Architecture & System Design](docs/architecture.md)
- [Database Design](docs/db-design.md)
- [API Design](docs/api-design.md)
- [Deployment Guide](docs/deployment.md)

## Roles & Permissions

| Role | Capabilities |
|---|---|
| ADMIN | Full system access, user management |
| EDITOR | Publish/unpublish articles, manage all content |
| JOURNALIST | Create/edit own articles, submit for review |
| READER | Read, comment, subscribe |

## Editorial Workflow

```
DRAFT → REVIEW → PUBLISHED
              ↓
           REJECTED → DRAFT (with note)
DRAFT → SCHEDULED → PUBLISHED (auto, via scheduler)
PUBLISHED → ARCHIVED
```
# news-portal
