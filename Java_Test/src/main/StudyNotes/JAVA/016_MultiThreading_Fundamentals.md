# 16. Multithreading Fundamentals

## 16. Multithreading Fundamentals

## Basics

---

# 1. What is a Thread?

<details>
<summary>Show Answer</summary>

**Answer:**

A **thread** is the **smallest unit of execution** within a process—a lightweight, independent path of execution that shares the process's memory but has its own stack and program counter.

### Simple Definition

```text
Process = program in execution (has its own memory)
Thread  = lightweight unit inside a process (shares memory)
```

### Key Characteristics

| Feature | Detail |
|---------|--------|
| Unit of execution | Smallest schedulable unit by OS |
| Memory | Shares heap with other threads in same process |
| Own stack | Each thread has separate stack |
| Concurrent | Multiple threads run seemingly at same time |
| Lightweight | Cheaper to create than new process |

### Example

```java
Thread t = new Thread(() -> {
    System.out.println("Running in thread: " + Thread.currentThread().getName());
});
t.start();
```

### Thread vs Main Thread

```java
public static void main(String[] args) {
    // main() runs on "main" thread
    System.out.println(Thread.currentThread().getName()); // main

    Thread worker = new Thread(() -> {
        System.out.println(Thread.currentThread().getName()); // Thread-0
    });
    worker.start();
}
```

### What Threads Share vs Own

```text
SHARED (same process):  Heap memory, static variables, class data
PER THREAD:             Stack, program counter, local variables
```

**Interview Point:**

> Thread = lightweight execution unit inside a process. Shares heap, has own stack. Enables concurrent execution within one application.

</details>

---

# 2. Process vs Thread?

<details>
<summary>Show Answer</summary>

**Answer:**

A **process** is an independent program with its own memory space. A **thread** is a unit of execution **inside** a process that shares memory with other threads.

### Comparison Table

| Feature | Process | Thread |
|---------|---------|--------|
| Definition | Independent running program | Execution unit inside process |
| Memory | Separate memory space | Shares process heap |
| Creation cost | Heavy (OS allocates memory) | Light |
| Communication | IPC (pipes, sockets) — slow | Shared memory — fast |
| Crash impact | One process crash ≠ other | One thread crash can kill process |
| Isolation | High | Low |
| Context switch | Expensive | Cheaper |

### Visual

```text
Process A                    Process B
┌─────────────────┐         ┌─────────────────┐
│ Thread 1        │         │ Thread 1        │
│ Thread 2        │         │ Thread 2        │
│ Shared Heap     │         │ Shared Heap     │
└─────────────────┘         └─────────────────┘
  Separate memory             Separate memory
```

```text
Single Process — Multiple Threads
┌──────────────────────────────────┐
│  Thread 1  │  Thread 2  │ Thread 3 │
│  (own stack) (own stack) (own stack)│
│         SHARED HEAP                │
└──────────────────────────────────┘
```

### Real Example

```text
Chrome browser:
  Process = one Chrome instance
  Threads = UI thread, network thread, rendering thread per tab

Java application:
  Process = JVM instance
  Threads = main thread, GC thread, worker threads
```

### When to Use

| Use Process | Use Thread |
|-------------|------------|
| Strong isolation needed | Shared data, fast communication |
| Separate applications | Concurrent tasks in same app |
| Crash must not affect others | Parallel processing same data |

**Interview Point:**

> Process = isolated program with own memory. Thread = shared-memory concurrent execution inside process. Threads lighter, faster communication.

</details>

---

# 3. Why multithreading is required?

<details>
<summary>Show Answer</summary>

**Answer:**

Multithreading is required to **utilize multiple CPU cores**, improve **responsiveness**, and handle **concurrent tasks** efficiently within a single application.

### Key Reasons

| Reason | Explanation |
|--------|-------------|
| **CPU utilization** | Modern CPUs have multiple cores — one thread wastes cores |
| **Responsiveness** | UI stays responsive while background work runs |
| **Concurrent I/O** | Handle multiple requests/users simultaneously |
| **Throughput** | Process more work in same time |
| **Resource sharing** | Threads share memory — efficient data access |

### Without Multithreading

```text
Single thread app on 8-core CPU:
  → Uses 1 core, 7 cores idle
  → User clicks button → UI freezes during heavy work
  → Server handles 1 request at a time
```

### With Multithreading

```text
Web server:
  → Thread per request → handle 1000 concurrent users
  → UI thread + background worker → responsive app
  → Parallel data processing → faster batch jobs
```

