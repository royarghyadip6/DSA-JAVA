# 15. Java 8 Features

## Java 8 Features List

---

# 1. Lambda Expressions

<details>
<summary>Show Answer</summary>

### What It Is
A **short way to write a function** — replaces bulky anonymous inner classes with clean, readable code.

### Remember in One Line
> **Lambda = function without name, passed as value**

### Syntax (Memorize This)

```text
(parameters) -> expression
(parameters) -> { statements; }
```

```java
// Old way — anonymous class
Runnable r = new Runnable() {
    public void run() { System.out.println("Hello"); }
};

// Lambda way
Runnable r = () -> System.out.println("Hello");
```

### Key Points

| Point | Remember |
|-------|----------|
| Introduced in | Java 8 |
| Works with | Functional interfaces only |
| Syntax arrow | `->` separates params from body |
| No access modifiers | No `public`, no name |
| Return | Last expression auto-returned (no `{}`) |

### Parameter Styles

```java
(x) -> x * 2        // one param — brackets optional
x -> x * 2          // same
(x, y) -> x + y     // two params — brackets required
() -> 42            // no params — empty brackets
```

### Effectively Final (Must Know)

```java
int count = 10;       // effectively final
list.forEach(x -> System.out.println(count)); // ✅

count = 20;           // ❌ after this, lambda above won't compile
```

* Local variables used in lambda **cannot be changed** after capture
* Instance/static fields — can be modified

### Lambda vs Anonymous Class

| | Lambda | Anonymous Class |
|---|--------|-----------------|
| Code | Short | Long |
| `this` | Caller class | Anonymous class itself |
| Compiler | `invokedynamic` | Separate class file |
| Interface | Functional only | Any interface/class |

### Common Uses

```java
list.forEach(item -> System.out.println(item));
list.sort((a, b) -> a.compareTo(b));
threadPool.submit(() -> doWork());
stream.filter(x -> x > 10);
```

**Interview Point:** Lambda needs a **functional interface** target. Variable capture must be **effectively final**.

**Memory Trick:** **L**ambda = **L**ess code, **L**inked to one abstract method.

</details>

---

# 2. Functional Interfaces

<details>
<summary>Show Answer</summary>

### What It Is
An interface with **exactly one abstract method** — lambda's best friend.

### Remember in One Line
> **One abstract method = can use lambda**

### Rules

```text
✅ One abstract method
✅ Multiple default methods OK
✅ Multiple static methods OK
✅ Methods from Object (equals, hashCode) don't count
```

### @FunctionalInterface

```java
@FunctionalInterface
interface MyFunc {
    void doWork();  // only one abstract
}
// Compiler error if second abstract method added
```

### Built-in Functional Interfaces (Memorize Table)

| Interface | Input | Output | Use | Example |
|-----------|-------|--------|-----|---------|
| `Predicate<T>` | T | `boolean` | Test/filter | `x -> x > 10` |
| `Function<T,R>` | T | R | Transform | `s -> s.length()` |
| `Consumer<T>` | T | void | Act on item | `x -> print(x)` |
| `Supplier<T>` | none | T | Provide value | `() -> getData()` |
| `UnaryOperator<T>` | T | T | Transform same type | `x -> x * 2` |
| `BinaryOperator<T>` | T, T | T | Combine two | `(a,b) -> a+b` |

### Memory Trick — **PFCS**

```text
P = Predicate   → tests (boolean)
F = Function    → transforms (T → R)
C = Consumer    → consumes (no return)
S = Supplier    → supplies (no input)
```

### Primitive Specializations (Avoid boxing)

```java
IntPredicate    // int → boolean
IntFunction<R>  // int → R
IntConsumer     // int → void
IntSupplier     // → int
```

### Custom Functional Interface

```java
@FunctionalInterface
interface Validator<T> {
    boolean validate(T input);
}

Validator<String> notEmpty = s -> !s.isEmpty();
```

**Interview Point:** `@FunctionalInterface` is optional but recommended — compiler enforces single abstract method rule.

**Memory Trick:** **Functional = ONE job** (one abstract method).

</details>

---

# 3. Streams API

<details>
<summary>Show Answer</summary>

### What It Is
A **pipeline** to process collections — filter, map, sort, collect — in a functional, declarative style.

### Remember in One Line
> **Stream = lazy pipeline of operations on data**

### Stream vs Collection

| Collection | Stream |
|------------|--------|
| Stores data | Does NOT store data |
| Can reuse | **One-time use** |
| External iteration (`for`) | Internal iteration |
| Eager | Lazy (intermediate ops) |

