# 20. Deadlock

## 20. Deadlock

## Very Common

---

# 1. What is Deadlock?

<details>
<summary>Show Answer</summary>

**Answer:**

A **deadlock** is a situation where two or more threads are **permanently blocked**, each waiting for a resource held by another—none can proceed and the application hangs forever.

### Simple Analogy

```text
Two cars on a narrow bridge — each blocked by the other
Car A waits for Car B to move
Car B waits for Car A to move
Neither moves → deadlock
```

### Classic Java Deadlock

```java
Object lockA = new Object();
Object lockB = new Object();

// Thread 1
new Thread(() -> {
    synchronized (lockA) {
        System.out.println("T1: holding A, waiting for B");
        synchronized (lockB) { // waits forever
            System.out.println("T1: got both");
        }
    }
}).start();

// Thread 2
new Thread(() -> {
    synchronized (lockB) {
        System.out.println("T2: holding B, waiting for A");
        synchronized (lockA) { // waits forever
            System.out.println("T2: got both");
        }
    }
}).start();
```

### Deadlock vs Other Blocking

| Situation | Can Recover? |
|-----------|--------------|
| **Deadlock** | ❌ Never — permanent unless external intervention |
| **BLOCKED on lock** | ✅ Yes — when lock holder releases |
| **WAITING on notify** | ✅ Yes — when notify/interrupt |
| **Livelock** | ⚠️ Threads active but make no progress |

### Symptoms in Production

```text
✅ Application hangs — no errors thrown
✅ CPU may be idle (threads blocked, not computing)
✅ Requests timeout — threads stuck waiting
✅ Thread dump shows circular lock dependency
```

**Interview Point:**

> Deadlock = circular wait — threads permanently blocked waiting on each other. No progress, no exception. Detect via thread dump.

</details>

---

# 2. Conditions for Deadlock?

<details>
<summary>Show Answer</summary>

**Answer:**

Deadlock occurs only when **all four Coffman conditions** are simultaneously true. Break **any one** condition and deadlock cannot happen.

### The Four Coffman Conditions

| # | Condition | Meaning |
|---|-----------|---------|
| 1 | **Mutual Exclusion** | Resource used by only one thread at a time |
| 2 | **Hold and Wait** | Thread holds one resource while waiting for another |
| 3 | **No Preemption** | Resources cannot be forcibly taken from holder |
| 4 | **Circular Wait** | Circular chain of threads waiting on each other |

### All Four Required

```text
If ANY condition is broken → deadlock IMPOSSIBLE

Example: break Circular Wait
  → always acquire locks in same order
  → no circular dependency → no deadlock
```

### Visual — Circular Wait

```text
Thread 1 holds Lock A → waits for Lock B
Thread 2 holds Lock B → waits for Lock A

    T1 ──holds──► Lock A
     │              │
  waits│              │held by
     │              │
    Lock B ◄──holds── T2
```

### Prevention Strategies Map

| Break Condition | Strategy |
|-----------------|----------|
| Mutual Exclusion | Use atomic variables (rarely practical) |
| Hold and Wait | Acquire all locks at once |
| No Preemption | Use `tryLock()` with timeout |
| Circular Wait | **Lock ordering** (most common fix) |

**Interview Point:**

> Deadlock needs **all 4 Coffman conditions**. Interview answer: list all four, then say "break any one to prevent." Lock ordering breaks Circular Wait — most practical fix.

</details>

---

### Coffman Conditions

---

# 3. Mutual Exclusion

<details>
<summary>Show Answer</summary>

**Answer:**

**Mutual Exclusion** means a resource can be used by **only one thread at a time**—if one thread holds it, others must wait.

### In Java

```java
synchronized (lock) {
    // only ONE thread here — mutual exclusion enforced
    sharedResource.modify();
}
```

### Why It's Necessary for Deadlock

```text
Without mutual exclusion:
  Multiple threads access resource simultaneously
  No blocking → no waiting → no deadlock possible

With mutual exclusion (synchronized):
  Thread holds lock → others block → waiting chain possible
```

### Example

```java
Object printer = new Object();

// Only one thread prints at a time
synchronized (printer) {
    System.out.println("Printing..."); // mutual exclusion
}
```

### Can We Break This Condition?

```text
Technically: use non-exclusive resources (read-only shared data)
Practically: almost always need mutual exclusion for mutable shared state
Better approach: break Circular Wait instead (lock ordering)
```

### Mutual Exclusion vs Synchronization

