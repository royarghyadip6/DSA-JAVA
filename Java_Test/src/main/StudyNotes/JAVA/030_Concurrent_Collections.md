# 30. Concurrent Collections

## 30. Concurrent Collections

## ConcurrentHashMap

---

# 1. How ConcurrentHashMap works?

<details>
<summary>Show Answer</summary>

**Answer:**

`ConcurrentHashMap` is a **thread-safe HashMap** that allows multiple threads to read and write concurrently—without locking the entire map like `Hashtable`.

### Simple Idea

```text
HashMap       → fast but NOT thread-safe
Hashtable     → thread-safe but locks ENTIRE map (slow)
ConcurrentHashMap → thread-safe + multiple threads work in parallel
```

### Java 8 Internal Model

```text
Array of buckets (like HashMap)
Each bucket can be:
  - empty
  - list of nodes (collision)
  - tree (many collisions)
  - locked during write (only THAT bucket)
```

### Write Operation (put)

```text
1. Calculate hash of key
2. Find bucket index
3. Lock ONLY that bucket (not whole map)
4. Insert/update node in bucket
5. Release bucket lock
Other threads can still work on OTHER buckets in parallel
```

### Read Operation (get)

```text
Most reads need NO lock at all
Thread reads bucket directly
Uses volatile/CAS for visibility
Very fast concurrent reads
```

### Key Methods

```java
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

map.put("key", 1);
map.get("key");
map.putIfAbsent("key", 2);       // atomic — only if absent
map.computeIfAbsent("key", k -> loadFromDB(k)); // atomic load
map.remove("key");
```

### vs HashMap

| | HashMap | ConcurrentHashMap |
|---|---------|-------------------|
| Thread-safe | ❌ | ✅ |
| Null key/value | ✅ Allowed | ❌ Not allowed |
| Lock scope | None | Per-bucket (Java 8) |
| Iterator | Fail-fast | Weakly consistent |

**Interview Point:**

> ConcurrentHashMap = thread-safe HashMap with **bucket-level locking** (Java 8). Multiple threads read/write different buckets in parallel. No null keys/values.

</details>

---

# 2. Bucket locking?

<details>
<summary>Show Answer</summary>

**Answer:**

**Bucket locking** means only the **specific bucket** being modified is locked—not the entire map—so other threads can still access other buckets freely.

### Java 7 vs Java 8

| Version | Locking Strategy |
|---------|------------------|
| **Java 7** | Segment locking (16 segments default) |
| **Java 8** | **Bucket-level** locking (per bucket) |

### Java 8 Bucket Lock

```text
Thread A: put("user1") → locks bucket 3 only
Thread B: put("user2") → locks bucket 7 only
Thread C: get("user3") → reads bucket 1 — no lock needed

All three run IN PARALLEL
```

### When Bucket Is Locked

```java
// Only during write to a specific bucket:
synchronized (bucketHead) {  // lock ONE bucket
    insertOrUpdate(node);
}
// Reads on same bucket: usually lock-free (volatile reads)
// Writes on other buckets: completely unaffected
```

### Treeify Under Lock

```text
When bucket has many collisions:
  Convert linked list → Red-Black Tree (like HashMap)
  Lock held during treeify operation
  Only that bucket blocked briefly
```

### Why Bucket Lock Is Fast

```text
Hashtable:     1 lock for entire map → 1 writer at a time
Java 7 CHM:    16 segment locks → 16 writers max
Java 8 CHM:    lock per bucket → hundreds of parallel writers
```

**Interview Point:**

> Java 8 ConcurrentHashMap locks **individual buckets** on write—not the whole map. Enables high parallel throughput. Java 7 used Segment locking.

</details>

---

# 3. CAS?

<details>
<summary>Show Answer</summary>

**Answer:**

**CAS (Compare-And-Swap)** is a hardware atomic operation used in `ConcurrentHashMap` for **lock-free reads and some updates**—threads update a value only if it still matches the expected value.

### CAS in Simple Words

```text
"If this memory location still holds value X,
 set it to Y — all in one atomic step.
 If someone else changed it → fail and retry."
```

### CAS in ConcurrentHashMap

```java
// putIfAbsent uses CAS internally
map.putIfAbsent("key", value);
// CAS: if key absent → insert atomically
// If another thread inserted first → CAS fails → retry or skip
```

### CAS vs Lock

| | CAS | Lock (synchronized) |
|---|-----|---------------------|
| Blocking | ❌ Non-blocking (retry) | ✅ Blocks threads |
| Contention | Good for low contention | Good for high contention |
| Use in CHM | Counter updates, init | Bucket writes |

