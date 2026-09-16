# 54.1 IoC Container Internals

[← 054 Fundamentals](054_Spring_Framework_Fundamentals.md) | [Course map](00_COURSE_MAP.md) | **Next:** [054_2 Lifecycle and Scopes →](054_2_Bean_Lifecycle_and_Scopes.md)

Teaching is simple first. **Interview Q&A at the end is 5–8 year standard.**

---

## Simple first (restaurant)

Think of Spring starting your app like a restaurant opening for the day.

1. **Write recipes** (not food yet). A recipe says: “`OrderService`, one copy, needs a `PaymentGateway`.” That recipe is a **BeanDefinition**.
2. **Change recipes if needed** (pencil on paper). Example: replace `${db.url}` with a real URL. These helpers are **BeanFactoryPostProcessors** — they touch *recipes*, not cooked food.
3. **Cook the food** (`new OrderService(...)`). After the dish exists, wrap it (put it on a plate, add garnish). These helpers are **BeanPostProcessors** — they touch *objects*.
4. **Keep finished dishes warm** (singleton cache). The next waiter who asks for `OrderService` gets the same dish.

```text
BeanDefinition          = recipe on paper
BeanFactory             = kitchen that follows recipes
refresh()               = “open the restaurant”
Singleton cache         = finished dishes on the pass
Early singleton         = a dish still in the oven, shown to another cook
                             so they can finish their dish
```

**Objects are created late. Recipes are written early.** That one sentence explains most internals.

**Circular dependency in plain English:** A needs B, B needs A.

- If both need each other in the **constructor**, nobody can be born first → **fail**.
- If they need each other in a **setter** (after they exist), Spring can show B a “half-ready A” so B can finish, then finish A. That trick uses **three maps** (people say “3-level cache”).

You do **not** need to memorize Java class names to understand this picture. Interviews will ask for the names — those are in the sections below and in the Q&A.

---

## When you interview (5–8 years)

This chapter separates “I use Spring” from “I can debug a failed context.”

Typical prompts: walk through `refresh()`; circular dependency; BFPP vs BPP; `FactoryBean` vs `@Bean`; why constructor cycles cannot use the 3-level cache.

If you only memorized “Spring uses a 3-level cache,” you will fail the counter-question. Draw the caches on a whiteboard.

---

## 1. BeanDefinition — the recipe

A **bean** at runtime is an object. A **`BeanDefinition`** is the metadata Spring stores *before* that object exists.

Typical fields (simplified):

| Field | Meaning |
|-------|---------|
| `beanClass` | Which class to instantiate |
| `scope` | singleton, prototype, … |
| `lazyInit` | skip eager singleton creation |
| `dependsOn` | other beans that must be initialized first |
| `autowireMode` | constructor / byType / byName (legacy) |
| `primary` | `@Primary` |
| `initMethodName` / `destroyMethodName` | callbacks |
| constructor argument values | for XML / programmatic definitions |
| property values | setter injection metadata |

Sources of definitions:

```text
@ComponentScan     → AnnotatedBeanDefinitionReader / ClassPathBeanDefinitionScanner
@Bean methods      → ConfigurationClassPostProcessor (a BeanFactoryPostProcessor)
@Import            → same processor
XML                → XmlBeanDefinitionReader
programmatic       → GenericApplicationContext.registerBean / BeanDefinitionRegistry
```

You can have a definition **without** an instance yet (lazy, or prototype, or factory not called).

Default bean name for `@Component` classes: **decapitalized short class name**. `OrderService` → `orderService`. Override with `@Component("orders")` or `@Bean("orders")`.

---

## 2. DefaultListableBeanFactory

This is the real engine. `ApplicationContext` **has** a `BeanFactory` (usually `DefaultListableBeanFactory`).

Responsibilities:

- Registry of `BeanDefinition`s (`BeanDefinitionRegistry`)
- Singleton cache
- Type indexes for `getBean(Class)`
- Autowire candidate resolution
- Creating beans (`AbstractAutowireCapableBeanFactory.createBean`)

`GenericApplicationContext`, `AnnotationConfigApplicationContext`, and Boot’s web contexts all wrap this factory, then add Environment, events, and servlet support.

```java
// You rarely do this by hand — this is the idea
DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
RootBeanDefinition def = new RootBeanDefinition(OrderService.class);
factory.registerBeanDefinition("orderService", def);
factory.preInstantiateSingletons();
OrderService s = factory.getBean(OrderService.class);
```

