CREATE TABLE IF NOT EXISTS tags (
    id      SERIAL PRIMARY KEY,
    name    VARCHAR(100) NOT NULL UNIQUE,
    name_bn VARCHAR(100),
    slug    VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS article_tags (
    article_id  UUID NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
    tag_id      INT  NOT NULL REFERENCES tags(id)     ON DELETE CASCADE,
    PRIMARY KEY (article_id, tag_id)
);

CREATE INDEX IF NOT EXISTS idx_article_tags_tag ON article_tags(tag_id);
