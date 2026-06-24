-- =====================================================================
-- SETUP COMPLETO — DIAS / Gestao Petroleira (SGNP)
-- Cria o schema 'dias', todas as tabelas (na versao usada pelo codigo)
-- e insere dados de teste. Correr este ficheiro de uma vez no SQL Server.
--
-- ATENCAO: apaga e recria as tabelas do schema 'dias' (perde dados).
-- =====================================================================

-- 1) Apagar tabelas (ordem das chaves estrangeiras: filhas -> pai)
DROP TABLE IF EXISTS dias.VIAGEM_CARGA;
DROP TABLE IF EXISTS dias.TRIPULACAO_VIAGEM;
DROP TABLE IF EXISTS dias.COMPATIBILIDADE;
DROP TABLE IF EXISTS dias.CARGA;
DROP TABLE IF EXISTS dias.VIAGEM;
DROP TABLE IF EXISTS dias.MANUTENCAO;
DROP TABLE IF EXISTS dias.NAVIO;
DROP TABLE IF EXISTS dias.TRIPULANTE;
DROP TABLE IF EXISTS dias.TIPO_CARGA;
DROP TABLE IF EXISTS dias.PORTO;
DROP TABLE IF EXISTS dias.TIPO_NAVIO;
GO

-- 2) Criar o schema se nao existir
IF NOT EXISTS (SELECT 1 FROM sys.schemas WHERE name = 'dias')
    EXEC('CREATE SCHEMA dias');
GO

-- 3) Tabelas -----------------------------------------------------------

CREATE TABLE dias.TIPO_NAVIO (
    id                INT IDENTITY(1,1) PRIMARY KEY,
    nome              VARCHAR(100) NOT NULL,
    capacidade_maxima FLOAT        NOT NULL,
    max_cargas        INT          NOT NULL
);
GO

CREATE TABLE dias.PORTO (
    id     INT IDENTITY(1,1) PRIMARY KEY,
    nome   VARCHAR(100) NOT NULL,
    pais   VARCHAR(100) NOT NULL,
    codigo VARCHAR(20)  NULL
);
GO

CREATE TABLE dias.NAVIO (
    id                 INT IDENTITY(1,1) PRIMARY KEY,
    nome               VARCHAR(100) NOT NULL,
    codigo_imo         VARCHAR(20)  NOT NULL UNIQUE,
    id_tipo_navio      INT          NOT NULL REFERENCES dias.TIPO_NAVIO(id),
    capacidade_maxima  FLOAT        NOT NULL,
    num_compartimentos INT          NOT NULL,
    bandeira           VARCHAR(60)  NOT NULL,
    ano_fabrico        INT          NOT NULL,
    estado_operacional VARCHAR(20)  NOT NULL
                       CHECK (estado_operacional IN ('ATIVO','INATIVO','EM_MANUTENCAO')),
    id_porto_atual     INT          NULL REFERENCES dias.PORTO(id)
);
GO

CREATE TABLE dias.MANUTENCAO (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    id_navio    INT  NOT NULL REFERENCES dias.NAVIO(id),
    data_inicio DATE NOT NULL,
    data_fim    DATE NULL,
    descricao   VARCHAR(255) NOT NULL
);
GO

CREATE TABLE dias.TRIPULANTE (
    id               INT IDENTITY(1,1) PRIMARY KEY,
    nome             VARCHAR(100) NOT NULL,
    numero_matricula VARCHAR(20)  NOT NULL UNIQUE,
    funcao           VARCHAR(30)  NULL
                     CHECK (funcao IN ('CAPITAO','OFICIAL','ENGENHEIRO','OPERADOR')),
    disponivel       BIT          NOT NULL DEFAULT 1
);
GO

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

CREATE TABLE dias.TRIPULACAO_VIAGEM (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    id_viagem     INT NOT NULL REFERENCES dias.VIAGEM(id),
    id_tripulante INT NOT NULL REFERENCES dias.TRIPULANTE(id),
    funcao        VARCHAR(30) NOT NULL
                  CHECK (funcao IN ('CAPITAO','OFICIAL','ENGENHEIRO','OPERADOR')),
    UNIQUE (id_viagem, id_tripulante)
);
GO

CREATE TABLE dias.TIPO_CARGA (
    id         INT IDENTITY(1,1) PRIMARY KEY,
    nome       VARCHAR(100) NOT NULL,
    inflamavel BIT NOT NULL DEFAULT 0,
    corrosiva  BIT NOT NULL DEFAULT 0,
    toxica     BIT NOT NULL DEFAULT 0
);
GO

