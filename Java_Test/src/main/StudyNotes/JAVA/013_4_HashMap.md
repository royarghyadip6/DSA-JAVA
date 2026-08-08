# 13.4 HashMap

## HashMap

### Most Asked Topic

---

# 1. How HashMap works internally?

<details>
<summary>Show Answer</summary>

**Answer:**

`HashMap` stores key-value pairs using an **array of buckets** (hash table). Each bucket can hold one or more entries.

### High-Level Flow

```text
put(key, value)
    ↓
key.hashCode() → hash spread → bucket index
    ↓
store in bucket (or chain/tree in bucket)
    ↓
get(key)
    ↓
hash → bucket → equals() match → return value
```

### Internal Structure (Java 8+)

```java
transient Node<K,V>[] table;  // bucket array
int size;
int threshold;                // resize trigger
final float loadFactor;
```

### Example

```java
Map<Integer, String> map = new HashMap<>();
map.put(101, "John");
map.put(102, "Jane");

System.out.println(map.get(101)); // John
```

### Core Idea

| Component | Role |
|-----------|------|
| `hashCode()` | Locate bucket quickly |
| `equals()` | Find exact key in bucket |
| Bucket array | O(1) average access |
| Linked list / Tree | Handle collisions |

**Interview Point:**

> HashMap = hash table + collision handling. Fast lookup because you never scan the entire map—only one bucket chain/tree.

</details>

---

# 2. What is hashing?

<details>
<summary>Show Answer</summary>

**Answer:**

**Hashing** is the process of converting a key into a **numeric hash value** using `hashCode()`, then mapping it to a **bucket index** in the internal array.

### Steps in HashMap

```text
1. key.hashCode()           → integer hash
2. spread hash bits         → reduce collisions
3. index = hash % capacity  → bucket position
```

### Example

```java
String key = "Java";
int hash = key.hashCode();
// HashMap applies additional spreading before index calculation
```

### Purpose

| Without Hashing | With Hashing |
|-----------------|--------------|
| Search all entries O(n) | Jump to bucket O(1) average |
| Linear scan | Direct bucket access |

### Good vs Bad Hashing

```java
// ✅ Good — distributes keys across buckets
@Override
public int hashCode() {
    return Objects.hash(id, name);
}

// ❌ Bad — all keys same bucket
@Override
public int hashCode() {
    return 1;
}
```

**Interview Point:**

> Hashing converts object identity into array index. Quality of `hashCode()` directly determines HashMap performance.

</details>

---

# 3. What is bucket?

<details>
<summary>Show Answer</summary>

**Answer:**

A **bucket** is a **slot** in HashMap's internal array (`table[]`) that can hold zero, one, or multiple key-value entries.

### Visual

```text
table (bucket array):

Index:  0      1      2      3
       [ ] → [K1,V1] [ ] → [K2,V2]→[K3,V3]
              bucket1       bucket3 (collision chain)
```

### Bucket Contents

| State | Content |
|-------|---------|
| Empty | `null` |
| Single entry | One `Node` |
| Collision | Linked list of `Node`s |
| Many collisions (Java 8+) | Red-Black `TreeNode`s |

### Bucket Index Calculation

```text
index = (n - 1) & hash

where n = table length (power of 2)
```

### Example

```java
Map<String, Integer> map = new HashMap<>();
map.put("A", 1);
map.put("B", 2);

// "A" and "B" may land in same or different buckets
// depending on hashCode() and table size
```

**Interview Point:**

> Bucket = array slot. Collisions mean multiple keys share one bucket—handled by linked list or tree inside that bucket.

</details>

---

# 4. How key-value pair stored?

<details>
<summary>Show Answer</summary>

**Answer:**

Each entry is stored as a **`Node<K,V>`** object containing:

```java
static class Node<K,V> {
    final int hash;
    final K key;
    V value;
    Node<K,V> next;  // for collision chain
}
```

### Storage Flow (put)

