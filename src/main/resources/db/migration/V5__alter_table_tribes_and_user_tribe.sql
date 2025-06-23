-- Migration V5: Ajustes nas tabelas tribes_tb e criação da tabela de relacionamento user_tribe
CREATE TABLE IF NOT EXISTS tribes_tb (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS user_tribe (
    user_id BIGINT NOT NULL,
    tribe_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, tribe_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users_tb(id) ON DELETE CASCADE,
    CONSTRAINT fk_tribe FOREIGN KEY (tribe_id) REFERENCES tribes_tb(id) ON DELETE CASCADE
);