### Real Production Examples

```java
// Web server — thread per request
ExecutorService pool = Executors.newFixedThreadPool(50);
pool.submit(() -> handleHttpRequest(request));

// UI application — background task
new Thread(() -> downloadFile()).start();
// UI thread continues responding to clicks

// Parallel processing
list.parallelStream().map(processItem).collect(toList());
```

### When Single Thread Is Enough

```text
Simple CLI scripts
Sequential batch with no I/O wait
Single-core embedded systems (rare today)
```

**Interview Point:**

> Multithreading = use all CPU cores + concurrent I/O + responsive UI. Essential for servers, modern apps, and parallel processing.

</details>

---

# 4. Advantages of multithreading?

<details>
<summary>Show Answer</summary>

**Answer:**

Multithreading improves **performance**, **responsiveness**, and **resource utilization** by running multiple tasks concurrently.

### Key Advantages

| Advantage | Benefit |
|-----------|---------|
| **Better CPU utilization** | All cores work in parallel |
| **Improved responsiveness** | UI doesn't freeze during background work |
| **Higher throughput** | More requests/tasks per second |
| **Efficient resource sharing** | Threads share heap — no IPC overhead |
| **Faster I/O handling** | One thread waits on I/O, others continue |
| **Modular design** | Separate concerns in separate threads |

### Performance — Multi-Core

```java
// 4-core CPU — parallel stream uses all cores
bigList.parallelStream()
       .map(expensiveComputation)
       .collect(Collectors.toList());
// Near 4x speedup vs single thread
```

### Responsiveness

```java
// Swing/JavaFX — never block UI thread
button.onClick(() -> {
    new Thread(() -> {
        String result = callSlowAPI();
        updateUI(result);
    }).start();
});
```

### Throughput — Web Server

```text
Single thread:  100 requests/sec
50 threads:     5000 requests/sec (with pool)
```

### I/O Concurrency

```text
Thread 1: waiting for DB response
Thread 2: waiting for HTTP response
Thread 3: processing data
→ CPU not idle while threads wait on I/O
```

### Disadvantages to Mention (Balanced Answer)

```text
Complexity — race conditions, deadlocks
Debugging harder — non-deterministic behavior
Context switch overhead
Thread safety required for shared data
```

**Interview Point:**

> Advantages: CPU utilization, responsiveness, throughput, shared memory efficiency. Senior answer mentions trade-offs too.

</details>

---

# 5. User thread vs daemon thread?

<details>
<summary>Show Answer</summary>

**Answer:**

**User threads** keep the JVM alive until they finish. **Daemon threads** are background threads—the JVM exits when **only daemon threads** remain.

### Comparison

| | User Thread | Daemon Thread |
|---|-------------|---------------|
| Default | Yes (default type) | Must set explicitly |
| JVM waits? | Yes — JVM waits for completion | No — JVM exits when user threads end |
| Use case | Main work, business logic | Background services (GC, monitoring) |
| Set daemon | `setDaemon(false)` default | `setDaemon(true)` before start |

### User Thread Example

```java
Thread userThread = new Thread(() -> {
    System.out.println("User thread running");
    Thread.sleep(5000);
});
userThread.start();
// JVM waits 5 seconds for this thread before exiting
```

### Daemon Thread Example

```java
Thread daemon = new Thread(() -> {
    while (true) {
        System.out.println("Daemon running...");
        Thread.sleep(1000);
    }
});
daemon.setDaemon(true);  // MUST set before start()
daemon.start();

// Main ends → JVM exits even though daemon still running
```

### Important Rules

```java
// ❌ Cannot set daemon after start
thread.start();
thread.setDaemon(true); // IllegalThreadStateException

// ✅ Set before start
thread.setDaemon(true);
thread.start();
```

### JVM Exit Rule

```text
JVM exits when:
  → All USER threads have terminated
  → Daemon threads are killed automatically on JVM exit
```

### Real Examples

```text
User threads:  HTTP request handlers, main business logic
Daemon threads: JVM GC thread, JIT compiler, finalizer thread
```

**Interview Point:**

> User thread = JVM waits. Daemon = background, JVM doesn't wait. Set `setDaemon(true)` **before** `start()`.

</details>

---

# 6. How to create a thread?

<details>
<summary>Show Answer</summary>

**Answer:**

Java provides **multiple ways** to create threads—from extending `Thread`, implementing `Runnable`, using lambdas, to `ExecutorService` and `Callable`.

### All Ways (Overview)

