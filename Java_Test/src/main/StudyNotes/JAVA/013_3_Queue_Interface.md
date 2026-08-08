# 13.3 Queue Interface

## Queue Interface

### PriorityQueue

---

# 1. What is PriorityQueue?

<details>
<summary>Show Answer</summary>

**Answer:**

`PriorityQueue` is an **unbounded queue** where elements are ordered by **priority**, not by insertion time.

* The **head** of the queue is always the **least** element (min-heap by default)
* Or the **greatest** element if a custom `Comparator` is provided
* Based on **priority heap** data structure
* Does **not** permit `null` elements

### Key Characteristics

| Feature | PriorityQueue |
|---------|---------------|
| Ordering | Priority / heap order |
| FIFO? | ❌ No |
| Bounded? | Unbounded (grows as needed) |
| Thread-safe? | ❌ No |
| `null` | ❌ Not allowed |
| Implements | `Queue`, `Serializable` |

### Example — Natural Ordering (min-heap)

```java
Queue<Integer> pq = new PriorityQueue<>();
pq.offer(30);
pq.offer(10);
pq.offer(20);

System.out.println(pq.poll()); // 10 — smallest first
System.out.println(pq.poll()); // 20
System.out.println(pq.poll()); // 30
```

### Real-World Use Cases

* Task scheduling by priority (high-priority jobs first)
* Dijkstra's algorithm
* Merge K sorted lists
* Top K / Kth largest problems
* Event-driven simulation

**Interview Point:**

> PriorityQueue is a **heap-backed priority queue**—always gives highest-priority (min or max) element at `poll()`, not the first inserted.

</details>

---

# 2. Internal data structure?

<details>
<summary>Show Answer</summary>

**Answer:**

`PriorityQueue` is internally implemented as a **binary heap** stored in a **dynamic array** (`Object[] queue`).

### Default: Min-Heap

```text
        10
       /  \
     20    30
    /  \
  40   50

Array: [10, 20, 30, 40, 50]
Index:  0   1   2   3   4
```

### Internal Fields (conceptual)

```java
public class PriorityQueue<E> {

    transient Object[] queue;  // heap array
    private int size;          // number of elements
    private final Comparator<? super E> comparator; // optional
}
```

### Heap Array Index Rules

| Relationship | Formula |
|--------------|---------|
| Parent of `i` | `(i - 1) / 2` |
| Left child of `i` | `2 * i + 1` |
| Right child of `i` | `2 * i + 2` |

### Example

```java
PriorityQueue<Integer> pq = new PriorityQueue<>();
pq.offer(50);
pq.offer(20);
pq.offer(80);
pq.offer(10);

// Internally stored as heap array, NOT sorted array
// Root (index 0) = smallest element
System.out.println(pq.peek()); // 10
```

**Interview Point:**

> Not a sorted list—a **heap array** where parent ≤ children (min-heap). Root is always min, but rest of array is not fully sorted.

</details>

---

# 3. Is insertion order maintained?

<details>
<summary>Show Answer</summary>

**Answer:**

**No.** `PriorityQueue` does **NOT** maintain **insertion order**.

It maintains **heap priority order**—the element with highest priority (min or max) is always at the head.

### Example — Insertion vs Removal Order

```java
Queue<Integer> pq = new PriorityQueue<>();
pq.offer(30);  // inserted first
pq.offer(10);  // inserted second
pq.offer(20);  // inserted third

// Removal order (priority, NOT insertion):
System.out.println(pq.poll()); // 10  — not 30
System.out.println(pq.poll()); // 20
System.out.println(pq.poll()); // 30
```

### Iterator Also Unordered

```java
PriorityQueue<Integer> pq = new PriorityQueue<>();
pq.add(30);
pq.add(10);
pq.add(20);

for (Integer n : pq) {
    System.out.println(n);
}
// May print: 10, 30, 20 — NOT sorted, NOT insertion order
// Iterator walks heap array directly
```

### Compare with FIFO Queue

```java
Queue<Integer> fifo = new LinkedList<>();
fifo.offer(30);
fifo.offer(10);
fifo.offer(20);

System.out.println(fifo.poll()); // 30 — insertion order (FIFO)
```

