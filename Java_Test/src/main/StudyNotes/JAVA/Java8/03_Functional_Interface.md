# 🚀 Functional Interfaces — Deep Dive (Interview Ready)

---

## 🔹 1. What is a Functional Interface?

> A **Functional Interface** is an interface that contains **exactly one abstract method**.

### ✅ Example

```java
@FunctionalInterface
interface Add {
    int sum(int a, int b);
}
```

### 💡 Key Point:

* Can have **only one abstract method**
* Can have **multiple default/static methods**

---

## 🔹 2. Why Functional Interfaces Exist?

They were introduced to support:

> **Lambda Expressions**

### 🔥 Core Idea:

Lambda needs a **target type** → that target type is a **Functional Interface**

### Example:

```java
Add add = (a, b) -> a + b;
```

👉 Here:

* `Add` = Functional Interface
* Lambda = implementation

---

## 🔹 3. @FunctionalInterface Annotation (Important)

```java
@FunctionalInterface
interface Test {
    void show();
}
```

### 💡 Why use it?

* Ensures **only one abstract method**
* Compile-time error if violated

### ❌ Error Example

```java
@FunctionalInterface
interface Test {
    void show();
    void display(); // ❌ Error
}
```

---

## 🔹 4. Rules of Functional Interface (🔥 Must Know)

✔ Only **one abstract method**

✔ Can have:

* `default` methods ✅
* `static` methods ✅
* Methods from `Object` class ✅

### Example:

```java
@FunctionalInterface
interface Demo {
    void run(); // abstract

    default void test() {
        System.out.println("Default");
    }

    static void util() {
        System.out.println("Static");
    }
}
```

---

## 🔹 5. Functional Interface + Lambda Relationship

---

### 💡 Key Understanding:

> Lambda = implementation of Functional Interface

```text
Functional Interface → defines method
Lambda → provides implementation
```

---

## 🔹 6. Built-in Functional Interfaces (🔥🔥 VERY IMPORTANT)

All built-in functional interfaces are in:
👉 `java.util.function` package

### 🧠 6.1. Core Idea (VERY IMPORTANT)

All built-in functional interfaces follow patterns:

```text
Input → Output
```

There are mainly **4 categories**:

| Type      | Takes Input? | Returns Output? | Use Case        | Method   |
|-----------|--------------|-----------------|-----------------|----------|
| Predicate | ✅ Yes        | ❌ No (boolean)  | Condition check | test()   |
| Function  | ✅ Yes        | ✅ Yes           | Transformation  | apply()  |
| Consumer  | ✅ Yes        | ❌ No            | Perform action  | accept() |
| Supplier  | ❌ No         | ✅ Yes           | Provide value   | get()    |

---

### 🔥 6.2. Predicate<T>

#### 📌 Definition:

> Takes input → returns **boolean**

##### ✅ Method:

```java
boolean test(T t);
```

##### 💡 Example:

```java
Predicate<Integer> isEven = n -> n % 2 == 0;
System.out.println(isEven.test(10)); // true
```

##### 🔥 Use Cases:

* Filtering data
* Conditions in Streams

##### 🔹 Extra Methods (IMPORTANT)

```java
and()
or()
negate()
```

##### Example:

```java
Predicate<Integer> p1 = n -> n > 10;
Predicate<Integer> p2 = n -> n % 2 == 0;

p1.and(p2).test(12); // true
```

---

### 🔥 6.3. Function<T, R>

#### 📌 Definition:

> Takes input → returns output

##### ✅ Method:

```java
R apply(T t);
```

##### 💡 Example:

```java
Function<Integer, Integer> square = x -> x * x;
System.out.println(square.apply(5)); // 25
```

##### 🔥 Use Cases:

* Data transformation
* Mapping in Streams

##### 🔹 Extra Methods:

```java
andThen()
compose()
```

* f1.`andThen`(f2): Executes f1 first, then f2. (`Flow: Left to Right`)
* f1.`compose`(f2): Executes f2 first, then f1. (`Flow: Right to Left`)

##### Example:

```java
Function<Integer, Integer> f1 = x -> x * 2;
Function<Integer, Integer> f2 = x -> x + 3;

f1.andThen(f2).apply(5); // (5*2)+3 = 13
```

---

### 🔥 6.4. Consumer<T>

#### 📌 Definition:

> Takes input → **no return** (side effects)

##### ✅ Method:

```java
void accept(T t);
```

##### 💡 Example:

```java
Consumer<String> print = s -> System.out.println(s);
print.accept("Hello");
```

