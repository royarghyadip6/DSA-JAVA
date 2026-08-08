# 17. Thread Lifecycle

## 17. Thread Lifecycle

## Most Asked

---

# 1. What are thread states?

<details>
<summary>Show Answer</summary>

**Answer:**

A thread in Java exists in one of **six states** defined by `Thread.State` enum—representing the thread's lifecycle from creation to termination.

### All Six States

```text
NEW
RUNNABLE
BLOCKED
WAITING
TIMED_WAITING
TERMINATED
```

### Enum in Java

```java
public enum State {
    NEW,
    RUNNABLE,
    BLOCKED,
    WAITING,
    TIMED_WAITING,
    TERMINATED
}
```

### Check Thread State

```java
Thread t = new Thread(() -> System.out.println("Running"));
System.out.println(t.getState()); // NEW

t.start();
System.out.println(t.getState()); // RUNNABLE
```

### Lifecycle Flow (Simplified)

```text
NEW → start() → RUNNABLE ⇄ BLOCKED / WAITING / TIMED_WAITING → TERMINATED
```

### State Categories

| Category | States |
|----------|--------|
| Not alive | NEW, TERMINATED |
| Alive — active | RUNNABLE |
| Alive — waiting | BLOCKED, WAITING, TIMED_WAITING |

### Note — No RUNNING State in Java

```text
Java API has RUNNABLE — not RUNNING
RUNNABLE means: ready to run OR actually running on CPU
OS distinguishes ready vs running — Java does not expose RUNNING
```

**Interview Point:**

> Six states: **NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED**. Memorize all six. No separate RUNNING in Java API.

</details>

---

# 2. Explain each state with example.

<details>
<summary>Show Answer</summary>

**Answer:**

Each thread state represents a specific point in the thread's lifecycle—from creation until it finishes execution.

### 1. NEW

Thread object created but `start()` not yet called.

```java
Thread t = new Thread(() -> System.out.println("Hello"));
System.out.println(t.getState()); // NEW
// Thread exists as object but OS thread not created yet
```

### 2. RUNNABLE

Thread is executing or ready to execute on CPU.

```java
t.start();
System.out.println(t.getState()); // RUNNABLE
// Thread is running OR waiting for CPU time slice
```

### 3. BLOCKED

Thread waiting to acquire a **monitor lock** (synchronized block/method).

```java
synchronized (lock) {
    // Thread B tries to enter — BLOCKED until Thread A releases lock
    synchronized (lock) {
        Thread.sleep(5000);
    }
}
```

### 4. WAITING

Thread waiting **indefinitely** for another thread's action (no timeout).

```java
synchronized (lock) {
    lock.wait();  // thread enters WAITING
    // waits until another thread calls notify()/notifyAll()
}
```

### 5. TIMED_WAITING

Thread waiting for a **specified time period**.

```java
Thread.sleep(5000);           // TIMED_WAITING for 5 seconds
lock.wait(3000);                // TIMED_WAITING for 3 seconds
thread.join(1000);              // TIMED_WAITING for 1 second
```

### 6. TERMINATED

Thread has completed execution—`run()` finished or exception thrown.

```java
Thread t = new Thread(() -> System.out.println("Done"));
t.start();
t.join();
System.out.println(t.getState()); // TERMINATED
```

### State Summary Table

| State | Meaning | Example Trigger |
|-------|---------|-----------------|
| NEW | Created, not started | `new Thread()` |
| RUNNABLE | Running or ready | `start()`, after notify |
| BLOCKED | Waiting for lock | `synchronized` contention |
| WAITING | Waiting indefinitely | `wait()`, `join()` |
| TIMED_WAITING | Waiting with timeout | `sleep()`, `wait(ms)` |
| TERMINATED | Finished | `run()` completes |

**Interview Point:**

> Know trigger for each state: `start()`→RUNNABLE, `synchronized`→BLOCKED, `wait()`→WAITING, `sleep()`→TIMED_WAITING, run ends→TERMINATED.

</details>

---

# 3. Difference between RUNNABLE and RUNNING?

<details>
<summary>Show Answer</summary>

**Answer:**

Java defines only **RUNNABLE**—there is **no RUNNING state** in `Thread.State`. RUNNABLE covers both "ready to run" and "actually executing on CPU."

