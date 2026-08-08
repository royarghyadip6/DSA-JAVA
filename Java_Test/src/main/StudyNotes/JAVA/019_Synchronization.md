# 19. Synchronization

## 19. Synchronization

## Basics

---

# 1. What is synchronization?

<details>
<summary>Show Answer</summary>

**Answer:**

**Synchronization** is a mechanism that ensures **only one thread** accesses a shared resource at a time—preventing data corruption when multiple threads read/write the same data.

### Simple Analogy

```text
Bank vault — only ONE person inside at a time
Others wait outside until vault is free
Synchronization = vault lock for shared data
```

### In Java

```java
class Counter {
    private int count = 0;

    // Only one thread can execute this at a time
    public synchronized void increment() {
        count++; // shared resource — protected
    }

    public synchronized int getCount() {
        return count;
    }
}
```

### What It Controls

| Controls | Explanation |
|----------|-------------|
| **Mutual exclusion** | One thread at a time in critical section |
| **Visibility** | Changes visible to other threads after release |
| **Atomicity** | Operation completes fully before another thread enters |

### Ways to Achieve in Java

```text
1. synchronized keyword (method / block)
2. volatile (visibility only — not full mutex)
3. java.util.concurrent locks (ReentrantLock)
4. Atomic classes (AtomicInteger, etc.)
5. Thread-safe collections (ConcurrentHashMap)
```

**Interview Point:**

> Synchronization = **one thread at a time** on shared data. Prevents race conditions. In Java mainly via `synchronized` keyword and monitor locks.

</details>

---

# 2. Why synchronization needed?

<details>
<summary>Show Answer</summary>

**Answer:**

Synchronization is needed because multiple threads sharing data without protection cause **race conditions**, **inconsistent results**, and **data corruption**.

### Without Synchronization — Problem

```java
class Counter {
    int count = 0;

    void increment() {
        count++; // NOT atomic! 3 steps: read → add → write
    }
}

// Two threads both read count=5
// Both compute 6
// Both write 6
// Expected: 7 — Actual: 6 ❌
```

### Real Production Scenarios

| Scenario | Without Sync |
|----------|--------------|
| Bank balance update | Lost deposits |
| Inventory count | Overselling stock |
| Session counter | Wrong user counts |
| Cache update | Stale/inconsistent data |
| Singleton creation | Multiple instances |

### count++ Is Not Atomic

```text
count++ internally:
  1. READ count from memory
  2. ADD 1
  3. WRITE back to memory

Thread A reads 5
Thread B reads 5  (before A writes)
Both write 6 — one increment LOST
```

### With Synchronization — Fixed

```java
public synchronized void increment() {
    count++; // only one thread executes — safe
}
```

### When You DON'T Need Sync

```text
✅ Each thread has its own local variables
✅ Immutable objects (String, final fields)
✅ Thread-confined data (ThreadLocal)
✅ Concurrent collections designed for multi-thread
```

**Interview Point:**

> Sync needed when **multiple threads share mutable state**. `count++` is not atomic—classic lost-update problem. Always identify shared mutable data first.

</details>

---

# 3. Race condition?

<details>
<summary>Show Answer</summary>

**Answer:**

A **race condition** occurs when the **outcome depends on the unpredictable timing** of thread execution—multiple threads access shared data concurrently and at least one modifies it.

### Classic Example — Lost Update

```java
class BankAccount {
    int balance = 1000;

    void withdraw(int amount) {
        if (balance >= amount) {      // Thread A checks: 1000 >= 800 ✓
            // Thread B also checks: 1000 >= 800 ✓ (before A deducts)
            balance -= amount;        // A writes 200
            // B also writes 200 — should be -600 or reject!
        }
    }
}
```

### Race Condition Characteristics

```text
✅ Multiple threads access same data
✅ At least one thread MODIFIES data
✅ No proper synchronization
✅ Result varies run-to-run — nondeterministic
```

### Check-Then-Act Race

