# 15.4 Stream API

## Stream API

## Basics

---

# 1. What is Stream?

<details>
<summary>Show Answer</summary>

**Answer:**

A **Stream** is a **sequence of elements** that supports **functional-style operations** (filter, map, reduce) in a **pipeline**—it is not a data storage structure.

### Definition

```java
List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);

list.stream()
    .filter(n -> n % 2 == 0)
    .map(n -> n * 2)
    .forEach(System.out::println);
// Output: 4, 8
```

### Key Properties

| Property | Detail |
|----------|--------|
| **Not a data structure** | Does not store elements |
| **Pipeline** | Source → intermediate → terminal |
| **Lazy** | Intermediate ops deferred until terminal |
| **Consumable** | One-time use only |
| **Sequential or parallel** | `stream()` or `parallelStream()` |

### Stream Pipeline

```text
Source (collection, array, etc.)
    ↓
Intermediate operations (filter, map, sorted) — lazy
    ↓
Terminal operation (collect, forEach, count) — triggers execution
```

### Creating Streams

```java
list.stream()
Arrays.stream(array)
Stream.of(1, 2, 3)
Stream.iterate(0, n -> n + 1).limit(10)
Stream.generate(() -> Math.random()).limit(5)
IntStream.range(1, 10)
```

### Primitive Streams

```java
IntStream    // int values — no boxing
LongStream   // long values
DoubleStream // double values
```

### What Stream Does

```text
✅ Filter, transform, sort, aggregate data
✅ Declarative "what to do" style
✅ Supports parallel processing
❌ Does NOT store data
❌ Does NOT modify source (usually)
```

**Interview Point:**

> Stream = **computation pipeline** on a data source. Not a collection—operations chained lazily until terminal op runs.

</details>

---

# 2. Difference between Stream and Collection?

<details>
<summary>Show Answer</summary>

**Answer:**

A **Collection** stores data; a **Stream** performs computations on data without storing it.

### Comparison Table

| Feature | Collection | Stream |
|---------|------------|--------|
| **Purpose** | Store elements | Process elements |
| **Nature** | Data structure | Computation pipeline |
| **Storage** | Holds data in memory | No storage |
| **Iteration** | External (`for`, `iterator`) | Internal (stream drives) |
| **Reusable** | ✅ Yes | ❌ One-time use |
| **Lazy** | No (eager) | Yes (intermediate ops) |
| **Parallel** | Manual threading | `parallelStream()` built-in |
| **Modification** | Can add/remove | Does not modify source* |

*Stream operations don't modify source collection unless mutating terminal op used.

### Collection — Stores Data

```java
List<String> list = new ArrayList<>();
list.add("Java");
list.add("Python");
// Data lives in list — can access anytime
list.get(0); // "Java"
```

### Stream — Processes Data

```java
list.stream()
    .filter(s -> s.startsWith("J"))
    .map(String::toUpperCase)
    .forEach(System.out::println);
// No data stored in stream — pipeline executes and done
```

### Reusability Difference

```java
Stream<String> stream = list.stream();

stream.forEach(System.out::println); // ✅ first use
stream.forEach(System.out::println); // ❌ IllegalStateException — already consumed
```

```java
List<String> list = ...;
list.forEach(...); // ✅ can iterate again and again
```

### When to Use Which

| Use Collection | Use Stream |
|----------------|------------|
| Store and retrieve data | Transform/filter/aggregate |
| Multiple traversals needed | One-pass computation |
| CRUD operations | Read-only processing |
| Entity/model layer | Business logic layer |

**Interview Point:**

> Collection = **container**. Stream = **pipeline**. Collection stores; Stream computes. Stream is consumable once.

</details>

---

# 3. Internal iteration vs external iteration?

<details>
<summary>Show Answer</summary>

**Answer:**

**External iteration** — you control the loop (iterator, for-each). **Internal iteration** — the Stream API controls iteration—you pass behavior, stream applies it.

### External Iteration (Traditional)

```java
// YOU control the loop
List<String> names = Arrays.asList("Alice", "Bob", "Charlie");

for (String name : names) {
    if (name.startsWith("A")) {
        System.out.println(name.toUpperCase());
    }
}

// Or with iterator
Iterator<String> it = names.iterator();
while (it.hasNext()) {
    String name = it.next();
    // process
}
```

