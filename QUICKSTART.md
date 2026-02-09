# CafeFlow - Quick Start Guide

Get CafeFlow running and create your first workflow using Google Antigravity prompts.

## Prerequisites

- Java 17+ (21 recommended)
- Maven
- Docker & Docker Compose
- Google Antigravity IDE

## Step 1: Clone the Project

```bash
git clone https://github.com/anthropic/cafeflow.git
cd cafeflow
```

## Step 2: Start Docker Infrastructure

CafeFlow uses Docker Compose to run the entire Temporal ecosystem. A single command starts everything:

```bash
docker-compose up -d
```

### What Docker starts

The `docker-compose.yml` creates 4 services on a shared `temporal-network` bridge:

| Service | Image | Port | Purpose |
|---------|-------|------|---------|
| **db** | `postgres:15` | `5432` | PostgreSQL database for Temporal state persistence |
| **temporal** | `temporalio/auto-setup:1.24.0` | `7233` | Temporal Server with automatic schema migration |
| **temporal-admin-tools** | `temporalio/admin-tools:latest` | - | CLI tools (`tctl`) for namespace and workflow management |
| **temporal-ui** | `temporalio/ui:latest` | `8081` | Web dashboard for visual workflow monitoring |

### Startup order

1. **PostgreSQL** starts first and runs a healthcheck (`pg_isready`)
2. **Temporal Server** waits until PostgreSQL is healthy, then auto-configures the database schema
3. **Admin Tools** and **Web UI** connect to the Temporal Server

### Verify infrastructure

```bash
# Check all 4 services are running
docker-compose ps

# Expected output:
# temporal-postgresql   ... Up (healthy)
# temporal              ... Up
# temporal-admin-tools  ... Up
# temporal-ui           ... Up
```

### Useful Docker commands

```bash
# View Temporal server logs
docker-compose logs temporal

# View all logs in real-time
docker-compose logs -f

# Stop all services
docker-compose down

# Full reset (stop + remove database volumes)
docker-compose down -v

# Access Temporal CLI inside the admin container
docker exec -it temporal-admin-tools bash
#   tctl namespace list
#   tctl workflow list
#   tctl schedule list
```

### Alternative: official_postgres.yml

The project also includes `official_postgres.yml`, an alternative Docker Compose based on Temporal's official configuration. It supports version variables (`TEMPORAL_VERSION`, `POSTGRESQL_VERSION`), volume persistence, and dynamic config files.

## Step 3: Configure Environment

CafeFlow uses `spring-dotenv` to automatically load a `.env` file from the project root. Start from the template:

```bash
cp .env.example .env
```

Then edit `.env` and **uncomment only the variables your workflow needs**:

```env
# === LLM / AI Helpers ===
# At least one provider required for AI helpers (summarize, translate, classify, etc.)
GEMINI_API_KEY=your-google-ai-key
# GROQ_API_KEY=your-groq-key

# === Email (SMTP) ===
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password

# === Telegram ===
# TELEGRAM_BOT_TOKEN=your-bot-token

# === Twitter/X ===
# X_BEARER_TOKEN=your-bearer-token

# === Google Drive ===
# GD_CREDENTIALS_PATH=/credentials.json
```

Only configure the variables for the helpers your workflow uses. The `ConfigurationValidator` will tell you on startup which helpers are ready and which are missing configuration.

### Gmail App Password

To use Gmail as SMTP provider:
1. Enable 2-Factor Authentication on your Google account
2. Go to Google Account > Security > App Passwords
3. Generate a new app password for "Mail"
4. Use this password as `SMTP_PASSWORD`

## Step 4: Run the Application

```bash
mvn spring-boot:run
```

On startup, the `ConfigurationValidator` prints a report showing which helpers are ready:

```
╔══════════════════════════════════════════════════════════════════╗
║              CafeFlow Configuration Report                      ║
╠══════════════════════════════════════════════════════════════════╣
║ ✅ RedditHelper         — ready (no config needed)              ║
║ ✅ EmailHelper          — ready                                 ║
║ ✅ TextSummarizerHelper — ready                                 ║
║ ⬚  SlackHelper          — not active                           ║
╚══════════════════════════════════════════════════════════════════╝
```

If any helper shows ⚠️ MISSING, edit your `.env` file and restart.

