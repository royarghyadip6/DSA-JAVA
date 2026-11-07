# Longest common subsequence

## Simple idea

A subsequence keeps characters in order and may skip some. A substring may not skip. The longest common subsequence (LCS) of two strings is the longest sequence that appears in both, in the same order, not necessarily as a block.

`abcde` and `ace` have LCS `ace`, length 3. The letters are not a contiguous block inside `abcde`.

## Picture

Rows are the first i characters of `s`. Columns are the first j characters of `t`.

```text
s = ace, t = abcde

      ""  a  b  c  d  e
  ""   0  0  0  0  0  0
  a    0  1  1  1  1  1
  c    0  1  1  2  2  2
  e    0  1  1  2  2  3

When the characters match, the cell is 1 + diagonal (the answer without those two characters).
When they do not, the cell is the better of "drop from s" (above) or "drop from t" (left).
```

The bottom-right cell is the length.

## Java example

```java
int lcsLength(String s, String t) {
    int n = s.length();
    int m = t.length();
    int[][] dp = new int[n + 1][m + 1];
    for (int i = 1; i <= n; i++) {
        for (int j = 1; j <= m; j++) {
            if (s.charAt(i - 1) == t.charAt(j - 1)) {
                dp[i][j] = dp[i - 1][j - 1] + 1;
            } else {
                dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
            }
        }
    }
    return dp[n][m];
}
```

Time and memory are O(n * m). If you only need the length, two rolling rows are enough: the current row and the previous row. You need the diagonal, so one row is trickier than knapsack. Two rows are the clean version.

To rebuild the string, start at the bottom-right and walk back. On a match, that character is in the answer and you step diagonally. On a mismatch, step toward the larger neighbor.

## When to use it

Use LCS when order matters and gaps are allowed: diff tools, "how similar are these sequences," edit distance's cousin. Do not use it for a contiguous block. That is the longest common substring, and the match cell does not add to a diagonal that skipped characters. The substring version resets to 0 on a mismatch instead of taking max(left, above), and it also adds 1 on a match from the diagonal.

If the strings are length 10^5, an O(n * m) table does not fit. Say so.

**Corner case:** empty string. The first row and column stay 0, and the loops that start at 1 handle it. Case and Unicode: `charAt` compares code units. `A` and `a` are different unless you normalize. A very long LCS length fits in `int` because it cannot exceed the shorter string. Rebuilding the string by appending on the way back produces the reverse. I reverse at the end, or I push to the front. Two equal strings: the LCS is the string itself. One string empty: length 0, not 1.

## Practice

- LCS length — the table above.
- Print one LCS — walk back from the corner.
- Longest common substring — contiguous. Reset on mismatch, track a global max.
- Delete operations for two strings — `n + m - 2 * lcs`. Each character not in the LCS must be deleted from one side.
- Shortest common supersequence — `n + m - lcs`. You need the merge, not only the length, if the problem asks for the string.
- Edit distance — a nearby table. Match copies the diagonal. A mismatch is 1 + the best of replace (diagonal), delete (above), or insert (left).

## Interview Q&A — 6 to 8 years

### S1. Subsequence versus substring. What changes in the cell?

<details>
<summary>Show Answer</summary>

**Answer:**

A subsequence may skip, so a mismatch still keeps the best result from the top or the left. A substring may not skip, so a mismatch breaks the run and the cell is 0. A match in both versions looks at the diagonal. In the substring version that diagonal means "this pair extends a contiguous run." I also keep a separate `best`, because the answer might sit in the middle of the table, not in the corner. LCS length always ends at the corner. Substring length does not.

**Corner cases:** Using the substring reset on an LCS problem returns 1 for `abc` and `cba`, whose LCS is 1 anyway, and returns 1 for `ace` and `abcde`, whose LCS is 3. The sample with a contiguous answer hides the bug. The opposite bug, using max(left, above) for a substring, glues two separate runs together. I state the word the interviewer used before I fill the grid.

</details>

### S2. You need the string, not the length. Why is the result backwards?

<details>
<summary>Show Answer</summary>

**Answer:**

I start at `(n, m)` and I emit a character when I move to the diagonal. That walks the string from the end. I append to a `StringBuilder` and `reverse()` once. Walking forward from `(0, 0)` also works if I always know which way the table built the answer, but the backward walk is the one that matches the recurrence I wrote. I copy the characters, not indexes into a changing buffer.

**Corner cases:** Several LCS strings of the same length (`ab`, `ba` wait, those differ). `abc` and `acb` can yield `ab` or `ac` depending on whether I prefer the top or the left on a tie. I pick one rule and I stick to it. An empty LCS: I return an empty string, not null. Building with `String + char` inside the walk is O(n^2) copies. `StringBuilder` is the O(length) finish. If I only kept two rolling rows, I cannot walk back. I need the full table, or I accept that I only have the length.

</details>

### S3. Edit distance is the same grid with a different bill. What do you add?

<details>
<summary>Show Answer</summary>

**Answer:**

The cell is the minimum edits to turn the first i characters into the first j. If the characters match, the cell is the diagonal, with no added edit. If they do not, the cell is 1 plus the minimum of three neighbors: diagonal is a replace, up is a delete from `s`, left is an insert into `s`. The first row is `0, 1, 2, ...` because turning an empty string into j characters takes j inserts. The first column is the deletes. LCS starts those borders at 0. Copying the LCS borders into edit distance returns a length, not an edit count.

**Corner cases:** Both strings empty: 0 edits. One empty: the length of the other. A match must not cost 1. Adding 1 on the diagonal for a match overcounts. Unicode and surrogate pairs: `charAt` can split an emoji and count two edits for one character. I mention code points if the product is real text. Memory is the same O(n * m). Two rows work for the distance. They do not reconstruct the script of edits unless I store parent pointers.

</details>

### S4. n and m are both 5,000. What do you say about time and memory before you allocate?

<details>
<summary>Show Answer</summary>

**Answer:**

25 million cells. An `int` table is about 100 MB, plus overhead. That may pass a judge and hurt a small service. Time is about 25 million operations, which is acceptable in a single request and rude on a hot path. I can roll to two rows if I only need the length, cutting memory to O(min(n, m)). I put the longer string on the rolled side only after I get the indexes right. If this runs per user keystroke, I do not recompute the full table from scratch. I say that constraint. I do not pretend there is an O(n + m) LCS for general strings.

**Corner cases:** A recursive memo with the same state count plus a deep stack. Two strings of length 10^5 cannot use this table. I ask whether a contiguous match or a fixed alphabet approximation is the real need. `short` cells if the length fits in a short, which it does when both strings are under 32,767. That halves memory. I only do it if the max length cannot exceed `Short.MAX_VALUE`. The second call allocates a second table if I am not careful in a service. I keep the method pure and let the caller decide reuse.

</details>

**Senior recap:** mismatch keeps the best skip for a subsequence and resets for a substring, reverse the rebuilt string, edit distance bills 1 for insert, delete, or replace, and 5,000 by 5,000 is a real allocation.

Next: [04_DP_Advanced.md](04_DP_Advanced.md)
