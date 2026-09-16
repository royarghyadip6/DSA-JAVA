# 56.1 Events, SpEL, Resources, Conversion, MessageSource

[← 056 AOP](056_Spring_AOP.md) | [Course map](00_COURSE_MAP.md) | **Next:** [056_2 Spring MVC Internals →](056_2_Spring_MVC_Internals.md)

Teaching is simple first. **Interview Q&A at the end is 5–8 year standard.**

---

## Simple first

The container is not only a bean factory. It also has five extra “desks”:

| Desk | What you use it for | Picture |
|------|---------------------|---------|
| **Events** | “Something happened” — other beans can listen | Office announcement |
| **SpEL** | A tiny formula in an annotation | Calculator |
| **Resource** | Open a file from classpath / disk | “Get me that document” |
| **Conversion** | `"true"` → `boolean`, `"42"` → `int` | Translator |
| **MessageSource** | `order.created` → “Order created” in English/Hindi | Phrase book (i18n) |

**Events (most important desk):**

```text
CheckoutService:  publish “order 99 placed”
InvoiceListener:  hears it and sends an invoice
```

By default this is **the same thread, right now**. If checkout is in a database transaction, the listener runs **before commit** unless you use `@TransactionalEventListener(AFTER_COMMIT)`. That is how you avoid “email sent, then database rolled back.”

**`${}` vs `#{}`**

- `${app.port}` = property
- `#{systemProperties['user.home']}` = SpEL formula  
  SpEL on `@Value` runs **once** when the bean is created, not on every request.

**Files:** use `classpath:license.txt` and `getInputStream()`. Do **not** call `getFile()` on a file inside a JAR (there is no real `File` on disk).

---

## When you interview (5–8 years)

They will ask: sync listeners vs after-commit; SpEL vs placeholders; `classpath` vs `classpath*`; Converter vs Formatter.

---

## 1. Application events

Spring implements an in-process pub/sub.

```java
public class OrderPlacedEvent extends ApplicationEvent {
    private final long orderId;
    public OrderPlacedEvent(Object source, long orderId) {
        super(source);
        this.orderId = orderId;
    }
    public long getOrderId() { return orderId; }
}

@Service
public class CheckoutService {
    private final ApplicationEventPublisher publisher;

    public CheckoutService(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void checkout(Order order) {
        // persist...
        publisher.publishEvent(new OrderPlacedEvent(this, order.getId()));
    }
}

@Component
public class InvoiceListener {
    @EventListener
    public void on(OrderPlacedEvent event) {
        // send invoice
    }
}
```

Since Spring 4.2, the event **does not need** to extend `ApplicationEvent`. A POJO works:

```java
public record OrderPlaced(long orderId) { }

@EventListener
public void on(OrderPlaced event) { }
```

`publishEvent` is available because `ApplicationContext` **is** an `ApplicationEventPublisher`. Inject the interface, not the context.

### Sync vs async

**Default: synchronous, same thread, same call stack.** Publisher waits for every listener. If a listener throws, later listeners may not run (unless you configure an error handler). The publisher’s transaction is still open if it published **inside** `@Transactional` — listeners see uncommitted data and **participate in the same transaction** unless they start their own.

```java
@Async
@EventListener
public void on(OrderPlaced event) { }   // different thread; needs @EnableAsync
```

Async listeners: no shared transaction, harder error handling, `@Transactional` on the listener is a **new** transaction.

### `@TransactionalEventListener`

```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void on(OrderPlaced event) {
    // only if the publishing transaction commits
}
```

| Phase | When |
|-------|------|
| `BEFORE_COMMIT` | Still in tx, about to commit |
| `AFTER_COMMIT` | Tx committed — **usual for “send email / message”** |
| `AFTER_ROLLBACK` | Tx rolled back |
| `AFTER_COMPLETION` | Either outcome |

If you publish **outside** a transaction, the listener is **not** called (default `fallbackExecution = false`). Set `fallbackExecution = true` to run anyway.

