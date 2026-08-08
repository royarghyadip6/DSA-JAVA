# 18. Thread Methods

## 18. Thread Methods

## Frequently Asked

---

# 1. start() vs run()?

<details>
<summary>Show Answer</summary>

**Answer:**

`start()` creates a **new thread** and calls `run()` in it. Calling `run()` directly executes the code in the **current thread**—no multithreading.

### Comparison Table

| | `start()` | `run()` |
|---|-----------|---------|
| Thread created? | ✅ Yes — new OS thread | ❌ No — current thread |
| Async execution? | ✅ Yes | ❌ No — synchronous |
| State change | NEW → RUNNABLE | No state change |
| Can call twice? | ❌ IllegalThreadStateException | ✅ Can call multiple times |
| Purpose | Begin thread lifecycle | Just executes task body |

### start() — Correct

```java
Thread t = new Thread(() -> {
    System.out.println("Thread: " + Thread.currentThread().getName());
});

t.start(); // Creates new thread → prints "Thread-0"
```

### run() — Wrong for Multithreading

```java
Thread t = new Thread(() -> {
    System.out.println("Thread: " + Thread.currentThread().getName());
});

t.run(); // Runs in MAIN thread → prints "main"
// No new thread created!
```

### Side-by-Side Output

```java
System.out.println("Main: " + Thread.currentThread().getName()); // main

Thread t = new Thread(() ->
    System.out.println("Worker: " + Thread.currentThread().getName()));

t.run();   // Worker: main     ← same thread!
t.start(); // Worker: Thread-0 ← new thread!
```

**Interview Point:**

> `start()` = new thread + async. `run()` = ordinary method call in current thread. Always use `start()` for multithreading.

</details>

---

# 2. Why should we call start() instead of run()?

<details>
<summary>Show Answer</summary>

**Answer:**

`start()` is required because only it **creates a new OS thread** and enables **parallel/concurrent execution**—`run()` alone defeats the purpose of multithreading.

### Reasons to Use start()

| Reason | Explanation |
|--------|-------------|
| **New thread** | JVM creates native thread — true concurrency |
| **Async execution** | Caller continues without waiting for task |
| **Thread lifecycle** | Proper NEW → RUNNABLE → TERMINATED flow |
| **OS scheduling** | Thread competes for CPU with other threads |
| **Thread-local stack** | New thread gets its own stack |

### Problem with run()

```java
// ❌ Sequential — no parallelism
for (int i = 0; i < 10; i++) {
    new Thread(() -> processRequest()).run();
    // Each runs in main thread — one after another
}

// ✅ Concurrent — 10 parallel threads
for (int i = 0; i < 10; i++) {
    new Thread(() -> processRequest()).start();
}
```

### run() Only Valid For

```text
Testing run() logic without threading
Template method pattern override in Thread subclass
Reusing run() body in same thread intentionally
```

### Real Production Impact

```java
// Web server — WRONG
new Thread(() -> handleRequest(req)).run();
// Blocks caller — handles 1 request at a time

// Web server — CORRECT
new Thread(() -> handleRequest(req)).start();
// Returns immediately — concurrent request handling
```

**Interview Point:**

> `start()` enables concurrency. `run()` is a normal method call—no parallelism, no new thread, wrong thread name in logs.

</details>

---

# 3. What happens internally when start() is called?

<details>
<summary>Show Answer</summary>

**Answer:**

When `start()` is called, the JVM **registers the thread with the OS**, creates a **native thread**, transitions state to **RUNNABLE**, and schedules `run()` to execute in the new thread.

### Internal Steps

```text
1. Thread.start() called
2. JVM checks thread state == NEW (else throw exception)
3. JVM calls native start0() method
4. OS creates new native thread
5. Thread state → RUNNABLE
6. OS scheduler picks thread for CPU
7. JVM invokes run() in new thread context
8. run() executes until completion → TERMINATED
```

### Native Method

```java
// Thread.java (simplified)
public synchronized void start() {
    if (threadStatus != 0)
        throw new IllegalThreadStateException();
    start0(); // native method — creates OS thread
}

private native void start0();
```

