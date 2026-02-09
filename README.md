# CafeFlow

**Build automation workflows with natural language prompts using Google Antigravity IDE.**

*[Portugues (PT-BR)](#cafeflow-pt-br)*

## How It Works

```
git clone → docker-compose up -d → Antigravity prompt → configure .env → mvn spring-boot:run
```

1. **Clone** the project and start Docker infrastructure
2. **Open Google Antigravity IDE** and describe your workflow in natural language
3. **Antigravity generates** the complete code + tells you which env vars to configure
4. **Configure `.env`** with only the variables your workflow needs
5. **Run** — `ConfigurationValidator` confirms everything is ready

```bash
git clone https://github.com/anthropic/cafeflow.git && cd cafeflow
docker-compose up -d
# Open in Google Antigravity → write prompt → generate workflow
cp .env.example .env    # edit with your API keys
mvn spring-boot:run     # ConfigurationValidator shows helper status
```

## Prompt Examples

```
Create a CafeFlow workflow that:
1. Fetches the 5 most upvoted posts from subreddit "java"
2. Summarizes each post using TextSummarizerHelper
3. Sends the digest via email to team@company.com
4. Runs every day at 8:00 AM
```

```
Create a CafeFlow workflow that:
1. Fetches top 20 posts from subreddit "product"
2. Analyzes sentiment using SentimentAnalyzerHelper
3. Filters negative posts with confidence > 0.7
4. Sends Telegram alert for each negative post
```

```
Create a CafeFlow workflow that:
1. Fetches top 5 posts from subreddit "machinelearning"
2. Translates titles to Portuguese using TextTranslatorHelper
3. Generates hashtags using TopicExtractorHelper
4. Sends translated digest via email
```

## Available Helpers

| Helper | Category | Description |
|--------|----------|-------------|
| **RedditHelper** | Marketing | Fetch top posts from any subreddit (no config needed) |
| **EmailHelper** | Communication | SMTP email (Gmail, Outlook, any provider) |
| **TelegramHelper** | Communication | Send messages, inline menus, notifications |
| **TwitterHelper** | Marketing | User lookup, post tweets (X API v2) |
| **GDriveHelper** | Office | List, upload, download files (OAuth2) |
| **TextSummarizerHelper** | AI | Text summarization |
| **SentimentAnalyzerHelper** | AI | Sentiment analysis with confidence score |
| **TextTranslatorHelper** | AI | Translation and language detection |
| **ContentGeneratorHelper** | AI | Generate emails, social posts, reports, tweets |
| **DataExtractorHelper** | AI | Extract fields, entities, key-values from text |
| **TextClassifierHelper** | AI | Classify text into categories, yes/no questions |
| **TopicExtractorHelper** | AI | Extract topics, hashtags, keywords |

## Environment Variables

Configure via `.env` file (copy from `.env.example`). Loaded automatically by `spring-dotenv`.

| Variable | Helper(s) | Description |
|----------|-----------|-------------|
| `GEMINI_API_KEY` | All AI helpers | Google AI Studio key |
| `GROQ_API_KEY` | All AI helpers | Alternative LLM provider |
| `SMTP_USERNAME` / `SMTP_PASSWORD` | EmailHelper | SMTP credentials (Gmail: use App Password) |
| `TELEGRAM_BOT_TOKEN` | TelegramHelper | From @BotFather |
| `X_BEARER_TOKEN` | TwitterHelper | X/Twitter API |
| `GD_CREDENTIALS_PATH` | GDriveHelper | Google OAuth2 JSON |

*Only configure what your workflow uses. AI helpers need at least one LLM provider (Gemini or Groq). Full variable list in `.env.example`.*

## Docker Infrastructure

```bash
docker-compose up -d    # Start all services
docker-compose ps       # Verify status
docker-compose down     # Stop
docker-compose down -v  # Full reset
```

| Service | Port | Purpose |
|---------|------|---------|
| PostgreSQL 15 | 5432 | Temporal state persistence |
| Temporal Server 1.24.0 | 7233 | Workflow orchestration |
| Temporal Web UI | 8081 | Monitoring dashboard |
| Temporal Admin Tools | - | CLI (`tctl`) |

## Tech Stack

Java 17+ / Spring Boot 3.2.2 / Temporal.io 1.24.1 / PostgreSQL 15 / Maven / Docker Compose

## Quick Start

See **[QUICKSTART.md](QUICKSTART.md)** for the full step-by-step guide.

## License

MIT

---

# CafeFlow (PT-BR)

**Crie workflows de automacao com prompts em linguagem natural usando o Google Antigravity IDE.**

## Como Funciona

```
git clone → docker-compose up -d → prompt no Antigravity → configurar .env → mvn spring-boot:run
```

1. **Clone** o projeto e inicie a infraestrutura Docker
2. **Abra o Google Antigravity IDE** e descreva seu workflow em linguagem natural
3. **O Antigravity gera** o codigo completo + diz quais variaveis de ambiente configurar
4. **Configure o `.env`** apenas com as variaveis que seu workflow precisa
5. **Execute** — o `ConfigurationValidator` confirma que tudo esta pronto

```bash
git clone https://github.com/anthropic/cafeflow.git && cd cafeflow
docker-compose up -d
# Abra no Google Antigravity → escreva o prompt → gere o workflow
cp .env.example .env    # edite com suas API keys
mvn spring-boot:run     # ConfigurationValidator mostra status dos helpers
```

## Exemplos de Prompts

```
Crie um CafeFlow workflow que:
1. Busca os 5 posts mais curtidos do subreddit "java"
2. Resume cada post usando TextSummarizerHelper
3. Envia o digest por email para equipe@empresa.com
4. Executa todo dia as 8:00
```

```
Crie um CafeFlow workflow que:
1. Busca top 20 posts do subreddit "product"
2. Analisa sentimento usando SentimentAnalyzerHelper
3. Filtra posts negativos com confianca > 0.7
4. Envia alerta Telegram para cada post negativo
```

```
Crie um CafeFlow workflow que:
1. Busca top 5 posts do subreddit "machinelearning"
2. Traduz titulos para portugues usando TextTranslatorHelper
3. Gera hashtags usando TopicExtractorHelper
4. Envia digest traduzido por email
```

## Referencia Rapida

Helpers disponiveis, variaveis de ambiente, infraestrutura Docker e stack tecnologica: veja as [secoes em ingles acima](#available-helpers) — o conteudo tecnico e identico.

Para o guia passo a passo completo: **[QUICKSTART.md](QUICKSTART.md)**

## Licenca

MIT
