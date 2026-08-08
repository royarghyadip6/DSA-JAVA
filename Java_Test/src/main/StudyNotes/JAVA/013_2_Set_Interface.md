# 13.2 Set Interface

## Set Interface

### HashSet

---

# 1. How HashSet works internally?

<details>
<summary>Show Answer</summary>

**Answer:**

`HashSet` stores unique elements using **hashing** internally. It does **not** implement its own hash table—it delegates to a **`HashMap`**.

### Internal Flow (add element)

```text
1. Calculate hashCode() of element
2. Determine bucket index in internal HashMap
3. If bucket empty → store element as KEY
4. If bucket has entries → compare using equals()
5. If duplicate found → ignore (Set contract)
6. If not duplicate → add to bucket (handle collision)
```

### Simplified Internal Representation

```java
// Conceptual — HashSet internally
private transient HashMap<E, Object> map;
private static final Object PRESENT = new Object();

public boolean add(E e) {
    return map.put(e, PRESENT) == null;
}
```

### Example

```java
Set<String> set = new HashSet<>();
set.add("Java");
set.add("Python");
set.add("Java"); // duplicate — not added

System.out.println(set); // [Java, Python] (order not guaranteed)
```

**Interview Point:**

> HashSet = HashMap where **element is the key** and value is a dummy constant. Uniqueness comes from HashMap key uniqueness.

</details>

---

# 2. Does HashSet allow duplicates?

<details>
<summary>Show Answer</summary>

**Answer:**

**No.** `HashSet` does **not** allow duplicate elements.

### Example

```java
Set<Integer> set = new HashSet<>();
set.add(10);
set.add(20);
set.add(10); // duplicate

System.out.println(set.size()); // 2
System.out.println(set);        // [10, 20]
```

### add() Return Value

```java
boolean added = set.add(10);
System.out.println(added); // false — already present

boolean added2 = set.add(30);
System.out.println(added2); // true — new element
```

### Set Contract

* No duplicate elements
* At most one `null` (if allowed by implementation)
* Uniqueness based on `equals()` — not `==`

**Interview Point:**

> Duplicates are rejected silently—`add()` returns `false` when element already exists.

</details>

---

# 3. Why duplicates are not allowed?

<details>
<summary>Show Answer</summary>

**Answer:**

`Set` interface defines a **mathematical set**—a collection with **no duplicate members**.

### How HashSet Enforces Uniqueness

1. Compute `hashCode()` of new element
2. Locate bucket in internal `HashMap`
3. Compare with existing keys using `equals()`
4. If `equals()` returns `true` → element already exists → **do not add**

### Example — Same Logical Value

```java
Set<String> set = new HashSet<>();

String s1 = new String("Java");
String s2 = new String("Java");

set.add(s1);
set.add(s2); // equals() says same content

System.out.println(set.size()); // 1
```

### Custom Objects — Must Override equals/hashCode

```java
class Employee {
    int id;
    // equals() and hashCode() based on id
}

Set<Employee> employees = new HashSet<>();
employees.add(new Employee(101));
employees.add(new Employee(101)); // duplicate if equals/hashCode correct
```

### Without Proper equals/hashCode

```java
// ❌ Broken — treats same id as different objects
class BadEmployee {
    int id;
    // no equals/hashCode override
}
// Both added — duplicates allowed incorrectly!
```

**Interview Point:**

> Uniqueness depends on **equals() + hashCode() contract**. Broken contract = broken Set behavior.

</details>

---

# 4. Which methods are used internally?

<details>
<summary>Show Answer</summary>

**Answer:**

`HashSet` relies on these key methods during add/search/remove:

### On Element (stored as HashMap key)

| Method | Purpose |
|--------|---------|
| `hashCode()` | Find bucket index |
| `equals(Object)` | Confirm duplicate vs collision |
| `hashCode()` + `equals()` | Full lookup chain |

### HashMap Internal Operations

