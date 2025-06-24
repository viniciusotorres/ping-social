-- v7__add_nickname_and_update_existing.sql
ALTER TABLE users_tb
    ADD COLUMN nickname VARCHAR(50) UNIQUE;

UPDATE users_tb
SET nickname = 'user' || id
WHERE nickname IS NULL;

ALTER TABLE users_tb
    ALTER COLUMN nickname SET NOT NULL;
