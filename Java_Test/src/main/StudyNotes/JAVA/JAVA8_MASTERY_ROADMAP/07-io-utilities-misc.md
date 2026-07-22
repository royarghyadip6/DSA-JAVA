# Phase 7 — I/O, Utilities & Misc APIs

**Duration:** 1 week | **Goal:** Files.lines, Base64, StringJoiner, Arrays enhancements, wrapper utilities

[← Phase 6](06-reflection-annotations.md) | [Index](README.md) | **Next:** [Phase 8 — Nashorn →](08-nashorn-scripting.md)

---

## Theory (Easy to Remember)

### 1. Files.lines() — Stream File Reading

- Returns **`Stream<String>`** — one line per element.
- **Must close stream** — use try-with-resources.
- Default charset: **UTF-8** (overload with `Charset` available).
- Lazy — reads line by line, good for large files.

```java
try (Stream<String> lines = Files.lines(path)) {
    lines.filter(l -> !l.isEmpty()).forEach(System.out::println);
}
```

---

### 2. Base64

- **`java.util.Base64`** — standard RFC 4648 encoding (replaces sun.misc internal APIs).
- **`getEncoder()`** / **`getDecoder()`** — basic
- **`getUrlEncoder()`** — URL-safe (no `+/`)
- **`getMimeEncoder()`** — MIME line breaks

---

### 3. StringJoiner & String.join

- **`StringJoiner`** — build delimited string with optional prefix/suffix.
- **`String.join(delimiter, iterable)`** — static convenience.
- Used internally by `Collectors.joining()`.

---

### 4. Arrays.parallelSort & parallelPrefix

- **`parallelSort`** — multi-threaded sort for large arrays (primitive + object).
- **`parallelPrefix`** — parallel cumulative operation (scan).
- Stable sort for object arrays.

---

### 5. Collection Default Methods (Java 8)

| Method | Interface | Purpose |
|--------|-----------|---------|
| `forEach(Consumer)` | `Iterable` | Iterate with lambda |
| `removeIf(Predicate)` | `Collection` | Conditional remove |
| `replaceAll(UnaryOperator)` | `List` | Transform all elements |
| `sort(Comparator)` | `List` | In-place sort |

---

### 6. Map Enhancements

| Method | Purpose |
|--------|---------|
| `getOrDefault` | Value or default if missing |
| `putIfAbsent` | Atomic-ish put only if absent |
| `computeIfAbsent` | Lazy compute value |
| `computeIfPresent` | Update if exists |
| `merge` | Combine old and new values |

---

### 7. Objects Utility

- **`Objects.requireNonNull(obj, "message")`** — fail fast on null.
- **`Objects.equals(a, b)`** — null-safe equals.
- **`Objects.hash(...)`** — null-safe hash.
- **`Objects.isNull` / `nonNull`** — for predicates.

---

### 8. SplittableRandom

- For parallel computations needing independent random streams.
- **`split()`** — fork new generator for subtask.
- Better than sharing `Random` across threads.

---

## Examples

### Read file, process, write

```java
Path input = Paths.get("input.txt");
Path output = Paths.get("output.txt");

List<String> upper = Files.lines(input)
    .map(String::toUpperCase)
    .collect(Collectors.toList());

Files.write(output, upper);
```

### Base64 encode/decode

```java
String original = "Hello Java 8";
String encoded = Base64.getEncoder().encodeToString(original.getBytes(StandardCharsets.UTF_8));
byte[] decoded = Base64.getDecoder().decode(encoded);
```

### StringJoiner

```java
StringJoiner joiner = new StringJoiner(", ", "[", "]");
joiner.add("Alice").add("Bob").add("Charlie");
String result = joiner.toString(); // [Alice, Bob, Charlie]

String joined = String.join(" | ", List.of("a", "b", "c")); // a | b | c
```

### Map computeIfAbsent — cache pattern

```java
Map<String, List<String>> cache = new HashMap<>();
cache.computeIfAbsent("fruits", k -> new ArrayList<>()).add("apple");
```

### parallelSort

```java
int[] data = IntStream.range(0, 1_000_000).toArray();
Arrays.parallelSort(data); // faster than sort() for large arrays
```

### Unsigned arithmetic (Integer)

```java
int unsigned = Integer.parseUnsignedInt("4294967295");
String hex = Integer.toHexString(unsigned);
long sum = Integer.sum(1, 2);
```

---

## Practice Exercises

| # | Exercise | Difficulty |
|---|----------|------------|
| 1 | Count lines in file containing a word (Files.lines) | Easy |
| 2 | Encode/decode image bytes with Base64 URL-safe | Easy |
| 3 | Build CSV from `List<Employee>` using StringJoiner | Easy |
| 4 | Implement word frequency from 1GB file using streams (no load all) | Hard |
| 5 | Use `removeIf` to purge expired entries from list | Easy |
| 6 | `computeIfAbsent` memoization for expensive Fibonacci | Medium |
| 7 | Benchmark `Arrays.sort` vs `parallelSort` on 10M ints | Medium |
| 8 | Use `Objects.requireNonNull` in constructor with custom message | Easy |

---

## Interview Q&A (5–8 Years Experience)

### Q1. Why must Files.lines be closed?

**Answer:** It wraps underlying `InputStream`/`BufferedReader`. Stream's `onClose` handler closes resources. Leak if not closed (try-with-resources or `stream.close()`). Unlike collection streams, this ties to OS file handle.

---

### Q2. Base64.getEncoder vs getUrlEncoder?

**Answer:** Standard uses `+` and `/` — problematic in URLs. URL encoder uses `-` and `_`, no padding (optional). MIME encoder inserts line separators every 76 chars for email.

---

### Q3. computeIfAbsent vs putIfAbsent?

**Answer:**
- **`putIfAbsent`:** Only puts if key missing; returns existing or null.
- **`computeIfAbsent`:** If absent, **computes** value via function. Atomic in `ConcurrentHashMap`. Beware expensive computation inside — can block bucket.

---

### Q4. When does parallelSort beat sort?

**Answer:** Large arrays (typically **millions** of elements) on multi-core. Small arrays — sequential faster due to fork/join overhead. Object arrays require comparable — stable sort guaranteed.

---

### Q5. What is parallelPrefix used for?

**Answer:** Parallel prefix sum/scan. Example: cumulative totals, parallel histogram building. Each element replaced with aggregate of all previous + self per associative op.

---

### Q6. Difference between StringJoiner and Collectors.joining?

**Answer:** `StringJoiner` is mutable builder; `Collectors.joining` is terminal stream operation. `Collectors.joining` uses StringJoiner internally. Use joiner for manual building; joining in streams.

---

### Q7. Objects.requireNonNull vs explicit if-throw?

**Answer:** `requireNonNull` is standard, concise, may be optimized by JVM. Overload with `Supplier<String>` for lazy message. Consistent NPE messages across codebase.

---

### Q8. Pattern.splitAsStream vs String.split?

**Answer:** `splitAsStream` returns lazy `Stream<String>` — better for large input, composable with filters. `String.split` returns array — eager, all in memory. Regex compiled once with `Pattern`.

---

## Self-Check

- [ ] I always close Files.lines streams
- [ ] I use Base64 instead of deprecated internal APIs
- [ ] I know Map compute/merge methods
- [ ] I use Objects.requireNonNull in public APIs

**Next:** [Phase 8 — Nashorn →](08-nashorn-scripting.md)