| Operation | Internal Call |
|-----------|---------------|
| `add(e)` | `map.put(e, PRESENT)` |
| `remove(e)` | `map.remove(e)` |
| `contains(e)` | `map.containsKey(e)` |
| `size()` | `map.size()` |

### Flow for contains()

```text
element.hashCode()
      ↓
bucket index
      ↓
traverse bucket chain
      ↓
element.equals(existingKey)?
      ↓
true → found | false → not found
```

### Example

```java
Set<String> set = new HashSet<>();
set.add("Java");

// Internally uses:
// "Java".hashCode() → bucket
// "Java".equals(existing) → match check
System.out.println(set.contains("Java")); // true
```

**Interview Point:**

> Always `hashCode()` first for speed, then `equals()` for accuracy. Never skip `hashCode()` override when overriding `equals()`.

</details>

---

# 5. How HashSet uses HashMap?

<details>
<summary>Show Answer</summary>

**Answer:**

`HashSet` is a **thin wrapper** around `HashMap<E, Object>`.

### Internal Structure

```java
public class HashSet<E> {

    private transient HashMap<E, Object> map;

  // Dummy value — only keys matter
    private static final Object PRESENT = new Object();

    public HashSet() {
        map = new HashMap<>();
    }

    public boolean add(E e) {
        return map.put(e, PRESENT) == null;
    }

    public boolean remove(Object o) {
        return map.remove(o) != null;
    }

    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    public int size() {
        return map.size();
    }
}
```

### Why This Design?

| Benefit | Explanation |
|---------|-------------|
| Code reuse | No duplicate hash table implementation |
| Consistency | Same collision/resize behavior as HashMap |
| Maintenance | Bug fixes in HashMap benefit HashSet |

### Mapping Concept

```text
HashSet Element  →  HashMap KEY
(dummy PRESENT)  →  HashMap VALUE (ignored)
```

### Example

```java
Set<String> languages = new HashSet<>();
languages.add("Java");

// Internally equivalent to:
// map.put("Java", PRESENT);
```

**Interview Point:**

> HashSet stores elements as **HashMap keys** with a static dummy value—classic composition over reimplementation.

</details>

---

# 6. Can HashSet contain null?

<details>
<summary>Show Answer</summary>

**Answer:**

**Yes.** `HashSet` allows **one `null` element**.

Because internal `HashMap` allows **one `null` key**.

### Example

```java
Set<String> set = new HashSet<>();
set.add(null);
set.add("Java");

System.out.println(set.contains(null)); // true
System.out.println(set.size());         // 2
```

### Second null Attempt

```java
Set<String> set = new HashSet<>();
set.add(null);
set.add(null); // duplicate null — not added

System.out.println(set.size()); // 1
```

### Compare with TreeSet

```java
Set<String> treeSet = new TreeSet<>();
treeSet.add(null); // ❌ NullPointerException (natural ordering)
```

**Interview Point:**

> HashSet allows one `null` (HashMap null key rule). TreeSet generally rejects `null` when using natural ordering.

</details>

---

# 7. How many nulls can be stored?

<details>
<summary>Show Answer</summary>

**Answer:**

`HashSet` can store **exactly one `null` element**.

### Reason

* Internal `HashMap` allows only **one `null` key**
* `HashSet` element = `HashMap` key
* Second `null` is a **duplicate**

### Example

```java
Set<String> set = new HashSet<>();

set.add(null);
System.out.println(set.size()); // 1

boolean added = set.add(null);
System.out.println(added);      // false — duplicate
System.out.println(set.size()); // still 1
```

### Visual

```text
HashSet: [ null, "A", "B" ]  ✅ one null allowed
HashSet: [ null, null ]      ❌ second null rejected
```

**Interview Point:**

> One null maximum—treat null as a valid unique element, not multiple slots.

</details>

---

# 8. Time complexity of add()?

<details>
<summary>Show Answer</summary>

**Answer:**

