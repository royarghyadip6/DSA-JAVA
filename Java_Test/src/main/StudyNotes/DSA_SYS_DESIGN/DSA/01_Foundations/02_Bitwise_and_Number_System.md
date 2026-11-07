# Bitwise operations and the number system

## Simple idea

Every `int` is 32 bits. AND, OR, and XOR work on those bits. XOR is the interview favorite: a bit that appears twice cancels out, so the number that appears once remains.

## Picture

```text
decimal 5 = 0101
decimal 3 = 0011

AND  0001 = 1     both bits must be 1
OR   0111 = 7     either bit is 1
XOR  0110 = 6     bits that differ

5 XOR 5 = 0000    a number XOR itself is 0
5 XOR 0 = 0101    a number XOR 0 stays itself
```

`n << 1` moves bits left and doubles n, until the sign bit overflows. `n >> 1` moves bits right and halves a non-negative n.

## Java example

```java
int singleNumber(int[] a) {
    int x = 0;
    for (int v : a) {
        x ^= v;          // pairs cancel: 4 ^ 1 ^ 4 = 1
    }
    return x;
}
```

For input `{4, 1, 2, 1, 2}` the result is `4`. This is O(n) time and O(1) extra memory.

Useful checks:

- `n & 1` is 0 when n is even.
- `n & (n - 1)` clears the lowest set bit. If the result is 0, n is a power of two (and n > 0).
- `Integer.bitCount(n)` counts the 1 bits.

## When to use it

Use XOR when the problem says "every element appears twice except one." Use bits when you need a set of flags in one word, or a power-of-two test. Do not use bits to look clever when a map is clearer and n is small.

**Corner case:** Java `>>` keeps the sign (arithmetic shift). `>>>` fills with zeros. `-1 >> 1` stays `-1`. Also `1 << 31` is `Integer.MIN_VALUE`, not a positive power you can print as 2^31 in an `int`.

## Practice

- Single number in an array of pairs — XOR fold.
- Count 1 bits — loop `n &= n - 1` until n is 0.
- Is power of two — `n > 0 && (n & (n - 1)) == 0`.
- Swap two ints with XOR — only if they are different variables. `a ^= a` zeroes a variable.

## Interview Q&A — 6 to 8 years

### S1. Why does XOR find the single number, and when does it fail?

<details>
<summary>Show Answer</summary>

**Answer:**

XOR is associative and commutative, and `x ^ x` is 0, and `x ^ 0` is x. Folding the array cancels every pair. It fails when two values appear once, when a value appears three times, or when the "pair" rule is not true. Three of the same value leaves that value, not zero.

**Corner cases:** The single value can be 0. Starting the fold at 0 is correct, because the real 0 still cancels with nothing extra only if it is the single one: `0 ^ 0 ^ 5 = 5` if 0 is paired, and `4 ^ 4 ^ 0 = 0` if 0 is the single. An empty array returns 0, which is ambiguous. I check the length first if 0 is a legal answer.

</details>

### S2. A candidate writes `1 << n` to compute a size. What do you flag?

<details>
<summary>Show Answer</summary>

**Answer:**

Shift count is masked. For an `int`, Java uses only the low 5 bits of the shift, so `1 << 32` is `1 << 0`, which is 1, not 0 and not 2^32. `1 << 31` is negative. Sizes and masks that must exceed 31 bits belong in a `long`, and the literal must be `1L << k`. I also reject a shift that builds a collection capacity without checking k.

**Corner cases:** `n << 1` as a multiply overflows silently into the sign bit. `-5 << 1` is defined but surprising. Right-shifting a negative hash to get a bucket index needs `>>>` or `Math.floorMod`, or the index becomes negative and `array[index]` throws.

</details>

### S3. How do you store a small permission set in one integer, and what breaks?

<details>
<summary>Show Answer</summary>

**Answer:**

Each permission is one bit: `READ = 1 << 0`, `WRITE = 1 << 1`. Grant with `|`, test with `&`, revoke with `& ~flag`. This is O(1) and compact. It breaks past 32 flags in an `int`, and it is hard to log. A `EnumSet` is clearer when the set is not on a hot path.

**Corner cases:** `~flag` flips all 32 bits, so `& ~WRITE` is right only if the other bits are real flags, not garbage. Combining flags from two sources with `+` instead of `|` corrupts the set when bits overlap (`1 + 1 = 2`). Serialization of the raw int is stable only if nobody reorders the bit positions.

</details>

### S4. How is this different from the math you do on the decimal value?

<details>
<summary>Show Answer</summary>

**Answer:**

`n / 2` and `n >> 1` match for non-negative ints. They do not match for negatives, because division truncates toward zero and arithmetic shift rounds down. `(-3) / 2` is `-1`. `(-3) >> 1` is `-2`. I use `/` when I mean arithmetic, and shifts when I mean bits.

**Corner cases:** `Integer.MIN_VALUE / -1` overflows. `Math.abs(Integer.MIN_VALUE)` is still negative, because the positive value does not fit in an `int`. Any trick that does `Math.abs(a - b)` must use a `long`, or the compare is wrong for the full `int` range.

</details>

**Senior recap:** XOR cancels pairs, shifts are not safe sizes, flag sets use `|` and `&`, and negative numbers do not follow the positive-bit story.

Next: [03_Math_Basics.md](03_Math_Basics.md)
