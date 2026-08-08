# 13.7 Iterator

## Iterator

## Basic

---

# 1. What is Iterator?

<details>
<summary>Show Answer</summary>

**Answer:**

An **Iterator** is a cursor object used to **traverse (iterate)** over a collection one element at a time, without exposing the internal structure of the collection.

### Core Methods

| Method | Purpose |
|--------|---------|
| `boolean hasNext()` | Returns `true` if more elements exist |
| `E next()` | Returns next element, advances cursor |
| `void remove()` | Removes last returned element (optional) |

### Example

```java
List<String> list = Arrays.asList("Java", "Python", "Go");

Iterator<String> it = list.iterator();

while (it.hasNext()) {
    String lang = it.next();
    System.out.println(lang);
}
```

### For-Each Loop (uses Iterator internally)

```java
for (String lang : list) {
    System.out.println(lang);
}
// Compiler generates Iterator-based loop under the hood
```

### Iterator in Collection Framework

```text
Iterable
   ↓ iterator()
Iterator
   ↓ hasNext() / next() / remove()
Traverse Collection
```

### Which Collections Provide Iterator?

* All `Collection` implementations (`ArrayList`, `HashSet`, `LinkedList`, etc.)
* `Map` uses separate iterators: `keySet().iterator()`, `values().iterator()`, `entrySet().iterator()`

### Iterator vs Index-Based Access

```java
// Iterator — works on all Collections (Set has no get(index))
Set<String> set = new HashSet<>();
for (String s : set) { ... }

// Index — only List
List<String> list = new ArrayList<>();
for (int i = 0; i < list.size(); i++) {
    System.out.println(list.get(i));
}
```

**Interview Point:**

> Iterator = uniform traversal API for all collections. Enables fail-fast detection and hides internal representation.

</details>

---

# 2. Difference between Iterator and Enumeration?

<details>
<summary>Show Answer</summary>

**Answer:**

| Feature | Iterator | Enumeration |
|---------|----------|-------------|
| Introduced | Java 1.2 (Collections Framework) | Java 1.0 (legacy) |
| Package | `java.util` | `java.util` |
| Methods | `hasNext()`, `next()`, `remove()` | `hasMoreElements()`, `nextElement()` |
| Remove elements | ✅ `remove()` | ❌ Not supported |
| Fail-fast | ✅ Yes | ❌ No |
| Used by | All modern collections | Legacy (`Vector`, `Hashtable`) |
| Generics support | ✅ Yes | ❌ Raw types (legacy) |

### Iterator Example

```java
List<String> list = Arrays.asList("A", "B", "C");
Iterator<String> it = list.iterator();

while (it.hasNext()) {
    String s = it.next();
    if (s.equals("B")) {
        it.remove(); // ✅ safe removal during iteration
    }
}
```

### Enumeration Example (Legacy)

```java
Vector<String> vector = new Vector<>();
vector.add("A");

Enumeration<String> en = vector.elements();

while (en.hasMoreElements()) {
  String s = en.nextElement();
  // ❌ cannot remove during iteration
}
```

### Method Mapping

| Enumeration | Iterator |
|-------------|----------|
| `hasMoreElements()` | `hasNext()` |
| `nextElement()` | `next()` |
| — | `remove()` |

### Why Iterator Replaced Enumeration

* Added `remove()` during iteration
* Fail-fast behavior for safety
* Part of unified Collections Framework
* Generics type safety

**Interview Point:**

> `Enumeration` is legacy—use `Iterator` everywhere. Key addition: `remove()` and fail-fast behavior.

</details>

---

# 3. Difference between Iterator and ListIterator?

<details>
<summary>Show Answer</summary>

**Answer:**

`ListIterator` is a **bidirectional** iterator that extends `Iterator`—works **only with `List`** implementations.

| Feature | Iterator | ListIterator |
|---------|----------|--------------|
| Direction | **Forward only** | **Forward + backward** |
| Works on | Any `Collection` | **List only** |
| Methods | `hasNext()`, `next()`, `remove()` | + `hasPrevious()`, `previous()`, `nextIndex()`, `previousIndex()` |
| Add elements | ❌ | ✅ `add()` |
| Set/replace | ❌ | ✅ `set()` |
| Cursor position | Between elements | Between elements (with index tracking) |

### Iterator — Forward Only

```java
List<String> list = new ArrayList<>();
list.add("A");
list.add("B");
list.add("C");

Iterator<String> it = list.iterator();
while (it.hasNext()) {
    System.out.println(it.next()); // A, B, C
}
// ❌ cannot go backward
```

### ListIterator — Bidirectional

