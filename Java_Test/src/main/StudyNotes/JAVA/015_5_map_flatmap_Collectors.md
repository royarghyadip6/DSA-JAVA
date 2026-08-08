# 15.5 map() vs flatMap() & Collectors

## map() vs flatMap()

### Most Asked

---

# 1. Difference between map and flatMap?

<details>
<summary>Show Answer</summary>

**Answer:**

`map()` transforms **one element into one element**. `flatMap()` transforms **one element into many elements** and **flattens** them into a single stream.

### Core Difference

| | `map()` | `flatMap()` |
|---|---------|-------------|
| Signature | `T → R` | `T → Stream\<R\>` → flattened `R` |
| Output count | Same as input (1-to-1) | Can be more or fewer (1-to-many) |
| Nested structure | Keeps nesting | Flattens nesting |
| Use | Transform | Flatten + transform |

### map() — 1 to 1

```java
List<String> words = Arrays.asList("hello", "world");

List<Integer> lengths = words.stream()
    .map(s -> s.length())       // "hello"→5, "world"→5
    .collect(Collectors.toList());
// [5, 5]
```

### flatMap() — 1 to many (flattened)

```java
List<List<Integer>> nested = Arrays.asList(
    Arrays.asList(1, 2),
    Arrays.asList(3, 4, 5)
);

List<Integer> flat = nested.stream()
    .flatMap(list -> list.stream())  // flatten
    .collect(Collectors.toList());
// [1, 2, 3, 4, 5]
```

### Visual

```text
map:     [A, B, C]  →  [f(A), f(B), f(C)]           — same size

flatMap: [A, B]     →  [a1,a2, b1,b2,b3]            — flattened
         A produces stream [a1,a2]
         B produces stream [b1,b2,b3]
```

### map() Trap — Nested Result

```java
// map keeps List inside List
List<List<Integer>> wrong = nested.stream()
    .map(list -> list)   // each element stays a List
    .collect(Collectors.toList());
// [[1,2], [3,4,5]] — NOT flattened!
```

### Optional — map vs flatMap

```java
Optional<String> opt = Optional.of("hello");

opt.map(s -> s.length());        // Optional<Integer> — wraps result
opt.flatMap(s -> Optional.of(s.length())); // Optional<Integer> — no double wrap

// flatMap with Optional::stream unwraps
users.stream()
     .map(User::getAddress)           // Stream<Optional<Address>>
     .flatMap(Optional::stream)       // Stream<Address> — skips empty
```

**Interview Point:**

> `map` = transform 1-to-1. `flatMap` = map then flatten. Use `flatMap` when function returns Stream, List, or Optional.

</details>

---

# 2. Real-life example?

<details>
<summary>Show Answer</summary>

**Answer:**

Real production uses of `map()` and `flatMap()` appear in DTO mapping, nested data extraction, text processing, and Optional chaining.

### Example 1 — map(): Employee to Name (DTO)

```java
// Transform each employee to name string
List<String> names = employees.stream()
    .map(Employee::getName)
    .collect(Collectors.toList());

// Transform to DTO
List<EmployeeDTO> dtos = employees.stream()
    .map(e -> new EmployeeDTO(e.getId(), e.getName(), e.getSalary()))
    .collect(Collectors.toList());
```

### Example 2 — flatMap(): All Orders from Customers

```java
// Each customer has list of orders — get ALL orders flattened
List<Order> allOrders = customers.stream()
    .flatMap(customer -> customer.getOrders().stream())
    .collect(Collectors.toList());

// Equivalent without flatMap — verbose
List<Order> allOrders = new ArrayList<>();
for (Customer c : customers) {
    allOrders.addAll(c.getOrders());
}
```

### Example 3 — flatMap(): Split Sentence into Words

```java
List<String> sentences = Arrays.asList(
    "Hello World",
    "Java Streams"
);

List<String> words = sentences.stream()
    .flatMap(sentence -> Arrays.stream(sentence.split(" ")))
    .collect(Collectors.toList());
// ["Hello", "World", "Java", "Streams"]
```

### Example 4 — flatMap(): Department → All Employees

```java
// Company has departments, each has employees
List<Employee> allEmployees = company.getDepartments().stream()
    .flatMap(dept -> dept.getEmployees().stream())
    .collect(Collectors.toList());
```

### Example 5 — map(): Parse and Transform

