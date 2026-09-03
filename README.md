# 🐾 PetFlow API

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Oracle](https://img.shields.io/badge/Oracle-Database-red.svg)](https://www.oracle.com/database/)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI-85EA2D.svg)](https://swagger.io/)
[![Flyway](https://img.shields.io/badge/Flyway-Migrations-orange.svg)](https://flywaydb.org/)

## 📋 Sumário

- [Sobre o Projeto](#sobre-o-projeto)
- [Equipe](#equipe)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Arquitetura do Projeto](#arquitetura-do-projeto)
- [Requisitos da Sprint 3](#requisitos-da-sprint-3)
- [Visão de Domínio](#visão-de-domínio)
- [Endpoints da API](#endpoints-da-api)
- [Banco de Dados](#banco-de-dados)
- [Swagger / OpenAPI](#swagger--openapi)
- [Como Executar](#como-executar)
- [Credenciais de Teste](#credenciais-de-teste)
- [Vídeo de Demonstração](#vídeo-de-demonstração)
- [Observações Finais](#observações-finais)

---

## 📌 Sobre o Projeto

O **PetFlow** é uma aplicação web completa desenvolvida em Java com Spring Boot para gerenciamento de saúde preventiva pet. A solução gamifica o cuidado com o pet: eventos de saúde concluídos geram pontos para o tutor, que podem ser trocados por cupons de desconto em clínicas parceiras.

A aplicação conta com uma interface web responsiva, autenticação JWT com dois perfis de usuário (ADMIN e TUTOR), versionamento de banco de dados com Flyway e documentação automática via Swagger/OpenAPI.

---

## Equipe

| Nome | RM |
|---|---:|
| Lucas Grillo Alcântara | 561413 |
| Pietro Ferreira Gomes Abrahamian | 561469 |
| Pedro Peres Benitez | 561792 |
| Lucca Ramos Mussumecci | 562027 |

**Turma:** 2TDSPX

---

## Tecnologias Utilizadas

- **Java 21** - Linguagem de programação
- **Spring Boot 4.0.6** - Framework principal
- **Spring Data JPA** - Persistência de dados
- **Spring Security** - Autenticação e autorização (JWT)
- **Spring Cache** - Cacheamento de dados
- **Spring Web MVC** - API RESTful
- **Spring Validation** - Validação de dados
- **Flyway** - Controle de versão do banco de dados
- **Oracle Database** - Banco de dados principal
- **JWT (java-jwt)** - Tokens de autenticação
- **Lombok** - Redução de código boilerplate
- **Swagger/OpenAPI** - Documentação interativa da API
- **Maven** - Gerenciamento de dependências

---

## Arquitetura do Projeto

O projeto segue uma arquitetura em camadas bem definida:

```text
src/main/java/br/com/petflow/petflow_api/
├── config/          # Configurações (Security, Cache, Swagger)
├── controller/      # Endpoints REST
├── dto/             # Data Transfer Objects
├── entity/          # Entidades JPA
├── enums/           # Enumeradores
├── exception/       # Tratamento de exceções
├── repository/      # Repositórios JPA
├── security/        # Configurações de segurança (JWT)
├── service/         # Regras de negócio
└── PetflowApiApplication.java  # Classe principal
```

---

## Requisitos da Sprint 3

### 1. Frontend (30 pontos)

A interface web do PetFlow não está neste repositório. Ela está em um projeto separado, disponível em:

https://github.com/lgaxd/petflow-front-api-java

Para testar a aplicação completa, a API deve ser iniciada primeiro e, em seguida, o frontend deve ser executado no repositório do front-end. A aplicação web será aberta em http://localhost:5173/.

### 2. Flyway (20 pontos)

O versionamento do banco de dados é gerenciado pelo Flyway:
- ✅ **V1__Create_Tables.sql** - Criação de todas as tabelas
- ✅ **V2__Seed_Data.sql** - Carga inicial de dados (admin, tutor, clínicas, planos, cupons)
- ✅ Migrações aplicadas automaticamente na inicialização

### 3. Spring Security (30 pontos)

Autenticação e autorização com JWT:
- ✅ Dois perfis de usuário: **ADMIN** e **TUTOR**
- ✅ Proteção de rotas baseada no perfil
- ✅ Login via `/auth/login` com geração de token JWT
- ✅ Filtro de autenticação para validação do token
- ✅ Tratamento de erros 401 e 403 em formato JSON

### 4. Funcionalidades Completas (20 pontos)

#### Fluxos do Sistema:
- ✅ **Admin:** Gerenciamento de clínicas, planos e cupons (CRUD)
- ✅ **Tutor:** Cadastro de pets, eventos de saúde, assinaturas e resgate de cupons
- ✅ Validações básicas em formulários e dados

---

## Visão de Domínio

### 👤 Tutores
Gerencia os responsáveis pelos pets, com autenticação via e-mail e senha.

### 🐾 Pets
Armazena os dados principais dos animais cadastrados e seu vínculo com o tutor.

### 🏥 Clínicas
Representa as clínicas veterinárias parceiras do sistema.

### 📄 Planos
Controla os planos de saúde/prevenção ligados às clínicas.

### 📅 Assinaturas
Registra a contratação de planos por pets.

### ❤️ Eventos de Saúde
Armazena o histórico clínico e preventivo dos pets.

### 🎟️ Cupons
Gerencia cupons emitidos para resgate.

### 🎫 Resgates
Registra o uso de cupons pelos tutores.

## 🎮 Gamificação

O PetFlow possui um sistema de gamificação onde tutores acumulam pontos ao realizar ações como cadastrar pets, registrar eventos de saúde e assinar planos. Esses pontos podem ser trocados por cupons de desconto em clínicas parceiras.

---

## Endpoints da API

> **Nota:** A documentação completa com exemplos de requisição e resposta está disponível via Swagger.

### 👤 Tutores

| Método | Endpoint | Descrição | Permissão |
|--------|----------|-----------|-----------|
| POST | `/tutors` | Cadastrar novo tutor | Público |
| GET | `/tutors` | Listar tutores | ADMIN |
| GET | `/tutors/{id}` | Buscar tutor por ID | ADMIN |
| PUT | `/tutors/{id}` | Atualizar tutor | ADMIN |
| DELETE | `/tutors/{id}` | Remover tutor | ADMIN |

### 🔐 Autenticação

| Método | Endpoint | Descrição | Permissão |
|--------|----------|-----------|-----------|
| POST | `/auth/login` | Login e geração de token JWT | Público |

### 🐾 Pets

| Método | Endpoint | Descrição | Permissão |
|--------|----------|-----------|-----------|
| POST | `/pets` | Cadastrar pet | TUTOR/Authenticated |
| GET | `/pets` | Listar pets (com filtros) | Authenticated |
| GET | `/pets/{id}` | Buscar pet por ID | Authenticated |
| PUT | `/pets/{id}` | Atualizar pet | Authenticated |
| DELETE | `/pets/{id}` | Remover pet | Authenticated |

### 🏥 Clínicas

| Método | Endpoint | Descrição | Permissão |
|--------|----------|-----------|-----------|
| POST | `/clinics` | Cadastrar clínica | ADMIN |
| GET | `/clinics` | Listar clínicas | Authenticated |
| GET | `/clinics/{id}` | Buscar clínica por ID | Authenticated |
| PUT | `/clinics/{id}` | Atualizar clínica | ADMIN |
| DELETE | `/clinics/{id}` | Remover clínica | ADMIN |

### 📄 Planos

| Método | Endpoint | Descrição | Permissão |
|--------|----------|-----------|-----------|
| POST | `/plans` | Criar plano | ADMIN |
| GET | `/plans` | Listar planos | Authenticated |
| GET | `/plans/{id}` | Buscar plano por ID | Authenticated |
| PUT | `/plans/{id}` | Atualizar plano | ADMIN |
| DELETE | `/plans/{id}` | Remover plano | ADMIN |

### 📅 Assinaturas

| Método | Endpoint | Descrição | Permissão |
|--------|----------|-----------|-----------|
| POST | `/subscriptions` | Criar assinatura | Authenticated |
| GET | `/subscriptions` | Listar assinaturas | Authenticated |
| GET | `/subscriptions/{id}` | Buscar assinatura por ID | Authenticated |
| PUT | `/subscriptions/{id}/status` | Atualizar status | Authenticated |
| DELETE | `/subscriptions/{id}` | Remover assinatura | Authenticated |

### ❤️ Eventos de Saúde

| Método | Endpoint | Descrição | Permissão |
|--------|----------|-----------|-----------|
| POST | `/health-events` | Registrar evento | Authenticated |
| GET | `/health-events` | Listar eventos | Authenticated |
| GET | `/health-events/{id}` | Buscar evento por ID | Authenticated |
| PUT | `/health-events/{id}` | Atualizar evento | Authenticated |
| DELETE | `/health-events/{id}` | Remover evento | Authenticated |

### 🎟️ Cupons

| Método | Endpoint | Descrição | Permissão |
|--------|----------|-----------|-----------|
| POST | `/coupons` | Gerar cupom | ADMIN |
| GET | `/coupons` | Listar cupons | Authenticated |
| GET | `/coupons/{id}` | Buscar cupom por ID | Authenticated |
| PUT | `/coupons/{id}/status` | Atualizar status | ADMIN |
| DELETE | `/coupons/{id}` | Remover cupom | ADMIN |

### 🎫 Resgates

| Método | Endpoint | Descrição | Permissão |
|--------|----------|-----------|-----------|
| GET | `/redeems` | Listar resgates | Authenticated |
| GET | `/redeems/{id}` | Buscar resgate por ID | Authenticated |

### Endpoints de Gamificação

| Método | Endpoint | Descrição | Permissão |
|--------|----------|-----------|-----------|
| GET | `/gamification/points` | Retorna os pontos do tutor logado e histórico | TUTOR |
| GET | `/gamification/pets/{petId}/risk` | Retorna o score de risco de um pet | TUTOR |
| GET | `/gamification/coupons/available` | Lista cupons disponíveis para resgate | TUTOR |
| POST | `/gamification/redeem` | Resgata um cupom usando pontos | TUTOR |

O resgate valida o status e a validade do cupom e compara o custo com o saldo atual do tutor. Quando os pontos são insuficientes, a API retorna HTTP 422 com o código `INSUFFICIENT_POINTS` e os valores `availablePoints` e `requiredPoints`; o cupom não é alterado. Cupons realmente expirados retornam o código `EXPIRED_COUPON`.

---

## Banco de Dados

O projeto utiliza **Oracle Database** como banco principal.

### Migrações Flyway

| Arquivo | Descrição |
|---------|-----------|
| `V1__Create_Tables.sql` | Criação de todas as tabelas (SPECIES, EVENT_TYPE, REWARD_ACTION, RISK_LEVEL, TUTOR, CLINIC, PLAN, PARTNER_DISCOUNT, COUPON_TEMPLATE, COUPON, PET, HEALTH_EVENT, SUBSCRIPTION, REDEEM) |
| `V2__Seed_Data.sql` | Carga inicial de dados de referência e usuários de demonstração |

### Modelo de Dados

O modelo foi estruturado com JPA e relacionamentos entre entidades para garantir integridade e organização dos dados.

O relacionamento entre `HEALTH_EVENT` e `EVENT_TYPE` é mapeado como associação JPA para a coluna `EVENT_TYPE_ID`, conforme a chave estrangeira definida no SQL Oracle. O mesmo padrão é usado para `REWARD_POINT`, `REWARD_ACTION`, `TUTOR` e as demais entidades relacionadas.

---

## Swagger / OpenAPI

A documentação interativa da API está disponível em:

```text
/swagger-ui/index.html
```

---

## Como Executar

### Pré-requisitos

- Java 21
- Maven 3.6+
- Oracle Database

### Execução com Oracle Database

1. **Configure as credenciais do banco** no arquivo `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
```

2. **Execute a aplicação:**

```bash
./mvnw clean compile
./mvnw spring-boot:run
```

Ao iniciar, o Flyway aplica as migrations pendentes. Em um schema que já contenha tabelas antigas, ele não reexecuta automaticamente seeds já marcados como aplicados; nesse caso, limpe o schema de desenvolvimento ou insira os dados de referência conforme `V2__Seed_Data.sql` antes de testar.

### Fluxo recomendado de teste

1. Inicie a API localmente.
2. Inicie o frontend no repositório separado.
3. Acesse a interface em http://localhost:5173/.
4. Faça login e use o token retornado para acessar o painel correspondente.
5. No painel do tutor, cadastre um pet e confirme a pontuação na aba **Gamificação**.
6. Registre um evento de saúde com status `REALIZADO` e confirme o novo lançamento de pontos.
7. Crie uma assinatura ativa e confirme o lançamento de `ASSINATURA_ATIVA`.
8. Em **Cupons & Resgate**, tente um cupom mais caro que o saldo. A mensagem deve aparecer nessa seção, informando o saldo disponível e o custo necessário.
9. Com saldo suficiente, resgate o cupom e confirme o registro em **Meus cupons resgatados**.

### Teste rápido (frontend em repositório separado)

- A API deste repositório deve ser iniciada primeiro.
- O frontend deve ser executado a partir do repositório do projeto web em: https://github.com/lgaxd/petflow-front-api-java
- Após iniciar o frontend, acesse http://localhost:5173/ e realize os testes usando as credenciais listadas em **Credenciais de Teste**.
- As credenciais de acesso ao banco já estão definidas em [src/main/resources/application.properties](src/main/resources/application.properties), portanto não é necessário configurar um banco adicional para testes rápidos.

### Opcional: usar Oracle local com Flyway

- Se preferir executar um Oracle Database localmente, inicie o serviço do banco e atualize as credenciais em [src/main/resources/application.properties](src/main/resources/application.properties). Ao iniciar a aplicação, o Flyway aplicará automaticamente as migrations em `src/main/resources/db/migration` para criar as tabelas e popular os dados necessários para teste.

---

## Credenciais de Teste

### ADMIN
- **Email:** `admin@petflow.com`
- **Senha:** `Admin@123`

### TUTOR
- **Email:** `maria@petflow.com`
- **Senha:** `Tutor@123`

*Esses usuários são criados automaticamente pelo Flyway (V2__Seed_Data.sql).*

---

## Vídeo de Demonstração

[Link para o vídeo de demonstração da aplicação](https://youtu.be/SEU_LINK_AQUI)

*O vídeo apresenta todas as funcionalidades da aplicação, incluindo:*

- ✅ Autenticação com JWT (Admin e Tutor)
- ✅ Painel do Administrador (CRUD de clínicas, planos e cupons)
- ✅ Painel do Tutor (cadastro de pets, eventos de saúde, assinaturas e resgate)
- ✅ Documentação Swagger
- ✅ Controle de versão do banco com Flyway

---

## 📄 Documentação Complementar

O repositório inclui:

- ✅ **PetFlow API.postman_collection.json** - Coleção completa de requisições Postman
- ✅ **Scripts Flyway** - Migrações do banco de dados
- ✅ **Swagger/OpenAPI** - Documentação interativa da API
- ✅ **README completo** - Instruções de instalação e uso

---

## Observações Finais

Esta versão do PetFlow foi desenvolvida para atender aos requisitos da **Sprint 3 de Java Advanced**:

- **Frontend:** Interface web completa com HTML, CSS e JavaScript
- **Flyway:** Controle de versão do banco de dados com migrações
- **Spring Security:** Autenticação JWT com dois perfis (ADMIN/TUTOR)
- **Funcionalidades:** Fluxos completos de negócio com validações

Desenvolvido como parte do Challenge 2TDSPX - FIAP

---