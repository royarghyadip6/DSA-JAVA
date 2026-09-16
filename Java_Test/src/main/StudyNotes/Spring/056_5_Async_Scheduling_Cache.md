# 56.5 Async, Scheduling, and Cache Abstraction

[← 056_4 Spring Testing](056_4_Spring_Testing.md) | [Course map](00_COURSE_MAP.md) | [Back to 056 AOP](056_Spring_AOP.md)

These three share one fact: **they use a wrapper (proxy)** or a **timer thread**. `this.method()` still skips the extra behavior. Same as `@Transactional`.

Teaching is simple first. **Interview Q&A at the end is 5–8 year standard.**

---

## Simple first

| Annotation | Everyday meaning |
|------------|------------------|
| `@Async` | “Don’t wait. Run this on another thread.” |
| `@Scheduled` | “Run this on a timer.” |
| `@Cacheable` | “If I asked this before, return the saved answer.” |

All three must be on a **Spring bean**, called **from outside** (through the proxy), usually **public**.

**`@Async`:** the HTTP thread can return while email sends in the background. Default executor creates a **new thread every time** (no pool) — fine for a demo, bad in production. Always define a thread pool.

Exceptions from `void @Async` **do not** go back to the caller. They go to a handler (often only a log). If the caller must know, return `CompletableFuture`.

**`@Scheduled`:** default timer has **one thread**. One slow job blocks all other jobs. `fixedRate` = “try every 5 seconds even if the last run is still going” (can overlap). `fixedDelay` = “wait 5 seconds **after it finished**.”

**Twelve Kubernetes pods** = twelve timers. Your nightly job runs **twelve times** unless you lock or use a single worker.

**`@Cacheable`:** Spring is **not** Redis. It is a sticker. You plug in a **CacheManager** (a HashMap for tests, Redis/Caffeine in prod). Two app servers with a HashMap = two different memories (stale data). Put **tenant/user** in the cache key or you leak data.

---

## When you interview (5–8 years)

They pair this with AOP: `@Async` on the HTTP thread, overlapping schedules, cache misses from `this`, MDC on worker threads, TTL not on the annotation.

---

## 1. `@Async`

```java
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(4);
        ex.setMaxPoolSize(8);
        ex.setQueueCapacity(100);
        ex.setThreadNamePrefix("async-");
        ex.setTaskDecorator(new MdcTaskDecorator());
        ex.initialize();
        return ex;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> { /* log */ };
    }
}
```

```java
@Service
public class Notifier {
    @Async
    public void sendEmail(String to) { /* SMTP */ }

    @Async
    public CompletableFuture<String> fetch() {
        return CompletableFuture.completedFuture("ok");
    }
}
```

### Return types

| Return | Behavior |
|--------|----------|
| `void` | Fire and forget. Exceptions → `AsyncUncaughtExceptionHandler` (not the caller) |
| `Future` / `CompletableFuture` / `ListenableFuture` | Caller can `get()` / compose. Exceptions on `get()` |
| Other types | Illegal — the interceptor cannot bridge them |

The HTTP thread **returns immediately** for `void` / when you do not wait on the future. If the controller does `future.get()`, you blocked anyway — you only moved work, then waited.

### Executor vs default

Without a custom executor, Spring uses `SimpleAsyncTaskExecutor` (creates a **new thread per task**, no pool). Fine for demos; bad under load. Always define a pool in production.

Java 21: you may set a virtual-thread executor. Still do not share JDBC connections across tasks; each `@Async` with `@Transactional` gets its **own** transaction on that worker thread.

### Qualifier — several executors

```java
@Async("mailExecutor")
public void send() { }
```

`@EnableAsync` + multiple `Executor` beans: name them; `@Async("beanName")`.

### Same AOP limits