```java
List<String> list = new ArrayList<>();
list.add("A");
list.add("B");
list.add("C");

ListIterator<String> lit = list.listIterator();

while (lit.hasNext()) {
    System.out.println(lit.next()); // forward: A, B, C
}

while (lit.hasPrevious()) {
    System.out.println(lit.previous()); // backward: C, B, A
}
```

### ListIterator Add and Set

```java
ListIterator<String> lit = list.listIterator();

lit.next();           // "A"
lit.set("Alpha");     // replace A with Alpha
lit.add("Inserted");  // insert after current position
```

### When to Use ListIterator

| Use Iterator | Use ListIterator |
|--------------|------------------|
| Set, Queue traversal | List backward traversal |
| Simple forward loop | Insert/replace during iteration |
| for-each equivalent | Need index position |

**Interview Point:**

> `ListIterator` = bidirectional `Iterator` for Lists with `add()`, `set()`, and backward traversal. `Iterator` is forward-only and works on any Collection.

</details>

---

### Advanced

---

# 4. What is fail-fast iterator?

<details>
<summary>Show Answer</summary>

**Answer:**

A **fail-fast iterator** immediately throws `ConcurrentModificationException` if the collection is **structurally modified** (add/remove) during iteration—except through the iterator's own `remove()`.

### Fail-Fast Collections

* `ArrayList`
* `HashMap`
* `HashSet`
* `LinkedList`
* Most non-concurrent collections

### Example — Fail-Fast Behavior

```java
List<String> list = new ArrayList<>();
list.add("A");
list.add("B");
list.add("C");

for (String s : list) {
    list.remove(s); // ❌ ConcurrentModificationException
}
```

### Safe Removal via Iterator

```java
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    String s = it.next();
    if (s.equals("B")) {
        it.remove(); // ✅ safe — updates modCount correctly
    }
}
```

### How Fail-Fast Detects Change

```text
Iterator created → stores expectedModCount = collection.modCount
Each next()      → checks modCount == expectedModCount
If mismatch      → ConcurrentModificationException
```

### Purpose

* **Early detection** of concurrent modification bugs
* Prevents silent data corruption
* Best-effort — not a strict guarantee in multi-threaded code

### Not Truly Concurrent-Safe

```text
Fail-fast = detects modification in single-threaded misuse
            NOT designed for multi-threaded concurrent access
```

**Interview Point:**

> Fail-fast = throw exception on structural change during iteration. Detects bugs early via `modCount` check—not a substitute for thread-safe collections.

</details>

---

# 5. What is fail-safe iterator?

<details>
<summary>Show Answer</summary>

**Answer:**

A **fail-safe iterator** does **not** throw `ConcurrentModificationException` when the collection is modified during iteration. It works on a **snapshot or weakly consistent view** of the data.

### Fail-Safe / Weakly Consistent Collections

| Collection | Mechanism |
|------------|-----------|
| `CopyOnWriteArrayList` | Iterates over **array snapshot** |
| `CopyOnWriteArraySet` | Same as above |
| `ConcurrentHashMap` | **Weakly consistent** — may miss or reflect partial updates |
| `ConcurrentSkipListMap` | Weakly consistent |

### Example — CopyOnWriteArrayList

```java
List<String> list = new CopyOnWriteArrayList<>();
list.add("A");
list.add("B");

Iterator<String> it = list.iterator();
list.add("C"); // modify during iteration

while (it.hasNext()) {
    System.out.println(it.next()); // A, B — C not seen (snapshot)
}
// ✅ No ConcurrentModificationException
```

### Example — ConcurrentHashMap

```java
ConcurrentHashMap<String, Integer> map =
        new ConcurrentHashMap<>();
map.put("A", 1);
map.put("B", 2);

for (String key : map.keySet()) {
    map.put("C", 3); // safe — no exception
    System.out.println(key);
}
// May or may not include "C" — weakly consistent
```

### Fail-Fast vs Fail-Safe

| | Fail-Fast | Fail-Safe |
|---|-----------|-----------|
| Exception on modify | ✅ Throws CME | ❌ No exception |
| Data seen | Current state | Snapshot / weak view |
| Memory | No copy | May copy (CopyOnWrite) |
| Use case | Single-threaded | Concurrent environments |

### CopyOnWrite Trade-off

```text
Read:  fast (no locking, uses snapshot)
Write: slow (copies entire array on each mutation)
Best for: read-heavy, write-rare workloads
```

**Interview Point:**

> Fail-safe iterators never throw CME—they iterate snapshot or weakly consistent data. `CopyOnWriteArrayList` for read-heavy; `ConcurrentHashMap` for concurrent maps.

</details>

---

# 6. ConcurrentModificationException?

<details>
<summary>Show Answer</summary>