### Internal Iteration (Stream)

```java
// STREAM controls the loop — you pass WHAT to do
names.stream()
     .filter(name -> name.startsWith("A"))
     .map(String::toUpperCase)
     .forEach(System.out::println);
```

### Comparison

| | External | Internal |
|---|----------|----------|
| Loop control | Developer | Stream/library |
| Style | Imperative ("how") | Declarative ("what") |
| Example | `for`, `while`, `iterator` | `stream().forEach()` |
| Parallel | Manual | `parallelStream()` |
| Abstraction | Low | High |

### Benefits of Internal Iteration

```text
1. Cleaner declarative code
2. Library can optimize (lazy, parallel, short-circuit)
3. Separation of iteration logic from business logic
4. Easy parallelization
```

### Internal Iteration Enables Short-Circuit

```java
// Stream can stop early — findFirst doesn't process entire list
Optional<String> first = names.stream()
    .filter(s -> s.length() > 10)
    .findFirst(); // stops at first match
```

### External Cannot Short-Circuit Easily

```java
// Must manually break loop
for (String s : names) {
    if (s.length() > 10) {
        result = s;
        break; // manual short-circuit
    }
}
```

**Interview Point:**

> External = you iterate. Internal = stream iterates. Streams use internal iteration—enables lazy evaluation, parallel processing, and short-circuit ops.

</details>

---

# 4. Lazy evaluation?

<details>
<summary>Show Answer</summary>

**Answer:**

**Lazy evaluation** means **intermediate stream operations are not executed** until a **terminal operation** is invoked—computation is deferred.

### How It Works

```text
Intermediate ops (filter, map) → build pipeline, do NOTHING yet
Terminal op (collect, forEach)   → triggers entire pipeline execution
```

### Example — Nothing Runs Until Terminal

```java
List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);

Stream<Integer> stream = list.stream()
    .filter(n -> {
        System.out.println("filter: " + n);
        return n % 2 == 0;
    })
    .map(n -> {
        System.out.println("map: " + n);
        return n * 2;
    });

// Nothing printed yet! Pipeline built but not executed

stream.forEach(n -> System.out.println("result: " + n));
// NOW everything runs:
// filter: 1, filter: 2, map: 2, result: 4
// filter: 3, filter: 4, map: 4, result: 8
// filter: 5
```

### Lazy vs Eager

| | Lazy (Intermediate) | Eager (Terminal) |
|---|---------------------|------------------|
| Examples | `filter`, `map`, `sorted` | `collect`, `forEach`, `count` |
| Executes | Only when terminal called | Immediately |
| Returns | New Stream | Result or void |

### Benefits of Laziness

```text
1. Short-circuit — stop early (findFirst, anyMatch)
2. Fusion — combine operations efficiently
3. No unnecessary work — process only needed elements
4. Infinite streams possible (Stream.iterate, generate)
```

### Short-Circuit Example

```java
// Only processes until first match — lazy + short-circuit
Optional<Integer> first = list.stream()
    .filter(n -> n > 2)
    .findFirst(); // doesn't scan entire list if match found early
```

### Infinite Stream (Only Possible with Laziness)

```java
Stream.iterate(1, n -> n + 1)
      .filter(n -> n % 2 == 0)
      .limit(5)           // lazy — stops at 5
      .forEach(System.out::println);
// 2, 4, 6, 8, 10 — infinite source, finite result
```

**Interview Point:**

> Intermediate ops are **lazy**—deferred until terminal op. Enables short-circuit, infinite streams, and performance optimization.

</details>

---

## Intermediate

---

# 5. Intermediate operations?

<details>
<summary>Show Answer</summary>

**Answer:**

**Intermediate operations** transform a stream into another stream—they are **lazy**, return a **new Stream**, and do not execute until a terminal operation is called.

### Characteristics

| Property | Detail |
|----------|--------|
| Returns | New Stream |
| Lazy | Not executed until terminal op |
| Chainable | Can chain multiple intermediate ops |
| Stateless vs Stateful | Most are stateless; `sorted`, `distinct` are stateful |

### Common Intermediate Operations

