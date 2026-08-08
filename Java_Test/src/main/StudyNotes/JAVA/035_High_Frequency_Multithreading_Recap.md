# 35. High-Frequency Multithreading Questions (Recap)

Interview rapid-fire recap (Java 5–8 YOE). Short answers only — not deep dives.

---

## 1. `start()` vs `run()`

<details>
<summary>Show Answer</summary>

| | `start()` | `run()` |
|---|-----------|---------|
| What it does | Creates a **new thread** and calls `run()` on it | Runs on the **current** thread (no new thread) |
| Scheduler | Involves JVM/OS thread scheduling | Just a normal method call |
| Calling twice | `IllegalThreadStateException` | Can call many times |

```java
Thread t = new Thread(() -> System.out.println(Thread.currentThread().getName()));
t.start();  // new thread: Thread-0
t.run();    // same thread that called run (e.g. main)
```

**One-liner:** `start()` = new thread; `run()` = plain method call on current thread.

</details>

---

## 2. `sleep()` vs `wait()`

<details>
<summary>Show Answer</summary>

| | `sleep()` | `wait()` |
|---|-----------|----------|
| Belongs to | `Thread` | `Object` |
| Lock | Does **not** release monitor | **Releases** monitor |
| Wake up | Time ends (or interrupt) | `notify` / `notifyAll` / timeout |
| Where | Anywhere | Must be inside `synchronized` |

```java
synchronized (lock) {
    lock.wait();           // releases lock, waits for notify
}
Thread.sleep(1000);        // holds whatever lock you already have
```

**One-liner:** `sleep` keeps the lock and waits time; `wait` drops the lock and waits for notify.

</details>

---

## 3. `notify()` vs `notifyAll()`

<details>
<summary>Show Answer</summary>

| | `notify()` | `notifyAll()` |
|---|------------|---------------|
| Wakes | **One** waiting thread | **All** waiting threads |
| Safe default | Risky if multiple wait conditions | Prefer this |
| Spurious wake | Still need `while` loop | Same — always use `while` |

```java
synchronized (lock) {
    while (!ready) lock.wait();
    lock.notifyAll();  // safer when >1 waiter / condition
}
```

**One-liner:** Prefer `notifyAll()`; `notify()` wakes only one waiter and can miss the right thread.

</details>

---

## 4. `synchronized` vs `ReentrantLock`

<details>
<summary>Show Answer</summary>

| | `synchronized` | `ReentrantLock` |
|---|---------------|-----------------|
| Unlock | Automatic (block end) | Must `unlock()` in `finally` |
| Try lock | No | `tryLock()`, timeout |
| Fairness | No control | Optional fair lock |
| Conditions | One wait-set per monitor | Multiple `Condition`s |
| Interrupt | Limited | `lockInterruptibly()` |

```java
ReentrantLock lock = new ReentrantLock();
lock.lock();
try {
    // critical section
} finally {
    lock.unlock();
}
```

**One-liner:** Use `synchronized` by default; use `ReentrantLock` when you need tryLock, fairness, or multiple conditions.

</details>

---

## 5. `HashMap` vs `ConcurrentHashMap`

<details>
<summary>Show Answer</summary>

| | `HashMap` | `ConcurrentHashMap` |
|---|-----------|---------------------|
| Threads | Not thread-safe | Thread-safe |
| Null | Key/value null OK | **No** null key/value |
| Locking | N/A (external sync) | Fine-grained (buckets/nodes) |
| Iterators | Fail-fast | Weakly consistent |

```java
Map<String, Integer> map = new ConcurrentHashMap<>();
map.put("a", 1);
map.compute("a", (k, v) -> v + 1);  // atomic update
```

**One-liner:** Never share raw `HashMap` across threads; use `ConcurrentHashMap` (no nulls).

</details>

---

## 6. `volatile` vs `synchronized`

<details>
<summary>Show Answer</summary>