- Must be a Spring bean, public, called through proxy
- `this.sendEmail()` stays synchronous
- `final` methods not intercepted (CGLIB)
- `@Async` on a `@Transactional` method: **which interceptor is outer?** Order them. Typical: async **outer** (submit whole transactional method to the pool) **or** split: HTTP thread `@Transactional` then call another bean’s `@Async` after commit (`@TransactionalEventListener`). Combining both on one method is a design smell.

```text
If @Async is outer:
  HTTP thread submits
  worker starts tx, runs method, commits

If @Transactional is outer and then you call @Async inside:
  tx on HTTP thread; async work later may see committed data if AFTER_COMMIT
```

---

## 2. TaskDecorator and context

Worker threads do not inherit:

- `SecurityContext`
- MDC / logging correlation id
- Request attributes / request-scoped beans

```java
public class MdcTaskDecorator implements TaskDecorator {
    @Override
    public Runnable decorate(Runnable r) {
        Map<String, String> mdc = MDC.getCopyOfContextMap();
        return () -> {
            if (mdc != null) MDC.setContextMap(mdc);
            try {
                r.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
```

Security: `DelegatingSecurityContextAsyncTaskExecutor` (Security module) or copy the context yourself. Request scope: **do not** use request-scoped beans inside `@Async`; pass IDs as arguments.

---

## 3. Scheduling

```java
@Configuration
@EnableScheduling
public class SchedConfig implements SchedulingConfigurer {
    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        ThreadPoolTaskScheduler ts = new ThreadPoolTaskScheduler();
        ts.setPoolSize(4);
        ts.setThreadNamePrefix("sched-");
        ts.initialize();
        registrar.setTaskScheduler(ts);
    }
}
```

```java
@Component
public class Jobs {
    @Scheduled(fixedRate = 5000)
    public void poll() { }

    @Scheduled(fixedDelay = 5000)
    public void afterEach() { }

    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Kolkata")
    public void hourly() { }
}
```

| Mode | Meaning |
|------|---------|
| `fixedRate` | Next start = previous **start** + rate. Can **overlap** if the job runs longer than the rate **and** the pool has extra threads |
| `fixedDelay` | Next start = previous **end** + delay. No overlap of **that** method on one scheduler thread |
| `cron` | Cron expression (Spring 5.3+ 6-field: second minute hour day month weekday) |
| `initialDelay` | Wait before first run |

**Default scheduler is a single thread.** One slow job blocks all `@Scheduled` methods. Always set `poolSize` if you have more than one job — **or** accept that they queue.

**Overlap with `fixedRate` + poolSize > 1:** two executions of `poll()` at once. If they are not idempotent, you corrupt data. Fix: `shedlock` (library), a DB lock, `synchronized` (hurts), or `fixedDelay`.

Spring 6.1 / Boot 3.2: `spring.task.scheduling.pool.size` (Boot). Core: `SchedulingConfigurer` as above.

`@Scheduled` methods: no arguments (unless using a custom trigger / reactive — keep it simple). Exceptions are logged; the next trigger still fires (unless you stop the context).

`@Async` + `@Scheduled` on the same method: the scheduler thread only **submits**; overlap becomes even easier. Prefer one or the other.

Disable in tests: do not `@Import` the scheduling config, or use a profile `!test`.

---

## 4. Cache abstraction

Spring Cache is **not** a cache implementation. It is AOP around methods + a `CacheManager` SPI.

```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("orders");
    }
}
```

Production `CacheManager`s: Caffeine, Redis (`RedisCacheManager`), EhCache, JCache (JSR-107). `ConcurrentMapCacheManager` is a **local HashMap** — no eviction of size, not clustered.

```java
@Service
public class OrderQuery {
    @Cacheable(cacheNames = "orders", key = "#id")
    public Order find(long id) { return repo.find(id); }

    @CachePut(cacheNames = "orders", key = "#order.id")
    public Order save(Order order) { return repo.save(order); }

    @CacheEvict(cacheNames = "orders", key = "#id")
    public void delete(long id) { repo.delete(id); }

    @CacheEvict(cacheNames = "orders", allEntries = true)
    public void evictAll() { }
}
```

