# 13.6 Hashtable, LinkedHashMap & TreeMap

## Hashtable

---

# 1. Difference between Hashtable and HashMap?

<details>
<summary>Show Answer</summary>

**Answer:**

| Feature | HashMap | Hashtable |
|---------|---------|-----------|
| Introduced | Java 1.2 | Java 1.0 (legacy) |
| Thread-safe | ❌ No | ✅ Yes (synchronized) |
| `null` key | ✅ One allowed | ❌ Not allowed |
| `null` value | ✅ Allowed | ❌ Not allowed |
| Synchronization | None | Every method synchronized |
| Performance | Faster (single-thread) | Slower (lock on every op) |
| Iterator | Fail-fast | Fail-fast |
| Parent class | `AbstractMap` | `Dictionary` (legacy) |
| Modern replacement | `ConcurrentHashMap` | `ConcurrentHashMap` |

### Example — HashMap

```java
Map<String, Integer> map = new HashMap<>();
map.put(null, 100);       // ✅ allowed
map.put("key", null);     // ✅ allowed
```

### Example — Hashtable

```java
Map<String, Integer> table = new Hashtable<>();
table.put(null, 100);     // ❌ NullPointerException
table.put("key", null);   // ❌ NullPointerException
```

### When to Use Today

| Scenario | Use |
|----------|-----|
| General purpose | `HashMap` |
| Thread-safe | `ConcurrentHashMap` |
| Legacy code | `Hashtable` — migrate away |

**Interview Point:**

> Hashtable = synchronized HashMap with no nulls. Legacy—use HashMap or ConcurrentHashMap in modern code.

</details>

---

# 2. Why Hashtable is legacy?

<details>
<summary>Show Answer</summary>

**Answer:**

`Hashtable` is considered **legacy** because it was designed before the Collections Framework (Java 1.2) and has been superseded by better alternatives.

### Why Legacy?

| Problem | Detail |
|---------|--------|
| Coarse synchronization | Every method locks entire table |
| Poor scalability | Single lock → threads block each other |
| No null support | Restrictive API |
| Extends `Dictionary` | Old API, not `Map` heritage cleanly |
| Superseded | `ConcurrentHashMap` is far better for threads |

### Performance Problem

```java
Hashtable<String, Integer> table = new Hashtable<>();

// EVERY operation synchronizes on the entire table
table.get("key");  // synchronized
table.put("key", 1); // synchronized — blocks all other threads
```

### Modern Replacements

```java
// Single-threaded
Map<String, Integer> map = new HashMap<>();

// Multi-threaded
Map<String, Integer> map = new ConcurrentHashMap<>();
```

### Still in JDK Because

* Backward compatibility
* Some legacy APIs return `Hashtable`
* `Properties` class extends `Hashtable`

### Properties Example (still uses Hashtable)

```java
Properties props = new Properties();
props.setProperty("db.url", "jdbc:...");
// Properties extends Hashtable<String,Object>
```

**Interview Point:**

> Hashtable survives for backward compatibility only. Never choose it for new code—use ConcurrentHashMap for thread safety.

</details>

---

# 3. Synchronization difference?

<details>
<summary>Show Answer</summary>

**Answer:**

### Hashtable — Method-Level Synchronization

Every public method is **`synchronized`** — locks the **entire table**.

```java
public synchronized V get(Object key) { ... }
public synchronized V put(K key, V value) { ... }
public synchronized V remove(Object key) { ... }
```

```text
Thread 1: put()  → locks entire Hashtable
Thread 2: get()  → WAITS (even different keys)
Thread 3: put()  → WAITS
```

### HashMap — No Synchronization

```java
public V get(Object key) { ... }  // no lock
public V put(K key, V value) { ... } // no lock
```

### ConcurrentHashMap — Fine-Grained Locking

```text
Thread 1: put(keyA) → lock bucket A only
Thread 2: put(keyB) → lock bucket B only (parallel!)
Thread 3: get(keyC) → no lock (read)
```

### Comparison Table