| Operation | Purpose | Example |
|-----------|---------|---------|
| `filter(Predicate)` | Keep matching elements | `.filter(x -> x > 10)` |
| `map(Function)` | Transform each element | `.map(x -> x * 2)` |
| `flatMap(Function)` | Flatten nested structures | `.flatMap(list -> list.stream())` |
| `distinct()` | Remove duplicates | `.distinct()` |
| `sorted()` | Sort elements | `.sorted()` |
| `peek(Consumer)` | Debug/observe (side effect) | `.peek(System.out::println)` |
| `limit(n)` | Take first n elements | `.limit(10)` |
| `skip(n)` | Skip first n elements | `.skip(5)` |
| `mapToInt/Long/Double` | Map to primitive stream | `.mapToInt(String::length)` |

### Example Pipeline

```java
List<String> result = words.stream()      // source
    .filter(w -> w.length() > 3)          // intermediate — lazy
    .map(String::toUpperCase)             // intermediate — lazy
    .distinct()                           // intermediate — lazy
    .sorted()                             // intermediate — lazy
    .collect(Collectors.toList());        // terminal — triggers all above
```

### Stateless vs Stateful

```java
// Stateless — each element processed independently
.filter(), .map(), .peek(), .limit(), .skip()

// Stateful — need to see all/some elements
.distinct(), .sorted(), .limit(n), .skip(n)
```

### Nothing Executes Without Terminal

```java
Stream<String> s = list.stream()
    .filter(x -> x.length() > 5)
    .map(String::toUpperCase);
// No filtering or mapping happened yet!
```

**Interview Point:**

> Intermediate ops return Stream, are lazy, and chainable. Common: `filter`, `map`, `flatMap`, `sorted`, `distinct`, `limit`, `skip`.

</details>

---

# 6. Terminal operations?

<details>
<summary>Show Answer</summary>

**Answer:**

**Terminal operations** **trigger pipeline execution**, produce a **result** or **side effect**, and **close the stream**—after terminal op, stream cannot be reused.

### Characteristics

| Property | Detail |
|----------|--------|
| Returns | Result, Optional, or void |
| Eager | Triggers all intermediate ops |
| Closes stream | Stream consumed — cannot reuse |
| One per pipeline | Only one terminal op allowed |

### Common Terminal Operations

| Operation | Returns | Purpose |
|-----------|---------|---------|
| `forEach(Consumer)` | void | Act on each element |
| `collect(Collector)` | Collection/Map | Accumulate to collection |
| `reduce(identity, op)` | Optional/T | Combine all elements |
| `count()` | long | Count elements |
| `min(Comparator)` | Optional | Minimum element |
| `max(Comparator)` | Optional | Maximum element |
| `findFirst()` | Optional | First element |
| `findAny()` | Optional | Any element |
| `anyMatch(Predicate)` | boolean | Any matches? |
| `allMatch(Predicate)` | boolean | All match? |
| `noneMatch(Predicate)` | boolean | None match? |
| `toArray()` | Object[] | To array |

### Examples

```java
// forEach — side effect
list.stream().forEach(System.out::println);

// collect — accumulate
List<String> upper = list.stream()
    .map(String::toUpperCase)
    .collect(Collectors.toList());

// reduce — combine
int sum = numbers.stream().reduce(0, Integer::sum);

// count
long count = list.stream().filter(x -> x > 10).count();

// findFirst — short-circuit
Optional<String> first = list.stream()
    .filter(s -> s.startsWith("A"))
    .findFirst();
```

### Short-Circuit Terminal Ops

```java
// Stop as soon as condition met
boolean hasEven = list.stream().anyMatch(n -> n % 2 == 0);
Optional<Integer> first = list.stream().findFirst();
boolean allPositive = list.stream().allMatch(n -> n > 0);
```

### collect() — Most Used Terminal

```java
// To list
.collect(Collectors.toList())

// To set
.collect(Collectors.toSet())

// Grouping
.collect(Collectors.groupingBy(Employee::getDept))

// Joining strings
.collect(Collectors.joining(", "))
```

**Interview Point:**

> Terminal op **triggers execution** and **consumes stream**. `collect`, `forEach`, `reduce`, `count`, `findFirst` are most common. Only one terminal per pipeline.

</details>

---

# 7. map() vs flatMap()?

<details>
<summary>Show Answer</summary>