---

## 3. `ApplicationContext.refresh()` — the 12-step day

**Simple:** `refresh()` is the “open the restaurant” method. Spring runs it once at startup.

You do not need to recite all 12 names on day one. Remember **three buckets**:

```text
A. Load recipes     (scan @Component, read @Bean methods)
B. Edit recipes     (placeholders, extra @Bean from @Configuration)
C. Cook singletons  (new the objects, inject, wrap)
```

The names below match the code closely enough for interviews. Read them as a checklist, not as poetry.

```text
prepareRefresh()
  └── validate, early listeners, Environment ready

obtainFreshBeanFactory()
  └── create/refresh internal DefaultListableBeanFactory
  └── load BeanDefinitions (scan, @Bean, XML) into the factory
        (some contexts load definitions here; annotation configs
         also finish via a BeanFactoryPostProcessor — see below)

prepareBeanFactory()
  └── register ApplicationContext, Environment, ClassLoader as resolvable deps
  └── ignore Aware interfaces for autowire
  └── add ApplicationContextAwareProcessor

postProcessBeanFactory()
  └── subclass hook (web contexts add servlet scopes)

invokeBeanFactoryPostProcessors()     ★ recipes can still change
  └── BeanDefinitionRegistryPostProcessor first (e.g. ConfigurationClassPostProcessor)
  └── then remaining BeanFactoryPostProcessor (PropertySourcesPlaceholderConfigurer)

registerBeanPostProcessors()
  └── register BeanPostProcessor beans (they are created now, before business singletons)

initMessageSource()
initApplicationEventMulticaster()
onRefresh()                           ★ web: create embedded server in Boot; Framework: hook
registerListeners()

finishBeanFactoryInitialization()     ★ cook the food
  └── freeze configuration
  └── preInstantiateSingletons() — create all non-lazy singletons

finishRefresh()
  └── LifecycleProcessor start (SmartLifecycle)
  └── publish ContextRefreshedEvent
```

Two facts to keep:

1. **`ConfigurationClassPostProcessor`** is a `BeanDefinitionRegistryPostProcessor`. It parses `@Configuration`, `@ComponentScan`, `@Bean`, `@Import` and **registers more definitions**. That is why `@Bean` methods are not “just Java.” They become definitions during this phase.
2. **Business singletons are created only in `preInstantiateSingletons`**, after all factory post-processors and after BeanPostProcessors are registered. Order is not accidental.

If a `@Bean` method runs *during* `BeanFactoryPostProcessor` execution (because you called `getBean` too early), you can hit “BeanPostProcessor not yet registered” warnings. Do not `getBean()` from a `BeanFactoryPostProcessor`.

---

## 4. BeanFactoryPostProcessor vs BeanPostProcessor

This pair is asked constantly. They run at **different times** on **different things**.

**Simple:**

| Question | BFPP | BPP |
|----------|------|-----|
| Does the **object** exist yet? | No — only the recipe | Yes — object is already `new`'d |
| What can you change? | Class, scope, property placeholders | Wrap with a proxy, run `@PostConstruct` helpers |
| Kitchen analogy | Change the recipe card | Garnish the finished dish |

| | `BeanFactoryPostProcessor` (BFPP) | `BeanPostProcessor` (BPP) |
|--|-----------------------------------|---------------------------|
| Operates on | **BeanDefinitions** (and the factory) | **Bean instances** |
| When | After definitions loaded, **before** most beans are created | During each bean’s create pipeline |
| Can change class/scope/property placeholders | Yes | Too late for that |
| Typical examples | `PropertySourcesPlaceholderConfigurer`, `ConfigurationClassPostProcessor`, `CustomEditorConfigurer` | `AutowiredAnnotationBeanPostProcessor`, `CommonAnnotationBeanPostProcessor` (`@PostConstruct`), `AnnotationAwareAspectJAutoProxyCreator` |
| `getBean()` inside | Dangerous — triggers early creation | Normal (the bean is already being created) |

```java
public class PrefixBeanNameProcessor implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory f) {
        for (String name : f.getBeanDefinitionNames()) {
            // still metadata — no OrderService instance yet
        }
    }
}
```

```java
public class TimingProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String name) {
        return bean; // instance already constructed and injected
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String name) {
        return bean; // last chance to wrap with a proxy
    }
}
```

