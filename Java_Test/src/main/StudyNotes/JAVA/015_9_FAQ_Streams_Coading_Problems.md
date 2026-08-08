# 15.9 FAQ — Streams Coding Problems

## Frequently Asked Coding Questions Using Streams

---

# 1. Find duplicate elements.

<details>
<summary>Show Answer</summary>

**Answer:**

Find elements that appear **more than once** using `groupingBy` + `filter`, or `Set` with `filter`.

### Solution 1 — groupingBy (Most Common)

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 2, 4, 3, 5, 1);

List<Integer> duplicates = numbers.stream()
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
    .entrySet().stream()
    .filter(e -> e.getValue() > 1)
    .map(Map.Entry::getKey)
    .collect(Collectors.toList());
// [1, 2, 3]
```

### Solution 2 — Set Tracking

```java
Set<Integer> seen = new HashSet<>();
List<Integer> duplicates = numbers.stream()
    .filter(n -> !seen.add(n))  // add returns false if already present
    .distinct()
    .collect(Collectors.toList());
```

### For Strings

```java
List<String> words = Arrays.asList("java", "go", "java", "python", "go");

List<String> dupWords = words.stream()
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
    .entrySet().stream()
    .filter(e -> e.getValue() > 1)
    .map(Map.Entry::getKey)
    .collect(Collectors.toList());
// ["java", "go"]
```

### Return as Set (Unique Duplicates Only)

```java
Set<Integer> duplicateSet = numbers.stream()
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
    .entrySet().stream()
    .filter(e -> e.getValue() > 1)
    .map(Map.Entry::getKey)
    .collect(Collectors.toSet());
```

**Interview Point:**

> `groupingBy` + `counting()` then filter count > 1. Alternative: `Set.add()` returns false for duplicates.

</details>

---

# 2. Find first non-repeated character.

<details>
<summary>Show Answer</summary>

**Answer:**

Count character frequencies, then find the **first character** in the string with count = 1.

### Solution

```java
String str = "swiss";

Map<Character, Long> freq = str.chars()
    .mapToObj(c -> (char) c)
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

Optional<Character> firstNonRepeated = str.chars()
    .mapToObj(c -> (char) c)
    .filter(c -> freq.get(c) == 1)
    .findFirst();

System.out.println(firstNonRepeated.orElse(null)); // 'w'
```

### One-Pass with LinkedHashMap (Preserves Order)

```java
Character result = str.chars()
    .mapToObj(c -> (char) c)
    .collect(Collectors.groupingBy(
        Function.identity(),
        LinkedHashMap::new,
        Collectors.counting()))
    .entrySet().stream()
    .filter(e -> e.getValue() == 1)
    .map(Map.Entry::getKey)
    .findFirst()
    .orElse(null);
// 'w'
```

### Using indexOf trick

```java
Optional<Character> first = str.chars()
    .mapToObj(c -> (char) c)
    .filter(c -> str.indexOf(c) == str.lastIndexOf(c))
    .findFirst();
```

**Interview Point:**

> Build frequency map first, then stream original string and filter count == 1, `findFirst()`. Use `LinkedHashMap` to preserve insertion order.

</details>

---

# 3. Find second highest number.

<details>
<summary>Show Answer</summary>

**Answer:**

Sort in descending order, skip first, take next—or use `distinct` + sort + skip.

### Solution 1 — Sort + Skip

```java
List<Integer> numbers = Arrays.asList(5, 12, 3, 12, 8, 5, 20);

Optional<Integer> secondHighest = numbers.stream()
    .distinct()
    .sorted(Comparator.reverseOrder())
    .skip(1)
  .findFirst();

System.out.println(secondHighest.orElse(-1)); // 12
```

### Solution 2 — Two Max Pass

```java
Optional<Integer> second = numbers.stream()
    .distinct()
    .sorted(Comparator.reverseOrder())
    .limit(2)
    .reduce((first, second) -> second);
