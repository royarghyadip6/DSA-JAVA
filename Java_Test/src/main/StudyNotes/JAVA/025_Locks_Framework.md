# 25. Locks Framework

## 25. Locks Framework

## ReentrantLock

---

# 1. Difference between synchronized and ReentrantLock?

<details>
<summary>Show Answer</summary>

**Answer:**

Both provide mutual exclusion, but `ReentrantLock` is an **explicit lock** with more features—`tryLock()`, fairness, interruptible locking, and multiple `Condition` objects.

### Comparison Table

| Feature | `synchronized` | `ReentrantLock` |
|---------|----------------|-----------------|
| Type | Keyword — implicit | Class — explicit lock/unlock |
| Lock release | Automatic on block exit | Manual `unlock()` in `finally` |
| `tryLock()` | ❌ No | ✅ Yes — with timeout |
| Fairness | ❌ Non-fair only | ✅ Fair or non-fair option |
| Interruptible | ❌ Cannot interrupt while waiting | ✅ `lockInterruptibly()` |
| Multiple conditions | ❌ One wait set per object | ✅ Multiple `Condition` objects |
| Performance | Good — JVM optimized | Similar — slightly more overhead |
| Deadlock detection | ❌ | ❌ (but tryLock helps avoid) |

### synchronized — Simple

```java
public synchronized void transfer(int amount) {
    balance -= amount; // auto lock/unlock
}
```

### ReentrantLock — Explicit

```java
private final ReentrantLock lock = new ReentrantLock();

public void transfer(int amount) {
    lock.lock();
    try {
        balance -= amount;
    } finally {
        lock.unlock(); // MUST unlock in finally
    }
}
```

### ReentrantLock Advantages

```java
// tryLock — don't block forever
if (lock.tryLock(100, TimeUnit.MILLISECONDS)) {
    try { doWork(); }
    finally { lock.unlock(); }
} else {
    handleTimeout();
}

// Fair lock — prevent starvation
ReentrantLock fairLock = new ReentrantLock(true);

// Multiple conditions
Condition notEmpty = lock.newCondition();
Condition notFull  = lock.newCondition();
```

### When to Use synchronized

```text
✅ Simple critical sections
✅ Single lock, no special features needed
✅ Less code — no try/finally unlock
✅ JVM optimizes synchronized well (biased locking, etc.)
```

**Interview Point:**

> synchronized = simple, automatic. ReentrantLock = explicit, tryLock, fairness, interruptible, multiple Conditions. Use ReentrantLock when you need those features.

</details>

---

# 2. Why ReentrantLock?

<details>
<summary>Show Answer</summary>

**Answer:**

`ReentrantLock` exists because `synchronized` lacks **tryLock, fairness, interruptible waiting, and multiple condition variables**—needed for advanced concurrency patterns.

### Problems synchronized Cannot Solve

| Need | synchronized | ReentrantLock |
|------|--------------|---------------|
| Try lock with timeout | ❌ Blocks forever | ✅ `tryLock(timeout)` |
| Fair ordering | ❌ Non-fair only | ✅ `new ReentrantLock(true)` |
| Interrupt while waiting | ❌ | ✅ `lockInterruptibly()` |
| Separate wait queues | ❌ One wait set | ✅ Multiple Conditions |
| Lock status query | ❌ | ✅ `isLocked()`, `getHoldCount()` |

### tryLock — Avoid Deadlock

```java
ReentrantLock lockA = new ReentrantLock();
ReentrantLock lockB = new ReentrantLock();

// tryLock prevents deadlock — release and retry
while (true) {
    if (lockA.tryLock()) {
        try {
            if (lockB.tryLock()) {
                try { transfer(); return; }
                finally { lockB.unlock(); }
            }
        } finally { lockA.unlock(); }
    }
    Thread.sleep(50); // backoff
}
```

### Fair Lock — Prevent Starvation

```java
// Long-waiting threads get lock first
ReentrantLock fairLock = new ReentrantLock(true);
fairLock.lock(); // FIFO queue for lock acquisition
```

### Multiple Conditions — Producer-Consumer

```java
ReentrantLock lock = new ReentrantLock();
Condition notEmpty = lock.newCondition(); // consumers wait here
Condition notFull  = lock.newCondition(); // producers wait here

// Producer wakes only consumers — not other producers
notFull.await();
queue.add(item);
notEmpty.signal(); // precise notification
```

