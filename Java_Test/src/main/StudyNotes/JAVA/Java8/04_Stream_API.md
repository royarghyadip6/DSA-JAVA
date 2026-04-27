# 🚀 Java Streams API — Complete Guide

---

# Referance : [Interface Stream<T>](https://docs.oracle.com/javase/8/docs/api/java/util/stream/Stream.html)

---

# 🔹 Introduction to Streams

👉 A **Stream** is a sequence of elements supporting sequential and parallel operations.

* To perform a computation, stream operations are composed into a stream pipeline. A stream pipeline consists of a **source (`which might be an array, a collection, a generator function, an I/O channel, etc`), zero or more intermediate operations (`which transform a stream into another stream, such as filter(Predicate`)), and a terminal operation (`which produces a result or side-effect, such as count() or forEach(Consumer)`)**.
* Streams are lazy; computation on the source data is only performed when the terminal operation is initiated, and source elements are consumed only as needed.

* Stream is a **stream of object** references, primitive specializations streams are  **IntStream**, **LongStream**, and **DoubleStream**.

* Streams have a `BaseStream.close()` method and implement `AutoCloseable`, but nearly all stream instances do not actually need to be closed after use. Generally, only streams whose source is an IO channel (`such as those returned by Files.lines(Path, Charset)`) will require closing.

* `Collection.stream()` creates a sequential stream, and `Collection.parallelStream()` creates a parallel one.

> **Syntax : public interface Stream<T> extends BaseStream<T,Stream<T>>**

---

### Example:

```java
List<Integer> list = Arrays.asList(1,2,3,4);

list.stream()
    .filter(n -> n % 2 == 0)
    .forEach(System.out::println);
```

---

# 🔹 Stream vs Collection

| Feature     | Collection     | Stream      |
|-------------|----------------|-------------|
| Storage     | Stores data    | No storage  |
| Nature      | Data structure | Computation |
| Iteration   | External       | Internal    |
| Reusability | Yes            | No          |

---

### Key Insight:

> Stream = **pipeline of operations**, not a container

---

# 🔹 Stream Lifecycle

```text
Source → Intermediate Ops → Terminal Op
```

---

### Example:

```java
list.stream()        // Source
    .filter(x -> x > 2)  // Intermediate
    .forEach(System.out::println); // Terminal
```

---

# 🔹 Creating Streams

---

### 1. From Collection

```java
list.stream();
```

---

### 2. From Array

```java
Arrays.stream(arr);
```

---

### 3. Using Stream.of()

```java
Stream.of(1,2,3);
```

---

### 4. Infinite Streams

```java
Stream.generate(() -> Math.random());
Stream.iterate(1, x -> x + 1);
```

---

# 🔹 Intermediate Operations

👉 These are **lazy** and return a new stream.

---

### Common Operations:

#### 🔸 filter

```java
.filter(n -> n % 2 == 0)
```

#### 🔸 map

```java
.map(n -> n * 2)
```

#### 🔸 sorted

```java
.sorted()
```

#### 🔸 distinct

```java
.distinct()
```

---

### Example:

```java
list.stream()
    .filter(n -> n > 2)
    .map(n -> n * 2);
```

❗ Nothing executes yet (lazy)

---

# 🔹 Terminal Operations

👉 These trigger execution.

---

### Common:

#### 🔸 forEach

```java
.forEach(System.out::println);
```

#### 🔸 collect

```java
.collect(Collectors.toList());
```

#### 🔸 count

```java
.count();
```

#### 🔸 reduce

```java
.reduce(0, (a, b) -> a + b);
```

---

# 🔹 Lazy Evaluation

👉 Intermediate operations are NOT executed immediately.

---

### Example:

```java
list.stream()
    .filter(n -> {
        System.out.println("Filtering: " + n);
        return n > 2;
    });
```

👉 No output until terminal operation is added.

---

# 🔹 Short-Circuiting

👉 Stops processing early

---

### Example:

```java
list.stream()
    .filter(n -> n > 2)
    .findFirst();
```

👉 Stops after first match

---

# 🔹 Stream Pipeline

👉 Chain of operations

---

### Example:

```java
list.stream()
    .filter(n -> n % 2 == 0)
    .map(n -> n * n)
    .sorted()
    .forEach(System.out::println);
```

---

# 🔹 Parallel Streams

👉 Enables multi-threading

---