This is how you avoid “email sent, then rollback.”

### Ordering and conditions

```java
@EventListener(condition = "#event.orderId > 0")
@Order(1)
public void on(OrderPlaced event) { }
```

`@Order` on listeners: lower runs first (sync).

### Built-in context events

| Event | Meaning |
|-------|---------|
| `ContextRefreshedEvent` | `refresh()` finished (may fire twice if you call refresh twice) |
| `ContextStartedEvent` / `ContextStoppedEvent` | `start()` / `stop()` Lifecycle |
| `ContextClosedEvent` | close |
| `RequestHandledEvent` | MVC request finished (web) |

Prefer `ContextRefreshedEvent` or `ApplicationRunner` over “start threads in `@PostConstruct`.”

### Multicaster

`SimpleApplicationEventMulticaster` — sync by default. You can `setTaskExecutor` for async **all** events (blunt). Prefer `@Async` on specific listeners.

Do **not** use events as a replacement for a return value on a local call. They shine for **decoupling side effects** (metrics, notifications) and **after-commit** work.

---

## 2. SpEL (Spring Expression Language)

SpEL is an expression language over objects, beans, and properties.

| Syntax | Engine |
|--------|--------|
| `${app.port}` | Property placeholder (Environment) |
| `#{expression}` | SpEL |

They combine: `#{'${app.env}'.toUpperCase()}` — placeholder first, then SpEL.

Where SpEL appears in Core:

- `@Value("#{...}")`
- `@EventListener(condition = "...")`
- `@Cacheable(key = "#userId", condition = "...")` ([056_5](056_5_Async_Scheduling_Cache.md))
- `@Conditional` implementations that parse expressions
- Security (later): `hasRole('ADMIN')` — **not Core**, but same family
- XML `#{bean.property}`

```java
@Value("#{systemProperties['user.home']}")
String home;

@Value("#{T(java.lang.Math).PI}")
double pi;

@Value("#{clock.instant}")  // bean named clock
Instant now;
```

Useful pieces:

| Feature | Example |
|---------|---------|
| Literal | `#{42}`, `#{'text'}` |
| Bean | `#{inventoryService}` |
| Property | `#{user.name}` |
| Method | `#{user.getName()}` |
| Static | `#{T(java.lang.Math).random()}` |
| Relational | `#{count > 10}` |
| Elvis / safe nav | `#{user?.name ?: 'guest'}` |
| Selection | `#{list.?[age > 18]}` |
| Projection | `#{list.![name]}` |
| `#root` / `#this` | current object |
| `#p0` / parameter names | in annotation expressions |

**`@Value` + SpEL on a singleton is evaluated once at injection**, not on every request. Do not put “current time” in `@Value` expecting it to update.

SpEL can call arbitrary methods — **do not** parse untrusted user input as SpEL (injection attacks). CVE history here is real.

---

## 3. Resources and ResourceLoader

`Resource` is Spring’s handle for a byte stream + metadata (exists, filename, URL).

| Prefix | Meaning |
|--------|---------|
| `classpath:` | Classpath, one resource (`ClassPathResource`) |
| `classpath*:` | Scan multiple (only in certain APIs, e.g. `PathMatchingResourcePatternResolver`) |
| `file:` | Filesystem |
| `http:` / `https:` | URL |
| no prefix | Context-dependent (`ServletContextResource` in web, classpath otherwise) |

```java
@Service
public class LicenseLoader {
    private final ResourceLoader loader;

    public LicenseLoader(ResourceLoader loader) {
        this.loader = loader;
    }

    public String load() throws IOException {
        Resource r = loader.getResource("classpath:license.txt");
        try (InputStream in = r.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
```

`ApplicationContext` **is** a `ResourceLoader`. Inject `ResourceLoader` or `ResourcePatternResolver`.

```java
@Value("classpath:license.txt")
Resource license;
```

`ResourcePatternResolver.getResources("classpath*:META-INF/spring.components")` — `classpath*` is required to search **all** JARs.

