-- V4 - janelas de funcionamento semanais de cada empresa.

CREATE TABLE business_hours (
    id           BIGSERIAL   PRIMARY KEY,
    business_id  BIGINT      NOT NULL,
    day_of_week  VARCHAR(16) NOT NULL,
    start_time   TIME        NOT NULL,
    end_time     TIME        NOT NULL,
    active       BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_business_hours_business FOREIGN KEY (business_id) REFERENCES businesses (id) ON DELETE CASCADE,
    CONSTRAINT uk_business_hours_business_day UNIQUE (business_id, day_of_week)
);