| | `volatile` | `synchronized` |
|---|------------|----------------|
| Visibility | Yes | Yes |
| Atomicity | Only for single r/w | Full critical section |
| Ordering | Happens-before on write to read | Enter/exit monitor |
| Compound ops | `i++` **not** safe | Safe inside block |

```java
volatile boolean flag = false;  // visibility OK
// count++ still needs AtomicInteger or synchronized
```

**One-liner:** `volatile` = visibility/ordering for one field; `synchronized` = visibility + mutual exclusion.

</details>

---

## 7. `Callable` vs `Runnable`

<details>
<summary>Show Answer</summary>

| | `Runnable` | `Callable<V>` |
|---|------------|---------------|
| Return | `void` | Returns `V` |
| Exception | Cannot throw checked | Can throw checked |
| Typical use | `Thread` / fire-and-forget | `ExecutorService.submit` then `Future` |

```java
Callable<Integer> c = () -> 42;
Future<Integer> f = executor.submit(c);
Integer result = f.get();
```

**One-liner:** `Runnable` = no result; `Callable` = result + checked exceptions via `Future`.

</details>

---

## 8. `Future` vs `CompletableFuture`

<details>
<summary>Show Answer</summary>

| | `Future` | `CompletableFuture` |
|---|----------|---------------------|
| Get result | Blocking `get()` | Non-blocking chaining |
| Compose | Hard | `thenApply`, `thenCompose`, `allOf` |
| Complete manually | Limited | `complete` / `completeExceptionally` |
| Callbacks | No (until CF) | Yes |

```java
CompletableFuture.supplyAsync(() -> "hi")
    .thenApply(String::toUpperCase)
    .thenAccept(System.out::println);
```

**One-liner:** `Future` = wait for result; `CompletableFuture` = async pipeline + compose.

</details>

---

## 9. `wait()` vs `join()`

<details>
<summary>Show Answer</summary>

| | `wait()` | `join()` |
|---|----------|----------|
| Purpose | Wait on a **monitor** / condition | Wait for a **thread to finish** |
| Lock | Must hold object monitor | No monitor required |
| Wake | `notify` / `notifyAll` | Thread terminates |

```java
worker.join();           // current thread waits until worker ends
synchronized (lock) {
    while (!done) lock.wait();
}
```

**One-liner:** `join` waits for thread death; `wait` waits for a condition on a lock.

</details>

---

## 10. `AtomicInteger` vs `synchronized`

<details>
<summary>Show Answer</summary>

| | `AtomicInteger` | `synchronized` |
|---|---------------|----------------|
| Mechanism | CAS (lock-free for single var) | Monitor / mutex |
| Scope | One numeric (or ref) update | Any multi-step critical section |
| Contention | Often better for counters | Heavier under high contention |
| Example | `incrementAndGet()` | Protect several fields together |

```java
AtomicInteger count = new AtomicInteger(0);
count.incrementAndGet();  // atomic ++
```

**One-liner:** Prefer `Atomic*` for simple counters; use `synchronized`/`Lock` for multi-step logic.

</details>

---

## 11. ConcurrentHashMap internal working

<details>
<summary>Show Answer</summary>

**Idea (Java 8+):** Array of bins (nodes). Reads mostly lock-free. Writes lock **only the bin** (synchronized on first node) or use CAS to insert into empty bin. Large bins treeify (red-black) like `HashMap`. Size uses striped counters (`CounterCell`), not one global lock.

```java
map.put(key, value);
map.putIfAbsent(key, value);  // atomic check-then-act
```

**One-liner:** CHM locks per bin (not whole map); reads are mostly lock-free; trees for long chains.

</details>

---

## 12. Deadlock detection and prevention

<details>
<summary>Show Answer</summary>

**Deadlock needs all four:** mutual exclusion, hold-and-wait, no preemption, circular wait.

