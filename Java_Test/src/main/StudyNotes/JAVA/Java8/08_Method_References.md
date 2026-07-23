
---

# 🔥 Method References (Java 8 Deep Dive)

---

# 🧠 1. What is a Method Reference?

A **Method Reference** in Java is a **shorthand syntax for lambda expressions** when you already have a method that matches the required functional interface.

---

### ✅ Basic Idea

Instead of writing:

```java
x -> System.out.println(x)
```

You can write:

```java
System.out::println
```

👉 You are **referring to an existing method**, not creating new logic.

---

# 🎯 2. Why Method References?

### 🔹 Problems with Lambda

* Verbose for simple operations
* Repeats existing logic

---

### ✅ Benefits of Method References

* ✔ Cleaner and shorter code
* ✔ Improves readability
* ✔ Promotes reuse of existing methods
* ✔ Reduces boilerplate

---

### 💡 Key Insight

👉 Use method reference **only when lambda is just calling another method**

---

# ⚙️ 3. Syntax

### General Syntax:

```java
ClassName::methodName
```

---

### Breakdown:

| Part       | Meaning                   |
|------------|---------------------------|
| `::`       | Method reference operator |
| Left side  | Class or object           |
| Right side | Method name               |

---

# 🔥 4. Types of Method References

---

There are exactly four types of method references:

---

### 1. Reference to a Static Method
This type replaces a lambda expression that calls an existing static utility method. 
* **Syntax:** `ContainingClass::staticMethodName`
* **Lambda Equivalent:** `(args) -> ContainingClass.staticMethodName(args)`

**Example:**
```java
// Lambda expression
Function<String, Integer> lambda = (s) -> Integer.parseInt(s);

// Method reference
Function<String, Integer> methodRef = Integer::parseInt;
```

---

### 2. Reference to an Instance Method of a Particular Object
This is used when you invoke an instance method on a specific, already existing object variable. It is also referred to as a **bound** method reference.
* **Syntax:** `containingObject::instanceMethodName`
* **Lambda Equivalent:** `(args) -> containingObject.instanceMethodName(args)`

**Example:**
```java
String searchStr = "Hello World";

// Lambda expression
Predicate<String> lambda = (s) -> searchStr.contains(s);

// Method reference (bound to the 'searchStr' object)
Predicate<String> methodRef = searchStr::contains;
```

---

### 3. Reference to an Instance Method of an Arbitrary Object of a Particular Type
This type is used when the method being called is an instance method, but the target object is not determined until runtime. The first parameter of the lambda acts as the target object executing the method. This is also known as an **unbound** method reference.
* **Syntax:** `ContainingType::methodName`
* **Lambda Equivalent:** `(instance, args) -> instance.methodName(args)`

**Example:**
```java
// Lambda expression (takes a string and calls its instance method)
Function<String, String> lambda = (s) -> s.toLowerCase();

// Method reference
Function<String, String> methodRef = String::toLowerCase;
```

---

### 4. Reference to a Constructor
This allows you to reference a class constructor to instantiate objects without explicitly typing the `new` keyword inside the functional expression.
* **Syntax:** `ClassName::new`
* **Lambda Equivalent:** `(args) -> new ClassName(args)`

**Example:**
```java
// Lambda expression
Supplier<List<String>> lambda = () -> new ArrayList<>();

// Method reference
Supplier<List<String>> methodRef = ArrayList::new;
```

---

### Quick Comparison Table

| Type | Syntax | Lambda Equivalent |
| :--- | :--- | :--- |
| **Static Method** | `Math::abs` | `x -> Math.abs(x)` |
| **Particular Object** | `myObj::foo` | `x -> myObj.foo(x)` |
| **Arbitrary Object** | `String::isEmpty` | `s -> s.isEmpty()` |
| **Constructor** | `HashSet::new` | `() -> new HashSet()` |

---

# 🧠 5. Functional Interface Matching (VERY IMPORTANT)

Method reference works only if:

👉 **Method signature matches functional interface**

---

### Example:

```java
Function<String, Integer> f = Integer::parseInt;
```