**Answer:**

`map()` transforms **one element to one element**. `flatMap()` transforms **one element to a stream of elements** and **flattens** the result.

### map() — 1 to 1

```java
Function<T, R>  // one input → one output

List<String> words = Arrays.asList("hello", "world");
List<Integer> lengths = words.stream()
    .map(s -> s.length())    // "hello" → 5, "world" → 5
    .collect(Collectors.toList());
// [5, 5]
```

### flatMap() — 1 to many (flattened)

```java
Function<T, Stream<R>>  // one input → stream of outputs → flattened

List<List<Integer>> nested = Arrays.asList(
    Arrays.asList(1, 2),
    Arrays.asList(3, 4, 5)
);

List<Integer> flat = nested.stream()
    .flatMap(list -> list.stream())  // [[1,2],[3,4,5]] → [1,2,3,4,5]
    .collect(Collectors.toList());
```

### Visual Comparison

```text
map:     [A, B]  → map(f)  → [f(A), f(B)]        — same count
flatMap: [A, B]  → flatMap → [a1,a2, b1,b2,b3]   — flattened
```

### map() Trap — Nested Structure

```java
// map keeps nested structure
List<List<Integer>> stillNested = nested.stream()
    .map(list -> list)  // each list stays as list
    .collect(Collectors.toList());
// [[1,2], [3,4,5]] — still nested!
```

### Real Examples

```java
// map — transform
employees.stream().map(Employee::getName)
words.stream().map(String::toUpperCase)

// flatMap — split and flatten
sentence.stream()
    .flatMap(s -> Stream.of(s.split(" ")))  // words from sentence

// flatMap — optional unwrapping
users.stream()
     .map(User::getAddress)        // Optional<Address>
     .flatMap(Optional::stream)    // Address or skip

// flatMap — nested collections
departments.stream()
    .flatMap(d -> d.getEmployees().stream())
    .map(Employee::getName)
```

### Comparison Table

| | `map()` | `flatMap()` |
|---|---------|-------------|
| Input→Output | T → R | T → Stream\<R\> → R |
| Result size | Same as source | Can be larger |
| Use | Transform | Flatten nested |
| Optional | `map` gives Optional\<R\> | `flatMap` unwraps |

**Interview Point:**

> `map` = 1-to-1 transform. `flatMap` = 1-to-many then flatten. Use `flatMap` for nested lists, splitting strings, Optional chaining.

</details>

---

# 8. filter()?

<details>
<summary>Show Answer</summary>

**Answer:**

`filter()` is an **intermediate operation** that **selects elements** matching a `Predicate`—keeps elements where condition is `true`.

### Syntax

```java
Stream<T> filter(Predicate<? super T> predicate)
```

### Basic Example

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6);

List<Integer> evens = numbers.stream()
    .filter(n -> n % 2 == 0)
    .collect(Collectors.toList());
// [2, 4, 6]
```

### With Predicate

```java
Predicate<String> notEmpty = s -> !s.isEmpty();
Predicate<Integer> positive = n -> n > 0;

list.stream()
    .filter(notEmpty)
    .filter(positive)
    .collect(Collectors.toList());
```

### Real Production Examples

```java
// Filter active employees
employees.stream()
         .filter(Employee::isActive)
         .collect(Collectors.toList());

// Filter by salary
employees.stream()
         .filter(e -> e.getSalary() > 50000)
         .map(Employee::getName)
         .collect(Collectors.toList());

// Filter nulls
list.stream()
    .filter(Objects::nonNull)
    .collect(Collectors.toList());
```

### Chaining Filters

```java
orders.stream()
      .filter(o -> o.getStatus().equals("PENDING"))
      .filter(o -> o.getAmount() > 1000)
      .filter(o -> o.getCustomer().isVIP())
      .collect(Collectors.toList());
```

### filter() Properties

| Property | Detail |
|----------|--------|
| Type | Intermediate (lazy) |
| Returns | Stream of matching elements |
| Short-circuit | Works with findFirst, anyMatch |
| Can chain | Multiple filters in sequence |

### filter vs removeIf

```java
// filter — creates new stream/list (non-destructive)
List<Integer> result = list.stream()
    .filter(x -> x > 10)
    .collect(Collectors.toList());

