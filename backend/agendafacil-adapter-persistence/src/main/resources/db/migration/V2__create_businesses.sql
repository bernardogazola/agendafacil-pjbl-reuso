-- V2 - empresas cadastradas. Cada empresa tem um administrador responsável.

CREATE TABLE businesses (
    id                       BIGSERIAL    PRIMARY KEY,
    trade_name               VARCHAR(200) NOT NULL,
    tax_id                   VARCHAR(20),
    email                    VARCHAR(160) NOT NULL,
    phone                    VARCHAR(20),
    category                 VARCHAR(30)  NOT NULL,
    plan                     VARCHAR(30)  NOT NULL,
    cancellation_policy_type VARCHAR(30)  NOT NULL DEFAULT 'FREE',
    active                   BOOLEAN      NOT NULL DEFAULT TRUE,
    owner_id                 BIGINT       NOT NULL,
    created_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_businesses_email UNIQUE (email),
    CONSTRAINT fk_businesses_owner FOREIGN KEY (owner_id) REFERENCES administrators (id)
);

CREATE INDEX idx_businesses_owner ON businesses (owner_id);