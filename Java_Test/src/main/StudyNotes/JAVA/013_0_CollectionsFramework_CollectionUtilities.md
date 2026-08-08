# 13.0 Collections Framework & Collection Utilities

## Basics

---

# 1. What is Collection Framework?

<details>
<summary>Show Answer</summary>

**Answer:**

The **Java Collections Framework (JCF)** is a unified architecture for representing and manipulating groups of objects as a single unit.

It provides:

* **Interfaces** — define common contracts (`List`, `Set`, `Queue`, `Map`)
* **Implementations** — concrete classes (`ArrayList`, `HashSet`, `HashMap`, etc.)
* **Algorithms** — utility methods via `java.util.Collections` (sort, search, reverse, etc.)

### Core Hierarchy (simplified)

```text
Iterable
   ↓
Collection
   ├── List
   ├── Set
   └── Queue
        └── Deque

Map (separate hierarchy — does NOT extend Collection)
```

### Example

```java
List<String> names = new ArrayList<>();
names.add("John");
names.add("Jane");

Collections.sort(names);
```

### Why It Matters (5+ Years)

* Standard APIs across the entire Java ecosystem
* Predictable performance characteristics
* Enables generics-based type safety
* Foundation for Streams, parallel processing, and framework code (Spring, Hibernate)

**Interview Point:**

> The Collections Framework is not just data structures—it is a standardized API for storing, searching, sorting, and processing groups of objects efficiently.

</details>

---

# 2. Difference between Collection and Collections?

<details>
<summary>Show Answer</summary>

**Answer:**

| Collection | Collections |
|------------|-------------|
| **Interface** in `java.util` | **Utility class** in `java.util` |
| Root of List, Set, Queue hierarchy | Cannot be instantiated |
| Defines contract (`add`, `remove`, `iterator`) | Provides static helper methods |
| Implemented by `ArrayList`, `HashSet`, etc. | Sort, search, synchronize, wrap |

### Collection (Interface)

```java
Collection<String> list = new ArrayList<>();
list.add("Java");
```

### Collections (Utility Class)

```java
List<Integer> nums = Arrays.asList(3, 1, 4);

Collections.sort(nums);
Collections.reverse(nums);
List<String> syncList = Collections.synchronizedList(new ArrayList<>());
```

**Interview Point:**

> `Collection` is an interface defining structure behavior; `Collections` is a helper class providing algorithms and wrapper utilities on top of collections.

</details>

---

# 3. Difference between Collection and Map?

<details>
<summary>Show Answer</summary>

**Answer:**

| Collection | Map |
|------------|-----|
| Stores **single elements** | Stores **key-value pairs** |
| Extends `Iterable` | Does **NOT** extend `Collection` |
| `add(element)` | `put(key, value)` |
| One object per entry | Two objects per entry (key + value) |
| Examples: `List`, `Set`, `Queue` | Examples: `HashMap`, `TreeMap` |

### Collection Example

```java
List<String> list = new ArrayList<>();
list.add("John");          // single element
```

### Map Example

```java
Map<Integer, String> map = new HashMap<>();
map.put(101, "John");      // key + value
```

### Important Trap

```java
// Map is NOT a Collection
Map<String, Integer> map = new HashMap<>();
// map.add("key");  ❌ No add() method
```

**Interview Point:**

> `Map` is part of the Collections Framework ecosystem but is a separate hierarchy because it models associations (keys → values), not simple element groups.

</details>

---

# 4. What are the major interfaces in Collection Framework?

<details>
<summary>Show Answer</summary>

**Answer:**

### Core Interfaces

| Interface | Purpose | Common Implementations |
|-----------|---------|------------------------|
| `Collection` | Root interface for groups of objects | — |
| `List` | Ordered, allows duplicates | `ArrayList`, `LinkedList` |
| `Set` | No duplicate elements | `HashSet`, `LinkedHashSet`, `TreeSet` |
| `Queue` | FIFO / ordered processing | `LinkedList`, `PriorityQueue` |
| `Deque` | Double-ended queue | `ArrayDeque`, `LinkedList` |
| `Map` | Key-value pairs (separate hierarchy) | `HashMap`, `LinkedHashMap`, `TreeMap`, `ConcurrentHashMap` |