### Key Point

```text
RUNNING state does NOT exist in Java Thread.State enum
RUNNABLE = ready to run + actually running on CPU
```

### Why No RUNNING in Java?

```text
OS scheduler decides which thread runs on CPU
Java JVM abstracts OS-level "running" into RUNNABLE
From Java's view: thread is either not runnable or runnable
```

### OS vs Java View

| OS Level | Java Thread.State |
|----------|-------------------|
| Ready (in run queue) | RUNNABLE |
| Running (on CPU) | RUNNABLE |
| Blocked (waiting for lock) | BLOCKED |
| Sleeping | TIMED_WAITING |

### Example

```java
Thread t = new Thread(() -> {
    while (true) { } // busy loop — RUNNABLE (actually running)
});
t.start();

// Another thread also RUNNABLE — may be waiting for CPU
Thread t2 = new Thread(() -> compute());
t2.start();

// Both show RUNNABLE — Java can't distinguish running vs ready
System.out.println(t.getState());  // RUNNABLE
System.out.println(t2.getState()); // RUNNABLE
```

### Interview Trap

```text
Question: "What is RUNNING state?"
Answer:   Java has no RUNNING — only RUNNABLE
          RUNNABLE includes both ready and executing
```

**Interview Point:**

> **No RUNNING in Java API.** RUNNABLE = ready OR executing. OS distinguishes; Java abstracts both as RUNNABLE.

</details>

---

# 4. Difference between WAITING and BLOCKED?

<details>
<summary>Show Answer</summary>

**Answer:**

**BLOCKED** waits for a **monitor lock** (synchronized). **WAITING** waits for another thread's **notification** (wait/join)—no lock contention involved.

### Comparison Table

| | BLOCKED | WAITING |
|---|---------|---------|
| Cause | Waiting for `synchronized` lock | `wait()`, `join()`, `LockSupport.park()` |
| Lock related? | Yes — monitor lock | No — voluntary wait |
| Who releases? | Thread holding lock | Another thread calls `notify()` |
| Timeout? | No timeout | No timeout (use TIMED_WAITING for timeout) |
| Entry method | Trying to enter `synchronized` | `wait()`, `join()` |

### BLOCKED Example

```java
Object lock = new Object();

Thread t1 = new Thread(() -> {
    synchronized (lock) {
        Thread.sleep(10000); // holds lock
    }
});

Thread t2 = new Thread(() -> {
    synchronized (lock) { // BLOCKED — waiting for t1 to release lock
        System.out.println("Got lock");
    }
});

t1.start();
t2.start();
Thread.sleep(100);
System.out.println(t2.getState()); // BLOCKED
```

### WAITING Example

```java
Object lock = new Object();

Thread t = new Thread(() -> {
    synchronized (lock) {
        try {
            lock.wait(); // WAITING — voluntarily waiting for notify
        } catch (InterruptedException e) { }
    }
});

t.start();
Thread.sleep(100);
System.out.println(t.getState()); // WAITING
```

### Memory Trick

```text
BLOCKED  → wants lock, can't get it (passive — lock contention)
WAITING  → gave up CPU voluntarily, needs signal (wait/notify)
```

**Interview Point:**

> BLOCKED = **lock contention** (synchronized). WAITING = **voluntary wait** (wait/join). Different causes, both no timeout.

</details>

---

# 5. Difference between WAITING and TIMED_WAITING?

<details>
<summary>Show Answer</summary>

**Answer:**

Both are voluntary waiting states—but **WAITING** has **no timeout** (waits forever until notified), while **TIMED_WAITING** waits for a **specified duration**.

### Comparison Table

| | WAITING | TIMED_WAITING |
|---|---------|---------------|
| Timeout | **No** — infinite wait | **Yes** — specified time |
| Wake up | `notify()` / `notifyAll()` / interrupt | Timeout OR notify / interrupt |
| Methods | `wait()`, `join()` | `sleep(ms)`, `wait(ms)`, `join(ms)` |
| Risk | Can wait forever (deadlock risk) | Auto-wakes after timeout |

### WAITING — No Timeout

```java
synchronized (lock) {
    lock.wait();    // WAITING — until notify()
}

thread.join();    // WAITING — until thread dies
```

