# 54. Spring Framework Fundamentals

[Course map](00_COURSE_MAP.md) | **Next:** [054_1 IoC Container Internals →](054_1_IoC_Container_Internals.md)

Teaching is written so a **new learner** can follow. The **Interview Ready Q&A** at the end is **5–8 year interview** standard (read it after you understand the notes).

---

## Simple first

Imagine you are cooking.

- **Without Spring:** you buy every ingredient yourself, chop it, and cook. Every class does `new OtherClass()`.
- **With Spring:** you write a recipe (“I need a payment gateway”). A **manager** (the **container**) buys the ingredients, puts them on your table, and cleans up at the end of the day.

That manager is why you can write business code (`place order`) and not factory code (`new StripeGateway()`).

**Three short words**

| Word | Everyday meaning |
|------|------------------|
| **Bean** | An object Spring manages (like `OrderService`) |
| **Container** | The manager that creates beans and connects them |
| **Inject** | The manager *hands* a bean to another bean |

```text
Without Spring
  Your code:  new StripeGateway()
              new OrderService(gateway)
              new OrderController(service)
  You own:    creation, wiring, lifecycle, swapping implementations

With Spring
  You write:  classes + “I need a PaymentGateway”
  Container:  creates objects, injects them, manages how long they live
  You own:    business rules
```

**Memory trick:** IoC = *who creates*. DI = *how the created object is given to you*.

---

## When you interview (5–8 years)

Interviewers assume you can use `@Service` and constructor injection. They check whether you can explain **why the container exists**, **what problem DI solves**, and **what Spring is not**.

A junior says: “Spring creates objects for us.”

A senior says: “Spring inverts object graph construction. My classes depend on abstractions. The container owns lifecycle, wiring, and later proxying. That is why the same code is testable without a server.”

---

## 1. What Spring actually is

**In one sentence:** Spring is a set of Java libraries whose heart is a **manager of objects** (IoC container).

**IoC** = Inversion of Control. Plain English: *you stop creating helpers yourself; the framework does it.*

It is not a replacement for the JDK (you still write Java). It is not an application server like Tomcat. It is not Spring Boot (Boot is a shortcut *on top of* Spring).

| Term | Meaning |
|------|---------|
| Spring Framework | Core libraries: `spring-core`, `spring-beans`, `spring-context`, `spring-aop`, `spring-webmvc`, `spring-tx`, `spring-jdbc`, `spring-test` |
| Spring Boot | Opinionated packaging on top of Framework: auto-config, starters, embedded server |
| Spring portfolio | Data, Security, Cloud, Batch, Integration, … |

```text
2003–2010  Lightweight alternative to EJB 2.x (XML + BeanFactory)
2013–      Java config + annotations become the default
2014+      Spring Boot makes Framework usable without ceremony
2022+      Spring 6 → Jakarta EE 9+ namespace (jakarta.*)
```

### Why it won against EJB

| Pain with old EJB | Spring answer |
|-------------------|---------------|
| Heavy container, hard to unit test | POJOs, inject mocks in a plain constructor |
| XML and checked exceptions everywhere | Unchecked `DataAccessException`, later annotations |
| “Use the whole platform or nothing” | Modular jars — pull only what you need |
| Business objects coupled to the server | Code against interfaces; container is optional in tests |

You still need to be honest in interviews: **today people choose Spring Boot**, not raw Framework XML. Core knowledge still matters because Boot *is* Framework plus conventions.

---

## 2. Modules you must name correctly

**Module** = one JAR / library. Spring is a **toolbox**, not one giant file. You pick the drawers you need (web, JDBC, test, …).

For Core, these are the drawers that matter:

| Module | What it is for |
|--------|----------------|
| `spring-core` | Utilities, `Resource`, conversion, ordered interfaces |
| `spring-beans` | `BeanFactory`, `BeanDefinition`, property injection |
| `spring-context` | `ApplicationContext`, events, `@Configuration`, scheduling, cache, i18n |
| `spring-aop` | Proxy-based AOP |
| `spring-expression` | SpEL |
| `spring-web` | Servlet abstractions, HTTP converters |
| `spring-webmvc` | DispatcherServlet, MVC annotations |
| `spring-jdbc` | `JdbcTemplate`, DataSource utils |
| `spring-tx` | `PlatformTransactionManager`, `@Transactional` |
| `spring-test` | Test context framework, MockMvc |