```java
// ❌ Race condition
if (!map.containsKey(key)) {
    map.put(key, value); // two threads may both put
}

// ✅ Fixed
synchronized (map) {
    if (!map.containsKey(key)) {
        map.put(key, value);
    }
}
```

### Lazy Initialization Race

```java
class Singleton {
    private static Singleton instance;

  // ❌ Race — two threads create two instances
    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }
}
```

### How to Prevent

```text
synchronized blocks/methods
volatile + double-checked locking
AtomicReference
Concurrent collections
Immutable objects
```

**Interview Point:**

> Race condition = outcome depends on thread timing. Shared mutable data + no sync = race. Fix with synchronization or atomic/concurrent utilities.

</details>

---

# 4. Critical section?

<details>
<summary>Show Answer</summary>

**Answer:**

A **critical section** is a block of code that accesses **shared resources**—only **one thread** should execute it at a time to avoid race conditions.

### Definition

```text
Critical Section = code region that:
  1. Accesses shared mutable data
  2. Must not run concurrently with other critical sections on same data
  3. Requires mutual exclusion (mutex)
```

### Example — Identifying Critical Section

```java
class SharedCounter {
    private int count = 0;        // shared resource

    // ─── CRITICAL SECTION ───
    public void increment() {
        count++;                  // shared data modified
    }
    // ─── END CRITICAL SECTION ───

    // NOT critical — local variable only
    public void logLocal() {
        int local = 10;
        System.out.println(local);
    }
}
```

### Protecting Critical Section

```java
// Method-level protection
public synchronized void increment() {
    count++; // entire method = critical section
}

// Block-level protection — finer control
public void increment() {
    // non-critical code here — no lock needed
    synchronized (this) {
        count++; // only this block protected
    }
    // more non-critical code — lock released
}
```

### Critical Section vs Synchronized Block

| Term | Meaning |
|------|---------|
| **Critical section** | Concept — code that needs protection |
| **Synchronized block** | Java mechanism to protect critical section |

### Smaller Critical Sections = Better Performance

```java
// ❌ Lock held too long
public synchronized void process() {
    fetchFromDB();    // slow — holds lock unnecessarily
    updateCounter();  // actually needs lock
    sendEmail();      // slow — holds lock unnecessarily
}

// ✅ Lock only what's needed
public void process() {
    Data data = fetchFromDB();    // no lock
    synchronized (this) {
        updateCounter(data);      // critical section only
    }
    sendEmail();                  // no lock
}
```

**Interview Point:**

> Critical section = shared mutable data access code. Protect with sync. **Minimize** critical section size—hold lock only as long as needed.

</details>

---

# 5. Thread interference?

<details>
<summary>Show Answer</summary>

**Answer:**

**Thread interference** occurs when multiple threads operate on shared data in overlapping ways, causing **incorrect or inconsistent results**—a visible symptom of race conditions.

### Example — Thread Interference

```java
class SharedData {
    int x = 0;
    int y = 0;

    void update() {
        x = 1;
        y = 2;
        // Another thread might read x=1, y=0 (partial update)
    }

    void read() {
        System.out.println("x=" + x + ", y=" + y);
        // May print x=1, y=0 instead of x=1, y=2
    }
}
```

### Interference Timeline

```text
Thread A (update):  x=1  →  y=2
Thread B (read):         reads x=1, y=0  ← interference!
                     ↑ reads between A's two writes
```

### StringBuilder Interference

```java
StringBuilder sb = new StringBuilder(); // NOT thread-safe

// Two threads append simultaneously
thread1: sb.append("Hello");
thread2: sb.append("World");

// Result unpredictable: "HelloWorld", "WorldHello", garbled chars
```

### Thread Interference vs Memory Consistency

| Issue | Cause |
|-------|-------|
| **Thread interference** | Overlapping operations on shared data |
| **Memory consistency** | Changes not visible across threads |

Both fixed by proper synchronization.

### Prevention

```java
// Use thread-safe alternative
StringBuffer sb = new StringBuffer(); // synchronized internally

// Or external synchronization
synchronized (sb) {
    sb.append("Hello");
}

// Or concurrent utilities
ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
```