| Case | Complexity |
|------|------------|
| Average | **O(1)** |
| Worst case | **O(n)** — all elements hash to same bucket |
| With resize | Occasional O(n) when internal HashMap resizes |

### Why O(1) Average?

```text
hashCode() → bucket index → add to bucket
```

No full scan of entire set required.

### Worst Case — Bad hashCode()

```java
// All objects collide → single bucket → linked list / tree
// add() becomes O(n) or O(log n) depending on Java version
```

### Example

```java
Set<Integer> set = new HashSet<>();

for (int i = 0; i < 1000000; i++) {
    set.add(i); // O(1) average per add
}
```

### Operations Summary

| Operation | Average | Worst |
|-----------|---------|-------|
| `add()` | O(1) | O(n) |
| `remove()` | O(1) | O(n) |
| `contains()` | O(1) | O(n) |

**Interview Point:**

> O(1) average assumes good hash distribution. Poor `hashCode()` implementation degrades to linear search in bucket chain.

</details>

---

# 9. How collision impacts HashSet?

<details>
<summary>Show Answer</summary>

**Answer:**

A **collision** occurs when two different elements produce the same bucket index (same hash or hash % capacity).

### Impact

| Effect | Consequence |
|--------|-------------|
| Slower operations | Must traverse bucket chain |
| More `equals()` calls | Compare each entry in bucket |
| Worst case | All elements in one bucket → O(n) |
| Resize pressure | More entries per bucket before treeify |

### Collision Handling (Java 8+ HashMap backing)

```text
Bucket empty     → direct store
Collision        → linked list in bucket
Many collisions  → treeify to Red-Black Tree (if size ≥ 8)
```

### Example — Hash Collision

```java
String s1 = "FB";
String s2 = "Ea";

System.out.println(s1.hashCode()); // 2236
System.out.println(s2.hashCode()); // 2236 — collision!

Set<String> set = new HashSet<>();
set.add(s1);
set.add(s2); // different strings, same hashCode

System.out.println(set.size()); // 2 — both stored (equals() distinguishes)
```

### Production Risk

Poor `hashCode()` on custom objects → performance degradation.

```java
// ❌ Bad — all objects same bucket
@Override
public int hashCode() {
    return 1;
}
```

**Interview Point:**

> Collisions don't break correctness (`equals()` resolves), but hurt **performance**. Good `hashCode()` distribution is critical.

</details>

---

# 10. How equality is determined?

<details>
<summary>Show Answer</summary>

**Answer:**

`HashSet` determines equality in **two steps**:

### Step 1 — hashCode() (fast filter)

```text
If hashCodes differ → NOT equal (usually)
Same bucket → possible match or collision
```

### Step 2 — equals() (definitive)

```text
equals(existingElement) == true → DUPLICATE (reject add)
equals(existingElement) == false → COLLISION (store both)
```

### Example

```java
Set<String> set = new HashSet<>();

String a = new String("Java");
String b = new String("Java");

set.add(a);
set.add(b);

// a == b        → false (different references)
// a.equals(b)   → true  (same content)
// set.size()    → 1
```

### Custom Object Rules

```java
class Employee {
    int id;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        return id == ((Employee) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
```

### Contract (Critical)

```text
If a.equals(b) == true  → a.hashCode() == b.hashCode() MUST be true
```

**Interview Point:**

> Equality is **equals()**, not `==`. hashCode() is a performance optimization—never the sole equality check.

</details>

---

### LinkedHashSet

---

# 11. Difference between HashSet and LinkedHashSet?

<details>
<summary>Show Answer</summary>

**Answer:**

| Feature | HashSet | LinkedHashSet |
|---------|---------|---------------|
| Internal map | `HashMap` | `LinkedHashMap` |
| Ordering | **No guaranteed order** | **Insertion order** maintained |
| Performance | Slightly faster | Slightly slower (linked list overhead) |
| Memory | Less | More (prev/next pointers) |
| `null` | One allowed | One allowed |
| Use case | Unique elements, order irrelevant | Unique + predictable iteration order |

