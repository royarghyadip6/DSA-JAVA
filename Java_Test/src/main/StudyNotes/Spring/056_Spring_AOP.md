# 56. Spring AOP

## 56. Spring AOP

## Very Frequently Asked

---

# 1. What is AOP?

<details>
<summary>Show Answer</summary>

**Answer:**

**Aspect-Oriented Programming (AOP)** is a programming paradigm that separates **cross-cutting concerns** (logging, security, transactions) from **business logic** — so you don't repeat the same code in every method.

### Without AOP

```java
public void createOrder(Order o) {
    log.info("Entering createOrder");     // repeated
    checkPermission();                     // repeated
    beginTransaction();                    // repeated
    // actual business logic
    commitTransaction();                   // repeated
    log.info("Exiting createOrder");       // repeated
}
```

### With AOP

```java
public void createOrder(Order o) {
    // only business logic — logging, security, tx handled by aspects
    orderRepo.save(o);
}
```

```text
AOP = write cross-cutting logic ONCE in an Aspect
      apply it to MANY methods via Pointcuts
```

**Interview Point:**

> AOP = separate cross-cutting concerns from business code. Spring AOP uses proxies to weave advice around your methods.

</details>

---

# 2. Why AOP?

<details>
<summary>Show Answer</summary>

**Answer:**

| Problem Without AOP | AOP Solution |
|---------------------|--------------|
| Logging copied in every method | One logging aspect |
| Transaction boilerplate everywhere | `@Transactional` aspect |
| Security checks duplicated | Security aspect |
| Hard to change — update 100 methods | Change one aspect |
| Business code mixed with infrastructure | Clean separation |

```java
@Aspect
@Component
public class LoggingAspect {
    @Before("execution(* com.app.service.*.*(..))")
    public void logBefore(JoinPoint jp) {
        log.info("Calling: {}", jp.getSignature().getName());
    }
}
```

```text
Benefits:
  DRY — Don't Repeat Yourself
  Single place to maintain cross-cutting logic
  Business methods stay focused and readable
```

**Interview Point:**

> AOP avoids code duplication for logging, transactions, security, caching. Change once, apply everywhere via pointcuts.

</details>

---

# 3. Cross-cutting concerns?

<details>
<summary>Show Answer</summary>

**Answer:**

**Cross-cutting concerns** are functionality that **spans multiple layers/modules** — not belonging to one class but needed everywhere.

| Cross-Cutting Concern | Example |
|-----------------------|---------|
| **Logging** | Log every service method entry/exit |
| **Transaction management** | Begin/commit/rollback DB transactions |
| **Security** | Check roles before method execution |
| **Caching** | Cache method return values |
| **Performance monitoring** | Measure execution time |
| **Exception handling** | Global error wrapping |

```text
Business concern:     "Create order" — belongs to OrderService
Cross-cutting concern: "Log this call" — needed in OrderService,
                       PaymentService, UserService, everywhere
```

**Interview Point:**

> Cross-cutting = logic that cuts across many classes/layers. AOP's main target — extract it into aspects.

</details>

---

## Terminologies

---

# 4. Aspect

<details>
<summary>Show Answer</summary>

**Answer:**

An **Aspect** is a module that encapsulates **cross-cutting logic** — combines **advice** (what to do) with **pointcuts** (where to apply).

```java
@Aspect      // marks this class as an aspect
@Component
public class PerformanceAspect {

    @Around("execution(* com.app.service.*.*(..))")  // pointcut + advice
    public Object measureTime(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = pjp.proceed();
        log.info("{} took {}ms", pjp.getSignature(), System.currentTimeMillis() - start);
        return result;
    }
}
```

```text
Aspect = Advice + Pointcut combined
@Component makes it a Spring bean
@Aspect enables AOP processing
```

**Interview Point:**

> Aspect = class with cross-cutting logic. Annotated @Aspect + @Component. Contains advice methods with pointcut expressions.

</details>

---

# 5. Advice