`spring-context` depends on `spring-beans` and `spring-core`. Almost every app uses `ApplicationContext`, not raw `BeanFactory`.

Spring 6 dropped Java EE `javax.*` for **Jakarta**. Servlet, persistence, annotation APIs are `jakarta.*`. If a resume says “Spring 5”, they still live on `javax.servlet`.

---

## 3. Inversion of Control (IoC)

**Plain English:** normally *your* class is the boss: it creates the objects it needs. With IoC, the **framework** is the boss: it creates those objects and gives them to you.

**Collaborator / dependency** = “the other object I need” (a repository, a payment gateway).

**Wired** = plugged together.

```java
// Control in YOUR code (no IoC)
public class OrderService {
    private final PaymentGateway gateway = new StripeGateway(); // you chose the impl
}

// Control inverted
public class OrderService {
    private final PaymentGateway gateway;
    public OrderService(PaymentGateway gateway) { // someone else chose the impl
        this.gateway = gateway;
    }
}
```

IoC is bigger than Spring. Factories, service locators, and template methods are also IoC. **Spring’s main IoC style is Dependency Injection.**

**Interview trap:** “IoC and DI are the same.” They are not. DI is one way to implement IoC.

---

## 4. Dependency Injection (DI)

**Plain English:** instead of your class going to the store (`new` / `getBean`), the store **delivers** what you need.

- **Push** = Spring puts the dependency into your constructor/field.
- **Pull** = your class asks for it (`new`, or `context.getBean()` — this last one is called a *service locator*, and we avoid it in business code).

```text
1. You declare a need (constructor parameter, setter, or field)
2. Container finds a matching bean
3. Container injects it when the bean is created
4. If it cannot decide, startup fails (fail-fast — this is a feature)
```

### Why DI is useful (even if you are new)

| Benefit | What it means in a real codebase |
|---------|----------------------------------|
| Loose coupling | `OrderService` depends on `PaymentGateway`, not `StripeGateway` |
| Testability | `new OrderService(mockGateway)` — no Spring needed for unit tests |
| Replaceability | Swap Stripe for Adyen in config, not in 40 call sites |
| Single responsibility | The class does not also play “object factory” |

```java
@Test
void chargesThroughGateway() {
    PaymentGateway gateway = Mockito.mock(PaymentGateway.class);
    OrderService service = new OrderService(gateway); // DI makes this trivial
    service.place(order);
    Mockito.verify(gateway).charge(order);
}
```

If a class is hard to unit-test, it usually **new-s** its dependencies or calls `ApplicationContext.getBean()` inside business methods. That is the service locator anti-pattern.

---

## 5. Three injection styles

**Injection style** = *where* Spring puts the helper: in the constructor, in a setter method, or directly on a field.

Picture a laptop:

- **Constructor:** the charger is plugged in *while the laptop is being built*. No charger → it does not leave the factory. Best.
- **Setter:** the laptop exists, then someone plugs the charger in. You might forget.
- **Field:** someone opens the case and solders a charger inside. It works, but you cannot see it from the outside. Hard to test.

| Style | How | When to use |
|-------|-----|-------------|
| Constructor | Parameters of a constructor | **Required** collaborators. Default choice. |
| Setter | `@Autowired` setter (or XML property) | **Optional** collaborator, or reconfiguration after create |
| Field | `@Autowired` on a field | Quick demos. Avoid in production services. |

```java
@Service
public class OrderService {

    // 1. Constructor — preferred
    private final OrderRepository repo;
    private final PaymentGateway gateway;

    public OrderService(OrderRepository repo, PaymentGateway gateway) {
        this.repo = repo;
        this.gateway = gateway;
    }

    // 2. Setter — optional
    private AuditLogger audit;

    @Autowired(required = false)
    public void setAudit(AuditLogger audit) {
        this.audit = audit;
    }

    // 3. Field — works, not recommended
    // @Autowired
    // private NotificationClient notifier;
}
```

### Constructor vs setter (the table interviewers want)

| | Constructor | Setter |
|--|-------------|--------|
| When injected | During `new` | After the object exists |
| `final` fields | Yes | No |
| Missing dependency | Startup failure | Bean exists in a half-ready state |
| Unit test | `new Service(mock)` | Must call setter or use reflection |
| Circular dependency | Constructor cycle **fails** | Setter cycle can be wired via early exposure |
| Typical use | Mandatory deps | Optional / framework callbacks |