##### 🔥 Use Cases:

* Printing/logging
* Updating values
* `forEach()` in collections

##### 🔹 Extra Method:

```java
andThen()
```

##### Example:

```java
Consumer<String> c1 = s -> System.out.println(s);
Consumer<String> c2 = s -> System.out.println(s.toUpperCase());

c1.andThen(c2).accept("java");
```

---

### 🔥 6.5. Supplier<T>

#### 📌 Definition:

> Takes no input → returns value

##### ✅ Method:

```java
T get();
```

##### 💡 Example:

```java
Supplier<Double> random = () -> Math.random();
System.out.println(random.get());
```

##### 🔥 Use Cases:

* Lazy value generation
* Object creation
* Random values

---

### ⚡ 6.6. Bi-Functional Interfaces (2 Inputs)

---

#### 🔹 BiPredicate<T, U>

```java
boolean test(T t, U u);
```

#### 🔹 BiFunction<T, U, R>

```java
R apply(T t, U u);
```

#### 🔹 BiConsumer<T, U>

```java
void accept(T t, U u);
```

---

##### 💡 Example:

```java
BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
```

---

### ⚡ 6.7. Unary & Binary Operators

These are special types of Function.

---

#### 🔹 UnaryOperator<T>

👉 Same input & output type

```java
UnaryOperator<Integer> square = x -> x * x;
```

---

#### 🔹 BinaryOperator<T>

👉 Two inputs, same type

```java
BinaryOperator<Integer> add = (a, b) -> a + b;
```

---

### ⚡ 6.8. Primitive Functional Interfaces (🔥 Performance)

To avoid **boxing/unboxing overhead**:

---

#### Examples:

* `IntPredicate`
* `IntFunction`
* `IntConsumer`
* `IntSupplier`

---

##### 💡 Example:

```java
IntPredicate isEven = x -> x % 2 == 0;
```

---

##### 🔥 Why Important?

* Faster than `Predicate<Integer>`
* Used heavily in streams

---

### 🧠 6.9. Function Chaining (VERY IMPORTANT)

---

##### Example:

```java
Function<Integer, Integer> multiply = x -> x * 2;
Function<Integer, Integer> add = x -> x + 3;

multiply.andThen(add).apply(5); // 13
```

---

### 🔥 6.10. Real-World Usage (Interview Gold)

---

##### 🔹 Stream Example:

```java
List<Integer> list = Arrays.asList(1, 2, 3, 4);

list.stream()
    .filter(n -> n % 2 == 0)   // Predicate
    .map(n -> n * n)           // Function
    .forEach(System.out::println); // Consumer
```

👉 This combines:

* Predicate
* Function
* Consumer


---

## 🔹 8. Functional Interface Inheritance (Tricky)

---

### Case 1: Valid

```java
interface A {
    void show();
}

interface B extends A {
}
```

✔ Still functional (only one abstract method)

---

### Case 2: Invalid

```java
interface A {
    void show();
}

interface B {
    void display();
}

interface C extends A, B { // ❌
}
```

❌ Now has 2 abstract methods → not functional

---

## 🔹 9. Real Use Cases

Functional interfaces are used in:

* Lambda expressions
* Streams API
* Event handling
* Multithreading (`Runnable`, `Callable`)

---

## 🔹 10. Internal Working (Concept Link)

When Lambda is used:

```java
Runnable r = () -> System.out.println("Hi");
```

👉 Internally:

* JVM creates implementation of `Runnable`
* Uses **Functional Interface contract**

---

## 🔹 11. Custom Functional Interface (Important)

You can create your own:

```java
@FunctionalInterface
interface Calculator {
    int operate(int a, int b);
}
```

### Use:

```java
Calculator add = (a, b) -> a + b;
Calculator mul = (a, b) -> a * b;
```

---

## 🔹 12. Functional Interface vs Normal Interface

| Feature          | Functional Interface | Normal Interface     |
|------------------|----------------------|----------------------|
| Abstract methods | 1                    | Multiple             |
| Lambda support   | ✅ Yes                | ❌ No                 |
| Purpose          | Behavior passing     | Structure definition |

---

## 🔹 13. Common Interview Questions 🔥

Be ready for:

---

### ❓ Can a functional interface have multiple methods?

<details>
👉 Yes (but only one abstract)
</details>

---

### ❓ Is `Runnable` a functional interface?

<details>👉 Yes (one abstract method `run()`)</details>

---

### ❓ Why do we need functional interfaces?

<details>👉 To support Lambda expressions </details>

---