| Map | Lock Scope | Concurrent Reads | Concurrent Writes |
|-----|------------|------------------|-------------------|
| HashMap | None | ✅ Unsafe | ❌ Unsafe |
| Hashtable | Entire map | ✅ (serialized) | 1 at a time |
| synchronizedMap | Entire map | ✅ (serialized) | 1 at a time |
| ConcurrentHashMap | Per bucket | ✅ Lock-free | ✅ Multiple buckets |

### Example — Throughput Difference

```java
// 10 threads writing different keys:

Hashtable ht = new Hashtable<>();
// All threads block each other → poor throughput

ConcurrentHashMap<String, Integer> chm = new ConcurrentHashMap<>();
// Threads write different buckets → high throughput
```

**Interview Point:**

> Hashtable locks the whole map on every operation—bottleneck under concurrency. ConcurrentHashMap locks only conflicting buckets.

</details>

---

## LinkedHashMap

---

# 4. Difference between HashMap and LinkedHashMap?

<details>
<summary>Show Answer</summary>

**Answer:**

| Feature | HashMap | LinkedHashMap |
|---------|---------|---------------|
| Ordering | No guaranteed order | **Insertion order** or **access order** |
| Internal structure | Hash table only | Hash table + **doubly linked list** |
| Performance | Faster | Slightly slower (list overhead) |
| Memory | Less | More (prev/next pointers) |
| `null` key/value | Allowed | Allowed |
| Use case | General lookup | Order-sensitive iteration, LRU cache |

### Example — HashMap (unordered)

```java
Map<String, Integer> map = new HashMap<>();
map.put("Apple", 1);
map.put("Banana", 2);
map.put("Cherry", 3);

System.out.println(map);
// Order not guaranteed — e.g. {Cherry=3, Apple=1, Banana=2}
```

### Example — LinkedHashMap (insertion order)

```java
Map<String, Integer> map = new LinkedHashMap<>();
map.put("Apple", 1);
map.put("Banana", 2);
map.put("Cherry", 3);

System.out.println(map);
// {Apple=1, Banana=2, Cherry=3} — insertion order preserved
```

### When to Choose LinkedHashMap

* Predictable iteration order
* LRU cache implementation
* Logging / audit trails needing insertion sequence

**Interview Point:**

> LinkedHashMap = HashMap + linked list for order. Default choice when iteration order must match insertion or access order.

</details>

---

# 5. How insertion order maintained?

<details>
<summary>Show Answer</summary>

**Answer:**

`LinkedHashMap` extends `HashMap` and adds a **doubly linked list** threading through all entries—in addition to the hash table.

### Internal Structure

```text
Hash Table (buckets)     Doubly Linked List (order)
table[i] → Entry         head → A ↔ B ↔ C → tail
```

### Entry Structure

```java
static class Entry<K,V> extends HashMap.Node<K,V> {
    Entry<K,V> before;  // previous in order
    Entry<K,V> after;   // next in order
}
```

### On put()

```text
1. HashMap put (store in bucket)
2. Link new entry at tail of doubly linked list
3. Update head/tail pointers
```

### Example

```java
LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
map.put("First", 1);
map.put("Second", 2);
map.put("Third", 3);

for (String key : map.keySet()) {
    System.out.println(key);
}
// First → Second → Third (always insertion order)
```

### Visual

```text
put("A") → head: A
put("B") → head: A ↔ B
put("C") → head: A ↔ B ↔ C :tail
```

### Iterator Order

```java
map.keySet().iterator()   // insertion order
map.values().iterator()   // insertion order
map.entrySet().iterator() // insertion order
```

**Interview Point:**

> Doubly linked list across hash entries maintains order. O(1) add with order tracking—slight memory overhead for before/after pointers.

</details>

---

# 6. Access-order mode?

<details>
<summary>Show Answer</summary>

**Answer:**

By default, `LinkedHashMap` uses **insertion-order**. With `accessOrder = true`, it switches to **access-order**—reorders entries on every `get()` or `put()`.