**Interview Point:**

> Thread interference = overlapping ops on shared data → wrong results. Use sync, thread-safe collections, or atomic classes to prevent.

</details>

---

## Synchronized Keyword

---

# 6. Synchronized method?

<details>
<summary>Show Answer</summary>

**Answer:**

A **synchronized method** uses the `synchronized` keyword on a method—the thread must acquire the **monitor lock** on the object (or class) before executing the entire method body.

### Instance Synchronized Method

```java
class Counter {
    private int count = 0;

    // Lock on THIS object (monitor = this)
    public synchronized void increment() {
        count++;
    }

    public synchronized int getCount() {
        return count;
    }
}
```

### Static Synchronized Method

```java
class Counter {
    private static int count = 0;

    // Lock on Counter.class object
    public static synchronized void increment() {
        count++;
    }
}
```

### How It Works

```text
Thread calls synchronized method
    ↓
Try acquire lock on monitor object
    ↓
Lock available → acquire → execute method → release lock
Lock held     → BLOCKED state → wait → acquire when free
```

### Equivalent Block Form

```java
// synchronized method
public synchronized void increment() {
    count++;
}

// equivalent synchronized block
public void increment() {
    synchronized (this) {
        count++;
    }
}

// static synchronized
public static synchronized void increment() {
    count++;
}
// equivalent:
public static void increment() {
    synchronized (Counter.class) {
        count++;
    }
}
```

### Rules

```text
✅ Only ONE thread per monitor at a time
✅ Same object lock → methods block each other
✅ Different object locks → can run in parallel
✅ Reentrant — same thread can re-enter
```

**Interview Point:**

> `synchronized` method = lock on `this` (instance) or `Class` object (static). Entire method body is critical section. One thread per monitor.

</details>

---

# 7. Synchronized block?

<details>
<summary>Show Answer</summary>

**Answer:**

A **synchronized block** locks on a **specific object** for only a **portion of code**—more flexible and often more efficient than synchronizing entire methods.

### Syntax

```java
synchronized (lockObject) {
    // critical section — only this block protected
}
```

### Example — Finer Control

```java
class OrderService {
    private final Object lock = new Object();
    private int orderCount = 0;

    public void processOrder(Order order) {
        // Step 1 — no lock needed (slow DB call)
        validateOrder(order);

        // Step 2 — only counter needs protection
        synchronized (lock) {
            orderCount++;
        }

        // Step 3 — no lock needed
        sendConfirmation(order);
    }
}
```

### synchronized Block vs synchronized Method

| | Synchronized Method | Synchronized Block |
|---|---------------------|-------------------|
| Lock object | `this` or `Class` | Any object you specify |
| Scope | Entire method | Only block inside |
| Flexibility | Less | More — choose lock object |
| Performance | May hold lock too long | Minimize lock duration |

### Different Lock Objects

```java
class Service {
    private final Object lockA = new Object();
    private final Object lockB = new Object();
    int a = 0, b = 0;

    void updateA() {
        synchronized (lockA) { a++; } // only blocks other updateA/updateB-on-A
    }

    void updateB() {
        synchronized (lockB) { b++; } // parallel with updateA!
    }
}
```

### Common Pattern — Private Lock Object

```java
// ✅ Better than synchronized(this)
private final Object lock = new Object();

public void doWork() {
    synchronized (lock) { ... }
}
// External code cannot lock on your object accidentally
```

**Interview Point:**

> Synchronized block = lock specific object, protect only needed code. Prefer **private lock object** over `synchronized(this)`. Better performance and encapsulation.

</details>

---

# 8. Object-level lock?

<details>
<summary>Show Answer</summary>

**Answer:**

An **object-level lock** (instance lock) is acquired when synchronizing on an **instance object**—typically `this`—only threads sharing that same object instance compete for the lock.

### How Instance Lock Works

```java
class Counter {
    private int count = 0;

    public synchronized void increment() {
        count++; // lock = this (current Counter instance)
    }
}
```

### Multiple Instances — No Blocking