### Interruptible Lock

```java
try {
    lock.lockInterruptibly(); // can be interrupted while waiting
    doWork();
} catch (InterruptedException e) {
    // respond to shutdown signal
} finally {
    if (lock.isHeldByCurrentThread()) lock.unlock();
}
```

**Interview Point:**

> ReentrantLock when you need: tryLock (deadlock avoidance), fair ordering, interruptible wait, multiple Conditions, or lock status inspection.

</details>

---

# 3. Fair lock?

<details>
<summary>Show Answer</summary>

**Answer:**

A **fair lock** grants access in **FIFO order**—the thread waiting the longest gets the lock first, preventing starvation of long-waiting threads.

### Creating Fair Lock

```java
ReentrantLock fairLock = new ReentrantLock(true); // fair=true
ReentrantLock unfairLock = new ReentrantLock();  // default: false
ReentrantLock unfairLock2 = new ReentrantLock(false);
```

### How Fair Lock Works

```text
Non-fair lock:
  New thread may acquire lock even if others waiting
  → faster but possible starvation

Fair lock:
  Threads queued in arrival order
  Longest-waiting thread gets lock next
  → slower but no starvation
```

### Example

```java
ReentrantLock lock = new ReentrantLock(true);

// Thread 1 waits 10 seconds for lock
// Thread 2 arrives later
// Fair lock: Thread 1 gets lock first (FIFO)
// Non-fair: Thread 2 might get lock first (barging)
```

### Fair vs Non-Fair

| | Fair Lock | Non-Fair Lock |
|---|-----------|---------------|
| Ordering | FIFO queue | Barging allowed |
| Starvation | ❌ Prevented | ⚠️ Possible |
| Performance | Slower — queue overhead | Faster — less overhead |
| Default | Must specify `true` | Default for ReentrantLock |
| Use when | Starvation is a problem | Performance matters |

### When to Use Fair Lock

```text
✅ Long-running tasks competing for same lock
✅ Starvation observed in production
✅ All threads must get fair CPU/resource access

❌ High-throughput systems — fair lock adds overhead
❌ Short critical sections — non-fair usually fine
```

### synchronized Is Always Non-Fair

```text
synchronized keyword = always non-fair
No option to make synchronized fair
Use ReentrantLock(true) if fairness needed
```

**Interview Point:**

> Fair lock = FIFO ordering, prevents starvation. `new ReentrantLock(true)`. Slower than non-fair. synchronized is always non-fair.

</details>

---

# 4. Non-fair lock?

<details>
<summary>Show Answer</summary>

**Answer:**

A **non-fair lock** (default) allows **barging**—a newly arriving thread can acquire the lock even if other threads are waiting, prioritizing throughput over fairness.

### Default Behavior

```java
ReentrantLock lock = new ReentrantLock();        // non-fair (default)
ReentrantLock lock2 = new ReentrantLock(false); // explicitly non-fair
```

### Barging Explained

```text
Thread A: holds lock, releases
Thread B: waiting in queue for 5 seconds
Thread C: just arrived, tries lock

Non-fair: Thread C may get lock BEFORE Thread B (barging)
Fair:     Thread B gets lock first (FIFO)
```

### Why Non-Fair Is Default

```text
1. Higher throughput — less queue management
2. Newly arrived thread may already be running (CPU cache warm)
3. Waiting thread needs context switch to wake up
4. Most applications don't need strict fairness
5. synchronized is also non-fair — proven performance
```

### Starvation Risk

```java
// Under heavy load with non-fair lock:
// High-priority or lucky threads may repeatedly win lock
// Low-priority thread may wait very long — starvation

ReentrantLock fairLock = new ReentrantLock(true); // fix starvation
```

### Performance Comparison

```text
Non-fair ReentrantLock:  fastest — default choice
Fair ReentrantLock:     ~10-100x slower under contention
synchronized:           similar to non-fair — JVM optimized
```

### When Non-Fair Is Fine

```text
✅ Short critical sections
✅ Low contention
✅ Throughput more important than fairness
✅ Most production scenarios
```

**Interview Point:**

> Non-fair lock = default, allows barging, faster throughput. Risk: starvation under heavy contention. synchronized is always non-fair.

</details>

---

## Advanced

---

