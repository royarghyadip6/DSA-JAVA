# 29. Fork Join Framework

## 29. Fork Join Framework

## Frequently Asked

---

# 1. What is ForkJoinPool?

<details>
<summary>Show Answer</summary>

**Answer:**

`ForkJoinPool` is a special **ExecutorService** designed for **parallel divide-and-conquer** tasks—it splits work into smaller subtasks (fork), executes them in parallel, and merges results (join).

### Basic Concept

```text
Fork  = split task into smaller subtasks
Join  = combine subtask results
Pool  = manages worker threads executing fork/join work
```

### Creation and Usage

```java
ForkJoinPool pool = new ForkJoinPool(); // parallelism = CPU cores - 1

int result = pool.invoke(new SumTask(array, 0, array.length));
// invoke = submit + wait for result
```

### vs Regular ExecutorService

| | `ExecutorService` | `ForkJoinPool` |
|---|-------------------|----------------|
| Task model | Independent tasks | Divide-and-conquer |
| Work distribution | Queue-based | Work-stealing deque |
| Best for | Independent async tasks | Recursive parallel computation |
| Example | HTTP requests, emails | Array sum, merge sort, tree traversal |

### Default Pool for Parallel Streams

```java
list.parallelStream().map(x -> compute(x));
// Uses ForkJoinPool.commonPool() internally
```

### Key API

```java
ForkJoinPool pool = ForkJoinPool.commonPool(); // shared pool
pool.invoke(task);           // run and wait for result
pool.submit(task);           // returns ForkJoinTask
pool.getParallelism();       // number of worker threads
```

**Interview Point:**

> ForkJoinPool = parallel divide-and-conquer executor. `invoke()` for recursive tasks. Uses work-stealing. Default backend for `parallelStream()`.

</details>

---

# 2. Why ForkJoinPool?

<details>
<summary>Show Answer</summary>

**Answer:**

`ForkJoinPool` exists because regular thread pools are **poor at recursive parallel work**—ForkJoinPool uses **work-stealing** to balance load when tasks split unevenly, maximizing CPU utilization.

### Problem with FixedThreadPool for Recursive Work

```java
// Split array sum into halves recursively
// FixedThreadPool: subtasks queue up — some threads idle while others overloaded
// ForkJoinPool: idle threads STEAL work from busy threads' deques
```

### Why Work-Stealing Matters

```text
Task splits unevenly:
  Thread 1: huge left half (slow)
  Thread 2: tiny right half (done quickly)

FixedThreadPool: Thread 2 idle while Thread 1 works
ForkJoinPool:    Thread 2 STEALS tasks from Thread 1's deque
```

### Ideal Use Cases

```text
✅ Recursive algorithms (merge sort, quick sort, tree walk)
✅ Large array processing (sum, max, filter)
✅ Parallel search in data structures
✅ CPU-bound parallel computation
✅ parallelStream() backend

❌ Independent unrelated tasks → use ExecutorService
❌ IO-bound tasks → threads block, stealing doesn't help
❌ Simple fire-and-forget → use ExecutorService
```

### Performance on Multi-Core

```text
Regular pool on 8 cores with uneven split:
  2 threads busy, 6 idle → 25% CPU utilization

ForkJoinPool with work-stealing:
  All 8 threads active → near 100% CPU utilization
```

**Interview Point:**

> ForkJoinPool for recursive/divide-and-conquer parallel work. Work-stealing balances uneven splits. Not for IO-bound or independent tasks.

</details>

---

# 3. Work stealing algorithm?

<details>
<summary>Show Answer</summary>

**Answer:**

**Work-stealing** is a scheduling algorithm where **idle worker threads steal tasks** from the **tail of other busy threads' deques**—keeping all CPUs busy without central coordination.

### How It Works

```text
Each worker thread has a DEQUE (double-ended queue):

Owner thread:  push/pop from HEAD (LIFO) — own work
Thief thread:  steal from TAIL (FIFO) — other's work

Thread idle → tries to steal from random other thread's tail
```

### Visual

```text
Thread-1 deque: [TaskA, TaskB, TaskC] ← owner pops from head
Thread-2 deque: [empty] → steals TaskC from Thread-1's tail

Thread-1: working on TaskA
Thread-2: stole TaskC — both busy!
```

### Why Deque (Not Simple Queue)

```text
Owner uses HEAD (LIFO):
  Most recently forked subtask first
  Better cache locality — divide-and-conquer depth-first

Thief uses TAIL (FIFO):
  Steals oldest/largest remaining chunk
  Reduces contention with owner (opposite ends)
```

### Algorithm Steps

```text
1. Worker finishes its deque → becomes idle
2. Try own deque head → empty
3. Try steal from random worker's tail
4. If steal succeeds → execute stolen task
5. If all deques empty → park thread (wait)
6. New work arrives → wake idle threads
```

