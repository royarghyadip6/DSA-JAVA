# 26. Executor Framework

## 26. Executor Framework

## Most Important for 5+ Years

---

# 1. Why Executor Framework introduced?

<details>
<summary>Show Answer</summary>

**Answer:**

The **Executor Framework** (Java 5) was introduced to **separate task submission from thread management**—replacing manual `new Thread()` creation with reusable thread pools, lifecycle control, and better resource management.

### Problems It Solved

```text
❌ Manual thread creation — one thread per task
❌ No thread reuse — expensive creation/destruction
❌ No limit on thread count — resource exhaustion
❌ Hard to shutdown gracefully
❌ No task queue — unbounded thread growth
❌ Tight coupling — task logic mixed with thread management
```

### Before Executor Framework

```java
// ❌ Old way — manual thread per request
for (Request req : requests) {
    new Thread(() -> handleRequest(req)).start();
    // 10,000 requests = 10,000 threads!
}
```

### After Executor Framework

```java
// ✅ Submit tasks to pool — threads reused
ExecutorService executor = Executors.newFixedThreadPool(10);
for (Request req : requests) {
    executor.submit(() -> handleRequest(req));
    // 10 threads handle all 10,000 requests
}
```

### Key Benefits Introduced

| Feature | Benefit |
|---------|---------|
| Thread pools | Reuse threads — avoid creation cost |
| Task queue | Buffer tasks when all threads busy |
| Lifecycle API | `shutdown()`, `awaitTermination()` |
| `Callable` + `Future` | Tasks that return results |
| `ScheduledExecutorService` | Delayed/periodic tasks |

### Package

```text
java.util.concurrent:
  Executor, ExecutorService
  ThreadPoolExecutor
  Executors (factory)
  Future, Callable
  ScheduledExecutorService
```

**Interview Point:**

> Executor Framework = decouple task from thread management. Thread pools, queues, lifecycle, Future. Replaces `new Thread()` in production code.

</details>

---

# 2. Problems with manual thread creation?

<details>
<summary>Show Answer</summary>

**Answer:**

Manual `new Thread()` per task causes **resource exhaustion, no reuse, uncontrolled growth, difficult shutdown, and poor performance** under load.

### Problem 1 — Unlimited Thread Growth

```java
// Web server — one thread per request
while (true) {
    Socket client = server.accept();
    new Thread(() -> handle(client)).start();
}
// 10,000 concurrent requests → 10,000 OS threads → crash
```

### Problem 2 — Thread Creation Cost

```text
Creating thread:
  Allocate stack memory (~1MB per thread)
  OS thread registration
  JVM thread object creation
  Context switch overhead

Destroying thread:
  Cleanup resources
  Very expensive if done per task
```

### Problem 3 — No Reuse

```java
new Thread(task).start(); // thread dies after task
new Thread(task).start(); // new thread created again
// Same work, repeated creation/destruction cost
```

### Problem 4 — Hard to Shutdown

```java
List<Thread> threads = new ArrayList<>();
for (int i = 0; i < 100; i++) {
    Thread t = new Thread(worker);
    threads.add(t);
    t.start();
}
// How to stop all gracefully? No standard API
// thread.stop() is deprecated and dangerous
```

### Problem 5 — No Task Queue

```text
All tasks run immediately in new threads
No buffering when system overloaded
No backpressure mechanism
```

### Problem 6 — No Return Value

```java
new Thread(() -> compute()).start();
// How to get result? Shared variable, complex coordination
```

### Summary Table

| Problem | Impact |
|---------|--------|
| Unlimited threads | OOM, system crash |
| No reuse | Poor performance |
| No queue | No backpressure |
| No shutdown API | Graceful stop hard |
| No Future | Getting results complex |
| Coupling | Task + thread management mixed |

**Interview Point:**

> Manual threads: unlimited growth, no reuse, no queue, hard shutdown, no Future. Executor Framework fixes all of these.

</details>

---

# 3. What is Executor?

<details>
<summary>Show Answer</summary>

**Answer:**

`Executor` is the **root interface** in the Executor Framework—it defines a single method `execute(Runnable)` to run a task asynchronously, decoupling task submission from execution details.

### Interface

```java
public interface Executor {
    void execute(Runnable command);
}
```

### Basic Usage

```java
Executor executor = Executors.newSingleThreadExecutor();

executor.execute(() -> {
    System.out.println("Task running on: " + Thread.currentThread().getName());
});
// Submits task — does not return result
```

### Executor Decouples Submission from Execution

```text
Developer:  submits Runnable task
Executor:   decides HOW and WHEN to run it
            (thread pool, single thread, direct call, etc.)
```

### ThreadPoolExecutor as Executor

```java
Executor executor = Executors.newFixedThreadPool(5);
executor.execute(() -> processOrder(order));
// Executor manages which thread runs the task
```

### Executor vs Direct Thread