**Pitfall:** `classpath:dir/*.txt` is **not** a glob with `getResource`. Use `getResources` + `classpath*:`.

Servlet context resources (`/WEB-INF/views/...`) work in WAR deployments; in Boot JAR they may not exist as files. Prefer classpath.

---

## 4. ConversionService and formatters

Spring converts strings (properties, path variables, `@RequestParam`) to target types through `ConversionService` (`DefaultFormattingConversionService` in MVC).

SPI:

| SPI | Role |
|-----|------|
| `Converter<S,T>` | One source type → one target |
| `ConverterFactory` | e.g. String → Enum |
| `GenericConverter` | Complex type info |
| `Formatter<T>` | Print + parse with `Locale` (MVC forms, dates) |

```java
public class StringToMoneyConverter implements Converter<String, Money> {
    @Override
    public Money convert(String s) {
        return Money.parse(s);
    }
}

@Configuration
public class ConvConfig {
    @Bean
    public ConversionServiceFactoryBean conversionService() {
        ConversionServiceFactoryBean f = new ConversionServiceFactoryBean();
        f.setConverters(Set.of(new StringToMoneyConverter()));
        return f;
    }
}
```

In MVC you usually implement `WebMvcConfigurer.addFormatters`.

`@Value("${timeout}") int timeout` uses conversion (and `DefaultConversionService` for Environment).

`PropertyEditor` is the old JavaBeans API. Converters replaced it. You may still see `CustomEditorConfigurer` in XML apps.

If conversion fails at injection → context fails (good). If it fails on a request → 400 in MVC (see [056_2](056_2_Spring_MVC_Internals.md)).

---

## 5. MessageSource (i18n)

`ApplicationContext` extends `MessageSource`.

```java
@Bean
public MessageSource messageSource() {
    ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
    ms.setBasename("classpath:i18n/messages");
    ms.setDefaultEncoding("UTF-8");
    return ms;
}
```

Files: `messages.properties`, `messages_fr.properties`.

```java
String text = messageSource.getMessage("order.created", new Object[]{id}, locale);
```

MVC: `LocaleResolver` (cookie, session, `Accept-Language`) + `LocaleChangeInterceptor` + `#{...}` / `th:text="#{order.created}"` in views.

`HierarchicalMessageSource`: child context can fall back to parent’s messages.

If no `MessageSource` bean is defined, context uses a default empty one; `getMessage` throws `NoSuchMessageException` or returns the default you pass.

---

## Production pitfalls

1. **Publishing events inside a transaction** then doing I/O in a sync listener — holds DB connections; listener failure rolls back business work. Use `@TransactionalEventListener(AFTER_COMMIT)` + async for I/O.
2. **Assuming listeners are independent** — default is sync, ordered, same thread.
3. **SpEL on user input** — security bug.
4. **`@Value("#{...}")` expecting live updates** — evaluated at inject time.
5. **`classpath:` glob** with `getResource` — use `classpath*` + `getResources`.
6. **Missing converter** — cryptic `ConversionFailedException` at startup or 400 at runtime.
7. **Forgetting MessageSource basename** — keys printed instead of text, or exceptions in APIs.

---

## Interview Ready Q&A (5–8 year standard)

The notes used office announcements. **Here, name phases, fallbackExecution, SpEL safety, and Resource `getFile()` vs stream.**

### Q1. How do Spring events work by default?

**Answer:** `publishEvent` on `ApplicationEventPublisher`. Matching `@EventListener` methods run **synchronously** on the same thread. The event may be a POJO (4.2+) or `ApplicationEvent`.

**Counter:** Does the listener run inside the publisher’s `@Transactional`?

**Counter-answer:** Yes, if it is sync and the publish happens inside the tx. The listener can see uncommitted rows and can mark the tx rollback-only. That is why after-commit listeners exist.

---

### Q2. When do you use `@TransactionalEventListener`?

**Answer:** Side effects that must happen **only if** the transaction commits (email, Kafka, push). `AFTER_COMMIT` is the usual phase.