### If You Need Insertion Order

Use `LinkedList` or `ArrayDeque` as a `Queue`—not `PriorityQueue`.

**Interview Point:**

> PriorityQueue orders by **priority**, not insertion time. Iterator does not traverse in sorted or insertion order.

</details>

---

# 4. How sorting happens?

<details>
<summary>Show Answer</summary>

**Answer:**

`PriorityQueue` does **not sort the entire collection**. It uses a **heap** to ensure the **head** is always the highest-priority element.

### Sorting Mechanisms

| Approach | How It Works |
|----------|--------------|
| **Natural ordering** | Elements implement `Comparable` → min-heap via `compareTo()` |
| **Custom ordering** | Pass `Comparator` to constructor → min/max based on comparator |

### Natural Ordering (Min-Heap)

```java
PriorityQueue<Integer> pq = new PriorityQueue<>();
pq.offer(5);
pq.offer(1);
pq.offer(3);

System.out.println(pq.peek()); // 1 — smallest at head
```

### Custom Comparator (Max-Heap)

```java
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(
        (a, b) -> b - a); // reverse order

maxHeap.offer(5);
maxHeap.offer(1);
maxHeap.offer(3);

System.out.println(maxHeap.poll()); // 5 — largest first
```

### How Elements Are Ordered on Insert (siftUp)

```text
add(element)
    ↓
place at end of array
    ↓
compare with parent
    ↓
if smaller → swap up (siftUp)
    ↓
repeat until heap property restored
```

### How Head Is Removed (siftDown)

```text
poll()
    ↓
remove root, place last element at root
    ↓
compare with children
    ↓
swap with smaller child (siftDown)
    ↓
repeat until heap property restored
```

### Full Sort?

To get fully sorted output from PriorityQueue:

```java
PriorityQueue<Integer> pq = new PriorityQueue<>();
pq.addAll(List.of(30, 10, 20, 5));

while (!pq.isEmpty()) {
    System.out.println(pq.poll()); // 5, 10, 20, 30
}
// Repeated poll() gives sorted order — O(n log n) total
```

**Interview Point:**

> Heap maintains **partial order** for O(1) peek and O(log n) insert/remove—not full sorting. Poll repeatedly to extract sorted sequence.

</details>

---

### Advanced

---

# 5. Difference between Queue and PriorityQueue?

<details>
<summary>Show Answer</summary>

**Answer:**

| Feature | Queue (FIFO) | PriorityQueue |
|---------|--------------|---------------|
| Ordering | **FIFO** — First In, First Out | **Priority** — highest priority first |
| Head element | Oldest inserted | Min/max per ordering |
| Internal structure | Usually linked list / array deque | **Binary heap** array |
| Insertion order | ✅ Maintained | ❌ Not maintained |
| `null` | Allowed (most implementations) | ❌ Not allowed |
| Use case | Fair scheduling, BFS, task lines | Priority scheduling, algorithms |
| Implementations | `LinkedList`, `ArrayDeque` | `PriorityQueue` only |

### FIFO Queue Example

```java
Queue<String> fifo = new LinkedList<>();
fifo.offer("Task1");
fifo.offer("Task2");
fifo.offer("Task3");

System.out.println(fifo.poll()); // Task1 — first added, first removed
```

### PriorityQueue Example

```java
Queue<Integer> pq = new PriorityQueue<>();
pq.offer(30);
pq.offer(10);
pq.offer(20);

System.out.println(pq.poll()); // 10 — smallest priority value first
```

### Interface Relationship

```text
Queue (interface)
   ├── LinkedList     → FIFO
   ├── ArrayDeque     → FIFO / Deque
   └── PriorityQueue  → Priority-based
```

### When to Choose

| Choose FIFO Queue | Choose PriorityQueue |
|-------------------|----------------------|
| Fair task processing | Priority-based scheduling |
| BFS traversal | Top K problems |
| Simple buffering | Dijkstra / greedy algorithms |
| Order must match arrival | Urgent tasks first |