**AOP proxies are created in `postProcessAfterInitialization`.** That is why `@PostConstruct` runs on the **raw** bean, then the proxy is published. See [054_2](054_2_Bean_Lifecycle_and_Scopes.md).

`BeanDefinitionRegistryPostProcessor` extends BFPP and runs **even earlier**, because it can add new definitions (component scan). `ConfigurationClassPostProcessor` is the one you should name.

---

## 5. Instantiation pipeline (`createBean`)

For one bean, simplified:

```text
getBean("orderService")
  → getSingleton (cache hit? return)
  → createBean
       1. Resolve class, apply InstantiationAwareBeanPostProcessor (can replace instance)
       2. createBeanInstance
            constructor resolution + constructor injection
            or FactoryBean.getObject()
            or instance supplier
       3. populateBean
            setter / field @Autowired (AutowiredAnnotationBeanPostProcessor)
       4. initializeBean
            invoke Aware callbacks
            BPP.postProcessBeforeInitialization
            @PostConstruct / InitializingBean / init-method
            BPP.postProcessAfterInitialization  ← AOP proxy often born here
       5. put into singleton cache (if singleton)
```

Constructor injection happens in **step 2**, before the object is in the singleton cache as a finished bean. That timing is why constructor circular dependencies cannot be solved by the early-reference trick in the same way.

---

## 6. The three-level singleton cache

Spring keeps three maps. Interviewers say “3-level cache.”

**Simple picture (two friends A and B):**

```text
A is being built (constructor already ran, not fully ready).
B is being built and says “I need A.”
Spring cannot wait forever, so it shows B a half-ready A.
B finishes. Then A finishes. Both are stored as “done.”
```

That “half-ready A” is the **early reference**. Spring uses **three boxes** so that:

- finished beans stay in box 1
- the first time someone needs a half-ready bean, a small factory in box 3 creates **one** early object
- that same early object is reused from box 2 (so B and later code do not get two different As)

If Spring put a half-built A into box 1 too soon, other code might use it before `@PostConstruct` ran. That is why box 1 is only for **finished** beans.

| Level | Field name | Holds |
|-------|------------|--------|
| L1 | `singletonObjects` | **Fully initialized** singletons |
| L2 | `earlySingletonObjects` | Early beans that were already requested (cache of early refs) |
| L3 | `singletonFactories` | `ObjectFactory` that can produce an **early reference** (often a raw instance, later maybe a proxy) |

```text
createBean starts for A
  addSingletonFactory(A)     → L3 now has a factory for A
  instantiate A
  populate A  → needs B
    createBean B
      B needs A
      getSingleton(A) → factory in L3 → getEarlyBeanReference(A)
                    → store in L2, remove from L3
      B is populated with that early A
      B finishes → L1
  A populate finishes
  A initialize (PostConstruct, AOP)
  A goes to L1; L2/L3 entries for A cleared
```

**Why three levels, not two?**

`getEarlyBeanReference` is where `SmartInstantiationAwareBeanPostProcessor` (the AOP auto-proxy creator) can wrap an **early proxy**. If you cached that proxy in a single “early map” too naively, or returned the raw instance after a proxy was already published, you would inject **raw A** into B and **proxy A** into everyone else — two identities, broken equals, transactions that do not run.

L3 stores a **factory**, not the object. The first time someone needs an early ref, the factory runs once, the result goes to L2, the factory is removed. Everyone after that gets the **same** early object.

**Spring 6.1+ / Boot 3:** constructor circular dependency is **not** supported by default in some setups; even historically it was unreliable. Do not design cycles.

---

## 7. Circular dependencies — what works, what fails

```text
Setter/field cycle (singleton ← singleton)
  A has @Autowired void setB(B b)
  B has @Autowired void setA(A a)
  → usually STARTS, thanks to early reference from L3

Constructor cycle
  A(A's ctor needs B)
  B(B's ctor needs A)
  → BeanCurrentlyInCreationException
  Early ref cannot help: A does not exist until B returns, B cannot return until A exists

Prototype cycle
  → fails. Early cache is a singleton feature.

@Lazy on one constructor parameter
  → injects a proxy; real bean created on first method call
  → starts, but hides a design problem
```

```java
@Service
public class OrderService {
    private final InventoryService inventory;
    public OrderService(@Lazy InventoryService inventory) { // smell
        this.inventory = inventory;
    }
}
```

