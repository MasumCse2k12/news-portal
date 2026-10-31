-- Ensures the article_tags join table exists.
-- Idempotent: safe on both clean databases (where V4 already created it)
-- and on databases where V4 ran with an earlier version that lacked this table.
CREATE TABLE IF NOT EXISTS article_tags (
    article_id  UUID NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    tag_id      INT  NOT NULL REFERENCES tags(id)     ON DELETE CASCADE,
    PRIMARY KEY (article_id, tag_id)
);

CREATE INDEX IF NOT EXISTS idx_article_tags_tag ON article_tags(tag_id);
