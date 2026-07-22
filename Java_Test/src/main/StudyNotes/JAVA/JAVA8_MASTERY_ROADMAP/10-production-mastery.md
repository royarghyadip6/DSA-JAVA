# Phase 10 — Production Mastery

**Duration:** Ongoing | **Goal:** Senior-level design, anti-patterns, testing, migration, code review

[← Phase 9](09-jvm-platform.md) | [Index](README.md)

---

## Theory (Easy to Remember)

### 1. When Streams Make Code Worse

- **Don't** use streams for simple 3-line loops — readability suffers.
- **Don't** use parallel streams by default.
- **Don't** nest streams deeply — extract methods.
- **Do** use streams for: filter-map-collect pipelines, grouping, large data transforms.

**Rule:** If junior dev can't read it in 10 seconds, refactor.

---

### 2. Optional Anti-Patterns

| ❌ Bad | ✅ Good |
|--------|---------|
| `Optional` as field | Nullable field or empty object pattern |
| `Optional` as method parameter | Overloads, `@Nullable` |
| `optional.get()` without check | `orElse`, `map`, `ifPresent` |
| `Optional.of(nullable)` | `Optional.ofNullable` |
| `Optional` in serialization/DTO | Plain null or absent field |

---

### 3. Exception Handling in Streams

- Checked exceptions **don't work** in lambdas without wrapping.
- Patterns:
  - Try-catch inside lambda (ugly)
  - Wrapper method that catches and wraps in unchecked
  - Use library: Vavr `Try`, or custom `Function` wrapper

```java
@FunctionalInterface
interface ThrowingFunction<T, R> {
    R apply(T t) throws Exception;
    static <T, R> Function<T, R> wrap(ThrowingFunction<T, R> f) {
        return t -> {
            try { return f.apply(t); }
            catch (Exception e) { throw new RuntimeException(e); }
        };
    }
}
```

---

### 4. Testing Java 8 Code

| What | How |
|------|-----|
| Streams | Assert on collected result, not intermediate |
| Optional | Assert `isPresent`, value via `orElseThrow` in test |
| Time | Inject `Clock.fixed()` |
| Async | `CompletableFuture` with timeout in test; use `join` with test executor |
| Parallel | Deterministic tests: use sequential stream or fixed data |

---

### 5. Migration Playbook

| Legacy | Java 8 Replacement |
|--------|-------------------|
| `Date` / `Calendar` | `Instant`, `ZonedDateTime`, `LocalDate` |
| Anonymous SAM classes | Lambdas / method refs |
| for-loop filter/map | Stream pipeline |
| null checks | `Optional` (return types) |
| `StringBuffer` concat in loop | `StringJoiner`, `Collectors.joining` |
| sun.misc.BASE64 | `java.util.Base64` |
| Manual thread + Future | `CompletableFuture` |

**Strategy:** Migrate at boundaries first (API layer, DB layer). Don't big-bang entire monolith.

---

### 6. Code Review Checklist (Senior)

- [ ] No shared mutation in stream pipelines
- [ ] `Files.lines` in try-with-resources
- [ ] CompletableFuture uses appropriate executor
- [ ] Optional not abused in DTOs
- [ ] Parallel stream justified with comment/benchmark
- [ ] `toMap` has merge function if duplicates possible
- [ ] Timezone explicit at system boundaries
- [ ] No `get()` on Optional in production code

---

### 7. Design Patterns with Java 8

| Pattern | Java 8 Expression |
|---------|-------------------|
| Strategy | Lambda / method ref as strategy |
| Factory | `Supplier<T>` |
| Observer | `Consumer<T>` callback |
| Builder | Method refs + `Consumer<Builder>` setup |
| Null Object | `Optional.empty()` or default methods |
| Template Method | Default interface methods |

---

## Examples

### Clean vs over-engineered stream

```java
// Over-engineered
boolean hasAdmin = users.stream()
    .filter(Objects::nonNull)
    .map(User::getRole)
    .filter(Objects::nonNull)
    .anyMatch("ADMIN"::equals);

// Clear for simple case
boolean hasAdmin = false;
for (User u : users) {
    if (u != null && "ADMIN".equals(u.getRole())) {
        hasAdmin = true;
        break;
    }
}
// Both valid — choose based on team style and complexity
```

### Testable service with Clock

```java
class ExpiryChecker {
    private final Clock clock;
    ExpiryChecker(Clock clock) { this.clock = clock; }

    boolean isExpired(Instant expiry) {
        return Instant.now(clock).isAfter(expiry);
    }
}

@Test
void expired() {
    Clock fixed = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
  ExpiryChecker checker = new ExpiryChecker(fixed);
  assertTrue(checker.isExpired(Instant.parse("2025-01-01T00:00:00Z")));
}
```

### CompletableFuture with timeout (Java 8 pattern)

```java
ExecutorService executor = Executors.newSingleThreadExecutor();
Future<String> future = executor.submit(() -> slowCall());
try {
    String result = future.get(5, TimeUnit.SECONDS);
} catch (TimeoutException e) {
    future.cancel(true);
}
```

---

## Practice Exercises

