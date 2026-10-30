# API Design

## Base URL
```
Development: http://localhost:8080/api/v1
Production:  https://api.yournewsportal.com/api/v1
```

## Authentication

All protected endpoints require:
```
Authorization: Bearer <access_token>
```

Refresh token is sent via HttpOnly cookie on login.

---

## Auth Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | /auth/register | None | Register reader account |
| POST | /auth/login | None | Login, returns JWT + sets refresh cookie |
| POST | /auth/refresh | Cookie | Get new access token |
| POST | /auth/logout | Bearer | Invalidate refresh token |
| POST | /auth/forgot-password | None | Send reset email |
| POST | /auth/reset-password | None | Reset with token |

---

## Public Article Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | /public/articles | None | Paginated article list |
| GET | /public/articles/{slug} | None | Single article by slug |
| GET | /public/articles/breaking | None | Breaking news (max 5) |
| GET | /public/articles/featured | None | Featured articles (homepage) |
| GET | /public/categories | None | Category tree |
| GET | /public/categories/{slug}/articles | None | Articles by category |
| GET | /public/tags/{slug}/articles | None | Articles by tag |
| GET | /public/search | None | Full-text search (`?q=flood&page=0`) |
| GET | /public/trending | None | Most-read articles (last 24h) |

**Query params for lists:**
```
?page=0&size=20&sort=publishedAt,desc&category=sports&tag=dhaka
```

**Article response:**
```json
{
  "id": "uuid",
  "title": "Flood hits Sylhet again",
  "titleBn": "সিলেটে আবার বন্যা",
  "slug": "flood-hits-sylhet-again-20241030",
  "summary": "...",
  "content": "<p>HTML content...</p>",
  "status": "PUBLISHED",
  "author": { "id": "uuid", "fullName": "Rahim Uddin" },
  "category": { "id": 3, "name": "Bangladesh", "slug": "bangladesh" },
  "tags": [{ "name": "Sylhet", "slug": "sylhet" }],
  "featuredImage": "https://media.newsportal.com/images/articles/...",
  "isBreaking": false,
  "viewCount": 4521,
  "publishedAt": "2024-10-30T14:30:00Z"
}
```

---

## Comment Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | /public/articles/{id}/comments | None | Approved comments |
| POST | /comments | READER | Post comment |
| DELETE | /comments/{id} | READER (own) | Delete own comment |

---

## CMS Endpoints (Journalist / Editor)

| Method | Path | Role | Description |
|---|---|---|---|
| GET | /cms/articles | JOURNALIST+ | Own articles list |
| POST | /cms/articles | JOURNALIST+ | Create article (DRAFT) |
| GET | /cms/articles/{id} | JOURNALIST+ | Get article |
| PUT | /cms/articles/{id} | JOURNALIST+ | Update article |
| PATCH | /cms/articles/{id}/submit | JOURNALIST | Submit for review |
| PATCH | /cms/articles/{id}/publish | EDITOR+ | Publish immediately |
| PATCH | /cms/articles/{id}/schedule | EDITOR+ | Schedule publish |
| PATCH | /cms/articles/{id}/reject | EDITOR+ | Reject with note |
| PATCH | /cms/articles/{id}/archive | EDITOR+ | Archive |
| PATCH | /cms/articles/{id}/breaking | EDITOR+ | Toggle breaking news |
| DELETE | /cms/articles/{id} | EDITOR+ | Delete (soft) |
| POST | /cms/media/upload | JOURNALIST+ | Upload image/file |
| GET | /cms/media | JOURNALIST+ | Own media library |

---

## Admin Endpoints

| Method | Path | Role | Description |
|---|---|---|---|
| GET | /admin/users | ADMIN | All users (paginated) |
| POST | /admin/users | ADMIN | Create journalist/editor |
| PUT | /admin/users/{id} | ADMIN | Update user |
| PATCH | /admin/users/{id}/activate | ADMIN | Activate user |
| PATCH | /admin/users/{id}/deactivate | ADMIN | Deactivate user |
| GET | /admin/categories | ADMIN | Manage categories |
| POST | /admin/categories | ADMIN | Create category |
| PUT | /admin/categories/{id} | ADMIN | Update category |
| GET | /admin/comments/pending | ADMIN/EDITOR | Pending comments |
| PATCH | /admin/comments/{id}/approve | ADMIN/EDITOR | Approve comment |
| PATCH | /admin/comments/{id}/reject | ADMIN/EDITOR | Reject comment |
| GET | /admin/analytics | ADMIN | Dashboard stats |
| GET | /admin/audit-log | ADMIN | Audit trail |

---

## Standard Response Envelope

**Success:**
```json
{
  "success": true,
  "message": "Article published successfully",
  "data": { ... },
  "timestamp": "2024-10-30T14:30:00Z"
}
```

**Paginated:**
```json
{
  "success": true,
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 20,
    "totalElements": 1542,
    "totalPages": 78,
    "last": false
  }
}
```

**Error:**
```json
{
  "success": false,
  "message": "Article not found",
  "errorCode": "ARTICLE_NOT_FOUND",
  "timestamp": "2024-10-30T14:30:00Z"
}
```

**Validation Error:**
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    { "field": "title", "message": "Title must not be blank" },
    { "field": "categoryId", "message": "Category is required" }
  ]
}
```

---

## HTTP Status Codes

| Code | Usage |
|---|---|
| 200 | Success (GET, PUT, PATCH) |
| 201 | Created (POST) |
| 204 | No content (DELETE) |
| 400 | Bad request / validation error |
| 401 | Unauthenticated |
| 403 | Forbidden (wrong role) |
| 404 | Resource not found |
| 409 | Conflict (slug already exists) |
| 422 | Business logic error |
| 429 | Rate limit exceeded |
| 500 | Internal server error |
