# Spring Boot Annotations Quick Reference

## Application & Configuration

| Annotation | Target | Purpose |
|---|---|---|
| `@SpringBootApplication` | Class | Entry point — combines `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan` |
| `@Configuration` | Class | Declares a Spring configuration class (source of `@Bean` definitions) |
| `@Bean` | Method | Registers the return value as a Spring bean |
| `@ConfigurationProperties(prefix)` | Class/Record | Binds `application.yml` properties to a type-safe object |
| `@ConfigurationPropertiesScan` | Class | Auto-detects `@ConfigurationProperties` classes |
| `@EnableAsync` | Class | Enables `@Async` method execution |
| `@EnableScheduling` | Class | Enables `@Scheduled` method execution |
| `@Profile("dev")` | Class/Method | Only active when the specified profile is active |
| `@ConditionalOnProperty` | Class/Method | Conditional bean registration based on property value |

## Component Scanning

| Annotation | Purpose |
|---|---|
| `@Component` | Generic Spring-managed bean |
| `@Service` | Business logic bean (specialization of `@Component`) |
| `@Repository` | Data access bean (specialization of `@Component`, adds exception translation) |
| `@Controller` | Web MVC controller — returns view names |
| `@RestController` | REST controller — `@Controller` + `@ResponseBody` on every method |

## Dependency Injection

| Annotation | Target | Purpose |
|---|---|---|
| `@Autowired` | Constructor/Field/Setter | Inject dependency (constructor injection preferred, implicit for single constructor) |
| `@Qualifier("name")` | Parameter/Field | Disambiguate when multiple beans of same type exist |
| `@Primary` | Class/Method | Default bean when multiple candidates exist |
| `@Value("${prop}")` | Field/Parameter | Inject a single property value |
| `@Lazy` | Class/Field | Delay bean initialization until first use |

## Web / REST

| Annotation | Target | Purpose |
|---|---|---|
| `@RequestMapping("/path")` | Class/Method | Map HTTP requests to handler — base path on class |
| `@GetMapping` | Method | Handle GET requests |
| `@PostMapping` | Method | Handle POST requests |
| `@PutMapping` | Method | Handle PUT requests |
| `@DeleteMapping` | Method | Handle DELETE requests |
| `@PatchMapping` | Method | Handle PATCH requests |
| `@PathVariable` | Parameter | Bind URI template variable — `/users/{id}` |
| `@RequestParam` | Parameter | Bind query parameter — `?name=value` |
| `@RequestBody` | Parameter | Bind HTTP request body (JSON → object) |
| `@ResponseBody` | Method/Class | Return value is the response body (not a view name) |
| `@ResponseStatus(HttpStatus.CREATED)` | Method/Class | Set HTTP status code |
| `@RequestHeader` | Parameter | Bind HTTP header value |
| `@CookieValue` | Parameter | Bind cookie value |
| `@CrossOrigin` | Class/Method | Enable CORS for specific handler |

## Validation

| Annotation | Target | Purpose |
|---|---|---|
| `@Valid` | Parameter | Trigger validation on `@RequestBody` |
| `@Validated` | Class | Enable validation on `@ConfigurationProperties` |
| `@NotNull` | Field | Must not be null |
| `@NotBlank` | Field | Must not be null or whitespace (strings) |
| `@NotEmpty` | Field | Must not be null or empty (strings, collections) |
| `@Size(min, max)` | Field | String length or collection size bounds |
| `@Min(value)` / `@Max(value)` | Field | Numeric range |
| `@Email` | Field | Must be valid email format |
| `@Pattern(regexp)` | Field | Must match regex |
| `@Positive` / `@Negative` | Field | Must be positive/negative number |
| `@Past` / `@Future` | Field | Date must be in past/future |

## Exception Handling

| Annotation | Target | Purpose |
|---|---|---|
| `@RestControllerAdvice` | Class | Global exception handler for `@RestController` (returns JSON) |
| `@ControllerAdvice` | Class | Global exception handler for `@Controller` (returns views) |
| `@ExceptionHandler(Ex.class)` | Method | Handle specific exception type |
| `@ResponseStatus(HttpStatus.NOT_FOUND)` | Class (exception) | Default HTTP status for this exception |

## Async & Scheduling

| Annotation | Target | Purpose |
|---|---|---|
| `@Async` | Method | Execute method asynchronously (returns `void`, `Future`, or `CompletableFuture`) |
| `@Async("executorName")` | Method | Use specific executor bean |
| `@Scheduled(fixedRate = 5000)` | Method | Run every 5 seconds |
| `@Scheduled(cron = "0 0 * * * *")` | Method | Run on cron schedule (every hour) |

## Testing

| Annotation | Target | Purpose |
|---|---|---|
| `@SpringBootTest` | Class | Full application context for integration tests |
| `@WebMvcTest(Controller.class)` | Class | Test only the web layer (controller + filters) |
| `@MockBean` | Field | Replace a bean with a Mockito mock in the context |
| `@ActiveProfiles("test")` | Class | Activate test profile |
| `@AutoConfigureMockMvc` | Class | Auto-configure MockMvc for HTTP testing |
| `@TestConfiguration` | Class | Additional config only for tests |

## Common Patterns

```java
// Constructor injection (no @Autowired needed for single constructor)
@Service
public class UserService {
    private final UserRepository repo;
    public UserService(UserRepository repo) { this.repo = repo; }
}

// @Value with default
@Value("${app.feature.enabled:false}")
private boolean featureEnabled;

// @Bean with condition
@Bean
@ConditionalOnProperty(name = "app.cache.enabled", havingValue = "true")
public CacheManager cacheManager() { ... }

// @Profile on @Configuration
@Configuration
@Profile("!test")  // active in all profiles EXCEPT test
public class ProdOnlyConfig { ... }
```