### Hierarchy Diagram

```text
Iterable
   ↓
Collection
   ├── List        → ArrayList, LinkedList
   ├── Set         → HashSet, LinkedHashSet, TreeSet
   └── Queue       → PriorityQueue, LinkedList
        └── Deque   → ArrayDeque

Map (separate)
   → HashMap, LinkedHashMap, TreeMap, Hashtable, ConcurrentHashMap
```

### Legacy (still in framework, avoid in new code)

* `Vector`, `Stack`, `Hashtable`

**Interview Point:**

> Know the interface first, then the implementation—interviewers expect you to pick the right implementation based on access pattern, ordering, uniqueness, and concurrency needs.

</details>

---

# 5. Explain List, Set and Queue.

<details>
<summary>Show Answer</summary>

**Answer:**

### List

* **Ordered** collection (insertion or index order)
* **Allows duplicates**
* Supports positional access (`get(index)`)

```java
List<String> list = new ArrayList<>();
list.add("A");
list.add("B");
list.add("A");   // duplicate allowed

System.out.println(list.get(0)); // A
```

**Use when:** you need index-based access, ordering, or duplicate values.

---

### Set

* **No duplicate elements** (uniqueness enforced)
* May or may not maintain order (depends on implementation)

```java
Set<String> set = new HashSet<>();
set.add("Java");
set.add("Java");  // ignored — duplicate

System.out.println(set.size()); // 1
```

**Use when:** you need uniqueness (unique IDs, deduplication, cache keys).

---

### Queue

* **FIFO** processing (First In, First Out) by default
* Designed for holding elements prior to processing

```java
Queue<String> queue = new LinkedList<>();
queue.offer("Task1");
queue.offer("Task2");

System.out.println(queue.poll()); // Task1 (head removed)
```

**Use when:** task scheduling, BFS, producer-consumer patterns, buffering.

### Quick Comparison

| Feature | List | Set | Queue |
|---------|------|-----|-------|
| Duplicates | ✅ Yes | ❌ No | ✅ Yes (usually) |
| Order | Index/insertion | Varies | FIFO (typically) |
| Key operation | `get(index)` | `add` uniqueness | `offer` / `poll` |

**Interview Point:**

> List = indexed + duplicates; Set = uniqueness; Queue = processing order. Pick based on business rule, not convenience.

</details>

---

# 6. Which collection allows duplicates?

<details>
<summary>Show Answer</summary>

**Answer:**

### ✅ Allow Duplicates

| Collection | Notes |
|------------|-------|
| `List` (all) | `ArrayList`, `LinkedList`, `Vector` |
| `Queue` / `Deque` | `LinkedList`, `PriorityQueue`, `ArrayDeque` |
| `Map` values | Same value can map to different keys |
| `Map` keys | ❌ Keys must be unique (values can repeat) |

### ❌ Do NOT Allow Duplicates

| Collection | Notes |
|------------|-------|
| `Set` (all) | `HashSet`, `LinkedHashSet`, `TreeSet` |
| `Map` keys | Duplicate keys overwrite previous value |

### Example — List allows duplicates

```java
List<Integer> list = new ArrayList<>();
list.add(10);
list.add(10);
list.add(20);

System.out.println(list); // [10, 10, 20]
```

### Example — Set rejects duplicates

```java
Set<Integer> set = new HashSet<>();
set.add(10);
set.add(10);

System.out.println(set); // [10]
```

### Map nuance

```java
Map<String, String> map = new HashMap<>();
map.put("A", "Java");
map.put("B", "Java");  // duplicate VALUE — allowed

map.put("A", "Python"); // duplicate KEY — overwrites
```

**Interview Point:**

> Duplicates are allowed in `List` and most `Queue` implementations; `Set` enforces uniqueness using `equals()` and `hashCode()`.

</details>

---

# 7. Which collection maintains insertion order?

