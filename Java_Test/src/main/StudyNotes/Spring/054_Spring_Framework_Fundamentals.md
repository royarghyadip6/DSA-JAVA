# 54. Spring Framework Fundamentals

## 54. Spring Framework Fundamentals

## Spring Core

---

# 1. What is Spring Framework?

<details>
<summary>Show Answer</summary>

**Answer:**

**Spring Framework** is an open-source **Java application framework** that simplifies enterprise development by providing **IoC (Inversion of Control)** container, **Dependency Injection**, AOP, transaction management, and integration with databases, messaging, and web technologies.

### Simple Idea

```text
Without Spring: You create objects, wire dependencies manually
With Spring:    Container creates objects and injects dependencies for you
```

```java
// Spring manages this — you don't write new UserService() everywhere
@Service
public class UserService {
    private final UserRepository repo;
    public UserService(UserRepository repo) { this.repo = repo; }
}
```

**Interview Point:**

> Spring = lightweight Java framework. Core value = IoC container + DI. You focus on business logic; Spring handles object creation and wiring.

</details>

---

# 2. Why Spring became popular?

<details>
<summary>Show Answer</summary>

**Answer:**

Spring replaced heavy **EJB (Enterprise JavaBeans)** and **XML-heavy** setups with a **lightweight, POJO-based** approach.

| Reason | Detail |
|--------|--------|
| **Simplified Java EE** | No complex EJB containers |
| **Loose coupling** | DI removes tight object dependencies |
| **Testability** | Easy to mock dependencies in unit tests |
| **Modular** | Use only what you need (Core, Web, Data, Security) |
| **Huge ecosystem** | Spring Boot, Spring Data, Spring Security, Cloud |
| **Community** | Large adoption, docs, Stack Overflow support |

```text
2000s: EJB was complex → Spring offered simpler alternative
2010s: Spring Boot made it even easier → massive adoption
```

**Interview Point:**

> Spring won because it simplified enterprise Java — POJOs, DI, less boilerplate than EJB. Spring Boot later removed XML and config pain.

</details>

---

# 3. Advantages of Spring?

<details>
<summary>Show Answer</summary>

**Answer:**

| Advantage | Benefit |
|-----------|---------|
| **IoC / DI** | Loose coupling, easier maintenance |
| **AOP** | Cross-cutting concerns (logging, security, transactions) without cluttering business code |
| **Transaction management** | Declarative `@Transactional` |
| **Integration** | JDBC, JPA, REST, JMS, Kafka — unified abstractions |
| **Testing** | `@SpringBootTest`, mock beans, test slices |
| **Modularity** | Pick modules — don't need full stack |
| **Community & ecosystem** | Boot, Cloud, Security, Data |

```java
@Transactional  // Spring handles commit/rollback — you write business logic only
public void transferMoney(Long from, Long to, BigDecimal amount) { ... }
```

**Interview Point:**

> Key advantages: loose coupling (DI), AOP for cross-cutting, declarative transactions, rich ecosystem, excellent test support.

</details>

---

# 4. What are Spring modules?

<details>
<summary>Show Answer</summary>

**Answer:**

Spring is **modular** — you use only the jars you need:

| Module | Purpose |
|--------|---------|
| **Spring Core** | IoC container, DI, BeanFactory, ApplicationContext |
| **Spring AOP** | Aspect-oriented programming |
| **Spring Context** | Enterprise services (JNDI, internationalization, events) |
| **Spring Web / WebMVC** | REST/MVC web applications |
| **Spring Data** | JPA, MongoDB, Redis repositories |
| **Spring Security** | Authentication & authorization |
| **Spring Boot** | Auto-config, embedded server, starters |
| **Spring Cloud** | Microservices (Config, Gateway, Eureka) |

```text
spring-core.jar     → IoC container
spring-webmvc.jar   → @RestController, DispatcherServlet
spring-data-jpa.jar → JpaRepository
```

**Interview Point:**

> Spring is not one big jar — it's modules. Core = IoC. Web = MVC/REST. Data = repositories. Boot sits on top with auto-config.

</details>

---

# 5. What is IoC?

<details>
<summary>Show Answer</summary>