```java
// Direct — you manage thread
new Thread(() -> task()).start();

// Executor — executor manages thread
executor.execute(() -> task());
```

### Limitations of Executor

```text
❌ No return value — execute() returns void
❌ No shutdown API
❌ No submit() with Future
→ Use ExecutorService for these features
```

**Interview Point:**

> `Executor` = simplest interface, `execute(Runnable)`. Decouples task from execution. Use `ExecutorService` for shutdown, Future, and more.

</details>

---

# 4. What is ExecutorService?

<details>
<summary>Show Answer</summary>

**Answer:**

`ExecutorService` extends `Executor` with **lifecycle management** (`shutdown`), **Future support** (`submit`), and **batch operations**—the main interface used in production.

### Key Methods

```java
public interface ExecutorService extends Executor {
    // Submit with Future return
    Future<?> submit(Runnable task);
    <T> Future<T> submit(Callable<T> task);

    // Lifecycle
    void shutdown();              // orderly shutdown
    List<Runnable> shutdownNow(); // immediate shutdown
    boolean awaitTermination(long timeout, TimeUnit unit);

    // Batch
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks);
}
```

### submit() vs execute()

```java
ExecutorService executor = Executors.newFixedThreadPool(5);

// execute() — no return value, exception swallowed
executor.execute(() -> doWork());

// submit() — returns Future
Future<String> future = executor.submit(() -> {
    return fetchData(); // Callable — returns result
});
String result = future.get(); // wait for result
```

### Lifecycle Management

```java
ExecutorService executor = Executors.newFixedThreadPool(10);

// Submit tasks
executor.submit(() -> processTask());

// Graceful shutdown
executor.shutdown();           // no new tasks, wait for running to finish
executor.awaitTermination(60, TimeUnit.SECONDS); // wait up to 60s
// if still running after timeout → shutdownNow()
```

### shutdown() vs shutdownNow()

| | `shutdown()` | `shutdownNow()` |
|---|--------------|-----------------|
| New tasks | ❌ Rejected | ❌ Rejected |
| Running tasks | ✅ Continue | ⚠️ Attempt interrupt |
| Queued tasks | ✅ Processed | ❌ Returned as list |
| Use for | Graceful shutdown | Force stop |

### Production Pattern

```java
ExecutorService executor = Executors.newFixedThreadPool(10);
try {
    for (Task task : tasks) {
        executor.submit(() -> process(task));
    }
} finally {
    executor.shutdown();
    if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
        executor.shutdownNow();
    }
}
```

**Interview Point:**

> `ExecutorService` = Executor + shutdown + Future + batch. `submit()` returns Future. Always `shutdown()` + `awaitTermination()` in production.

</details>

---

## Thread Pools

---

# 5. What is Thread Pool?

<details>
<summary>Show Answer</summary>

**Answer:**

A **thread pool** is a collection of **pre-created worker threads** that execute submitted tasks from a queue—threads are **reused** instead of created per task.

### How It Works

```text
Thread Pool (e.g., 5 threads)
    ↓
Task Queue [Task1, Task2, Task3, Task4, ...]
    ↓
Worker threads pick tasks from queue
    ↓
Thread completes task → picks next task from queue
    ↓
Thread reused — NOT destroyed
```

### Visual

```text
Submit: Task1, Task2, Task3, Task4, Task5, Task6

Pool (3 threads):
  Thread-1: Task1 → Task4 → ...
  Thread-2: Task2 → Task5 → ...
  Thread-3: Task3 → Task6 → ...

Task queue buffers overflow tasks
```

### Basic Example

```java
ExecutorService pool = Executors.newFixedThreadPool(3);

for (int i = 0; i < 10; i++) {
    pool.submit(() -> {
        System.out.println("Task on " + Thread.currentThread().getName());
    });
}
// 3 threads handle 10 tasks — threads reused
```

### Thread Pool Components

```text
Core threads:     always alive, even if idle
Max threads:      maximum threads under load
Work queue:       buffers submitted tasks
Thread factory:   creates new threads when needed
Rejection handler: what to do when queue full + max threads
```

**Interview Point:**

> Thread pool = pre-created reusable worker threads + task queue. Submit task → queue → worker picks up → reuse thread. Core of Executor Framework.

</details>

---

# 6. Why Thread Pool?

<details>
<summary>Show Answer</summary>

**Answer:**

Thread pools **reuse threads**, **limit resource usage**, and **improve throughput** by avoiding the expensive cost of creating and destroying threads for every task.

### Why Not new Thread() Per Task?

```text
Thread creation cost:
  ~1MB stack memory per thread
  OS kernel thread creation
  JVM Thread object allocation
  Context switch setup

For 1000 tasks/sec → 1000 thread create/destroy cycles/sec
  → massive overhead, GC pressure, OOM risk
```

### Thread Pool Benefits