| Annotation | Meaning |
|------------|---------|
| `@Cacheable` | Lookup by key; on miss call method and put |
| `@CachePut` | Always call method, then put return value |
| `@CacheEvict` | Remove key (or `allEntries`) |
| `@Caching` | Combine several |
| `@CacheConfig` | Class-level cache names |

### Key, condition, unless

```java
@Cacheable(
    cacheNames = "orders",
    key = "#id",
    condition = "#id > 0",      // skip cache before call
    unless = "#result == null"  // do not store nulls
)
```

SpEL: `#p0`, `#id`, `#root.methodName`, `#result` (unless / put).

Default key (no `key` / `keyGenerator`): `SimpleKey` of all arguments. `find(1)` and `find(1, locale)` are different if both args exist.

### Same AOP limits

`@Cacheable` on `this.find(id)` from another method in the same class → **always miss**, always hits DB.

Nulls: by default may be cached (`unless` to stop). Cached nulls hide newly inserted rows until evict.

TTL is **not** on the annotation in Core. TTL is a property of the **cache implementation** (Caffeine spec, Redis `entryTtl`). Interviewers who ask “`@Cacheable(ttl=...)`” — that is not portable Core; it is Boot Redis config or a custom `CacheManager`.

### Consistency

Local cache + two app nodes = **two maps**. Writes on node A, reads on node B stale. Use Redis (or similar) for a shared cache, or accept eventual consistency, or evict via pub/sub.

`@CachePut` vs updating in place: the method **must return** the object you want stored.

`sync = true` on `@Cacheable` (Spring 4.3+): one thread loads on miss, others wait — stampede control **per JVM**, not cluster-wide.

---

## 5. `@Enable*` internals (one paragraph)

Each `@EnableAsync` / `@EnableScheduling` / `@EnableCaching` is `@Import` of a selector/registrar ([055](055_Spring_Annotations.md)). They register advisors (`AsyncAnnotationBeanPostProcessor`, `ScheduledAnnotationBeanPostProcessor`, `CacheInterceptor` + auto-proxy). `ScheduledAnnotationBeanPostProcessor` is **not** a method interceptor on each call — it **registers tasks** at startup by scanning beans for `@Scheduled`. That is why `@Scheduled` on a non-bean does nothing, and why self-invocation is irrelevant for *triggering* (the scheduler calls the method via reflection on the bean — **through the proxy if the bean is proxied**). If the scheduled method is `@Transactional` and the bean is proxied, the scheduler’s call **does** go through the proxy → tx applies. Good news compared to `this`.

---

## Production pitfalls

1. Default `SimpleAsyncTaskExecutor` unbounded threads.
2. QueueCapacity + AbortPolicy → rejected tasks in production.
3. Lost MDC / security on worker threads.
4. `@Async` + request-scoped beans.
5. Single-thread scheduler, one job blocks all.
6. `fixedRate` overlap, duplicate processing.
7. `@Cacheable` self-invocation.
8. Caching mutable entities then mutating the cached instance (especially JPA entities).
9. Local map cache in a horizontally scaled app.
10. Caching user-specific data with a key that is only `id` (cross-user leak) — include tenant/user in the key.
11. `@Scheduled` in every replica of a k8s deployment — 12 pods run the job 12 times (use ShedLock or a leader).

---

## Interview Ready Q&A (5–8 year standard)

The notes used timers and “saved answers.” **Here, name executors, overlap, ShedLock, cache stampede, and `sync = true` limits.**

### Q1. How does `@Async` work?

**Answer:** `@EnableAsync` registers an advisor. A call through the proxy submits a `Runnable`/`Callable` to a `TaskExecutor`. The target runs on a worker thread.

**Counter:** Why did it still run on the Tomcat thread?

