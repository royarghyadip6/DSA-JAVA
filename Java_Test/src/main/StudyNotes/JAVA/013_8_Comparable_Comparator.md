# 13.8 Comparable and Comparator

## Comparable and Comparator

## Comparable

---

# 1. What is Comparable?

<details>
<summary>Show Answer</summary>

**Answer:**

`Comparable` is a **functional interface** in `java.lang` that defines **natural ordering** for a class by implementing a single method: `compareTo()`.

```java
public interface Comparable<T> {
    int compareTo(T o);
}
```

### Purpose

* Enables objects to compare **themselves** to another object
* Used by sorting algorithms (`Collections.sort()`, `TreeSet`, `TreeMap`)
* Defines the **default** sort order for a class

### Example

```java
class Employee implements Comparable<Employee> {

    private int id;
    private String name;

    Employee(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int compareTo(Employee other) {
        return Integer.compare(this.id, other.id);
    }
}
```

### Usage

```java
List<Employee> employees = new ArrayList<>();
employees.add(new Employee(102, "Jane"));
employees.add(new Employee(101, "John"));

Collections.sort(employees); // uses compareTo() — sorted by id
```

### Built-in Comparable Classes

| Class | Natural Order |
|-------|---------------|
| `Integer`, `Long`, `Double` | Numerical ascending |
| `String` | Alphabetical (Unicode) |
| `Date`, `LocalDate` | Chronological |
| `BigDecimal` | Numerical value |

**Interview Point:**

> `Comparable` = "I know how to sort myself." One natural order per class, defined inside the class itself.

</details>

---

# 2. compareTo() method?

<details>
<summary>Show Answer</summary>

**Answer:**

`compareTo()` compares the **current object** with the specified object and returns an **integer** indicating their relative order.

### Return Values

| Return | Meaning |
|--------|---------|
| **Negative** (`< 0`) | Current object is **less than** argument |
| **Zero** (`0`) | Objects are **equal** (for ordering) |
| **Positive** (`> 0`) | Current object is **greater than** argument |

### Example

```java
String s1 = "Apple";
String s2 = "Banana";

System.out.println(s1.compareTo(s2)); // negative — Apple < Banana
System.out.println(s2.compareTo(s1)); // positive — Banana > Apple
System.out.println(s1.compareTo("Apple")); // 0 — equal
```

### Custom compareTo()

```java
@Override
public int compareTo(Employee other) {
    return Integer.compare(this.salary, other.salary);
}

// Integer.compare() handles overflow safely
// Prefer over: this.salary - other.salary (overflow risk!)
```

### Important Rules

```java
// ✅ Good — consistent with equals
@Override
public int compareTo(Employee o) {
    return Integer.compare(this.id, o.id);
}

// ⚠️ compareTo consistent with equals is recommended
// but not strictly required by contract
```

### Contract (Must Follow)

| Rule | Description |
|------|-------------|
| Reflexive | `a.compareTo(a) == 0` |
| Consistent with equals | If `compareTo == 0`, usually `equals` should be true |
| Transitive | If `a < b` and `b < c`, then `a < c` |
| Sign consistent | Opposite signs for `a.compareTo(b)` and `b.compareTo(a)` |

### Null Handling

```java
@Override
public int compareTo(Employee other) {
    if (other == null) throw new NullPointerException();
    return Integer.compare(this.id, other.id);
}
```

**Interview Point:**

> Negative = less, zero = equal, positive = greater. Use `Integer.compare()` / `Comparator.comparing()`—never subtract integers (overflow bug).

</details>

---

# 3. Natural sorting?

<details>
<summary>Show Answer</summary>

**Answer:**

**Natural sorting** (or **natural ordering**) is the **default sort order** defined by a class's `Comparable` implementation—without any external `Comparator`.

### How It Works

```text
Class implements Comparable
    ↓
compareTo() defines natural order
    ↓
Collections.sort(), TreeSet, TreeMap use it automatically
```

### Example — String Natural Order

```java
List<String> names = Arrays.asList("Charlie", "Alice", "Bob");
Collections.sort(names); // String.compareTo() — alphabetical

System.out.println(names); // [Alice, Bob, Charlie]
```

### Example — Integer Natural Order

```java
TreeSet<Integer> set = new TreeSet<>();
set.add(30);
set.add(10);
set.add(20);

System.out.println(set); // [10, 20, 30] — ascending
```

### Example — Employee by ID (Natural Order)

```java
class Employee implements Comparable<Employee> {
    int id;

    @Override
    public int compareTo(Employee o) {
        return Integer.compare(this.id, o.id);
    }
}

TreeSet<Employee> set = new TreeSet<>();
// Always sorted by id — natural order
```