```java
Counter c1 = new Counter();
Counter c2 = new Counter();

// These run IN PARALLEL — different lock objects
new Thread(() -> c1.increment()).start(); // lock on c1
new Thread(() -> c2.increment()).start(); // lock on c2 — no wait
```

### Same Instance — Blocking

```java
Counter c = new Counter();

new Thread(() -> c.increment()).start(); // acquires lock on c
new Thread(() -> c.increment()).start(); // BLOCKED — same lock on c
```

### Explicit Object Lock

```java
Object lock = new Object();

synchronized (lock) {
    // object-level lock on 'lock' object
}

synchronized (this) {
    // object-level lock on current instance
}
```

### Object Lock Summary

```text
Monitor = any Java object
Each object has ONE intrinsic lock (monitor)
Instance sync method → lock on this
synchronized(obj)    → lock on obj
One thread per object lock at a time
Different objects → independent locks
```

**Interview Point:**

> Object-level lock = intrinsic monitor on a specific object instance. `synchronized` method locks `this`. Different instances = different locks = parallel execution.

</details>

---

# 9. Class-level lock?

<details>
<summary>Show Answer</summary>

**Answer:**

A **class-level lock** is acquired when synchronizing on the **Class object**—used by `static synchronized` methods. All threads across all instances share this single lock.

### Static Synchronized Method

```java
class Counter {
    private static int count = 0;

    // Class-level lock = Counter.class
    public static synchronized void increment() {
        count++;
    }
}
```

### Equivalent Block Form

```java
public static void increment() {
    synchronized (Counter.class) {
        count++;
    }
}
```

### Class Lock vs Object Lock

| | Object Lock | Class Lock |
|---|-------------|------------|
| Lock object | `this` (instance) | `ClassName.class` |
| Scope | Per instance | All instances + static |
| Used by | Instance sync method | Static sync method |
| Threads blocked | Same instance only | ALL threads on class |

### Example — Class Lock Blocks Everything

```java
Counter c1 = new Counter();
Counter c2 = new Counter();

// BOTH blocked — same class-level lock
new Thread(() -> c1.incrementStatic()).start();
new Thread(() -> c2.incrementStatic()).start();
// c1 and c2 are different objects but share Counter.class lock
```

### Instance vs Static — Different Locks

```java
class MyClass {
    public synchronized void instanceMethod() { }  // lock = this
    public static synchronized void staticMethod() { } // lock = MyClass.class
}

// instanceMethod on obj1 and staticMethod can run PARALLEL
// — different locks (this vs MyClass.class)
```

### When Class Lock Matters

```text
Static shared state (static counters, caches)
Singleton pattern (static getInstance)
Class-level initialization
Static factory methods
```

**Interview Point:**

> Class-level lock = `ClassName.class` monitor. `static synchronized` uses it. ONE lock for entire class—all instances share it. Different from instance lock on `this`.

</details>

---

## Locking Questions

---

# 10. How many threads can enter synchronized block?

<details>
<summary>Show Answer</summary>

**Answer:**

Only **ONE thread** can hold the monitor lock and execute inside a synchronized block on the same lock object at any time—all other threads **block** until the lock is released.

### Single Thread Rule

```text
Same lock object:
  Thread 1 inside synchronized block  ✅
  Thread 2 trying same lock           ❌ BLOCKED
  Thread 3 trying same lock           ❌ BLOCKED

When Thread 1 exits block → releases lock
  One waiting thread acquires lock → RUNNABLE
```

### Demo

```java
Object lock = new Object();

for (int i = 0; i < 5; i++) {
    new Thread(() -> {
        synchronized (lock) {
            System.out.println(Thread.currentThread().getName() + " inside");
            Thread.sleep(2000);
            System.out.println(Thread.currentThread().getName() + " leaving");
        }
    }).start();
}
// Output: only ONE thread inside at a time — sequential execution
```

### Different Locks — Multiple Threads Parallel

```java
Object lock1 = new Object();
Object lock2 = new Object();

// Thread A: synchronized(lock1)  — parallel OK
// Thread B: synchronized(lock2)  — different locks
```