<details>
<summary>Show Answer</summary>

**Answer:**

### Maintains Insertion Order

| Collection | Order Type |
|------------|------------|
| `ArrayList` | Insertion order (index-based) |
| `LinkedList` | Insertion order |
| `LinkedHashSet` | Insertion order of elements |
| `LinkedHashMap` | Insertion order of keys (or access-order mode) |
| `Queue` / `Deque` | FIFO order (`LinkedList`, `ArrayDeque`) |
| `PriorityQueue` | ❌ Sort order (heap), not insertion order |

### Does NOT Maintain Insertion Order

| Collection | Order Type |
|------------|------------|
| `HashSet` | No guaranteed order |
| `HashMap` | No guaranteed order |
| `TreeSet` / `TreeMap` | Sorted order (natural or Comparator) |

### Example — LinkedHashSet preserves insertion order

```java
Set<String> set = new LinkedHashSet<>();
set.add("Apple");
set.add("Banana");
set.add("Cherry");

System.out.println(set);
// [Apple, Banana, Cherry]
```

### Example — HashSet does not

```java
Set<String> set = new HashSet<>();
set.add("Apple");
set.add("Banana");
set.add("Cherry");

System.out.println(set);
// Order not guaranteed — may vary
```

**Interview Point:**

> Use `LinkedHashSet` or `LinkedHashMap` when you need predictable iteration order; use `HashSet`/`HashMap` when order does not matter and you want faster average performance.

</details>

---

# 8. Which collection stores unique values?

<details>
<summary>Show Answer</summary>

**Answer:**

### Store Unique Values (Elements)

| Collection | Uniqueness Rule |
|------------|-----------------|
| `Set` (all implementations) | No duplicate elements |
| `HashSet` | Uniqueness via `hashCode()` + `equals()` |
| `LinkedHashSet` | Same as HashSet + insertion order |
| `TreeSet` | Uniqueness + sorted order |

### Store Unique Keys (Map)

| Collection | Uniqueness Rule |
|------------|-----------------|
| `Map` keys | Each key appears once |
| `HashMap` | Unique keys, values can repeat |
| `TreeMap` | Unique keys in sorted order |

### Example — Set uniqueness

```java
Set<Integer> ids = new HashSet<>();
ids.add(101);
ids.add(102);
ids.add(101);  // duplicate — not added

System.out.println(ids.size()); // 2
```

### Example — Map unique keys

```java
Map<Integer, String> employees = new HashMap<>();
employees.put(101, "John");
employees.put(101, "Jane"); // same key — overwrites John

System.out.println(employees.get(101)); // Jane
```

### List does NOT guarantee uniqueness

```java
List<String> list = new ArrayList<>();
list.add("A");
list.add("A"); // allowed
```

**Interview Point:**

> For unique elements use `Set`; for unique identifiers with associated data use `Map` keys. Uniqueness depends on correct `equals()` and `hashCode()` implementation.

</details>

---

# 9. Which collection provides sorting?

<details>
<summary>Show Answer</summary>

**Answer:**

### Naturally Sorted Collections

| Collection | Sorting Behavior |
|------------|------------------|
| `TreeSet` | Elements always in sorted order |
| `TreeMap` | Keys always in sorted order |
| `PriorityQueue` | Head is always smallest/largest (heap order) |

### Sorted via Utility Methods

| Approach | Example |
|----------|---------|
| `Collections.sort()` | Sort any `List` |
| `List.sort()` | Java 8+ lambda sorting |
| `Stream.sorted()` | Functional sorting |

### Example — TreeSet (automatic sorting)

```java
Set<Integer> set = new TreeSet<>();
set.add(30);
set.add(10);
set.add(20);

System.out.println(set); // [10, 20, 30]
```

### Example — Collections.sort()

```java
List<String> names = new ArrayList<>();
names.add("Charlie");
names.add("Alice");
names.add("Bob");

Collections.sort(names);
System.out.println(names); // [Alice, Bob, Charlie]
```

### Example — Custom sort with Comparator

