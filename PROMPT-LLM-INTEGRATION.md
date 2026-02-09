# Prompt para o Google Antigravity

Cole o prompt abaixo no Google Antigravity com o projeto CafeFlow aberto.

---

## PROMPT

```
Analise o projeto CafeFlow e execute as 4 tarefas abaixo em sequencia.
Respeite TODOS os padroes existentes: BaseHelper, @Component, executeWithProtection(),
@Value para configuracao, e a estrutura de pacotes do projeto.

O pacote com.cafeflow.core.llm ja existe com: LLMClient (interface), BaseLLMClient (abstract),
GeminiClient, GroqClient e LLMClientFactory. Esses clients NAO sao Spring components —
sao criados via construtor/factory com API key.

==========================================================================
TAREFA 1: Registrar LLM como beans Spring
==========================================================================

Crie o arquivo: src/main/java/com/cafeflow/core/llm/LLMConfig.java

Requisitos:
- Anotacao @Configuration
- Leia as API keys e o provider padrao do application.yml usando @Value
- Crie beans condicionais: se a API key do Gemini existir, crie o bean GeminiClient.
  Se a API key do Groq existir, crie o bean GroqClient.
- Crie um bean @Primary chamado "llmClient" que retorna o client do provider padrao
  (definido por llm.default-provider, default "gemini")
- Use LLMClientFactory internamente
- Adicione log.info no PostConstruct mostrando qual provider foi inicializado
- Se nenhuma API key estiver configurada, logue um warn e NAO quebre a aplicacao

Exemplo de uso esperado depois:
  @Autowired private LLMClient llmClient;

==========================================================================
TAREFA 2: Adicionar configuracao ao application.yml
==========================================================================

Adicione a seguinte secao ao arquivo src/main/resources/application.yml
(mantendo TUDO que ja existe, nao remova nada):

llm:
  default-provider: ${LLM_DEFAULT_PROVIDER:gemini}
  gemini:
    api-key: ${GEMINI_API_KEY:}
    model: ${GEMINI_MODEL:gemini-1.5-flash}
  groq:
    api-key: ${GROQ_API_KEY:}
    model: ${GROQ_MODEL:llama-3.3-70b-versatile}

==========================================================================
TAREFA 3: Criar helpers AI
==========================================================================

Crie 2 novos helpers no pacote: src/main/java/com/cafeflow/helpers/ai/

Ambos DEVEM:
- Estender BaseHelper
- Ter @Component e @Slf4j
- Usar @RequiredArgsConstructor para injetar LLMClient
- Usar executeWithProtection() para TODA chamada ao LLM
- Implementar getServiceName()

--- HELPER 1: TextSummarizerHelper.java ---

Pacote: com.cafeflow.helpers.ai
Service name: "text_summarizer"

Metodos:
1. String summarize(String text)
   - Envia prompt para o LLM: "Summarize the following text in 2-3 concise sentences:\n\n" + text
   - Retorna a resposta do LLM

2. String summarize(String text, int maxSentences)
   - Envia prompt para o LLM: "Summarize the following text in exactly {maxSentences} sentences:\n\n" + text
   - Retorna a resposta do LLM

3. List<String> summarizeBatch(List<String> texts)
   - Chama summarize(text) para cada texto da lista
   - Retorna lista de resumos

4. String summarizeToLanguage(String text, String targetLanguage)
   - Envia prompt: "Summarize the following text in 2-3 sentences. Write the summary in {targetLanguage}:\n\n" + text
   - Retorna a resposta

--- HELPER 2: SentimentAnalyzerHelper.java ---

Pacote: com.cafeflow.helpers.ai
Service name: "sentiment_analyzer"

Crie tambem um record no mesmo pacote:
SentimentResult.java com campos: String sentiment, double confidence, String explanation

Metodos:
1. SentimentResult analyze(String text)
   - Envia prompt para o LLM pedindo analise de sentimento
   - O prompt DEVE instruir o LLM a responder SOMENTE em JSON:
     {"sentiment": "positive|negative|neutral", "confidence": 0.0-1.0, "explanation": "..."}
   - Faca parse do JSON de resposta usando ObjectMapper para SentimentResult
   - Se o parse falhar, retorne new SentimentResult("unknown", 0.0, "Failed to parse LLM response")

2. List<SentimentResult> analyzeBatch(List<String> texts)
   - Chama analyze(text) para cada texto
   - Retorna lista de resultados

3. String classifySimple(String text)
   - Chama analyze(text) e retorna apenas o campo sentiment (String)

==========================================================================
TAREFA 4: Atualizar o skill cafeflow-workflow-creator
==========================================================================

Edite o arquivo: .agent/skills/cafeflow-workflow-creator/SKILL.md

Adicione as seguintes informacoes ao skill EXISTENTE (nao substitua, ADICIONE):

4A) Na secao "## Available Framework Helpers", depois dos helpers existentes,
    adicione uma nova subsecao:

### AI Helpers (LLM-powered)

**TextSummarizerHelper** (`helpers/ai/`):
- Requires: LLM API key configured (Gemini or Groq)
- Methods: `summarize(text)`, `summarize(text, maxSentences)`, `summarizeBatch(texts)`, `summarizeToLanguage(text, language)`
- Use when: Workflow needs to summarize posts, articles, emails, documents
- Configuration: `GEMINI_API_KEY` or `GROQ_API_KEY` environment variable

**SentimentAnalyzerHelper** (`helpers/ai/`):
- Requires: LLM API key configured (Gemini or Groq)
- Methods: `analyze(text)`, `analyzeBatch(texts)`, `classifySimple(text)`
- Returns: SentimentResult record with sentiment, confidence, explanation
- Use when: Workflow needs to classify sentiment, filter by mood, detect negativity
- Configuration: `GEMINI_API_KEY` or `GROQ_API_KEY` environment variable

4B) Na secao "## Common Patterns", adicione NOVOS patterns:

### Pattern 6: LLM-Enhanced Workflow
```
User: "Summarize Reddit posts before sending email"
Helpers: RedditHelper (exists) + TextSummarizerHelper (exists) + EmailHelper (needs config)
Action: Compose all three in activities
Files: Workflow + Activities (no new helpers needed)
```

### Pattern 7: Sentiment-Filtered Workflow
```
User: "Monitor Reddit and alert only on negative posts"
Helpers: RedditHelper + SentimentAnalyzerHelper + TelegramHelper
Action: Fetch → Analyze sentiment → Filter negative → Send alert
Files: Workflow + Activities (no new helpers needed)
```

### Pattern 8: Multi-Language Workflow
```
User: "Summarize posts in Portuguese and send to Telegram"
Helpers: RedditHelper + TextSummarizerHelper (summarizeToLanguage) + TelegramHelper
Action: Fetch → Summarize to pt-BR → Send
Files: Workflow + Activities (no new helpers needed)
```

4C) Na secao "## Critical Architecture Principles", adicione:

### 5. LLM Integration Layer
**IMPORTANT**: The `core.llm` package provides AI capabilities as infrastructure.
- LLMClient is registered as a Spring bean via LLMConfig
- Helpers in `helpers/ai/` wrap LLMClient with BaseHelper protection
- NEVER call LLMClient directly in workflows or activities — always use AI helpers
- AI helpers can be composed with ANY other helper (Reddit + Summarizer + Email)
- At least one LLM API key (GEMINI_API_KEY or GROQ_API_KEY) must be set for AI helpers to work

4D) Na secao "## Helper Creation Checklist", adicione ao final:

### AI Helper Checklist (additional)
- [ ] Injects LLMClient (not creates manually)
- [ ] Prompt engineering is clear and specific
- [ ] Handles LLM response parsing errors gracefully
- [ ] Falls back to sensible default on failure
- [ ] Does NOT expose raw LLM responses to workflows

4E) Adicione ao arquivo .agent/skills/cafeflow-workflow-creator/references/helpers-reference.md
    uma nova secao ao final:

---

### 🤖 AI Helpers (LLM-Powered)

#### TextSummarizerHelper
**Location**: `helpers/ai/TextSummarizerHelper.java`

**Status**: Fully implemented - requires LLM API key

**Methods**:
```java
String summarize(String text)
String summarize(String text, int maxSentences)
List<String> summarizeBatch(List<String> texts)
String summarizeToLanguage(String text, String targetLanguage)
```

**Example**:
```java
@RequiredArgsConstructor
public class MyActivitiesImpl {
    private final RedditHelper redditHelper;
    private final TextSummarizerHelper summarizerHelper;
    private final EmailHelper emailHelper;

