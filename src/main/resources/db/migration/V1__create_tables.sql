-- Criar nova tabela de usuários
CREATE TABLE users_tb (
                          id SERIAL PRIMARY KEY,
                          email VARCHAR(100) NOT NULL UNIQUE,
                          password VARCHAR(255) NOT NULL,
                          latitude DOUBLE PRECISION,
                          longitude DOUBLE PRECISION
);

-- Criar nova tabela de roles
CREATE TABLE roles_tb (
                          id SERIAL PRIMARY KEY,
                          name VARCHAR(50) NOT NULL
);

-- Criar tabela de associação entre usuários e roles
CREATE TABLE user_roles_tb (
                               user_id INTEGER NOT NULL,
                               role_id INTEGER NOT NULL,
                               CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users_tb(id) ON DELETE CASCADE,
                               CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles_tb(id) ON DELETE CASCADE,
                               CONSTRAINT unique_user_role UNIQUE (user_id, role_id)
);