### Reentrant — Same Thread Can Re-enter

```java
public synchronized void methodA() {
    methodB(); // same thread re-enters — OK (reentrant)
}

public synchronized void methodB() {
  // same lock — same thread holds it already
}
```

### Read vs Write — Still One Thread

```text
Even read-only access in synchronized block:
  Still only ONE thread at a time
  synchronized does NOT support multiple readers
  (Use ReadWriteLock for that)
```

**Interview Point:**

> **One thread** per monitor lock at a time. Others BLOCKED. Different lock objects allow parallelism. Same thread can re-enter (reentrant).

</details>

---

# 11. Which object acts as monitor?

<details>
<summary>Show Answer</summary>

**Answer:**

Every Java object has an **intrinsic monitor (lock)**. The monitor object depends on how synchronization is declared:

### Monitor Object by Context

| Code | Monitor (Lock) Object |
|------|------------------------|
| `synchronized` instance method | `this` (current object) |
| `synchronized` static method | `ClassName.class` |
| `synchronized (obj)` block | `obj` (specified object) |
| `synchronized (this)` block | `this` |

### Examples

```java
class Example {
    private final Object customLock = new Object();

    // Monitor = this
    public synchronized void method1() { }

    // Monitor = Example.class
    public static synchronized void method2() { }

    // Monitor = customLock
    public void method3() {
        synchronized (customLock) { }
    }

    // Monitor = any object
    public void method4() {
        synchronized ("literal") { } // ⚠️ bad practice — shared string pool
    }
}
```

### Object Header — Monitor Storage

```text
Every object in heap has object header:
  Mark Word → stores lock state, hashCode, GC age
  When synchronized → mark word points to monitor structure
```

### String Literal Lock — Danger!

```java
// ❌ NEVER lock on string literal
synchronized ("LOCK") {
    // "LOCK" is in string pool — ALL code using "LOCK" shares same lock!
}

// ✅ Use private final object
private final Object lock = new Object();
synchronized (lock) { }
```

### Class Object as Monitor

```java
// All static synchronized methods on Counter share Counter.class monitor
class Counter {
    public static synchronized void a() { } // Counter.class
    public static synchronized void b() { } // Counter.class — same lock
}
```

**Interview Point:**

> Monitor = intrinsic lock on any object. Instance method → `this`. Static method → `Class`. Block → specified object. Never lock on string literals.

</details>

---

# 12. What is monitor lock?

<details>
<summary>Show Answer</summary>

**Answer:**

A **monitor lock** (intrinsic lock) is the built-in synchronization mechanism associated with every Java object—it provides **mutual exclusion** and **visibility** for synchronized code.

### Monitor Concept

```text
Monitor = mutex + condition variable built into every object
  - Only one thread owns monitor at a time
  - Other threads wait in entry set (blocked queue)
  - wait()/notify() use same monitor
```

### Monitor States (Conceptual)

```text
UNLOCKED  → no thread holds lock
LOCKED    → one thread holds lock (owner)
BLOCKED   → threads waiting to acquire lock
WAITING   → owner called wait() — released lock, waits for notify
```

### Entry and Exit

```java
synchronized (lock) {
    // ENTRY: acquire monitor on lock
    // if free → acquire immediately
    // if held → thread BLOCKED until release
    criticalSection();
    // EXIT: release monitor automatically
}
```

### Monitor vs Explicit Lock

| | Monitor (intrinsic) | ReentrantLock |
|---|---------------------|---------------|
| Keyword | `synchronized` | `lock()` / `unlock()` |
| Auto release | ✅ On block exit | ❌ Must call unlock() |
| tryLock | ❌ | ✅ |
| Fairness option | ❌ | ✅ |
| Condition vars | wait/notify | multiple Conditions |

### wait/notify Use Same Monitor

```java
synchronized (lock) {
    while (queue.isEmpty()) {
        lock.wait();   // release monitor, wait for notify
    }
    item = queue.remove();
}

synchronized (lock) {
    queue.add(item);
    lock.notify();   // must hold same monitor
}
```

