-- Adicionar colunas na tabela users_tb
ALTER TABLE users_tb
    ADD COLUMN ultimo_login TIMESTAMP,
ADD COLUMN data_de_registro TIMESTAMP;

-- Atualizar registros existentes com valores padrão
UPDATE users_tb
SET data_de_registro = NOW(),
    ultimo_login = NULL;