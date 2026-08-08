# 15.3 Method References

## Method References

---

# 1. What is Method Reference?

<details>
<summary>Show Answer</summary>

**Answer:**

A **method reference** is a **shortcut for a lambda expression** when the lambda only **calls an existing method**—no extra logic needed.

### Basic Idea

```java
// Lambda — calls println
Consumer<String> lambda = s -> System.out.println(s);

// Method reference — same thing, shorter
Consumer<String> ref = System.out::println;
```

### Syntax

```text
ClassName::methodName
object::methodName
ClassName::new
```

The `::` operator is the **method reference operator**.

### What It Is

| Method Reference | Lambda Equivalent |
|------------------|-------------------|
| Shortcut syntax | Full lambda form |
| References existing method | Implements functional interface |
| Same bytecode behavior | Functionally identical |

### Requirements

```text
1. Must target a functional interface
2. Referenced method signature must match functional interface
3. Lambda only calls ONE existing method — no extra logic
```

### Real Examples

```java
list.forEach(System.out::println);
list.sort(String::compareToIgnoreCase);
list.stream().map(String::toUpperCase);
Stream.of("a", "b").map(String::new);
Optional.of("hello").map(String::length);
```

### When to Use

```text
✅ Lambda only calls existing method → use method reference
❌ Lambda has extra logic → keep lambda

// ✅ Good
list.forEach(System.out::println);

// ❌ Keep lambda
list.forEach(s -> System.out.println("Item: " + s));
```

**Interview Point:**

> Method reference = **lambda shortcut** when you're just delegating to an existing method. Syntax: `::` operator.

</details>

---

# 2. Types of Method References?

<details>
<summary>Show Answer</summary>

**Answer:**

There are **four types** of method references in Java—each maps to a different lambda pattern.

### All Four Types

| # | Type | Syntax | Lambda Equivalent |
|---|------|--------|-------------------|
| 1 | **Static** | `Class::staticMethod` | `x -> Class.staticMethod(x)` |
| 2 | **Bound instance** | `obj::method` | `x -> obj.method(x)` |
| 3 | **Unbound instance** | `Class::method` | `(obj, x) -> obj.method(x)` |
| 4 | **Constructor** | `Class::new` | `x -> new Class(x)` |

### Memory Trick — **S B U C**

```text
S = Static method reference
B = Bound instance (specific object)
U = Unbound instance (arbitrary object)
C = Constructor reference
```

### Quick Examples

```java
// 1. Static
Function<String, Integer> parse = Integer::parseInt;

// 2. Bound instance (particular object)
String prefix = "ID:";
Predicate<String> starts = prefix::startsWith;

// 3. Unbound instance (arbitrary type)
Function<String, String> upper = String::toUpperCase;

// 4. Constructor
Supplier<ArrayList<String>> list = ArrayList::new;
```

### How to Identify Type

```text
Has ::new          → Constructor
Class::method      → Static OR Unbound instance
  static method?   → Static
  instance method? → Unbound instance
obj::method        → Bound instance (object on left)
```

### Functional Interface Must Match

```java
// Method signature must fit functional interface
Function<String, Integer> f = String::length;     // String → int ✅
Function<String, Integer> f = Integer::parseInt;  // String → int ✅
Consumer<String> c = System.out::println;         // String → void ✅
```

**Interview Point:**

> Four types: **static**, **bound instance**, **unbound instance**, **constructor**. Know syntax and lambda equivalent for each.

</details>

---

### Types

---

# 3. Static Method Reference

<details>
<summary>Show Answer</summary>

**Answer:**

A **static method reference** refers to a **static method** of a class—lambda passes arguments directly to the static method.

### Syntax

```text
ContainingClass::staticMethodName
```

### Lambda Equivalent

```java
// Lambda
(args) -> ContainingClass.staticMethodName(args)

// Method reference
ContainingClass::staticMethodName
```

### Examples

```java
// Parse string to int
Function<String, Integer> parse = Integer::parseInt;
// same as: s -> Integer.parseInt(s)

// Math absolute value
Function<Double, Double> abs = Math::abs;
// same as: d -> Math.abs(d)

// Print to console
Consumer<String> print = System.out::println;
// same as: s -> System.out.println(s)

// Compare strings
Comparator<String> cmp = String::compareTo;
// same as: (a, b) -> String.compareTo(a, b) — static compareTo on class
```

### Stream Usage

```java
list.stream()
    .map(String::valueOf)      // static valueOf
    .map(Integer::parseInt)    // static parseInt
    .forEach(System.out::println);
```

### Real Production Examples

```java
// Validation
Predicate<String> isBlank = String::isBlank;

// Sorting
list.sort(Comparator.comparingInt(String::length));

// Factory utility
Function<String, BigDecimal> toDecimal = BigDecimal::new;
// Note: BigDecimal::new is constructor, not static — trap question!
Function<String, Integer> toInt = Integer::valueOf; // static valueOf
```