**Senior take:** a cycle usually means the two classes share a third responsibility. Extract that, or use an event, or a dedicated application service. `@Lazy` is for emergencies and legacy.

`spring.main.allow-circular-references=false` (Boot 2.6+) makes even setter cycles fail. Treat that as the healthy default.

---

## 8. FactoryBean vs `@Bean` vs `ObjectFactory` vs `ObjectProvider`

**Simple:**

- `@Bean` method = *you* write `return new X()` in a config class. Most common.
- `FactoryBean` = a special bean whose *job* is to produce another object (libraries like MyBatis use this). Asking for the bean name gives you the **product**, not the factory.
- `ObjectProvider` = “give me the bean when I ask, and don’t crash if it is missing.” Also: “give me a **new** prototype each time.”

| API | What it is |
|-----|------------|
| `@Bean` method | Factory *method* on a config class. Return value becomes the bean. |
| `FactoryBean<T>` | A bean whose job is to **produce** another object. The container publishes `T` by default, not the factory. |
| `ObjectFactory<T>` | Callback: `getObject()` every time — used internally; you can inject it. |
| `ObjectProvider<T>` | Spring 4.3+ `ObjectFactory` with `getIfAvailable`, `getIfUnique`, `stream()`. Preferred for optional / lazy / multi. |

```java
public class SqlSessionFactoryBean implements FactoryBean<SqlSessionFactory> {
    @Override
    public SqlSessionFactory getObject() { /* build MyBatis factory */ return factory; }
    @Override
    public Class<?> getObjectType() { return SqlSessionFactory.class; }
    @Override
    public boolean isSingleton() { return true; }
}
```

To inject the `FactoryBean` itself, ask for `&beanName` (the `&` prefix) or inject `FactoryBean<SqlSessionFactory>`.

**Interview trap:** “`FactoryBean` is the same as `@Bean`.” `@Bean` is how *you* register a product. `FactoryBean` is a **bean type** used by libraries (MyBatis, Spring Integration) when creation is heavy or needs lifecycle.

`ObjectProvider` example:

```java
@Service
public class Notifier {
    private final ObjectProvider<SmsClient> sms; // no exception if missing

    public Notifier(ObjectProvider<SmsClient> sms) {
        this.sms = sms;
    }

    public void ping() {
        sms.ifAvailable(SmsClient::sendHealth);
    }
}
```

---

## 9. Aware interfaces

After population, `initializeBean` calls:

| Interface | Injected |
|-----------|----------|
| `BeanNameAware` | the bean’s id |
| `BeanClassLoaderAware` | class loader |
| `BeanFactoryAware` | the factory |
| `ApplicationContextAware` | the context |
| `EnvironmentAware` | Environment |
| `ResourceLoaderAware` | resource loader |
| `ApplicationEventPublisherAware` | event publisher |
| `EmbeddedValueResolverAware` | placeholder resolver |
| `ServletContextAware` | web only |

These couple your class to Spring. Use them in **infrastructure**, not in `OrderService`. Prefer injecting `Environment`, `ApplicationEventPublisher`, or `ResourceLoader` through the constructor — they are beans/`@Autowired` resolvable types too.

`ApplicationContextAwareProcessor` is the BPP that performs these callbacks.

---

## 10. Instantiation tricks interviewers like

**`@Lookup`** — method injection. Each call to an abstract/overridden method returns a new bean (usually prototype). Spring subclasses your bean (CGLIB) and implements the method with `getBean`.

**InstantiationAwareBeanPostProcessor** — can short-circuit construction (`postProcessBeforeInstantiation` returns a replacement — used by AOP for some cases and by load-time weaver style integrations).

**Supplier / instance supplier** (Spring 5+) — `BeanDefinition.setInstanceSupplier(() -> new Foo())` used heavily by Boot functional registration and Graal-friendly arrangements.

**Resolvable dependencies** — `BeanFactory` is registered as a type you can inject without it being a normal `@Bean`. That is how `ApplicationContext` injection works.

---

## Production pitfalls

