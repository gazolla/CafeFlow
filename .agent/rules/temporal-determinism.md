# Regra de Determinismo do Temporal

Esta é uma lei fundamental para garantir a resiliência e a corretude dos workflows no CafeFlow.

**NUNCA** use operações não determinísticas diretamente dentro de uma classe que implementa uma interface de Workflow do Temporal (geralmente `XxxWorkflowImpl`).

## Operações Proibidas em Workflows

- **Relógio do sistema:** Não use `System.currentTimeMillis()` ou `new Date()`.
- **Aleatoriedade:** Não use `java.util.Random`, `Math.random()`, ou qualquer outra fonte de aleatoriedade não controlada.
- **Threads:** Não crie novas threads com `new Thread()` ou use pools de threads. Para concorrência, use as primitivas do Temporal como `Async.function` ou `Async.procedure`.
- **Chamadas de Rede/IO:** Não faça chamadas de rede, acesso a banco de dados ou leitura de arquivos diretamente do workflow.
- **Variáveis de Ambiente/Configuração:** Não acesse configurações do sistema ou variáveis de ambiente diretamente.

## A Forma Correta (The Temporal Way)

Para qualquer uma das operações acima, você **DEVE** usar a API do `io.temporal.workflow.Workflow` ou delegar a tarefa para uma **Atividade** (Activity).

| Operação Proibida | Alternativa Correta no Workflow | Exemplo |
|---|---|---|
| `System.currentTimeMillis()` | `Workflow.currentTimeMillis()` | `long now = Workflow.currentTimeMillis();` |
| `new Random()` | `Workflow.newRandom()` | `UUID randomId = Workflow.newRandom().nextUUID();` |
| `Thread.sleep()` | `Workflow.sleep(duration)` | `Workflow.sleep(Duration.ofMinutes(5));` |
| Chamada de API externa | Executar uma Atividade | `String result = anActivity.callApi(arg1);` |
| Ler `credentials.json` | Passar como parâmetro na Atividade | `String config = anActivity.readConfig(filePath);`|

## Por quê?

O código do workflow é re-executado (replayed) a partir do seu histórico de eventos para reconstruir o estado sempre que um worker reinicia. Se o código do workflow produzir resultados diferentes em cada execução (ex: `Math.random()`), a reconstrução do estado falhará, resultando em um `NonDeterministicWorkflowError`. As APIs do Temporal garantem que, durante a re-execução, os mesmos valores do histórico sejam usados, preservando o determinismo.
