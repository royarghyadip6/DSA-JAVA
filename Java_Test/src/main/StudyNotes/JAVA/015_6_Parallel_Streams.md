# 15.6 Parallel Streams

## Parallel Streams

### Frequently Asked

---

# 1. What is Parallel Stream?

<details>
<summary>Show Answer</summary>

**Answer:**

A **parallel stream** splits data into chunks and processes them **concurrently on multiple threads**—leveraging multiple CPU cores for faster processing.

### Sequential vs Parallel

```java
// Sequential — one thread
list.stream()
    .map(n -> n * 2)
    .forEach(System.out::println);

// Parallel — multiple threads
list.parallelStream()
    .map(n -> n * 2)
    .forEach(System.out::println);
```

### How to Create

```java
list.parallelStream()           // direct
list.stream().parallel()        // convert sequential to parallel
stream.parallel()               // mid-pipeline switch
stream.sequential()             // back to sequential
```

### Key Characteristics

| Feature | Detail |
|---------|--------|
| Threads | Uses `ForkJoinPool` (common pool) |
| Splitting | `Spliterator` divides source into chunks |
| Processing | Each chunk on different thread |
| Combining | Results merged at terminal op |
| Default pool | `ForkJoinPool.commonPool()` |

### Visual Flow

```text
Source data [1,2,3,4,5,6,7,8]
    ↓ split
Chunk1 [1,2]  Chunk2 [3,4]  Chunk3 [5,6]  Chunk4 [7,8]
    ↓           ↓           ↓           ↓
Thread1     Thread2     Thread3     Thread4
    ↓           ↓           ↓           ↓
    └───────────┴───────────┴───────────┘
                    ↓ merge
              Final result
```

### Same API as Sequential

```java
// All stream operations work the same
list.parallelStream()
    .filter(n -> n % 2 == 0)
    .map(n -> n * 2)
    .sorted()
    .collect(Collectors.toList());
```

**Interview Point:**

> Parallel stream = same Stream API but processes chunks on multiple threads via ForkJoinPool. Not automatically faster—depends on workload.

</details>

---

# 2. How parallelStream() works?

<details>
<summary>Show Answer</summary>

**Answer:**

`parallelStream()` splits the data source using a **Spliterator**, assigns chunks to **ForkJoinPool worker threads**, processes in parallel, and **merges results** at the terminal operation.

### Step-by-Step Flow

```text
1. Source created (list, array, etc.)
2. Spliterator splits source into splittable chunks
3. Tasks submitted to ForkJoinPool.commonPool()
4. Worker threads process chunks independently
5. Combiner merges partial results (collect, reduce)
6. Terminal operation returns final result
```

### Example Trace

```java
List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);

int sum = list.parallelStream()
    .filter(n -> n % 2 == 0)    // parallel filter on chunks
    .mapToInt(n -> n * 2)       // parallel map on chunks
    .sum();                     // merge sums from all threads
// sum = 4+8+12+16 = 40 (from 2,4,6,8 doubled)
```

### Spliterator Role

```java
// Spliterator splits based on source type
ArrayList  → efficient split (index-based)
LinkedList → poor split (must traverse)
HashSet    → moderate split
```

### ForkJoinPool — Work Stealing

```text
Thread finishes its chunk early
    ↓
Steals work from another thread's queue
    ↓
Better CPU utilization
```

### parallelStream() vs stream().parallel()

```java
list.parallelStream();      // creates parallel stream directly
list.stream().parallel();   // same result

// Can switch mid-pipeline
list.stream()
    .filter(x -> x > 0)      // sequential
    .parallel()              // parallel from here
    .map(x -> x * 2)
    .collect(Collectors.toList());
```

### Thread Count

```java
// Default: ForkJoinPool.commonPool()
// Size = Runtime.getRuntime().availableProcessors() - 1
int threads = ForkJoinPool.commonPool().getParallelism();
```

**Interview Point:**

> Split (Spliterator) → process (ForkJoinPool threads) → merge (combiner). ArrayList splits well; LinkedList splits poorly.

</details>

---

# 3. ForkJoinPool?

<details>
<summary>Show Answer</summary>

**Answer:**

**ForkJoinPool** is a special `ExecutorService` for **divide-and-conquer** tasks—parallel streams use the **common pool** (`ForkJoinPool.commonPool()`).

### What It Does

```text
Fork  → split task into subtasks
Join  → combine subtask results
Work-stealing → idle threads steal work from busy threads
```

### Parallel Stream Connection

```java
list.parallelStream()  // internally uses:
// ForkJoinPool.commonPool()
```

### Common Pool Details