| Term | Scope |
|------|-------|
| **Mutual Exclusion** | General concept — one at a time |
| **Synchronization** | Java mechanism to achieve mutual exclusion |

**Interview Point:**

> Mutual Exclusion = resource exclusive to one thread. `synchronized` enforces it. Necessary for deadlock but alone doesn't cause it—needs all 4 conditions.

</details>

---

# 4. Hold and Wait

<details>
<summary>Show Answer</summary>

**Answer:**

**Hold and Wait** means a thread **already holds at least one lock** while **waiting to acquire another lock**—it won't release what it has while waiting.

### Classic Hold and Wait

```java
// Thread holds lockA, waits for lockB — does NOT release lockA
synchronized (lockA) {          // HOLDING lockA
    // ... some work ...
    synchronized (lockB) {      // WAITING for lockB
        // won't reach here until lockB free
        // but lockA still held!
    }
}
```

### Timeline

```text
Thread 1: acquire lockA (HOLD) → try lockB (WAIT) — holds A while waiting
Thread 2: acquire lockB (HOLD) → try lockA (WAIT) — holds B while waiting
→ Circular wait → deadlock
```

### Breaking Hold and Wait

**Strategy 1 — Acquire all locks upfront:**

```java
// Acquire both before doing work — no hold-and-wait
synchronized (lockA) {
    synchronized (lockB) {
        // work with both locks
    }
}
// Still need consistent ordering to avoid deadlock!
```

**Strategy 2 — `tryLock()` with backoff:**

```java
while (true) {
    if (lockA.tryLock()) {
        try {
            if (lockB.tryLock()) {
                try {
                    doWork();
                    return;
                } finally { lockB.unlock(); }
            }
        } finally { lockA.unlock(); }
    }
    Thread.sleep(50); // release and retry — breaks hold-and-wait
}
```

**Interview Point:**

> Hold and Wait = hold one lock while waiting for another. Break it with `tryLock()` + release + retry, or acquire all resources atomically upfront.

</details>

---

# 5. No Preemption

<details>
<summary>Show Answer</summary>

**Answer:**

**No Preemption** means a resource **cannot be forcibly taken** from a thread—it must be **voluntarily released** by the holder. The OS/JVM won't grab a lock away from a thread.

### In Java

```java
synchronized (lock) {
    // lock CANNOT be taken away by JVM or another thread
    // only released when block exits or exception
    longRunningOperation(); // others wait — no preemption
}
```

### Contrast with Preemptive Systems

```text
CPU scheduling: OS CAN preempt (take away CPU from thread)
Lock/monitor:    CANNOT preempt — holder keeps lock until release

Thread holds lock → runs or blocks others
No external force removes the lock
```

### Why No Preemption Enables Deadlock

```text
Thread 1 holds A, wants B — won't give up A
Thread 2 holds B, wants A — won't give up B
Neither releases → neither progresses → deadlock
If locks COULD be preempted → deadlock broken
```

### Breaking No Preemption

```java
ReentrantLock lockA = new ReentrantLock();
ReentrantLock lockB = new ReentrantLock();

// tryLock with timeout — effectively "preempt" by giving up
if (lockA.tryLock(100, TimeUnit.MILLISECONDS)) {
    try {
        if (lockB.tryLock(100, TimeUnit.MILLISECONDS)) {
            try { doWork(); }
            finally { lockB.unlock(); }
        } else {
            // couldn't get B — release A and retry (breaks no-preemption)
        }
    } finally { lockA.unlock(); }
}
```

### synchronized vs ReentrantLock

| | `synchronized` | `ReentrantLock.tryLock()` |
|---|----------------|---------------------------|
| Preemption | ❌ No | ✅ Timeout-based give-up |
| Forced release | Only on exit/exception | `unlock()` explicitly |

**Interview Point:**

> No Preemption = lock can't be forcibly taken. `synchronized` has no preemption. Use `tryLock(timeout)` to voluntarily release and retry — breaks this condition.

</details>

---

# 6. Circular Wait

<details>
<summary>Show Answer</summary>

**Answer:**

**Circular Wait** is a closed chain where each thread waits for a resource held by the next thread in the chain—the last waits for the first, forming a **cycle**.

### Two-Thread Circular Wait

```text
Thread 1: holds Lock A → waits for Lock B
Thread 2: holds Lock B → waits for Lock A

Cycle: T1 → waits B → held by T2 → waits A → held by T1 → cycle!
```

### Code Example

