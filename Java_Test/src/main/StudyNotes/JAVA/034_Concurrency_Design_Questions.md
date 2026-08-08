# 34. Concurrency Design Questions

## 34. Concurrency Design Questions

### Frequently Asked in Product Companies

---

# 1. Design a thread-safe Singleton.

<details>
<summary>Show Answer</summary>

**Answer:**

A **thread-safe Singleton** ensures **only one instance** of a class exists—even when **many threads** try to create it at the same time during app startup.

### Layman Explanation

```text
One boss for the whole company:
  100 people try to "hire the boss" at same time
  Only ONE boss should exist — not 100 bosses
```

### Approach Comparison

| Approach | Thread-Safe? | Performance | Recommended |
|----------|-------------|-------------|-------------|
| Eager initialization | ✅ | Best | Simple apps |
| synchronized getInstance() | ✅ | Slow (lock every call) | Avoid |
| Double-checked locking | ✅ | Good | Legacy |
| **Bill Pugh (holder)** | ✅ | Best | ✅ Preferred |
| **Enum singleton** | ✅ | Best | ✅ Best for singletons |

### ✅ Bill Pugh — Holder Pattern (Recommended)

```java
public class DatabaseConfig {

    private DatabaseConfig() {
        // private constructor — nobody can create from outside
    }

    private static class Holder {
        private static final DatabaseConfig INSTANCE = new DatabaseConfig();
    }

    public static DatabaseConfig getInstance() {
        return Holder.INSTANCE; // JVM loads Holder only when called — thread-safe
    }

    public String getUrl() {
        return "jdbc:oracle:thin:@localhost:1521/db";
    }
}
```

**Why it works:** JVM guarantees class initialization is thread-safe. `Holder` class loads only when `getInstance()` is first called—lazy and safe.

### ✅ Enum Singleton (Best Practice)

```java
public enum AppConfig {
    INSTANCE;

    private final String appName = "OrderService";

    public String getAppName() {
        return appName;
    }
}

// Usage
AppConfig.INSTANCE.getAppName();
```

**Why enum:** Serialization-safe, reflection-safe, thread-safe by JVM—Joshua Bloch recommends this.

### Double-Checked Locking (Know for Interviews)

```java
public class Singleton {
    private static volatile Singleton instance;

    private Singleton() {}

    public static Singleton getInstance() {
        if (instance == null) {                    // first check — no lock
            synchronized (Singleton.class) {
                if (instance == null) {            // second check — inside lock
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```

`volatile` is **required** — without it, another thread may see partially constructed object.

### Architecture Answer (Interview Short)

```text
Production: Enum singleton or Bill Pugh holder
Spring apps: Don't write Singleton — use @Service bean (Spring manages one instance)
Connection pools (HikariCP): already singleton per datasource
```

**Interview Point:**

> Enum singleton = best. Bill Pugh holder = best for class-based. Double-checked locking needs volatile. Spring `@Service` replaces manual singleton in most apps.

</details>

---

# 2. Design a rate limiter.

<details>
<summary>Show Answer</summary>

**Answer:**

A **rate limiter** controls how many requests a user or API can make in a time window—prevents abuse, protects downstream systems, and ensures fair usage.

### Layman Explanation

```text
Highway toll booth:
  Only 100 cars allowed per minute
  Car 101 must wait until next minute
  Protects road from overload
```

### Common Algorithms

| Algorithm | Idea | Pros | Cons |
|-----------|------|------|------|
| **Token Bucket** | Tokens refill at fixed rate; request takes one token | Smooth, allows bursts | Slightly complex |
| **Fixed Window** | Count requests per minute window | Simple | Burst at window edge |
| **Sliding Window** | Rolling time window | Accurate | More memory |
| **Leaky Bucket** | Queue drips at fixed rate | Smooth output | Queue can grow |

### Token Bucket — Java Implementation

