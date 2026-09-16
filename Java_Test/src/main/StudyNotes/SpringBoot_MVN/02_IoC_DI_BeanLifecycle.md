# 02. IoC, Dependency Injection, and Bean Lifecycle

## Start here (simple English)

**In one sentence:** You stop writing `new Service()` everywhere. You tell Spring **what** you need. Spring **creates** the objects and **hands them** to each other.

**Everyday picture:** A restaurant kitchen.

- You (the chef) should not also **hire staff, buy ovens, and plug in the fridge**.
- The **manager** (Spring container) hires people (creates objects), assigns stations (injects dependencies), and fires them at closing (destroy).

**Without Spring:**

```java
OrderService svc = new OrderService(new OrderRepository(new DataSourceImpl()));
```

You built the whole chain yourself. If `OrderRepository` needs something new next month, you touch every `new`.

**With Spring:** you label classes (`@Service`, `@Repository`). Spring builds the chain.

**Three words people mix up:**

| Word | Simple meaning |
|------|----------------|
| **IoC** (Inversion of Control) | **Who is the boss?** Spring, not your `new` |
| **DI** (Dependency Injection) | **How do objects get helpers?** Spring **pushes** them in |
| **Bean** | An object Spring created and looks after |

Not every Java object is a bean. `new HashMap<>()` inside a method is just a normal object.

The rest of this chapter is the same idea with **lifecycle, scopes, and proxies** — that is what interviews drill. Q&A at the end is **5–8 year standard**.

---

This is the core of Spring. Boot does not change these rules. Auto-config only **registers** beans; the container still creates, injects, and destroys them the same way.

If `@Transactional` “doesn’t work”, you are usually looking at a **proxy / lifecycle** bug from this chapter, not at JPA.

---

## 1. IoC vs DI (use the right words)

**Inversion of Control** is the *principle*: your code does not `new` the graph. The container owns creation, wiring, and lifecycle.

**Dependency Injection** is the *mechanism*: the container **pushes** collaborators in (constructor, setter, field).

```text
IoC  = who is in charge of objects?  → the container
DI   = how do objects get collaborators? → they are injected
AOP  = another way to invert control (for cross-cutting code)
```

Without Spring:

```java
OrderService svc = new OrderService(new OrderRepository(new DataSourceImpl()));
```

With Spring: you describe **types** and **qualifiers**. The container builds the graph.

---

## 2. What a bean is

A **Spring bean** is an object whose lifecycle is managed by the container: instantiated, possibly proxied, injected into others, destroyed on shutdown.

Not every Java object is a bean. `new HashMap<>()` inside a method is a plain object.

**BeanDefinition** is the recipe: class name, scope, lazy flag, constructor args, factory method. The container instantiates from definitions, not by “scanning annotations” at call time. Scanning only **creates definitions**.

Ways to get a definition:

| Mechanism | Example |
|-----------|---------|
| Component scan | `@Component` / `@Service` / `@Repository` / `@Controller` |
| `@Bean` method | `@Configuration` class |
| Auto-config | Boot `@AutoConfiguration` classes |
| XML | legacy `<bean>` |
| Programmatic | `GenericApplicationContext.registerBean` |

---

## 3. The container: BeanFactory vs ApplicationContext

`BeanFactory` is the **minimal** IoC contract: `getBean`, singleton cache, FactoryBean.

`ApplicationContext` **is a** `BeanFactory` plus:

- Eager instantiation of singletons by default
- Internationalization
- Event publication (`ApplicationEventPublisher`)
- Resource loading
- AOP / `BeanPostProcessor` support expected in enterprise apps
- Environment / profiles

Boot always uses an `ApplicationContext` (`AnnotationConfigServletWebServerApplicationContext` for web).

| | BeanFactory | ApplicationContext |
|--|-------------|--------------------|
| Default singleton creation | Lazy (`getBean`) | Eager at `refresh` |
| Events, i18n, AOP-ready | No / limited | Yes |
| Used in Boot | Internally (the factory inside the context) | Yes |

`XmlBeanFactory` is gone. Do not mention it as current API.

