# CafeFlow - Quick Start Guide

*[Portugues (PT-BR)](#cafeflow---guia-de-inicio-rapido-pt-br)*

## The Flow

```
 1. Clone          git clone → cd cafeflow
       ↓
 2. Docker         docker-compose up -d
       ↓
 3. Antigravity    Open project → write prompt → generate workflow
       ↓
 4. Configure      Antigravity tells you what to set → cp .env.example .env → fill values
       ↓
 5. Run            mvn spring-boot:run → ConfigurationValidator confirms status
       ↓
 6. Monitor        http://localhost:8081
```

## Prerequisites

- Java 17+ (21 recommended)
- Maven
- Docker & Docker Compose
- Google Antigravity IDE

## Step 1: Clone & Start Infrastructure

```bash
git clone https://github.com/anthropic/cafeflow.git
cd cafeflow
docker-compose up -d
```

Verify with `docker-compose ps` — all 4 services should be `Up`:
- **PostgreSQL 15** (:5432) — Temporal state persistence
- **Temporal Server** (:7233) — workflow orchestration
- **Temporal Web UI** (:8081) — monitoring dashboard
- **Temporal Admin Tools** — CLI (`tctl`)

## Step 2: Generate Workflow with Google Antigravity

Open the project in **Google Antigravity IDE** and write a prompt:

```
Create a CafeFlow workflow that:
1. Fetches the 5 most upvoted posts from subreddit "GoogleAntigravityIDE"
2. Sends a summary via email to myemail@outlook.com
3. Use SMTP server: smtp.gmail.com, port 587
```

Antigravity generates the complete workflow code (interfaces, activities, implementation, data model, worker config).

**After generating code, Antigravity also tells you what to configure:**

```
Your workflow uses: RedditHelper, EmailHelper

Add these to your .env file:
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password

Setup: Gmail → Enable 2FA → Account > Security > App Passwords > Mail
```

This is the key: **you don't guess which variables to configure — Antigravity tells you.**

## Step 3: Configure Environment

```bash
cp .env.example .env    # first time only
```

Edit `.env` — uncomment **only** what Antigravity told you:

```env
# For the Reddit-to-Email example, you only need:
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-app-password
```

CafeFlow uses `spring-dotenv` — no manual `export` needed.

### Getting credentials

| Service | How to get |
|---------|-----------|
| Gmail SMTP | Enable 2FA → Google Account > Security > App Passwords |
| Gemini (AI) | https://aistudio.google.com/apikey |
| Groq (AI) | https://console.groq.com/keys |
| Telegram | @BotFather → `/newbot` |
| Twitter/X | https://developer.twitter.com |
| Google Drive | Google Cloud Console → Drive API → credentials.json |

## Step 4: Run

```bash
mvn spring-boot:run
```

`ConfigurationValidator` prints on startup:

```
╔══════════════════════════════════════════════════════════════════╗
║              CafeFlow Configuration Report                      ║
╠══════════════════════════════════════════════════════════════════╣
║ ✅ RedditHelper         — ready (no config needed)              ║
║ ✅ EmailHelper          — ready                                 ║
║ ⬚  TextSummarizerHelper — not active                           ║
╚══════════════════════════════════════════════════════════════════╝
```

- **✅ ready** — configured and working
- **⚠️ MISSING** — edit `.env` and restart
- **⬚ not active** — not used (ignore)

## Step 5: Monitor

```
http://localhost:8081
```

View running/completed workflows, inspect history, manage schedules.

## Step 6: Schedule (Optional)

Ask Antigravity:

```
Add a Temporal schedule that runs RedditAutomationWorkflow
every day at 08:00 AM on task queue "REDDIT_WORKER"
```

## More Prompt Ideas

```
Create a CafeFlow workflow that:
1. Fetches top 5 posts from subreddit "machinelearning"
2. Summarizes each using TextSummarizerHelper
3. Translates to Portuguese using TextTranslatorHelper
4. Sends translated digest via email
```

```
Create a CafeFlow workflow that:
1. Fetches top 20 posts from subreddit "product"
2. Analyzes sentiment using SentimentAnalyzerHelper
3. Filters negative posts (confidence > 0.7)
4. Sends Telegram alert for each
5. Runs every 4 hours
```

## Troubleshooting

| Problem | Solution |
|---------|----------|
| Temporal not connecting | `docker-compose ps` — ensure all services `Up` |
| ⚠️ MISSING in validator | Edit `.env` with missing variables, restart |
| Email not sending | Check `SMTP_USERNAME`/`SMTP_PASSWORD`. Gmail: use App Password |
| AI helpers failing | Set `GEMINI_API_KEY` or `GROQ_API_KEY` in `.env` |
| Port 8080/8081 in use | Change in `application.yml` / `docker-compose.yml` |

## File Placement

```
src/main/java/com/cafeflow/workflows/<workflow-name>/
    WorkflowInterface.java
    WorkflowImpl.java
    Activities.java
    ActivitiesImpl.java
    Models.java
```

---

# CafeFlow - Guia de Inicio Rapido (PT-BR)

## O Fluxo

```
 1. Clone          git clone → cd cafeflow
       ↓
 2. Docker         docker-compose up -d
       ↓
 3. Antigravity    Abrir projeto → escrever prompt → gerar workflow
       ↓
 4. Configurar     Antigravity diz o que configurar → cp .env.example .env → preencher valores
       ↓
 5. Executar       mvn spring-boot:run → ConfigurationValidator confirma status
       ↓
 6. Monitorar      http://localhost:8081
```

## Passo a Passo

O fluxo e identico ao guia em ingles acima. Resumo:

```bash
git clone https://github.com/anthropic/cafeflow.git && cd cafeflow
docker-compose up -d                    # 1. infraestrutura
# Abra no Google Antigravity → prompt   # 2. gerar workflow
cp .env.example .env                    # 3. configurar (so o que o Antigravity indicou)
mvn spring-boot:run                     # 4. executar
# http://localhost:8081                 # 5. monitorar
```

**Ponto-chave**: voce nao precisa adivinhar quais variaveis configurar. O Antigravity detecta quais helpers o workflow usa e diz exatamente o que colocar no `.env`.

## Exemplos de Prompts

```
Crie um CafeFlow workflow que:
1. Busca top 5 posts do subreddit "machinelearning"
2. Resume cada post usando TextSummarizerHelper
3. Traduz para portugues usando TextTranslatorHelper
4. Envia digest traduzido por email
```

```
Crie um CafeFlow workflow que:
1. Busca top 20 posts do subreddit "product"
2. Analisa sentimento usando SentimentAnalyzerHelper
3. Filtra posts negativos (confianca > 0.7)
4. Envia alerta Telegram para cada post negativo
5. Executa a cada 4 horas
```

## Obtendo Credenciais

| Servico | Como obter |
|---------|-----------|
| Gmail SMTP | Ativar 2FA → Conta Google > Seguranca > Senhas de App |
| Gemini (IA) | https://aistudio.google.com/apikey |
| Groq (IA) | https://console.groq.com/keys |
| Telegram | @BotFather → `/newbot` |
| Twitter/X | https://developer.twitter.com |
| Google Drive | Google Cloud Console → Drive API → credentials.json |

## Solucao de Problemas

| Problema | Solucao |
|----------|---------|
| Temporal nao conecta | `docker-compose ps` — verifique se servicos estao `Up` |
| ⚠️ MISSING no validator | Edite `.env` com variaveis faltantes, reinicie |
| Email nao envia | Verifique `SMTP_USERNAME`/`SMTP_PASSWORD`. Gmail: use Senha de App |
| Helpers IA nao funcionam | Configure `GEMINI_API_KEY` ou `GROQ_API_KEY` no `.env` |

Para detalhes completos (codigo de exemplo, arquitetura, Docker), veja o [guia em ingles acima](#the-flow).
