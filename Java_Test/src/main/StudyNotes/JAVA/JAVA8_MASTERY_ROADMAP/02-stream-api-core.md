# Phase 2 — Stream API Core

**Duration:** 2–3 weeks | **Goal:** Master streams, collectors, and Optional for everyday coding

[← Phase 1](01-language-foundations.md) | [Index](README.md) | **Next:** [Phase 3 — Stream API Advanced →](03-stream-api-advanced.md)

---

## Theory (Easy to Remember)

### 1. What Is a Stream?

- A **stream** is NOT a data structure — it's a **pipeline** for processing data.
- Think of it like a **conveyor belt**: items come from a source, pass through operations, and end at a terminal step.
- **Lazy** = intermediate operations don't run until a **terminal** operation triggers the pipeline.
- **Single-use** = once consumed, you need a new stream from the source.
- **Does not store data** — unlike `List` or `Set`.

**Memory trick:** *"Stream = recipe. Terminal operation = actually cooking."*

---

### 2. Creating Streams

| Source | How |
|--------|-----|
| Collection | `list.stream()`, `list.parallelStream()` |
| Array | `Arrays.stream(arr)` |
| Values | `Stream.of("a", "b", "c")` |
| Infinite | `Stream.iterate(0, n -> n + 1)`, `Stream.generate(Supplier)` |
| File | `Files.lines(path)` — **close it!** (use try-with-resources) |
| Primitives | `IntStream.range(0, 10)` |

---

### 3. Intermediate vs Terminal Operations

| Type | Returns | Runs When | Examples |
|------|---------|-----------|----------|
| **Intermediate** | New stream | Lazy (on terminal) | `filter`, `map`, `sorted`, `distinct` |
| **Terminal** | Result or void | Immediately | `collect`, `forEach`, `reduce`, `count` |

**Stateful intermediate ops** (`sorted`, `distinct`) may need to see all elements — memory cost.

---

### 4. Key Intermediate Operations

- **`filter(Predicate)`** — keep elements that match condition
- **`map(Function)`** — transform each element
- **`flatMap`** — flatten nested structures (`List<List<T>>` → `Stream<T>`)
- **`distinct()`** — remove duplicates (uses `equals`)
- **`sorted()`** / **`sorted(Comparator)`** — sort elements
- **`peek(Consumer)`** — debug/observe without changing (use carefully)
- **`limit(n)`** / **`skip(n)`** — take or skip elements

---

### 5. Key Terminal Operations

- **`collect(Collector)`** — most powerful; build List, Map, String, etc.
- **`reduce`** — combine all elements into one value
- **`forEach`** — do something with each (side effects — avoid in complex logic)
- **`count`**, **`min`**, **`max`**, **`findFirst`**, **`findAny`**
- **`anyMatch`**, **`allMatch`**, **`noneMatch`**
- **`toArray()`**

---

### 6. Collectors — Your Best Friend

| Collector | Result |
|-----------|--------|
| `toList()`, `toSet()` | Collection |
| `toMap(keyFn, valueFn)` | Map (watch duplicate keys!) |
| `groupingBy(classifier)` | `Map<K, List<T>>` |
| `partitioningBy(predicate)` | `Map<Boolean, List<T>>` |
| `joining(delimiter)` | String |
| `counting()`, `summingInt()`, `averagingDouble()` | Statistics |
| `summarizingInt()` | `IntSummaryStatistics` |

**Combinators:** `mapping`, `filtering`, `flatMapping`, `collectingAndThen`

---

### 7. Optional — Safe Null Handling

- **Never use `null` inside Optional** — use `Optional.empty()` instead.
- **Don't use Optional for fields/parameters** in most cases — use for **return types**.
- **Never call `get()` without checking** — use `orElse`, `orElseGet`, `map`, `flatMap`.
- **`orElse(default)`** = always evaluates default (even if value present)
- **`orElseGet(Supplier)`** = lazy default (only if empty)

**Memory trick:** *"Optional = maybe box. Open it safely or use orElse."*

---

## Examples

### Basic pipeline

```java
List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "Anna");

List<String> result = names.stream()
    .filter(n -> n.startsWith("A"))
    .map(String::toUpperCase)
    .sorted()
    .collect(Collectors.toList());
// [ALICE, ANNA]
```

### flatMap — one-to-many

```java
List<List<Integer>> nested = Arrays.asList(
    Arrays.asList(1, 2), Arrays.asList(3, 4));

List<Integer> flat = nested.stream()
    .flatMap(List::stream)
    .collect(Collectors.toList());
// [1, 2, 3, 4]
```

### groupingBy — SQL GROUP BY style

```java
record Order(String city, double amount) {}

List<Order> orders = List.of(
    new Order("NYC", 100), new Order("LA", 200), new Order("NYC", 150));

Map<String, Double> totalByCity = orders.stream()
    .collect(Collectors.groupingBy(
        Order::city,
        Collectors.summingDouble(Order::amount)));
// {NYC=250.0, LA=200.0}
```

### Optional chaining

```java
public Optional<String> getCityUpper(User user) {
    return Optional.ofNullable(user)
        .map(User::getAddress)
        .map(Address::getCity)
        .map(String::toUpperCase);
}

// Usage
String city = getCityUpper(user).orElse("UNKNOWN");
```

