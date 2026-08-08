# 55. Spring Annotations

## 55. Spring Annotations

## Frequently Asked

---

# 1. @Autowired

<details>
<summary>Show Answer</summary>

**Answer:**

`@Autowired` tells Spring to **automatically inject** a matching bean from the container into a field, constructor, setter, or method parameter.

```java
@Service
public class OrderService {

    // Constructor (preferred — no @Autowired needed for single constructor)
    private final OrderRepository repo;
    public OrderService(OrderRepository repo) { this.repo = repo; }

    // Field injection (works but not recommended)
    @Autowired
    private PaymentGateway gateway;

    // Setter injection (optional dependencies)
    @Autowired
    public void setAuditService(AuditService audit) { }
}
```

### How It Works

```text
1. Spring finds @Autowired on field/constructor/setter
2. Looks up bean by type in ApplicationContext
3. Injects via reflection (field/setter) or constructor call
4. Fails at startup if no match (required=true by default)
```

| Attribute | Default | Meaning |
|-----------|---------|---------|
| `required` | `true` | Fail if no bean found |
| `required = false` | optional | Leaves null if no bean |

**Interview Point:**

> @Autowired = inject by type. Prefer constructor injection. Post-processor `AutowiredAnnotationBeanPostProcessor` handles it at startup.

</details>

---

# 2. @Qualifier

<details>
<summary>Show Answer</summary>

**Answer:**

`@Qualifier` specifies **which bean by name** when multiple beans of the **same type** exist.

```java
public interface NotificationSender { void send(String msg); }

@Service("emailSender")
public class EmailSender implements NotificationSender { }

@Service("smsSender")
public class SmsSender implements NotificationSender { }

@Service
public class AlertService {
    private final NotificationSender sender;

    public AlertService(@Qualifier("emailSender") NotificationSender sender) {
        this.sender = sender;  // injects EmailSender, not SmsSender
    }
}
```

```text
Without @Qualifier → NoUniqueBeanDefinitionException (2 beans of same type)
With @Qualifier("emailSender") → picks exact bean by name
```

**Interview Point:**

> @Qualifier = disambiguate by bean name when multiple implementations exist. Use with @Autowired on constructor/field.

</details>

---

# 3. @Primary

<details>
<summary>Show Answer</summary>

**Answer:**

`@Primary` marks a bean as the **default choice** when multiple beans match the same type — no @Qualifier needed.

```java
@Service
@Primary  // default when injecting PaymentGateway
public class StripeGateway implements PaymentGateway { }

@Service
public class PayPalGateway implements PaymentGateway { }

@Service
public class CheckoutService {
    private final PaymentGateway gateway;
    public CheckoutService(PaymentGateway gateway) {
        this.gateway = gateway;  // gets StripeGateway ( @Primary )
    }
}
```

| @Qualifier vs @Primary |
|------------------------|
| @Qualifier = explicit choice at injection point |
| @Primary = default bean for that type everywhere |

```text
Rule: @Qualifier beats @Primary when both are present
```

**Interview Point:**

> @Primary = default bean for a type. Use when one implementation is standard. @Qualifier overrides @Primary.

</details>

---

# 4. @Value

<details>
<summary>Show Answer</summary>

**Answer:**

`@Value` injects **values from properties files, environment variables, or SpEL expressions** into fields, constructor params, or method params.

```java
@Service
public class EmailService {

    @Value("${app.mail.host}")
    private String mailHost;

    @Value("${app.mail.port:587}")  // default 587 if missing
    private int mailPort;

    @Value("#{systemProperties['user.name']}")  // SpEL
    private String systemUser;
}
```

```properties
# application.properties
app.mail.host=smtp.gmail.com
app.mail.port=465
```

| Syntax | Example |
|--------|---------|
| `${key}` | Property from properties/env |
| `${key:default}` | With default value |
| `#{expression}` | SpEL expression |

**Interview Point:**

> @Value injects config values — properties, env vars, defaults. Use for simple config; `@ConfigurationProperties` for grouped config in larger apps.

</details>

---

# 5. @Bean

<details>
<summary>Show Answer</summary>

**Answer:**

