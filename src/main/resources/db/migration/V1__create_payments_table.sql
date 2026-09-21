CREATE TABLE payments (
                          id            UUID PRIMARY KEY,
                          sender_id     VARCHAR(64)    NOT NULL,
                          receiver_id   VARCHAR(64)    NOT NULL,
                          amount        NUMERIC(19, 4) NOT NULL,
                          currency      VARCHAR(3)     NOT NULL,
                          channel       VARCHAR(20)    NOT NULL,
                          ip_address    VARCHAR(45),
                          device_id     VARCHAR(128),
                          status        VARCHAR(20)    NOT NULL,
                          created_at    TIMESTAMPTZ    NOT NULL
);

CREATE INDEX idx_payments_sender_id ON payments (sender_id);
CREATE INDEX idx_payments_status ON payments (status);
CREATE INDEX idx_payments_created_at ON payments (created_at);
