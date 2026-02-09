# CafeFlow

**Build automation workflows with natural language prompts using Google Antigravity IDE.**

CafeFlow is a Java automation framework designed to work seamlessly with **Google Antigravity** AI-powered IDE. Describe what you want in plain language, and Antigravity generates production-ready Temporal workflows using CafeFlow's helper library.

## How It Works

### 1. Clone the project
```bash
git clone https://github.com/anthropic/cafeflow.git
cd cafeflow
```

### 2. Start Docker infrastructure
```bash
docker-compose up -d
```

### 3. Open Google Antigravity and write a prompt

Open the CafeFlow project in **Google Antigravity IDE** and describe what you want:

```
Create a CafeFlow workflow that:
1. Fetches the top 5 most upvoted posts from subreddit "java"
2. Sends a summary via email to team@company.com
3. Runs every day at 8:00 AM
```

### 4. Run

```bash
mvn spring-boot:run
```

Antigravity reads the CafeFlow project structure, understands the available helpers, and generates the complete workflow code: interfaces, implementations, activities, and configuration.

## Prompt Examples

### Reddit monitoring + Email notification
```
Create a CafeFlow workflow that:
1. Fetches the 5 most upvoted posts from subreddit "GoogleAntigravityIDE"
2. Sends a summary via email to myemail@outlook.com
3. Use SMTP server: smtp.gmail.com, port 587
```

### Telegram alerts from Reddit
```
Create a CafeFlow workflow that:
1. Fetches the top 10 posts from subreddit "artificial"
2. Filters posts with more than 100 upvotes
3. Sends each post title and link to Telegram chat ID "123456789"
```

### Google Drive backup + Email report
```
Create a CafeFlow workflow that:
1. Lists the 20 most recent files from Google Drive
2. Generates an HTML report with file names and sizes
3. Sends the report via email to admin@company.com
4. Runs weekly on Mondays at 9:00 AM
```

### Twitter monitoring + multi-channel notification
```
Create a CafeFlow workflow that:
1. Looks up the Twitter user "temporalio"
2. Sends the profile data via email to marketing@company.com
3. Also sends a Telegram notification to chat "987654321"
```

### Scheduled social media digest
```
Create a CafeFlow workflow that:
1. Fetches top 5 posts from subreddit "programming"
2. Fetches top 5 posts from subreddit "java"
3. Combines both lists into a single digest
4. Sends the digest via email
5. Sends a Telegram notification saying "Daily digest sent!"
6. Runs every day at 7:30 AM
```

## Project Structure

```
CafeFlow/
src/main/java/com/cafeflow/
    core/
        base/BaseHelper.java          # Base class - error handling & logging
        exception/HelperException.java # Unified exception wrapper
    helpers/
        communication/
            EmailHelper.java           # SMTP email (Gmail, Outlook, etc.)
            TelegramHelper.java        # Telegram Bot API
            DiscordHelper.java         # TODO
            WhatsAppHelper.java        # TODO
        marketing/
            RedditHelper.java          # Reddit JSON API
            TwitterHelper.java         # X/Twitter API v2
            FacebookHelper.java        # TODO
            InstagramHelper.java       # TODO
            LinkedInHelper.java        # TODO
        office/
            google/
                GDriveHelper.java      # Google Drive API v3 (OAuth2)
                GCalendarHelper.java   # TODO
                GDocsHelper.java       # TODO
                GSheetsHelper.java     # TODO
                GmailHelper.java       # TODO
            microsoft/
                MS365Helper.java       # TODO
                MSGraphHelper.java     # TODO
                MSOutlookHelper.java   # TODO
            SlackHelper.java           # TODO
            NotionHelper.java          # TODO
            AsanaJiraHelper.java       # TODO
        development/
            GitHubHelper.java          # TODO
        devops/database/
            PostgreSQLHelper.java      # TODO
    workflows/                         # Your workflows go here
    CafeFlowApplication.java          # Spring Boot entry point
examples/
    reddit-to-email/                   # Complete reference implementation
docker-compose.yml                     # Temporal + PostgreSQL infrastructure
```

## Available Helpers

