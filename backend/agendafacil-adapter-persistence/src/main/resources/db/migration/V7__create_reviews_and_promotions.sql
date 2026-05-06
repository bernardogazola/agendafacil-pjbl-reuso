-- V7 - módulos opcionais agrupados: avaliações e promoções.

CREATE TABLE reviews (
    id             BIGSERIAL     PRIMARY KEY,
    appointment_id BIGINT        NOT NULL,
    customer_id    BIGINT        NOT NULL,
    rating         INTEGER       NOT NULL,
    comment        VARCHAR(1000),
    created_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_reviews_appointment        UNIQUE (appointment_id),
    CONSTRAINT fk_reviews_appointment        FOREIGN KEY (appointment_id) REFERENCES appointments (id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_customer           FOREIGN KEY (customer_id)    REFERENCES customers (id)
);

CREATE INDEX idx_reviews_customer ON reviews (customer_id);

CREATE TABLE promotions (
    id                  BIGSERIAL      PRIMARY KEY,
    business_id         BIGINT         NOT NULL,
    name                VARCHAR(150)   NOT NULL,
    description         VARCHAR(500),
    discount_percentage NUMERIC(5, 2),
    discount_amount     NUMERIC(10, 2),
    valid_from          DATE           NOT NULL,
    valid_to            DATE           NOT NULL,
    active              BOOLEAN        NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_promotions_business FOREIGN KEY (business_id) REFERENCES businesses (id) ON DELETE CASCADE
);

CREATE INDEX idx_promotions_business_window ON promotions (business_id, valid_from, valid_to);

CREATE TABLE promotion_offered_services (
    promotion_id        BIGINT NOT NULL,
    offered_service_id  BIGINT NOT NULL,
    CONSTRAINT pk_promotion_offered_services PRIMARY KEY (promotion_id, offered_service_id),
    CONSTRAINT fk_pos_promotion        FOREIGN KEY (promotion_id)       REFERENCES promotions (id)        ON DELETE CASCADE,
    CONSTRAINT fk_pos_offered_service  FOREIGN KEY (offered_service_id) REFERENCES offered_services (id)  ON DELETE CASCADE
);