```java
Object lockA = new Object();
Object lockB = new Object();

// T1: A then B
new Thread(() -> {
    synchronized (lockA) {
        synchronized (lockB) { work(); }
    }
}).start();

// T2: B then A — OPPOSITE order → circular wait
new Thread(() -> {
    synchronized (lockB) {
        synchronized (lockA) { work(); }
    }
}).start();
```

### Three-Thread Circular Wait

```text
T1 holds L1 → waits L2
T2 holds L2 → waits L3
T3 holds L3 → waits L1
→ cycle: T1→T2→T3→T1
```

### Breaking Circular Wait — Lock Ordering (Best Fix)

```java
// Assign global order: always acquire lower ID first
Object lockA = new Object(); // id = 1
Object lockB = new Object(); // id = 2

// BOTH threads use same order: A then B
void safeWork(Object first, Object second) {
    synchronized (first) {
        synchronized (second) {
            doWork();
        }
    }
}

// T1: safeWork(lockA, lockB)  — A then B
// T2: safeWork(lockA, lockB)  — A then B — same order, no cycle!
```

### Lock Ordering with hashCode

```java
void transfer(Account from, Account to, int amount) {
    Account first  = from.hashCode() < to.hashCode() ? from : to;
    Account second = from.hashCode() < to.hashCode() ? to : from;

    synchronized (first) {
        synchronized (second) {
            from.debit(amount);
            to.credit(amount);
        }
    }
}
```

**Interview Point:**

> Circular Wait = cycle of threads waiting on each other. **Lock ordering** (always same global order) is the most practical fix — breaks circular wait.

</details>

---

## Advanced

---

# 7. How to detect Deadlock?

<details>
<summary>Show Answer</summary>

**Answer:**

Deadlock is detected at runtime by analyzing **thread dumps** for circular lock dependencies, or programmatically via `ThreadMXBean.findDeadlockedThreads()`.

### Method 1 — Thread Dump Analysis

```bash
# Linux — send signal to JVM
kill -3 <pid>

# Or jstack
jstack <pid> > thread_dump.txt

# JDK tools
jcmd <pid> Thread.print
```

### What to Look For in Thread Dump

```text
"Thread-1" BLOCKED on lockB
  waiting to lock object 0x... (lockB)
  locked object 0x... (lockA)

"Thread-2" BLOCKED on lockA
  waiting to lock object 0x... (lockA)
  locked object 0x... (lockB)

Found one Java-level deadlock:
  Thread-1 waits for lockB held by Thread-2
  Thread-2 waits for lockA held by Thread-1
```

### Method 2 — Programmatic Detection

```java
ThreadMXBean bean = ManagementFactory.getThreadMXBean();

long[] deadlocked = bean.findDeadlockedThreads();
if (deadlocked != null) {
    ThreadInfo[] infos = bean.getThreadInfo(deadlocked);
    for (ThreadInfo info : infos) {
        System.out.println("Deadlocked: " + info.getThreadName());
        System.out.println("  Waiting on: " + info.getLockName());
        System.out.println("  Held by: " + info.getLockOwnerName());
    }
}
```

### Method 3 — Monitoring Tools

```text
VisualVM    → Threads tab → detect deadlock button
JConsole    → Threads → deadlock detection
APM tools   → Dynatrace, AppDynamics alert on deadlock
```

### Detection vs Prevention

| Approach | When |
|----------|------|
| **Detection** | Production monitoring, post-incident analysis |
| **Prevention** | Design time — lock ordering, tryLock |
| **Avoidance** | Banker's algorithm (rare in practice) |

**Interview Point:**

> Detect via `jstack`/thread dump — look for BLOCKED threads with circular lock chain. Programmatic: `ThreadMXBean.findDeadlockedThreads()`. JVM often prints "Found Java-level deadlock" in dump.

</details>

---

# 8. How to avoid Deadlock?

<details>
<summary>Show Answer</summary>

**Answer:**

Avoid deadlock by breaking one or more Coffman conditions—most practically through **lock ordering**, **timeouts**, and **reducing lock scope**.

### Strategy 1 — Lock Ordering (Most Common)

```java
// Always acquire locks in consistent global order
private static final Object lockA = new Object();
private static final Object lockB = new Object();

void safeOperation() {
    synchronized (lockA) {      // always A first
        synchronized (lockB) {  // then B
            doWork();
        }
    }
}
```

### Strategy 2 — tryLock with Timeout