```text
Create 10 threads once
Reuse for thousands of tasks
No per-task creation/destruction
Controlled resource usage
```

### Controlled Concurrency

```java
// Without pool — unbounded threads
for (Request r : requests) {
    new Thread(() -> handle(r)).start(); // unlimited!
}

// With pool — controlled
ExecutorService pool = Executors.newFixedThreadPool(20);
for (Request r : requests) {
    pool.submit(() -> handle(r)); // max 20 concurrent
}
```

### Real Production Numbers

```text
Thread creation: ~1ms + 1MB memory
Task execution:  may be 10ms
Ratio: 10% overhead just on thread management!

Pool reuse: 0 creation cost for subsequent tasks
```

### Resource Protection

```text
Database with 50 connections
  → pool size 50 (not unlimited threads all hitting DB)

CPU with 8 cores
  → pool size 8-16 (not 1000 threads competing)
```

**Interview Point:**

> Thread pool avoids per-task thread creation cost. Controls concurrency. Protects resources (DB connections, CPU). Essential for server applications.

</details>

---

# 7. Benefits?

<details>
<summary>Show Answer</summary>

**Answer:**

Thread pools provide **performance, resource control, lifecycle management, and better scalability** compared to manual thread creation.

### Key Benefits

| Benefit | Explanation |
|---------|-------------|
| **Thread reuse** | Avoid creation/destruction overhead |
| **Resource control** | Limit max concurrent threads |
| **Task queuing** | Buffer tasks during peak load |
| **Graceful shutdown** | `shutdown()` + `awaitTermination()` |
| **Future support** | Get task results via `submit()` |
| **Decoupling** | Task logic separate from thread mgmt |
| **Monitoring** | Pool size, queue size, active threads |

### Performance Benefit

```java
// 1000 tasks, 10-thread pool
ExecutorService pool = Executors.newFixedThreadPool(10);
for (int i = 0; i < 1000; i++) {
    pool.submit(() -> processTask(i));
}
// 10 threads reused 100 times each
// vs 1000 thread creations
```

### Backpressure via Queue

```text
Peak load: 1000 tasks submitted
Pool: 10 threads + queue of 100
  → 110 tasks processing/queued
  → 890 tasks rejected (with proper policy)
  → system stays stable vs crashing
```

### Lifecycle Control

```java
pool.shutdown();
pool.awaitTermination(30, TimeUnit.SECONDS);
// Clean shutdown — all tasks complete or timeout
```

### Monitoring (ThreadPoolExecutor)

```java
ThreadPoolExecutor tpe = (ThreadPoolExecutor) pool;
tpe.getPoolSize();        // current thread count
tpe.getActiveCount();     // threads executing tasks
tpe.getQueue().size();    // queued tasks
tpe.getCompletedTaskCount(); // finished tasks
```

**Interview Point:**

> Benefits: reuse, resource limits, queuing, graceful shutdown, Future, decoupling, monitoring. Standard for all server-side Java applications.

</details>

---

## Types of Executors

---

# 8. FixedThreadPool

<details>
<summary>Show Answer</summary>

**Answer:**

`FixedThreadPool` creates a pool with a **fixed number of threads**—if all threads are busy, new tasks wait in an **unbounded queue**.

### Creation

```java
ExecutorService pool = Executors.newFixedThreadPool(5);
// 5 threads always — core = max = 5
// Queue: LinkedBlockingQueue (unbounded)
```

### Behavior

```text
Pool size: fixed at N (never grows or shrinks)
Tasks > N threads: queued in LinkedBlockingQueue
Threads: never die (idle threads stay alive)
Queue: unbounded — can grow indefinitely
```

### Use Cases

```java
// Web request handling — fixed concurrency
ExecutorService pool = Executors.newFixedThreadPool(20);

// Batch processing
ExecutorService pool = Executors.newFixedThreadPool(
    Runtime.getRuntime().availableProcessors()
);
```

### Internal Configuration

```text
corePoolSize  = N
maxPoolSize   = N  (same as core — fixed)
queue         = LinkedBlockingQueue (unbounded)
keepAliveTime = 0 (threads never timeout)
```

### Risk — Unbounded Queue

```java
// ⚠️ If tasks submitted faster than processed:
// Queue grows indefinitely → OutOfMemoryError
ExecutorService pool = Executors.newFixedThreadPool(5);
// Millions of tasks submitted → millions queued → OOM
```

### Production Recommendation

```java
// Use bounded queue with custom ThreadPoolExecutor
ThreadPoolExecutor pool = new ThreadPoolExecutor(
    5, 5, 0L, TimeUnit.MILLISECONDS,
    new ArrayBlockingQueue<>(100), // bounded queue!
    new ThreadPoolExecutor.CallerRunsPolicy()
);
```

**Interview Point:**

> FixedThreadPool = fixed N threads + unbounded queue. Simple but queue can grow → OOM. Prefer custom ThreadPoolExecutor with bounded queue in production.

