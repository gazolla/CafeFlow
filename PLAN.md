# Plano: Fazer o Antigravity Seguir as Regras

## Diagnóstico

O Antigravity **não carrega `AGENTS.md` automaticamente**. Ele lê regras de:
1. `.agent/rules/*.md` — **always-on** (system prompt persistente)
2. `.agent/skills/*/SKILL.md` — **on-demand** (carregado por matching semântico)
3. `.cursorrules` — legado, carregamento inconsistente

Problemas atuais:
- **Não existe `.agent/rules/`** — logo ZERO regras always-on estão ativas
- O `SKILL.md` principal não tem **YAML frontmatter** (campo `description` é obrigatório para matching semântico)
- O `SKILL.md` diz "Read AGENTS.md first" — mas o agente ignora essa instrução indireta
- Regras críticas (record vs @Data, no @Slf4j em WorkflowImpl) existem apenas no AGENTS.md não-carregado

## Estratégia

Mover as regras do AGENTS.md para `.agent/rules/` (always-on) e reforçar nos SKILLs.

```
ANTES (não funciona):
  AGENTS.md ─────────────────── ✗ Antigravity NÃO lê
  .cursorrules ──────────────── ✗ Deprecated, inconsistente
  .agent/rules/ ─────────────── ✗ NÃO EXISTE
  skills/*/SKILL.md ─────────── △ Sem frontmatter, matching fraco

DEPOIS (funciona):
  .agent/rules/*.md ─────────── ✓ Always-on, regras invioláveis
  skills/*/SKILL.md ─────────── ✓ Com frontmatter, matching forte
  AGENTS.md ─────────────────── ✓ Mantido para Claude Code/Codex/etc.
  .cursorrules ──────────────── ✗ DELETAR (deprecated, redundante)
```

## Arquivos a Criar/Modificar

### 1. CRIAR: `.agent/rules/java-standards.md`
Regras always-on de Java. Conteúdo extraído do AGENTS.md:
- `record` obrigatório para DTOs (com exemplo correto + incorreto)
- `@JsonIgnoreProperties` em todos os records
- `@RequiredArgsConstructor` para DI (nunca `@Autowired`)
- Text blocks para strings multi-linha
- Imports organizados no topo

### 2. CRIAR: `.agent/rules/temporal-rules.md`
Regras always-on de Temporal. Conteúdo extraído do AGENTS.md:
- WorkflowImpl: NO `@Component`, NO `@Slf4j`, NO `@RequiredArgsConstructor`
- WorkflowImpl: `Workflow.getLogger()`, nunca SLF4J
- WorkflowImpl: ZERO I/O (com lista de proibições explícitas)
- ActivityImpl: YES `@Component` + `@Slf4j` + `@RequiredArgsConstructor`
- Interface + Impl sempre separados

### 3. CRIAR: `.agent/rules/project-structure.md`
Regras always-on de organização:
- DTOs FLAT no package do workflow (NUNCA criar `dto/`, `model/`)
- Um workflow por package em `workflows/`
- Helpers em `helpers/[category]/`
- Scheduler em classe separada `@Configuration` (NUNCA no `CafeFlowApplication.java`)
- NUNCA criar controllers, REST endpoints, novos config classes

### 4. CRIAR: `.agent/rules/execution-policy.md`
Regras always-on de execução:
- NUNCA rodar `mvn spring-boot:run`
- NUNCA criar arquivos de debug/diagnóstico
- NUNCA hardcodar API keys ou secrets
- NUNCA gerar docker-compose.yml ou Dockerfile
- NUNCA gerar arquivos de log na raiz do projeto
- Verificar `mvn compile` no máximo
- Verificar Docker com `docker ps` antes de instruir o usuário

### 5. MODIFICAR: `.agent/skills/cafeflow-workflow-creator/SKILL.md`
- Adicionar YAML frontmatter com `name` e `description` para matching semântico
- Remover a instrução "Read AGENTS.md first" (agora redundante — rules são always-on)
- Adicionar checklist de validação pós-geração com as regras críticas

### 6. MODIFICAR: `.agent/skills/temporal-orchestrator/SKILL.md`
- Já tem frontmatter ✓, apenas verificar se description é específica o suficiente

### 7. MODIFICAR: `.agent/skills/temporal-scheduler/SKILL.md`
- Já tem frontmatter ✓, apenas verificar se description é específica o suficiente

### 8. MANTER: `AGENTS.md`
- Manter como está para compatibilidade com Claude Code, Codex, e outros agentes
- Adicionar header dizendo que as regras canônicas estão em `.agent/rules/`

### 9. DELETAR: `.cursorrules`
- Deprecated pelo Cursor, não lido pelo Antigravity de forma confiável
- Toda a informação agora está em `.agent/rules/` + `AGENTS.md`

## Princípios de Escrita das Regras

Baseado na pesquisa sobre eficácia de regras para LLMs:

1. **Exemplos > Descrições** — Sempre mostrar código CORRETO e INCORRETO
2. **Curtas e focadas** — Um arquivo por domínio, <100 linhas cada
3. **Linguagem imperativa** — "NUNCA use X" em vez de "Evite X quando possível"
4. **Contra-exemplos explícitos** — Mostrar o que NÃO fazer com comentário de erro
5. **Sem ambiguidade** — Cada regra deve ter uma única interpretação possível