```java
public class TokenBucketRateLimiter {
    private final long capacity;       // max tokens (burst size)
    private final long refillRate;     // tokens per second
    private long tokens;
    private long lastRefillTime;

    public TokenBucketRateLimiter(long capacity, long refillRatePerSec) {
        this.capacity = capacity;
        this.refillRate = refillRatePerSec;
        this.tokens = capacity;
        this.lastRefillTime = System.nanoTime();
    }

    public synchronized boolean tryAcquire() {
        refill();
        if (tokens > 0) {
            tokens--;
            return true;  // allowed
        }
        return false;     // rate limited
    }

    private void refill() {
        long now = System.nanoTime();
        long elapsed = now - lastRefillTime;
        long tokensToAdd = (elapsed * refillRate) / 1_000_000_000L;
        if (tokensToAdd > 0) {
            tokens = Math.min(capacity, tokens + tokensToAdd);
            lastRefillTime = now;
        }
    }
}
```

### Using Guava (Production Shortcut)

```java
RateLimiter limiter = RateLimiter.create(100.0); // 100 permits per second

if (limiter.tryAcquire()) {
    processRequest();
} else {
    throw new RateLimitException("Too many requests");
}
```

### Distributed Rate Limiter — Redis

```text
Multiple app servers share ONE counter in Redis:

  Key: rate:user:123:minute:1709123400
  INCR key → if count > 100 → reject
  EXPIRE key 60 seconds

Tools: Redis + Lua script, Bucket4j, Resilience4j
```

### Architecture Answer

```text
Single server:     Guava RateLimiter or in-memory token bucket
Multi-server:      Redis sliding window or token bucket
API Gateway:       Kong, AWS API Gateway, Nginx limit_req
Per-user limits:   Key = userId or API key
Global limits:     Key = endpoint name
Response:          HTTP 429 Too Many Requests + Retry-After header
```

**Interview Point:**

> Token bucket allows bursts; fixed window is simpler but has edge spikes. Distributed = Redis counter. Return 429 with Retry-After. Guava/Resilience4j in Java apps.

</details>

---

# 3. Design a cache.

<details>
<summary>Show Answer</summary>

**Answer:**

A **thread-safe cache** stores frequently accessed data in memory for fast reads—must handle concurrent access, eviction when full, and optional expiration.

### Layman Explanation

```text
Restaurant menu on table:
  Waiter doesn't run to kitchen for every order
  Reads from menu (cache) — fast
  Menu updated when dish price changes (cache invalidation)
  Old menus thrown away when too many (eviction)
```

### Requirements for Production Cache

```text
✅ Thread-safe concurrent reads/writes
✅ Max size limit (eviction policy)
✅ Optional TTL (time-to-live)
✅ Cache miss → load from DB
✅ Metrics (hit rate, size)
```

### Simple Thread-Safe Cache — ConcurrentHashMap + TTL

```java
public class SimpleCache<K, V> {
    private static class Entry<V> {
        final V value;
        final long expiryTime;

        Entry(V value, long ttlMs) {
            this.value = value;
            this.expiryTime = System.currentTimeMillis() + ttlMs;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    private final ConcurrentHashMap<K, Entry<V>> map = new ConcurrentHashMap<>();
    private final long defaultTtlMs;

    public SimpleCache(long defaultTtlMs) {
        this.defaultTtlMs = defaultTtlMs;
    }

    public V get(K key) {
        Entry<V> entry = map.get(key);
        if (entry == null || entry.isExpired()) {
            map.remove(key);
            return null; // cache miss
        }
        return entry.value;
    }

    public void put(K key, V value) {
        map.put(key, new Entry<>(value, defaultTtlMs));
    }

    // Cache-aside pattern
    public V getOrLoad(K key, Function<K, V> loader) {
        V cached = get(key);
        if (cached != null) return cached;

        V loaded = loader.apply(key);
        if (loaded != null) put(key, loaded);
        return loaded;
    }
}
```

### LRU Cache — LinkedHashMap

