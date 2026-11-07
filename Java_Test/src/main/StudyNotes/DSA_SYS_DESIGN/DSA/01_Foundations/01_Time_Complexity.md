# Time complexity

## Simple idea

Big-O says how the work grows when the input grows. It ignores constants. A loop over n items is O(n). A loop inside a loop is O(n^2). A good sort is O(n log n).

## Picture

```text
n = 4

one loop          nested loops
i: 0 1 2 3        i=0, j=0..3
4 steps           i=1, j=0..3
                  i=2, j=0..3
                  i=3, j=0..3
                  16 steps
```

When n becomes 1,000, the first way does about 1,000 steps. The second does about 1,000,000.

## Java example

```java
int maxOf(int[] a) {          // O(n): one look at each slot
    int best = a[0];
    for (int x : a) {
        if (x > best) best = x;
    }
    return best;
}

boolean hasDuplicatePair(int[] a) {  // O(n^2): every pair
    for (int i = 0; i < a.length; i++) {
        for (int j = i + 1; j < a.length; j++) {
            if (a[i] == a[j]) return true;
        }
    }
    return false;
}
```

`Arrays.sort(a)` is O(n log n) for primitive `int`. `HashMap.get` is O(1) on average and O(n) if every key lands in one bucket.

## When to use it

Say the cost before you write the code. If n can be 10^5, an O(n^2) answer is usually too slow. O(n log n) and O(n) are the usual targets.

**Corner case:** an empty array makes `a[0]` throw. Complexity does not save you from a missing length check. Also, `int` addition inside an O(n) loop can still overflow.

## Practice

- Sum of an array — one pass, O(n).
- Count pairs with a given difference after sorting — O(n log n).
- Print a multiplication table — O(n^2), and say so.
- Find a duplicate with a `HashSet` — O(n) time, O(n) extra memory.

## Interview Q&A — 6 to 8 years

### S1. Why can two O(n) methods still feel different in production?

<details>
<summary>Show Answer</summary>

**Answer:**

Big-O hides the constant and the memory traffic. One pass that touches each `int` is cheap. One pass that builds a `String` with `+` inside the loop copies the whole string every time and becomes O(n^2). A `HashMap` lookup is O(1) on average, but a hash of a large object and a cache miss can dominate a tight loop over a primitive array.

**Corner cases:** `ArrayList` growth copies the backing array, so a loop of `add` without a capacity is still O(n) overall, with spikes. Worst-case `HashMap` is O(n) per call if every key collides. Java mitigates that by tree-ifying long bins, but a bad `hashCode` still hurts.

</details>

### S2. The interviewer says "optimize this." What do you measure first?

<details>
<summary>Show Answer</summary>

**Answer:**

The hot loop and the biggest term. If the method is already O(n) and n is small, a fancier algorithm will not show up. If it is O(n^2) at n = 10^5, change the algorithm. I also separate time from extra memory. An O(n) map can be the right trade when the O(n^2) scan is the outage.

**Corner cases:** Sorting a nearly sorted list still costs O(n log n) with `Arrays.sort` on primitives (dual-pivot quicksort is not stable and is not linear on sorted input). Object sort uses Timsort, which is faster on partial order. Micro-benchmarks lie if the JIT has not warmed up, or if you allocate inside the measured loop.

</details>

### S3. How do you talk about recursion cost?

<details>
<summary>Show Answer</summary>

**Answer:**

Time is the number of calls times the work per call. Space includes the call stack. A recursion that goes n deep uses O(n) stack frames even if you allocate nothing else. Java has no tail-call elimination. A deep recursion can throw `StackOverflowError` while the same idea written as a loop succeeds.

**Corner cases:** A balanced tree walk is O(n) time and O(log n) stack. A skewed tree is O(n) stack. Fibonacci written as two recursive calls without a cache is O(2^n), not O(n). Adding a memo table makes it O(n) time and O(n) memory.

</details>

### S4. A method is O(n) but fails on the second call. What happened?

<details>
<summary>Show Answer</summary>

**Answer:**

The first call probably changed the input. `Arrays.sort` sorts in place. The second call sees a different order, so a two-pointer answer that assumed the original order is now wrong, or a method that assumed unsorted data looks "too fast" and skips work. Shared mutable static maps also leak results from the previous test.

**Corner cases:** An iterator left open, or a list modified during `for-each`, throws `ConcurrentModificationException` on the next use. An `int` counter that overflowed to a negative number makes a later comparison look empty. I copy the array when the caller still needs the old order, and I reset scratch structures at the start of the method.

</details>

**Senior recap:** name the biggest term, watch hidden copies and hash costs, count stack frames, and do not leave the input mutated for the next call.

Next: [02_Bitwise_and_Number_System.md](02_Bitwise_and_Number_System.md)
