CREATE TABLE media (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    filename        VARCHAR(300) NOT NULL,
    stored_name     VARCHAR(300) NOT NULL,
    url             VARCHAR(500) NOT NULL,
    thumbnail_url   VARCHAR(500),
    mime_type       VARCHAR(100),
    size_bytes      BIGINT,
    width           INT,
    height          INT,
    alt_text        VARCHAR(300),
    uploaded_by     UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_media_uploaded_by ON media(uploaded_by, created_at DESC);