**Counter:** You published outside a transaction. Listener did not run. Why?

**Counter-answer:** Default `fallbackExecution = false`. Either publish from a `@Transactional` method or set `fallbackExecution = true`.

---

### Q3. `@Async` on an event listener — what changes?

**Answer:** Listener runs on a task executor thread. No shared transaction. Publisher does not wait (after submit). Need `@EnableAsync`. Exceptions do not roll back the publisher.

**Counter:** Self-invocation rules?

**Counter-answer:** `@Async` is AOP. The listener method must be called through the listener **bean proxy**. `@EventListener` infrastructure invokes the bean via the container, so it **does** go through the proxy — unlike `this.asyncMethod()`. This is a case where the framework call path is already proxied.

---

### Q4. `${}` vs `#{}`?

**Answer:** `${}` = property placeholder from Environment. `#{}` = SpEL. You can nest `${}` inside `#{}`.

**Counter:** `@Value("${app.hosts}")` vs `@Value("#{${app.hosts}}")`?

**Counter-answer:** First is a string (or converted type). Second tries to parse the property value as a SpEL program — usually wrong and dangerous. Do not put SpEL in property files unless you fully control them.

---

### Q5. When is `@Value` SpEL evaluated?

**Answer:** At bean creation (injection). Singleton: once. Prototype: each instance.

**Counter:** How do you get a value that changes per request?

**Counter-answer:** Read `Environment` in the method, or inject a request-scoped bean, or parse the HTTP request in MVC. Not `@Value`.

---

### Q6. What is a `Resource`?

**Answer:** Spring’s abstraction over an InputStream source: classpath, file, URL, servlet context. `ResourceLoader.getResource(location)` with prefixes `classpath:`, `file:`, etc.

**Counter:** `classpath:` vs `classpath*:`?

**Counter-answer:** `classpath:` = first match on the classpath. `classpath*:` = all matches across JARs, used with `ResourcePatternResolver.getResources`.

---

### Q7. Why inject `ResourceLoader` instead of `new ClassPathResource`?

**Answer:** The context can resolve servlet-relative and protocol-relative locations; tests can override; you stay on the abstraction. Direct `ClassPathResource` is fine for a known classpath file in a library.

**Counter:** `@Value("classpath:x.txt") Resource` vs loader?

**Counter-answer:** Both work. `@Value` is static location. Loader is for locations computed at runtime.

---

### Q8. How does `"true"` become `boolean` in `@Value`?

**Answer:** `ConversionService` / default property editors. Environment type conversion.

**Counter:** Custom type `Money`?

**Counter-answer:** Register a `Converter<String, Money>` on the conversion service (or a `Formatter`). Without it, injection fails.

---

### Q9. Converter vs Formatter vs PropertyEditor?

**Answer:** `Converter` = type to type, no locale. `Formatter` = parse/print with locale (web forms). `PropertyEditor` = legacy JavaBeans. Prefer Converter/Formatter.

**Counter:** Where do MVC path variables get converted?

**Counter-answer:** Same `ConversionService` / `WebDataBinder` used by `RequestMappingHandlerAdapter`. See [056_2](056_2_Spring_MVC_Internals.md).

---

### Q10. What is `MessageSource`?

**Answer:** i18n facade: key + args + locale → string. `ApplicationContext` is a `MessageSource`. Typical impl: `ReloadableResourceBundleMessageSource` with `messages_en.properties`.

**Counter:** Parent/child contexts?

**Counter-answer:** Child `MessageSource` can have a parent; unresolved codes fall back. Web apps often have a root context message source used by the servlet child.

---

### Q11. `ContextRefreshedEvent` vs `@PostConstruct`?

**Answer:** `@PostConstruct` is that bean only; other beans may still be initializing. `ContextRefreshedEvent` fires when the context is fully refreshed. Use it (or `ApplicationRunner`) for “system up” tasks.

**Counter:** Event fires twice in tests?

