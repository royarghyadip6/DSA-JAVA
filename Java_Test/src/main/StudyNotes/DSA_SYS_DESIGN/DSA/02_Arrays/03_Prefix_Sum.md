# Prefix sum

## Simple idea

A prefix sum at index i is the sum of everything from the start through i. After you build that array once, any range sum from L to R is `prefix[R] - prefix[L - 1]`, which is O(1).

## Picture

```text
values:     2    4   -1    3
prefix:     2    6    5    8
              index 0  1  2  3

sum from index 1 to 3 = prefix[3] - prefix[0] = 8 - 2 = 6
which is 4 + -1 + 3
```

Put a 0 in front of the prefix array if you do not want a special case for L = 0.

```text
prefix:  0    2    6    5    8
index:       0    1    2    3
sum(L, R) = prefix[R + 1] - prefix[L]
```

## Java example

```java
long rangeSum(int[] a, int left, int right) {
    long[] prefix = new long[a.length + 1];
    for (int i = 0; i < a.length; i++) {
        prefix[i + 1] = prefix[i] + a[i];
    }
    return prefix[right + 1] - prefix[left];
}
```

Build is O(n). Each query is O(1). Many queries on a fixed array are the reason to bother.

## When to use it

Use it for many range-sum questions, for "subarray sum equals K" together with a map, and for difference arrays when you add a value on many ranges and ask for the final array once.

**Corner case:** `int` prefix overflows even when every element is small, because n can be large. Store prefix in `long`. Also `left = 0` is the off-by-one that people special-case wrong. The extra leading 0 removes that branch. A negative number in the array is fine. Prefix sums do not need a sorted array.

## Practice

- Range sum query, immutable array — build `long` prefix once.
- Find if any subarray sums to K — prefix plus a `HashMap` of how often each prefix appeared. That file is [Subarray sum equals K](../03_Hashing/02_Subarray_Sum_Equals_K.md).
- Difference array — mark `+v` at L and `-v` at R + 1, then one prefix pass.
- Equilibrium index — index where the left sum equals the right sum. `total - prefix[i] - a[i] == prefix[i] - a[i]`, with the leading-zero form in mind.

## Interview Q&A — 6 to 8 years

### S1. Why is the prefix array length n + 1, and what bug does length n cause?

<details>
<summary>Show Answer</summary>

**Answer:**

The extra slot holds 0, the sum before any element. Then every query is one formula. If prefix has length n and `prefix[i]` means sum through i, the query for L = 0 must not subtract `prefix[-1]`. People write `prefix[L - 1]` and crash, or they forget the branch and drop `a[0]`. I pay one extra cell and delete the branch.

**Corner cases:** R inclusive versus exclusive. I say it in the method name or the comment, because `substring` in Java is exclusive on the end and array problems are usually inclusive. An empty range (L > R) should be rejected, not return a negative "sum." Updating one element later makes the whole prefix stale. This structure is for a fixed array. If the array changes a lot, I use a Fenwick tree or a segment tree and I say the extra complexity.

</details>

### S2. The range sums are wrong only for large inputs. What happened?

<details>
<summary>Show Answer</summary>

**Answer:**

`int` overflow. The prefix wrapped, so a later subtraction compared wrapped values and looked plausible in small tests. I store prefix as `long`. If the problem asks for the sum modulo 10^9+7, I apply the modulus on every add and I fix a negative difference with `(diff % MOD + MOD) % MOD`, because Java `%` keeps the sign of the dividend.

**Corner cases:** `Long` overflow is still possible if the problem uses huge values. Then the problem usually wants a modulo. Subtracting two `Integer` objects instead of `long` primitives can also null-pointer if a prefix slot was never set. I use a primitive array, not a list of boxed integers, on this path.

</details>

### S3. How does a difference array answer "add v on many ranges, then read the array"?

<details>
<summary>Show Answer</summary>

**Answer:**

I do not walk each range. I add `v` at index L and subtract `v` at R + 1. After all updates, one prefix sum spreads each add until its matching subtract. Build is O(1) per update and O(n) at the end. That beats O(range length) per update when there are many overlapping ranges.

**Corner cases:** R + 1 equal to n is a slot I must have. The difference array is length n + 1. A subtract past the end means the add runs to the last index, so the extra cell is the place I put "stop." Overlapping ranges just add their marks. Order of updates does not matter. If I need the array after every update, the trick loses its point and I prefix after each one, or I use a tree.

</details>

### S4. Prefix sums on a 2D grid. What is the formula, and where do people go out of bounds?

<details>
<summary>Show Answer</summary>

**Answer:**

`prefix[r + 1][c + 1]` is the sum of the rectangle from (0,0) through (r,c). A query from (r1,c1) to (r2,c2) is the big rectangle minus the part above and the part to the left, plus the corner I subtracted twice. The leading zero row and zero column make every index in that formula exist.

**Corner cases:** Forgetting to add the corner back. Using `int` for a grid of 200 by 200 is usually fine, and a grid of large values is not. A query rectangle that is inverted (r1 > r2). I reject it. Building the prefix by summing the whole rectangle from scratch at each cell is O(n^3) or worse and misses the point. Each cell is `grid + up + left - corner`, which is O(1) per cell.

</details>

**Senior recap:** one leading zero, prefix in `long`, difference marks for range updates, and add back the corner you subtracted twice on a grid.

Next: [04_Two_Pointers.md](04_Two_Pointers.md)