```

### Solution 3 — IntStream

```java
int second = numbers.stream()
    .distinct()
    .mapToInt(Integer::intValue)
    .sorted()
    .skip(numbers.stream().distinct().count() - 2)
    .findFirst()
    .orElse(-1);
```

**Interview Point:**

> `distinct()` → `sorted(reverseOrder())` → `skip(1)` → `findFirst()`. Always handle duplicates with `distinct()`.

</details>

---

# 4. Count frequency of elements.

<details>
<summary>Show Answer</summary>

**Answer:**

Use `Collectors.groupingBy` with `Collectors.counting()` to build a frequency map.

### Solution

```java
List<String> words = Arrays.asList("apple", "banana", "apple", "cherry", "banana", "apple");

Map<String, Long> frequency = words.stream()
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

// {apple=3, banana=2, cherry=1}
```

### For Integers

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 2, 1, 3, 1);

Map<Integer, Long> freq = numbers.stream()
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
// {1=3, 2=2, 3=2}
```

### Sorted by Frequency

```java
Map<String, Long> sortedFreq = words.stream()
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
    .entrySet().stream()
    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
    .collect(Collectors.toMap(
        Map.Entry::getKey,
        Map.Entry::getValue,
        (a, b) -> a,
        LinkedHashMap::new));
```

**Interview Point:**

> `collect(groupingBy(identity(), counting()))` — standard frequency map pattern.

</details>

---

# 5. Reverse string using Stream.

<details>
<summary>Show Answer</summary>

**Answer:**

Convert string to char stream, collect to list, reverse, and join—or use `reduce` from end.

### Solution 1 — Collect + Reverse

```java
String str = "Hello";

String reversed = str.chars()
    .mapToObj(c -> String.valueOf((char) c))
    .collect(Collectors.collectingAndThen(
        Collectors.toList(),
        list -> {
            Collections.reverse(list);
            return list.stream().collect(Collectors.joining());
        }));
// "olleH"
```

### Solution 2 — reduce (Right Fold)

```java
String reversed = str.chars()
    .mapToObj(c -> String.valueOf((char) c))
    .reduce("", (acc, ch) -> ch + acc);
// "olleH"
```

### Solution 3 — Simple (Interview — mention built-in too)

```java
// Stream approach
String reversed = new StringBuilder(str).reverse().toString();

// Pure stream
String rev = str.chars()
    .sorted((a, b) -> -1)
    .collect(StringBuilder::new,
             StringBuilder::appendCodePoint,
             StringBuilder::append)
    .toString();
```

**Interview Point:**

> `chars()` → map to String → `reduce("", (a,c) -> c + a)` OR collect to list, reverse, join.

</details>

---

# 6. Merge two lists.

<details>
<summary>Show Answer</summary>

**Answer:**

Use `Stream.concat()` or `Stream.of()` with `flatMap` to combine two lists into one.

### Solution 1 — Stream.concat

```java
List<String> list1 = Arrays.asList("a", "b", "c");
List<String> list2 = Arrays.asList("d", "e", "f");

List<String> merged = Stream.concat(list1.stream(), list2.stream())
    .collect(Collectors.toList());
// [a, b, c, d, e, f]
```

### Solution 2 — flatMap

```java
List<String> merged = Stream.of(list1, list2)
    .flatMap(List::stream)
    .collect(Collectors.toList());
```

### Merge Multiple Lists

```java
List<List<Integer>> lists = Arrays.asList(
    Arrays.asList(1, 2),
    Arrays.asList(3, 4),
    Arrays.asList(5, 6)
);

List<Integer> merged = lists.stream()
    .flatMap(List::stream)
    .collect(Collectors.toList());
// [1, 2, 3, 4, 5, 6]
```

### Remove Duplicates While Merging

```java
List<String> merged = Stream.concat(list1.stream(), list2.stream())
    .distinct()
    .collect(Collectors.toList());
```

