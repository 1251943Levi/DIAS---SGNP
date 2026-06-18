USE [2026_LP2_G1_FEIRA];
GO

-- Schema
IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = 'dias')
    EXEC('CREATE SCHEMA dias');
GO

-- =============================================================================
-- SECÇÃO A — DROP completo (ordem inversa das FK)
--            Descomente este bloco na apresentação / quando quiser reset total
-- =============================================================================
IF OBJECT_ID('dias.TRIPULACAO_VIAGEM', 'U') IS NOT NULL DROP TABLE dias.TRIPULACAO_VIAGEM;
IF OBJECT_ID('dias.VIAGEM_CARGA',      'U') IS NOT NULL DROP TABLE dias.VIAGEM_CARGA;
IF OBJECT_ID('dias.VIAGEM',            'U') IS NOT NULL DROP TABLE dias.VIAGEM;
IF OBJECT_ID('dias.MANUTENCAO',        'U') IS NOT NULL DROP TABLE dias.MANUTENCAO;
IF OBJECT_ID('dias.COMPATIBILIDADE',   'U') IS NOT NULL DROP TABLE dias.COMPATIBILIDADE;
IF OBJECT_ID('dias.CARGA',             'U') IS NOT NULL DROP TABLE dias.CARGA;
IF OBJECT_ID('dias.NAVIO',             'U') IS NOT NULL DROP TABLE dias.NAVIO;
IF OBJECT_ID('dias.TRIPULANTE',        'U') IS NOT NULL DROP TABLE dias.TRIPULANTE;
IF OBJECT_ID('dias.TIPO_CARGA',        'U') IS NOT NULL DROP TABLE dias.TIPO_CARGA;
IF OBJECT_ID('dias.TIPO_NAVIO',        'U') IS NOT NULL DROP TABLE dias.TIPO_NAVIO;
IF OBJECT_ID('dias.PORTO',             'U') IS NOT NULL DROP TABLE dias.PORTO;
GO

-- =============================================================================
-- SECÇÃO B — CREATE (ordem das FK)
-- =============================================================================

-- ── DIAS-1 : Navios ───────────────────────────────────────────────────────────
CREATE TABLE dias.PORTO (
    id     INT IDENTITY(1,1) PRIMARY KEY,
    nome   VARCHAR(100) NOT NULL,
    pais   VARCHAR(80)  NOT NULL,
    codigo VARCHAR(10)  NULL    -- UN/LOCODE, ex: PTLSB
);

CREATE TABLE dias.TIPO_NAVIO (
    id                INT IDENTITY(1,1) PRIMARY KEY,
    nome              VARCHAR(80)   NOT NULL,
    capacidade_maxima DECIMAL(12,2) NOT NULL DEFAULT 0,
    max_cargas        INT           NOT NULL DEFAULT 0
);

CREATE TABLE dias.NAVIO (
    id                 INT IDENTITY(1,1) PRIMARY KEY,
    nome               VARCHAR(100)  NOT NULL,
    codigo_imo         VARCHAR(20)   NOT NULL UNIQUE,
    id_tipo_navio      INT           NOT NULL REFERENCES dias.TIPO_NAVIO(id),
    capacidade_maxima  DECIMAL(12,2) NOT NULL,
    num_compartimentos INT           NOT NULL,
    bandeira           VARCHAR(80)   NOT NULL,
    ano_fabrico        INT           NOT NULL,
    estado_operacional VARCHAR(20)   NOT NULL
        CONSTRAINT chk_estado_navio CHECK (estado_operacional IN ('ATIVO','EM_MANUTENCAO','INATIVO')),
    id_porto_atual     INT           REFERENCES dias.PORTO(id)
);

CREATE TABLE dias.MANUTENCAO (
    id          INT IDENTITY(1,1) PRIMARY KEY,
    id_navio    INT          NOT NULL REFERENCES dias.NAVIO(id) ON DELETE CASCADE,
    data_inicio DATE         NOT NULL,
    data_fim    DATE,
    descricao   VARCHAR(300) NOT NULL
);

