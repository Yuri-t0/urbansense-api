CREATE TABLE IF NOT EXISTS alerts (
    id BIGSERIAL PRIMARY KEY,
    city VARCHAR(100) NOT NULL,
    type VARCHAR(30) NOT NULL,
    severity VARCHAR(10) NOT NULL,
    message VARCHAR(512) NOT NULL,
    region VARCHAR(150),
    lat DOUBLE PRECISION,
    lon DOUBLE PRECISION,
    valid_from TIMESTAMP,
    valid_until TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_alerts_city_active ON alerts(city, active);