# 5. tryLock()?

<details>
<summary>Show Answer</summary>

**Answer:**

`tryLock()` attempts to acquire the lock **without blocking**—returns `true` if acquired, `false` if not. The timed version waits up to a specified duration.

### Method Variants

```java
ReentrantLock lock = new ReentrantLock();

// Immediate — no waiting
boolean acquired = lock.tryLock();

// With timeout
boolean acquired = lock.tryLock(100, TimeUnit.MILLISECONDS);

// Blocking — waits indefinitely
lock.lock();
```

### Non-Blocking Try

```java
if (lock.tryLock()) {
    try {
        doWork();
    } finally {
        lock.unlock();
    }
} else {
    System.out.println("Lock busy — skip or retry later");
}
```

### Timed Try — Avoid Infinite Wait

```java
try {
    if (lock.tryLock(5, TimeUnit.SECONDS)) {
        try {
            processOrder();
        } finally {
            lock.unlock();
        }
    } else {
        throw new TimeoutException("Could not acquire lock in 5s");
    }
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}
```

### Deadlock Prevention with tryLock

```java
ReentrantLock lockA = new ReentrantLock();
ReentrantLock lockB = new ReentrantLock();

while (true) {
    if (lockA.tryLock()) {
        try {
            if (lockB.tryLock()) {
                try {
                    transfer();
                    return; // success
                } finally { lockB.unlock(); }
            }
        } finally { lockA.unlock(); }
    }
    Thread.sleep(50); // release and retry — breaks hold-and-wait
}
```

### tryLock vs synchronized

```text
synchronized:  blocks until lock available — no timeout option
tryLock():   returns immediately or times out — responsive
```

### Always Check Return Value

```java
// ❌ Wrong — assumes lock acquired
lock.tryLock();
doWork(); // may run without lock!
lock.unlock();

// ✅ Correct
if (lock.tryLock()) {
    try { doWork(); }
    finally { lock.unlock(); }
}
```

**Interview Point:**

> `tryLock()` = non-blocking or timed lock attempt. Key for deadlock prevention (release + retry). synchronized has no equivalent.

</details>

---

# 6. lockInterruptibly()?

<details>
<summary>Show Answer</summary>

**Answer:**

`lockInterruptibly()` acquires the lock like `lock()`, but if the thread is **interrupted while waiting**, it throws `InterruptedException` instead of waiting forever.

### Usage

```java
ReentrantLock lock = new ReentrantLock();

try {
    lock.lockInterruptibly(); // can be interrupted while waiting
    doWork();
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    System.out.println("Lock acquisition interrupted — shutting down");
} finally {
    if (lock.isHeldByCurrentThread()) {
        lock.unlock();
    }
}
```

### lock() vs lockInterruptibly()

| | `lock()` | `lockInterruptibly()` |
|---|----------|----------------------|
| Blocks while waiting | ✅ Yes | ✅ Yes |
| Interrupt while waiting | ❌ Ignored — keeps waiting | ✅ Throws InterruptedException |
| Use for | Normal locking | Responsive shutdown |

### Shutdown Scenario

```java
class Worker implements Runnable {
    private final ReentrantLock lock = new ReentrantLock();
    private volatile boolean running = true;

    public void run() {
        while (running) {
            try {
                lock.lockInterruptibly();
                try {
                    processTask();
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                running = false; // respond to shutdown
                Thread.currentThread().interrupt();
            }
        }
    }

    public void shutdown() {
        running = false;
        thread.interrupt(); // breaks lockInterruptibly wait
    }
}
```

### synchronized Cannot Be Interrupted

```java
synchronized (lock) {
    // if blocked waiting for lock:
    // interrupt() does NOT release the wait
    // thread stays BLOCKED until lock available
}

lock.lockInterruptibly();
// interrupt while waiting → InterruptedException immediately
```

### Safe Unlock Pattern

```java
lock.lockInterruptibly();
try {
    doWork();
} finally {
    if (lock.isHeldByCurrentThread()) { // check before unlock
        lock.unlock();
    }
}
```

**Interview Point:**

> `lockInterruptibly()` = interruptible lock wait. Essential for graceful shutdown. synchronized cannot be interrupted while blocked on lock acquisition.

</details>

---

# 7. Condition object?

<details>
<summary>Show Answer</summary>

**Answer:**

