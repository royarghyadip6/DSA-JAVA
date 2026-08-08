# 13.5 ConcurrentHashMap

## ConcurrentHashMap

### Most Important

---

# 1. Difference between HashMap and ConcurrentHashMap?

<details>
<summary>Show Answer</summary>

**Answer:**

| Feature | HashMap | ConcurrentHashMap |
|---------|---------|-------------------|
| Thread-safe | ❌ No | ✅ Yes |
| `null` key | ✅ One allowed | ❌ Not allowed |
| `null` value | ✅ Allowed | ❌ Not allowed |
| Locking | None | Fine-grained (segment/bucket) |
| Iterator | Fail-fast | Weakly consistent |
| Performance (single thread) | Faster | Slightly slower |
| Performance (multi-thread) | Unsafe / needs external lock | Designed for concurrency |
| Resize | Not safe concurrently | Safe concurrent resize |

### Example — HashMap (not thread-safe)

```java
Map<String, Integer> map = new HashMap<>();
// Concurrent put by multiple threads → data loss / corruption
```

### Example — ConcurrentHashMap

```java
Map<String, Integer> map = new ConcurrentHashMap<>();
map.put("count", 1);
map.putIfAbsent("count", 2);
map.computeIfAbsent("key", k -> 100);
```

### When to Use

| Use HashMap | Use ConcurrentHashMap |
|-------------|----------------------|
| Single-threaded code | Shared cache, session store |
| Local method scope | Concurrent counters, registries |
| No thread access | Spring bean maps, rate limiters |

**Interview Point:**

> HashMap = fast but not thread-safe. ConcurrentHashMap = concurrent reads + fine-grained writes without locking entire map.

</details>

---

# 2. Why HashMap is not thread-safe?

<details>
<summary>Show Answer</summary>

**Answer:**

`HashMap` has **no internal synchronization**. Multiple threads modifying it simultaneously cause **race conditions** and **data corruption**.

### Problem 1 — Lost Updates

```java
Map<String, Integer> map = new HashMap<>();

// Thread 1: map.put("count", 1)
// Thread 2: map.put("count", 2)
// Result unpredictable — one update may be lost
```

### Problem 2 — Infinite Loop (Java 7)

```text
Two threads resize simultaneously
    ↓
Circular linked list created in bucket
    ↓
get() loops forever → CPU 100%
```

### Problem 3 — Structural Corruption

```text
Thread 1: resize (moving entries)
Thread 2: put (inserting entry)
    ↓
Entries lost or duplicated
size() incorrect
```

### Problem 4 — ConcurrentModificationException

```java
for (String key : map.keySet()) {
    map.remove(key); // another thread modifies
}
// ConcurrentModificationException
```

### Wrong "Fix"

```java
// ❌ Locks ENTIRE map — poor scalability
Map<String, Integer> map =
        Collections.synchronizedMap(new HashMap<>());
```

### Correct Fix

```java
Map<String, Integer> map = new ConcurrentHashMap<>();
```

**Interview Point:**

> HashMap fails under concurrency: lost data, infinite loops (Java 7), corrupted structure. Never use raw HashMap as shared mutable state.

</details>

---

# 3. How ConcurrentHashMap works?

<details>
<summary>Show Answer</summary>

**Answer:**

`ConcurrentHashMap` allows **multiple threads** to read and write concurrently using **fine-grained locking** instead of locking the entire map.

### Java 7 — Segment Locking

```text
ConcurrentHashMap
    ↓
Segment[] (default 16 segments)
    ↓
Each Segment = small HashMap + ReentrantLock
    ↓
Threads lock only ONE segment at a time
```

### Java 8+ — Bucket-Level Locking + CAS

```text
Node[] table (like HashMap)
    ↓
Empty bucket → CAS insert (no lock)
Occupied bucket → synchronize on bucket head node
Tree bucket → synchronize on tree root
    ↓
Reads mostly lock-free
Writes lock only affected bucket
```

### put() Flow (Java 8+)

```text
1. Compute hash → bucket index
2. If bucket empty → CAS insert (atomic, no lock)
3. If bucket occupied → lock bucket head
4. Insert/update in bucket chain or tree
5. Release lock
6. If resize needed → cooperative multi-thread resize
```

### Example

```java
ConcurrentHashMap<String, Integer> cache =
        new ConcurrentHashMap<>();

cache.put("user:101", 1);
cache.computeIfAbsent("user:102", k -> loadFromDB(k));
```