**Interview Point:**

> `Stream.concat(list1.stream(), list2.stream())` or `Stream.of(l1,l2).flatMap(List::stream)`.

</details>

---

# 7. Convert list to map.

<details>
<summary>Show Answer</summary>

**Answer:**

Use `Collectors.toMap()` with key and value mappers.

### Basic — id → Employee

```java
List<Employee> employees = getEmployees();

Map<Long, Employee> map = employees.stream()
    .collect(Collectors.toMap(Employee::getId, Function.identity()));
```

### name → salary

```java
Map<String, Integer> nameSalary = employees.stream()
    .collect(Collectors.toMap(Employee::getName, Employee::getSalary));
```

### Handle Duplicate Keys

```java
Map<String, Employee> map = employees.stream()
    .collect(Collectors.toMap(
        Employee::getName,
        Function.identity(),
        (existing, replacement) -> existing  // keep first
    ));
```

### List to Map with Index

```java
List<String> list = Arrays.asList("a", "b", "c");

Map<Integer, String> indexMap = IntStream.range(0, list.size())
    .boxed()
    .collect(Collectors.toMap(i -> i, list::get));
// {0=a, 1=b, 2=c}
```

**Interview Point:**

> `toMap(keyMapper, valueMapper)`. Always provide merge function for duplicate keys: `(old, new) -> old`.

</details>

---

# 8. Group by department.

<details>
<summary>Show Answer</summary>

**Answer:**

Use `Collectors.groupingBy(Employee::getDepartment)`.

### Basic Grouping

```java
Map<String, List<Employee>> byDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment));

// "IT"    → [emp1, emp2]
// "HR"    → [emp3]
// "Sales" → [emp4, emp5]
```

### Group Names Only

```java
Map<String, List<String>> namesByDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.mapping(Employee::getName, Collectors.toList())));
```

### Count per Department

```java
Map<String, Long> countByDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.counting()));
```

**Interview Point:**

> `groupingBy(Employee::getDepartment)` — returns `Map<String, List<Employee>>`.

</details>

---

# 9. Find longest string.

<details>
<summary>Show Answer</summary>

**Answer:**

Use `max(Comparator.comparingInt(String::length))` or `sorted` + `findFirst`.

### Solution 1 — max()

```java
List<String> words = Arrays.asList("java", "python", "go", "javascript");

Optional<String> longest = words.stream()
    .max(Comparator.comparingInt(String::length));

System.out.println(longest.orElse("")); // "javascript"
```

### Solution 2 — sorted + findFirst

```java
Optional<String> longest = words.stream()
    .sorted(Comparator.comparingInt(String::length).reversed())
    .findFirst();
```

### With Length Value

```java
words.stream()
    .max(Comparator.comparingInt(String::length))
    .ifPresent(s -> System.out.println(s + " length=" + s.length()));
```

**Interview Point:**

> `max(Comparator.comparingInt(String::length))` — O(n) single pass. Better than sort for one element.

</details>

---

# 10. Find max salary employee.

<details>
<summary>Show Answer</summary>

**Answer:**

Use `max(Comparator.comparingInt(Employee::getSalary))`.

### Solution

```java
Optional<Employee> topEarner = employees.stream()
    .max(Comparator.comparingInt(Employee::getSalary));

topEarner.ifPresent(e ->
    System.out.println(e.getName() + " - " + e.getSalary()));
```

### With Filter

```java
Optional<Employee> topInIT = employees.stream()
    .filter(e -> e.getDepartment().equals("IT"))
    .max(Comparator.comparingInt(Employee::getSalary));
```

### Get Salary Value Only

```java
OptionalInt maxSalary = employees.stream()
    .mapToInt(Employee::getSalary)
    .max();
```

**Interview Point:**

> `max(Comparator.comparingInt(Employee::getSalary))` — same pattern as longest string.

</details>