| Property | Value |
|----------|-------|
| Pool type | Shared static pool |
| Default size | `availableProcessors() - 1` |
| Min size | 1 |
| Created | First parallel stream use |
| Shared by | All parallel streams in JVM |

### Check Pool Size

```java
ForkJoinPool pool = ForkJoinPool.commonPool();
System.out.println(pool.getParallelism());  // e.g., 7 on 8-core CPU
```

### Custom ForkJoinPool (Advanced)

```java
ForkJoinPool customPool = new ForkJoinPool(4);

customPool.submit(() ->
    list.parallelStream()
        .map(process)
        .collect(Collectors.toList())
).get();
// Uses custom 4-thread pool instead of common pool
```

### ForkJoinPool vs ThreadPoolExecutor

| ForkJoinPool | ThreadPoolExecutor |
|--------------|-------------------|
| Work-stealing | Fixed task queue |
| Divide-and-conquer | Independent tasks |
| Parallel streams | ExecutorService.submit() |
| Recursive splitting | One task per thread |

### Work-Stealing Diagram

```text
Thread1 queue: [chunk1] [chunk2]     ← busy
Thread2 queue: [chunk3]              ← done early
Thread2 steals chunk2 from Thread1   ← work stealing
```

**Interview Point:**

> Parallel streams use `ForkJoinPool.commonPool()` — shared pool, size ≈ CPU cores. Work-stealing balances load. Custom pool possible for isolation.

</details>

---

# 4. Advantages?

<details>
<summary>Show Answer</summary>

**Answer:**

Parallel streams offer **multi-core utilization**, **simpler parallel code**, and **automatic splitting/merging** for suitable workloads.

### Key Advantages

| Advantage | Detail |
|-----------|---------|
| **Multi-core usage** | Utilizes all CPU cores |
| **Simple API** | Same stream syntax—just add `parallel()` |
| **Automatic splitting** | Spliterator handles chunking |
| **Work-stealing** | ForkJoinPool balances load |
| **No manual threading** | No Thread/Runnable management |
| **Built-in merging** | Collectors combine results safely |

### CPU-Intensive Speedup

```java
// Heavy computation — parallel helps
List<Result> results = bigList.parallelStream()
    .map(item -> expensiveComputation(item))
    .collect(Collectors.toList());
// Near-linear speedup on multi-core CPU
```

### Large Dataset Processing

```java
// Millions of records — overhead worth it
long count = millionRecords.parallelStream()
    .filter(record -> record.isValid())
    .count();
```

### Clean vs Manual Threading

```java
// Parallel stream — simple
list.parallelStream().map(process).collect(toList());

// Manual — complex
ExecutorService pool = Executors.newFixedThreadPool(4);
List<Future<Result>> futures = new ArrayList<>();
// split, submit, collect, merge...
```

### Stateless Operations Scale Well

```java
numbers.parallelStream()
       .filter(n -> n % 2 == 0)
       .map(n -> n * n)
       .sum();  // associative — safe to parallelize
```

### When Advantages Apply

```text
✅ Large data (thousands+ elements)
✅ CPU-intensive per-element work
✅ Stateless operations
✅ Associative reductions (sum, max, collect)
✅ ArrayList / array sources (good splitting)
```

**Interview Point:**

> Advantages shine for **large CPU-bound** tasks on **ArrayList/array** with **stateless** ops. Simple API beats manual thread management.

</details>

---

# 5. Disadvantages?

<details>
<summary>Show Answer</summary>

**Answer:**

Parallel streams have **overhead**, **thread safety risks**, **ordering issues**, and can **hurt performance** on small data or I/O-bound tasks.

### Key Disadvantages

| Disadvantage | Problem |
|--------------|---------|
| **Overhead** | Thread creation, splitting, merging cost |
| **Small data** | Overhead exceeds benefit |
| **I/O blocking** | Threads block on network/DB—no gain |
| **Shared mutable state** | Race conditions, data corruption |
| **Order not guaranteed** | `forEach` order unpredictable |
| **Common pool sharing** | All parallel streams compete for same pool |
| **Poor sources** | LinkedList, IO streams split badly |
| **Debugging** | Harder to trace multi-threaded execution |

### Overhead on Small Data

```java
// ❌ Worse than sequential — overhead dominates
List.of(1, 2, 3).parallelStream()
    .map(n -> n * 2)
    .collect(Collectors.toList());
```

### I/O Trap

```java
// ❌ Threads block waiting for API — no parallelism benefit
urls.parallelStream()
    .forEach(url -> httpClient.get(url));  // blocking I/O

// ✅ Use async instead
CompletableFuture.allOf(...)
```

### Shared Mutable State — Danger