### Constructor

```java
// Insertion order (default)
LinkedHashMap<String, Integer> insertOrder =
        new LinkedHashMap<>();

// Access order
LinkedHashMap<String, Integer> accessOrder =
        new LinkedHashMap<>(16, 0.75f, true);
//                                      ↑ accessOrder = true
```

### Access-Order Behavior

```text
get(key) or put(existingKey) → entry moved to tail (most recently used)
```

### Example

```java
LinkedHashMap<String, Integer> map =
        new LinkedHashMap<>(16, 0.75f, true);

map.put("A", 1);
map.put("B", 2);
map.put("C", 3);

map.get("A"); // access A → A moves to tail

System.out.println(map);
// {B=2, C=3, A=1} — A is last (most recently accessed)
```

### Insertion vs Access Order

| Mode | Order Based On |
|------|----------------|
| Insertion-order (`false`) | When key was first added |
| Access-order (`true`) | When key was last accessed |

### Use Case

Access-order is the foundation for **LRU (Least Recently Used) cache**—oldest accessed entry at head, newest at tail.

**Interview Point:**

> `accessOrder = true` reorders on get/put—entries slide to tail when touched. Head = least recently used.

</details>

---

# 7. LRU Cache implementation using LinkedHashMap?

<details>
<summary>Show Answer</summary>

**Answer:**

LRU cache evicts the **least recently used** entry when capacity is exceeded. `LinkedHashMap` with **access-order** + `removeEldestEntry()` provides a built-in LRU cache.

### LRU Principle

```text
Most recently used → tail
Least recently used → head (evicted when full)
```

### Implementation

```java
class LRUCache<K, V> extends LinkedHashMap<K, V> {

    private final int capacity;

    LRUCache(int capacity) {
        super(capacity, 0.75f, true); // access-order = true
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity; // remove head when over capacity
    }
}
```

### Usage

```java
LRUCache<String, String> cache = new LRUCache<>(3);

cache.put("A", "1");
cache.put("B", "2");
cache.put("C", "3");
// Cache: A → B → C (A is eldest)

cache.get("A"); // access A → A moves to tail
// Cache: B → C → A

cache.put("D", "4"); // capacity exceeded → remove eldest (B)
// Cache: C → A → D
```

### How removeEldestEntry() Works

```text
After every put():
    if size() > capacity
        remove head entry (least recently used)
```

### Production Alternative

```java
// Caffeine / Guava Cache — more features
Cache<String, Object> cache = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(Duration.ofMinutes(10))
        .build();
```

### Why LinkedHashMap LRU Works

| Feature | Role in LRU |
|---------|-------------|
| Access-order | Tracks recency on get/put |
| Doubly linked list | O(1) move to tail |
| removeEldestEntry() | O(1) eviction hook |

**Interview Point:**

> LRU = LinkedHashMap(accessOrder=true) + removeEldestEntry override. Classic interview coding question—know it cold.

</details>

---

## TreeMap

---

# 8. Difference between TreeMap and HashMap?

<details>
<summary>Show Answer</summary>

**Answer:**

| Feature | HashMap | TreeMap |
|---------|---------|---------|
| Ordering | No order | **Sorted** (natural or Comparator) |
| Internal structure | Hash table + list/tree | **Red-Black Tree** |
| `null` key | ✅ One allowed | ❌ Not allowed (natural order) |
| Performance | O(1) average | O(log n) |
| Interface | `Map` | `NavigableMap` |
| Range queries | ❌ | ✅ `headMap`, `tailMap`, `subMap` |
| Use case | Fast general lookup | Sorted keys, range operations |

### Example — HashMap

```java
Map<Integer, String> map = new HashMap<>();
map.put(30, "C");
map.put(10, "A");
map.put(20, "B");

System.out.println(map); // order not guaranteed
```

### Example — TreeMap

```java
Map<Integer, String> map = new TreeMap<>();
map.put(30, "C");
map.put(10, "A");
map.put(20, "B");

System.out.println(map); // {10=A, 20=B, 30=C} — sorted
```

