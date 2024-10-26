# GreenSpace API

GreenSpace API é uma aplicação Spring Boot projetada para alimentar a rede social GreenSpace.

<div style="text-align: center;">
    <img src="https://cesusc.edu.br/app/themes/sage/dist/images/img-topo/logo.png" alt="CesuscLogo" style="background-color:#13141f;border-radius:10px;max-width: 100%; height: 100px;margin-right:10px;padding: 10px">
    <img src="./public/images/GreenSpaceLogo.png" alt="GreenSpaceLogo" style="background-color:#13141f;border-radius:10px; max-width: 100%; height: 100px;margin-left:10px;padding: 10px">
</div>

## Índice

-   [Introdução](#introdução)
-   [Pré-requisitos](#pré-requisitos)
-   [Instalação e configuração](#instalação)
-   [Executando a Aplicação](#executando-a-aplicação)
-   [Executando Testes](#executando-testes)
-   [Documentação da API](#documentação-da-api)
-   [Suporte a Docker](#usando-docker)

## Introdução

Estas instruções ajudarão você a configurar e executar o projeto em sua máquina local para fins de desenvolvimento e teste.

## Pré-requisitos

-   Java 21
-   Maven 3.9.5
-   Docker
-   Docker Compose
-   PostgreSQL

## Instalação

1. **Clone o repositório:**

    ```sh
    git clone https://github.com/BenoGustavo/greenspace-api.git
    cd greenspace-api
    ```

2. **Configure as variáveis de ambiente:**

    Crie um arquivo `.env` no diretório raiz e adicione as seguintes variáveis:

    ```env
    DATABASE_URL=jdbc:postgresql://localhost:5432/greenspace
    DATABASE_USERNAME=seu_usuario_db
    DATABASE_PASSWORD=sua_senha_db
    DATABASE_NAME=greenspace
    ```

3. **Instale as dependências:**

    ```sh
    ./mvnw dependency:go-offline -B
    ```

## Executando a Aplicação

### Usando Maven

1. **Compile o projeto:**

    ```sh
    ./mvnw clean package
    ```

2. **Execute a aplicação:**

    ```sh
    ./mvnw spring-boot:run
    ```

### Usando Docker

Por enquanto o docker funciona apenas para deploy, não tendo a habilidade de atualizar a aplicação quando uma mudança no codigo fonte é realizada.

1. **Compile e execute os containers:**

    ```sh
    docker-compose up --build
    ```

2. **Acesse a aplicação:**

    Abra seu navegador e navegue para `http://localhost:8080`.

## Executando Testes

Para executar os testes, use o seguinte comando:

```sh
./mvnw test
```

## Documentação da API

A documentação pode ser encontrada iniciando o projeto e indo até a rota:

```https
http://localhost:8080/api-docs
```

Lá você vai encontrar toda a documentação de rotas feitas através do swagger.