</details>

---

# 9. CachedThreadPool

<details>
<summary>Show Answer</summary>

**Answer:**

`CachedThreadPool` creates **new threads as needed** and reuses idle threads—pool grows unboundedly under load and shrinks when threads are idle for 60 seconds.

### Creation

```java
ExecutorService pool = Executors.newCachedThreadPool();
// core=0, max=Integer.MAX_VALUE
// SynchronousQueue — no buffering
```

### Behavior

```text
No core threads (corePoolSize = 0)
New thread created for each task if no idle thread
Idle threads die after 60 seconds
Max threads: Integer.MAX_VALUE — essentially unlimited!
Queue: SynchronousQueue — handoff directly to thread
```

### Use Cases

```java
// Short-lived async tasks
ExecutorService pool = Executors.newCachedThreadPool();
pool.submit(() -> sendEmail(notification));
pool.submit(() -> logEvent(event));
```

### Risk — Thread Explosion

```java
// ⚠️ DANGEROUS under heavy load
ExecutorService pool = Executors.newCachedThreadPool();
for (int i = 0; i < 100000; i++) {
    pool.submit(() -> slowDatabaseCall());
}
// Creates up to 100,000 threads → OOM crash!
```

### vs FixedThreadPool

| | FixedThreadPool | CachedThreadPool |
|---|-----------------|------------------|
| Pool size | Fixed N | 0 to MAX_INT |
| Queue | Unbounded LinkedBlockingQueue | SynchronousQueue (no queue) |
| Idle threads | Stay alive | Die after 60s |
| Risk | Queue OOM | Thread count OOM |
| Use for | Steady workload | Short burst tasks |

### Deprecated in Java 21+

```text
Executors.newCachedThreadPool() discouraged
  → unbounded thread growth risk
  → use newFixedThreadPool with bounded queue instead
```

**Interview Point:**

> CachedThreadPool = unlimited threads, no queue. Dangerous under load — thread explosion. Avoid in production. Use fixed pool with bounded queue.

</details>

---

# 10. SingleThreadExecutor

<details>
<summary>Show Answer</summary>

**Answer:**

`SingleThreadExecutor` uses **exactly one worker thread** that processes tasks **sequentially** from a queue—guarantees tasks execute in submission order.

### Creation

```java
ExecutorService executor = Executors.newSingleThreadExecutor();
// 1 thread, unbounded queue
```

### Behavior

```text
Single worker thread
Tasks processed one at a time — sequential
Queue: LinkedBlockingQueue (unbounded)
Tasks never run in parallel
Order preserved — FIFO execution
```

### Use Cases

```java
// Sequential event processing
ExecutorService executor = Executors.newSingleThreadExecutor();
executor.submit(() -> writeToLog(event1));
executor.submit(() -> writeToLog(event2));
// Guaranteed order — no concurrent log writes

// Background task — one at a time
executor.submit(() -> sendDailyReport());

// Replace synchronized block for async sequential work
executor.submit(() -> updateState());
```

### vs new Thread()

```java
// ❌ new Thread per task — no ordering guarantee
new Thread(task1).start();
new Thread(task2).start();

// ✅ SingleThreadExecutor — ordered, reusable
ExecutorService exec = Executors.newSingleThreadExecutor();
exec.submit(task1);
exec.submit(task2); // task2 runs after task1 completes
```

### Internal Configuration

```text
corePoolSize  = 1
maxPoolSize   = 1
queue         = LinkedBlockingQueue (unbounded)
keepAliveTime = 0
```

### When to Use

```text
✅ Tasks must run sequentially
✅ Order matters
✅ Background single-worker processing
✅ Replace Timer for task scheduling (with ScheduledExecutorService)
❌ Parallel processing needed → use FixedThreadPool
```

**Interview Point:**

> SingleThreadExecutor = 1 thread, sequential, ordered execution. Good for log writing, state updates. Not for parallel work.

</details>

---

# 11. ScheduledThreadPool

<details>
<summary>Show Answer</summary>

**Answer:**

`ScheduledThreadPool` executes tasks **after a delay** or **periodically**—replacement for `Timer` with better thread pool management.

### Creation

```java
ScheduledExecutorService scheduler =
    Executors.newScheduledThreadPool(3);
```

### Schedule Once — Delayed Execution

```java
scheduler.schedule(
    () -> System.out.println("Runs after 5 seconds"),
    5, TimeUnit.SECONDS
);
```

### Schedule At Fixed Rate — Periodic

```java
// Every 10 seconds — regardless of task duration
scheduler.scheduleAtFixedRate(
    () -> checkHealth(),
    0, 10, TimeUnit.SECONDS
);
```

### Schedule With Fixed Delay — Gap Between Runs

```java
// 10 seconds AFTER previous task completes
scheduler.scheduleWithFixedDelay(
    () -> cleanupTempFiles(),
    0, 10, TimeUnit.SECONDS
);
```