```text
1. Compute hash of key
2. Find bucket index
3. Create Node(key, value, hash)
4. If bucket empty → store Node
5. If collision → append to chain or tree
```

### Example

```java
Map<Integer, String> map = new HashMap<>();
map.put(101, "John");

// Internally stored as:
// Node { hash=..., key=101, value="John", next=null }
```

### Memory Layout

```text
Bucket[index]
    ↓
Node { key, value, hash, next }
    ↓
Node { key, value, hash, next }  ← collision chain
```

### TreeNode (Java 8+ — heavy collisions)

```java
static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {
    TreeNode<K,V> parent;
    TreeNode<K,V> left;
    TreeNode<K,V> right;
    // Red-Black tree structure
}
```

**Interview Point:**

> Key-value pairs are `Node` objects in bucket arrays—not stored as parallel arrays. Value is inside the Node alongside the key.

</details>

---

# 5. Why key should be immutable?

<details>
<summary>Show Answer</summary>

**Answer:**

If a **mutable key** changes after insertion, its `hashCode()` may change → HashMap **cannot find** the entry in the original bucket.

### Problem Scenario

```java
class MutableKey {
    int id;
    MutableKey(int id) { this.id = id; }

    @Override
    public int hashCode() { return id; }

    @Override
    public boolean equals(Object o) {
        return id == ((MutableKey) o).id;
    }
}

Map<MutableKey, String> map = new HashMap<>();
MutableKey key = new MutableKey(101);
map.put(key, "John");

key.id = 202; // ❌ key mutated after insert

System.out.println(map.get(key)); // null — lost in wrong bucket!
```

### Why It Breaks

```text
Insert: hashCode based on id=101 → bucket 5
Mutate:  id becomes 202
Get:     hashCode based on id=202 → bucket 12
         Entry still in bucket 5 → NOT FOUND
```

### Best Immutable Keys

| Key Type | Why Safe |
|----------|----------|
| `String` | Immutable |
| `Integer`, `Long` | Immutable wrappers |
| Custom immutable class | `final` fields, no setters |

**Interview Point:**

> Mutable keys break hash bucket location. Immutable keys guarantee stable `hashCode()` → reliable retrieval.

</details>

---

# 6. Can HashMap have null key?

<details>
<summary>Show Answer</summary>

**Answer:**

**Yes.** `HashMap` allows **exactly one `null` key**.

### Example

```java
Map<String, Integer> map = new HashMap<>();
map.put(null, 100);
map.put("A", 200);

System.out.println(map.get(null)); // 100
System.out.println(map.size());    // 2
```

### Internal Handling

```text
null key → hash = 0 → always goes to bucket index 0
```

### Second null Key

```java
map.put(null, 100);
map.put(null, 200); // overwrites previous null key value

System.out.println(map.get(null)); // 200
```

### Compare with ConcurrentHashMap

```java
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
map.put(null, 1); // ❌ NullPointerException
```

**Interview Point:**

> One null key allowed (hash = 0, bucket 0). `ConcurrentHashMap` does not allow null keys or values.

</details>

---

# 7. Can HashMap have null values?

<details>
<summary>Show Answer</summary>

**Answer:**

**Yes.** `HashMap` allows **multiple `null` values**.

### Example

```java
Map<String, String> map = new HashMap<>();
map.put("A", null);
map.put("B", null);
map.put("C", "Java");

System.out.println(map.get("A")); // null
System.out.println(map.get("B")); // null
System.out.println(map.size());   // 3
```

### Null Key + Null Value

```java
map.put(null, null); // ✅ allowed
```

### containsKey vs get with null value

```java
map.put("key", null);

map.containsKey("key"); // true — key exists
map.get("key");         // null — value is null
map.get("missing");     // null — key doesn't exist

// ⚠️ get() returns null for both cases — use containsKey() to distinguish
```

**Interview Point:**

> Multiple null values OK. `get()` returning null is ambiguous—use `containsKey()` when null values are possible.

