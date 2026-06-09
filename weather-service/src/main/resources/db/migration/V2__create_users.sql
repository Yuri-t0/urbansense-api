CREATE TABLE IF NOT EXISTS users (
    id       BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    active   BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role    VARCHAR(50) NOT NULL
);

-- Usuário admin padrão (senha: admin123)
INSERT INTO users (username, password, active)
VALUES ('admin', '$2b$10$pH01qadbFxTtm/cOyfByQeFBcdbsFDxDS68oK3DlkuS./Oj1SsQC2', TRUE)
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN' FROM users WHERE username = 'admin'
ON CONFLICT DO NOTHING;

-- Usuário padrão (senha: user123)
INSERT INTO users (username, password, active)
VALUES ('user', '$2b$10$BUjjK7VNjAOy0VTJvh3hUeoZE0DL519W90RRKgbXPDSjk75OorThC', TRUE)
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role)
SELECT id, 'USER' FROM users WHERE username = 'user'
ON CONFLICT DO NOTHING;