### fixedRate vs fixedDelay

| | `scheduleAtFixedRate` | `scheduleWithFixedDelay` |
|---|----------------------|--------------------------|
| Timing | Fixed interval from start | Delay after previous completes |
| Task runs long | Tasks may overlap | No overlap — waits after finish |
| Use for | Heartbeats, polling | Cleanup, maintenance |

### vs Timer

```java
// ❌ Timer — single thread, exception kills timer
Timer timer = new Timer();
timer.schedule(task, 5000);

// ✅ ScheduledExecutorService — thread pool, exception-safe
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
scheduler.schedule(task, 5, TimeUnit.SECONDS);
```

### Use Cases

```text
✅ Health checks every N seconds
✅ Cache refresh periodically
✅ Report generation on schedule
✅ Session timeout cleanup
✅ Retry with delay
```

**Interview Point:**

> ScheduledThreadPool = delayed and periodic tasks. `scheduleAtFixedRate` vs `scheduleWithFixedDelay`. Replaces Timer. Use for polling, cleanup, heartbeats.

</details>

---

## Advanced

---

# 12. How ThreadPoolExecutor works?

<details>
<summary>Show Answer</summary>

**Answer:**

`ThreadPoolExecutor` is the core implementation—it manages a **pool of worker threads**, a **task queue**, and applies rules to decide when to create threads, queue tasks, or reject them.

### Task Submission Flow

```text
Task submitted
    ↓
Active threads < corePoolSize?
  YES → create new thread, run task
  NO  ↓
Queue not full?
  YES → add task to queue
  NO  ↓
Active threads < maxPoolSize?
  YES → create new thread, run task
  NO  ↓
Execute RejectedExecutionHandler
```

### Visual Flow

```text
core=2, max=4, queue=2

Submit T1: threads=0 < core=2 → create Thread-1, run T1
Submit T2: threads=1 < core=2 → create Thread-2, run T2
Submit T3: threads=2 = core → queue T3
Submit T4: queue T4
Submit T5: queue full, threads=2 < max=4 → create Thread-3, run T5
Submit T6: queue full, threads=3 < max=4 → create Thread-4, run T6
Submit T7: queue full, threads=4 = max → REJECT T7
```

### Key Components

```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    2,                              // corePoolSize
    4,                              // maximumPoolSize
    60L, TimeUnit.SECONDS,          // keepAliveTime
    new ArrayBlockingQueue<>(2),    // workQueue
    Executors.defaultThreadFactory(),
    new ThreadPoolExecutor.AbortPolicy() // rejectionHandler
);
```

### Thread Lifecycle in Pool

```text
Core threads: created on demand, never die (keepAlive=0 for core)
Extra threads: created when queue full, die after keepAliveTime idle
Worker thread: loops — get task from queue → execute → repeat
```

**Interview Point:**

> ThreadPoolExecutor flow: fill core threads → queue → create extra threads up to max → reject. Know this flow for interviews — core, queue, max, reject.

</details>

---

# 13. Core Pool Size?

<details>
<summary>Show Answer</summary>

**Answer:**

**Core pool size** is the number of threads the pool **maintains as minimum**—threads are created up to core size on demand and stay alive even when idle (unless `allowCoreThreadTimeOut` is set).

### Definition

```text
corePoolSize = minimum threads always kept in pool
Tasks submitted → create threads up to core size first
Core threads process tasks OR wait in pool for new tasks
Core threads NOT destroyed when idle (by default)
```

### Example — corePoolSize=3

```java
ThreadPoolExecutor pool = new ThreadPoolExecutor(
    3, 10, 60L, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(5)
);

// Submit 1 task → 1 thread created (1 < core=3)
// Submit 2 more → 2 more threads (3 = core)
// Submit 4th → queued (all 3 core threads busy)
// No more tasks → 3 threads stay alive, waiting
```

### Core vs Max

```text
corePoolSize ≤ maximumPoolSize always

corePoolSize: threads created first, kept alive
maxPoolSize:  extra threads only when queue is full
```

### Setting Core Pool Size

```text
CPU-bound tasks:  core = number of CPU cores
IO-bound tasks:   core = higher (cores * 2 or more)
Steady workload:  core = expected concurrent tasks
```

### allowCoreThreadTimeOut

```java
pool.allowCoreThreadTimeOut(true);
// Core threads also die after keepAliveTime when idle
// Pool can shrink to 0 threads when no work
```

**Interview Point:**

> corePoolSize = minimum threads, created first, kept alive. Tasks queue only after core threads are all busy. Set based on workload type.

</details>

---

# 14. Maximum Pool Size?

<details>
<summary>Show Answer</summary>

**Answer:**

**Maximum pool size** is the **upper limit** on threads the pool can create—extra threads beyond core size are created only when the queue is full.

