# Advanced DP: matrix chain and partitions

## Simple idea

Some problems ask where to split a sequence. The split costs something now, and the two sides are the same kind of problem. You try every split point and remember the best cost for each stretch.

Matrix chain multiplication is the clean example. You are not multiplying yet. You are choosing the parenthesization with the cheapest number of scalar multiplies.

Palindrome partition is the same shape on a string: the cost of a stretch is "cut here, if the left piece is a palindrome."

## Picture

Matrix chain. One matrix is free (no multiply). A chain of many matrices tries a last multiply that joins a left chain and a right chain.

```text
matrices A B C with sizes
A is 2 x 3
B is 3 x 4
C is 4 x 2

(A B) C:  (2*3*4) + (2*4*2) = 24 + 16 = 40
A (B C):  (3*4*2) + (2*3*2) = 24 + 12 = 36

36 is better. The middle dimension is what you multiply by.
```

The table is over stretches. `dp[i][j]` is the best cost to multiply matrices i through j.

```text
length 1: cost 0 on the diagonal
length 2: only one place to split
length 3: try each split between the ends
dp[i][j] = min over k of
    dp[i][k] + dp[k+1][j] + rows[i] * cols[k] * cols[j]
```

You fill short stretches before long ones, because a long stretch reads shorter answers.

## Java example

```java
int matrixChain(int[] dim) {
    // dim[i] is rows of matrix i, dim[i + 1] is its columns. m matrices => dim length m + 1
    int m = dim.length - 1;
    int[][] dp = new int[m][m];
    for (int len = 2; len <= m; len++) {
        for (int i = 0; i + len - 1 < m; i++) {
            int j = i + len - 1;
            dp[i][j] = Integer.MAX_VALUE;
            for (int k = i; k < j; k++) {
                long cost = (long) dp[i][k] + dp[k + 1][j]
                        + (long) dim[i] * dim[k + 1] * dim[j + 1];
                if (cost < dp[i][j]) dp[i][j] = (int) cost;
            }
        }
    }
    return dp[0][m - 1];
}
```

Time is O(m^3). Memory is O(m^2). One matrix costs 0. Two matrices have one possible multiply.

Palindrome partition, at a high level: `cut[i]` is the minimum cuts for the prefix `s[0..i]`. For every end `j <= i`, if `s[j..i]` is a palindrome, `cut[i]` can be `cut[j - 1] + 1` (or 0 cuts if the whole prefix is a palindrome). You precompute which stretches are palindromes in O(n^2), then the cuts are another O(n^2). A naive "is palindrome" inside the loop makes it O(n^3), which is the version to mention if n is small.

## When to use it

Use a split DP when the answer for a range depends on the answers for smaller ranges inside it: matrix chain, burst balloons, optimal BST, palindrome cuts. Use a linear DP (knapsack, LCS) when the state is a prefix and one decision, not a range. If m is 500, m^3 is 1.25 * 10^8 operations. Say that number before you claim it is fine.

**Corner case:** the index of the dimension array is the place people go off by one. Matrix i uses `dim[i]` and `dim[i + 1]`. The multiply cost uses `dim[i] * dim[k + 1] * dim[j + 1]` with the variables in the example above. I trace two matrices and three matrices on paper before I trust the formula. `Integer.MAX_VALUE` plus a cost overflows. I use `long`, and I do not add to the sentinel. A single matrix returns 0. An empty dimension array is invalid. Palindrome checks must use inclusive ends, and a single character is a palindrome. Forgetting that forces a cut before every character.

## Practice

- Matrix chain multiplication — the table above. Trace `dim = {2, 3, 4, 2}` and expect 36.
- Palindrome partition II (minimum cuts) — prefix cuts plus a palindrome table.
- Burst balloons — range DP. The "last balloon to burst" in a range is the split. The cost uses the balloons just outside the range, which is the part that does not match matrix chain. Do this after matrix chain feels mechanical.
- Longest palindromic subsequence — LCS of the string with its reverse, or a range DP. It is a range, and it is not the same as palindrome cuts.

## Interview Q&A — 6 to 8 years