**Counter-answer:** `refresh()` twice, parent and child contexts, or Boot + extra initializer. Guard with a boolean or use `ApplicationRunner` once.

---

### Q12. Can an `@EventListener` return a value?

**Answer:** Yes — the return can be published as a **new** event (or array/collection of events). That chains events.

**Counter:** Good design?

**Counter-answer:** Easy to create implicit graphs. Fine for small pipelines; for workflows prefer explicit services. Document the chain.

---

### Q13. How do you keep a listener from rolling back checkout?

**Answer:** `@TransactionalEventListener(AFTER_COMMIT)` and/or `@Async` so failures are isolated. Catch exceptions inside the listener. Do not throw from a sync listener if the publisher must succeed.

**Counter:** AFTER_COMMIT and then the listener’s DB write fails?

**Counter-answer:** Checkout is already committed. You need retry / outbox / compensation. Events are not a distributed transaction. (Outbox pattern belongs with later Kafka notes.)

---

### Q14. SpEL `T(com.app.Flags).enabled` — what is `T`?

**Answer:** Type operator — reference a `Class` for static members.

**Counter:** Security risk?

**Counter-answer:** If expressions are static in annotations you wrote, fine. If users can supply SpEL, they can call static methods — treat as code execution.

---

### Q15. `Resource.exists()` is false on classpath in a JAR. Why?

**Answer:** Sometimes people use `resource.getFile()` which **requires** a file on disk. Inside a JAR there is no `File`. Use `getInputStream()`. `exists()` should still work; `getFile()` throws.

**Counter:** Tests pass in IDE, fail in JAR?

**Counter-answer:** Classic `getFile()` on classpath resources. Always stream.

---

### Q16. How would you load all `*.json` under `classpath:contracts/`?

**Answer:** `resourcePatternResolver.getResources("classpath*:contracts/*.json")`. Not `getResource` with a glob.

**Counter:** Subdirectories?

**Counter-answer:** `classpath*:contracts/**/*.json` with Ant-style patterns.

---

### Q17. Default locale for `MessageSource`?

**Answer:** `Locale.getDefault()` JVM default unless you `setDefaultLocale`. Web: `LocaleResolver` chooses per request.

**Counter:** Why did CI tests fail on a French OS?

**Counter-answer:** JVM default locale leaked. Set default locale in `MessageSource` or in tests (`LocaleContextHolder`).

---

### Q18. Is `ApplicationContext` too fat to inject for events?

**Answer:** Yes if you only need publish. Inject `ApplicationEventPublisher`. Same for `ResourceLoader`, `Environment`. Interface segregation.

**Counter:** Is injecting `ApplicationContext` always wrong?

**Counter-answer:** Infrastructure (plugin loaders) sometimes needs `getBean`. Domain services should not.

---

### Q19. `@EventListener` vs implementing `ApplicationListener<T>`?

**Answer:** Annotation is shorter, supports condition SpEL and `@Async`. Interface is older, type-safe, one `onApplicationEvent`. Both work. Prefer `@EventListener` in new code.

**Counter:** Generic erasure on the interface?

**Counter-answer:** `ApplicationListener<OrderPlaced>` is resolved via generic type. Raw `ApplicationListener` gets all events — easy to mess up. Annotations specify the method parameter type clearly.

---

### Q20. ConversionService vs `@Value` default syntax `${x:1}`?

**Answer:** Default is placeholder syntax, still a string `"1"` then converted to int. Conversion happens after placeholder resolution.

**Counter:** Empty string vs missing key?

**Counter-answer:** Missing key uses default. Present-but-empty `${x:}` may convert badly (`""` to int fails). Know the difference when ops set `X=` empty in the environment.

---

### Interview one-liner

> Events are sync and in-process by default; use `@TransactionalEventListener(AFTER_COMMIT)` for side effects. `${}` is Environment; `#{}` is SpEL (once at inject for `@Value`). `Resource` + prefixes; never `getFile()` on classpath JARs. `ConversionService` binds strings to types. `MessageSource` is i18n on the context.