</details>

---

# 8. Time complexity of get()?

<details>
<summary>Show Answer</summary>

**Answer:**

| Case | Complexity |
|------|------------|
| Average | **O(1)** |
| Worst case (Java 7) | **O(n)** — long linked list in bucket |
| Worst case (Java 8+) | **O(log n)** — treeified bucket |

### Why O(1) Average?

```text
hashCode() → bucket index → few equals() checks in chain
```

### Worst Case

```java
// Bad hashCode — all keys same bucket
@Override
public int hashCode() { return 1; }

// get() scans entire chain/tree in one bucket → O(n) or O(log n)
```

### Operations Summary

| Operation | Average | Worst (Java 8+) |
|-----------|---------|-----------------|
| `get()` | O(1) | O(log n) |
| `put()` | O(1) | O(log n) |
| `remove()` | O(1) | O(log n) |
| `containsKey()` | O(1) | O(log n) |

**Interview Point:**

> O(1) average assumes good hash distribution. Treeification caps worst case at O(log n) in Java 8+.

</details>

---

### Intermediate

---

# 9. What happens during put()?

<details>
<summary>Show Answer</summary>

**Answer:**

### put(key, value) Step-by-Step

```text
1. If table empty → initialize (default capacity 16)
2. Compute hash from key.hashCode()
3. Calculate bucket index
4. If bucket empty → create Node, insert
5. If bucket not empty:
   a. Traverse chain/tree
   b. If key exists (equals) → update value, return old value
   c. If collision → add to end of chain (or tree)
   d. If chain size ≥ 8 → treeify (Java 8+)
6. Increment size
7. If size > threshold → resize (double capacity)
```

### Example

```java
Map<String, Integer> map = new HashMap<>();
map.put("Java", 1);    // new entry
map.put("Java", 2);    // update existing key

System.out.println(map.get("Java")); // 2
```

### Return Value

```java
Integer old = map.put("Java", 3);
System.out.println(old); // 2 — previous value
```

### Resize Trigger

```text
if (size > threshold) → resize()
threshold = capacity × loadFactor (default 0.75)
```

**Interview Point:**

> put() = hash → bucket → equals check → insert/update → maybe resize. Know the full flow for senior interviews.

</details>

---

# 10. How equals() and hashCode() are used?

<details>
<summary>Show Answer</summary>

**Answer:**

HashMap uses **both** methods together—never one alone.

### Role of Each

| Method | Role | When Used |
|--------|------|-----------|
| `hashCode()` | Find **bucket** | First — fast filter |
| `equals()` | Find **exact key** | Second — in bucket chain/tree |

### get() Flow

```text
1. hash = key.hashCode()
2. bucket = table[hash % capacity]
3. For each Node in bucket:
      if (node.hash == hash && node.key.equals(key))
          return node.value
4. return null
```

### Example

```java
String k1 = new String("Java");
String k2 = new String("Java");

Map<String, Integer> map = new HashMap<>();
map.put(k1, 100);

System.out.println(map.get(k2)); // 100
// hashCode() same → same bucket
// equals() true → match found
```

### Contract (Critical)

```text
If a.equals(b) == true  → a.hashCode() == b.hashCode() MUST be true
```

### Broken Contract

```java
// equals() true but hashCode() different → entry lost
class BadKey {
    int id;
    @Override public boolean equals(Object o) { ... }
    // forgot hashCode() → broken HashMap behavior
}
```

**Interview Point:**

> hashCode() = bucket location. equals() = identity confirmation. Broken contract = silent data loss.

</details>

---

# 11. Collision handling in HashMap?

<details>
<summary>Show Answer</summary>

**Answer:**

A **collision** occurs when two different keys map to the **same bucket**.

### Handling Strategy

| Java Version | Collision Handling |
|--------------|-------------------|
| Java 7 | **Linked list** in bucket (head insertion) |
| Java 8+ | Linked list → **Red-Black Tree** when chain grows |