### Pipeline Flow (Memorize)

```text
Source → Intermediate ops (lazy) → Terminal op (triggers execution)
```

```java
list.stream()
    .filter(x -> x > 10)      // intermediate — lazy
    .map(x -> x * 2)          // intermediate — lazy
    .collect(Collectors.toList()); // terminal — executes all
```

### Operation Types

| Type | Lazy? | Examples |
|------|-------|----------|
| **Intermediate** | Yes | `filter`, `map`, `flatMap`, `sorted`, `distinct`, `limit`, `skip` |
| **Terminal** | Triggers | `collect`, `forEach`, `reduce`, `count`, `min`, `max`, `anyMatch` |

### Creating Streams

```java
list.stream()
Arrays.stream(arr)
Stream.of(1, 2, 3)
Stream.iterate(0, n -> n + 1).limit(10)
Stream.generate(() -> Math.random()).limit(5)
```

### Key Concepts

| Concept | Meaning |
|---------|---------|
| **Lazy evaluation** | Intermediate ops run only when terminal op called |
| **Internal iteration** | Stream drives the loop, not you |
| **Not reusable** | After terminal op, stream is consumed |
| **Not parallel by default** | Use `parallelStream()` for parallel |

### map() vs flatMap()

```java
// map — 1 input → 1 output
["hello", "world"].map(s -> s.length()) → [5, 5]

// flatMap — 1 input → stream of outputs → flattened
[["a","b"], ["c"]].flatMap(list -> list.stream()) → ["a","b","c"]
```

### Common Terminal Ops

```java
.collect(Collectors.toList())
.count()
.reduce((a, b) -> a + b)
.forEach(System.out::println)
.anyMatch(x -> x > 10)
.findFirst()
```

**Interview Point:** Streams are **lazy**, **not a data structure**, and **cannot be reused**. Intermediate ops return stream; terminal op returns result.

**Memory Trick:** **S**tream = **S**ource → **S**tages (intermediate) → **S**top (terminal).

</details>

---

# 4. Method References

<details>
<summary>Show Answer</summary>

### What It Is
A **shortcut for lambda** when lambda only calls an existing method — cleaner syntax.

### Remember in One Line
> **Method reference = lambda that just calls one method**

### Syntax Forms (Memorize All 4)

| Type | Syntax | Lambda Equivalent |
|------|--------|-------------------|
| **Static** | `Class::staticMethod` | `x -> Class.staticMethod(x)` |
| **Instance (specific)** | `obj::method` | `x -> obj.method(x)` |
| **Instance (arbitrary)** | `Class::method` | `(obj, x) -> obj.method(x)` |
| **Constructor** | `Class::new` | `x -> new Class(x)` |

### Examples

```java
// Static method reference
list.forEach(System.out::println);
// same as: x -> System.out.println(x)

// Instance method (arbitrary type)
list.sort(String::compareToIgnoreCase);
// same as: (a, b) -> a.compareToIgnoreCase(b)

// Constructor reference
list.stream().map(String::new);
// same as: s -> new String(s)

Supplier<List<String>> sup = ArrayList::new;
// same as: () -> new ArrayList<>()
```

### When to Use

```text
Lambda only calls ONE existing method → use method reference
Lambda has extra logic → keep lambda
```

```java
// ✅ Good — just println
list.forEach(System.out::println);

// ❌ Keep lambda — extra logic
list.forEach(x -> System.out.println("Item: " + x));
```

### Method Reference vs Lambda

| Method Reference | Lambda |
|------------------|--------|
| Shorter | More flexible |
| Existing method only | Any logic |
| Preferred when equal | When logic differs |

**Interview Point:** Four types — **static**, **bound instance**, **unbound instance**, **constructor**. Use when lambda is a simple method call.

**Memory Trick:** **::** means "use this method instead of writing lambda".

</details>

---

# 5. Optional

<details>
<summary>Show Answer</summary>

### What It Is
A **container** that may or may not hold a value — designed to avoid `NullPointerException`.

### Remember in One Line
> **Optional = explicit "maybe null" wrapper**

### Creating Optional

```java
Optional<String> o1 = Optional.of("hello");     // value must NOT be null
Optional<String> o2 = Optional.ofNullable(str); // null OK → empty Optional
Optional<String> o3 = Optional.empty();           // explicitly empty
```

| Method | Null Input | Result |
|--------|------------|--------|
| `of(value)` | ❌ throws NPE | Optional with value |
| `ofNullable(value)` | ✅ | empty Optional |
| `empty()` | — | empty Optional |

