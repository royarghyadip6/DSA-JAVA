# 15.1 Functional Interface

## Functional Interface

### Very Frequently Asked

---

# 1. What is Functional Interface?

<details>
<summary>Show Answer</summary>

**Answer:**

A **functional interface** is an interface with **exactly one abstract method**—the target type for **lambda expressions** and **method references**.

### Definition

```java
@FunctionalInterface
public interface Runnable {
    void run();  // single abstract method
}
```

### Key Characteristics

| Rule | Detail |
|------|--------|
| Abstract methods | **Exactly one** |
| Default methods | Allowed (multiple) |
| Static methods | Allowed (multiple) |
| Object methods | `equals()`, `hashCode()`, `toString()` don't count |

### Example

```java
@FunctionalInterface
interface Calculator {
    int calculate(int a, int b);
}

// Lambda implements the abstract method
Calculator add = (a, b) -> a + b;
Calculator mul = (a, b) -> a * b;

System.out.println(add.calculate(5, 3)); // 8
```

### Relationship with Lambda

```text
Functional Interface  → defines WHAT to do (method signature)
Lambda Expression     → defines HOW to do it (implementation)
```

```java
Runnable task = () -> System.out.println("Running");
// Runnable = functional interface
// () -> ...  = lambda implementation of run()
```

### Before vs After Java 8

```java
// Before — anonymous class
Runnable r = new Runnable() {
    public void run() { System.out.println("Hello"); }
};

// After — lambda on functional interface
Runnable r = () -> System.out.println("Hello");
```

### Common Functional Interfaces

```java
Predicate<T>    // T → boolean
Function<T,R>   // T → R
Consumer<T>     // T → void
Supplier<T>      // → T
Comparator<T>    // (T,T) → int
Runnable         // → void
Callable<V>      // → V
```

**Interview Point:**

> Functional interface = **one abstract method**. It's the bridge between lambda syntax and real Java types. Without it, lambda has no target type.

</details>

---

# 2. Why only one abstract method?

<details>
<summary>Show Answer</summary>

**Answer:**

A functional interface allows **only one abstract method** because a **lambda expression represents a single function**—one behavior, one method implementation.

### Core Reason

```text
Lambda has NO method name
    ↓
Compiler must know WHICH method to implement
    ↓
Only ONE abstract method = no ambiguity
```

### Ambiguity Problem (If Multiple Abstract Methods)

```java
interface TwoMethods {
    void methodA();
    void methodB();
}

// ❌ Which method does this lambda implement?
TwoMethods t = () -> System.out.println("Hi");
// Compile error — ambiguous
```

### With One Abstract Method — Clear Target

```java
interface OneMethod {
    void methodA();
    void methodB() { } // default — not abstract
}

OneMethod t = () -> System.out.println("Hi"); // ✅ clearly methodA
```

### What Does NOT Count as Abstract

```java
@FunctionalInterface
interface Valid {
    void doWork();           // 1 abstract ✅

    default void helper() { } // default — not counted
    static void util() { }    // static — not counted

  // Object methods — inherited, not counted
  // boolean equals(Object o)
  // int hashCode()
  // String toString()
}
```

### SAM Type (Single Abstract Method)

```text
Functional Interface = SAM Type
SAM = Single Abstract Method
```

### Historical Context

```text
Java 8 goal: add lambdas without breaking existing code
Solution: any interface with ONE abstract method can use lambda
Existing interfaces (Runnable, Callable, Comparator) already fit!
```

### Multiple Abstract Methods = Regular Interface

```java
interface Regular {
    void a();
    void b();
}
// Cannot use lambda — must use class implementation
```

**Interview Point:**

> One abstract method = **no ambiguity** for lambda. Multiple abstract methods → compiler can't map lambda to a method. SAM type is the foundation of Java's lambda design.

</details>

---

# 3. @FunctionalInterface annotation?

<details>
<summary>Show Answer</summary>

**Answer:**

`@FunctionalInterface` is a **marker annotation** that tells the compiler to verify the interface has **exactly one abstract method**.

### Usage

```java
@FunctionalInterface
interface MyFunc {
    void execute();
}
```

### Purpose

| Purpose | Detail |
|---------|--------|
| Compile-time check | Enforces single abstract method rule |
| Documentation | Signals intent to developers |
| Not required | Interface works without it if rule is met |
| Recommended | Always use for clarity and safety |