---

# 11. Sort employees by salary.

<details>
<summary>Show Answer</summary>

**Answer:**

Use `sorted(Comparator.comparingInt(Employee::getSalary))` — ascending or `.reversed()` for descending.

### Ascending by Salary

```java
List<Employee> sorted = employees.stream()
    .sorted(Comparator.comparingInt(Employee::getSalary))
    .collect(Collectors.toList());
```

### Descending (Highest First)

```java
List<Employee> sorted = employees.stream()
    .sorted(Comparator.comparingInt(Employee::getSalary).reversed())
    .collect(Collectors.toList());
```

### Multi-Level Sort

```java
List<Employee> sorted = employees.stream()
    .sorted(Comparator.comparing(Employee::getDepartment)
                      .thenComparingInt(Employee::getSalary).reversed())
    .collect(Collectors.toList());
```

**Interview Point:**

> `sorted(Comparator.comparingInt(Employee::getSalary))`. Chain `thenComparing` for multi-field sort.

</details>

---

# 12. Partition even/odd numbers.

<details>
<summary>Show Answer</summary>

**Answer:**

Use `Collectors.partitioningBy(n -> n % 2 == 0)` — returns `Map<Boolean, List<Integer>>`.

### Solution

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);

Map<Boolean, List<Integer>> partitioned = numbers.stream()
    .collect(Collectors.partitioningBy(n -> n % 2 == 0));

List<Integer> evens = partitioned.get(true);   // [2, 4, 6, 8]
List<Integer> odds  = partitioned.get(false);  // [1, 3, 5, 7]
```

### Count Even vs Odd

```java
Map<Boolean, Long> counts = numbers.stream()
    .collect(Collectors.partitioningBy(n -> n % 2 == 0, Collectors.counting()));
// true → 4, false → 4
```

**Interview Point:**

> `partitioningBy(predicate)` — always `Map<Boolean, List<T>>`. `true` = matched, `false` = not matched.

</details>

---

# 13. Remove duplicates.

<details>
<summary>Show Answer</summary>

**Answer:**

Use `distinct()` intermediate operation or collect to `Set`.

### Solution 1 — distinct()

```java
List<Integer> numbers = Arrays.asList(1, 2, 2, 3, 3, 3, 4);

List<Integer> unique = numbers.stream()
    .distinct()
    .collect(Collectors.toList());
// [1, 2, 3, 4]
```

### Solution 2 — toSet()

```java
List<Integer> unique = numbers.stream()
    .collect(Collectors.toCollection(LinkedHashSet::new))
    .stream()
    .collect(Collectors.toList()); // preserves order
```

### Strings

```java
List<String> unique = words.stream()
    .distinct()
    .collect(Collectors.toList());
```

**Interview Point:**

> `distinct()` uses `equals()` + `hashCode()`. Preserves encounter order. O(n) with HashSet internally.

</details>

---

# 14. Find top 3 highest numbers.

<details>
<summary>Show Answer</summary>

**Answer:**

Sort descending + `limit(3)`, or use `distinct` + sort + limit.

### Solution

```java
List<Integer> numbers = Arrays.asList(10, 5, 20, 15, 20, 8, 25, 3);

List<Integer> top3 = numbers.stream()
    .distinct()
    .sorted(Comparator.reverseOrder())
    .limit(3)
    .collect(Collectors.toList());
// [25, 20, 15]
```

### Top 3 Salaries

```java
List<Employee> top3 = employees.stream()
    .sorted(Comparator.comparingInt(Employee::getSalary).reversed())
    .limit(3)
    .collect(Collectors.toList());
```

### General — Top N Pattern

```java
int n = 3;
List<Integer> topN = numbers.stream()
    .distinct()
    .sorted(Comparator.reverseOrder())
    .limit(n)
    .collect(Collectors.toList());
