# Sliding window

## Simple idea

A window is a contiguous stretch of the array or string. You slide it to the right. A fixed window has a constant length k. A variable window grows and shrinks until a condition is true. You almost never restart from scratch at each index.

## Picture

Fixed window, k = 3, sum of each window:

```text
[1  2  3] 4  5    sum 6
 1 [2  3  4] 5    sum 9   = previous - left + new right
 1  2 [3  4  5]   sum 12
```

Variable window, longest substring without a repeated character. `abcabcbb`:

```text
L and R both start at 0
R walks forward and counts characters inside the window
when a count becomes 2, move L forward until that count drops to 1
best length is 3 ("abc")
```

## Java example

```java
int maxSumFixed(int[] a, int k) {
    if (a.length < k || k <= 0) return 0;
    long window = 0;
    for (int i = 0; i < k; i++) window += a[i];
    long best = window;
    for (int r = k; r < a.length; r++) {
        window += a[r] - a[r - k];
        if (window > best) best = window;
    }
    return (int) best; // only if the problem promises the sum fits
}

int longestUnique(String s) {
    int[] count = new int[128]; // ASCII. Use a map for full Unicode.
    int best = 0;
    int left = 0;
    for (int right = 0; right < s.length(); right++) {
        char c = s.charAt(right);
        count[c]++;
        while (count[c] > 1) {
            count[s.charAt(left)]--;
            left++;
        }
        best = Math.max(best, right - left + 1);
    }
    return best;
}
```

Each index enters and leaves the window at most once, so both are O(n).

## When to use it

Use a fixed window for "every stretch of length k" (average, max sum, anagram counts of a pattern of length k). Use a variable window for "longest or shortest stretch that satisfies a rule" (at most K distinct, no repeats, sum at least S). If the stretch does not have to be contiguous, this is the wrong tool.

**Corner case:** k = 0, k bigger than n, and a null string. Shrinking with `if` instead of `while` leaves the window illegal when one step is not enough. For the sum "at least S" with positive numbers, you can shrink while the condition holds. If numbers can be negative, shrinking is no longer safe, because dropping a negative makes the sum smaller. Then this pattern does not apply.

## Practice

- Maximum sum of a window of size k — fixed window.
- Average of every window of size k — same, watch integer division.
- Longest substring without repeating characters — variable window and counts.
- Minimum window substring that covers all characters of a pattern — variable window, need-count of the pattern.
- Longest substring with at most K distinct characters — variable window and a distinct counter.
- Sliding window maximum — a deque, in [Queue and deque](../05_Linear_Structures/04_Queue_Deque.md).

## Interview Q&A — 6 to 8 years

### S1. Why is the variable window O(n) even though there is a while loop inside the for loop?

<details>
<summary>Show Answer</summary>

**Answer:**

`right` only moves forward. `left` only moves forward. Each character is added once and removed once. The inner `while` does not restart from zero. The total number of left moves across the whole run is at most n. That is amortized O(n), which is what I say in the interview. It is not O(n) per starting index.

**Corner cases:** A `while` that moves `left` backward, or a fresh scan from `left` on every `right`, brings back O(n^2). Using `if` instead of `while` when three copies of a character sit in the window leaves a count of 2 and the answer is too long. A window on UTF-16: `charAt` is a code unit, so an emoji can look like two "characters" and a repeat check on `char` is wrong. I mention `codePointAt` if the input is real text, and I use the simple `char` model if the problem says lowercase English.

</details>

### S2. Fixed window sum is wrong only when k is large. What failed?

<details>
<summary>Show Answer</summary>

**Answer:**

The running sum was an `int` and wrapped. Small k hides it. I keep the window in `long`. I also check `k > n` before I read `a[k - 1]`. Integer division for an average truncates. If the problem wants a double average, I divide only when I store the answer, not inside the running sum.

**Corner cases:** Negative numbers in a fixed window are fine. The "subtract the leaving element" formula does not need positives. k = 1. An empty array. A window that starts at the last index and the first loop `i < k` throws. Calling the method twice is safe if the array is not modified. This algorithm does not modify it. A caller who passes the same array to a sort afterward is a different bug.

</details>

### S3. When must you refuse the sliding-window shrink rule?

<details>
<summary>Show Answer</summary>

**Answer:**

When the array can contain negatives and the condition is a sum (at least S, or exactly a maximum sum of any length). Dropping an element does not move the sum in one direction, so the first time the condition fails is not a reason to stop growing, and shrinking may skip the best window. I use prefix sums, Kadane, or a different structure. The shrink rule is safe for positive numbers, for counts of characters, and for "at most K distinct."

**Corner cases:** Zeros. They do not break the positive-sum shrink, but they can make many windows share a sum, so "shortest" needs `<=` or `>=` written carefully. A problem that says "subarray" in some blogs and means "subsequence." I ask. Non-contiguous is not a window.

</details>

### S4. Minimum window that covers a pattern. What do you count, and what do you forget?

<details>
<summary>Show Answer</summary>

**Answer:**

I count what the pattern still needs, not just the window length. A `need` map stores how many of each required character are missing. I move `right` and decrease need. When missing hits zero, I move `left` to drop extra characters and I record the smallest window. Extra characters of a required letter are allowed. Characters not in the pattern can sit in the window but should be shrunk away when they are at `left`.

**Corner cases:** Pattern longer than the string: no window. Duplicate letters in the pattern (`AAB`): a window with one A is not done. I count frequencies, not a set. Case. Unicode, same as above. Updating the best window before it is valid. An answer that keeps the start index from a previous call because it was a field. I use locals. If several windows share the minimum length, I state which one I return (the first, usually).

</details>

**Senior recap:** left and right only move forward, sum in `long`, do not shrink a window of negative numbers, and pattern windows count frequencies.

Next folder: [Hashing](../03_Hashing/01_Hashing_Basics.md)