**Answer:**

`ConcurrentModificationException` is a **runtime exception** thrown when a collection is **structurally modified** while being iterated—detected by fail-fast iterators.

### When It Occurs

```java
List<Integer> list = new ArrayList<>();
list.add(1);
list.add(2);
list.add(3);

for (Integer n : list) {
    list.remove(n); // ❌ CME
}
```

### Structural Modifications That Trigger CME

* `add()`
* `remove()`
* `clear()`
* Any operation changing collection size/structure

### Modifications That Do NOT Trigger CME

* `set(index, element)` — replace without size change (List)
* `put(existingKey, value)` — update in Map

### Common Scenarios

```java
// ❌ Scenario 1: remove in for-each
for (String s : list) {
    list.remove(s);
}

// ❌ Scenario 2: add during iteration
for (String s : list) {
    list.add("new");
}

// ❌ Scenario 3: another thread modifies (multi-threaded)
// Thread 1 iterates, Thread 2 removes → CME
```

### Safe Alternatives

```java
// ✅ Iterator.remove()
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    if (it.next().equals("B")) it.remove();
}

// ✅ Java 8+ removeIf()
list.removeIf(s -> s.equals("B"));

// ✅ Concurrent collection
CopyOnWriteArrayList<String> safe = new CopyOnWriteArrayList<>(list);
```

### Exception Message

```text
java.util.ConcurrentModificationException
```

**Interview Point:**

> CME = fail-fast safety net for single-threaded misuse. Fix with `iterator.remove()`, `removeIf()`, or concurrent collections—not external synchronization on fail-fast iterators.

</details>

---

# 7. How does modCount work?

<details>
<summary>Show Answer</summary>

**Answer:**

`modCount` is an **internal counter** in collection classes that tracks the number of **structural modifications** (add/remove). Iterators use it for **fail-fast** detection.

### Internal Field

```java
// Conceptual — in ArrayList, HashMap, etc.
transient int modCount = 0;  // structural change counter
```

### When modCount Increments

| Operation | modCount |
|-----------|----------|
| `add()` | +1 |
| `remove()` | +1 |
| `clear()` | +1 |
| `set()` (replace) | No change |
| `get()` | No change |

### Iterator Tracking

```java
// When iterator is created:
int expectedModCount = modCount;

// On each next():
if (modCount != expectedModCount) {
    throw new ConcurrentModificationException();
}
```

### Flow Diagram

```text
Collection created     modCount = 0
add("A")               modCount = 1
add("B")               modCount = 2
Iterator created       expectedModCount = 2

next() → check 2 == 2 ✅
list.remove("A")       modCount = 3
next() → check 3 != 2 ❌ → ConcurrentModificationException
```

### Iterator.remove() Updates Correctly

```java
Iterator<String> it = list.iterator();
it.next();
it.remove();
// Iterator updates both modCount AND expectedModCount
// No exception on continued iteration
```

### Example

```java
List<String> list = new ArrayList<>();
list.add("A");
list.add("B");

Iterator<String> it = list.iterator();
System.out.println(it.next()); // "A"

list.add("C"); // modCount changed externally

System.out.println(it.next()); // ❌ ConcurrentModificationException
```

### Limitations

| Limitation | Detail |
|------------|--------|
| Single-threaded focus | Not reliable for concurrent multi-thread access |
| Best-effort | Timing-dependent in concurrent scenarios |
| Subclasses must maintain | Custom collections must update modCount correctly |

### HashMap modCount

```java
// HashMap also uses modCount
// Incremented on put, remove, clear
// Iterator checks on each next()
```

**Interview Point:**

> `modCount` = structural change counter. Iterator stores snapshot at creation; mismatch on `next()` throws CME. `iterator.remove()` updates both counters safely.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Can Iterator traverse Map directly?

<details>
<summary>Show Answer</summary>

**Answer:** No. Use `map.keySet().iterator()`, `map.values().iterator()`, or `map.entrySet().iterator()`.

</details>

---

### Q: Is for-each loop fail-fast?

<details>
<summary>Show Answer</summary>

**Answer:** Yes. for-each uses Iterator internally—structural modification during loop throws `ConcurrentModificationException`.

</details>

---

### Q: Iterator vs Stream for traversal?

<details>
<summary>Show Answer</summary>

**Answer:** Iterator = manual loop, supports `remove()`. Stream = functional pipeline, lazy, no `remove()` during forEach—use `filter` + `collect` for transformations.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Iterator enables uniform forward traversal with optional remove. Fail-fast uses modCount to catch concurrent structural changes. For multi-threaded iteration use fail-safe collections like CopyOnWriteArrayList or ConcurrentHashMap.

</details>