### NavigableMap Operations (TreeMap only)

```java
TreeMap<Integer, String> map = new TreeMap<>();
map.put(10, "A");
map.put(20, "B");
map.put(30, "C");

map.firstKey();           // 10
map.lastKey();            // 30
map.headMap(20);          // keys < 20
map.tailMap(20);          // keys >= 20
```

**Interview Point:**

> HashMap = speed, no order. TreeMap = sorted keys, O(log n), range queries. Pick TreeMap only when sorting or navigation is required.

</details>

---

# 9. Internal structure?

<details>
<summary>Show Answer</summary>

**Answer:**

`TreeMap` is backed by a **Red-Black Tree**—a self-balancing binary search tree.

### Structure

```java
public class TreeMap<K,V> {

    private transient Entry<K,V> root; // tree root
    private final Comparator<? super K> comparator;
    private transient int size;
}
```

### Entry (Tree Node)

```java
static final class Entry<K,V> {
    K key;
    V value;
    Entry<K,V> left;
    Entry<K,V> right;
    Entry<K,V> parent;
    boolean color; // RED or BLACK
}
```

### Visual — Red-Black Tree

```text
        20(B)
       /     \
    10(B)   30(B)
    /  \
  5(R) 15(R)

(B) = Black node  (R) = Red node
Keys always in sorted BST order
```

### Key Properties

| Property | Detail |
|----------|--------|
| BST ordering | Left < parent < right |
| Self-balancing | Color flips + rotations |
| Root | Entry at `root` field |
| No hash table | Pure tree—no buckets |

### Example

```java
TreeMap<String, Integer> map = new TreeMap<>();
map.put("Charlie", 3);
map.put("Alice", 1);
map.put("Bob", 2);

// Internally stored as sorted BST, not hash buckets
for (String key : map.keySet()) {
    System.out.println(key); // Alice, Bob, Charlie
}
```

**Interview Point:**

> TreeMap = Red-Black Tree, not hash table. Every key in sorted tree order—O(log n) all operations.

</details>

---

# 10. Complexity?

<details>
<summary>Show Answer</summary>

**Answer:**

### TreeMap — Time Complexity

| Operation | Complexity |
|-----------|------------|
| `put()` | **O(log n)** |
| `get()` | **O(log n)** |
| `remove()` | **O(log n)** |
| `containsKey()` | **O(log n)** |
| `firstKey()` / `lastKey()` | **O(log n)** |
| `size()` | **O(1)** |
| Iteration | **O(n)** in sorted order |

### TreeMap vs HashMap

| Operation | HashMap | TreeMap |
|-----------|---------|---------|
| `put()` | O(1)* | O(log n) |
| `get()` | O(1)* | O(log n) |
| `remove()` | O(1)* | O(log n) |
| Sorted iteration | O(n log n) sort needed | O(n) natural |

\* Average case for HashMap

### Example — Large Dataset

```java
TreeMap<Integer, String> map = new TreeMap<>();

for (int i = 0; i < 1_000_000; i++) {
    map.put(i, "val" + i); // O(log n) each
}

map.get(500000); // O(log n) ≈ 20 comparisons
```

### Space Complexity

**O(n)** — one tree node per entry (more overhead than HashMap array).

**Interview Point:**

> All TreeMap key operations are O(log n). Trade speed for sorted order and navigable range queries.

</details>

---

# 11. Null key support?

<details>
<summary>Show Answer</summary>

**Answer:**

### Natural Ordering (default) — ❌ No null key

```java
TreeMap<String, Integer> map = new TreeMap<>();
map.put(null, 1); // ❌ NullPointerException
// compareTo() cannot compare null
```

### Null Values — ✅ Allowed

```java
TreeMap<String, Integer> map = new TreeMap<>();
map.put("key", null); // ✅ allowed
```

### With Custom Comparator

Depends on comparator implementation:

```java
TreeMap<String, Integer> map = new TreeMap<>(
        Comparator.nullsFirst(String::compareTo));

map.put(null, 1); // ✅ if comparator handles null
```

### Compare All Maps

| Map | null key | null value |
|-----|----------|------------|
| HashMap | ✅ one | ✅ |
| LinkedHashMap | ✅ one | ✅ |
| TreeMap | ❌ (natural order) | ✅ |
| Hashtable | ❌ | ❌ |
| ConcurrentHashMap | ❌ | ❌ |

### Why TreeMap Rejects null Key

```text
Tree insertion uses compareTo() or Comparator
null.compareTo(anything) → NullPointerException
Cannot determine position in tree for null
```

**Interview Point:**

> TreeMap with natural ordering cannot store null keys. HashMap/LinkedHashMap allow one null key.

</details>

---

# 12. How sorting works?

<details>
<summary>Show Answer</summary>

**Answer:**

`TreeMap` maintains keys in **sorted order** continuously using either **natural ordering** or a **custom Comparator**.

### Method 1 — Natural Ordering (`Comparable`)

```java
TreeMap<String, Integer> map = new TreeMap<>();
map.put("Charlie", 3);
map.put("Alice", 1);
map.put("Bob", 2);

// String.compareTo() → alphabetical
System.out.println(map); // {Alice=1, Bob=2, Charlie=3}
```

```java
TreeMap<Integer, String> map = new TreeMap<>();
map.put(30, "C");
map.put(10, "A");
map.put(20, "B");

System.out.println(map); // {10=A, 20=B, 30=C}
```

### Method 2 — Custom Comparator

```java
TreeMap<String, Integer> map = new TreeMap<>(
        (a, b) -> b.compareTo(a)); // reverse alphabetical

map.put("A", 1);
map.put("B", 2);
map.put("C", 3);

System.out.println(map); // {C=3, B=2, A=1}
```

### How Insert Works

```text
put(key, value)
    ↓
compare key with root using compareTo()/Comparator
    ↓
go left (smaller) or right (larger)
    ↓
insert at correct BST position
    ↓
balance tree (Red-Black color fix + rotations)
```

### Sorted Navigation

```java
TreeMap<Integer, String> map = new TreeMap<>();
map.put(10, "A");
map.put(20, "B");
map.put(30, "C");

map.firstEntry();  // 10=A (smallest)
map.lastEntry();   // 30=C (largest)
map.pollFirstEntry(); // remove & return smallest
```

### Custom Object Sorting

```java
TreeMap<Employee, String> map = new TreeMap<>(
        Comparator.comparingInt(Employee::getSalary));

// Employees sorted by salary in tree
```

**Interview Point:**

> TreeMap sorts on every insert via Comparable or Comparator. Iterator always returns keys in sorted order—no separate sort step needed.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: HashMap vs LinkedHashMap vs TreeMap — quick pick?

<details>
<summary>Show Answer</summary>

**Answer:** HashMap = fast, no order. LinkedHashMap = insertion/access order. TreeMap = sorted keys. Default to HashMap unless order or sorting is required.

</details>

---

### Q: Is LinkedHashMap thread-safe?

<details>
<summary>Show Answer</summary>

**Answer:** No. Use external synchronization or `Collections.synchronizedMap(new LinkedHashMap<>())` for thread safety.

</details>

---

### Q: Can TreeMap have duplicate keys?

<details>
<summary>Show Answer</summary>

**Answer:** No. Duplicate key put overwrites previous value—same as all Map implementations.

</details>

---

### Q: Properties class — why Hashtable?

<details>
<summary>Show Answer</summary>

**Answer:** `Properties` extends `Hashtable` for historical reasons (Java 1.0). It is synchronized and thread-safe for loading config files—though modern apps often use external config libraries.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Hashtable = legacy synchronized map—use ConcurrentHashMap instead. LinkedHashMap adds predictable order and LRU cache via access-order + removeEldestEntry. TreeMap = Red-Black tree for sorted keys and range navigation at O(log n).

</details>
