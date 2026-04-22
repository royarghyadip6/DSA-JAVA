# 🚀 Collectors Deep Dive (Interview Mastery)

---

# 📌 1. What is a Collector?

> A **Collector** is a utility that transforms a stream into a final result (List, Map, String, etc.)

---

### Signature:

```java
<R, A> R collect(Collector<T, A, R> collector)
```

👉 Don’t ignore this:

* `T` → input type
* `A` → intermediate container
* `R` → final result

---

# 📌 2. Internal Working (VERY IMPORTANT)

A Collector works in **4 steps**:

```text
Supplier → Accumulator → Combiner → Finisher
```

---

### Example: `Collectors.toList()`

* Supplier → create new List
* Accumulator → add elements
* Combiner → merge lists (parallel)
* Finisher → return list

---

💡 Interview line:

> “Collector internally uses supplier, accumulator, combiner, and finisher”

---

# 📌 3. Most Important Collectors (Must Know)

---

## 🔹 `toList()`

👉 Collects elements into List

```java
list.stream()
    .collect(Collectors.toList());
```

---

## 🔹 `toSet()`

👉 Removes duplicates

```java
list.stream()
    .collect(Collectors.toSet());
```

---

## 🔹 `toMap()`

👉 Converts stream into Map

---

### ⚠️ Duplicate key trap:

```java
list.stream()
    .collect(Collectors.toMap(
        Function.identity(),
        String::length,
        (oldVal, newVal) -> oldVal
    ));
```

💡 Always handle merge function

---

# 📌 4. groupingBy (VERY VERY IMPORTANT)

---

## 🔹 Basic grouping

```java
Map<Integer, List<Integer>> map =
    list.stream()
        .collect(Collectors.groupingBy(n -> n % 2));
```

---

## 🔹 With downstream collector

```java
Map<String, Long> result =
    employees.stream()
        .collect(Collectors.groupingBy(
            Employee::getDept,
            Collectors.counting()
        ));
```

---

💡 Interview gold line:

> “groupingBy is like SQL GROUP BY”

---

## 🔹 Advanced: Multiple grouping

```java
Map<String, Map<String, List<Employee>>> result =
    employees.stream()
        .collect(Collectors.groupingBy(
            Employee::getDept,
            Collectors.groupingBy(Employee::getRole)
        ));
```

---

# 📌 5. partitioningBy

---

👉 Special case of groupingBy (boolean key)

```java
Map<Boolean, List<Integer>> result =
    list.stream()
        .collect(Collectors.partitioningBy(n -> n % 2 == 0));
```

---

💡 Only 2 groups: true/false

---

# 📌 6. counting()

---

👉 Counts elements

```java
long count =
    list.stream()
        .collect(Collectors.counting());
```

---

# 📌 7. joining()

---

👉 Concatenates strings

```java
String result =
    list.stream()
        .collect(Collectors.joining(", "));
```

---

## With prefix/suffix:

```java
Collectors.joining(", ", "[", "]")
```

---

# 📌 8. mapping() (Downstream Collector)

---

👉 Transform before collecting

```java
Map<String, List<String>> result =
    employees.stream()
        .collect(Collectors.groupingBy(
            Employee::getDept,
            Collectors.mapping(Employee::getName, Collectors.toList())
        ));
```

---

💡 Very important for nested transformations

---

# 📌 9. collectingAndThen()

---

👉 Post-process result

```java
List<Integer> result =
    list.stream()
        .collect(Collectors.collectingAndThen(
            Collectors.toList(),
            Collections::unmodifiableList
        ));
```

---

💡 Used for final transformation

---

# 📌 10. reducing() (Advanced)

---

👉 General-purpose reduction collector

```java
int sum =
    list.stream()
        .collect(Collectors.reducing(
            0,
            n -> n,
            Integer::sum
        ));
```

---

💡 Usually replaced by `map + reduce`

---

# 📌 11. summarizingInt()

---

👉 Gives statistics in one shot

```java
IntSummaryStatistics stats =
    list.stream()
        .collect(Collectors.summarizingInt(n -> n));
```

---

👉 Provides:

* sum
* avg
* min
* max
* count

---

# 📌 12. maxBy() / minBy()

---

```java
Optional<Integer> max =
    list.stream()
        .collect(Collectors.maxBy(Integer::compareTo));
```

---

# 📌 13. flatMapping() (Java 9)

---

👉 Like flatMap but inside collector