### What start0() Does (Conceptual)

```text
Register thread with OS thread scheduler
Allocate thread stack memory
Set up thread context (program counter, registers)
Schedule thread for execution
When scheduled → call run() in new thread
```

### State Transitions

```text
Before start():  NEW
start() called:  RUNNABLE (OS thread exists)
run() executing: RUNNABLE
run() completes: TERMINATED
```

### synchronized on start()

```java
// start() is synchronized — prevents race condition
// on double-start from two threads simultaneously
public synchronized void start() { ... }
```

**Interview Point:**

> `start()` → native `start0()` → OS thread created → RUNNABLE → `run()` in new thread. Not just a method call—OS-level thread creation.

</details>

---

# 4. Can start() be called twice?

<details>
<summary>Show Answer</summary>

**Answer:**

**No.** Calling `start()` twice on the same `Thread` object throws `IllegalThreadStateException`—a thread can only be started once.

### Example

```java
Thread t = new Thread(() -> System.out.println("Running"));

t.start(); // ✅ first call — OK
t.start(); // ❌ IllegalThreadStateException
```

### Why Not Allowed

```text
Thread lifecycle is one-way:
  NEW → RUNNABLE → ... → TERMINATED

After first start(), thread is no longer NEW
Second start() violates lifecycle rules
OS thread already exists or has terminated
```

### Internal Check

```java
public synchronized void start() {
    if (threadStatus != 0)  // 0 = NEW
        throw new IllegalThreadStateException();
    start0();
}
```

### To Run Task Again

```java
// ❌ Cannot restart same thread
Thread t = new Thread(task);
t.start();
t.join();
t.start(); // Exception!

// ✅ Create new Thread object
Thread t2 = new Thread(task);
t2.start(); // OK — new thread object
```

**Interview Point:**

> One `start()` per `Thread` object. To rerun task → **new Thread instance**. Thread cannot be recycled.

</details>

---

# 5. What happens if start() called twice?

<details>
<summary>Show Answer</summary>

**Answer:**

The second `start()` throws **`IllegalThreadStateException`** immediately—the thread is not restarted and no new execution occurs.

### Exception Details

```java
Thread t = new Thread(() -> {
    System.out.println("Running");
});

t.start();  // OK — thread runs

try {
    t.start();  // second call
} catch (IllegalThreadStateException e) {
    System.out.println("Cannot start twice!");
}
```

### Thread State After First start()

```text
First start()  → RUNNABLE (or TERMINATED if already finished)
Second start() → IllegalThreadStateException
                 thread state unchanged
```

### Even After TERMINATED

```java
Thread t = new Thread(() -> System.out.println("Done"));
t.start();
t.join(); // wait for completion
System.out.println(t.getState()); // TERMINATED

t.start(); // ❌ Still IllegalThreadStateException!
// TERMINATED thread cannot be restarted
```

### Safe Pattern

```java
ExecutorService executor = Executors.newCachedThreadPool();
// Pool reuses threads safely — don't manage Thread objects manually
executor.submit(task); // can submit same Runnable many times
executor.submit(task); // OK — pool handles thread lifecycle
```

**Interview Point:**

> Second `start()` = `IllegalThreadStateException`. Even after TERMINATED. Use ExecutorService to reuse thread pools, not restart Thread objects.

</details>

---

## sleep()

---

# 6. What is Thread.sleep()?

<details>
<summary>Show Answer</summary>

**Answer:**

`Thread.sleep(long millis)` **pauses the current thread** for a specified duration—thread enters **TIMED_WAITING** state and does not consume CPU.

### Usage

```java
try {
    Thread.sleep(5000); // pause 5 seconds
    System.out.println("Awake!");
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}
```

### Overloads

```java
Thread.sleep(1000);              // milliseconds
Thread.sleep(1000, 500000);      // millis + nanos (nanos often ignored by OS)
TimeUnit.SECONDS.sleep(5);       // cleaner — Java 5+
TimeUnit.MINUTES.sleep(1);
```

### What Happens

