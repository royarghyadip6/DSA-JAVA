# Phase 1 — Language Foundations

**Duration:** 2 weeks | **Goal:** Master lambdas, functional interfaces, method references, and interface evolution

[← Phase 0](00-prerequisites.md) | [Index](README.md) | **Next:** [Phase 2 — Stream API Core →](02-stream-api-core.md)

---

## Theory (Easy to Remember)

### 1. Lambda Expressions

- A **lambda** is a **short block of code** that implements a **functional interface** (interface with exactly one abstract method).
- Syntax: `(parameters) -> expression` or `(parameters) -> { statements; }`
- If one parameter and no type: `x -> x * 2` (parentheses optional)
- The compiler figures out the type from **context** (target typing) — where the lambda is used.
- Lambdas can use **local variables** only if they are **effectively final** (not reassigned after capture).
- Lambdas can access **instance fields** and **static fields** freely.
- **`this` inside lambda** refers to the enclosing class, NOT the anonymous class instance (unlike anonymous inner class).

**Memory trick:** *"Lambda = function as data. Pass behavior like you pass values."*

| Old (anonymous class)                                                | New (lambda)                     |
|----------------------------------------------------------------------|----------------------------------|
| `new Runnable() { public void run() { System.out.println("Hi"); } }` | `() -> System.out.println("Hi")` |

---

### 2. Functional Interfaces

- **SAM** = Single Abstract Method interface.
- `@FunctionalInterface` = compiler checks only one abstract method (optional but recommended).
- Java 8 adds **43 built-in** functional interfaces in `java.util.function`.

| Interface           | Input   | Output  | Plain English                    |
|---------------------|---------|---------|----------------------------------|
| `Supplier<T>`       | nothing | T       | "Give me a value"                |
| `Consumer<T>`       | T       | void    | "Do something with this"         |
| `Function<T,R>`     | T       | R       | "Transform T to R"               |
| `Predicate<T>`      | T       | boolean | "Test if true/false"             |
| `BiFunction<T,U,R>` | T, U    | R       | "Combine two into one"           |
| `UnaryOperator<T>`  | T       | T       | "T in, T out" (extends Function) |
| `BinaryOperator<T>` | T, T    | T       | "Two T's in, T out"              |

**Primitive specializations** (`IntPredicate`, `ToIntFunction`, etc.) avoid **boxing** overhead — use them with primitive streams.

**Composition methods:**
- `Predicate`: `and`, `or`, `negate`
- `Function`: `andThen`, `compose`
- `Consumer`: `andThen`

---

### 3. Method References

Shorthand when lambda only calls an existing method.

| Type               | Syntax                        | When to Use                         |
|--------------------|-------------------------------|-------------------------------------|
| Static             | `Integer::parseInt`           | `s -> Integer.parseInt(s)`          |
| Instance on object | `System.out::println`         | `x -> System.out.println(x)`        |
| Instance on type   | `String::compareToIgnoreCase` | `(a,b) -> a.compareToIgnoreCase(b)` |
| Constructor        | `ArrayList::new`              | `() -> new ArrayList<>()`           |

**Memory trick:** *"If lambda only forwards to one method, use `::`."*

---

### 4. Default & Static Methods in Interfaces

- **`default`** = method with body in interface. Implementing classes inherit it. **Does not break** old code.
- **`static`** = utility method on interface (e.g., `Comparator.comparing()`).
- **Diamond problem:** Class implements two interfaces with same default method → class **must override** and choose.
- Resolution order: class method > parent interface default > conflict (must override).

**Why added:** Add `stream()`, `forEach()` to `Collection` without breaking millions of existing implementations.

---

### 5. `@FunctionalInterface` and Related

- Put on your custom SAM interfaces for documentation + compile-time safety.
- Compiler error if you add a second abstract method.

---

## Examples

### Lambda basics

```java
// Runnable
Runnable task = () -> System.out.println("Running in: " + Thread.currentThread().getName());
new Thread(task).start();

// Comparator
List<String> names = Arrays.asList("Charlie", "Alice", "Bob");
names.sort((a, b) -> a.compareToIgnoreCase(b));
names.sort(String::compareToIgnoreCase);
```

### Functional interface composition

```java
Predicate<String> notEmpty = s -> s != null && !s.isEmpty();
Predicate<String> shortEnough = s -> s.length() <= 10;
Predicate<String> valid = notEmpty.and(shortEnough);

Function<Integer, Integer> times2 = x -> x * 2;
Function<Integer, Integer> plus3 = x -> x + 3;
Function<Integer, Integer> times2ThenPlus3 = times2.andThen(plus3); // (x*2)+3
```

### Custom functional interface

```java
@FunctionalInterface
interface DiscountCalculator {
    double apply(double price, int quantity);
}

DiscountCalculator tenPercentOff = (price, qty) -> price * qty * 0.9;
double total = tenPercentOff.apply(100.0, 5); // 450.0
```

### Default method — API evolution

```java
interface PaymentGateway {
    void pay(double amount);           // existing abstract method

    default void payWithReceipt(double amount) {  // new in "v2" of interface
        pay(amount);
        System.out.println("Receipt issued for: " + amount);
    }
}
```

### Diamond problem resolution

```java
interface A { default void hello() { System.out.println("A"); } }
interface B { default void hello() { System.out.println("B"); } }

class C implements A, B {
    @Override
    public void hello() {
        A.super.hello(); // explicitly call A's default
    }
}
```

---

## Practice Exercises