// removeIf — modifies original list
list.removeIf(x -> x > 10);
```

**Interview Point:**

> `filter(Predicate)` keeps elements where condition is true. Lazy intermediate op. Chain multiple filters or combine with `Predicate.and()`.

</details>

---

# 9. distinct()?

<details>
<summary>Show Answer</summary>

**Answer:**

`distinct()` is a **stateful intermediate operation** that removes **duplicate elements** using `equals()` and `hashCode()`.

### Basic Example

```java
List<Integer> numbers = Arrays.asList(1, 2, 2, 3, 3, 3, 4);

List<Integer> unique = numbers.stream()
    .distinct()
    .collect(Collectors.toList());
// [1, 2, 3, 4]
```

### String Deduplication

```java
List<String> words = Arrays.asList("java", "python", "java", "go");

List<String> unique = words.stream()
    .distinct()
    .collect(Collectors.toList());
// ["java", "python", "go"]
```

### How It Works

```text
Uses HashSet internally to track seen elements
Relies on equals() and hashCode() of elements
Stateful — must see elements (or maintain state)
```

### Custom Objects

```java
// Uses Employee.equals() and hashCode()
List<Employee> unique = employees.stream()
    .distinct()
    .collect(Collectors.toList());
// Employees with same equals/hashCode → only one kept
```

### distinct() vs Set

```java
// Stream distinct
List<String> unique = list.stream()
    .distinct()
    .collect(Collectors.toList());

// Set approach — same result often
Set<String> uniqueSet = new HashSet<>(list);
```

### With Other Operations

```java
// Unique uppercase words
List<String> result = words.stream()
    .map(String::toLowerCase)
    .distinct()
    .sorted()
    .collect(Collectors.toList());
```

### Properties

| Property | Detail |
|----------|--------|
| Type | Stateful intermediate |
| Uses | `equals()` + `hashCode()` |
| Order | Preserves encounter order |
| Memory | Holds seen elements in HashSet |

**Interview Point:**

> `distinct()` removes duplicates via `equals()`/`hashCode()`. Stateful intermediate op. Preserves first occurrence order.

</details>

---

# 10. sorted()?

<details>
<summary>Show Answer</summary>

**Answer:**

`sorted()` is a **stateful intermediate operation** that returns elements in **sorted order**—natural order or custom `Comparator`.

### Natural Order

```java
List<Integer> numbers = Arrays.asList(3, 1, 4, 1, 5);

List<Integer> sorted = numbers.stream()
    .sorted()  // natural order — Comparable
    .collect(Collectors.toList());
// [1, 1, 3, 4, 5]
```

### Custom Comparator

```java
List<String> words = Arrays.asList("java", "go", "python");

// Ascending by length
List<String> byLength = words.stream()
    .sorted(Comparator.comparingInt(String::length))
    .collect(Collectors.toList());
// ["go", "java", "python"]

// Descending
List<String> descending = words.stream()
    .sorted(Comparator.comparing(String::toLowerCase).reversed())
    .collect(Collectors.toList());
```

### Sort Employees

```java
employees.stream()
         .sorted(Comparator.comparing(Employee::getSalary).reversed())
         .limit(10)
         .collect(Collectors.toList());
```

### sorted() Variants

```java
.sorted()                              // natural order
.sorted(Comparator.naturalOrder())     // explicit natural
.sorted(Comparator.reverseOrder())     // reverse natural
.sorted(Comparator.comparing(Employee::getName))
.sorted(Comparator.comparingInt(Employee::getSalary))
```

### Multi-Level Sort

```java
employees.stream()
         .sorted(Comparator.comparing(Employee::getDepartment)
                           .thenComparing(Employee::getName))
         .collect(Collectors.toList());
```

### Properties

| Property | Detail |
|----------|--------|
| Type | Stateful intermediate |
| No arg | Natural order (`Comparable`) |
| With Comparator | Custom order |
| Stable | Equal elements keep order |

### sorted() vs Collections.sort()

```java
// Stream — lazy until terminal
list.stream().sorted().collect(Collectors.toList());

