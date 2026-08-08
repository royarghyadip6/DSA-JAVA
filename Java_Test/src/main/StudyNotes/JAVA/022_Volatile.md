# 22. Volatile Keyword

## 22. Volatile Keyword

## Most Asked

---

# 1. What is volatile?

<details>
<summary>Show Answer</summary>

**Answer:**

`volatile` is a keyword that ensures a variable's value is **always read from main memory** and **written to main memory immediately**—guaranteeing **visibility** across threads.

### Declaration

```java
private volatile boolean flag = false;
private volatile int status = 0;
```

### What volatile Guarantees

| Guarantee | Meaning |
|-----------|---------|
| **Visibility** | Write visible to all threads immediately |
| **Ordering** | Prevents certain instruction reordering |
| **No caching** | Reads/writes go to main memory, not CPU cache |

### Simple Example — Stop Thread

```java
class Worker implements Runnable {
    private volatile boolean running = true;

    public void run() {
        while (running) { // always reads fresh value from main memory
            doWork();
        }
        System.out.println("Worker stopped");
    }

    public void stop() {
        running = false; // immediately visible to worker thread
    }
}
```

### Without volatile — May Never Stop

```java
// ❌ Without volatile
private boolean running = true;

// Worker thread may cache running=true in CPU register
// stop() sets running=false in main memory
// Worker never sees the change → infinite loop!
```

### What volatile Does NOT Guarantee

```text
❌ Atomicity — count++ still not safe
❌ Mutual exclusion — no locking
❌ Compound operations — i++, i = i + 1 unsafe
```

### Valid Use Cases

```java
volatile boolean shutdownFlag;     // status flag
volatile int state;              // single write, multiple read
volatile long timestamp;         // visibility for long/double
volatile Object reference;     // reference assignment visibility
```

**Interview Point:**

> `volatile` = **visibility** guarantee. Reads/writes bypass CPU cache → main memory. Good for flags and status. **Not** a replacement for synchronization on compound operations.

</details>

---

# 2. Why volatile introduced?

<details>
<summary>Show Answer</summary>

**Answer:**

`volatile` was introduced to solve the **visibility problem**—when threads read stale values from CPU cache instead of the latest value in main memory, causing incorrect behavior without any synchronization.

### The Problem Before volatile

```text
Modern CPUs have multiple cores with separate caches
Thread writes variable → may stay in CPU cache
Other thread reads → gets OLD cached value
No error thrown — silent wrong behavior
```

### Example — Flag Never Seen

```java
class Task {
    boolean done = false; // no volatile

    void execute() {
        new Thread(() -> {
            while (!done) { } // may loop forever
        }).start();

        done = true; // main thread sets — worker may never see it
    }
}
```

### Why synchronized Was Too Heavy

```text
For simple flags/status variables:
  synchronized → acquires lock → overhead
  volatile     → just visibility → lightweight

volatile introduced for:
  ✅ One writer, multiple readers scenarios
  ✅ Status flags (shutdown, initialized)
  ✅ Avoid lock overhead for visibility only
```

### Java Memory Model Fix

```text
Before JMM (Java 1.0-1.4):
  No formal visibility guarantees
  Compiler/CPU could reorder freely
  Programs behaved unpredictably on multi-core

Java 5+ JMM + volatile:
  Formal visibility rules
  volatile establishes happens-before
  Predictable cross-thread behavior
```

### Real Production Need

```java
// Shutdown flag — lightweight, no lock needed
private volatile boolean shutdown = false;

public void shutdown() {
    shutdown = true; // all threads see immediately
}

// Worker threads check shutdown in loop
while (!shutdown) {
    processTask();
}
```

**Interview Point:**

> volatile solves **visibility** without lock overhead. Introduced with Java Memory Model (Java 5) for flags/status where full synchronization is unnecessary.

</details>

---

# 3. Visibility issue?

<details>
<summary>Show Answer</summary>

**Answer:**

A **visibility issue** occurs when one thread modifies a shared variable but **other threads continue reading an old cached copy**—they never see the update.

### How Visibility Breaks

```text
Thread A writes x = 10
  → value stored in CPU cache (not main memory yet)

Thread B reads x
  → reads from its own CPU cache → gets x = 0 (stale!)

No synchronization → no guarantee of visibility
```

### Example