<details>
<summary>Show Answer</summary>

**Answer:**

**Advice** is the **action** taken by an aspect at a **join point** — the actual code that runs (log, check security, start transaction).

```java
@Aspect
@Component
public class AuditAspect {

    @Before("execution(* com.app.repo.*.save*(..))")
    public void beforeSave(JoinPoint jp) {
        log.info("About to save: {}", jp.getArgs());  // this IS the advice
    }

    @AfterReturning(pointcut = "execution(* com.app.service.*.*(..))", returning = "result")
    public void afterSuccess(JoinPoint jp, Object result) {
        log.info("Method {} returned {}", jp.getSignature().getName(), result);
    }
}
```

| Advice Type | When It Runs |
|-------------|--------------|
| @Before | Before method |
| @After | After method (finally) |
| @AfterReturning | After successful return |
| @AfterThrowing | After exception |
| @Around | Wraps entire method |

**Interview Point:**

> Advice = what to do (the code). Five types: Before, After, AfterReturning, AfterThrowing, Around.

</details>

---

# 6. Join Point

<details>
<summary>Show Answer</summary>

**Answer:**

A **Join Point** is a **point in program execution** where an aspect **can** be applied — in Spring AOP, this is always a **method execution**.

```java
public class OrderService {
    public Order createOrder(OrderRequest req) { }  // ← join point (method execution)
    public void cancelOrder(Long id) { }              // ← another join point
}
```

```text
Join Point = specific moment where aspect CAN hook in
Spring AOP supports: method execution only (not field access, constructors)
AspectJ (full AOP) supports more join point types
```

**Interview Point:**

> Join point = candidate execution point. Spring AOP = method execution only. Represented by JoinPoint parameter in advice methods.

</details>

---

# 7. Pointcut

<details>
<summary>Show Answer</summary>

**Answer:**

A **Pointcut** is an **expression** that **selects which join points** advice applies to — filters from all possible join points.

```java
// Pointcut expression — selects all public methods in service package
@Before("execution(public * com.app.service.*.*(..))")
public void logServiceCalls() { }

// Named pointcut — reusable
@Pointcut("within(com.app.service..*)")
public void serviceLayer() { }

@Before("serviceLayer()")
public void beforeService() { }
```

### Common Pointcut Expressions

| Expression | Meaning |
|------------|---------|
| `execution(* com.app.service.*.*(..))` | All methods in service package |
| `within(com.app.service..*)` | All methods in service and sub-packages |
| `@annotation(com.app.Audited)` | Methods with @Audited annotation |
| `bean(orderService)` | Specific bean by name |

**Interview Point:**

> Pointcut = filter defining WHERE advice runs. Uses AspectJ expression language. `execution()` most common.

</details>

---

# 8. Weaving

<details>
<summary>Show Answer</summary>

**Answer:**

**Weaving** is the process of **linking aspects with target objects** — applying advice to join points to create the final running code.

| Weaving Type | When | Used By |
|--------------|------|---------|
| **Compile-time** | During compilation | AspectJ compiler |
| **Load-time** | When class loaded into JVM | AspectJ LTW |
| **Runtime (proxy)** | At runtime via proxies | **Spring AOP** |

```text
Spring AOP weaving at RUNTIME:
  1. Spring creates proxy around your bean
  2. Client calls proxy (not real object)
  3. Proxy runs advice → then calls real method
  4. Returns result to client
```

```java
OrderService proxy = (OrderService) context.getBean("orderService");
// proxy internally calls LoggingAspect + real OrderService
```

**Interview Point:**

> Weaving = connecting aspects to code. Spring AOP weaves at runtime using JDK or CGLIB proxies — not compile-time like full AspectJ.

</details>

---

## Types of Advice

---

# 9. Before

<details>
<summary>Show Answer</summary>

**Answer:**

**@Before** advice runs **before** the join point method executes — cannot prevent execution (unless it throws exception).

