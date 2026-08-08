# 13.1 List Interface

## List Interface

### ArrayList

---

# 1. Difference between ArrayList and LinkedList?

<details>
<summary>Show Answer</summary>

**Answer:**

| Feature | ArrayList | LinkedList |
|---------|-----------|------------|
| Internal structure | Dynamic **array** | **Doubly linked list** of nodes |
| Random access (`get(index)`) | Fast — O(1) | Slow — O(n) |
| Insert/remove at **beginning/middle** | Slow — O(n) | Fast at ends — O(1) at head/tail |
| Insert at **end** | Fast — O(1) amortized | Fast — O(1) |
| Memory | Less overhead (array only) | More overhead (node + 2 pointers per element) |
| Implements | `List` | `List`, `Deque`, `Queue` |
| Best for | Frequent reads, index access | Frequent insert/delete at ends, queue/deque use |
| Cache locality | Better (contiguous memory) | Poorer (scattered nodes) |

### Example

```java
List<String> arrayList = new ArrayList<>();
arrayList.add("A");
System.out.println(arrayList.get(0)); // fast index access

List<String> linkedList = new LinkedList<>();
linkedList.addFirst("A");   // efficient at head
linkedList.addLast("B");    // efficient at tail
```

**Interview Point:**

> Default choice is usually `ArrayList` unless you have proven frequent head insertions, deque operations, or no random access needs.

</details>

---

# 2. How ArrayList grows internally?

<details>
<summary>Show Answer</summary>

**Answer:**

`ArrayList` stores elements in an internal **object array** called `elementData`.

When the array is full and a new element is added:

1. Check if `size + 1` exceeds current capacity
2. If yes → **grow** the array (create new larger array)
3. Copy existing elements to the new array
4. Add the new element
5. Update `size`

### Internal Flow

```text
elementData = [10, 20, 30]  (capacity may be 10)
        ↓
add(40) — space available → direct add
        ↓
add(...) until full
        ↓
grow() → new array (larger capacity)
        ↓
copy old elements → add new element
```

### Grow Method (conceptual)

```java
// Simplified internal logic
int newCapacity = oldCapacity + (oldCapacity >> 1); // 1.5x growth
elementData = Arrays.copyOf(elementData, newCapacity);
```

### Example

```java
List<Integer> list = new ArrayList<>(); // default capacity 10

for (int i = 0; i < 15; i++) {
    list.add(i); // triggers growth when capacity exceeded
}
```

**Interview Point:**

> Growth involves **array resizing + copying**—that's why occasional `add()` at the end can be expensive (amortized O(1), but resize is O(n)).

</details>

---

# 3. Default capacity of ArrayList?

<details>
<summary>Show Answer</summary>

**Answer:**

The **default initial capacity** of `ArrayList` is **10**.

```java
List<String> list = new ArrayList<>();
// Internally creates array with capacity 10 (empty, size = 0)
```

### Custom Initial Capacity

```java
List<String> list = new ArrayList<>(50);
// Starts with capacity 50 — avoids early resizing
```

### Why It Matters (Production)

If you know approximate size upfront:

```java
List<Employee> employees = new ArrayList<>(expectedCount);
```

* Reduces number of resize operations
* Reduces memory copying during growth
* Better performance for large lists

**Interview Point:**

> Default capacity is **10**. Use constructor with initial capacity when you know list size to avoid repeated internal resizing.

</details>

---

# 4. Growth formula of ArrayList?

<details>
<summary>Show Answer</summary>

**Answer:**

When `ArrayList` needs to grow, new capacity is calculated as:

```text
newCapacity = oldCapacity + (oldCapacity >> 1)
```

Which equals **1.5 times** the old capacity (integer division).

### Examples

| Old Capacity | Growth Calculation | New Capacity |
|--------------|-------------------|--------------|
| 10 | 10 + (10 >> 1) = 10 + 5 | **15** |
| 15 | 15 + (15 >> 1) = 15 + 7 | **22** |
| 22 | 22 + (22 >> 1) = 22 + 11 | **33** |

### Source Logic (OpenJDK style)