CREATE TABLE dias.CARGA (
    id               INT IDENTITY(1,1) PRIMARY KEY,
    designacao       VARCHAR(150) NOT NULL,
    id_tipo_carga    INT   NOT NULL REFERENCES dias.TIPO_CARGA(id),
    volume           FLOAT NOT NULL,
    peso             FLOAT NOT NULL,
    id_porto_carga   INT   NOT NULL REFERENCES dias.PORTO(id),
    id_porto_descarga INT  NOT NULL REFERENCES dias.PORTO(id)
);
GO

CREATE TABLE dias.VIAGEM_CARGA (
    id_viagem INT NOT NULL REFERENCES dias.VIAGEM(id),
    id_carga  INT NOT NULL REFERENCES dias.CARGA(id),
    PRIMARY KEY (id_viagem, id_carga)
);
GO

CREATE TABLE dias.COMPATIBILIDADE (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    id_tipo_navio INT NOT NULL REFERENCES dias.TIPO_NAVIO(id),
    id_tipo_carga INT NOT NULL REFERENCES dias.TIPO_CARGA(id),
    UNIQUE (id_tipo_navio, id_tipo_carga)
);
GO

-- 4) Dados de teste ----------------------------------------------------

INSERT INTO dias.TIPO_NAVIO (nome, capacidade_maxima, max_cargas) VALUES
    ('Petroleiro Crude (VLCC)',   300000, 1),
    ('Petroleiro de Produtos',     75000, 4),
    ('Transportador de Gas (LNG)', 145000, 2);
GO

INSERT INTO dias.PORTO (nome, pais, codigo) VALUES
    ('Porto de Leixoes', 'Portugal',      'PTLEI'),
    ('Porto de Sines',   'Portugal',      'PTSIN'),
    ('Roterdao',         'Paises Baixos', 'NLRTM'),
    ('Antuerpia',        'Belgica',       'BEANR');
GO

INSERT INTO dias.NAVIO (nome, codigo_imo, id_tipo_navio, capacidade_maxima, num_compartimentos, bandeira, ano_fabrico, estado_operacional, id_porto_atual) VALUES
    ('Atlantico I', 'IMO9111111', 1, 280000, 1, 'Portugal', 2015, 'ATIVO', 1),
    ('Sines Star',  'IMO9222222', 2,  70000, 4, 'Portugal', 2018, 'ATIVO', 2),
    ('Boreal',      'IMO9333333', 3, 140000, 2, 'Malta',    2012, 'ATIVO', 3);
GO

INSERT INTO dias.TRIPULANTE (nome, numero_matricula, funcao, disponivel) VALUES
    ('Joao Martins', 'MAT1001', 'CAPITAO',    1),
    ('Ana Sousa',    'MAT1002', 'OFICIAL',    1),
    ('Carlos Pinto', 'MAT1003', 'ENGENHEIRO', 1),
    ('Rita Lopes',   'MAT1004', 'OPERADOR',   1);
GO

INSERT INTO dias.TIPO_CARGA (nome, inflamavel, corrosiva, toxica) VALUES
    ('Petroleo bruto',          1, 0, 0),
    ('Gasolina',                1, 0, 0),
    ('Produtos quimicos liquidos', 1, 1, 1);
GO

-- Compatibilidades de exemplo (tipo de navio x tipo de carga)
INSERT INTO dias.COMPATIBILIDADE (id_tipo_navio, id_tipo_carga) VALUES
    (1, 1),  -- VLCC <-> Petroleo bruto
    (2, 2),  -- Produtos <-> Gasolina
    (2, 3);  -- Produtos <-> Quimicos
GO

-- 5) Verificacao -------------------------------------------------------
SELECT 'TIPO_NAVIO' AS tabela, COUNT(*) AS n FROM dias.TIPO_NAVIO
UNION ALL SELECT 'PORTO',          COUNT(*) FROM dias.PORTO
UNION ALL SELECT 'NAVIO',          COUNT(*) FROM dias.NAVIO
UNION ALL SELECT 'TRIPULANTE',     COUNT(*) FROM dias.TRIPULANTE
UNION ALL SELECT 'TIPO_CARGA',     COUNT(*) FROM dias.TIPO_CARGA
UNION ALL SELECT 'COMPATIBILIDADE',COUNT(*) FROM dias.COMPATIBILIDADE
UNION ALL SELECT 'VIAGEM',         COUNT(*) FROM dias.VIAGEM;
GO