```java
public class LruCache<K, V> extends LinkedHashMap<K, V> {
    private final int maxSize;

    public LruCache(int maxSize) {
        super(maxSize, 0.75f, true); // accessOrder = true for LRU
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize; // remove oldest when over limit
    }
}

// Wrap for thread safety:
Map<String, User> cache = Collections.synchronizedMap(new LruCache<>(1000));
// or use Caffeine (better)
```

### Production Libraries

| Library | Features |
|---------|----------|
| **Caffeine** | LRU, TTL, async, stats — ✅ preferred |
| **Guava Cache** | TTL, size limit, loading cache |
| **EhCache** | Distributed, off-heap |
| **Redis** | Distributed cache across servers |

### Caffeine — Production Pattern

```java
Cache<String, User> userCache = Caffeine.newBuilder()
    .maximumSize(10_000)
    .expireAfterWrite(Duration.ofMinutes(10))
    .recordStats()
    .build();

User user = userCache.get(userId, id -> userRepository.findById(id));
```

### Architecture Answer

```text
L1 (local):    Caffeine in each app instance — fastest
L2 (shared):   Redis — shared across instances
Pattern:       Cache-aside (read cache → miss → DB → write cache)
Eviction:      LRU or TTL
Invalidation:  On write/update → delete cache key
Danger:        Cache stampede — use single-flight or lock on miss
```

**Interview Point:**

> ConcurrentHashMap for simple cache. Caffeine for production local cache. Redis for distributed. Cache-aside pattern. LRU + TTL. Invalidate on update. Watch cache stampede on hot keys.

</details>

---

# 4. Design producer-consumer system.

<details>
<summary>Show Answer</summary>

**Answer:**

Design a system where **producers** generate work items and **consumers** process them through a **bounded queue**—with backpressure, monitoring, and graceful shutdown.

### Layman Explanation

```text
Pizza shop:
  Online orders (producers) → order queue (buffer) → chefs (consumers)
  Queue full → stop accepting orders temporarily (backpressure)
  Queue empty → chefs wait for new orders
```

### Architecture

```text
  [API / Kafka Consumer]  →  [Bounded BlockingQueue]  →  [Worker Pool]
       Producers                    Buffer                    Consumers
```

### Full Design — Order Processing

```java
@Service
public class OrderProcessingSystem {

    private final BlockingQueue<Order> queue = new ArrayBlockingQueue<>(1000);
    private final ExecutorService workers;
    private volatile boolean running = true;

    public OrderProcessingSystem() {
        this.workers = new ThreadPoolExecutor(
            5, 10, 60L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(50),
            new ThreadPoolExecutor.CallerRunsPolicy() // backpressure
        );
        startConsumers();
    }

    // Producer side — API receives order
    public boolean submitOrder(Order order) {
        boolean accepted = queue.offer(order, 2, TimeUnit.SECONDS);
        if (!accepted) {
            log.warn("Queue full — order rejected: {}", order.getId());
            return false; // tell client to retry
        }
        return true;
    }

    // Consumer side — workers pull from queue
    private void startConsumers() {
        for (int i = 0; i < 5; i++) {
            workers.submit(() -> {
                while (running) {
                    try {
                        Order order = queue.poll(1, TimeUnit.SECONDS);
                        if (order != null) {
                            processOrder(order);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }
    }

    private void processOrder(Order order) {
        paymentService.charge(order);
        inventoryService.reserve(order);
        notificationService.send(order);
    }

    @PreDestroy
    public void shutdown() {
        running = false;
        workers.shutdown();
        try {
            if (!workers.awaitTermination(30, TimeUnit.SECONDS)) {
                workers.shutdownNow();
            }
        } catch (InterruptedException e) {
            workers.shutdownNow();
        }
    }
}
```

### Key Design Decisions

| Decision | Choice | Why |
|----------|--------|-----|
| Queue type | `ArrayBlockingQueue` bounded | Prevents OOM |
| Queue full | `offer()` + timeout or reject | Backpressure |
| Workers | `ThreadPoolExecutor` | Reuse threads |
| Rejection | `CallerRunsPolicy` | Slows producer naturally |
| Shutdown | `shutdown()` + `awaitTermination` | No lost tasks |