// Collections — sorts in place
Collections.sort(list);
```

**Interview Point:**

> `sorted()` = stateful intermediate op. Natural order or `Comparator`. Use `Comparator.comparing()` for field-based sorting.

</details>

---

# 11. limit()?

<details>
<summary>Show Answer</summary>

**Answer:**

`limit(n)` is a **short-circuit intermediate operation** that returns a stream of **at most n elements**—truncates after n items.

### Basic Example

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

List<Integer> firstThree = numbers.stream()
    .limit(3)
    .collect(Collectors.toList());
// [1, 2, 3]
```

### Top N Pattern

```java
// Top 5 highest salaries
List<Employee> top5 = employees.stream()
    .sorted(Comparator.comparingInt(Employee::getSalary).reversed())
    .limit(5)
    .collect(Collectors.toList());
```

### With Infinite Stream

```java
// First 10 even numbers from infinite stream
List<Integer> evens = Stream.iterate(0, n -> n + 1)
    .filter(n -> n % 2 == 0)
    .limit(10)
    .collect(Collectors.toList());
// [0, 2, 4, 6, 8, 10, 12, 14, 16, 18]
```

### limit() Enables Short-Circuit

```java
// Only processes first 3 elements — doesn't scan entire list
List<Integer> result = bigList.stream()
    .filter(expensiveOperation)
    .limit(3)
    .collect(Collectors.toList());
```

### limit() vs skip() Together

```java
// Pagination — skip 10, take 5 (page 3, size 5)
List<String> page = items.stream()
    .skip(10)
    .limit(5)
    .collect(Collectors.toList());
```

### Properties

| Property | Detail |
|----------|--------|
| Type | Short-circuit intermediate |
| Arg | `long n` — max elements |
| Behavior | Stops after n elements |
| With infinite | Makes finite stream possible |

**Interview Point:**

> `limit(n)` takes first n elements. Short-circuit op—stops early. Common for top-N queries and paginating streams.

</details>

---

# 12. skip()?

<details>
<summary>Show Answer</summary>

**Answer:**

`skip(n)` is a **stateful intermediate operation** that **discards the first n elements** and returns a stream of the remaining elements.

### Basic Example

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6);

List<Integer> afterSkip = numbers.stream()
    .skip(3)
    .collect(Collectors.toList());
// [4, 5, 6] — first 3 discarded
```

### Skip and Limit — Pagination

```java
// Page 2, page size 5 (skip first 5, take next 5)
List<String> page2 = items.stream()
    .skip(5)
    .limit(5)
    .collect(Collectors.toList());

// General pagination
int page = 2, pageSize = 10;
List<Item> pageItems = allItems.stream()
    .skip((page - 1) * pageSize)
    .limit(pageSize)
    .collect(Collectors.toList());
```

### Skip Header Rows

```java
// Skip first row (header) in CSV-like data
rows.stream()
    .skip(1)
    .forEach(processRow);
```

### skip() on Infinite Stream

```java
// Skip first 100, take next 10
Stream.iterate(0, n -> n + 1)
      .skip(100)
      .limit(10)
      .forEach(System.out::println);
// 100, 101, 102, ... 109
```

### Properties

| Property | Detail |
|----------|--------|
| Type | Stateful intermediate |
| Arg | `long n` — elements to skip |
| Behavior | Discards first n, returns rest |
| Order | skip then limit for pagination |

### skip() vs subList()

```java
// Stream — lazy, works on any stream
list.stream().skip(5).limit(10).collect(toList());

// List subList — eager, list only
list.subList(5, 15);
```

**Interview Point:**

> `skip(n)` discards first n elements. Stateful intermediate. Combine with `limit()` for pagination: `skip((page-1)*size).limit(size)`.

</details>

---

## Advanced Stream Questions

---

# 13. How Stream pipeline works?

<details>
<summary>Show Answer</summary>

**Answer:**

A stream pipeline has three parts: **source → intermediate operations → terminal operation**. Execution is **lazy** until the terminal op triggers the full pipeline.

### Pipeline Structure

```text
┌─────────┐    ┌──────────────────────┐    ┌─────────────┐
│ SOURCE  │ →  │ INTERMEDIATE (lazy)  │ →  │ TERMINAL    │
│         │    │ filter, map, sorted   │    │ collect     │
└─────────┘    └──────────────────────┘    └─────────────┘
```

### Step-by-Step Execution

```java
List<Integer> result = numbers.stream()           // 1. Source
    .filter(n -> n % 2 == 0)                      // 2. Build pipeline (lazy)
    .map(n -> n * 2)                              // 3. Build pipeline (lazy)
    .sorted()                                     // 4. Build pipeline (lazy)
    .collect(Collectors.toList());                // 5. EXECUTE everything
