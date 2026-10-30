# Deployment Guide

## Local Development

```bash
# Start all infrastructure containers
docker-compose -f infrastructure/docker-compose.yml up -d

# Verify all containers are running
docker-compose -f infrastructure/docker-compose.yml ps

# Backend (Flyway migrations run automatically on startup)
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Frontend
cd frontend
npm install && npm run dev
```

## CI/CD Pipeline (Jenkins)

### Branch Strategy
```
feature/* → develop → staging → main → production
```

### Pipeline Stages
1. **Checkout** — clone repo
2. **Unit Tests** — `mvn test`
3. **Integration Tests** — `mvn verify -P integration-test`
4. **Code Quality** — SonarQube scan
5. **Security Scan** — OWASP dependency check
6. **Build JAR** — `mvn package -DskipTests`
7. **Build Docker Image** — tag with build number
8. **Push to Registry** — Docker Hub or private registry
9. **DB Migration** — Flyway against target DB
10. **Deploy to Staging** (develop branch)
11. **Deploy to Production** (main branch, manual approval gate)

## Docker Build

```bash
# Backend
cd backend
docker build -t news-portal-api:latest .

# Frontend
cd frontend
docker build -t news-portal-web:latest .

# Full stack
docker-compose -f infrastructure/docker-compose.yml up --build
```

## Production Infrastructure Checklist

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
| test | H2 in-memory | Embedded | Unit/integration tests |
| staging | Staging RDS | ElastiCache | Pre-prod validation |
| prod | Production RDS | ElastiCache | Live traffic |

## Flyway Migration Rules

1. NEVER modify an existing migration file after it's committed
2. Always add new `Vn__description.sql` files
3. Use descriptive names: `V12__add_seo_columns_to_articles.sql`
4. Test migrations on staging before applying to production
5. Keep a rollback plan for every migration
