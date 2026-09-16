# 08. AOP and Proxies

## Start here (simple English)

**In one sentence:** AOP means **“run extra code around a method”** (start a transaction, log time, check security) **without pasting that extra code into every method**.

**Everyday picture:** A **gift wrap** around a box.

- The box = your real `OrderService`
- The wrap = a **proxy** Spring gives to everyone else
- Anyone who calls through the wrap can trigger extra steps (open transaction, then unwrap and call the real method)

```text
Caller  →  PROXY (wrapper)  →  real OrderService.place()
```

**The #1 bug (learn this on day 1):**

```java
public void facade() {
    this.place(); // this = the raw box, NOT the wrap
}
```

`this.place()` **skips** the proxy. So `@Transactional`, `@Async`, `@Cacheable`, `@PreAuthorize` on `place` **do not run**.

**Fix:** call `place` from **another Spring bean**, so the call goes through the wrapper.

Spring AOP only works on **Spring beans** and (in practice) **public methods**. It is not magic on `new OrderService()`.

Interview Q&A is **5–8 year standard** (JDK vs CGLIB, `final`, order).

---

Almost every “Spring magic didn’t run” bug is this chapter.

---

## 1. Mental model

```text
Caller
  → Spring bean (PROXY)
      → interceptor chain (tx, security, your @Around, …)
          → target method on the real object
```

Spring AOP is **runtime, proxy-based, method-execution join points on Spring beans**. It is not full AspectJ (no field intercept, no `new`, no constructors).

**Memory trick:** Spring AOP = decorator around methods of beans. AspectJ = bytecode weaver.

---

## 2. Vocabulary

| Term | Meaning |
|------|---------|
| Join point | A point you *could* advise (a method execution) |
| Pointcut | Predicate selecting join points |
| Advice | Code that runs (`@Before`, `@Around`, …) |
| Aspect | Class holding pointcuts + advice (`@Aspect`) |
| Weaving | Attaching advice — runtime proxy in Spring |
| Target | Real object |
| Proxy | Object in the singleton cache that callers receive |
| Introduction | Add an interface to a bean (rare) |

The aspect class **must be a Spring bean** (`@Component` or `@Bean`). `@Aspect` alone is not enough.

Boot: `spring-boot-starter-aop` (or any starter that pulls it, e.g. transactions) enables `@EnableAspectJAutoProxy` equivalent. `spring.aop.auto=true` by default.

---

## 3. Advice types

| Advice | When |
|--------|------|
| `@Before` | Before method; cannot skip unless it throws |
| `@AfterReturning` | After normal return; can see return value |
| `@AfterThrowing` | After exception |
| `@After` (`finally`) | Always after |
| `@Around` | Full control: `proceed()`, skip, change args/return |

```java
@Aspect
@Component
public class TimingAspect {

    @Pointcut("execution(* com.acme.payments.service..*(..))")
    void services() {}

    @Around("services()")
    public Object time(ProceedingJoinPoint pjp) throws Throwable {
        long t0 = System.nanoTime();
        try {
            return pjp.proceed();
        } finally {
            // log pjp.getSignature(), nanos
        }
    }
}
```

`ProceedingJoinPoint` is **only** for `@Around`. Others use `JoinPoint`.

**Order of mixed advice on one join point (simplified):**

```text
@Around (before proceed)
  @Before
    method
  @AfterReturning or @AfterThrowing
  @After
@Around (after proceed)
```

Multiple aspects: `@Order` on the aspect class. **Lower value runs first** on the way in (outer). Transaction aspect order vs your aspect matters: if you log **after** commit, order accordingly. Spring TX advisor has a defined order (`Ordered.LOWEST_PRECEDENCE` ish — don’t assume; if you need “after commit”, use `TransactionSynchronizationManager.registerSynchronization`).

---

## 4. Pointcut designators (the ones you use)

```text
execution(* com.acme.service.OrderService.create(..))
execution(public * com.acme.service..*(..))
within(com.acme.service..*)
@annotation(org.springframework.transaction.annotation.Transactional)
@Bean  @within(org.springframework.stereotype.Service)
target(com.acme.service.OrderService)
this(com.acme.service.OrderService)
args(java.util.UUID)
@args(...)
```

- `execution` — method signature
- `within` — type
- `@annotation` — method has that annotation
- `@within` / `@target` — class has that annotation
- `this` — **proxy** type
- `target` — **target** type

Prefer `@annotation` on a custom `@Audited` over a huge `execution` string.

**Don’t** use `execution(* *(..))` on the whole app. You will intercept `toString`, config classes, and destroy startup.

---

## 5. JDK vs CGLIB (again, because interviews repeat it)

Boot default: `spring.aop.proxy-target-class=true` → **CGLIB subclass** even if interfaces exist.

| Constraint | Effect |
|------------|--------|
| `final` class | Cannot subclass → CGLIB fails; may fall back or explode |
| `final` method | Cannot override → **advice never runs** on that method |
| `private` / `package` method | Not overridden by CGLIB proxy → **no advice** |
| Call from `this` | Bypass proxy → **no advice** |

JDK proxy: only interface methods. Casting to impl class fails.

Force:

```yaml
spring:
  aop:
    proxy-target-class: true
```

`@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)` — `exposeProxy` allows:

```java
((OrderService) AopContext.currentProxy()).place();
```

That is a smell. Prefer splitting classes so an inner call becomes an **outer** call through another bean.

---

## 6. Self-invocation — the #1 bug

