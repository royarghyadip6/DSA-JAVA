# LRU cache

## Simple idea

LRU means least recently used. When the cache is full and a new key arrives, you evict the key nobody has touched for the longest time. `get` and `put` must both count as a use. Both should be O(1).

The structure is a hash map plus a doubly linked list. The map finds the node. The list keeps the order. The head side is most recent. The tail side is the one you evict.

## Picture

```text
map:  A -> nodeA    B -> nodeB    C -> nodeC

most recent                          least recent
head <-> A <-> B <-> C <-> tail

get(B): unlink B, move it next to head
head <-> B <-> A <-> C <-> tail

put(D) when capacity is 3: evict C (next to tail), then insert D at head
head <-> D <-> B <-> A <-> tail
```

Dummy head and dummy tail nodes mean the real list can be empty and you never unlink a null neighbor.

## Java example

```java
final class LruCache {
    private final int capacity;
    private final java.util.Map<Integer, Node> map = new java.util.HashMap<>();
    private final Node head = new Node(0, 0); // dummy
    private final Node tail = new Node(0, 0); // dummy

    LruCache(int capacity) {
        this.capacity = capacity;
        head.next = tail;
        tail.prev = head;
    }

    int get(int key) {
        Node node = map.get(key);
        if (node == null) return -1;
        moveToFront(node);
        return node.value;
    }

    void put(int key, int value) {
        Node node = map.get(key);
        if (node != null) {
            node.value = value;
            moveToFront(node);
            return;
        }
        Node created = new Node(key, value);
        map.put(key, created);
        insertAfterHead(created);
        if (map.size() > capacity) {
            Node oldest = tail.prev;
            unlink(oldest);
            map.remove(oldest.key);
        }
    }

    private void moveToFront(Node node) {
        unlink(node);
        insertAfterHead(node);
    }

    private void insertAfterHead(Node node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    private void unlink(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private static final class Node {
        final int key;
        int value;
        Node prev, next;
        Node(int key, int value) { this.key = key; this.value = value; }
    }
}
```

`LinkedHashMap` can do this in a library with `removeEldestEntry` and access-order. The interview usually wants the two structures drawn anyway.

## When to use it

Use an LRU when a bounded cache should prefer keys that were used lately: a small in-process cache in front of a database. It does not solve a stampede, and it is not shared across servers. One server's LRU is not another server's LRU.

**Corner case:** capacity 0 or negative. `put` of an existing key must update the value and the order, not evict. `get` must move the node, or the order is only "least recently inserted." Forgetting to remove the key from the map on eviction leaves a ghost. Forgetting to unlink before reinserting makes a loop. This class is not thread-safe. Two threads calling `put` corrupt the pointers.

## Practice

- Design LRU cache — the class above, capacity at least 1.
- LFU is the follow-up — counts plus a structure per frequency. Say it is more bookkeeping, and do not start it until LRU is solid.
- Use `LinkedHashMap` access-order — override `removeEldestEntry`. Know that this is the production shortcut.

## Interview Q&A — 6 to 8 years

### S1. Why is a map of keys to timestamps not enough for O(1) eviction?

<details>
<summary>Show Answer</summary>

**Answer:**

A timestamp tells you when a key was used. Finding the oldest key is a scan of the map, which is O(n), unless you add a second structure ordered by time. A `TreeMap` from time to key is O(log n) for eviction and breaks ties when two events share a millisecond. The doubly linked list gives O(1) move-to-front and O(1) eviction because the oldest node is always `tail.prev`. The map exists so I can find that node from the key without scanning the list.

**Corner cases:** Two operations in the same nanosecond if I used a clock. The list does not need a clock. A `LinkedHashMap` in insertion order, not access order, evicts the oldest insert and ignores `get`. I pass `accessOrder = true`. A max-heap of timestamps is O(log n) and lazy deletion is easy to get wrong when a key is updated and the old timestamp remains.

</details>

### S2. Walk through put of a key that is already present, on a full cache.

<details>
<summary>Show Answer</summary>

**Answer:**

I do not evict. I replace the value and I move that node to the front. Size stays the same. Eviction happens only when I insert a new key and `size` would exceed capacity. If I evict first and the key was already there, I throw away some other live key for no reason. If I insert a second node for the same key, the map points at one of them and the list holds a duplicate.

**Corner cases:** The key being updated is the one that would have been evicted (`tail.prev`). An update must save it. Capacity 1: `put` a new key replaces the only node. `get` on the only node stays at the front. A `put` that moves the node without unlinking first creates a cycle, and the next `get` never ends. I unlink, then insert.

</details>

### S3. How do you make this safe for more than one request thread?

<details>
<summary>Show Answer</summary>

**Answer:**

The pointer updates are not atomic. I synchronize the public methods on one lock, or I use a library cache that already defines concurrency (`Caffeine` is the usual Java choice). A `ConcurrentHashMap` alone does not keep the list order. If I lock, I keep the critical section to the map and the list. I do not call the database inside the lock. I also decide what `get` does under contention: it must still move the node, so every `get` takes the same lock. That lock becomes hot. Then a per-process LRU is the wrong scale and I move the hot keys to Redis, accepting that eviction is approximate.

**Corner cases:** A read lock for `get` is wrong, because `get` writes the list order. Lock striping is hard because order is one global list. A bug that evicts outside the lock lets two threads delete the same node and unlink a neighbor that was already reused. I document "one thread" if this is interview code, and I mention the lock if they ask about a service.

</details>

### S4. What does this cache not do, even when the code is correct?

<details>
<summary>Show Answer</summary>

**Answer:**

It does not load the value on a miss. A real cache-aside `get` goes to the database on null and then `put`s. It does not collapse a stampede: a hundred misses on one key all hit the database. It does not expire by time unless I add a TTL check. It does not stay coherent with another server. And a `get` that returns -1 cannot store a real value of -1. I use `Integer` and null for "missing," or a boolean, when -1 is a legal value.

**Corner cases:** Caching a mutable list that the caller later edits. The cache sees the edit with no `put`. I store immutable values or a copy. A huge value that makes "capacity 100" still exhaust the heap. Capacity counts entries, not bytes. I mention a weight if values vary in size. Evicting an entry that holds a resource (a stream) without closing it leaks the resource. The node needs a close hook, or I do not cache that type.

</details>

**Senior recap:** map plus doubly linked list, `get` and update both move the node, evict only on a new key, and do not pretend this lock-free object is a shared cache.

Next: [03_Stack.md](03_Stack.md)