```java
List<Employee> employees = new ArrayList<>();

employees.sort((e1, e2) ->
        Integer.compare(e1.getSalary(), e2.getSalary()));
```

### Does NOT Sort Automatically

* `ArrayList`, `LinkedList`, `HashSet`, `HashMap` — require external sorting

**Interview Point:**

> `TreeSet`/`TreeMap` maintain order continuously (O(log n) inserts); sorting a `List` is typically O(n log n) and done on demand.

</details>

---

# 10. Which collection is thread-safe?

<details>
<summary>Show Answer</summary>

**Answer:**

### Built-in Thread-Safe Collections

| Collection | Mechanism |
|------------|-----------|
| `Vector` | Method-level synchronization (legacy) |
| `Stack` | Extends `Vector` (legacy) |
| `Hashtable` | Method-level synchronization (legacy) |
| `ConcurrentHashMap` | Segment / bucket-level locking (Java 7), CAS + synchronized blocks (Java 8+) |
| `ConcurrentSkipListMap` | Lock-free concurrent sorted map |
| `CopyOnWriteArrayList` | Copy-on-write for read-heavy scenarios |
| `CopyOnWriteArraySet` | Backed by `CopyOnWriteArrayList` |

### Wrapper-Based Thread Safety

```java
List<String> syncList =
        Collections.synchronizedList(new ArrayList<>());

Map<String, Integer> syncMap =
        Collections.synchronizedMap(new HashMap<>());
```

### ❌ NOT Thread-Safe (common trap)

| Collection | Risk |
|------------|------|
| `ArrayList` | ConcurrentModificationException / data corruption |
| `HashMap` | Infinite loop / lost data (Java 7), race conditions |
| `HashSet` | Internal HashMap is not thread-safe |
| `LinkedList` | Not thread-safe |

### Example — ConcurrentHashMap

```java
ConcurrentHashMap<String, Integer> map =
        new ConcurrentHashMap<>();

map.put("count", 1);
map.putIfAbsent("count", 2);
map.computeIfAbsent("key", k -> 100);
```

### Production Guidance (5+ Years)

* Prefer `ConcurrentHashMap` over `Hashtable`
* Avoid `Vector` / `Stack` — use `ArrayDeque` + proper synchronization
* For read-heavy lists: `CopyOnWriteArrayList`
* For general concurrency: `ExecutorService` + regular collections

**Interview Point:**

> Legacy synchronized collections (`Vector`, `Hashtable`) lock the entire structure; modern concurrent collections (`ConcurrentHashMap`, `CopyOnWriteArrayList`) use finer-grained strategies for better scalability.

</details>

---

## Collection Utilities

---

# 11. Collections.sort() vs Arrays.sort()?

<details>
<summary>Show Answer</summary>

**Answer:**

| Feature | `Collections.sort()` | `Arrays.sort()` |
|---------|-------------------|-----------------|
| Works on | `List<T>` | **Array** (`int[]`, `Object[]`, etc.) |
| Class | `java.util.Collections` | `java.util.Arrays` |
| Algorithm (objects) | **TimSort** (stable) | **TimSort** (Java 7+) |
| Algorithm (primitives) | N/A | **Dual-Pivot QuickSort** |
| Stability | Stable (order of equals preserved) | Stable for objects; not for primitives |
| Modifies in place | ✅ Yes | ✅ Yes |

### Collections.sort() — List

```java
List<String> names = new ArrayList<>();
names.add("Charlie");
names.add("Alice");
names.add("Bob");

Collections.sort(names); // natural order
System.out.println(names); // [Alice, Bob, Charlie]

Collections.sort(names, Comparator.reverseOrder());
```

### Arrays.sort() — Array

```java
int[] nums = {3, 1, 4, 1, 5};
Arrays.sort(nums);
System.out.println(nums); // [1, 1, 3, 4, 5]

String[] words = {"Java", "Python", "Go"};
Arrays.sort(words);
```

### Java 8+ Preference for Lists

```java
// Modern — preferred over Collections.sort()
list.sort(Comparator.naturalOrder());
list.sort((a, b) -> a.compareTo(b));
```