### Key Methods (Memorize)

| Method | Purpose |
|--------|---------|
| `isPresent()` | true if value exists |
| `isEmpty()` | true if no value (Java 11+) |
| `get()` | get value — throws if empty |
| `orElse(default)` | value or default |
| `orElseGet(() -> ...)` | value or lazy default |
| `orElseThrow()` | value or throw exception |
| `ifPresent(x -> ...)` | run if present |
| `map(fn)` | transform if present |
| `flatMap(fn)` | transform to Optional |
| `filter(pred)` | keep if matches |

### orElse vs orElseGet (Important)

```java
// orElse — default ALWAYS evaluated
opt.orElse(computeExpensive()); // computeExpensive() runs even if opt has value!

// orElseGet — default only if empty (lazy)
opt.orElseGet(() -> computeExpensive()); // runs only when empty ✅
```

### Good Usage

```java
return findUser(id)
    .map(User::getName)
    .orElse("Unknown");

user.ifPresent(u -> sendEmail(u));
```

### Bad Usage (Avoid)

```java
// ❌ Don't use as field in entity
class User { Optional<String> name; } // bad

// ❌ Don't use as method parameter
void process(Optional<String> name) { } // bad

// ❌ Don't replace all null checks blindly
if (opt.isPresent()) opt.get(); // use map/ifPresent instead
```

**Interview Point:** Optional is for **return types** to signal absence. Never fields, rarely parameters. Prefer `orElseGet` over `orElse` for expensive defaults.

**Memory Trick:** **Optional = O**ptional value, not **O**bligatory null check everywhere.

</details>

---

# 6. Default Methods

<details>
<summary>Show Answer</summary>

### What It Is
Methods in an **interface with a body** — added in Java 8 so interfaces can evolve without breaking existing implementations.

### Remember in One Line
> **Default method = interface method with implementation**

### Syntax

```java
interface Vehicle {
    default void start() {
        System.out.println("Vehicle starting...");
    }

    void drive(); // still abstract
}
```

### Why Introduced

```text
Problem: Add new method to interface → all implementers must update
Solution: default method → existing classes work without change
```

### Real Example — Collection API

```java
interface List<E> {
    default void forEach(Consumer<? super E> action) {
        for (E e : this) action.accept(e);
    }
}
// ArrayList got forEach() without changing ArrayList code!
```

### Key Rules

| Rule | Detail |
|------|--------|
| Keyword | `default` (not `public default` — public is implicit) |
| Can override | Implementing class can override default |
| Not abstract | Has body |
| Static methods | Separate feature — no `default` keyword |

### Diamond Problem

```java
interface A { default void hello() { System.out.println("A"); } }
interface B { default void hello() { System.out.println("B"); } }

class C implements A, B {
    // Must override — compiler error without this
    @Override
    public void hello() {
        A.super.hello(); // or B.super.hello()
    }
}
```

### default vs abstract

| default method | abstract method |
|----------------|-----------------|
| Has body in interface | No body |
| Optional to override | Must implement |
| For evolution | For contract |

**Interview Point:** Default methods enable **backward-compatible interface evolution**. Diamond conflict → class must override and choose `InterfaceName.super.method()`.

**Memory Trick:** **Default = D**efault implementation so old code **D**oesn't break.

</details>

---

# 7. Static Methods in Interface

<details>
<summary>Show Answer</summary>

### What It Is
**Static methods inside interfaces** — utility methods belonging to the interface, not implementations.

### Remember in One Line
> **Interface static method = utility tied to the interface type**

### Syntax

```java
interface MathUtils {
    static int max(int a, int b) {
        return a > b ? a : b;
    }
}

// Call — use interface name, NOT instance
int m = MathUtils.max(10, 20);
```

### Why Introduced

```text
Before: utility class (Collections, Objects)
After:  static methods live in the interface itself
Example: Stream.of(), Comparator.comparing()
```

### Key Rules

| Rule | Detail |
|------|--------|
| Call syntax | `InterfaceName.staticMethod()` |
| Not inherited | Implementing class does NOT get static methods |
| No override | Classes cannot override interface static methods |
| Keyword | `static` — NOT `default` |
| Access | Implicitly `public` |

### Real Examples in Java

```java
List.of(1, 2, 3)              // List interface static
Stream.of("a", "b")           // Stream interface static
Comparator.comparing(String::length)
Collections.sort(list)        // Collections class (older style)
```

### static vs default in Interface