```java
ReentrantLock lockA = new ReentrantLock();
ReentrantLock lockB = new ReentrantLock();

boolean success = false;
while (!success) {
    try {
        if (lockA.tryLock(100, TimeUnit.MILLISECONDS)) {
            try {
                if (lockB.tryLock(100, TimeUnit.MILLISECONDS)) {
                    try {
                        doWork();
                        success = true;
                    } finally { lockB.unlock(); }
                }
            } finally { lockA.unlock(); }
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
    }
    if (!success) Thread.sleep(50); // backoff before retry
}
```

### Strategy 3 — Reduce Lock Scope

```java
// ❌ Hold lock during slow operation
synchronized (lock) {
    fetchFromDatabase(); // holds lock too long
    updateCache();
}

// ✅ Lock only critical section
Data data = fetchFromDatabase(); // no lock
synchronized (lock) {
    updateCache(data); // minimal lock time
}
```

### Strategy 4 — Use Concurrent Utilities

```java
// Avoid manual locking where possible
ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
AtomicInteger counter = new AtomicInteger();
BlockingQueue<Task> queue = new LinkedBlockingQueue<>();
```

### Strategy 5 — Single Lock

```java
// One lock for related resources — no circular wait possible
private final Object globalLock = new Object();

synchronized (globalLock) {
    updateAccountA();
    updateAccountB();
}
```

### Avoidance Checklist

```text
✅ Define global lock order — document it
✅ Use tryLock with timeout in complex scenarios
✅ Minimize critical section size
✅ Prefer concurrent collections over manual sync
✅ Avoid nested locks when possible
✅ Use higher-level APIs (ExecutorService, CompletableFuture)
```

**Interview Point:**

> Avoid deadlock: **lock ordering** (break circular wait), **tryLock + timeout** (break hold-and-wait), **concurrent utilities**, **minimize lock scope**. Lock ordering is the go-to answer.

</details>

---

# 9. Real production Deadlock example?

<details>
<summary>Show Answer</summary>

**Answer:**

A common production deadlock: **database connection pool + application lock** acquired in opposite order by two request-handling threads.

### Scenario — Transfer Service Deadlock

```java
class TransferService {
    private final Object accountLock = new Object();

    void transfer(Account from, Account to, int amount) {
        // Step 1: get DB connection (pool lock internally)
        Connection conn = connectionPool.getConnection(); // may block

        synchronized (accountLock) { // application lock
            conn.execute("UPDATE accounts...");
            conn.commit();
        }
        conn.close();
    }
}

class ReportService {
    private final Object accountLock = new Object();

    void generateReport() {
        synchronized (accountLock) { // holds application lock FIRST
            Connection conn = connectionPool.getConnection(); // waits for pool
            conn.execute("SELECT ...");
            conn.close();
        }
    }
}
```

### Deadlock Timeline

```text
Request A (Transfer):  gets DB conn → waits accountLock (held by B)
Request B (Report):    gets accountLock → waits DB conn (held by A)
→ DEADLOCK — app hangs, requests timeout
```

### Another Classic — Nested Service Calls

```java
// Service A calls Service B while holding lockA
synchronized (lockA) {
    serviceB.process(); // B tries to acquire lockB then lockA
}

// Service B calls Service A while holding lockB
synchronized (lockB) {
    serviceA.process(); // A tries lockA then lockB
}
```

### Spring @Transactional + Custom Lock

```text
Thread 1: @Transactional (DB lock) → custom synchronized block
Thread 2: custom synchronized block → @Transactional (DB lock)
Opposite order → production deadlock under load
```

### How It Was Fixed

```java
// Fix 1: consistent lock ordering
Account first  = from.hashCode() < to.hashCode() ? from : to;
Account second = from.hashCode() < to.hashCode() ? to : from;
synchronized (first) {
    synchronized (second) { transfer(from, to, amount); }
}

// Fix 2: get all resources before any lock
Connection conn = pool.getConnection();
try {
    synchronized (accountLock) { ... }
} finally { conn.close(); }

// Fix 3: reduce lock scope — DB transaction only
@Transactional
void transfer(Account from, Account to, int amount) {
    // no extra synchronized blocks inside
}
```

### Production Symptoms

```text
Sudden spike in request timeouts (30s+)
CPU drops — threads blocked, not working
Thread dump shows circular BLOCKED chain
Restart fixes temporarily — deadlock is timing-dependent
```

**Interview Point:**

> Real deadlock: opposite lock order across services (DB pool vs app lock, or nested service calls). Fix with consistent ordering and avoid holding locks during I/O.

</details>

---

# 10. How thread dump helps?