**Interview Point:**

> Monitor = intrinsic lock on every object. Provides mutex + wait/notify. `synchronized` acquires/releases automatically. Foundation of Java thread coordination.

</details>

---

# 13. Reentrant lock behavior?

<details>
<summary>Show Answer</summary>

**Answer:**

Java's intrinsic locks are **reentrant**—a thread that already holds a lock can **acquire the same lock again** without blocking itself (no self-deadlock).

### Reentrant Example

```java
class ReentrantDemo {
    public synchronized void methodA() {
        System.out.println("In methodA");
        methodB(); // re-enters same lock — no deadlock!
    }

    public synchronized void methodB() {
        System.out.println("In methodB");
    }
}
// Same thread: acquires lock in A → re-acquires in B → releases B → releases A
```

### How Reentrancy Works Internally

```text
Monitor tracks:
  owner thread
  recursion count (hold count)

Thread T acquires lock:   owner=T, count=1
T acquires again:         owner=T, count=2  (no block)
T releases once:          owner=T, count=1
T releases again:         owner=null, count=0  (fully released)
```

### Without Reentrancy — Deadlock!

```java
// If locks were NOT reentrant:
methodA() acquires lock
  → calls methodB() which tries same lock
  → thread blocks on ITSELF → deadlock ❌
```

### ReentrantLock Class

```java
ReentrantLock lock = new ReentrantLock();

lock.lock();
try {
    // can call lock.lock() again — reentrant
    lock.lock();
    try {
        // nested critical section
    } finally {
        lock.unlock();
    }
} finally {
    lock.unlock(); // must unlock same number of times as lock()
}
```

### Reentrancy Scope

```text
✅ Same thread, same lock object — reentrant
❌ Different threads, same lock — one blocks
❌ Same thread, different lock objects — independent
```

**Interview Point:**

> Reentrant = thread holding lock can acquire it again. Tracked by hold count. Prevents self-deadlock when synchronized methods call each other.

</details>

---

## Advanced

---

# 14. What happens internally when thread enters synchronized block?

<details>
<summary>Show Answer</summary>

**Answer:**

When a thread enters a synchronized block, the JVM attempts to **acquire the object's monitor**—if successful it executes the code; if not, the thread enters **BLOCKED** state until the lock is free.

### Step-by-Step Internal Flow

```text
1. Thread reaches monitorenter bytecode instruction
2. JVM checks object's mark word (lock state)
3. Lock FREE:
     a. Thread becomes lock owner
     b. Set mark word to locked state
     c. Increment hold count (if reentrant)
     d. Execute synchronized block
4. Lock HELD by another thread:
     a. Thread state → BLOCKED
     b. Thread added to object's entry set (wait queue)
     c. OS parks thread (no CPU consumption)
5. On monitorexit (block exit):
     a. Decrement hold count
     b. If count=0 → release lock
     c. Wake one/more threads in entry set
     d. One thread competes for lock
```

### Bytecode Level

```java
synchronized (lock) {
    count++;
}

// Compiled roughly to:
monitorenter   // acquire lock on 'lock' object
count++;
monitorexit    // release lock
// + exception handler monitorexit (always releases on exception)
```

### Object Mark Word

```text
Object header (64-bit):
  Normal: hashCode | age | 0 | 01 (unlocked)
  Locked: pointer to lock record | 00
  Heavy:  pointer to monitor | 10 (contended)
```

### BLOCKED → RUNNABLE Transition

```text
Lock holder exits synchronized block
    ↓
JVM signals waiting threads
    ↓
One thread acquires lock → RUNNABLE
    ↓
Scheduler runs thread → enters block
```

### Exception Safety

```java
// monitorexit ALWAYS called — even on exception
synchronized (lock) {
    throw new RuntimeException(); // lock still released!
}
```

**Interview Point:**

> Enter sync block → `monitorenter` → acquire monitor or BLOCKED. Exit → `monitorexit` → release. Lock always released on exception too.

</details>

---

# 15. How JVM implements synchronization?