### TIMED_WAITING — With Timeout

```java
Thread.sleep(5000);         // TIMED_WAITING — 5 sec max
lock.wait(3000);            // TIMED_WAITING — 3 sec max
thread.join(2000);          // TIMED_WAITING — 2 sec max
```

### Wake-Up Conditions

```text
WAITING:
  → notify() / notifyAll()
  → thread interrupt
  → target thread terminates (for join)

TIMED_WAITING:
  → timeout expires
  → notify() / notifyAll()
  → thread interrupt
```

### Example — Both States

```java
// WAITING thread
Thread waiting = new Thread(() -> {
    synchronized (lock) {
        lock.wait(); // no timeout
    }
});

// TIMED_WAITING thread
Thread timed = new Thread(() -> {
    try {
        Thread.sleep(10000); // max 10 seconds
    } catch (InterruptedException e) { }
});
```

### When to Use Which

```text
WAITING         → wait until event (producer-consumer)
TIMED_WAITING   → polling with timeout, retries, sleep delays
```

**Interview Point:**

> WAITING = infinite until signal. TIMED_WAITING = `sleep()`, `wait(ms)`, `join(ms)` — auto-wakes on timeout.

</details>

---

## State Transition Questions

---

# 6. What causes BLOCKED state?

<details>
<summary>Show Answer</summary>

**Answer:**

A thread enters **BLOCKED** when it tries to enter a `synchronized` block or method but another thread already holds the **monitor lock**.

### Cause

```text
Thread wants synchronized lock
    ↓
Another thread holds the lock
    ↓
Thread enters BLOCKED state
    ↓
Waits until lock is released
    ↓
Returns to RUNNABLE when lock acquired
```

### Example

```java
Object lock = new Object();

Thread holder = new Thread(() -> {
    synchronized (lock) {
        try { Thread.sleep(10000); } // holds lock 10 sec
        catch (InterruptedException e) { }
    }
});

Thread blocked = new Thread(() -> {
    synchronized (lock) { // BLOCKED until holder releases
        System.out.println("Finally got lock");
    }
});

holder.start();
blocked.start();
Thread.sleep(500);
System.out.println(blocked.getState()); // BLOCKED
```

### What Causes BLOCKED

| Trigger | Example |
|---------|---------|
| `synchronized` method | Two threads call same sync method |
| `synchronized` block | Contention on same lock object |
| Static synchronized | Class-level lock contention |

### BLOCKED vs Lock API (java.util.concurrent)

```text
synchronized keyword  → Thread.State.BLOCKED
ReentrantLock.lock()  → Thread.State.WAITING (not BLOCKED!)
```

```java
ReentrantLock lock = new ReentrantLock();
lock.lock(); // waiting thread shows WAITING, not BLOCKED
```

### Transition

```text
RUNNABLE → (try enter synchronized) → BLOCKED → (lock acquired) → RUNNABLE
```

**Interview Point:**

> BLOCKED only from **`synchronized`** lock contention. `ReentrantLock` waiting shows WAITING—not BLOCKED.

</details>

---

# 7. What causes WAITING state?

<details>
<summary>Show Answer</summary>

**Answer:**

A thread enters **WAITING** when it calls methods that **voluntarily release CPU** and wait indefinitely for another thread's action.

### Methods That Cause WAITING

| Method | Condition |
|--------|-----------|
| `Object.wait()` | No timeout — waits for `notify()` |
| `Thread.join()` | Waits for target thread to die |
| `LockSupport.park()` | Waits until `unpark()` |

### wait() Example

```java
Object lock = new Object();

Thread waiter = new Thread(() -> {
    synchronized (lock) {
        try {
            System.out.println("Waiting for signal...");
            lock.wait(); // → WAITING
            System.out.println("Resumed!");
        } catch (InterruptedException e) { }
    }
});

Thread notifier = new Thread(() -> {
    synchronized (lock) {
        lock.notify(); // wakes waiter → RUNNABLE
    }
});

waiter.start();
Thread.sleep(100);
System.out.println(waiter.getState()); // WAITING
notifier.start();
```

### join() Example

