# 54.2 Bean Lifecycle and Scopes

[← 054_1 Container Internals](054_1_IoC_Container_Internals.md) | [Course map](00_COURSE_MAP.md) | **Next:** [055 Config and Annotations →](055_Spring_Annotations.md)

Teaching is simple first. **Interview Q&A at the end is 5–8 year standard.**

---

## Simple first

**Lifecycle** = the life of one bean: born → plugged in → “I’m ready” → used → cleaned up.

**Scope** = *how many copies* and *how long each copy lives*.

Everyday picture:

| Scope | Like | How many |
|-------|------|----------|
| **Singleton** (default) | One office printer for the whole company | One per Spring app |
| **Prototype** | A paper cup — new cup every time you ask | New object every request from Spring |
| **Request** | A tray for *this* HTTP request only | One per browser request |
| **Session** | A shopping basket for one logged-in user | One per login session |

**The trap every beginner hits:** you inject a paper cup (prototype) into the office printer (singleton). The printer holds **one cup forever**. You did not get a new cup per call unless you ask Spring again (`ObjectProvider` / `@Lookup`).

**Order of life (remember this drawing):**

```text
new (constructor)  →  inject helpers  →  @PostConstruct (“I’m ready”)
       →  maybe wrap with a proxy  →  use  →  @PreDestroy (singletons only)
```

**Proxy** (from later AOP chapter) = a wrapper. `@PostConstruct` runs on the **real** object. Calling `this.myTransactionalMethod()` from inside the same class skips the wrapper.

---

## When you interview (5–8 years)

Lifecycle questions tell whether you know **when collaborators are safe**, **why `@Transactional` is missing on `this`**, and **why a prototype in a singleton is not a prototype anymore**.

---

## 1. Full callback order (one singleton)

This is the order you should be able to write from memory **after** you understand the simple picture.

**Plain English of the same list:**

1. `new` — constructor runs (required helpers are passed in here)
2. Fill remaining fields / setters
3. Spring says “here is your name / context” (Aware)
4. `@PostConstruct` — “you are fully plugged in, you may start”
5. Maybe wrap you in a proxy
6. Other beans may now use you
7. On shutdown, `@PreDestroy` — only if Spring still tracks you (singletons)

The numbered list below is the same story with official names.

```text
1.  Instantiation
      constructor (constructor injection happens here)

2.  populateBean
      setter / field injection (@Autowired, @Value)

3.  Aware callbacks (inside initializeBean)
      BeanNameAware.setBeanName
      BeanClassLoaderAware
      BeanFactoryAware
      ApplicationContextAware / EnvironmentAware / ResourceLoaderAware / ...

4.  BeanPostProcessor.postProcessBeforeInitialization
      (many processors no-op here; some wrap, some validate)

5.  Initialization callbacks (in this order if several exist)
      @PostConstruct
      InitializingBean.afterPropertiesSet()
      custom init-method  (@Bean(initMethod = "start") or XML)

6.  BeanPostProcessor.postProcessAfterInitialization
      ★ AnnotationAwareAspectJAutoProxyCreator often returns a proxy here
      @Transactional / @Async / custom @Aspect now sit on the object
      that will be stored in the singleton cache

7.  Bean in use  (L1 cache for singletons)

8.  Destruction (only if the container manages destruction)
      DestructionAwareBeanPostProcessor
      @PreDestroy
      DisposableBean.destroy()
      custom destroy-method
```

```java
@Component
public class CacheWarmer implements InitializingBean, DisposableBean,
        ApplicationContextAware {

    private final CatalogRepository repo;

    public CacheWarmer(CatalogRepository repo) {          // 1 constructor
        this.repo = repo;
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) { } // 3 Aware

    @PostConstruct
    public void postConstruct() { }                       // 5a

    @Override
    public void afterPropertiesSet() { }                  // 5b

    @PreDestroy
    public void preDestroy() { }                          // 8a

    @Override
    public void destroy() { }                             // 8b
}
```

Plus `@Bean(initMethod = "start", destroyMethod = "stop")` as 5c / 8c.

**JSR-250 vs Spring APIs:** prefer `@PostConstruct` / `@PreDestroy` (Jakarta annotations in Spring 6). `InitializingBean` / `DisposableBean` couple you to Spring. `initMethod` is useful when you cannot change a third-party class.

---

## 2. What is already safe in `@PostConstruct`?