1. **Doing work in a `BeanFactoryPostProcessor` that calls `getBean`** — creates beans before BPPs (AOP, `@Autowired` on other beans) are ready.
2. **Constructor circular dependency + “but Spring has a 3-level cache”** — cache does not save constructor cycles.
3. **Two identities of the same singleton** — mixing early raw reference and later proxy. Rare if you stay on stock Spring; possible with custom BPPs that do not implement `getEarlyBeanReference`.
4. **`FactoryBean` vs product confusion** — logging `getBean("sqlSessionFactory").getClass()` and being surprised it is not `SqlSessionFactoryBean`.
5. **Relying on bean creation order** without `@DependsOn` or a real injection edge — `depends-on` is order, injection is graph. Prefer injection.
6. **Assuming bean name is always the field name** — name generation has rules; `@Qualifier` is explicit.

---

## Interview Ready Q&A (5–8 year standard)

Teaching above used a restaurant. **Speak like a senior here:** class names, `refresh()` order, constructor vs setter cycles, `FactoryBean`.

### Q1. What is a BeanDefinition?

**Answer:** Metadata that describes a bean before it exists: class, scope, lazy flag, constructor args, property values, init/destroy methods, primary, depends-on. The factory instantiates from definitions, not by scanning the heap.

**Counter:** Can two definitions point at the same class?

**Counter-answer:** Yes. Two `@Bean` methods returning `RestTemplate`, or `@Component` plus a manual `@Bean` of the same class, become two beans (different names). Injection by type then needs `@Qualifier`/`@Primary`. Same class ≠ one bean.

---

### Q2. Walk through `refresh()`.

**Answer:** Prepare environment → create/load `BeanFactory` + definitions → prepare factory (ignore Aware, register context as dependency) → run **BFPPs** (parse `@Configuration`, placeholders) → register **BPPs** → message source + event multicaster → `onRefresh` → register listeners → **pre-instantiate non-lazy singletons** → finish (`ContextRefreshedEvent`, Lifecycle start).

**Counter:** Where does component scanning actually happen?

**Counter-answer:** During BFPP phase, inside `ConfigurationClassPostProcessor` (a `BeanDefinitionRegistryPostProcessor`). Scan does not wait until `preInstantiateSingletons`. Definitions are registered first; instances come later.

---

### Q3. BeanFactoryPostProcessor vs BeanPostProcessor?

**Answer:** BFPP = mutate definitions / factory before beans exist. BPP = wrap or init each instance. Placeholders and `@Configuration` parsing are BFPP. `@Autowired`, `@PostConstruct`, AOP proxy are BPP.

**Counter:** Is `ConfigurationClassPostProcessor` a BPP?

**Counter-answer:** No. It is a `BeanDefinitionRegistryPostProcessor` (special BFPP). It *creates* definitions for `@Bean` methods. If you call it a BPP, you have the timeline wrong.

---

### Q4. Why must BFPPs run before singleton instantiation?

**Answer:** Because `${}` placeholders, profile-specific definitions, and extra `@Bean` methods must be applied to **recipes**. Once `OrderService` is constructed, changing its class or constructor args is too late.

**Counter:** Can a BPP still replace the object?

**Counter-answer:** Yes — `postProcessAfterInitialization` can return a proxy. That replaces the **instance**, not the definition. Scope, name, and injected collaborators were already decided.

---

### Q5. Explain the three-level cache.

**Answer:** L1 `singletonObjects` = finished beans. L3 `singletonFactories` = factory to create an early reference while the bean is still being built. L2 `earlySingletonObjects` = that early reference after first use, so every dependent sees the same object. Used to break setter/field singleton cycles and to expose one AOP early proxy.

**Counter:** Why not cache the early instance in L1 immediately?

**Counter-answer:** L1 means “fully initialized, safe to use.” `@PostConstruct` and after-init BPPs have not run. Putting a half-built bean in L1 would let other code observe it too soon. L3/L2 are a narrow exception for in-creation cycles.

---

### Q6. How does Spring resolve a circular dependency?

**Answer:** For singleton setter/field: start creating A, register an `ObjectFactory` in L3, create B, B asks for A, factory returns early A (maybe proxied), B finishes, A finishes, both in L1.

**Counter:** Why does constructor injection still fail?

**Counter-answer:** A’s instance does not exist until its constructor returns. The constructor cannot return until B is ready. B’s constructor needs A. There is no object to expose early. Result: `BeanCurrentlyInCreationException`. Fix the design, or `@Lazy` one side (proxy).

---

### Q7. Does `@Lazy` on a constructor parameter use the 3-level cache?

