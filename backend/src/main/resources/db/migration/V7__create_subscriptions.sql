CREATE TABLE subscriptions (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email             VARCHAR(100) NOT NULL UNIQUE,
    name              VARCHAR(100),
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,
    subscribed_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    unsubscribed_at   TIMESTAMPTZ
);

CREATE INDEX idx_subscriptions_active ON subscriptions(is_active) WHERE is_active = TRUE;