| Need | Safe in `@PostConstruct`? |
|------|---------------------------|
| Constructor-injected deps | Yes — injection already happened |
| `@Value` fields | Yes |
| `ApplicationContext` if Aware or injected | Yes |
| Calling **another bean’s** `@Transactional` method | Yes — you go through **that** bean’s proxy |
| Calling **your own** `@Transactional` / `@Async` method via `this` | **No** — you are the raw target; AOP wrap is step 6, after init. Even after wrap, `this` is still the raw instance. |
| Publishing events | Yes, but listeners may not all be up if you publish very early; `ContextRefreshedEvent` is safer for “app is fully up” |

**Memory trick:** Init callbacks run on the **raw** bean. Proxies are applied **after** init.

---

## 3. Destruction rules

Spring calls destroy callbacks when:

- `ConfigurableApplicationContext.close()` / JVM shutdown hook registered by Boot
- The bean is a **singleton** (or another scope whose destruction Spring tracks, e.g. request/session in a web context)

Spring does **not** call `@PreDestroy` for **prototypes**. You created N objects; Spring does not keep a list of them to destroy. If a prototype holds a file handle, **you** close it (or wrap it in something singleton that does).

`DisposableBean` and `destroyMethod` follow the same rule.

`@Bean` methods that return `AutoCloseable` / `Closeable`: Spring 4.3+ infers `destroyMethod = "close"` by default. Set `destroyMethod = ""` if you do not want that (shared / borrowed resources).

---

## 4. Scopes

| Scope | How many | Lifetime | Typical use |
|-------|----------|----------|-------------|
| `singleton` (default) | One per container | Context start → close | Services, repos, config, most everything |
| `prototype` | New every `getBean` / injection | Spring stops after init | Stateful workers, per-operation builders |
| `request` | One per HTTP request | Request | Request ID, current tenant from header |
| `session` | One per HTTP session | Session | Shopping cart (legacy style) |
| `application` | One per `ServletContext` | Web app | Rarely needed; almost a singleton |
| `websocket` | One per WebSocket session | Socket | Chat session state |
| custom | You decide | You decide | Tenant, conversation, batch step |

Web scopes exist only in a **web** `ApplicationContext`. They need a scoped proxy (or lookup) when injected into a singleton.

```java
@Service                         // singleton
public class OrderService { }

@Component
@Scope("prototype")
public class ReportJob { }

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST,
       proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestTrace { }
```

### Singleton vs application vs “static singleton”

- Spring singleton = one per **container**
- `application` scope = one per **ServletContext** (usually one web app)
- JVM static singleton = one per **ClassLoader**

Two Spring contexts in one JVM (parent/child, tests) → two Spring singletons of the same class.

### Thread safety

Default singleton is **shared across threads** (all HTTP requests). Rules:

- No mutable request state in fields
- Dependencies should themselves be thread-safe (typical services/repos are)
- If you must cache, use concurrent structures or a real cache ([056_5](056_5_Async_Scheduling_Cache.md))
- Prototype is **not** a thread-safety strategy unless each thread gets its own instance *and* you requested it correctly

---

## 5. The prototype-inside-singleton problem

This is the highest-value scope question.

```java
@Component
@Scope("prototype")
public class ReportJob {
    public void run() { /* stateful */ }
}

@Service
public class ReportService {
    private final ReportJob job;           // injected ONCE

    public ReportService(ReportJob job) {
        this.job = job;
    }

    public void generate() {
        job.run();                         // SAME instance every call
    }
}
```

`ReportService` is created once. Constructor injection asks for `ReportJob` **once**. You stored a single prototype. Further calls reuse it. Scope is not “contagious.”

### Fixes

**1. `ObjectProvider` / `ObjectFactory` (preferred)**

```java
@Service
public class ReportService {
    private final ObjectProvider<ReportJob> jobs;

    public ReportService(ObjectProvider<ReportJob> jobs) {
        this.jobs = jobs;
    }

    public void generate() {
        jobs.getObject().run();            // new prototype each time
    }
}
```

**2. `@Lookup` (CGLIB subclass of the singleton)**

```java
@Service
public abstract class ReportService {
    public void generate() {
        createJob().run();
    }

    @Lookup
    protected abstract ReportJob createJob();
}
```

Works unless the singleton class is `final` or you cannot subclass (and you must not call `new ReportService()` in tests without Spring — `@Lookup` needs the container).

**3. Scoped proxy on the prototype** (less common for prototype; standard for request/session)

```java
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ReportJob { }
```

Every method call on the injected proxy hits `getBean` again. Easy to misuse: two method calls = two instances. Prefer `ObjectProvider` when you want one instance per **use case**, not per **method**.

