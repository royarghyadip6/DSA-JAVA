# 56. Spring AOP

[← 055 Config and Annotations](055_Spring_Annotations.md) | [Course map](00_COURSE_MAP.md) | **Next:** [056_1 Events, SpEL, Resources →](056_1_Events_SpEL_Resources.md)

Also after this chapter: [056_5 Async, Scheduling, Cache](056_5_Async_Scheduling_Cache.md) (same wrapper idea).

Teaching is simple first. **Interview Q&A at the end is 5–8 year standard.**

---

## Simple first

**AOP** = Aspect-Oriented Programming.

**Plain English:** some work is the *same* in many methods: start a database transaction, write a log, check security. Instead of copy-paste, you write that work **once** and Spring **wraps** your method.

**The wrapper is called a proxy.** Callers talk to the wrapper. The wrapper does extra work, then calls your real method.

```text
You:     restaurant kitchen (real OrderService)
Proxy:   waiter
Caller:  customer

Customer talks to the waiter, not the kitchen.
Waiter can: check the bill (security), start a tab (transaction), then pass the order to the kitchen.
```

**The #1 beginner bug — “self-invocation”**

Inside the kitchen, a cook calling another cook (`this.save()`) **does not go through the waiter**. So `@Transactional` / `@Async` / your logging aspect **do not run**.

```text
Caller → waiter (proxy) → place()          ← extra work runs
               place() does this.save()    ← extra work SKIPPED
```

Fix: split into two beans (two kitchens), or call the waiter again (inject `self`). Do not just hope.

**What Spring AOP can wrap:** public methods of **Spring beans**. Not `private` methods, not `new` objects you created yourself, not field access.

**Two kinds of waiter:**

| Kind | How | Remember |
|------|-----|----------|
| **JDK proxy** | Pretends to implement your **interface** | You must call through the interface |
| **CGLIB** | Pretends to be a **child class** of your class | `final` methods cannot be wrapped |

Boot often uses CGLIB by default. Interviews still want both names.

---

## When you interview (5–8 years)

Almost every “Spring magic” bug is AOP. A senior can explain JDK vs CGLIB, self-invocation (especially inner `REQUIRES_NEW`), `final`, and advice order around transactions.

---

## 1. Why AOP exists

Cross-cutting concerns repeat on many methods: transactions, metrics, audit, retries, extra security.

```java
// without AOP — noise in every method
public void transfer(...) {
    log.info("start");
    tx.begin();
    try {
        // business
        tx.commit();
    } catch (RuntimeException e) {
        tx.rollback();
        throw e;
    }
}
```

With AOP the business method stays business. The concern lives in one aspect (or in `@Transactional`, which *is* an aspect Spring ships).

Use AOP when the policy is **orthogonal** (same rule, many methods). Do not use it to hide business workflow (“if amount > X, call compliance”) — that belongs in a service.

---

## 2. AspectJ vocabulary (everyday words first)

You must use the right words in interviews. Learn them with this table.

| Term | Meaning | Example |
|------|---------|---------|
| Join point | A point in execution you *could* advise | A method call |
| Pointcut | Predicate that selects join points | `execution(* com.app.service.*.*(..))` |
| Advice | Code that runs at those join points | `@Around` method |
| Aspect | Module that holds pointcuts + advice | `@Aspect` class |
| Weaving | Attaching advice to join points | Runtime proxy (Spring) or compile/load-time (AspectJ) |
| Advisor | Spring: one pointcut + one advice | Used internally |
| Introduction | Add interface to a bean | Rare (`@DeclareParents`) |
| Target | The real object | `OrderServiceImpl` |
| Proxy | Object callers receive | CGLIB subclass or JDK proxy |

Spring only exposes **method execution** join points on **Spring beans**.

---

## 3. Enabling Spring AOP

```java
@Configuration
@EnableAspectJAutoProxy
@ComponentScan("com.app")
public class AopConfig { }
```

`@EnableAspectJAutoProxy` imports an auto-proxy creator (`AnnotationAwareAspectJAutoProxyCreator`) — a `BeanPostProcessor` that wraps matching beans in `postProcessAfterInitialization`.