### toMap with merge function

```java
Map<String, Integer> wordCount = words.stream()
    .collect(Collectors.toMap(
        w -> w,
        w -> 1,
        Integer::sum)); // merge on duplicate keys
```

---

## Practice Exercises

| # | Exercise | Difficulty |
|---|----------|------------|
| 1 | From `List<Integer>`, get sum of even numbers | Easy |
| 2 | From `List<String>`, return distinct words sorted by length | Easy |
| 3 | Find second highest salary from `List<Employee>` | Medium |
| 4 | Group employees by department; find department with highest avg salary | Medium |
| 5 | Parse CSV lines `name,age,city` into `List<Person>` using streams | Medium |
| 6 | Implement `partitionByPredicate(List<T>, Predicate)` using `partitioningBy` | Easy |
| 7 | Chain Optional: `User` → `Address` → `ZipCode` without NPE | Medium |
| 8 | Count word frequency in a paragraph → `Map<String, Long>` | Medium |
| 9 | Refactor a 30-line nested for-loop to a stream pipeline | Hard |
| 10 | Use `collectingAndThen` to return unmodifiable list from stream | Hard |

### Exercise 1 — Solution

```java
int sum = numbers.stream()
    .filter(n -> n % 2 == 0)
    .mapToInt(Integer::intValue)
    .sum();
```

---

## Interview Q&A (5–8 Years Experience)

### Q1. What is the difference between Stream and Collection?

**Answer:**
| Collection | Stream |
|------------|--------|
| Stores elements | Does not store |
| Can iterate multiple times | Single consumption |
| Eager structure | Lazy computation |
| Can add/remove (mostly) | Does not modify source |

Stream is a **view/computation** over a data source, not a container.

---

### Q2. When does a stream pipeline actually execute?

**Answer:** On **terminal operation** invocation. All intermediate ops are lazy. Example: `filter` doesn't run until `collect` or `forEach` is called. Short-circuit ops (`findFirst`, `anyMatch`) may stop early.

---

### Q3. What is the difference between `map` and `flatMap`?

**Answer:**
- **`map`:** 1 input → 1 output. `Stream<T>` → `Stream<R>`
- **`flatMap`:** 1 input → 0 or many outputs, then flattened. `Stream<T>` where each T produces `Stream<R>` → single `Stream<R>`

Use `flatMap` for: nested lists, `Optional` chains in streams, exploding strings to characters.

---

### Q4. Why is `forEach` with side effects considered bad in streams?

**Answer:** In parallel streams, order is not guaranteed and multiple threads may run `forEach` concurrently. Mutating shared state (e.g., `list.add()`) causes **race conditions**. Prefer **`collect`** for aggregation. `forEach` is OK for logging/debugging on thread-safe sinks.

---

### Q5. Explain `Collectors.toMap` pitfalls.

**Answer:**
1. **Duplicate keys** → `IllegalStateException` unless merge function provided
2. **Null values** → `NullPointerException` (HashMap doesn't allow null values in `toMap`)
3. **No guaranteed order** — use `LinkedHashMap` supplier or `groupingBy` if order matters

```java
Collectors.toMap(key, val, (a, b) -> a, LinkedHashMap::new)
```

---

### Q6. `Optional.orElse` vs `orElseGet` — when does it matter?

**Answer:**
```java
optional.orElse(expensive());    // ALWAYS calls expensive()
optional.orElseGet(() -> expensive()); // Only if empty
```
Use `orElseGet` for expensive defaults (DB call, heavy computation). Use `orElse` for constants (`orElse("")`).

---

### Q7. Should Optional be used as method parameter or field?

**Answer:** **Generally no** (Brian Goetz / JDK team guidance). Optional is for **return types** to signal "may be absent." For parameters, use overloads, `@Nullable` annotations, or validation. For fields, use null or redesign. Exception: domain models in some frameworks (controversial).

---

### Q8. What happens if you reuse a stream?

**Answer:** `IllegalStateException: stream has already been operated upon or closed`. Get a fresh stream from the source for a new pipeline.

---

### Q9. Difference between `findFirst()` and `findAny()`?

**Answer:**
- **`findFirst`:** First element in encounter order (deterministic in sequential; ordered parallel still tries first)
- **`findAny`:** Any element — faster in parallel (less coordination)

Use `findAny` when any match suffices and order doesn't matter.

---

### Q10. How do primitive streams (`IntStream`) help?

**Answer:** Avoid `Integer` boxing. Methods: `mapToInt`, `sum()`, `average()`, `summaryStatistics()`. `IntStream.range(0, n)` for loops. Converting back: `boxed()` → `Stream<Integer>`.

---

### Q11. What is encounter order?

**Answer:** Order elements are met from source (e.g., `List` order). Some ops preserve it (`forEachOrdered`), some don't care (`forEach`). `sorted()` imposes order on unordered streams. Parallel streams may process out of order unless `forEachOrdered` or ordered collectors used.

---

## Self-Check

- [ ] I can refactor loops to streams without creating bugs
- [ ] I use `groupingBy` and `partitioningBy` confidently
- [ ] I never call `optional.get()` without `isPresent` check
- [ ] I know when `flatMap` beats nested `map`

**Next:** [Phase 3 — Stream API Advanced →](03-stream-api-advanced.md)