### Natural vs Custom Sorting

| Natural Sorting | Custom Sorting |
|-----------------|----------------|
| Via `Comparable` | Via `Comparator` |
| One default order | Multiple orders possible |
| Built into class | External / passed at runtime |
| `Collections.sort(list)` | `Collections.sort(list, comparator)` |

### When Natural Order Is Wrong

```java
// Employee natural order = by id
// But user wants sort by name → need Comparator
employees.sort(Comparator.comparing(Employee::getName));
```

**Interview Point:**

> Natural sorting = default order from `Comparable`. One per class. `TreeSet`/`TreeMap` always use natural order unless you pass a `Comparator`.

</details>

---

## Comparator

---

# 4. What is Comparator?

<details>
<summary>Show Answer</summary>

**Answer:**

`Comparator` is a **functional interface** in `java.util` that defines **custom comparison logic** between two objects—**external** to the class being compared.

```java
public interface Comparator<T> {
    int compare(T o1, T o2);
}
```

### Key Difference from Comparable

```text
Comparable  → sorting logic INSIDE the class (natural order)
Comparator  → sorting logic OUTSIDE the class (custom order)
```

### Example — Sort by Name

```java
class Employee {
    int id;
    String name;
    // does NOT need to implement Comparable
}

Comparator<Employee> byName =
        (e1, e2) -> e1.name.compareTo(e2.name);

List<Employee> list = getEmployees();
list.sort(byName);
```

### Multiple Comparators for Same Class

```java
Comparator<Employee> byId   = Comparator.comparingInt(Employee::getId);
Comparator<Employee> byName = Comparator.comparing(Employee::getName);
Comparator<Employee> bySalary = Comparator.comparingInt(Employee::getSalary);

list.sort(byName);   // sort by name
list.sort(bySalary); // sort by salary — different strategy
```

### Used By

* `Collections.sort(list, comparator)`
* `List.sort(comparator)`
* `TreeSet(comparator)`, `TreeMap(comparator)`
* `Stream.sorted(comparator)`
* `PriorityQueue(comparator)`

**Interview Point:**

> `Comparator` = external, flexible, multiple sort strategies. Does not require modifying the compared class.

</details>

---

# 5. compare() method?

<details>
<summary>Show Answer</summary>

**Answer:**

`compare()` is the single abstract method of `Comparator`—it compares **two objects** and returns the same sign convention as `compareTo()`.

```java
int compare(T o1, T o2)
```

### Return Values

| Return | Meaning |
|--------|---------|
| Negative | `o1 < o2` |
| Zero | `o1 equals o2` (for ordering) |
| Positive | `o1 > o2` |

### compare() vs compareTo()

| | `compareTo()` | `compare()` |
|---|---------------|-------------|
| Interface | `Comparable` | `Comparator` |
| Arguments | `this` vs `other` | `o1` vs `o2` |
| Location | Inside class | External class/lambda |
| Objects compared | Self vs another | Any two objects |

### Example

```java
Comparator<String> comparator = new Comparator<String>() {
    @Override
    public int compare(String s1, String s2) {
        return s1.length() - s2.length(); // shorter first
    }
};

List<String> words = Arrays.asList("Java", "Go", "Python");
words.sort(comparator);

System.out.println(words); // [Go, Java, Python]
```

### Lambda Form (Java 8+)

```java
Comparator<String> byLength =
        (s1, s2) -> Integer.compare(s1.length(), s2.length());

words.sort(byLength);
```

### Null-Safe Compare

```java
Comparator<String> nullSafe =
        Comparator.nullsFirst(String::compareTo);

// nulls sort first, then natural order
```

**Interview Point:**

> `compare(o1, o2)` same sign rules as `compareTo()`. Comparator compares any two objects—no need for either to be `this`.

</details>

---

# 6. Custom sorting?

<details>
<summary>Show Answer</summary>

**Answer:**

**Custom sorting** uses `Comparator` to define **any sort order** at runtime—without changing the class's natural order.

### Sort by Single Field

```java
List<Employee> employees = getEmployees();

// By salary ascending
employees.sort(Comparator.comparingInt(Employee::getSalary));

// By name
employees.sort(Comparator.comparing(Employee::getName));

// By salary descending
employees.sort(Comparator.comparingInt(Employee::getSalary).reversed());
```

### Multi-Level Sorting

```java
// Sort by department, then by salary
employees.sort(
    Comparator.comparing(Employee::getDepartment)
              .thenComparingInt(Employee::getSalary)
);
```

### Custom Logic