### Example — HashSet (unordered)

```java
Set<String> hashSet = new HashSet<>();
hashSet.add("Apple");
hashSet.add("Banana");
hashSet.add("Cherry");

System.out.println(hashSet);
// Order not guaranteed — e.g. [Cherry, Apple, Banana]
```

### Example — LinkedHashSet (insertion order)

```java
Set<String> linkedSet = new LinkedHashSet<>();
linkedSet.add("Apple");
linkedSet.add("Banana");
linkedSet.add("Cherry");

System.out.println(linkedSet);
// [Apple, Banana, Cherry] — insertion order preserved
```

**Interview Point:**

> `LinkedHashSet` = `HashSet` uniqueness + `LinkedHashMap` ordering. Use when iteration order must match insertion order.

</details>

---

# 12. How insertion order is maintained?

<details>
<summary>Show Answer</summary>

**Answer:**

`LinkedHashSet` uses internal **`LinkedHashMap`**, which maintains a **doubly linked list** across all entries—in addition to the hash table.

### Internal Structure

```text
Hash Table (buckets)
    +
Doubly Linked List (insertion order)

head → [Apple] ↔ [Banana] ↔ [Cherry] → tail
```

### How add() Works

```text
1. HashMap put (hash bucket storage)
2. Link new entry into doubly linked list at tail
3. Maintain before/after pointers
```

### Conceptual LinkedHashMap Entry

```java
static class Entry<K,V> extends HashMap.Node<K,V> {
    Entry<K,V> before;  // previous in order
    Entry<K,V> after;   // next in order
}
```

### Example

```java
Set<String> set = new LinkedHashSet<>();
set.add("First");
set.add("Second");
set.add("Third");

for (String s : set) {
    System.out.println(s);
}
// First
// Second
// Third  — always insertion order
```

### Access-Order Mode (LinkedHashMap feature)

