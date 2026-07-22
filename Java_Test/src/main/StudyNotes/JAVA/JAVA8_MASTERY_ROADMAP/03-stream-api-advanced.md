# Phase 3 — Stream API Advanced

**Duration:** 1–2 weeks | **Goal:** Parallel streams, Spliterator, custom collectors, performance tuning

[← Phase 2](02-stream-api-core.md) | [Index](README.md) | **Next:** [Phase 4 — Date & Time API →](04-date-time-api.md)

---

## Theory (Easy to Remember)

### 1. Parallel Streams

- **Parallel stream** = pipeline may run on **multiple threads** (default: `ForkJoinPool.commonPool()`).
- Enable with: `collection.parallelStream()` or `stream.parallel()`.
- **Fork/Join** = split data → process chunks → combine results.

**Memory trick:** *"Parallel = many workers on one job. Only worth it for BIG jobs."*

---

### 2. When to Use Parallel Streams

| ✅ Good | ❌ Bad |
|---------|--------|
| Large data (10k+ elements) | Small lists (< 1000) |
| CPU-heavy, stateless ops | IO-bound (blocks common pool) |
| No order dependency | Strict ordering required |
| Associative reductions | Stateful side effects |

**Rule:** Measure first. Parallel has **split/merge overhead**.

---

### 3. Spliterator

- **Spliterator** = "splittable iterator" — feeds streams and supports parallel splitting.
- Key method: `trySplit()` — divide work for another thread.
- **Characteristics** flags: `SIZED`, `ORDERED`, `DISTINCT`, `SORTED`, `NONNULL`, `IMMUTABLE`, `CONCURRENT`

---

### 4. Stream Safety Rules

- **Non-interference:** Don't modify source while stream is running.
- **Stateless lambdas:** No mutable external state in parallel.
- **Associative reduce:** `(a op b) op c == a op (b op c)` for correct parallel reduce.

---

### 5. Custom Collector

Four parts of `Collector<T, A, R>`:
1. **Supplier** — create accumulator container
2. **Accumulator** — add one element
3. **Combiner** — merge two accumulators (parallel)
4. **Finisher** — transform result (optional)

**Characteristics:** `CONCURRENT`, `UNORDERED`, `IDENTITY_FINISH`

---

### 6. Performance Tips

- Use **primitive streams** (`IntStream`) to avoid boxing.
- Avoid `parallel()` on **IO** — starves `commonPool()`.
- Use **`unordered()`** when order doesn't matter (parallel boost).
- Prefer **`reduce`** over mutable `forEach` + external list.

---

## Examples

### Parallel sum (when it makes sense)

```java
long sum = LongStream.range(0, 10_000_000)
    .parallel()
    .sum();
```

### Wrong: side effect in parallel

```java
// BUG — race condition
List<Integer> result = new ArrayList<>();
IntStream.range(0, 1000).parallel().forEach(result::add);

// FIX
List<Integer> result = IntStream.range(0, 1000)
    .parallel()
    .boxed()
    .collect(Collectors.toList());
```

### Custom Collector — toImmutableList pattern

```java
public static <T> Collector<T, ?, List<T>> toImmutableList() {
    return Collector.of(
        ArrayList::new,
        List::add,
        (left, right) -> { left.addAll(right); return left; },
        Collections::unmodifiableList
    );
}
```

### Simple Spliterator for custom source

```java
Spliterator<String> spliterator = Spliterators.spliterator(
    Arrays.asList("a", "b", "c"),
    Spliterator.ORDERED | Spliterator.NONNULL
);
Stream<String> stream = StreamSupport.stream(spliterator, false);
```

### Concurrent reduction with groupingByConcurrent

```java
Map<String, List<Order>> byCity = orders.parallelStream()
    .collect(Collectors.groupingByConcurrent(Order::city));
```

---

## Practice Exercises

| # | Exercise | Difficulty |
|---|----------|------------|
| 1 | Benchmark sequential vs parallel sum on 10M integers | Medium |
| 2 | Fix a broken parallel pipeline that uses shared `HashMap` | Medium |
| 3 | Implement custom `Collector` that returns comma-separated string | Medium |
| 4 | Create stream from custom `Iterator` using `Spliterators.spliteratorUnknownSize` | Hard |
| 5 | Explain why `distinct()` is expensive in parallel | Medium |
| 6 | Use `IntSummaryStatistics` in one pass over prices | Easy |
| 7 | Build immutable `Set` collector with `Collector.of` | Hard |
| 8 | Demonstrate `unordered()` improving parallel `limit` performance | Hard |