### Why constructor is the recommendation (Spring 4.3+)

1. **Immutable** — fields can be `final`. Safer in a singleton (default scope).
2. **Fail-fast** — missing bean = context does not start. You do not discover NPE in production at 2 a.m.
3. **Honest API** — the constructor *is* the list of dependencies. New joiners see it immediately.
4. **Easy tests** — no Spring, no reflection.
5. **No partial state** — you cannot observe the object before deps exist.

Since **Spring 4.3**, a class with **one constructor** is auto-wired. You do not need `@Autowired` on it.

### Multiple constructors

| Situation | What Spring does |
|-----------|------------------|
| One constructor | Used automatically (4.3+) |
| Several, one marked `@Autowired` | That one is used |
| Several, none marked | Historically ambiguous. Do not rely on “most args wins” folklore — mark one explicitly |
| Cannot decide | `BeanCreationException` at startup |

```java
@Service
public class PaymentService {
    private final PaymentGateway gateway;

    @Autowired
    public PaymentService(PaymentGateway gateway) {
        this.gateway = gateway;
    }

    public PaymentService() { // ignored when the other is @Autowired
        this.gateway = null;
    }
}
```

### Why field injection is disliked

- Cannot use `final`
- Hidden dependencies (class looks dependency-free)
- Harder to test without Spring or reflection
- Makes circular dependencies *too easy*, so design problems stay hidden

Field injection still appears in `@Configuration` test classes and some `@SpringBootTest`s. That is convenience, not a pattern to copy into domain services.

---

## 6. Stereotypes — same engine, different meaning

**Stereotype** = a sticker on a class that says *what job it has*.

**Component scan** = Spring walks your package, sees the sticker, and registers the class as a bean.

All of these stickers tell the scanner: “this class is a bean.” Three of them add meaning. Only `@Repository` adds extra *behavior*.

| Annotation | Layer | Extra behavior |
|------------|-------|----------------|
| `@Component` | Generic | None |
| `@Service` | Business | None (documentation + AOP pointcuts often target it) |
| `@Repository` | Persistence | **Exception translation** via `PersistenceExceptionTranslationPostProcessor` |
| `@Controller` | Web MVC | Detected by `RequestMappingHandlerMapping` |
| `@RestController` | Web REST | `@Controller` + `@ResponseBody` |

```java
@Component
public class IbanValidator { }

@Service
public class TransferService { }

@Repository
public class JdbcAccountRepository { }  // SQLException → DataAccessException

@Controller
public class TransferPageController { } // view names

@RestController
public class TransferApiController { }  // JSON body
```

**Important:** `@Service` is **not** “transactional by default.” `@Transactional` is separate (and is AOP). People confuse the two.

`@Repository` exception translation matters for JDBC/JPA DAOs you write yourself. Spring Data repositories already sit behind a similar mechanism.

Layering convention (not enforced by Spring):

```text
Controller → Service → Repository → Database
     web        rules      SQL/ORM
```

---

## 7. The container you actually use

The **IoC container** is the manager: it creates beans, injects them, runs extra hooks, and keeps a list of beans (the **registry**).

Two names you will hear. Do not panic — you almost always use the second one.

Two APIs:

| | `BeanFactory` | `ApplicationContext` |
|--|---------------|----------------------|
| Role | Basic factory + registry | `BeanFactory` plus enterprise features |
| Singleton default | **Lazy** | **Eager** (created at `refresh`) |
| Events | No | `ApplicationEventPublisher` |
| i18n | No | `MessageSource` |
| AOP / processors | Limited | Full |
| Environment | No | Yes |
| What you use | Almost never directly | Always |

```java
ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
OrderService service = ctx.getBean(OrderService.class);
```

In Spring Boot you still have an `ApplicationContext`. Boot just chooses a subclass (`AnnotationConfigServletWebServerApplicationContext`, etc.).

**Do not** call `getBean()` from business code. That hides dependencies again.

---

## 8. How a bean gets into the container (overview)

Details live in [054_1](054_1_IoC_Container_Internals.md). The mental sequence:

