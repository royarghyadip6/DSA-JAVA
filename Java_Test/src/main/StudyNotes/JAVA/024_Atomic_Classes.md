# 24. Atomic Classes

## 24. Atomic Classes

## Frequently Asked

---

# 1. Why Atomic classes introduced?

<details>
<summary>Show Answer</summary>

**Answer:**

**Atomic classes** were introduced in Java 5 to provide **thread-safe atomic operations** on single variables **without using synchronized locks**—using hardware-level CAS (Compare-And-Swap) for better performance under low contention.

### Problems Before Atomic Classes

```java
// synchronized — works but heavy for simple counter
private int count = 0;

public synchronized void increment() {
    count++; // lock acquire + release overhead
}

// volatile — NOT enough
private volatile int count = 0;
count++; // NOT atomic — lost updates still possible
```

### What Atomic Classes Solve

| Problem | Atomic Solution |
|---------|-----------------|
| `count++` not atomic | `AtomicInteger.incrementAndGet()` |
| Lock overhead on counters | CAS — no lock blocking |
| Visibility + atomicity together | Built-in volatile semantics |
| Complex compare-and-update | `compareAndSet()` |

### Java 5 Atomic Package

```text
java.util.concurrent.atomic:
  AtomicInteger
  AtomicLong
  AtomicBoolean
  AtomicReference
  AtomicIntegerArray
  LongAdder / LongAccumulator (Java 8)
```

### Example — Before vs After

```java
// Before Java 5
synchronized (lock) { count++; }

// After Java 5
AtomicInteger count = new AtomicInteger(0);
count.incrementAndGet(); // atomic + visible, no lock
```

### When to Use

```text
✅ Counters, sequence numbers, statistics
✅ Flags with atomic state transitions
✅ Reference updates (AtomicReference)
✅ Low contention scenarios
❌ Complex multi-variable operations → use synchronized/Lock
❌ Very high contention → LongAdder may be better
```

**Interview Point:**

> Atomic classes = thread-safe single-variable ops without locks. Introduced Java 5 with j.u.c. CAS-based — faster than synchronized for counters under low contention.

</details>

---

# 2. AtomicInteger?

<details>
<summary>Show Answer</summary>

**Answer:**

`AtomicInteger` is a thread-safe wrapper around an `int` that supports **atomic read-modify-write operations** like increment, decrement, and compare-and-set using CAS.

### Basic Usage

```java
AtomicInteger counter = new AtomicInteger(0);

counter.set(10);              // atomic write
int val = counter.get();      // atomic read
counter.incrementAndGet();    // ++counter, returns new value
counter.getAndIncrement();    // counter++, returns old value
counter.addAndGet(5);         // counter += 5
counter.compareAndSet(10, 20); // CAS update
```

### Thread-Safe Counter

```java
AtomicInteger requestCount = new AtomicInteger(0);

// Multiple threads — no lost updates
executor.submit(() -> requestCount.incrementAndGet());
executor.submit(() -> requestCount.incrementAndGet());

// Guaranteed accurate count
System.out.println(requestCount.get());
```

### Common Methods

| Method | Description |
|--------|-------------|
| `get()` | Atomic read |
| `set(int)` | Atomic write |
| `incrementAndGet()` | ++i, return new |
| `getAndIncrement()` | i++, return old |
| `addAndGet(int)` | i += delta, return new |
| `compareAndSet(exp, update)` | CAS update |
| `getAndSet(int)` | Set and return old |

### vs int + synchronized

```java
// synchronized — blocks threads
private int count = 0;
synchronized void increment() { count++; }

// AtomicInteger — CAS, no blocking (usually)
AtomicInteger count = new AtomicInteger(0);
count.incrementAndGet();
```

### Internal Storage

```java
// AtomicInteger internally:
private volatile int value; // volatile for visibility
// CAS operations on this value field
```

**Interview Point:**

> `AtomicInteger` = thread-safe int with atomic increment/decrement/CAS. Uses volatile + CAS internally. Go-to for shared counters.

</details>

---

# 3. AtomicLong?

<details>
<summary>Show Answer</summary>

**Answer:**

`AtomicLong` is the **64-bit equivalent** of `AtomicInteger`—provides atomic operations on a `long` value using CAS, safe for timestamps, IDs, and large counters.

### Basic Usage