### Counter Example — size()

```text
ConcurrentHashMap tracks size using CAS counters
  Multiple threads increment size counter via CAS
  No lock needed for counter increment
  Retry on CAS failure
```

### CAS Retry Loop

```java
// Conceptual CAS loop in CHM
while (true) {
    Node current = bucketHead;
    if (CAS(bucketHead, current, newNode)) {
        break; // success
    }
    // retry — someone else modified bucketHead
}
```

### ABA Problem (Awareness)

```text
Value: A → B → A (back to A)
CAS sees expected A → succeeds
But state changed in between
ConcurrentHashMap handles via versioned nodes
```

**Interview Point:**

> CAS = atomic compare-and-swap at hardware level. Used in ConcurrentHashMap for lock-free operations. Foundation of Java concurrent collections. Know CAS + bucket lock together for CHM internals.

</details>

---

## CopyOnWriteArrayList

---

# 4. What is CopyOnWriteArrayList?

<details>
<summary>Show Answer</summary>

**Answer:**

`CopyOnWriteArrayList` is a **thread-safe List** where every **write creates a new copy** of the underlying array—readers always see a stable snapshot without locks.

### Simple Analogy

```text
Like a shared document:
  Readers keep reading the current copy
  Writer makes edits on a NEW copy
  When done, writer swaps pointer to new copy
  Readers never blocked during reads
```

### Basic Usage

```java
CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();

list.add("A");     // creates new array copy with "A"
list.add("B");     // creates new array copy with "A","B"
String val = list.get(0); // reads current array — no lock
```

### Write = Copy Entire Array

```text
add("C"):
  1. Copy current array
  2. Add "C" to new array
  3. Replace reference to point to new array
  4. Old array discarded (GC)
```

### Read = No Lock

```text
get(index):
  Read directly from current array reference
  No synchronization needed
  Never throws ConcurrentModificationException
```

### Iterator — Snapshot

```java
Iterator<String> it = list.iterator();
list.add("new"); // modifies list
it.next();       // still iterates OLD snapshot — no CME!
```

**Interview Point:**

> CopyOnWriteArrayList = copy-on-write. Reads fast (no lock). Writes slow (copy entire array). Iterator is snapshot — safe for concurrent read.

</details>

---

# 5. Advantages?

<details>
<summary>Show Answer</summary>

**Answer:**

`CopyOnWriteArrayList` excels when **reads vastly outnumber writes**—iterators are safe, reads are lock-free, and no `ConcurrentModificationException`.

### Key Advantages

| Advantage | Explanation |
|-----------|-------------|
| **Lock-free reads** | `get()`, `iterator()` — no synchronization |
| **Safe iteration** | Iterator never throws CME |
| **Snapshot iterator** | Iterates stable copy — consistent view |
| **Thread-safe** | No external synchronization needed |
| **Simple to use** | Just use like ArrayList — thread-safe |

### Ideal Scenario — Listener List

```java
// Event listeners — add rarely, iterate frequently
CopyOnWriteArrayList<EventListener> listeners =
    new CopyOnWriteArrayList<>();

// Add listener (rare write)
listeners.add(new MyListener());

// Fire event (frequent read/iterate)
for (EventListener l : listeners) {
    l.onEvent(event); // safe — no lock, no CME
}
```

### Read-Heavy Workload

```text
Configuration list:  read 1000x/sec, update 1x/hour
Cache keys list:     iterate constantly, add rarely
Observer pattern:    notify all listeners frequently
```

### Iterator Safety

```java
// Safe even if list modified during iteration
for (String item : copyOnWriteList) {
    // another thread may add/remove
    // this loop continues on original snapshot
    process(item);
}
```

**Interview Point:**

> Advantages: lock-free reads, safe iteration, no CME. Perfect for **read-heavy, write-rare** scenarios — listeners, config, observer lists.

</details>

---

# 6. Disadvantages?

<details>
<summary>Show Answer</summary>

**Answer:**

`CopyOnWriteArrayList` is **expensive for writes** (copies entire array) and uses **extra memory**—unsuitable for write-heavy or large lists.

### Key Disadvantages

| Disadvantage | Explanation |
|--------------|-------------|
| **Slow writes** | Every add/remove copies entire array — O(n) |
| **Memory overhead** | Old + new array exist briefly during write |
| **Stale reads** | Iterator sees snapshot — may miss recent writes |
| **Not for large lists** | Copying 1M elements on each add is disastrous |
| **Write-heavy** | Performance degrades badly with frequent writes |