```java
int newCapacity = oldCapacity + (oldCapacity >> 1);

if (newCapacity < minCapacity) {
    newCapacity = minCapacity;
}
```

### Why 1.5x?

* Balance between **memory waste** and **resize frequency**
* Too small growth → frequent expensive copies
* Too large growth → wasted heap memory

**Interview Point:**

> ArrayList grows by **50%** (1.5x), not double like some other dynamic arrays. Know the bit-shift formula: `oldCapacity + (oldCapacity >> 1)`.

</details>

---

# 5. Difference between size() and capacity()?

<details>
<summary>Show Answer</summary>

**Answer:**

| Concept | Meaning |
|---------|---------|
| **size()** | Number of elements **actually stored** in the list |
| **capacity** | Length of internal backing **array** (slots available without resize) |

### Important Trap

```java
ArrayList<String> list = new ArrayList<>();
// ❌ ArrayList has NO public capacity() method
```

`capacity()` exists on **legacy `Vector`**, not on `ArrayList`.

### Example — size vs internal capacity

```java
ArrayList<String> list = new ArrayList<>(); // capacity = 10, size = 0

list.add("A");
list.add("B");

System.out.println(list.size()); // 2

// Internal array may still have capacity 10
// but only 2 slots are "used" (size = 2)
```

### Vector (has capacity())

```java
Vector<String> vector = new Vector<>();
vector.add("A");
System.out.println(vector.size());      // 1
System.out.println(vector.capacity());  // 10 (default)
```

### Relationship

```text
size() ≤ capacity (internal array length)
```

**Interview Point:**

> `size()` = elements count. Capacity is internal array size—`ArrayList` does not expose it publicly; only `Vector` has `capacity()`.

</details>

---

# 6. Why random access is fast in ArrayList?

<details>
<summary>Show Answer</summary>

**Answer:**

`ArrayList` uses a **contiguous dynamic array** internally.

To access element at index `i`:

```text
memory address = base address + (i × element size)
```

No traversal needed — **direct index calculation**.

### Internal Access

```java
E element = elementData[index]; // O(1)
```

### Example

```java
List<Integer> list = new ArrayList<>();
list.add(10);
list.add(20);
list.add(30);

System.out.println(list.get(2)); // O(1) — direct array access
```

### Why LinkedList is Slow for This

`LinkedList` must traverse from `head` (or `tail`) node by node:

```text
index 0 → node → node → node → target  (O(n))
```

### CPU Cache Benefit

Array elements sit in contiguous memory → better **cache locality** → faster real-world performance.

**Interview Point:**

> ArrayList random access is O(1) because it is backed by an array with direct index addressing.

</details>

---

# 7. Why insertion in middle is slow?

<details>
<summary>Show Answer</summary>

**Answer:**

Inserting at index `i` in `ArrayList` requires:

1. **Shift** all elements from index `i` to `size-1` one position to the right
2. Place new element at index `i`
3. Increment `size`

### Visual

```text
Before: [A, B, C, D, _]
Insert X at index 2

Step 1: Shift C, D → [A, B, _, C, D]
Step 2: Insert X      → [A, B, X, C, D]
```

### Time Complexity

**O(n)** — proportional to number of elements after insertion point.

### Example

```java
List<String> list = new ArrayList<>();
list.add("A");
list.add("B");
list.add("C");

list.add(1, "X"); // insert in middle — shifts B and C
```

### Worst Case

Insert at index **0** → shift **entire array** → most expensive.

### If Array is Full

Additional **resize + copy** on top of shifting → even more costly.

**Interview Point:**

> Middle insertion is O(n) due to element shifting. Avoid frequent middle inserts on large `ArrayList`—consider `LinkedList` for head operations or batch strategies.

</details>

---

# 8. Internal structure of ArrayList.

<details>
<summary>Show Answer</summary>

**Answer:**

### Core Fields (conceptual)

```java
public class ArrayList<E> {

    transient Object[] elementData;  // backing array
    private int size;                // actual element count
    // modCount for fail-fast iterator
}
```

### Memory Layout

