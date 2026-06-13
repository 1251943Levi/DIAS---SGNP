-- =====================================================================
-- DIAS-3 / slice Viagens  -- acrescenta ao schema 'dias' ja existente
-- =====================================================================

-- Porto
CREATE TABLE dias.PORTO (
    id     INT IDENTITY(1,1) PRIMARY KEY,
    nome   VARCHAR(100) NOT NULL,
    pais   VARCHAR(100) NOT NULL,
    codigo VARCHAR(20)  NULL
);
GO

-- Viagem
CREATE TABLE dias.VIAGEM (
    id               INT IDENTITY(1,1) PRIMARY KEY,
    id_navio         INT  NOT NULL REFERENCES dias.NAVIO(id),
    id_porto_origem  INT  NOT NULL REFERENCES dias.PORTO(id),
    id_porto_destino INT  NOT NULL REFERENCES dias.PORTO(id),
    data_partida     DATE NOT NULL,
    data_chegada     DATE NULL,
    estado           VARCHAR(20) NOT NULL
                     CHECK (estado IN ('PLANEADA','EM_CURSO','CONCLUIDA','CANCELADA'))
);
GO

-- (Recomendado) fechar a FK que ja existe implicitamente em NAVIO.id_porto_atual:
-- ALTER TABLE dias.NAVIO ADD CONSTRAINT FK_NAVIO_PORTO
--     FOREIGN KEY (id_porto_atual) REFERENCES dias.PORTO(id);
-- GO

-- ---------------------------------------------------------------------
-- Tabelas de juncao (dependem dos slices Cargas / Tripulacao) -- TBD:
-- CREATE TABLE dias.VIAGEM_CARGA (
--     id_viagem INT NOT NULL REFERENCES dias.VIAGEM(id),
--     id_carga  INT NOT NULL REFERENCES dias.CARGA(id),
--     PRIMARY KEY (id_viagem, id_carga)
-- );
-- GO
-- CREATE TABLE dias.VIAGEM_TRIPULACAO (
--     id_viagem     INT NOT NULL REFERENCES dias.VIAGEM(id),
--     id_tripulante INT NOT NULL REFERENCES dias.TRIPULANTE(id),
--     funcao        VARCHAR(50) NULL,
--     PRIMARY KEY (id_viagem, id_tripulante)
-- );
-- GO