### Example:

```java
list.parallelStream()
    .forEach(System.out::println);
```

---

### ⚠️ Use Carefully:

* Not always faster
* Avoid shared mutable state

---

# 🔹 Collectors

👉 Used with `collect()`

---

### Common Collectors:

#### toList

```java
.collect(Collectors.toList());
```

#### groupingBy

```java
.collect(Collectors.groupingBy(n -> n % 2));
```

#### joining

```java
.collect(Collectors.joining(", "));
```

---

### Example:

```java
Map<Integer, List<Integer>> map =
list.stream()
    .collect(Collectors.groupingBy(n -> n % 2));
```

---

# 🔹 Reduction Operations

👉 Combine elements into single result

---

### Example:

```java
int sum = list.stream()
              .reduce(0, (a, b) -> a + b);
```

---

### Using BinaryOperator:

```java
BinaryOperator<Integer> add = (a, b) -> a + b;
list.stream().reduce(add);
```

---

# 🔹 FlatMap vs Map

---

### map:

```java
List<String> sentences = Arrays.asList("Hello world", "Java is fun");

// 1. Using map: Result is a Stream of Arrays (Stream<String[]>)
// You end up with [[Hello, world], [Java, is, fun]]
System.out.println("Using map:");
sentences.stream()
    .map(sentence -> sentence.split(" "))
        .forEach(arr -> System.out.println(Arrays.toString(arr)));
```

👉 Produces: Stream<Stream>

---

### flatMap:

```java
// 2. Using flatMap: Result is a Stream of Strings (Stream<String>)
// The arrays are flattened into one sequence: [Hello, world, Java, is, fun]
System.out.println("\nUsing flatMap:");
sentences.stream()
    .flatMap(sentence -> Stream.of(sentence.split(" ")))
        .forEach(System.out::println);
```

👉 Produces: Stream<Integer>