### Java 8+ Flow

```text
Collision detected
    ↓
Append to linked list in bucket
    ↓
If chain length ≥ TREEIFY_THRESHOLD (8)
    AND table capacity ≥ MIN_TREEIFY_CAPACITY (64)
    ↓
Convert linked list → Red-Black Tree
```

### Example — Collision

```java
String s1 = "FB";
String s2 = "Ea";

System.out.println(s1.hashCode()); // 2236
System.out.println(s2.hashCode()); // 2236 — same hash!

Map<String, Integer> map = new HashMap<>();
map.put(s1, 1);
map.put(s2, 2); // collision — both stored via equals()
```

### Collision Resolution Methods (General CS)

| Method | Used in HashMap? |
|--------|------------------|
| Chaining (linked list/tree) | ✅ Yes |
| Open addressing | ❌ No |

**Interview Point:**

> Collisions don't break HashMap—`equals()` distinguishes keys in same bucket. Java 8 treeification prevents O(n) chains.

</details>

---

# 12. What is load factor?

<details>
<summary>Show Answer</summary>

**Answer:**

**Load factor** is the threshold ratio that determines when HashMap should **resize** (expand internal array).

```text
Load Factor = threshold / capacity
```

### Meaning

| Load Factor | Interpretation |
|-------------|----------------|
| 0.75 | Resize when 75% of buckets are occupied |
| Higher (e.g. 0.9) | Less memory, more collisions |
| Lower (e.g. 0.5) | More memory, fewer collisions |

### Example

```java
Map<String, Integer> map = new HashMap<>(16, 0.75f);
// capacity = 16, load factor = 0.75
// threshold = 16 × 0.75 = 12
// resize when size > 12
```

### Trade-off

```text
Higher load factor → less memory, more collisions, slower ops
Lower load factor  → more memory, fewer collisions, faster ops
```

**Interview Point:**

> Load factor balances **memory vs performance**. Default 0.75 is JVM's tuned sweet spot.

</details>

---

# 13. Default load factor?

<details>
<summary>Show Answer</summary>

**Answer:**

The default load factor of `HashMap` is **0.75** (75%).

```java
public HashMap() {
    this.loadFactor = DEFAULT_LOAD_FACTOR; // 0.75
}
```

### What It Means

```text
Default capacity = 16
Default threshold = 16 × 0.75 = 12

Resize when 13th element is added
```

### Custom Load Factor

```java
HashMap<String, Integer> map = new HashMap<>(16, 0.5f);
// Resizes earlier — at 8 elements (16 × 0.5)
```

**Interview Point:**

> Default load factor = **0.75**. Resize triggers at `capacity × 0.75` entries.

</details>

---

# 14. Default initial capacity?

<details>
<summary>Show Answer</summary>

**Answer:**

The default initial capacity of `HashMap` is **16** buckets.

```java
HashMap<String, Integer> map = new HashMap<>();
// Internal table size = 16 (power of 2)
```

### Custom Initial Capacity

```java
HashMap<String, Integer> map = new HashMap<>(100);
// JVM rounds to next power of 2 → 128
```

### Why Power of 2?

```text
index = (n - 1) & hash

Bitwise AND is faster than modulo division
n must be power of 2 for this optimization
```

### Production Tip

```java
// If you expect ~1000 entries:
int capacity = (int) (1000 / 0.75) + 1;
Map<String, Object> map = new HashMap<>(capacity);
// Avoids multiple resize operations
```

**Interview Point:**

> Default capacity = **16**. Always power of 2 internally. Set initial capacity to avoid resize churn.

</details>

---

# 15. What is threshold?

<details>
<summary>Show Answer</summary>

**Answer:**

**Threshold** is the maximum number of entries HashMap can hold **before resizing**.

```text
threshold = capacity × loadFactor
```

### Default Example

```text
capacity    = 16
loadFactor  = 0.75
threshold   = 12

When size > 12 → resize to capacity 32
New threshold = 32 × 0.75 = 24
```

