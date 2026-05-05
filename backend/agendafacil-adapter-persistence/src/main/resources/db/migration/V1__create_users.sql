-- V1 — hierarquia de usuários (JOINED): users + customers + administrators

CREATE TABLE users (
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(120) NOT NULL,
    email      VARCHAR(160) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    phone      VARCHAR(20),
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE customers (
    id         BIGINT PRIMARY KEY,
    birth_date DATE,
    CONSTRAINT fk_customers_user FOREIGN KEY (id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE administrators (
    id           BIGINT      PRIMARY KEY,
    access_level VARCHAR(30) NOT NULL,
    CONSTRAINT fk_administrators_user FOREIGN KEY (id) REFERENCES users (id) ON DELETE CASCADE
);

-- @ElementCollection Set<NotificationPreference> em Customer
CREATE TABLE customer_notification_preferences (
    customer_id BIGINT      NOT NULL,
    preference  VARCHAR(16) NOT NULL,
    CONSTRAINT pk_customer_notification_preferences PRIMARY KEY (customer_id, preference),
    CONSTRAINT fk_cnp_customer FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE CASCADE
);