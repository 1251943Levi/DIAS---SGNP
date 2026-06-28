-- =====================================================================
-- Alteração incremental: acrescenta o compartimento (numero_tanque) à CARGA.
-- Correr UMA vez numa base de dados já criada (sem apagar dados).
-- =====================================================================

ALTER TABLE dias.CARGA ADD numero_tanque INT NULL;
GO

-- Um compartimento só pode ter uma carga por viagem (índice filtrado: ignora o catálogo).
CREATE UNIQUE INDEX UQ_CARGA_viagem_tanque
    ON dias.CARGA(id_viagem, numero_tanque) WHERE id_viagem IS NOT NULL;
GO