### Internal Field

```java
int threshold;  // resize trigger point
int size;       // current entry count

if (++size > threshold) resize();
```

### Example

```java
Map<Integer, String> map = new HashMap<>(); // threshold = 12

for (int i = 0; i < 13; i++) {
    map.put(i, "val" + i); // 13th insert triggers resize
}
```

**Interview Point:**

> Threshold = resize trigger = `capacity × loadFactor`. Default: 12 entries before first resize.

</details>

---

### Advanced

---

# 16. HashMap internal structure in Java 7?

<details>
<summary>Show Answer</summary>

**Answer:**

### Java 7 Structure

```text
Entry<K,V>[] table   ← array of buckets

Each Entry:
  K key
  V value
  int hash
  Entry<K,V> next    ← singly linked list
```

### Collision Handling

* **Singly linked list** in each bucket
* **Head insertion** for new collisions (changed in Java 8)

### Resize

* Double capacity
* **Rehash** all entries into new table
* All entries redistributed to new buckets

### Known Problem — Infinite Loop (Java 7)

When two threads resize concurrently:

```text
Thread 1 resize: A → B → A (circular linked list)
Thread 2 resize: infinite loop in get()
```

```text
⚠️ HashMap is NOT thread-safe
Concurrent resize in Java 7 → circular list → CPU hang
```

### Visual (Java 7)

```text
table[5] → Entry(k1) → Entry(k2) → Entry(k3)
           (head insertion — reversed order on resize)
```

**Interview Point:**

> Java 7 = array + linked lists + head insertion. Famous infinite loop bug on concurrent resize—why HashMap is not thread-safe.

</details>

---

# 17. HashMap internal structure in Java 8?

<details>
<summary>Show Answer</summary>

**Answer:**

### Java 8+ Structure

```text
Node<K,V>[] table

Node types:
  Node         → regular entry (linked list)
  TreeNode     → Red-Black tree node (heavy collisions)
```

### Key Improvements over Java 7

| Feature | Java 7 | Java 8+ |
|---------|--------|---------|
| Collision chain | Linked list (head insert) | Linked list (tail insert) |
| Long chains | O(n) search | Treeify to O(log n) |
| Infinite loop risk | Yes (concurrent resize) | Reduced (tail insertion) |
| Node types | Entry only | Node + TreeNode |

### Structure Visual

```text
table[i] → Node → Node → Node        (short chain)
table[j] → TreeNode (RB Tree)        (long chain treeified)
```

### TreeNode Fields

```java
TreeNode parent, left, right, prev;
boolean red;
```

### When Tree Used

```text
Chain length ≥ 8 AND table capacity ≥ 64 → treeify
Chain length ≤ 6 → untreeify back to list
```

**Interview Point:**

> Java 8+ = array + linked list + Red-Black tree hybrid. Major performance improvement for hash collision attacks and bad hashCode().

</details>

---

# 18. What is Treeification?

<details>
<summary>Show Answer</summary>

**Answer:**

**Treeification** is the process of converting a bucket's **linked list** into a **Red-Black Tree** when the chain grows too long.

### When Treeification Happens

```text
Conditions (both required):
1. Chain length ≥ TREEIFY_THRESHOLD (8)
2. Table capacity ≥ MIN_TREEIFY_CAPACITY (64)
```

If capacity < 64 but chain ≥ 8 → **resize** instead of treeify.

### Why Treeify?

| Linked List | Red-Black Tree |
|-------------|----------------|
| O(n) search in bucket | O(log n) search |
| Vulnerable to collision attacks | Bounded worst case |

### Example Scenario

```java
// Many keys collide in same bucket
for (int i = 0; i < 10; i++) {
    map.put(collidingKey(i), i);
}
// After 8 entries in same bucket → treeified
```

### Untreeify

When entries removed and tree size ≤ `UNTREEIFY_THRESHOLD` (6):

```text
Red-Black Tree → linked list again
```