```java
AtomicLong counter = new AtomicLong(0L);
AtomicLong timestamp = new AtomicLong(System.currentTimeMillis());

counter.incrementAndGet();
counter.addAndGet(1000L);
long val = counter.get();

// Update timestamp atomically
timestamp.set(System.currentTimeMillis());
```

### Use Cases

```java
// Unique ID generator
AtomicLong idGenerator = new AtomicLong(0);
long nextId = idGenerator.incrementAndGet();

// High-volume counter
AtomicLong totalBytes = new AtomicLong(0);
totalBytes.addAndGet(bytesReceived);

// Last-updated timestamp
AtomicLong lastModified = new AtomicLong(0);
lastModified.set(System.currentTimeMillis());
```

### AtomicLong vs AtomicInteger

| | `AtomicInteger` | `AtomicLong` |
|---|-----------------|--------------|
| Type | `int` (32-bit) | `long` (64-bit) |
| Use for | Counters, flags | IDs, timestamps, large counts |
| CAS | 32-bit CAS | 64-bit CAS |
| Methods | Same API | Same API |

### Java 8 Alternative — LongAdder

```java
// High contention — LongAdder faster than AtomicLong
LongAdder counter = new LongAdder();
counter.increment();
counter.sum(); // get total

// AtomicLong: single CAS variable — contention under heavy load
// LongAdder: striped cells — better under high contention
```

### 64-bit Atomicity

```text
volatile long on 32-bit JVM: torn read possible without atomic wrapper
AtomicLong: guaranteed atomic 64-bit read/write on all platforms
```

**Interview Point:**

> `AtomicLong` = atomic 64-bit ops. Use for IDs, timestamps, large counters. Under **high contention**, prefer `LongAdder` (Java 8).

</details>

---

# 4. AtomicBoolean?

<details>
<summary>Show Answer</summary>

**Answer:**

`AtomicBoolean` provides **atomic operations on a boolean flag**—ideal for one-time initialization flags, shutdown signals, and state toggles without synchronized blocks.

### Basic Usage

```java
AtomicBoolean initialized = new AtomicBoolean(false);
AtomicBoolean shutdown  = new AtomicBoolean(false);

// Atomic compare-and-set for one-time init
if (initialized.compareAndSet(false, true)) {
  // only ONE thread executes this
    setupResources();
}

// Shutdown flag
shutdown.set(true);
while (!shutdown.get()) {
    processTask();
}
```

### One-Time Initialization

```java
class Service {
    private final AtomicBoolean started = new AtomicBoolean(false);

    public void start() {
        if (started.compareAndSet(false, true)) {
            // only first caller initializes
            initializeConnections();
            startWorkers();
        }
    }
}
```

### vs volatile boolean

```java
// volatile boolean — assignment atomic but compare-and-set not safe
volatile boolean flag = false;
if (!flag) { flag = true; } // NOT atomic check-then-act!

// AtomicBoolean — atomic check-then-act
AtomicBoolean flag = new AtomicBoolean(false);
flag.compareAndSet(false, true); // atomic CAS
```

### Common Methods

```java
AtomicBoolean flag = new AtomicBoolean(false);

flag.get();                          // read
flag.set(true);                      // write
flag.compareAndSet(false, true);     // CAS
flag.getAndSet(true);                // set and return old
flag.lazySet(true);                  // delayed write
```

### Use Cases

```text
✅ Initialization flags (started, initialized)
✅ Shutdown/cancellation flags
✅ Feature toggles with atomic state change
✅ Gate/lock-free one-time setup
```

**Interview Point:**

> `AtomicBoolean` = atomic flag with CAS. Use for one-time init (`compareAndSet(false, true)`) and shutdown flags. Safer than volatile for check-then-act.

</details>

---

## Advanced

---

# 5. CAS (Compare And Swap)?

<details>
<summary>Show Answer</summary>

**Answer:**

**CAS (Compare-And-Swap)** is a hardware atomic instruction that updates a value **only if it matches an expected value**—the foundation of lock-free programming and all atomic classes.

### How CAS Works

```text
CAS(memory location, expectedValue, newValue):
  if (memory == expectedValue):
    memory = newValue
    return true   ← success
  else:
    return false  ← someone else changed it, retry
```

### Pseudocode