### Compiler Enforcement

```java
@FunctionalInterface
interface Broken {
    void methodA();
    void methodB(); // ❌ compile error — not a functional interface
}
```

### Without Annotation — Silent Risk

```java
// No annotation — still functional today
interface Risky {
    void methodA();
}

// Someone adds method later — breaks all lambdas!
interface Risky {
    void methodA();
    void methodB(); // now NOT functional — all lambdas break
}
```

```java
// With annotation — immediate compile error on second method
@FunctionalInterface
interface Safe {
    void methodA();
    void methodB(); // ❌ caught at compile time
}
```

### Annotation Details

```java
@FunctionalInterface  // java.lang.FunctionalInterface
// Target: TYPE (interfaces only)
// Retention: RUNTIME
// Not inherited
```

### Valid Examples

```java
@FunctionalInterface
interface A {
    void run();
    default void help() { }
    static void util() { }
}

@FunctionalInterface
interface B {
    String process(String input);
    boolean equals(Object o); // Object method — OK
}
```

### Invalid Examples

```java
@FunctionalInterface
interface C {
    void a();
    void b(); // ❌ two abstract
}

@FunctionalInterface
abstract class D { // ❌ only for interfaces
    void a();
}
```

**Interview Point:**

> `@FunctionalInterface` is **optional but recommended**—compiler enforces SAM rule and prevents accidental breaking changes when someone adds a second abstract method.

</details>

---

### Java Built-in Interfaces

---

# 4. Predicate

<details>
<summary>Show Answer</summary>

**Answer:**

`Predicate<T>` tests a value and returns **`boolean`**—used for filtering and conditions.

### Definition

```java
interface Predicate<T> {
    boolean test(T t);
}
```

### Signature

```text
Input:  T
Output: boolean
Use:    Test / filter / validate
```

### Examples

```java
Predicate<String> notEmpty = s -> !s.isEmpty();
Predicate<Integer> isEven = n -> n % 2 == 0;
Predicate<Employee> isSenior = e -> e.getYears() > 5;

System.out.println(notEmpty.test("hello")); // true
System.out.println(isEven.test(4));         // true
```

### Stream Usage

```java
list.stream()
    .filter(x -> x > 10)           // lambda
    .filter(n -> n % 2 == 0)       // same

Predicate<Integer> gt10 = n -> n > 10;
list.stream().filter(gt10);
```

### Chaining Predicates

```java
Predicate<String> notEmpty = s -> !s.isEmpty();
Predicate<String> startsWithA = s -> s.startsWith("A");

Predicate<String> combined = notEmpty.and(startsWithA);
// or: notEmpty.or(startsWithA)
// or: notEmpty.negate()
```

### Real Production Use

```java
// Validation
Predicate<String> validEmail = email ->
    email != null && email.contains("@");

// Security filter
Predicate<User> isAdmin = user ->
    user.getRoles().contains("ADMIN");

// Stream filter
orders.stream()
      .filter(o -> o.getAmount() > 1000)
      .filter(o -> o.getStatus().equals("PENDING"));
```

### Primitive Specialization

```java
IntPredicate   // int → boolean (no boxing)
LongPredicate
DoublePredicate
```

**Interview Point:**

> `Predicate` = **test** — `T → boolean`. Used in `filter()`, validation, and conditional logic. Chain with `and()`, `or()`, `negate()`.

</details>

---

# 5. Function

<details>
<summary>Show Answer</summary>

**Answer:**

`Function<T, R>` **transforms** an input of type `T` into a result of type `R`.

### Definition

```java
interface Function<T, R> {
    R apply(T t);
}
```

### Signature

```text
Input:  T
Output: R
Use:    Transform / map / convert
```

### Examples

```java
Function<String, Integer> length = s -> s.length();
Function<Integer, String> toStr = n -> "Number: " + n;
Function<String, String> upper = s -> s.toUpperCase();

System.out.println(length.apply("Java"));  // 4
System.out.println(upper.apply("hello"));  // HELLO
```

### Stream Usage

```java
list.stream()
    .map(s -> s.length())        // lambda
    .map(String::length)         // method reference

Function<String, Integer> len = String::length;
list.stream().map(len);
```

### Chaining Functions