```text
1. Register BeanDefinitions
     @ComponentScan, @Bean methods, @Import, XML
2. BeanFactoryPostProcessors run
     they can still change definitions (example: property placeholders)
3. Instantiate matching singletons
     constructor injection happens here
4. Populate remaining properties
     setter / field injection
5. BeanPostProcessors
     @Autowired leftover, @PostConstruct, AOP proxy wrapping
6. Bean is in the singleton cache — ready
7. Context close → @PreDestroy for singletons
```

You write a class. Spring stores **metadata** (`BeanDefinition`) first, objects second. That split is why processors can change definitions before any `new`.

---

## 9. What this chapter deliberately leaves out

| Topic | Where |
|-------|--------|
| `refresh()` steps, 3-level cache, circular deps | [054_1](054_1_IoC_Container_Internals.md) |
| Full lifecycle order, all scopes, `@Lookup` | [054_2](054_2_Bean_Lifecycle_and_Scopes.md) |
| `@Configuration` full vs lite, `@Conditional` | [055](055_Spring_Annotations.md) |
| Proxies and `@Transactional` internals | [056](056_Spring_AOP.md) |

---

## Production pitfalls

1. **`new` inside a `@Service`** — that object is not a Spring bean. No injection, no proxy, no transaction.
2. **Service locator** — `ctx.getBean(X.class)` in domain code. Dependencies disappear from the constructor.
3. **Stateful singleton** — default scope is singleton. Request data in fields races under load.
4. **Assuming `@Service` is transactional** — it is not.
5. **Field injection in production services** — hides the graph; fights immutability.
6. **Calling `getBean` in a unit test to prove DI works** — if the class has a constructor, test it with `new`.

---

## Interview Ready Q&A (5–8 year standard)

The notes above are written simply. **These answers must sound senior:** definition + how Spring does it + what breaks. Each item has a **counter-question** — that is the real interview.

### Q1. What is Spring Framework in one paragraph?

**Answer:** An open-source, modular Java framework whose core is an IoC container. It creates and wires beans, then offers AOP, transactions, JDBC, MVC, events, and test support as separate modules. You write POJOs; the container owns construction and lifecycle.

**Counter:** How is that different from Spring Boot?

**Counter-answer:** Boot is not a different DI engine. It is Framework plus auto-configuration, starters, an embedded server, and an opinionated `SpringApplication` bootstrap. If Boot vanished, you could still build the same app with `AnnotationConfigApplicationContext` and more manual config.

---

### Q2. IoC vs DI — same or different?

**Answer:** IoC is the principle: control of object creation/wiring moves out of your code. DI is the mechanism Spring uses: the container injects collaborators. Service locator (`getBean`) is also IoC, but it is the opposite of DI because the class still *asks* for dependencies.

**Counter:** Then why do people use the words interchangeably in interviews?

**Counter-answer:** Because in Spring discussions DI is *the* IoC style. A precise answer (“DI implements IoC”) scores higher than treating them as synonyms, but do not lecture. State the distinction in one sentence, then talk DI.

---

### Q3. Why did Spring beat EJB for so many teams?

**Answer:** EJB 2 made simple things heavy: container services, XML, testing pain. Spring let you write POJOs, inject interfaces, and unit-test with `new`. Modular jars meant you did not buy a full Java EE stack. Boot later removed remaining ceremony.

**Counter:** Is EJB dead? Why does Jakarta EE still exist?

**Counter-answer:** Modern Jakarta (CDI, JAX-RS) learned from Spring. The market still standardized on Spring because of ecosystem (Boot, Data, Security, Cloud) and hiring. The win was not “XML vs annotations”; it was testability and a complete programming model.

---

### Q4. What are the three DI types, and which do you use?

**Answer:** Constructor, setter, field. Constructor for required deps. Setter for optional. Field almost never in services.

**Counter:** If constructor is best, why does `@Autowired` on fields still appear in so much code?

**Counter-answer:** History (Spring 2.5 made field injection easy), less typing, and tests that load a full context. It works until the class has eight hidden deps and cannot be constructed in a unit test. Teams migrate to constructors because reviews and testing get easier, not because field injection “stops working.”

---

### Q5. Why is constructor injection recommended?

**Answer:** Required deps are explicit, fields can be `final`, missing beans fail at startup, unit tests pass mocks in `new Service(...)`, and the object is never half-initialized. Spring 4.3+ autowires a single constructor without `@Autowired`.