**4. Inject `ApplicationContext` and `getBean`** — works, hides the dependency, service locator smell.

---

## 6. Request/session into a singleton — scoped proxies

```java
@Service
public class PricingService {
    private final RequestTrace trace; // singleton cannot hold a real request bean

    public PricingService(RequestTrace trace) {
        this.trace = trace;
    }
}
```

Without a proxy, Spring would inject **one** request-scoped object at startup — there is no request yet → error, or a stale instance.

With `proxyMode = TARGET_CLASS` (CGLIB) or `INTERFACES` (JDK):

```text
PricingService stores a proxy
Each method call on proxy → fetch the object bound to current HTTP request
No request → IllegalStateException (you called it from a thread with no request)
```

You must also register `RequestContextListener` or use Spring MVC’s `DispatcherServlet` (it already binds request attributes). Async threads **lose** the request unless you wrap with `RequestContextHolder` / task decorator.

`ScopedProxyMode.INTERFACES` requires the bean to implement an interface. `TARGET_CLASS` subclasses the concrete class (`final` methods not intercepted).

---

## 7. Custom scopes

Implement `org.springframework.beans.factory.config.Scope`:

```text
get(name, ObjectFactory)  — return existing or factory.getObject() and store
remove(name)
registerDestructionCallback
resolveContextualObject  — e.g. "request" → HttpServletRequest
getConversationId
```

Register with `beanFactory.registerScope("tenant", new TenantScope())`.

Use when the lifetime is neither singleton nor HTTP (tenant per message on a Kafka listener thread, one context per batch step). Custom scopes are rare; interviewers want to hear you *would not* invent one for ordinary request data.

---

## 8. `SmartLifecycle` vs `@PostConstruct`

`@PostConstruct` = this bean is ready.

`SmartLifecycle` / `Lifecycle` = participate in a **phased start/stop** of the whole context (`finishRefresh` / close). Use for listeners, schedulers, connectors that should start **after** most beans, and stop in reverse order.

`@Order` / `getPhase()`: higher phase starts later, stops earlier.

Boot’s `ApplicationRunner` / `CommandLineRunner` run after the context is up — even later than typical `@PostConstruct`. Prefer them for “run this once when the app is accepting work.”

---

## Production pitfalls

1. **Heavy work in constructors** — collaborators may not be injected yet (constructor is fine for assigning `final` fields; do I/O in `@PostConstruct` or a runner). Constructor injection assigns fields; the *other* beans might still be initializing if you call out from a constructor.
2. **Prototype `@PreDestroy` never runs** — connection leaks.
3. **Prototype in singleton field** — silent logic bug, not a startup error.
4. **Request-scoped bean on a `@Scheduled` / `@Async` thread** — no request bound.
5. **Mutable fields on a singleton** — race conditions.
6. **`@PostConstruct` calling `this.transactionalMethod()`** — no proxy, no transaction.
7. **Destroy order** — dependents may already be destroyed; do not call other beans in `@PreDestroy` unless you know order (`@DependsOn` is not a full destroy-order API). Keep destroy local (close *your* handles).

---

## Interview Ready Q&A (5–8 year standard)

The notes used printers and paper cups. **Here, name callbacks in order, scoped proxies, `@Lookup`, and destroy rules.**

### Q1. Recite the bean lifecycle.

**Answer:** Instantiate (constructor injection) → populate (setter/field) → Aware → `postProcessBeforeInitialization` → `@PostConstruct` / `afterPropertiesSet` / init-method → `postProcessAfterInitialization` (AOP proxy) → in use → `@PreDestroy` / `destroy` / destroy-method on shutdown for container-managed scopes.

**Counter:** Where does `@Transactional` wrapping happen in that list?

**Counter-answer:** `postProcessAfterInitialization` (auto-proxy creator). Init methods have already run on the raw object. That is why self-invocation and “transaction in `@PostConstruct` on `this`” do not work.

---

### Q2. `@PostConstruct` vs `InitializingBean` vs `initMethod` — order if all three exist?

**Answer:** `@PostConstruct`, then `afterPropertiesSet()`, then `initMethod`. All run after injection and Aware, before AOP after-init wrapping.

**Counter:** Which should you use in new code?

**Counter-answer:** `@PostConstruct` (Jakarta). Use `initMethod` when you cannot annotate a third-party class. Avoid `InitializingBean` unless writing Spring infrastructure.

---

### Q3. Can `@PostConstruct` use injected dependencies?

**Answer:** Yes. Population already happened.

