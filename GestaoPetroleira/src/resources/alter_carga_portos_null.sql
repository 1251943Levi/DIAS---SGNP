-- =====================================================================
-- SGNP — Opção 2 (cargas-template sem portos)
-- Torna os portos da CARGA opcionais (NULL).
--   - Template (catálogo): id_porto_carga / id_porto_descarga ficam NULL.
--   - Cópia por viagem: herda os portos da rota (origem -> carga, destino -> descarga).
-- Correr UMA vez sobre uma BD já existente (não apaga dados).
-- =====================================================================

ALTER TABLE dias.CARGA ALTER COLUMN id_porto_carga    INT NULL;
GO
ALTER TABLE dias.CARGA ALTER COLUMN id_porto_descarga INT NULL;
GO

-- (Opcional) limpar os portos das cargas-template já existentes no catálogo,
-- para ficarem coerentes com o novo modelo (só as que ainda não têm viagem).
UPDATE dias.CARGA
   SET id_porto_carga = NULL, id_porto_descarga = NULL
 WHERE id_viagem IS NULL;
GO
