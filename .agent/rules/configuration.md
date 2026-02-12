# Regras de Configuração e Validação

Este documento descreve como a configuração do ambiente é gerenciada e validada no CafeFlow.

## 1. Arquivo de Ambiente `.env`

- **Fonte da Verdade:** A configuração específica do ambiente (chaves de API, senhas, URLs) **DEVE** ser armazenada em um arquivo `.env` na raiz do projeto.
- **NÃO FAÇA COMMIT do `.env`:** O arquivo `.env` contém segredos e nunca deve ser versionado no Git. O arquivo `.gitignore` já está configurado para ignorá-lo.
- **Modelo de Exemplo:** O arquivo `.env.example` serve como um template de todas as variáveis de ambiente possíveis. Ao adicionar uma nova configuração, sempre atualize o `.env.example` com a variável comentada e uma breve descrição.

## 2. Carregamento de Configuração

- **Spring-Dotenv:** O projeto usa a biblioteca `me.paulschwarz.spring-dotenv` para carregar automaticamente as variáveis do arquivo `.env` para o ambiente do Spring. Não é necessário nenhum `source` ou `export` manual.
- **Acesso via `@Value`:** As propriedades podem ser injetadas em componentes Spring usando a anotação `@Value("${NOME_DA_VARIAVEL}")`.

## 3. Validação na Inicialização (`ConfigurationValidator`)

- **Validação Obrigatória:** O CafeFlow usa uma classe `ConfigurationValidator` que é executada na inicialização da aplicação para verificar se todas as dependências de configuração para os `Helpers` utilizados estão satisfeitas.
- **Como Funciona:**
    1. Cada `BaseHelper` expõe suas variáveis de ambiente necessárias através do método `getRequiredVars()`.
    2. O `ConfigurationValidator` inspeciona todos os beans do tipo `BaseHelper` no contexto do Spring.
    3. Ele verifica se o `ApplicationContext` do Spring possui as propriedades (variáveis) que cada helper declarou como obrigatórias.
    4. Ele imprime um relatório de status no console.
- **Status do Relatório:**
    - **✅ ready**: O Helper está configurado corretamente e pronto para uso.
    - **⚠️ MISSING**: Uma ou mais variáveis de ambiente obrigatórias para o Helper não foram definidas no `.env`. **A aplicação falhará ao iniciar.**
    - **⬚ not active**: O Helper não é uma dependência ativa de nenhum workflow em execução e pode ser ignorado.

## Regra Fundamental

Ao criar um novo `Helper` que requer configuração (ex: uma chave de API), você **DEVE** sobrescrever o método `getRequiredVars()` e declarar as variáveis necessárias. Isso integra o seu helper ao sistema de validação automática.

**Exemplo em `MeuHelper.java`:**
```java
@Override
protected List<RequiredVar> getRequiredVars() {
    return List.of(
        new RequiredVar("MINHA_API_KEY", "A chave de API para o Meu Serviço."),
        new RequiredVar("MINHA_API_SECRET", "O segredo da API para o Meu Serviço.")
    );
}
```
