-- Substituir TODOS os nicknames pela parte do e-mail antes do '@'
UPDATE users_tb
SET nickname = substring(email from '^[^@]+');
