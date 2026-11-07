# Math basics: GCD, primes, sieve

## Simple idea

Many interview problems shrink to "divide out the common factor" or "is this number prime?" GCD uses Euclid's algorithm. A prime has no divisor other than 1 and itself. A sieve marks multiples so you can list every prime up to n in about O(n log log n).

## Picture

```text
gcd(48, 18)
48 = 2*18 + 12
18 = 1*12 + 6
12 = 2*6  + 0
gcd = 6

sieve up to 10, cross out multiples:
2  3  4  5  6  7  8  9  10
2  3  x  5  x  7  x  x   x
primes: 2, 3, 5, 7
```

You start crossing from `i * i`, because smaller multiples were already crossed by smaller primes.

## Java example

```java
int gcd(int a, int b) {
    a = Math.abs(a);
    b = Math.abs(b);
    while (b != 0) {
        int r = a % b;
        a = b;
        b = r;
    }
    return a;
}

boolean isPrime(int n) {
    if (n <= 1) return false;
    if (n <= 3) return true;
    if (n % 2 == 0 || n % 3 == 0) return false;
    for (int i = 5; (long) i * i <= n; i += 6) {
        if (n % i == 0 || n % (i + 2) == 0) return false;
    }
    return true;
}
```

`gcd * lcm` equals the product only when you do the multiply in a wide type: `lcm = (long) a / gcd * b`.

## When to use it

Use GCD for "simplify a fraction" and "greatest shared step." Use a single prime test when you have one number. Use a sieve when you need every prime up to n, or many checks against the same limit.

**Corner case:** `Math.abs(Integer.MIN_VALUE)` is still negative. GCD of that value needs a `long`. The prime loop must use `(long) i * i <= n`. If you write `i * i <= n` in `int`, `i` past 46,340 overflows and the loop never ends, or it ends too soon.

## Practice

- GCD and LCM of two numbers — Euclid, then product over GCD in a `long`.
- Count primes less than n — sieve.
- Smallest prime factor of n — sieve that stores the factor, then factor in O(log n).
- Power of x^n — fast exponentiation. For `int` results, multiply in `long` and watch the modulus.

## Interview Q&A — 6 to 8 years

### S1. Why is Euclid fast, and what is the Java bug in a naive version?

<details>
<summary>Show Answer</summary>

**Answer:**

Each step replaces the pair with a strictly smaller remainder, so it finishes in a handful of steps even for large ints. The bug is overflow and sign. `%` in Java keeps the sign of the dividend, so a negative remainder must be normalized, or I take absolute values up front. I also cannot `Math.abs` the minimum int.

**Corner cases:** `gcd(0, 0)` is often defined as 0. Callers that then divide by the GCD will crash. I document that. `gcd(a, 0)` is `|a|`. A recursive version is fine for ints (the depth is tiny) and still ugly if someone passes longs near 2^63 without a checked loop.

</details>

### S2. A prime check passed tests and hung in production. What line was it?

<details>
<summary>Show Answer</summary>

**Answer:**

Almost always `for (int i = 3; i * i <= n; i += 2)` when n is near `Integer.MAX_VALUE`. `i * i` overflows to a negative int, so `i * i <= n` stays true. The fix is `(long) i * i <= n`, or a loop bound of `Math.sqrt` computed once in a `long` with care for rounding. I also return false for n <= 1 and treat 2 as prime before the odd loop.

**Corner cases:** `n = Integer.MAX_VALUE` is prime. A test suite of small n never hits the overflow. Using `Math.sqrt(n)` as a `double` and comparing `i <= sqrt` can skip or add a factor because of rounding. I prefer the `long` multiply. Trial division is O(sqrt n). At n = 2^31 that is about 46,000 steps, which is fine once. It is not fine inside a hot request for every id.

</details>

### S3. When do you build a sieve instead of calling isPrime in a loop?

<details>
<summary>Show Answer</summary>

**Answer:**

When the limit is known and I will ask many times. One sieve of size n is about O(n log log n) and then each check is O(1). A loop of prime tests is O(q * sqrt n). For n around 10^6 or 10^7 a boolean array is normal. I do not allocate a boolean per number up to 10^12. Then I go back to a single test, or a segmented sieve.

**Corner cases:** `new boolean[n]` is about n bytes of real use after the JVM packs them, but the array header and the heap still matter. Default `false` means "not composite yet" only if I remember that 0 and 1 are not prime. A sieve that starts marking at `2 * i` instead of `i * i` is correct and slower. `i * i` in the sieve also overflows if i is an `int` and n is large. Use `long`.

</details>

### S4. How do you compute (a^b) mod m without blowing the stack or the int?

<details>
<summary>Show Answer</summary>

**Answer:**

Fast exponentiation: square the base, halve the exponent, and multiply into the answer when the exponent is odd. Each multiply is `(x * y) % m` done in a type wide enough that `x * y` does not overflow, or with a careful modular multiply if m itself is near 2^63. Time is O(log b). I use a loop, not recursion, so the stack stays flat.

**Corner cases:** A negative base. I normalize `a %= m` into the range 0..m-1 before multiplying. `m = 1` makes every result 0. `b = 0` returns 1, including `0^0` if the problem defines it that way. I say the definition. Java's `BigInteger.modPow` is the production tool when the numbers do not fit in a long. In an interview I still show the loop so the complexity is visible.

</details>

**Senior recap:** Euclid for GCD, a `long` multiply in the prime loop, a sieve only when the limit fits in memory, and modular power in a loop.

Next folder: [Arrays](../02_Arrays/01_Arrays_Basics.md)