### Definition

```text
maxPoolSize = absolute maximum threads
Extra threads (beyond core) created ONLY when:
  1. All core threads busy
  2. Task queue is FULL
Extra threads die after keepAliveTime when idle
```

### Example — core=2, max=5, queue=3

```text
T1,T2 → create 2 core threads
T3,T4,T5 → queue (3 slots)
T6 → queue full, create thread 3 (extra)
T7 → queue full, create thread 4 (extra)
T8 → queue full, create thread 5 (extra) = max reached
T9 → REJECTED
```

### max = core (FixedThreadPool)

```java
Executors.newFixedThreadPool(5);
// core=5, max=5 — never creates extra threads
// Always queue when all 5 busy
```

### max > core (Flexible Pool)

```java
new ThreadPoolExecutor(2, 10, 60L, SECONDS, new ArrayBlockingQueue<>(5));
// Normal: 2 core threads + queue
// Burst:   up to 10 threads when queue full
// Idle extra threads die after 60s
```

### Setting Max Pool Size

```text
CPU-bound:  max ≈ number of cores (no benefit from more threads)
IO-bound:   max can be much higher (threads wait on IO)
Safety cap: prevent runaway thread creation
```

### keepAliveTime for Extra Threads

```text
Extra threads (beyond core): die after keepAliveTime idle
Core threads: stay alive (unless allowCoreThreadTimeOut=true)
```

**Interview Point:**

> maxPoolSize = hard cap on threads. Extra threads only when queue full. Die after keepAliveTime. FixedThreadPool: max=core. CachedThreadPool: max=Integer.MAX_VALUE (dangerous).

</details>

---

# 15. Queue Capacity?

<details>
<summary>Show Answer</summary>

**Answer:**

The **work queue** buffers tasks when all core threads are busy—queue type and capacity determine backpressure behavior and when extra threads are created.

### Queue Types

| Queue Type | Capacity | Behavior |
|------------|----------|----------|
| `LinkedBlockingQueue` | Unbounded (default in FixedThreadPool) | Never rejects — grows until OOM |
| `ArrayBlockingQueue` | Bounded (fixed capacity) | Blocks/rejects when full |
| `SynchronousQueue` | Zero capacity (CachedThreadPool) | Direct handoff — no buffering |
| `PriorityBlockingQueue` | Unbounded | Priority ordering |
| `DelayedWorkQueue` | Unbounded | Scheduled tasks |

### Bounded Queue — Production Safe

```java
ThreadPoolExecutor pool = new ThreadPoolExecutor(
    5, 10, 60L, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(100) // max 100 queued tasks
);
// After 100 queued → create extra threads up to max
// After max threads + full queue → reject
```

### Unbounded Queue Risk

```java
// FixedThreadPool — unbounded queue
Executors.newFixedThreadPool(5);
// Queue grows forever under sustained overload → OOM
// Never creates threads beyond core (queue always accepts)
```

### SynchronousQueue — CachedThreadPool

```text
Zero capacity — task must be immediately handed to thread
No buffering → creates new thread for each task if all busy
→ thread explosion under load
```

### Queue Capacity Tuning

```text
Small queue + higher max:
  → more threads created under burst
  → faster response, more memory

Large queue + lower max:
  → tasks wait in queue
  → fewer threads, more latency

Bounded queue:
  → backpressure — reject when overwhelmed
  → system stays stable
```

### Monitoring Queue

```java
ThreadPoolExecutor tpe = (ThreadPoolExecutor) pool;
int queued = tpe.getQueue().size();
if (queued > 80) {
    log.warn("Thread pool queue near capacity: {}", queued);
}
```

**Interview Point:**

> Queue buffers tasks when core threads busy. Use **bounded ArrayBlockingQueue** in production. Unbounded queue → OOM. SynchronousQueue → no buffering, thread explosion.

</details>

---

# 16. Rejection Policy?

<details>
<summary>Show Answer</summary>

**Answer:**

A **rejection policy** (RejectedExecutionHandler) defines what happens when the pool **cannot accept a new task**—all core threads busy, queue full, and max threads reached.

### When Rejection Happens

```text
corePoolSize threads: all busy
workQueue: full
maxPoolSize threads: all busy
New task submitted → REJECTED
```

### Four Built-in Policies

| Policy | Behavior |
|--------|----------|
| `AbortPolicy` | Throw `RejectedExecutionException` |
| `CallerRunsPolicy` | Caller thread runs the task |
| `DiscardPolicy` | Silently drop the task |
| `DiscardOldestPolicy` | Drop oldest queued task, retry new |

### Setting Policy

```java
ThreadPoolExecutor pool = new ThreadPoolExecutor(
    2, 4, 60L, TimeUnit.SECONDS,
    new ArrayBlockingQueue<>(2),
    new ThreadPoolExecutor.CallerRunsPolicy() // rejection policy
);
```