**Counter-answer:** Self-invocation, missing `@EnableAsync`, method not public, or you called a non-proxied instance (`new`). Also: `@Async` on the same class as the caller.

---

### Q2. Who sees exceptions from `void @Async`?

**Answer:** Not the caller. `AsyncUncaughtExceptionHandler`. If you need errors at the call site, return `CompletableFuture` and handle it.

**Counter:** Default handler?

**Counter-answer:** Logs at error. Easy to miss. Set a handler that metrics + alerts.

---

### Q3. Default async executor — problem?

**Answer:** `SimpleAsyncTaskExecutor` = new thread per task, no bound. Under traffic you can create tens of thousands of threads.

**Counter:** What values do you set on `ThreadPoolTaskExecutor`?

**Counter-answer:** `core`/`max` sized to the **blocking** nature of the work (SMTP vs CPU), `queueCapacity` to absorb bursts, `threadNamePrefix` for dumps, `rejectedExecutionHandler` you have chosen (Abort vs CallerRuns). CallerRuns on a web thread can stall HTTP — know that trade-off.

---

### Q4. `@Async` and `@Transactional` on the same method?

**Answer:** Two advisors; order matters. Usually **split**: commit on the request thread, async after commit for I/O. If both on one method, the worker typically opens the transaction (async outer). Do not assume the HTTP thread’s tx is visible to the worker.

**Counter:** Request-scoped bean inside `@Async`?

**Counter-answer:** Scope not active. Pass primitives/IDs.

---

### Q5. How do you propagate MDC?

**Answer:** `TaskDecorator` copying MDC (and clearing in `finally`). Same idea for SecurityContext.

**Counter:** `@Async` on a listener already on a worker?

**Counter-answer:** You may double-hop. Decorate **every** executor you own (async, scheduling, MVC async).

---

### Q6. `fixedRate` vs `fixedDelay` vs `cron`?

**Answer:** Rate = from start to start (overlap possible). Delay = from end to start. Cron = calendar (set `zone`).

**Counter:** Job duration 10s, `fixedRate = 5s`, pool size 4. What happens?

**Counter-answer:** Overlapping executions. Need idempotency or a lock or switch to `fixedDelay`.

---

### Q7. Why did all my scheduled jobs stop when one hung?

**Answer:** Default scheduler has **one** thread. The hung job owns it.

**Counter:** poolSize = 4, still saw overlap on one job?

**Counter-answer:** That job’s `fixedRate` is shorter than runtime. Pool size fixes *other* jobs waiting, not overlap of the **same** method.

---

### Q8. Twelve Kubernetes pods and `@Scheduled`?

**Answer:** Twelve independent schedulers. Use ShedLock / Quartz clustered / a single worker deployment / Kubernetes CronJob.

**Counter:** Is Spring Cluster-aware?

**Counter-answer:** No. Spring scheduling is per JVM.

---

### Q9. Is `@Scheduled` AOP?

**Answer:** Registration is a BPP that finds annotations. **Invocation** is the scheduler calling the method on the bean (proxy). `@Transactional` on that method **does** apply. Self-invocation **inside** the scheduled method still skips extra advisors.

**Counter:** `@Scheduled` on a `@Configuration` `@Bean` instance you created with `new` inside the factory and returned — wait, it is a bean. Fine. On a helper you `new` inside the bean — **not** scheduled.

---

### Q10. What is Spring Cache?

**Answer:** An abstraction: `CacheManager` / `Cache` + interceptor. `@Cacheable` looks up a key; miss → method → put. Implementations are pluggable.

**Counter:** Is it EhCache?

**Counter-answer:** EhCache is one implementation. Core ships a concurrent map manager for tests. Production: Caffeine or Redis, chosen in config.

---

### Q11. Why didn’t `@Cacheable` cache?

**Answer:** Self-invocation; different key SpEL; `condition` false; `unless` dropped it; not a proxy; cache name not in the manager (some managers create lazily, some don’t).

**Counter:** First call cached `null`, second never hits DB even after insert?