```java
boolean compareAndSet(int expected, int newValue) {
    if (value == expected) {   // atomic hardware check
        value = newValue;      // atomic hardware write
        return true;
    }
    return false;
}
```

### CAS Increment Example

```java
// incrementAndGet() internally:
public int incrementAndGet() {
    int current, next;
    do {
        current = get();       // read current value
        next = current + 1;    // compute new value
    } while (!compareAndSet(current, next)); // retry if CAS fails
    return next;
}
```

### CAS Timeline — Two Threads

```text
count = 5

Thread A: read 5, compute 6, CAS(5,6) → SUCCESS → count=6
Thread B: read 5, compute 6, CAS(5,6) → FAIL (count is 6)
Thread B: read 6, compute 7, CAS(6,7) → SUCCESS → count=7
```

### CAS Properties

| Property | Detail |
|----------|--------|
| **Atomic** | Hardware guarantees — no partial update |
| **Non-blocking** | Failed CAS retries — no thread blocking |
| **Optimistic** | Assumes no contention — fast when true |
| **ABA problem** | Value changes A→B→A — CAS sees no change |

### Hardware Support

```text
x86:  CMPXCHG instruction
ARM:  LDREX/STREX
JVM:  Unsafe.compareAndSwapInt/Long/Object
```

### ABA Problem

```text
value = A
Thread 1: reads A, prepares to CAS to C
Thread 2: A → B → A (value back to A)
Thread 1: CAS(A, C) succeeds — but state changed in between!
Fix: AtomicStampedReference (version stamp)
```

**Interview Point:**

> CAS = atomic "if current==expected, set to new" at hardware level. Foundation of atomic classes. Retry loop on failure. Watch ABA problem.

</details>

---

# 6. How AtomicInteger works internally?

<details>
<summary>Show Answer</summary>

**Answer:**

`AtomicInteger` stores value in a **`volatile int` field** and uses **CAS loops** via `Unsafe.compareAndSwapInt()` for atomic read-modify-write operations.

### Internal Structure

```java
public class AtomicInteger {
    private volatile int value; // visibility via volatile

    public AtomicInteger(int initialValue) {
        value = initialValue;
    }

    public final int get() {
        return value; // volatile read
    }

    public final void set(int newValue) {
        value = newValue; // volatile write
    }
}
```

### incrementAndGet() Internals

```java
public final int incrementAndGet() {
    return unsafe.getAndAddInt(this, valueOffset, 1) + 1;
    // OR CAS loop:
    // int current, next;
    // do {
    //     current = get();
    //     next = current + 1;
    // } while (!compareAndSet(current, next));
    // return next;
}
```

### compareAndSet() Internals

```java
public final boolean compareAndSet(int expect, int update) {
    return unsafe.compareAndSwapInt(this, valueOffset, expect, update);
    // Single hardware CAS instruction — atomic
}
```

### Unsafe — Bridge to Hardware

```text
AtomicInteger
    ↓
Unsafe.compareAndSwapInt(obj, offset, expect, update)
    ↓
Native CAS instruction (CMPXCHG on x86)
    ↓
CPU atomic operation on memory
```

### valueOffset

```java
// Offset of 'value' field computed at class load time
private static final Unsafe unsafe = Unsafe.getUnsafe();
private static final long valueOffset;

static {
    valueOffset = unsafe.objectFieldOffset(
        AtomicInteger.class.getDeclaredField("value"));
}
// Direct memory access to value field — bypasses accessor overhead
```

### Why volatile + CAS

```text
volatile:  visibility — all threads see latest value
CAS:       atomicity — read-modify-write in one hardware op
Together:  thread-safe without mutex lock
```

### getAndAddInt Optimization

```text
Modern JVM uses native getAndAddInt when available:
  Single atomic ADD instruction on modern CPUs
  Faster than CAS retry loop
  Falls back to CAS loop on older hardware
```

**Interview Point:**

> AtomicInteger = `volatile int` + `Unsafe.compareAndSwapInt()`. CAS retry loop for updates. Hardware CAS via CMPXCHG. volatile ensures visibility.

</details>

---

# 7. Difference between AtomicInteger and synchronized?

<details>
<summary>Show Answer</summary>

**Answer:**

`AtomicInteger` uses **CAS (lock-free)** for single-variable atomic ops. `synchronized` uses **monitor locks** for mutual exclusion—blocking threads and protecting arbitrary code blocks.

### Comparison Table