```

**Interview Point:**

> `distinct()` → `sorted(reversed())` → `limit(n)`. Pattern for top-N queries.

</details>

---

# 15. Find common elements between lists.

<details>
<summary>Show Answer</summary>

**Answer:**

Filter first list where element exists in second list, or use `Set` intersection.

### Solution 1 — filter + contains

```java
List<Integer> list1 = Arrays.asList(1, 2, 3, 4, 5);
List<Integer> list2 = Arrays.asList(3, 4, 5, 6, 7);

List<Integer> common = list1.stream()
    .filter(list2::contains)
    .distinct()
    .collect(Collectors.toList());
// [3, 4, 5]
```

### Solution 2 — Set Intersection (Better Performance)

```java
Set<Integer> set2 = new HashSet<>(list2);

List<Integer> common = list1.stream()
    .filter(set2::contains)
    .distinct()
    .collect(Collectors.toList());
```

### Solution 3 — Stream Intersection

```java
Set<Integer> commonSet = list1.stream()
    .filter(new HashSet<>(list2)::contains)
    .collect(Collectors.toSet());
```

### For Strings

```java
List<String> common = list1.stream()
    .filter(new HashSet<>(list2)::contains)
    .collect(Collectors.toList());
```

**Interview Point:**

> `filter(list2::contains)` — use `HashSet` for O(1) lookup on large lists. `distinct()` to avoid duplicates in result.

</details>

---

# 16. Count vowels in string.

<details>
<summary>Show Answer</summary>

**Answer:**

Filter characters that are vowels and count.

### Solution

```java
String str = "Hello World";

long vowelCount = str.chars()
    .mapToObj(c -> (char) c)
    .filter(c -> "aeiouAEIOU".indexOf(c) >= 0)
    .count();

System.out.println(vowelCount); // 3 (e, o, o)
```

### Using Set

```java
Set<Character> vowels = Set.of('a','e','i','o','u','A','E','I','O','U');

long count = str.chars()
    .mapToObj(c -> (char) c)
    .filter(vowels::contains)
    .count();
```

### Case-Insensitive

```java
long count = str.toLowerCase().chars()
    .filter(c -> "aeiou".indexOf(c) >= 0)
    .count();
```

**Interview Point:**

> `str.chars()` → filter vowels → `count()`. Use `Set.of` for clean vowel check.

</details>

---

# 17. Find occurrence of characters.

<details>
<summary>Show Answer</summary>

**Answer:**

Use `groupingBy` on character stream with `counting()`.

### Solution

```java
String str = "programming";

Map<Character, Long> occurrences = str.chars()
    .mapToObj(c -> (char) c)
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

// {p=1, r=2, o=1, g=2, a=1, m=2, i=1, n=1}
```

### Sorted by Occurrence

```java
Map<Character, Long> sorted = str.chars()
    .mapToObj(c -> (char) c)
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
    .entrySet().stream()
    .sorted(Map.Entry.<Character, Long>comparingByValue().reversed())
    .collect(Collectors.toMap(
        Map.Entry::getKey,
        Map.Entry::getValue,
        (a, b) -> a,
        LinkedHashMap::new));
```

### Filter Characters with Count > 1

```java
Map<Character, Long> repeated = str.chars()
    .mapToObj(c -> (char) c)
    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
    .entrySet().stream()
    .filter(e -> e.getValue() > 1)
    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
// {r=2, g=2, m=2}
```

**Interview Point:**

> Same as word frequency — `groupingBy` on `str.chars()` with `counting()`.

</details>

---

# 18. Convert list of strings to uppercase.

<details>
<summary>Show Answer</summary>

**Answer:**

Use `map(String::toUpperCase)` and collect.

### Solution

```java
List<String> words = Arrays.asList("java", "python", "go");

List<String> upper = words.stream()
    .map(String::toUpperCase)
    .collect(Collectors.toList());