Boot: starter + auto-config, you rarely type `@EnableAspectJAutoProxy`.

`@EnableAspectJAutoProxy(proxyTargetClass = true)` → always CGLIB. `exposeProxy = true` → `AopContext.currentProxy()` for the self-invocation workaround.

Aspects must be **Spring beans** (`@Aspect` + `@Component`, or `@Bean` + `@Aspect`). `@Aspect` alone is not enough.

---

## 4. Advice types

**Advice** = the extra code. **When** it runs is the only difference.

| You want… | Use |
|-----------|-----|
| Run *before* the method | `@Before` |
| Run *after success* | `@AfterReturning` |
| Run *after a throw* | `@AfterThrowing` |
| Run *always* after (success or fail) | `@After` (like `finally`) |
| Control everything (time it, retry, skip the method) | `@Around` |

| Annotation | When | Typical use |
|------------|------|-------------|
| `@Before` | Before method | Authz check, MDC, argument validation |
| `@AfterReturning` | After normal return | Audit success, cache put |
| `@AfterThrowing` | After exception | Metrics, translate |
| `@After` | Finally (success or fail) | Clear ThreadLocal |
| `@Around` | Wrap; you call `proceed()` | Timing, retry, `@Transactional` |

```java
@Aspect
@Component
public class TimingAspect {

    @Around("execution(* com.app.service..*.*(..))")
    public Object time(ProceedingJoinPoint pjp) throws Throwable {
        long t = System.nanoTime();
        try {
            return pjp.proceed();
        } finally {
            // log pjp.getSignature() and duration
        }
    }
}
```

**`@Around` is the most powerful.** You can skip `proceed()` (short-circuit), change args, change the return, swallow or wrap exceptions. That is also why it is the most dangerous.

`@After` is “after finally,” not “after success.” Use `@AfterReturning` for success-only.

If `@Before` throws, the target is **not** called.

---

## 5. Pointcuts you should actually write

Designators you will see:

| PCD | Meaning |
|-----|---------|
| `execution` | Method signature — main one in Spring AOP |
| `within` | Type (and nested) |
| `this` | Proxy implements type |
| `target` | Target object type |
| `args` | Runtime argument types |
| `@annotation` | Method has annotation |
| `@within` / `@target` | Type has annotation |
| `@args` | Argument types have annotation |
| `bean` | Spring bean name (Spring extension, not pure AspectJ) |

```java
@Pointcut("execution(public * com.app.service..*(..))")
public void serviceMethods() {}

@Pointcut("@annotation(org.springframework.transaction.annotation.Transactional)")
public void transactional() {}

@Before("serviceMethods() && !transactional()")
public void beforeNonTx() {}
```

Prefer **composed `@Pointcut` methods** over copying `execution` strings.

**Good vs brittle:**

```text
Good:  @annotation(com.app.Audit)
       execution(* com.app.application..*Service.*(..))

Brittle: execution(* *(..))     // everything, including toString
         within(com.app..*)     // too wide; toString, getters, equals
```

`execution(* com.app.service.OrderService.*(..))` matches methods **declared** on that type as Spring’s proxy sees them — package-private / private are not on the proxy.

---

## 6. How the proxy is built (internals)

```text
1. @EnableAspectJAutoProxy registers AnnotationAwareAspectJAutoProxyCreator (BPP)
2. After a bean is initialized, the BPP asks: any advisors match this bean?
3. If yes, create proxy:
     - JDK dynamic proxy if (interfaces exist AND proxyTargetClass=false)
     - else CGLIB subclass
4. Callers get the proxy from the singleton cache
5. Method call → ReflectiveMethodInvocation / interceptor chain
     interceptor 0 → 1 → … → target
```

Advisors are sorted (`@Order` on aspects, `Ordered` on advisors). **Lowest order value runs first** on the way **in**. On the way out, `@Around`/`@After` reverse naturally through the stack.

`@Transactional` is not implemented as your `@Aspect`. It is `BeanFactoryTransactionAttributeSourceAdvisor` + `TransactionInterceptor`. Same proxy infrastructure.

---