### Read Operations

* `get()` — **no lock** in most cases (volatile reads)
* Multiple threads can read simultaneously

**Interview Point:**

> Java 7 = segment locks. Java 8+ = CAS for empty buckets + synchronized bucket heads. Reads scale; writes lock minimally.

</details>

---

### Java 7

---

# 4. What is Segment?

<details>
<summary>Show Answer</summary>

**Answer:**

In **Java 7**, `ConcurrentHashMap` divided the map into **Segments**—each segment is an independent **small HashMap** protected by its own **`ReentrantLock`**.

### Structure

```text
ConcurrentHashMap
    ↓
Segment[] segments (default size = 16)

Segment:
  - HashEntry[] table  (bucket array)
  - ReentrantLock lock
  - int count          (entry count)
  - int modCount
```

### How Segment Works

```text
hash(key) → segment index → lock segment → operate on segment's HashMap
```

### Default Configuration

| Parameter | Default |
|-----------|---------|
| Segments | 16 |
| Concurrency Level | 16 |
| Segment load factor | 0.75 |

### Example — Concurrency Level

```java
ConcurrentHashMap<String, Integer> map =
        new ConcurrentHashMap<>(16, 0.75f, 32);
// 32 segments → up to 32 threads can write different segments concurrently
```

### Segment Index

```text
segmentIndex = (hash >>> segmentShift) & segmentMask
```

### Limitation

* Fixed number of segments at creation
* Two keys in same segment still contend for same lock
* Java 8 replaced segments with finer bucket locking

**Interview Point:**

> Segment = partitioned HashMap + lock. Java 7 concurrency level = number of segments = max parallel write throughput.

</details>

---

### Java 8

---

# 5. How synchronization changed?

<details>
<summary>Show Answer</summary>

**Answer:**

Java 8 **removed Segments** and adopted **bucket-level synchronization** with **CAS** for better scalability.

### Java 7 vs Java 8

| Aspect | Java 7 | Java 8+ |
|--------|--------|---------|
| Structure | Segment array | Single Node array (like HashMap) |
| Lock granularity | Entire segment | Single bucket head |
| Empty bucket insert | Lock segment | **CAS** (lock-free) |
| Read | Lock-free within segment | Lock-free globally |
| Resize | Per-segment | Cooperative multi-thread |
| Tree support | No | Red-Black tree (like HashMap) |

### Java 8 Synchronization Strategy

```text
Bucket empty     → CAS put (no lock)
Bucket has nodes → synchronized(bucketHead)
Tree bucket      → synchronized(treeRoot)
Resize           → cooperative transfer with stride assignment
```

### Why Better?

```text
Java 7: 16 segments → max 16 concurrent writes (default)
Java 8: N buckets → concurrent writes on different buckets don't block
```

### Example

```java
// Java 8 ConcurrentHashMap — no segments
ConcurrentHashMap<String, Integer> map =
        new ConcurrentHashMap<>();

// Two threads putting different keys → different buckets → no lock contention
map.put("keyA", 1);  // Thread 1
map.put("keyB", 2);  // Thread 2 — parallel
```

**Interview Point:**

> Java 8 dropped segments for finer bucket-level locks + CAS. More parallelism, especially with large maps and many threads.

</details>

---

# 6. What is CAS?

<details>
<summary>Show Answer</summary>

**Answer:**

**CAS (Compare-And-Swap)** is a **lock-free atomic operation** at the CPU level used to update a value only if it matches an expected value.

### CAS Logic

```text
CAS(memory location, expectedValue, newValue):

  if (current value == expectedValue)
      update to newValue → return true
  else
      do nothing → return false
```

### In ConcurrentHashMap (Java 8+)

```text
Empty bucket insert:

  CAS(table[i], null, newNode)
      ↓
  If bucket still null → insert atomically
  If another thread inserted → retry or lock
```

### Pseudocode

```java
// Conceptual CAS usage in put
if (table[i] == null) {
    if (CAS(table[i], null, newNode)) {
        return; // success — no lock needed
    }
}
// CAS failed — another thread won → synchronize on bucket
```

### CAS vs Lock

| CAS | Lock |
|-----|------|
| Lock-free | Thread may block |
| No deadlock | Possible deadlock (if misused) |
| Retry on failure | Waits for lock release |
| Best for simple atomic updates | Best for complex critical sections |