### ❓ Can we create our own functional interfaces?

<details> 👉 Yes </details>

---

### ❓ What happens if 2 abstract methods exist?

<details> 👉 Compilation error (if annotated with @FunctionalInterface) </details>

---

## 🔹 14. Hidden Trick (VERY IMPORTANT)

---

### ❓ Does `Object` methods count?

```java
@FunctionalInterface
interface Test {
    void show();
    String toString(); // allowed
}
```

<details> 
👉 Still valid!
Because `toString()` belongs to `Object`
</details>

---

## 🔹 15. Interview Summary (Best Answer)

> A Functional Interface is an interface with exactly one abstract method, used as the target type for Lambda expressions. It can contain multiple default and static methods, and is the core foundation for enabling functional programming in Java.

---

---

# 🚀 Built-in Functional Interfaces (Deep Dive)

---

### 🔹 1. Predicate<T>

---

👉 Takes input, returns boolean, contains test().

```java
Predicate<Integer> isEven = n -> n % 2 == 0;
```

#### Methods

* `boolean test(T t)` : Returns boolean based on condition.
* `default Predicate<T> and(Predicate<? super T> other)` : Represents logical **AND** of this predicate and the other predicate. Throws `NullPointerException` if other is null.
* `default Predicate<T> or(Predicate<? super T> other)` : Represents logical **OR** of this predicate and the other predicate. Throws `NullPointerException` if other is null.
* `default Predicate<T> negate()` : Represents the logical **NOT** of this predicate.
* `static <T> Predicate<T> isEqual(Object targetRef)` : Tests if two arguments are equal according to `Objects.equals(Object, Object)`.

---

### 🔹 2. Function<T, R>

---

👉 Takes input, returns output, contains apply().

```java
Function<Integer, Integer> multiplyBy2 = (n) -> n * 2;
Function<Integer, Integer> add3 = (n) -> n + 3;

// 1. andThen: (5 * 2) + 3 = 13
// It executes multiplyBy2 FIRST, then add3.
Function<Integer, Integer> andThenResult = multiplyBy2.andThen(add3);
System.out.println("andThen (Multiply then Add): " + andThenResult.apply(5));

// 2. compose: (5 + 3) * 2 = 16
// It executes add3 FIRST, then multiplyBy2.
Function<Integer, Integer> composeResult = multiplyBy2.compose(add3);
System.out.println("compose (Add then Multiply): " + composeResult.apply(5));
```

#### Methods

* `R apply(T t)` : Takes input, returns output.
* `default <V> Function<V,R> compose(Function<? super V,? extends T> before)` : First applies the before function and then applies this function. Throws `NullPointerException` if before function is null.
* `default <V> Function<T,V> andThen(Function<? super R,? extends V> after)` : First applies this function and then applies the after function. Throws `NullPointerException` if after function is null.
* `static <T> Function<T,T> identity()` : Always returns its input argument.

---

### 🔹 3. Consumer<T>

---

👉 Takes input, returns nothing

```java
Consumer<String> print = s -> System.out.println(s);
```

#### Methods

* `void accept(T t)` : No return.
* `default Consumer<T> andThen(Consumer<? super T> after)` : sequence this operation followed by the after operation. Throws `NullPointerException` if after function is null.

---

### 🔹 4. Supplier<T>

---

👉 Takes no input, returns value

```java
Supplier<Double> random = () -> Math.random();
```


#### Methods

* `T get()` : Does not take input, But return some output. Ex: OTP generator.

---

## 🔹 7. Primitive Functional Interfaces (Performance)

> Standard interfaces like `Predicate<Integer>` or `Function<Integer, String>` use Generic Types, which only work with Objects. If you pass a primitive int, Java has to "box" it into an Integer object. Using these primitive specializations we can avoid that overhead, making the code faster and more memory-efficient.

---

### 👉 Predicate, Consumer, & Supplier

---

| Category  | int          | long          | double          | Return Type     |
|-----------|--------------|---------------|-----------------|-----------------|
| Predicate | IntPredicate | LongPredicate | DoublePredicate | boolean         |
| Consumer  | IntConsumer  | LongConsumer  | DoubleConsumer  | void            |
| Supplier  | IntSupplier  | LongSupplier  | DoubleSupplier  | int/long/double |

*Note: There is also a **`BooleanSupplier`**, which takes no input and returns a `boolean`.*

---

### 👉 Function Specializations

---

Functions are more complex because they have an **input** and an **output**. Java provides three patterns:

#### A. Primitive Input → Object Output
| Interface               | Input    | Output |
|:------------------------|:---------|:-------|
| **`IntFunction<R>`**    | `int`    | `R`    |
| **`LongFunction<R>`**   | `long`   | `R`    |
| **`DoubleFunction<R>`** | `double` | `R`    |

#### B. Object Input → Primitive Output
| Interface                 | Input | Output   |
|:--------------------------|:------|:---------|
| **`ToIntFunction<T>`**    | `T`   | `int`    |
| **`ToLongFunction<T>`**   | `T`   | `long`   |
| **`ToDoubleFunction<T>`** | `T`   | `double` |

#### C. Primitive Input → Primitive Output (Cross-Type)
These are used for direct conversion between primitives.
* **From `int`:** `IntToLongFunction`, `IntToDoubleFunction`
* **From `long`:** `LongToIntFunction`, `LongToDoubleFunction`
* **From `double`:** `DoubleToIntFunction`, `DoubleToLongFunction`

---

### 👉 Operators

---

Operators are special cases of Functions where the **input and output types are the same**.

| Category                      | `int`               | `long`               | `double`               |
|:------------------------------|:--------------------|:---------------------|:-----------------------|
| **UnaryOperator** (1 input)   | `IntUnaryOperator`  | `LongUnaryOperator`  | `DoubleUnaryOperator`  |
| **BinaryOperator** (2 inputs) | `IntBinaryOperator` | `LongBinaryOperator` | `DoubleBinaryOperator` |

---

### 👉 Bi-Interfaces (Mixed Types)

---

While there are no primitive `BiFunction` or `BiPredicate` interfaces in the standard library, there is a special set of **`BiConsumer`** interfaces used frequently in collections and streams:

* **`ObjIntConsumer<T>`**: Takes an object of type `T` and a primitive `int`.
* **`ObjLongConsumer<T>`**: Takes an object of type `T` and a primitive `long`.
* **`ObjDoubleConsumer<T>`**: Takes an object of type `T` and a primitive `double`.

---

### 👉 Example

---

```java
import java.util.function.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A comprehensive demonstration of Java's primitive functional interfaces for 'int'.
 */
public class PrimitiveInterfaceDemo {

    public static void main(String[] args) {
        System.out.println("--- 1. BASIC PRIMITIVE INTERFACES ---");

        // IntPredicate: Returns boolean
        IntPredicate isEven = n -> n % 2 == 0;
        System.out.println("IntPredicate (is 10 even?): " + isEven.test(10));

        // IntConsumer: Returns void (side effects)
        IntConsumer logger = n -> System.out.println("IntConsumer logging: " + n);
        logger.accept(42);

        // IntSupplier: Returns int
        IntSupplier randomInt = () -> (int) (Math.random() * 100);
        System.out.println("IntSupplier value: " + randomInt.getAsInt());


        System.out.println("\n--- 2. TRANSFORMATION FUNCTIONS ---");

        // IntFunction<R>: int -> Object
        IntFunction<String> toCurrency = i -> "$" + i + ".00";
        System.out.println("IntFunction (to String): " + toCurrency.apply(50));

        // ToIntFunction<T>: Object -> int
        ToIntFunction<String> stringLength = s -> s.length();
        System.out.println("ToIntFunction (length of 'Gemini'): " + stringLength.applyAsInt("Gemini"));

        // IntToLongFunction: int -> long
        IntToLongFunction multiplyToLong = i -> (long) i * 1_000_000_000L;
        System.out.println("IntToLongFunction: " + multiplyToLong.applyAsLong(2));

        // IntToDoubleFunction: int -> double
        IntToDoubleFunction divideByThree = i -> i / 3.0;
        System.out.println("IntToDoubleFunction: " + divideByThree.applyAsDouble(10));


        System.out.println("\n--- 3. OPERATORS (Same Input/Output) ---");

        // IntUnaryOperator: 1 input, 1 output
        IntUnaryOperator increment = n -> n + 1;
        System.out.println("IntUnaryOperator (10 + 1): " + increment.applyAsInt(10));

        // IntBinaryOperator: 2 inputs, 1 output
        IntBinaryOperator multiply = (a, b) -> a * b;
        System.out.println("IntBinaryOperator (5 * 4): " + multiply.applyAsInt(5, 4));


        System.out.println("\n--- 4. HYBRID INTERFACES ---");

        // ObjIntConsumer<T>: Object + int -> void
        List<Integer> numbers = new ArrayList<>();
        ObjIntConsumer<List<Integer>> listAdder = (list, val) -> list.add(val);
        
        listAdder.accept(numbers, 100);
        listAdder.accept(numbers, 200);
        System.out.println("ObjIntConsumer result (List contents): " + numbers);
    }
}
```