A **Condition** is a `ReentrantLock` feature that replaces `wait()/notify()`—allowing threads to wait for a specific state with `await()` and be awakened with `signal()`.

### Creating Condition

```java
ReentrantLock lock = new ReentrantLock();
Condition condition = lock.newCondition();

// Equivalent to wait/notify but for this specific condition
```

### Basic Pattern

```java
lock.lock();
try {
    while (!conditionMet()) {
        condition.await();    // release lock, wait for signal
    }
    doWork();
} finally {
    lock.unlock();
}

// Another thread:
lock.lock();
try {
    makeConditionTrue();
    condition.signal();       // wake one waiting thread
} finally {
    lock.unlock();
}
```

### Condition Methods

| Method | Equivalent | Description |
|--------|------------|-------------|
| `await()` | `wait()` | Release lock, wait indefinitely |
| `await(long, TimeUnit)` | `wait(timeout)` | Timed wait |
| `signal()` | `notify()` | Wake one thread |
| `signalAll()` | `notifyAll()` | Wake all threads |

### vs Object wait/notify

```java
// Object wait/notify — one wait set per object
synchronized (lock) {
    lock.wait();
    lock.notify();
}

// Condition — named, specific wait queue
Condition notEmpty = lock.newCondition();
lock.lock();
try {
    notEmpty.await();
    notEmpty.signal();
} finally { lock.unlock(); }
```

### Must Hold Lock

```java
// ❌ IllegalMonitorStateException
condition.await(); // must hold lock first!

// ✅ Correct
lock.lock();
try {
    condition.await();
} finally {
    lock.unlock();
}
```

### Always Use while Loop

```java
lock.lock();
try {
    while (queue.isEmpty()) {
        notEmpty.await(); // while loop — handles spurious wakeup
    }
    return queue.remove();
} finally {
    lock.unlock();
}
```

**Interview Point:**

> `Condition` = ReentrantLock's wait/notify. `await()`/`signal()`. Must hold lock. Use while loop with await(). More flexible than Object wait/notify.

</details>

---

# 8. Multiple conditions?

<details>
<summary>Show Answer</summary>

**Answer:**

`ReentrantLock` supports **multiple `Condition` objects** on the same lock—each with its own wait queue, enabling precise signaling (wake only relevant threads).

### Problem with Single wait/notify

```java
// One wait set — producers and consumers share it
synchronized (queue) {
    queue.wait();  // both producers and consumers wait here
    queue.notify(); // may wake wrong thread type!
}
```

### Multiple Conditions Solution

```java
class BoundedBuffer {
    private final Queue<Item> queue = new LinkedList<>();
    private final int capacity = 10;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull  = lock.newCondition(); // producers wait
    private final Condition notEmpty = lock.newCondition(); // consumers wait

    public void produce(Item item) throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() == capacity) {
                notFull.await(); // producers wait on notFull
            }
            queue.add(item);
            notEmpty.signal(); // wake ONE consumer only
        } finally {
            lock.unlock();
        }
    }

    public Item consume() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                notEmpty.await(); // consumers wait on notEmpty
            }
            Item item = queue.remove();
            notFull.signal(); // wake ONE producer only
            return item;
        } finally {
            lock.unlock();
        }
    }
}
```

### Why Multiple Conditions Help

```text
Single notify:  may wake producer when queue full (wrong thread)
Multiple Conditions:
  notFull.signal()  → wakes only producers waiting for space
  notEmpty.signal() → wakes only consumers waiting for items
  → more efficient, no wasted wakeups
```

### Object vs ReentrantLock Conditions

| | Object wait/notify | Multiple Conditions |
|---|-------------------|---------------------|
| Wait queues | 1 per object | Multiple per lock |
| Signaling | May wake wrong threads | Precise targeting |
| Available with | synchronized only | ReentrantLock only |

### Three-Condition Example

```java
ReentrantLock lock = new ReentrantLock();
Condition idle    = lock.newCondition(); // workers waiting for task
Condition running = lock.newCondition(); // tasks being processed
Condition done    = lock.newCondition(); // waiting for completion

// Signal only the relevant group
idle.signal();    // wake idle workers
done.signalAll(); // all waiting for completion
```

**Interview Point:**

> Multiple Conditions = separate wait queues per state. `notFull`/`notEmpty` pattern. Precise `signal()` — no waking wrong threads. Key ReentrantLock advantage over synchronized.