| # | Method | Modern? |
|---|--------|---------|
| 1 | Extend `Thread` class | Legacy |
| 2 | Implement `Runnable` | Common |
| 3 | Lambda on `Runnable` | Java 8+ |
| 4 | `ExecutorService` | **Preferred (production)** |
| 5 | `Callable` + `Future` | When return value needed |

### Quick Examples

```java
// 1. Thread class
new MyThread().start();

// 2. Runnable
new Thread(new MyRunnable()).start();

// 3. Lambda
new Thread(() -> System.out.println("Hi")).start();

// 4. ExecutorService (preferred)
executor.submit(() -> System.out.println("Hi"));

// 5. Callable
Future<String> future = executor.submit(() -> "result");
```

### Always Call start() — Not run()

```java
Thread t = new Thread(() -> System.out.println("Hello"));

t.run();   // ❌ runs in CURRENT thread — not new thread
t.start(); // ✅ creates new thread and runs
```

### Production Recommendation

```text
Avoid: new Thread() directly — no pool management
Use:   ExecutorService thread pool — reuse threads, control concurrency
```

**Interview Point:**

> Five ways: Thread class, Runnable, Lambda, ExecutorService, Callable. Production = **ExecutorService**. Always `start()` not `run()`.

</details>

---

# 7. Thread class vs Runnable interface?

<details>
<summary>Show Answer</summary>

**Answer:**

**Thread class** approach extends `Thread` and overrides `run()`. **Runnable interface** approach implements `run()` and passes to `Thread`—Runnable is preferred because it avoids limiting inheritance.

### Thread Class Approach

```java
class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("Thread class approach");
    }
}

MyThread t = new MyThread();
t.start();
```

### Runnable Interface Approach

```java
class MyTask implements Runnable {
    @Override
    public void run() {
        System.out.println("Runnable approach");
    }
}

Thread t = new Thread(new MyTask());
t.start();
```

### Comparison

| | Extend `Thread` | Implement `Runnable` |
|---|-----------------|----------------------|
| Inheritance | Uses extends — limited | Uses interface — flexible |
| Reuse | Task tied to Thread class | Runnable can run on any executor |
| Multiple inheritance | Can't extend another class | Can extend other classes |
| ExecutorService | Awkward | Natural fit |
| Recommended | ❌ No | ✅ Yes |

### Why Runnable Is Better

```text
1. Java single inheritance — extending Thread wastes extends slot
2. Separation of task (Runnable) from execution (Thread/Executor)
3. Same Runnable can run on Thread, ExecutorService, parallel stream
4. OOP design — task is not a thread, task IS RUN BY a thread
```

```java
// Same task — multiple execution options
Runnable task = () -> processOrder();

new Thread(task).start();           // manual thread
executor.submit(task);                // thread pool
CompletableFuture.runAsync(task);     // async framework
```

**Interview Point:**

> Prefer **Runnable** over extending Thread. Separates task from execution thread. Enables ExecutorService and better OOP design.

</details>

---

# 8. Which approach is preferred and why?

<details>
<summary>Show Answer</summary>

**Answer:**

**ExecutorService** with **Runnable/Callable** is the preferred approach in production—not raw `Thread` creation.

### Preference Ranking

```text
1. ExecutorService + Callable   → production (return values, pool)
2. ExecutorService + Runnable   → production (fire-and-forget tasks)
3. Runnable + Lambda            → simple cases, tests
4. Extend Thread class          → avoid in production
```

### Why ExecutorService?

| Benefit | Detail |
|---------|--------|
| **Thread reuse** | Pool reuses threads — no create/destroy overhead |
| **Concurrency control** | Fixed pool size limits resource usage |
| **Task queue** | Handles burst of tasks gracefully |
| **Lifecycle management** | `shutdown()`, `awaitTermination()` |
| **Return values** | `Callable` + `Future` |
| **Framework integration** | Spring, Java EE use executor patterns |

### Production Pattern

```java
ExecutorService executor = Executors.newFixedThreadPool(10);

// Submit tasks
executor.submit(() -> processOrder(order));
executor.submit(() -> sendEmail(user));

// Shutdown gracefully
executor.shutdown();
executor.awaitTermination(60, TimeUnit.SECONDS);
```

### Why NOT new Thread() Every Time

```text
❌ Creates new OS thread per task — expensive
❌ No limit — 10,000 requests = 10,000 threads → OOM
❌ No reuse — thread creation/destruction overhead
❌ Hard to manage lifecycle
```