    public void fetchAndSummarize() {
        List<RedditPost> posts = redditHelper.fetchTopPosts("java", 5, RedditPost.class);
        List<String> titles = posts.stream().map(RedditPost::title).toList();
        List<String> summaries = summarizerHelper.summarizeBatch(titles);
        emailHelper.sendTextEmail("user@email.com", "Daily Digest", String.join("\n\n", summaries));
    }
}
```

**Configuration**: `GEMINI_API_KEY` or `GROQ_API_KEY` env var

---

#### SentimentAnalyzerHelper
**Location**: `helpers/ai/SentimentAnalyzerHelper.java`

**Status**: Fully implemented - requires LLM API key

**Methods**:
```java
SentimentResult analyze(String text)
List<SentimentResult> analyzeBatch(List<String> texts)
String classifySimple(String text)
```

**SentimentResult record**:
```java
record SentimentResult(String sentiment, double confidence, String explanation)
```

**Example**:
```java
@RequiredArgsConstructor
public class MyActivitiesImpl {
    private final RedditHelper redditHelper;
    private final SentimentAnalyzerHelper sentimentHelper;
    private final TelegramHelper telegramHelper;

    public void alertOnNegative() {
        List<RedditPost> posts = redditHelper.fetchTopPosts("product", 10, RedditPost.class);
        for (RedditPost post : posts) {
            SentimentResult result = sentimentHelper.analyze(post.title());
            if ("negative".equals(result.sentiment()) && result.confidence() > 0.7) {
                telegramHelper.sendNotification("CHAT_ID",
                    "⚠️ Negative post: " + post.title() + "\n" + result.explanation());
            }
        }
    }
}
```

**Configuration**: `GEMINI_API_KEY` or `GROQ_API_KEY` env var

**Decision Matrix (updated)**:

| Situation | Strategy | Location |
|-----------|----------|----------|
| Need to summarize text | Use TextSummarizerHelper | helpers/ai/ |
| Need sentiment analysis | Use SentimentAnalyzerHelper | helpers/ai/ |
| Need custom LLM behavior | Create new AI helper in helpers/ai/ | helpers/ai/ |
| Need LLM in one workflow only | Create workflow helper extending BaseHelper + inject LLMClient | workflows/[name]/helpers/ |

==========================================================================
FIM DAS TAREFAS
==========================================================================

Depois de completar as 4 tarefas, mostre:
1. Lista completa de arquivos criados ou modificados
2. As variaveis de ambiente novas que precisam ser configuradas
3. Um exemplo de prompt de workflow que usa os novos AI helpers

Nao crie testes unitarios. Nao modifique nenhum arquivo que nao foi mencionado.
```