**Lazy vs eager:** `@Lazy` on a bean or injection point delays creation. Boot still **defines** the bean at startup; it instantiates later. Failures in that bean move from startup to first use — worse for production. Use lazy for **heavy optional** collaborators, not as a default.

---

## 4. Dependency injection styles

### Constructor injection (default for 5–8 years)

```java
@Service
public class OrderService {
    private final OrderRepository orders;
    private final PaymentClient payments;

    public OrderService(OrderRepository orders, PaymentClient payments) {
        this.orders = orders;
        this.payments = payments;
    }
}
```

- Fields can be `final` — immutable after construction
- Easy to unit-test: `new OrderService(mock, mock)` with **no Spring**
- Missing dependency → **startup failure** (fail fast)
- Circular constructor injection → **fail** (good; you see the design smell)

Since Spring 4.3, a **single constructor** does not need `@Autowired`.

Lombok: `@RequiredArgsConstructor` on a class with `final` fields is the usual style. Know what it generates.

### Setter / method injection

Use for **optional** dependencies or for breaking a cycle you cannot redesign yet.

```java
@Autowired
public void setAuditClient(@Autowired(required = false) AuditClient audit) {
    this.audit = audit;
}
```

### Field injection

```java
@Autowired
private OrderRepository orders;
```

Works. Avoid in production services:

- Cannot be `final`
- Hidden dependencies
- Tests need Spring or reflection
- Harder to see the graph

Acceptable: `@SpringBootTest` test classes, `@Configuration` that is not unit-tested.

### Lookup injection

When a **singleton** needs a **new prototype** every call:

```java
@Lookup
protected PaymentSession paymentSession() {
    return null; // Spring overrides this method on a CGLIB subclass
}
```

Alternatives: `ObjectFactory<T>`, `ObjectProvider<T>`, `Provider<T>` (JSR-330).

```java
private final ObjectProvider<PrototypeBean> provider;

public void handle() {
    PrototypeBean b = provider.getObject(); // new instance
}
```

---

## 5. `@Autowired` vs `@Resource` vs `@Inject`

| | `@Autowired` (Spring) | `@Resource` (Jakarta) | `@Inject` (Jakarta) |
|--|----------------------|------------------------|---------------------|
| Default match | **Type**, then `@Qualifier` / name | **Name** (field name), then type | Type, then `@Named` |
| `required` | `required = false` | N/A (optional via other means) | N/A |
| `@Primary` honored | Yes | Not the same way | Similar to Autowired |
| Use in Boot | Standard | Fine for name-based | Fine if you want JSR-330 |

**Ambiguity:** two beans of type `Vehicle`. `@Autowired Vehicle` fails. Fix with:

1. `@Primary` on the usual one
2. `@Qualifier("bike")` on the injection point **and** on the bean (`@Bean("bike")` or `@Component("bike")`)
3. Inject `List<Vehicle>` / `Map<String, Vehicle>` / `ObjectProvider<Vehicle>`

`@Qualifier` is stereotype-friendly — you can write `@MainGateway` as a custom qualifier annotation.

`@Order` / `Ordered` / `@Priority` sort **lists** of beans, they do **not** pick a single winner for a scalar injection (except in some special cases). Scalar winner = `@Primary` or `@Qualifier`.

`ObjectProvider<T>` is the senior tool: `getIfAvailable()`, `getIfUnique()`, stream, lazy.

---

## 6. Full bean lifecycle (singleton)

**Simple version first:** Spring **builds** the object, **fills in** its helpers, **runs startup methods** (`@PostConstruct`), then **may wrap it in a proxy**, then it is ready. On shutdown it runs cleanup (`@PreDestroy`).

Memorize this sequence. Interviews walk it.

```text
1. Load BeanDefinition
2. Instantiate (constructor / factory method)
3. Populate properties (injection of other beans)
4. Aware callbacks (if implemented)
     BeanNameAware → BeanClassLoaderAware → BeanFactoryAware
     ApplicationContextAware, EnvironmentAware, EmbeddedValueResolverAware, …
5. BeanPostProcessor.postProcessBeforeInitialization
     (includes @PostConstruct via CommonAnnotationBeanPostProcessor)
6. InitializingBean.afterPropertiesSet
7. Custom init method (initMethod = "start")
8. BeanPostProcessor.postProcessAfterInitialization
     **AOP proxies are usually created HERE**
     (AbstractAutoProxyCreator)
9. Bean is in the singleton cache and ready
   …
10. Destruction (context close, reverse order of creation)
     @PreDestroy
     DisposableBean.destroy
     custom destroyMethod
```