✔ Works because:

```text
String → int
```

---

### ❌ Invalid Case

```java
Function<String, Integer> f = String::length;
```

👉 Actually valid—trap depends on context
(returns int, takes String)

---

### 🧠 Rule

```text
Input + Output of method = Functional interface signature
```

---

# 🔄 6. Method Reference vs Lambda

| Feature     | Lambda       | Method Reference |
|-------------|--------------|------------------|
| Syntax      | Verbose      | Short            |
| Logic       | Custom logic | Existing method  |
| Readability | Medium       | High             |

---

### Example:

```java
list.forEach(x -> System.out.println(x));
```

👉 becomes:

```java
list.forEach(System.out::println);
```

---

# 🔥 7. Where Used?

---

## ✅ Streams API

```java
list.stream()
    .map(String::toUpperCase)
    .forEach(System.out::println);
```

---

## ✅ Functional Interfaces

* `Function`
* `Consumer`
* `Supplier`
* `Predicate`

---

# ⚠️ 8. Limitations

---

## ❌ 1. Cannot handle complex logic

```java
x -> {
    int y = x + 1;
    return y * 2;
}
```

👉 Cannot convert to method reference

---

## ❌ 2. Needs exact signature match

---

## ❌ 3. Harder to read in complex generics

---

# 🔥 9. Internal Working (Interview Level)

Method reference is compiled into:

👉 **Lambda + invokedynamic**

So internally:

```java
System.out::println
```

becomes something like:

```java
x -> System.out.println(x)
```

---

# 🔥 10. Key Interview Traps

---

### ⚠️ Trap 1: Confusing instance vs static

```java
String::valueOf  // static
String::length   // instance
```

---

### ⚠️ Trap 2: Hidden parameter passing

```java
String::toUpperCase
```

👉 Actually:

```java
s -> s.toUpperCase()
```

---

### ⚠️ Trap 3: Overloaded methods

Method reference may become **ambiguous**

---

# 🧠 FINAL MEMORY RULE

```text
Method Reference = Lambda without logic
```

---

# 💥 INTERVIEW KILLER LINE

👉 “Method references in Java are a concise form of lambda expressions that allow direct reuse of existing methods when their signature matches a functional interface, improving readability and maintainability.”

---

---

# 🔥 Method References Deep Dive (Theory + Traps)

---

## 🧠 Q1. What is a Method Reference?

### Options:

1. A pointer to a variable
2. A shorthand for lambda expressions
3. A way to create threads
4. A new Java keyword

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 Method reference is a **shortcut for lambda expressions** when method logic already exists

Example:

```java
x -> System.out.println(x)
```

becomes:

```java
System.out::println
```

</details>

---

## 🧠 Q2. Why are Method References required?

### Options:

1. Improve performance
2. Reduce memory usage
3. Improve readability and reuse existing methods
4. Mandatory for streams

<details>
<summary>Show Answer</summary>

**Correct Answer: 3**

👉 They make code:

* Cleaner
* More readable
* Reusable

</details>

---

## 🧠 Q3. What is the syntax of Method Reference?

### Options:

1. `object.method()`
2. `Class.method()`
3. `Class::method`
4. `method::Class`

<details>
<summary>Show Answer</summary>

**Correct Answer: 3**

👉 General syntax:

```java
ClassName::methodName
```

</details>

---

# 🔥 TYPES OF METHOD REFERENCES

---

## 🧠 Q4. Identify the type

```java
Integer::parseInt
```

### Options:

1. Instance method reference
2. Static method reference
3. Constructor reference
4. Lambda

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 `Class::staticMethod`

</details>

---

## 🧠 Q5. Identify the type

```java
System.out::println
```

### Options:

1. Static method
2. Instance method of a specific object
3. Constructor reference
4. Invalid

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 `object::instanceMethod`

</details>

---

## 🧠 Q6. Identify the type

```java
String::new
```

### Options:

1. Static method
2. Instance method
3. Constructor reference
4. Lambda

<details>
<summary>Show Answer</summary>