**Counter:** Can it safely call an `@Async` method on another bean? On itself?

**Counter-answer:** Other bean: yes, you invoke the proxy. Self: no, `this` is raw; also the async BPP may not have wrapped you yet. Publish `ContextRefreshedEvent` or use `ApplicationRunner` if you need the full proxy graph.

---

### Q4. Default scope? Is it per JVM?

**Answer:** Singleton per **container**. Two contexts, two instances.

**Counter:** Parent and child contexts?

**Counter-answer:** Child can override beans; parent beans are visible to child lookups. A singleton in the parent is still one instance, shared. A child-defined bean with the same name shadows. Classic web: root context (services) + servlet context (controllers).

---

### Q5. Prototype vs singleton — who destroys what?

**Answer:** Singleton: container calls `@PreDestroy` on close. Prototype: container only creates and initializes; **no destroy**. Caller owns cleanup.

**Counter:** What if a singleton holds a prototype that opened a socket in `@PostConstruct`?

**Counter-answer:** That prototype is never destroyed. The singleton should not store it, or the prototype should not own resources without a closer you call explicitly. This is why prototypes with resources are awkward — prefer a singleton pool.

---

### Q6. You inject a prototype into a singleton. How many instances?

**Answer:** One, unless you look up again (`ObjectProvider`, `@Lookup`, scoped proxy per call). Injection happens once while creating the singleton.

**Counter:** Is a scoped proxy on the prototype a good default?

**Counter-answer:** It makes *every method call* a new instance, which is surprising (`job.a(); job.b();` = two objects). `ObjectProvider.getObject()` is explicit: one instance per business operation.

---

### Q7. How does `@Lookup` work?

**Answer:** Spring CGLIB-subclasses the bean and implements the annotated method as `context.getBean(ReturnType.class)` (or by name). Each call can return a new prototype.

**Counter:** Why might `@Lookup` fail in a test?

**Counter-answer:** You `new` the class — no subclass, method is abstract or empty. Or the class is `final`. Or you used a concrete method without Spring’s override. `@Lookup` tests belong in a Spring test, or you extract a `JobFactory` interface you can fake.

---

### Q8. Why do request-scoped beans need `proxyMode` when injected into services?

**Answer:** The service is a singleton created at startup. There is no current request object to inject. A proxy defers `getBean` to each invocation against the request `Scope`.

**Counter:** Why do you get `Scope 'request' is not active` from a message listener?

**Counter-answer:** That thread has no bound request attributes. Request scope is for HTTP. Pass tenant/user as method args or use a thread-bound custom scope you control.

---

### Q9. Session vs request vs application scope?

**Answer:** Request = one instance per HTTP request. Session = per HTTP session (all requests of that user). Application = per `ServletContext` (all users). Application ≈ singleton for most apps; use singleton unless you have two DispatcherServlets and care about ServletContext identity.

**Counter:** Is a session-scoped shopping cart a good 2020s design?

**Counter-answer:** Often no — sticky sessions, replication, and APIs are stateless. Prefer cart in DB/Redis keyed by user id. Session beans still appear in legacy MVC. Know them for interviews; do not default to them.

---

### Q10. Are singleton beans thread-safe?

**Answer:** The container does not make them so. Stateless singletons are fine. Mutable instance fields need synchronization, concurrency utilities, or a redesign.

**Counter:** Does prototype fix thread safety?

**Counter-answer:** Only if each thread truly gets its own instance. A prototype stored on a singleton is still shared. Prototype also costs allocation. Prefer stateless singletons.

---

### Q11. `@Bean(destroyMethod = "close")` vs inferred close?

**Answer:** For `@Bean` methods returning `Closeable`/`AutoCloseable`, Spring infers `close` as destroy method. Override with `destroyMethod = ""` to disable (for example a shared `DataSource` you do not own).

**Counter:** Will inference destroy a prototype returned from `@Bean`?

**Counter-answer:** Destroy callbacks are tracked for singleton `@Bean` products the container owns. Prototypes still fall under “we do not track them.” Do not rely on destroy for prototype `@Bean` methods.

---

### Q12. What is a custom scope useful for?

**Answer:** Lifetimes Spring does not ship: per-tenant on a thread, per-JMS-message, per-batch-step. Implement `Scope`, register on the factory, use `@Scope("tenant")` plus a proxy.

**Counter:** Why not just use `ThreadLocal`?