**Answer:**

**Inversion of Control (IoC)** means the **framework controls object creation and lifecycle** — not your application code.

### Traditional (You Control)

```java
UserRepository repo = new UserRepositoryImpl();  // YOU decide implementation
UserService service = new UserService(repo);
```

### IoC (Spring Controls)

```java
@Service
public class UserService {
    public UserService(UserRepository repo) { }  // Spring injects implementation
}
```

```text
Control INVERTED:
  Before: Your code creates dependencies
  After:  Spring container creates and injects them
```

**Interview Point:**

> IoC = control of object creation moves from your code to Spring container. DI is how Spring implements IoC.

</details>

---

# 6. What is Dependency Injection?

<details>
<summary>Show Answer</summary>

**Answer:**

**Dependency Injection (DI)** is the **mechanism** by which Spring provides required dependencies to a class — instead of the class creating them itself.

```java
// Bad — tight coupling
public class OrderService {
    private PaymentGateway gateway = new StripeGateway(); // hard-coded
}

// Good — DI
public class OrderService {
    private final PaymentGateway gateway;
    public OrderService(PaymentGateway gateway) {  // injected
        this.gateway = gateway;
    }
}
```

### How Spring Injects

```text
1. You declare dependency (constructor parameter, @Autowired field)
2. Spring finds matching bean in container
3. Spring injects it when creating your bean
```

**Interview Point:**

> DI = dependencies are given to you, not created by you. Spring's main way to achieve IoC.

</details>

---

# 7. Why DI is important?

<details>
<summary>Show Answer</summary>

**Answer:**

| Benefit | Explanation |
|---------|-------------|
| **Loose coupling** | Code depends on interface, not concrete class |
| **Testability** | Inject mock in tests — no real DB needed |
| **Flexibility** | Swap implementation without changing consumer |
| **Single Responsibility** | Class focuses on logic, not object creation |
| **Maintainability** | Changes in one bean don't break others |

```java
// Unit test — inject mock
@Test
void testCreateOrder() {
    PaymentGateway mock = Mockito.mock(PaymentGateway.class);
    OrderService service = new OrderService(mock);  // easy to test
}
```

**Interview Point:**

> DI enables loose coupling and testability. You code against interfaces; Spring wires the right implementation.

</details>

---

# 8. Types of Dependency Injection?

<details>
<summary>Show Answer</summary>

**Answer:**

Spring supports **three types** of DI:

| Type | How | Example |
|------|-----|---------|
| **Constructor** | Dependencies via constructor parameters | `public Service(Repo repo)` |
| **Setter** | Dependencies via setter methods | `@Autowired setRepo(Repo r)` |
| **Field** | Direct injection on field | `@Autowired private Repo repo` |

```java
// 1. Constructor (recommended)
@Service
public class UserService {
    private final UserRepository repo;
    public UserService(UserRepository repo) { this.repo = repo; }
}

// 2. Setter
@Autowired
public void setRepo(UserRepository repo) { this.repo = repo; }

// 3. Field (works but not recommended)
@Autowired
private UserRepository repo;
```

**Interview Point:**

> Three types: Constructor, Setter, Field. Constructor is preferred — immutable, testable, required deps clear.

</details>

---

## Dependency Injection

---

# 9. Constructor Injection vs Setter Injection?

<details>
<summary>Show Answer</summary>

**Answer:**

| Aspect | Constructor Injection | Setter Injection |
|--------|----------------------|------------------|
| **When injected** | At object creation | After object created |
| **Immutability** | `final` fields possible | Fields can change later |
| **Required deps** | Forces all required deps upfront | Optional — can forget to call setter |
| **Testing** | Pass deps in constructor easily | Need setters or reflection |
| **Circular deps** | Harder to create (fails early) | Can partially wire (risky) |
| **Use case** | Mandatory dependencies | Optional dependencies |

```java
// Constructor — deps required at birth
public OrderService(OrderRepository repo, PaymentGateway gateway) { }

// Setter — optional, can change later
@Autowired
public void setAuditLogger(AuditLogger logger) { this.logger = logger; }
```

**Interview Point:**