**Interview Point:**

> Queue = fairness (FIFO). PriorityQueue = urgency (heap order). Wrong choice breaks business logic in scheduling systems.

</details>

---

# 6. Heap implementation details?

<details>
<summary>Show Answer</summary>

**Answer:**

Java `PriorityQueue` uses a **binary min-heap** represented as an **array** (not linked nodes).

### Array Representation

```text
Heap tree:          Array:
      10           [10, 20, 30, 40, 50]
     /  \          index: 0  1   2   3   4
   20    30
  /  \
 40  50
```

### Index Formulas

```java
int parent = (i - 1) / 2;
int left   = 2 * i + 1;
int right  = 2 * i + 2;
```

### Heap Property (Min-Heap)

```text
parent ≤ left child
parent ≤ right child
```

Only root is guaranteed smallest—not entire array sorted.

### siftUp — On Insert (offer/add)

```java
// Conceptual siftUp
void siftUp(int k, E x) {
    while (k > 0) {
        int parent = (k - 1) / 2;
        if (compare(x, queue[parent]) >= 0) break;
        queue[k] = queue[parent];  // swap up
        k = parent;
    }
    queue[k] = x;
}
```

```text
Insert 5 into heap [10, 20, 30]:

Step 1: add 5 at end     → [10, 20, 30, 5]
Step 2: 5 < 20 → swap     → [10, 5, 30, 20]
Step 3: 5 < 10 → swap     → [5, 10, 30, 20]
```

### siftDown — On Poll (remove head)

```java
// Conceptual siftDown
void siftDown(int k, E x) {
    int half = size / 2;
    while (k < half) {
        int child = 2 * k + 1;       // left child
        int right = child + 1;
        if (right < size &&
            compare(queue[child], queue[right]) > 0)
            child = right;           // pick smaller child
        if (compare(x, queue[child]) <= 0) break;
        queue[k] = queue[child];
        k = child;
    }
    queue[k] = x;
}
```

```text
Poll from [5, 10, 30, 20]:

Step 1: remove 5, move 20 to root → [20, 10, 30]
Step 2: 20 > 10 → swap with left   → [10, 20, 30]
```

### Time Complexity

| Operation | Complexity |
|-----------|------------|
| `offer()` / `add()` | O(log n) — siftUp |
| `poll()` / `remove()` | O(log n) — siftDown |
| `peek()` | O(1) — root access |
| `contains()` | O(n) — linear scan |

### Default Initial Capacity

```java
PriorityQueue<Integer> pq = new PriorityQueue<>();
// Default internal array capacity = 11
// Grows when needed (similar to ArrayList)
```

### Max-Heap via Comparator

```java
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(
        Comparator.reverseOrder());

// Comparator flips comparison → largest at root
```

**Interview Point:**

> Know **siftUp** (insert) and **siftDown** (poll)—core heap operations. Array + index math, not linked tree nodes.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Is PriorityQueue thread-safe?

<details>
<summary>Show Answer</summary>

**Answer:** No. Use `PriorityBlockingQueue` for thread-safe priority queue in concurrent environments.

</details>

---

### Q: Can PriorityQueue store null?

<details>
<summary>Show Answer</summary>

**Answer:** No. `null` is not permitted—`NullPointerException` on insert. Natural ordering cannot compare null.

</details>

---

### Q: Does PriorityQueue iterator return sorted order?

<details>
<summary>Show Answer</summary>

**Answer:** No. Iterator walks the internal heap array in arbitrary order. Use repeated `poll()` for sorted extraction.

</details>

---

### Q: PriorityQueue vs TreeSet for getting min element?

<details>
<summary>Show Answer</summary>

**Answer:** Both O(log n) insert. `PriorityQueue` is better when you only need repeated min/max extraction. `TreeSet` is better when you need full sorted set with range queries (`headSet`, `tailSet`).

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> `PriorityQueue` is a binary min-heap in an array—O(log n) insert/remove, O(1) peek. It orders by priority not insertion order. Use `PriorityBlockingQueue` for thread-safe priority scheduling in production.

</details>