```java
Function<String, String> trim = s -> s.trim();
Function<String, String> upper = s -> s.toUpperCase();

Function<String, String> trimThenUpper = trim.andThen(upper);
// or: upper.compose(trim)

trimThenUpper.apply("  hello  "); // "HELLO"
```

### Real Production Use

```java
// DTO mapping
Function<User, UserDTO> toDTO = user ->
    new UserDTO(user.getId(), user.getName());

// Config lookup
Function<String, Integer> parsePort = Integer::parseInt;

// Stream transform
employees.stream()
         .map(Employee::getName)
         .map(String::toUpperCase)
         .collect(Collectors.toList());
```

### Specializations

```java
IntFunction<R>     // int → R
ToIntFunction<T>   // T → int
BiFunction<T,U,R>  // (T, U) → R
```

**Interview Point:**

> `Function` = **transform** — `T → R`. Used in `map()`, DTO conversion, and data transformation pipelines.

</details>

---

# 6. Consumer

<details>
<summary>Show Answer</summary>

**Answer:**

`Consumer<T>` **accepts** an input of type `T` and returns **nothing** (`void`)—used for actions/side effects.

### Definition

```java
interface Consumer<T> {
    void accept(T t);
}
```

### Signature

```text
Input:  T
Output: void (nothing)
Use:    Act on / consume / side effect
```

### Examples

```java
Consumer<String> print = s -> System.out.println(s);
Consumer<String> log = s -> logger.info(s);
Consumer<Order> process = order -> orderService.process(order);

print.accept("Hello");
process.accept(order);
```

### Stream Usage

```java
list.forEach(item -> System.out.println(item)); // lambda
list.forEach(System.out::println);              // method ref

Consumer<String> printer = System.out::println;
list.forEach(printer);
```

### Chaining Consumers

```java
Consumer<String> print = s -> System.out.println(s);
Consumer<String> log = s -> logger.info(s);

Consumer<String> both = print.andThen(log);
both.accept("test"); // prints AND logs
```

### Real Production Use

```java
// Event handling
Consumer<Order> onOrderCreated = order -> {
    sendEmail(order);
    updateInventory(order);
};

// Optional
optional.ifPresent(user -> sendWelcomeEmail(user));

// Stream terminal
errors.forEach(error -> log.error(error.getMessage()));

// Collection iteration
employees.forEach(e -> e.setProcessed(true));
```

### BiConsumer

```java
BiConsumer<T, U>  // (T, U) → void
Map.forEach((key, value) -> System.out.println(key + "=" + value));
```

**Interview Point:**

> `Consumer` = **consume** — `T → void`. Used in `forEach()`, logging, processing. Takes input, produces no return value.

</details>

---

# 7. Supplier

<details>
<summary>Show Answer</summary>

**Answer:**

`Supplier<T>` **provides** a value of type `T`—takes **no input**, returns a result.

### Definition

```java
interface Supplier<T> {
    T get();
}
```

### Signature

```text
Input:  none
Output: T
Use:    Provide / supply / lazy create
```

### Examples

```java
Supplier<String> greeting = () -> "Hello World";
Supplier<Double> random = () -> Math.random();
Supplier<List<String>> listSupplier = () -> new ArrayList<>();

System.out.println(greeting.get()); // Hello World
System.out.println(random.get());   // 0.723...
```

### Lazy Evaluation with Optional

```java
// orElse — default always computed
String name = optional.orElse(computeExpensiveDefault());

// orElseGet with Supplier — lazy, only if empty
String name = optional.orElseGet(() -> computeExpensiveDefault());
```

### Real Production Use

```java
// Lazy initialization
Supplier<Connection> connSupplier = () -> dataSource.getConnection();

// Factory pattern
Supplier<Employee> employeeFactory = () -> new Employee();

// Stream generation
Stream.generate(() -> Math.random()).limit(5);

// Config default
Supplier<Integer> defaultTimeout = () -> config.getInt("timeout", 30);
```

### Supplier vs Factory

```java
// Supplier — simple, no params
Supplier<Date> now = Date::new;

// Used in lazy patterns
private Supplier<ExpensiveObject> lazy =
    () -> new ExpensiveObject();
```

### Primitive Specializations

```java
IntSupplier      // → int
LongSupplier     // → long
DoubleSupplier   // → double
BooleanSupplier  // → boolean
```