> Constructor = mandatory, immutable, preferred. Setter = optional dependencies or reconfiguration.

</details>

---

# 10. Which one is preferred and why?

<details>
<summary>Show Answer</summary>

**Answer:**

**Constructor injection** is preferred (Spring team and industry standard since Spring 4.3+).

### Why Constructor Wins

```text
✅ Dependencies are explicit and required
✅ Fields can be final (immutable)
✅ Easy unit testing — new Service(mockRepo)
✅ No reflection needed on fields
✅ Bean is fully initialized after construction
✅ Avoids NullPointerException from forgotten @Autowired
```

```java
@Service
public class UserService {
    private final UserRepository repo;  // final = safe, thread-friendly

    public UserService(UserRepository repo) {
        this.repo = repo;
    }
}
```

**Interview Point:**

> Constructor injection preferred — immutable, explicit, testable. Spring auto-wires single constructor without @Autowired since 4.3.

</details>

---

# 11. What happens if multiple constructors exist?

<details>
<summary>Show Answer</summary>

**Answer:**

Spring picks **one constructor** for injection:

| Scenario | Spring Behavior |
|----------|-----------------|
| **One constructor** | Auto-wired (no @Autowired needed since 4.3) |
| **Multiple constructors, one @Autowired** | Uses the annotated constructor |
| **Multiple constructors, no @Autowired** | Uses constructor with **most parameters** |
| **Ambiguous — can't decide** | `BeanCreationException` at startup |

```java
@Service
public class PaymentService {

    private final PaymentGateway gateway;

  @Autowired  // Spring uses THIS constructor
    public PaymentService(PaymentGateway gateway) {
        this.gateway = gateway;
    }

    public PaymentService() {
        this.gateway = null;  // ignored if above is @Autowired
    }
}
```

**Interview Point:**

> Multiple constructors: mark one with @Autowired, or Spring picks the one with most args. Ambiguity = startup failure.

</details>

---

# 12. Why constructor injection is recommended?

<details>
<summary>Show Answer</summary>

**Answer:**

```text
1. Immutability     → final fields, thread-safe
2. Fail fast        → missing dependency = startup error, not NPE at runtime
3. Testability      → new Service(mockDep) in unit tests
4. Clear contract   → all dependencies visible in constructor signature
5. No partial state → object never exists without required deps
6. Spring best practice → official recommendation since 4.3
```

```java
// Production and test look the same
UserService service = new UserService(mockRepo);  // test
// Spring does the same in production automatically
```

**Interview Point:**

> Constructor injection = fail-fast, immutable, testable, explicit. Industry standard for mandatory dependencies.

</details>

---

## Spring Container

---

# 13. BeanFactory vs ApplicationContext?

<details>
<summary>Show Answer</summary>

**Answer:**

Both are Spring **IoC containers**, but `ApplicationContext` is the **superset** used in almost all modern apps.

| Feature | BeanFactory | ApplicationContext |
|---------|-------------|-------------------|
| **Bean instantiation** | Lazy by default | Eager for singletons |
| **Internationalization** | ❌ | ✅ MessageSource |
| **Event publishing** | ❌ | ✅ ApplicationEvent |
| **AOP support** | Limited | ✅ Built-in |
| **Enterprise features** | Basic | JNDI, scheduling, etc. |
| **Usage** | Rare (resource-critical) | Standard in all apps |

```java
// Modern apps — always ApplicationContext
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(App.class, args);
        UserService service = ctx.getBean(UserService.class);
    }
}
```

**Interview Point:**

> BeanFactory = basic lazy container. ApplicationContext = BeanFactory + events, i18n, AOP, eager singletons. Use ApplicationContext always.

</details>

---

# 14. What is Spring Container?

<details>
<summary>Show Answer</summary>

**Answer:**

The **Spring Container** (IoC container) is the **heart of Spring** — it creates beans, wires dependencies, manages lifecycle, and provides beans when requested.

```text
Your @Service, @Repository, @Controller classes
        ↓
Spring Container reads them at startup
        ↓
Creates objects (beans), injects dependencies
        ↓
Stores in ApplicationContext (bean registry)
        ↓
You get fully wired objects ready to use
```

