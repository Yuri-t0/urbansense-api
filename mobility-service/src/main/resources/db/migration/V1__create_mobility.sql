CREATE TABLE IF NOT EXISTS transit_lines (
    id                    BIGSERIAL PRIMARY KEY,
    code                  VARCHAR(10)  UNIQUE NOT NULL,
    name                  VARCHAR(100) NOT NULL,
    operator              VARCHAR(50),
    color                 VARCHAR(30),
    rain_vulnerability    DOUBLE PRECISION DEFAULT 0.0,
    rain_threshold_mm     DOUBLE PRECISION DEFAULT 10.0,
    vulnerable_section    VARCHAR(200),
    avg_delay_pct_on_rain INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS transit_impacts (
    id              BIGSERIAL PRIMARY KEY,
    line_id         BIGINT NOT NULL REFERENCES transit_lines(id),
    impact_type     VARCHAR(30) NOT NULL,
    description     VARCHAR(512),
    rain_mm_at_event DOUBLE PRECISION,
    delay_minutes   INTEGER,
    occurred_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    resolved_at     TIMESTAMP,
    active          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_impacts_line_occurred ON transit_impacts(line_id, occurred_at DESC);
CREATE INDEX idx_impacts_active        ON transit_impacts(active) WHERE active = TRUE;

-- Dados reais das linhas de SP com histórico de vulnerabilidade
INSERT INTO transit_lines (code, name, operator, color, rain_vulnerability, rain_threshold_mm, vulnerable_section, avg_delay_pct_on_rain)
VALUES
    ('L1',  'Linha 1 Azul',      'Metro SP', 'blue',     0.2, 25.0, 'Jabaquara-Tucuruvi',        15),
    ('L2',  'Linha 2 Verde',     'Metro SP', 'green',    0.2, 25.0, 'Vila Madalena-Vila Prudente',10),
    ('L3',  'Linha 3 Vermelha',  'Metro SP', 'red',      0.7, 15.0, 'Brás-Corinthians',          45),
    ('L4',  'Linha 4 Amarela',   'Metro SP', 'yellow',   0.1, 40.0, 'Butantã-Paulista',           8),
    ('L5',  'Linha 5 Lilás',     'Metro SP', 'purple',   0.1, 40.0, 'Capão Redondo-Chácara Klabin', 5),
    ('L7',  'Linha 7 Rubi',      'CPTM',     'ruby',     0.5, 12.0, 'Luz-Francisco Morato',      35),
    ('L8',  'Linha 8 Diamante',  'CPTM',     'diamond',  0.5, 10.0, 'Júlio Prestes-Amador Aguiar', 40),
    ('L9',  'Linha 9 Esmeralda', 'CPTM',     'emerald',  0.6, 10.0, 'Osasco-Grajaú',             50),
    ('L10', 'Linha 10 Turquesa', 'CPTM',     'teal',     0.8, 8.0,  'ABC-Brás',                  60),
    ('L11', 'Linha 11 Coral',    'CPTM',     'coral',    0.7, 10.0, 'Guaianazes-Luz',            55),
    ('L12', 'Linha 12 Safira',   'CPTM',     'sapphire', 0.4, 15.0, 'Calmon Viana-Brás',         30),
    ('L13', 'Linha 13 Jade',     'CPTM',     'jade',     0.3, 20.0, 'Engenheiro Goulart-Aeroporto', 20)
ON CONFLICT (code) DO NOTHING;