## Step 5: Create Workflows with Google Antigravity

Open **Google Antigravity IDE**, load the CafeFlow project, and write a prompt describing the automation you need. Antigravity reads the project structure, understands the available helpers, and generates the complete workflow code.

### Example Prompt: Reddit to Email

```
Create a CafeFlow workflow that:
1. Fetches the 5 most upvoted posts from subreddit "GoogleAntigravityIDE"
2. Sends a summary via email to myemail@outlook.com
3. Use SMTP server: smtp.gmail.com, port 587
```

Antigravity generates the following files:

**1. Workflow Interface** - `RedditAutomationWorkflow.java`
```java
@WorkflowInterface
public interface RedditAutomationWorkflow {
    @WorkflowMethod
    void run();
}
```

**2. Workflow Implementation** - `RedditAutomationWorkflowImpl.java`
```java
public class RedditAutomationWorkflowImpl implements RedditAutomationWorkflow {

    private final RedditActivities activities = Workflow.newActivityStub(
            RedditActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(2))
                    .build());

    @Override
    public void run() {
        List<RedditPost> posts = activities.fetchTopPosts("GoogleAntigravityIDE", 5);
        if (!posts.isEmpty()) {
            activities.sendEmail(posts);
        }
    }
}
```

**3. Activities Interface** - `RedditActivities.java`
```java
@ActivityInterface
public interface RedditActivities {
    @ActivityMethod
    List<RedditPost> fetchTopPosts(String subreddit, int limit);

    @ActivityMethod
    void sendEmail(List<RedditPost> posts);
}
```

**4. Activities Implementation** - `RedditActivitiesImpl.java`
```java
@Component
@RequiredArgsConstructor
public class RedditActivitiesImpl implements RedditActivities {

    private final RedditHelper redditHelper;
    private final EmailHelper emailHelper;

    @Override
    public List<RedditPost> fetchTopPosts(String subreddit, int limit) {
        return redditHelper.fetchTopPosts(subreddit, limit, RedditPost.class);
    }

    @Override
    public void sendEmail(List<RedditPost> posts) {
        StringBuilder body = new StringBuilder("Here are the top posts:\n\n");
        for (int i = 0; i < posts.size(); i++) {
            RedditPost post = posts.get(i);
            body.append(i + 1).append(". ").append(post.title()).append("\n")
                .append("   Link: ").append(post.link()).append("\n")
                .append("   Upvotes: ").append(post.upvotes()).append("\n\n");
        }
        emailHelper.sendTextEmail(
            "myemail@outlook.com",
            "CafeFlow: Top Reddit Posts",
            body.toString()
        );
    }
}
```

**5. Data Model** - `RedditPost.java`
```java
@JsonIgnoreProperties(ignoreUnknown = true)
public record RedditPost(
        String title,
        @JsonProperty("url") String link,
        @JsonProperty("ups") int upvotes) {
}
```

## Step 6: Register the Worker

Add the worker configuration to `application.yml`:

```yaml
spring:
  temporal:
    workers:
      - name: "REDDIT_WORKER"
        task-queue: "REDDIT_WORKER"
        capacity:
          max-concurrent-workflow-task-pollers: 2
        workflow-classes:
          - com.cafeflow.workflows.reddit.RedditAutomationWorkflowImpl
        activity-beans:
          - redditActivitiesImpl
```

## Step 7: Add a Schedule (Optional)

To run the workflow on a schedule, ask Antigravity:

```
Add a Temporal schedule to the CafeFlow application that runs
RedditAutomationWorkflow every day at 08:00 AM on task queue "REDDIT_WORKER"
```

Or uncomment and configure the `scheduleRunner` bean in `CafeFlowApplication.java`.

## Step 8: Monitor

Open the Temporal Web UI to monitor your workflows:

```
http://localhost:8081
```

From the UI you can:
- View running and completed workflows
- Inspect workflow history and activity results
- Terminate or cancel workflows
- View scheduled workflows

## More Prompt Ideas

### Telegram notification workflow
```
Create a CafeFlow workflow that:
1. Fetches top 3 posts from subreddit "news"
2. Sends each post title as a Telegram message to chat "123456789"
3. Runs every 6 hours
```