### Write Cost

```java
// List with 10,000 elements
list.add("newItem");
// Copies ALL 10,000 elements to new array + 1
// O(n) per write operation!
```

### Memory During Write

```text
Before write: array [A,B,C,D] — 4 elements
During write: array [A,B,C,D] + array [A,B,C,D,E] — 9 elements in memory!
After write:  array [A,B,C,D,E] — old array GC'd
```

### When NOT to Use

```text
❌ Frequent add/remove operations
❌ Large lists (thousands of elements)
❌ Write-heavy workloads
❌ Need latest data in iterator immediately

✅ Use synchronizedList or regular List + lock for write-heavy
```

### vs synchronizedList

```text
CopyOnWriteArrayList:  fast reads, slow writes
Collections.synchronizedList: slow reads (lock), fast writes
Choose based on read/write ratio
```

**Interview Point:**

> Disadvantages: O(n) writes, memory copy overhead, stale iterator snapshot. Never use for write-heavy or large lists. Choose based on read/write ratio.

</details>

---

## BlockingQueue

---

# 7. What is BlockingQueue?

<details>
<summary>Show Answer</summary>

**Answer:**

A `BlockingQueue` is a **thread-safe queue** that **blocks** the calling thread when the queue is empty (on take) or full (on put)—perfect for producer-consumer patterns.

### Core Behavior

```text
put(item):  if queue FULL  → thread WAITS until space available
take():     if queue EMPTY → thread WAITS until item available
```

### Basic Usage

```java
BlockingQueue<String> queue = new ArrayBlockingQueue<>(10);

// Producer
queue.put("task1"); // blocks if queue full (capacity 10)

// Consumer
String task = queue.take(); // blocks if queue empty
```

### Key Methods

| Method | On Full | On Empty | Throws |
|--------|---------|----------|--------|
| `put(e)` | Blocks | — | — |
| `take()` | — | Blocks | — |
| `offer(e)` | Returns false | — | — |
| `offer(e, timeout)` | Waits/timeout | — | — |
| `poll()` | Returns null | Returns null | — |
| `poll(timeout)` | — | Waits/timeout | — |

### Non-Blocking Alternatives

```java
// Don't want to block?
boolean added = queue.offer(item);     // false if full
String item   = queue.poll();          // null if empty

// Timed wait
boolean added = queue.offer(item, 1, SECONDS);
String item   = queue.poll(5, SECONDS);
```

### Thread-Safe Built-In

```text
No manual synchronized/wait/notify needed
BlockingQueue handles all thread coordination internally
Production-standard for producer-consumer
```

**Interview Point:**

> BlockingQueue = thread-safe queue with blocking put/take. Blocks when full/empty. Foundation of ExecutorService task queue and producer-consumer.

</details>

---

# 8. Producer Consumer problem?

<details>
<summary>Show Answer</summary>

**Answer:**

The **producer-consumer problem** is a classic concurrency scenario: **producers** add items to a shared buffer while **consumers** remove them—requiring safe coordination when the buffer is full or empty.

### The Problem

```text
Producer: makes items → puts in shared buffer
Consumer: takes items from shared buffer → processes them

Challenges:
  Buffer full   → producer must wait
  Buffer empty  → consumer must wait
  Multiple producers/consumers → thread safety needed
```

### Without Coordination — Broken

```java
Queue<String> queue = new LinkedList<>(); // NOT thread-safe!

// Producer
queue.add(item); // race condition!

// Consumer
String item = queue.remove(); // race condition! may throw exception
```

### Solution with BlockingQueue

```java
BlockingQueue<Task> queue = new ArrayBlockingQueue<>(100);

// Producer thread
new Thread(() -> {
    while (running) {
        Task task = createTask();
        queue.put(task); // waits if full — automatic!
    }
}).start();

// Consumer thread
new Thread(() -> {
    while (running) {
        Task task = queue.take(); // waits if empty — automatic!
        process(task);
    }
}).start();
```

### Real Production Examples

```text
✅ Order processing: API receives orders → queue → workers process
✅ Log processing: app writes logs → queue → log aggregator
✅ Email service: events → queue → email sender threads
✅ ExecutorService: tasks submitted → queue → pool threads execute
```

**Interview Point:**

> Producer-consumer = shared buffer between producers and consumers. BlockingQueue is the standard Java solution — no manual wait/notify needed.

</details>

---

# 9. ArrayBlockingQueue?

<details>
<summary>Show Answer</summary>