### Distributed Version

```text
Producers → Kafka topic → Consumer group (multiple consumers)
  Durable, replayable, scales across servers
  Use when: multiple services, need durability, high volume
```

**Interview Point:**

> Bounded BlockingQueue + worker pool. offer() for backpressure. CallerRunsPolicy slows producers. @PreDestroy shutdown. Kafka for distributed scale.

</details>

---

# 5. Design file processing system using ExecutorService.

<details>
<summary>Show Answer</summary>

**Answer:**

Split large file processing into **parallel tasks** using `ExecutorService`—each task handles a chunk or one file, with controlled concurrency and error handling.

### Layman Explanation

```text
1000 documents to scan:
  Don't hire 1000 people (too many)
  Hire 10 workers (thread pool)
  Each worker picks next document from pile
  Manager tracks who finished, who failed
```

### Scenario — Process 10,000 Files in a Folder

```java
@Service
public class FileProcessorService {

    private final ExecutorService executor =
        new ThreadPoolExecutor(
            8, 8, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(100),
            new ThreadPoolExecutor.CallerRunsPolicy()
        );

    public ProcessingResult processDirectory(Path dir) throws Exception {
        List<Path> files = listFiles(dir);
        List<Future<FileResult>> futures = new ArrayList<>();

        for (Path file : files) {
            futures.add(executor.submit(() -> processFile(file)));
        }

        int success = 0, failed = 0;
        List<String> errors = new ArrayList<>();

        for (Future<FileResult> future : futures) {
            try {
                FileResult result = future.get(30, TimeUnit.SECONDS);
                if (result.isSuccess()) success++;
                else { failed++; errors.add(result.getError()); }
            } catch (TimeoutException e) {
                future.cancel(true);
                failed++;
                errors.add("Timeout");
            } catch (ExecutionException e) {
                failed++;
                errors.add(e.getCause().getMessage());
            }
        }

        return new ProcessingResult(success, failed, errors);
    }

    private FileResult processFile(Path file) {
        try {
            String content = Files.readString(file);
            String parsed = parse(content);
            saveToDatabase(file.getFileName().toString(), parsed);
            return FileResult.success();
        } catch (Exception e) {
            return FileResult.failure(e.getMessage());
        }
    }
}
```

### Large Single File — Chunk by Lines

```java
public void processLargeFile(Path file) throws Exception {
    List<String> lines = Files.readAllLines(file);
    int chunkSize = 1000;
    List<Future<Integer>> futures = new ArrayList<>();

    for (int i = 0; i < lines.size(); i += chunkSize) {
        List<String> chunk = lines.subList(i, Math.min(i + chunkSize, lines.size()));
        futures.add(executor.submit(() -> processChunk(chunk)));
    }

    int total = 0;
    for (Future<Integer> f : futures) {
        total += f.get();
    }
    log.info("Processed {} records", total);
}
```

### Architecture Answer

```text
Thread pool size:     CPU cores for CPU-bound, higher for I/O-bound
Queue:                Bounded — prevents memory blow-up
Timeout:              future.get(timeout) per task
Error handling:       Collect failures — don't fail entire batch
Progress:             AtomicInteger counter + periodic log
Shutdown:             shutdown + awaitTermination after batch
Very large files:     Stream lines (not readAllLines) + batch submit
Distributed:          Split files across pods, each pod runs ExecutorService
```

### Pool Size Guide

| Work Type | Pool Size |
|-----------|-----------|
| CPU-bound (parse, compress) | `cores` or `cores + 1` |
| I/O-bound (DB, network) | `cores * 2` to `cores * 4` |
| Mixed | Start with cores, tune with metrics |

**Interview Point:**

> ExecutorService + Future for parallel file processing. Bounded queue + CallerRunsPolicy. Per-task timeout. Collect errors per file. Stream large files don't readAllLines. Pool size = cores for CPU work.