**Correct Answer: 3**

👉 `Class::new`

</details>

---

# 🔥 DEEP UNDERSTANDING (IMPORTANT)

---

## 🧠 Q7. What is the lambda equivalent?

```java
list.forEach(System.out::println);
```

### Options:

1. `() -> println()`
2. `x -> System.out.println(x)`
3. `System.out.println(x)`
4. Invalid

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 Method reference replaces lambda

</details>

---

## 🧠 Q8. When can we NOT use method reference?

### Options:

1. When method exists
2. When lambda has multiple statements
3. When functional interface is used
4. When using streams

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 Method reference works only for **single method call**

</details>

---

## 🧠 Q9. What is required for method reference to work?

### Options:

1. Same method name
2. Matching method signature
3. Same class
4. Static method only

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 Functional interface method signature must match

</details>

---

# 🔥 TRICKY CONCEPTS (INTERVIEW LEVEL)

---

## 🧠 Q10. What happens here?

```java
Function<String, Integer> f = Integer::parseInt;
```

### Options:

1. Compile error
2. Works fine
3. Runtime error
4. Needs lambda

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 Signature matches:
`String → int`

</details>

---

## 🧠 Q11. What is happening here?

```java
Function<String, String> f = String::toUpperCase;
```

### Options:

1. Static call
2. Instance method reference
3. Constructor reference
4. Compile error

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 Special case:
`Class::instanceMethod` → object passed automatically

Equivalent:

```java
s -> s.toUpperCase()
```

</details>

---

## 🧠 Q12. What is the output?

```java
List<String> list = List.of("a","b");

list.stream()
    .map(String::toUpperCase)
    .forEach(System.out::print);
```

### Options:

1. ab
2. AB
3. AaBb
4. Random

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 Converts each element to uppercase

</details>

---

# 🔥 ADVANCED TYPE (VERY IMPORTANT)

---

## 🧠 Q13. What type is this?

```java
BiFunction<String, String, String> f = String::concat;
```

### Options:

1. Static
2. Instance method of object
3. Instance method of arbitrary object
4. Constructor

<details>
<summary>Show Answer</summary>

**Correct Answer: 3**

👉 Special case:
`Class::instanceMethod` with multiple args

Equivalent:

```java
(s1, s2) -> s1.concat(s2)
```

</details>

---

# 🧠 FINAL MEMORY RULES

<details>
<summary>Show Rules</summary>

```text
1. Method reference = shortcut of lambda
2. Syntax = Class::method
3. Must match functional interface signature
4. Types:
   - Class::staticMethod
   - object::instanceMethod
   - Class::instanceMethod (special case)
   - Class::new
```

</details>

---

# 💥 INTERVIEW KILLER LINE

<details>
<summary>Show Answer</summary>

👉 “Method references in Java are syntactic sugar over lambda expressions, enabling cleaner and more readable code when an existing method implementation matches a functional interface signature.”

</details>

---

---

# 🔥 Tricky Edge-Case MCQs on Method References

---

## 🧠 Q1. What happens here?

```java
Function<String, Integer> f = String::length;
System.out.println(f.apply("hello"));
```

### Options:

1. Compile error
2. Runtime error
3. Prints 5
4. Prints null

<details>
<summary>Show Answer</summary>

**Correct Answer: 3**

👉 `String::length` → `s -> s.length()`

</details>

---

## 🧠 Q2. What happens here?

```java
Supplier<String> s = String::new;
System.out.println(s.get());
```

### Options:

1. Compile error
2. Runtime exception
3. Prints empty string
4. Prints null

<details>
<summary>Show Answer</summary>

**Correct Answer: 3**

👉 Default constructor → empty string

</details>

---

## 🧠 Q3. What is the issue?

```java
Function<String, Integer> f = Integer::valueOf;
```

### Options:

1. Compile error
2. Works fine
3. Runtime exception
4. Ambiguous method reference

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 Autoboxing handles conversion

</details>

---

## 🧠 Q4. What happens here?

```java
BiFunction<String, String, String> f = String::concat;
System.out.println(f.apply("A", "B"));
```