```text
ArrayList object
    ├── size = 3
    ├── elementData → [ ref0 | ref1 | ref2 | null | null | ... ]
    │                    "A"    "B"    "C"   (unused slots)
    └── modCount
```

### Key Properties

| Field | Purpose |
|-------|---------|
| `elementData` | Stores element references |
| `size` | Number of valid elements |
| `capacity` | `elementData.length` (may be > size) |
| `modCount` | Tracks structural modifications for iterators |

### Example

```java
ArrayList<String> list = new ArrayList<>(3);
list.add("Java");
list.add("Python");
list.add("Go");

// size = 3, capacity may still be 3 or more after growth
```

**Interview Point:**

> `ArrayList` is a **resizable array wrapper**—not a linked structure. Understanding `elementData`, `size`, and growth is essential for performance interviews.

</details>

---

# 9. Time complexity of add(), remove(), get().

<details>
<summary>Show Answer</summary>

**Answer:**

### ArrayList — Time Complexity

| Operation | Complexity | Notes |
|-----------|------------|-------|
| `get(index)` | **O(1)** | Direct array access |
| `add(element)` — end | **O(1)** amortized | O(n) when resize needed |
| `add(index, element)` | **O(n)** | Shift elements right |
| `remove(index)` | **O(n)** | Shift elements left |
| `remove(element)` | **O(n)** | Search + shift |
| `contains(element)` | **O(n)** | Linear search |
| `set(index, element)` | **O(1)** | Replace at index |

### Example

```java
List<Integer> list = new ArrayList<>();

list.get(5);           // O(1)
list.add(100);         // O(1) amortized at end
list.add(2, 50);       // O(n) — shift required
list.remove(0);        // O(n) — shift all elements
```

### Amortized O(1) for End Add

Occasional resize is O(n), but spread over many adds → **amortized constant time**.

**Interview Point:**

> Know the table cold: `get` and end `add` are fast; middle `add`/`remove` are O(n) due to shifting.

</details>

---

# 10. Fail-fast behavior in ArrayList.

<details>
<summary>Show Answer</summary>

**Answer:**

