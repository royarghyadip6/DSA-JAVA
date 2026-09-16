# 14. Caching, `@Async`, and Scheduling

## Start here (simple English)

Three different features. All look like “just add an annotation.” All can hurt production if you stop there.

| Feature | Simple meaning | Everyday picture |
|---------|----------------|------------------|
| **Cache** `@Cacheable` | Remember the answer so you don’t hit the DB every time | Sticky note on the fridge |
| **Async** `@Async` | “Do this later on another thread; don’t block the user” | Asking a colleague to send a mail while you keep talking |
| **Scheduled** `@Scheduled` | Run on a clock | Alarm every night at 2 a.m. |

**Same proxy rule as transactions:** `this.get(id)` will **not** use the cache or async. Call through another Spring bean.

**Three beginner traps:**

1. Cache in memory (Caffeine) on **12 pods** = **12 different sticky notes**. Updates on pod A, pod B still stale. Use Redis if pods must share.
2. `@Async` with **no thread pool** = Boot may start **unlimited threads** and freeze the machine.
3. `@Scheduled` on **12 pods** = the job runs **12 times**. Use a lock, Quartz cluster, or a Kubernetes CronJob.

Interview Q&A is **5–8 year standard**.

---

These three features look like annotations. They are **infrastructure**.

All of `@Cacheable` and `@Async` use **AOP proxies**. Self-invocation rules from chapter 08 apply.

---

## 1. Cache abstraction

Spring’s cache is a **facade**: `CacheManager` → Redis / Caffeine / Ehcache / ConcurrentMap.

```java
@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    CacheManager cacheManager() {
        CaffeineCacheManager m = new CaffeineCacheManager("orders", "products");
        m.setCaffeine(Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(Duration.ofMinutes(5)));
        return m;
    }
}
```

```java
@Service
public class ProductService {

    @Cacheable(cacheNames = "products", key = "#id")
    public Product get(long id) { return repo.findById(id).orElseThrow(); }

    @CachePut(cacheNames = "products", key = "#p.id")
    public Product update(Product p) { return repo.save(p); }

    @CacheEvict(cacheNames = "products", key = "#id")
    public void delete(long id) { repo.deleteById(id); }

    @CacheEvict(cacheNames = "products", allEntries = true)
    public void evictAll() {}
}
```

| Annotation | Effect |
|------------|--------|
| `@Cacheable` | Return cached value if key exists; else call method and store |
| `@CachePut` | Always call method; put result in cache |
| `@CacheEvict` | Remove key (or all) |
| `@Caching` | Combine several |

**Key:** SpEL. Default is all parameters. `SimpleKey` of no-args is `SimpleKey.EMPTY`.

**`unless` / `condition`:**

```java
@Cacheable(cacheNames = "products", key = "#id", unless = "#result == null")
```

Don’t cache `null` unless you intend “negative caching”.

### Local vs distributed

| | Caffeine (in-heap) | Redis |
|--|--------------------|-------|
| Speed | Fastest | Network hop |
| Multi-pod | **Each pod has its own cache** | Shared |
| Evict from one instance | Others stale | Visible to all |

If you scale to 2+ pods and cache **writes**, use Redis (or a cache that talks to all nodes). Otherwise evict on pod A, pod B still serves old data.

**Stampede:** 100 threads miss the same key → 100 DB queries. Caffeine has refresh/lock options; Redis + single-flight is a known pattern. Don’t ignore it for hot keys.

`@Cacheable` is **not** Hibernate L2 cache. Different layer.

### Pitfalls

- Mutating a cached mutable object (everyone shares the reference in Caffeine)
- Key missing tenant id in a multi-tenant app
- Caching user-specific data under a global key
- No TTL → memory leak
- `@Cacheable` on `this.get(id)` inside the same class

---

## 2. `@Async`

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean
    ThreadPoolTaskExecutor appExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(8);
        ex.setMaxPoolSize(16);
        ex.setQueueCapacity(500);
        ex.setThreadNamePrefix("app-async-");
        ex.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        ex.initialize();
        return ex;
    }
}
```

```java
@Async("appExecutor")
public CompletableFuture<Void> sendEmail(Email e) {
    mail.send(e);
    return CompletableFuture.completedFuture(null);
}
```

**Default executor** if you only write `@EnableAsync`: `SimpleAsyncTaskExecutor` — **new thread per task**, no bound. That can take the JVM down.

Return types: `void` (exceptions go to `AsyncUncaughtExceptionHandler` — **easy to miss**), `Future`, `CompletableFuture`. Prefer `CompletableFuture` and handle errors.

**Self-invocation:** `this.sendEmail()` is **synchronous**.

**TX:** the async method is a **new thread** → new TX if annotated. The caller may have already committed. Pass **ids**, not managed entities.

**Security context:** wrap executor with `DelegatingSecurityContextAsyncTaskExecutor` if you need the principal.

**MDC / trace:** wrap with a `TaskDecorator` that copies MDC.

WebMVC: don’t `@Async` the controller method and return immediately unless you know `DeferredResult` / `CompletableFuture` return types. Fire-and-forget from a **service** after you have the request data.

Virtual threads (Boot 3.2+, Java 21): `spring.threads.virtual.enabled=true` changes Tomcat’s thread model. It does **not** automatically make `@Async` virtual. You can use a virtual-thread executor for blocking I/O tasks. Don’t pool virtual threads like platform threads.

---

## 3. `@Scheduled`

```java
@Configuration
@EnableScheduling
public class ScheduleConfig {}