**Interview Point:**

> Treeification = collision chain → Red-Black tree at 8 nodes. Caps worst-case get/put at O(log n).

</details>

---

# 19. Why Red Black Tree introduced?

<details>
<summary>Show Answer</summary>

**Answer:**

Red-Black Tree was introduced in Java 8 to solve **performance degradation** from long collision chains.

### Problem Before Java 8

```text
Bad hashCode() or malicious input
    ↓
All keys in one bucket
    ↓
Linked list of length n
    ↓
get() becomes O(n) — HashMap behaves like linked list
```

### Security Issue — Hash Collision Attack

Attackers could craft keys with same hash → DoS via CPU exhaustion.

### Red-Black Tree Benefits

| Benefit | Detail |
|---------|--------|
| O(log n) worst case | Bounded search time |
| Self-balancing | No skewed tree |
| Security | Mitigates collision attacks |
| Performance | Better than long linked lists |

### Complexity Comparison

```text
Chain of 1000 entries:
  Linked list: O(1000) comparisons
  RB Tree:     O(log 1000) ≈ 10 comparisons
```

### Why Not Always Tree?

| Structure | Overhead |
|-----------|----------|
| Linked list | Low memory, fast for short chains |
| RB Tree | Higher memory, tree maintenance cost |

Tree only when chain is long enough to justify overhead.

**Interview Point:**

> RB Tree prevents O(n) degradation and hash flooding attacks. Used only when collision chain exceeds threshold.

</details>

---

# 20. TREEIFY_THRESHOLD value?

<details>
<summary>Show Answer</summary>

**Answer:**

`TREEIFY_THRESHOLD` = **8**

```java
static final int TREEIFY_THRESHOLD = 8;
```

### Meaning

When a bucket's linked list reaches **8 nodes**, treeification is considered.

### Full Condition

```text
if (chain length ≥ 8 AND table capacity ≥ 64)
    → treeify bucket
else if (chain length ≥ 8 AND capacity < 64)
    → resize table (double capacity)
```

### Example

```text
Bucket chain: 7 nodes → linked list
Add 8th node → treeify (if capacity ≥ 64)
```

**Interview Point:**

> TREEIFY_THRESHOLD = **8**. Know it pairs with MIN_TREEIFY_CAPACITY = 64.

</details>

---

# 21. UNTREEIFY_THRESHOLD value?

<details>
<summary>Show Answer</summary>

**Answer:**

`UNTREEIFY_THRESHOLD` = **6**

```java
static final int UNTREEIFY_THRESHOLD = 6;
```

### Meaning

When a treeified bucket shrinks to **6 or fewer** nodes, it converts back to a **linked list**.

### Why 6 and Not 8?

```text
TREEIFY_THRESHOLD  = 8  (treeify when growing)
UNTREEIFY_THRESHOLD = 6  (untreeify when shrinking)

Gap prevents frequent flip between list ↔ tree
```

### Example

```text
Tree with 7 nodes → still tree
Remove entries → 6 nodes → untreeify to linked list
```

**Interview Point:**

> UNTREEIFY_THRESHOLD = **6**. Hysteresis gap (8 vs 6) avoids constant treeify/untreeify oscillation.

</details>

---

# 22. Resize process?

<details>
<summary>Show Answer</summary>

**Answer:**

**Resize** doubles the internal table capacity and **rehashes** all existing entries.

### Trigger

```text
size > threshold
threshold = capacity × loadFactor
```

### Resize Steps

```text
1. Create new table with 2× capacity
2. For each entry in old table:
   a. Recalculate bucket index (new capacity)
   b. Insert into new table
3. Replace old table with new table
4. Update threshold = newCapacity × loadFactor
```

### Example — Default Growth

```text
Initial:  capacity = 16, threshold = 12
Resize 1: capacity = 32, threshold = 24
Resize 2: capacity = 64, threshold = 48
```

### Cost

**O(n)** — must rehash every entry. Expensive for large maps.