| Prevention | How |
|------------|-----|
| Lock ordering | Always acquire A then B (same order everywhere) |
| Try lock | `tryLock` + timeout; back off and retry |
| Avoid nested locks | Prefer one lock or lock-free designs |
| Detection | Thread dump / `jstack` — look for circular waiting to lock |

```java
if (lock1.tryLock(100, TimeUnit.MILLISECONDS)) {
    try {
        if (lock2.tryLock(100, TimeUnit.MILLISECONDS)) {
            try { /* work */ } finally { lock2.unlock(); }
        }
    } finally { lock1.unlock(); }
}
```

**One-liner:** Prevent with consistent lock order; detect with thread dumps showing circular waits.

</details>

---

## 13. CAS operation

<details>
<summary>Show Answer</summary>

**CAS** = Compare-And-Swap: atomically if value == expected, set to update; else fail.

Used by `AtomicInteger`, `ConcurrentHashMap`, many lock-free structures. On failure, retry (spin). ABA problem: value A to B to A; mitigated by versioned refs (`AtomicStampedReference`).

```java
AtomicInteger x = new AtomicInteger(10);
x.compareAndSet(10, 20);  // true if still 10
```

**One-liner:** CAS = atomic check expected then set; basis of lock-free atomics (watch ABA).

</details>

---

## 14. Java Memory Model (JMM)

<details>
<summary>Show Answer</summary>

JMM defines **when** writes by one thread become visible to another, and what reorderings are allowed.

Without sync/`volatile`/atomics: threads may see stale/cached values; compiler/CPU may reorder.

Guarantees come from **happens-before** edges (see next topic).

```java
data = 42;
ready = true;  // if ready is volatile, seeing ready=true means see data=42
```

**One-liner:** JMM = rules for visibility and ordering between threads; sync/`volatile` create those guarantees.

</details>

---

## 15. Happens-before relationship

<details>
<summary>Show Answer</summary>

If action A **happens-before** B, then A effects are visible to B and ordered before B.

**Common edges:**
- Program order in one thread
- Unlock then later lock on same monitor
- Write `volatile` then later read of that `volatile`
- Thread `start` then actions in started thread
- Actions in thread then successful `join` on that thread
- Transitivity

```java
synchronized (lock) { shared = 1; }
synchronized (lock) { use(shared); }
```

**One-liner:** Happens-before = visibility + order guarantee between two actions across threads.

</details>

---

## 16. ThreadLocal memory leak

<details>
<summary>Show Answer</summary>

`ThreadLocal` stores values in the **thread** `ThreadLocalMap`. Entry keys are **weak** refs to `ThreadLocal`; **values are strong**.

If you set a value and never `remove()`, and the `ThreadLocal` object is GC'd, the value can stay pinned by a long-lived thread (pool threads) = leak.

```java
try {
    tl.set(userContext);
} finally {
    tl.remove();  // always in pools / servlet filters
}
```

**One-liner:** Always `ThreadLocal.remove()` in `finally` — especially with thread pools.

</details>

---

## 17. Producer Consumer implementation

<details>
<summary>Show Answer</summary>

Classic: shared buffer + wait when full/empty. Prefer `BlockingQueue` in real code.

```java
BlockingQueue<Integer> q = new ArrayBlockingQueue<>(10);
q.put(item);           // producer: waits if full
Integer x = q.take();  // consumer: waits if empty
```

Low-level sketch: `synchronized` + `while` + `wait`/`notifyAll` on a bounded buffer.

**One-liner:** Use `BlockingQueue`; if DIY, always `while` (not `if`) around `wait`.

</details>

---

## 18. ExecutorService lifecycle

<details>
<summary>Show Answer</summary>

| Method | Meaning |
|--------|---------|
| `shutdown()` | No new tasks; finish queued |
| `shutdownNow()` | Try cancel running; return waiting tasks |
| `awaitTermination` | Block until done or timeout |
| `isShutdown` / `isTerminated` | State checks |