<details>
<summary>Show Answer</summary>

**Answer:**

The JVM implements synchronization through **object headers (mark word)**, **lock records on stack**, and **heavyweight monitors**—with optimizations that start lightweight and escalate only when contested.

### Three Implementation Levels

```text
1. Biased Locking    → no contention, CAS on mark word
2. Lightweight Lock  → low contention, CAS + spin
3. Heavyweight Lock  → high contention, OS mutex + monitor
```

### Object Header — Mark Word

```text
Every object has 64-bit mark word in header:
  Stores: hashCode, GC age, lock state bits

Lock state encoding:
  01 = unlocked/biased
  00 = lightweight locked
  10 = heavyweight locked
  11 = marked for GC
```

### Lightweight Lock — Lock Record

```text
Thread wants lock:
  1. Create Lock Record on thread's stack
  2. CAS mark word → point to Lock Record
  3. Success → thread owns lock (no OS call)
  4. Failure → spin or escalate to heavyweight
```

### Heavyweight Monitor

```text
When CAS fails repeatedly:
  1. Allocate Monitor object in heap
  2. Mark word points to Monitor
  3. Monitor has:
     - Owner thread
     - Entry set (blocked threads queue)
     - Wait set (wait() threads)
  4. OS mutex used — thread parked/unparked
```

### Bytecode Instructions

```text
monitorenter  → acquire intrinsic lock
monitorexit   → release intrinsic lock
```

### Evolution Across Java Versions

| Version | Feature |
|---------|---------|
| Java 6 | Lock biasing introduced |
| Java 7 | Biased locking default |
| Java 15+ | Biased locking **disabled by default** (JEP 374) |
| Java 8+ | Lightweight + heavyweight still used |

**Interview Point:**

> JVM sync = mark word + CAS (lightweight) → Monitor + OS mutex (heavyweight). `monitorenter`/`monitorexit` bytecode. Escalates under contention.

</details>

---

# 16. Lock escalation?

<details>
<summary>Show Answer</summary>

**Answer:**

**Lock escalation** is the JVM's process of upgrading a lock from a **cheaper lightweight form** to a **more expensive heavyweight form** when contention increases.

### Escalation Path

```text
No lock needed (biased)
    ↓ contention detected
Revoke bias → Lightweight Lock (CAS)
    ↓ CAS fails repeatedly
Escalate → Heavyweight Monitor (OS mutex)
```

### Biased Locking → Lightweight

```text
Biased lock: object "biased" toward one thread
  First access by biased thread → no CAS, near-zero cost
  Different thread accesses → bias revoked
  → upgrade to lightweight lock (CAS on mark word)
```

### Lightweight → Heavyweight

```text
Lightweight lock: CAS on mark word
  Thread A CAS succeeds → owns lock
  Thread B CAS fails → spins briefly
  B keeps failing → inflate to heavyweight Monitor
  B parked by OS → BLOCKED state
```

### Why Escalate?

| Lock Type | Cost | When Used |
|-----------|------|-----------|
| Biased | ~0 overhead | Single thread, no contention |
| Lightweight | CAS only | Brief contention, low thread count |
| Heavyweight | OS mutex + park | Sustained contention |

### Escalation Is One-Way (Inflation)

```text
Biased → Lightweight  ✅ can escalate
Lightweight → Heavyweight ✅ can escalate (inflate)
Heavyweight → Lightweight ❌ does NOT de-escalate
  (Monitor stays inflated for object's lifetime)
```

### Java 15+ Change

```text
Biased locking disabled by default (JEP 374)
  Objects start unlocked → lightweight on first sync
  Escalation path: Unlocked → Lightweight → Heavyweight
```

### Practical Impact

```java
// Low contention — lightweight CAS, fast
synchronized (lock) { quickOp(); }

// High contention — escalates to heavyweight, slower
// 100 threads fighting for same lock → OS-level blocking
```

**Interview Point:**

> Lock escalation = biased → lightweight (CAS) → heavyweight (monitor). One-way inflation under contention. Biased locking disabled in Java 15+ by default.