### Google Drive cleanup report
```
Create a CafeFlow workflow that:
1. Lists all files from Google Drive
2. Builds a summary with total file count
3. Sends the summary via email to admin@company.com
```

### Multi-source aggregator
```
Create a CafeFlow workflow that:
1. Fetches top 5 posts from subreddit "programming"
2. Fetches top 5 posts from subreddit "java"
3. Merges both lists sorted by upvotes
4. Sends the merged digest via email
5. Sends a Telegram notification saying "Digest ready!"
```

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Temporal not connecting | Run `docker-compose ps` - ensure all services are `Up` |
| Maven build fails | Verify Java 17+ with `java -version` |
| Email not sending | Check `SMTP_USERNAME` and `SMTP_PASSWORD` are set. For Gmail, use an App Password |
| Telegram not working | Verify `TELEGRAM_BOT_TOKEN` is valid. Create a bot via @BotFather |
| Google Drive init fails | Place `credentials.json` in `src/main/resources/` |
| Port 8080 in use | Change `server.port` in `application.yml` |
| Port 8081 in use | Change the Temporal UI port in `docker-compose.yml` |

## File Placement

Place generated workflow files in:

```
src/main/java/com/cafeflow/workflows/<your-workflow-name>/
```

Example:
```
src/main/java/com/cafeflow/workflows/reddit/
    RedditAutomationWorkflow.java
    RedditAutomationWorkflowImpl.java
    RedditActivities.java
    RedditActivitiesImpl.java
    RedditPost.java
```

---

# CafeFlow - Guia de Inicio Rapido (PT-BR)

Coloque o CafeFlow para funcionar e crie seu primeiro workflow usando prompts no Google Antigravity.

## Pre-requisitos

- Java 17+ (21 recomendado)
- Maven
- Docker & Docker Compose
- Google Antigravity IDE

## Passo 1: Clonar o Projeto

```bash
git clone https://github.com/anthropic/cafeflow.git
cd cafeflow
```

## Passo 2: Iniciar Infraestrutura Docker

O CafeFlow usa Docker Compose para rodar todo o ecossistema Temporal. Um unico comando inicia tudo:

```bash
docker-compose up -d
```

### O que o Docker inicia

O `docker-compose.yml` cria 4 servicos em uma rede bridge compartilhada `temporal-network`:

| Servico | Imagem | Porta | Funcao |
|---------|--------|-------|--------|
| **db** | `postgres:15` | `5432` | Banco PostgreSQL para persistencia de estado do Temporal |
| **temporal** | `temporalio/auto-setup:1.24.0` | `7233` | Servidor Temporal com migracao automatica de schema |
| **temporal-admin-tools** | `temporalio/admin-tools:latest` | - | Ferramentas CLI (`tctl`) para gerenciar namespaces e workflows |
| **temporal-ui** | `temporalio/ui:latest` | `8081` | Dashboard web para monitoramento visual de workflows |

### Ordem de inicializacao

1. **PostgreSQL** inicia primeiro e executa um healthcheck (`pg_isready`)
2. **Temporal Server** aguarda o PostgreSQL ficar saudavel, depois configura o schema do banco automaticamente
3. **Admin Tools** e **Web UI** conectam ao Temporal Server

### Verificar infraestrutura

```bash
# Verificar se os 4 servicos estao rodando
docker-compose ps

# Saida esperada:
# temporal-postgresql   ... Up (healthy)
# temporal              ... Up
# temporal-admin-tools  ... Up
# temporal-ui           ... Up
```

### Comandos Docker uteis

```bash
# Ver logs do Temporal Server
docker-compose logs temporal

# Ver todos os logs em tempo real
docker-compose logs -f

# Parar todos os servicos
docker-compose down

# Reset completo (parar + remover volumes do banco)
docker-compose down -v

# Acessar o CLI do Temporal dentro do container admin
docker exec -it temporal-admin-tools bash
#   tctl namespace list
#   tctl workflow list
#   tctl schedule list
```

### Alternativa: official_postgres.yml

O projeto tambem inclui o `official_postgres.yml`, um Docker Compose alternativo baseado na configuracao oficial do Temporal. Suporta variaveis de versao (`TEMPORAL_VERSION`, `POSTGRESQL_VERSION`), persistencia em volumes e arquivos de configuracao dinamica.