```text
Thread calls sleep(5000)
    ↓
State → TIMED_WAITING
    ↓
OS scheduler removes thread from CPU
    ↓
After 5 seconds (or interrupt) → RUNNABLE
    ↓
Scheduler may run thread again
```

### Use Cases

```java
// Rate limiting
for (String url : urls) {
    fetch(url);
    Thread.sleep(1000); // 1 request per second
}

// Retry with delay
for (int i = 0; i < 3; i++) {
    if (tryConnect()) break;
    Thread.sleep(2000);
}

// Polling interval
while (running) {
    checkStatus();
    Thread.sleep(5000);
}
```

### Important Notes

```text
✅ Static method — sleeps CURRENT thread
✅ Does NOT release locks (if in synchronized block)
✅ Throws InterruptedException (checked)
❌ Does NOT guarantee exact wake time (OS scheduling)
```

**Interview Point:**

> `sleep()` pauses current thread for fixed time → TIMED_WAITING. Static method. Does **not** release lock. Always handle `InterruptedException`.

</details>

---

# 7. Does sleep release lock?

<details>
<summary>Show Answer</summary>

**Answer:**

**No.** `Thread.sleep()` does **not release** any locks the thread holds—it keeps all `synchronized` locks while sleeping.

### Proof

```java
Object lock = new Object();

Thread t1 = new Thread(() -> {
    synchronized (lock) {
        try {
            System.out.println("T1 sleeping with lock...");
            Thread.sleep(10000); // holds lock while sleeping!
            System.out.println("T1 awake");
        } catch (InterruptedException e) { }
    }
});

Thread t2 = new Thread(() -> {
    synchronized (lock) {
        System.out.println("T2 got lock"); // waits 10 seconds!
    }
});

t1.start();
Thread.sleep(500);
t2.start(); // BLOCKED — t1 still holds lock during sleep
```

### sleep() vs wait() — Lock Behavior

| Method | Releases Lock? |
|--------|----------------|
| `Thread.sleep()` | ❌ **No** — keeps lock |
| `Object.wait()` | ✅ **Yes** — releases lock |

### Why This Matters

```text
sleep() inside synchronized block:
  → Other threads CANNOT enter synchronized block
  → Potential performance issue — lock held unnecessarily

Better: release lock before sleeping, or use wait()
```

### Best Practice

```java
// ❌ Holds lock during sleep — blocks others
synchronized (lock) {
    Thread.sleep(5000);
}

// ✅ Use wait() if you need to release lock
synchronized (lock) {
    lock.wait(5000); // releases lock, reacquires after timeout
}
```

**Interview Point:**

> **sleep() does NOT release lock.** Classic trap: `sleep()` inside `synchronized` blocks other threads. Use `wait()` to release lock.

</details>

---

# 8. Checked exception in sleep?

<details>
<summary>Show Answer</summary>

**Answer:**

`Thread.sleep()` throws **`InterruptedException`**—a **checked exception** that must be caught or declared.

### Declaration

```java
public static void sleep(long millis) throws InterruptedException
public static void sleep(long millis, int nanos) throws InterruptedException
```

### Must Handle

```java
// ✅ try-catch
try {
    Thread.sleep(1000);
} catch (InterruptedException e) {
    // handle interruption
}

// ✅ declare throws
public void myMethod() throws InterruptedException {
    Thread.sleep(1000);
}
```

### What InterruptedException Means

```text
Another thread called interrupt() on sleeping thread
OR JVM interrupted thread during sleep
→ sleep() ends early
→ InterruptedException thrown
→ interrupt flag cleared when exception caught
```

### Proper Handling

```java
try {
    Thread.sleep(5000);
} catch (InterruptedException e) {
    // ✅ Restore interrupt flag — let caller know
    Thread.currentThread().interrupt();
    // Don't swallow silently!
}

// ❌ Bad — swallows interrupt
catch (InterruptedException e) { }
```

### Lambda / Runnable

```java
// Runnable.run() cannot throw checked exceptions
new Thread(() -> {
    try {
        Thread.sleep(1000);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}).start();
```

**Interview Point:**