## 7. JDK dynamic proxy vs CGLIB

| | JDK proxy | CGLIB (or Spring’s bytecode subclass) |
|--|-----------|----------------------------------------|
| Mechanism | `java.lang.reflect.Proxy` implements **interfaces** | Subclass of the **concrete class** |
| Methods advised | Interface methods only | Public (and sometimes protected) methods of the class |
| `final` class | OK (proxy is not a subclass of your class) | **Cannot** subclass |
| `final` method | N/A on interface | **Cannot** override → no advice |
| Injection type | Must inject the **interface** | Can inject concrete class |
| Default in older Spring | If the bean implements any interface → JDK | If no interface → CGLIB |
| `spring.aop.proxy-target-class=true` or `@EnableAspectJAutoProxy(proxyTargetClass=true)` | Forced CGLIB | Forced CGLIB |

**Spring Boot 2+ default:** `proxy-target-class=true` (CGLIB even when interfaces exist). Spring Framework Core default was historically JDK-if-interface. **Always state which default you mean.**

Spring 6 still uses CGLIB-style subclassing (Objenesis + bytecode). You do not need to name the library version; you need the subclass vs interface model.

```java
public interface Billing { void charge(); }

@Service
public class StripeBilling implements Billing {
    public void charge() { }
    public void extra() { } // not on interface
}
```

JDK proxy: `extra()` is **not** advised and is **not** even callable through `Billing`. You must inject `StripeBilling` and use CGLIB to advise `extra()`.

`final` service class: CGLIB fails to create a subclass. Either remove `final`, introduce an interface + JDK proxy, or do not apply AOP.

---

## 8. Self-invocation — the question they will not skip

```java
@Service
public class OrderService {

    public void place(Order o) {
        this.save(o);          // THIS — not the proxy
    }

    @Transactional
    public void save(Order o) { }
}
```

`place()` was called on the proxy. Inside `place`, `this` is the **target**. `save` runs with **no** transaction interceptor.

Same for `@Async`, `@Cacheable`, custom aspects, `@PreAuthorize`.

### Fixes (in order of taste)

1. **Split classes** — `OrderService` calls `OrderRepository` or `OrderWriter` bean (another proxy). Best.
2. **Inject self** (ugly but explicit):

```java
@Service
public class OrderService {
    private final OrderService self;
    public OrderService(@Lazy OrderService self) { this.self = self; }

    public void place(Order o) {
        self.save(o);          // through proxy
    }
}
```

3. **`AopContext.currentProxy()`** — requires `exposeProxy = true`. Thread-local, easy to forget, ugly in tests.
4. **AspectJ mode** (`mode = AdviceMode.ASPECTJ`) + load-time / compile-time weaver — advises `this` calls. Heavy; rare in typical apps.

Private methods: **never advised** by Spring AOP (not on the proxy). `final` methods: not overridable by CGLIB. `static`: no instance proxy. Calls between methods in the same class that are `private` cannot be fixed with CGLIB.

---

## 9. What Spring AOP cannot do

| Join point | Spring AOP | AspectJ weaver |
|------------|------------|----------------|
| Public method on a Spring bean | Yes | Yes |
| `private` / `static` method | No | Yes |
| `this.foo()` self-call | No | Yes |
| Constructor | No | Yes |
| Field get/set | No | Yes |
| Objects created with `new` | No | Yes |
| `final` method (CGLIB) | No | Yes (with caveats) |

`@EnableAspectJAutoProxy` ≠ full AspectJ. `mode = ASPECTJ` switches to AspectJ weaving if the weaver is on the classpath (`spring-aspects` + LTW agent).

---

## 10. Ordering multiple aspects

```java
@Aspect
@Order(1)   // inner? wait — lowest order = first inbound
@Component
public class LoggingAspect { }

@Aspect
@Order(2)
@Component
public class SecurityAspect { }
```

Inbound: `@Order(1)` around/before runs **before** `@Order(2)`. Transaction advisor also has an order (`@EnableTransactionManagement(order = ...)`). If your aspect must see the **transactional** connection, it needs to be **inside** the transaction (higher order number than the tx advisor so tx starts first). If it must run **outside** tx (logging duration including commit), give it a **lower** order than tx.

