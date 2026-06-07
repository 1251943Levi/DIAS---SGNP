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

-- Tripulante
CREATE TABLE dias.TRIPULANTE (
    id               INT IDENTITY(1,1) PRIMARY KEY,
    nome             VARCHAR(100) NOT NULL,
    numero_matricula VARCHAR(20)  NOT NULL UNIQUE
);
GO

-- Viagem
CREATE TABLE dias.VIAGEM (
    id           INT IDENTITY(1,1) PRIMARY KEY,
    codigo       VARCHAR(20)  NOT NULL UNIQUE,
    id_navio     INT          NOT NULL REFERENCES dias.NAVIO(id),
    data_partida DATE         NOT NULL,
    data_chegada DATE         NULL
);
GO

-- Tripulação da Viagem
CREATE TABLE dias.TRIPULACAO_VIAGEM (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    id_viagem     INT NOT NULL REFERENCES dias.VIAGEM(id),
    id_tripulante INT NOT NULL REFERENCES dias.TRIPULANTE(id),
    funcao        VARCHAR(30) NOT NULL CHECK (funcao IN ('CAPITAO','IMEDIATO','PILOTO','ENGENHEIRO_CHEFE','ENGENHEIRO','MARINHEIRO','COZINHEIRO')),
    UNIQUE (id_viagem, id_tripulante)
);
GO