### Custom Policy

```java
ThreadPoolExecutor pool = new ThreadPoolExecutor(
    2, 4, 60L, SECONDS,
    new ArrayBlockingQueue<>(10),
    (r, executor) -> {
        log.warn("Task rejected — saving to dead letter queue");
        deadLetterQueue.add(r);
    }
);
```

### Choosing Policy

```text
AbortPolicy:       fail fast — caller handles exception
CallerRunsPolicy:  backpressure — slows down submitter
DiscardPolicy:     fire-and-forget — acceptable loss
DiscardOldestPolicy: keep latest — real-time systems
Custom:            log, retry, dead letter queue
```

**Interview Point:**

> Rejection when core busy + queue full + max threads reached. Default: AbortPolicy (throws exception). CallerRunsPolicy for backpressure. Know all four policies.

</details>

---

## Rejection Policies

---

# 17. AbortPolicy

<details>
<summary>Show Answer</summary>

**Answer:**

`AbortPolicy` is the **default rejection policy**—throws `RejectedExecutionException` when the pool cannot accept a task, forcing the caller to handle the failure.

### Behavior

```java
ThreadPoolExecutor pool = new ThreadPoolExecutor(
    1, 1, 0L, TimeUnit.MILLISECONDS,
    new ArrayBlockingQueue<>(1),
    new ThreadPoolExecutor.AbortPolicy() // default
);

pool.submit(task1); // runs
pool.submit(task2); // queued
pool.submit(task3); // REJECTED — throws RejectedExecutionException
```

### Handling the Exception

```java
try {
    pool.submit(() -> processOrder(order));
} catch (RejectedExecutionException e) {
    log.error("Pool overloaded — order rejected: {}", order.getId());
    // Retry later, save to DB, alert ops team
    retryQueue.add(order);
}
```

### When to Use

```text
✅ Fail-fast — caller must know task was rejected
✅ Critical tasks — cannot silently lose
✅ When caller has retry logic
✅ Default — safe choice when unsure
```

### AbortPolicy Characteristics

```text
Throws: RejectedExecutionException
Task:   NOT executed
Caller: Must catch and handle
Risk:   Unhandled exception crashes caller thread
```

**Interview Point:**

> AbortPolicy = default, throws RejectedExecutionException. Fail-fast. Caller must handle. Use when task loss is unacceptable without notification.

</details>

---

# 18. CallerRunsPolicy

<details>
<summary>Show Answer</summary>

**Answer:**

`CallerRunsPolicy` runs the rejected task in the **calling thread** itself—providing natural **backpressure** by slowing down the task submitter.

### Behavior

```java
ThreadPoolExecutor pool = new ThreadPoolExecutor(
    2, 2, 0L, TimeUnit.MILLISECONDS,
    new ArrayBlockingQueue<>(2),
    new ThreadPoolExecutor.CallerRunsPolicy()
);

// Pool full + queue full
pool.submit(task4);
// task4 runs in CALLER's thread — not pool thread
// Caller blocked until task4 completes
// Natural slowdown — backpressure
```

### Backpressure Effect

```text
System overloaded:
  Pool threads: all busy
  Queue: full
  New submit: CallerRunsPolicy kicks in
  Caller thread runs task itself → slowed down
  Fewer new tasks submitted → system recovers
```

### When to Use

```text
✅ Backpressure desired — slow down producer when overloaded
✅ Cannot lose tasks — task still runs (in caller thread)
✅ Web servers — caller thread handles overflow
✅ Most common production choice for bounded pools
```

### Example — Web Request Overflow

```java
ThreadPoolExecutor pool = new ThreadPoolExecutor(
    10, 20, 60L, SECONDS,
    new ArrayBlockingQueue<>(50),
    new ThreadPoolExecutor.CallerRunsPolicy()
);
// When pool saturated: request handled in servlet thread
// Servlet thread blocked → fewer new requests accepted
// Natural throttling without dropping requests
```

### Risk

```text
⚠️ Caller thread blocked — may affect caller's other responsibilities
⚠️ If caller is main/UI thread — bad UX
✅ If caller is worker thread — acceptable backpressure
```

**Interview Point:**

> CallerRunsPolicy = rejected task runs in caller thread. Natural backpressure. Most popular production policy. Task not lost — just slower.

</details>

---

# 19. DiscardPolicy

<details>
<summary>Show Answer</summary>

**Answer:**

`DiscardPolicy` **silently drops** the rejected task without throwing an exception or notifying the caller—the task is simply lost.

### Behavior

```java
ThreadPoolExecutor pool = new ThreadPoolExecutor(
    1, 1, 0L, TimeUnit.MILLISECONDS,
    new ArrayBlockingQueue<>(1),
    new ThreadPoolExecutor.DiscardPolicy()
);

pool.submit(task1); // runs
pool.submit(task2); // queued
pool.submit(task3); // SILENTLY DROPPED — no exception, no execution
// Caller has no idea task3 was lost!
```

