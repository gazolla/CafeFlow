# Fluxo de Trabalho de Desenvolvimento e Execução

Este documento descreve o ciclo de vida padrão para configurar, executar e monitorar o projeto CafeFlow. Siga estas etapas na ordem correta.

## O Fluxo de 6 Passos

O fluxo operacional principal segue seis etapas sequenciais. Não pule etapas.

```
 1. Clone          → Obter o código-fonte
       ↓
 2. Docker         → Iniciar a infraestrutura (Temporal, Postgres)
       ↓
 3. Gerar          → Usar o agente para gerar o código do workflow a partir de um prompt
       ↓
 4. Configurar     → Preencher as variáveis de ambiente (.env) exigidas pelo novo workflow
       ↓
 5. Executar       → Iniciar a aplicação Spring Boot e validar a configuração
       ↓
 6. Monitorar      → Acompanhar a execução do workflow na UI do Temporal
```

## Etapa 1: Clone

Obtenha o código-fonte do repositório.
```bash
# Executar apenas uma vez
git clone <URL_DO_REPOSITORIO>
cd cafeflow-agentic
```

## Etapa 2: Docker (Infraestrutura)

A infraestrutura do Temporal é gerenciada via Docker Compose.

**Ação:** Verifique se o Docker Desktop está em execução e, em seguida, inicie os contêineres em modo detached.

```bash
# Verifique se o Docker está rodando
docker --version

# Inicie os serviços (Temporal Server, Web UI, Postgres)
docker-compose up -d
```

**Verificação:** Use `docker-compose ps` para garantir que os 4 serviços (`temporal`, `temporal-web`, `postgres`, `temporal-admin-tools`) estão com o status `Up` ou `Running`.

## Etapa 3: Gerar Workflow

Use o prompt do agente para gerar um novo workflow. Especifique as fontes de dados, a lógica de negócio e as ações de saída.

**Exemplo de Prompt:**
```
"Crie um workflow CafeFlow que busca os 5 principais posts do subreddit 'java', resume cada um usando o TextSummarizerHelper e envia o resumo para um canal do Discord."
```
O agente deve usar a skill `cafeflow-architect` para gerar os arquivos necessários.

## Etapa 4: Configurar Ambiente

Após a geração do código, o agente (ou o `ConfigurationValidator`) informará quais variáveis de ambiente são necessárias.

**Ação:**
1.  Se for a primeira vez, copie o arquivo de exemplo: `cp .env.example .env`
2.  Abra o arquivo `.env` e preencha **apenas** as variáveis exigidas pelo workflow recém-gerado (ex: `DISCORD_WEBHOOK_URL`, `GEMINI_API_KEY`, etc.).

## Etapa 5: Executar a Aplicação

Compile e execute a aplicação usando o Maven.

**Ação:**
```bash
mvn spring-boot:run
```
**Verificação:** Observe o log de inicialização. O `ConfigurationValidator` imprimirá um relatório. Procure por "✅ ready" para os helpers que você configurou. Se algum helper mostrar "⚠️ MISSING", pare a aplicação (`Ctrl+C`), corrija o arquivo `.env` e execute novamente.

## Etapa 6: Monitorar

Com a aplicação em execução, acesse a UI web do Temporal para observar os workflows.

**Ação:** Abra o navegador e acesse `http://localhost:8081`.

Você poderá ver o histórico de execuções, o status dos workflows (em execução, concluídos, falhados) e inspecionar as entradas e saídas de cada etapa.
