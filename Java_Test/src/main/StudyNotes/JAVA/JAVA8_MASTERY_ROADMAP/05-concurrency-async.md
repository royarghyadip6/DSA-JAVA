# Phase 5 — Concurrency & Async Programming

**Duration:** 2 weeks | **Goal:** Master CompletableFuture, new locks, and high-throughput counters

[← Phase 4](04-date-time-api.md) | [Index](README.md) | **Next:** [Phase 6 — Reflection & Annotations →](06-reflection-annotations.md)

---

## Theory (Easy to Remember)

### 1. CompletableFuture — Async Without Callback Hell

- **`CompletableFuture<T>`** = a result that will arrive **later**, and you can chain actions on it.
- Implements **`CompletionStage`** — fluent API for async pipelines.
- Can be created **already completed** or run async on thread pool.
- Default executor: **`ForkJoinPool.commonPool()`** (same as parallel streams).

**Memory trick:** *"Future you could only get(). CompletableFuture = Future + then-do-this."*

---

### 2. Key Methods (Learn in Groups)

| Group | Methods | Purpose |
|-------|---------|---------|
| Create | `supplyAsync`, `runAsync`, `completedFuture` | Start async work |
| Transform | `thenApply`, `thenApplyAsync` | T → U (like map) |
| Consume | `thenAccept`, `thenRun` | Use result, no return |
| Combine | `thenCombine`, `thenCompose`, `allOf`, `anyOf` | Merge futures |
| Handle errors | `exceptionally`, `handle`, `whenComplete` | Success or failure |
| Complete | `complete`, `join`, `get` | Finish or block |

---

### 3. thenCompose vs thenCombine

- **`thenCompose`** = flatMap for futures. One future depends on another (avoid `Future<Future<T>>`).
- **`thenCombine`** = combine two **independent** futures when both done.

```java
// thenCompose: second depends on first
future.thenCompose(result -> fetchDetails(result));

// thenCombine: parallel, merge results
future1.thenCombine(future2, (a, b) -> a + b);
```

---

### 4. StampedLock

- **Three modes:** write lock, read lock, **optimistic read**.
- **Optimistic read** = read without locking, then validate stamp — very fast for read-heavy workloads.
- **Not reentrant** — same thread can't lock twice.
- Use when: many reads, few writes, short critical sections.

---

### 5. LongAdder / DoubleAdder

- **High-throughput counters** under contention.
- Relaxed consistency: not for strict atomic sequences — for **statistics/metrics**.
- Better than `AtomicLong` when many threads increment frequently.

---

### 6. CountedCompleter

- **ForkJoinTask** variant for **completion-driven** workflows (e.g., IO pipelines).
- Triggers completion action when all subtasks done.
- Advanced — used inside JDK and high-performance frameworks.

---

## Examples

### Basic async chain

```java
CompletableFuture<String> future = CompletableFuture
    .supplyAsync(() -> fetchUserId())
    .thenApply(id -> fetchUserName(id))
    .thenApply(name -> "Hello, " + name);

String greeting = future.join(); // blocks until done
```

### Error handling

```java
CompletableFuture<Double> price = CompletableFuture
    .supplyAsync(() -> fetchPrice())
    .exceptionally(ex -> {
        log.error("Price fetch failed", ex);
        return 0.0; // fallback
    });
```

### Parallel API calls — allOf

```java
CompletableFuture<String> user = CompletableFuture.supplyAsync(() -> callUserApi());
CompletableFuture<String> orders = CompletableFuture.supplyAsync(() -> callOrdersApi());

CompletableFuture<Void> all = CompletableFuture.allOf(user, orders);
all.join();

String userData = user.join();
String orderData = orders.join();
```

### Custom executor (best practice for IO)

```java
ExecutorService pool = Executors.newFixedThreadPool(10);
CompletableFuture.supplyAsync(() -> httpCall(), pool)
    .whenComplete((r, ex) -> pool.shutdown());
```

### StampedLock optimistic read

```java
StampedLock lock = new StampedLock();
double x = 1.0, y = 2.0;

long stamp = lock.tryOptimisticRead();
double currentX = x, currentY = y;
if (!lock.validate(stamp)) {
    stamp = lock.readLock();
    try {
        currentX = x;
        currentY = y;
    } finally {
        lock.unlockRead(stamp);
    }
}
```

### LongAdder for metrics

```java
LongAdder requestCount = new LongAdder();
// many threads:
requestCount.increment();
long total = requestCount.sum();
```