---

## Variaveis de ambiente novas (adicionar ao .env)

```env
# LLM Configuration
LLM_DEFAULT_PROVIDER=gemini
GEMINI_API_KEY=your-gemini-api-key
GEMINI_MODEL=gemini-1.5-flash
GROQ_API_KEY=your-groq-api-key
GROQ_MODEL=llama-3.3-70b-versatile
```

## Arquivos que serao criados/modificados

| Arquivo | Acao |
|---------|------|
| `src/main/java/com/cafeflow/core/llm/LLMConfig.java` | CRIAR |
| `src/main/resources/application.yml` | MODIFICAR (adicionar secao llm) |
| `src/main/java/com/cafeflow/helpers/ai/TextSummarizerHelper.java` | CRIAR |
| `src/main/java/com/cafeflow/helpers/ai/SentimentAnalyzerHelper.java` | CRIAR |
| `src/main/java/com/cafeflow/helpers/ai/SentimentResult.java` | CRIAR |
| `.agent/skills/cafeflow-workflow-creator/SKILL.md` | MODIFICAR |
| `.agent/skills/cafeflow-workflow-creator/references/helpers-reference.md` | MODIFICAR |

## Exemplo de workflow apos execucao

```
Crie um CafeFlow workflow que:
1. Busca os top 10 posts do subreddit "artificial"
2. Usa o TextSummarizerHelper para resumir cada post em 1 frase em portugues
3. Usa o SentimentAnalyzerHelper para classificar o sentimento de cada post
4. Filtra apenas posts positivos com confianca acima de 0.7
5. Monta um digest com resumos e sentimentos
6. Envia por email para equipe@empresa.com
7. Envia notificacao no Telegram dizendo "Digest AI pronto!"
8. Executa todo dia as 8:00
```