**Implications:**

- `@PostConstruct` runs on the **raw target**, **before** the AOP proxy is applied. Do not call transactional methods on `this` here expecting a proxy — there is no proxy yet, and even later `this` is not the proxy.
- Other beans may not be fully started in `@PostConstruct`. Prefer `ApplicationReadyEvent` for “call other services”.
- Prototype beans: Spring **does not** call destroy callbacks unless you use a `BeanMemoryLeak`-aware holder. You own prototype destruction.

`InitializingBean` / `DisposableBean` couple you to Spring. Prefer `@PostConstruct` / `@PreDestroy` or `initMethod`/`destroyMethod` on `@Bean`.

---

## 7. BeanPostProcessor vs BeanFactoryPostProcessor

| | BeanFactoryPostProcessor | BeanPostProcessor |
|--|--------------------------|-------------------|
| When | After definitions loaded, **before** beans instantiated | Around **each** bean init |
| Typical | `PropertySourcesPlaceholderConfigurer`, `ConfigurationClassPostProcessor` | AOP auto-proxy, `@Autowired` annotation processor |
| Can change | Bean **definitions** (what will be created) | Bean **instances** (wrap in proxy) |

`@Configuration` classes are processed by `ConfigurationClassPostProcessor` (a BFPP). That is why `@Bean` methods become definitions.

**Never** get a random bean with `getBean` inside a BFPP — the factory is not ready; you will create beans too early and skip post-processors (classic “Bean is not eligible for getting processed by all BeanPostProcessors” warning).

---

## 8. Scopes

| Scope | Instances | Destroyed by Spring? |
|-------|-----------|----------------------|
| `singleton` (default) | One per **container** | Yes |
| `prototype` | Every `getBean` / injection (see pitfall) | **No** |
| `request` | One per HTTP request | Yes (request end) |
| `session` | One per HTTP session | Yes (session end) |
| `application` | One per `ServletContext` | Yes |
| `websocket` | One per WS session | Yes |

**Singleton ≠ Gang of Four singleton.** One per `ApplicationContext`, not per JVM. Two contexts (rare) → two instances. Tests that load multiple contexts surprise people.

### Prototype inside singleton

```java
@Service
public class OrderService {
    @Autowired
    private PrototypeBean proto; // injected ONCE — same instance forever
}
```

Fix: `@Lookup`, `ObjectProvider`, or inject `ApplicationContext` (ugly).

For request/session beans injected into a singleton, set:

```java
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
```

The singleton receives a **proxy**. Each call on the proxy delegates to the current request’s instance. Without `proxyMode`, you would capture one request bean at startup and reuse it (wrong, or fail at startup).

### Thread safety

Singleton services **must be stateless** (or use thread-safe state: `ConcurrentHashMap`, no mutable instance fields for request data). Request data belongs on the stack, in method args, or in a request-scoped bean.

---

## 9. Circular dependencies

```text
A constructor-injects B
B constructor-injects A
→ BeanCurrentlyInCreationException
```

Setter / field injection **can** be satisfied via the singleton factory: Spring puts an **early reference** (sometimes a raw object, sometimes a proxy) in the cache so the other bean can inject it. This hides a design problem.

**Boot 2.6+:** circular references are **disallowed by default**. You can re-enable:

```yaml
spring:
  main:
    allow-circular-references: true
```

Do not. Break the cycle:

- Extract `C` that both use
- Use events
- `@Lazy` on one constructor parameter (injects a proxy; still a smell)

**Counter-interview fact:** `@Lazy` on a constructor arg creates a **proxy**. First *use* of the proxy triggers creation of the real bean. That can move a cycle to runtime.

---

## 10. JDK interface proxy vs CGLIB class proxy

Spring AOP (transactions, security, async, cache, custom `@Aspect`) wraps beans in **proxies**.