> `sleep()` throws checked `InterruptedException`. Always catch and **restore interrupt flag**: `Thread.currentThread().interrupt()`.

</details>

---

# 9. Difference between sleep() and wait()?

<details>
<summary>Show Answer</summary>

**Answer:**

`sleep()` is a **static Thread method** that pauses without releasing locks. `wait()` is an **Object method** that releases the monitor lock and waits for notification.

### Comparison Table

| | `Thread.sleep()` | `Object.wait()` |
|---|------------------|-----------------|
| Class | `Thread` (static) | `Object` |
| Lock release | ❌ No | ✅ Yes |
| Wake up | Timeout or interrupt | `notify()` / timeout / interrupt |
| Must be in synchronized? | No (but holds lock if inside) | ✅ Yes — must hold lock |
| State | TIMED_WAITING | WAITING or TIMED_WAITING |
| Purpose | Pause execution | Thread coordination |

### sleep() Example

```java
Thread.sleep(5000); // just pauses — no lock involved
```

### wait() Example

```java
synchronized (lock) {
    lock.wait(5000); // releases lock, waits for notify or timeout
}
```

### Lock Behavior — Critical Difference

```java
synchronized (lock) {
    Thread.sleep(5000);  // ❌ lock NOT released — others blocked
}

synchronized (lock) {
    lock.wait(5000);       // ✅ lock released — others can enter
}
```

### When to Use

| Use sleep() | Use wait() |
|-------------|------------|
| Simple delay | Producer-consumer |
| Rate limiting | Thread coordination |
| Retry backoff | Wait for condition |
| Polling interval | notify/notifyAll pattern |

### Producer-Consumer with wait/notify

```java
synchronized (queue) {
    while (queue.isEmpty()) {
        queue.wait(); // releases lock — producer can add
    }
    item = queue.remove();
}
```

**Interview Point:**

> **sleep** = pause, no lock release, static. **wait** = release lock, wait for notify, must be in synchronized. Most common thread interview comparison.

</details>

---

## join()

---

# 10. What is join()?

<details>
<summary>Show Answer</summary>

**Answer:**

`join()` waits for another thread to **complete** (die)—the calling thread pauses until the target thread reaches **TERMINATED** state.

### Basic Usage

```java
Thread worker = new Thread(() -> {
    System.out.println("Worker working...");
    try { Thread.sleep(3000); }
    catch (InterruptedException e) { }
    System.out.println("Worker done");
});

worker.start();
worker.join(); // main thread WAITS here until worker finishes
System.out.println("Main continues after worker");
```

### Output Order

```text
Worker working...
Worker done
Main continues after worker
```

### Method Signatures

```java
thread.join();              // wait indefinitely
thread.join(long millis);   // wait max millis
thread.join(long millis, int nanos);
```

### State During join()

```text
Calling thread → WAITING (or TIMED_WAITING with timeout)
Target thread  → RUNNABLE → TERMINATED
Calling thread → RUNNABLE (when target terminates)
```

### Throws InterruptedException

```java
try {
    worker.join();
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}
```

**Interview Point:**

> `join()` = wait for thread to die. Calling thread enters WAITING. Ensures worker completes before continuing.

</details>

---

# 11. Why join() is used?

<details>
<summary>Show Answer</summary>

**Answer:**

`join()` ensures **order of execution** and **result availability**—the caller waits for a worker thread to finish before proceeding.

### Use Cases

| Use Case | Example |
|----------|---------|
| **Wait for completion** | Main waits for all workers before shutdown |
| **Result dependency** | Wait for data fetch before processing |
| **Ordered output** | Ensure thread A finishes before thread B starts |
| **Testing** | Wait for async task in tests |

### Wait for All Workers

```java
List<Thread> workers = new ArrayList<>();

for (int i = 0; i < 5; i++) {
    Thread t = new Thread(() -> processBatch(i));
    workers.add(t);
    t.start();
}

// Wait for ALL to complete
for (Thread t : workers) {
    t.join();
}
System.out.println("All batches done — safe to shutdown");
```

### Dependent Processing

