# 🐾 PetFlow API

[![Java](https://img.shields.io/badge/Java-21-blue.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Oracle](https://img.shields.io/badge/Oracle-Database-red.svg)](https://www.oracle.com/database/)
[![Swagger](https://img.shields.io/badge/Swagger-OpenAPI-85EA2D.svg)](https://swagger.io/)

## 📋 Sumário

- [Sobre o Projeto](#-sobre-o-projeto)
- [Equipe](#-equipe)
- [Objetivo do Challenge](#-objetivo-do-challenge)
- [Tecnologias Utilizadas](#️-tecnologias-utilizadas)
- [Arquitetura do Projeto](#-arquitetura-do-projeto)
- [Requisitos Técnicos Atendidos](#-requisitos-técnicos-atendidos)
- [Visão de Domínio](#-vis%C3%A3o-de-dom%C3%ADnio)
- [Endpoints da API](#-endpoints-da-api)
- [Banco de Dados](#️-banco-de-dados)
- [Swagger / OpenAPI](#-swagger--openapi)
- [Como Executar](#-como-executar)
- [Testes da API](#-testes-da-api)
- [Documentação Complementar](#-documenta%C3%A7%C3%A3o-complementar)
- [Observações Finais](#-observa%C3%A7%C3%B5es-finais)

## 📌 Sobre o Projeto

O **PetFlow** é uma API REST desenvolvida em Java com Spring Boot para gerenciamento de saúde preventiva pet. A solução centraliza o relacionamento entre tutores, pets, clínicas veterinárias, planos, assinaturas, eventos de saúde e cupons de resgate.

O foco do projeto é demonstrar uma aplicação backend organizada em camadas, com persistência relacional, validação de dados, documentação automática e boas práticas de APIs RESTful.

## 👥 Equipe

| Nome | RM |
|---|---:|
| Lucas Grillo Alcântara | 561413 |
| Pietro Ferreira Gomes Abrahamian | 561469 |
| Pedro Peres Benitez | 561792 |
| Lucca Ramos Mussumecci | 562027 |

**Turma:** 2TDSPX

## 🎯 Objetivo do Challenge

Desenvolver uma solução utilizando Java e Spring Boot capaz de:

- persistir dados em banco relacional;
- gerenciar informações de saúde pet;
- aplicar conceitos de Programação Orientada a Objetos;
- utilizar JPA e relacionamentos entre entidades;
- garantir validações e tratamento de exceções;
- respeitar os fundamentos de APIs REST;
- disponibilizar documentação da API;
- atender aos requisitos técnicos da disciplina Java Advanced.

## 🛠️ Tecnologias Utilizadas

- Java 21
- Spring Boot
- Spring Data JPA
- Spring Validation
- Spring Cache
- Oracle Database
- H2 Database
- Lombok
- Swagger / OpenAPI
- Maven

## 🧱 Arquitetura do Projeto

O projeto segue uma arquitetura em camadas:

```text
src/main/java/br/com/petflow/petflow_api/
├── config/
├── controller/
├── dto/
├── entity/
├── enums/
├── exception/
├── repository/
├── service/
└── PetflowApiApplication.java
```

## ✅ Requisitos Técnicos Atendidos

A solução contempla os requisitos obrigatórios da disciplina:

- Bean Validation
- Paginação de resultados
- Ordenação de resultados
- Busca com parâmetros
- Cache com `@Cacheable`
- Tratamento global de exceções
- Utilização de DTOs
- Documentação com Swagger/OpenAPI
- Relacionamentos JPA
- API RESTful

---

## 🧠 Visão de Domínio

### 👤 Tutores
Gerencia os responsáveis pelos pets.

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

---

## 📦 Endpoints da API

### 👤 Tutores

| Método | Endpoint |
| --- | --- |
| GET | `/tutors` |
| GET | `/tutors/{id}` |
| POST | `/tutors` |
| PUT | `/tutors/{id}` |
| DELETE | `/tutors/{id}` |

### 🐾 Pets

| Método | Endpoint |
| --- | --- |
| GET | `/pets` |
| GET | `/pets/{id}` |
| POST | `/pets` |
| PUT | `/pets/{id}` |
| DELETE | `/pets/{id}` |

### 🏥 Clínicas

| Método | Endpoint |
| --- | --- |
| GET | `/clinics` |
| GET | `/clinics/{id}` |
| POST | `/clinics` |
| PUT | `/clinics/{id}` |
| DELETE | `/clinics/{id}` |

### 📄 Planos

| Método | Endpoint |
| --- | --- |
| GET | `/plans` |
| GET | `/plans/{id}` |
| POST | `/plans` |
| PUT | `/plans/{id}` |
| DELETE | `/plans/{id}` |

### 📅 Assinaturas

| Método | Endpoint |
| --- | --- |
| GET | `/subscriptions` |
| GET | `/subscriptions/{id}` |
| POST | `/subscriptions` |
| PUT | `/subscriptions/{id}` |
| DELETE | `/subscriptions/{id}` |

### ❤️ Eventos de Saúde

| Método | Endpoint |
| --- | --- |
| GET | `/health-events` |
| GET | `/health-events/{id}` |
| POST | `/health-events` |
| PUT | `/health-events/{id}` |
| DELETE | `/health-events/{id}` |

### 🎟️ Cupons

| Método | Endpoint |
| --- | --- |
| GET | `/coupons` |
| GET | `/coupons/{id}` |
| POST | `/coupons` |
| PUT | `/coupons/{id}` |
| DELETE | `/coupons/{id}` |

### 🎫 Resgates

| Método | Endpoint |
| --- | --- |
| GET | `/redeems` |
| GET | `/redeems/{id}` |
| POST | `/redeems` |

---

## 🗃️ Banco de Dados

O projeto utiliza banco relacional com suporte a:

- Oracle Database
- H2 Database para testes e desenvolvimento local

O modelo foi estruturado com JPA e relacionamentos entre entidades para garantir integridade e organização dos dados.

---

## 📖 Swagger / OpenAPI

A documentação interativa da API está disponível em:

```text
/swagger-ui/index.html
```

---

## 🚀 Como Executar

### Pré-requisitos

- Java 21
- Maven 3.6+

### Execução local

```bash
./mvnw spring-boot:run
```

> Antes de executar, ajuste as credenciais e a URL do banco no arquivo de configuração da aplicação conforme o ambiente utilizado.

## 🧪 Testes da API

Os endpoints podem ser testados com Postman, Insomnia ou outra ferramenta de API.

- Validar chamados CRUD para cada recurso
- Verificar regras de negócio e mensagens de erro
- Testar paginação e ordenação
- Checar fluxo de cupons e resgates

## 📄 Documentação Complementar

A entrega final deve incluir:

- coleção exportada das requisições
- exemplos de payloads
- evidências de resposta dos endpoints
- cronograma de desenvolvimento
- diagrama de classes
- DER

## 🧭 Observações Finais

Esta versão do PetFlow foi organizada para manter foco em:

- consistência arquitetural
- clareza de domínio
- aderência aos requisitos técnicos
- manutenção mais simples
- apresentação objetiva do projeto

Desenvolvido como parte do Challenge 2TDSPX - FIAP