```java
List<Integer> ports = configLines.stream()
    .map(line -> line.split(":"))
    .map(parts -> Integer.parseInt(parts[1]))
    .collect(Collectors.toList());
```

### Example 6 — flatMap(): API Response Nesting

```java
// Fetch user IDs, then flatMap to fetch each user's orders
List<Order> orders = userIds.stream()
    .map(id -> orderService.getOrdersByUser(id))  // List<Order> per user
    .flatMap(List::stream)                        // flatten all lists
    .filter(order -> order.getStatus().equals("ACTIVE"))
    .collect(Collectors.toList());
```

### Decision Guide

| Scenario | Use |
|----------|-----|
| Change field/type | `map` |
| Extract nested list | `flatMap` |
| Split string to words | `flatMap` |
| Optional unwrapping | `flatMap(Optional::stream)` |
| One object → one object | `map` |

**Interview Point:**

> `map` for field extraction and DTO mapping. `flatMap` for nested collections, splitting text, and flattening API responses.

</details>

---

# 3. Nested collection flattening?

<details>
<summary>Show Answer</summary>

**Answer:**

**Nested collection flattening** converts a structure like `List<List<T>>` into a flat `List<T>` using `flatMap()`.

### Problem — Nested List

```java
List<List<Integer>> nested = Arrays.asList(
    Arrays.asList(1, 2, 3),
    Arrays.asList(4, 5),
    Arrays.asList(6, 7, 8, 9)
);

// Want: [1, 2, 3, 4, 5, 6, 7, 8, 9]
```

### Solution — flatMap()

```java
List<Integer> flat = nested.stream()
    .flatMap(List::stream)           // or: list -> list.stream()
    .collect(Collectors.toList());
// [1, 2, 3, 4, 5, 6, 7, 8, 9]
```

### Why map() Fails

```java
// map keeps nested structure
List<List<Integer>> stillNested = nested.stream()
    .map(list -> list)
    .collect(Collectors.toList());
// [[1,2,3], [4,5], [6,7,8,9]] — NOT flat!
```

### List of Sets

```java
List<Set<String>> setList = ...;

List<String> flat = setList.stream()
    .flatMap(Set::stream)
    .distinct()
    .collect(Collectors.toList());
```

### Three Levels Deep

```java
List<List<List<Integer>>> deepNested = ...;

List<Integer> flat = deepNested.stream()
    .flatMap(list -> list.stream())      // List<List<Integer>> → Stream<Integer>
    .flatMap(inner -> inner.stream())    // need second flatMap
    .collect(Collectors.toList());

// Or chain
deepNested.stream()
        .flatMap(outer -> outer.stream().flatMap(List::stream))
        .collect(Collectors.toList());
```

### Nested Employees by Department

```java
// departments: each dept has list of employees
List<Employee> allEmployees = departments.stream()
    .flatMap(dept -> dept.getEmployees().stream())
    .collect(Collectors.toList());
```

### Stream of Optional Values

```java
List<Optional<String>> optionals = ...;

List<String> present = optionals.stream()
    .flatMap(Optional::stream)   // skips empty Optionals
    .collect(Collectors.toList());
```

### Arrays.stream() in flatMap

```java
String sentence = "Hello Java World";

List<String> words = Stream.of(sentence)
    .flatMap(s -> Arrays.stream(s.split(" ")))
    .collect(Collectors.toList());
// ["Hello", "Java", "World"]
```

### Pattern to Remember

```text
List<List<T>>  → .flatMap(List::stream)
List<Set<T>>   → .flatMap(Set::stream)
List<Optional<T>> → .flatMap(Optional::stream)
T[] per element  → .flatMap(arr -> Arrays.stream(arr))
```

**Interview Point:**

> Nested flattening = `flatMap(collection::stream)`. `map` preserves nesting; `flatMap` collapses one level. Multiple levels need chained `flatMap`.

</details>

---

## Collectors

---

# 4. collect()?

<details>
<summary>Show Answer</summary>

**Answer:**

`collect()` is a **terminal operation** that accumulates stream elements into a **final result** using a `Collector`—List, Map, String, or custom type.

### Syntax

```java
<R, A> R collect(Collector<T, A, R> collector)
```

| Type | Meaning |
|------|---------|
| `T` | Stream element type |
| `A` | Accumulator container (mutable) |
| `R` | Final result type |

### Basic Usage

```java
List<String> list = stream.collect(Collectors.toList());
Set<Integer> set = stream.collect(Collectors.toSet());
Map<String, Integer> map = stream.collect(Collectors.toMap(...));
String joined = stream.collect(Collectors.joining(", "));
```

