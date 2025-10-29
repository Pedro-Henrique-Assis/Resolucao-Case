USE master;
GO

IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'AvaliacaoColaboradores')
BEGIN
    CREATE DATABASE AvaliacaoColaboradores;
END;
GO


USE AvaliacaoColaboradores;
GO

-- Tabela de Colaboradores
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[tb_colaborador]') AND type in (N'U'))
BEGIN
    CREATE TABLE tb_colaborador (
        matricula UNIQUEIDENTIFIER NOT NULL,
        nome NVARCHAR(255) NOT NULL,
        data_admissao DATE NOT NULL,
        cargo NVARCHAR(255) NOT NULL,
        PRIMARY KEY (matricula)
    );
END;
GO

-- Tabela de Avaliação Comportamental
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[tb_avaliacao_comportamental]') AND type in (N'U'))
BEGIN
    CREATE TABLE tb_avaliacao_comportamental (
        id BIGINT IDENTITY(1,1) NOT NULL,
        nota_ambiente_colaborativo FLOAT(53),
        nota_aprendizado FLOAT(53),
        nota_tomada_decisao FLOAT(53),
        nota_autonomia FLOAT(53),
        matricula UNIQUEIDENTIFIER NOT NULL,
        PRIMARY KEY (id)
    );
END;
GO

-- Tabela de Entregas
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[tb_entrega]') AND type in (N'U'))
BEGIN
    CREATE TABLE tb_entrega (
        id BIGINT IDENTITY(1,1) NOT NULL,
        descricao NVARCHAR(255),
        nota FLOAT(53),
        matricula UNIQUEIDENTIFIER NOT NULL,
        PRIMARY KEY (id)
    );
END;
GO

IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'FK_avaliacao_colaborador') AND parent_object_id = OBJECT_ID(N'[dbo].[tb_avaliacao_comportamental]'))
BEGIN
    ALTER TABLE tb_avaliacao_comportamental 
    ADD CONSTRAINT FK_avaliacao_colaborador 
    FOREIGN KEY (matricula) 
    REFERENCES tb_colaborador (matricula);
    
    -- Adiciona a restrição ÚNICA para garantir 1:1
    ALTER TABLE tb_avaliacao_comportamental
    ADD CONSTRAINT UQ_avaliacao_matricula UNIQUE (matricula);
END;
GO

IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'FK_entrega_colaborador') AND parent_object_id = OBJECT_ID(N'[dbo].[tb_entrega]'))
BEGIN
    ALTER TABLE tb_entrega 
    ADD CONSTRAINT FK_entrega_colaborador 
    FOREIGN KEY (matricula) 
    REFERENCES tb_colaborador (matricula);
END;
GO