<details>
<summary>Show Answer</summary>

**Answer:**

A **thread dump** is a snapshot of all threads—their state, stack trace, and lock ownership—which reveals exactly which threads are deadlocked and what they're waiting on.

### How to Capture Thread Dump

```bash
# jstack (most common)
jstack -l <pid> > dump.txt

# kill signal (Linux)
kill -3 <pid>  # prints to JVM stdout/log

# jcmd
jcmd <pid> Thread.print

# VisualVM / JConsole GUI
```

### Key Information in Thread Dump

```text
Thread name and ID
Thread state: RUNNABLE, BLOCKED, WAITING, etc.
Stack trace — what code thread is executing
Lock info:
  - "locked object 0x..." — lock this thread HOLDS
  - "waiting to lock 0x..." — lock this thread WANTS
Lock owner — which thread holds the waited lock
```

### Deadlock Section in Dump

```text
"pool-1-thread-1" #12 BLOCKED
   java.lang.Thread.State: BLOCKED (on object monitor)
   at com.app.TransferService.transfer(TransferService.java:25)
   - waiting to lock object 0x000000076ab2c8f0 (lockB)
   - locked object 0x000000076ab2c8e0 (lockA)

"pool-1-thread-2" #13 BLOCKED
   at com.app.ReportService.report(ReportService.java:18)
   - waiting to lock object 0x000000076ab2c8e0 (lockA)
   - locked object 0x000000076ab2c8f0 (lockB)

Found one Java-level deadlock:
  pool-1-thread-1 waits for lockB held by pool-1-thread-2
  pool-1-thread-2 waits for lockA held by pool-1-thread-1
```

### What Thread Dump Reveals

| Info | Use |
|------|-----|
| **BLOCKED threads** | Which threads stuck waiting for locks |
| **Lock chain** | Circular dependency → deadlock proof |
| **Stack trace** | Exact line of code causing block |
| **RUNNABLE threads** | What's still running |
| **WAITING threads** | Threads on wait()/join() |
| **Thread count** | Too many threads? thread leak? |

### Analysis Workflow

```text
1. Capture dump when app hangs
2. Search for "BLOCKED" threads
3. Check "waiting to lock" vs "locked" pairs
4. Look for "Found Java-level deadlock" section
5. Map lock objects to your code (class names in dump)
6. Identify opposite lock ordering in stack traces
7. Apply fix: lock ordering / tryLock / reduce scope
```

### Thread Dump vs Heap Dump

| | Thread Dump | Heap Dump |
|---|-------------|-----------|
| Shows | Thread states, locks, stacks | Object memory, references |
| Use for | Deadlock, hangs, slow threads | Memory leaks, OOM |
| Tool | jstack | jmap, VisualVM |

**Interview Point:**

> Thread dump = snapshot of all threads. Shows BLOCKED state, held locks, waiting locks, stack traces. JVM auto-detects and reports "Found Java-level deadlock". First tool for production hangs.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Deadlock vs Livelock vs Starvation?

<details>
<summary>Show Answer</summary>

**Answer:**

| | Deadlock | Livelock | Starvation |
|---|----------|----------|------------|
| Threads active? | ❌ All blocked | ✅ Active but stuck | ⚠️ Some blocked |
| Progress? | ❌ None forever | ❌ None (busy retrying) | ❌ Some never run |
| Example | Circular lock wait | Two people stepping aside endlessly | Low-priority thread never scheduled |

```text
Deadlock:   T1 waits T2, T2 waits T1 — frozen
Livelock:   Both threads keep yielding — active but no work done
Starvation: Thread always loses lock race — never acquires lock
```

</details>

---

### Q: Can deadlock happen with one lock?

<details>
<summary>Show Answer</summary>

**Answer:**

**No.** Deadlock requires **circular wait** — at least two resources and two threads. One lock + one thread cannot form a cycle. A single thread re-entering same lock is **reentrant** — not deadlock.

</details>

---

### Q: Does synchronized always cause deadlock?

<details>
<summary>Show Answer</summary>

**Answer:**

**No.** `synchronized` alone doesn't cause deadlock. Deadlock needs **nested locks acquired in opposite order** by multiple threads. Single lock or consistent ordering = safe.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Deadlock = circular wait, all threads blocked forever. **4 Coffman conditions** — break any one. Fix: **lock ordering**, `tryLock` timeout, concurrent utilities. Detect: **jstack** thread dump, `ThreadMXBean.findDeadlockedThreads()`.

</details>