```java
Thread worker = new Thread(() -> {
    try { Thread.sleep(5000); }
    catch (InterruptedException e) { }
});

Thread joiner = new Thread(() -> {
    try {
        worker.join(); // WAITING until worker terminates
    } catch (InterruptedException e) { }
});

worker.start();
joiner.start();
Thread.sleep(100);
System.out.println(joiner.getState()); // WAITING
```

### How to Exit WAITING

```text
wait()  → notify() / notifyAll() / interrupt
join()  → target thread TERMINATED / interrupt
park()  → unpark() / interrupt
```

**Interview Point:**

> WAITING caused by: **`wait()`** (no timeout), **`join()`**, **`LockSupport.park()`**. Voluntary—no lock contention.

</details>

---

# 8. What causes TIMED_WAITING state?

<details>
<summary>Show Answer</summary>

**Answer:**

A thread enters **TIMED_WAITING** when it waits for a **specified time period** using sleep, timed wait, or timed join.

### Methods That Cause TIMED_WAITING

| Method | Behavior |
|--------|----------|
| `Thread.sleep(long ms)` | Sleep for specified milliseconds |
| `Object.wait(long timeout)` | Wait with timeout |
| `Thread.join(long ms)` | Wait for thread with timeout |
| `LockSupport.parkNanos()` | Park with nanosecond timeout |
| `LockSupport.parkUntil()` | Park until deadline |

### sleep() Example

```java
Thread t = new Thread(() -> {
    try {
        Thread.sleep(10000); // TIMED_WAITING for 10 seconds
    } catch (InterruptedException e) { }
});

t.start();
Thread.sleep(100);
System.out.println(t.getState()); // TIMED_WAITING
```

### wait(timeout) Example

```java
synchronized (lock) {
    lock.wait(5000); // TIMED_WAITING — max 5 seconds
    // wakes after 5 sec OR notify()
}
```

### join(timeout) Example

```java
worker.join(3000); // TIMED_WAITING — wait max 3 sec for worker
```

### Exit TIMED_WAITING

```text
1. Timeout expires        → auto wake → RUNNABLE
2. notify() / notifyAll() → wake → RUNNABLE (for wait)
3. Thread interrupt         → InterruptedException → RUNNABLE
4. Target thread dies       → RUNNABLE (for join)
```

### Real Use — Retry with Timeout

```java
Thread t = new Thread(() -> {
    for (int i = 0; i < 3; i++) {
        try {
            Thread.sleep(2000); // TIMED_WAITING between retries
            if (tryConnect()) break;
        } catch (InterruptedException e) { break; }
    }
});
```

**Interview Point:**

> TIMED_WAITING = **`sleep()`**, **`wait(ms)`**, **`join(ms)`**. Auto-wakes after timeout—safer than infinite WAITING.

</details>

---

# 9. How thread moves from NEW to RUNNABLE?

<details>
<summary>Show Answer</summary>

**Answer:**

A thread moves from **NEW** to **RUNNABLE** when `start()` is called—JVM creates the OS thread and schedules it for execution.

### Transition

```text
NEW (thread object created)
    ↓
start() called
    ↓
JVM creates native OS thread
    ↓
RUNNABLE (ready to run / running)
    ↓
JVM calls run() method in new thread
```

### Example

```java
Thread t = new Thread(() -> {
    System.out.println("Running in: " + Thread.currentThread().getName());
});

System.out.println(t.getState()); // NEW

t.start(); // NEW → RUNNABLE

System.out.println(t.getState()); // RUNNABLE
```

### What start() Does Internally

```text
1. Check thread not already started (not NEW)
2. Create native OS thread
3. Set thread state to RUNNABLE
4. Schedule thread on OS scheduler
5. When scheduled, JVM invokes run() in new thread
```

### NEW State Rules

```java
Thread t = new Thread(() -> System.out.println("Hi"));

// ✅ Only valid transition from NEW
t.start(); // NEW → RUNNABLE

// ❌ Cannot go back to NEW
// ❌ Cannot start twice
t.start(); // IllegalThreadStateException
```

### NOT run() — That Skips NEW→RUNNABLE Properly

```java
Thread t = new Thread(() -> System.out.println("Hi"));

t.run();  // ❌ executes in CURRENT thread — stays in caller's state
          //    does NOT create new thread — NEW stays NEW

t.start(); // ✅ creates new thread — NEW → RUNNABLE
```

**Interview Point:**