**Interview Point:**

> `Supplier` = **supply** — `() → T`. No input, returns value. Used for lazy defaults (`orElseGet`), factories, and `Stream.generate()`.

</details>

---

# 8. UnaryOperator

<details>
<summary>Show Answer</summary>

**Answer:**

`UnaryOperator<T>` is a **specialized `Function`** where input and output types are the **same**—`T → T`.

### Definition

```java
interface UnaryOperator<T> extends Function<T, T> {
    // inherits R apply(T t) where R = T
}
```

### Signature

```text
Input:  T
Output: T (same type)
Use:    Transform T to T
```

### Examples

```java
UnaryOperator<String> toUpper = s -> s.toUpperCase();
UnaryOperator<Integer> doubleIt = n -> n * 2;
UnaryOperator<List<String>> sort = list -> {
    list.sort(String::compareTo);
    return list;
};

System.out.println(toUpper.apply("java"));  // JAVA
System.out.println(doubleIt.apply(5));        // 10
```

### Stream Usage

```java
Stream.of("a", "b", "c")
      .map(s -> s.toUpperCase())  // Function<String, String>
      .map(String::toUpperCase)   // UnaryOperator<String>
```

### Real Production Use

```java
// String normalization
UnaryOperator<String> normalize = s ->
    s.trim().toLowerCase();

// Math on same type
UnaryOperator<BigDecimal> addTax = amount ->
    amount.multiply(BigDecimal.valueOf(1.18));

// Identity function
UnaryOperator<String> identity = UnaryOperator.identity();
// returns input unchanged
```

### vs Function

```java
Function<String, String>  f = s -> s.toUpperCase(); // general
UnaryOperator<String>     u = s -> s.toUpperCase(); // same types only
```

**Interview Point:**

> `UnaryOperator<T>` = `Function<T, T>`. Same input and output type. Use when transforming a value within the same type domain.

</details>

---

# 9. BinaryOperator

<details>
<summary>Show Answer</summary>

**Answer:**

`BinaryOperator<T>` is a **specialized `BiFunction`** where all types are the **same**—combines two `T` values into one `T`.

### Definition

```java
interface BinaryOperator<T> extends BiFunction<T, T, T> {
    // T apply(T t1, T t2)
}
```

### Signature

```text
Input:  T, T
Output: T
Use:    Combine / reduce / merge two values
```

### Examples

```java
BinaryOperator<Integer> sum = (a, b) -> a + b;
BinaryOperator<String> concat = (a, b) -> a + b;
BinaryOperator<Integer> max = Integer::max;

System.out.println(sum.apply(10, 20));    // 30
System.out.println(concat.apply("Hi", "!")); // Hi!
```

### Stream reduce()

```java
List<Integer> nums = Arrays.asList(1, 2, 3, 4);

int total = nums.stream()
    .reduce(0, (a, b) -> a + b);           // lambda
    .reduce(0, Integer::sum);              // method ref

String joined = words.stream()
    .reduce("", (a, b) -> a + b);
```

### Real Production Use

```java
// Merge maps
BinaryOperator<Map<String, Integer>> mergeMaps = (m1, m2) -> {
    m1.putAll(m2);
    return m1;
};

// Comparator max
BinaryOperator<Employee> higherSalary = (e1, e2) ->
    e1.getSalary() > e2.getSalary() ? e1 : e2;

// Collectors
Collectors.reducing(0, Integer::sum);
```

### Built-in BinaryOperators

```java
BinaryOperator.minBy(Comparator.naturalOrder())
BinaryOperator.maxBy(Comparator.naturalOrder())
```

### vs BiFunction

```java
BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
BinaryOperator<Integer> addOp = (a, b) -> a + b; // same types all around
```

**Interview Point:**

> `BinaryOperator<T>` = `(T, T) → T`. Used in `reduce()`, merging, and combining two values of the same type.

</details>

---

### Scenario Questions

---

# 10. When would you use Predicate?

<details>
<summary>Show Answer</summary>

**Answer:**

Use `Predicate` whenever you need to **test a condition** and get a **boolean** result—filtering, validation, authorization checks.

### Use Case 1 — Stream Filtering