### S1. Why must you fill the matrix-chain table by increasing length?

<details>
<summary>Show Answer</summary>

**Answer:**

`dp[i][j]` reads `dp[i][k]` and `dp[k + 1][j]`, which are strictly shorter stretches. If I fill by row from the top, a cell may read a stretch I have not computed. Filling by length guarantees every shorter stretch is done. The diagonal is 0 because a single matrix needs no multiply. I do not start `len` at 1 and then treat it as a multiply.

**Corner cases:** A bottom-up loop that starts `j` at `i` includes the diagonal and then runs `k` from `i` to `j - 1`, which is an empty range. That is harmless if I skip `len < 2`. Starting `k` at `i + 1` and going through `j` drops a legal split or walks off the matrix. I check `k` from `i` inclusive to `j` exclusive so both sides are non-empty. An off-by-one in `dim[j + 1]` throws or reads the next matrix's column. The three-matrix hand trace catches it.

</details>

### S2. The cost is negative. What sentinel did you add to?

<details>
<summary>Show Answer</summary>

**Answer:**

I initialized the cell to `Integer.MAX_VALUE` and then added a positive multiply cost in `int`. The sum wrapped to a negative number, and the min comparison kept it. I compute the cost in `long`. I set the initial best to a `long` sentinel that I never add to, or I use the first split as the initial best and only then take mins. I store `long` if the true cost does not fit in an `int`.

**Corner cases:** A dimension of 0. The cost is 0 and the order does not matter. A negative dimension is bad input. I reject it rather than produce a negative cost that looks "better." Overflow in `rows * cols * cols` even when each factor fits in `int`. Three ints multiplied left to right overflow before the widening cast if I cast too late. I cast the first factor to `long`. The second call is safe if the table is local. A static table of the wrong size from a previous chain returns a cost for a different parenthesization.

</details>

### S3. Palindrome partition: why do people get O(n^3) without noticing?

<details>
<summary>Show Answer</summary>

**Answer:**

For every pair of ends they walk the substring to see if it is a palindrome. That inner walk is O(n), and the pairs are O(n^2), so the total is O(n^3). It is correct for small n. I precompute `isPal[j][i]` by length, each cell in O(1) from the inside, which is O(n^2), and then the cut loop is O(n^2). I say both bounds. I do not micro-optimize the cut loop while the palindrome test is still a full scan.

**Corner cases:** A single character is a palindrome and needs no cut. The whole string already a palindrome: answer 0. Empty string: 0. Cuts are the number of cuts, not the number of pieces. Pieces = cuts + 1. An off-by-one returns 1 for a palindrome. Centers for odd and even lengths if I expand around centers instead of a table. Both work. The table is easier to attach to the cut DP. Rebuilding one partition, not only the cut count, needs parent indexes. I mention that only if they ask for the pieces.

</details>

### S4. How do you recognize the next range-DP problem as the same shape?

<details>
<summary>Show Answer</summary>

**Answer:**

I look for "best way to fully resolve a contiguous stretch," where a choice picks a last operation or a split, and the two sides never interact except through that choice. Matrix chain's last multiply is the split. Burst balloons' last balloon in the range is the split. If the sides interact element by element, it is probably LCS-style or a different state. I write the state as `dp[left][right]` and the loop as increasing length before I invent a prefix state that cannot see the right boundary.

**Corner cases:** Burst balloons reads the value just outside the range. If I define the range as inclusive and I forget the sentinels at both ends of the array, the edge balloon multiplies by zero or throws. That problem is the follow-up, not the first implementation. A range DP on n = 10^3 is n^3 = 10^9, which is too slow. I say the limit. Memoized recursion on ranges has the same complexity and a stack of depth n, plus a cache of n^2. It is acceptable when I am sure about the recurrence and less sure about the loop bounds. I still watch the stack.

</details>

**Senior recap:** fill short ranges first, multiply dimensions in `long`, palindrome cuts need a palindrome table or they become O(n^3), and a range state is `dp[left][right]`.

Back to the map: [00_COURSE_MAP.md](../00_COURSE_MAP.md)
