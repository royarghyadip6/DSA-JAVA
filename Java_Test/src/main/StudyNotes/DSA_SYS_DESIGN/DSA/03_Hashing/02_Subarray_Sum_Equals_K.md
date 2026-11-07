# Subarray sum equals K

## Simple idea

A subarray is a contiguous stretch. You want the number of stretches whose sum is K. Prefix sums turn that into a question the map can answer: "how many earlier prefixes equal `currentPrefix - K`?"

## Picture

```text
values:        1    2    3         K = 3
prefix:        1    3    6
prefix - K:   -2    0    3

At prefix 3, we have seen prefix 0 (the empty start). 3 - 0 = 3. One hit: [1, 2] is not the only one.
The stretch [3] sums to 3 because 6 - 3 = 3, and prefix 3 was seen.

Stretches that sum to 3: [1, 2] and [3]. Count = 2.
```

If you only need "does any stretch sum to K?", you can stop at the first hit. The classic problem asks for the count.

## Java example

```java
int countSubarraysSumK(int[] a, int k) {
    java.util.Map<Long, Integer> seen = new java.util.HashMap<>();
    seen.put(0L, 1); // one way to have sum 0 before we start
    long prefix = 0;
    int count = 0;
    for (int value : a) {
        prefix += value;
        count += seen.getOrDefault(prefix - k, 0);
        seen.merge(prefix, 1, Integer::sum);
    }
    return count;
}
```

O(n) time and O(n) extra memory. Putting the current prefix into the map **after** the lookup stops a stretch from counting itself incorrectly. The initial 0 lets a prefix that is exactly K count as a subarray from the start.

## When to use it

Use it when the array has negatives or zeros, so a sliding window cannot shrink safely, and the stretch must be contiguous. If the array is only non-negative and you want the shortest stretch with sum at least K, use a sliding window instead. If you need the actual indexes of one stretch, store the first index of each prefix, not only the count.

**Corner case:** zeros create several stretches with the same sum. The map must count frequency, not store a boolean. Negatives mean the same prefix can appear, and "sum so far went down" is normal. `int` prefix wraps. I use `long`. An empty array returns 0. K = 0 is legal: the initial `seen 0 -> 1` must not by itself increment the count before any element. The code above only asks `prefix - k` after adding an element, so an empty sum is not reported as a hit until a real stretch sums to 0.

## Practice

- Count subarrays with sum K — the method above.
- Longest subarray with sum K — store the first index of each prefix, and skip later duplicates so the length stays the longest.
- Subarray sum divisible by M — store `prefix % M`, and fix a negative remainder.
- Binary subarrays with sum K — same map, or a window, because values are 0 and 1.
- Continuous array (equal 0s and 1s) — treat 0 as -1, then sum K = 0.

## Interview Q&A — 6 to 8 years

### S1. Why does the map store counts of prefixes, not the elements?

<details>
<summary>Show Answer</summary>

**Answer:**

The sum of a stretch ending at the current index and starting after an earlier index is `prefixNow - prefixThen`. If that difference is K, that earlier prefix is what I need. Many earlier indexes can share one prefix value when zeros and negatives exist. A count answers "how many starts." A set would only answer "is there one."

**Corner cases:** I insert the current prefix after I query. If I insert first, `prefix - k` can equal `prefix` when K is 0, and I count the empty stretch at this index. The seeded `{0: 1}` is the empty prefix before the array, which is the one empty sum I do want available as a start. Forgetting the seed misses every subarray that begins at index 0.

</details>

### S2. The count is too small on an array that contains negative numbers. What did a window solution get wrong?

<details>
<summary>Show Answer</summary>

**Answer:**

A sliding window that moves `left` when the sum exceeds K assumes that adding an element never decreases the sum. A negative element breaks that. You can skip a valid window. The prefix map does not assume order of the sums. It is the right tool as soon as the problem allows negatives or asks for a count of every window, not only the shortest positive one.

**Corner cases:** All negatives with K negative. The map still works. K = 0 with zeros: several overlapping subarrays. I need frequencies. Duplicate prefixes: longest-subarray variant must keep the earliest index only. If I overwrite with the later index, the length shrinks and I report a shorter answer.

</details>

### S3. How do you adapt this for "sum divisible by M"?

<details>
<summary>Show Answer</summary>

**Answer:**

Two prefixes with the same remainder modulo M differ by a multiple of M. I store counts of `floorMod(prefix, M)`. The seed is remainder 0 with count 1. At each step I add the count of the current remainder, then I record it. `Math.floorMod` handles a negative prefix. Java's `%` on a negative number is negative, and a negative map key would miss the positive remainder that is actually the same class.

**Corner cases:** M = 1: every subarray qualifies. The count is n * (n + 1) / 2, which itself overflows `int` for large n. I return `long`. M <= 0 is invalid. A prefix in `int` that overflows before the modulo is already the wrong remainder. I accumulate in `long`, then mod.

</details>

### S4. This passes once and the second call on the same map returns a huge count. Why?

<details>
<summary>Show Answer</summary>

**Answer:**

The map was a field and the second call continued from the first call's prefixes. Interview solutions should create the map inside the method, or clear it at the start. A shared map across requests is also a concurrency bug: `HashMap` can lose entries or loop under concurrent `merge`. One request, one local map.

**Corner cases:** A static `seen` used to "save allocations" in a service. Two threads count the wrong customers' numbers. Clearing without removing the seed `{0: 1}` makes the next call miss starts at index 0, or clearing and forgetting to re-seed does the same. I build a new map. The count variable must also be local. An `int` count overflows when n is 10^5 and every subarray matches. The usual constraint needs a `long` answer even if the template says `int`. I mention it.

</details>

**Senior recap:** query `prefix - K` before you insert, seed a zero prefix, use `long` prefixes, and keep the map local to the call.

Next folder: [Recursion](../04_Recursion_Backtracking/01_Recursion.md)