```java
class Shared {
    int x = 0; // no volatile, no sync
}

Shared shared = new Shared();

// Thread 1
new Thread(() -> {
    shared.x = 100;
    System.out.println("T1 wrote 100");
}).start();

// Thread 2
new Thread(() -> {
    Thread.sleep(1000);
    System.out.println("T2 reads: " + shared.x); // may print 0!
}).start();
```

### Visibility vs Atomicity

| Issue | Problem | Fix |
|-------|---------|-----|
| **Visibility** | Thread sees stale value | `volatile`, `synchronized` |
| **Atomicity** | Operation partially done | `synchronized`, `AtomicInteger` |

```java
// Visibility issue — fixed by volatile
volatile boolean flag = false;

// Atomicity issue — volatile NOT enough
volatile int count = 0;
count++; // still unsafe — volatile doesn't fix this
```

### When Visibility Matters

```text
✅ One thread writes, others read (flags, status)
✅ Configuration values updated rarely
✅ Shutdown/cancellation flags
❌ Compound operations (increment, add) — need atomic/sync
```

### Fix Options

```java
// Option 1: volatile
volatile boolean flag;

// Option 2: synchronized
synchronized void setFlag() { flag = true; }
synchronized boolean getFlag() { return flag; }

// Option 3: AtomicBoolean
AtomicBoolean flag = new AtomicBoolean(false);
```

**Interview Point:**

> Visibility issue = thread reads **stale cached value**, not latest main memory value. `volatile` forces read/write through main memory. Classic infinite loop with non-volatile flag.

</details>

---

# 4. What is cache memory problem?

<details>
<summary>Show Answer</summary>

**Answer:**

The **cache memory problem** occurs because each CPU core has its own **local cache**—when a thread updates a variable, the change may stay in the core's cache and not reach main memory or other cores' caches immediately.

### CPU Architecture

```text
Main Memory (RAM)
    ↑↓
Core 1 Cache    Core 2 Cache    Core 3 Cache
    ↑               ↑               ↑
 Thread 1        Thread 2        Thread 3
```

### The Problem Step-by-Step

```text
1. Thread 1 (Core 1): reads x=0 from main memory → caches in L1
2. Thread 1: writes x=1 → updates L1 cache only
3. Thread 2 (Core 2): reads x → reads from its L1 cache → x=0 (stale!)
4. Thread 2 never sees x=1
```

### Visual Timeline

```text
Time →
T1: read x=0 (cache)  write x=1 (cache only)
T2:                        read x=0 (stale cache!)
Main Memory: x=0 (never updated!)
```

### Why CPU Caches Exist

```text
Main memory access: ~100 nanoseconds
L1 cache access:    ~1 nanosecond
100x faster — CPUs cache aggressively for performance
Problem: caching breaks visibility without coordination
```

### Cache Coherence Protocols

```text
MESI protocol (Modified, Exclusive, Shared, Invalid)
  CPUs eventually synchronize caches
  BUT timing is unpredictable without volatile/sync
  volatile forces immediate flush to main memory
```

### volatile Fixes Cache Problem

```java
volatile int x = 0;

// Thread 1
x = 1; // write flushed to main memory immediately
       // other cores' cache lines invalidated

// Thread 2
int val = x; // reads from main memory — gets 1
```

### long/double Without volatile

```text
64-bit values on 32-bit JVM:
  Written in two 32-bit operations
  Thread may read half-updated value (torn read)
  volatile long/double → atomic 64-bit read/write
```

**Interview Point:**

> Each CPU core caches variables locally. Updates may not reach main memory → other threads see stale values. `volatile` bypasses cache — forces main memory read/write.

</details>

---

## Intermediate

---

# 5. Does volatile guarantee thread safety?

<details>
<summary>Show Answer</summary>

**Answer:**

**No.** `volatile` only guarantees **visibility**—it does **not** provide full thread safety for compound operations or multiple variable updates.

### What Thread Safety Requires

```text
Thread Safety = Visibility + Atomicity + Ordering

volatile provides:  Visibility + some Ordering
volatile missing:   Atomicity for compound ops
```

### volatile Is NOT Thread-Safe for count++

```java
volatile int count = 0;

// Two threads both increment — lost updates!
new Thread(() -> { for (int i = 0; i < 1000; i++) count++; }).start();
new Thread(() -> { for (int i = 0; i < 1000; i++) count++; }).start();

// Expected: 2000
// Actual:    < 2000 (lost updates) — NOT thread-safe!
```

