# Arrays basics

## Simple idea

An array is a row of slots with a number on each slot (the index). You can jump to any slot in O(1). You find a max, a min, or a "leader" by walking the row once. A leader is a value bigger than everything on its right.

## Picture

```text
index:   0   1   2   3   4
values: 16  17   4   3   5
                     leader? 5 is a leader (nothing on the right)
                 3 is not (5 is bigger)
             4 is not
         17 is a leader
     16 is not
```

Walk from the right. Keep the biggest value seen so far. If the current value is bigger than that, it is a leader.

## Java example

```java
int maxOf(int[] a) {
    if (a == null || a.length == 0) {
        throw new IllegalArgumentException("empty");
    }
    int best = a[0];
    for (int i = 1; i < a.length; i++) {
        if (a[i] > best) best = a[i];
    }
    return best;
}

java.util.List<Integer> leaders(int[] a) {
    java.util.List<Integer> out = new java.util.ArrayList<>();
    int bestOnRight = Integer.MIN_VALUE;
    for (int i = a.length - 1; i >= 0; i--) {
        if (a[i] > bestOnRight) {
            out.add(a[i]);
            bestOnRight = a[i];
        }
    }
    java.util.Collections.reverse(out); // we collected right to left
    return out;
}
```

For `{16, 17, 4, 3, 5}` the leaders are `17` and `5`.

## When to use it

Use a plain scan when the answer depends on every element once: max, min, sum, leaders, a rotation count. If you need "have I seen this value before?", switch to a set. If the array must stay in the caller's order, do not sort it in place.

**Corner case:** empty and null are different. `a.length` on null throws `NullPointerException`. All-equal arrays: with `>` only the rightmost is a leader. If the problem says "greater or equal," every element is a leader. `Integer.MIN_VALUE` as a real element is fine here because we compare against values in the array, but using it as a "not found" sentinel is not fine when the array can contain it.

## Practice

- Second largest — one pass, watch duplicates so 7, 7 does not report 7 as both first and second.
- Move zeros to the end — write index, keep relative order of the other numbers.
- Rotate right by k — reverse the whole array, then the two parts. `k %= n` first.
- Leaders — scan from the right, as above.

## Interview Q&A — 6 to 8 years

### S1. When do you pick `int[]` over `ArrayList<Integer>`?

<details>
<summary>Show Answer</summary>

**Answer:**

`int[]` when the size is fixed and the hot path is numeric. There is no boxing, and the values sit next to each other in memory. `ArrayList<Integer>` when the size grows, or an API already speaks `List`. Boxing turns each value into an object. A tight loop over a boxed list allocates and misses cache.

**Corner cases:** `ArrayList` of `Integer` treats `remove(1)` as "remove index 1," not "remove the value 1," if the argument is an `int`. `Arrays.asList` on an `int[]` makes a list of one array, not a list of ints. A shared `int[]` that one request sorts will surprise the next request. I copy when the contract says the input stays unchanged.

</details>

### S2. Find the second largest in one pass. What are the traps?

<details>
<summary>Show Answer</summary>

**Answer:**

Keep `first` and `second`. Update `second` only when the new value is strictly less than `first` and greater than `second`. Initialize both to a signal that cannot be a real answer, or handle the first two distinct values explicitly. Duplicates of the max must not become the second max.

**Corner cases:** All values equal: there is no second largest. I return a clear "absent" (`OptionalInt`) instead of `Integer.MIN_VALUE`, because that value may be in the array. One element. Negative numbers, so I cannot start `second` at 0. Overflow is not the issue. The sentinel is.

</details>

### S3. Rotate the array by k. Why is the reverse method safer than a temporary buffer of size k?

<details>
<summary>Show Answer</summary>

**Answer:**

Three reverses rotate in place with O(1) extra memory: reverse all, reverse the first k, reverse the rest. A buffer of the last k elements is also correct and easier to read, and it uses O(k) memory. I take the buffer when k is small and clarity matters. I take reverses when the interviewer asks for O(1) extra space. Both are O(n) time.

**Corner cases:** `k` bigger than n, or negative. I normalize with `Math.floorMod(k, n)` so a negative k means a left rotate and I never get a negative index. n = 0. `k % n` throws if n is 0. Rotating a view or a sub-array by reversing past its range corrupts the parent. I pass the real bounds.

</details>

### S4. A method returns the max and the caller says it is wrong on the second call. Why?

<details>
<summary>Show Answer</summary>

**Answer:**

The method sorted or swapped the array. The max is still somewhere, but the caller's later index logic uses the old positions. Or a static `best` field was not reset, so the second call with smaller numbers returns the first call's max. Interview code should keep state on the stack: local variables, not fields, unless the class is a real object with a clear lifecycle.

**Corner cases:** `Arrays.stream(a).max()` on an empty array throws `NoSuchElementException`. A parallel stream over a shared array that another thread writes is a data race. For interview DSA I stay single-threaded and I state that. An off-by-one (`i <= a.length`) throws `ArrayIndexOutOfBoundsException` only when the loop actually reaches that index, so a short test array may pass.

</details>

**Senior recap:** primitive arrays avoid boxing, sentinels collide with real data, normalize k before you rotate, and do not keep the answer in a static field.

Next: [02_Kadane.md](02_Kadane.md)