```java
List<Integer> result = new ArrayList<>();

list.parallelStream()
    .forEach(result::add);  // ❌ race condition — corrupted list!

// ✅ Use collector
List<Integer> result = list.parallelStream()
    .collect(Collectors.toList());  // thread-safe merge
```

### Order Lost

```java
list.parallelStream()
    .forEach(System.out::println);  // random order output

// Preserve order:
list.parallelStream()
    .forEachOrdered(System.out::println);  // slower
```

### Common Pool Contention

```text
App runs parallel streams in web requests
    ↓
All share ForkJoinPool.commonPool()
    ↓
One heavy job starves others
```

**Interview Point:**

> Disadvantages: overhead on small data, I/O blocking, mutable shared state, unordered forEach, common pool contention. Not a default choice.

</details>

---

### Advanced

---

# 6. When should parallel streams be avoided?

<details>
<summary>Show Answer</summary>

**Answer:**

Avoid parallel streams for **small data**, **I/O-bound work**, **shared mutable state**, **order-dependent logic**, and **poorly splittable sources**.

### Avoid Checklist

| Scenario | Why Avoid |
|----------|-----------|
| Small collections (< 10,000) | Split/merge overhead > benefit |
| I/O operations (HTTP, DB) | Threads block—use async |
| Shared mutable variables | Race conditions |
| Order matters | Parallel loses order |
| LinkedList source | Poor splitting |
| Non-associative reduce | Wrong parallel results |
| Already concurrent system | Pool contention |
| Low-latency requests | Thread overhead |

### Small Data

```java
// ❌ Avoid — 5 elements, parallel overhead wasteful
List.of(1,2,3,4,5).parallelStream().map(x -> x*2);

// ✅ Sequential is faster
List.of(1,2,3,4,5).stream().map(x -> x*2);
```

### I/O Bound

```java
// ❌ Avoid — threads wait on network
employees.parallelStream()
         .forEach(e -> emailService.send(e));  // blocking

// ✅ Use CompletableFuture or ExecutorService
employees.stream()
         .map(e -> CompletableFuture.supplyAsync(() -> emailService.send(e)))
         .collect(Collectors.toList());
```

### Mutable Shared State

```java
// ❌ Avoid
int count = 0;
list.parallelStream().forEach(x -> count++);  // race condition

// ✅ Use count() or AtomicInteger
long count = list.parallelStream().count();
```

### Order-Dependent

```java
// ❌ Avoid if order matters
list.parallelStream().forEach(processInOrder);

// ✅ Sequential or forEachOrdered
list.stream().forEach(processInOrder);
```

### LinkedList

```java
// ❌ Poor splitting — traversal cost
linkedList.parallelStream().map(process);

// ✅ ArrayList splits efficiently
arrayList.parallelStream().map(process);
```

### Rule of Thumb

```text
Use parallel when:
  - 10,000+ elements (rough guide)
  - CPU-intensive per element
  - Stateless operations
  - ArrayList / array source
  - Associative reduction

Otherwise use sequential stream.
```

**Interview Point:**

> Avoid: small data, I/O, mutable state, order-sensitive, LinkedList. Default to **sequential** unless profiling proves parallel helps.

</details>

---

# 7. Thread safety issues?

<details>
<summary>Show Answer</summary>

**Answer:**

Parallel streams are **not automatically thread-safe** for **shared mutable state**—multiple threads can corrupt data if you modify common variables or non-thread-safe collections during processing.

### Problem 1 — Mutable Shared Collection

```java
List<Integer> result = new ArrayList<>();  // NOT thread-safe

list.parallelStream()
    .forEach(result::add);  // ❌ multiple threads add concurrently
// Corrupted list — lost elements, ArrayIndexOutOfBoundsException
```

### Fix — Use Collector

```java
List<Integer> result = list.parallelStream()
    .collect(Collectors.toList());  // ✅ safe merge
```

### Problem 2 — Shared Counter

```java
int count = 0;
list.parallelStream()
    .forEach(x -> count++);  // ❌ not atomic — wrong count
```

### Fix — Atomic or Terminal Op

```java
long count = list.parallelStream().count();  // ✅

AtomicInteger count = new AtomicInteger();
list.parallelStream().forEach(x -> count.incrementAndGet());  // ✅
```

### Problem 3 — Modifying Source During Stream

```java
List<String> list = new ArrayList<>(...);
list.parallelStream()
    .forEach(list::remove);  // ❌ ConcurrentModificationException
```

### Problem 4 — Non-Thread-Safe Object Fields

```java
class Processor {
    int total = 0;  // shared across parallel threads

    void process(int x) {
        total += x;  // ❌ race condition in parallel forEach
    }
}

list.parallelStream().forEach(processor::process);
```

