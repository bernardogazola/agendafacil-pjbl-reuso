-- V3 - serviços oferecidos por cada empresa.

CREATE TABLE offered_services (
    id                  BIGSERIAL      PRIMARY KEY,
    name                VARCHAR(150)   NOT NULL,
    description         VARCHAR(500),
    base_price          NUMERIC(10, 2) NOT NULL,
    duration_minutes    INTEGER        NOT NULL,
    category            VARCHAR(50),
    pricing_policy_type VARCHAR(30)    NOT NULL DEFAULT 'FIXED',
    active              BOOLEAN        NOT NULL DEFAULT TRUE,
    business_id         BIGINT         NOT NULL,
    created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_offered_services_business FOREIGN KEY (business_id) REFERENCES businesses (id) ON DELETE CASCADE
);

CREATE INDEX idx_offered_services_business ON offered_services (business_id);