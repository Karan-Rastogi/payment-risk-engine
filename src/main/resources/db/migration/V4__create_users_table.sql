CREATE TABLE users (
                       id           UUID PRIMARY KEY,
                       username     VARCHAR(64)  NOT NULL,
                       password     VARCHAR(255) NOT NULL,
                       role         VARCHAR(32)  NOT NULL,
                       enabled      BOOLEAN      NOT NULL DEFAULT TRUE,
                       created_at   TIMESTAMPTZ  NOT NULL,
                       CONSTRAINT uk_users_username UNIQUE (username)
);

CREATE INDEX idx_users_username ON users (username);