```java
Thread fetchThread = new Thread(() -> data = fetchFromAPI());
fetchThread.start();
fetchThread.join(); // wait for data
process(data);      // safe — data is ready
```

### Without join() — Race Condition

```java
Thread t = new Thread(() -> result = compute());
t.start();
// result may still be null — race condition!
use(result); // ❌ unpredictable
```

### Production Alternative

```java
// Modern — Future instead of join()
Future<String> future = executor.submit(() -> fetchData());
String data = future.get(); // like join() but with return value
```

**Interview Point:**

> `join()` synchronizes thread completion—caller blocks until target dies. Use when next step depends on thread finishing.

</details>

---

# 12. join(long millis)?

<details>
<summary>Show Answer</summary>

**Answer:**

`join(long millis)` waits for the target thread to finish, but only up to the **specified milliseconds**—returns whether thread died within timeout.

### Usage

```java
Thread worker = new Thread(() -> {
    try { Thread.sleep(10000); } // 10 second task
    catch (InterruptedException e) { }
});

worker.start();

boolean finished = worker.join(3000); // wait max 3 seconds

if (!finished) {
    System.out.println("Worker still running — timeout!");
    worker.interrupt(); // optional cleanup
} else {
    System.out.println("Worker finished within 3 seconds");
}
```

### Return Value (Java 19+)

```java
// Java 19+ join returns boolean
boolean completed = thread.join(Duration.ofSeconds(5));
// true = thread terminated within timeout
// false = timeout expired, thread still alive
```

### Pre-Java 19 — Check isAlive()

```java
worker.join(3000);
if (worker.isAlive()) {
    System.out.println("Still running after 3 sec");
} else {
    System.out.println("Completed");
}
```

### State During Timed join()

```text
Calling thread → TIMED_WAITING (max millis)
Target dies before timeout → calling thread → RUNNABLE
Timeout expires first     → calling thread → RUNNABLE (target may still run)
```

### Real Use — Timeout with Fallback

```java
thread.start();
thread.join(5000); // max 5 sec wait

if (thread.isAlive()) {
    thread.interrupt();
    log.warn("Task timed out");
    return defaultValue;
}
return result;
```

**Interview Point:**

> `join(millis)` = wait with timeout → TIMED_WAITING. Check `isAlive()` after if thread may still be running. Handle timeout in production.

</details>

---

## yield()

---

# 13. What is yield()?

<details>
<summary>Show Answer</summary>

**Answer:**

`Thread.yield()` is a **hint to the scheduler** that the current thread is willing to **relinquish its CPU time slice**—allowing other threads to run.

### Usage

```java
Thread.yield(); // hint: let other threads run
```

### What It Does

```text
Current thread (RUNNABLE)
    ↓
yield() called
    ↓
Thread remains RUNNABLE
    ↓
Scheduler may run other threads of same priority
    ↓
Current thread may resume later
```

### Example

```java
Thread t1 = new Thread(() -> {
    for (int i = 0; i < 5; i++) {
        System.out.println("T1: " + i);
        Thread.yield(); // give T2 a chance
    }
});

Thread t2 = new Thread(() -> {
    for (int i = 0; i < 5; i++) {
        System.out.println("T2: " + i);
    }
});
```

### Characteristics

| Property | Detail |
|----------|--------|
| Static method | Affects current thread |
| State after | Still RUNNABLE |
| Lock behavior | Does not release locks |
| Guaranteed? | ❌ No — OS may ignore hint |

### Rarely Used in Production

```text
Modern JVMs and OS schedulers handle fairness well
yield() is unreliable and platform-dependent
Prefer proper concurrency constructs (locks, queues)
```

**Interview Point:**

> `yield()` = polite hint to scheduler. Thread stays RUNNABLE. **Not guaranteed** to switch threads. Rarely used in real code.

</details>

---

# 14. Is yield guaranteed?

<details>
<summary>Show Answer</summary>

**Answer:**

**No.** `yield()` is only a **hint** to the thread scheduler—the OS/JVM may **completely ignore** it with no thread switch occurring.

### Why Not Guaranteed