| Feature              | map                                                                                | flatMap                                                                                                 |
|----------------------|------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------|
| Defination           | The `map` operation takes a function and applies it to every element in the stream | The `flatMap` operation is used when each element in your stream contains another collection or stream. |
| Function Return Type | "Returns a Value (e.g., String, Integer)"                                          | "Returns a Stream (e.g., Stream<String>)"                                                               |
| Output Cardinality   | 1-to-1                                                                             | 1-to-Many (or 0-to-Many)                                                                                |
| Structure            | Maintains the original structure.                                                  | """Flattens"" the structure (removes nesting)."                                                         |
| Use Case             | "Data conversion (e.g.User to DTO)."                                               | Handling nested collections or Optionals.                                                               |

---

# 🔹 Stateful vs Stateless Operations

---

### Stateless:

* `map`, `filter`
* No dependency on previous elements

---

### Stateful:

* `sorted`, `distinct`
* Need full data

---

# 🔹 Internal Working (Important)

---

### Key Concepts:

👉 Uses **Spliterator**

👉 Supports **lazy evaluation**

👉 Uses **functional interfaces internally**

---

### Flow:

```text
Data Source
   ↓
Pipeline of operations
   ↓
Terminal operation triggers execution
```

---

# 🔹 Common Pitfalls

---

### ❌ Reusing Stream

```java
Stream s = list.stream();
s.forEach(...);
s.forEach(...); // ERROR
```

---

### ❌ Modifying source during stream

```java
list.stream().forEach(list::remove); // Exception
```

---

### ❌ Using parallel incorrectly

* Leads to race conditions

---

# 🎯 Final Interview Summary

> Stream API provides a functional approach to process collections using a pipeline of operations with lazy evaluation, enabling clean, concise, and parallelizable code.

---

---

# 🚀 Java Stream API — Complete Methods & Use Cases

---

# 🔹 1. Stream Creation Methods

---

## `stream()`

👉 Converts a collection into a sequential stream.

👉 Entry point for most stream operations.

```java
List<Integer> list = Arrays.asList(1,2,3);
list.stream().forEach(System.out::println);
```

---

## `parallelStream()`

👉 Creates a parallel stream using multiple threads.

👉 Useful for CPU-intensive operations on large datasets.

```java
list.parallelStream().forEach(System.out::println);
```

---

## `Stream.of()`

👉 Creates a stream from given values.

👉 Handy for small datasets or test data.

```java
Stream.of(1,2,3).forEach(System.out::println);
```

---

## `Arrays.stream()`

👉 Converts array into stream.

👉 Works for both object and primitive arrays.

```java
int[] arr = {1,2,3};
Arrays.stream(arr).forEach(System.out::println);
```

---

## `Stream.generate()`

👉 Generates infinite stream using Supplier.

👉 Use with `limit()` to avoid infinite loop.

```java
Stream.generate(() -> "Hi")
      .limit(3)
      .forEach(System.out::println);
```

---

## `Stream.iterate()`

👉 Produces infinite sequence based on seed + function.

👉 Useful for sequences like numbers.

```java
Stream.iterate(1, n -> n + 1)
      .limit(5)
      .forEach(System.out::println);
```

---

## `Stream.empty()`

👉 Creates an empty stream.

👉 Useful for null-safe stream handling.

```java
Stream.empty().forEach(System.out::println);
```

---

# 🔹 2. Intermediate Operations

👉 Lazy, return new stream

---

## `filter()`

👉 Filters elements based on condition.

👉 Used for selection logic.

```java
list.stream()
    .filter(n -> n % 2 == 0)
    .forEach(System.out::println);
```

---

## `map()`

👉 Transforms each element.

👉 Used for data conversion.

```java
list.stream()
    .map(n -> n * 2)
    .forEach(System.out::println);
```

---

## `flatMap()`

👉 Flattens nested structures.

👉 Converts Stream<Stream<T>> → Stream<T>

```java
listOfLists.stream()
           .flatMap(List::stream)
           .forEach(System.out::println);
```

---

## `distinct()`

👉 Removes duplicates.

👉 Uses equals() internally.

```java
list.stream().distinct().forEach(System.out::println);
```

---

## `sorted()`

👉 Sorts elements (natural or custom).

👉 Stateful operation.

```java
list.stream().sorted().forEach(System.out::println);
```

---

## `peek()`

👉 Used for debugging/logging.

👉 Does not modify stream.

```java
list.stream()
    .peek(System.out::println)
    .count();
```

---

## `limit()`

👉 Limits number of elements.

👉 Important for infinite streams.

```java
list.stream().limit(2).forEach(System.out::println);
```

---

## `skip()`

👉 Skips first N elements.

👉 Useful for pagination.

```java
list.stream().skip(2).forEach(System.out::println);
```

---

## `mapToInt()`, `mapToLong()`, `mapToDouble()`

👉 Convert object stream to primitive stream.

👉 Improves performance.

```java
list.stream()
    .mapToInt(Integer::intValue)
    .sum();
```

---

## `boxed()`

👉 Converts primitive stream → object stream.

👉 Needed when collecting to collections.

```java
IntStream.of(1,2,3)
         .boxed()
         .collect(Collectors.toList());
```

---

# 🔹 3. Terminal Operations

👉 Trigger execution

---

## `forEach()`

👉 Performs action on each element.

👉 No ordering guarantee in parallel streams.

```java
list.stream().forEach(System.out::println);
```

---

## `forEachOrdered()`

👉 Maintains encounter order.

👉 Important for parallel streams.

```java
list.parallelStream().forEachOrdered(System.out::println);
```

---

## `collect()`

👉 Converts stream into collection or result. Performs a mutable reduction operation on the elements of this stream using a Collector.

👉 Most powerful method.

```java
list.stream()
    .collect(Collectors.toList());
```

---

## `toArray()`

👉 Converts stream to array.

```java
Object[] arr = list.stream().toArray();
```

---

## `reduce()`

👉 Combines elements into single result.

👉 Used for aggregation.

```java
int sum = list.stream()
              .reduce(0, Integer::sum);
```

---

## `count()`

👉 Returns number of elements.

```java
long count = list.stream().count();
```

---

## `min()` / `max()`

👉 Finds smallest/largest element.

```java
list.stream().max(Integer::compareTo);
```

---

## `findFirst()`

👉 Returns first element.

👉 Deterministic.

```java
list.stream().findFirst();
```

---

## `findAny()`

👉 Returns any element.

👉 Faster in parallel streams.

```java
list.parallelStream().findAny();
```

---

## `anyMatch()`

👉 Returns true if any element matches condition.

```java
list.stream().anyMatch(n -> n > 5);
```

---

## `allMatch()`

👉 Checks if all elements match condition. Returns whether all elements of this stream match the provided predicate.

```java
list.stream().allMatch(n -> n > 0);
```

---

## `noneMatch()`

👉 Checks if no elements match condition.

```java
list.stream().noneMatch(n -> n < 0);
```

---

# 🔹 4. Primitive Stream Specific Methods

---

## `sum()`

👉 Returns total sum.

👉 Faster than reduce.

```java
IntStream.of(1,2,3).sum();
```

---

## `average()`

👉 Returns average as OptionalDouble.

```java
IntStream.of(1,2,3).average();
```

---

## `summaryStatistics()`

👉 Returns min, max, sum, avg in one object.

```java
IntStream.of(1,2,3)
         .summaryStatistics();
```

---

# 🔹 5. Static Utility Methods

---

## `Stream.concat()`

👉 Combines two streams.

```java
Stream.concat(s1, s2)
      .forEach(System.out::println);
```

---

# 🔹 6. Newer Java Methods (Important)

---

## `takeWhile()` (Java 9)

👉 Takes elements while condition is true.

```java
list.stream()
    .takeWhile(n -> n < 4)
    .forEach(System.out::println);
```

---

## `dropWhile()` (Java 9)

👉 Skips while condition is true.

```java
list.stream()
    .dropWhile(n -> n < 3)
    .forEach(System.out::println);
```

---

## `iterate(seed, predicate, function)` (Java 9)

👉 Controlled iteration (finite).

```java
Stream.iterate(1, n -> n < 5, n -> n + 1)
      .forEach(System.out::println);
```

---

# 🔹 7. Closing Method

---

## `close()`

👉 Closes stream (rarely used manually).

👉 Useful with IO streams.

```java
stream.close();
```

---

# Other methods

---

## `builder()`

👉 When builder is used, the desired type should be additionally specified in the right part of the statement, otherwise the build() method will create an instance of the Stream<Object>;

```java
Stream<String> streamBuilder = Stream.<String>builder().add("a").add("b").add("c").build();
```

---

# 🎯 Interview Summary (Power Answer)

> Stream API provides a declarative way to process data using a pipeline of operations including intermediate and terminal methods, supporting lazy evaluation, parallelism, and functional-style programming.

---

# 🧠 Pro Tips (Must Say in Interview)

* “Intermediate operations are lazy”
* “Terminal operation triggers execution”
* “Primitive streams avoid boxing overhead”
* “Streams are single-use”

---

---

# 🚀 Tricky Stream Interview Questions (With Hidden Traps)

---

## ❓ Q1. What is the output?

```java
List<Integer> list = Arrays.asList(1,2,3,4);

list.stream()
    .filter(n -> {
        System.out.println("Filter: " + n);
        return n > 2;
    });
```

<details>
<summary>Answer</summary>

👉 **No output**

💡 Trap:

* No **terminal operation**
* Stream is **lazy**, so nothing executes

</details>

---

## ❓ Q2. What happens here?

```java
Stream<Integer> s = Stream.of(1,2,3);

s.forEach(System.out::println);
s.forEach(System.out::println);
```

<details>
<summary>Answer</summary>

❌ **Exception at runtime**

```text
1
2
3
IllegalStateException: stream has already been operated upon or closed
```

💡 Trap:

* Streams are **single-use only**

</details>

---

## ❓ Q3. Output?

```java
List<String> list = Arrays.asList("a", "b", "c");

list.stream()
    .map(s -> s.toUpperCase());
```

<details>
<summary>Answer</summary>

👉 **No output**

💡 Trap:

* No terminal operation
* `map()` is lazy

</details>

---

## ❓ Q4. What is the output?

```java
List<Integer> list = Arrays.asList(1,2,3,4);

list.stream()
    .filter(n -> n > 2)
    .map(n -> n * 2)
    .findFirst()
    .ifPresent(System.out::println);
```

<details>
<summary>Answer</summary>

👉 **Output: 6**

💡 Flow:

* 1 → skipped
* 2 → skipped
* 3 → pass → 3*2 = 6 → stop

👉 **Short-circuiting**

</details>

---

## ❓ Q5. What is the output?

```java
List<Integer> list = Arrays.asList(1,2,3,4);

list.stream()
    .filter(n -> {
        System.out.println("Filter: " + n);
        return n > 2;
    })
    .findFirst();
```

<details>
<summary>Answer</summary>

👉 Output:

```text
Filter: 1
Filter: 2
Filter: 3
```

💡 Trap:

* Stops after finding first match (3)
* Lazy + short-circuit

</details>

---

## ❓ Q6. What is the output?

```java
List<Integer> list = Arrays.asList(1,2,3);

list.stream()
    .map(n -> n * 2)
    .peek(System.out::println)
    .count();
```

<details>
<summary>Answer</summary>

👉 Output:

```text
2
4
6
```

💡 Trap:

* `peek()` runs only when terminal op exists
* Here triggered by `count()`

</details>

---

## ❓ Q7. What happens?

```java
List<Integer> list = Arrays.asList(1,2,3,4);

list.stream()
    .forEach(n -> {
        if(n == 3) list.remove(n);
    });
```

<details>
<summary>Answer</summary>

❌ **UnsupportedOperationException**

💡 Trap:

* Cannot modify source during stream processing

</details>

---

## ❓ Q8. Output?

```java
List<Integer> list = Arrays.asList(1,2,3,4);

list.parallelStream()
    .forEach(System.out::print);
```

<details>
<summary>Answer</summary>

👉 **Output is unpredictable**

Example:

```text
2314
```

💡 Trap:

* Parallel stream → **no ordering guarantee**

</details>

---

## ❓ Q9. Fix the issue

```java
list.parallelStream()
    .forEachOrdered(System.out::print);
```

<details>
<summary>Answer</summary>

👉 Output will be ordered:

```text
1234
```

💡 `forEachOrdered()` preserves order

</details>

---

## ❓ Q10. What is the output?

```java
List<String> list = Arrays.asList("a", "bb", "ccc");

list.stream()
    .map(String::length)
    .reduce(0, Integer::sum);
```

<details>
<summary>Answer</summary>

👉 Output: **6**

💡 1 + 2 + 3 = 6

</details>

---

## ❓ Q11. map vs flatMap trap

```java
List<List<Integer>> list = Arrays.asList(
    Arrays.asList(1,2),
    Arrays.asList(3,4)
);

list.stream()
    .map(l -> l.stream())
    .forEach(System.out::println);
```

<details>
<summary>Answer</summary>

👉 Output:

```text
java.util.stream.ReferencePipeline$Head@...
java.util.stream.ReferencePipeline$Head@...
```

💡 Trap:

* Produces Stream<Stream>
* Need `flatMap`

</details>

---

## ❓ Q12. Correct version?

<details>
<summary>Answer</summary>

```java
list.stream()
    .flatMap(l -> l.stream())
    .forEach(System.out::println);
```

👉 Output:

```text
1
2
3
4
```

</details>

---

## ❓ Q13. What is the output?

```java
Stream.iterate(1, n -> n + 1)
      .limit(5)
      .forEach(System.out::print);
```

<details>
<summary>Answer</summary>

👉 Output:

```text
12345
```

</details>

---

## ❓ Q14. Infinite stream trap

```java
Stream.iterate(1, n -> n + 1)
      .forEach(System.out::println);
```

<details>
<summary>Answer</summary>

❌ Runs infinitely

💡 Missing `limit()`

</details>

---

## ❓ Q15. What is the output?

```java
List<Integer> list = Arrays.asList(1,2,3,4);

int sum = list.stream()
              .reduce((a, b) -> a + b)
              .get();

System.out.println(sum);
```

<details>
<summary>Answer</summary>

👉 Output: **10**

💡 But risky:

* `Optional.get()` may throw exception if empty

</details>

---

## ❓ Q16. Safer version?

<details>
<summary>Answer</summary>

```java
int sum = list.stream()
              .reduce(0, Integer::sum);
```

</details>

---

## ❓ Q17. What is the output?

```java
List<Integer> list = Arrays.asList(1,2,3);

list.stream()
    .sorted((a, b) -> b - a)
    .forEach(System.out::print);
```

<details>
<summary>Answer</summary>

👉 Output:

```text
321
```

</details>

---

## ❓ Q18. Stateful operation trap

```java
List<Integer> list = Arrays.asList(1,2,3,4);

List<Integer> result = new ArrayList<>();

list.stream()
    .forEach(n -> result.add(n * 2));
```

<details>
<summary>Answer</summary>

👉 Works (sequential)

⚠️ But dangerous in parallel:

* Not thread-safe

</details>

---

## ❓ Q19. What is the output?

```java
List<Integer> list = Arrays.asList(1,2,3,4);

list.stream()
    .peek(System.out::print);
```

<details>
<summary>Answer</summary>

👉 **No output**

💡 `peek()` needs terminal operation

</details>

---

## ❓ Q20. What is the output?

```java
List<Integer> list = Arrays.asList(1,2,3,4);

list.stream()
    .filter(n -> n > 2)
    .count();
```

<details>
<summary>Answer</summary>

👉 Output: **2**

</details>

---

# 🎯 What These Questions Test

* Lazy evaluation
* Short-circuiting
* Stream lifecycle
* Parallel behavior
* Internal pipeline understanding
* Common pitfalls

---

# 🧠 Pro Interview Tip

When answering:

> Always mention **WHY**, not just output

Example:

> “This happens due to lazy evaluation and absence of terminal operation”

---

---

# 🟢 EASY LEVEL (New + Important Patterns)

---

## ❓ Q1. Skip first N elements

👉 Skip first 3 elements and print rest

```java id="2k3f8p"
List<Integer> list = Arrays.asList(1,2,3,4,5,6);
```

<details>
<summary>Answer</summary>

```java id="b7t3ux"
list.stream()
    .skip(3)
    .forEach(System.out::println);
```

</details>

---

## ❓ Q2. Limit elements

👉 Print only first 2 elements

<details>
<summary>Answer</summary>

```java id="8h1k9r"
list.stream()
    .limit(2)
    .forEach(System.out::println);
```

</details>

---

## ❓ Q3. Find any element > 4

👉 (Important for parallel streams)

<details>
<summary>Answer</summary>

```java id="q2pf3l"
list.stream()
    .filter(n -> n > 4)
    .findAny()
    .ifPresent(System.out::println);
```

</details>

---

## ❓ Q4. Check if list contains number 5

<details>
<summary>Answer</summary>

```java id="q7h3tv"
boolean exists =
    list.stream()
        .anyMatch(n -> n == 5);
```

</details>

---

## ❓ Q5. Convert List<Integer> → int[]

<details>
<summary>Answer</summary>

```java id="x5u9ce"
int[] arr =
    list.stream()
        .mapToInt(Integer::intValue)
        .toArray();
```

</details>

---

## ❓ Q6. Print elements with index (tricky)

👉 Output:

```
0 -> 1
1 -> 2
...
```

<details>
<summary>Answer</summary>

```java id="1u8s4r"
IntStream.range(0, list.size())
         .forEach(i -> 
             System.out.println(i + " -> " + list.get(i))
         );
```

</details>

---

# 🟡 MEDIUM LEVEL (New Patterns)

---

## ❓ Q7. Find all elements starting with "A" (case-insensitive)

```java id="v2t8gn"
List<String> list = Arrays.asList("apple","Banana","Avocado","grape");
```

<details>
<summary>Answer</summary>

```java id="r8z1pw"
list.stream()
    .filter(s -> s.toLowerCase().startsWith("a"))
    .forEach(System.out::println);
```

</details>

---

## ❓ Q8. Reverse sort using Comparator

👉 Without using `reverseOrder()`

<details>
<summary>Answer</summary>

Integer List :

```java id="u4m1zy"
list.stream()
    .sorted((a, b) -> b - a)
    .forEach(System.out::println);
```

String List:

```java
list.stream()
    .sorted((a, b) -> b.compareTo(a))
        .forEach(System.out::println);
```

</details>

---

## ❓ Q9. Find sum of squares of even numbers

<details>
<summary>Answer</summary>

```java id="z7f4yo"
int sum =
    list.stream()
        .filter(n -> n % 2 == 0)
        .map(n -> n * n)
        .mapToInt(Integer::intValue)
        .sum();
```

</details>

---

## ❓ Q10. Find elements that appear only once

👉 Input:

```java id="p1k7ts"
List<Integer> list = Arrays.asList(1,2,2,3,4,4,5);
```

👉 Output:

```
[1,3,5]
```

---

<details>
<summary>Answer</summary>

```java
list2.stream()
    .filter(i -> Collections.frequency(list2, i) == 1)
    .forEach(System.out::println);
```

```java id="t4j6pl"
Map<Integer, Long> freq =
    list.stream()
        .collect(Collectors.groupingBy(
            Function.identity(),
            Collectors.counting()
        ));

List<Integer> result =
    freq.entrySet().stream()
        .filter(e -> e.getValue() == 1)
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
```

</details>

---

## ❓ Q11. Find common elements between two lists

---

<details>
<summary>Answer</summary>

```java id="q3x9wd"
List<Integer> result =
    list1.stream()
         .filter(list2::contains)
         .collect(Collectors.toList());
```

👉 Optimization tip:
Use Set for better performance

</details>

---

## ❓ Q12. Find frequency of each character

```java id="q5z2wx"
String str = "hello";
```

---

<details>
<summary>Answer</summary>

```java id="y8m3sd"
Map<Character, Long> result =
    str.chars()
       .mapToObj(c -> (char)c)
       .collect(Collectors.groupingBy(
           Function.identity(),
           Collectors.counting()
       ));
```

</details>

---

## ❓ Q13. Convert List<String> → Map<String, Integer> (length)

---

<details>
<summary>Answer</summary>

```java id="w9k4df"
Map<String, Integer> map =
    list.stream()
        .collect(Collectors.toMap(
            s -> s,
            String::length
        ));
```

</details>

---

## ❓ Q14. Find last element in stream (tricky)

---

<details>
<summary>Answer</summary>

```java id="u6j8er"
Optional<Integer> last =
    list.stream()
        .reduce((a, b) -> b);
```

👉 No direct method → use reduce trick

</details>

---

## ❓ Q15. Check if list is sorted

---

<details>
<summary>Answer</summary>

```java id="b5q2mn"
boolean sorted =
    IntStream.range(0, list.size() - 1)
             .allMatch(i -> list.get(i) <= list.get(i + 1));
```

</details>

---

# 🎯 What These Add

These cover patterns you hadn’t seen yet:

* `skip`, `limit`
* `findAny`, `anyMatch`
* Index-based stream
* Frequency problems
* Cross-list comparison
* Reduce tricks
* Sorting validation

---

# 🧠 Interview Insight

If interviewer asks something like:

> “Find last element”

Most candidates fail.

👉 If you say:

> “Stream doesn’t provide direct method, I’ll use reduce”

🔥 That’s a strong signal.

---

---

# 🚀 Advanced Stream Problems (Interview Level)

---

# 🔴 PROBLEM 1: Top K Frequent Words

👉 Input:

```java
List<String> words = Arrays.asList("apple","banana","apple","orange","banana","apple");
```

👉 Output:
Top 2 frequent → `["apple", "banana"]`

---

<details>
<summary>Answer</summary>

```java
Map<String, Long> freq =
    words.stream()
         .collect(Collectors.groupingBy(
             Function.identity(),
             Collectors.counting()
         ));

List<String> result =
    freq.entrySet().stream()
        .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
        .limit(2)
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
```

</details>

---

# 🔴 PROBLEM 2: Find Employees with Highest Salary per Department

👉 Output:

```text
IT → John
HR → Alice
```

---

<details>
<summary>Answer</summary>

```java
Map<String, Optional<Employee>> result =
    employees.stream()
        .collect(Collectors.groupingBy(
            Employee::getDept,
            Collectors.maxBy(Comparator.comparing(Employee::getSalary))
        ));
```

👉 If interviewer pushes:

```java
Map<String, Employee> finalResult =
    result.entrySet().stream()
          .collect(Collectors.toMap(
              Map.Entry::getKey,
              e -> e.getValue().orElse(null)
          ));
```

</details>

---

# 🔴 PROBLEM 3: Find First Non-Repeating Character (Optimized)

---

<details>
<summary>Answer</summary>

```java
String str = "swiss";

Character result =
    str.chars()
       .mapToObj(c -> (char)c)
       .collect(Collectors.groupingBy(
           Function.identity(),
           LinkedHashMap::new,
           Collectors.counting()
       ))
       .entrySet().stream()
       .filter(e -> e.getValue() == 1)
       .map(Map.Entry::getKey)
       .findFirst()
       .orElse(null);
```

</details>

---

# 🔴 PROBLEM 4: Merge Two Lists Without Duplicates

---

<details>
<summary>Answer</summary>

```java
List<Integer> merged =
    Stream.concat(list1.stream(), list2.stream())
          .distinct()
          .collect(Collectors.toList());
```

</details>

---

# 🔴 PROBLEM 5: Find Longest Consecutive Sequence Length

👉 Example:

```text
[100,4,200,1,3,2] → 4 (sequence: 1,2,3,4)
```

---

<details>
<summary>Answer</summary>

```java
Set<Integer> set = list.stream().collect(Collectors.toSet());

int longest = set.stream()
    .filter(n -> !set.contains(n - 1)) // start of sequence
    .mapToInt(start -> {
        int count = 1;
        while(set.contains(start + count)) count++;
        return count;
    })
    .max()
    .orElse(0);
```

</details>

---

# 🔴 PROBLEM 6: Group Strings by Length and Sort Each Group

---

<details>
<summary>Answer</summary>

```java
Map<Integer, List<String>> result =
    list.stream()
        .collect(Collectors.groupingBy(
            String::length,
            Collectors.collectingAndThen(
                Collectors.toList(),
                l -> l.stream()
                      .sorted()
                      .collect(Collectors.toList())
            )
        ));
```

</details>

---

# 🔴 PROBLEM 7: Custom Collector (Sum of Squares)

---

<details>
<summary>Answer</summary>

```java
int result =
    list.stream()
        .collect(Collectors.reducing(
            0,
            n -> n * n,
            Integer::sum
        ));
```

</details>

---

# 🔴 PROBLEM 8: Find Duplicate Words with Count

---

<details>
<summary>Answer</summary>

```java
Map<String, Long> duplicates =
    words.stream()
         .collect(Collectors.groupingBy(
             Function.identity(),
             Collectors.counting()
         ))
         .entrySet().stream()
         .filter(e -> e.getValue() > 1)
         .collect(Collectors.toMap(
             Map.Entry::getKey,
             Map.Entry::getValue
         ));
```

</details>

---

# 🔴 PROBLEM 9: Flatten and Sort Nested List

---

<details>
<summary>Answer</summary>

```java
List<Integer> result =
    list.stream()
        .flatMap(List::stream)
        .sorted()
        .collect(Collectors.toList());
```

</details>

---

# 🔴 PROBLEM 10: Find Second Highest Salary

---

<details>
<summary>Answer</summary>

```java
int second =
    employees.stream()
        .map(Employee::getSalary)
        .distinct()
        .sorted(Comparator.reverseOrder())
        .skip(1)
        .findFirst()
        .orElseThrow();
```

</details>

---

# 🔴 PROBLEM 11: Check if List Contains Only Unique Elements

---

<details>
<summary>Answer</summary>

```java
boolean isUnique =
    list.size() ==
    list.stream().distinct().count();
```

</details>

---

# 🔴 PROBLEM 12: Sliding Window Sum (Advanced)

👉 Window size = 3

---

<details>
<summary>Answer</summary>

```java
List<Integer> result =
    IntStream.range(0, list.size() - 2)
        .map(i -> list.get(i) + list.get(i+1) + list.get(i+2))
        .boxed()
        .collect(Collectors.toList());
```

</details>

---

# 🔴 PROBLEM 13: Convert List to Map with Duplicate Handling

---

<details>
<summary>Answer</summary>

```java
Map<String, Integer> map =
    list.stream()
        .collect(Collectors.toMap(
            Function.identity(),
            String::length,
            (oldVal, newVal) -> oldVal
        ));
```

</details>

---

# 🔴 PROBLEM 14: Partition Employees by Salary

---

<details>
<summary>Answer</summary>

```java
Map<Boolean, List<Employee>> result =
    employees.stream()
        .collect(Collectors.partitioningBy(
            e -> e.getSalary() > 50000
        ));
```

</details>

---

# 🔴 PROBLEM 15: Detect Parallel Stream Bug

👉 What's wrong?

```java
List<Integer> result = new ArrayList<>();

list.parallelStream()
    .forEach(n -> result.add(n));
```

---

<details>
<summary>Answer</summary>

❌ Not thread-safe

✔ Fix:

```java
List<Integer> result =
    list.parallelStream()
        .collect(Collectors.toList());
```

</details>

---

# 🎯 What These Problems Test

* Advanced collectors (`groupingBy`, `partitioningBy`)
* Stream transformations (`map`, `flatMap`)
* Sorting + limiting
* Handling duplicates
* Performance awareness
* Parallel stream pitfalls

---

# 🧠 Strong Interview Tip

While solving, say things like:

> “I’ll first group the data, then sort based on frequency, and finally limit results”

👉 This shows **structured thinking**

---