</details>

---

# 6. Design asynchronous notification service.

<details>
<summary>Show Answer</summary>

**Answer:**

An **async notification service** sends emails, SMS, or push notifications **without blocking** the main request—orders complete fast, notifications happen in background.

### Layman Explanation

```text
Restaurant:
  Waiter gives you receipt immediately (main request done)
  Kitchen sends SMS "order confirmed" later (async notification)
  You don't wait at counter for SMS to arrive
```

### Architecture

```text
  Order Service
       │
       │ publish event (non-blocking)
       ▼
  [Message Queue / Kafka]
       │
       ▼
  Notification Service (consumers)
       ├── Email sender
       ├── SMS sender
       └── Push notification sender
```

### Simple In-App Design — ExecutorService + Queue

```java
@Service
public class NotificationService {

    private final BlockingQueue<Notification> queue =
        new LinkedBlockingQueue<>(5000);
    private final ExecutorService workers =
        Executors.newFixedThreadPool(4);

    @PostConstruct
    public void startWorkers() {
        for (int i = 0; i < 4; i++) {
            workers.submit(() -> {
                while (true) {
                    try {
                        Notification n = queue.take(); // blocks until available
                        send(n);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        }
    }

    // Called from order service — returns immediately
    public void notifyAsync(Notification notification) {
        boolean queued = queue.offer(notification);
        if (!queued) {
            log.error("Notification queue full — dropping: {}", notification);
            // or persist to DB for retry
        }
    }

    private void send(Notification n) {
        switch (n.getType()) {
            case EMAIL:   emailClient.send(n); break;
            case SMS:     smsClient.send(n); break;
            case PUSH:    pushClient.send(n); break;
        }
    }
}
```

### With Spring @Async

```java
@Service
public class NotificationService {

    @Async("notificationExecutor")
    public void sendOrderConfirmation(Order order) {
        emailClient.send(order.getUserEmail(), buildTemplate(order));
    }
}

@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean("notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(4);
        ex.setMaxPoolSize(8);
        ex.setQueueCapacity(500);
        ex.setThreadNamePrefix("notify-");
        ex.initialize();
        return ex;
    }
}
```

### Kafka-Based — Production Scale

```text
OrderService → Kafka topic "notifications" → NotificationService consumer group
  Benefits: durable, retry, multiple consumers, decoupled
  Dead letter topic for failed notifications after N retries
```

### Reliability Checklist

```text
✅ Idempotent sends (same notification ID — don't double-send)
✅ Retry with backoff (3 attempts)
✅ Dead letter queue for permanent failures
✅ Template rendering separate from sending
✅ Rate limit per channel (SMS provider limits)
✅ Don't block main transaction on notification failure
```

**Interview Point:**

> Async = queue or @Async or Kafka. Main request publishes event and returns. Workers send in background. Retry + dead letter for reliability. Idempotent notification IDs.

</details>

---

# 7. Design parallel API aggregator.

<details>
<summary>Show Answer</summary>

**Answer:**

A **parallel API aggregator** calls **multiple external APIs at the same time** (user profile + orders + payments) and combines results—much faster than calling them one by one.

### Layman Explanation

```text
Travel booking site:
  Instead of:  call flights (3s) → then hotels (3s) → then cars (3s) = 9s
  Do:         call all three at same time → combine = ~3s total
```

### Sequential vs Parallel

```text
Sequential:  API1 (200ms) → API2 (200ms) → API3 (200ms) = 600ms
Parallel:    API1 + API2 + API3 at same time = ~200ms
```

### CompletableFuture — Recommended