```text
1. yield() is a hint, not a command
2. OS scheduler decides actual thread switching
3. On single-core: may just reschedule same thread
4. JVM implementation varies by platform
5. No specification mandates thread switch
```

### Possible Outcomes

```text
yield() called →
  Outcome A: current thread pauses, another runs  ✅ hoped for
  Outcome B: current thread immediately continues  ❌ yield ignored
  Outcome C: brief pause then same thread resumes
```

### Proof — Unreliable

```java
Thread t = new Thread(() -> {
    for (int i = 0; i < 1000000; i++) {
        Thread.yield();
        // may still monopolize CPU — yield ignored
    }
});
```

### Better Alternatives

```java
// For fairness — use proper concurrency
BlockingQueue<Task> queue = new LinkedBlockingQueue<>();
executor.submit(() -> queue.take()); // proper blocking wait

// For CPU sharing — use thread pools with limited threads
ExecutorService pool = Executors.newFixedThreadPool(4);
```

### Platform Differences

```text
Linux:   sched_yield() — may switch to same priority thread
Windows: SwitchToThread() — similar hint behavior
Behavior not consistent across JVM/OS combinations
```

**Interview Point:**

> yield() is **NOT guaranteed** to switch threads. Hint only—OS decides. Never rely on yield() for correctness or fairness.

</details>

---

## interrupt()

---

# 15. What is interruption?

<details>
<summary>Show Answer</summary>

**Answer:**

**Thread interruption** is a cooperative mechanism to **signal a thread to stop** what it's doing—via the `interrupt()` method setting the thread's **interrupt flag**.

### Core Idea

```text
Interruption is NOT forced kill
It is a POLITE REQUEST to stop
Target thread must check and respond to interrupt flag
```

### Setting Interrupt

```java
Thread worker = new Thread(() -> {
    while (!Thread.currentThread().isInterrupted()) {
        // do work
        processItem();
    }
    System.out.println("Worker stopped gracefully");
});

worker.start();
// later...
worker.interrupt(); // sets interrupt flag — cooperative stop
```

### Interrupt Flag

```java
thread.interrupt();    // set flag to true
thread.isInterrupted(); // check flag (doesn't clear)
Thread.interrupted();  // check AND clear flag (static)
```

### How Threads Respond

| State | Response to interrupt() |
|-------|---------------------------|
| RUNNABLE | Sets flag — thread must check |
| sleep/wait/join | Throws `InterruptedException`, clears flag |
| BLOCKED (synchronized) | Sets flag — checked when lock acquired |

### Cooperative Cancellation Pattern

```java
class Worker implements Runnable {
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                doWork();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // restore flag
                break;
            }
        }
        cleanup();
    }
}
```

**Interview Point:**

> Interruption = **cooperative** stop signal via interrupt flag. Not `stop()` (deprecated). Thread must check flag or handle `InterruptedException`.

</details>

---

# 16. interrupt() vs interrupted() vs isInterrupted()?

<details>
<summary>Show Answer</summary>

**Answer:**

Three related methods handle the **interrupt flag** differently—`interrupt()` sets it, `isInterrupted()` checks it, `interrupted()` checks and **clears** it.

### Comparison Table

| Method | Type | Action | Clears Flag? |
|--------|------|--------|--------------|
| `thread.interrupt()` | Instance | **Sets** interrupt flag | No |
| `thread.isInterrupted()` | Instance | **Checks** flag | ❌ No |
| `Thread.interrupted()` | **Static** | Checks **current** thread flag | ✅ Yes — clears after read |

### interrupt() — Set Flag

```java
Thread t = new Thread(() -> { /* work */ });
t.start();
t.interrupt(); // sets t's interrupt flag to true
```

### isInterrupted() — Check Without Clearing

```java
Thread t = ...;
t.interrupt();

System.out.println(t.isInterrupted()); // true
System.out.println(t.isInterrupted());   // true — still set
```

### interrupted() — Check AND Clear

```java
Thread.currentThread().interrupt();

System.out.println(Thread.interrupted()); // true — checked and CLEARED
System.out.println(Thread.interrupted()); // false — already cleared
```

### Static vs Instance