👉 Why important?

* Avoids boxing/unboxing
* Improves performance in streams

---

# 🎯 12. Interview Summary (Best Answer)

> Java provides built-in functional interfaces in `java.util.function` like Predicate, Function, Consumer, and Supplier, which represent common functional patterns such as condition checking, transformation, consuming data, and supplying values. These interfaces are widely used in Lambda expressions and Streams API to enable functional programming.

---

---

# 🚀 Tricky Interview Questions — Functional Interfaces

---

## ❓ 1. Can a functional interface have multiple methods?

<details> 

👉 **Answer: YES**

✔ It can have:
* Multiple `default` methods
* Multiple `static` methods

❌ But only **one abstract method**

</details>

---

## ❓ 2. Is this a functional interface?

```java
interface Test {
    void show();
    String toString();
}
```

<details> 

👉 **Answer: YES ✅**

💡 Explanation:
* `toString()` is from `Object`
* Not counted as abstract method

</details>

---

## ❓ 3. Will this compile?

```java
@FunctionalInterface
interface A {
    void show();
}

interface B {
    void show();
}

interface C extends A, B {
}
```

<details> 

👉 **Answer: YES ✅**

💡 Explanation:

* Both interfaces have SAME method signature
* Still counts as **one abstract method**

</details>

---

## ❓ 4. What about this?

```java
interface A {
    void show();
}

interface B {
    void display();
}

interface C extends A, B {
}
```

<details> 

👉 **Answer: NOT a functional interface ❌**

💡 Reason:

* Two different abstract methods → violates rule

</details> 

---

## ❓ 5. Can a functional interface be empty?

```java
interface Test {
}
```

<details> 

👉 **Answer: NO ❌**

💡 Must have exactly **one abstract method**

</details> 

---

## ❓ 6. Can we override default methods in functional interface?

<details> 

👉 **Answer: YES ✅**

```java
interface A {
    default void show() {
        System.out.println("A");
    }
}

class B implements A {
    public void show() {
        System.out.println("B");
    }
}
```

</details> 

---

## ❓ 7. What happens if two interfaces have same default method?

```java
interface A {
    default void show() { }
}

interface B {
    default void show() { }
}

class C implements A, B {
}
```

<details> 

👉 **Answer: Compilation Error ❌**

💡 Must override explicitly:

```java
public void show() {
    A.super.show();
}
```

</details>

---

## ❓ 8. Can Lambda work without functional interface?

<details> 

👉 **Answer: NO ❌**

💡 Lambda always needs:

> A target type → Functional Interface

</details>

---

## ❓ 9. Which of these is NOT valid?

```java
Predicate<String> p = s -> s.length();
```

<details> 

👉 **Answer: INVALID ❌**

💡 Reason:

* `Predicate` expects **boolean**
* `length()` returns `int`

</details>

---

## ❓ 10. Difference: Function vs Predicate?

<details> 

👉 **Answer:**

| Function       | Predicate       |
|----------------|-----------------|
| Returns value  | Returns boolean |
| Transformation | Condition       |

</details>

---

## ❓ 11. What is the output?

```java
Function<Integer, Integer> f = x -> x * 2;
System.out.println(f.andThen(x -> x + 3).apply(5));
```

<details> 

👉 **Answer: 13**

💡 Flow:

* 5 * 2 = 10
* 10 + 3 = 13

</details>

---

## ❓ 12. What is the output?

```java
Function<Integer, Integer> f = x -> x * 2;
System.out.println(f.compose(x -> x + 3).apply(5));
```

<details> 

👉 **Answer: 16**

💡 Flow:

* 5 + 3 = 8
* 8 * 2 = 16

</details>

---

## ❓ 13. Can we use `null` with Predicate?

```java
Predicate<String> p = s -> s.isEmpty();
p.test(null);
```

<details> 

👉 **Answer: Runtime Exception ❌**

💡 `NullPointerException`

</details>

---

## ❓ 14. Supplier vs Callable?

<details> 

👉 **Answer:**

| Supplier             | Callable            |
|----------------------|---------------------|
| No exception         | Can throw exception |
| No input             | No input            |
| Functional interface | Part of concurrency |

</details> 

---

## ❓ 15. Why do we have primitive functional interfaces?

<details> 

👉 **Answer:**
To avoid:

> **Boxing/Unboxing overhead**

```java
IntPredicate p = x -> x % 2 == 0;
```