**Answer:** Not the same mechanism. `@Lazy` injects a **proxy** that delays `getBean` until first method call. The other bean can finish constructing without the real collaborator existing yet.

**Counter:** Is `@Lazy` a good fix for cycles?

**Counter-answer:** It is a tactical fix. You now have a hidden graph, first-call latency, and harder reasoning. Prefer splitting a third type or using events. Boot 2.6+ disabling circular references is a hint that cycles are considered a defect.

---

### Q8. What is FactoryBean?

**Answer:** A Spring bean that implements `FactoryBean<T>` and produces `T` via `getObject()`. By default `getBean("name")` returns `T`. The factory itself is `&name`. Used by libraries that need a complex build (MyBatis `SqlSessionFactoryBean`, older Spring/FactoryBean integrations).

**Counter:** When do you choose `@Bean` instead?

**Counter-answer:** Almost always in application code. `@Bean` is simpler, plays with CGLIB `@Configuration` interception, and does not need `&` prefix. Implement `FactoryBean` when you are writing infrastructure that must plug into Spring’s factory lifecycle (`isSingleton`, `getObjectType` for type matching before creation).

---

### Q9. FactoryBean vs ObjectFactory vs ObjectProvider?

**Answer:** `FactoryBean` = a bean that *is* a factory for another bean in the registry. `ObjectFactory` = `getObject()` callback, often creating or fetching on demand. `ObjectProvider` = `ObjectFactory` plus optional/unique/stream APIs — inject this for optional or delayed deps.

**Counter:** Does `ObjectProvider.getObject()` always create a new instance?

**Counter-answer:** No. It goes through `getBean`. If the target is singleton, you get the same instance. If prototype, you get a new one each call. That is how you safely pull a prototype out of a singleton (see [054_2](054_2_Bean_Lifecycle_and_Scopes.md)).

---

### Q10. What are Aware interfaces for? Should services implement them?

**Answer:** Callbacks so the container can inject `beanName`, `BeanFactory`, `ApplicationContext`, `Environment`, etc. Services should **not** implement them; inject the specific collaborator instead. Aware is for framework-style classes.

**Counter:** How does `ApplicationContextAware` get invoked without `@Autowired`?

**Counter-answer:** `ApplicationContextAwareProcessor` (a BPP) checks interfaces and calls `setApplicationContext`. It runs in `initializeBean`, after population, before `@PostConstruct`.

---

### Q11. Why might you see “Bean created that is not eligible for getting processed by all BeanPostProcessors”?

**Answer:** Something requested that bean (`getBean` / injection) **during** BFPP execution, before all BPPs were registered. The bean is created too early and may skip AOP or autowiring post-processors.

**Counter:** Typical cause in a project?

**Counter-answer:** A `BeanFactoryPostProcessor` (or `@Configuration` used as BFPP) injects a regular `@Service`. Move that dependency so the service is created in `preInstantiateSingletons`, or do not make your config class demand business beans at factory-post-process time.

---

### Q12. How does Spring pick a constructor?

**Answer:** One constructor → use it. `@Autowired` on one constructor → use it. Multiple greedy constructors: it tries to satisfy the most specific match with available beans; if still ambiguous, fail. Prefer a single constructor.

**Counter:** What about a no-arg constructor plus an `@Autowired` all-args constructor?

**Counter-answer:** The annotated one wins. The no-arg exists for JPA/serializers, not for Spring. Keep that split explicit so Jackson/JPA are not confused with DI.

---

### Q13. What does `preInstantiateSingletons` skip?

**Answer:** Lazy singletons, abstract definitions, and non-singletons (prototype). FactoryBeans are initialized according to type matching rules; the factory is a singleton, `getObject()` may be called to warm the product.

**Counter:** If a singleton is lazy, when is it created?

**Counter-answer:** First `getBean` / first injection into something that *is* being created. A lazy bean injected into a non-lazy singleton is still created at startup — the injector needs it. `@Lazy` on the **injection point** is what delays that.

---

### Q14. How do `@Bean` methods become BeanDefinitions?

**Answer:** `ConfigurationClassPostProcessor` reads `@Configuration` classes, finds `@Bean` methods, and registers `BeanDefinition`s whose factory is that method. At creation time Spring calls the method (through a CGLIB subclass in full mode — see [055](055_Spring_Annotations.md)).

