CREATE TABLE IF NOT EXISTS articles (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title                   VARCHAR(500) NOT NULL,
    title_bn                VARCHAR(500),
    slug                    VARCHAR(500) NOT NULL UNIQUE,
    summary                 TEXT,
    summary_bn              TEXT,
    content                 TEXT NOT NULL,
    content_bn              TEXT,
    status                  VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    author_id               UUID NOT NULL REFERENCES users(id),
    category_id             INT  REFERENCES categories(id) ON DELETE SET NULL,
    featured_image          VARCHAR(500),
    featured_image_caption  VARCHAR(300),
    is_breaking             BOOLEAN NOT NULL DEFAULT FALSE,
    is_featured             BOOLEAN NOT NULL DEFAULT FALSE,
    view_count              BIGINT NOT NULL DEFAULT 0,
    rejection_note          TEXT,
    meta_title              VARCHAR(160),
    meta_description        VARCHAR(300),
    published_at            TIMESTAMPTZ,
    scheduled_at            TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_articles_slug               ON articles(slug);
CREATE INDEX IF NOT EXISTS idx_articles_status_published   ON articles(status, published_at DESC);
CREATE INDEX IF NOT EXISTS idx_articles_category_status    ON articles(category_id, status, published_at DESC);
CREATE INDEX IF NOT EXISTS idx_articles_author             ON articles(author_id);
CREATE INDEX IF NOT EXISTS idx_articles_breaking           ON articles(is_breaking) WHERE is_breaking = TRUE;
CREATE INDEX IF NOT EXISTS idx_articles_featured           ON articles(is_featured) WHERE is_featured = TRUE;
CREATE INDEX IF NOT EXISTS idx_articles_scheduled          ON articles(status, scheduled_at) WHERE status = 'SCHEDULED';