### Why NOT Extend Thread

```text
❌ Wastes inheritance slot
❌ Task coupled to Thread class
❌ Can't use thread pools easily
```

**Interview Point:**

> Production = **ExecutorService** + Runnable/Callable. Never `new Thread()` per request in servers. Pool size controls concurrency.

</details>

---

### Thread Creation

---

# 9. Creating thread using Thread class?

<details>
<summary>Show Answer</summary>

**Answer:**

Extend the `Thread` class, override `run()`, create instance, and call `start()`—not `run()` directly.

### Full Example

```java
class DownloadThread extends Thread {
    private String url;

    DownloadThread(String url) {
        this.url = url;
    }

    @Override
    public void run() {
        System.out.println("Downloading: " + url);
        System.out.println("Thread: " + Thread.currentThread().getName());
        // download logic here
    }
}

// Usage
DownloadThread thread = new DownloadThread("http://example.com/file");
thread.setName("DownloadWorker");
thread.start();  // ✅ creates new thread
```

### What Happens

```text
new DownloadThread()  → thread object created (NEW state)
thread.start()        → JVM creates OS thread, calls run() in new thread
thread.run()          → ❌ runs in caller thread — wrong!
```

### Limitations

```java
// ❌ Can't extend another class
class DownloadThread extends Thread { }  // extends slot used

// ❌ One task = one Thread subclass — not reusable with pools
```

### When Still Seen

```text
Legacy code
Very simple demos
When you need Thread subclass methods directly (rare)
```

**Interview Point:**

> Extend Thread → override `run()` → `start()`. Legacy approach—prefer Runnable + ExecutorService in production.

</details>

---

# 10. Creating thread using Runnable?

<details>
<summary>Show Answer</summary>

**Answer:**

Implement `Runnable` interface (or use anonymous class), pass to `Thread`, and call `start()`.

### Class Implementation

```java
class EmailTask implements Runnable {
    private String recipient;

    EmailTask(String recipient) {
        this.recipient = recipient;
    }

    @Override
    public void run() {
        System.out.println("Sending email to: " + recipient);
        // send email logic
    }
}

Thread t = new Thread(new EmailTask("user@example.com"));
t.start();
```

### Anonymous Class

```java
Runnable task = new Runnable() {
    @Override
    public void run() {
        System.out.println("Anonymous Runnable");
    }
};
new Thread(task).start();
```

### With ExecutorService (Preferred)

```java
ExecutorService executor = Executors.newFixedThreadPool(5);

Runnable task = () -> processReport();
executor.submit(task);

executor.shutdown();
```

### Runnable Interface

```java
public interface Runnable {
    void run();  // no return, no checked exceptions
}
```

**Interview Point:**

> Implement Runnable → pass to Thread or ExecutorService. Separates task from thread execution.

</details>

---

# 11. Creating thread using Lambda?

<details>
<summary>Show Answer</summary>

**Answer:**

Since `Runnable` is a functional interface, use a **lambda expression** instead of anonymous class—cleaner syntax.

### Lambda Runnable

```java
// Lambda replaces anonymous Runnable
Thread t = new Thread(() -> {
    System.out.println("Lambda thread: " + Thread.currentThread().getName());
});
t.start();
```

### One-Line Lambda

```java
new Thread(() -> System.out.println("Hello from lambda")).start();
```

### With ExecutorService

```java
ExecutorService executor = Executors.newFixedThreadPool(3);

executor.submit(() -> processOrder(order));
executor.submit(() -> sendNotification(user));
executor.submit(() -> updateInventory(item));

executor.shutdown();
```

### Lambda vs Anonymous Class

```java
// Old — anonymous class
new Thread(new Runnable() {
    public void run() {
        System.out.println("Running");
    }
}).start();

// Java 8+ — lambda
new Thread(() -> System.out.println("Running")).start();
```

### Multi-Line Lambda

```java
executor.submit(() -> {
    log.info("Task started");
    processData();
    log.info("Task completed");
});
```

**Interview Point:**

> Runnable is functional interface → lambda works. `new Thread(() -> { ... }).start()` or `executor.submit(() -> ...)`.

</details>

---

# 12. Creating thread using ExecutorService?

<details>
<summary>Show Answer</summary>

**Answer:**

`ExecutorService` manages a **thread pool**—submit `Runnable` tasks without manually creating `Thread` objects.

### Fixed Thread Pool

