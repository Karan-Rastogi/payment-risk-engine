CREATE TABLE rule_hits (
                           id           UUID PRIMARY KEY,
                           payment_id   UUID         NOT NULL,
                           rule_name    VARCHAR(64)  NOT NULL,
                           score        INTEGER      NOT NULL,
                           reason       VARCHAR(512),
                           created_at   TIMESTAMPTZ  NOT NULL,
                           CONSTRAINT fk_rule_hits_payment
                               FOREIGN KEY (payment_id) REFERENCES payments (id)
);

CREATE INDEX idx_rule_hits_payment_id ON rule_hits (payment_id);
CREATE INDEX idx_rule_hits_rule_name ON rule_hits (rule_name);
CREATE INDEX idx_rule_hits_created_at ON rule_hits (created_at);