### When volatile IS Sufficient

```java
// ✅ Single writer, flag pattern
volatile boolean initialized = false;

void init() {
    setupResources();
    initialized = true; // single write — safe
}

void run() {
    while (!initialized) { } // single read — safe
    doWork();
}

// ✅ Status flag — one variable, simple assignment
volatile int status = IDLE;
status = RUNNING; // assignment is atomic for int
```

### Thread Safety Options

| Scenario | Solution |
|----------|----------|
| Visibility only (flag) | `volatile` |
| Atomic increment | `AtomicInteger` |
| Multiple variables together | `synchronized` |
| Complex logic | `synchronized` or `Lock` |

```java
// Thread-safe counter
AtomicInteger count = new AtomicInteger(0);
count.incrementAndGet(); // atomic + visible

// Thread-safe block
synchronized (lock) {
    count++;
    total += count;
}
```

**Interview Point:**

> volatile does **NOT** guarantee thread safety for compound ops. Safe for single-variable visibility (flags). Use `Atomic*` or `synchronized` for counters and multi-step updates.

</details>

---

# 6. Does volatile guarantee atomicity?

<details>
<summary>Show Answer</summary>

**Answer:**

**No** for compound operations. `volatile` only guarantees atomicity for **simple read and write** of the variable itself—not for operations like `i++`, `i = i + 1`, or read-modify-write.

### count++ Is NOT Atomic

```java
volatile int count = 0;

count++; // NOT atomic even with volatile!

// Internally three steps:
// 1. READ count
// 2. ADD 1
// 3. WRITE count
// Another thread can interleave between any step
```

### Interleaving Example

```text
count = 5

Thread A: READ count=5
Thread B: READ count=5
Thread A: ADD 1 → 6
Thread B: ADD 1 → 6
Thread A: WRITE count=6
Thread B: WRITE count=6

Result: 6 (expected 7) — lost update
```

### What IS Atomic with volatile

```java
volatile int x;
volatile long y;
volatile boolean flag;
volatile Object ref;

// These assignments ARE atomic + visible:
x = 10;           // ✅ atomic write
flag = true;      // ✅ atomic write
ref = new Object(); // ✅ atomic reference write
int val = x;      // ✅ atomic read
```

### long/double Special Case

```java
// Without volatile on 32-bit JVM:
long timestamp; // torn read possible (two 32-bit ops)

// With volatile:
volatile long timestamp; // ✅ atomic 64-bit read/write guaranteed
```

### Fix for Atomicity

```java
// AtomicInteger — atomic increment
AtomicInteger count = new AtomicInteger(0);
count.incrementAndGet(); // atomic read-modify-write

// synchronized — atomic block
synchronized (lock) {
    count++;
}

// AtomicLong, AtomicBoolean, etc.
```

**Interview Point:**

> volatile atomic only for **single read/write**. `count++` is **NOT atomic** with volatile. Use `AtomicInteger` or `synchronized` for compound operations.

</details>

---

# 7. Volatile vs synchronized?

<details>
<summary>Show Answer</summary>

**Answer:**

`volatile` provides **visibility only** with no locking. `synchronized` provides **visibility + atomicity + mutual exclusion** through monitor locks.

### Comparison Table

| | `volatile` | `synchronized` |
|---|------------|----------------|
| Visibility | ✅ Yes | ✅ Yes |
| Atomicity | ❌ Only single read/write | ✅ Full block |
| Mutual exclusion | ❌ No | ✅ Yes |
| Blocking | ❌ Never blocks | ✅ Can block threads |
| Performance | Faster — no lock | Slower — lock overhead |
| Null check | N/A | Can use for compound ops |
| Use for | Flags, status | Critical sections |

### volatile Example

```java
private volatile boolean shutdown = false;

public void shutdown() {
    shutdown = true; // visible to all threads
}

// Worker
while (!shutdown) { doWork(); }
```

### synchronized Example

```java
private int count = 0;

public synchronized void increment() {
    count++; // atomic + visible + exclusive
}

public synchronized int getCount() {
    return count;
}
```

### When to Use Each

```java
// ✅ volatile — single flag, one writer
volatile boolean ready = false;

// ✅ synchronized — compound operation
synchronized (lock) {
    balance -= amount;
    logTransaction(amount);
}

// ✅ AtomicInteger — atomic counter without lock
AtomicInteger counter = new AtomicInteger(0);
counter.incrementAndGet();
```