### Benefits

```text
✅ No central task queue bottleneck
✅ Self-balancing under uneven workloads
✅ Minimal synchronization (deque ends)
✅ High CPU utilization
```

**Interview Point:**

> Work-stealing = idle threads steal from tail of busy threads' deques. Owner pops head, thief steals tail. Self-balancing parallel scheduler.

</details>

---

## Classes

---

# 4. RecursiveTask

<details>
<summary>Show Answer</summary>

**Answer:**

`RecursiveTask<V>` is a **ForkJoinTask that returns a result**—extend it for divide-and-conquer computations that produce a value (sum, max, search result).

### Example — Parallel Array Sum

```java
class SumTask extends RecursiveTask<Long> {
    private final int[] array;
    private final int start, end;
    private static final int THRESHOLD = 1000;

    SumTask(int[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Long compute() {
        int length = end - start;
        if (length <= THRESHOLD) {
            // Base case — compute directly
            long sum = 0;
            for (int i = start; i < end; i++) sum += array[i];
            return sum;
        }
        // Recursive case — fork subtasks
        int mid = start + length / 2;
        SumTask left  = new SumTask(array, start, mid);
        SumTask right = new SumTask(array, mid, end);
        left.fork();           // async left half
        long rightSum = right.compute(); // compute right in current thread
        long leftSum  = left.join();     // wait for left result
        return leftSum + rightSum;
    }
}

// Usage
ForkJoinPool pool = new ForkJoinPool();
long total = pool.invoke(new SumTask(array, 0, array.length));
```

### fork() vs compute() vs join()

| Method | Action |
|--------|--------|
| `fork()` | Submit subtask to pool asynchronously |
| `compute()` | Execute subtask in current thread |
| `join()` | Wait for forked subtask result |

### RecursiveTask vs RecursiveAction

```text
RecursiveTask<V>  → returns result (compute() returns V)
RecursiveAction   → no return (void compute())
```

**Interview Point:**

> `RecursiveTask<V>` = fork/join with return value. Pattern: split → fork left → compute right → join left. Set THRESHOLD to avoid over-splitting.

</details>

---

# 5. RecursiveAction

<details>
<summary>Show Answer</summary>

**Answer:**

`RecursiveAction` is a **ForkJoinTask with no return value**—extend it for parallel operations that don't produce a result (fill array, transform in-place, print).

### Example — Parallel Array Transform

```java
class TransformTask extends RecursiveAction {
    private final int[] array;
    private final int start, end;
    private static final int THRESHOLD = 500;

    TransformTask(int[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }

    @Override
    protected void compute() {
        int length = end - start;
        if (length <= THRESHOLD) {
            // Base case — transform directly
            for (int i = start; i < end; i++) {
                array[i] = array[i] * 2;
            }
        } else {
            // Split and fork
            int mid = start + length / 2;
            invokeAll(
                new TransformTask(array, start, mid),
                new TransformTask(array, mid, end)
            );
        }
    }
}

// Usage
ForkJoinPool pool = new ForkJoinPool();
pool.invoke(new TransformTask(array, 0, array.length));
```

### invokeAll() — Fork Both Subtasks

```java
// invokeAll runs both subtasks and waits for both
invokeAll(leftTask, rightTask);
// vs manual:
leftTask.fork();
rightTask.compute();
leftTask.join();
```

### When to Use RecursiveAction

```text
✅ In-place array transformation
✅ Parallel file processing (no aggregate result)
✅ Fill/generate large data structures
✅ Parallel tree traversal (side effects only)

Use RecursiveTask when you need aggregated result
```

### RecursiveAction vs Runnable

```text
Runnable:     run() — single task, no splitting
RecursiveAction: compute() — can fork subtasks recursively
```

**Interview Point:**

> `RecursiveAction` = fork/join without return value. `invokeAll()` forks both halves. Use for parallel side-effect work (transform, fill, process).

</details>

---

## Advanced

---

# 6. How work stealing works?

<details>
<summary>Show Answer</summary>

**Answer:**

Internally, each `ForkJoinPool` worker thread maintains a **deque of tasks**. The owner works from the **head**; idle threads **steal from the tail** of other workers—minimizing contention and maximizing parallelism.

### Internal Architecture

```text
ForkJoinPool
  ├── Worker-1: deque [subtask-A, subtask-B, subtask-C]
  ├── Worker-2: deque [subtask-D]
  ├── Worker-3: deque [empty — idle]
  └── Worker-4: deque [subtask-E, subtask-F]
```

### Stealing Step-by-Step

