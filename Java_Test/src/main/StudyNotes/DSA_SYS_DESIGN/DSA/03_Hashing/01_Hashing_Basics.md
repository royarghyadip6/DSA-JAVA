# Hashing basics

## Simple idea

A `HashMap` stores a value under a key. A `HashSet` stores keys only. On average, add and lookup are O(1). You trade memory for a second pass. Use them when the question is "have I seen this before?"

## Picture

```text
keys in, hash picks a bucket

  "b" --+
        v
bucket 0:  ("b", 1)
bucket 1:  ("a", 2) -> ("c", 3)   a chain, still correct, just slower
bucket 2:  empty

set: { "a", "b", "c" }   same idea, no value beside the key
```

In Java, a long chain of comparable keys can become a tree. You still write the interview answer as "O(1) average, O(n) worst case."

## Java example

```java
boolean hasDuplicate(int[] a) {
    java.util.Set<Integer> seen = new java.util.HashSet<>();
    for (int x : a) {
        if (!seen.add(x)) return true; // add returns false if x was already there
    }
    return false;
}

int firstUniqueChar(String s) {
    int[] count = new int[128];
    for (int i = 0; i < s.length(); i++) count[s.charAt(i)]++;
    for (int i = 0; i < s.length(); i++) {
        if (count[s.charAt(i)] == 1) return i;
    }
    return -1;
}
```

A small fixed alphabet (lowercase letters) is often faster as an `int[26]` than a `HashMap<Character, Integer>`. Both are hashing ideas. The array is a perfect hash you wrote by hand.

## When to use it

Use a set for membership and duplicates. Use a map for counts, for "index of the last time I saw this," and for two-sum. Use `LinkedHashMap` when order of insertion matters (it also supports an LRU). Use `TreeMap` when you need sorted keys, at O(log n) per call.

**Corner case:** a custom key class must implement `equals` and `hashCode` together. If they disagree, the map loses the entry. `Integer` and `String` already do this. A mutable key that changes after `put` is lost in the wrong bucket. Do not use an array as a key. Arrays use identity equality. `Map.get` returns null both for "missing" and for "stored null." `containsKey` tells them apart.

## Practice

- Contains duplicate — one set.
- Two sum, return indexes — one map from value to index.
- Group anagrams — key is the sorted characters, or a count signature.
- First unique character — counts, then a second pass.
- Longest consecutive sequence — set of the numbers, start a run only when `n - 1` is absent.

## Interview Q&A — 6 to 8 years

### S1. What is the real cost of `HashMap` in Java, beyond "O(1)"?

<details>
<summary>Show Answer</summary>

**Answer:**

Average O(1) if the hash spreads keys. Each entry is an object, so a map of a million ints uses far more memory than an `int[]`. A bad `hashCode` (always 0) makes one bucket. Java tree-ifies bins of comparable keys after a threshold, so the worst case becomes O(log n) for those keys, not a license to ignore hash quality. `String` hash is cached after the first use. A fresh `String` of length m costs O(m) to hash once.

**Corner cases:** The default load factor is 0.75. When size passes capacity times load factor, the map rehashes. One `put` is then O(n) for that call, and the average across a build is still O(1) per put. I size the map with `new HashMap<>(expected * 4 / 3)` when I know the size, so the request does not pause on a resize. `HashMap` is not thread-safe. Two puts can loop or lose entries. I use `ConcurrentHashMap` or I confine the map to one thread. `null` keys: one null key is allowed in `HashMap`, and none in `ConcurrentHashMap`.

</details>

### S2. `equals` and `hashCode` drifted. How does the bug show up?

<details>
<summary>Show Answer</summary>

**Answer:**

Two keys that are equal under `equals` must return the same `hashCode`. If they do not, `get` looks in the wrong bucket and returns null. If `hashCode` is equal for everything, the map works and becomes a slow list. If I change a field that participates in `hashCode` after `put`, the entry stays in the old bucket and `get` cannot find it. The second call, after a mutation, looks like the data vanished.

**Corner cases:** An IDE-generated pair that forgets a field used in `equals`. A `double` key: `NaN` and `-0.0` have equality surprises. I avoid floating keys. Using a mutable `ArrayList` as a key. `record` types in modern Java generate consistent `equals` and `hashCode`, which is the safe default for a small immutable key. Comparing with `==` on boxed `Integer` above 127 fails because those are not cached. Maps use `equals`, so they are fine. A hand-rolled check with `==` is not.

</details>

### S3. When do you refuse a hash map in an interview?

<details>
<summary>Show Answer</summary>

**Answer:**

When the interviewer asks for O(1) extra memory and the array is sorted: two pointers. When the alphabet is tiny: an array of counts. When I need the keys in order: `TreeMap`, or sort a list of keys after. When the "seen" test is "prefix of a string" across many strings: a trie, not a map of every prefix, unless n is small. I say the memory. O(n) extra is a real cost on a large heap-sensitive service.

**Corner cases:** A `HashSet` of boxed ints for "contains duplicate" on a huge array can allocate more than the algorithm's time saves, if the array is almost unique. For a one-shot script it is still the right answer. For a hot path I mention primitive maps (fastutil, agrona) only if this codebase already has them. I do not invent a dependency in the interview. `IdentityHashMap` compares with `==`. Using it by accident breaks `String` keys that are equal and not the same object.

</details>

### S4. How do you count with a map without an NPE or a wrong zero?

<details>
<summary>Show Answer</summary>

**Answer:**

`map.merge(key, 1, Integer::sum)` or `map.compute(key, (k, v) -> v == null ? 1 : v + 1)`. Both treat a missing key as one new count. `map.get(key) + 1` throws if the key is absent, because `get` returns null and the unboxing NPE is the bug. `getOrDefault(key, 0) + 1` is clear. I put the result back.

**Corner cases:** A count that overflows `int`. Rare in puzzles, real for telemetry. I use `Long`. Removing a key when the count hits zero, so "present with count 0" does not look like "seen." Iteration order of `HashMap` is not insertion order and is not a stable tie-break. If the problem says "the first one," I keep an index, or I use `LinkedHashMap`, and I do not rely on `keySet` order. A second pass over the map does not replay the original positions unless I stored them.

</details>

**Senior recap:** O(1) average with object overhead and a resize spike, `equals` must match `hashCode`, skip the map when an array or two pointers fit, and never unbox a missing `get`.

Next: [02_Subarray_Sum_Equals_K.md](02_Subarray_Sum_Equals_K.md)