### Performance Notes

| Type | Algorithm | Complexity |
|------|-----------|------------|
| `Arrays.sort(int[])` | Dual-pivot quicksort | O(n log n) |
| `Arrays.sort(Object[])` | TimSort | O(n log n) |
| `Collections.sort(List)` | TimSort | O(n log n) |

### Key Difference

```text
Collections.sort() → List interface (ArrayList, LinkedList, etc.)
Arrays.sort()        → primitive and object arrays
```

**Interview Point:**

> `Collections.sort()` for Lists, `Arrays.sort()` for arrays. Both use TimSort for objects. Primitives use faster dual-pivot quicksort in `Arrays.sort()`.

</details>

---

# 12. Collections.synchronizedList()?

<details>
<summary>Show Answer</summary>

**Answer:**

`Collections.synchronizedList()` returns a **thread-safe wrapper** around a `List` by synchronizing every method on a **mutex object**.

### Usage

```java
List<String> list = new ArrayList<>();
List<String> syncList = Collections.synchronizedList(list);

syncList.add("A");  // synchronized
syncList.get(0);  // synchronized
```

### How It Works Internally

```java
// Conceptual wrapper
public boolean add(E e) {
    synchronized (mutex) {
        return list.add(e);
    }
}
```

### Critical — Iteration Must Be Manual Sync

```java
List<String> syncList =
        Collections.synchronizedList(new ArrayList<>());

// ❌ Unsafe — other thread may modify during iteration
for (String s : syncList) { ... }

// ✅ Correct — manual synchronization
synchronized (syncList) {
    for (String s : syncList) {
        System.out.println(s);
    }
}
```

### Available Wrappers

```java
Collections.synchronizedList(list);
Collections.synchronizedSet(set);
Collections.synchronizedMap(map);
Collections.synchronizedCollection(collection);
```

### Limitations

| Issue | Detail |
|-------|--------|
| Coarse locking | Every method locks entire list |
| Poor scalability | Writers block readers and other writers |
| Iterator not safe | Must manually synchronize during iteration |
| Not atomic compound ops | `if (!list.contains(x)) list.add(x)` needs external sync |

### Modern Alternatives

```java
// Read-heavy
CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();

// General concurrent
// Use proper concurrent data structures or ConcurrentHashMap patterns
```

**Interview Point:**

> `synchronizedList` wraps every method with a lock—simple but coarse. Always synchronize manually during iteration. Prefer `CopyOnWriteArrayList` for read-heavy workloads.

</details>

---

# 13. Collections.unmodifiableList()?

<details>
<summary>Show Answer</summary>

**Answer:**

`Collections.unmodifiableList()` returns a **read-only view** of a list—mutating operations throw `UnsupportedOperationException`.

### Usage

```java
List<String> list = new ArrayList<>();
list.add("A");
list.add("B");

List<String> readOnly = Collections.unmodifiableList(list);

readOnly.get(0);        // ✅ "A"
readOnly.add("C");      // ❌ UnsupportedOperationException
readOnly.remove(0);     // ❌ UnsupportedOperationException
```

### Important Trap — Not Truly Immutable

```java
List<String> list = new ArrayList<>();
list.add("A");

List<String> view = Collections.unmodifiableList(list);

list.add("B"); // ✅ original list still modifiable!

System.out.println(view); // [A, B] — view reflects change
```

### Available Unmodifiable Wrappers

```java
Collections.unmodifiableList(list);
Collections.unmodifiableSet(set);
Collections.unmodifiableMap(map);
Collections.unmodifiableCollection(collection);
```

### Java 9+ Truly Immutable Alternatives

```java
// Truly immutable — cannot modify via any reference
List<String> immutable = List.of("A", "B", "C");

// Copy — independent immutable snapshot
List<String> copy = List.copyOf(mutableList);
```

### Comparison

| Approach | Underlying modifiable? | Truly immutable? |
|----------|------------------------|------------------|
| `unmodifiableList(view)` | ✅ Yes — view only | ❌ No |
| `List.of()` | N/A | ✅ Yes |
| `List.copyOf()` | N/A | ✅ Yes |