### Identification Rule

```text
Left side of :: is a CLASS name
Method is static on that class
Arguments flow: lambda args → static method args
```

### Common Static References

| Reference | Functional Interface | Use |
|-----------|---------------------|-----|
| `Integer::parseInt` | `Function<String,Integer>` | Parse |
| `Math::max` | `BinaryOperator<Integer>` | Max of two |
| `System.out::println` | `Consumer<String>` | Print |
| `Objects::isNull` | `Predicate<Object>` | Null check |
| `Objects::nonNull` | `Predicate<Object>` | Non-null check |

**Interview Point:**

> Static method reference = `Class::staticMethod`. Lambda args are passed directly to the static method. `System.out::println` is the most common example.

</details>

---

# 4. Instance Method Reference

<details>
<summary>Show Answer</summary>

**Answer:**

An **instance method reference** calls an **instance method**—there are two subtypes: **bound** (specific object) and **unbound** (arbitrary object).

### Type A — Bound Instance (Particular Object)

References a method on a **specific, existing object**.

```text
containingObject::instanceMethodName
```

```java
String prefix = "Hello";

// Lambda
Predicate<String> lambda = s -> prefix.startsWith(s);

// Bound instance reference — prefix is fixed
Predicate<String> ref = prefix::startsWith;
```

```java
Employee manager = getManager();

// Bound to manager object
Function<Project, Boolean> canApprove = manager::canApprove;
```

### Type B — Unbound Instance (Arbitrary Object)

First lambda parameter becomes the **object** that executes the method.

```text
ContainingType::instanceMethodName
```

```java
// Lambda — s is the object, toUpperCase called on s
Function<String, String> lambda = s -> s.toUpperCase();

// Unbound instance reference
Function<String, String> ref = String::toUpperCase;
```

```java
// Comparator — first param is object1, second is object2
Comparator<String> cmp = String::compareToIgnoreCase;
// same as: (a, b) -> a.compareToIgnoreCase(b)

Function<String, Integer> len = String::length;
// same as: s -> s.length()
```

### Bound vs Unbound Comparison

| | Bound | Unbound |
|---|-------|---------|
| Syntax | `obj::method` | `Class::method` |
| Object | Fixed at creation | First lambda param |
| Lambda | `x -> obj.method(x)` | `obj -> obj.method()` |
| Example | `prefix::startsWith` | `String::toUpperCase` |

### Stream Examples

```java
// Unbound — each element calls its own method
names.stream()
     .map(String::toUpperCase)
     .map(String::trim)
     .forEach(System.out::println);

// Bound — fixed object used
StringBuilder sb = new StringBuilder();
words.forEach(sb::append); // append each word to same sb
```

### Real Production

```java
// Sort by employee name
employees.sort(Comparator.comparing(Employee::getName));

// Map to uppercase
list.stream().map(String::toUpperCase).collect(toList());

// Bound — logger fixed
Logger log = LoggerFactory.getLogger(MyClass.class);
errors.forEach(log::error);
```

**Interview Point:**

> **Bound** = specific object (`obj::method`). **Unbound** = first param is the instance (`String::length`). Unbound is more common in streams.

</details>

---

# 5. Constructor Reference

<details>
<summary>Show Answer</summary>

**Answer:**

A **constructor reference** refers to a **class constructor**—creates new objects without explicit `new` in lambda body.

### Syntax

```text
ClassName::new
```

### Lambda Equivalent

```java
// Lambda
(args) -> new ClassName(args)

// Constructor reference
ClassName::new
```

### Examples

```java
// No-arg constructor
Supplier<List<String>> listSupplier = ArrayList::new;
// same as: () -> new ArrayList<>()

// One-arg constructor
Function<String, String> stringFactory = String::new;
// same as: s -> new String(s)

// Two-arg constructor
BiFunction<String, Integer, Pair> pairFactory = Pair::new;
// same as: (k, v) -> new Pair(k, v)
```

### Array Constructor Reference

```java
// Creates String array of given size
IntFunction<String[]> arrayFactory = String[]::new;
// same as: size -> new String[size]

String[] arr = arrayFactory.apply(10); // new String[10]
```

### Stream Usage

```java
// Convert strings to Employee objects
List<Employee> employees = names.stream()
    .map(Employee::new)           // constructor ref
    .collect(Collectors.toList());

// Collect to specific collection type
Set<String> set = list.stream()
    .collect(Collectors.toCollection(HashSet::new));
```

### Constructor Matching Rules

```text
Functional interface params must match constructor params

Supplier<T>        → no-arg constructor
Function<A,R>      → one-arg constructor
BiFunction<A,B,R>  → two-arg constructor
```

### Examples by Interface