`ArrayList` iterators are **fail-fast**—they throw `ConcurrentModificationException` if the list is structurally modified while iterating (except via iterator's own `remove()`).

### How It Works

* `ArrayList` maintains `modCount` (modification count)
* Iterator stores `expectedModCount` at creation
* On each `next()`, iterator checks:

```text
if (modCount != expectedModCount) → throw ConcurrentModificationException
```

### Example — Fail-Fast

```java
List<String> list = new ArrayList<>();
list.add("A");
list.add("B");
list.add("C");

for (String s : list) {
    list.remove(s); // ❌ structural change during iteration
}
// ConcurrentModificationException
```

### Safe Removal During Iteration

```java
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    String s = it.next();
    if (s.equals("B")) {
        it.remove(); // ✅ safe — updates modCount correctly
    }
}
```

### Fail-Fast vs Fail-Safe

| Type | Example | Behavior |
|------|---------|----------|
| Fail-fast | `ArrayList`, `HashMap` | Throws exception immediately |
| Fail-safe | `ConcurrentHashMap`, `CopyOnWriteArrayList` | Works on snapshot/copy |

**Interview Point:**

> Fail-fast detects concurrent structural modification via `modCount`. Use iterator.remove() or Java 8+ `removeIf()` for safe in-place removal.

</details>

---

### LinkedList

---

# 11. Internal structure of LinkedList?

<details>
<summary>Show Answer</summary>

**Answer:**

`LinkedList` in Java is a **doubly linked list** of nodes.

### Node Structure (conceptual)

```java
static class Node<E> {
    E item;
    Node<E> next;
    Node<E> prev;
}
```

### LinkedList Fields

```java
public class LinkedList<E> {
    transient Node<E> first;  // head
    transient Node<E> last;   // tail
    transient int size;
}
```

### Memory Layout

```text
first → [prev| A |next] ↔ [prev| B |next] ↔ [prev| C |next] → last
```

### Example

```java
LinkedList<String> list = new LinkedList<>();
list.add("A");
list.add("B");
list.add("C");

// Internally: first → A ↔ B ↔ C ← last
```

### Also Implements

* `Deque` — `addFirst()`, `addLast()`, `pollFirst()`
* `Queue` — `offer()`, `poll()`

**Interview Point:**

> Java `LinkedList` is a **doubly linked list** with `first` and `last` pointers—enables O(1) operations at both ends.

</details>

---

# 12. Why insertion is faster in LinkedList?

<details>
<summary>Show Answer</summary>

**Answer:**

Insertion at **head or tail** in `LinkedList` is **O(1)**—only pointer updates, no shifting.

### Insert at Head

```text
Before: first → B ↔ C

addFirst(A):

newNode → A
A.next = B
B.prev = A
first = A
```

### Insert at Tail

```text
last → new node, update prev/next pointers — O(1)
```

### Example

```java
LinkedList<String> list = new LinkedList<>();

list.addFirst("Task3");  // O(1)
list.addFirst("Task2");  // O(1)
list.addFirst("Task1");  // O(1) — no array shifting
```

### Compare with ArrayList Head Insert

```java
List<String> list = new ArrayList<>();
list.add(0, "X"); // O(n) — must shift ALL elements
```

### Middle Insertion Note

Insert at arbitrary **middle index** is still **O(n)** in `LinkedList` because you must **traverse** to find the position first.

**Interview Point:**

> LinkedList wins for frequent insert/delete at **ends** (or when using as deque/queue), not for random middle access.

</details>

---

# 13. Why searching is slower?

<details>
<summary>Show Answer</summary>

**Answer:**

`LinkedList` has **no direct index access**. To find element at index `i` or search for a value:

* Start from `head` (or `tail` if closer)
* Traverse node by node until target found

### Time Complexity

| Operation | LinkedList |
|-----------|------------|
| `get(index)` | **O(n)** |
| `contains(element)` | **O(n)** |
| `indexOf(element)` | **O(n)** |

### Visual — get(3)

```text
head → node0 → node1 → node2 → node3
         ↑ traverse 3 steps
```

### Example

```java
LinkedList<Integer> list = new LinkedList<>();
for (int i = 0; i < 100000; i++) {
    list.add(i);
}

list.get(99999); // must traverse ~99999 nodes — slow
```

### ArrayList Comparison

```java
ArrayList<Integer> list = new ArrayList<>();
list.get(99999); // O(1) — direct array index
```

### CPU Cache

Linked nodes are scattered in heap → poor cache locality → slower than array traversal even for similar Big-O in some cases.

**Interview Point:**

> No index-based addressing in linked lists—every `get(index)` is a linear walk from head/tail.

</details>

---

# 14. Doubly LinkedList or Singly LinkedList?

<details>
<summary>Show Answer</summary>

**Answer:**

Java's `LinkedList` is a **Doubly Linked List**.

Each node has:

```java
Node<E> prev;  // previous node
E item;        // data
Node<E> next;  // next node
```

### Why Doubly Linked?

| Capability | Singly Linked | Doubly Linked |
|------------|---------------|---------------|
| Forward traversal | ✅ | ✅ |
| Backward traversal | ❌ | ✅ |
| `removeLast()` efficiently | ❌ O(n) | ✅ O(1) |
| `addFirst()` / `addLast()` | Partial | ✅ Both O(1) |
| `Deque` operations | Limited | Full support |

### Example — Bidirectional Navigation

```java
LinkedList<String> list = new LinkedList<>();
list.add("A");
list.add("B");
list.add("C");

list.getFirst();  // O(1) via first pointer
list.getLast();   // O(1) via last pointer

list.removeLast(); // O(1) — prev pointer helps
```

### Memory Trade-off

Doubly linked uses **extra pointer per node** (`prev`) → more memory than singly linked.

**Interview Point:**

> Java `LinkedList` is **doubly linked** to support `Deque` API efficiently—especially `removeLast()` and backward iteration.

</details>

---

# 15. Time complexity analysis.

<details>
<summary>Show Answer</summary>

**Answer:**

### LinkedList — Time Complexity

| Operation | Complexity | Notes |
|-----------|------------|-------|
| `get(index)` | **O(n)** | Traverse to index |
| `addFirst()` / `offer()` | **O(1)** | Update head pointers |
| `addLast()` / `add()` | **O(1)** | Update tail pointers |
| `add(index, element)` | **O(n)** | Traverse + link |
| `removeFirst()` / `poll()` | **O(1)** | Unlink head |
| `removeLast()` | **O(1)** | Unlink tail (doubly linked) |
| `remove(index)` | **O(n)** | Traverse + unlink |
| `contains(element)` | **O(n)** | Linear search |
| `set(index, element)` | **O(n)** | Traverse to index |

### ArrayList vs LinkedList Summary

| Operation | ArrayList | LinkedList |
|-----------|-----------|------------|
| `get(i)` | O(1) | O(n) |
| `add(end)` | O(1)* | O(1) |
| `add(0)` | O(n) | O(1) |
| `add(middle)` | O(n) | O(n) |
| `remove(0)` | O(n) | O(1) |
| Memory | Lower | Higher (per-node overhead) |

\* Amortized O(1) for ArrayList end add

**Interview Point:**

> LinkedList is O(1) at ends, O(n) everywhere else. ArrayList is O(1) for get, O(n) for head insert/remove.

</details>

---

### Scenario

---

# 16. When would you choose LinkedList over ArrayList?

<details>
<summary>Show Answer</summary>

**Answer:**

Choose `LinkedList` when your access pattern favors **end/head operations** or **deque/queue behavior**, not random index access.

### ✅ Choose LinkedList When

| Scenario | Reason |
|----------|--------|
| Frequent `addFirst()` / `removeFirst()` | O(1) at head |
| Implementing a **queue** or **deque** | Native `Deque` API |
| **FIFO** task processing | `offer()` / `poll()` efficient |
| No frequent `get(index)` calls | Avoid O(n) traversal |
| Undo/redo stacks with deque ops | `push()` / `pop()` at ends |

### Example — Task Queue

```java
Deque<String> taskQueue = new LinkedList<>();

taskQueue.offer("Job1");
taskQueue.offer("Job2");

String job = taskQueue.poll(); // FIFO — O(1)
```

### Example — Frequent Head Inserts

```java
LinkedList<LogEntry> logs = new LinkedList<>();

logs.addFirst(newLog); // O(1) — new log at top
```

### ❌ Do NOT Choose LinkedList When

| Scenario | Better Choice |
|----------|---------------|
| Frequent `get(i)` by index | `ArrayList` |
| Mostly append + read by index | `ArrayList` |
| Large list, mostly iteration only | `ArrayList` (better cache locality) |
| General-purpose default list | `ArrayList` |

### Production Reality (5+ Years)

> In most enterprise codebases, **`ArrayList` is the default**. `LinkedList` is chosen only when deque/queue semantics or head-insert patterns are dominant.

**Interview Point:**

> LinkedList over ArrayList when: deque/queue usage, frequent head/tail mutations, no random access. Otherwise default to ArrayList.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Is LinkedList thread-safe?

<details>
<summary>Show Answer</summary>

**Answer:** No. Use `Collections.synchronizedList(new LinkedList<>())` or concurrent alternatives like `ConcurrentLinkedQueue` for thread-safe queue needs.

</details>

---

### Q: Can ArrayList shrink when elements are removed?

<details>
<summary>Show Answer</summary>

**Answer:** No. Removing elements reduces `size()` but **does not shrink** internal array capacity. Memory stays allocated until list is cleared or replaced.

</details>

---

### Q: Which is better for binary search on a list?

<details>
<summary>Show Answer</summary>

**Answer:** `ArrayList` — requires random access. `Collections.binarySearch()` needs O(1) `get(index)`, which LinkedList cannot provide efficiently.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> `ArrayList` is a resizable array—fast reads, slow middle inserts. `LinkedList` is a doubly linked deque—fast head/tail ops, slow indexed access. Default to `ArrayList`; choose `LinkedList` for queue/deque patterns or heavy front mutations.

</details>