```java
ExecutorService executor = Executors.newFixedThreadPool(10);

for (int i = 0; i < 100; i++) {
    executor.submit(() -> {
        System.out.println("Task on: " + Thread.currentThread().getName());
    });
}

executor.shutdown();  // orderly shutdown
```

### Pool Types

```java
Executors.newFixedThreadPool(10);    // fixed 10 threads
Executors.newCachedThreadPool();     // grows/shrinks dynamically
Executors.newSingleThreadExecutor(); // one thread, queued tasks
Executors.newScheduledThreadPool(5); // scheduled/delayed tasks
```

### submit() vs execute()

```java
// execute — void, no return
executor.execute(() -> doWork());

// submit — returns Future
Future<?> future = executor.submit(() -> doWork());
```

### Graceful Shutdown

```java
executor.shutdown();           // stop accepting new tasks
executor.awaitTermination(30, TimeUnit.SECONDS); // wait for tasks
// or
executor.shutdownNow();          // force stop
```

### Spring / Production Pattern

```java
@Bean
public ExecutorService taskExecutor() {
    return Executors.newFixedThreadPool(20);
}

// In service
taskExecutor.submit(() -> asyncProcess(data));
```

### Why Preferred

```text
✅ Thread reuse — no per-task thread creation
✅ Limits concurrency — pool size caps threads
✅ Task queue — handles bursts
✅ Standard in Spring, Java EE, microservices
```

**Interview Point:**

> `Executors.newFixedThreadPool(n)` + `submit(Runnable)` + `shutdown()`. Production standard—never unbounded `new Thread()`.

</details>

---

# 13. Creating thread using Callable?

<details>
<summary>Show Answer</summary>

**Answer:**

`Callable<V>` is like `Runnable` but **returns a value** and can **throw checked exceptions**—submit to `ExecutorService` and get `Future<V>`.

### Callable Interface

```java
public interface Callable<V> {
    V call() throws Exception;  // returns value, can throw
}
```

### vs Runnable

| | `Runnable` | `Callable<V>` |
|---|------------|---------------|
| Method | `void run()` | `V call()` |
| Return | void | Value of type V |
| Exception | Cannot throw checked | Can throw Exception |
| Result | None | `Future<V>` |

### Basic Example

```java
ExecutorService executor = Executors.newFixedThreadPool(3);

Callable<String> task = () -> {
    Thread.sleep(2000);
    return "Task result";
};

Future<String> future = executor.submit(task);

// Blocking get
String result = future.get();  // waits until done
System.out.println(result);  // "Task result"

executor.shutdown();
```

### Real Example — Parallel API Calls

```java
Callable<Integer> fetchUserCount = () -> userService.count();
Callable<Integer> fetchOrderCount = () -> orderService.count();

Future<Integer> usersFuture = executor.submit(fetchUserCount);
Future<Integer> ordersFuture = executor.submit(fetchOrderCount);

int users = usersFuture.get();
int orders = ordersFuture.get();
// Both ran in parallel
```

### Lambda Callable

```java
Future<Double> future = executor.submit(() -> {
    return calculateTax(amount);
});
```

### Exception Handling

```java
try {
    String result = future.get();
} catch (ExecutionException e) {
    // exception from call()
    Throwable cause = e.getCause();
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}
```

### invokeAll — Multiple Callables

```java
List<Callable<String>> tasks = List.of(
    () -> fetchFromServiceA(),
    () -> fetchFromServiceB()
);

List<Future<String>> futures = executor.invokeAll(tasks);
for (Future<String> f : futures) {
    System.out.println(f.get());
}
```

**Interview Point:**

> `Callable` returns value via `Future`. Use when task produces result. `executor.submit(callable)` → `future.get()`.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: run() vs start()?

<details>
<summary>Show Answer</summary>

**Answer:** `start()` creates new thread and calls `run()` in it. `run()` called directly runs in **current thread**—no multithreading.

</details>

---

### Q: Can we start a thread twice?

<details>
<summary>Show Answer</summary>

**Answer:** No. Second `start()` throws `IllegalThreadStateException`. Thread lifecycle: NEW → RUNNABLE → ... → TERMINATED. Cannot restart.

</details>

---

### Q: Main thread is user or daemon?

<details>
<summary>Show Answer</summary>

**Answer:** **User thread**. JVM waits for main thread to finish. Daemon threads created from main are still daemon—but JVM exits when all user threads end.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Thread = lightweight execution unit sharing process heap. Production: **ExecutorService** + Runnable/Callable—not `new Thread()` per task. Runnable over extending Thread. Callable when you need return values via Future.

</details>
