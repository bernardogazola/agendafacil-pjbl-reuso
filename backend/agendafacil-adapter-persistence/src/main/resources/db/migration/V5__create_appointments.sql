-- V5 - agendamentos (núcleo transacional do domínio).

CREATE TABLE appointments (
    id                           BIGSERIAL      PRIMARY KEY,
    customer_id                  BIGINT         NOT NULL,
    business_id                  BIGINT         NOT NULL,
    offered_service_id           BIGINT         NOT NULL,
    scheduled_at                 TIMESTAMP      NOT NULL,
    estimated_duration_minutes   INTEGER        NOT NULL,
    price_paid                   NUMERIC(10, 2) NOT NULL,
    status                       VARCHAR(20)    NOT NULL,
    notes                        VARCHAR(500),
    clinical_record_ref          VARCHAR(64),
    created_at                   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appointments_customer         FOREIGN KEY (customer_id)        REFERENCES customers (id),
    CONSTRAINT fk_appointments_business         FOREIGN KEY (business_id)        REFERENCES businesses (id),
    CONSTRAINT fk_appointments_offered_service  FOREIGN KEY (offered_service_id) REFERENCES offered_services (id)
);

CREATE INDEX idx_appointments_customer            ON appointments (customer_id);
CREATE INDEX idx_appointments_business_scheduled  ON appointments (business_id, scheduled_at);