```java
@Aspect
@Component
public class SecurityAspect {

    @Before("@annotation(RequiresRole)")
    public void checkRole(JoinPoint jp) {
        if (!SecurityContext.hasRole("ADMIN")) {
            throw new AccessDeniedException("Admin only");
        }
    }
}
```

```text
Timeline:  @Before advice → target method → (return or throw)
Use for:   validation, logging entry, security checks
```

**Interview Point:**

> @Before = runs before method. Good for validation, auth checks, logging. Cannot modify return value.

</details>

---

# 10. After

<details>
<summary>Show Answer</summary>

**Answer:**

**@After** advice runs **after** the join point method — **whether it succeeds or throws** (like `finally` block).

```java
@Aspect
@Component
public class CleanupAspect {

    @After("execution(* com.app.service.*.*(..))")
    public void afterMethod(JoinPoint jp) {
        MDC.clear();  // always cleanup thread-local, even on exception
        log.debug("Finished: {}", jp.getSignature().getName());
    }
}
```

```text
Timeline:  target method (success or fail) → @After advice (always runs)
Similar to: finally block
```

**Interview Point:**

> @After = finally-style advice. Always runs after method, success or exception. Use for cleanup.

</details>

---

# 11. AfterReturning

<details>
<summary>Show Answer</summary>

**Answer:**

**@AfterReturning** advice runs **after** the method **returns successfully** — can access the return value.

```java
@Aspect
@Component
public class AuditAspect {

    @AfterReturning(
        pointcut = "execution(* com.app.service.OrderService.createOrder(..))",
        returning = "order"
    )
    public void logCreatedOrder(JoinPoint jp, Order order) {
        auditLog.record("Order created: " + order.getId());
    }
}
```

```text
Timeline:  target method → returns normally → @AfterReturning
NOT run if: method throws exception
Can access: return value via returning="paramName"
```

**Interview Point:**

> @AfterReturning = runs only on successful return. Access return value. Not called on exception.

</details>

---

# 12. AfterThrowing

<details>
<summary>Show Answer</summary>

**Answer:**

**@AfterThrowing** advice runs **after** the method **throws an exception** — can access the exception object.

```java
@Aspect
@Component
public class ErrorAspect {

    @AfterThrowing(
        pointcut = "execution(* com.app.service.*.*(..))",
        throwing = "ex"
    )
    public void logException(JoinPoint jp, Exception ex) {
        log.error("Error in {}: {}", jp.getSignature().getName(), ex.getMessage());
        alertService.sendAlert(ex);
    }
}
```

```text
Timeline:  target method → throws exception → @AfterThrowing
Use for:    error logging, alerting, metrics on failures
```

**Interview Point:**

> @AfterThrowing = runs only when method throws. Access exception via throwing="paramName". Good for error monitoring.

</details>

---

# 13. Around

<details>
<summary>Show Answer</summary>

**Answer:**

**@Around** is the **most powerful** advice — **wraps** the method entirely. You control **whether** and **when** the method runs via `proceed()`.

```java
@Aspect
@Component
public class TransactionAspect {

    @Around("@annotation(Timed)")
    public Object measure(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();  // call actual method
            return result;
        } finally {
            log.info("{} took {}ms", pjp.getSignature(), System.currentTimeMillis() - start);
        }
    }
}
```

```text
@Around can:
  ✅ Run code BEFORE and AFTER method
  ✅ Skip method entirely (don't call proceed())
  ✅ Modify arguments before proceed()
  ✅ Modify return value after proceed()
  ✅ Catch and handle exceptions
```

**Interview Point:**

> @Around = full control. Must call pjp.proceed() to invoke target method. Used for transactions, caching, performance timing.

</details>

---

## Advanced

---

# 14. How Spring AOP works internally?

<details>
<summary>Show Answer</summary>

**Answer:**

```text
1. Spring scans for @Aspect beans at startup
2. Parses pointcut expressions — finds matching beans/methods
3. For each target bean, creates a PROXY (JDK or CGLIB)
4. Proxy implements/extends target + intercepts method calls
5. On method call → interceptor chain runs advice → then target method
6. Client always gets proxy, never raw object (when AOP applies)
```