> **NEW → RUNNABLE** only via `start()`. `start()` creates OS thread. `run()` does NOT change state—runs in current thread.

</details>

---

# 10. How thread becomes TERMINATED?

<details>
<summary>Show Answer</summary>

**Answer:**

A thread becomes **TERMINATED** when its `run()` method **completes normally** or **throws an uncaught exception**—the thread cannot be restarted.

### Causes of TERMINATED

| Cause | Example |
|-------|---------|
| Normal completion | `run()` method returns |
| Uncaught exception | Exception in `run()` not caught |
| `stop()` (deprecated) | Force stop — never use |

### Normal Completion

```java
Thread t = new Thread(() -> {
    System.out.println("Task done");
    // run() ends here → TERMINATED
});

t.start();
t.join();
System.out.println(t.getState()); // TERMINATED
```

### Exception Termination

```java
Thread t = new Thread(() -> {
    throw new RuntimeException("Crash!"); // → TERMINATED
});

t.start();
t.join();
System.out.println(t.getState()); // TERMINATED
// Exception printed but thread is dead
```

### Lifecycle End

```text
RUNNABLE → run() completes → TERMINATED
RUNNABLE → exception in run() → TERMINATED
WAITING/TIMED_WAITING → run() ends → TERMINATED
BLOCKED → run() ends after getting lock → TERMINATED
```

### Cannot Restart

```java
Thread t = new Thread(() -> System.out.println("Done"));
t.start();
t.join(); // wait for TERMINATED

System.out.println(t.getState()); // TERMINATED

t.start(); // ❌ IllegalThreadStateException — cannot restart
// Must create new Thread object for new task
```

### Daemon Thread Termination

```text
Daemon thread TERMINATED when:
  → run() completes (same as user thread)
  → JVM exits (all user threads done) — daemon killed automatically
```

### Checking if Alive

```java
Thread t = new Thread(() -> { /* work */ });
t.start();

while (t.isAlive()) {
    Thread.sleep(100); // wait until TERMINATED
}
System.out.println(t.getState()); // TERMINATED
```

**Interview Point:**

> TERMINATED when `run()` ends (normal or exception). **Cannot restart** — `start()` twice throws exception. Use `isAlive()` or `getState()` to check.

</details>

---

# Thread Lifecycle Diagram

<details>
<summary>Show Answer</summary>

### Complete State Transition Map

```text
                    start()
    NEW ──────────────────────────► RUNNABLE
                                      │
                    ┌─────────────────┼─────────────────┐
                    │                 │                 │
              synchronized      wait()/join()    sleep()/wait(ms)
              lock unavailable       │                 │
                    │                 │                 │
                    ▼                 ▼                 ▼
                 BLOCKED           WAITING         TIMED_WAITING
                    │                 │                 │
              lock acquired      notify()/         timeout/notify/
                    │            thread dies         interrupt
                    └─────────────────┼─────────────────┘
                                      │
                              run() completes
                                      │
                                      ▼
                                 TERMINATED
```

### Interview One-Liner

> Six states: NEW → start() → RUNNABLE ⇄ BLOCKED/WAITING/TIMED_WAITING → TERMINATED. No RUNNING in Java. BLOCKED = synchronized lock. WAITING = wait/join. TIMED_WAITING = sleep/wait(ms).

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Can thread go from TERMINATED to RUNNABLE?

<details>
<summary>Show Answer</summary>

**Answer:** No. TERMINATED is final. Create a **new Thread** object to run the task again.

</details>

---

### Q: sleep() state — WAITING or TIMED_WAITING?

<details>
<summary>Show Answer</summary>

**Answer:** **TIMED_WAITING** — because `sleep()` has a specified duration. WAITING is for indefinite waits like `wait()` without timeout.

</details>

---

### Q: Two threads BLOCKED on same lock?

<details>
<summary>Show Answer</summary>

**Answer:** Yes. Multiple threads can be BLOCKED waiting for the same lock. When lock is released, one thread acquires it (RUNNABLE), others remain BLOCKED.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Memorize six states and their triggers: `start()`→RUNNABLE, `synchronized`→BLOCKED, `wait()`→WAITING, `sleep()`→TIMED_WAITING, run ends→TERMINATED. No RUNNING in Java API.

</details>