### Performance

```text
volatile:  no lock acquisition → faster
synchronized: lock acquire/release → overhead
  But necessary when atomicity required
```

### Cannot Replace synchronized with volatile

```java
// ❌ volatile cannot do this safely
volatile int a, b;
a++;  // not atomic
b = a + 1;  // not atomic across two variables

// ✅ synchronized protects both
synchronized (lock) {
    a++;
    b = a + 1;
}
```

**Interview Point:**

> volatile = visibility only, no lock. synchronized = visibility + atomicity + mutex. Use volatile for flags; synchronized/Atomic for compound ops.

</details>

---

## Advanced

---

# 8. Java Memory Model and volatile?

<details>
<summary>Show Answer</summary>

**Answer:**

The **Java Memory Model (JMM)** defines how threads interact through memory. `volatile` establishes formal **happens-before** rules that guarantee visibility and ordering across threads.

### JMM Core Concepts

```text
Main Memory:  shared heap — all threads
Working Memory: thread-local cache (CPU registers/cache)

Without JMM rules:
  Compiler and CPU can reorder instructions freely
  Writes may stay in working memory
  Reads may get stale values
```

### volatile in JMM

```text
volatile write → flushes ALL prior writes to main memory
volatile read  → discards working memory, reads from main memory

volatile write happens-before volatile read of same variable
```

### Write Visibility Chain

```java
int a = 1;
volatile boolean flag = false;

// Thread 1
a = 2;
flag = true;  // volatile write — flushes a=2 to main memory

// Thread 2
if (flag) {   // volatile read — sees flag=true AND a=2
    System.out.println(a); // guaranteed to print 2
}
```

### Reordering Prevention

```java
// Without volatile — compiler may reorder:
flag = true;
a = 2;  // might execute BEFORE flag=true!

// With volatile flag:
a = 2;
flag = true; // volatile write — a=2 guaranteed visible before flag=true
```

### JMM Guarantees for volatile

| Guarantee | Detail |
|-------------|--------|
| **Visibility** | Write visible to subsequent reads |
| **Ordering** | No reorder across volatile access |
| **64-bit atomicity** | long/double read/write atomic |
| **Happens-before** | volatile write → volatile read |

### Non-volatile — No Guarantee

```java
int a = 1;
boolean flag = false; // NOT volatile

// Thread 1: a=2; flag=true;
// Thread 2: if(flag) print(a);
// May print 0, 1, or 2 — unpredictable!
```

**Interview Point:**

> JMM defines cross-thread visibility rules. volatile establishes happens-before — writes before volatile write are visible after volatile read. Foundation of Java concurrency correctness.

</details>

---

# 9. Happens-before relationship?

<details>
<summary>Show Answer</summary>

**Answer:**

**Happens-before** is a JMM ordering guarantee—if action A happens-before B, then A's memory changes are **visible** to B. It defines what reordering is forbidden.

### Definition

```text
If A happens-before B:
  All memory writes by A are visible to B
  B sees A's changes as if A completed before B started
```

### Happens-Before Rules in Java

| Rule | Example |
|------|---------|
| **Program order** | Statement 1 happens-before Statement 2 in same thread |
| **Monitor lock** | `unlock` happens-before subsequent `lock` |
| **volatile** | volatile write happens-before volatile read |
| **Thread start** | `thread.start()` happens-before thread's actions |
| **Thread join** | Thread's actions happen-before `thread.join()` returns |
| **Transitivity** | A hb B, B hb C → A hb C |

### volatile Happens-Before

```java
int x = 0;
volatile boolean ready = false;

// Thread 1
x = 42;           // (1) write x
ready = true;     // (2) volatile write — hb rule triggered

// Thread 2
if (ready) {      // (3) volatile read — sees ready=true
    print(x);     // (4) guaranteed to see x=42
}
// (2) happens-before (3) → (1) visible at (4)
```

### synchronized Happens-Before

```java
synchronized (lock) {
    x = 10;       // write inside lock
}                 // unlock — happens-before next lock

synchronized (lock) {
    print(x);     // guaranteed to see x=10
}
```

### Without Happens-Before — Undefined

```java
// No volatile, no sync — NO happens-before relationship
// Compiler/CPU free to reorder
// Thread 2 may see x=0 even after Thread 1 set x=42
```

### Transitivity