| # | Exercise | Difficulty |
|---|----------|------------|
| 1 | Code review: find 5 anti-patterns in a provided Java 8 class | Medium |
| 2 | Refactor Date-based service to java.time with tests | Hard |
| 3 | Write ThrowingFunction wrapper and use in stream | Medium |
| 4 | Document team coding standards for Optional usage | Medium |
| 5 | Build end-to-end mini app using streams, Optional, CF, java.time | Hard |
| 6 | Performance test: parallel vs sequential on your hardware | Medium |
| 7 | Migrate anonymous Comparator usages across a module | Medium |

---

## Interview Q&A (5–8 Years Experience)

### Q1. When would you NOT use Java 8 streams?

**Answer:** Simple iterations, indexed access, early break with complex state, checked exception heavy code, very small collections, performance-critical tight loops where allocation matters. Streams add object overhead (lambda captures, Spliterator). Readability and maintainability trump "stream everything."

---

### Q2. How do you handle checked exceptions in streams?

**Answer:** Lambdas can't throw checked exceptions without handling. Options:
1. Wrap in try-catch → rethrow as unchecked (`RuntimeException` or custom)
2. `ThrowingFunction` utility wrapper
3. Don't use stream — use loop for IO-heavy checked exception code
4. Use `flatMap` with `Optional` for per-element failure (skip failures)

No single best — team convention matters.

---

### Q3. Design a thread-safe cache with Java 8 APIs.

**Answer:**
```java
ConcurrentHashMap<String, ExpensiveObject> cache = new ConcurrentHashMap<>();

public ExpensiveObject get(String key) {
    return cache.computeIfAbsent(key, this::loadExpensive);
}
```
`computeIfAbsent` is atomic per key. Watch for recursive computation on same key (deadlock risk). For time-based expiry, combine with `Caffeine` or scheduled cleanup — CHM alone doesn't expire.

---

### Q4. How do you debug a complex stream pipeline?

**Answer:**
1. Add `.peek(x -> log.debug("{}", x))` at stages
2. Run **sequential** first
3. Break into smaller methods with meaningful names
4. Replace terminal with `collect` and inspect intermediate list
5. IDE debugger: step through lambda (works but tedious)
6. Use `stream().limit(5)` during development

---

### Q5. Optional in REST API JSON — serialize or not?

**Answer:** Jackson 2+ can treat Optional as absent field if configured (`OPTIONAL` module). Default may serialize as object `{"present":true}` — bad. Configure:
- `JsonInclude.NON_ABSENT` 
- Or don't use Optional in DTOs — use null with `@JsonInclude(NON_NULL)`

Domain layer Optional OK; boundary layer often plain null.

---

### Q6. CompletableFuture in microservices — pitfalls?

**Answer:**
- Default pool exhaustion (use dedicated executors)
- No timeout → hanging requests (`get(timeout)`)
- Exception swallowed if not `handle`/`whenComplete`
- `join()` in servlet thread blocks Tomcat thread pool
- Cascading failures — add circuit breaker (Resilience4j), not raw CF alone
- Thread context (MDC logging) lost — use `TaskDecorator` or manual MDC copy

---

### Q7. How to migrate 1M line codebase to Java 8 style incrementally?

**Answer:**
1. Enable Java 8 compiler, no style change required
2. ESLint-equivalent: Sonar rules for Optional misuse, stream side effects
3. New code = Java 8 idioms; touch old code only when fixing bugs
4. Workshops on streams, java.time
5. Shared utilities: date converters, `ThrowingFunction`
6. Measure: don't mass-refactor loops to streams without value

---

### Q8. Explain a production bug you might introduce with parallelStream.

**Answer:** Example: `orders.parallelStream().forEach(o -> totals.merge(o.getCategory(), o.getAmount(), Double::sum))` where `totals` is `HashMap` — race condition, lost updates. Fix: `collect(groupingByConcurrent(..., summingDouble()))`. Another: thread-local `SimpleDateFormat` in parallel mapper — corruption. Another: order-dependent logic with `forEach` in parallel — non-deterministic results.

---

### Q9. Collector internals — what are the 4 functions?

**Answer:**
1. **Supplier** — `() -> new ArrayList<>()` creates accumulator
2. **Accumulator** — `(list, item) -> list.add(item)`
3. **Combiner** — `(list1, list2) -> { list1.addAll(list2); return list1; }`
4. **Finisher** — `list -> Collections.unmodifiableList(list)` (optional)

**Characteristics** guide optimization: `CONCURRENT`, `UNORDERED`, `IDENTITY_FINISH`.

---

### Q10. How do you stay current after Java 8?

**Answer:** Java 8 is baseline; production often on 11/17/21 LTS. Know what came after:
- **9–11:** modules, `var`, HTTP client, `List.of`
- **12–17:** records, sealed classes, pattern matching
- **21+:** virtual threads

Interview: "Strong Java 8 foundation; experienced with 11/17 in production" — map features to Java 8 concepts (e.g., records ≈ immutable data classes).

---

## Final Mastery Checklist

- [ ] I write idiomatic Java 8 without over-using streams
- [ ] I can defend architectural choices in code review
- [ ] I handle async, time, and null safely in production
- [ ] I can answer senior interview questions with real examples
- [ ] I've built at least one project using all major Java 8 APIs

**Congratulations!** Review [API Reference](API-REFERENCE.md) and revisit weak phases.

[← Back to Index](README.md)
