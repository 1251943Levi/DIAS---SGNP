-- =====================================================================
-- CORRIGE a coluna 'funcao' para os valores usados pelo codigo atual:
--   CAPITAO, OFICIAL, ENGENHEIRO, OPERADOR
-- Aplica-se as tabelas dias.TRIPULANTE e dias.TRIPULACAO_VIAGEM.
-- Correr UMA vez na base de dados (servidor do ISEP).
-- =====================================================================

-- 1) Remover as restricoes CHECK antigas (nome e gerado automaticamente,
--    por isso descobrimo-las dinamicamente).
DECLARE @sql NVARCHAR(MAX) = N'';
SELECT @sql += N'ALTER TABLE dias.' + t.name + N' DROP CONSTRAINT ' + cc.name + N';' + CHAR(10)
FROM sys.check_constraints cc
JOIN sys.tables   t ON cc.parent_object_id = t.object_id
JOIN sys.schemas  s ON t.schema_id = s.schema_id
WHERE s.name = 'dias'
  AND t.name IN ('TRIPULANTE','TRIPULACAO_VIAGEM');
IF @sql <> N'' EXEC sp_executesql @sql;
GO

-- 2) Converter os valores antigos -> novos (nas duas tabelas)
UPDATE dias.TRIPULANTE SET funcao =
    CASE funcao
        WHEN 'CAPITAO'          THEN 'CAPITAO'
        WHEN 'IMEDIATO'         THEN 'OFICIAL'
        WHEN 'PILOTO'           THEN 'OFICIAL'
        WHEN 'OFICIAL'          THEN 'OFICIAL'
        WHEN 'ENGENHEIRO_CHEFE' THEN 'ENGENHEIRO'
        WHEN 'ENGENHEIRO'       THEN 'ENGENHEIRO'
        WHEN 'MARINHEIRO'       THEN 'OPERADOR'
        WHEN 'COZINHEIRO'       THEN 'OPERADOR'
        WHEN 'OPERADOR'         THEN 'OPERADOR'
        ELSE 'OPERADOR'
    END
WHERE funcao IS NOT NULL;
GO

UPDATE dias.TRIPULACAO_VIAGEM SET funcao =
    CASE funcao
        WHEN 'CAPITAO'          THEN 'CAPITAO'
        WHEN 'IMEDIATO'         THEN 'OFICIAL'
        WHEN 'PILOTO'           THEN 'OFICIAL'
        WHEN 'OFICIAL'          THEN 'OFICIAL'
        WHEN 'ENGENHEIRO_CHEFE' THEN 'ENGENHEIRO'
        WHEN 'ENGENHEIRO'       THEN 'ENGENHEIRO'
        WHEN 'MARINHEIRO'       THEN 'OPERADOR'
        WHEN 'COZINHEIRO'       THEN 'OPERADOR'
        WHEN 'OPERADOR'         THEN 'OPERADOR'
        ELSE 'OPERADOR'
    END;
GO

-- 3) Voltar a criar a restricao CHECK com os 4 valores corretos
ALTER TABLE dias.TRIPULANTE
    ADD CONSTRAINT CK_TRIPULANTE_funcao
    CHECK (funcao IN ('CAPITAO','OFICIAL','ENGENHEIRO','OPERADOR'));
GO

ALTER TABLE dias.TRIPULACAO_VIAGEM
    ADD CONSTRAINT CK_TRIPULACAO_VIAGEM_funcao
    CHECK (funcao IN ('CAPITAO','OFICIAL','ENGENHEIRO','OPERADOR'));
GO

-- 4) Verificacao: valores distintos que ficaram
SELECT 'TRIPULANTE' AS tabela, funcao, COUNT(*) AS n FROM dias.TRIPULANTE GROUP BY funcao
UNION ALL
SELECT 'TRIPULACAO_VIAGEM', funcao, COUNT(*) FROM dias.TRIPULACAO_VIAGEM GROUP BY funcao;
GO