**Counter:** How does Spring resolve circular constructor dependencies then?

**Counter-answer:** It does **not** — constructor cycles fail. Setter/field cycles can succeed because Spring exposes an early singleton reference. `@Lazy` on one constructor parameter is a workaround, not a design. See [054_1](054_1_IoC_Container_Internals.md).

---

### Q6. Do you need `@Autowired` on a constructor?

**Answer:** No, if there is exactly one constructor. Yes (or some other marker) if there are multiple and Spring would not know which to pick.

**Counter:** What if there is one constructor *and* you put `@Autowired(required = false)` on a parameter?

**Counter-answer:** Then that parameter may stay unsatisfied if no bean exists (Spring 5.1+ / optional injection). Required constructor params still fail the context if missing. Optional deps are clearer as `Optional<Foo>` or `ObjectProvider<Foo>` than as `required = false` soup.

---

### Q7. Setter vs constructor for optional dependencies?

**Answer:** Setter (or `ObjectProvider` / `Optional`) for optional. Constructor for required. Mixing is normal: constructor has the three must-haves; a setter takes an optional `MetricsRecorder`.

**Counter:** Isn’t `Optional<Foo>` in a constructor cleaner than a setter?

**Counter-answer:** Yes for a single optional collaborator. `ObjectProvider<Foo>` is better when you want lazy lookup or a stream of candidates. Setters still win when a framework injects after construction (some older Spring callbacks). Prefer constructor + `ObjectProvider` in new code.

---

### Q8. What is the Spring container?

**Answer:** The runtime that holds `BeanDefinition`s, creates beans, injects dependencies, runs lifecycle callbacks, and serves beans by type or name. In practice that object is an `ApplicationContext`.

**Counter:** Is the container thread-safe? Can I create beans at runtime?

**Counter-answer:** Singleton access is thread-safe after refresh. Creating definitions while the app is live (`registerSingleton` from random threads) is not how production apps work. You register everything at startup. Runtime registration is a special case (for example dynamic modules), and you treat it as advanced.

---

### Q9. BeanFactory vs ApplicationContext?

**Answer:** `BeanFactory` is the basic IoC SPI (lazy singletons). `ApplicationContext` extends it with eager singleton refresh, events, i18n, Environment, and full post-processor support. Real apps use `ApplicationContext`.

**Counter:** If BeanFactory is lazy, is it “faster/better” for microservices?

**Counter-answer:** No. Lazy startup hides configuration errors until the first request. You want fail-fast at deploy. Use `@Lazy` on *specific* heavy beans, not a whole `BeanFactory` mindset. Boot always gives you an `ApplicationContext`.

---

### Q10. Default bean scope?

**Answer:** Singleton — one instance per `ApplicationContext` (per container), not one per JVM if you somehow built two contexts.

**Counter:** Is a Spring singleton the same as the Gang of Four Singleton?

**Counter-answer:** No. GoF Singleton is typically one instance per ClassLoader, often via a static holder. Spring singleton is one instance **per container**. Two contexts = two instances of the same `@Service` class. That distinction comes up when people embed Spring twice or write tests that build extra contexts.

---

### Q11. Are `@Service` beans thread-safe?

**Answer:** No, not automatically. Singleton + mutable fields = shared state across requests. Keep services stateless (dependencies + locals), or protect state explicitly.

**Counter:** Then where do I store per-request data?

**Counter-answer:** Method arguments, `ThreadLocal` only with care (and clear it), request-scoped beans, or security context / MDC. Do not put “current user” on a field of a singleton service.

---

### Q12. Difference between `@Component`, `@Service`, `@Repository`, `@Controller`?

**Answer:** All are `@Component` for scanning. `@Service` is semantic. `@Repository` adds persistence exception translation. `@Controller` is picked up by MVC. `@RestController` adds `@ResponseBody`.

**Counter:** If I put `@Component` on a DAO, do I lose exception translation?

**Counter-answer:** Yes, unless another processor is registered for that class. The translation AOP advisor looks for `@Repository` (or `repository-impl` XML). Use the right stereotype; it is not only “for readability.”

---

### Q13. Can two beans have the same type?

**Answer:** Yes. Injection by type then fails with `NoUniqueBeanDefinitionException` unless you use `@Primary`, `@Qualifier`, the parameter name matching a bean name, or inject a `List`/`Map` of that type.

