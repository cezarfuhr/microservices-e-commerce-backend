CREATE TABLE events (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    user_id BIGINT,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE analytics_summary (
    id BIGSERIAL PRIMARY KEY,
    total_orders BIGINT NOT NULL DEFAULT 0,
    total_revenue DECIMAL(15, 2) NOT NULL DEFAULT 0,
    total_users BIGINT NOT NULL DEFAULT 0,
    total_products BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_events_event_type ON events(event_type);
CREATE INDEX idx_events_user_id ON events(user_id);
CREATE INDEX idx_events_created_at ON events(created_at);

INSERT INTO analytics_summary (total_orders, total_revenue, total_users, total_products)
VALUES (0, 0, 0, 0);