### Java 8 Optimization

```text
On resize, nodes in same bucket may stay in same bucket
(split into lo and hi chains — no full rehash of tree structure)
```

### Production Impact

```java
// ❌ Bad — causes many resizes
Map<String, Object> map = new HashMap<>();
for (int i = 0; i < 100000; i++) map.put("key" + i, i);

// ✅ Better — pre-size
Map<String, Object> map = new HashMap<>(131072);
```

**Interview Point:**

> Resize = double capacity + rehash all entries O(n). Pre-size HashMap to avoid resize storms in production.

</details>

---

# 23. Rehashing?

<details>
<summary>Show Answer</summary>

**Answer:**

**Rehashing** is recalculating the **bucket index** for every entry when the table capacity changes (resize).

### Why Rehashing Is Needed

```text
index = (capacity - 1) & hash

When capacity changes 16 → 32:
  Same hash → DIFFERENT bucket index
  All entries must be redistributed
```

### Example

```text
hash = 21

capacity = 16: index = 21 & 15 = 5
capacity = 32: index = 21 & 31 = 21

Same entry moves from bucket 5 to bucket 21
```

### Rehashing Process

```text
old table[0..n] → for each entry:
    newIndex = hash & (newCapacity - 1)
    place in new table[newIndex]
```

### Cost

* **O(n)** for all entries during resize
* Can cause latency spike in production

### Java 8 Improvement — Bin Splitting

```text
On resize, entries in a bucket split into:
  lo chain (stays in same index)
  hi chain (index + oldCapacity)

Avoids full re-evaluation for tree nodes
```

**Interview Point:**

> Rehashing redistributes all entries after resize. O(n) cost—why initial capacity tuning matters at scale.

</details>

---

# 24. How HashMap avoids infinite loop issue from Java 7?

<details>
<summary>Show Answer</summary>

**Answer:**

Java 7 had a famous bug: **concurrent resize** by multiple threads created **circular linked lists** → infinite loop in `get()`.

### Java 7 Problem

```text
Thread 1 resizes: reverses linked list during transfer
Thread 2 resizes: creates circular reference A → B → A
get() loops forever → CPU at 100%
```

```text
⚠️ Root cause: HashMap is NOT thread-safe
Head insertion during resize + concurrent access
```

### Java 8 Fixes

| Fix | Detail |
|-----|--------|
| **Tail insertion** | New nodes added at end of chain (not head) |
| **Treeification** | Long chains become trees—no circular list possible |
| **Improved resize** | Lo/hi bin splitting reduces transfer issues |

### Important — Still Not Thread-Safe!

```java
// Java 8 fixes infinite loop in resize transfer logic
// BUT HashMap is STILL not thread-safe for concurrent put/get

Map<String, Integer> map = new HashMap<>();
// Two threads putting simultaneously → data loss, corruption possible
```

### Use ConcurrentHashMap for Threads

```java
Map<String, Integer> map = new ConcurrentHashMap<>();
// Thread-safe — no resize infinite loop
```

**Interview Point:**

> Java 8 fixed the resize transfer infinite loop (tail insert + treeify), but HashMap remains **not thread-safe**. Use `ConcurrentHashMap` for multi-threaded access.

</details>

---

### Production Questions

---

# 25. How would you optimize a large HashMap?

<details>
<summary>Show Answer</summary>

**Answer:**

### 1. Set Initial Capacity

```java
int expectedSize = 100000;
int capacity = (int) (expectedSize / 0.75) + 1;
Map<String, Object> map = new HashMap<>(capacity);
// Avoids repeated resize + rehash
```

### 2. Good hashCode() Implementation

```java
@Override
public int hashCode() {
    return Objects.hash(id, department); // well-distributed
}
// Avoid: return 1; or poor distribution
```

### 3. Use Immutable Keys

```java
// String, Integer, or immutable custom keys
// Prevents bucket location corruption
```

### 4. Right Load Factor