### How Collector Works Internally

```text
1. Supplier     → creates accumulator (e.g., new ArrayList)
2. Accumulator  → adds each element to accumulator
3. Combiner     → merges accumulators (parallel streams)
4. Finisher     → transforms accumulator to final result
```

### collect() vs reduce()

```java
// collect — flexible, many collectors
List<String> list = stream.collect(Collectors.toList());

// reduce — combine into single value
int sum = numbers.stream().reduce(0, Integer::sum);
```

### Custom Collector

```java
Collector<String, ?, String> custom = Collector.of(
    StringBuilder::new,           // supplier
    StringBuilder::append,        // accumulator
    (sb1, sb2) -> sb1.append(sb2), // combiner
    StringBuilder::toString       // finisher
);
```

**Interview Point:**

> `collect()` = terminal op with `Collector`. Four parts: Supplier, Accumulator, Combiner, Finisher. Most flexible way to gather stream results.

</details>

---

# 5. Collectors.toList()?

<details>
<summary>Show Answer</summary>

**Answer:**

`Collectors.toList()` collects stream elements into an **`ArrayList`**—the most common collector.

### Usage

```java
List<String> names = employees.stream()
    .map(Employee::getName)
    .collect(Collectors.toList());
```

### What It Returns

```java
// Returns List<T> — actually ArrayList
List<String> list = stream.collect(Collectors.toList());
// Mutable list — can add/remove after collect
```

### toList() vs toUnmodifiableList() (Java 10+)

```java
// Mutable ArrayList
List<String> mutable = stream.collect(Collectors.toList());

// Unmodifiable list (Java 10+)
List<String> immutable = stream.collect(Collectors.toUnmodifiableList());

// Stream.toList() — Java 16+ unmodifiable
List<String> list = stream.toList();
```

### Common Pipeline

```java
List<Employee> seniors = employees.stream()
    .filter(e -> e.getYearsOfService() > 5)
    .sorted(Comparator.comparing(Employee::getName))
    .collect(Collectors.toList());
```

### Related Collectors

```java
Collectors.toSet()              // HashSet — unique elements
Collectors.toCollection(TreeSet::new)  // specific collection type
Collectors.toUnmodifiableList() // Java 10+
```

**Interview Point:**

> `toList()` returns mutable `ArrayList`. For immutable result use `toUnmodifiableList()` or `stream.toList()` (Java 16+).

</details>

---

# 6. groupingBy()?

<details>
<summary>Show Answer</summary>

**Answer:**

`Collectors.groupingBy()` groups stream elements into a **`Map<K, List<T>>`** based on a classifier function—like SQL `GROUP BY`.

### Basic Grouping

```java
Map<String, List<Employee>> byDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment));

// Result:
// "IT"    → [emp1, emp2]
// "HR"    → [emp3]
// "Sales" → [emp4, emp5]
```

### With Downstream Collector

```java
// Count per department
Map<String, Long> countByDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.counting()
    ));
// "IT" → 5, "HR" → 3

// Sum salary per department
Map<String, Integer> salarySum = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.summingInt(Employee::getSalary)
    ));
```

### Nested Grouping

```java
Map<String, Map<String, List<Employee>>> nested = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.groupingBy(Employee::getRole)
    ));
// dept → role → employees
```

### Specify Map Type

```java
Map<String, List<Employee>> map = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        TreeMap::new,                    // sorted map
        Collectors.toList()
    ));
```

### groupingBy Variants

```java
Collectors.groupingBy(classifier)                    // Map<K, List<T>>
Collectors.groupingBy(classifier, downstream)        // Map<K, D>
Collectors.groupingBy(classifier, mapFactory, downstream)
```

**Interview Point:**

> `groupingBy` = SQL GROUP BY. Returns `Map<K, List<T>>`. Add downstream collector for count, sum, or nested grouping.

</details>

---

# 7. partitioningBy()?

<details>
<summary>Show Answer</summary>

**Answer:**

`Collectors.partitioningBy()` is a **special groupingBy** with a **boolean classifier**—splits elements into **true** and **false** groups.

### Basic Usage

```java
Map<Boolean, List<Employee>> partitioned = employees.stream()
    .collect(Collectors.partitioningBy(e -> e.getSalary() > 50000));

// true  → employees with salary > 50000
// false → employees with salary <= 50000
```