**Counter-answer:** `ThreadLocal` is easy to leak (thread pools), has no destroy callbacks, and is not injectable as a type-safe bean. A Scope can register destruction and play with `@PreDestroy`. Still: both are easy to get wrong; prefer passing context as parameters when possible.

---

### Q13. `SmartLifecycle` vs `@PostConstruct` vs `ApplicationRunner`?

**Answer:** `@PostConstruct` = this bean’s local init. `SmartLifecycle` = coordinated start/stop with phases (connectors). `ApplicationRunner` (Boot) = run after context is fully up. Use the latest hook that matches the need.

**Counter:** Can `@PostConstruct` start a thread that uses other beans?

**Counter-answer:** Risky — other singletons may not be finished. Prefer `SmartLifecycle.start()` or `ApplicationRunner`. If you start a thread in `@PostConstruct`, you may observe half-initialized graphs.

---

### Q14. Parent/child context and scopes?

**Answer:** Each context has its own singleton cache. A child looking up a bean defined in the parent gets the parent singleton. Request scope is typically on the **web child** context. Mixing parent singleton + child request proxy is the usual MVC setup.

**Counter:** If I define the same `@Service` in parent and child?

**Counter-answer:** Child lookup hits the child first. You get two instances if both define it. Accidental duplicate scans (component scan in both) are a real bug.

---

### Q15. Why doesn’t Spring destroy prototypes — isn’t that a leak?

**Answer:** The container would have to retain every prototype forever to destroy them later — which defeats prototype (and leaks memory). The contract is: short-lived objects, GC after you drop the reference. If they need deterministic destroy, they should not be prototypes, or you destroy them yourself.

**Counter:** `DisposableBean` on a prototype?

**Counter-answer:** Still not called by Spring. Implementing the interface does not change tracking.

---

### Q16. `ScopedProxyMode.INTERFACES` vs `TARGET_CLASS`?

**Answer:** INTERFACES = JDK proxy, must implement an interface, inject by interface type. TARGET_CLASS = CGLIB subclass, can inject concrete class, cannot advise `final` methods.

**Counter:** You inject the concrete class but used INTERFACES. What happens?

**Counter-answer:** Type mismatch / proxy does not extend the class. Injection fails or you get a ClassCastException. Match proxy mode to the injection type.

---

### Q17. Can you change scope of a third-party class?

**Answer:** Yes — `@Bean` + `@Scope` on the factory method, or a `BeanFactoryPostProcessor` that edits the `BeanDefinition`’s scope. Do not expect `@Scope` on a class you do not own unless you subclass.

**Counter:** Changing a library `RestTemplate` `@Bean` to prototype — good idea?

**Counter-answer:** Usually no. `RestTemplate` is thread-safe as a singleton (with a pooled request factory). Prototype multiplies connections. Scope follows **state and lifecycle**, not “feels cheaper.”

---

### Q18. What happens if `@PreDestroy` throws?

**Answer:** Spring logs and continues destroying other beans. One failing destroy should not skip the rest. Do not throw; catch and log inside destroy methods.

**Counter:** Order of destruction vs creation?

**Counter-answer:** Roughly reverse of dependency order, not a guarantee you can call collaborators. Close only resources this bean opened.

---

### Q19. How do you test lifecycle without Boot?

**Answer:** `AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(Config.class);` then `ctx.close();` and assert init/destroy flags. For request scope, `MockHttpServletRequest` + `RequestContextHolder` or Spring MVC tests.

**Counter:** Why did destroy not run in my test?

**Counter-answer:** You never `close()`d the context. JUnit may keep the cached context alive for the whole suite ([056_4](056_4_Spring_Testing.md)). Prototype, or bean created with `new`.

---

### Q20. `lazy-init` vs prototype vs `@Lazy` injection?

**Answer:** Lazy singleton: still one instance, created on first use. Prototype: new instance every request. `@Lazy` on an injection point: inject a proxy; the real singleton/prototype is fetched on first method call (or `getObject()` depending on setup). Three different knobs.

**Counter:** A lazy singleton injected into an eager singleton without `@Lazy` on the parameter?

**Counter-answer:** The eager bean’s creation **forces** the lazy one at startup. Lazy is skipped only if nothing eager needs it, or the injection point itself is `@Lazy`.

---

### Interview one-liner

> Construct → inject → Aware → `@PostConstruct` → AOP proxy → use → `@PreDestroy` (singletons). Default scope is singleton and not thread-safe by magic. A prototype injected into a singleton is one object — use `ObjectProvider` or `@Lookup`. Request/session beans need a scoped proxy inside singletons. Spring will not destroy prototypes.