Draw it:

```text
Order 0 (outer)  logging around
  Order 50       transaction around
    target method
  commit/rollback
log duration
```

Do not guess in production — set `order` explicitly when two around-advices interact.

`@After` vs `@AfterThrowing` vs `@Around` on the **same** aspect: `@Around` wraps everything; mixing several advice types on one method is allowed but easy to confuse. Prefer one `@Around` or a clean before/after pair.

---

## 11. `@Transactional` as AOP (preview of 056_3)

- `@EnableTransactionManagement` registers advisor + interceptor
- Public method on a Spring bean, called **through the proxy**
- Default rollback: unchecked exceptions
- Isolation/propagation live on the interceptor, not on JDBC by themselves

All self-invocation rules apply. Full story: [056_3](056_3_Jdbc_and_Transaction_Abstraction.md).

---

## 12. Introductions and `AopUtils`

`AopUtils.isAopProxy(bean)`, `AopProxyUtils.ultimateTargetClass(bean)` — useful in diagnostics.

`@DeclareParents` / introduction mixins: add an interface to beans matching a pointcut. Rare; mention if asked about “introduction advice.”

---

## Production pitfalls

1. **Self-invocation** — #1 production bug.
2. **Pointcut too wide** — advising `toString`/`equals` or every getter; stack overflow or noise.
3. **Checked exceptions from `@Before`** — declared throws vs wrapping.
4. **Swallowing in `@Around`** — lost errors, empty API responses.
5. **Aspect not a bean** — `@Aspect` without `@Component`/`@Bean`.
6. **`final` class + CGLIB** — silent skip or context failure depending on version/config.
7. **Calling aspects on objects you `new`** — not beans, no proxy.
8. **ThreadLocal in `@Before` without `@After`** — leak on thread pools.
9. **Order vs transactions** — audit that reads uncommitted data, or retry that retries outside tx incorrectly.
10. **Using AOP for business rules** — undebuggable implicit flow.

---

## Interview Ready Q&A (5–8 year standard)

The notes used a waiter. **Here, name proxy types, interceptor chain, `REQUIRES_NEW` + `this`, AspectJ vs Spring AOP, and `@Order` vs transactions.**

### Q1. What is AOP, and why does Spring have it?

**Answer:** A way to apply cross-cutting logic (tx, metrics, audit) without copying it into every method. Spring implements it with **runtime proxies** around beans.

**Counter:** Is AOP the only way `@Transactional` could work?

**Counter-answer:** No. You could use a `TransactionTemplate` in every method (programmatic). AOP is the *declarative* path. AspectJ weaving would also work and would catch self-invocation. Spring chose proxies for zero extra compile step.

---

### Q2. Join point vs pointcut vs advice vs aspect?

**Answer:** Join point = candidate location (method execution). Pointcut = which ones. Advice = what to run. Aspect = the class grouping them.

**Counter:** What join points does Spring AOP support?

**Counter-answer:** Method execution on Spring beans only. Not constructors, fields, or `new`.

---

### Q3. Which advice type is most powerful? Why be careful?

**Answer:** `@Around` — you control `proceed()`, args, return, exceptions. You can accidentally skip the target or swallow errors.

**Counter:** Can you replace `@Before` + `@AfterReturning` with `@Around`?

**Counter-answer:** Yes. Teams often prefer one `@Around` for timing. For “must run even if proceed throws,” use try/finally inside around, which is `@After`’s meaning.

---

### Q4. JDK proxy vs CGLIB — which one do you get?

**Answer:** If `proxyTargetClass=true` (Boot default): CGLIB subclass always (unless the class is not subclassable). If false: JDK proxy when the bean implements an interface, else CGLIB.

**Counter:** I inject the concrete class, but Spring built a JDK proxy. What happens?

**Counter-answer:** `NoSuchBeanDefinitionException` or a failure to autowire by concrete type, because the JDK proxy **is not** a subclass of the concrete class — only of `Proxy` + interfaces. Inject the interface, or force CGLIB.

---

### Q5. Why doesn’t `@Transactional` work on a private method?

