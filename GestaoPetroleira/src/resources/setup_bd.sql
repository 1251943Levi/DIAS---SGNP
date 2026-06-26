-- =====================================================================
-- SGNP — Sistema de Gestão de Navios Petroleiros
-- Script da base de dados (DDL + dados de exemplo) — SQL Server / schema 'dias'
--
-- Este é o ÚNICO script a usar (substitui versões antigas).
-- Está alinhado com o código da aplicação.
--
-- Secções:
--   1) Remoção das tabelas (re-execução limpa)
--   2) Criação do schema
--   3) Criação das tabelas (DDL)
--   4) Dados de exemplo
--   5) Verificação
--
-- ATENCAO: as secções 1 e 4 apagam/inserem dados. Para entregar apenas o
--          MODELO, podes correr só as secções 2 e 3.
-- =====================================================================

-- 1) Remover tabelas (ordem das chaves estrangeiras: filhas -> pai) ----
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

-- 2) Schema ------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM sys.schemas WHERE name = 'dias')
    EXEC('CREATE SCHEMA dias');
GO

-- 3) Tabelas (DDL) -----------------------------------------------------

-- Tipo de navio: define o nº máximo de cargas por viagem.
-- "De que tipo de carga" cada tipo de navio aceita é dado pela tabela COMPATIBILIDADE.
CREATE TABLE dias.TIPO_NAVIO (
    id_tipo_navio INT IDENTITY(1,1) PRIMARY KEY,
    designacao    VARCHAR(100) NOT NULL,
    max_cargas    INT          NOT NULL
);
GO

CREATE TABLE dias.PORTO (
    id_porto INT IDENTITY(1,1) PRIMARY KEY,
    nome     VARCHAR(100) NOT NULL,
    pais     VARCHAR(100) NOT NULL,
    codigo   VARCHAR(20)  NULL          -- ex.: UN/LOCODE
);
GO

CREATE TABLE dias.NAVIO (
    id_navio           INT IDENTITY(1,1) PRIMARY KEY,
    nome               VARCHAR(100) NOT NULL,
    codigo_imo         VARCHAR(20)  NOT NULL UNIQUE,
    id_tipo_navio      INT          NOT NULL REFERENCES dias.TIPO_NAVIO(id_tipo_navio),
    capacidade_max     FLOAT        NOT NULL,        -- capacidade máxima de carga
    numero_tanques     INT          NOT NULL,        -- nº de compartimentos/tanques
    bandeira           VARCHAR(60)  NOT NULL,        -- país de registo
    ano_fabrico        INT          NOT NULL,
    estado_operacional VARCHAR(20)  NOT NULL
                       CHECK (estado_operacional IN ('ATIVO','INATIVO','EM_MANUTENCAO')),
    id_porto_atual     INT          NULL REFERENCES dias.PORTO(id_porto)
);
GO

CREATE TABLE dias.MANUTENCAO (
    id_manutencao INT IDENTITY(1,1) PRIMARY KEY,
    id_navio      INT  NOT NULL REFERENCES dias.NAVIO(id_navio),
    data_inicio   DATE NOT NULL,
    data_fim      DATE NULL,                          -- NULL = manutenção em curso
    descricao     VARCHAR(255) NOT NULL
);
GO

CREATE TABLE dias.TRIPULANTE (
    id_tripulante    INT IDENTITY(1,1) PRIMARY KEY,
    nome             VARCHAR(100) NOT NULL,
    nr_identificacao VARCHAR(20)  NOT NULL UNIQUE,
    funcao           VARCHAR(30)  NULL
                     CHECK (funcao IN ('CAPITAO','OFICIAL','ENGENHEIRO','OPERADOR')),
    disponibilidade  BIT          NOT NULL DEFAULT 1
);
GO

CREATE TABLE dias.VIAGEM (
    id_viagem             INT IDENTITY(1,1) PRIMARY KEY,
    id_navio              INT  NOT NULL REFERENCES dias.NAVIO(id_navio),
    id_porto_origem       INT  NOT NULL REFERENCES dias.PORTO(id_porto),
    id_porto_destino      INT  NOT NULL REFERENCES dias.PORTO(id_porto),
    data_partida          DATE NOT NULL,
    data_chegada_prevista DATE NULL,
    estado                VARCHAR(20) NOT NULL
                          CHECK (estado IN ('PLANEADA','EM_CURSO','CONCLUIDA','CANCELADA'))
);
GO

CREATE TABLE dias.TIPO_CARGA (
    id_tipo_carga INT IDENTITY(1,1) PRIMARY KEY,
    designacao    VARCHAR(100) NOT NULL,
    inflamavel    BIT NOT NULL DEFAULT 0,
    corrosiva     BIT NOT NULL DEFAULT 0,
    toxica        BIT NOT NULL DEFAULT 0
);
GO

-- Carga: pertence a uma viagem (id_viagem). peso = 0 => carga "pendente"
-- (gerada automaticamente ao planear a viagem e preenchida depois na gestão de cargas).
CREATE TABLE dias.CARGA (
    id_carga          INT IDENTITY(1,1) PRIMARY KEY,
    designacao        VARCHAR(150) NOT NULL,
    id_tipo_carga     INT   NOT NULL REFERENCES dias.TIPO_CARGA(id_tipo_carga),
    volume            FLOAT NOT NULL,
    peso              FLOAT NOT NULL,
    id_porto_carga    INT   NOT NULL REFERENCES dias.PORTO(id_porto),
    id_porto_descarga INT   NOT NULL REFERENCES dias.PORTO(id_porto),
    id_viagem         INT   NULL REFERENCES dias.VIAGEM(id_viagem)
);
GO