</details>

---

## ReadWriteLock

---

# 9. What is ReadWriteLock?

<details>
<summary>Show Answer</summary>

**Answer:**

`ReadWriteLock` maintains **two locks**—a **read lock** (shared) and a **write lock** (exclusive)—allowing multiple concurrent readers OR one writer, optimizing read-heavy workloads.

### Interface

```java
ReadWriteLock rwLock = new ReentrantReadWriteLock();
Lock readLock  = rwLock.readLock();
Lock writeLock = rwLock.writeLock();
```

### Lock Rules

```text
Multiple threads:  can hold READ lock simultaneously
Only ONE thread:   can hold WRITE lock
READ + WRITE:      mutually exclusive
READ + READ:       allowed concurrently
```

### Basic Usage

```java
class Cache {
    private final Map<String, String> cache = new HashMap<>();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    public String get(String key) {
        rwLock.readLock().lock();
        try {
            return cache.get(key); // multiple readers OK
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public void put(String key, String value) {
        rwLock.writeLock().lock();
        try {
            cache.put(key, value); // exclusive write
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}
```

### When to Use

```text
✅ Read-heavy data (caches, config, registries)
✅ Many readers, few writers
✅ Read operations dominate write operations

❌ Write-heavy workloads — no benefit
❌ Simple counters — use AtomicInteger
```

### vs synchronized

```text
synchronized:  one thread at a time (read OR write)
ReadWriteLock: multiple readers OR one writer
  → better throughput when reads >> writes
```

**Interview Point:**

> ReadWriteLock = shared read lock + exclusive write lock. Multiple concurrent readers. Use for read-heavy caches and config data.

</details>

---

# 10. Read lock?

<details>
<summary>Show Answer</summary>

**Answer:**

The **read lock** is a **shared lock**—multiple threads can hold it simultaneously, but it blocks if any thread holds the write lock.

### Acquiring Read Lock

```java
ReadWriteLock rwLock = new ReentrantReadWriteLock();
Lock readLock = rwLock.readLock();

readLock.lock();
try {
    String value = cache.get(key); // shared read access
} finally {
    readLock.unlock();
}
```

### Read Lock Rules

```text
✅ Multiple threads can hold read lock together
❌ Cannot acquire read lock while write lock held
❌ Cannot acquire write lock while any read lock held
✅ Read lock compatible with other read locks
```

### Concurrent Readers

```java
// 10 threads reading simultaneously — all allowed
for (int i = 0; i < 10; i++) {
    new Thread(() -> {
        readLock.lock();
        try {
            System.out.println(cache.get("key")); // parallel reads
        } finally {
            readLock.unlock();
        }
    }).start();
}
```

### Read Lock Blocks Write

```text
Thread A: acquires read lock
Thread B: tries write lock → BLOCKED (read lock held)
Thread C: acquires read lock → OK (shared with A)
Thread A releases read lock
Thread C releases read lock
Thread B: acquires write lock → OK
```

### Read Lock Is Reentrant

```java
readLock.lock();
try {
    readLock.lock(); // same thread re-enters — OK
    doWork();
    readLock.unlock();
} finally {
    readLock.unlock();
}
```

### Downgrade — Write to Read

```java
writeLock.lock();
try {
    updateCache();
    readLock.lock();   // acquire read while holding write
    writeLock.unlock(); // release write — now holding read only
    // other readers can now enter
    return readData();
} finally {
    readLock.unlock();
}
```

**Interview Point:**

> Read lock = shared, multiple readers allowed. Blocks writers. Use for read-only access to shared data. Supports lock downgrade (write → read).

</details>

---

# 11. Write lock?

<details>
<summary>Show Answer</summary>

**Answer:**

The **write lock** is an **exclusive lock**—only one thread can hold it, and it blocks all readers and other writers.

### Acquiring Write Lock

```java
ReadWriteLock rwLock = new ReentrantReadWriteLock();
Lock writeLock = rwLock.writeLock();

writeLock.lock();
try {
    cache.put(key, value); // exclusive — no readers or writers
} finally {
    writeLock.unlock();
}
```

### Write Lock Rules

```text
✅ Only ONE thread holds write lock at a time
❌ No read locks allowed while write lock held
❌ No other write locks while write lock held
✅ Full exclusive access — like synchronized
```