| #  | Exercise                                                                                                  | Difficulty |
|----|-----------------------------------------------------------------------------------------------------------|------------|
| 1  | Rewrite 5 anonymous inner classes as lambdas (`Runnable`, `Comparator`, `ActionListener`-style interface) | Easy       |
| 2  | Create `Predicate<String>` that checks valid email format (simple: contains `@` and `.`)                  | Easy       |
| 3  | Chain two `Function`s: parse String to Integer, then square it                                            | Easy       |
| 4  | Use method reference to sort `List<Employee>` by name, then by salary                                     | Medium     |
| 5  | Design `@FunctionalInterface EventHandler<T>` and use it in a mini event bus                              | Medium     |
| 6  | Interface `Repository<T>` with default method `saveAll(List<T>)` calling abstract `save(T)`               | Medium     |
| 7  | Resolve diamond problem with two conflicting default methods                                              | Medium     |
| 8  | Replace boxing lambda with primitive specialization: sum `List<Integer>` using `ToIntFunction`            | Hard       |
| 9  | Build a validator using `Predicate.and()` for user registration (name, age, email)                        | Hard       |
| 10 | Explain (in comments) why `this` differs in lambda vs anonymous class — prove with code                   | Hard       |

### Exercise 3 — Sample Solution

```java
Function<String, Integer> parse = Integer::parseInt;
Function<Integer, Integer> square = x -> x * x;
Function<String, Integer> parseAndSquare = parse.andThen(square);
int result = parseAndSquare.apply("12"); // 144
```

---

## Interview Q&A (5–8 Years Experience)

### Q1. Is lambda just syntactic sugar for anonymous inner classes?

**Answer:** **No, not exactly.** Lambdas are implemented via **`invokedynamic`** and **`LambdaMetafactory`**, not by generating a new `.class` file per lambda (in most cases). Benefits:
- Less bytecode
- No synthetic `this$0` outer reference issues
- Better performance for repeated creation

Anonymous classes still create a separate class file and can capture `this` differently.

---

### Q2. What is "effectively final" and why does it exist?

**Answer:** A variable is **effectively final** if it is never reassigned after initialization. Lambdas can capture such variables. If reassignment were allowed, the lambda might see changing values across threads — confusing and unsafe. Instance/static fields don't have this restriction because their mutability is explicit.

```java
int x = 10;
Runnable r = () -> System.out.println(x); // OK
// x = 20; // Would make capture illegal
```

---

### Q3. When can you NOT use a lambda?

**Answer:**
- Interface has **more than one abstract method** (not a functional interface)
- Need to override **Object methods** like `equals`/`hashCode` on the anonymous type itself
- Need **multiple methods** in one anonymous class
- Abstract class (not interface) — lambdas only target functional interfaces
- Serialization edge cases requiring specific anonymous class identity (rare)

---

### Q4. Explain the four types of method references with examples.

**Answer:**
1. **Static:** `Integer::parseInt` ↔ `s -> Integer.parseInt(s)`
2. **Bound instance:** `System.out::println` ↔ `x -> System.out.println(x)`
3. **Unbound instance:** `String::toLowerCase` ↔ `s -> s.toLowerCase()`
4. **Constructor:** `ArrayList::new` ↔ `() -> new ArrayList<>()` or `ArrayList::new` for `Function<Integer, ArrayList>` with size (use `n -> new ArrayList<>(n)`)

---

### Q5. Why were default methods added to interfaces? Doesn't it break the "interface vs abstract class" design?

**Answer:** To **evolve JDK APIs** without breaking backward compatibility. `Collection.stream()` added to interface — all existing `ArrayList`, `HashSet` etc. got it automatically. Trade-off: interfaces now can have state via default methods (rare) and multiple inheritance of behavior (diamond problem). Rule of thumb: defaults for backward-compatible API evolution; not for heavy implementation sharing (use composition or abstract class).

---

### Q6. [How do you resolve the diamond problem?](https://www.geeksforgeeks.org/java/diamond-problem-in-java/)

**Answer:** Class must **override** the conflicting method. Inside override:
- `InterfaceName.super.methodName()` to call specific default
- Or provide completely new implementation

Most specific rule: class wins over interface; subinterface default wins over superinterface if only one path.

---

### Q7. Difference between `Function.identity()` and `x -> x`?

**Answer:** Functionally identical. `Function.identity()` returns a **pre-built singleton** reference — slightly cleaner and can be reused. Prefer it in streams: `.map(Function.identity())` or `.filter(Objects::nonNull)`.

---

### Q8. What is target typing? Give an example where the same lambda has different meanings.

**Answer:** Compiler infers functional interface type from **assignment context**:

```java
// Same lambda body, different target types:
Predicate<String> isEmpty = s -> s.isEmpty();      // boolean return
Function<String, Integer> length = s -> s.length();  // int return — won't compile with same lambda if types differ
```

The lambda's parameter and return types must match the **single abstract method** of the target interface.

---

### Q9. Can a functional interface have `default`, `static`, and `Object` methods?

**Answer:** **Yes.** Only **one abstract method** is required. It can also have:
- Multiple `default` methods
- Multiple `static` methods
- Methods from `Object` (`equals`, `hashCode`, `toString`) — these don't count toward abstract method limit

---

### Q10. How do primitive specializations help performance?

**Answer:** `Stream<Integer>` boxes every `int` to `Integer`. `IntStream` avoids boxing/unboxing. On millions of elements, GC pressure and CPU overhead matter. Use `mapToInt`, `IntPredicate`, `IntSupplier` etc. when working with primitives at scale.

---

## Self-Check

- [ ] I can convert any anonymous SAM class to lambda in under 30 seconds
- [ ] I know when to use `::` vs lambda
- [ ] I can compose Predicates and Functions
- [ ] I can explain default methods and diamond problem to a junior dev

**Next:** [Phase 2 — Stream API Core →](02-stream-api-core.md)