```java
Map<String, List<String>> result =
    orders.stream()
        .collect(Collectors.groupingBy(
            Order::getCustomer,
            Collectors.flatMapping(
                o -> o.getItems().stream(),
                Collectors.toList()
            )
        ));
```

---

# 📌 14. filtering() (Java 9)

---

👉 Filter inside collector

```java
Map<String, List<Employee>> result =
    employees.stream()
        .collect(Collectors.groupingBy(
            Employee::getDept,
            Collectors.filtering(
                e -> e.getSalary() > 50000,
                Collectors.toList()
            )
        ));
```

---

# 📌 15. teeing() (Java 12)

---

👉 Combine two collectors

```java
double avg =
    list.stream()
        .collect(Collectors.teeing(
            Collectors.summingInt(n -> n),
            Collectors.counting(),
            (sum, count) -> sum / (double) count
        ));
```

---

💡 Very advanced + impressive

---

# 📌 16. Custom Collector (Interview Favorite)

---

```java
Collector<Integer, List<Integer>, List<Integer>> custom =
    Collector.of(
        ArrayList::new,
        List::add,
        (l1, l2) -> { l1.addAll(l2); return l1; }
    );
```

---

💡 Shows deep understanding

---

# 🎯 17. Real Interview Patterns

---

### ✔ Top K elements

→ `groupingBy + counting + sorting`

---

### ✔ Frequency map

→ `groupingBy + counting`

---

### ✔ Nested grouping

→ `groupingBy + groupingBy`

---

### ✔ Transform + group

→ `groupingBy + mapping`

---

# 🧠 Pro Interview Tips

Say these lines:

* “I’ll use groupingBy for aggregation”
* “I’ll use downstream collector to transform”
* “I’ll use collectingAndThen for final transformation”
* “I’ll handle duplicate keys in toMap”

---

# ⚠️ Common Mistakes

---

### ❌ Forgetting merge function in `toMap()`

---

### ❌ Overusing reduce instead of collectors

---

### ❌ Not using primitive collectors (performance loss)

---

# 🏁 Final Interview Summary

> Collectors provide a powerful abstraction to transform streams into complex data structures using composable operations like grouping, mapping, reducing, and post-processing.

---

---

# 🚀 Tricky Edge-Case Questions — Java Streams

---

## ❓ Q1. What is the output?

```java
List<Integer> list = Arrays.asList(1,2,3,4);

list.stream()
    .filter(n -> n > 2)
    .map(n -> n * 2);
```

<details>
<summary>Answer</summary>

👉 **No output**

💡 Trap:

* Missing **terminal operation**
* Intermediate ops are lazy

</details>

---

## ❓ Q2. What happens?

```java
Stream<Integer> s = Stream.of(1,2,3);

s.findFirst();
s.findFirst();
```

<details>
<summary>Answer</summary>

❌ **IllegalStateException**

💡 Stream is **single-use**

</details>

---

## ❓ Q3. Output?

```java
List<Integer> list = Arrays.asList(1,2,3,4);

list.stream()
    .filter(n -> {
        System.out.println("filter: " + n);
        return n % 2 == 0;
    })
    .findFirst();
```

<details>
<summary>Answer</summary>

```
filter: 1
filter: 2
```

💡 Stops early due to **short-circuiting**

</details>

---

## ❓ Q4. What is printed?

```java
List<Integer> list = Arrays.asList(1,2,3);

list.stream()
    .map(n -> n * 2)
    .peek(System.out::println);
```

<details>
<summary>Answer</summary>

👉 **Nothing**

💡 `peek()` only works if terminal operation exists

</details>

---

## ❓ Q5. What happens?

```java
List<Integer> list = Arrays.asList(1,2,3,4);

list.stream()
    .forEach(n -> {
        if(n == 3) list.remove(n);
    });
```

<details>
<summary>Answer</summary>

❌ **ConcurrentModificationException**

💡 Cannot modify source while streaming

</details>

---

## ❓ Q6. Output?

```java
List<Integer> list = Arrays.asList(1,2,3);

list.parallelStream()
    .forEach(System.out::print);
```

<details>
<summary>Answer</summary>

👉 **Unpredictable order** (e.g., 213, 132)

💡 Parallel streams don’t guarantee order

</details>

---

## ❓ Q7. Fix ordering issue

<details>
<summary>Answer</summary>

```java
list.parallelStream()
    .forEachOrdered(System.out::print);
```

