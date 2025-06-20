-- Remover duplicatas na tabela roles_tb
DELETE FROM roles_tb
WHERE id NOT IN (
    SELECT MIN(id)
    FROM roles_tb
    GROUP BY name
);

-- Garantir que a tabela roles_tb tenha apenas papéis únicos
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'unique_role_name'
    ) THEN
ALTER TABLE roles_tb
    ADD CONSTRAINT unique_role_name UNIQUE (name);
END IF;
END $$;