```java
@Service
public class UserDashboardAggregator {

    public DashboardResponse getDashboard(String userId) {
        CompletableFuture<UserProfile> profileFuture =
            CompletableFuture.supplyAsync(() -> userApi.getProfile(userId));

        CompletableFuture<List<Order>> ordersFuture =
            CompletableFuture.supplyAsync(() -> orderApi.getOrders(userId));

        CompletableFuture<PaymentSummary> paymentFuture =
            CompletableFuture.supplyAsync(() -> paymentApi.getSummary(userId));

        // Wait for all + combine
        return CompletableFuture
            .allOf(profileFuture, ordersFuture, paymentFuture)
            .thenApply(v -> new DashboardResponse(
                profileFuture.join(),
                ordersFuture.join(),
                paymentFuture.join()
            ))
            .join();
    }
}
```

### With Timeout — Production Safety

```java
public DashboardResponse getDashboardSafe(String userId) {
    ExecutorService executor = Executors.newFixedThreadPool(3);

    CompletableFuture<UserProfile> profile =
        CompletableFuture.supplyAsync(() -> userApi.getProfile(userId), executor)
            .orTimeout(2, TimeUnit.SECONDS)
            .exceptionally(ex -> UserProfile.empty());

    CompletableFuture<List<Order>> orders =
        CompletableFuture.supplyAsync(() -> orderApi.getOrders(userId), executor)
            .orTimeout(2, TimeUnit.SECONDS)
            .exceptionally(ex -> List.of());

    try {
        CompletableFuture.allOf(profile, orders).join();
        return new DashboardResponse(profile.join(), orders.join());
    } finally {
        executor.shutdown();
    }
}
```

### Architecture Answer

```text
Pattern:        Scatter-gather (fan-out → fan-in)
Tool:           CompletableFuture.allOf() or anyOf()
Timeout:        orTimeout() per call — don't let one slow API block all
Fallback:       exceptionally() → default/partial response
Circuit breaker: Resilience4j — stop calling failing API
Caching:          Cache slow APIs (Caffeine) to reduce calls
Thread pool:      Custom executor — don't use common ForkJoinPool for I/O
Partial response: Return what succeeded if one API fails (graceful degradation)
```

### Resilience4j Circuit Breaker

```java
CircuitBreaker cb = CircuitBreaker.of("paymentApi",
    CircuitBreakerConfig.custom()
        .failureRateThreshold(50)
        .waitDurationInOpenState(Duration.ofSeconds(30))
        .build());

Supplier<PaymentSummary> protectedCall =
    CircuitBreaker.decorateSupplier(cb, () -> paymentApi.getSummary(userId));
```

### Comparison Table

| Approach | Pros | Cons |
|----------|------|------|
| Sequential | Simple | Slow |
| CompletableFuture | Fast, composable | Complex error handling |
| Reactive (WebFlux) | Non-blocking I/O | Learning curve |
| API Gateway BFF | Centralized aggregation | Extra service |

**Interview Point:**

> CompletableFuture.supplyAsync for parallel calls. allOf() to wait. orTimeout() per API. exceptionally() for fallback. Circuit breaker for failing services. Custom executor for I/O-bound calls.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Singleton in Spring — need manual design?

<details>
<summary>Show Answer</summary>

**Answer:**

**No** — Spring `@Service` / `@Component` beans are **singleton scope by default**. Spring container creates one instance per bean definition. Manual singleton pattern is rarely needed in Spring apps.

</details>

---

### Q: Rate limiter — token bucket vs sliding window?

<details>
<summary>Show Answer</summary>

**Answer:**

**Token bucket** allows controlled bursts (tokens accumulate). **Sliding window** is stricter — counts exact requests in rolling time window. Use token bucket for APIs allowing bursts; sliding window for strict per-minute limits.

</details>

---

### Q: CompletableFuture thenApply vs thenCompose?

<details>
<summary>Show Answer</summary>

**Answer:**

`thenApply` transforms result synchronously (like `map`). `thenCompose` chains another async operation that returns CompletableFuture (like `flatMap`). Use `thenCompose` when next step is also async API call.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Singleton: enum or Bill Pugh. Rate limiter: token bucket + Redis distributed. Cache: Caffeine local + Redis shared. Producer-consumer: bounded queue + worker pool. Parallel API: CompletableFuture.allOf + timeout + fallback per call.

</details>