| | JDK dynamic proxy | CGLIB (or ByteBuddy in some setups) |
|--|-------------------|-------------------------------------|
| Requires | Interface | Concrete class (non-`final`) |
| Proxy type | Implements the interface(s) | Subclass of the target class |
| Casting to impl class | **Fails** | Works |
| `final` methods | n/a (call via interface) | **Cannot be advised** |
| Default in Boot for `@Transactional` if class has interfaces | Historically JDK; Boot often uses class proxies depending on settings | `spring.aop.proxy-target-class=true` is Boot default |

**Self-invocation:**

```java
@Service
public class OrderService {
    @Transactional
    public void place() { /* ... */ }

    public void facade() {
        this.place(); // NO PROXY → no transaction
    }
}
```

Callers must go through the Spring bean (the proxy). `this` is the raw target. Full treatment in chapter 08 and 10.

`FactoryBean<T>`: the bean named `foo` is the **product** of `FooFactoryBean.getObject()`. The factory itself is `&foo`. Data JPA repositories are factory-produced proxies.

---

## 11. `@Configuration` vs `@Component` for `@Bean` methods

`@Configuration` is **CGLIB-enhanced** (full mode): calls between `@Bean` methods on the same class go through the container (singleton guaranteed).

```java
@Configuration
public class HttpConfig {
    @Bean
    RestTemplate restTemplate(RestTemplateBuilder b) { return b.build(); }
}
```

`@Configuration(proxyBeanMethods = false)` (**lite**, Boot auto-config uses this): `@Bean` methods are plain methods. Faster startup. Do not call one `@Bean` method from another expecting a singleton — inject the other bean as a parameter instead.

`@Component` + `@Bean` method = lite mode. Prefer `@Configuration`.

---

## 12. Stereotype annotations (container meaning)

| Annotation | Extra behavior beyond `@Component` |
|------------|------------------------------------|
| `@Component` | Generic scan target |
| `@Service` | Semantic only (so far) |
| `@Repository` | Persistence exception translation (`DataAccessException`) |
| `@Controller` / `@RestController` | MVC handler detection |
| `@Configuration` | `@Bean` factory, possible CGLIB subclass |
| `@ControllerAdvice` | MVC advice bean |

Use the stereotype that matches the layer. AOP pointcuts and exception translation depend on it.

---

## 13. Production pitfalls

1. Mutable fields on a singleton → race conditions under Tomcat’s thread pool.
2. Prototype injected once into a singleton → accidental singleton.
3. `@PostConstruct` calling `@Transactional` methods on `this`.
4. Two beans of same type, no `@Primary` → startup failure in one profile, “works on my machine” in another (only one bean present).
5. `ApplicationContext.getBean` in business code — service locator smell; inject instead.
6. Storing `HttpServletRequest` in a singleton field.
7. Implementing `BeanPostProcessor` in a `@Component` that also has `@Autowired` collaborators — ordering hell. Keep BPPs isolated.

---

# Interview Q&A (5–8 year bar)

A fresher can say “Spring creates objects.” A 5–8 year answer walks lifecycle, circular constructor injection, and prototype-in-singleton.

### Q1. What is IoC? What is DI?

**Answer:** IoC = container owns object creation and lifecycle. DI = container supplies dependencies (constructor/setter/field). DI is how Spring implements IoC for collaborators.

**Counter:** Is the Factory pattern IoC?  
**Answer:** Related. A factory inverts *creation*, but you still call the factory. The Spring container inverts that too — you rarely call `getBean` in app code.

---

### Q2. What is a Spring bean?

**Answer:** An object instantiated, configured, and managed by the Spring container, described by a `BeanDefinition`.

**Counter:** Is every `@Entity` a bean?  
**Answer:** No. Entities are persistence instances, created by Hibernate, not by the singleton cache.

---

### Q3. BeanFactory vs ApplicationContext?

**Answer:** `ApplicationContext` extends `BeanFactory` and adds eager singleton startup, events, i18n, resources. Boot uses `ApplicationContext`.

**Counter:** Are Boot beans lazy?  
**Answer:** Singletons are eager unless `@Lazy`. The inner factory can still create lazily when asked.