### Under the Hood

```text
sun.misc.Unsafe.compareAndSwapObject()
    ↓
CPU atomic instruction (e.g., cmpxchg on x86)
```

### Limitation — ABA Problem

Rare in HashMap context but CAS can succeed if value changes A→B→A. Not a practical issue for bucket head insertion.

**Interview Point:**

> CAS enables lock-free inserts into empty buckets—key to ConcurrentHashMap's read-heavy performance in Java 8+.

</details>

---

# 7. What is bucket-level locking?

<details>
<summary>Show Answer</summary>

**Answer:**

**Bucket-level locking** means synchronizing only on the **head node of a specific bucket**, not the entire map or a large segment.

### How It Works (Java 8+)

```text
put(key, value):
    index = hash(key) % table.length

    if table[index] == null:
        CAS insert (no lock)
    else:
        synchronized(table[index]) {  // lock THIS bucket only
            insert or update in chain/tree
        }
```

### Visual

```text
table[0] → Node A     ← Thread 1 locks bucket 0
table[1] → Node B     ← Thread 2 locks bucket 1 (parallel!)
table[2] → null       ← Thread 3 CAS insert (no lock)
table[3] → TreeNode   ← Thread 4 locks bucket 3
```

### Compare Lock Granularity

| Approach | Lock Scope | Parallelism |
|----------|------------|-------------|
| `synchronizedMap(HashMap)` | Entire map | 1 writer at a time |
| Java 7 Segments | 1 of 16 segments | ~16 writers |
| Java 8 Bucket lock | 1 bucket | N writers (different buckets) |

### Read Operations

```java
Integer value = map.get("key");
// No lock — volatile read of table and node references
// May return slightly stale value — acceptable for concurrent map
```

### Example — Parallel Writes

```java
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

// 100 threads each putting different keys
// Most land in different buckets → minimal lock contention
ExecutorService pool = Executors.newFixedThreadPool(100);
for (int i = 0; i < 100; i++) {
    pool.submit(() -> map.put("key" + Thread.currentThread().getId(), 1));
}
```

**Interview Point:**

> Bucket-level locking = synchronize only conflicting bucket. Different buckets = parallel writes. Core scalability improvement in Java 8.

</details>

---

### Advanced

---

# 8. putIfAbsent()?

<details>
<summary>Show Answer</summary>

**Answer:**

`putIfAbsent()` atomically inserts a key-value pair **only if the key is not already present**.

```java
V putIfAbsent(K key, V value)
```

### Returns

| Return | Meaning |
|--------|---------|
| `null` | Key was absent — value inserted |
| non-null | Key existed — returns **existing value** (no update) |

### Example

```java
ConcurrentHashMap<String, Integer> map =
        new ConcurrentHashMap<>();

map.put("count", 1);

Integer result = map.putIfAbsent("count", 2);
System.out.println(result); // 1 — existing value, not updated

Integer result2 = map.putIfAbsent("newKey", 100);
System.out.println(result2); // null — inserted
```

### Why Not HashMap put()?

```java
// ❌ NOT atomic — race condition
if (!map.containsKey(key)) {
    map.put(key, value); // another thread may insert between check and put
}

// ✅ Atomic
map.putIfAbsent(key, value);
```

### Production Use — Lazy Initialization

```java
map.putIfAbsent("config", loadExpensiveConfig());
// Only first thread loads config
```

### Java 8+ Equivalent Pattern

```java
map.computeIfAbsent(key, k -> loadValue(k));
// More flexible — computes value only if absent
```

**Interview Point:**

> `putIfAbsent()` = atomic "insert if missing". Essential for caches and one-time initialization in concurrent code.

</details>

---

# 9. computeIfAbsent()?

<details>
<summary>Show Answer</summary>

**Answer:**

`computeIfAbsent()` atomically computes and inserts a value **only if the key is absent**—using a `Function` to generate the value.

```java
V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction)
```

### Behavior

```text
if key absent:
    value = mappingFunction.apply(key)
    insert value
    return value
else:
    return existing value (mappingFunction NOT called)
```

### Example — Cache Pattern

```java
ConcurrentHashMap<String, User> userCache =
        new ConcurrentHashMap<>();

User user = userCache.computeIfAbsent("U101", id -> {
    System.out.println("Loading from DB...");
    return database.findUser(id); // only called if absent
});
```