## Passo 3: Configurar Ambiente

O CafeFlow usa `spring-dotenv` para carregar automaticamente um arquivo `.env` da raiz do projeto. Comece pelo template:

```bash
cp .env.example .env
```

Depois edite o `.env` e **descomente apenas as variaveis que seu workflow precisa**:

```env
# === LLM / Helpers de IA ===
# Pelo menos um provedor necessario para helpers de IA (resumir, traduzir, classificar, etc.)
GEMINI_API_KEY=sua-chave-google-ai
# GROQ_API_KEY=sua-chave-groq

# === Email (SMTP) ===
SMTP_USERNAME=seu-email@gmail.com
SMTP_PASSWORD=sua-senha-de-app

# === Telegram ===
# TELEGRAM_BOT_TOKEN=seu-token-do-bot

# === Twitter/X ===
# X_BEARER_TOKEN=seu-bearer-token

# === Google Drive ===
# GD_CREDENTIALS_PATH=/credentials.json
```

Configure apenas as variaveis dos helpers que seu workflow usa. O `ConfigurationValidator` informa no startup quais helpers estao prontos e quais estao faltando configuracao.

### Senha de App do Gmail

Para usar Gmail como provedor SMTP:
1. Ative a Autenticacao em 2 Fatores na sua conta Google
2. Acesse Conta Google > Seguranca > Senhas de App
3. Gere uma nova senha de app para "E-mail"
4. Use essa senha como `SMTP_PASSWORD`

## Passo 4: Executar a Aplicacao

```bash
mvn spring-boot:run
```

No startup, o `ConfigurationValidator` imprime um relatorio mostrando quais helpers estao prontos:

```
╔══════════════════════════════════════════════════════════════════╗
║              CafeFlow Configuration Report                      ║
╠══════════════════════════════════════════════════════════════════╣
║ ✅ RedditHelper         — ready (no config needed)              ║
║ ✅ EmailHelper          — ready                                 ║
║ ✅ TextSummarizerHelper — ready                                 ║
║ ⬚  SlackHelper          — not active                           ║
╚══════════════════════════════════════════════════════════════════╝
```

Se algum helper mostrar ⚠️ MISSING, edite seu arquivo `.env` e reinicie.

## Passo 5: Criar Workflows com o Google Antigravity

Abra o **Google Antigravity IDE**, carregue o projeto CafeFlow e escreva um prompt descrevendo a automacao que voce precisa. O Antigravity le a estrutura do projeto, entende os helpers disponiveis e gera o codigo completo do workflow.

### Exemplo de Prompt: Reddit para Email

```
Crie um CafeFlow workflow que:
1. Busca as 5 mensagens mais curtidas do subreddit "GoogleAntigravityIDE"
2. Envia resumo por email para meuemail@outlook.com
3. Use o server SMTP: smtp.gmail.com, porta 587
```

O Antigravity gera os seguintes arquivos:

**1. Interface do Workflow** - `RedditAutomationWorkflow.java`
```java
@WorkflowInterface
public interface RedditAutomationWorkflow {
    @WorkflowMethod
    void run();
}
```

**2. Implementacao do Workflow** - `RedditAutomationWorkflowImpl.java`
```java
public class RedditAutomationWorkflowImpl implements RedditAutomationWorkflow {

    private final RedditActivities activities = Workflow.newActivityStub(
            RedditActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(2))
                    .build());

    @Override
    public void run() {
        List<RedditPost> posts = activities.fetchTopPosts("GoogleAntigravityIDE", 5);
        if (!posts.isEmpty()) {
            activities.sendEmail(posts);
        }
    }
}
```

**3. Interface das Activities** - `RedditActivities.java`
```java
@ActivityInterface
public interface RedditActivities {
    @ActivityMethod
    List<RedditPost> fetchTopPosts(String subreddit, int limit);

    @ActivityMethod
    void sendEmail(List<RedditPost> posts);
}
```

