# 🐾 PetFlow API

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Oracle](https://img.shields.io/badge/Oracle-Database-red.svg)](https://www.oracle.com/database/)

## 📋 Sumário

- [Sobre o Projeto](#-sobre-o-projeto)
- [Desenvolvedores](#-desenvolvedores)
- [Objetivo do Challenge](#-objetivo-do-challenge)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Arquitetura do Projeto](#-arquitetura-do-projeto)
- [Funcionalidades Implementadas](#-funcionalidades-implementadas)
- [Banco de Dados](#-banco-de-dados)
- [Documentação Swagger](#-documentação-swagger)
- [Configuração do Projeto](#-configuração-do-projeto)
- [Testes da API](#-testes-da-api)
- [Endpoints da API](#-endpoints-da-api)
- [Conceitos Aplicados](#-conceitos-aplicados)
- [Documentação Complementar](#-documentação-complementar)

## 📌 Sobre o Projeto

O **PetFlow** é uma API REST desenvolvida em Java com Spring Boot para gerenciamento de saúde preventiva pet. A plataforma oferece um ecossistema completo que conecta tutores, pets, clínicas veterinárias, planos de saúde, sistema de gamificação (pontuação e recompensas), cupons de desconto e análise de risco animal.

O objetivo do projeto é centralizar o acompanhamento da saúde dos pets, facilitar a comunicação entre tutores e clínicas veterinárias e incentivar ações preventivas através de um sistema de recompensas.

## 👨‍💻 Desenvolvedores

| Nome                          | RM     |
|-------------------------------|--------|
| Lucas Grillo Alcântara        | 561413 |
| Pietro Ferreira Gomes Abrahamian | 561469 |
| Pedro Peres Benitez           | 561792 |
| Lucca Ramos Mussumecci        | 562027 |

**Turma:** 2TDSPX

## 🎯 Objetivo do Challenge

Desenvolver uma solução utilizando Java e Spring Boot capaz de:

- Persistir dados em banco relacional
- Gerenciar informações de saúde pet
- Implementar boas práticas de APIs REST
- Utilizar Programação Orientada a Objetos
- Aplicar conceitos de arquitetura em camadas
- Utilizar JPA e relacionamentos entre entidades
- Garantir validações e tratamento de exceções
- Disponibilizar documentação da API

## 🛠️ Tecnologias Utilizadas

- **Java 21**
- **Spring Boot**
- **Spring Data JPA**
- **Spring Validation**
- **Spring Cache**
- **Oracle Database**
- **Lombok**
- **Swagger / OpenAPI**
- **Maven**

## 🧱 Arquitetura do Projeto

O projeto segue uma arquitetura em camadas bem definida:

```
src/main/java/br/com/petflow/petflow_api/
├── config/          # Configurações do Swagger
├── controller/      # Controladores REST
├── dto/             # Data Transfer Objects
├── entity/          # Entidades JPA
├── exception/       # Tratamento de exceções
├── repository/      # Repositórios de dados
├── service/         # Lógica de negócio
└── PetflowApiApplication.java  # Classe principal
```

## 📚 Funcionalidades Implementadas

### ✅ Requisitos Técnicos Atendidos

- Bean Validation
- Paginação de resultados
- Ordenação de resultados
- Busca com parâmetros
- Cache com `@Cacheable`
- Tratamento global de exceções
- Utilização de DTOs
- Documentação Swagger/OpenAPI
- Relacionamentos JPA
- API RESTful

### 🔥 Funcionalidades da Plataforma

#### 👤 Tutores
- Cadastro de tutores
- Atualização de dados
- Histórico de pets
- Histórico de pontos
- Histórico de resgates

#### 🐾 Pets
- Cadastro de pets
- Histórico médico
- Histórico de eventos
- Histórico de risco
- Controle de assinaturas

#### 🏥 Clínicas
- Cadastro de clínicas
- Gerenciamento de planos
- Controle de descontos parceiros

#### ❤️ Sistema de Saúde Preventiva
- Registro de eventos de saúde
- Tipos de eventos
- Scores de risco
- Níveis de risco

#### 🎁 Sistema de Recompensas
- Pontuação por ações
- Resgate de cupons
- Cupons de desconto
- Templates de cupons

## 🗃️ Banco de Dados

O projeto utiliza:

- **Oracle Database** para persistência relacional em produção

Os relacionamentos foram implementados utilizando anotações JPA:

- `@OneToMany`
- `@ManyToOne`
- `@OneToOne`
- `@ManyToMany`

## 📖 Documentação Swagger

A documentação interativa da API pode ser acessada em:

```
/swagger-ui/index.html
```

## ⚙️ Configuração do Projeto

### Pré-requisitos

- Java 21
- Maven 3.6+

### Instalação

1. **Clonar o repositório:**
   ```bash
   git clone https://github.com/seu-repositorio/petflow-api.git
   ```

2. **Entrar na pasta do projeto:**
   ```bash
   cd petflow-api
   ```

3. **Executar o projeto:**
   ```bash
   ./mvnw spring-boot:run
   ```

### Configuração do `application.properties`

#### Para H2 Database
```properties
spring.datasource.url=jdbc:h2:mem:petflow
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

#### Para Oracle Database (Produção)
Configure as variáveis de ambiente:
- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`

## 🧪 Testes da API

Os endpoints foram testados utilizando:

- **Postman**
- **Swagger UI**

Os arquivos de coleção das requisições devem ser disponibilizados na pasta `/documentos`.

## 📦 Endpoints da API

### 🐾 Pets

| Método  | Endpoint                  | Descrição              |
|---------|---------------------------|------------------------|
| GET     | `/pets`                   | Listar pets            |
| GET     | `/pets/{id}`              | Buscar pet por ID      |
| POST    | `/pets`                   | Cadastrar pet          |
| PUT     | `/pets/{id}`              | Atualizar pet          |
| DELETE  | `/pets/{id}`              | Remover pet            |
| GET     | `/pets/{id}/subscriptions` | Histórico de assinaturas |
| GET     | `/pets/{id}/risk-scores`  | Histórico de risco     |
| GET     | `/pets/{id}/health-events`| Histórico de eventos   |

### 👤 Tutores

| Método  | Endpoint              | Descrição              |
|---------|-----------------------|------------------------|
| GET     | `/tutors`             | Listar tutores         |
| GET     | `/tutors/{id}`        | Buscar tutor por ID    |
| POST    | `/tutors`             | Cadastrar tutor        |
| PUT     | `/tutors/{id}`        | Atualizar tutor        |
| DELETE  | `/tutors/{id}`        | Remover tutor          |
| GET     | `/tutors/{id}/redeems`| Histórico de resgates  |
| GET     | `/tutors/{id}/points` | Histórico de pontos    |
| GET     | `/tutors/{id}/pets`   | Pets do tutor          |

### 🏥 Clínicas

| Método  | Endpoint              | Descrição              |
|---------|-----------------------|------------------------|
| GET     | `/clinics`            | Listar clínicas        |
| GET     | `/clinics/{id}`       | Buscar clínica por ID  |
| POST    | `/clinics`            | Cadastrar clínica      |
| PUT     | `/clinics/{id}`       | Atualizar clínica      |
| DELETE  | `/clinics/{id}`       | Remover clínica        |
| GET     | `/clinics/{id}/plans` | Planos da clínica      |

### 📄 Planos

| Método  | Endpoint      | Descrição          |
|---------|---------------|--------------------|
| GET     | `/plans`      | Listar planos      |
| GET     | `/plans/{id}` | Buscar plano por ID|
| POST    | `/plans`      | Criar plano        |
| PUT     | `/plans/{id}` | Atualizar plano    |
| DELETE  | `/plans/{id}` | Remover plano      |

### ❤️ Eventos de Saúde

| Método  | Endpoint              | Descrição                  |
|---------|-----------------------|----------------------------|
| GET     | `/health-events`      | Listar eventos de saúde    |
| GET     | `/health-events/{id}` | Buscar evento por ID       |
| POST    | `/health-events`      | Criar evento de saúde      |
| PUT     | `/health-events/{id}` | Atualizar evento de saúde  |
| DELETE  | `/health-events/{id}` | Remover evento de saúde    |

### ⚠️ Scores de Risco

| Método  | Endpoint              | Descrição              |
|---------|-----------------------|------------------------|
| GET     | `/risk-scores`        | Listar scores de risco |
| GET     | `/risk-scores/{id}`   | Buscar score por ID    |
| POST    | `/risk-scores`        | Criar score de risco   |

### 📊 Níveis de Risco

| Método  | Endpoint                      | Descrição                  |
|---------|-------------------------------|----------------------------|
| GET     | `/risk-levels`                | Listar níveis de risco     |
| GET     | `/risk-levels/{id}`           | Buscar nível por ID        |
| POST    | `/risk-levels`                | Criar nível de risco       |
| PUT     | `/risk-levels/{id}`           | Atualizar nível de risco   |
| DELETE  | `/risk-levels/{id}`           | Remover nível de risco     |
| GET     | `/risk-levels/search/name`    | Buscar por nome            |

### 🎟️ Cupons

| Método  | Endpoint          | Descrição          |
|---------|-------------------|--------------------|
| GET     | `/coupons`        | Listar cupons      |
| GET     | `/coupons/{id}`   | Buscar cupom por ID|
| POST    | `/coupons`        | Criar cupom        |
| PUT     | `/coupons/{id}`   | Atualizar cupom    |
| DELETE  | `/coupons/{id}`   | Remover cupom      |

### 🎁 Templates de Cupom

| Método  | Endpoint                  | Descrição                  |
|---------|---------------------------|----------------------------|
| GET     | `/coupon-templates`       | Listar templates de cupom  |
| GET     | `/coupon-templates/{id}`  | Buscar template por ID     |
| POST    | `/coupon-templates`       | Criar template de cupom    |
| PUT     | `/coupon-templates/{id}`  | Atualizar template de cupom|
| DELETE  | `/coupon-templates/{id}`  | Remover template de cupom  |

### 🪙 Pontos de Recompensa

| Método  | Endpoint              | Descrição                  |
|---------|-----------------------|----------------------------|
| GET     | `/reward-points`      | Listar pontos de recompensa|
| GET     | `/reward-points/{id}` | Buscar ponto por ID        |
| POST    | `/reward-points`      | Criar ponto de recompensa  |
| PUT     | `/reward-points/{id}` | Atualizar ponto de recompensa|
| DELETE  | `/reward-points/{id}` | Remover ponto de recompensa|

### ⭐ Ações de Recompensa

| Método  | Endpoint              | Descrição                  |
|---------|-----------------------|----------------------------|
| GET     | `/reward-actions`     | Listar ações de recompensa |
| GET     | `/reward-actions/{id}`| Buscar ação por ID         |
| POST    | `/reward-actions`     | Criar ação de recompensa   |
| PUT     | `/reward-actions/{id}`| Atualizar ação de recompensa|
| DELETE  | `/reward-actions/{id}`| Remover ação de recompensa |

### 🎫 Resgates

| Método  | Endpoint      | Descrição      |
|---------|---------------|----------------|
| GET     | `/redeems`    | Listar resgates |
| GET     | `/redeems/{id}`| Buscar resgate por ID|
| POST    | `/redeems`    | Criar resgate   |

### 🤝 Descontos de Parceiros

| Método  | Endpoint                              | Descrição                      |
|---------|---------------------------------------|--------------------------------|
| GET     | `/partner-discounts`                  | Listar descontos de parceiros  |
| GET     | `/partner-discounts/{id}`             | Buscar desconto por ID         |
| POST    | `/partner-discounts`                  | Criar desconto de parceiro     |
| PUT     | `/partner-discounts/{id}`             | Atualizar desconto de parceiro |
| DELETE  | `/partner-discounts/{id}`             | Remover desconto de parceiro   |
| GET     | `/partner-discounts/search/category`  | Buscar por categoria           |
| GET     | `/partner-discounts/by-clinic/{clinicId}` | Descontos por clínica         |

### 🧬 Espécies

| Método  | Endpoint      | Descrição          |
|---------|---------------|--------------------|
| GET     | `/species`    | Listar espécies    |
| GET     | `/species/{id}`| Buscar espécie por ID|
| POST    | `/species`    | Criar espécie      |
| PUT     | `/species/{id}`| Atualizar espécie  |
| DELETE  | `/species/{id}`| Remover espécie    |

### 📅 Assinaturas

| Método  | Endpoint          | Descrição          |
|---------|-------------------|--------------------|
| GET     | `/subscriptions`  | Listar assinaturas |
| GET     | `/subscriptions/{id}`| Buscar assinatura por ID|
| POST    | `/subscriptions`  | Criar assinatura   |
| PUT     | `/subscriptions/{id}`| Atualizar assinatura|
| DELETE  | `/subscriptions/{id}`| Remover assinatura |

### 📌 Tipos de Evento

| Método  | Endpoint          | Descrição              |
|---------|-------------------|------------------------|
| GET     | `/event-types`    | Listar tipos de evento |
| GET     | `/event-types/{id}`| Buscar tipo por ID     |
| POST    | `/event-types`    | Criar tipo de evento   |
| PUT     | `/event-types/{id}`| Atualizar tipo de evento|
| DELETE  | `/event-types/{id}`| Remover tipo de evento |

## 🧠 Conceitos Aplicados

- Programação Orientada a Objetos
- APIs RESTful
- DTO Pattern
- Repository Pattern
- Service Layer Pattern
- Tratamento global de exceções
- Bean Validation
- Cache de requisições
- Relacionamentos JPA
- Paginação e ordenação
- Consultas parametrizadas

## 📄 Documentação Complementar

O projeto também contém:

- **DER (Diagrama Entidade Relacionamento)**
- **Diagrama de Classes**
- **Cronograma do projeto**
- **Coleção Postman**
- **Evidências de testes**

---

*Desenvolvido como parte do Challenge 2TDSPX - FIAP*