```text
1. Worker-3 idle — own deque empty
2. Randomly pick Worker-1 as victim
3. CAS steal subtask-C from tail of Worker-1's deque
4. Worker-3 executes stolen subtask-C
5. If steal fails (contention) → try another victim
6. If all deques empty → park thread in wait queue
```

### fork() Internal Flow

```java
leftTask.fork();
// Internally:
// 1. Push leftTask to current worker's deque HEAD
// 2. Notify pool of new work
// 3. Another worker may steal it OR owner runs it later
```

### Threshold — Avoid Over-Splitting

```java
private static final int THRESHOLD = 1000;

if (length <= THRESHOLD) {
    // compute directly — don't fork tiny tasks
    return directCompute();
}
// Only fork when chunk is large enough
// Too small → fork overhead > parallel benefit
```

### Contention Reduction

```text
Owner pops HEAD, thief steals TAIL:
  → opposite ends of deque
  → minimal lock contention
  → CAS on tail for steal, simple pop for owner
```

### Production Tuning

```java
ForkJoinPool pool = new ForkJoinPool(
    4,                          // parallelism level
    ForkJoinPool.defaultForkJoinWorkerThreadFactory,
    null,                       // uncaught exception handler
    true                        // asyncMode for event-style tasks
);
```

**Interview Point:**

> Each worker has deque. Owner: head LIFO. Thief: tail FIFO via CAS. Set THRESHOLD to prevent over-forking tiny tasks. Idle threads park when all deques empty.

</details>

---

# 7. Parallel Stream relationship with ForkJoinPool?

<details>
<summary>Show Answer</summary>

**Answer:**

`parallelStream()` uses **`ForkJoinPool.commonPool()`** as its default executor—all parallel stream operations run on the shared common ForkJoin pool.

### Connection

```java
List<Integer> numbers = List.of(1, 2, 3, 4, 5);

numbers.parallelStream()
    .map(n -> n * 2)
    .forEach(System.out::println);

// Internally uses:
ForkJoinPool.commonPool()
```

### commonPool() Details

```text
Shared across entire JVM
Parallelism = Runtime.getRuntime().availableProcessors() - 1
Single instance — all parallelStream() calls share it
Also used by CompletableFuture.supplyAsync() by default
```

### Custom ForkJoinPool for Parallel Stream

```java
ForkJoinPool customPool = new ForkJoinPool(4);
try {
    customPool.submit(() ->
        list.parallelStream()
            .map(x -> heavyCompute(x))
            .collect(Collectors.toList())
    ).get();
} finally {
    customPool.shutdown();
}
// parallelStream runs in customPool, not commonPool
```

### parallelStream vs ForkJoinPool.invoke()

| | `parallelStream()` | `ForkJoinPool.invoke()` |
|---|-------------------|-------------------------|
| API | Stream pipeline | RecursiveTask/Action |
| Splitting | Automatic per element | Manual divide-and-conquer |
| Control | Less control | Full control over split logic |
| Use for | Simple parallel map/filter | Custom recursive algorithms |

### Risk — Blocking on commonPool

```java
// ❌ BAD — IO blocks common pool threads
list.parallelStream()
    .map(url -> httpClient.get(url)) // blocking IO!
    .collect(Collectors.toList());
// Starves other parallelStream and CompletableFuture tasks

// ✅ Use custom ExecutorService for IO
ExecutorService ioPool = Executors.newFixedThreadPool(20);
```

### Tuning commonPool

```text
-Djava.util.concurrent.ForkJoinPool.common.parallelism=8
// Override default parallelism
// Use carefully — affects all parallel streams in JVM
```

**Interview Point:**

> `parallelStream()` → `ForkJoinPool.commonPool()`. Shared pool — don't block with IO. Use custom ForkJoinPool for isolated parallel work. RecursiveTask for custom split logic.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: ForkJoinPool vs ThreadPoolExecutor?

<details>
<summary>Show Answer</summary>

**Answer:**

ForkJoinPool = divide-and-conquer + work-stealing for recursive CPU tasks. ThreadPoolExecutor = independent tasks from shared queue. Use ForkJoinPool for parallel array/tree algorithms; ThreadPoolExecutor for async IO and independent jobs.

</details>

---

### Q: fork() then compute() vs invokeAll()?

<details>
<summary>Show Answer</summary>

**Answer:**

`fork()` + `compute()` + `join()` — fork one side async, compute other in current thread (one less thread switch). `invokeAll()` — fork both subtasks. `invokeAll()` simpler; fork/compute/join slightly more efficient for binary split.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> ForkJoinPool = divide-and-conquer + work-stealing. `RecursiveTask` (returns value) vs `RecursiveAction` (void). Idle threads steal from deque tail. `parallelStream()` uses `commonPool()`. Set THRESHOLD to avoid over-splitting.

</details>