-- ── DIAS-2 : Cargas ───────────────────────────────────────────────────────────
CREATE TABLE dias.TIPO_CARGA (
    id         INT IDENTITY(1,1) PRIMARY KEY,
    nome       VARCHAR(80) NOT NULL,
    inflamavel BIT NOT NULL DEFAULT 0,
    corrosiva  BIT NOT NULL DEFAULT 0,
    toxica     BIT NOT NULL DEFAULT 0
);

CREATE TABLE dias.COMPATIBILIDADE (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    id_tipo_navio INT NOT NULL REFERENCES dias.TIPO_NAVIO(id),
    id_tipo_carga INT NOT NULL REFERENCES dias.TIPO_CARGA(id),
    CONSTRAINT uq_compat UNIQUE (id_tipo_navio, id_tipo_carga)
);

CREATE TABLE dias.CARGA (
    id                INT IDENTITY(1,1) PRIMARY KEY,
    designacao        VARCHAR(150)  NOT NULL,
    id_tipo_carga     INT           NOT NULL REFERENCES dias.TIPO_CARGA(id),
    volume            DECIMAL(12,3) NOT NULL,
    peso              DECIMAL(12,3) NOT NULL,
    id_porto_carga    INT           NOT NULL REFERENCES dias.PORTO(id),
    id_porto_descarga INT           NOT NULL REFERENCES dias.PORTO(id)
);

-- ── DIAS-3 : Viagens ──────────────────────────────────────────────────────────
CREATE TABLE dias.VIAGEM (
    id               INT IDENTITY(1,1) PRIMARY KEY,
    id_navio         INT  NOT NULL REFERENCES dias.NAVIO(id),
    id_porto_origem  INT  NOT NULL REFERENCES dias.PORTO(id),
    id_porto_destino INT  NOT NULL REFERENCES dias.PORTO(id),
    data_partida     DATE NOT NULL,
    data_chegada     DATE,
    estado           VARCHAR(20) NOT NULL DEFAULT 'PLANEADA'
        CONSTRAINT chk_estado_viagem CHECK (estado IN ('PLANEADA','EM_CURSO','CONCLUIDA','CANCELADA'))
);

CREATE TABLE dias.VIAGEM_CARGA (
    id        INT IDENTITY(1,1) PRIMARY KEY,
    id_viagem INT NOT NULL REFERENCES dias.VIAGEM(id) ON DELETE CASCADE,
    id_carga  INT NOT NULL REFERENCES dias.CARGA(id),
    CONSTRAINT uq_viagem_carga UNIQUE (id_viagem, id_carga)
);

-- ── DIAS-4 : Tripulação ───────────────────────────────────────────────────────
CREATE TABLE dias.TRIPULANTE (
    id               INT IDENTITY(1,1) PRIMARY KEY,
    nome             VARCHAR(100) NOT NULL,
    numero_matricula VARCHAR(30)  NOT NULL UNIQUE,
    funcao           VARCHAR(20)  NOT NULL
        CONSTRAINT chk_funcao CHECK (funcao IN ('CAPITAO','OFICIAL','ENGENHEIRO','OPERADOR')),
    disponivel       BIT NOT NULL DEFAULT 1
);

CREATE TABLE dias.TRIPULACAO_VIAGEM (
    id            INT IDENTITY(1,1) PRIMARY KEY,
    id_viagem     INT NOT NULL REFERENCES dias.VIAGEM(id) ON DELETE CASCADE,
    id_tripulante INT NOT NULL REFERENCES dias.TRIPULANTE(id),
    funcao        VARCHAR(20) NOT NULL
        CONSTRAINT chk_funcao_tv CHECK (funcao IN ('CAPITAO','OFICIAL','ENGENHEIRO','OPERADOR')),
    CONSTRAINT uq_trip_viagem UNIQUE (id_viagem, id_tripulante)
);
GO

