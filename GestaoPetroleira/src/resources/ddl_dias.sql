-- Schema
CREATE SCHEMA dias;
GO

-- Tipo de Navio
CREATE TABLE dias.TIPO_NAVIO (
    id               INT IDENTITY(1,1) PRIMARY KEY,
    nome             VARCHAR(100) NOT NULL,
    capacidade_maxima FLOAT        NOT NULL,
    max_cargas       INT          NOT NULL
);
GO

-- Navio
CREATE TABLE dias.NAVIO (
    id                   INT IDENTITY(1,1) PRIMARY KEY,
    nome                 VARCHAR(100) NOT NULL,
    codigo_imo           VARCHAR(20)  NOT NULL UNIQUE,
    id_tipo_navio        INT          NOT NULL REFERENCES dias.TIPO_NAVIO(id),
    estado_operacional   VARCHAR(20)  NOT NULL CHECK (estado_operacional IN ('ATIVO','INATIVO','EM_MANUTENCAO')),
    id_porto_atual       INT          NULL
);
GO

-- Manutenção
CREATE TABLE dias.MANUTENCAO (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    id_navio    INT  NOT NULL REFERENCES dias.NAVIO(id),
    data_inicio DATE NOT NULL,
    data_fim    DATE NULL,
    descricao   VARCHAR(255) NOT NULL
);
GO