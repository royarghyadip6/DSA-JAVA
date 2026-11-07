# Two pointers

## Simple idea

Two indexes move across the data, usually toward each other or in the same direction. On a sorted array, that finds a pair with a given sum in O(n) after the sort. You do not need a nested loop.

## Picture

```text
sorted:  1   2   4   6   8   11     target 10
         L                   R
         1 + 11 = 12 > 10, move R left
         L               R
         1 + 8 = 9 < 10, move L right
             L           R
         2 + 8 = 10, found
```

If the sum is too big, the right value is too big. If the sum is too small, the left value is too small. That is only true when the array is sorted.

## Java example

```java
boolean hasPairSum(int[] a, int target) {
    int[] b = java.util.Arrays.copyOf(a, a.length); // keep the caller's order
    java.util.Arrays.sort(b);
    int l = 0;
    int r = b.length - 1;
    while (l < r) {
        long sum = (long) b[l] + b[r];
        if (sum == target) return true;
        if (sum < target) l++;
        else r--;
    }
    return false;
}
```

Same direction (slow and fast) is the other shape: remove duplicates in a sorted array, or detect a cycle later in a linked list.

## When to use it

Use opposite pointers on a sorted array for pair sum, container with most water, or a triplet (fix one number, then two pointers on the rest). Use a map instead when the array must stay unsorted and you need the original indexes. Use a sliding window when the condition is about a contiguous stretch, not a pair.

**Corner case:** sorting the caller's array changes it for the next call. I copy if the contract is "do not modify." Duplicate values: `l < r` stops you from using one element twice. `int` sum overflows, so I add in `long`. An unsorted array plus this move rule is simply wrong.

## Practice

- Two sum, return any pair — sorted pointers, or a map if you need original indexes.
- Two sum II on an array that is already sorted — pointers, O(1) extra memory.
- 3Sum — sort, fix i, two pointers, skip duplicates.
- Container with most water — pointers at both ends, move the shorter side.
- Remove duplicates from a sorted array — slow write pointer.

## Interview Q&A — 6 to 8 years

### S1. Two sum: map or two pointers? How do you choose out loud?

<details>
<summary>Show Answer</summary>

**Answer:**

If I need the original indexes and the array is not sorted, I use one pass with a `HashMap` from value to index. That is O(n) time and O(n) memory, and it does not reorder the array. If the array is already sorted and any pair is enough, two pointers are O(n) time and O(1) extra memory. If I may sort a copy, pointers cost O(n log n) because of the sort.

**Corner cases:** Two identical values that sum to the target (`3, 3` and target 6). The map must not reuse the same index. I insert the value after the lookup, or I check `index != i`. A pair that overflows `int` (`Integer.MAX_VALUE` and 1). I compare with `long`. A map key of `Integer` is fine. A hand-rolled long key is also fine. Returning indexes from the sorted copy without remembering the original positions is the bug I mention before they ask.

</details>

### S2. 3Sum returns the same triplet many times. What did you skip?

<details>
<summary>Show Answer</summary>

**Answer:**

After sorting, equal values sit together. When I move a pointer, I skip while the next value equals the one I just used. I do the same for the fixed index i. I still keep one copy of a real triplet such as `0, 0, 0` by allowing the pointers to land on different indexes before I skip onward.

**Corner cases:** I skip before I ever record, and I lose the only valid triplet. The skip belongs after a hit, and at the start of i only when `i > 0` and `a[i] == a[i - 1]`. Not copying the array means the caller's order is gone and a later call sees sorted data, so "the second call is mysteriously faster" and also wrong if order mattered. Target near the overflow line: `(long) a[i] + a[l] + a[r]`.

</details>

### S3. Container with most water: why do you move the shorter line, and when is that unsafe?

<details>
<summary>Show Answer</summary>

**Answer:**

Width only shrinks as the pointers move in. The area is width times the shorter height. If I move the taller side, the width shrinks and the height is still limited by the shorter side, so the area cannot improve. Moving the shorter side is the only move that might raise the limit. This proof needs a contiguous pair of boundaries on a single array of heights. It is not a license to move pointers on unsorted pair-sum data.

**Corner cases:** Equal heights: either side can move. I move one, or both, and I do not infinite-loop. Zero height. One or zero bars: no container. `int` area overflow: height and width both large. I compute area in `long`. The greedy move finds the max area. It does not list every pair. If the problem asks for the pair of indexes, I store them when I update the best area.

</details>

### S4. The slow pointer and the fast pointer are not at two ends. What problem is that?

<details>
<summary>Show Answer</summary>

**Answer:**

Same-direction pointers. The slow one writes the next kept element, the fast one scans. That removes duplicates from a sorted array in place, or partitions. On a linked list, fast moves two steps and slow moves one. If they meet, there is a cycle. Those are still "two pointers," and the sorted-array sum rule does not apply.

**Corner cases:** Fast running off the end of a list: I check `fast != null && fast.next != null` before `fast.next.next`. A slow write that starts at 0 and copies `a[0]` onto itself is fine. Starting the write at the wrong index drops the first unique value. Partition pointers that cross and swap again undo the partition. I stop when they meet.

</details>

**Senior recap:** sort plus opposite pointers when you may reorder, a map when indexes matter, skip duplicates only after you record a hit, and add pair sums in `long`.

Next: [05_Sliding_Window.md](05_Sliding_Window.md)
