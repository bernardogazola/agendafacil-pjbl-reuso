-- V6 - registro persistido de notificações.

CREATE TABLE notifications (
    id             BIGSERIAL     PRIMARY KEY,
    appointment_id BIGINT,
    recipient_id   BIGINT        NOT NULL,
    type           VARCHAR(30)   NOT NULL,
    channel        VARCHAR(16)   NOT NULL,
    message        VARCHAR(1000) NOT NULL,
    status         VARCHAR(16)   NOT NULL,
    sent_at        TIMESTAMP,
    created_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_appointment FOREIGN KEY (appointment_id) REFERENCES appointments (id) ON DELETE SET NULL,
    CONSTRAINT fk_notifications_recipient   FOREIGN KEY (recipient_id)   REFERENCES users (id)
);

CREATE INDEX idx_notifications_recipient ON notifications (recipient_id);
CREATE INDEX idx_notifications_status    ON notifications (status);