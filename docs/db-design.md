# Database Design

## PostgreSQL Schema

### users
| Column | Type | Notes |
|---|---|---|
| id | UUID PK | gen_random_uuid() |
| username | VARCHAR(50) UNIQUE | login handle |
| email | VARCHAR(100) UNIQUE | contact + login |
| password | VARCHAR(255) | BCrypt hashed |
| full_name | VARCHAR(100) | display name |
| full_name_bn | VARCHAR(100) | Bengali name |
| role | VARCHAR(20) | ADMIN, EDITOR, JOURNALIST, READER |
| profile_image | VARCHAR(500) | MinIO URL |
| is_active | BOOLEAN | soft delete |
| last_login_at | TIMESTAMPTZ | |
| created_at | TIMESTAMPTZ | |
| updated_at | TIMESTAMPTZ | |

### categories
| Column | Type | Notes |
|---|---|---|
| id | SERIAL PK | |
| name | VARCHAR(100) | English name |
| name_bn | VARCHAR(100) | Bengali name |
| slug | VARCHAR(100) UNIQUE | URL segment |
| parent_id | INT FK → categories.id | NULL = top-level |
| sort_order | INT | display order |
| is_active | BOOLEAN | |

### articles
| Column | Type | Notes |
|---|---|---|
| id | UUID PK | |
| title | VARCHAR(500) | English |
| title_bn | VARCHAR(500) | Bengali |
| slug | VARCHAR(500) UNIQUE | SEO URL |
| summary | TEXT | excerpt |
| summary_bn | TEXT | Bengali excerpt |
| content | TEXT | HTML (sanitized) |
| content_bn | TEXT | Bengali HTML |
| status | VARCHAR(20) | DRAFT, REVIEW, PUBLISHED, SCHEDULED, ARCHIVED |
| author_id | UUID FK → users.id | journalist |
| category_id | INT FK → categories.id | |
| featured_image | VARCHAR(500) | MinIO URL |
| featured_image_caption | VARCHAR(300) | |
| is_breaking | BOOLEAN | breaking news flag |
| is_featured | BOOLEAN | homepage featured |
| view_count | BIGINT | |
| rejection_note | TEXT | editor note on REJECTED |
| meta_title | VARCHAR(160) | SEO meta title |
| meta_description | VARCHAR(300) | SEO meta desc |
| published_at | TIMESTAMPTZ | |
| scheduled_at | TIMESTAMPTZ | for SCHEDULED status |
| created_at | TIMESTAMPTZ | |
| updated_at | TIMESTAMPTZ | |

### tags
| Column | Type | Notes |
|---|---|---|
| id | SERIAL PK | |
| name | VARCHAR(100) UNIQUE | |
| name_bn | VARCHAR(100) | |
| slug | VARCHAR(100) UNIQUE | |

### article_tags (join)
| Column | Type | Notes |
|---|---|---|
| article_id | UUID FK | |
| tag_id | INT FK | |
| PK | (article_id, tag_id) | |

### comments
| Column | Type | Notes |
|---|---|---|
| id | UUID PK | |
| article_id | UUID FK → articles.id | CASCADE DELETE |
| user_id | UUID FK → users.id | commenter |
| parent_id | UUID FK → comments.id | NULL = top-level |
| content | TEXT | |
| status | VARCHAR(20) | PENDING, APPROVED, REJECTED |
| created_at | TIMESTAMPTZ | |

### media
| Column | Type | Notes |
|---|---|---|
| id | UUID PK | |
| filename | VARCHAR(300) | original filename |
| stored_name | VARCHAR(300) | UUID-based MinIO key |
| url | VARCHAR(500) | full MinIO URL |
| thumbnail_url | VARCHAR(500) | resized version |
| mime_type | VARCHAR(100) | image/webp, etc. |
| size_bytes | BIGINT | |
| width | INT | pixels |
| height | INT | pixels |
| alt_text | VARCHAR(300) | accessibility |
| uploaded_by | UUID FK → users.id | |
| created_at | TIMESTAMPTZ | |

### subscriptions
| Column | Type | Notes |
|---|---|---|
| id | UUID PK | |
| email | VARCHAR(100) UNIQUE | |
| name | VARCHAR(100) | optional |
| is_active | BOOLEAN | |
| subscribed_at | TIMESTAMPTZ | |
| unsubscribed_at | TIMESTAMPTZ | |

### audit_log
| Column | Type | Notes |
|---|---|---|
| id | BIGSERIAL PK | |
| user_id | UUID FK → users.id | actor |
| action | VARCHAR(100) | ARTICLE_PUBLISHED, USER_BANNED, etc. |
| entity_type | VARCHAR(50) | Article, User, Comment |
| entity_id | VARCHAR(100) | the target entity UUID |
| details | JSONB | before/after snapshot |
| ip_address | INET | |
| user_agent | VARCHAR(300) | |
| created_at | TIMESTAMPTZ | |

## Key Indexes

```sql
-- Articles: most common query patterns
CREATE INDEX idx_articles_status_published  ON articles(status, published_at DESC);
CREATE INDEX idx_articles_category_status   ON articles(category_id, status, published_at DESC);
CREATE INDEX idx_articles_author            ON articles(author_id);
CREATE INDEX idx_articles_slug              ON articles(slug);
CREATE INDEX idx_articles_breaking          ON articles(is_breaking) WHERE is_breaking = true;
CREATE INDEX idx_articles_featured          ON articles(is_featured) WHERE is_featured = true;
CREATE INDEX idx_articles_scheduled         ON articles(status, scheduled_at) WHERE status = 'SCHEDULED';

-- Comments
CREATE INDEX idx_comments_article_status    ON comments(article_id, status, created_at DESC);

-- Audit log
CREATE INDEX idx_audit_user_time            ON audit_log(user_id, created_at DESC);
CREATE INDEX idx_audit_entity               ON audit_log(entity_type, entity_id);
```

## Flyway Migration Files

```
V1__create_users.sql
V2__create_categories.sql
V3__create_articles.sql
V4__create_tags.sql
V5__create_comments.sql
V6__create_media.sql
V7__create_subscriptions.sql
V8__create_audit_log.sql
V9__add_indexes.sql
V10__seed_default_categories.sql
```

## ER Diagram (simplified)

```
users ──< articles (author_id)
categories ──< articles (category_id)
articles >──< tags (via article_tags)
articles ──< comments
comments ──< comments (parent_id, self-ref)
users ──< comments
users ──< media (uploaded_by)
users ──< audit_log
```