---

## Practice Exercises

| # | Exercise | Difficulty |
|---|----------|------------|
| 1 | Chain 3 async steps: get ID → get profile → format JSON | Medium |
| 2 | Fetch 5 URLs in parallel; combine results with `allOf` | Medium |
| 3 | Add timeout handling (use `get(timeout, unit)` or `orTimeout` in Java 9+) | Hard |
| 4 | Implement retry (3 attempts) with `exceptionally` + recursion | Hard |
| 5 | Compare `AtomicLong` vs `LongAdder` under 10 threads (benchmark) | Medium |
| 6 | Use `StampedLock` for read-heavy cache | Hard |
| 7 | Avoid blocking common pool: dedicated executor for HTTP calls | Medium |
| 8 | `thenCompose` vs nested `thenApply` — refactor bad code | Medium |

---

## Interview Q&A (5–8 Years Experience)

### Q1. CompletableFuture vs Future — what problems does CF solve?

**Answer:** `Future.get()` is **blocking** with no composition. `CompletableFuture` adds:
- Non-blocking callbacks (`thenApply`, `whenComplete`)
- Chaining and combining multiple async operations
- Manual completion (`complete`)
- Exception propagation in pipeline

Foundation for reactive/async Java before Project Reactor.

---

### Q2. What thread runs `thenApply` vs `thenApplyAsync`?

**Answer:**
- **`thenApply`:** Runs on **same thread** that completed previous stage (or caller if already complete).
- **`thenApplyAsync`:** Runs on **default ForkJoinPool** (or supplied `Executor`).

For CPU work after IO, use `thenApplyAsync` with custom pool to avoid blocking common pool.

---

### Q3. Difference between `handle` and `exceptionally`?

**Answer:**
- **`exceptionally`:** Only called on failure; returns recovery value. Success path unchanged.
- **`handle`:** Called on **both** success and failure `(result, throwable)` — unified handler.

Use `handle` when one function should process both outcomes.

---

### Q4. What is `CompletionException`?

**Answer:** Wrapper thrown by `join()`/`get()` when async pipeline failed. **Cause** contains original exception (`getCause()`). Like `ExecutionException` for Future but unchecked.

---

### Q5. How do you combine 10 independent CompletableFutures?

**Answer:**
```java
List<CompletableFuture<String>> futures = urls.stream()
    .map(url -> CompletableFuture.supplyAsync(() -> fetch(url)))
    .toList();

CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
List<String> results = futures.stream().map(CompletableFuture::join).toList();
```

Or use custom aggregator / `CompletableFuture` reduction pattern.

---

### Q6. StampedLock vs ReadWriteLock?

**Answer:**
| ReadWriteLock | StampedLock |
|---------------|-------------|
| Reentrant | Not reentrant |
| Only pessimistic read | Optimistic read option |
| Well understood | Harder to use correctly |

`StampedLock` can be faster for read-heavy; misuse causes subtle bugs. Prefer `ReadWriteLock` unless profiling proves need.

---

### Q7. LongAdder vs AtomicLong?

**Answer:** `AtomicLong` = strict CAS on single variable — contention causes retries. `LongAdder` = **striped cells** — threads update different cells, sum at end. Higher throughput for increment-only; `sum()` not atomic snapshot in middle of updates (fine for metrics).

---

### Q8. What happens if `commonPool` is exhausted?

**Answer:** Tasks queue. Parallel streams, default `supplyAsync`, and `thenApplyAsync` **compete** for same pool. Blocking tasks stall all. **Solution:** dedicated `ExecutorService` for blocking IO; size pool appropriately.

---

### Q9. Is CompletableFuture truly non-blocking?

**Answer:** Callbacks are non-blocking, but **`join()`/`get()` block** calling thread. True non-blocking requires async runtime (Netty, reactive) or not blocking on CF at all. CF is "composition of async tasks" — not magic.

---

### Q10. How does ConcurrentHashMap improve in Java 8?

**Answer:** Replaced segment locks with **node-level CAS + synchronized bin head**. Added bulk ops: `forEach`, `search`, `reduce` (parallel over map). Better parallelism for reads and writes.

---

## Self-Check

- [ ] I use custom Executor for IO-bound CompletableFuture
- [ ] I know thenCompose vs thenCombine
- [ ] I can handle exceptions without swallowing them
- [ ] I understand LongAdder use case

**Next:** [Phase 6 — Reflection & Annotations →](06-reflection-annotations.md)
