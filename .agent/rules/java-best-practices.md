# Regras de Boas Práticas e Convenções de Código em Java

Este arquivo define os padrões de código e as convenções a serem seguidas no projeto CafeFlow para garantir consistência, legibilidade e manutenibilidade, com foco em funcionalidades modernas do Java 21.

## 1. Injeção de Dependência

- **Use Injeção pelo Construtor:** Sempre prefira injeção de dependência via construtor para componentes Spring.
- **Use `final` e `@RequiredArgsConstructor`:** Declare as dependências como `private final` e use a anotação `@RequiredArgsConstructor` do Lombok para gerar o construtor automaticamente. Isso garante a imutabilidade das dependências.

**Exemplo:**
```java
@Component
@RequiredArgsConstructor // Gera o construtor para campos 'final'
public class MeuServico {
    private final OutroServico outroServico;
    private final MeuRepositorio meuRepositorio;

    // Construtor é gerado pelo Lombok
}
```

## 2. Logging

- **Use SLF4J com Lombok:** Utilize a anotação `@Slf4j` do Lombok para obter uma instância do logger. Não instancie o logger manualmente.

**Exemplo:**
```java
@Slf4j
@Component
public class MeuComponente {
    public void fazerAlgo() {
        log.info("Executando a operação...");
        try {
            // ...
        } catch (Exception e) {
            log.error("Ocorreu um erro ao fazer algo", e);
        }
    }
}
```

## 3. Estrutura de Helpers

- **Herde de `BaseHelper`:** Todos os `Helpers` (classes que interagem com sistemas externos) devem estender `com.cafeflow.core.base.BaseHelper`.
- **Implemente `getServiceName()`:** Sobrescreva o método `getServiceName()` para retornar um identificador único em minúsculas (ex: "email", "reddit", "google-drive"). Este nome é usado pelo `ConfigurationValidator`.
- **Use `executeWithProtection()`:** Encapsule a lógica principal de cada método público do helper dentro de uma chamada a `executeWithProtection()`.

## 4. Records para Imutabilidade (DTOs)

- **Prefira Records para DTOs:** Para objetos de transferência de dados (DTOs) ou modelos de dados imutáveis, use `records` do Java 17+.
- **Validação no Construtor Compacto:** Use o construtor compacto para validações.

**Exemplo:**
```java
public record User(UUID id, String name, String email) {
    // Construtor compacto para validação
    public User {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(email, "email cannot be null");
        if (!email.contains("@")) {
            throw new IllegalArgumentException("invalid email");
        }
    }
}
```

## 5. Hierarquias Controladas com Sealed Classes

- Use `sealed` interfaces ou classes para definir explicitamente quais classes podem implementá-las ou estendê-las. Isso funciona muito bem com `records` e `pattern matching`.

**Exemplo:**
```java
public sealed interface PaymentResult 
    permits PaymentSuccess, PaymentFailure, PaymentPending {
}

public record PaymentSuccess(String transactionId) implements PaymentResult {}
public record PaymentFailure(String errorCode) implements PaymentResult {}
public record PaymentPending(String pendingId) implements PaymentResult {}
```

## 6. Pattern Matching

- **`instanceof` Inteligente:** Simplifique a verificação de tipo e o casting.
- **`switch` Avançado:** Use `switch` como uma expressão para tratar múltiplos tipos e condições de forma limpa, especialmente com `sealed classes`.

**Exemplo:**
```java
// Pattern matching para instanceof
if (obj instanceof String s && s.length() > 5) {
    System.out.println(s.toUpperCase());
}

// Pattern matching para switch com sealed classes
String describeResult(PaymentResult result) {
    return switch (result) {
        case PaymentSuccess s -> "Success, T_ID: " + s.transactionId();
        case PaymentFailure f -> "Failure, Code: " + f.errorCode();
        case PaymentPending p -> "Pending, P_ID: " + p.pendingId();
    };
}
```

## 7. API de Streams

- **Prefira Streams a Loops:** Use a API de Streams para processamento de coleções. É mais declarativo e menos propenso a erros.
- **Use Coletores:** Utilize a classe `Collectors` para agrupar, transformar e sumarizar dados.
- **`flatMap` para Estruturas Aninhadas:** Use `flatMap` para achatar listas de listas.

**Exemplo:**
```java
// Agrupar ordens por status
Map<Status, List<Order>> ordersByStatus = orders.stream()
    .collect(Collectors.groupingBy(Order::status));

// Obter todas as tags únicas de produtos dentro de uma lista de ordens
List<String> allTags = orders.stream()
    .flatMap(order -> order.items().stream())
    .flatMap(item -> item.product().tags().stream())
    .distinct()
    .sorted()
    .toList();
```

## 8. Uso Correto de `Optional`

- **Apenas para Retornos:** Use `Optional` exclusivamente como tipo de retorno para métodos que podem não encontrar um valor.
- **NUNCA como Parâmetro ou Campo:** Não use `Optional` como parâmetro de método ou campo de classe.

**Exemplo:**
```java
// CORRETO: Tipo de retorno
public Optional<User> findById(UUID id) { ... }

// CORRETO: Processamento encadeado
String username = findById(id)
    .map(User::name)
    .orElse("Convidado");

// INCORRETO: Campo de classe
public class User {
    private Optional<String> nickname; // NÃO FAÇA ISSO
}
```

## 9. Text Blocks para Strings Multi-linha

- Para JSON, SQL, ou qualquer string longa, use Text Blocks (`"""`) para melhorar a legibilidade.

**Exemplo:**
```java
String json = """
    {
        "name": "%s",
        "email": "%s"
    }
    """.formatted(name, email);
```