```java
Comparator<Employee> bySeniority = (e1, e2) -> {
    int years1 = e1.getYearsOfService();
    int years2 = e2.getYearsOfService();
    return Integer.compare(years1, years2);
};

employees.sort(bySeniority);
```

### TreeSet with Custom Order

```java
// TreeSet sorted by name, not natural order
Set<Employee> set = new TreeSet<>(
        Comparator.comparing(Employee::getName));
```

### Stream Custom Sort

```java
employees.stream()
         .sorted(Comparator.comparingInt(Employee::getSalary).reversed())
         .limit(5)
         .forEach(System.out::println);
```

### Comparator Utility Methods (Java 8+)

```java
Comparator.comparing(Employee::getName);
Comparator.comparingInt(Employee::getSalary);
Comparator.comparingDouble(Employee::getRating);
Comparator.naturalOrder();
Comparator.reverseOrder();
Comparator.nullsFirst(naturalOrder);
Comparator.nullsLast(naturalOrder);
```

**Interview Point:**

> Custom sorting = `Comparator` at runtime. Chain with `thenComparing()` for multi-field sorts. Use method references + `comparingInt` for clean code.

</details>

---

## Frequently Asked

---

# 7. Comparable vs Comparator?

<details>
<summary>Show Answer</summary>

**Answer:**

| Feature | Comparable | Comparator |
|---------|------------|------------|
| Package | `java.lang` | `java.util` |
| Method | `compareTo(T o)` | `compare(T o1, T o2)` |
| Location | **Inside** the class | **Outside** the class |
| Sort orders | **One** (natural) | **Multiple** custom |
| Modifies class? | Yes — implements interface | No — external |
| Used by | `TreeSet`, `TreeMap` (default) | When custom order needed |
| Functional interface | Yes (Java 8+) | Yes (Java 8+) |

### Comparable — Natural Order

```java
class Student implements Comparable<Student> {
    int rollNo;

    @Override
    public int compareTo(Student s) {
        return this.rollNo - s.rollNo;
    }
}

Collections.sort(students); // uses compareTo()
```

### Comparator — Custom Order

```java
class Student {
    int rollNo;
    String name;
}

students.sort((s1, s2) -> s1.name.compareTo(s2.name));
```

### When to Use Which

| Use Comparable | Use Comparator |
|--------------|----------------|
| One obvious default order | Multiple sort options |
| Order is intrinsic to object | Order depends on context |
| `TreeSet` without comparator | Different UIs need different sorts |
| Domain rule (e.g., id is primary key) | Presentation layer sorting |

### Both Together

```java
class Employee implements Comparable<Employee> {
    // Natural order by id
    @Override
    public int compareTo(Employee o) {
        return Integer.compare(this.id, o.id);
    }
}

// Custom order by salary when needed
employees.sort(Comparator.comparingInt(Employee::getSalary));
```

**Interview Point:**

> Comparable = one natural order inside class. Comparator = flexible external orders. Senior answer: "Comparable for domain default, Comparator for UI/report sorting."

</details>

---

# 8. Multiple sorting strategies?

<details>
<summary>Show Answer</summary>

**Answer:**

Java supports **multiple sorting strategies** using different `Comparator` instances or chaining comparators—without modifying the class.

### Strategy 1 — Separate Comparators

```java
Comparator<Employee> byName   = Comparator.comparing(Employee::getName);
Comparator<Employee> bySalary = Comparator.comparingInt(Employee::getSalary);
Comparator<Employee> byDept   = Comparator.comparing(Employee::getDepartment);

// Apply based on user selection
if (sortBy.equals("name"))   list.sort(byName);
if (sortBy.equals("salary")) list.sort(bySalary);
if (sortBy.equals("dept"))   list.sort(byDept);
```

### Strategy 2 — Chained Comparators (Multi-field)

```java
Comparator<Employee> strategy =
        Comparator.comparing(Employee::getDepartment)   // 1st: dept
                  .thenComparing(Employee::getName)    // 2nd: name
                  .thenComparingInt(Employee::getSalary); // 3rd: salary

employees.sort(strategy);
```

### Strategy 3 — Enum-Based Strategy Pattern

```java
enum SortStrategy {
    BY_NAME(Comparator.comparing(Employee::getName)),
    BY_SALARY(Comparator.comparingInt(Employee::getSalary)),
    BY_ID(Comparator.comparingInt(Employee::getId));

    final Comparator<Employee> comparator;
    SortStrategy(Comparator<Employee> c) { this.comparator = c; }
}

employees.sort(SortStrategy.BY_SALARY.comparator);
```

### Strategy 4 — Reversed Order

```java
Comparator<Employee> highestSalaryFirst =
        Comparator.comparingInt(Employee::getSalary).reversed();
```