</details>

---

# 17. Lock optimization in JVM?

<details>
<summary>Show Answer</summary>

**Answer:**

The JVM applies several **lock optimizations** to reduce synchronization overhead—biased locking, lightweight locks, lock coarsening, lock elision, and adaptive spinning.

### Optimization Techniques

| Optimization | What It Does |
|--------------|--------------|
| **Biased locking** | Assume one thread owns lock — skip CAS |
| **Lightweight locking** | CAS instead of OS mutex |
| **Adaptive spinning** | Spin before parking (avoid OS call) |
| **Lock coarsening** | Merge adjacent synchronized blocks |
| **Lock elision** | Remove lock if object not escaped (JIT) |

### Lock Elision (Escape Analysis)

```java
public void method() {
    Object lock = new Object(); // object never escapes method
    synchronized (lock) {
        compute(); // JIT may REMOVE lock entirely!
    }
}
// Escape analysis proves no other thread can access 'lock'
// → synchronization eliminated at runtime
```

### Lock Coarsening

```java
// JIT may merge into one lock acquisition:
synchronized (lock) { a++; }
synchronized (lock) { b++; }
synchronized (lock) { c++; }

// Optimized to:
synchronized (lock) {
    a++; b++; c++; // one lock acquire/release
}
```

### Adaptive Spinning

```text
Thread fails to acquire lightweight lock:
  Instead of immediate OS park:
    Spin for short time (CPU busy-wait)
    If lock released during spin → acquire without OS call
    If spin expires → park thread (heavyweight)
  Spin duration adapts based on history
```

### Biased Locking (Pre-Java 15)

```text
Object biased to thread T1:
  T1 enters synchronized → no atomic ops at all
  Near-zero cost for single-threaded access to sync code
```

### What You Cannot Rely On

```text
❌ Optimizations are JVM/JIT decisions — not guaranteed
❌ Don't assume lock elision happens
❌ Performance varies by JVM version, workload, -XX flags
✅ Write correct sync code — let JVM optimize
```

### Tuning Flags (Advanced)

```text
-XX:+UseBiasedLocking       (disabled by default Java 15+)
-XX:BiasedLockingStartupDelay=0
-XX:+EliminateLocks          (lock elision)
-XX:+DoEscapeAnalysis
```

**Interview Point:**

> JVM optimizes locks: elision (escape analysis), coarsening, adaptive spin, lightweight CAS. Don't code for optimizations—write correct sync, JVM handles performance.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: synchronized vs volatile?

<details>
<summary>Show Answer</summary>

**Answer:**

| | `synchronized` | `volatile` |
|---|----------------|------------|
| Atomicity | ✅ Full block/method | ❌ Only read/write of variable |
| Visibility | ✅ | ✅ |
| Blocking | ✅ Can block threads | ❌ No blocking |
| Use for | Critical sections | Single variable visibility flag |

```java
volatile boolean flag = true; // visibility only
synchronized (lock) { count++; } // atomicity + visibility
```

</details>

---

### Q: Can synchronized block be nested?

<details>
<summary>Show Answer</summary>

**Answer:**

Yes. Same thread can nest synchronized blocks on the **same lock** (reentrant) or **different locks**. Different locks — no blocking between nested blocks on different monitors.

```java
synchronized (lockA) {
    synchronized (lockB) { } // OK — different locks
    synchronized (lockA) { } // OK — reentrant
}
```

</details>

---

### Q: Deadlock with two locks?

<details>
<summary>Show Answer</summary>

**Answer:**

Yes — classic deadlock when two threads acquire locks in opposite order:

```java
// Thread 1: lockA → lockB
// Thread 2: lockB → lockA
// Both wait forever → deadlock

// Fix: always acquire locks in same global order
```

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Sync = one thread per monitor. Instance lock = `this`, class lock = `Class`. Critical section needs mutex. Monitor = intrinsic lock on every object. Reentrant locks. JVM: lightweight CAS → heavyweight monitor under contention. Lock elision/coarsening by JIT.

</details>
