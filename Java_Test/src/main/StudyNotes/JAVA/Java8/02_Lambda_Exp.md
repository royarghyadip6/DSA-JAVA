
# 🚀 Lambda Expressions (Complete Deep Dive)

---

## 🔹 1. What is a Lambda Expression?

> A **Lambda Expression** is a **concise way to represent an anonymous function** (a function without a name). It Does not contain Access Modifier, Return Type and Method name.

### ✅ Syntax

```java
(parameters) -> expression
        
or
        
(parameters) -> { statements }
```

### ✅ Example

```java
(a, b) -> a + b
```

This represents a function that adds two numbers.

---

## 🔹 2. Why Lambda Was Introduced?

Before Java 8, we used **anonymous classes** to pass behavior.

### 🔥 Key Problems Lambda Solves:

* Reduces too much boilerplate code
* Reduces anonymous class usages
* Reduces functional programming support

### ❌ Without Lambda (Verbose)

```java
Runnable r = new Runnable() {
    @Override
    public void run() {
        System.out.println("Running...");
    }
};
```

### ✅ With Lambda (Clean)

```java
Runnable r = () -> System.out.println("Running...");
```

---

## 🔹 3. Functional Interface (VERY IMPORTANT LINK)

Lambda works **only with Functional Interfaces**.

### Example:

```java
@FunctionalInterface
interface Add {
    int sum(int a, int b);
}
```

### Using Lambda:

```java
Add add = (a, b) -> a + b;
System.out.println(add.sum(5, 3)); // 8
```

---

## 🔹 4. How Lambda Works Internally (🔥 Interview Gold)


> Lambda expressions are compiled into `invokedynamic` instructions, and at runtime the JVM uses `LambdaMetafactory` to dynamically create the functional interface implementation instead of generating a separate class.

> Lambda → `invokedynamic` → `LambdaMetafactory` → Object → Execute

### Internally:

* Converted into **functional interface implementation**
* Uses **invokedynamic** (JVM instruction)
* No separate `.class` file (unlike anonymous class)

👉 Benefits:

* Better performance
* Less memory usage

---

## 🔹 5. Lambda Syntax Variations

### 1. No parameter

```java
() -> System.out.println("Hello")
```

---

### 2. One parameter (no parentheses optional)

```java
x -> x * x
```

---

### 3. Multiple parameters

```java
(a, b) -> a + b
```

---

### 4. With return statement

```java
(a, b) -> {
    return a + b;
}
```

---

### 5. Without return (implicit return)

```java
(a, b) -> a + b
```

---

## 🔹 6. Lambda vs Anonymous Class (🔥 Important)

| Feature        | Lambda                | Anonymous Class       |
|----------------|-----------------------|-----------------------|
| Syntax         | Short                 | Verbose               |
| `this` keyword | Refers to outer class | Refers to inner class |
| Class file     | No separate class     | Creates class         |
| Performance    | Better                | Slightly slower       |

---

## 🔹 7. Variable Capture (Effectively Final) ⚠️

Lambda can access **only final or effectively final variables**.

---

### ❌ Not Allowed

```java
int x = 10;
x = 20;

Runnable r = () -> System.out.println(x); // ERROR
```

---

### ✅ Allowed

```java
int x = 10;

Runnable r = () -> System.out.println(x);
```

👉 “Effectively final” = value not changed after initialization

---

## 🔹 8. Lambda with Collections (Real Usage)

### Example:

```java
List<String> list = Arrays.asList("A", "B", "C");

list.forEach(s -> System.out.println(s));
```

---

## 🔹 9. Lambda + Functional Interfaces (Built-in)

### Predicate Example

```java
Predicate<Integer> isEven = n -> n % 2 == 0;
System.out.println(isEven.test(4)); // true
```

---

### Function Example

```java
Function<Integer, Integer> square = x -> x * x;
```

---

### Consumer Example

```java
Consumer<String> print = s -> System.out.println(s);
```

---

### Supplier Example

```java
Supplier<Double> random = () -> Math.random();
```

---

## 🔹 10. Method References (Shortcut of Lambda)

Lambda:

```java
s -> System.out.println(s)
```

Method Reference:

```java
System.out::println
```

---

## 🔹 11. Limitations of Lambda

* Cannot modify external variables
* Cannot have multiple abstract methods
* Hard to debug sometimes
* Not suitable for complex logic (use normal methods)

---

## 🔹 12. Where Lambda is Used (Real World)

* Streams API
* Event handling
* Multithreading (`Runnable`)
* Collections processing

---

## 🔹 13. Common Interview Questions

👉 Be ready for:

1. What is Lambda Expression?
2. Why was it introduced?
3. What is a Functional Interface?
4. Difference between Lambda & Anonymous Class?
5. What is effectively final?
6. How Lambda works internally?
7. Can Lambda access instance variables?
8. Why Lambda is faster?

---

## 🎯 Final Understanding (One Line)

> Lambda = **Compact way to implement a Functional Interface**

---

---

# 🚀 Lambda Expressions — Interview Question Set

---

## ❓ Q1. What will be the output?

```java
Runnable r = () -> System.out.println("Hello Lambda");
r.run();
```