### Strategy 5 — Null Handling Variants

```java
Comparator<Employee> nullsLastByName =
        Comparator.comparing(
            Employee::getName,
            Comparator.nullsLast(String::compareTo));
```

### Production Example — API Sort Parameter

```java
public List<Employee> getEmployees(String sortBy, String direction) {
    Comparator<Employee> comparator = switch (sortBy) {
        case "name"   -> Comparator.comparing(Employee::getName);
        case "salary" -> Comparator.comparingInt(Employee::getSalary);
        default       -> Comparator.comparingInt(Employee::getId);
    };

    if ("desc".equals(direction)) {
        comparator = comparator.reversed();
    }

    return employees.stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
}
```

**Interview Point:**

> Multiple strategies = multiple `Comparator` instances or `thenComparing()` chains. Enum strategy pattern is clean for UI-driven sort options.

</details>

---

# 9. Lambda-based Comparator?

<details>
<summary>Show Answer</summary>

**Answer:**

Since Java 8, `Comparator` is a **functional interface**—sort logic can be written as **lambda expressions** or **method references**.

### Basic Lambda Comparator

```java
List<String> list = Arrays.asList("Java", "Go", "Python");

// Lambda — sort by length
list.sort((s1, s2) -> Integer.compare(s1.length(), s2.length()));
```

### Method Reference Style

```java
// Comparator factory methods — preferred
list.sort(Comparator.comparing(String::length));
list.sort(Comparator.comparingInt(String::length));
```

### Employee Sorting Examples

```java
List<Employee> employees = getEmployees();

// By name
employees.sort(Comparator.comparing(Employee::getName));

// By salary
employees.sort(Comparator.comparingInt(Employee::getSalary));

// By salary descending
employees.sort(Comparator.comparingInt(Employee::getSalary).reversed());

// Multi-field chain
employees.sort(
    Comparator.comparing(Employee::getDepartment)
              .thenComparingInt(Employee::getSalary)
              .reversed()
);
```

### Comparator Factory Methods

```java
Comparator.comparing(Employee::getName);           // Comparable field
Comparator.comparingInt(Employee::getSalary);      // int field
Comparator.comparingDouble(Employee::getRating);     // double field
Comparator.comparing(Employee::getName, String.CASE_INSENSITIVE_ORDER);
```

### Stream Integration

```java
employees.stream()
         .sorted(Comparator.comparingInt(Employee::getSalary).reversed())
         .limit(10)
         .forEach(System.out::println);
```

### Before vs After Java 8

```java
// ❌ Old — anonymous class
Comparator<Employee> comp = new Comparator<Employee>() {
    public int compare(Employee e1, Employee e2) {
        return e1.getName().compareTo(e2.getName());
    }
};

// ✅ Java 8+ — lambda
Comparator<Employee> comp =
        (e1, e2) -> e1.getName().compareTo(e2.getName());

// ✅ Best — method reference
Comparator<Employee> comp =
        Comparator.comparing(Employee::getName);
```

### Comparator as Field (Reusable)

```java
public class EmployeeService {

    private static final Comparator<Employee> BY_SALARY =
            Comparator.comparingInt(Employee::getSalary);

    public List<Employee> topEarners(List<Employee> list) {
        return list.stream()
                   .sorted(BY_SALARY.reversed())
                   .limit(5)
                   .collect(Collectors.toList());
    }
}
```

**Interview Point:**

> Prefer `Comparator.comparing()` / `comparingInt()` over manual lambdas. Chain `thenComparing()` for multi-field sorts. Store reusable comparators as `static final` constants.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Can compareTo() return any negative/positive integer?

<details>
<summary>Show Answer</summary>

**Answer:** Yes—any negative (not just -1) means less, any positive means greater. Only zero means equal for ordering. Use `Integer.compare()` for safety.

</details>

---

### Q: What happens if compareTo() is inconsistent with equals()?

<details>
<summary>Show Answer</summary>

**Answer:** `TreeSet`/`TreeMap` may behave incorrectly—objects equal by `equals()` may both be stored if `compareTo()` returns non-zero. Always keep them consistent.

</details>

---

### Q: Comparator.comparing() vs Collections.sort() with Comparable?

<details>
<summary>Show Answer</summary>

**Answer:** `Collections.sort(list)` uses natural order (`Comparable`). `list.sort(Comparator.comparing(...))` uses custom order without changing the class.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> `Comparable` defines one natural order inside the class via `compareTo()`. `Comparator` provides flexible external sorting via `compare()`—use `Comparator.comparing()` and `thenComparing()` chains for production-grade multi-field sorting.

</details>