### What IS Thread-Safe in Parallel Streams

| Safe | Unsafe |
|------|--------|
| `collect(Collectors.toList())` | `forEach` on shared list |
| `reduce()` with associative op | Shared int counter |
| `count()`, `sum()`, `max()` | Modifying source collection |
| Stateless lambda | Mutable object fields |
| Reading immutable data | `HashMap` without sync |

### Thread-Safe Patterns

```java
// ✅ Collect — combiner merges thread-local results
.parallelStream().collect(Collectors.groupingBy(...))

// ✅ Reduce — associative
.parallelStream().reduce(0, Integer::sum)

// ✅ Concurrent collector
.parallelStream().collect(Collectors.toConcurrentMap(...))
```

**Interview Point:**

> Parallel streams safe for **stateless ops** and **collectors**. Unsafe for **shared mutable state**—use `collect()` not `forEach` on shared collections.

</details>

---

# 8. Performance considerations?

<details>
<summary>Show Answer</summary>

**Answer:**

Parallel stream performance depends on **data size**, **task type**, **source splittability**, **CPU cores**, and **overhead**—always **measure**, don't assume parallel is faster.

### Performance Factors

| Factor | Impact |
|--------|--------|
| Data size | Larger = better parallel benefit |
| Task type | CPU-bound ✅, I/O-bound ❌ |
| Source | ArrayList ✅, LinkedList ❌ |
| CPU cores | More cores = more potential speedup |
| Overhead | Split + merge + thread coordination |
| Pool contention | Shared common pool |

### Data Size Threshold (Rule of Thumb)

```text
< 1,000 elements    → sequential usually faster
1,000 – 10,000      → benchmark both
> 10,000 + CPU work → parallel often wins
```

### CPU vs I/O

```java
// ✅ CPU-bound — parallel helps
list.parallelStream()
    .map(n -> complexMath(n))  // pure computation
    .sum();

// ❌ I/O-bound — parallel doesn't help
list.parallelStream()
    .map(url -> httpGet(url))  // threads block on network
    .collect(toList());
```

### Source Splittability

```java
// Fast split — O(1) per chunk
ArrayList, arrays, IntStream.range()

// Slow split — must traverse
LinkedList, Stream.iterate(), BufferedReader.lines()
```

### Amdahl's Law

```text
Speedup limited by sequential portion
If 10% of work is sequential → max speedup ~10x even on infinite cores
sorted(), distinct() have sequential phases
```

### Benchmark Example

```java
// Always measure — don't guess
long start = System.nanoTime();
list.parallelStream().map(heavyWork).collect(toList());
long parallelTime = System.nanoTime() - start;

start = System.nanoTime();
list.stream().map(heavyWork).collect(toList());
long sequentialTime = System.nanoTime() - start;
```

### Optimization Tips

```text
1. Use primitive streams (IntStream) — avoid boxing
2. Avoid sorted/distinct early in parallel pipeline
3. Use collect() not shared forEach
4. Right source — ArrayList not LinkedList
5. Custom ForkJoinPool for isolation in production
6. Don't parallelize in tight loops
```

### Production Anti-Pattern

```java
// ❌ Parallel stream per HTTP request — pool exhaustion
@GetMapping("/report")
public Report getReport() {
    return data.parallelStream()  // uses common pool
               .map(this::process)
               .collect(...);
}
// Many concurrent requests → common pool saturated
```

**Interview Point:**

> Profile before using parallel. CPU + large data + ArrayList = good candidate. I/O, small data, LinkedList = use sequential or async APIs.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: parallelStream() in servlet — safe?

<details>
<summary>Show Answer</summary>

**Answer:** Risky. Uses shared `ForkJoinPool.commonPool()` — concurrent requests compete. Heavy parallel work in web layer can starve other requests. Prefer dedicated `ExecutorService` or batch jobs for heavy parallel work.

</details>

---

### Q: Does parallel stream always use all CPU cores?

<details>
<summary>Show Answer</summary>

**Answer:** No. Limited by `ForkJoinPool.commonPool()` parallelism (typically `cores - 1`). Data size, splittability, and operation type also limit actual parallelism.

</details>

---

### Q: parallel reduce order?

<details>
<summary>Show Answer</summary>

**Answer:** `reduce` with associative op (sum, max) — order doesn't matter, parallel safe. Non-associative `(a,b) -> a - b` — parallel gives **wrong** results. Use sequential for non-associative ops.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Parallel stream = ForkJoinPool + Spliterator split + merge. Good for **large CPU-bound** data on **ArrayList**. Avoid: small data, I/O, mutable shared state, LinkedList. Default sequential; parallel only when profiled.

</details>
