-- Seed users for local development.
-- Passwords are bcrypt hashes of the plaintext values shown in comments.
--
--   analyst / analyst123
--   senior  / senior123
--   admin   / admin123
--
-- DO NOT use these in production.

INSERT INTO users (id, username, password, role, enabled, created_at) VALUES
                                                                          (gen_random_uuid(), 'analyst', '$2a$10$FBhlw0hzirfgLmcDwnJvRO0hlXMEE1E7q8HPpaidWw5KbhF086t1a', 'ANALYST',        TRUE, NOW()),
                                                                          (gen_random_uuid(), 'senior',  '$2a$10$DzG6M959hAxHFmY09X9jaOct1OySBe4QC3GHwwTzu0L8LCPaJe5qy', 'SENIOR_ANALYST', TRUE, NOW()),
                                                                          (gen_random_uuid(), 'admin',   '$2a$10$HvH7lAUwE7nLoNPxxvfNYutXe2UBrBjAdsQb/Z4Ldu3WeVzhfuNK2', 'ADMIN',          TRUE, NOW());