### Key Responsibilities

```text
✅ Bean creation
✅ Dependency injection
✅ Scope management (singleton, prototype, etc.)
✅ Lifecycle callbacks (@PostConstruct, @PreDestroy)
✅ Configuration (@Configuration, @Bean)
```

**Interview Point:**

> Container = factory + registry + lifecycle manager. ApplicationContext is the container you use in practice.

</details>

---

# 15. How beans are managed?

<details>
<summary>Show Answer</summary>

**Answer:**

```text
1. Component Scan / @Bean methods → find bean candidates
2. Create BeanDefinition (metadata: class, scope, dependencies)
3. Instantiate bean (constructor injection)
4. Populate properties (setter/field injection)
5. BeanPostProcessor hooks (e.g., @Autowired processing)
6. Initialization (@PostConstruct, InitializingBean)
7. Bean ready in container
8. On shutdown → @PreDestroy, DisposableBean
```

```java
@Component
public class EmailService {
    @PostConstruct
    public void init() { /* called after injection */ }

    @PreDestroy
    public void cleanup() { /* called on shutdown */ }
}
```

| Phase | What Happens |
|-------|--------------|
| **Registration** | BeanDefinition stored in context |
| **Instantiation** | Object created |
| **Injection** | Dependencies wired |
| **Initialization** | @PostConstruct, custom init |
| **Use** | Bean served from container |
| **Destruction** | Cleanup on context close |

**Interview Point:**

> Beans go through: define → create → inject → initialize → use → destroy. BeanPostProcessors hook into creation pipeline.

</details>

---

## Bean Scope

---

# 16. Singleton scope?

<details>
<summary>Show Answer</summary>

**Answer:**

**Singleton** (default scope) = **one instance per Spring container** — shared across the entire application.

```java
@Service  // default scope = singleton
@Scope("singleton")  // explicit (optional)
public class CacheService { }
```

```text
Request 1 → same CacheService instance
Request 2 → same CacheService instance
Request 3 → same CacheService instance
```

| Point | Detail |
|-------|--------|
| **Default** | Yes — all @Service/@Repository are singleton |
| **Thread safety** | YOU must make singleton beans thread-safe if they hold mutable state |
| **Memory** | Efficient — one object reused |

**Interview Point:**

> Singleton = one bean per container. Default scope. Stateless services are safe; mutable state needs synchronization or avoid storing request data in fields.

</details>

---

# 17. Prototype scope?

<details>
<summary>Show Answer</summary>

**Answer:**

**Prototype** = **new instance every time** the bean is requested from the container.

```java
@Component
@Scope("prototype")
public class ReportGenerator { }
```

```text
getBean() call 1 → new ReportGenerator()
getBean() call 2 → another new ReportGenerator()
```

| Point | Detail |
|-------|--------|
| **Creation** | New object per injection/getBean |
| **Lifecycle** | Spring does NOT manage full destruction — GC handles it |
| **Use case** | Stateful objects, per-operation workers |
| **Injecting into singleton** | Singleton gets ONE prototype reference — use `ObjectProvider` or `@Lookup` for fresh instance each time |

**Interview Point:**

> Prototype = new instance per request. Don't inject prototype into singleton field directly — you'll get only one prototype instance.

</details>

---

# 18. Request scope?

<details>
<summary>Show Answer</summary>

**Answer:**

**Request scope** = **one bean instance per HTTP request** — lives only for that request.

```java
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestContext { }
```

```text
HTTP Request A → RequestContext instance #1 (destroyed after response)
HTTP Request B → RequestContext instance #2
```

| Point | Detail |
|-------|--------|
| **Web only** | Requires web application context |
| **Lifecycle** | Created at request start, destroyed at request end |
| **Use case** | Request-specific data (user context, request ID) |
| **Proxy** | Often needs scoped proxy when injected into singleton |

**Interview Point:**

> Request scope = one bean per HTTP request. Web apps only. Use proxy when injecting into singleton beans.

</details>

---

# 19. Session scope?

<details>
<summary>Show Answer</summary>

**Answer:**

**Session scope** = **one bean instance per HTTP session** — shared across all requests in the same user session.

