-- V10__create_messages_table.sql

-- Criar a tabela de mensagens
CREATE TABLE messages_tb (
                             id SERIAL PRIMARY KEY,               -- Identificador único da mensagem
                             sender_id INTEGER NOT NULL,          -- ID do usuário remetente
                             recipient_id INTEGER NOT NULL,       -- ID do usuário destinatário
                             text TEXT NOT NULL,                  -- Conteúdo da mensagem
                             timestamp TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,  -- Data e hora da mensagem
                             is_read BOOLEAN DEFAULT FALSE,       -- Flag indicando se a mensagem foi lida
                             CONSTRAINT fk_sender FOREIGN KEY (sender_id) REFERENCES users_tb(id) ON DELETE CASCADE,  -- Relacionamento com o usuário remetente
                             CONSTRAINT fk_recipient FOREIGN KEY (recipient_id) REFERENCES users_tb(id) ON DELETE CASCADE  -- Relacionamento com o usuário destinatário
);

-- Criar índices para otimizar as consultas de mensagens entre dois usuários
CREATE INDEX idx_sender_recipient ON messages_tb(sender_id, recipient_id);
CREATE INDEX idx_recipient_sender ON messages_tb(recipient_id, sender_id);