`@Bean` marks a **method** inside a `@Configuration` class — Spring calls the method and registers the **return value** as a bean in the container.

```java
@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();  // Spring manages this object
    }

    @Bean(name = "primaryDataSource")
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:postgresql://localhost/mydb");
        return ds;
    }
}
```

### When to Use @Bean vs @Component

| @Component | @Bean |
|------------|-------|
| On your own class | On method — for third-party classes |
| Auto-scanned | You control creation logic |
| `new MyService()` by Spring | `return new RestTemplate()` by you |

**Interview Point:**

> @Bean = programmatic bean registration. Use for third-party classes you can't annotate (@Component). Method name = default bean name.

</details>

---

# 6. @Configuration

<details>
<summary>Show Answer</summary>

**Answer:**

`@Configuration` marks a class as a **source of bean definitions** — contains `@Bean` methods. Spring creates a **CGLIB proxy** so `@Bean` methods return singletons (not new object each call).

```java
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(...);
    }
}
```

```text
@Configuration class = Java-based config (replaces XML)
@Bean methods inside = bean factory methods
CGLIB proxy ensures @Bean method called once per container
```

**Interview Point:**

> @Configuration = Java config class with @Bean methods. Replaces XML. CGLIB-enhanced so @Bean methods don't create duplicate instances.

</details>

---

## Advanced

---

# 7. @Lazy

<details>
<summary>Show Answer</summary>

**Answer:**

`@Lazy` delays bean creation until **first use** — instead of at application startup (default for singletons is eager).

```java
@Service
@Lazy
public class HeavyReportService {
    public HeavyReportService() {
        // expensive init — only runs when first bean is used
    }
}

@Configuration
public class AppConfig {
    @Bean
    @Lazy
    public ExpensiveClient client() { return new ExpensiveClient(); }
}
```

| Eager (default) | Lazy |
|-----------------|------|
| Created at startup | Created on first injection/getBean |
| Fail-fast for config errors | Faster startup, delayed failure |
| Good for most beans | Good for rarely used heavy beans |

**Interview Point:**

> @Lazy = create bean on first access, not startup. Speeds startup. Can hide config errors until runtime.

</details>

---

# 8. @DependsOn

<details>
<summary>Show Answer</summary>

**Answer:**

`@DependsOn` ensures specified beans are **initialized before** this bean — controls startup order.

```java
@Service
@DependsOn({"databaseInitializer", "cacheWarmer"})
public class UserService {
    // UserService created ONLY after databaseInitializer and cacheWarmer are ready
}

@Bean
@DependsOn("dataSource")
public JdbcTemplate jdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
}
```

```text
Use when: Bean B needs Bean A fully initialized first
Does NOT inject dependency — only controls ORDER
For actual dependency, use constructor injection
```

**Interview Point:**

> @DependsOn = initialization order only, not injection. Use when one bean must exist before another starts, but no direct dependency link.

</details>

---

# 9. @Profile

<details>
<summary>Show Answer</summary>

**Answer:**

`@Profile` registers a bean **only when a specific profile is active** — enables environment-specific configuration.

```java
@Configuration
@Profile("dev")
public class DevConfig {
    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder().build();  // H2 for dev
    }
}

@Configuration
@Profile("prod")
public class ProdConfig {
    @Bean
    public DataSource dataSource() {
        return new HikariDataSource();  // real DB for prod
    }
}
```

```properties
# application-dev.properties
spring.profiles.active=dev

# Or command line
java -jar app.jar --spring.profiles.active=prod
```

| Use Case | Example |
|----------|---------|
| Dev vs Prod DB | Different DataSource beans |
| Mock services | @Profile("test") |
| Feature flags | @Profile("feature-x") |

**Interview Point:**

> @Profile = conditional bean registration by environment. Activate via `spring.profiles.active`. Clean way to separate dev/test/prod config.

</details>

---

# 10. @PropertySource

<details>
<summary>Show Answer</summary>

**Answer:**

`@PropertySource` loads **additional property files** into Spring's Environment — beyond default `application.properties`.

```java
@Configuration
@PropertySource("classpath:mail.properties")
@PropertySource("classpath:payment-${spring.profiles.active}.properties")
public class AppConfig {

    @Value("${mail.smtp.host}")
    private String smtpHost;
}
```