```java
// What you write
@Service
public class OrderService {
    public Order create(Order o) { return repo.save(o); }
}

// What Spring gives callers (simplified)
OrderService proxy = new OrderServiceProxy(realOrderService, [loggingAspect, txAspect]);
proxy.create(order);  // runs aspects first, then real method
```

### Key Components

| Component | Role |
|-----------|------|
| **AspectJAutoProxyCreator** | Creates proxies for advised beans |
| **Advisor** | Combines pointcut + advice |
| **MethodInterceptor** | Invokes advice chain |
| **Proxy** | Wrapper around target bean |

**Interview Point:**

> Spring AOP = runtime proxy weaving. @Aspect beans registered → proxy created → advice runs before/after/around target method via interceptor chain.

</details>

---

# 15. JDK Dynamic Proxy vs CGLIB?

<details>
<summary>Show Answer</summary>

**Answer:**

Spring creates **two types of proxies** depending on the target class:

| | JDK Dynamic Proxy | CGLIB Proxy |
|---|-------------------|-------------|
| **Requires** | Target implements interface | Concrete class (no interface needed) |
| **How** | Proxy implements same interface | Subclass of target class |
| **Method type** | Only interface methods | Can proxy concrete methods |
| **Limitation** | Needs interface | Cannot proxy `final` classes/methods |
| **Performance** | Slightly faster creation | Slightly slower creation |
| **Package** | `java.lang.reflect.Proxy` | Third-party bytecode library |

```java
// Has interface → JDK proxy
public interface PaymentService { void pay(); }
@Service
public class StripePayment implements PaymentService { }

// No interface → CGLIB proxy
@Service
public class ReportService { }  // no interface → CGLIB subclass created
```

**Interview Point:**

> Interface present → JDK proxy. No interface → CGLIB subclass. `final` classes/methods cannot be proxied by CGLIB.

</details>

---

# 16. Which proxy gets created?

<details>
<summary>Show Answer</summary>

**Answer:**

Spring decides proxy type based on configuration and target class:

```text
Default (Spring Boot):
  Target has interface → JDK Dynamic Proxy
  Target has NO interface → CGLIB Proxy

Force CGLIB (application.properties):
  spring.aop.proxy-target-class=true
  → Always CGLIB, even if interface exists
```

```java
// JDK: proxy implements PaymentService
PaymentService proxy = context.getBean(PaymentService.class);

// CGLIB: proxy extends ReportService
ReportService proxy = context.getBean(ReportService.class);
```

| Scenario | Proxy Type |
|----------|------------|
| `@Service class implements UserService` | JDK (default) |
| `@Service class, no interface` | CGLIB |
| `proxy-target-class=true` | CGLIB always |
| `final` class | ❌ Cannot proxy |
| Self-invocation (`this.method()`) | ❌ AOP bypassed — no proxy |

**Interview Point:**

> Default: interface → JDK, no interface → CGLIB. Boot often sets proxy-target-class=true. Self-invocation skips AOP — common gotcha.

</details>

---

## Real World

---

# 17. Logging using AOP?

<details>
<summary>Show Answer</summary>

**Answer:**

Logging is the **most common AOP use case** — log method entry, exit, arguments, and execution time without touching business code.

```java
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("execution(* com.app.service..*.*(..))")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        String method = pjp.getSignature().toShortString();
        log.info("→ Entering: {} args={}", method, Arrays.toString(pjp.getArgs()));
        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            log.info("← Exiting: {} ({}ms) result={}", method,
                System.currentTimeMillis() - start, result);
            return result;
        } catch (Exception e) {
            log.error("✗ Exception in {}: {}", method, e.getMessage());
            throw e;
        }
    }
}
```

```text
Production tip: Use @Around for full control
Avoid logging in every service method manually
Combine with MDC for request tracing (correlation ID)
```

