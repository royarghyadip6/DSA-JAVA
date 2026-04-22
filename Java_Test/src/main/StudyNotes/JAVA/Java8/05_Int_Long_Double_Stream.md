# 🚀 Primitive Streams Deep Dive

1. [x] [IntStream](https://docs.oracle.com/javase/8/docs/api/java/util/stream/IntStream.html)
2. [x] [LongStream](https://docs.oracle.com/javase/8/docs/api/java/util/stream/LongStream.html)
3. [x] [DoubleStream](https://docs.oracle.com/javase/8/docs/api/java/util/stream/DoubleStream.html)

---

# 📌 1. What are Primitive Streams?

> Primitive Streams are specialized versions of Stream designed for **primitive data types** (`int`, `long`, `double`) to avoid **boxing/unboxing overhead**.

---

### 💡 Why they exist?

Normal Stream:

```java
Stream<Integer>
```

👉 Causes:

* **Boxing** (int → Integer)
* **Unboxing** (Integer → int)
* Extra memory + CPU cost

---

### ✅ Primitive Streams:

* `IntStream`
* `LongStream`
* `DoubleStream`

👉 Work directly with primitives → **faster & efficient**

---

# ⚡ 2. Key Differences vs Stream<T>

| Feature     | Stream<Integer> | IntStream    |
|-------------|-----------------|--------------|
| Type        | Object          | Primitive    |
| Boxing      | Required        | Not required |
| Performance | Slower          | Faster       |
| Memory      | More            | Less         |

---

# 🔹 3. IntStream Deep Dive

---

## ✅ Creating IntStream

---

### 1. From Array

```java
int[] arr = {1,2,3};
IntStream stream = Arrays.stream(arr);
```

---

### 2. Range

```java
IntStream.range(1, 5);     // 1,2,3,4
IntStream.rangeClosed(1,5); // 1,2,3,4,5
```

---

### 3. Generate

```java
IntStream.generate(() -> 5).limit(3);
```

---

### 4. Iterate

```java
IntStream.iterate(1, n -> n + 1).limit(5);
```

---

## ✅ Important Operations

---

### 🔸 filter

```java
IntStream.range(1,10)
         .filter(n -> n % 2 == 0)
         .forEach(System.out::println);
```

---

### 🔸 map

```java
IntStream.range(1,5)
         .map(n -> n * n)
         .forEach(System.out::println);
```

---

### 🔸 sum (SPECIAL METHOD)

```java
int sum = IntStream.range(1,5).sum();
```

👉 No need for `reduce()`

---

### 🔸 average

```java
OptionalDouble avg = IntStream.range(1,5).average();
```

---

### 🔸 max / min

```java
OptionalInt max = IntStream.of(1,2,3).max();
```

---

## 🔥 Special Feature: Summary Statistics

```java
IntSummaryStatistics stats =
    IntStream.of(1,2,3,4).summaryStatistics();

System.out.println(stats.getSum());
System.out.println(stats.getAverage());
System.out.println(stats.getMax());
```

---

# 🔹 4. LongStream

👉 Same as `IntStream` but for `long`

---

### Example:

```java
LongStream.range(1,5)
          .forEach(System.out::println);
```

---

### Use Case:

* Large numeric ranges
* Big data calculations

---

# 🔹 5. DoubleStream

👉 Used for floating-point numbers

---

### Example:

```java
DoubleStream.of(1.2, 2.3, 3.4)
            .forEach(System.out::println);
```

---

### Special Methods:

```java
double sum = DoubleStream.of(1.2, 2.3).sum();
OptionalDouble avg = DoubleStream.of(1.2, 2.3).average();
```

---

# 🔄 6. Converting Between Streams

---

## 🔹 Object → Primitive

```java
List<Integer> list = Arrays.asList(1,2,3);

IntStream stream = list.stream()
                       .mapToInt(Integer::intValue);
```

---

## 🔹 Primitive → Object

```java
IntStream.of(1,2,3)
         .boxed()
         .collect(Collectors.toList());
```

---

# 🔥 7. map vs mapToInt (Important Interview Point)

---

### ❌ Using map

```java
list.stream()
    .map(x -> x * 2); // returns Stream<Integer>
```

---

### ✅ Using mapToInt

```java
list.stream()
    .mapToInt(x -> x * 2); // returns IntStream
```

---

👉 **Why important?**

* Avoids boxing
* Better performance

---

# ⚡ 8. Performance Insight (VERY IMPORTANT)

---

### Without Primitive Stream:

```java
list.stream()
    .map(x -> x * 2)
    .reduce(0, Integer::sum);
```

---

### With IntStream:

```java
list.stream()
    .mapToInt(x -> x * 2)
    .sum();
```

---

👉 Second is:

* Faster
* Cleaner
* Interview-preferred

---

# 🔹 9. Terminal Operations Specific to Primitive Streams

* `sum()`
* `average()`
* `max()`
* `min()`
* `summaryStatistics()`

---

# 🔹 10. Optional Types (Tricky Area)

| Stream Type  | Optional Type  |
|--------------|----------------|
| IntStream    | OptionalInt    |
| LongStream   | OptionalLong   |
| DoubleStream | OptionalDouble |

---

### Example:

```java
OptionalInt max = IntStream.of(1,2,3).max();
max.ifPresent(System.out::println);
```

---

# 🔥 11. Parallel Primitive Streams

```java
IntStream.range(1, 1000)
         .parallel()
         .sum();
```

---

👉 Faster for large data (but depends on CPU)

---

# ⚠️ 12. Common Mistakes

---

### ❌ Forgetting `mapToInt`

* Leads to unnecessary boxing

---

### ❌ Using reduce instead of sum

```java
.reduce(0, (a,b) -> a+b);
```

👉 Use:

```java
.sum();
```

---

### ❌ Ignoring Optional

```java
max.getAsInt(); // risky
```

---

# 🧠 13. Internal Working (Important)

---

* Uses **primitive spliterators**
* Avoids object creation
* Optimized for CPU pipelines

---

# 🎯 14. Interview Summary (Best Answer)

> Primitive streams like IntStream, LongStream, and DoubleStream are specialized streams that operate directly on primitive types to avoid boxing overhead, providing better performance and additional utility methods like sum, average, and summary statistics.

---

# 🚀 15. When to Use What?

| Use Case       | Stream Type  |
|----------------|--------------|
| Numbers (int)  | IntStream    |
| Large numbers  | LongStream   |
| Decimal values | DoubleStream |
| Objects        | Stream<T>    |

---

# 🧠 Pro Interview Tip

Always say:

> “I prefer `mapToInt()` and `IntStream` to avoid boxing overhead and improve performance”

👉 This is a **strong signal answer**

---

---

# 🟢 EASY LEVEL (Foundation Building)

---

## ❓ Q1. Print all even numbers

```java
List<Integer> list = Arrays.asList(1,2,3,4,5,6);
```

<details>
<summary>Answer</summary>

```java
list.stream()
    .filter(n -> n % 2 == 0)
    .forEach(System.out::println);
```

</details>

---

## ❓ Q2. Square all numbers

<details>
<summary>Answer</summary>

```java
list.stream()
    .map(n -> n * n)
    .forEach(System.out::println);
```

</details>

---

## ❓ Q3. Count numbers greater than 3

<details>
<summary>Answer</summary>

```java
long count = list.stream()
                 .filter(n -> n > 3)
                 .count();

System.out.println(count);
```

</details>

---

## ❓ Q4. Find max element

<details>
<summary>Answer</summary>

```java
int max = list.stream()
              .max(Integer::compareTo)
              .orElseThrow();

System.out.println(max);
```

</details>

---

## ❓ Q5. Convert to uppercase

```java
List<String> list = Arrays.asList("java", "spring");
```

<details>
<summary>Answer</summary>

```java
list.stream()
    .map(String::toUpperCase)
    .forEach(System.out::println);
```

</details>

---

# 🟡 MEDIUM LEVEL (Interview Standard)

---

## ❓ Q6. Remove duplicates

<details>
<summary>Answer</summary>

```java
List<Integer> result =
    list.stream()
        .distinct()
        .collect(Collectors.toList());
```

</details>

---

## ❓ Q7. Sort list in descending order

<details>
<summary>Answer</summary>

```java
list.stream()
    .sorted(Comparator.reverseOrder())
    .forEach(System.out::println);
```

</details>

---

## ❓ Q8. Sum of all elements

<details>
<summary>Answer</summary>

```java
int sum = list.stream()
              .mapToInt(Integer::intValue)
              .sum();
```

</details>

---

## ❓ Q9. Find first element greater than 3

<details>
<summary>Answer</summary>

```java
list.stream()
    .filter(n -> n > 3)
    .findFirst()
    .ifPresent(System.out::println);
```

</details>

---

## ❓ Q10. Check if all numbers are even

<details>
<summary>Answer</summary>

```java
boolean result = list.stream()
                     .allMatch(n -> n % 2 == 0);
```

</details>

---

## ❓ Q11. Convert list to map

👉 Key = number, Value = square

<details>
<summary>Answer</summary>

```java
Map<Integer, Integer> map =
    list.stream()
        .collect(Collectors.toMap(
            n -> n,
            n -> n * n
        ));
```

</details>

---

## ❓ Q12. Group numbers by even/odd

<details>
<summary>Answer</summary>

```java
Map<Boolean, List<Integer>> result =
    list.stream()
        .collect(Collectors.partitioningBy(n -> n % 2 == 0));
```

</details>

---

## ❓ Q13. Join strings with comma

```java
List<String> list = Arrays.asList("A", "B", "C");
```

<details>
<summary>Answer</summary>

```java
String result =
    list.stream()
        .collect(Collectors.joining(","));

System.out.println(result); // A,B,C
```

</details>

---

## ❓ Q14. Find average

<details>
<summary>Answer</summary>

```java
double avg = list.stream()
                 .mapToInt(Integer::intValue)
                 .average()
                 .orElse(0);
```

</details>

---

## ❓ Q15. Filter + transform combo

👉 Multiply even numbers by 10

<details>
<summary>Answer</summary>

```java
list.stream()
    .filter(n -> n % 2 == 0)
    .map(n -> n * 10)
    .forEach(System.out::println);
```

</details>

---

# 🎯 What These Cover

* `filter`, `map`, `reduce`
* `distinct`, `sorted`
* `collect`, `joining`
* `partitioningBy`
* `mapToInt`, `average`

---

# 🧠 Interview Tip

For medium questions, always say:

> “I’ll first filter, then transform, then collect”

👉 This shows **pipeline thinking**

---

---

# 🚀 Advanced Stream Problems (Product-Level)

---

# 🔴 HARD LEVEL

---

## ❓ Q1. Top K Frequent Elements

👉 Given:

```java
List<Integer> list = Arrays.asList(1,1,1,2,2,3,3,3,3,4);
```

👉 Find **top 2 most frequent elements**

---

<details>
<summary>Answer</summary>

```java
Map<Integer, Long> freqMap =
    list.stream()
        .collect(Collectors.groupingBy(
            Function.identity(),
            Collectors.counting()
        ));

List<Integer> result =
    freqMap.entrySet().stream()
           .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
           .limit(2)
           .map(Map.Entry::getKey)
           .collect(Collectors.toList());

System.out.println(result); // [3, 1]
```

</details>

---

## ❓ Q2. Group Employees by Department & Count

👉 Given:

```java
class Employee {
    String name;
    String dept;
}
```

👉 Output:

```text
HR → 3
IT → 5
```

---

<details>
<summary>Answer</summary>

```java
Map<String, Long> result =
    employees.stream()
             .collect(Collectors.groupingBy(
                 e -> e.dept,
                 Collectors.counting()
             ));
```

</details>

---

## ❓ Q3. Find Duplicate Elements

👉 Input:

```java
List<Integer> list = Arrays.asList(1,2,3,2,4,1,5);
```

👉 Output:

```text
[1,2]
```

---

<details>
<summary>Answer</summary>

```java
Set<Integer> seen = new HashSet<>();

List<Integer> duplicates =
    list.stream()
        .filter(n -> !seen.add(n))
        .distinct()
        .collect(Collectors.toList());
```

</details>

---

## ❓ Q4. Longest String

👉 Input:

```java
List<String> list = Arrays.asList("java", "springboot", "microservices");
```

---

<details>
<summary>Answer</summary>

```java
String longest =
    list.stream()
        .max(Comparator.comparingInt(String::length))
        .orElse("");

System.out.println(longest);
```

</details>

---

## ❓ Q5. Partition Numbers (Even/Odd)

---

<details>
<summary>Answer</summary>

```java
Map<Boolean, List<Integer>> result =
    list.stream()
        .collect(Collectors.partitioningBy(n -> n % 2 == 0));
```

</details>

---

## ❓ Q6. Flatten Nested List

👉 Input:

```java
List<List<Integer>> list = Arrays.asList(
    Arrays.asList(1,2),
    Arrays.asList(3,4)
);
```

---

<details>
<summary>Answer</summary>

```java
List<Integer> flat =
    list.stream()
        .flatMap(List::stream)
        .collect(Collectors.toList());
```

</details>

---

## ❓ Q7. Find Second Highest Number

---

<details>
<summary>Answer</summary>

```java
int second =
    list.stream()
        .distinct()
        .sorted(Comparator.reverseOrder())
        .skip(1)
        .findFirst()
        .orElseThrow();
```

</details>

---

## ❓ Q8. Convert List to Map (Key Conflict Trap)

👉 Input:

```java
List<String> list = Arrays.asList("a", "b", "a");
```

---

<details>
<summary>Answer</summary>

```java
Map<String, Integer> map =
    list.stream()
        .collect(Collectors.toMap(
            Function.identity(),
            String::length,
            (oldVal, newVal) -> oldVal // resolve conflict
        ));
```

💡 Trap: duplicate keys

</details>

---

## ❓ Q9. Find First Non-Repeating Character

---

<details>
<summary>Answer</summary>

```java
String str = "swiss";

Character result =
    str.chars()
       .mapToObj(c -> (char) c)
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

## ❓ Q10. Custom Sorting (Multi-level)

👉 Sort employees by:

1. Salary DESC
2. Name ASC

---

<details>
<summary>Answer</summary>

```java
employees.stream()
         .sorted(
             Comparator.comparing(Employee::getSalary).reversed()
                       .thenComparing(Employee::getName)
         )
         .forEach(System.out::println);
```

</details>

---

# 🔥 VERY HARD LEVEL

---

## ❓ Q11. Sliding Window (Advanced)

👉 Find subarrays of size 3

---

<details>
<summary>Answer</summary>

```java
List<List<Integer>> result =
    IntStream.range(0, list.size() - 2)
             .mapToObj(i -> list.subList(i, i + 3))
             .collect(Collectors.toList());
```

</details>

---

## ❓ Q12. Find Anagrams Grouping

---

<details>
<summary>Answer</summary>

```java
Map<String, List<String>> result =
    list.stream()
        .collect(Collectors.groupingBy(str -> {
            char[] chars = str.toCharArray();
            Arrays.sort(chars);
            return new String(chars);
        }));
```

</details>

---

## ❓ Q13. Running Sum (Prefix Sum)

---

<details>
<summary>Answer</summary>

```java
AtomicInteger sum = new AtomicInteger(0);

List<Integer> result =
    list.stream()
        .map(sum::addAndGet)
        .collect(Collectors.toList());
```

</details>

---

## ❓ Q14. Custom Collector (Advanced)

👉 Create collector to join strings with prefix/suffix

---

<details>
<summary>Answer</summary>

```java
String result =
    list.stream()
        .collect(Collectors.collectingAndThen(
            Collectors.joining(","),
            s -> "[" + s + "]"
        ));
```

</details>

---

## ❓ Q15. Parallel Stream Bug Detection

👉 What’s wrong?

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

# 🎯 What Product Companies Check

* Can you think in pipelines?
* Do you avoid mutable shared state?
* Do you handle edge cases?
* Do you choose correct collector?

---

# 🧠 Pro Interview Tip

While solving, say:

> “I’ll use groupingBy for aggregation, then sort based on values”

👉 This shows **design thinking**, not just coding

---
