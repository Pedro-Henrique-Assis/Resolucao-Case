# Projeto de Avaliação de Colaboradores (Resolução Case)

Esta é uma API RESTful desenvolvida em Java e Spring Boot para o gerenciamento e avaliação de performance de colaboradores. O sistema permite o cadastro de colaboradores, o registro de suas entregas (com notas) e o lançamento de suas avaliações comportamentais (baseadas em 4 critérios).

O objetivo principal da API é consolidar essas informações para calcular uma nota de performance final para cada colaborador, auxiliando em processos de feedback.

## Visão Geral da Arquitetura

O projeto segue uma arquitetura em camadas (Resources, Services, Repositories e DTOs) para garantir a separação de responsabilidades, manutenibilidade e testabilidade.

## Tecnologias Utilizadas

Este projeto utiliza um conjunto de tecnologias modernas para desenvolvimento e monitoramento:

  * **Backend:** Java 21 e Spring Boot 3.4.11
  * **Acesso a Dados:** Spring Data JPA
  * **Banco de Dados:** Microsoft SQL Server
  * **Build Tool:** Gradle
  * **Documentação da API:** Springdoc (OpenAPI/Swagger)
  * **Monitoramento:** Micrometer com Prometheus
  * **Ambiente:** Docker e Docker Compose

## Pré-requisitos

Para compilar e executar este projeto localmente, você precisará das seguintes ferramentas instaladas em seu sistema.

### 1\. Java (JDK)

É necessário o Java Development Kit (JDK) na versão 21 (ou superior).

  * **Como verificar a instalação:**
    ```sh
    java -version
    ```
    A saída deve indicar a versão 21 (ex: `openjdk version "21.0.0"`).

### 2\. Docker e Docker Compose

O Docker é recomendado para executar os serviços de monitoramento (Prometheus/Grafana) de forma isolada.

  * **Como verificar a instalação:**
    ```sh
    docker --version
    docker compose version
    ```

### 3\. Cliente de API (Opcional)

Para interagir com a API, recomenda-se um cliente como Postman, Insomnia ou similar. O projeto inclui uma coleção do Postman (`api-files/API-Collection.json`) que pode ser importada.

## Como Executar o Projeto

Siga estes passos para configurar e executar a aplicação em seu ambiente local.

### 1\. Clonar o Repositório

```sh
git clone <https://github.com/Pedro-Henrique-Assis/Resolucao-Case>
cd resolucao-case
```

### 2\. Configurar o Banco de Dados (SQL Server)

A aplicação está configurada para se conectar a um banco de dados SQL Server chamado `AvaliacaoColaboradores`. A forma mais simples de subir uma instância é usando Docker:

1.  Execute o comando abaixo para iniciar um container do SQL Server. A senha `SA_PASSWORD` deve ser a mesma definida no arquivo `application.properties` (`123456`).

    ```sh
    docker run -e "ACCEPT_EULA=Y" -e "SA_PASSWORD=123456" \
    -p 1433:1433 --name sqlserver-dev \
    -d mcr.microsoft.com/mssql/server:latest
    ```

2.  Aguarde o container iniciar. Você precisará criar o banco de dados `AvaliacaoColaboradores` manualmente através de uma ferramenta de sua preferência (DBeaver, Azure Data Studio, etc.) conectando-se a `localhost:1433`. O Spring Data JPA cuidará da criação das tabelas (`ddl-auto=update`).

### 3\. Executar a Aplicação

O projeto utiliza o Gradle Wrapper, então você não precisa ter o Gradle instalado globalmente.

  * **No Linux/macOS:**

    ```sh
    ./gradlew bootRun
    ```

  * **No Windows:**

    ```sh
    gradlew.bat bootRun
    ```

A API estará disponível em `http://localhost:8080`.

### 4\. (Opcional) Executar Monitoramento

O projeto inclui um `docker-compose.yml` para subir o Prometheus e o Grafana.

```sh
docker compose up -d
```

  * **Prometheus:** `http://localhost:9090` (Configurado para monitorar o endpoint `/actuator/prometheus` da aplicação).
  * **Grafana:** `http://localhost:3000`

## Manual de Uso (API)

A documentação interativa completa da API (Swagger) pode ser acessada em:
**[http://localhost:8080/swagger-ui.html](https://www.google.com/search?q=http://localhost:8080/swagger-ui.html)**

Abaixo estão os exemplos para as operações básicas de cadastro e consulta.

### 1\. Cadastrar um Novo Colaborador

Para criar um novo colaborador, envie uma requisição `POST` para o endpoint `/api/v1/colaborador`.

  * **Endpoint:** `POST /api/v1/colaborador`

  * **Body (Exemplo):**

    ```json
    {
      "nome": "Carlos Alberto",
      "dataAdmissao": "2020-08-12",
      "cargo": "Analista de Engenharia de Analytics Júnior"
    }
    ```


  * **Resposta (Sucesso):** `201 Created`

      * Um header `Location` será retornado indicando a URL do novo recurso (ex: `/api/v1/colaborador/ce0a1014-e9c0-46ce-b056-9d81dacd1a36`).

### 2\. Consultar um Colaborador

Para consultar um colaborador específico, utilize a `matricula` (UUID) fornecida no header `Location` do cadastro ou através da listagem.

  * **Endpoint:** `GET /api/v1/colaborador/{matricula}`

  * **Exemplo:**

    ```
    GET http://localhost:8080/api/v1/colaborador/ce0a1014-e9c0-46ce-b056-9d81dacd1a36
    ```

    *Fonte: `api-files/API-Collection.json`*

  * **Resposta (Sucesso):** `200 OK`

      * A resposta conterá os dados completos do colaborador, incluindo suas entregas e avaliações (se existirem).

    <!-- end list -->

    ```json
    {
      "matricula": "ce0a1014-e9c0-46ce-b056-9d81dacd1a36",
      "nome": "Carlos Alberto",
      "dataAdmissao": "2020-08-12",
      "cargo": "Analista de Engenharia de Analytics Júnior",
      "avaliacaoComportamento": null,
      "entregas": []
    }
    ```