```

### Execution Flow (When Terminal Runs)

```text
1. Terminal op requests element from upstream
2. filter checks element — pass or skip
3. map transforms passed element
4. sorted buffers (stateful) or passes through
5. Terminal receives processed element
6. Repeat until source exhausted
```

### Fusion Optimization

```text
JVM may fuse intermediate ops:
  filter + map → single pass per element
  Instead of: filter all → then map all
  Does: filter-map-filter-map per element (pipelined)
```

### Example — Full Pipeline Trace

```java
Arrays.asList(1,2,3,4,5).stream()
    .filter(n -> n > 2)    // keeps 3,4,5
    .map(n -> n * 10)      // 30,40,50
    .filter(n -> n < 45)   // keeps 30,40
    .forEach(System.out::println);
// Output: 30, 40
```

### Parallel Pipeline

```text
Source split into chunks
Each chunk processed on different thread (ForkJoinPool)
Results combined at terminal op
```

**Interview Point:**

> Pipeline = source + lazy intermediates + eager terminal. Terminal triggers execution. Ops may be fused for efficiency. One terminal per pipeline.

</details>

---

# 14. Why Streams are lazy?

<details>
<summary>Show Answer</summary>

**Answer:**

Streams are lazy because **intermediate operations defer work** until a **terminal operation** demands results—avoiding unnecessary computation and enabling optimizations.

### Reasons for Laziness

| Reason | Benefit |
|--------|---------|
| **Avoid unnecessary work** | Don't process elements not needed |
| **Short-circuit** | `findFirst` stops at first match |
| **Infinite streams** | `iterate`, `generate` need laziness |
| **Fusion** | Combine ops into single pass |
| **Performance** | Process only what's required |

### Without Laziness — Wasteful

```java
// If eager: would filter ALL, map ALL, then take first
// With laziness: filter-map until first match, then STOP

Optional<Integer> first = numbers.stream()
    .filter(n -> n > 100)      // expensive check
    .map(n -> compute(n))       // expensive transform
    .findFirst();               // stops at first match — lazy!
```

### Infinite Streams Require Laziness

```java
// Only possible because of laziness
Stream.iterate(1, n -> n * 2)   // infinite: 1,2,4,8,16...
      .limit(10)                 // lazy limit
      .forEach(System.out::println);
// 1, 2, 4, 8, 16, 32, 64, 128, 256, 512
```

### Lazy Build vs Eager Execute

```java
// Building pipeline — NO execution
Stream<Integer> pipeline = list.stream()
    .filter(x -> x > 10)
    .map(x -> x * 2);

// Execution happens HERE
long count = pipeline.count();
```

### Short-Circuit Depends on Laziness

```java
boolean hasNegative = list.stream()
    .anyMatch(n -> n < 0);  // stops at first negative — lazy

Optional<String> first = list.stream()
    .filter(s -> s.length() > 5)
    .findFirst();            // stops at first match — lazy
```

**Interview Point:**

> Laziness = intermediate ops don't run until terminal op. Enables short-circuit, infinite streams, and fusion optimization.

</details>

---

# 15. Can Stream be reused?

<details>
<summary>Show Answer</summary>

**Answer:**

**No.** A stream can be used **only once**. After a terminal operation, the stream is **consumed** and cannot be reused.

### Example — Illegal Reuse

```java
Stream<String> stream = list.stream();

stream.forEach(System.out::println);  // ✅ first use — OK

stream.forEach(System.out::println);  // ❌ IllegalStateException
// "stream has already been operated upon or closed"
```

### Why Not Reusable?

```text
Stream = one-pass iterator over source
Terminal op consumes all elements
Internal state marked as closed/consumed
Source elements may be partially processed
```

### Collection vs Stream Reuse

```java
List<String> list = ...;

list.forEach(...);  // ✅ reuse collection
list.stream()...;   // ✅ create new stream anytime