```java
List<Employee> seniors = employees.stream()
    .filter(e -> e.getYearsOfService() > 5)  // Predicate
    .collect(Collectors.toList());

Predicate<Employee> isSenior = e -> e.getYearsOfService() > 5;
employees.stream().filter(isSenior);
```

### Use Case 2 — Validation

```java
Predicate<String> validEmail = email ->
    email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");

Predicate<String> validPhone = phone ->
    phone != null && phone.length() == 10;

if (validEmail.test(userInput)) {
    saveUser(email);
}
```

### Use Case 3 — Authorization / Security

```java
Predicate<User> canAccessAdmin = user ->
    user.hasRole("ADMIN") || user.hasRole("SUPER_USER");

if (canAccessAdmin.test(currentUser)) {
    showAdminPanel();
}
```

### Use Case 4 — RemoveIf (Collection)

```java
List<String> list = new ArrayList<>(names);
list.removeIf(s -> s.isEmpty());           // Predicate
list.removeIf(s -> s.startsWith("temp_"));
```

### Use Case 5 — Configurable Business Rules

```java
Map<String, Predicate<Order>> rules = Map.of(
    "highValue",  o -> o.getAmount() > 10000,
    "international", o -> !o.getCountry().equals("IN"),
    "urgent",     o -> o.isPriority()
);

Predicate<Order> rule = rules.get("highValue");
if (rule.test(order)) applyDiscount(order);
```

### Use Case 6 — Chained Conditions

```java
Predicate<Employee> eligible = isActive
    .and(hasValidContract)
    .and(salaryAbove(50000))
    .and(not(onLeave));

List<Employee> bonusList = employees.stream()
    .filter(eligible)
    .collect(Collectors.toList());
```

### Decision Guide

| Need | Use |
|------|-----|
| Test condition → boolean | `Predicate` |
| Transform T → R | `Function` |
| Act on T, no return | `Consumer` |
| Provide T, no input | `Supplier` |

**Interview Point:**

> Use `Predicate` for **filter**, **validation**, **authorization**, and **removeIf**. Chain with `and()`, `or()`, `negate()` for complex rules.

</details>

---

# 11. Function vs Consumer?

<details>
<summary>Show Answer</summary>

**Answer:**

`Function` **transforms** input to output; `Consumer` **acts on** input with **no return**.

### Quick Comparison

| | `Function<T, R>` | `Consumer<T>` |
|---|------------------|---------------|
| Method | `R apply(T t)` | `void accept(T t)` |
| Input | T | T |
| Output | **R** (returns value) | **void** (nothing) |
| Purpose | Transform / map | Act / side effect |
| Stream op | `map()` | `forEach()` |
| Memory | **F**unction = **F**orward (gives back) | **C**onsumer = **C**onsumes (takes only) |

### Function — Transform and Return

```java
Function<String, Integer> length = s -> s.length();
Integer result = length.apply("Java"); // returns 4

employees.stream()
    .map(Employee::getName)     // Function — returns new value
    .map(String::toUpperCase)   // Function — returns transformed
    .collect(Collectors.toList());
```

### Consumer — Act, No Return

```java
Consumer<String> printer = s -> System.out.println(s);
printer.accept("Hello"); // prints, returns nothing

employees.forEach(e -> sendEmail(e));  // Consumer — side effect
errors.forEach(System.out::println);   // Consumer — no return
```

### Side-by-Side Example

```java
List<String> names = Arrays.asList("alice", "bob");

// Function — transform, use result
List<String> upper = names.stream()
    .map(s -> s.toUpperCase())  // Function: String → String
    .collect(Collectors.toList());
// Result: [ALICE, BOB]

// Consumer — act, no result
names.forEach(s -> System.out.println(s)); // Consumer: void
// Prints each name — no collection returned
```

### Production Scenario

```java
// Function — DTO mapping (need return value)
Function<User, UserDTO> mapper = user ->
    new UserDTO(user.getId(), user.getName());
UserDTO dto = mapper.apply(user);

// Consumer — logging (no return needed)
Consumer<Order> logger = order ->
    auditLog.info("Order processed: " + order.getId());
logger.accept(order);
```

### When to Choose

| Choose Function | Choose Consumer |
|-----------------|-----------------|
| Need transformed result | Only side effect needed |
| `map()` in streams | `forEach()` in streams |
| DTO conversion | Logging, email, DB update |
| Parsing / computing | Event notification |