---

## Interview Q&A (5–8 Years Experience)

### Q1. How do parallel streams work internally?

**Answer:** Source is split via `Spliterator.trySplit()` into chunks. Chunks are submitted to `ForkJoinPool` (usually common pool). Each chunk runs pipeline stages. Terminal ops like `reduce`/`collect` use **combiner** functions to merge partial results. Work-stealing balances load across threads.

---

### Q2. What is the common ForkJoinPool and what are the risks of blocking IO in parallel streams?

**Answer:** `ForkJoinPool.commonPool()` has `parallelism = CPU cores - 1`. Blocking IO in parallel stream ties up pool threads → **starvation** for all JVM users of common pool (including other parallel streams, `CompletableFuture.supplyAsync` without executor). **Fix:** use custom `ExecutorService` for IO; reserve parallel streams for CPU-bound work.

---

### Q3. Why might parallel stream be slower than sequential?

**Answer:**
- Split/merge overhead exceeds work per element (small data)
- Boxing, allocation pressure
- Ordered/stateful ops (`sorted`, `distinct`) need coordination
- Poor data splitting (bad Spliterator)
- False sharing, cache misses

Always benchmark with realistic data size.

---

### Q4. Explain Spliterator characteristics and why they matter.

**Answer:**
- **`SIZED`:** Known size → better splitting
- **`ORDERED`:** Encounter order defined
- **`CONCURRENT`:** Source can be modified concurrently (e.g., `ConcurrentHashMap`)
- **`IMMUTABLE`:** Source won't change

Stream implementation uses flags to optimize (e.g., skip buffer for `SIZED` + `SUBSIZED`).

---

### Q5. What makes a good combiner for `reduce`/`collect` in parallel?

**Answer:** Combiner must be **associative** and compatible with accumulator. For `Collectors.toList()`, combiner merges two lists. For `toMap`, merge maps with key collision handling. Wrong combiner = wrong results in parallel.

---

### Q6. Difference between `groupingBy` and `groupingByConcurrent`?

**Answer:**
- **`groupingBy`:** Thread-safe merge in parallel but not concurrent insertion into same map during accumulation (uses merge in combiner)
- **`groupingByConcurrent`:** Uses concurrent map; can insert concurrently in parallel if `CONCURRENT` characteristic satisfied and stream unordered

`groupingByConcurrent` faster for highly parallel + large grouping but non-deterministic inner list order.

---

### Q7. How do you write a thread-safe custom Collector?

**Answer:** Either:
1. Accumulator is thread-local, combiner merges (standard pattern)
2. Use `Collector.Characteristics.CONCURRENT` with thread-safe container (`ConcurrentHashMap`) and `UNORDERED` if order irrelevant

Don't use `ArrayList` with `CONCURRENT` unless each thread has separate list merged in combiner.

---

### Q8. What is non-interference? Give a violating example.

**Answer:** Don't modify stream source during pipeline execution (unless source is concurrent and CONCURRENT spliterator). Violation:

```java
List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
list.stream().forEach(s -> list.add(s)); // ConcurrentModificationException or infinite loop
```

---

### Q9. `reduce` vs `collect` — when to use which?

**Answer:**
- **`reduce`:** Immutable reduction — combine into single value (`sum`, `max`, string concat with caution)
- **`collect`:** **Mutable reduction** — build collection/StringBuilder/Map. More efficient for "gather into container" because mutates accumulator instead of creating new objects each step.

---

### Q10. How does `Stream.iterate` differ from `Stream.generate`?

**Answer:**
- **`iterate(seed, UnaryOperator)`:** seeded, deterministic sequence (0, 1, 2, …). Java 9 adds `iterate` with predicate for finite.
- **`generate(Supplier)`:** infinite, each element independent (random, UUID). No seed progression.

---

## Self-Check

- [ ] I can explain when NOT to use parallel streams
- [ ] I can fix race conditions in stream pipelines
- [ ] I understand Collector's 4 components
- [ ] I know common pool starvation risks

**Next:** [Phase 4 — Date & Time API →](04-date-time-api.md)