</details>

---

## ❓ 16. Is Runnable a functional interface?

<details> 

👉 **Answer: YES ✅**

```java
void run();
```

</details>

---

## ❓ 17. Can a functional interface extend another functional interface?

<details> 

👉 **Answer: YES ✅**

✔ Only if total abstract methods = 1

</details>

---

## ❓ 18. What happens here?

```java
@FunctionalInterface
interface Test {
    void show();
    default void show2() {}
}
```

<details> 

👉 **Answer: VALID ✅** 

</details>

---

## ❓ 19. Can a functional interface have constructors?

<details> 

👉 **Answer: NO ❌**

💡 Interfaces don’t have constructors

</details>

---

## ❓ 20. Which one should you use?

```java
Function<Integer, Boolean>
Predicate<Integer>
```

<details> 

👉 **Answer: Predicate**

💡 Cleaner, semantic, and readable

</details>

---

# 🔥 Bonus Rapid Fire (Interview Style)

👉 Answer quickly:

1. Can functional interface have static methods? → ✅ YES
2. Can it have private methods? → ✅ YES (Java 9+)
3. Can lambda implement 2 methods? → ❌ NO
4. Does `equals()` count as abstract? → ❌ NO
5. Can we pass lambda as parameter? → ✅ YES

---

# 🎯 Final Tip (Very Important)

In interviews, always:

* Prefer **Predicate over Function<Boolean>**
* Use **built-in interfaces instead of custom**
* Explain **why you chose it**

---

---

# 🚀 Lambda + Functional Interfaces — Interview Problems

---

# 🟡 MEDIUM LEVEL

---

## ❓ Q1. Filter and Transform List

👉 Given a list of integers:

* Filter even numbers
* Square them
* Print result

**Constraint:** Use **Predicate + Function + Consumer**

```java id="n9b0y1"
List<Integer> list = Arrays.asList(1,2,3,4,5,6);
```

<details>
<summary>Answer</summary>

```java id="2b0y8f"
Predicate<Integer> isEven = n -> n % 2 == 0;
Function<Integer, Integer> square = n -> n * n;
Consumer<Integer> print = n -> System.out.println(n);

list.stream()
    .filter(isEven)
    .map(square)
    .forEach(print);
```

</details>

---

## ❓ Q2. Custom Functional Interface Calculator

👉 Create a functional interface:

* Method: `operate(int a, int b)`
* Use Lambda to implement:

    * Addition
    * Multiplication

<details>
<summary>Answer</summary>

```java id="54rjv9"
@FunctionalInterface
interface Calculator {
    int operate(int a, int b);
}

Calculator add = (a, b) -> a + b;
Calculator multiply = (a, b) -> a * b;

System.out.println(add.operate(2, 3));      // 5
System.out.println(multiply.operate(2, 3)); // 6
```

</details>

---

## ❓ Q3. Conditional Execution Using Predicate

👉 Write a method:

* Accepts a list and a `Predicate`
* Prints only matching elements

<details>
<summary>Answer</summary>

```java id="kp1n3t"
public static void printCondition(List<Integer> list, Predicate<Integer> condition) {
    list.stream()
        .filter(condition)
        .forEach(System.out::println);
}

// Usage
printCondition(list, n -> n > 3);
```

</details>

---

## ❓ Q4. Combine Two Functions

👉 Given:

```java id="8bnd9q"
Function<Integer, Integer> f1 = x -> x * 2;
Function<Integer, Integer> f2 = x -> x + 5;
```

👉 Combine them so output becomes:

```
(x * 2) + 5
```

<details>
<summary>Answer</summary>

```java id="t3p4vu"
Function<Integer, Integer> result = f1.andThen(f2);
System.out.println(result.apply(5)); // 15
```

</details>

---

# 🔴 HARD LEVEL

---

## ❓ Q5. Dynamic Strategy Pattern using Lambda

👉 Problem:

* Based on operation type (`"add"`, `"mul"`), execute logic dynamically

---

### Input:

```java id="jqshfy"
String op = "add";
int a = 10, b = 5;
```

---

### Expected:

* Use `Map<String, FunctionalInterface>`

---

<details>
<summary>Answer</summary>

```java id="t6nxtq"
Map<String, BiFunction<Integer, Integer, Integer>> operations = new HashMap<>();

operations.put("add", (x, y) -> x + y);
operations.put("mul", (x, y) -> x * y);

System.out.println(operations.get(op).apply(a, b));
```

👉 This replaces traditional if-else / switch

</details>

---

