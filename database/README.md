# Scripts de banco de dados (Oracle e MongoDB)

Esta pasta reúne os scripts da disciplina de **Banco de Dados** usados com a mesma base relacional da API Java. A **API Java não depende deles para funcionar**: o schema que ela usa é criado pelo Flyway (`src/main/resources/db/migration`, V1 a V3). Os scripts abaixo complementam esse schema com empacotamento PL/SQL, auditoria e a carga NoSQL.

## Dois cenários de banco

| Cenário | Schema | Como obter |
|---|---|---|
| **Oracle vazio** (ex.: o do professor, via Docker) | Tabelas do domínio, seed e pacote de gamificação | Subir a API: o Flyway executa V1, V2 e V3 sozinho. Veja [Oracle local](../README.md#oracle-local-sem-usar-o-banco-da-fiap) |
| **Oracle da FIAP do grupo** | O acima **mais** `ADDRESS`, `RISK_SCORE`, `LOG_ERROS`, `LOG_AUDITORIA_HEALTH_EVENT` e as procedures da Sprint 3 | Criado pelo script `2TDSPX_CodigoSql_PetFlow.sql` (Sprint 3), entregue no zip da disciplina de Banco |

O script `01` pressupõe o segundo cenário (ele usa `LOG_ERROS`, `LOG_AUDITORIA_HEALTH_EVENT`, `ADDRESS` e `RISK_SCORE`). **Em um Oracle criado só pelo Flyway ele não executa**, porque essas tabelas só existem no script da Sprint 3. O script da Sprint 3 não está neste repositório porque a primeira seção dele apaga todas as tabelas (`DROP`).

## Arquivos

| Ordem | Arquivo | O que faz |
|---|---|---|
| 1 | `01_PetFlow_Sprint4_Pacotes.sql` | Empacota as procedures e functions em `PKG_PETFLOW_CADASTRO`, `PKG_PETFLOW_RELATORIO` e `PKG_PETFLOW_AUDITORIA`, e recria a trigger `TRG_AUDIT_HEALTH_EVENT` (objeto de schema, chamando o pacote de auditoria). **Altera o banco.** |
| 2 | `02_PetFlow_Sprint4_Export_JSON.sql` | Gera 6 arquivos JSON denormalizados com `JSON_OBJECT`/`JSON_ARRAYAGG`: `tutores_pets`, `eventos_saude`, `carteira_recompensas`, `assinaturas`, `score_risco` e `catalogo_parceiros`. Só leitura. |
| 3 | `03_PetFlow_Sprint4_MongoDB_Import.js` | Cria o banco `petflow_nosql`, as coleções com validadores `$jsonSchema` e os índices, e documenta a estrutura. |
| 4 | `05_PetFlow_Sprint4_MongoDB_Load.js` | Carrega os 6 JSON nas coleções (idempotente; use `DATA_DIR` para indicar a pasta). |

O pacote de gamificação usado pela API (`PKG_PETFLOW_GAMIFICATION`) **não está aqui**: é a migração [`V3__Create_Gamification_Package.sql`](../src/main/resources/db/migration/V3__Create_Gamification_Package.sql), aplicada pelo Flyway. Ele é chamado por `GamificationProcedureService` e converte os erros Oracle em respostas HTTP:

| Código Oracle | Situação | Resposta da API |
|---|---|---|
| `ORA-20081` | Saldo insuficiente | 422 `INSUFFICIENT_POINTS` (com `availablePoints` e `requiredPoints`) |
| `ORA-20082` | Cupom expirado | 422 `EXPIRED_COUPON` |
| `ORA-20083` | Cupom indisponível | 422 `BUSINESS_RULE_VIOLATION` |
| `ORA-20084` | Cupom ou tutor não encontrado | 404 |
| `ORA-20101` | Referência de recompensa não encontrada | 404 |
| `ORA-20102` | Evento não realizado | 422 `INVALID_EVENT_STATUS` |
| `ORA-20103` | Tipo de referência inválido | 422 `INVALID_REWARD_REFERENCE` |
| `ORA-20104` | Tutor divergente da referência | 403 |

## Execução

**Pacotes (Oracle SQL Developer ou SQLcl):** conectado ao schema do projeto, execute o `01` inteiro com *Run Script* (F5).

**Exportação JSON** (SQLcl ou SQL*Plus; os arquivos são gravados na pasta corrente):

```text
@02_PetFlow_Sprint4_Export_JSON.sql
```

**MongoDB** (`mongosh`), a partir da pasta com os JSON:

```bash
mongosh --file 03_PetFlow_Sprint4_MongoDB_Import.js
mongosh --file 05_PetFlow_Sprint4_MongoDB_Load.js        # ou: DATA_DIR=/caminho/dos/json mongosh --file ...
```

## Modelo MongoDB

Cada coleção agrupa o que é consultado junto: pets dentro do tutor; tipo de evento, pet e clínica dentro do evento; plano e clínica dentro da assinatura; pontos e resgates dentro da carteira; templates e cupons dentro do catálogo do parceiro. Os IDs Oracle são preservados para rastreabilidade. Tabelas de domínio (`SPECIES`, `EVENT_TYPE`, `REWARD_ACTION`) ficam embutidas nos documentos que as usam. Eventos e assinaturas têm coleções próprias para evitar arrays ilimitados. `PASSWORD_HASH` não é exportado.

O arquivo [examples/tutores_pets.example.json](examples/tutores_pets.example.json) mostra a forma de um documento com dados fictícios.

## Segurança dos dados

Os JSON exportados contêm dados pessoais e de saúde. Por isso `database/*.json` está no `.gitignore` (somente `examples/` é versionado). Não os publique sem anonimização.