**Interview Point:**

> Logging via @Around aspect — one place for all service layer logs. Include args, time, exceptions. Real apps also use MDC for trace IDs.

</details>

---

# 18. Transaction management using AOP?

<details>
<summary>Show Answer</summary>

**Answer:**

Spring's `@Transactional` is **implemented using AOP** — an aspect wraps your method with begin/commit/rollback logic.

```java
@Service
public class TransferService {

    @Transactional  // AOP proxy handles transaction
    public void transfer(Long from, Long to, BigDecimal amount) {
        accountRepo.debit(from, amount);
        accountRepo.credit(to, amount);
        // if exception → automatic rollback
    }
}
```

```text
What @Transactional aspect does (@Around):
  1. Get/create transaction from TransactionManager
  2. Call pjp.proceed() — your method runs
  3. Success → commit
  4. RuntimeException → rollback
  5. Checked exception → rollback only if configured
```

| Without AOP | With @Transactional |
|-------------|---------------------|
| Manual begin/commit/rollback in every method | Declarative — one annotation |
| Easy to forget rollback | Consistent behavior |

**Interview Point:**

> @Transactional = AOP-based. Proxy wraps method with tx begin/commit/rollback. Self-invocation bypasses proxy — tx won't work on internal calls.

</details>

---

# 19. Security using AOP?

<details>
<summary>Show Answer</summary>

**Answer:**

Spring Security uses AOP (and filters) to enforce **authentication and authorization** before method execution.

```java
@Service
public class AdminService {

    @PreAuthorize("hasRole('ADMIN')")  // AOP checks role before method
    public void deleteUser(Long userId) {
        userRepo.deleteById(userId);
    }

    @Secured("ROLE_MANAGER")
    public void approveLeave(Long leaveId) { }
}
```

```java
// Custom security aspect
@Aspect
@Component
public class RoleCheckAspect {

    @Before("@annotation(RequiresAdmin)")
    public void checkAdmin() {
        if (!SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Admin role required");
        }
    }
}
```

```text
Spring Security flow:
  HTTP Request → Security Filter Chain → Authentication
  Method call → @PreAuthorize AOP → Authorization check → method runs
```

**Interview Point:**

> Security via @PreAuthorize/@Secured (Spring Security AOP) or custom @Before aspect. Checks roles/permissions before method executes.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Self-invocation and AOP?

<details>
<summary>Show Answer</summary>

**Answer:**

Calling `this.method()` inside same class **bypasses proxy** — `@Transactional`, `@Cacheable`, custom aspects won't apply. Fix: inject self, use `AopContext.currentProxy()`, or refactor to another bean.

</details>

---

### Q: Can AOP advise private methods?

<details>
<summary>Show Answer</summary>

**Answer:**

**No** — Spring AOP proxies intercept **public** methods only (proxy limitation). Private methods are not advised.

</details>

---

### Q: @Transactional is AOP?

<details>
<summary>Show Answer</summary>

**Answer:**

**Yes** — `TransactionInterceptor` is an AOP advice. `@Transactional` triggers proxy-based transaction management.

</details>

---

### Q: AspectJ vs Spring AOP?

<details>
<summary>Show Answer</summary>

**Answer:**

**Spring AOP** = proxy-based, method execution only, easier setup. **AspectJ** = compile/load-time weaving, more join points (fields, constructors), more powerful but complex.

</details>

---

### Q: Most powerful advice type?

<details>
<summary>Show Answer</summary>

**Answer:**

**@Around** — full control before/after, can skip or modify method call via `proceed()`.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> AOP separates cross-cutting concerns (logging, tx, security) via aspects. Terms: Aspect (module), Advice (action), Join Point (method execution), Pointcut (where), Weaving (linking). Advice types: Before, After, AfterReturning, AfterThrowing, Around. Spring AOP uses runtime proxies — JDK (interface) or CGLIB (class). @Transactional and @PreAuthorize are real-world AOP.

</details>