## ❓ Q6. Lazy Initialization using Supplier

👉 Problem:

* Expensive object should be created **only when needed**

---

<details>
<summary>Answer</summary>

```java id="fl6mrm"
Supplier<String> lazyValue = () -> {
    System.out.println("Creating...");
    return "Expensive Object";
};

// Not created yet

System.out.println(lazyValue.get()); 
```

👉 Object created only when `get()` is called

</details>

---

## ❓ Q7. Exception Handling Wrapper (Advanced)

👉 Problem:

* Wrap a lambda so it handles exceptions internally

---

<details>
<summary>Answer</summary>

```java id="whx2o7"
public static Consumer<Integer> safeConsumer(Consumer<Integer> consumer) {
    return i -> {
        try {
            consumer.accept(i);
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    };
}

// Usage
list.forEach(safeConsumer(i -> {
    if (i == 3) throw new RuntimeException("Error");
    System.out.println(i);
}));
```

</details>

---

## ❓ Q8. Custom Comparator using Lambda

👉 Sort list based on:

* String length
* If equal → lexicographically

---

<details>
<summary>Answer</summary>

```java id="0xfpxz"
List<String> list = Arrays.asList("apple", "bat", "cat", "banana");

list.sort((s1, s2) -> {
    if (s1.length() == s2.length()) {
        return s1.compareTo(s2);
    }
    return s1.length() - s2.length();
});
```

</details>

---

## ❓ Q9. Higher-Order Function (Very Important Concept)

👉 Write a function:

* Takes a `Function`
* Returns another function

---

<details>
<summary>Answer</summary>

```java id="w9l9r2"
public static Function<Integer, Integer> multiplyBy(int factor) {
    return x -> x * factor;
}

Function<Integer, Integer> times3 = multiplyBy(3);
System.out.println(times3.apply(5)); // 15
```

👉 Function returning function

</details>

---

## ❓ Q10. Combine Predicate Dynamically

👉 Given multiple conditions:

* Greater than 10
* Even

👉 Combine dynamically

---

<details>
<summary>Answer</summary>

```java id="h0k9no"
Predicate<Integer> p1 = x -> x > 10;
Predicate<Integer> p2 = x -> x % 2 == 0;

Predicate<Integer> combined = p1.and(p2);

list.stream()
    .filter(combined)
    .forEach(System.out::println);
```

</details>

---

# 🎯 What Interviewer Checks Here

These problems test:

* Functional thinking
* Proper use of:

    * Predicate
    * Function
    * Supplier
    * Consumer
* Real-world design (strategy pattern, lazy loading)
* Clean & readable code

---

# 🧠 Pro Tip (Very Important)

In interviews:

* Don’t just write lambda
* **Explain WHY this functional interface is used**

Example:

> “I used `Predicate` because I need a boolean condition”

---

---

# 🚀 Functional Interfaces — Medium & Hard Problems

---

# 🟡 MEDIUM LEVEL

---

## ❓ Q1. Filter and Print Even Numbers Efficiently

👉 Use **IntPredicate + IntConsumer**

* Filter even numbers from an array
* Print them

```java
int[] arr = {1,2,3,4,5,6};
```

<details>
<summary>Answer</summary>

```java
IntPredicate isEven = x -> x % 2 == 0;
IntConsumer print = x -> System.out.println(x);

Arrays.stream(arr)
      .filter(isEven)
      .forEach(print);
```

</details>

---

## ❓ Q2. Transform Array Using IntFunction

👉 Convert int array into:

* Square of each number

<details>
<summary>Answer</summary>

```java
IntFunction<Integer> square = x -> x * x;

Arrays.stream(arr)
      .map(x -> square.apply(x))
      .forEach(System.out::println);
```

</details>

---

## ❓ Q3. Generate Random Values Using IntSupplier

👉 Generate 5 random integers between 1–100

<details>
<summary>Answer</summary>

```java
IntSupplier random = () -> (int)(Math.random() * 100);

IntStream.generate(random)
         .limit(5)
         .forEach(System.out::println);
```

</details>

---

## ❓ Q4. UnaryOperator Transformation

👉 Multiply each number in a list by 3 using `UnaryOperator`

<details>
<summary>Answer</summary>

```java
UnaryOperator<Integer> multiply = x -> x * 3;

list.replaceAll(multiply);
System.out.println(list);
```

</details>

---

## ❓ Q5. BinaryOperator Aggregation

👉 Find sum of list using `BinaryOperator`

<details>
<summary>Answer</summary>