| | `AtomicInteger` | `synchronized` |
|---|-----------------|----------------|
| Mechanism | CAS (hardware) | Monitor lock |
| Blocking | ❌ Non-blocking (retries) | ✅ Blocks threads |
| Scope | Single variable ops | Any code block |
| Performance (low contention) | ✅ Faster | Slower — lock overhead |
| Performance (high contention) | ⚠️ CAS retries pile up | Mutex queues threads |
| Atomicity | Single variable only | Multiple variables + logic |
| Deadlock risk | ❌ None | ✅ Possible |
| Visibility | ✅ volatile semantics | ✅ happens-before |

### Code Comparison

```java
// AtomicInteger — single variable
AtomicInteger count = new AtomicInteger(0);
count.incrementAndGet(); // atomic, no lock

// synchronized — any logic
private int count = 0;
synchronized (lock) {
    count++;
    total += count;  // multiple variables — needs sync
    log(count);
}
```

### Performance Under Contention

```text
Low contention (few threads):
  AtomicInteger wins — no lock/unlock overhead

High contention (many threads CAS-failing):
  synchronized may win — threads sleep instead of spinning
  LongAdder best for high-contention counters
```

### When to Use Each

```java
// ✅ AtomicInteger
AtomicInteger pageViews = new AtomicInteger();
pageViews.incrementAndGet();

// ✅ synchronized — multiple variables
synchronized (lock) {
    balance -= amount;
    transactionCount++;
    lastTransaction = now();
}

// ✅ synchronized — complex logic
synchronized (lock) {
    if (queue.size() < MAX) {
        queue.add(item);
        notifyAll();
    }
}
```

### AtomicInteger Cannot Do

```text
❌ Protect multiple variables together
❌ Atomic increment + notify pattern
❌ Complex conditional updates across fields
❌ Read-modify-write spanning multiple objects
```

**Interview Point:**

> AtomicInteger = CAS, lock-free, single variable, no deadlock. synchronized = mutex, blocks threads, protects any code. Use Atomic for counters; sync for compound operations.

</details>

---

# 8. Lock-free programming?

<details>
<summary>Show Answer</summary>

**Answer:**

**Lock-free programming** is a concurrency approach where threads **never block** waiting for locks—instead they use atomic operations (CAS) and retry until successful, guaranteeing system-wide progress.

### Lock-Free vs Lock-Based

| | Lock-Based | Lock-Free |
|---|------------|-----------|
| Mechanism | Mutex/synchronized | CAS atomic ops |
| Blocking | Threads block on lock | Threads never block |
| Progress | One thread at a time | At least one thread always progresses |
| Deadlock | Possible | Impossible |
| Examples | synchronized, ReentrantLock | AtomicInteger, ConcurrentLinkedQueue |

### Lock-Free Increment

```java
// Lock-free — CAS retry, no blocking
AtomicInteger count = new AtomicInteger(0);

void increment() {
    int current, next;
    do {
        current = count.get();
        next = current + 1;
    } while (!count.compareAndSet(current, next));
    // retry if another thread won the CAS race
}
```

### Progress Guarantees

```text
Blocking:     one thread holds lock — others blocked
Lock-free:    at least ONE thread makes progress always
Wait-free:    EVERY thread completes in finite steps (strongest)
```

### Lock-Free Data Structures

```java
// ConcurrentLinkedQueue — lock-free queue
ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
queue.offer("task");  // lock-free add
queue.poll();         // lock-free remove

// AtomicReference — lock-free reference update
AtomicReference<Config> config = new AtomicReference<>(defaultConfig);
config.compareAndSet(oldConfig, newConfig);
```

### Advantages

```text
✅ No deadlock — no locks to deadlock on
✅ No priority inversion
✅ Better under low contention
✅ No context switch on contention (usually)
```

### Disadvantages

```text
❌ CAS retry loops under high contention (CPU spinning)
❌ Complex to implement correctly
❌ ABA problem
❌ Harder to reason about than locks
```

### Lock-Free vs Wait-Free

```text
Lock-free:  system progresses (some thread succeeds)
Wait-free:  every thread completes in bounded steps
  Lock-free is weaker but more practical
```

**Interview Point:**

> Lock-free = no mutex blocking, uses CAS retries. At least one thread always progresses. Atomic classes are lock-free. High contention → consider LongAdder or locks.