### Use Case

```java
// Expose read-only API to callers
public List<String> getNames() {
    return Collections.unmodifiableList(internalList);
}
```

**Interview Point:**

> `unmodifiableList` is a **read-only view**, not true immutability. Original list can still change. Use `List.of()` or `List.copyOf()` for guaranteed immutability.

</details>

---

# 14. Difference between synchronized and unmodifiable collections?

<details>
<summary>Show Answer</summary>

**Answer:**

| Feature | Synchronized | Unmodifiable |
|---------|--------------|--------------|
| Purpose | **Thread safety** | **Read-only view** |
| Can modify? | ✅ Yes (with lock) | ❌ Throws exception |
| Thread-safe? | ✅ Yes | ❌ No |
| Underlying collection | Can still be modified | Can still be modified |
| Iterator safety | Manual sync required | Read-only iteration OK |
| Performance | Lock overhead | No lock overhead |

### Synchronized — Thread Safety

```java
List<String> syncList =
        Collections.synchronizedList(new ArrayList<>());

// Multiple threads can safely add/remove
syncList.add("A");  // thread-safe write
```

### Unmodifiable — Read-Only Access

```java
List<String> readOnly =
        Collections.unmodifiableList(originalList);

readOnly.get(0);   // ✅ read OK
readOnly.add("X"); // ❌ UnsupportedOperationException
```

### Different Goals

```text
Synchronized  → prevent concurrent modification (thread safety)
Unmodifiable  → prevent mutation through this reference (API safety)
```

### Combined Scenario

```java
// Read-only AND thread-safe exposure
public List<String> getSnapshot() {
    synchronized (internalList) {
        return Collections.unmodifiableList(
                new ArrayList<>(internalList)); // defensive copy
    }
}
```

### Neither Is Truly Immutable

```java
List<String> original = new ArrayList<>();
original.add("A");

List<String> sync = Collections.synchronizedList(original);
List<String> unmod = Collections.unmodifiableList(original);

original.add("B"); // both wrappers see the change!
```

### Production Recommendations

| Need | Solution |
|------|----------|
| Thread-safe mutable | `Collections.synchronizedList()` or concurrent collections |
| Read-only API | `Collections.unmodifiableList()` + defensive copy |
| True immutability | `List.of()`, `List.copyOf()` |
| Concurrent read/write | `CopyOnWriteArrayList`, `ConcurrentHashMap` |

**Interview Point:**

> Synchronized = concurrency control. Unmodifiable = mutation control. They solve different problems and are often combined with defensive copying for safe public APIs.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Does Map extend Collection?

<details>
<summary>Show Answer</summary>

**Answer:** No. `Map` is a separate hierarchy in the Collections Framework.

</details>

---

### Q: Which is faster — HashSet or TreeSet?

<details>
<summary>Show Answer</summary>

**Answer:** `HashSet` — O(1) average add/remove vs `TreeSet` O(log n). Use `TreeSet` only when sorted order is required.

</details>

---

### Q: Can we store null in HashMap?

<details>
<summary>Show Answer</summary>

**Answer:** Yes — one `null` key and multiple `null` values allowed. `ConcurrentHashMap` does **not** allow null keys or values.

</details>

---

### Q: Best collection for LRU cache?

<details>
<summary>Show Answer</summary>

**Answer:** `LinkedHashMap` with access-order mode (`true` constructor flag) or `LinkedHashMap` subclass overriding `removeEldestEntry()`.

</details>

---

### Q: Is unmodifiableList thread-safe?

<details>
<summary>Show Answer</summary>

**Answer:** No. It only prevents mutation through the wrapper. For thread-safe read-only exposure, copy the list first or synchronize during copy.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Collections Framework: pick structure by access pattern and concurrency. `Collections` utility provides sort, synchronized wrappers, and unmodifiable views—know when to use `List.sort()`, `synchronizedList`, `unmodifiableList`, and modern alternatives like `List.copyOf()` and concurrent collections.

</details>