**Counter:** Which wins — `@Qualifier` or `@Primary`?

**Counter-answer:** `@Qualifier` on the injection point wins. `@Primary` is the default when nothing more specific is said. Details in [055](055_Spring_Annotations.md).

---

### Q14. Why is `new` inside a Spring bean a bug more often than not?

**Answer:** The collaborator is not in the container: no DI, no scope, no AOP (`@Transactional`, `@Async`, security). You silently built a parallel object graph.

**Counter:** When *is* `new` correct?

**Counter-answer:** True value objects, DTOs, entities you map yourself, and strategy objects created per call that must not be beans. Also `new` in a `@Bean` factory method is how you *register* third-party types. The rule is: if it needs Spring services, it must be a bean (or created by a bean factory method).

---

### Q15. What does `@Repository` exception translation actually do?

**Answer:** A `BeanPostProcessor` / advisor wraps `@Repository` beans so that vendor exceptions (`SQLException`, Hibernate exceptions) become Spring’s unchecked `DataAccessException` hierarchy. Service code can catch `DataIntegrityViolationException` instead of JDBC types.

**Counter:** Does this work for Spring Data JPA repositories?

**Counter-answer:** Spring Data applies its own translation. You still catch `DataAccessException` (or subclasses). You do not need `@Repository` on the interface for that to work; the infrastructure already registers it.

---

### Q16. Is Spring a framework or an inversion-of-control container?

**Answer:** Both. The container (`spring-beans` / `spring-context`) is the heart. The Framework is the container plus AOP, web, tx, jdbc, test, and the rest. Saying “Spring is just DI” undersells it; saying “Spring is a full application server” oversells it.

**Counter:** Where does an application server still fit?

**Counter-answer:** You can run Spring in Tomcat/Jetty (WAR) or with an embedded server (Boot JAR). The servlet container still handles HTTP sockets and the servlet spec. Spring MVC sits *inside* that as `DispatcherServlet`.

---

### Q17. How do you unit-test a class that uses DI without starting Spring?

**Answer:** Constructor-inject mocks or fakes: `new OrderService(mockRepo, mockGateway)`. That is the payoff of constructor injection.

**Counter:** When do you start a Spring context in tests then?

**Counter-answer:** When you are testing wiring, MVC mapping, slice of persistence, or AOP (transactions). That is an integration test. See [056_4](056_4_Spring_Testing.md). Mixing “I new the service” and “I `@SpringBootTest` everything” without a reason is how suites become slow.

---

### Q18. What happens if a required dependency is missing?

**Answer:** Context refresh fails with `NoSuchBeanDefinitionException` / `UnsatisfiedDependencyException`. Fail-fast at startup.

**Counter:** `@Autowired(required = false)` then?

**Counter-answer:** Field/parameter stays `null` (or optional handling applies). You traded a startup error for a possible NPE later. Use it only when the absence is a real configuration variant, and null-check or use `ObjectProvider`.

---

### Q19. Name the Core modules you would pull for a non-web batch job vs a REST service.

**Answer:** Batch/job: `spring-context` (and `spring-tx`/`spring-jdbc` if it hits a DB). REST: those plus `spring-webmvc`. AOP/tx as needed. You do not need `spring-webmvc` for a CLI that only processes files.

**Counter:** Does adding `spring-webmvc` automatically start Tomcat?

**Counter-answer:** In **raw Framework**, no — you still need a servlet container and to register `DispatcherServlet`. In **Boot**, `spring-boot-starter-web` pulls Tomcat and auto-config. Do not confuse Framework jars with Boot starters in an interview.

---

### Q20. What is a POJO in the Spring sense, and why does it matter?

**Answer:** A class with no required superclass and no forced container API. You *can* implement `InitializingBean`, but you should not need to. That is why the same class runs in a unit test and in production.

**Counter:** Then why do `ApplicationContextAware` and `BeanNameAware` exist?

**Counter-answer:** Escape hatches for infrastructure. Using them in domain services couples you to Spring and is a smell. They appear in Framework internals and some libraries. Prefer constructor injection of an interface you own.

---

### Interview one-liner

> Spring Core = IoC container + DI. Constructor injection is the contract for required dependencies. `ApplicationContext` is the container you use. Stereotypes are mostly documentation; `@Repository` also translates persistence exceptions. Default scope is singleton — keep services stateless.