</details>

---

## Methods

---

# 9. incrementAndGet()

<details>
<summary>Show Answer</summary>

**Answer:**

`incrementAndGet()` atomically **increments by 1** and **returns the new value**—equivalent to prefix `++counter` but thread-safe.

### Usage

```java
AtomicInteger counter = new AtomicInteger(5);

int newVal = counter.incrementAndGet();
// counter: 5 → 6
// returns: 6
```

### Equivalent

```java
// incrementAndGet() ≡ prefix ++
int result = counter.incrementAndGet(); // ++counter

// getAndIncrement() ≡ postfix ++
int result = counter.getAndIncrement(); // counter++
```

### Thread-Safe Counter

```java
AtomicInteger visitors = new AtomicInteger(0);

// 100 threads incrementing — all counted
for (int i = 0; i < 100; i++) {
    new Thread(() -> visitors.incrementAndGet()).start();
}
// visitors.get() == 100 (guaranteed)
```

### Internal Flow

```text
1. Read current value (e.g., 5)
2. Compute new value (6)
3. CAS(current=5, new=6)
4. If CAS fails → retry from step 1
5. Return new value (6)
```

### vs synchronized increment

```java
// AtomicInteger
counter.incrementAndGet(); // CAS — no lock

// synchronized equivalent
synchronized (lock) {
    counter++; // blocks other threads
}
```

**Interview Point:**

> `incrementAndGet()` = atomic `++i`, returns **new** value. `getAndIncrement()` = atomic `i++`, returns **old** value. Core counter method.

</details>

---

# 10. getAndIncrement()

<details>
<summary>Show Answer</summary>

**Answer:**

`getAndIncrement()` atomically **increments by 1** and **returns the old value** before increment—equivalent to postfix `counter++` but thread-safe.

### Usage

```java
AtomicInteger counter = new AtomicInteger(5);

int oldVal = counter.getAndIncrement();
// counter: 5 → 6
// returns: 5 (old value)
```

### incrementAndGet() vs getAndIncrement()

| Method | Operation | Returns |
|--------|-----------|---------|
| `incrementAndGet()` | `++i` (prefix) | **New** value |
| `getAndIncrement()` | `i++` (postfix) | **Old** value |

```java
AtomicInteger c = new AtomicInteger(10);

c.incrementAndGet();  // returns 11, c=11
c.getAndIncrement();  // returns 11, c=12
```

### ID Generator Use Case

```java
AtomicLong idSeq = new AtomicLong(1000);

long assignedId = idSeq.getAndIncrement();
// Returns current ID, then increments for next caller
// Thread-safe ID assignment without lock
```

### Sequence Number

```java
AtomicInteger seq = new AtomicInteger(0);

int orderNum = seq.getAndIncrement(); // 0, 1, 2, 3...
String orderId = "ORD-" + orderNum;
```

### Internal — Same CAS Loop

```text
Same CAS mechanism as incrementAndGet()
Difference: returns OLD value instead of NEW value
```

**Interview Point:**

> `getAndIncrement()` = atomic postfix `i++`, returns **old** value. Pair with `incrementAndGet()` (prefix `++i`, returns new).

</details>

---

# 11. compareAndSet()

<details>
<summary>Show Answer</summary>

**Answer:**

`compareAndSet(expected, update)` atomically sets the value to `update` **only if** the current value equals `expected`—returns `true` on success, `false` if value changed.

### Usage

```java
AtomicInteger counter = new AtomicInteger(10);

boolean success = counter.compareAndSet(10, 20);
// current=10, expected=10 → SUCCESS, counter=20, returns true

boolean fail = counter.compareAndSet(10, 30);
// current=20, expected=10 → FAIL, counter stays 20, returns false
```

### One-Time Initialization

```java
AtomicBoolean initialized = new AtomicBoolean(false);

if (initialized.compareAndSet(false, true)) {
    // ONLY first thread enters here
    loadConfiguration();
    startServices();
}
```

### Optimistic Update Pattern

```java
AtomicInteger balance = new AtomicInteger(1000);

boolean withdrawn = false;
int current, newBalance;
do {
    current = balance.get();
    if (current < 500) break; // insufficient funds
    newBalance = current - 500;
} while (!balance.compareAndSet(current, newBalance));

if (withdrawn) processWithdrawal();
```

