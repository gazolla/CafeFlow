# Fluxo de Trabalho para Criação de Novas Funcionalidades

Este documento define o passo a passo para o agente criar um novo Workflow ou adicionar uma nova funcionalidade (como um Helper) ao projeto CafeFlow.

## 1. Análise do Pedido

- **Entrada:** Um prompt do usuário descrevendo a funcionalidade desejada.
- **Ação:**
    1.  Interprete o pedido para identificar os componentes a serem criados ou modificados.
    2.  Determine quais `Helpers` existentes serão utilizados (ex: `EmailHelper`, `RedditHelper`).
    3.  Identifique se um novo `Helper` precisa ser criado (ex: integração com uma API que ainda não existe no projeto).
    4.  Defina a assinatura do Workflow (entradas) e das Activities.

## 2. Geração de Código (Usando a Skill `cafeflow-workflow-creator`)

- **Objetivo:** Gerar o esqueleto de todos os arquivos necessários.

- **Ação:**
    1.  Use o template `Workflow` da skill `cafeflow-workflow-creator` para criar a `Interface` e a `Impl` do novo workflow.
        - `{{WorkflowName}}Workflow.java`
        - `{{WorkflowName}}WorkflowImpl.java`
    2.  Use o template `Activities` para criar a `Interface` e a `Impl` das atividades que darão suporte ao workflow.
        - `{{WorkflowName}}Activities.java`
        - `{{WorkflowName}}ActivitiesImpl.java`
    3.  Se um novo `Helper` for necessário, use o template `Helper` para criar a classe.
        - `{{HelperName}}Helper.java`
    4.  Coloque os arquivos gerados no diretório correto, conforme a regra de `File Placement` do projeto: `src/main/java/com/cafeflow/workflows/<workflow-name>/` para workflows e `src/main/java/com/cafeflow/helpers/<category>/` para helpers.

## 3. Implementação da Lógica de Negócio

- **Objetivo:** Preencher o esqueleto gerado com a lógica de negócio específica.

- **Ação:**
    1.  **Workflow (`...WorkflowImpl.java`):**
        - Orquestre as chamadas para as Activities.
        - Implemente a lógica de controle (loops, condicionais).
        - **LEMBRE-SE:** Respeite 100% as [regras de determinismo do Temporal](./../rules/temporal-determinism.md).
    2.  **Activities (`...ActivitiesImpl.java`):**
        - Injete os `Helpers` necessários (`@RequiredArgsConstructor`).
        - Faça as chamadas para os `Helpers` para interagir com o mundo externo (APIs, bancos de dados, etc.).
        - Manipule os dados (transformação, filtragem).
    3.  **Helper (`...Helper.java`):**
        - Implemente a interação de baixo nível com a API externa.
        - Defina as variáveis de ambiente necessárias no método `getRequiredVars()` para integração com o `ConfigurationValidator`.

## 4. Atualização da Configuração

- **Objetivo:** Informar ao usuário (e ao sistema) sobre as novas necessidades de configuração.

- **Ação:**
    1.  Atualize o arquivo `.env.example` com as novas variáveis de ambiente exigidas pelo novo `Helper`.
    2.  Instrua o usuário a configurar as novas variáveis em seu arquivo `.env`.

## 5. Validação e Teste

- **Objetivo:** Garantir que a nova funcionalidade funciona e não quebra nada.

- **Ação:**
    1.  Execute o `ConfigurationValidator` (iniciando a aplicação com `mvn spring-boot:run`) para confirmar que a nova configuração é detectada e validada.
    2.  (Opcional, mas recomendado) Crie um teste de unidade para o workflow usando `temporal-testing` para verificar a lógica de orquestração sem depender da infraestrutura externa.
    3.  Execute o workflow de ponta a ponta e monitore na UI do Temporal (`http://localhost:8081`).