### With Downstream Collector

```java
// Count in each partition
Map<Boolean, Long> counts = employees.stream()
    .collect(Collectors.partitioningBy(
        e -> e.getSalary() > 50000,
        Collectors.counting()
    ));
// true → 15, false → 35
```

### Even/Odd Partition

```java
Map<Boolean, List<Integer>> evenOdd = numbers.stream()
    .collect(Collectors.partitioningBy(n -> n % 2 == 0));

List<Integer> evens = evenOdd.get(true);
List<Integer> odds = evenOdd.get(false);
```

### partitioningBy vs groupingBy

| | `partitioningBy` | `groupingBy` |
|---|------------------|--------------|
| Key type | **Boolean** only | Any type |
| Map size | Always 2 keys | Any number of keys |
| Use | True/false split | General grouping |

```java
// partitioningBy — boolean only
partitioningBy(e -> e.isActive())

// groupingBy — any key
groupingBy(Employee::getDepartment)
```

### Real Example — Active vs Inactive

```java
Map<Boolean, List<Employee>> activeMap = employees.stream()
    .collect(Collectors.partitioningBy(Employee::isActive));

List<Employee> active = activeMap.get(true);
List<Employee> inactive = activeMap.get(false);
```

**Interview Point:**

> `partitioningBy` = boolean `groupingBy`. Always `Map<Boolean, List<T>>` with true/false keys. Use for binary splits.

</details>

---

# 8. counting()?

<details>
<summary>Show Answer</summary>

**Answer:**

`Collectors.counting()` counts the number of elements in a stream or group—returns `Long`.

### Standalone Count

```java
long count = employees.stream()
    .filter(e -> e.getSalary() > 50000)
    .collect(Collectors.counting());
// Same as: employees.stream().filter(...).count()
```

### As Downstream in groupingBy

```java
Map<String, Long> countByDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.counting()
    ));
// "IT" → 12, "HR" → 5, "Sales" → 8
```

### Related Counting Collectors

```java
Collectors.counting()           // count elements → Long
Collectors.summingInt(Employee::getSalary)   // sum → Integer
Collectors.summingLong(...)
Collectors.summingDouble(...)
Collectors.averagingInt(Employee::getSalary) // average → Double
Collectors.summarizingInt(Employee::getSalary) // IntSummaryStatistics
```

### summarizingInt — Full Stats

```java
IntSummaryStatistics stats = employees.stream()
    .collect(Collectors.summarizingInt(Employee::getSalary));

stats.getCount();   // number of employees
stats.getSum();     // total salary
stats.getMin();     // min salary
stats.getMax();     // max salary
stats.getAverage(); // average salary
```

**Interview Point:**

> `counting()` returns `Long`. Use standalone or as downstream in `groupingBy`. Related: `summingInt`, `averagingInt`, `summarizingInt`.

</details>

---

# 9. joining()?

<details>
<summary>Show Answer</summary>

**Answer:**

`Collectors.joining()` concatenates stream elements into a **single String** with optional delimiter, prefix, and suffix.

### Basic Join

```java
List<String> words = Arrays.asList("Java", "Python", "Go");

String result = words.stream()
    .collect(Collectors.joining());
// "JavaPythonGo"
```

### With Delimiter

```java
String result = words.stream()
    .collect(Collectors.joining(", "));
// "Java, Python, Go"
```

### With Prefix and Suffix

```java
String result = words.stream()
    .collect(Collectors.joining(", ", "[", "]"));
// "[Java, Python, Go]"
```

### joining() Variants

```java
Collectors.joining()              // no delimiter
Collectors.joining(delimiter)     // with delimiter
Collectors.joining(delimiter, prefix, suffix)
```

### Real Examples

```java
// Join employee names
String names = employees.stream()
    .map(Employee::getName)
    .collect(Collectors.joining(", "));

// Build CSV line
String csv = fields.stream()
    .map(Object::toString)
    .collect(Collectors.joining(","));

// SQL IN clause
String ids = idList.stream()
    .map(String::valueOf)
    .collect(Collectors.joining(",", "(", ")"));
// "(1, 2, 3, 4)"
```

### joining vs StringBuilder Loop

```java
// Stream — clean
String joined = list.stream().collect(Collectors.joining(", "));

// Traditional
StringBuilder sb = new StringBuilder();
for (int i = 0; i < list.size(); i++) {
    if (i > 0) sb.append(", ");
    sb.append(list.get(i));
}
```

