-- V9__alter_posts_for_many_to_many_tribes.sql
-- Remove a coluna tribe_id da tabela posts_tb e cria a tabela de associação post_tribe_tb


-- Remove a constraint e a coluna tribe_id
ALTER TABLE posts_tb DROP CONSTRAINT IF EXISTS fk_post_tribe;
ALTER TABLE posts_tb DROP COLUMN IF EXISTS tribe_id;

-- Cria a tabela de associação post_tribe_tb
CREATE TABLE post_tribe_tb (
                               post_id BIGINT NOT NULL,
                               tribe_id BIGINT NOT NULL,
                               PRIMARY KEY (post_id, tribe_id),
                               CONSTRAINT fk_post_tribe_post FOREIGN KEY (post_id) REFERENCES posts_tb(id) ON DELETE CASCADE,
                               CONSTRAINT fk_post_tribe_tribe FOREIGN KEY (tribe_id) REFERENCES tribes_tb(id) ON DELETE CASCADE
);

CREATE INDEX idx_post_tribe_post_id ON post_tribe_tb(post_id);
CREATE INDEX idx_post_tribe_tribe_id ON post_tribe_tb(tribe_id);