```java
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ShoppingCart { }
```

```text
User logs in → ShoppingCart created
Same session, 10 requests → same ShoppingCart
User logs out / session expires → cart destroyed
```

| Use Case | Example |
|----------|---------|
| Shopping cart | Items persist across requests |
| User preferences | Session-level settings |
| Wizard forms | Multi-step form state |

**Interview Point:**

> Session scope = one bean per HTTP session. Good for cart, user session data. Needs scoped proxy in singleton beans.

</details>

---

# 20. Application scope?

<details>
<summary>Show Answer</summary>

**Answer:**

**Application scope** = **one bean per ServletContext** — shared across all users and sessions in the web app (like a global singleton for the web layer).

```java
@Component
@Scope(value = WebApplicationContext.SCOPE_APPLICATION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AppWideCounter { }
```

| Scope | Lifetime |
|-------|----------|
| **Singleton** | One per Spring container |
| **Application** | One per ServletContext (web) |
| **Session** | One per user session |
| **Request** | One per HTTP request |

```text
All users share the same Application-scoped bean
(similar to singleton but tied to ServletContext lifecycle)
```

**Interview Point:**

> Application scope = one instance per ServletContext. Broader than session, narrower than nothing — shared across all sessions in the web app.

</details>

---

## Bean Lifecycle

---

# 21. Explain Bean lifecycle.

<details>
<summary>Show Answer</summary>

**Answer:**

```text
1. BeanDefinition registered
2. Bean instantiated (constructor called)
3. Dependencies injected (@Autowired, constructor)
4. BeanPostProcessor.beforeInitialization()
5. @PostConstruct / InitializingBean.afterPropertiesSet()
6. Custom init-method (if configured)
7. BeanPostProcessor.afterInitialization()
8. Bean ready — in use
9. Context shutdown
10. @PreDestroy / DisposableBean.destroy()
11. Custom destroy-method
```

```java
@Component
public class DataLoader {

    public DataLoader() { System.out.println("1. Constructor"); }

    @PostConstruct
    public void init() { System.out.println("2. PostConstruct"); }

    @PreDestroy
    public void cleanup() { System.out.println("3. PreDestroy"); }
}
```

**Interview Point:**

> Lifecycle: construct → inject → initialize (@PostConstruct) → use → destroy (@PreDestroy). BeanPostProcessors wrap this pipeline.

</details>

---

# 22. Bean initialization?

<details>
<summary>Show Answer</summary>

**Answer:**

Initialization runs **after** dependency injection — to set up the bean before it's used.

### Ways to Initialize

| Method | Example |
|--------|---------|
| **@PostConstruct** | `@PostConstruct void init()` — most common |
| **InitializingBean** | `afterPropertiesSet()` — older Spring interface |
| **init-method** | XML or `@Bean(initMethod = "setup")` |

```java
@PostConstruct
public void loadCache() {
    cache.putAll(repository.findAll());  // setup after injection
}
```

```text
Order: Constructor → Injection → @PostConstruct → Bean ready
```

**Interview Point:**

> Initialization = post-injection setup. Prefer @PostConstruct (JSR-250 standard). Runs once before bean is used.

</details>

---

# 23. Bean destruction?

<details>
<summary>Show Answer</summary>

**Answer:**

Destruction runs when the **ApplicationContext shuts down** — to release resources cleanly.

### Ways to Destroy

| Method | Example |
|--------|---------|
| **@PreDestroy** | `@PreDestroy void cleanup()` — most common |
| **DisposableBean** | `destroy()` — older interface |
| **destroy-method** | `@Bean(destroyMethod = "close")` |

```java
@PreDestroy
public void shutdown() {
    connectionPool.close();
    executorService.shutdown();
}
```

```text
Only called for singleton beans when context closes
Prototype beans: Spring does NOT call @PreDestroy
```

**Interview Point:**

> @PreDestroy for cleanup — close connections, stop threads. Only for container-managed singleton beans on shutdown.

</details>

---

# 24. @PostConstruct?

<details>
<summary>Show Answer</summary>

**Answer:**

