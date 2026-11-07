# 0/1 knapsack

## Simple idea

You have items. Each item has a weight and a value. You have a bag that holds at most `capacity` weight. Each item may be taken once or not at all. You want the largest total value that fits.

Dynamic programming means you remember the answer to a smaller question. The smaller question is: using only the first `i` items, with room `w` left, what value can you reach?

## Picture

Items: (weight 2, value 3) and (weight 3, value 4). Capacity 5.

```text
rows = items already considered
cols = capacity 0..5

          cap 0  1  2  3  4  5
no items      0  0  0  0  0  0
item A (2,3)  0  0  3  3  3  3
item B (3,4)  0  0  3  4  4  7

At row B, cap 5: take B and the best of cap 2 after A, which is 4+3 = 7.
Skip B: the cell above is 3. Best is 7.
```

The cell is `max(skip, take)`. Skip is the cell above. Take is value plus the cell above, `w - weight` columns to the left. Take is allowed only when the weight fits.

## Java example

```java
int knapsack(int[] weight, int[] value, int capacity) {
    int n = weight.length;
    int[][] dp = new int[n + 1][capacity + 1];
    for (int i = 1; i <= n; i++) {
        for (int w = 0; w <= capacity; w++) {
            dp[i][w] = dp[i - 1][w]; // skip item i - 1
            if (weight[i - 1] <= w) {
                int take = value[i - 1] + dp[i - 1][w - weight[i - 1]];
                if (take > dp[i][w]) dp[i][w] = take;
            }
        }
    }
    return dp[n][capacity];
}
```

Time is O(n * capacity). Memory is O(n * capacity). A one-row version walks the capacity from right to left so you do not use the same item twice in one row.

This is 0/1: one decision per item. Unbounded knapsack (you may reuse an item) walks the row from left to right, so the same row can feed itself.

## When to use it

Use it when each item is take-or-skip and the capacity is not huge (a few thousand, sometimes 10^5 with the one-row form). If capacity is 10^9, this table does not fit. Then you need a different constraint (small values, or a greedy proof). If the items may be cut into fractions, greedy by value per weight solves the fractional knapsack, and this table is the wrong tool.

**Corner case:** a weight of 0. In the 0/1 form, taking it adds the value once, from the previous row. In the unbounded left-to-right form, a weight of 0 and a positive value can loop forever in a recursive solution, and a table cell can keep adding. I reject a non-positive weight unless the problem defines it. Values can be zero. A negative value should never be taken in this formula if we maximize, but a negative weight is nonsense for "fits in the bag." `take` computed in `int` can overflow if values are large. I use `long` when the statement allows big values. The answer for capacity 0 is 0. n = 0 is 0.

## Practice

- 0/1 knapsack — the table above.
- Partition equal subset sum — this is a knapsack: can you reach `total / 2` using each number once? If the total is odd, no.
- Target sum — split into a positive group and a negative group. It reduces to a subset sum.
- Unbounded knapsack / coin change (minimum coins) — reuse allowed. Coin change is a min count, so the cell starts as "impossible," not 0.
- Ones and zeroes (strings of 0 and 1) — two capacities. Same skip-or-take idea.

## Interview Q&A — 6 to 8 years

### S1. Why does the one-row version have to walk capacity from the right?

<details>
<summary>Show Answer</summary>

**Answer:**

The row stores the previous item's answers and then overwrites them with the current item. If I walk left to right, the left cell already includes the current item. Using it for `w - weight` takes the item twice. Walking from the right, the cells I still need (`w - weight`, which is to the left) have not been overwritten yet, so they still mean "previous item." Unbounded knapsack wants the opposite: reuse is allowed, so left to right is correct.

**Corner cases:** I allocate `capacity + 1`. Index `capacity` is the full bag. Off-by-one drops the last unit. A weight larger than capacity is only a skip. I must not compute `w - weight` when it is negative. The rolling row must be initialized to 0. A leftover row from a previous call, stored in a field, poisons the second call. I allocate inside the method. `Arrays.fill` is required if the array is reused.

</details>

### S2. Subset sum returns true for an odd total. What did you forget?

<details>
<summary>Show Answer</summary>

**Answer:**

If the total is odd, no two equal subsets exist. I return false before I build a table. If I skip that check, a bug in the table might still return false, so the test does not save me, or a bug might return true. I also watch the target `total / 2` computed in `int`. The sum itself may overflow `int` before the parity check. I sum in `long`.

**Corner cases:** Zeros in the array. They do not change the sum and they are valid items. An empty subset sums to 0, so target 0 is true. I usually set `dp[0] = true` and I do not treat that as two non-empty subsets unless the problem requires non-empty. The 0/1 direction still matters: a number used twice can fake a partition. Coins-style unbounded DP is the wrong variant here.

</details>

### S3. Coin change asks for the fewest coins, and your knapsack table returns 0. Why?

<details>
<summary>Show Answer</summary>

**Answer:**

Knapsack maximizes value and starts at 0, so "do nothing" looks like a real answer. Coin change minimizes a count. The impossible cells must start as a sentinel (amount + 1, or -1), and the zero amount is 0 coins. A coin may be reused, so this is unbounded: one row, left to right, `dp[x] = min(dp[x], dp[x - coin] + 1)` when `dp[x - coin]` is possible. If the sentinel remains at the end, I return -1.

**Corner cases:** A coin of 0. Left-to-right reuse never moves the amount and can spin. I skip non-positive coins. A coin larger than the amount. Order of coins does not matter for this DP. Greedy largest-coin fails on the classic 1, 3, 4 and amount 6. I mention that so I do not "optimize" this into greedy. `int` sentinel `amount + 1` is safe if amount is not `Integer.MAX_VALUE`. The count cannot exceed amount if the smallest coin is at least 1.

</details>

### S4. The table is O(n * capacity) and the service cannot allocate it. What do you change?

<details>
<summary>Show Answer</summary>

**Answer:**

I say the memory out loud. `int[n + 1][capacity + 1]` is about `4 * n * capacity` bytes, plus the real array overhead. The rolling row drops the n factor. If capacity is 10^9, no row fits. I then need a constraint I did not use: small total value (meet a value with minimum weight), or n <= 40 and meet-in-the-middle, or a greedy proof. I do not allocate the table and hope. In an interview I ask for the constraints before I commit to the state.

**Corner cases:** A `HashMap` of only the reachable weights saves memory when few sums are possible, and it is slower and easier to use twice by accident. Recursion with memo on `(index, remaining)` has the same state count and a worse constant, plus stack depth n. It still dies when the state count is huge. `int` value sums overflow and a max comparison prefers the wrapped negative. I use `long` for values when the statement is large. The weight check stays exact.

</details>

**Senior recap:** skip or take from the previous row, roll the row from the right for 0/1, do not use a maximize-from-zero table for a minimum coin count, and ask the constraints before you allocate n times capacity.

Next: [03_LCS.md](03_LCS.md)