```java
ExecutorService es = Executors.newFixedThreadPool(4);
try {
    es.submit(task);
} finally {
    es.shutdown();
    es.awaitTermination(30, TimeUnit.SECONDS);
}
```

**One-liner:** Always `shutdown()` (then optionally `awaitTermination`); pools do not die by themselves.

</details>

---

## 19. CompletableFuture chaining

<details>
<summary>Show Answer</summary>

| Method | Role |
|--------|------|
| `thenApply` | Transform result (sync function) |
| `thenCompose` | Flat-map another CF (async dependency) |
| `thenCombine` | Wait two CFs, combine |
| `exceptionally` / `handle` | Error recovery |
| `allOf` / `anyOf` | Barrier / race |

```java
CompletableFuture.supplyAsync(() -> fetch())
    .thenCompose(id -> fetchDetail(id))
    .thenApply(this::map)
    .exceptionally(ex -> fallback);
```

**One-liner:** Chain with `thenApply`/`thenCompose`; handle errors with `exceptionally`/`handle`.

</details>

---

## 20. ForkJoinPool work stealing

<details>
<summary>Show Answer</summary>

`ForkJoinPool`: each worker has a **deque** of tasks. Worker pops from **own** deque (LIFO); idle workers **steal** from another deque (FIFO). Cuts contention and balances uneven work (divide-and-conquer).

Default common pool used by parallel streams / many `CompletableFuture` async methods.

```java
ForkJoinPool pool = new ForkJoinPool();
pool.invoke(new MyRecursiveTask(data));
```

**One-liner:** Work stealing = busy workers keep own tasks; idle ones steal from others queues.

</details>

---

## Cheat Sheet — All 20

| # | Topic | Remember |
|---|--------|----------|
| 1 | `start` vs `run` | `start` = new thread; `run` = method call |
| 2 | `sleep` vs `wait` | `wait` releases lock; `sleep` does not |
| 3 | `notify` vs `notifyAll` | Prefer `notifyAll` |
| 4 | `synchronized` vs `ReentrantLock` | Lock for tryLock / fairness / Conditions |
| 5 | `HashMap` vs CHM | CHM for concurrency; no nulls |
| 6 | `volatile` vs `synchronized` | Volatile is not atomic for compound ops |
| 7 | `Callable` vs `Runnable` | Callable returns + throws |
| 8 | `Future` vs CF | CF = compose / non-blocking |
| 9 | `wait` vs `join` | Condition vs thread end |
| 10 | `AtomicInteger` vs sync | CAS counter vs critical section |
| 11 | CHM internals | Per-bin lock / CAS; treeify |
| 12 | Deadlock | Lock order + thread dump |
| 13 | CAS | Compare-and-swap; ABA risk |
| 14 | JMM | Visibility and reordering rules |
| 15 | Happens-before | Sync / volatile create edges |
| 16 | ThreadLocal leak | Always `remove()` in pools |
| 17 | Producer-Consumer | `BlockingQueue` + `while`/`wait` |
| 18 | Executor lifecycle | `shutdown` + `awaitTermination` |
| 19 | CF chaining | `thenApply` / `thenCompose` / `exceptionally` |
| 20 | ForkJoin work steal | Steal from others deques |

---

<details>
<summary>Interview One-Liner</summary>

**High-frequency MT in one breath:** `start` is not `run`; `wait` releases lock / `sleep` does not; prefer `notifyAll`; `ReentrantLock` when you need tryLock; never share `HashMap` — use CHM; `volatile` is not a mutex; `Callable`/`Future` vs CF pipelines; `join` is not `wait`; atomics for counters; CHM bins+CAS; prevent deadlock with lock order; CAS underpins lock-free; JMM/happens-before for visibility; always `ThreadLocal.remove()`; `BlockingQueue` for P-C; always shut down executors; chain CFs carefully; ForkJoin steals work.

</details>