**Answer:**

`ArrayBlockingQueue` is a **bounded** blocking queue backed by a **fixed-size array**—when full, producers block; when empty, consumers block.

### Creation

```java
// Bounded — fixed capacity
BlockingQueue<String> queue = new ArrayBlockingQueue<>(100);

// Fair ordering option
BlockingQueue<String> fairQueue =
    new ArrayBlockingQueue<>(100, true); // FIFO fair
```

### Characteristics

| Property | Detail |
|----------|--------|
| **Bounded** | Fixed capacity — must specify size |
| **Backing store** | Single array (circular buffer) |
| **Memory** | Fixed — no growth |
| **Lock** | One ReentrantLock for head and tail |
| **Fair option** | Optional FIFO fairness |

### Usage

```java
ArrayBlockingQueue<Order> orderQueue =
    new ArrayBlockingQueue<>(50);

// Producer
orderQueue.put(order); // blocks if 50 orders queued

// Consumer
Order order = orderQueue.take(); // blocks if empty
```

### When to Use

```text
✅ Fixed buffer size known upfront
✅ Memory-bounded — won't grow unexpectedly
✅ ExecutorService internal queue (ThreadPoolExecutor)
✅ Backpressure — producers slow down when consumers lag

❌ Unknown/unlimited queue size → LinkedBlockingQueue
```

### Circular Array Internal

```text
Array: [item0, item1, item2, _, _]
       head=0, tail=3, capacity=5
put() → tail advances (wraps around)
take() → head advances
Fixed memory — no allocation on add/remove
```

**Interview Point:**

> ArrayBlockingQueue = **bounded** array-backed queue. Fixed memory. Blocks on full/empty. Used in ThreadPoolExecutor. Choose when buffer size must be capped.

</details>

---

# 10. LinkedBlockingQueue?

<details>
<summary>Show Answer</summary>

**Answer:**

`LinkedBlockingQueue` is a **optionally bounded** blocking queue backed by **linked nodes**—can be unbounded (default `Integer.MAX_VALUE`) or bounded with a capacity limit.

### Creation

```java
// Unbounded (default capacity = Integer.MAX_VALUE)
BlockingQueue<String> unbounded =
    new LinkedBlockingQueue<>();

// Bounded
BlockingQueue<String> bounded =
    new LinkedBlockingQueue<>(1000);
```

### vs ArrayBlockingQueue

| | `ArrayBlockingQueue` | `LinkedBlockingQueue` |
|---|---------------------|----------------------|
| Backing | Fixed array | Linked nodes |
| Default size | Must specify | Integer.MAX_VALUE (unbounded) |
| Memory | Fixed, pre-allocated | Grows with elements |
| Locks | Single lock | Two locks (put/take separate) |
| Throughput | Good | Better under mixed put/take |

### Two-Lock Design

```text
LinkedBlockingQueue uses TWO locks:
  putLock  → for producers (put/offer)
  takeLock → for consumers (take/poll)

Producer and consumer can operate SIMULTANEOUSLY
Better throughput than single-lock ArrayBlockingQueue
```

### Risk — Unbounded Queue

```java
// ⚠️ Default unbounded — can grow until OOM!
BlockingQueue<Task> queue = new LinkedBlockingQueue<>();
// Producers faster than consumers → queue grows forever → OOM

// ✅ Always set capacity in production
BlockingQueue<Task> queue = new LinkedBlockingQueue<>(10000);
```

### When to Use

```text
✅ Producer-consumer with variable load
✅ Need higher throughput (two-lock design)
✅ Variable queue size acceptable
✅ LinkedBlockingQueue in Executors.newFixedThreadPool (unbounded internal queue)

❌ Strict memory limit → ArrayBlockingQueue
```

**Interview Point:**

> LinkedBlockingQueue = linked-node blocking queue. Default **unbounded** — set capacity in production! Two locks for better put/take parallelism.

</details>

---

## Concurrent Collections Comparison

---

# 11. ConcurrentHashMap vs Hashtable?

<details>
<summary>Show Answer</summary>

**Answer:**

Both are thread-safe maps, but `ConcurrentHashMap` uses **fine-grained locking** (bucket-level) while `Hashtable` locks the **entire table** on every operation—making CHM far faster under concurrency.

### Comparison Table