**Counter-answer:** Null was stored. Use `unless = "#result == null"` or evict on insert.

---

### Q12. `@Cacheable` vs `@CachePut`?

**Answer:** Cacheable may skip the method. Put **always** runs the method then updates the cache (load-then-store on save).

**Counter:** Save method returns `void` with `@CachePut`?

**Counter-answer:** Nothing useful to store. Return the saved entity.

---

### Q13. Cache key for `find(User user)`?

**Answer:** Default `SimpleKey` uses `equals` of arguments. Unstable if `User` equals is identity-based. Prefer `key = "#user.id"`.

**Counter:** Multi-tenant?

**Counter-answer:** `key = "#tenant + ':' + #id"` or a `KeyGenerator` that reads tenant from a context. Missing tenant in the key is a data leak.

---

### Q14. Two nodes, `ConcurrentMapCacheManager`?

**Answer:** Each JVM has its own map. Stale reads after writes on the other node.

**Counter:** Fix?

**Counter-answer:** Central cache (Redis) or no cache or invalidation pub/sub. Do not pretend a HashMap is distributed.

---

### Q15. `sync = true` on `@Cacheable`?

**Answer:** Serializes loading of the same key in **this** cache instance (JVM). Prevents local stampede.

**Counter:** 20 nodes cold start?

**Counter-answer:** 20 loads still hit the DB. Need locking in Redis (`SETNX`) or accept stampede / use a warming job.

---

### Q16. Where is TTL configured?

**Answer:** On the `CacheManager` / native cache spec, not on `@Cacheable` in portable Core.

**Counter:** Boot Redis `spring.cache.redis.time-to-live`?

**Counter-answer:** Boot property that configures the Redis `CacheManager`. Still not an attribute of the annotation. You can have per-cache TTLs with a custom manager.

---

### Q17. Caching a JPA entity?

**Answer:** Risky: detached instances, lazy associations, mutating the cached object (it is shared). Prefer caching immutable DTOs.

**Counter:** Second-level Hibernate cache vs Spring `@Cacheable`?

**Counter-answer:** Different layers. L2 is persistence context / session factory. Spring Cache is method-level. Do not double-cache without a reason. L2 belongs to [061](061_Hibernate_Internals.md).

---

### Q18. How do you test `@Cacheable`?

**Answer:** Small `@EnableCaching` + `ConcurrentMapCacheManager` + service bean; call through the proxy twice; mock repo `times(1)`. Or unit-test without cache and integration-test the interceptor separately.

**Counter:** `@Mock` repository + `new Service(mock)`?

**Counter-answer:** No cache interceptor. Two calls = two mock hits. Proves nothing about caching.

---

### Q19. `@EnableCaching` `mode = AdviceMode.ASPECTJ`?

**Answer:** Weaves cache advice so self-invocation works. Needs AspectJ weaver. Same trade-off as transactions.

**Counter:** Worth it?

**Counter-answer:** Almost never. Split the method onto another bean.

---

### Q20. `@Async` return `CompletableFuture` that you never complete?

**Answer:** The interceptor may wrap your future. If you create a future and never complete it, callers hang on `get()`. Complete it (or use `supplyAsync` on **your** executor consistently). Mixing `supplyAsync(ForkJoinPool.commonPool())` with Spring’s executor splits thread policies — be explicit.

**Counter:** `void` vs future for emails?

**Counter-answer:** Email is fire-and-forget → `void` + metrics in the method + uncaught handler. Future if the caller must know success before responding.

---

### Interview one-liner

> `@Async`, `@Scheduled`, and `@Cacheable` are container features on top of executors and AOP. Define real thread pools; the defaults are not production. Self-invocation skips async/cache. `fixedRate` can overlap; default scheduler has one thread. Spring Cache is an SPI — TTL and clustering come from the `CacheManager`, not from the annotation. Copy MDC/security onto worker threads yourself.