| | static | default |
|---|--------|---------|
| Keyword | `static` | `default` |
| Called on | Interface name | Instance of implementer |
| Inherited? | No | Yes (can override) |
| Purpose | Utility/helper | Default behavior for implementers |

### Cannot Do

```java
interface MyInterface {
    static void method() { }

    default void callStatic() {
        method();        // ✅ OK — inside interface
    }
}

class MyClass implements MyInterface {
    void test() {
        method();        // ❌ compile error — use MyInterface.method()
    }
}
```

**Interview Point:** Interface static methods are **not inherited**. Call with `InterfaceName.method()`. Different from `default` methods.

**Memory Trick:** **Static in interface = S**pecial utility, call with **S**interface name.

</details>

---

# 8. CompletableFuture

<details>
<summary>Show Answer</summary>

### What It Is
An enhanced `Future` for **asynchronous programming** — compose, chain, and combine async tasks without blocking threads.

### Remember in One Line
> **CompletableFuture = async task you can chain, combine, and handle errors on**

### Future vs CompletableFuture

| Future | CompletableFuture |
|--------|-------------------|
| `get()` blocks | Callback-based chaining |
| No chaining | `thenApply`, `thenCompose` |
| No combine | `allOf`, `anyOf` |
| No exception handling | `exceptionally`, `handle` |

### Creating CompletableFuture

```java
// Already completed
CompletableFuture<String> cf = CompletableFuture.completedFuture("done");

// Async supply (background thread)
CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> fetchData());

// Async run (no return)
CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> doWork());

// Custom executor
CompletableFuture.supplyAsync(() -> work(), executor);
```

### Chaining (Memorize)

```java
CompletableFuture.supplyAsync(() -> fetchUser())
    .thenApply(user -> user.getName())       // transform result
    .thenCompose(name -> fetchOrders(name))  // flatMap — returns another CF
    .thenAccept(orders -> process(orders))   // consume result
    .exceptionally(ex -> handleError(ex));   // handle failure
```

### Chaining Methods

| Method | Like Stream | Purpose |
|--------|-------------|---------|
| `thenApply(fn)` | `map` | Transform result |
| `thenCompose(fn)` | `flatMap` | Chain async ops |
| `thenAccept(consumer)` | `forEach` | Consume result |
| `thenRun(action)` | — | Run after complete |
| `exceptionally(fn)` | — | Handle error |
| `handle(fn)` | — | Handle result OR error |

### Combining Futures

```java
// Wait for both
CompletableFuture.allOf(f1, f2).join();

// First to complete wins
CompletableFuture.anyOf(f1, f2).join();

// Combine two results
f1.thenCombine(f2, (a, b) -> a + b);
```

### Blocking vs Non-Blocking

```java
String result = cf.get();        // blocks until done
String result = cf.join();       // blocks — unchecked exception
cf.thenAccept(r -> use(r));      // non-blocking callback ✅
```

### Use Cases

```text
Parallel API calls (fetch user + orders + profile)
Microservice aggregation
Async database + cache lookup
Non-blocking web handlers
```

**Interview Point:** `thenApply` = sync transform. `thenCompose` = async chain (returns another CF). Use `supplyAsync` with custom executor in production.

**Memory Trick:** **CompletableFuture = C**ompose **C**hains of **C**oncurrent tasks.

</details>

---

# 9. Date Time API

<details>
<summary>Show Answer</summary>

### What It Is
New **immutable, thread-safe** date/time classes in `java.time` package — replaces broken `Date` and `Calendar`.

### Remember in One Line
> **java.time = immutable, clear, no timezone surprises**

### Old vs New

| Old (`Date`/`Calendar`) | New (`java.time`) |
|-------------------------|-------------------|
| Mutable | **Immutable** |
| Not thread-safe | **Thread-safe** |
| Confusing API | Clear naming |
| Poor timezone support | Full timezone support |

### Core Classes (Memorize)

| Class | Holds | Example |
|-------|-------|---------|
| `LocalDate` | Date only | `2024-03-15` |
| `LocalTime` | Time only | `14:30:00` |
| `LocalDateTime` | Date + time (no zone) | `2024-03-15T14:30` |
| `ZonedDateTime` | Date + time + zone | `2024-03-15T14:30+05:30` |
| `Instant` | UTC timestamp | `2024-03-15T09:00:00Z` |
| `Duration` | Time amount (hours, mins) | `Duration.ofHours(2)` |
| `Period` | Date amount (years, months) | `Period.ofDays(7)` |

### Common Operations

