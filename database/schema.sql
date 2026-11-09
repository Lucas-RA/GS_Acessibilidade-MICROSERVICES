-- Estrutura que o Hibernate cria no SQL Server (ddl-auto=update). Fica como referência do banco;
-- a aplicação não executa este arquivo.

IF OBJECT_ID('dbo.usuarios', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.usuarios (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        ativo BIT NOT NULL,
        criado_em DATETIME2 NOT NULL,
        email VARCHAR(150) NOT NULL,
        nome VARCHAR(100) NOT NULL,
        perfil_mobilidade VARCHAR(30) NULL CHECK (perfil_mobilidade IN ('CADEIRA_DE_RODAS', 'DEFICIENCIA_VISUAL', 'MOBILIDADE_REDUZIDA', 'OUTRO')),
        tipo_perfil VARCHAR(20) NOT NULL CHECK (tipo_perfil IN ('CIDADAO', 'GESTOR')),
        CONSTRAINT UKkfsp0s1tflm1cwlj8idhqsad0 UNIQUE (email)
    );
END;

IF OBJECT_ID('dbo.locais_publicos', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.locais_publicos (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        ativo BIT NOT NULL,
        endereco VARCHAR(200) NOT NULL,
        latitude FLOAT NULL,
        longitude FLOAT NULL,
        nome VARCHAR(120) NOT NULL,
        tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('PONTO_ATENDIMENTO', 'PARADA_ONIBUS'))
    );
END;

IF OBJECT_ID('dbo.relatos_acessibilidade', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.relatos_acessibilidade (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        categoria VARCHAR(20) NOT NULL CHECK (categoria IN ('CALCADA', 'RAMPA', 'PISO_TATIL', 'SINALIZACAO', 'ELEVADOR', 'BANHEIRO', 'ATENDIMENTO', 'OUTRO')),
        criado_em DATETIME2 NOT NULL,
        descricao VARCHAR(500) NOT NULL,
        descricao_foto VARCHAR(300) NULL,
        foto_url VARCHAR(300) NULL,
        prioridade VARCHAR(10) NOT NULL CHECK (prioridade IN ('BAIXA', 'MEDIA', 'ALTA')),
        status VARCHAR(20) NOT NULL CHECK (status IN ('ABERTO', 'EM_ANALISE', 'RESOLVIDO')),
        local_id BIGINT NOT NULL,
        usuario_id BIGINT NOT NULL,
        CONSTRAINT FK42er2nw1halyusfnk9r9nfnfy FOREIGN KEY (local_id) REFERENCES dbo.locais_publicos (id),
        CONSTRAINT FKap0l21g4bukdy86gfc1yt6an2 FOREIGN KEY (usuario_id) REFERENCES dbo.usuarios (id)
    );
END;

-- Um usuário valida o mesmo relato uma única vez.
IF OBJECT_ID('dbo.validacoes_relato', 'U') IS NULL
BEGIN
    CREATE TABLE dbo.validacoes_relato (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        comentario VARCHAR(300) NULL,
        criado_em DATETIME2 NOT NULL,
        situacao VARCHAR(20) NOT NULL CHECK (situacao IN ('AINDA_EXISTE', 'RESOLVIDO')),
        relato_id BIGINT NOT NULL,
        usuario_id BIGINT NOT NULL,
        CONSTRAINT UKtd2eujqm8jli21csu6ymh91e2 UNIQUE (relato_id, usuario_id),
        CONSTRAINT FKf0tigaf7ch7fsmom6sypwuvle FOREIGN KEY (relato_id) REFERENCES dbo.relatos_acessibilidade (id),
        CONSTRAINT FKg2ajg8bcufjoprqqevu5eux7j FOREIGN KEY (usuario_id) REFERENCES dbo.usuarios (id)
    );
END;