### Write Blocks Everything

```text
Thread A: acquires write lock
Thread B: tries read lock  → BLOCKED
Thread C: tries write lock   → BLOCKED
Thread A: releases write lock
Threads B, C: can now acquire their locks
```

### Write Lock Use Cases

```java
// Cache update — must be exclusive
writeLock.lock();
try {
    cache.clear();
    cache.putAll(newData);
} finally {
    writeLock.unlock();
}

// Config reload
writeLock.lock();
try {
    config = loadNewConfig();
} finally {
    writeLock.unlock();
}
```

### Write Lock vs synchronized

```text
Write lock alone = same exclusivity as synchronized
Benefit of ReadWriteLock = read lock allows concurrent reads
Write lock used only when data is being modified
```

### Upgrade Not Supported

```java
// ❌ Cannot upgrade read lock to write lock — deadlock risk!
readLock.lock();
writeLock.lock(); // DEADLOCK — waiting for own read lock to release

// ✅ Downgrade OK: write → read
writeLock.lock();
readLock.lock();
writeLock.unlock(); // downgrade
```

**Interview Point:**

> Write lock = exclusive, blocks all readers and writers. One thread at a time. Cannot upgrade read→write (deadlock). Can downgrade write→read.

</details>

---

## StampedLock

---

# 12. What is StampedLock?

<details>
<summary>Show Answer</summary>

**Answer:**

`StampedLock` (Java 8) is a **optimistic read lock** that uses a **stamp (version number)** instead of traditional locking for reads—faster than `ReadWriteLock` for read-heavy workloads.

### Basic Usage

```java
StampedLock sl = new StampedLock();

// Optimistic read — no lock acquired
long stamp = sl.tryOptimisticRead();
String value = cache.get(key);
if (!sl.validate(stamp)) { // check if modified during read
    stamp = sl.readLock(); // fallback to real read lock
    try {
        value = cache.get(key);
    } finally {
        sl.unlockRead(stamp);
    }
}

// Write lock
long writeStamp = sl.writeLock();
try {
    cache.put(key, value);
} finally {
    sl.unlockWrite(writeStamp);
}
```

### StampedLock Modes

| Mode | Description |
|------|-------------|
| **Optimistic read** | No lock — validate stamp after read |
| **Read lock** | Shared — like ReadWriteLock |
| **Write lock** | Exclusive — like ReadWriteLock |

### vs ReentrantReadWriteLock

| | `ReentrantReadWriteLock` | `StampedLock` |
|---|--------------------------|---------------|
| Optimistic read | ❌ | ✅ |
| Reentrant | ✅ | ❌ Not reentrant |
| Condition support | ✅ | ❌ No Conditions |
| Performance (reads) | Good | Better (optimistic) |
| Complexity | Lower | Higher |

### Stamp — Version Number

```text
Stamp = long value representing lock state/version
  Changes on every write
  Optimistic read: read stamp, read data, validate stamp
  If stamp unchanged → no write happened → read was valid
```

**Interview Point:**

> StampedLock = optimistic reads via stamp validation. Faster than ReadWriteLock for read-heavy. Not reentrant. No Conditions. Java 8+.

</details>

---

# 13. Optimistic locking?

<details>
<summary>Show Answer</summary>

**Answer:**

**Optimistic locking** assumes no conflict during a read—reads data without acquiring a lock, then **validates** that no write occurred. If validation fails, retries with a real lock.

### Optimistic Read Flow

```text
1. tryOptimisticRead() → get stamp (no lock acquired!)
2. Read shared data freely
3. validate(stamp) → check if data changed during read
4. If valid → read was safe, use data
5. If invalid → fallback to readLock() and re-read
```

### Code Example

```java
StampedLock lock = new StampedLock();

public String read() {
    long stamp = lock.tryOptimisticRead(); // no blocking!
    String value = data;                   // read without lock

    if (!lock.validate(stamp)) {           // someone wrote during read?
        stamp = lock.readLock();           // fallback — real lock
        try {
            value = data;                  // re-read safely
        } finally {
            lock.unlockRead(stamp);
        }
    }
    return value;
}
```

### Optimistic vs Pessimistic