```java
LocalDate today = LocalDate.now();
LocalDate tomorrow = today.plusDays(1);
LocalDate nextMonth = today.plusMonths(1);

LocalDateTime now = LocalDateTime.now();
LocalDateTime later = now.plusHours(3);

boolean before = today.isBefore(tomorrow);
```

### Formatting & Parsing

```java
LocalDate date = LocalDate.parse("2024-03-15");
String formatted = date.format(DateTimeFormatter.ISO_LOCAL_DATE);

DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy");
String s = date.format(fmt);
LocalDate parsed = LocalDate.parse("15-03-2024", fmt);
```

### Period vs Duration

| | Period | Duration |
|---|--------|----------|
| Measures | Date-based (days, months, years) | Time-based (hours, mins, secs) |
| Use with | `LocalDate` | `LocalTime`, `LocalDateTime` |
| Example | `Period.between(date1, date2)` | `Duration.between(time1, time2)` |

### Timezone

```java
ZonedDateTime india = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
ZonedDateTime utc = india.withZoneSameInstant(ZoneId.of("UTC"));
Instant instant = Instant.now(); // always UTC
```

### Convert Old Date

```java
Date old = new Date();
Instant instant = old.toInstant();
LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
```

**Interview Point:** Always prefer `java.time`. `LocalDate` for birthdays, `ZonedDateTime` for events with timezone, `Instant` for timestamps/logs.

**Memory Trick:** **L**ocal = no zone, **Z**oned = with zone, **I**nstant = UTC point in time.

</details>

---

# 10. Nashorn Engine

<details>
<summary>Show Answer</summary>

### What It Is
Java 8's **JavaScript engine** on the JVM — lets Java run JavaScript code and vice versa.

### Remember in One Line
> **Nashorn = run JavaScript inside Java (JVM)**

### Package

```java
import javax.script.*;
```

### Basic Usage

```java
ScriptEngine engine = new ScriptEngineManager()
    .getEngineByName("nashorn");

engine.eval("print('Hello from JS');");

Object result = engine.eval("1 + 2"); // returns 3
```

### Java ↔ JavaScript Interaction

```java
// Call Java from JavaScript
engine.eval("var BigDecimal = Java.type('java.math.BigDecimal');");
engine.eval("var bd = new BigDecimal('123.45');");

// Pass Java object to JS
engine.put("name", "Java");
engine.eval("print(name);"); // prints Java
```

### Use Cases (Historical)

```text
Scripting automation inside Java apps
Quick prototyping
Template processing
Legacy app scripting
```

### Important Status (Must Know for Interview)

| Version | Status |
|---------|--------|
| Java 8 | Introduced Nashorn |
| Java 11 | Nashorn deprecated |
| Java 15 | Nashorn **removed** |

### Replacement

```text
GraalVM JavaScript — external, not bundled in JDK
Use GraalVM or standalone JS engine for JS-on-JVM today
```

### Nashorn vs JVM

```text
Nashorn compiles JS to Java bytecode → runs on JVM
Not just interpretation — optimized execution
```

**Interview Point:** Nashorn was **removed in Java 15**. Know it for legacy Java 8 context; modern apps use GraalVM JS or avoid JS-in-JVM.

**Memory Trick:** **N**ashorn = **N**ot in modern JDK (**N**o longer available after Java 15).

</details>

---

# Java 8 Features — Quick Memory Cheat Sheet

<details>
<summary>Show Answer</summary>

### All 10 Features at a Glance

| # | Feature | One-Line Memory |
|---|---------|-----------------|
| 1 | Lambda | `(x) -> x * 2` — short functions |
| 2 | Functional Interface | One abstract method — PFCS |
| 3 | Streams API | Source → intermediate → terminal |
| 4 | Method References | `Class::method` — lambda shortcut |
| 5 | Optional | Maybe-null container — return types only |
| 6 | Default Methods | `default` in interface — backward compatible |
| 7 | Static in Interface | `Interface.staticMethod()` — utility |
| 8 | CompletableFuture | Async chain — thenApply / thenCompose |
| 9 | Date Time API | `java.time` — Local, Zoned, Instant |
| 10 | Nashorn | JS on JVM — removed in Java 15 |

### How They Connect

```text
Lambda  → needs → Functional Interface
Lambda  → powers → Streams, Optional, CompletableFuture
Method Reference → shortcut for Lambda
Default Methods → added forEach() to Collection
Streams → uses Collectors, Optional, method references
```

### Interview One-Liner

> Java 8 = functional shift: Lambdas on functional interfaces, Streams for data pipelines, Optional for null safety, CompletableFuture for async, java.time for dates, default/static methods for interface evolution.

</details>