**Answer:** Spring AOP proxies do not intercept private methods. The call never goes through the interceptor. Also, `@Transactional` on non-public methods is ignored by the default annotation parser (`public` only, unless you switch to AspectJ mode).

**Counter:** Protected method on a concrete class with CGLIB?

**Counter-answer:** CGLIB can override protected methods; Spring’s transaction annotation parser still **defaults to public-only**. Do not rely on protected. Make it public on another bean.

---

### Q6. Explain self-invocation.

**Answer:** Internal `this.method()` hits the target, not the proxy, so no advisors. Split beans, inject self, or AspectJ weave.

**Counter:** If `place()` is `@Transactional` and it calls `this.save()`, does `save` join the transaction?

**Counter-answer:** **Yes, as plain Java**, because they are the same thread and `place` already opened the tx via the proxy. `save` does **not** get its **own** interceptor (so `REQUIRES_NEW` on `save` would **not** apply). Nested `REQUIRED` appears to “work” because of the outer tx; `REQUIRES_NEW` / `NOT_SUPPORTED` on the inner method will **silently not apply**. That is the killer counter-question.

---

### Q7. How do you force a self-call through the proxy?

**Answer:** Extract a second bean; or inject `self` with `@Lazy`; or `exposeProxy=true` and `((OrderService) AopContext.currentProxy()).save()`.

**Counter:** Why is `AopContext` disliked?

**Counter-answer:** Hidden thread-local, easy to forget `exposeProxy`, ugly unit tests, couples code to Spring AOP. A second bean is ordinary DI.

---

### Q8. Is `@EnableAspectJAutoProxy` full AspectJ?

**Answer:** No. It enables **@Aspect-style pointcuts** on **Spring proxies**. Full AspectJ is `mode = ASPECTJ` plus a weaver.

**Counter:** When would you pay for load-time weaving?

**Counter-answer:** You must intercept `new`, private, or self-calls in code you cannot split (legacy). Ops cost: javaagent, classloader issues. Most teams split beans instead.

---

### Q9. How is advice ordered?

**Answer:** `@Order` / `Ordered` — lower value = outer/inbound first. Transaction management has its own order. Set it when aspects must run inside or outside a transaction.

**Counter:** Two `@Around` without `@Order`?

**Counter-answer:** Order is undefined from your point of view (aspect bean name / definition order). Non-deterministic relative to tx. Always order when it matters.

---

### Q10. Why must an aspect be a Spring bean?

**Answer:** The auto-proxy creator looks up `@Aspect` beans in the container to build advisors. A plain `new LoggingAspect()` is invisible.

**Counter:** `@Bean` method returning an `@Aspect` instance?

**Counter-answer:** That works — it is a bean. `@Component` + `@Aspect` is the usual scan path.

---

### Q11. Can AOP advise a bean created with `new` inside a service?

**Answer:** No. Not a container bean, no proxy.

**Counter:** `new` inside a `@Bean` method?

**Counter-answer:** The **returned** object becomes the bean and **can** be proxied in `postProcessAfterInitialization`. The `new` is how the factory creates the target. That is normal.

---

### Q12. `execution` vs `within` vs `@annotation`?

**Answer:** `execution` = method signature. `within` = declaring type. `@annotation` = method-level annotation. Combine with `&&` / `||` / `!`.

**Counter:** `@annotation(Transactional)` vs `execution` on `*Service.*`?

**Counter-answer:** Annotation pointcut only hits methods (or classes, depending) that bear the annotation — precise. Execution-on-package is broader and can catch methods you did not intend. For audit, prefer an explicit `@Audit` annotation.

---

### Q13. What is the interceptor chain?

**Answer:** A list of `MethodInterceptor`s (and other `Advisor`s) around `MethodInvocation.proceed()`. Each around-advice is an interceptor. The last proceed hits the target via reflection.

**Counter:** Where does `@Before` sit in that chain?

**Counter-answer:** Adapted to an interceptor that runs logic then `proceed()`. Same machinery.

---

### Q14. CGLIB and `final` methods — what happens to `@Transactional`?