**Interview Point:**

> **Function** = input → **output** (transform). **Consumer** = input → **void** (consume/act). `map` vs `forEach` in streams is the classic example.

</details>

---

# 12. Consumer vs Supplier?

<details>
<summary>Show Answer</summary>

**Answer:**

`Consumer` **takes** input and returns nothing; `Supplier` **takes nothing** and **provides** output—they are **opposites** in data flow.

### Quick Comparison

| | `Consumer<T>` | `Supplier<T>` |
|---|---------------|---------------|
| Method | `void accept(T t)` | `T get()` |
| Input | **T** (takes value) | **none** |
| Output | **void** | **T** (gives value) |
| Direction | **Inbound** — receives | **Outbound** — produces |
| Stream op | `forEach()` | `generate()` |
| Memory | **C**onsumer = **C**omes in | **S**upplier = **S**ends out |

### Visual Flow

```text
Consumer:  data ──→ accept() ──→ void (consumed)
Supplier:  void ──→ get() ──→ data (produced)
```

### Consumer — Takes and Uses

```java
Consumer<String> printer = s -> System.out.println(s);
printer.accept("Hello"); // takes "Hello", prints it

Consumer<Order> processor = order -> orderService.process(order);
processor.accept(order); // takes order, processes it
```

### Supplier — Produces and Gives

```java
Supplier<String> greeting = () -> "Hello World";
String msg = greeting.get(); // produces "Hello World"

Supplier<Double> random = () -> Math.random();
double value = random.get(); // produces random number
```

### Classic Pair — Lazy Default

```java
Optional<String> opt = findConfig("timeout");

// Consumer path — orElse (always runs default)
String val = opt.orElse(computeDefault()); // computeDefault() ALWAYS runs

// Supplier path — orElseGet (lazy)
String val = opt.orElseGet(() -> computeDefault()); // runs ONLY if empty
```

### Stream Examples

```java
// Consumer — terminal, consumes each element
list.forEach(item -> process(item));

// Supplier — generates infinite/lazy stream
Stream.generate(() -> Math.random()).limit(10);
Stream.generate(Supplier::getData).limit(5);
```

### Production Scenario

```java
// Consumer — event handler (receives event)
Consumer<OrderEvent> onOrderCreated = event -> {
    notifyWarehouse(event);
    sendConfirmationEmail(event);
};
eventBus.subscribe("order.created", onOrderCreated);

// Supplier — factory / lazy init (produces object)
Supplier<Connection> connectionSupplier = () ->
    dataSource.getConnection();

Connection conn = connectionSupplier.get(); // creates on demand
```

### When to Choose

| Choose Consumer | Choose Supplier |
|-----------------|-----------------|
| Process incoming data | Produce/create data |
| Handle events | Factory pattern |
| `forEach`, logging | `orElseGet`, lazy init |
| Write to DB/file | `Stream.generate()` |

### All Four Together (PFCS)

```text
Predicate  → T → boolean   (test)
Function   → T → R         (transform)
Consumer   → T → void      (consume IN)
Supplier   → void → T      (supply OUT)
```

**Interview Point:**

> **Consumer** = data flows **in** (accept). **Supplier** = data flows **out** (get). Opposite directions. `orElseGet(Supplier)` is the classic Supplier interview example.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Can functional interface have constructor?

<details>
<summary>Show Answer</summary>

**Answer:** No. Interfaces cannot have constructors. Functional interfaces can have `default` and `static` methods, but not constructors.

</details>

---

### Q: Is Comparator a functional interface?

<details>
<summary>Show Answer</summary>

**Answer:** Yes. `Comparator<T>` has one abstract method `compare(T o1, T o2)`. Lambda: `(a, b) -> a.compareTo(b)`.

</details>

---

### Q: Runnable vs Callable as functional interfaces?

<details>
<summary>Show Answer</summary>

**Answer:** Both are functional interfaces. `Runnable` → `void run()` (no return). `Callable<V>` → `V call()` (returns value, can throw checked exception).

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Functional interface = SAM (one abstract method). **PFCS**: Predicate tests, Function transforms, Consumer consumes, Supplier supplies. Lambdas need a functional interface target. Use `@FunctionalInterface` to enforce the rule at compile time.

</details>