```properties
# mail.properties
mail.smtp.host=smtp.company.com
mail.smtp.port=25
```

```text
Default: application.properties / application.yml (auto-loaded by Boot)
@PropertySource: extra custom property files
Access via @Value or Environment.getProperty()
```

**Interview Point:**

> @PropertySource loads extra property files. Spring Boot auto-loads application.properties/yml. Use for modular config files.

</details>

---

## Scenario

---

# 11. What happens when multiple beans of same type exist?

<details>
<summary>Show Answer</summary>

**Answer:**

When Spring finds **more than one bean** matching the injection type, it throws:

```text
NoUniqueBeanDefinitionException:
  expected single matching bean but found 2: emailSender, smsSender
```

```java
@Service("emailSender")
public class EmailSender implements Notifier { }

@Service("smsSender")
public class SmsSender implements Notifier { }

@Service
public class AlertService {
    @Autowired
    private Notifier notifier;  // ❌ FAILS — which one?
}
```

### Solutions

```text
1. @Qualifier("emailSender")  → pick by name
2. @Primary on one bean       → default choice
3. Use @Resource(name="...")  → JSR-250 by name
4. Inject List<Notifier>      → get all implementations
5. Inject Map<String, Notifier> → all beans keyed by name
```

**Interview Point:**

> Multiple same-type beans without @Qualifier/@Primary = startup failure. Fix with @Qualifier, @Primary, or inject collection.

</details>

---

# 12. How Spring resolves dependency ambiguity?

<details>
<summary>Show Answer</summary>

**Answer:**

Spring follows this **resolution order** when injecting by type:

```text
1. Find all beans matching the required type
2. If exactly ONE → inject it ✅
3. If ZERO → NoSuchBeanDefinitionException (unless required=false)
4. If MULTIPLE → try @Qualifier on injection point
5. If no @Qualifier → look for @Primary bean
6. If still ambiguous → NoUniqueBeanDefinitionException ❌
```

| Priority | Mechanism |
|----------|-----------|
| 1 | @Qualifier on field/parameter |
| 2 | @Primary on one candidate bean |
| 3 | Bean name matches parameter name (constructor) |
| 4 | Fail with exception |

```java
// Parameter name "emailSender" matches bean name — works in Spring 4+
public AlertService(Notifier emailSender) { }  // injects bean named emailSender
```

```text
Best practice: Don't rely on parameter name matching
Use @Qualifier or @Primary explicitly — clear and portable
```

**Interview Point:**

> Resolution: type match → @Qualifier → @Primary → parameter name match → fail. Always be explicit with @Qualifier or @Primary in production code.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: @Autowired on constructor — needed?

<details>
<summary>Show Answer</summary>

**Answer:**

**No** — if there is only one constructor, Spring 4.3+ auto-wires it without `@Autowired`.

</details>

---

### Q: @Bean vs @Component?

<details>
<summary>Show Answer</summary>

**Answer:**

`@Component` on class — Spring creates it. `@Bean` on method — you create and return object (usually third-party classes).

</details>

---

### Q: Can @Value inject List?

<details>
<summary>Show Answer</summary>

**Answer:**

**Yes** — `@Value("${app.allowed-roles}")` with comma-separated values, or use `@ConfigurationProperties` for complex binding.

</details>

---

### Q: @Profile on class vs method?

<details>
<summary>Show Answer</summary>

**Answer:**

Both work. On `@Configuration` class — all `@Bean` methods inside follow profile. On individual `@Service`/`@Bean` — only that bean is conditional.

</details>

---

### Q: What if @Autowired required=false and no bean?

<details>
<summary>Show Answer</summary>

**Answer:**

Field stays **null** — no exception. Use with caution; can cause NPE later if not checked.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> @Autowired injects by type. Multiple beans → @Qualifier (by name) or @Primary (default). @Value for properties. @Bean + @Configuration for Java config. @Lazy delays creation. @DependsOn controls init order. @Profile for environment-specific beans. Ambiguity resolution: Qualifier > Primary > parameter name > fail.

</details>
