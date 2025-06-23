-- V6__create_follows_table.sql
-- Criação da tabela follows_tb para a entidade Follow

CREATE TABLE follows_tb (
    id BIGSERIAL PRIMARY KEY,
    follower_id BIGINT NOT NULL,
    followed_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_follower FOREIGN KEY (follower_id) REFERENCES users_tb(id) ON DELETE CASCADE,
    CONSTRAINT fk_followed FOREIGN KEY (followed_id) REFERENCES users_tb(id) ON DELETE CASCADE
);

CREATE INDEX idx_follows_follower_id ON follows_tb(follower_id);
CREATE INDEX idx_follows_followed_id ON follows_tb(followed_id);

-- Criação da tabela posts_tb para a entidade Post
CREATE TABLE posts_tb (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    tribe_id BIGINT,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_post_user FOREIGN KEY (user_id) REFERENCES users_tb(id) ON DELETE CASCADE,
    CONSTRAINT fk_post_tribe FOREIGN KEY (tribe_id) REFERENCES tribes_tb(id) ON DELETE SET NULL
);

CREATE INDEX idx_posts_user_id ON posts_tb(user_id);
CREATE INDEX idx_posts_tribe_id ON posts_tb(tribe_id);