`LinkedHashSet` uses **insertion-order** by default (not access-order—that's `LinkedHashMap` constructor flag).

**Interview Point:**

> Insertion order via **doubly linked list** threaded through hash table entries—O(1) add with order tracking.

</details>

---

### TreeSet

---

# 13. Difference between HashSet and TreeSet?

<details>
<summary>Show Answer</summary>

**Answer:**

| Feature | HashSet | TreeSet |
|---------|---------|---------|
| Internal structure | `HashMap` (hash table) | `TreeMap` (Red-Black Tree) |
| Ordering | No order | **Sorted order** |
| Performance | O(1) average | O(log n) |
| Comparator | Not used | `Comparable` or `Comparator` |
| `null` | One allowed | ❌ Not allowed (natural order) |
| Duplicates | Not allowed | Not allowed |
| Implementation | `Set` | `NavigableSet` |

### Example — HashSet

```java
Set<Integer> hashSet = new HashSet<>();
hashSet.add(30);
hashSet.add(10);
hashSet.add(20);

System.out.println(hashSet); // [10, 20, 30] or any order
```

### Example — TreeSet (sorted)

```java
Set<Integer> treeSet = new TreeSet<>();
treeSet.add(30);
treeSet.add(10);
treeSet.add(20);

System.out.println(treeSet); // [10, 20, 30] — always sorted
```

### When to Choose

| Choose HashSet | Choose TreeSet |
|----------------|----------------|
| Need fast add/contains | Need sorted iteration |
| Order doesn't matter | Need range queries (`headSet`, `tailSet`) |
| General uniqueness | Need nearest/lower/higher element |

**Interview Point:**

> HashSet = speed + no order. TreeSet = sorted + O(log n). Never use TreeSet just for uniqueness—use HashSet unless sorting is required.

</details>

---

# 14. How TreeSet maintains sorting?

<details>
<summary>Show Answer</summary>

**Answer:**

`TreeSet` stores elements in a **Red-Black Tree** (self-balancing BST) and keeps them in **sorted order** continuously.

### Sorting Mechanisms

| Approach | When Used |
|----------|-----------|
| **Natural ordering** | Elements implement `Comparable` → `compareTo()` |
| **Custom ordering** | Pass `Comparator` to constructor |

### Natural Ordering Example

```java
Set<String> set = new TreeSet<>();
set.add("Charlie");
set.add("Alice");
set.add("Bob");

System.out.println(set); // [Alice, Bob, Charlie]
// String.compareTo() — alphabetical
```

### Comparator Example

```java
Set<String> set = new TreeSet<>(
        (a, b) -> b.compareTo(a)); // reverse order

set.add("A");
set.add("B");
set.add("C");

System.out.println(set); // [C, B, A]
```

### Internal Compare on add()

```text
add(element)
    ↓
traverse Red-Black Tree using compareTo() / Comparator
    ↓
insert at correct position
    ↓
balance tree (color flips / rotations)
```

### NavigableSet Operations

```java
TreeSet<Integer> set = new TreeSet<>();
set.add(10);
set.add(20);
set.add(30);

set.lower(20);  // 10
set.higher(20); // 30
set.headSet(20); // elements < 20
```

**Interview Point:**

> TreeSet is always sorted—every `add()` places element in correct tree position using `Comparable` or `Comparator`.

</details>

---

# 15. Can TreeSet store null?

<details>
<summary>Show Answer</summary>

**Answer:**

### With Natural Ordering (default)

**No.** Adding `null` throws **`NullPointerException`**.

```java
Set<String> set = new TreeSet<>();
set.add("A");
set.add(null); // ❌ NullPointerException
// compareTo() cannot compare null
```

### With Custom Comparator

**Depends on comparator**—if comparator handles null, it may work (not recommended).

```java
Set<String> set = new TreeSet<>(
        Comparator.nullsFirst(String::compareTo));

set.add(null);  // ✅ if comparator allows
set.add("A");
```

### Compare with HashSet

```java
Set<String> hashSet = new HashSet<>();
hashSet.add(null); // ✅ allowed (one null)
```

### Why TreeSet Rejects null (natural order)

* Tree uses `compareTo()` to find insertion point
* `null.compareTo(anything)` → NPE
* Tree structure requires comparable position for every element

**Interview Point:**

> Default `TreeSet` does **not** accept `null`. Use `HashSet` or `LinkedHashSet` if null membership is needed.

</details>

---

# 16. Internal data structure?

<details>
<summary>Show Answer</summary>

**Answer:**

`TreeSet` is backed by a **`TreeMap`**, which uses a **Red-Black Tree** (self-balancing binary search tree).

### Structure

```java
public class TreeSet<E> {

    private transient NavigableMap<E,Object> m;

    private static final Object PRESENT = new Object();

    public TreeSet() {
        this(new TreeMap<>());
    }

    public boolean add(E e) {
        return m.put(e, PRESENT) == null;
    }
}
```

### Red-Black Tree Properties

```text
        20
       /  \
     10    30
    /  \
   5   15

• Sorted order maintained
• Self-balancing (no skewed tree)
• O(log n) insert/search/delete
```

### Element Storage

```text
TreeSet Element → TreeMap KEY
PRESENT         → TreeMap VALUE (dummy)
```

### Example

```java
TreeSet<Integer> set = new TreeSet<>();
set.add(50);
set.add(20);
set.add(80);

// Internally stored in balanced BST order
for (Integer n : set) {
    System.out.println(n); // 20, 50, 80
}
```

**Interview Point:**

> TreeSet = TreeMap keys in a Red-Black Tree. Sorted, balanced, O(log n) operations.

</details>

---

# 17. TreeSet backed by which Map?

<details>
<summary>Show Answer</summary>

**Answer:**

`TreeSet` is backed by **`TreeMap<E, Object>`**.

### Internal Code Pattern

```java
public class TreeSet<E> {

    private transient NavigableMap<E, Object> m;

    private static final Object PRESENT = new Object();

    TreeSet(NavigableMap<E,Object> m) {
        this.m = m;
    }

    public TreeSet() {
        this(new TreeMap<E,Object>());
    }

    public TreeSet(Comparator<? super E> comparator) {
        this(new TreeMap<>(comparator));
    }
}
```

### Relationship

| Collection | Backing Map |
|------------|-------------|
| `HashSet` | `HashMap` |
| `LinkedHashSet` | `LinkedHashMap` |
| `TreeSet` | `TreeMap` |

### Why NavigableMap?

`TreeSet` implements `NavigableSet` → needs `TreeMap`'s navigation methods:

```java
TreeSet<Integer> set = new TreeSet<>();
set.add(10);
set.add(20);
set.add(30);

set.pollFirst();  // 10 — via TreeMap
set.pollLast();   // 30
```

**Interview Point:**

> Same pattern as HashSet: Set wraps Map, elements are keys, dummy `PRESENT` is value. TreeSet uses `TreeMap` for sorted tree storage.

</details>

---

# 18. Complexity of operations?

<details>
<summary>Show Answer</summary>

**Answer:**

### TreeSet — Time Complexity

| Operation | Complexity | Notes |
|-----------|------------|-------|
| `add()` | **O(log n)** | Tree insert + balance |
| `remove()` | **O(log n)** | Tree delete + balance |
| `contains()` | **O(log n)** | Tree search |
| `size()` | **O(1)** | Map size counter |
| Iteration | **O(n)** | In-order tree traversal |
| `first()` / `last()` | **O(log n)** | Tree navigation |
| `lower()` / `higher()` | **O(log n)** | Tree navigation |

### Compare All Set Implementations

| Operation | HashSet | LinkedHashSet | TreeSet |
|-----------|---------|---------------|---------|
| `add()` | O(1)* | O(1)* | O(log n) |
| `contains()` | O(1)* | O(1)* | O(log n) |
| `remove()` | O(1)* | O(1)* | O(log n) |
| Ordering | None | Insertion | Sorted |
| `null` | 1 allowed | 1 allowed | Not allowed (default) |

\* Average case; worst case O(n) with bad hashing

### Example — Large TreeSet

```java
TreeSet<Integer> set = new TreeSet<>();

for (int i = 0; i < 1000000; i++) {
    set.add(i); // O(log n) each
}

set.contains(500000); // O(log n) — ~20 comparisons for 1M elements
```

**Interview Point:**

> TreeSet trades O(1) hash performance for O(log n) sorted structure. Choose based on whether sorting/range ops matter more than raw speed.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Which Set implementation is default choice?

<details>
<summary>Show Answer</summary>

**Answer:** `HashSet` — fastest average performance when ordering is not required.

</details>

---

### Q: Can two unequal objects be in the same HashSet bucket?

<details>
<summary>Show Answer</summary>

**Answer:** Yes — hash collision. They coexist if `equals()` returns `false`.

</details>

---

### Q: Is TreeSet thread-safe?

<details>
<summary>Show Answer</summary>

**Answer:** No. Use `Collections.synchronizedSet(new TreeSet<>())` or concurrent alternatives like `ConcurrentSkipListSet` for sorted concurrent sets.

</details>

---

### Q: LinkedHashSet vs TreeSet for LRU-like order?

<details>
<summary>Show Answer</summary>

**Answer:** `LinkedHashSet` preserves **insertion order**. For **access-order** LRU cache, use `LinkedHashMap` (access-order mode) — not LinkedHashSet.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> `HashSet` uses `HashMap` for O(1) uniqueness via hashing. `LinkedHashSet` adds insertion-order via `LinkedHashMap`. `TreeSet` uses `TreeMap` Red-Black Tree for sorted O(log n) uniqueness. Always override `equals()` and `hashCode()` for custom objects in hash-based sets.

</details>