### AtomicReference CAS

```java
AtomicReference<String> status = new AtomicReference<>("IDLE");

// Only transition IDLE → RUNNING if still IDLE
boolean started = status.compareAndSet("IDLE", "RUNNING");
```

### CAS Loop Pattern

```java
// General update pattern
int current, newVal;
do {
    current = atomic.get();
    newVal = computeNewValue(current);
} while (!atomic.compareAndSet(current, newVal));
```

### Returns

```text
true  → value was expected, updated to new value
false → value changed since read, update NOT applied
```

**Interview Point:**

> `compareAndSet(expected, update)` = atomic if-then-update. Foundation of CAS. Use for optimistic updates, state transitions, lock-free algorithms.

</details>

---

# 12. lazySet()

<details>
<summary>Show Answer</summary>

**Answer:**

`lazySet(newValue)` sets the value with **delayed visibility**—cheaper than `set()` because it may not immediately flush to main memory, but eventually becomes visible to other threads.

### Usage

```java
AtomicInteger status = new AtomicInteger(0);

status.lazySet(2); // delayed write — faster than set()
```

### lazySet() vs set()

| | `set(value)` | `lazySet(value)` |
|---|--------------|------------------|
| Visibility | Immediate (volatile write) | Delayed — eventual visibility |
| Performance | Standard volatile write | Faster — may skip memory barrier |
| Ordering | Full happens-before | Reordering allowed before lazySet |
| Use when | Value must be seen immediately | Delayed visibility acceptable |

### When to Use lazySet()

```java
// Statistics/metrics — exact immediate visibility not critical
AtomicLong eventCount = new AtomicLong();

void recordEvent() {
    eventCount.lazySet(eventCount.get() + 1);
    // Other threads may see count slightly delayed — OK for metrics
}

// Progress indicator — approximate value fine
AtomicInteger progress = new AtomicInteger();
progress.lazySet(computedProgress);
```

### When NOT to Use lazySet()

```java
// ❌ Don't use for flags other threads depend on immediately
AtomicBoolean shutdown = new AtomicBoolean();
shutdown.lazySet(true); // worker may not see shutdown promptly!

// ✅ Use set() for coordination flags
shutdown.set(true); // immediate visibility required
```

### Internal Behavior

```text
set():     volatile write → store barrier → immediate main memory flush
lazySet(): ordered write → may stay in store buffer longer
           eventually visible — not guaranteed immediate
           cheaper on some architectures
```

### Java Memory Model

```text
lazySet establishes ordering:
  All prior writes visible before lazySet value becomes visible
  But lazySet value itself may be delayed
  Cannot reorder writes AFTER lazySet to before lazySet
```

**Interview Point:**

> `lazySet()` = cheaper delayed write. Use for stats/metrics where immediate visibility not critical. Use `set()` for flags and coordination where threads must see change immediately.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: AtomicInteger vs LongAdder?

<details>
<summary>Show Answer</summary>

**Answer:**

| | `AtomicInteger` | `LongAdder` (Java 8) |
|---|-----------------|---------------------|
| Contention | Single CAS variable | Striped cells |
| Low contention | Fast | Slightly more overhead |
| High contention | CAS retries — slow | Much faster |
| `sum()` | `get()` — exact | `sum()` — may not be exact mid-update |

Use `LongAdder` for high-throughput counters (metrics, stats).

</details>

---

### Q: ABA problem in CAS?

<details>
<summary>Show Answer</summary>

**Answer:**

Value changes A → B → A. CAS sees expected A and succeeds — but intermediate change B happened. Fix: `AtomicStampedReference` with version stamp, or `AtomicMarkableReference`.

```java
AtomicStampedReference<String> ref =
    new AtomicStampedReference<>("A", 0);
// CAS checks value AND stamp (version)
```

</details>

---

### Q: Can AtomicInteger prevent deadlock?

<details>
<summary>Show Answer</summary>

**Answer:**

**Yes** — atomic classes use CAS, not locks. No mutex → no deadlock possible. One reason to prefer atomics over synchronized for simple counters.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Atomic classes = **CAS-based** thread-safe single-variable ops, no locks. `AtomicInteger` for counters, `AtomicBoolean` for flags. Key methods: `incrementAndGet`, `compareAndSet`. Lock-free but CAS retries under contention → `LongAdder` for high throughput.

</details>