**Interview Point:**

> `joining()` concatenates strings. Delimiter, prefix, suffix optional. Cleaner than manual StringBuilder loops.

</details>

---

# 10. mapping()?

<details>
<summary>Show Answer</summary>

**Answer:**

`Collectors.mapping()` applies a **transform function** before a **downstream collector**—used inside `groupingBy` or `partitioningBy`.

### Syntax

```java
Collectors.mapping(function, downstreamCollector)
```

### Example — Group Names by Department

```java
Map<String, List<String>> namesByDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.mapping(Employee::getName, Collectors.toList())
    ));
// "IT" → ["Alice", "Bob"]
// "HR" → ["Charlie"]
```

### mapping + counting

```java
Map<String, Long> countByDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.mapping(Employee::getId, Collectors.counting())
    ));
```

### mapping vs stream map()

```java
// mapping — inside collector (groupingBy downstream)
groupingBy(Employee::getDept,
    Collectors.mapping(Employee::getName, toList()))

// stream map — in pipeline before collect
employees.stream()
    .map(Employee::getName)
    .collect(Collectors.toList())
```

### Complex — Group Salaries by Dept

```java
Map<String, Set<Integer>> salariesByDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.mapping(Employee::getSalary, Collectors.toSet())
    ));
```

### mapping in partitioningBy

```java
Map<Boolean, List<String>> activeNames = employees.stream()
    .collect(Collectors.partitioningBy(
        Employee::isActive,
        Collectors.mapping(Employee::getName, Collectors.toList())
    ));
```

**Interview Point:**

> `mapping(fn, downstream)` transforms elements before downstream collector. Essential inside `groupingBy` when you want transformed grouped results.

</details>

---

# 11. collectingAndThen()?

<details>
<summary>Show Answer</summary>

**Answer:**

`Collectors.collectingAndThen()` applies a **finishing function** to the result of another collector—post-process the collected result.

### Syntax

```java
Collectors.collectingAndThen(downstreamCollector, finisherFunction)
```

### Example — Unmodifiable List

```java
List<String> immutable = stream.collect(
    Collectors.collectingAndThen(
        Collectors.toList(),
        Collections::unmodifiableList
    )
);
```

### Example — Trim Joined String

```java
String result = words.stream()
    .collect(Collectors.collectingAndThen(
        Collectors.joining(", "),
        s -> s.trim()
    ));
```

### Example — Get Max from Grouped Map

```java
Map<String, Optional<Employee>> maxByDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.collectingAndThen(
            Collectors.maxBy(Comparator.comparingInt(Employee::getSalary)),
            opt -> opt  // Optional<Employee> per group
        )
    ));
```

### Example — Size After Collect

```java
Integer size = stream.collect(
    Collectors.collectingAndThen(
        Collectors.toList(),
        List::size
    )
);
```

### Flow

```text
Stream elements
    ↓
downstream collector (e.g., toList)
    ↓
finisher function (e.g., unmodifiableList)
    ↓
Final transformed result
```

### Real Use — Immutable Set from Stream

```java
Set<String> immutableSet = tags.stream()
    .collect(Collectors.collectingAndThen(
        Collectors.toSet(),
        Collections::unmodifiableSet
    ));
```

**Interview Point:**

> `collectingAndThen(collector, finisher)` = collect then transform. Use for unmodifiable wrappers, trimming, or converting collected result type.

</details>

---

### Coding Questions

---

# 12. Group employees by department.

<details>
<summary>Show Answer</summary>

**Answer:**

Use `Collectors.groupingBy()` with department as the classifier.

### Basic — List per Department

```java
Map<String, List<Employee>> byDepartment = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment));

// "IT"    → [emp1, emp2, emp3]
// "HR"    → [emp4]
// "Sales" → [emp5, emp6]
```

### With Employee Class

```java
class Employee {
    private String name;
    private String department;
    private int salary;

    public String getDepartment() { return department; }
    // ...
}

List<Employee> employees = getEmployees();

Map<String, List<Employee>> grouped = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment));
```

### Group Names Only

```java
Map<String, List<String>> namesByDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.mapping(Employee::getName, Collectors.toList())
    ));
// "IT" → ["Alice", "Bob"]
```

### Sorted Map Result

```java
Map<String, List<Employee>> sorted = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        TreeMap::new,
        Collectors.toList()
    ));
```

### Handle Null Department