```java
Thread t = new Thread(() -> {
    // checks CURRENT thread's flag (this worker thread)
    if (Thread.interrupted()) { ... }  // static — current thread
    if (Thread.currentThread().isInterrupted()) { ... } // same effect here
});

// From another thread:
t.isInterrupted(); // check t's flag from outside
t.interrupted();   // ❌ WRONG — checks CALLER's flag, not t's!
```

### Common Pattern in Catch Block

```java
try {
    Thread.sleep(1000);
} catch (InterruptedException e) {
    // sleep() cleared the flag when throwing exception
    Thread.currentThread().interrupt(); // restore flag for caller
}
```

**Interview Point:**

> `interrupt()` sets flag. `isInterrupted()` reads without clearing. `Thread.interrupted()` static — reads **current** thread and **clears** flag.

</details>

---

# 17. How interruption works internally?

<details>
<summary>Show Answer</summary>

**Answer:**

Internally, `interrupt()` sets a **boolean interrupt flag** on the thread object. If the thread is in a blocking state (`sleep`, `wait`, `join`), the JVM **awakens** it and throws `InterruptedException`.

### Internal Mechanism

```text
interrupt() called on thread
    ↓
JVM sets interrupted status flag = true
    ↓
If thread is:
  RUNNABLE     → flag set, thread continues (must check manually)
  sleep/wait   → JVM wakes thread, throws InterruptedException, clears flag
  BLOCKED      → flag set, checked when lock acquired
    ↓
Target thread responds based on its code
```

### Interrupt Flag Storage

```java
// Thread object internally has:
private volatile boolean interrupted = false;

public void interrupt() {
    interrupted = true; // set flag
    // if in native blocking call → wake up native thread
}
```

### Blocking Method Behavior

```java
// Thread sleeping
Thread.sleep(10000);
// Another thread calls interrupt()
// → sleep() ends immediately
// → InterruptedException thrown
// → interrupt flag CLEARED (set to false)

// Thread in wait()
synchronized (lock) {
    lock.wait();
}
// interrupt() called
// → wait() ends
// → InterruptedException thrown
// → lock re-acquired after exception
```

### Cooperative vs Forced

```text
interrupt()     → cooperative — thread decides how to stop
Thread.stop()     → forced kill — DEPRECATED, unsafe
Future.cancel()   → may interrupt underlying thread
Executor.shutdownNow() → interrupts all pool threads
```

### Proper Response to Interrupt

```java
@Override
public void run() {
    while (!Thread.currentThread().isInterrupted()) {
        try {
            processNextItem();
        } catch (InterruptedException e) {
            // blocking method was interrupted
            Thread.currentThread().interrupt(); // restore flag
            break; // exit loop
        }
    }
    // cleanup resources
}
```

### Interrupt in ExecutorService

```java
Future<?> future = executor.submit(longRunningTask);
future.cancel(true); // true = send interrupt to running thread
// Equivalent to thread.interrupt() on worker thread
```

**Interview Point:**

> Interrupt = set volatile flag. Blocking methods (`sleep`, `wait`, `join`) throw `InterruptedException` and clear flag. Always restore flag in catch block.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: stop() vs interrupt()?

<details>
<summary>Show Answer</summary>

**Answer:** `stop()` is **deprecated**—forcefully kills thread, leaves locks in bad state. `interrupt()` is cooperative—sets flag, thread stops gracefully. Always use `interrupt()`.

</details>

---

### Q: sleep(0) effect?

<details>
<summary>Show Answer</summary>

**Answer:** Similar hint to `yield()`—thread remains RUNNABLE, may allow other same-priority threads to run. Not guaranteed. Rarely used.

</details>

---

### Q: Can you interrupt a TERMINATED thread?

<details>
<summary>Show Answer</summary>

**Answer:** `interrupt()` on TERMINATED thread does nothing harmful—interrupt flag may be set but thread is already dead. No effect on execution.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> `start()` not `run()`. `sleep()` no lock release; `wait()` releases lock. `join()` waits for death. `yield()` hint only—not guaranteed. `interrupt()` cooperative; restore flag after `InterruptedException`.

</details>