**4. Implementacao das Activities** - `RedditActivitiesImpl.java`
```java
@Component
@RequiredArgsConstructor
public class RedditActivitiesImpl implements RedditActivities {

    private final RedditHelper redditHelper;
    private final EmailHelper emailHelper;

    @Override
    public List<RedditPost> fetchTopPosts(String subreddit, int limit) {
        return redditHelper.fetchTopPosts(subreddit, limit, RedditPost.class);
    }

    @Override
    public void sendEmail(List<RedditPost> posts) {
        StringBuilder body = new StringBuilder("Aqui estao os top posts:\n\n");
        for (int i = 0; i < posts.size(); i++) {
            RedditPost post = posts.get(i);
            body.append(i + 1).append(". ").append(post.title()).append("\n")
                .append("   Link: ").append(post.link()).append("\n")
                .append("   Upvotes: ").append(post.upvotes()).append("\n\n");
        }
        emailHelper.sendTextEmail(
            "meuemail@outlook.com",
            "CafeFlow: Top Reddit Posts",
            body.toString()
        );
    }
}
```

**5. Modelo de Dados** - `RedditPost.java`
```java
@JsonIgnoreProperties(ignoreUnknown = true)
public record RedditPost(
        String title,
        @JsonProperty("url") String link,
        @JsonProperty("ups") int upvotes) {
}
```

## Passo 6: Registrar o Worker

Adicione a configuracao do worker no `application.yml`:

```yaml
spring:
  temporal:
    workers:
      - name: "REDDIT_WORKER"
        task-queue: "REDDIT_WORKER"
        capacity:
          max-concurrent-workflow-task-pollers: 2
        workflow-classes:
          - com.cafeflow.workflows.reddit.RedditAutomationWorkflowImpl
        activity-beans:
          - redditActivitiesImpl
```

## Passo 7: Agendar Execucao (Opcional)

Para executar o workflow em um agendamento, peca ao Antigravity:

```
Adicione um schedule Temporal ao CafeFlow que executa
RedditAutomationWorkflow todo dia as 08:00 na task queue "REDDIT_WORKER"
```

Ou descomente e configure o bean `scheduleRunner` em `CafeFlowApplication.java`.

## Passo 8: Monitorar

Abra a Temporal Web UI para monitorar seus workflows:

```
http://localhost:8081
```

Na UI voce pode:
- Visualizar workflows em execucao e concluidos
- Inspecionar historico de workflows e resultados de activities
- Encerrar ou cancelar workflows
- Visualizar workflows agendados

## Mais Ideias de Prompts

### Workflow de notificacao Telegram
```
Crie um CafeFlow workflow que:
1. Busca os top 3 posts do subreddit "news"
2. Envia o titulo de cada post como mensagem Telegram para o chat "123456789"
3. Executa a cada 6 horas
```

### Relatorio de Google Drive
```
Crie um CafeFlow workflow que:
1. Lista todos os arquivos do Google Drive
2. Monta um resumo com a quantidade total de arquivos
3. Envia o resumo por email para admin@empresa.com
```

### Agregador multi-fonte
```
Crie um CafeFlow workflow que:
1. Busca top 5 posts do subreddit "programming"
2. Busca top 5 posts do subreddit "java"
3. Junta ambas as listas ordenando por upvotes
4. Envia o digest unificado por email
5. Envia notificacao Telegram dizendo "Digest pronto!"
```

## Solucao de Problemas

| Problema | Solucao |
|----------|---------|
| Temporal nao conecta | Execute `docker-compose ps` - verifique se todos os servicos estao `Up` |
| Build Maven falha | Verifique Java 17+ com `java -version` |
| Email nao envia | Verifique se `SMTP_USERNAME` e `SMTP_PASSWORD` estao configurados. Para Gmail, use Senha de App |
| Telegram nao funciona | Verifique se `TELEGRAM_BOT_TOKEN` e valido. Crie um bot via @BotFather |
| Google Drive falha ao iniciar | Coloque `credentials.json` em `src/main/resources/` |
| Porta 8080 em uso | Altere `server.port` no `application.yml` |
| Porta 8081 em uso | Altere a porta da Temporal UI no `docker-compose.yml` |

## Onde Colocar os Arquivos

Coloque os arquivos de workflow gerados em:

```
src/main/java/com/cafeflow/workflows/<nome-do-seu-workflow>/
```

Exemplo:
```
src/main/java/com/cafeflow/workflows/reddit/
    RedditAutomationWorkflow.java
    RedditAutomationWorkflowImpl.java
    RedditActivities.java
    RedditActivitiesImpl.java
    RedditPost.java
```