| Helper | Category | Status | Description |
|--------|----------|--------|-------------|
| **EmailHelper** | Communication | Ready | SMTP email (Gmail, Outlook, any provider) |
| **TelegramHelper** | Communication | Ready | Send messages, inline menus, notifications |
| **RedditHelper** | Marketing | Ready | Fetch top posts from any subreddit |
| **TwitterHelper** | Marketing | Ready | User lookup, post tweets (X API v2) |
| **GDriveHelper** | Office | Ready | List, upload, download files (OAuth2) |
| **TextSummarizerHelper** | AI | Ready | LLM-powered text summarization |
| **SentimentAnalyzerHelper** | AI | Ready | LLM-powered sentiment analysis |
| **TextTranslatorHelper** | AI | Ready | LLM-powered translation & language detection |
| **ContentGeneratorHelper** | AI | Ready | Generate emails, social posts, reports, tweets |
| **DataExtractorHelper** | AI | Ready | Extract fields, entities, key-values from text |
| **TextClassifierHelper** | AI | Ready | Classify text into categories, yes/no questions |
| **TopicExtractorHelper** | AI | Ready | Extract topics, hashtags, keywords |
| DiscordHelper | Communication | TODO | - |
| WhatsAppHelper | Communication | TODO | - |
| FacebookHelper | Marketing | TODO | - |
| InstagramHelper | Marketing | TODO | - |
| LinkedInHelper | Marketing | TODO | - |
| GCalendarHelper | Office | TODO | - |
| GDocsHelper | Office | TODO | - |
| GSheetsHelper | Office | TODO | - |
| GmailHelper | Office | TODO | - |
| SlackHelper | Office | TODO | - |
| NotionHelper | Office | TODO | - |
| GitHubHelper | Development | TODO | - |

## Docker Infrastructure

CafeFlow runs on Docker. The `docker-compose.yml` provides the complete Temporal ecosystem:

```bash
docker-compose up -d
```

### Services

| Service | Image | Port | Purpose |
|---------|-------|------|---------|
| **db** | `postgres:15` | `5432` | PostgreSQL database for Temporal persistence |
| **temporal** | `temporalio/auto-setup:1.24.0` | `7233` | Temporal Server with auto-schema setup |
| **temporal-admin-tools** | `temporalio/admin-tools:latest` | - | CLI tools (`tctl`) for administration |
| **temporal-ui** | `temporalio/ui:latest` | `8081` | Web dashboard for monitoring workflows |

### Architecture Diagram

```
                         temporal-network (bridge)
    ┌─────────────────────────────────────────────────────────┐
    │                                                         │
    │  ┌──────────────┐    ┌──────────────┐                   │
    │  │ PostgreSQL 15 │◄───│   Temporal   │                   │
    │  │  :5432        │    │  Server      │                   │
    │  │              │    │  :7233       │                   │
    │  └──────────────┘    └──────┬───────┘                   │
    │                             │                           │
    │               ┌─────────────┼─────────────┐             │
    │               │             │             │             │
    │        ┌──────┴──────┐ ┌────┴────┐  ┌─────┴──────┐      │
    │        │ Admin Tools │ │ Web UI  │  │ CafeFlow   │      │
    │        │ (tctl CLI)  │ │ :8081   │  │ App :8080  │      │
    │        └─────────────┘ └─────────┘  └────────────┘      │
    └─────────────────────────────────────────────────────────┘
```

### Useful Commands

```bash
# Start all services
docker-compose up -d

# Check service status
docker-compose ps

# View Temporal server logs
docker-compose logs temporal

# View all logs in real-time
docker-compose logs -f

# Stop all services
docker-compose down

# Stop and remove volumes (full reset)
docker-compose down -v

# Access Temporal CLI (tctl)
docker exec -it temporal-admin-tools bash
# Inside the container:
#   tctl namespace list
#   tctl workflow list
#   tctl schedule list

# Open Web UI in browser
# http://localhost:8081
```

### Network

All services communicate through the `temporal-network` bridge network. The Temporal Server waits for PostgreSQL to be healthy before starting (healthcheck with `pg_isready`).

### Alternative Configuration

The file `official_postgres.yml` provides an alternative Docker Compose based on the official Temporal configuration with support for version variables (`TEMPORAL_VERSION`, `POSTGRESQL_VERSION`), volume persistence, and dynamic config files.

## Architecture

### BaseHelper Pattern