### Second Call — No DB Hit

```java
User sameUser = userCache.computeIfAbsent("U101", id -> {
    return database.findUser(id); // NOT executed
});
```

### vs putIfAbsent()

| Method | Value Source |
|--------|--------------|
| `putIfAbsent(key, value)` | Pre-computed value (always evaluated) |
| `computeIfAbsent(key, fn)` | Lazy — function runs **only if absent** |

```java
// ❌ putIfAbsent — expensive call always runs
map.putIfAbsent(key, loadFromDB()); // loadFromDB() ALWAYS called

// ✅ computeIfAbsent — lazy
map.computeIfAbsent(key, k -> loadFromDB()); // only if absent
```

### Atomic Guarantee

Entire check-compute-insert is atomic—no duplicate computation by multiple threads for same key (mapping function may run at most once per key).

### Related Methods

```java
map.computeIfPresent(key, (k, v) -> v + 1);
map.compute(key, (k, v) -> v == null ? 1 : v + 1);
map.merge(key, 1, Integer::sum);
```

**Interview Point:**

> `computeIfAbsent()` = atomic lazy cache load. Preferred over `containsKey` + `put` pattern. Function not called if key exists.

</details>

---

# 10. Why null keys are not allowed?

<details>
<summary>Show Answer</summary>

**Answer:**

`ConcurrentHashMap` does **not** allow `null` keys or `null` values.

```java
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
map.put(null, 1);   // ❌ NullPointerException
map.put("key", null); // ❌ NullPointerException
```

### Reason 1 — Ambiguity in Concurrent Context

```java
Integer value = map.get(key);

if (value == null) {
    // Does key NOT exist?
    // OR does key exist with null value?
    // In concurrent map — another thread may insert between get and check
}
```

`HashMap` allows this ambiguity locally; concurrent access makes it **unresolvable safely**.

### Reason 2 — Non-Blocking Reads

```java
// get() is often lock-free
V value = table[i].value;

// If null value allowed:
// null → key missing OR value is null? Cannot distinguish without locking
```

### Reason 3 — Doug Lea's Design Decision

> Null is often used as a sentinel for "not found." In concurrent maps, allowing null would require additional checks on every operation, hurting performance.

### Reason 4 — Consistency with Concurrent Collections

```text
ConcurrentHashMap  → no null
ConcurrentSkipListMap → no null
Hashtable          → no null

HashMap            → null allowed (single-threaded ambiguity acceptable)
```

### Workaround

```java
// Use optional wrapper or sentinel object
ConcurrentHashMap<String, Optional<User>> map = new ConcurrentHashMap<>();
map.put("U101", Optional.of(user));
map.put("U102", Optional.empty()); // explicit "no user"
```

### HashMap vs ConcurrentHashMap

| | HashMap | ConcurrentHashMap |
|---|---------|-------------------|
| null key | ✅ One | ❌ |
| null value | ✅ | ❌ |

**Interview Point:**

> Null banned to avoid ambiguous `get() == null` in lock-free reads. Use Optional or sentinel values instead.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: ConcurrentHashMap vs synchronized HashMap?

<details>
<summary>Show Answer</summary>

**Answer:** `ConcurrentHashMap` uses fine-grained bucket locking—multiple threads can write different buckets simultaneously. `Collections.synchronizedMap()` locks the entire map on every operation—poor scalability.

</details>

---

### Q: Is ConcurrentHashMap iterator fail-fast?

<details>
<summary>Show Answer</summary>

**Answer:** No. It is **weakly consistent**—reflects state at some point during iteration, may miss concurrent updates, never throws `ConcurrentModificationException`.

</details>

---

### Q: Default concurrency level in Java 7?

<details>
<summary>Show Answer</summary>

**Answer:** **16** — creates 16 segments by default, allowing up to 16 concurrent write operations on different segments.

</details>

---

### Q: Can ConcurrentHashMap size() be exact under concurrency?

<details>
<summary>Show Answer</summary>

**Answer:** `size()` is an estimate under heavy concurrent modification. Use `mappingCount()` (Java 8+) for long count, or external coordination for exact counts.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> ConcurrentHashMap uses CAS for empty buckets and synchronized bucket heads for writes—reads are mostly lock-free. No null keys/values. Use `computeIfAbsent()` for atomic lazy loading in concurrent caches.

</details>