-- Compatibilidade tipo de navio <-> tipo de carga (relação N:N, chave composta)
CREATE TABLE dias.COMPATIBILIDADE (
    id_tipo_navio INT NOT NULL REFERENCES dias.TIPO_NAVIO(id_tipo_navio),
    id_tipo_carga INT NOT NULL REFERENCES dias.TIPO_CARGA(id_tipo_carga),
    PRIMARY KEY (id_tipo_navio, id_tipo_carga)
);
GO

-- Tripulação de uma viagem (relação N:N viagem <-> tripulante, com função)
CREATE TABLE dias.TRIPULACAO_VIAGEM (
    id_viagem     INT NOT NULL REFERENCES dias.VIAGEM(id_viagem),
    id_tripulante INT NOT NULL REFERENCES dias.TRIPULANTE(id_tripulante),
    funcao        VARCHAR(30) NOT NULL
                  CHECK (funcao IN ('CAPITAO','OFICIAL','ENGENHEIRO','OPERADOR')),
    PRIMARY KEY (id_viagem, id_tripulante)
);
GO

-- 4) Dados de exemplo --------------------------------------------------

-- Os 4 tipos de navio-tanque do enunciado (com nº máximo de cargas por viagem)
INSERT INTO dias.TIPO_NAVIO (designacao, max_cargas) VALUES
    ('Petroleiro de Crude',        1),   -- id 1
    ('Navio de Produtos Refinados',4),   -- id 2
    ('Navio Quimico',              3),   -- id 3
    ('Navio Quimico/Produtos',     4);   -- id 4 (híbrido)
GO

-- Os 6 tipos de carga, com propriedades (inflamavel, corrosiva, toxica)
INSERT INTO dias.TIPO_CARGA (designacao, inflamavel, corrosiva, toxica) VALUES
    ('Petroleo bruto',            1, 0, 0),  -- id 1
    ('Gasolina',                  1, 0, 0),  -- id 2
    ('Diesel/Gasoleo',            1, 0, 0),  -- id 3
    ('Jet fuel/Querosene',        1, 0, 0),  -- id 4
    ('Fueloleo/Betume',           0, 0, 0),  -- id 5
    ('Produtos quimicos liquidos',1, 1, 1);  -- id 6
GO

INSERT INTO dias.PORTO (nome, pais, codigo) VALUES
    ('Porto de Leixoes', 'Portugal',      'PTLEI'),  -- id 1
    ('Porto de Sines',   'Portugal',      'PTSIN'),  -- id 2
    ('Roterdao',         'Paises Baixos', 'NLRTM'),  -- id 3
    ('Antuerpia',        'Belgica',       'BEANR');  -- id 4
GO

-- Compatibilidades (define "de que tipo" de carga cada tipo de navio aceita)
INSERT INTO dias.COMPATIBILIDADE (id_tipo_navio, id_tipo_carga) VALUES
    (1, 1),                       -- Crude        -> Petroleo bruto
    (2, 2), (2, 3), (2, 4), (2, 5),-- Produtos     -> Gasolina, Diesel, Jet fuel, Fueloleo
    (3, 6),                       -- Quimico      -> Produtos quimicos
    (4, 6), (4, 2), (4, 3);       -- Quimico/Prod -> Quimicos + alguns refinados
GO

-- Navios (um de cada tipo), todos ATIVO
INSERT INTO dias.NAVIO (nome, codigo_imo, id_tipo_navio, capacidade_max, numero_tanques, bandeira, ano_fabrico, estado_operacional, id_porto_atual) VALUES
    ('Atlantico I', 'IMO9111111', 1, 280000, 1, 'Portugal', 2015, 'ATIVO', 1),
    ('Sines Star',  'IMO9222222', 2,  70000, 4, 'Portugal', 2018, 'ATIVO', 2),
    ('Quimico Lis', 'IMO9333333', 3,  40000, 6, 'Malta',    2016, 'ATIVO', 1),
    ('Hibrido Tejo','IMO9444444', 4,  55000, 5, 'Portugal', 2020, 'ATIVO', 2);
GO

-- Tripulantes (as 4 funções), disponíveis
INSERT INTO dias.TRIPULANTE (nome, nr_identificacao, funcao, disponibilidade) VALUES
    ('Joao Martins', 'MAT1001', 'CAPITAO',    1),
    ('Ana Sousa',    'MAT1002', 'OFICIAL',    1),
    ('Carlos Pinto', 'MAT1003', 'ENGENHEIRO', 1),
    ('Rita Lopes',   'MAT1004', 'OPERADOR',   1);
GO

-- 5) Verificação -------------------------------------------------------
SELECT 'TIPO_NAVIO' AS tabela, COUNT(*) AS n FROM dias.TIPO_NAVIO
UNION ALL SELECT 'TIPO_CARGA',     COUNT(*) FROM dias.TIPO_CARGA
UNION ALL SELECT 'PORTO',          COUNT(*) FROM dias.PORTO
UNION ALL SELECT 'COMPATIBILIDADE',COUNT(*) FROM dias.COMPATIBILIDADE
UNION ALL SELECT 'NAVIO',          COUNT(*) FROM dias.NAVIO
UNION ALL SELECT 'TRIPULANTE',     COUNT(*) FROM dias.TRIPULANTE
UNION ALL SELECT 'VIAGEM',         COUNT(*) FROM dias.VIAGEM
UNION ALL SELECT 'CARGA',          COUNT(*) FROM dias.CARGA;
GO