**Counter:** If I call `restTemplate()` from another `@Bean` method, do I get two RestTemplates?

**Counter-answer:** In **full** `@Configuration`, no — the call is intercepted and goes to `getBean`. In **lite** (`@Bean` on `@Component`), yes — a raw method call, second instance, not in the container. That is a classic 5–8 YOE question.

---

### Q15. What is `getEarlyBeanReference`?

**Answer:** A `SmartInstantiationAwareBeanPostProcessor` hook used when exposing an in-creation singleton. The AOP auto-proxy creator may return a proxy here so that circular collaborators and the later L1 bean are the **same proxy**.

**Counter:** What if a custom BPP wraps in `postProcessAfterInitialization` but not in `getEarlyBeanReference`?

**Counter-answer:** Cycle dependents can receive the raw bean, later lookups receive the wrapper. Two objects, transactions only on one path. Custom BPPs that wrap must participate in early-reference if they care about identity.

---

### Q16. BeanFactory vs ApplicationContext at the internals level?

**Answer:** Context **owns** a factory, adds Environment, event multicaster, message source, Lifecycle, and `refresh()` orchestration. `getBean` is delegated to the factory. Saying “the context is a BeanFactory” is true via inheritance (`ApplicationContext` extends `ListableBeanFactory`), but the extra refresh algorithm lives on the context.

**Counter:** Can you use `DefaultListableBeanFactory` alone in production?

**Counter-answer:** Possible for a tiny embedded plugin. You lose events, i18n, annotation config processing unless you add it yourself. Not worth it; use `GenericApplicationContext` / `AnnotationConfigApplicationContext`.

---

### Q17. How does type matching work for `getBean(PaymentGateway.class)`?

**Answer:** The factory looks at definitions’ `beanClass` / factory method return type / `FactoryBean.getObjectType()`, considers generics where possible, filters autowire candidates, then applies `@Primary` / `@Qualifier` if injecting.

**Counter:** Why might a `FactoryBean` product not be found by type until started?

**Counter-answer:** If `getObjectType()` is inaccurate or the generic is raw, the type index is wrong. Libraries that implement `FactoryBean` must return the correct product type; otherwise you get `NoSuchBeanDefinitionException` for a bean that “is there” by name.

---

### Q18. What is `depends-on` vs a constructor dependency?

**Answer:** Constructor/setter **injection** is a real object graph. `@DependsOn` only forces **initialization order** without injecting. Use `@DependsOn` for static side effects (register a JDBC driver bean before a pool that assumes it). Prefer injection when you actually need the object.

**Counter:** If A injects B, do you still need `@DependsOn("b")` on A?

**Counter-answer:** No. The injection edge already creates B first. Redundant `@DependsOn` is noise.

---

### Q19. How would you debug `UnsatisfiedDependencyException` in production startup?

**Answer:** Read the nested cause: missing bean, two beans, or type mismatch. Check component scan base package, `@Profile`, `@Conditional`, qualifier names, and whether the missing class is created with `new`. Enable `DEBUG` on `org.springframework.beans`. Dump `ctx.getBeanDefinitionNames()` in a failing test.

**Counter:** The bean exists when I print names, but injection still fails. Why?

**Counter-answer:** Often a **generic type** mismatch (`Repository<Order>` vs `Repository<User>`), a **proxy** that does not expose the needed interface, or injecting by name that does not match. Also: the definition exists but is `abstract` or not an autowire candidate (`autowireCandidate = false`).

---

### Q20. Spring 6 / Boot 3: anything that changes this story?

**Answer:** Jakarta namespace; AOT/Graal prefers less reflection and more functional bean registration; circular references discouraged/disabled by default in Boot. The three-level cache still exists for singleton setter cycles when allowed. Constructor cycles still fail.

**Counter:** Does Graal native image use the same `refresh()`?

**Counter-answer:** Build-time AOT processes bean definitions and generates code so runtime `refresh()` does less reflection. Conceptually the same phases; implementation is ahead-of-time. For interviews: “AOT = do the container work at build, native image starts faster and with a closed world.”

---

### Interview one-liner

> Definitions first, instances later. BFPPs edit recipes; BPPs wrap instances. `refresh()` ends by creating non-lazy singletons. The 3-level cache breaks **singleton setter** cycles and preserves one early AOP proxy; **constructor cycles still fail**. `FactoryBean` publishes `getObject()`; `&name` is the factory.