### When to Use

```text
✅ Non-critical tasks — metrics, logging, notifications
✅ Fire-and-forget where loss is acceptable
✅ High-frequency events — missing one is OK
❌ NEVER for financial, order, payment tasks
```

### Example — Metrics Collection

```java
// Missing one metric sample is acceptable
ThreadPoolExecutor metricsPool = new ThreadPoolExecutor(
    2, 4, 30L, SECONDS,
    new ArrayBlockingQueue<>(100),
    new ThreadPoolExecutor.DiscardPolicy()
);
metricsPool.submit(() -> recordMetric("cpu", cpuUsage));
// If pool overloaded — drop metric, don't crash system
```

### Danger

```java
// ❌ DANGEROUS — silent task loss
pool.submit(() -> processPayment(order));
// Payment silently dropped — customer charged but not processed!
```

### vs AbortPolicy

| | DiscardPolicy | AbortPolicy |
|---|---------------|-------------|
| On reject | Silent drop | Throws exception |
| Caller knows? | ❌ No | ✅ Yes |
| Task runs? | ❌ Lost | ❌ Lost (but caller handles) |

**Interview Point:**

> DiscardPolicy = silent drop, no exception. Only for non-critical fire-and-forget tasks. Never for business-critical operations.

</details>

---

# 20. DiscardOldestPolicy

<details>
<summary>Show Answer</summary>

**Answer:**

`DiscardOldestPolicy` drops the **oldest queued task** and retries submitting the new task—keeps the queue fresh with the most recent tasks.

### Behavior

```java
ThreadPoolExecutor pool = new ThreadPoolExecutor(
    1, 1, 0L, TimeUnit.MILLISECONDS,
    new ArrayBlockingQueue<>(2),
    new ThreadPoolExecutor.DiscardOldestPolicy()
);

pool.submit(task1); // runs on thread
pool.submit(task2); // queued (oldest in queue)
pool.submit(task3); // queued
pool.submit(task4); // task2 DROPPED, task4 queued
// Queue now: [task3, task4] — task2 lost
```

### Flow

```text
Pool full + queue full
  → remove HEAD of queue (oldest task)
  → retry submitting new task
  → new task added to queue tail
```

### When to Use

```text
✅ Real-time systems — latest data more important than old
✅ Sensor data — old readings less valuable
✅ UI updates — show latest state, skip stale updates
✅ Market data feeds — latest price matters most
```

### Example — Real-Time Price Updates

```java
ThreadPoolExecutor pricePool = new ThreadPoolExecutor(
    1, 2, 30L, SECONDS,
    new ArrayBlockingQueue<>(10),
    new ThreadPoolExecutor.DiscardOldestPolicy()
);

// Stream of price updates — old prices less important
pricePool.submit(() -> updateUI(latestPrice));
// If overloaded: drop old price update, show latest
```

### Risk

```text
⚠️ Oldest task silently dropped — may be important
⚠️ No notification to caller
⚠️ Ordering not guaranteed under overload
```

### vs Other Policies

| Policy | What Gets Lost |
|--------|----------------|
| AbortPolicy | New task (exception thrown) |
| DiscardPolicy | New task (silent) |
| DiscardOldestPolicy | Oldest queued task (silent) |
| CallerRunsPolicy | Nothing — caller runs new task |

**Interview Point:**

> DiscardOldestPolicy = drop oldest queued task, accept new one. For real-time/latest-data scenarios. Old tasks less valuable than new ones.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: submit() vs execute()?

<details>
<summary>Show Answer</summary>

**Answer:**

| | `execute(Runnable)` | `submit(Callable/Runnable)` |
|---|---------------------|----------------------------|
| Returns | void | `Future<T>` |
| Exception handling | Swallowed — uncaught | Wrapped in Future |
| Use for | Fire-and-forget | Need result or exception |

```java
Future<String> f = executor.submit(() -> fetchData());
String result = f.get();
```

</details>

---

### Q: How to choose pool size?

<details>
<summary>Show Answer</summary>

**Answer:**

```text
CPU-bound:  pool size = CPU cores (or cores + 1)
IO-bound:   pool size = cores * (1 + wait/compute ratio)
            e.g., 8 cores, 90% IO wait → ~80 threads
General:    start with cores*2, monitor, tune
```

</details>

---

### Q: shutdown() not called — problem?

<details>
<summary>Show Answer</summary>

**Answer:**

Pool threads stay alive → **application won't exit**. JVM waits for non-daemon pool threads. Always call `shutdown()` + `awaitTermination()` in finally block or `@PreDestroy`.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Executor Framework = thread pools + task queue + lifecycle. Flow: core threads → queue → max threads → reject. FixedThreadPool for steady load. Bounded queue + CallerRunsPolicy for production. Always shutdown gracefully.

</details>
