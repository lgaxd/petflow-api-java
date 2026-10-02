# Oracle JSON e MongoDB

Os arquivos daqui são derivados do schema atual em `src/main/resources/db/migration/V1__Create_Tables.sql`. Eles não alteram o banco Oracle nem adicionam MongoDB à API.

## Procedures

`PKG_PETFLOW_GAMIFICATION` é aplicado pela migration V3. `GamificationProcedureService` chama consulta de saldo, concessão idempotente e resgate transacional. O package não executa `COMMIT`; a transação Spring controla o resultado.

| Código Oracle | Situação | Mapeamento HTTP/API |
|---|---|---|
| `ORA-20081` | Saldo insuficiente | 422 `INSUFFICIENT_POINTS`, com saldo/custo em `details`. |
| `ORA-20082` | Cupom expirado | 422 `EXPIRED_COUPON`, com código/validade em `details`. |
| `ORA-20083` | Cupom indisponível | 422 `BUSINESS_RULE_VIOLATION`. |
| `ORA-20084` | Cupom/tutor não encontrado | 404 `EntityNotFoundException`. |
| `ORA-20101` | Referência de recompensa não encontrada | 404 `EntityNotFoundException`. |
| `ORA-20102` | Evento não realizado | 422 `INVALID_EVENT_STATUS`. |
| `ORA-20103` | Tipo de referência inválido | 422 `INVALID_REWARD_REFERENCE`. |
| `ORA-20104` | Tutor divergente da referência | 403 `AccessDeniedException`. |

## Exportação

Com SQLcl ou SQL*Plus instalado, conecte ao mesmo schema Oracle configurado no `.env` e execute:

```text
@database/01_PetFlow_Export_JSON.sql
```

O script grava seis JSON arrays no diretório corrente: `tutores_pets.json`, `eventos_saude.json`, `carteira_recompensas.json`, `assinaturas.json`, `catalogo_parceiros.json` e `niveis_risco.json`. Os lookups de espécie, tipo de evento e ação de recompensa são embutidos nos documentos que os usam. `PASSWORD_HASH` não é exportado.

O schema desta API não possui `ADDRESS` nem `RISK_SCORE`; por isso esses dados não aparecem no export. O risco retornado pela API é calculado a partir dos dados atuais do pet e não é um histórico relacional.

## Modelo MongoDB

Cada coleção favorece as leituras do seu fluxo: pets dentro do documento do tutor; tipo de evento dentro do evento de saúde; plano/clínica dentro da assinatura; pontos e resgates dentro da carteira; templates/cupons dentro do catálogo do parceiro. Os IDs Oracle são preservados para rastreabilidade. Arrays agrupam conjuntos naturalmente pequenos ou consultados como histórico do tutor; eventos e assinaturas ficam em documentos próprios para evitar arrays potencialmente ilimitados.

Execute o script de coleções e índices:

```text
mongosh database/02_PetFlow_MongoDB_Import.js
```

Esse script aplica validadores e índices. Em uma reimportação, limpa os documentos dessas seis coleções, mas preserva as coleções, validadores e índices. Faça a exportação Oracle imediatamente antes da importação.

```powershell
mongoimport --db petflow_nosql --collection tutores_pets --file tutores_pets.json --jsonArray
mongoimport --db petflow_nosql --collection eventos_saude --file eventos_saude.json --jsonArray
mongoimport --db petflow_nosql --collection carteira_recompensas --file carteira_recompensas.json --jsonArray
mongoimport --db petflow_nosql --collection assinaturas --file assinaturas.json --jsonArray
mongoimport --db petflow_nosql --collection catalogo_parceiros --file catalogo_parceiros.json --jsonArray
mongoimport --db petflow_nosql --collection niveis_risco --file niveis_risco.json --jsonArray
```

O arquivo [examples/tutores_pets.example.json](examples/tutores_pets.example.json) mostra a forma de um documento e usa dados fictícios. Os JSONs exportados contêm dados pessoais e de saúde: não os publique nem os versione sem anonimização.

Índices principais: tutor/email únicos; nome do pet para busca embutida; evento por ID único e pet/data; assinatura por ID único e pet/status; parceiro/nível por ID único. O saldo da carteira não é indexado por padrão porque consultas comuns partem do tutor; acrescente índices com base no padrão real de busca.