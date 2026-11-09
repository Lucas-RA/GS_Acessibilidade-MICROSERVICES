# Mapa Colaborativo de Acessibilidade Urbana — API

API REST para mapear barreiras de acessibilidade em locais públicos. Cidadãos relatam problemas como calçadas quebradas, rampas bloqueadas ou falta de piso tátil em pontos de atendimento e paradas de ônibus. Outros usuários confirmam se a barreira ainda existe, e os gestores públicos acompanham os relatos por status e prioridade.

Projeto da Global Solution 2026 da disciplina Microservice and Web Engineering.

## Integrantes

| Nome | RM |
|---|---|
| Enzo Grisolia de Souza | RM555706 |
| Lucas Rodrigues Alves | RM555377 |
| Rafael Catapani Scharlack | RM554633 |

## Links

- GitHub: [Lucas-RA/GS_Acessibilidade-MICROSERVICES](https://github.com/Lucas-RA/GS_Acessibilidade-MICROSERVICES)
- Docker Hub: [270506lu/gs-acessibilidade](https://hub.docker.com/r/270506lu/gs-acessibilidade)

## Tecnologias

- Java 17 e Spring Boot 4.0.9
- Spring MVC, Spring Data JPA (Hibernate) e Bean Validation
- SQL Server em desenvolvimento e Azure SQL em produção
- H2 apenas nos testes automatizados
- Springdoc OpenAPI (Swagger UI) e ModelMapper
- Maven Wrapper, Docker, GitHub Actions e Azure Container Apps

## Arquitetura

```text
Cliente HTTP → Controller → DTO/Mapper → Service → Repository → SQL Server
```

| Pacote | Responsabilidade |
|---|---|
| `controller` | rotas REST, códigos HTTP e documentação do Swagger |
| `dto` | contratos de entrada (`CreateRequest`, `UpdateRequest`) e saída (`Response`), e os mappers |
| `service` | regras de negócio |
| `repository` | acesso ao banco com `JpaRepository` |
| `model` | entidades JPA; `model/enums` guarda os enums |
| `exception` | formato padrão das respostas de erro |
| `config` | CORS |

A API nunca devolve as entidades diretamente: toda resposta passa por um DTO.

## Modelo de dados

```text
Usuario 1──N RelatoAcessibilidade N──1 LocalPublico
Usuario 1──N ValidacaoRelato      N──1 RelatoAcessibilidade
```

| Tabela | Campos principais |
|---|---|
| `usuarios` | nome, e-mail (único), tipo de perfil (`CIDADAO`, `GESTOR`), perfil de mobilidade, data de cadastro, ativo |
| `locais_publicos` | nome, tipo (`PONTO_ATENDIMENTO`, `PARADA_ONIBUS`), endereço, latitude, longitude, ativo |
| `relatos_acessibilidade` | descrição, categoria, status, prioridade, URL da foto, descrição da foto, data, usuário, local |
| `validacoes_relato` | situação (`AINDA_EXISTE`, `RESOLVIDO`), comentário, data, relato, usuário |

O DDL completo para SQL Server está em [database/schema.sql](database/schema.sql). O arquivo serve como referência: a aplicação cria e atualiza as tabelas com `ddl-auto=update`.

## Regras de negócio

- **E-mail único:** o e-mail é gravado sem espaços nas pontas e em minúsculas, então `Ana@Email.com` e `ana@email.com` são o mesmo usuário. Um e-mail repetido retorna `409`.
- **Prioridade automática:** o cliente não informa a prioridade. Ela é calculada pela categoria do relato:

  | Prioridade | Categorias | Motivo |
  |---|---|---|
  | `ALTA` | `CALCADA`, `RAMPA`, `ELEVADOR` | impedem a circulação |
  | `MEDIA` | `PISO_TATIL`, `SINALIZACAO`, `BANHEIRO` | dificultam o uso do local |
  | `BAIXA` | `ATENDIMENTO`, `OUTRO` | demais situações |

- **Status do relato:** todo relato nasce `ABERTO`. O gestor muda para `EM_ANALISE` ou `RESOLVIDO` pelo `PUT`, que também recalcula a prioridade quando a categoria muda.
- **Vínculos obrigatórios:** relato e validação só são criados se o usuário, o local e o relato informados existirem e se o usuário estiver ativo. Caso contrário, a resposta é `404`.
- **Uma validação por pessoa:** cada usuário valida o mesmo relato uma única vez. A segunda tentativa retorna `409`.
- **Dados preservados no PUT:** a atualização altera só os campos editáveis. A data de criação e os vínculos com usuário, local e relato não mudam.
- **Texto alternativo da foto:** o relato aceita `descricaoFoto`, a descrição da imagem lida por leitores de tela.

### Exclusão de usuário

Os relatos avisam outras pessoas com deficiência sobre barreiras no caminho, então não podem sumir quando quem os fez sai da plataforma. Por isso o `DELETE /api/v1/usuarios/{id}` não apaga o registro: ele desativa o usuário (`ativo = false`) e anonimiza os dados pessoais, como prevê a LGPD.

- O nome passa a ser "Usuário removido", o e-mail é trocado por um endereço genérico e o perfil de mobilidade é apagado.
- Os relatos e as validações continuam no mapa, exibindo "Usuário removido" como autor.
- O e-mail original fica livre para um novo cadastro.
- O usuário desativado não cria relatos nem validações (`404`) e não pode mais ser editado (`404` no `PUT`).
- `GET /api/v1/usuarios` lista todos; `/ativos` e `/inativos` filtram pela situação.

### Exclusão de registros com vínculos

Nos demais recursos, um registro que tem outros ligados a ele não pode ser excluído, e a API responde `409 Conflict`:

- local público com relatos;
- relato com validações.

As chaves estrangeiras do banco barram a exclusão. Escolhemos esse caminho em vez da exclusão em cascata para que apagar um cadastro não leve junto os relatos e as validações feitos pela comunidade. Para tirar um local de uso, a alternativa é o `PUT` com `"ativo": false`.

## Endpoints

O Swagger UI abre na raiz da aplicação (`http://localhost:8080/`), e o OpenAPI em JSON fica em `/v3/api-docs`.

| Recurso | Rota-base |
|---|---|
| Usuários | `/api/v1/usuarios` |
| Locais públicos | `/api/v1/locais-publicos` |
| Relatos de acessibilidade | `/api/v1/relatos-acessibilidade` |
| Validações de relato | `/api/v1/validacoes-relato` |

Todos os recursos têm as mesmas operações:

| Método | Rota | Sucesso | Outras respostas |
|---|---|---|---|
| `POST` | `/` | `201` | `400` dados inválidos, `404` vínculo inexistente, `409` duplicidade |
| `GET` | `/` | `200` | — |
| `GET` | `/{id}` | `200` | `404` |
| `PUT` | `/{id}` | `200` | `400`, `404`, `409` |
| `DELETE` | `/{id}` | `204` | `404`, `409` registro com vínculos |

Usuários têm mais duas rotas: `GET /api/v1/usuarios/ativos` e `GET /api/v1/usuarios/inativos`. No caso deles, o `DELETE` desativa e nunca responde `409`.

### Exemplos de requisição

Os exemplos seguem a ordem de uso: primeiro o usuário e o local, depois o relato e a validação.

`POST /api/v1/usuarios`

```json
{
  "nome": "Ana Silva",
  "email": "ana@email.com",
  "tipoPerfil": "CIDADAO",
  "perfilMobilidade": "CADEIRA_DE_RODAS"
}
```

`POST /api/v1/locais-publicos`

```json
{
  "nome": "Terminal Bandeira",
  "tipo": "PARADA_ONIBUS",
  "endereco": "Praça da Bandeira, s/n - Centro, São Paulo",
  "latitude": -23.5489,
  "longitude": -46.6388
}
```

`POST /api/v1/relatos-acessibilidade`

```json
{
  "descricao": "Rampa de acesso quebrada na plataforma",
  "categoria": "RAMPA",
  "fotoUrl": "https://exemplo.com/fotos/rampa.jpg",
  "descricaoFoto": "Rampa de concreto com a borda quebrada e um degrau de 10 cm",
  "usuarioId": 1,
  "localId": 1
}
```

Resposta (`201`):

```json
{
  "id": 1,
  "descricao": "Rampa de acesso quebrada na plataforma",
  "categoria": "RAMPA",
  "status": "ABERTO",
  "prioridade": "ALTA",
  "fotoUrl": "https://exemplo.com/fotos/rampa.jpg",
  "descricaoFoto": "Rampa de concreto com a borda quebrada e um degrau de 10 cm",
  "criadoEm": "2026-11-09T10:15:30.123456",
  "usuarioId": 1,
  "usuarioNome": "Ana Silva",
  "localId": 1,
  "localNome": "Terminal Bandeira"
}
```

`PUT /api/v1/relatos-acessibilidade/1`

```json
{
  "descricao": "Rampa de acesso quebrada na plataforma",
  "categoria": "RAMPA",
  "status": "EM_ANALISE",
  "fotoUrl": "https://exemplo.com/fotos/rampa.jpg",
  "descricaoFoto": "Rampa de concreto com a borda quebrada e um degrau de 10 cm"
}
```

`POST /api/v1/validacoes-relato`

```json
{
  "situacao": "AINDA_EXISTE",
  "comentario": "Passei hoje e a rampa continua quebrada",
  "relatoId": 1,
  "usuarioId": 2
}
```

### Respostas de erro

Erros de validação (`400`) informam a mensagem de cada campo:

```json
{
  "timestamp": "2026-11-09T10:20:00.000000",
  "status": 400,
  "error": "Bad Request",
  "message": "Dados inválidos",
  "path": "/api/v1/usuarios",
  "fields": {
    "nome": "O nome é obrigatório",
    "email": "Informe um e-mail válido"
  }
}
```

Conflitos (`409`) usam o mesmo formato, com `fields` nulo. O `404` volta com o corpo vazio.

## Executar localmente

Pré-requisitos: JDK 17 ou superior e Docker.

1. Suba o SQL Server em um container e crie o banco `gs_acessibilidade`:

```powershell
$env:DB_PWD = Read-Host "Senha do SQL Server local"

docker run -d --name sqlserver-gs `
  -e "ACCEPT_EULA=Y" `
  -e "MSSQL_SA_PASSWORD=$env:DB_PWD" `
  -p 1433:1433 `
  mcr.microsoft.com/mssql/server:2022-latest

docker exec sqlserver-gs /opt/mssql-tools18/bin/sqlcmd `
  -S localhost -U sa -P $env:DB_PWD -C `
  -Q "IF DB_ID('gs_acessibilidade') IS NULL CREATE DATABASE gs_acessibilidade"
```

2. Rode a aplicação no profile `dev`, que é o padrão:

```powershell
.\mvnw.cmd spring-boot:run
```

3. Abra `http://localhost:8080/`.

No profile `dev` o Hibernate cria e atualiza as tabelas. O arquivo [.env.example](.env.example) lista as variáveis usadas; o Spring não lê esse arquivo, então os valores precisam estar definidos no terminal.

### Variáveis de ambiente

| Variável | `dev` | `prd` |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` (padrão) | `prd` |
| `DB_SERVER_URL` | padrão `localhost` | obrigatória |
| `DB_SERVER_PORT` | padrão `1433` | padrão `1433` |
| `DB_SCHEMA` | padrão `gs_acessibilidade` | obrigatória (nome do banco) |
| `DB_USER` | padrão `sa` | obrigatória |
| `DB_PWD` | obrigatória | obrigatória, guardada como secret no Azure |
| `DB_DDL_AUTO` | — | padrão `update` |
| `CORS_ALLOWED_ORIGIN_PATTERNS` | padrão `http://localhost:*` | origens permitidas |

O profile `prd` conecta com criptografia (`encrypt=true`) e valida o certificado do servidor.

## Testes

```powershell
.\mvnw.cmd clean verify
```

São testes de integração com MockMvc sobre um banco H2 em memória, um arquivo por recurso. Cada um cobre o CRUD completo e os casos de `400`, `404` e `409`. Não é preciso ter SQL Server para rodar.

## Docker

```powershell
docker build -t gs-acessibilidade:local .
```

Para rodar a imagem contra o SQL Server local, o host do banco é `host.docker.internal`, porque dentro do container `localhost` aponta para o próprio container:

```powershell
docker run --rm -p 8080:8080 `
  -e DB_SERVER_URL=host.docker.internal `
  -e DB_PWD=$env:DB_PWD `
  gs-acessibilidade:local
```

## Entrega contínua

O workflow [continuous_delivery.yml](.github/workflows/continuous_delivery.yml) roda a cada `push` na `main`, e também pode ser disparado pela aba Actions:

1. executa `./mvnw -B clean verify`;
2. gera as tags `v<número da execução>`, o SHA curto do commit e `latest`;
3. constrói a imagem e publica em `270506lu/gs-acessibilidade` no Docker Hub.