```java
BinaryOperator<Integer> sum = (a, b) -> a + b;

int result = list.stream()
                 .reduce(0, sum);

System.out.println(result);
```

</details>

---

## ❓ Q6. BiFunction for Full Name

👉 Combine first and last name

```java
String first = "John";
String last = "Doe";
```

<details>
<summary>Answer</summary>

```java
BiFunction<String, String, String> fullName = (f, l) -> f + " " + l;

System.out.println(fullName.apply(first, last));
```

</details>

---

# 🔴 HARD LEVEL

---

## ❓ Q7. Dynamic Filtering System

👉 Build a system where:

* Multiple `IntPredicate` conditions are combined dynamically
* Example:

    * > 10
    * divisible by 3

<details>
<summary>Answer</summary>

```java
IntPredicate greaterThan10 = x -> x > 10;
IntPredicate divisibleBy3 = x -> x % 3 == 0;

IntPredicate combined = greaterThan10.and(divisibleBy3);

Arrays.stream(arr)
      .filter(combined)
      .forEach(System.out::println);
```

</details>

---

## ❓ Q8. Function Pipeline (Transformation Chain)

👉 Use `IntFunction` to:

* Multiply by 2
* Then add 5
* Then square

<details>
<summary>Answer</summary>

```java
IntFunction<Integer> multiply = x -> x * 2;
IntFunction<Integer> add = x -> x + 5;
IntFunction<Integer> square = x -> x * x;

Function<Integer, Integer> pipeline =
        multiply.andThen(add).andThen(square);

System.out.println(pipeline.apply(3)); // ((3*2)+5)^2 = 121
```

</details>

---

## ❓ Q9. Lazy Computation System

👉 Use `IntSupplier`:

* Value should be computed only when requested
* Print message when computation happens

<details>
<summary>Answer</summary>

```java
IntSupplier lazy = () -> {
    System.out.println("Computing...");
    return 42;
};

System.out.println("Before get()");
System.out.println(lazy.getAsInt());
```

</details>

---

## ❓ Q10. Custom Reduce using BinaryOperator

👉 Find maximum element in list using `BinaryOperator`

<details>
<summary>Answer</summary>

```java
BinaryOperator<Integer> max = (a, b) -> a > b ? a : b;

int result = list.stream()
                 .reduce(Integer.MIN_VALUE, max);

System.out.println(result);
```

</details>

---

## ❓ Q11. BiPredicate for Validation System

👉 Validate:

* Age > 18
* Salary > 30000

<details>
<summary>Answer</summary>

```java
BiPredicate<Integer, Integer> validate =
        (age, salary) -> age > 18 && salary > 30000;

System.out.println(validate.test(25, 40000)); // true
```

</details>

---

## ❓ Q12. BiConsumer for Map Processing

👉 Print key-value pairs using `BiConsumer`

<details>
<summary>Answer</summary>

```java
BiConsumer<String, Integer> printer =
        (k, v) -> System.out.println(k + " : " + v);

map.forEach(printer);
```

</details>

---

## ❓ Q13. Strategy Pattern using BinaryOperator

👉 Dynamically select operation:

* add / multiply

<details>
<summary>Answer</summary>

```java
Map<String, BinaryOperator<Integer>> ops = new HashMap<>();

ops.put("add", (a, b) -> a + b);
ops.put("mul", (a, b) -> a * b);

System.out.println(ops.get("add").apply(5, 3));
```

</details>

---

## ❓ Q14. Chained Predicate System (Real-world)

👉 Combine:

* Even
* Greater than 50
* Less than 100

<details>
<summary>Answer</summary>

```java
IntPredicate even = x -> x % 2 == 0;
IntPredicate gt50 = x -> x > 50;
IntPredicate lt100 = x -> x < 100;

IntPredicate finalPredicate = even.and(gt50).and(lt100);

Arrays.stream(arr)
      .filter(finalPredicate)
      .forEach(System.out::println);
```

</details>

---

## ❓ Q15. Advanced: Custom Transformation Engine

👉 Build a reusable system:

* Accept `UnaryOperator`
* Apply to list dynamically

<details>
<summary>Answer</summary>

```java
public static void transform(List<Integer> list, UnaryOperator<Integer> op) {
    list.replaceAll(op);
}

// Usage
transform(list, x -> x * 10);
transform(list, x -> x + 5);
```

</details>

---

# 🧠 Pro Interview Tip

When solving:

* Don’t just code
* Say:

> “I’m using `IntPredicate` to avoid boxing overhead and improve performance”

👉 That line alone impresses interviewers.

---