Every helper extends `BaseHelper`, which provides automatic error handling and logging:

```java
public class RedditHelper extends BaseHelper {

    @Override
    protected String getServiceName() { return "reddit"; }

    public <T> List<T> fetchTopPosts(String subreddit, int limit, Class<T> type) {
        return executeWithProtection("fetchTopPosts", () -> {
            // API call logic here
        });
    }
}
```

### Helper Composition

Helpers are Spring components that can be injected and combined freely:

```java
@Component
@RequiredArgsConstructor
public class MyActivitiesImpl implements MyActivities {

    private final RedditHelper redditHelper;
    private final EmailHelper emailHelper;
    private final TelegramHelper telegramHelper;

    // Combine any helpers in your activity methods
}
```

### Temporal Workflows

Workflows define the orchestration logic. Activities perform the actual work:

```java
@WorkflowInterface
public interface MyWorkflow {
    @WorkflowMethod
    void run();
}

@ActivityInterface
public interface MyActivities {
    @ActivityMethod
    List<RedditPost> fetchPosts(String subreddit, int limit);

    @ActivityMethod
    void sendEmail(List<RedditPost> posts);
}
```

## Tech Stack

| Component | Technology | Version |
|-----------|------------|---------|
| Language | Java | 17+ |
| Framework | Spring Boot | 3.2.2 |
| Orchestration | Temporal.io | 1.24.1 |
| Database | PostgreSQL | 15 |
| Build | Maven | - |
| Infrastructure | Docker Compose | - |

## Quick Start

See [QUICKSTART.md](QUICKSTART.md) for step-by-step setup instructions.

```bash
# 1. Start infrastructure
docker-compose up -d

# 2. Configure environment (first time)
cp .env.example .env
# Edit .env with your API keys and credentials

# 3. Run the application
mvn spring-boot:run
# ConfigurationValidator will show which helpers are ready
```

## Environment Variables

Configure via `.env` file (copy from `.env.example`). The `spring-dotenv` library loads it automatically.

| Variable | Required | Description |
|----------|----------|-------------|
| `GEMINI_API_KEY` | Yes* | Google AI Studio key (for AI helpers) |
| `GEMINI_MODEL` | No | Gemini model (default: `gemini-1.5-flash`) |
| `GROQ_API_KEY` | Yes* | Groq API key (for AI helpers) |
| `GROQ_MODEL` | No | Groq model (default: `llama-3.3-70b-versatile`) |
| `LLM_DEFAULT_PROVIDER` | No | Default LLM provider: `gemini` or `groq` |
| `SMTP_HOST` | No | SMTP server (default: `smtp.gmail.com`) |
| `SMTP_PORT` | No | SMTP port (default: `587`) |
| `SMTP_USERNAME` | Yes* | Email for SMTP authentication |
| `SMTP_PASSWORD` | Yes* | App password for SMTP |
| `TELEGRAM_BOT_TOKEN` | Yes* | Telegram Bot API token |
| `X_BEARER_TOKEN` | Yes* | X/Twitter API bearer token |
| `X_API_KEY` | Yes* | X/Twitter API key |
| `X_API_SECRET` | Yes* | X/Twitter API secret |
| `GD_APP_NAME` | No | Google Drive app name (default: `CafeFlow`) |
| `GD_CREDENTIALS_PATH` | No | Path to Google credentials JSON |
| `GD_TOKENS_DIR` | No | Token storage directory (default: `tokens`) |

*Required only if using the corresponding helper. AI helpers need at least one LLM provider (Gemini or Groq).

## License

MIT

---

# CafeFlow (PT-BR)

**Crie workflows de automacao com prompts em linguagem natural usando o Google Antigravity IDE.**

CafeFlow e um framework de automacao Java projetado para funcionar com o **Google Antigravity**, a IDE com IA. Descreva o que voce quer em linguagem natural e o Antigravity gera workflows Temporal prontos para producao usando a biblioteca de helpers do CafeFlow.

## Como Funciona

### 1. Clone o projeto
```bash
git clone https://github.com/anthropic/cafeflow.git
cd cafeflow
```

### 2. Inicie a infraestrutura Docker
```bash
docker-compose up -d
```

### 3. Abra o Google Antigravity e escreva um prompt

Abra o projeto CafeFlow no **Google Antigravity IDE** e descreva o que voce quer:

```
Crie um CafeFlow workflow que:
1. Busca os 5 posts mais curtidos do subreddit "java"
2. Envia um resumo por email para equipe@empresa.com
3. Executa todo dia as 8:00
```

### 4. Execute

```bash
mvn spring-boot:run
```

O Antigravity le a estrutura do projeto CafeFlow, entende os helpers disponiveis e gera o codigo completo do workflow: interfaces, implementacoes, activities e configuracao.

## Exemplos de Prompts

### Monitoramento Reddit + Notificacao por Email
```
Crie um CafeFlow workflow que:
1. Busca as 5 mensagens mais curtidas do subreddit "GoogleAntigravityIDE"
2. Envia resumo por email para meuemail@outlook.com
3. Use o server SMTP: smtp.gmail.com, porta 587
```

### Alertas no Telegram a partir do Reddit
```
Crie um CafeFlow workflow que:
1. Busca os top 10 posts do subreddit "artificial"
2. Filtra posts com mais de 100 upvotes
3. Envia titulo e link de cada post para o chat Telegram ID "123456789"
```

### Backup Google Drive + Relatorio por Email
```
Crie um CafeFlow workflow que:
1. Lista os 20 arquivos mais recentes do Google Drive
2. Gera um relatorio HTML com nomes e tamanhos dos arquivos
3. Envia o relatorio por email para admin@empresa.com
4. Executa semanalmente nas segundas-feiras as 9:00
```

### Monitoramento Twitter + Notificacao multi-canal
```
Crie um CafeFlow workflow que:
1. Busca os dados do usuario "temporalio" no Twitter
2. Envia os dados do perfil por email para marketing@empresa.com
3. Tambem envia uma notificacao Telegram para o chat "987654321"
```

### Digest diario de redes sociais
```
Crie um CafeFlow workflow que:
1. Busca top 5 posts do subreddit "programming"
2. Busca top 5 posts do subreddit "java"
3. Combina ambas as listas em um unico digest
4. Envia o digest por email
5. Envia notificacao Telegram dizendo "Digest diario enviado!"
6. Executa todo dia as 7:30
```

## Estrutura do Projeto

```
CafeFlow/
src/main/java/com/cafeflow/
    core/
        base/BaseHelper.java          # Classe base - tratamento de erros & logging
        exception/HelperException.java # Wrapper unificado de excecoes
    helpers/
        communication/
            EmailHelper.java           # Email SMTP (Gmail, Outlook, etc.)
            TelegramHelper.java        # Telegram Bot API
            DiscordHelper.java         # TODO
            WhatsAppHelper.java        # TODO
        marketing/
            RedditHelper.java          # Reddit JSON API
            TwitterHelper.java         # X/Twitter API v2
            FacebookHelper.java        # TODO
            InstagramHelper.java       # TODO
            LinkedInHelper.java        # TODO
        office/
            google/
                GDriveHelper.java      # Google Drive API v3 (OAuth2)
                GCalendarHelper.java   # TODO
                GDocsHelper.java       # TODO
                GSheetsHelper.java     # TODO
                GmailHelper.java       # TODO
            microsoft/
                MS365Helper.java       # TODO
                MSGraphHelper.java     # TODO
                MSOutlookHelper.java   # TODO
            SlackHelper.java           # TODO
            NotionHelper.java          # TODO
            AsanaJiraHelper.java       # TODO
        development/
            GitHubHelper.java          # TODO
        devops/database/
            PostgreSQLHelper.java      # TODO
    workflows/                         # Seus workflows ficam aqui
    CafeFlowApplication.java          # Ponto de entrada Spring Boot
examples/
    reddit-to-email/                   # Implementacao de referencia completa
docker-compose.yml                     # Infraestrutura Temporal + PostgreSQL
```

## Helpers Disponiveis