| | `Hashtable` | `ConcurrentHashMap` |
|---|-------------|-------------------|
| Introduced | Java 1.0 (legacy) | Java 5 |
| Locking | **Entire table** | **Per-bucket** (Java 8) |
| Null key | ❌ Not allowed | ❌ Not allowed |
| Null value | ❌ Not allowed | ❌ Not allowed |
| Performance | Slow — one thread at a time | Fast — parallel access |
| Iterator | Fail-fast | Weakly consistent |
| Status | Legacy — avoid | ✅ Use this |

### Hashtable — Whole Table Lock

```java
// Every operation locks ENTIRE table
public synchronized V put(K key, V value) { ... }
public synchronized V get(Object key) { ... }
// Only ONE thread can access Hashtable at any time
```

### ConcurrentHashMap — Parallel Access

```java
// Multiple threads access different buckets simultaneously
map.put("key1", "val1"); // thread A — bucket 3
map.put("key2", "val2"); // thread B — bucket 7 — parallel!
map.get("key3");         // thread C — bucket 1 — parallel!
```

### Migration Path

```java
// ❌ Legacy
Hashtable<String, String> table = new Hashtable<>();

// ✅ Modern
ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();

// Also good for single-thread
HashMap<String, String> map = new HashMap<>();
```

**Interview Point:**

> Hashtable = synchronized entire map — legacy, slow. ConcurrentHashMap = bucket locking — modern, fast. Never use Hashtable in new code.

</details>

---

# 12. CopyOnWriteArrayList vs synchronizedList?

<details>
<summary>Show Answer</summary>

**Answer:**

`CopyOnWriteArrayList` optimizes for **read-heavy** workloads (lock-free reads, copy on write). `Collections.synchronizedList()` optimizes for **balanced** read/write (locks on every operation).

### Comparison Table

| | `CopyOnWriteArrayList` | `synchronizedList` |
|---|------------------------|-------------------|
| Read | **Lock-free** — fast | **Locked** — slower |
| Write | **Slow** — copies array | **Fast** — single lock |
| Iterator | Snapshot — no CME | Must manually sync iteration |
| Memory | Higher (copies) | Lower |
| Best for | Read-heavy, rare writes | Balanced read/write |

### synchronizedList Usage

```java
List<String> syncList =
    Collections.synchronizedList(new ArrayList<>());

syncList.add("A"); // locks entire list

// Must manually sync iteration!
synchronized (syncList) {
    for (String s : syncList) { // required!
        process(s);
    }
}
```

### CopyOnWriteArrayList Usage

```java
CopyOnWriteArrayList<String> cowList =
    new CopyOnWriteArrayList<>();

cowList.add("A"); // copies array — slow write

// No sync needed for iteration!
for (String s : cowList) {
    process(s); // safe — snapshot iterator
}
```

### Decision Guide

```text
Read >> Write (listeners, config):
  → CopyOnWriteArrayList

Balanced read/write:
  → synchronizedList or explicit lock

Write-heavy:
  → synchronizedList or ConcurrentLinkedQueue

Need latest data in iterator:
  → synchronizedList (not CopyOnWrite)
```

### Performance Summary

```text
1000 reads/sec, 1 write/sec  → CopyOnWriteArrayList wins
100 reads/sec, 100 writes/sec → synchronizedList wins
```

**Interview Point:**

> CopyOnWriteArrayList = fast reads, slow writes, snapshot iterator. synchronizedList = lock on every op, manual sync for iteration. Choose by read/write ratio.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: ConcurrentHashMap allows null?

<details>
<summary>Show Answer</summary>

**Answer:**

**No.** Neither null keys nor null values allowed in `ConcurrentHashMap` — avoids ambiguity in concurrent `get()`/`put()` null checks. `HashMap` allows one null key and multiple null values.

</details>

---

### Q: Which BlockingQueue for ThreadPoolExecutor?

<details>
<summary>Show Answer</summary>

**Answer:**

`FixedThreadPool` uses **unbounded `LinkedBlockingQueue`**. `CachedThreadPool` uses **`SynchronousQueue`** (zero capacity). Custom pools often use **bounded `ArrayBlockingQueue`** for backpressure.

</details>

---

### Q: Weakly consistent iterator in CHM?

<details>
<summary>Show Answer</summary>

**Answer:**

`ConcurrentHashMap` iterator reflects **some** state at creation time — may or may not include concurrent modifications. Never throws `ConcurrentModificationException`. May miss entries added after iterator created.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> ConcurrentHashMap = bucket lock + CAS, no nulls. CopyOnWriteArrayList = copy on write, read-heavy. BlockingQueue = blocking put/take for producer-consumer. ArrayBlockingQueue bounded; LinkedBlockingQueue default unbounded. Never Hashtable.

</details>