```java
Supplier<Date>       s = Date::new;           // () -> new Date()
Function<String, File> f = File::new;       // path -> new File(path)
BiFunction<String, String, Locale> l = Locale::new;
```

### Real Production

```java
// Factory pattern
Map<String, Supplier<Connection>> pools = Map.of(
    "mysql", MySQLConnection::new,
    "oracle", OracleConnection::new
);

// Stream collect to TreeSet
.collect(Collectors.toCollection(TreeSet::new));

// Thread factory
ExecutorService pool = Executors.newFixedThreadPool(
    4, Thread::new  // not common — usually custom factory
);
```

### Constructor vs Static Method Trap

```java
Integer::parseInt   // static method reference — parses String to int
Integer::new         // constructor reference — rarely used (Integer(int))
Integer::valueOf     // static method — boxing
String::new          // constructor — new String(source)
```

**Interview Point:**

> Constructor reference = `Class::new`. Params of functional interface map to constructor args. Common: `ArrayList::new`, `String::new`, `HashSet::new` in `Collectors.toCollection()`.

</details>

---

### Advanced

---

# 6. Lambda vs Method Reference?

<details>
<summary>Show Answer</summary>

**Answer:**

Method references are **syntactic shortcuts** for lambdas—they are **functionally identical** but method references are preferred when lambda only delegates to one existing method.

### Comparison Table

| Feature | Lambda | Method Reference |
|---------|--------|------------------|
| Syntax | `(x) -> x.method()` | `Class::method` |
| Flexibility | Any logic | Existing method only |
| Readability | Good for complex logic | Better for simple delegation |
| Performance | Same | Same (identical bytecode) |
| When to use | Extra logic needed | Simple method call |

### Functionally Identical

```java
Consumer<String> lambda = s -> System.out.println(s);
Consumer<String> ref    = System.out::println;

// Both compile to same invokedynamic — no performance difference
```

### When to Use Lambda

```java
// Extra logic — keep lambda
list.forEach(s -> System.out.println("Item: " + s));

// Multiple operations
list.forEach(s -> {
    log.info(s);
    process(s);
});

// Conditional logic
list.stream().filter(x -> x > 10 && x < 100);

// Method reference won't work — not a simple delegation
```

### When to Use Method Reference

```java
// Simple delegation — prefer method reference
list.forEach(System.out::println);
list.sort(String::compareToIgnoreCase);
list.stream().map(String::toUpperCase);
employees.sort(Comparator.comparing(Employee::getName));
```

### Side-by-Side Examples

```java
// Print
s -> System.out.println(s)     → System.out::println
x -> logger.error(x)           → logger::error  (bound)

// Transform
s -> s.toUpperCase()           → String::toUpperCase
s -> Integer.parseInt(s)       → Integer::parseInt

// Create
() -> new ArrayList<>()        → ArrayList::new
s -> new String(s)             → String::new

// Compare
(a, b) -> a.compareTo(b)       → String::compareTo (unbound)
```

### Readability Rule

```text
If lambda is ONLY "call this method with these args"
  → Method reference (cleaner)

If lambda has ANY extra logic
  → Keep lambda
```

### IDE Hint

Modern IDEs suggest converting lambda to method reference when possible:

```java
// IDE: "Replace lambda with method reference"
list.forEach(s -> System.out.println(s));
// becomes
list.forEach(System.out::println);
```

### Interview Trap — Not Always Shorter

```java
// Method reference — clear
Comparator.comparing(Employee::getName)

// Lambda — same but longer
Comparator.comparing(e -> e.getName())

// But for simple field access, method reference wins
```

### Performance

```text
No performance difference — both use invokedynamic
Compiler treats them equivalently
Choose based on readability, not speed
```

**Interview Point:**

> Method reference when lambda **only calls one method**. Lambda when there's **extra logic**. Same performance—readability is the only difference.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Can method reference call overloaded methods?

<details>
<summary>Show Answer</summary>

**Answer:** Yes. Compiler picks the overload that matches the functional interface signature. `Integer::valueOf` could match `valueOf(int)` or `valueOf(String)` depending on target type.

</details>

---

### Q: obj::method vs Class::method?

<details>
<summary>Show Answer</summary>

**Answer:** `obj::method` = **bound** — fixed object. `Class::method` on instance method = **unbound** — first lambda param is the instance. `Class::method` on static method = static reference.

</details>

---

### Q: Method reference to private method?

<details>
<summary>Show Answer</summary>

**Answer:** Yes, if accessible from the calling context. `this::privateMethod` works inside the same class. Cannot reference private method of another class.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Method reference = lambda shortcut using `::`. Four types: **static** (`Integer::parseInt`), **bound** (`obj::method`), **unbound** (`String::toUpperCase`), **constructor** (`ArrayList::new`). Use when lambda only delegates to one existing method.

</details>
