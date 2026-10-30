# Architecture & System Design

## Overview

Single monolith Spring Boot backend with clear domain-based package structure.
All features live in one deployable JAR. Extract to microservices only when traffic demands.

## Request Flow

```
Browser / Mobile
      |
   Cloudflare (CDN + WAF + DDoS protection)
      |
   Nginx (SSL termination, reverse proxy, static file serving)
      |
   ┌──────────────────────────────────────────┐
   │         Spring Boot Application           │
   │                                           │
   │  Controllers → Services → Repositories    │
   │       ↓            ↓                      │
   │  RabbitMQ     Redis Cache                 │
   │  Publisher    (read-through)              │
   │       ↓                                   │
   │  Consumers    PostgreSQL                  │
   │  (async jobs) (primary DB)                │
   └──────────────────────────────────────────┘
         |              |             |
      MinIO         Elasticsearch   RabbitMQ
   (media/images)  (full-text)     (events)
```

## Monolith Package Design (Domain-Driven)

Each domain package is self-contained:
```
domain/article/
  ├── Article.java           ← JPA Entity
  ├── ArticleStatus.java     ← Enum
  ├── ArticleRepository.java ← Spring Data JPA
  ├── ArticleService.java    ← Business logic
  ├── ArticleController.java ← REST endpoints
  ├── dto/
  │   ├── ArticleCreateRequest.java
  │   ├── ArticleUpdateRequest.java
  │   └── ArticleResponse.java
  └── mapper/
      └── ArticleMapper.java ← MapStruct
```

## Caching Strategy

```
L1 Browser cache     — React Query (60s for lists, 5min for single article)
L2 Nginx proxy cache — 30s for public GET endpoints
L3 Redis             — Article by slug (TTL 5min)
                       Category tree (TTL 1hr)
                       Trending list (TTL 1min)
                       User sessions (TTL 7 days)
```

Cache invalidation: When an article is published/updated, the service
evicts the relevant Redis keys using Spring Cache @CacheEvict.

## RabbitMQ Event Design

```
Exchange: news.portal.exchange (topic)

Routing Keys & Queues:
  article.published  → article.published.queue  → ElasticSearch indexer
                                                 → Newsletter trigger
                                                 → Sitemap regeneration
  article.viewed     → article.viewed.queue     → View count aggregator (bulk DB update)
  comment.created    → comment.moderation.queue → Auto-moderation check
  media.uploaded     → media.processing.queue   → Image resize/thumbnail generation
  notification.email → email.queue              → SMTP email sender
  notification.push  → push.queue               → Web push notification sender
```

## MinIO Storage Structure

```
Bucket: news-portal-media
  ├── images/
  │   ├── articles/{year}/{month}/{uuid}.webp
  │   ├── thumbnails/{year}/{month}/{uuid}_thumb.webp
  │   └── profiles/{userId}.webp
  └── attachments/
      └── {year}/{month}/{uuid}.pdf
```

## Elasticsearch Index

```json
Index: articles
{
  "id": "uuid",
  "title": "text (analyzed)",
  "title_bn": "text (bengali analyzer)",
  "summary": "text",
  "content": "text",
  "category": "keyword",
  "tags": ["keyword"],
  "author": "keyword",
  "published_at": "date",
  "is_breaking": "boolean",
  "view_count": "integer"
}
```

## Security Architecture

```
Public endpoints  → No auth required
CMS endpoints     → JWT Bearer token (JOURNALIST, EDITOR, ADMIN)
Admin endpoints   → JWT Bearer token (ADMIN only)

JWT:
  Access token:  15 minutes (sent in Authorization header)
  Refresh token: 7 days (stored in HttpOnly Secure cookie)

Password: BCrypt (cost 12)
```

## Scalability Path

When traffic grows beyond monolith limits:
1. Add read replicas to PostgreSQL → route read queries to replicas
2. Scale out: run multiple instances behind Nginx load balancer
3. Extract high-traffic domains (Article read) to dedicated service
4. Add Redis Cluster for horizontal cache scaling
5. Add Elasticsearch dedicated cluster nodes