Stream<String> s = list.stream();
s.forEach(...);     // consumed
s.forEach(...);     // ❌ cannot reuse same stream object
```

### Solution — Create New Stream

```java
// Need two operations? Create two streams from same source
List<String> upper = list.stream()
    .map(String::toUpperCase)
    .collect(Collectors.toList());

List<String> lower = list.stream()  // new stream from same list
    .map(String::toLowerCase)
    .collect(Collectors.toList());
```

### Or — Collect Once, Reuse Collection

```java
List<String> processed = list.stream()
    .filter(s -> s.length() > 3)
    .collect(Collectors.toList());

// Reuse the collected list
processed.forEach(...);
processed.stream().map(...);
```

### Supplier Pattern for Reusable Pipeline

```java
Supplier<Stream<String>> streamSupplier = () ->
    list.stream().filter(s -> s.length() > 5);

streamSupplier.get().forEach(...);  // new stream each time
streamSupplier.get().count();       // another new stream
```

**Interview Point:**

> Stream is **one-time use**. After terminal op → `IllegalStateException`. Create new stream from source or collect to list for reuse.

</details>

---

# 16. Why Stream is not a data structure?

<details>
<summary>Show Answer</summary>

**Answer:**

A Stream **does not store elements**—it is a **wrapper around a data source** that provides a pipeline for computation. Data lives in the **source** (List, array), not in the stream.

### Stream vs Data Structure

| Data Structure (Collection) | Stream |
|-------------------------------|--------|
| Stores elements in memory | No storage |
| `get(index)`, `size()` | No direct element access |
| Add, remove elements | Cannot add to stream |
| Persistent container | Temporary computation view |

### What Stream Actually Is

```text
Stream = abstraction for processing elements from a SOURCE
Source holds data: List, Set, array, file, generator
Stream = pipeline operations ON that source
```

### No Storage Proof

```java
List<Integer> list = Arrays.asList(1, 2, 3);

Stream<Integer> stream = list.stream()
    .filter(n -> n > 1)
    .map(n -> n * 2);

// stream has no size(), no get(i)
// stream.count() — must traverse to count
// stream.toList() — must traverse to collect
```

### Analogy

```text
Collection = warehouse (stores goods)
Stream      = conveyor belt (moves goods through processing)
Goods live in warehouse, not on the belt
```

### Stream Wraps Source

```java
// Data in list — stream just provides operations
List<String> data = Arrays.asList("a", "b", "c");

data.stream()           // stream wraps list — doesn't copy data
     .filter(...)
     .map(...);         // processes elements from list on-the-fly
```

### Implications

```text
1. Cannot get element by index
2. Cannot know size without traversing (for filtered streams)
3. One-time traversal — consumable
4. Operations are computations, not storage
5. Infinite streams possible — no storage needed
```

### Stream is a Computation Abstraction

```java
// This is a computation description, not stored data
Stream<Integer> pipeline = numbers.stream()
    .filter(n -> n % 2 == 0)
    .map(n -> n * 2);
// pipeline describes WHAT to compute, not a container of results
```

**Interview Point:**

> Stream = **computation pipeline**, not container. Data stays in source. No `get()`, no `add()`. One-pass consumable view over a source.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Does stream modify the source collection?

<details>
<summary>Show Answer</summary>

**Answer:** Generally **no**—intermediate ops don't modify source. Exception: if you collect to same list or use mutating operations in `forEach`. Source remains unchanged after `filter().map().collect()`.

</details>

---

### Q: parallelStream() vs stream().parallel()?

<details>
<summary>Show Answer</summary>

**Answer:** Functionally same—both create parallel stream. `parallelStream()` is shorthand. `stream().parallel()` can switch sequential→parallel mid-pipeline with `.sequential()`.

</details>

---

### Q: peek() vs forEach()?

<details>
<summary>Show Answer</summary>

**Answer:** `peek()` = intermediate (lazy, for debugging in pipeline). `forEach()` = terminal (eager, triggers execution). Use `peek` to inspect pipeline; `forEach` for final action.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Stream = lazy pipeline: source → intermediate ops → terminal op. Not a data structure, not reusable. Internal iteration, lazy until terminal. `map` = 1-to-1, `flatMap` = flatten. `filter`, `sorted`, `limit`, `skip` are key intermediates.

</details>
