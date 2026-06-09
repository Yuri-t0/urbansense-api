CREATE TABLE IF NOT EXISTS weather_records (
    id           BIGSERIAL PRIMARY KEY,
    city         VARCHAR(100) NOT NULL,
    latitude     DOUBLE PRECISION NOT NULL,
    longitude    DOUBLE PRECISION NOT NULL,
    temperature  DOUBLE PRECISION,
    feels_like   DOUBLE PRECISION,
    humidity     INTEGER,
    wind_speed   DOUBLE PRECISION,
    rain_mm      DOUBLE PRECISION DEFAULT 0,
    uv_index     INTEGER,
    condition    VARCHAR(20),
    description  VARCHAR(255),
    recorded_at  TIMESTAMP NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_weather_city_recorded ON weather_records(city, recorded_at DESC);
CREATE INDEX idx_weather_rain ON weather_records(rain_mm, recorded_at DESC);