// ["JAVA", "PYTHON", "GO"]
```

### With Filter

```java
List<String> upper = words.stream()
    .filter(s -> s.length() > 2)
    .map(String::toUpperCase)
    .collect(Collectors.toList());
```

### Method Reference vs Lambda

```java
.map(String::toUpperCase)   // preferred
.map(s -> s.toUpperCase())  // same
```

**Interview Point:**

> Simple `map(String::toUpperCase)` — basic stream transform interview question.

</details>

---

# 19. Find average salary.

<details>
<summary>Show Answer</summary>

**Answer:**

Use `mapToInt` + `average()` or `Collectors.averagingInt`.

### Solution 1 — mapToInt + average

```java
OptionalDouble avg = employees.stream()
    .mapToInt(Employee::getSalary)
    .average();

double average = avg.orElse(0.0);
```

### Solution 2 — averagingInt Collector

```java
double average = employees.stream()
    .collect(Collectors.averagingInt(Employee::getSalary));
```

### Average by Department

```java
Map<String, Double> avgByDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.averagingInt(Employee::getSalary)));
```

### With Filter

```java
double avgActive = employees.stream()
    .filter(Employee::isActive)
    .collect(Collectors.averagingInt(Employee::getSalary));
```

**Interview Point:**

> `averagingInt(Employee::getSalary)` or `mapToInt().average()`. Use groupingBy for per-group averages.

</details>

---

# 20. Find kth highest salary.

<details>
<summary>Show Answer</summary>

**Answer:**

Sort salaries descending, skip (k-1), take first—or distinct + sort + skip + findFirst.

### Solution — k = 2 (Second Highest)

```java
int k = 2;

Optional<Integer> kthHighest = employees.stream()
    .map(Employee::getSalary)
    .distinct()
    .sorted(Comparator.reverseOrder())
    .skip(k - 1)
    .findFirst();

System.out.println(kthHighest.orElse(-1));
```

### General kth Highest

```java
public Optional<Integer> findKthHighestSalary(List<Employee> employees, int k) {
    return employees.stream()
        .map(Employee::getSalary)
        .distinct()
        .sorted(Comparator.reverseOrder())
        .skip(k - 1)
        .findFirst();
}

// 3rd highest
findKthHighestSalary(employees, 3);
```

### kth Highest Employee (Not Just Salary)

```java
Optional<Employee> kthEmployee = employees.stream()
    .sorted(Comparator.comparingInt(Employee::getSalary).reversed())
    .skip(k - 1)
    .findFirst();
```

### Alternative — Limit + Reduce

```java
Optional<Integer> kth = employees.stream()
    .map(Employee::getSalary)
    .distinct()
    .sorted(Comparator.reverseOrder())
    .limit(k)
    .reduce((a, b) -> b);  // last of top k
```

**Interview Point:**

> `distinct()` → `sorted(reversed())` → `skip(k-1)` → `findFirst()`. k=2 for second highest. Handle duplicate salaries with `distinct()`.

</details>

---

# Quick Reference Cheat Sheet

<details>
<summary>Show Answer</summary>

| Problem | Key Pattern |
|---------|-------------|
| Duplicates | `groupingBy` + filter count > 1 |
| Frequency | `groupingBy(identity(), counting())` |
| Group by | `groupingBy(classifier)` |
| Partition | `partitioningBy(predicate)` |
| Top N | `sorted(reversed()).limit(n)` |
| Max/Min | `max(Comparator.comparing(...))` |
| Distinct | `.distinct()` |
| Merge lists | `Stream.concat` or `flatMap` |
| List to Map | `toMap(keyMapper, valueMapper)` |
| Average | `averagingInt` or `mapToInt().average()` |
| kth highest | `distinct().sorted().skip(k-1).findFirst()` |

### Interview One-Liner

> Master `groupingBy`, `partitioningBy`, `toMap`, `max/sorted/limit`, and `distinct().sorted().skip(k-1)` — covers 80% of stream coding interviews.

</details>