### Options:

1. Compile error
2. AB
3. BA
4. Runtime error

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 Equivalent: `(a, b) -> a.concat(b)`

</details>

---

## 🧠 Q5. What happens here?

```java
List<String> list = Arrays.asList("a", "b", "c");

list.stream()
    .map(String::toUpperCase)
    .forEach(System.out::print);
```

### Options:

1. abc
2. ABC
3. AaBbCc
4. Random

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

</details>

---

## 🧠 Q6. What happens here?

```java
Function<String, String> f = String::toUpperCase;
System.out.println(f.apply(null));
```

### Options:

1. Compile error
2. NullPointerException
3. Prints null
4. Empty string

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 Equivalent: `null.toUpperCase()`

</details>

---

## 🧠 Q7. What is the problem here?

```java
Function<String, Integer> f = Integer::parseInt;
System.out.println(f.apply("abc"));
```

### Options:

1. Compile error
2. Runtime exception
3. Prints 0
4. Prints null

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 NumberFormatException

</details>

---

## 🧠 Q8. What happens here?

```java
BiFunction<List<String>, String, Boolean> f = List::contains;
System.out.println(f.apply(List.of("A","B"), "A"));
```

### Options:

1. Compile error
2. true
3. false
4. Runtime error

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 Equivalent: `(list, val) -> list.contains(val)`

</details>

---

## 🧠 Q9. What happens here?

```java
Function<String, String> f = String::trim;
System.out.println(f.apply("  hi  "));
```

### Options:

1. "  hi  "
2. "hi"
3. Compile error
4. Runtime error

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

</details>

---

## 🧠 Q10. What happens here?

```java
Consumer<String> c = System.out::println;
c.accept(null);
```

### Options:

1. Compile error
2. Runtime exception
3. Prints null
4. Nothing happens

<details>
<summary>Show Answer</summary>

**Correct Answer: 3**

👉 println handles null safely

</details>

---

## 🧠 Q11. What is the issue?

```java
Function<Integer, Integer> f = Math::max;
```

### Options:

1. Compile error
2. Works fine
3. Runtime error
4. Ambiguous

<details>
<summary>Show Answer</summary>

**Correct Answer: 1**

👉 `Math.max` needs 2 arguments → mismatch

</details>

---

## 🧠 Q12. What happens here?

```java
BinaryOperator<Integer> op = Integer::sum;
System.out.println(op.apply(2, 3));
```

### Options:

1. Compile error
2. 5
3. 6
4. Runtime error

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

</details>

---

## 🧠 Q13. What happens here?

```java
Function<String, Integer> f = Integer::new;
```

### Options:

1. Compile error
2. Works fine
3. Runtime error
4. Deprecated usage warning

<details>
<summary>Show Answer</summary>

**Correct Answer: 4**

👉 Constructor exists but deprecated

</details>

---

## 🧠 Q14. What happens here?

```java
List<String> list = List.of("a", "bb", "ccc");

list.stream()
    .map(String::length)
    .forEach(System.out::print);
```

### Options:

1. 123
2. 6
3. Compile error
4. Runtime error

<details>
<summary>Show Answer</summary>

**Correct Answer: 1**

👉 lengths printed individually

</details>

---

## 🧠 Q15. What is tricky here?

```java
Predicate<String> p = String::isEmpty;
System.out.println(p.test(""));
```

### Options:

1. Compile error
2. true
3. false
4. Runtime error

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

</details>

---

# 🧠 FINAL EDGE-CASE RULES

<details>
<summary>Show Rules</summary>

```text
1. Method reference fails if signature mismatch
2. Null input → NPE for instance methods
3. Overloaded methods → ambiguity risk
4. Constructor reference may be deprecated
5. Instance methods → object passed implicitly
```

</details>

---

# 💥 INTERVIEW KILLER INSIGHT

<details>
<summary>Show Answer</summary>

👉 “Most tricky bugs in method references come from signature mismatches, null handling in instance methods, and ambiguity due to overloaded methods.”

</details>

---