```java
// Default 0.75 is usually optimal
// Lower (0.5) if collision-heavy workload
Map<K,V> map = new HashMap<>(1024, 0.75f);
```

### 5. Choose Right Map Type

| Need | Use |
|------|-----|
| Thread-safe | `ConcurrentHashMap` |
| Insertion order | `LinkedHashMap` |
| Sorted keys | `TreeMap` |
| General purpose | `HashMap` |

### 6. Avoid Unnecessary Operations

```java
// ❌ Slow
if (map.containsKey(k)) map.put(k, v);
else map.put(k, defaultVal);

// ✅ Better
map.putIfAbsent(k, defaultVal); // Java 8+
map.computeIfAbsent(k, k -> loadValue(k));
```

### 7. Memory — Weak/Soft References for Caches

```java
Map<String, Object> cache = new WeakHashMap<>();
// Entries GC'd when keys no longer referenced
```

### 8. Monitor Resize Events

Large resize causes latency spikes—pre-size to avoid in hot paths.

**Interview Point:**

> Pre-size capacity, good hashCode, immutable keys, right concurrent map—four pillars of production HashMap optimization.

</details>

---

# 26. Why immutable keys are recommended?

<details>
<summary>Show Answer</summary>

**Answer:**

Immutable keys guarantee **stable `hashCode()`** and **`equals()`** throughout the entry's lifetime in HashMap.

### Problems with Mutable Keys

| Problem | Consequence |
|---------|-------------|
| hashCode changes | Entry in wrong bucket — `get()` returns null |
| equals() changes | Duplicate keys or lost entries |
| Silent failures | No exception—data appears "missing" |

### Example — Production Bug

```java
class UserKey {
    String userId; // mutable!

    void setUserId(String id) { this.userId = id; }

    @Override public int hashCode() { return userId.hashCode(); }
    @Override public boolean equals(Object o) { ... }
}

UserKey key = new UserKey("U001");
sessionCache.put(key, session);

key.setUserId("U002"); // mutated after put

sessionCache.get(key); // null — session "lost"
```

### Recommended Immutable Keys

```java
// ✅ String — immutable, excellent hashCode
map.put("userId", value);

// ✅ Integer/Long — immutable
map.put(101, employee);

// ✅ Immutable custom key
public final class UserId {
    private final String id;
    public UserId(String id) { this.id = id; }
    // final fields, no setters, hashCode/equals on id
}
```

### String as HashMap Key — Why Perfect

* Immutable
* Cached hashCode (Java String optimization)
* Well-distributed hash function
* Used everywhere in production (caches, configs, lookups)

**Interview Point:**

> Immutable keys = stable bucket location. Mutable keys cause silent retrieval failures—one of the most common HashMap production bugs.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Is HashMap thread-safe?

<details>
<summary>Show Answer</summary>

**Answer:** No. Use `ConcurrentHashMap` for concurrent access. `Collections.synchronizedMap()` wraps but locks entire map—poor scalability.

</details>

---

### Q: HashMap vs Hashtable?

<details>
<summary>Show Answer</summary>

**Answer:** `HashMap` — not synchronized, allows one null key, faster. `Hashtable` — synchronized on every method, no null keys/values, legacy.

</details>

---

### Q: What is MIN_TREEIFY_CAPACITY?

<details>
<summary>Show Answer</summary>

**Answer:** **64** — minimum table capacity before treeification is allowed. Below 64, resize instead of treeify.

</details>

---

### Q: Can we iterate HashMap safely while another thread modifies it?

<details>
<summary>Show Answer</summary>

**Answer:** No with `HashMap` — `ConcurrentModificationException` or undefined behavior. Use `ConcurrentHashMap` or external synchronization.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> HashMap uses hashCode for bucket lookup and equals for key matching. Java 8 treeifies long chains to O(log n). Pre-size capacity, use immutable keys, and choose ConcurrentHashMap for thread safety—never assume HashMap is safe under concurrent access.

</details>