`@PostConstruct` is a **JSR-250 annotation** — marks a method to run **once after dependency injection** is complete.

```java
@Service
public class ConfigService {
    private final ConfigRepository repo;
    private Map<String, String> cache;

    public ConfigService(ConfigRepository repo) {
        this.repo = repo;
    }

    @PostConstruct
    public void init() {
        cache = repo.loadAll();  // repo is already injected here
    }
}
```

| Rule | Detail |
|------|--------|
| **When** | After constructor + injection |
| **How many times** | Once per bean instance |
| **Return type** | void |
| **Exceptions** | Unchecked exception fails bean creation |

**Interview Point:**

> @PostConstruct = init method after injection. Standard way to load cache, validate config, open resources.

</details>

---

# 25. @PreDestroy?

<details>
<summary>Show Answer</summary>

**Answer:**

`@PreDestroy` is a **JSR-250 annotation** — marks a method to run **before the bean is removed** from the container (on shutdown).

```java
@Service
public class FileWatcherService {
    private WatchService watcher;

    @PostConstruct
    public void start() { watcher = FileSystems.getDefault().newWatchService(); }

    @PreDestroy
    public void stop() throws IOException {
        watcher.close();  // clean shutdown
    }
}
```

```text
Triggered when: ApplicationContext.close() or app shutdown
NOT triggered for: prototype beans
```

**Interview Point:**

> @PreDestroy = cleanup on shutdown. Close files, DB connections, thread pools. Prototype scope — not called by Spring.

</details>

---

# 26. InitializingBean?

<details>
<summary>Show Answer</summary>

**Answer:**

`InitializingBean` is a **Spring-specific interface** — implement `afterPropertiesSet()` for initialization logic.

```java
@Service
public class CacheService implements InitializingBean {
    private final DataRepository repo;

    public CacheService(DataRepository repo) { this.repo = repo; }

    @Override
    public void afterPropertiesSet() {
        // called after all properties injected — same as @PostConstruct
        loadCache();
    }
}
```

| @PostConstruct vs InitializingBean |
|-----------------------------------|
| @PostConstruct = standard Java (JSR-250), no Spring coupling |
| InitializingBean = ties your class to Spring API — less preferred |

**Interview Point:**

> InitializingBean.afterPropertiesSet() = Spring's old init hook. Prefer @PostConstruct — standard and decoupled from Spring.

</details>

---

# 27. DisposableBean?

<details>
<summary>Show Answer</summary>

**Answer:**

`DisposableBean` is a **Spring-specific interface** — implement `destroy()` for cleanup logic.

```java
@Service
public class SchedulerService implements DisposableBean {
    private ScheduledExecutorService executor;

    @Override
    public void destroy() {
        executor.shutdown();  // cleanup on context close
    }
}
```

| @PreDestroy vs DisposableBean |
|-------------------------------|
| @PreDestroy = JSR-250 standard, preferred |
| DisposableBean = Spring coupling, legacy |

**Interview Point:**

> DisposableBean.destroy() = Spring's old cleanup hook. Prefer @PreDestroy. Same timing — on context shutdown.

</details>

---

## Bean Creation

---

# 28. @Component

<details>
<summary>Show Answer</summary>

**Answer:**

`@Component` is the **generic stereotype** — tells Spring to **detect and register** this class as a bean during component scanning.

```java
@Component
public class EmailValidator {
    public boolean isValid(String email) {
        return email != null && email.contains("@");
    }
}
```

```text
@Component = "Hey Spring, manage this class as a bean"
Spring scans → finds @Component → creates bean → registers in container
```

| Detail | Value |
|--------|-------|
| **Level** | Generic — any Spring-managed component |
| **Scanning** | Picked up by @ComponentScan |
| **Specializations** | @Service, @Repository, @Controller extend it |

**Interview Point:**

> @Component = base stereotype for auto-detected beans. @Service/@Repository/@Controller are specialized @Components.

</details>

---

# 29. @Service

<details>
<summary>Show Answer</summary>

**Answer:**

`@Service` is a **specialized @Component** for the **business/service layer** — same behavior as @Component, better semantics.