@Component
public class ExpireHoldsJob {
    @Scheduled(cron = "0 */5 * * * *", zone = "UTC")
    public void expire() { ... }
}
```

`fixedRate` vs `fixedDelay` vs `cron`:

- `fixedRate` — from start to start (can overlap if the job is slow — **disable overlapping** with a lock)
- `fixedDelay` — from **end** to next start
- `cron` — calendar

**One JVM, one scheduler.** Twelve pods = **twelve jobs**. Fix:

- ShedLock (`@SchedulerLock`)
- Quartz clustered
- Kubernetes `CronJob` (one runner)
- Leader election

Scheduler thread pool default size is **1**. A stuck job blocks all others:

```yaml
spring:
  task:
    scheduling:
      pool:
        size: 4
```

Don’t `@Scheduled` a method that calls a remote HTTP API without timeout and without a lock.

`@Transactional` on `@Scheduled`: the scheduler calls through the proxy if the method is public on a Spring bean — TX **does** start. Keep the job short.

---

## 4. Production pitfalls

1. Default `@Async` executor, production load, thread explosion.
2. Cache on multi-instance with Caffeine only.
3. Scheduled jobs in every replica.
4. Swallowing exceptions in `void @Async`.
5. Caching methods with current-user security inside — first user wins the cache.
6. `fixedRate` overlapping on a 1-thread scheduler.

---

# Interview Q&A (5–8 year bar)

A fresher knows `@Cacheable` and `@Scheduled`. A 5–8 year answer covers stampede, default async executor, and clustered schedulers.

### Q1. How does `@Cacheable` work?

**Answer:** AOP interceptor: compute key, `Cache.get`, on miss call method and `put`. Needs `@EnableCaching` and a `CacheManager`.

**Counter:** Why didn’t it cache?  
**Answer:** Self-invocation, different key SpEL, `unless` dropped it, or wrong cache name.

---

### Q2. Caffeine vs Redis?

**Answer:** Local vs distributed. Multiple pods + mutable data → Redis (or accept staleness).

---

### Q3. `@CachePut` vs `@Cacheable`?

**Answer:** `Cacheable` may skip the method. `CachePut` always runs the method, then updates the cache.

---

### Q4. Is Spring Cache Hibernate L2?

**Answer:** No. Spring Cache sits on service methods. L2 sits on the SessionFactory.

---

### Q5. Default `@Async` executor problem?

**Answer:** `SimpleAsyncTaskExecutor` is unbounded. Always declare a pool with queue + rejection policy.

**Counter:** `CallerRunsPolicy`?  
**Answer:** Caller thread runs the task when the queue is full — backpressure, may block Tomcat threads. Better than silently dropping or OOMing.

---

### Q6. Why is `@Async` running on the caller thread?

**Answer:** Self-invocation, missing `@EnableAsync`, or method not `public`.

---

### Q7. How do you handle `@Async` exceptions?

**Answer:** Return `CompletableFuture` and handle in the caller, or implement `AsyncUncaughtExceptionHandler` for `void`. Log + metric.

---

### Q8. Twelve pods run `@Scheduled` — how do you run once?

**Answer:** ShedLock, clustered Quartz, or move to k8s CronJob / a worker deployment with replicas=1.

---

### Q9. `fixedRate` vs `fixedDelay`?

**Answer:** Rate = period from start (overlap possible). Delay = wait after **finish**.

---

### Q10. Does `@Scheduled` go through the Spring proxy?

**Answer:** Yes if public on a bean. So `@Transactional` on that method usually works. Still don’t call it via `this` from another method if you need extra advisors.

---

### Q11. Cache stampede?

**Answer:** Many concurrent misses for one key. Use locking/single-flight, slightly randomized TTL, or Caffeine refreshAfterWrite.

---

### Q12. Can you cache a `Page<T>`?

**Answer:** You can; keys must include page/size/sort. Invalidation is painful. Often not worth it.

---

### Q13. Virtual threads + `@Async`?

**Answer:** Separate knobs. Enable virtual Tomcat for request threads. For `@Async`, plug an executor that uses virtual threads if tasks are blocking I/O. Don’t copy old pool sizes blindly.

---

### Q14. Security on a cached method?

**Answer:** Cache is unaware of Security. Either key includes user, or don’t cache personalized data, or authorize **before** cache (filter) so unauthorized never call the method.

---

### Q15. How do you test `@Async`?

**Answer:** Inject a sync executor in tests, or `await()` on `CompletableFuture`. Don’t assert too early.