```text
A happens-before B
B happens-before C
→ A happens-before C (transitive)

Used to chain visibility across multiple threads
```

**Interview Point:**

> Happens-before = visibility guarantee in JMM. volatile write hb volatile read. unlock hb lock. If no hb relationship → no visibility guarantee. Key to understanding Java concurrency.

</details>

---

# 10. Double Checked Locking with volatile?

<details>
<summary>Show Answer</summary>

**Answer:**

**Double Checked Locking (DCL)** creates a singleton lazily with minimal locking. The `instance` variable **must be `volatile`** to prevent partially constructed objects being seen by other threads due to instruction reordering.

### Broken DCL (Pre-Java 5)

```java
class Singleton {
    private static Singleton instance; // NO volatile — BROKEN!

    public static Singleton getInstance() {
        if (instance == null) {              // first check (no lock)
            synchronized (Singleton.class) {
                if (instance == null) {      // second check (with lock)
                    instance = new Singleton(); // may publish half-built object!
                }
            }
        }
        return instance;
    }
}
```

### Why Broken — Instruction Reordering

```text
instance = new Singleton() is NOT one step:

1. Allocate memory for object
2. Initialize object (constructor)
3. instance = memory address (publish reference)

CPU may reorder to: 1 → 3 → 2

Thread B sees instance != null (step 3 done)
But object not yet initialized (step 2 pending)
→ Thread B uses partially constructed Singleton!
```

### Fixed DCL with volatile (Java 5+)

```java
class Singleton {
    private static volatile Singleton instance; // volatile fixes reordering

    public static Singleton getInstance() {
        if (instance == null) {                    // first check — fast path
            synchronized (Singleton.class) {
                if (instance == null) {            // second check — safe
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}
```

### Why volatile Fixes It

```text
volatile write to instance:
  Prevents reordering of initialization steps
  Other threads cannot see reference until object fully constructed
  happens-before: volatile write → volatile read
```

### Better Alternatives Today

```java
// ✅ Best — initialization-on-demand holder (no sync needed)
class Singleton {
    private Singleton() {}
    private static class Holder {
        static final Singleton INSTANCE = new Singleton();
    }
    public static Singleton getInstance() {
        return Holder.INSTANCE;
    }
}

// ✅ Also good — enum singleton
enum Singleton {
    INSTANCE;
}

// ✅ Simple — static final (eager)
class Singleton {
    private static final Singleton INSTANCE = new Singleton();
    public static Singleton getInstance() { return INSTANCE; }
}
```

### DCL Flow

```text
getInstance() called
  ↓
instance != null? → YES → return (no lock — fast)
  ↓ NO
acquire lock
  ↓
instance != null? → YES → return (another thread created it)
  ↓ NO
create instance (volatile ensures safe publish)
  ↓
release lock, return instance
```

**Interview Point:**

> DCL singleton **requires volatile** on instance field — prevents publishing partially constructed object due to reordering. Modern preference: holder class pattern or enum singleton.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: volatile on long without volatile?

<details>
<summary>Show Answer</summary>

**Answer:**

On **32-bit JVM**, reading/writing `long`/`double` without `volatile` can cause **torn reads** — thread reads half-updated 64-bit value. `volatile long` guarantees atomic 64-bit read/write. On 64-bit JVM, plain long reads are typically atomic but visibility still not guaranteed without volatile.

</details>

---

### Q: Can volatile prevent instruction reordering?

<details>
<summary>Show Answer</summary>

**Answer:**

**Partially.** volatile prevents reordering **across** volatile reads/writes. Instructions before volatile write cannot move after it. Instructions after volatile read cannot move before it. Does not prevent reordering of non-volatile accesses relative to each other.

</details>

---

### Q: volatile vs AtomicInteger?

<details>
<summary>Show Answer</summary>

**Answer:**

| | `volatile int` | `AtomicInteger` |
|---|----------------|-----------------|
| Visibility | ✅ | ✅ |
| Atomic increment | ❌ | ✅ |
| CAS operations | ❌ | ✅ |
| Use for | Flags, status | Counters, atomic updates |

```java
volatile boolean flag;           // visibility only
AtomicInteger count = new AtomicInteger(); // visibility + atomicity
```

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> volatile = **visibility** (main memory read/write), not full thread safety. No atomicity for `count++`. vs synchronized: no lock, no mutex. JMM happens-before via volatile write→read. DCL singleton **needs volatile** on instance field.

</details>