```java
@Service
public class OrderService {
    private final OrderRepository repo;
    public OrderService(OrderRepository repo) { this.repo = repo; }

    public Order createOrder(OrderRequest req) {
        return repo.save(new Order(req));
    }
}
```

```text
@Service = @Component + semantic meaning "this is business logic"
Functionally identical — Spring treats it the same
```

**Interview Point:**

> @Service = business layer bean. Technically same as @Component but improves readability and layer separation.

</details>

---

# 30. @Repository

<details>
<summary>Show Answer</summary>

**Answer:**

`@Repository` is a **specialized @Component** for the **data access layer** — adds **exception translation** (SQLException → Spring DataAccessException).

```java
@Repository
public class UserRepository {
    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public User findById(Long id) {
        return jdbc.queryForObject("SELECT * FROM users WHERE id = ?", ...);
    }
}
```

| Extra Benefit | Detail |
|---------------|--------|
| **Exception translation** | Raw SQLException → unchecked DataAccessException |
| **Semantics** | Clearly marks persistence layer |

**Interview Point:**

> @Repository = DAO/persistence layer. Same as @Component + automatic SQLException to DataAccessException conversion.

</details>

---

# 31. @Controller

<details>
<summary>Show Answer</summary>

**Answer:**

`@Controller` is a **specialized @Component** for the **presentation/web layer** — handles HTTP requests (typically returns view names for MVC).

```java
@Controller
public class HomeController {
    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("msg", "Welcome");
        return "home";  // view name → Thymeleaf/JSP
    }
}
```

```text
@Controller     → returns view name (HTML page)
@RestController → @Controller + @ResponseBody → returns JSON directly
```

**Interview Point:**

> @Controller = web/MVC layer. Returns view names. For REST APIs use @RestController (= @Controller + @ResponseBody).

</details>

---

# 32. Difference among them?

<details>
<summary>Show Answer</summary>

**Answer:**

All four are **functionally @Component** — Spring creates and manages them the same way. Difference is **semantic layer** and **one extra feature**.

| Annotation | Layer | Special Behavior |
|------------|-------|------------------|
| **@Component** | Generic | None — any bean |
| **@Service** | Business logic | None — naming convention |
| **@Repository** | Data access | SQLException → DataAccessException |
| **@Controller** | Web/MVC | Works with DispatcherServlet, view resolution |

```java
@RestController  // @Controller + @ResponseBody — REST APIs
public class UserController {
    private final UserService service;
    // ...
}
```

```text
Best practice: use the right stereotype for the right layer
  Controller → Service → Repository → Database
```

**Interview Point:**

> All are @Component under the hood. Use correct stereotype for clarity: @Service (business), @Repository (DAO), @Controller (web). Only @Repository adds exception translation.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: IoC vs DI — same or different?

<details>
<summary>Show Answer</summary>

**Answer:**

**IoC** is the **principle** (control inverted to framework). **DI** is the **pattern/implementation** (dependencies injected by container). Often used interchangeably in interviews.

</details>

---

### Q: Default bean scope?

<details>
<summary>Show Answer</summary>

**Answer:**

**Singleton** — one instance per Spring container.

</details>

---

### Q: Is @Service thread-safe by default?

<details>
<summary>Show Answer</summary>

**Answer:**

**No.** Singleton scope means one shared instance. You must design stateless services or handle thread safety yourself for mutable state.

</details>

---

### Q: Can you have two beans of same type?

<details>
<summary>Show Answer</summary>

**Answer:**

**Yes** — use `@Qualifier` or `@Primary` to resolve which one to inject. Otherwise Spring throws `NoUniqueBeanDefinitionException`.

</details>

---

### Q: BeanFactory or ApplicationContext in Spring Boot?

<details>
<summary>Show Answer</summary>

**Answer:**

**ApplicationContext** — Spring Boot always uses it. BeanFactory is legacy/low-level.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Spring Core = IoC container + DI. Constructor injection preferred. ApplicationContext manages bean lifecycle. Default scope = singleton. Stereotypes: @Component (generic), @Service (business), @Repository (DAO + exception translation), @Controller (web). Lifecycle: construct → inject → @PostConstruct → use → @PreDestroy.

</details>
