# Kadane's algorithm

## Simple idea

Find the contiguous subarray with the largest sum, in one pass. At each index you choose: start a new subarray here, or extend the one you were already building. You keep the best sum you have ever seen.

## Picture

```text
values:  -2   1   -3   4   -1   2   1   -5   4
extend:  -2   1   -2   4    3   5   6    1   5
best:    -2   1    1   4    4   5   6    6   6

At 4, starting fresh (4) beats extending (-2).
The winning slice is [4, -1, 2, 1] and the sum is 6.
```

## Java example

```java
int maxSubarraySum(int[] a) {
    int best = a[0];
    int cur = a[0];
    for (int i = 1; i < a.length; i++) {
        cur = Math.max(a[i], cur + a[i]); // start here, or extend
        best = Math.max(best, cur);
    }
    return best;
}
```

This is O(n) time and O(1) extra memory. The brute force of every start and end is O(n^2).

If you also need the slice, remember the start index whenever you begin a new `cur`, and remember start and end whenever you update `best`.

## When to use it

Use it when the problem says "contiguous" and "maximum sum." It is not for a subsequence that can skip elements. It is not for a circular array unless you add a second pass (max of the normal answer, or total sum minus the minimum subarray).

**Corner case:** if every number is negative, the answer is the largest number, not 0. The code above does that, because `cur` never prefers an empty sum. A version that resets `cur` to 0 when it goes negative returns 0 on an all-negative array, which is wrong unless the problem allows an empty subarray.

Adding in `int` can overflow. If the values can be near `Integer.MAX_VALUE`, keep `cur` and `best` in `long`.

## Practice

- Maximum subarray sum — the loop above.
- Maximum subarray with the indexes — track start and end.
- Maximum circular subarray sum — Kadane, plus total minus minimum subarray. If the minimum is the whole array, fall back to the normal Kadane (all negative).
- Maximum product subarray — track both max and min ending here, because a negative times a negative becomes large.

## Interview Q&A — 6 to 8 years

### S1. Why is "reset to 0 when the sum goes negative" dangerous?

<details>
<summary>Show Answer</summary>

**Answer:**

It treats an empty subarray of sum 0 as allowed. On `[-3, -1, -2]` that version returns 0, but every real slice is negative and the answer is `-1`. I initialize `best` and `cur` to `a[0]` and I always take `max(a[i], cur + a[i])`. I only use the reset-to-0 form when the problem statement allows an empty answer.

**Corner cases:** A single negative element. An array that starts negative and then has a positive run: both forms can agree. The tests that only use mixed signs hide the bug. I include one all-negative case in every review.

</details>

### S2. The sum is correct and still wrong in Java. What type failed?

<details>
<summary>Show Answer</summary>

**Answer:**

`int` overflow. `cur + a[i]` wraps to a negative number and looks like a bad extension, so you drop a legal slice. I accumulate in `long`. If the required return type is `int`, I still compute in `long` and I define what happens when the true sum does not fit. Silent wrap is not a business answer.

**Corner cases:** `Integer.MAX_VALUE` plus a positive number. A product variant overflows even sooner. `Math.max` on two overflowed ints compares the wrapped values and picks the wrong one. Using `BigInteger` is correct and too slow for a tight interview loop unless the constraints demand it.

</details>

### S3. How do you extend Kadane to a circular array without double counting?

<details>
<summary>Show Answer</summary>

**Answer:**

The max wrap-around sum equals the total sum minus the minimum contiguous subarray in the middle. I compute normal Kadane and that "total - minSubarray" and take the larger one. If the minimum subarray is the entire array, every element is negative (or the wrap would remove everything). Then the wrap answer is 0 or empty, and I must return the normal Kadane instead.

**Corner cases:** One element. All positive: the min subarray is the smallest element, and total minus that is correct and usually worse than the whole array, which normal Kadane already has. Two elements where wrapping is better: `{5, -1, 5}` is not only circular. A true circular example is `{5, -3, 5}`: normal max is 5, total 7, min subarray is -3, wrap sum is 10, which is `5 + 5`. I check I did not use the same element twice by construction: the removed part is the middle, the kept part is the prefix plus the suffix.

</details>

### S4. Maximum product subarray is not Kadane with `*`. Why?

<details>
<summary>Show Answer</summary>

**Answer:**

A negative number flips the ranking. The smallest (most negative) product ending here can become the largest after the next negative. I keep both `maxEnding` and `minEnding`. At each step the candidates are the value itself, `maxEnding * a[i]`, and `minEnding * a[i]`. Zeros reset both to the next value, because a product through zero dies.

**Corner cases:** `{ -2, 0, -1 }` answer is 0, not -1, if a single element is allowed, and 0 is the best. `{ -2, 3, -4 }` answer is 24. Even count of negatives. Overflow of the product in `int` long before the sum would overflow. I use `long`, and I still watch `long` if the problem allows big values. An empty array has no product. I reject it up front.

</details>

**Senior recap:** do not return 0 for an all-negative array, add in a wider type, circular means total minus the min slice, and product needs both the min and the max.

Next: [03_Prefix_Sum.md](03_Prefix_Sum.md)