| Helper | Categoria | Status | Descricao |
|--------|-----------|--------|-----------|
| **EmailHelper** | Comunicacao | Pronto | Email SMTP (Gmail, Outlook, qualquer provedor) |
| **TelegramHelper** | Comunicacao | Pronto | Enviar mensagens, menus inline, notificacoes |
| **RedditHelper** | Marketing | Pronto | Buscar top posts de qualquer subreddit |
| **TwitterHelper** | Marketing | Pronto | Busca de usuario, postar tweets (X API v2) |
| **GDriveHelper** | Office | Pronto | Listar, upload, download de arquivos (OAuth2) |
| **TextSummarizerHelper** | IA | Pronto | Resumo de texto com LLM |
| **SentimentAnalyzerHelper** | IA | Pronto | Analise de sentimento com LLM |
| **TextTranslatorHelper** | IA | Pronto | Traducao e deteccao de idioma com LLM |
| **ContentGeneratorHelper** | IA | Pronto | Gerar emails, posts sociais, relatorios, tweets |
| **DataExtractorHelper** | IA | Pronto | Extrair campos, entidades, chave-valor de texto |
| **TextClassifierHelper** | IA | Pronto | Classificar texto em categorias, perguntas sim/nao |
| **TopicExtractorHelper** | IA | Pronto | Extrair topicos, hashtags, palavras-chave |
| DiscordHelper | Comunicacao | TODO | - |
| WhatsAppHelper | Comunicacao | TODO | - |
| FacebookHelper | Marketing | TODO | - |
| InstagramHelper | Marketing | TODO | - |
| LinkedInHelper | Marketing | TODO | - |
| GCalendarHelper | Office | TODO | - |
| GDocsHelper | Office | TODO | - |
| GSheetsHelper | Office | TODO | - |
| GmailHelper | Office | TODO | - |
| SlackHelper | Office | TODO | - |
| NotionHelper | Office | TODO | - |
| GitHubHelper | Development | TODO | - |

## Infraestrutura Docker

O CafeFlow roda em Docker. O `docker-compose.yml` fornece o ecossistema Temporal completo:

```bash
docker-compose up -d
```

### Servicos

| Servico | Imagem | Porta | Funcao |
|---------|--------|-------|--------|
| **db** | `postgres:15` | `5432` | Banco PostgreSQL para persistencia do Temporal |
| **temporal** | `temporalio/auto-setup:1.24.0` | `7233` | Servidor Temporal com setup automatico de schema |
| **temporal-admin-tools** | `temporalio/admin-tools:latest` | - | Ferramentas CLI (`tctl`) para administracao |
| **temporal-ui** | `temporalio/ui:latest` | `8081` | Dashboard web para monitoramento de workflows |

### Diagrama de Arquitetura

```
                         temporal-network (bridge)
    ┌─────────────────────────────────────────────────────────┐
    │                                                         │
    │  ┌──────────────┐    ┌──────────────┐                   │
    │  │ PostgreSQL 15 │◄───│   Temporal   │                   │
    │  │  :5432        │    │  Server      │                   │
    │  │              │    │  :7233       │                   │
    │  └──────────────┘    └──────┬───────┘                   │
    │                             │                           │
    │               ┌─────────────┼─────────────┐             │
    │               │             │             │             │
    │        ┌──────┴──────┐ ┌────┴────┐  ┌─────┴──────┐      │
    │        │ Admin Tools │ │ Web UI  │  │ CafeFlow   │      │
    │        │ (tctl CLI)  │ │ :8081   │  │ App :8080  │      │
    │        └─────────────┘ └─────────┘  └────────────┘      │
    └─────────────────────────────────────────────────────────┘
```

### Comandos Uteis

```bash
# Iniciar todos os servicos
docker-compose up -d

# Verificar status dos servicos
docker-compose ps

# Ver logs do Temporal Server
docker-compose logs temporal

# Ver todos os logs em tempo real
docker-compose logs -f

# Parar todos os servicos
docker-compose down

# Parar e remover volumes (reset completo)
docker-compose down -v

# Acessar o CLI do Temporal (tctl)
docker exec -it temporal-admin-tools bash
# Dentro do container:
#   tctl namespace list
#   tctl workflow list
#   tctl schedule list

# Abrir Web UI no navegador
# http://localhost:8081
```

### Rede

Todos os servicos se comunicam pela rede bridge `temporal-network`. O Temporal Server aguarda o PostgreSQL estar saudavel antes de iniciar (healthcheck com `pg_isready`).

### Configuracao Alternativa

O arquivo `official_postgres.yml` fornece um Docker Compose alternativo baseado na configuracao oficial do Temporal com suporte a variaveis de versao (`TEMPORAL_VERSION`, `POSTGRESQL_VERSION`), persistencia em volumes e arquivos de configuracao dinamica.

