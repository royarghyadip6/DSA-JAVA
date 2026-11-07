# Queue and deque

## Simple idea

A queue is first in, first out. The oldest item leaves first. A deque (double-ended queue) can add or remove at either end. BFS uses a queue. The sliding-window maximum uses a deque that keeps indexes of useful candidates, biggest at the front.

In Java, `ArrayDeque` is the queue and the deque. `addLast` / `removeFirst` is the queue. Do not use `LinkedList` as a queue unless you already hold a node.

## Picture

Queue:

```text
enqueue 1, then 2, then 3
front -> 1  2  3 <- back
dequeue returns 1
front -> 2  3
```

Sliding window maximum, k = 3:

```text
values: 1  3  2  5
deque stores indexes, values decreasing

window [1, 3, 2]: front index is 3's, because 3 beats 1 and 2 is smaller and still in the window
window [3, 2, 5]: 5 is bigger than both, deque holds only 5
answers: 3, 5
```

Before you read the front, drop indexes that have fallen out of the window (`index <= right - k`).

## Java example

```java
int[] slidingWindowMax(int[] a, int k) {
    if (k <= 0 || a.length < k) return new int[0];
    int[] ans = new int[a.length - k + 1];
    java.util.ArrayDeque<Integer> deque = new java.util.ArrayDeque<>();
    for (int i = 0; i < a.length; i++) {
        while (!deque.isEmpty() && deque.peekFirst() <= i - k) {
            deque.removeFirst(); // left the window
        }
        while (!deque.isEmpty() && a[deque.peekLast()] <= a[i]) {
            deque.removeLast();  // smaller and older, never the max again
        }
        deque.addLast(i);
        if (i >= k - 1) ans[i - k + 1] = a[deque.peekFirst()];
    }
    return ans;
}
```

Each index is added once and removed once, so this is O(n), not O(n * k).

## When to use it

Use a queue for BFS and for "process in arrival order." Use a deque when both ends move: window maximum, a structure that can push front and back, or 0-1 BFS (a weight of 0 goes in the front, a weight of 1 goes in the back). If you only need the largest k elements in the whole array, a heap is the usual tool, not a window deque.

**Corner case:** `removeFirst` on an empty deque throws. k larger than n. Equal values: `<=` when dropping from the back lets the newer equal index stay, which is what you want so the older equal can expire first. Using `<` keeps both, which is also correct and a bit more work. Forgetting to drop indexes that left the window returns a max from outside the window. A decreasing array makes the deque hold every index until they expire. That is still O(n) total.

## Practice

- Sliding window maximum — the method above.
- BFS shortest path in an unweighted grid — queue of cells, mark visited when you enqueue, not when you dequeue.
- First negative number in every window of size k — deque of indexes of negatives, or a simple queue of those indexes.
- Hit counter — queue of timestamps, drop the ones older than 5 minutes. Fine when hits per window are modest.

## Interview Q&A — 6 to 8 years

### S1. Why is the window maximum O(n) and not O(n * k)?

<details>
<summary>Show Answer</summary>

**Answer:**

A nested scan of each window is O(n * k). The deque never rescans a dead candidate. An index enters at the back once. It leaves either because a newer bigger value makes it useless, or because it slides out of the window. Those removals across the whole array are at most n. So the inner whiles are amortized O(n). I say amortized so a single window that clears the deque does not look like a contradiction.

**Corner cases:** All equal values. With `<=`, the deque stays size 1. With `<`, it can hold k indexes and still O(n). k = 1: the answer is the array. k = n: one answer, the global max, and the deque logic still works. Output length is `n - k + 1`, not n. An off-by-one writes past the answer array on the last index.

</details>

### S2. When do you mark a BFS node visited?

<details>
<summary>Show Answer</summary>

**Answer:**

When I enqueue it. If I mark it when I dequeue, the same neighbor can be enqueued many times before the first copy is processed. The path can still be correct and the queue can be huge. Marking on enqueue gives O(n) visits for a grid. I still check "already visited" before enqueue. The start cell is marked immediately.

**Corner cases:** A weighted graph. Plain BFS does not give the shortest path if edge weights are not all 1. I say that and I switch to Dijkstra. A grid where I can revisit a cell with a different state (a key I picked up). The visited mark is `(row, col, state)`, not the cell alone. An `int[]` as a queue element or a set key uses identity equality. I pack the coordinate into one int or a record. A queue shared between calls must be local. A field queue makes the second BFS start with leftover cells.

</details>

### S3. `ArrayDeque` as a queue versus `LinkedList` and `PriorityQueue`. How do you choose?

<details>
<summary>Show Answer</summary>

**Answer:**

`ArrayDeque` for a plain FIFO or a deque: contiguous buffer, no node per element, no random access. `LinkedList` when I already need a `List` and occasional deletes in the middle, which a window maximum does not need. `PriorityQueue` when the next item is the smallest priority, not the oldest. That is a heap, O(log n) per operation, and it is not FIFO. `PriorityQueue` does not accept null. `ArrayDeque` does not accept null either.

**Corner cases:** `PriorityQueue` iterator order is not sorted order. I must `poll`. Using it as a queue because "queue is in the name" processes the wrong element first. `ArrayDeque` is not thread-safe. A producer and a consumer need `ArrayBlockingQueue` or another concurrent queue. `offer` versus `add`: on a bounded queue, `offer` returns false when full, `add` throws. `ArrayDeque` is unbounded and grows, so both succeed until memory runs out.

</details>

### S4. A hit counter uses a queue of timestamps and falls over. What is the senior fix?

<details>
<summary>Show Answer</summary>

**Answer:**

One queue entry per hit stores every event. A hot key with millions of hits per window uses unbounded memory and the cleanup scan is the request. I bucket time (one count per second) in a circular array of 300 slots for a 5-minute window, or I use a library rate limiter. The deque lesson remains: drop data that has left the window from the front. The scale lesson is: do not store one object per event when a bucket count is enough.

**Corner cases:** Clocks go backward. A timestamp older than the previous one should not clear the window by accident. I use a monotonic source or I ignore a backwards step. Two servers have two windows. A user under the limit on each server is over the limit globally. I say that if this is a real limiter. The coding-problem version with a modest n can stay a queue. I mention the bucket when they ask "what if n is huge."

</details>

**Senior recap:** deque indexes for the window max, mark BFS visited on enqueue, `ArrayDeque` for FIFO, and do not store one object per event when a bucket will do.

Next folder: [Trees](../06_Trees_Heaps/01_Trees.md)