<details>
<summary>Answer</summary>

**Output:**

```
Hello Lambda
```

👉 Lambda provides implementation of `Runnable.run()`

</details>

---

## ❓ Q2. Identify the error

```java
(int a, int b) -> a + b;
```

<details>
<summary>Answer</summary>

❌ **Compilation Error**

👉 Lambda must have a **target type (Functional Interface)**

✔ Correct:

```java
BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
```

</details>

---

## ❓ Q3. What is the output?

```java
interface Test {
    void show();
}

public class Main {
    public static void main(String[] args) {
        Test t = () -> System.out.println("Lambda");
        t.show();
    }
}
```

<details>
<summary>Answer</summary>

**Output:**

```
Lambda
```

👉 Lambda implements `show()` method

</details>

---

## ❓ Q4. What will happen?

```java
int x = 10;
x = 20;

Runnable r = () -> System.out.println(x);
```

<details>
<summary>Answer</summary>

❌ **Compilation Error**

👉 Variable must be **effectively final**

</details>

---

## ❓ Q5. Output?

```java
Function<Integer, Integer> f = x -> x * 2;
System.out.println(f.apply(5));
```

<details>
<summary>Answer</summary>

**Output:**

```
10
```

</details>

---

## ❓ Q6. Difference in behavior?

```java
this
```

Inside:

* Lambda
* Anonymous class

<details>
<summary>Answer</summary>

👉 **Lambda:** refers to enclosing class
👉 **Anonymous class:** refers to inner class instance

</details>

---

## ❓ Q7. What is the output?

```java
Function<Integer, Integer> f = x -> x + 2;
System.out.println(f.andThen(x -> x * 3).apply(2));
```

<details>
<summary>Answer</summary>

👉 Step-by-step:

* 2 + 2 = 4
* 4 * 3 = 12

**Output:**

```
12
```

</details>

---

## ❓ Q8. What is the output?

```java
Function<Integer, Integer> f = x -> x + 2;
System.out.println(f.compose(x -> x * 3).apply(2));
```

<details>
<summary>Answer</summary>

👉 Step-by-step:

* 2 * 3 = 6
* 6 + 2 = 8

**Output:**

```
8
```

</details>

---

## ❓ Q9. Identify valid syntax

```java
x -> x * x
(x) -> x * x
(int x) -> x * x
```

<details>
<summary>Answer</summary>

✔ All are valid

👉 Type inference works automatically

</details>

---

## ❓ Q10. What is the output?

```java
List<String> list = Arrays.asList("a", "b", "c");

list.forEach(s -> {
    System.out.print(s);
});
```

<details>
<summary>Answer</summary>

**Output:**

```
abc
```

</details>

---

## ❓ Q11. What happens here?

```java
Runnable r = () -> {
    System.out.println("Hello");
    return;
};
```

<details>
<summary>Answer</summary>

✔ **Valid**

👉 `return` is allowed in block lambda

</details>

---

## ❓ Q12. What will happen?

```java
Runnable r = () -> return;
```

<details>
<summary>Answer</summary>

❌ **Compilation Error**

👉 Must use `{}` for multiple statements

✔ Correct:

```java
() -> { return; }
```

</details>

---

## ❓ Q13. What is the output?

```java
Supplier<String> s = () -> "Java";
System.out.println(s.get());
```

<details>
<summary>Answer</summary>

**Output:**

```
Java
```

</details>

---

## ❓ Q14. Can Lambda throw checked exception?

<details>
<summary>Answer</summary>

👉 Yes, but:

* Must handle inside lambda OR
* Functional interface must declare `throws`

</details>

---

## ❓ Q15. What happens?

```java
List<Integer> list = Arrays.asList(1,2,3,4);

list.stream()
    .filter(n -> n % 2 == 0)
    .forEach(System.out::println);
```

<details>
<summary>Answer</summary>

**Output:**

```
2
4
```

👉 Uses Predicate + Consumer

</details>

---

## ❓ Q16. Which is better?

```java
Function<Integer, Boolean>
Predicate<Integer>
```

<details>
<summary>Answer</summary>

👉 **Predicate**

✔ More readable
✔ Designed for boolean conditions

</details>

---

## ❓ Q17. What is the output?

```java
Function<Integer, Integer> f = x -> x++;
System.out.println(f.apply(5));
```

<details>
<summary>Answer</summary>

**Output:**

```
5
```

👉 Post-increment returns original value

</details>

---

## ❓ Q18. What is the output?

```java
Function<Integer, Integer> f = x -> ++x;
System.out.println(f.apply(5));
```

<details>
<summary>Answer</summary>

**Output:**

```
6
```

👉 Pre-increment returns incremented value

</details>

---

## ❓ Q19. Can Lambda be reused?

<details>
<summary>Answer</summary>

👉 Yes

```java
Function<Integer, Integer> f = x -> x * 2;
```

Used multiple times

</details>

---

## ❓ Q20. What is the biggest advantage of Lambda?

<details>
<summary>Answer</summary>

👉 Concise code + functional programming support
👉 No boilerplate (anonymous class removed)
👉 Better readability + performance

</details>

---