## Arquitetura

### Padrao BaseHelper

Cada helper estende `BaseHelper`, que fornece tratamento de erros e logging automaticos:

```java
public class RedditHelper extends BaseHelper {

    @Override
    protected String getServiceName() { return "reddit"; }

    public <T> List<T> fetchTopPosts(String subreddit, int limit, Class<T> type) {
        return executeWithProtection("fetchTopPosts", () -> {
            // Logica de chamada da API aqui
        });
    }
}
```

### Composicao de Helpers

Helpers sao componentes Spring que podem ser injetados e combinados livremente:

```java
@Component
@RequiredArgsConstructor
public class MinhasActivitiesImpl implements MinhasActivities {

    private final RedditHelper redditHelper;
    private final EmailHelper emailHelper;
    private final TelegramHelper telegramHelper;

    // Combine qualquer helper nos metodos de activity
}
```

### Workflows Temporal

Workflows definem a logica de orquestracao. Activities executam o trabalho real:

```java
@WorkflowInterface
public interface MeuWorkflow {
    @WorkflowMethod
    void executar();
}

@ActivityInterface
public interface MinhasActivities {
    @ActivityMethod
    List<RedditPost> buscarPosts(String subreddit, int limite);

    @ActivityMethod
    void enviarEmail(List<RedditPost> posts);
}
```

## Stack Tecnologica

| Componente | Tecnologia | Versao |
|------------|------------|--------|
| Linguagem | Java | 17+ |
| Framework | Spring Boot | 3.2.2 |
| Orquestracao | Temporal.io | 1.24.1 |
| Banco de Dados | PostgreSQL | 15 |
| Build | Maven | - |
| Infraestrutura | Docker Compose | - |

## Inicio Rapido

Veja [QUICKSTART.md](QUICKSTART.md) para instrucoes passo a passo.

```bash
# 1. Iniciar infraestrutura
docker-compose up -d

# 2. Configurar ambiente (primeira vez)
cp .env.example .env
# Edite o .env com suas API keys e credenciais

# 3. Executar a aplicacao
mvn spring-boot:run
# O ConfigurationValidator mostra quais helpers estao prontos
```

## Variaveis de Ambiente

Configure via arquivo `.env` (copie do `.env.example`). A biblioteca `spring-dotenv` carrega automaticamente.

| Variavel | Obrigatoria | Descricao |
|----------|-------------|-----------|
| `GEMINI_API_KEY` | Sim* | Chave do Google AI Studio (para helpers de IA) |
| `GEMINI_MODEL` | Nao | Modelo Gemini (padrao: `gemini-1.5-flash`) |
| `GROQ_API_KEY` | Sim* | Chave da API Groq (para helpers de IA) |
| `GROQ_MODEL` | Nao | Modelo Groq (padrao: `llama-3.3-70b-versatile`) |
| `LLM_DEFAULT_PROVIDER` | Nao | Provedor LLM padrao: `gemini` ou `groq` |
| `SMTP_HOST` | Nao | Servidor SMTP (padrao: `smtp.gmail.com`) |
| `SMTP_PORT` | Nao | Porta SMTP (padrao: `587`) |
| `SMTP_USERNAME` | Sim* | Email para autenticacao SMTP |
| `SMTP_PASSWORD` | Sim* | Senha de app para SMTP |
| `TELEGRAM_BOT_TOKEN` | Sim* | Token da Telegram Bot API |
| `X_BEARER_TOKEN` | Sim* | Bearer token da X/Twitter API |
| `X_API_KEY` | Sim* | Chave da X/Twitter API |
| `X_API_SECRET` | Sim* | Secret da X/Twitter API |
| `GD_APP_NAME` | Nao | Nome do app Google Drive (padrao: `CafeFlow`) |
| `GD_CREDENTIALS_PATH` | Nao | Caminho para o JSON de credenciais Google |
| `GD_TOKENS_DIR` | Nao | Diretorio de armazenamento de tokens (padrao: `tokens`) |

*Obrigatoria apenas se usar o helper correspondente. Helpers de IA precisam de pelo menos um provedor LLM (Gemini ou Groq).

## Licenca

MIT