**Answer:** Subclass cannot override `final`, so the proxy does not intercept that method. Calls go to the final implementation with **no** tx advice. Failure can be silent.

**Counter:** `final` class?

**Counter-answer:** Proxy creation fails (or the bean is not eligible), depending on setup. Remove `final` or use an interface + JDK proxy without subclassing the class.

---

### Q15. How does Spring decide a bean needs a proxy?

**Answer:** Auto-proxy creator matches advisors (transaction attributes, `@Aspect` pointcuts, `@Async`, cache). If any match, wrap. If none match, the raw bean is published.

**Counter:** Every bean proxied — performance?

**Counter-answer:** Unlikely unless a pointcut is `execution(* *(..))`. Proxies are cheap compared to IO; still, wide pointcuts cost startup (every method matched) and add a stack frame per call. Keep pointcuts tight.

---

### Q16. `@AfterThrowing` vs try/catch in `@Around`?

**Answer:** `@AfterThrowing` runs when the join point throws; you cannot swallow and convert to a return value easily. `@Around` can catch, wrap, or recover.

**Counter:** Does `@AfterThrowing` stop the exception from propagating?

**Counter-answer:** No. It observes, then the exception continues (unless you throw a different one from the advice, which replaces it).

---

### Q17. ThreadLocal in an aspect — what can go wrong?

**Answer:** Tomcat threads are pooled. If `@Before` sets MDC/ThreadLocal and the method throws before `@After`, you can leak unless you use `@After` or around-finally. Also self-invocation may skip the after advice if you put it only on the outer method incorrectly.

**Counter:** `@Async` method and MDC?

**Counter-answer:** New thread — ThreadLocal does not follow. You need a `TaskDecorator` (see [056_5](056_5_Async_Scheduling_Cache.md)). Aspects on the async method run on the worker thread, not the caller.

---

### Q18. Introduction advice — one sentence?

**Answer:** Dynamically implement extra interfaces on matching beans (`@DeclareParents`). Rare; used for mixin-style capabilities.

**Counter:** Is that a JDK proxy requirement?

**Counter-answer:** Introductions add interfaces, so JDK proxies fit naturally. You can still use CGLIB plus extra interfaces. Know it exists; do not design apps around it.

---

### Q19. How do you unit-test an aspect?

**Answer:** Unit-test the advice method with a mock `ProceedingJoinPoint`. Integration-test with a real `@SpringJUnitConfig`, a dummy `@Service`, and assert side effects. Do not start the whole Boot app for an aspect.

**Counter:** Why did the aspect not fire in the test?

**Counter-answer:** Forgot `@EnableAspectJAutoProxy`, aspect not scanned, called `new Service()`, or self-invocation in the dummy.

---

### Q20. When should you *not* use AOP?

**Answer:** Business branching, one-off logic, code that juniors must see in the method, or when a simple decorator/filter/interceptor (MVC) is the native hook. AOP is for **uniform** policies.

**Counter:** Logging — aspect or explicit logger?

**Counter-answer:** Prefer explicit logs at business meaning (“order placed”). Use an aspect for **technical** traces (metrics, audit of *every* service method) with a tight pointcut. Dumping args of all methods is a PII/security incident waiting to happen.

---

### Q21. `proxyTargetClass` vs injecting interfaces — what do you recommend?

**Answer:** Program to interfaces for domain APIs. In Boot, CGLIB is default so concrete injection happens to work — do not rely on that. If you need JDK proxies, inject interfaces and set `proxyTargetClass=false`, knowing only interface methods are advised.

**Counter:** Spring 6 / native image?

**Counter-answer:** Proxies need extra reflection config. AOT generates hints. Prefer simpler graphs and `proxyBeanMethods = false` on config; AOP still works but native is less forgiving of exotic pointcuts.

---

### Interview one-liner

> Spring AOP = runtime method proxies on beans. Call the proxy, not `this`. JDK proxy = interfaces; CGLIB = subclass; `final` and private are not advised. `@Around` is power and risk. `@Transactional`/`@Async`/`@Cacheable` are advisors on the same chain. Order matters around transactions. Full AspectJ weaving is a different switch.