```java
Map<String, List<Employee>> grouped = employees.stream()
    .collect(Collectors.groupingBy(
        e -> e.getDepartment() != null ? e.getDepartment() : "UNKNOWN"
    ));
```

**Interview Point:**

> `groupingBy(Employee::getDepartment)` — one line solution. Add `mapping` downstream for names only.

</details>

---

# 13. Find highest salary employee.

<details>
<summary>Show Answer</summary>

**Answer:**

Use `max()` with salary comparator, or `sorted().findFirst()` for top employee.

### Solution 1 — max() (Best)

```java
Optional<Employee> highestPaid = employees.stream()
    .max(Comparator.comparingInt(Employee::getSalary));

highestPaid.ifPresent(emp ->
    System.out.println(emp.getName() + " - " + emp.getSalary()));
```

### Solution 2 — sorted + findFirst

```java
Optional<Employee> highestPaid = employees.stream()
    .sorted(Comparator.comparingInt(Employee::getSalary).reversed())
    .findFirst();
```

### Solution 3 — reduce

```java
Optional<Employee> highestPaid = employees.stream()
    .reduce((e1, e2) ->
        e1.getSalary() > e2.getSalary() ? e1 : e2);
```

### Top N Highest Salaries

```java
List<Employee> top5 = employees.stream()
    .sorted(Comparator.comparingInt(Employee::getSalary).reversed())
    .limit(5)
    .collect(Collectors.toList());
```

### Highest Salary Value Only

```java
OptionalInt maxSalary = employees.stream()
    .mapToInt(Employee::getSalary)
    .max();

int highest = maxSalary.orElse(0);
```

### With Filter

```java
Optional<Employee> highestInIT = employees.stream()
    .filter(e -> e.getDepartment().equals("IT"))
    .max(Comparator.comparingInt(Employee::getSalary));
```

**Interview Point:**

> Prefer `max(Comparator.comparingInt(Employee::getSalary))` — O(n), single pass. `sorted().findFirst()` is O(n log n) — use only for top-N.

</details>

---

# 14. Count employees by department.

<details>
<summary>Show Answer</summary>

**Answer:**

Use `groupingBy` with `counting()` as downstream collector.

### Solution

```java
Map<String, Long> countByDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.counting()
    ));

// "IT"    → 12
// "HR"    → 5
// "Sales" → 8
```

### Print Results

```java
countByDept.forEach((dept, count) ->
    System.out.println(dept + ": " + count));
```

### With Filter — Count Active Only

```java
Map<String, Long> activeCountByDept = employees.stream()
    .filter(Employee::isActive)
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.counting()
    ));
```

### Alternative — Map with merge

```java
Map<String, Long> countByDept = employees.stream()
    .collect(Collectors.toMap(
        Employee::getDepartment,
        e -> 1L,
        Long::sum
    ));
// groupingBy + counting is cleaner
```

### Total Count Verification

```java
long total = countByDept.values().stream()
    .mapToLong(Long::longValue)
    .sum();
// equals employees.size()
```

### Sorted by Count

```java
Map<String, Long> sorted = countByDept.entrySet().stream()
    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
    .collect(Collectors.toMap(
        Map.Entry::getKey,
        Map.Entry::getValue,
        (a, b) -> a,
        LinkedHashMap::new
    ));
```

**Interview Point:**

> `groupingBy(Employee::getDepartment, Collectors.counting())` — standard pattern. Returns `Map<String, Long>`.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: flatMap(List::stream) vs map(List::stream)?

<details>
<summary>Show Answer</summary>

**Answer:** `flatMap(List::stream)` flattens to elements. `map(List::stream)` gives `Stream<Stream<T>>` — nested, not flattened.

</details>

---

### Q: groupingBy vs toMap for counting?

<details>
<summary>Show Answer</summary>

**Answer:** `groupingBy(dept, counting())` is cleaner for counts. `toMap` with merge function works but is more verbose and error-prone with duplicates.

</details>

---

### Q: collect(toList()) vs stream.toList()?

<details>
<summary>Show Answer</summary>

**Answer:** `collect(toList())` returns mutable `ArrayList`. `stream.toList()` (Java 16+) returns **unmodifiable** list. Choose based on whether you need to modify result.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> `map` = 1-to-1 transform; `flatMap` = flatten nested (List, Optional, split). Collectors: `groupingBy` = GROUP BY, `partitioningBy` = boolean split, `mapping` = transform in groups, `collectingAndThen` = post-process result.

</details>