| | Optimistic | Pessimistic (read lock) |
|---|------------|------------------------|
| Assumption | No write during read | Write may happen |
| Lock acquired? | ❌ No (initially) | ✅ Yes always |
| On conflict | Retry with real lock | Already protected |
| Best when | Reads >> writes | Writes frequent |

### When Optimistic Works Well

```text
Read-heavy cache: 99% reads, 1% writes
  → optimistic read almost always validates successfully
  → no lock overhead for most reads
  → significant performance gain
```

### When Optimistic Fails Often

```text
Write-heavy workload:
  validate() fails frequently
  → constant fallback to readLock
  → worse than just using readLock directly
```

### Database Optimistic Locking (Related Concept)

```java
// JPA @Version — similar idea
@Entity
class Account {
    @Version
    private int version; // stamp-like version field
    // update checks version — fails if changed
}
```

**Interview Point:**

> Optimistic locking = read without lock, validate stamp after. Fast when writes rare. Fallback to read lock on conflict. StampedLock's key feature.

</details>

---

# 14. When to use StampedLock?

<details>
<summary>Show Answer</summary>

**Answer:**

Use `StampedLock` for **read-dominated workloads** where optimistic reads provide a performance edge—caches, config stores, and statistics where writes are rare.

### Use StampedLock When

```text
✅ Read-heavy (reads >> writes, e.g., 90%+ reads)
✅ Performance critical read path
✅ Short read operations
✅ No need for reentrant locking
✅ No need for Condition variables
```

### Don't Use StampedLock When

```text
❌ Write-heavy workloads — optimistic reads fail often
❌ Need reentrant locks — StampedLock is NOT reentrant
❌ Need Condition/wait/notify — not supported
❌ Simple synchronization — synchronized is simpler
❌ Team unfamiliar with stamp validation pattern
```

### Ideal Use Case — Cache

```java
class ConfigCache {
    private volatile Config config = loadConfig();
    private final StampedLock lock = new StampedLock();

    public Config getConfig() {
        long stamp = lock.tryOptimisticRead();
        Config c = config;
        if (!lock.validate(stamp)) {
            stamp = lock.readLock();
            try { c = config; }
            finally { lock.unlockRead(stamp); }
        }
        return c;
    }

    public void refresh(Config newConfig) {
        long stamp = lock.writeLock();
        try { config = newConfig; }
        finally { lock.unlockWrite(stamp); }
    }
}
```

### Lock Selection Guide

| Scenario | Best Choice |
|----------|-------------|
| Simple critical section | `synchronized` |
| tryLock, fairness, Conditions | `ReentrantLock` |
| Read-heavy, moderate writes | `ReentrantReadWriteLock` |
| Read-heavy, rare writes, perf critical | `StampedLock` |
| Single variable counter | `AtomicInteger` / `LongAdder` |

### StampedLock Caveats

```text
⚠️ Not reentrant — don't call locked method while holding lock
⚠️ validate() must be called immediately after optimistic read
⚠️ More complex code — harder to maintain
⚠️ No lock upgrade (read → write) — use writeLock() directly
```

**Interview Point:**

> StampedLock for read-heavy, performance-critical paths. Optimistic reads when writes rare. Not reentrant, no Conditions. Know when NOT to use it.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: ReentrantLock without unlock()?

<details>
<summary>Show Answer</summary>

**Answer:**

Lock never released — other threads **blocked forever**. Always `unlock()` in `finally` block. Unlike synchronized, no auto-release on exception if you forget unlock.

```java
lock.lock();
try { doWork(); }
finally { lock.unlock(); } // mandatory
```

</details>

---

### Q: ReadWriteLock vs synchronized?

<details>
<summary>Show Answer</summary>

**Answer:**

synchronized: one thread at a time for any access. ReadWriteLock: **multiple readers OR one writer**. ReadWriteLock wins when reads dominate. synchronized simpler for balanced read/write.

</details>

---

### Q: Can StampedLock upgrade read to write?

<details>
<summary>Show Answer</summary>

**Answer:**

**No** — upgrading read lock to write lock causes **deadlock** (write waits for read to release, but current thread holds read). Use `writeLock()` directly, or downgrade write→read is supported.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> ReentrantLock: tryLock, fair/non-fair, lockInterruptibly, multiple Conditions. ReadWriteLock: shared read + exclusive write. StampedLock: optimistic reads via stamp — fastest for read-heavy, not reentrant.

</details>
