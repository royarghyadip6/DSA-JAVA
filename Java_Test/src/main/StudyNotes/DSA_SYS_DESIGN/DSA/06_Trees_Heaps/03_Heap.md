# Heap

## Simple idea

A heap is a complete binary tree stored in an array. A min-heap says: every parent is less than or equal to its children. The smallest value sits at the top. You can add a value or remove the top in O(log n). You cannot search for an arbitrary value quickly.

Java's `PriorityQueue` is a min-heap. The smallest element comes out of `poll` first. It is not sorted when you iterate it.

## Picture

```text
array: [1, 4, 2, 8, 5]

index 0 is the root
parent of i      = (i - 1) / 2
left child of i  = 2 * i + 1
right child of i = 2 * i + 2

        1
       / \
      4   2
     / \
    8   5

poll() returns 1, then the last value moves to the root and sifts down
```

A max-heap is the same with the comparison flipped. In Java: `new PriorityQueue<>(Comparator.reverseOrder())` for integers, or store negative numbers only when the values cannot overflow.

## Java example

```java
int[] topKLargest(int[] a, int k) {
    if (k <= 0) return new int[0];
    java.util.PriorityQueue<Integer> minHeap = new java.util.PriorityQueue<>();
    for (int value : a) {
        minHeap.offer(value);
        if (minHeap.size() > k) minHeap.poll(); // drop the smallest of the kept ones
    }
    int[] out = new int[minHeap.size()];
    for (int i = out.length - 1; i >= 0; i--) out[i] = minHeap.poll();
    return out;
}
```

A heap of size k keeps the k largest values. Total time is O(n log k), which beats sorting O(n log n) when k is much smaller than n. Memory is O(k).

`offer` is O(log n). `peek` is O(1). `poll` is O(log n). Building a heap from n items with the library, one offer at a time, is O(n log n). The bottom-up heapify algorithm is O(n). `PriorityQueue` has a constructor that takes a collection and heapifies it.

## When to use it

Use a heap for top-K, for "merge K sorted lists," for a median (two heaps), and for Dijkstra. Use a sort when you need every item in order and you will read them all. Use a BST or `TreeMap` when you need to delete an arbitrary key, not only the top. `PriorityQueue.remove(object)` is O(n).

**Corner case:** `PriorityQueue` does not allow null. `peek` and `poll` on empty return null for this queue, and unboxing into `int` throws. Size can be smaller than k if the array is short. A max-heap built with negative numbers breaks on `Integer.MIN_VALUE`. A comparator that is not transitive makes the heap order nonsense. Iteration order is not priority order. Two equal priorities: the heap does not promise which one comes first unless the comparator breaks the tie.

## Practice

- Kth largest — min-heap of size k, or quickselect if you want average O(n) and you may modify the array.
- Top K frequent elements — count in a map, then a heap of size k on the counts.
- Merge K sorted lists — heap of the current head of each list, with a pointer back to the list.
- Find median from a data stream — a max-heap for the lower half and a min-heap for the upper half, sizes kept within one.
- K closest points — heap of size k by distance. Compare distance in `long` so you do not square-overflow.

## Interview Q&A — 6 to 8 years

### S1. You want the k largest. Why a min-heap of size k, not a max-heap of size n?

<details>
<summary>Show Answer</summary>

**Answer:**

A max-heap of every element costs O(n) memory and O(n log n) to build if I insert one by one, and then k polls are cheap. A min-heap that never grows past k costs O(k) memory and O(n log k). When n is huge and k is 10, that is the difference between holding the stream and holding ten numbers. The smallest of those ten is on top, so I can reject a new number that is even smaller in O(log k).

**Corner cases:** k larger than n. The heap's size is the answer length. k = 0. A stream that never ends: the bounded heap still works and a full sort does not. Duplicates are allowed. If the problem says distinct, I have to filter before the heap. `poll` until empty does not return the k largest in sorted order unless I poll them all. The heap only promised to contain them.

</details>

### S2. `PriorityQueue` is not a stable sorted list. What bug does that cause?

<details>
<summary>Show Answer</summary>

**Answer:**

The iterator and `toArray` do not walk in priority order. Only `poll` does. A debugger that prints the queue looks shuffled. A second bug is assuming insertion order among equal keys. The heap is not stable. If I need "earlier task first" when priorities match, the comparator must compare a sequence number after the priority. I must not use `==` on boxed values to find an element.

**Corner cases:** `remove(value)` scans and uses `equals`, and it is O(n). Calling it in a loop to delete arbitrary items turns the algorithm into O(n^2). There is no decrease-key in the JDK queue. Dijkstra implementations often push a newer better pair and ignore stale pairs when they are polled, instead of updating in place. Forgetting to ignore the stale pair processes a node with a worse distance. `Comparator.comparingInt(a -> -a.score)` overflows if score is `Integer.MIN_VALUE`. I use `compare` on the ints.

</details>

### S3. How do two heaps answer "median so far"?

<details>
<summary>Show Answer</summary>

**Answer:**

The lower half lives in a max-heap. The upper half lives in a min-heap. I keep their sizes equal, or the lower half one larger. The median is the top of the lower half when it is larger, or the average of the two tops when the sizes match. Each new number goes to one side and I rebalance with at most one move. Each insert is O(log n).

**Corner cases:** Average of two ints done with `(a + b) / 2` overflows and truncates. I add in `long` and I return `double` if the problem wants a .5. Even and odd counts. The first number. A rebalance that moves the wrong top and leaves both heaps violating "every lower value <= every upper value." I always push to one heap and then move its top to the other, rather than inserting by a guess I do not repair.

</details>

### S4. Is a heap thread-safe, and what do you use in a service?

<details>
<summary>Show Answer</summary>

**Answer:**

`PriorityQueue` is not thread-safe. Two `offer` calls can break the array. For a shared work queue I use `PriorityBlockingQueue`, knowing that `take` waits and that it is unbounded unless I guard the size myself. It still has no efficient decrease-key. A scheduled job system in Java is usually `ScheduledExecutorService`, not a heap I wrote. I talk about the heap in the interview, and I name the library when the question becomes "in our service."

**Corner cases:** A bounded feel from checking `size()` then `offer()` is a race: two threads both see room. I need one lock around the check-and-offer, or a queue that rejects when full. `PriorityBlockingQueue` does not block on `offer` when you wish it were bounded. It grows. Iterator is weakly consistent. A comparator that calls into application code and locks can deadlock if that code tries to poll the same queue. I keep the comparator pure.

</details>

**Senior recap:** `PriorityQueue` is a min-heap, top-K is a heap of size k, only `poll` is ordered, and there is no cheap decrease-key in the JDK.

Next folder: [Graphs](../07_Graphs/01_Graph_Basics.md)