</details>

---

## ❓ Q8. What happens?

```java
List<Integer> list = Arrays.asList(1,2,3);

Optional<Integer> result =
    list.stream()
        .filter(n -> n > 10)
        .findFirst();

System.out.println(result.get());
```

<details>
<summary>Answer</summary>

❌ **NoSuchElementException**

💡 Optional is empty → `.get()` unsafe

</details>

---

## ❓ Q9. Safer version?

<details>
<summary>Answer</summary>

```java
result.ifPresent(System.out::println);
// OR
System.out.println(result.orElse(-1));
```

</details>

---

## ❓ Q10. What is the output?

```java
List<Integer> list = Arrays.asList(1,2,3,4);

int sum = list.stream()
              .reduce(10, Integer::sum);

System.out.println(sum);
```

<details>
<summary>Answer</summary>

👉 Output: **20**

💡 Trap:

* Initial value (10) is included

</details>

---

## ❓ Q11. What is the output?

```java
List<Integer> list = Arrays.asList(1,2,3);

list.stream()
    .sorted((a, b) -> b - a)
    .limit(2)
    .forEach(System.out::print);
```

<details>
<summary>Answer</summary>

👉 Output: **32**

💡 Sorted DESC → [3,2,1] → limit 2

</details>

---

## ❓ Q12. What happens?

```java
List<Integer> list = Arrays.asList(1,2,3);

list.stream()
    .map(n -> {
        if(n == 2) return null;
        return n * 2;
    })
    .forEach(System.out::println);
```

<details>
<summary>Answer</summary>

👉 Output:

```
2
null
6
```

💡 Streams allow nulls (but risky later)

</details>

---

## ❓ Q13. What is the output?

```java
List<String> list = Arrays.asList("a","b","c");

list.stream()
    .map(s -> s + s)
    .count();
```

<details>
<summary>Answer</summary>

👉 Output: **3**

💡 `map()` executes but result not used

</details>

---

## ❓ Q14. What happens?

```java
List<Integer> list = Arrays.asList(1,2,3);

list.stream()
    .filter(n -> n > 5)
    .findFirst()
    .ifPresent(System.out::println);
```

<details>
<summary>Answer</summary>

👉 No output

💡 Optional is empty

</details>

---

## ❓ Q15. What is the output?

```java
List<Integer> list = Arrays.asList(1,2,3,4);

list.stream()
    .reduce((a, b) -> a - b)
    .ifPresent(System.out::println);
```

<details>
<summary>Answer</summary>

👉 Output: **-8**

Step:

```
((1 - 2) - 3) - 4 = -8
```

💡 Not commutative → order matters

</details>

---

## ❓ Q16. Parallel reduce trap

```java
int result = list.parallelStream()
                 .reduce(0, (a, b) -> a - b);

System.out.println(result);
```

<details>
<summary>Answer</summary>

👉 Output unpredictable

💡 Non-associative operation → wrong result in parallel

</details>

---

## ❓ Q17. What is the output?

```java
List<Integer> list = Arrays.asList(1,2,3,4);

list.stream()
    .limit(2)
    .filter(n -> n > 2)
    .forEach(System.out::print);
```

<details>
<summary>Answer</summary>

👉 No output

💡 limit happens before filter → only [1,2]

</details>

---

## ❓ Q18. What is the output?

```java
list.stream()
    .filter(n -> n > 2)
    .limit(2)
    .forEach(System.out::print);
```

<details>
<summary>Answer</summary>

👉 Output: **34**

💡 Order of operations matters

</details>

---

## ❓ Q19. What happens?

```java
List<Integer> result = new ArrayList<>();

list.parallelStream()
    .forEach(n -> result.add(n));
```

<details>
<summary>Answer</summary>

❌ Race condition / inconsistent result

💡 Not thread-safe

</details>

---

## ❓ Q20. Correct approach?

<details>
<summary>Answer</summary>

```java
List<Integer> result =
    list.parallelStream()
        .collect(Collectors.toList());
```

</details>

---

# 🎯 What These Test

* Lazy evaluation
* Stream lifecycle
* Optional handling
* Parallel pitfalls
* Order of operations
* Side effects
* Reduce behavior

---

# 🧠 Killer Interview Tip

When stuck, say:

> “I’ll analyze this based on lazy evaluation and terminal operation behavior”

👉 This shows deep understanding—even if answer isn’t perfect.

---