---

### Q4. Why constructor injection?

**Answer:** Mandatory deps, immutability, easy unit tests, cycles fail at startup.

**Counter:** When setter?  
**Answer:** Optional deps, or breaking a cycle temporarily.

**Counter:** Why not field injection?  
**Answer:** Hidden deps, no `final`, testing requires Spring/reflection.

**Trap:** “Field injection is faster.” Irrelevant. Wiring cost is startup, not per-request.

---

### Q5. Two beans of the same type — what happens?

**Answer:** Autowiring by type fails with `NoUniqueBeanDefinitionException`. Use `@Primary`, `@Qualifier`, or `ObjectProvider`.

**Counter:** `@Primary` vs `@Qualifier`?  
**Answer:** `@Primary` = default when unqualified. `@Qualifier` = explicit name/stereotype at the injection point. Qualifier wins when both exist.

---

### Q6. Default bean scope? Singleton vs GoF singleton?

**Answer:** Default = Spring singleton = one instance **per container**. GoF = one per ClassLoader/JVM.

**Counter:** Prototype vs request?  
**Answer:** Prototype = new instance per retrieval, not HTTP-aware. Request = one per HTTP request, needs web context.

---

### Q7. Walk the bean lifecycle.

**Answer:** Instantiate → inject → `*Aware` → `@PostConstruct` / `afterPropertiesSet` → init-method → `postProcessAfterInitialization` (AOP proxy) → in use → `@PreDestroy` / `destroy`.

**Counter:** Where is the proxy created?  
**Answer:** After init, in `BeanPostProcessor.postProcessAfterInitialization`.

**Counter:** Does `@PostConstruct` run inside the transaction proxy?  
**Answer:** No. It runs on the raw instance before (or without using) the proxy.

---

### Q8. Circular dependency — constructor vs setter?

**Answer:** Constructor cycle fails. Setter/field may succeed via early references. Boot 2.6+ forbids it by default.

**Counter:** How do you fix it properly?  
**Answer:** Redesign (extract third type, events). `@Lazy` is a workaround.

---

### Q9. Prototype inside singleton?

**Answer:** Injected once → behaves as singleton. Use `ObjectProvider`, `@Lookup`, or scoped proxy.

---

### Q10. JDK proxy vs CGLIB?

**Answer:** JDK = interface proxy. CGLIB = subclass. `final` class/method cannot be CGLIB-advised. Boot defaults to class-based proxies for AOP (`proxy-target-class=true`).

**Counter:** Why did injecting the impl type fail?  
**Answer:** You got a JDK proxy that only implements the interface. Inject the interface, or force class proxy.

---

### Q11. Why is `@Transactional` ignored?

**Answer (this chapter):** Call did not go through the Spring proxy (`this.method()`, `private` method, `final` method, same-class call), or the bean was created with `new`, not by Spring.

**Counter:** Does it work on a `@Controller` method?  
**Answer:** It can, but the TX boundary belongs on the service. Controllers may be proxied; still a layering smell.

---

### Q12. `@Configuration` `proxyBeanMethods`?

**Answer:** `true` (default) = inter-`@Bean` calls are intercepted, singleton guaranteed. `false` = lite, faster, inject other beans as parameters.

---

### Q13. `@Autowired(required = false)` vs `Optional<T>` vs `ObjectProvider<T>`?

**Answer:** `required = false` may leave a field `null`. `Optional` is clearer. `ObjectProvider` can lazily fetch, list, or get-if-unique. Prefer `ObjectProvider` for optional + multi.

---

### Q14. What is a `FactoryBean`?

**Answer:** A bean that **produces** another object. The container exposes the product under the bean name. Spring Data JPA repositories are created this way.

**Counter:** How do you inject the factory itself?  
**Answer:** `&beanName`.

---

### Q15. Is a singleton thread-safe?

**Answer:** Not automatically. Shared mutable state must be synchronized or avoided. Stateless services are the model.

**Counter:** Is request scope thread-safe?  
**Answer:** One instance per request; if that request is concurrent (async dispatch to the same request bean), you can still race. Normally one thread per request in servlet MVC.