```java
@Service
public class OrderService {
    @Transactional
    public void place(Order o) { ... }

    public void facade(Order o) {
        this.place(o); // raw this → no TX, no async, no cache, no security
    }
}
```

Same for `@Async`, `@Cacheable`, `@PreAuthorize`, custom `@Around`.

Fixes:

1. Move `place` to another `@Service` and inject it
2. `AopContext.currentProxy()` (needs `exposeProxy`)
3. Inject self via setter/`@Lazy` (cycle; works, still a smell)

**Also:** `@Transactional` on `private` methods is silently ignored (CGLIB cannot override private). Compiles. Fails in prod.

---

## 7. Built-in aspects you already use

They are advisors in the same chain:

| Annotation | Module |
|------------|--------|
| `@Transactional` | `spring-tx` |
| `@Async` | `spring-context` |
| `@Scheduled` | not a method interceptor on each call; a registrar |
| `@Cacheable` `@CacheEvict` | `spring-context` |
| `@PreAuthorize` | Spring Security |
| `@Retryable` | Spring Retry / Resilience4j |

`@Scheduled` is **not** “AOP on every invocation from your code”; the scheduler **calls** the method from outside, so it usually **does** go through the proxy if the method is public on a bean. Still don’t make it `private`.

---

## 8. Limitations of Spring AOP

- Only Spring beans
- Only public (practical) methods
- Only method execution
- One proxy per bean; many advisors on that proxy
- Not for `new Xxx()` objects
- Aspect advising another aspect — keep it simple

Need constructor/field intercept? **AspectJ weaver**, not Boot default.

---

## 9. Production pitfalls

1. Logging aspect with `execution(* com.acme..*(..))` including repositories — N+1 logs, huge volume.
2. `@Around` swallowing exceptions.
3. `@Around` not calling `proceed()` in a path — method skipped silently.
4. Aspect depends on the bean it advises → cycle.
5. Using AOP to hide business workflow.
6. `@Order` confusion: your audit aspect runs inside an uncommitted TX and logs data that rolls back.

---

# Interview Q&A (5–8 year bar)

A fresher says “AOP is logging.” A 5–8 year answer explains self-invocation, proxy type, and after-commit vs `@After`.

### Q1. What is AOP?

**Answer:** Modularizing cross-cutting concerns (tx, metrics, audit) so business methods stay clean. Spring implements it with **proxies**.

**Counter:** AOP vs OOP?  
**Answer:** OOP modularizes by domain type. AOP modularizes by **concern** that cuts across types.

---

### Q2. Proxy vs AspectJ weaving?

**Answer:** Spring AOP = runtime decorator, methods of beans. AspectJ = compile/load-time bytecode, more join points.

**Counter:** Does Boot use AspectJ by default?  
**Answer:** It uses the AspectJ **pointcut language** (`execution`, …) with **Spring proxies**, not the AspectJ compiler.

---

### Q3. Why didn’t `@Transactional` / `@Async` work?

**Answer:** Self-invocation, `private`/`final` method, object not a bean (`new`), or calling through a non-Spring instance.

**Counter:** How do you prove it?  
**Answer:** Debugger: is the runtime type `*$$SpringCGLIB$$*` or `*$Proxy*`? If you see the concrete class with no suffix, you are on the target.

---

### Q4. `@Before` vs `@Around`?

**Answer:** `@Before` cannot skip or change the return. `@Around` can skip `proceed()`, change args, wrap exceptions. Most powerful; easiest to get wrong.

---

### Q5. What is a pointcut?

**Answer:** Expression selecting join points. Example: `execution(* com.acme.service.*.*(..))`.

**Counter:** `@annotation` vs `execution`?  
**Answer:** `@annotation` is explicit and refactor-safe. `execution` is broad and brittle.

---

### Q6. JDK vs CGLIB?

**Answer:** Interface proxy vs subclass. Boot defaults to class proxies. `final` methods are not advised with CGLIB.

---

### Q7. How do you control aspect order?

**Answer:** `@Order` on the aspect. Lower = outer (runs first on the way in).

**Counter:** How do you run code **after commit**?  
**Answer:** Not `@After` on the service method (that is still before commit if you’re inside TX). Use `TransactionSynchronization` `afterCommit`.

---

### Q8. Must an `@Aspect` be a `@Component`?

**Answer:** It must be a **bean**. `@Component` or a `@Bean` method. `@Aspect` without registration does nothing.

---

### Q9. Can you advise a `static` method?

**Answer:** Not with Spring AOP.

---

### Q10. `this` vs `target` in a pointcut?

**Answer:** `this` = proxy type. `target` = underlying object type. For CGLIB both are the concrete class hierarchy; for JDK `this` is the interface proxy.

---

### Q11. What is `exposeProxy` / `AopContext`?

**Answer:** Thread-local to the current proxy so `this` can be replaced by `AopContext.currentProxy()`. Workaround for self-invocation. Prefer splitting beans.

---

### Q12. Does `@Cacheable` use AOP?

**Answer:** Yes. Same self-invocation rules. `this.get(id)` will not hit the cache.

---

### Q13. Performance cost?

**Answer:** One extra virtual call + advisor chain per method. Negligible vs DB. Don’t put aspects on tight loops or `hashCode`.

---

### Q14. Can filters be replaced by AOP?

**Answer:** No. Filters see all servlets and run before handler mapping. AOP on controllers misses unmapped requests and static resources.

---

### Q15. What join points does Spring AOP support?

**Answer:** Method execution on Spring beans. That is the list.
