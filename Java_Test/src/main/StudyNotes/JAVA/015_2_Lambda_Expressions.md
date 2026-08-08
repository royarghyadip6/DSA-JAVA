# 15.2 Lambda Expressions

## Lambda Expressions

### Basics

---

# 1. What is Lambda?

<details>
<summary>Show Answer</summary>

**Answer:**

A **lambda expression** is a **short, anonymous function**—a compact way to implement a **functional interface** without writing a full anonymous class.

### Syntax

```text
(parameters) -> expression
(parameters) -> { statements; }
```

### Example

```java
// Lambda represents: add two numbers
(a, b) -> a + b

// Used with functional interface
BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
System.out.println(add.apply(5, 3)); // 8
```

### What Lambda Is NOT

| Lambda Is | Lambda Is NOT |
|-----------|---------------|
| Implementation of functional interface | A standalone function |
| Anonymous (no name) | A new language feature like JS functions |
| Compile-time construct | Independent of interfaces |

### Real Examples

```java
// Runnable
Runnable task = () -> System.out.println("Running");

// Comparator
list.sort((a, b) -> a.compareTo(b));

// Predicate
Predicate<Integer> isEven = n -> n % 2 == 0;

// Stream
list.stream().filter(x -> x > 10).forEach(System.out::println);
```

### Key Properties

```text
✅ No access modifier
✅ No return type declaration
✅ No method name
✅ Must target a functional interface
✅ Arrow operator -> separates params from body
```

**Interview Point:**

> Lambda = **compact implementation of a functional interface**. It's not a method—it's an expression assigned to a functional interface type.

</details>

---

# 2. Why Lambda introduced?

<details>
<summary>Show Answer</summary>

**Answer:**

Lambdas were introduced in **Java 8** to enable **functional programming** and eliminate verbose **anonymous inner classes** for passing behavior.

### Problems Before Java 8

```java
// Passing behavior required anonymous class — lots of boilerplate
Thread t = new Thread(new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello");
    }
});

list.sort(new Comparator<String>() {
    @Override
    public int compare(String a, String b) {
        return a.compareTo(b);
    }
});
```

### What Lambda Enabled

```java
Thread t = new Thread(() -> System.out.println("Hello"));
list.sort((a, b) -> a.compareTo(b));
```

### Why Java Needed Lambdas

| Goal | How Lambda Helps |
|------|------------------|
| **Less boilerplate** | 5 lines → 1 line |
| **Functional programming** | Pass behavior as data |
| **Streams API** | Lambdas power every stream operation |
| **Parallel processing** | Easy to pass tasks to threads/pools |
| **Readable code** | Declarative style over imperative |

### Functional Programming Shift

```text
Before: "how to do it" (loops, anonymous classes)
After:  "what to do"   (filter, map, reduce with lambdas)
```

```java
// Before — imperative
List<String> result = new ArrayList<>();
for (String s : list) {
    if (s.length() > 5) {
        result.add(s.toUpperCase());
    }
}

// After — functional with lambda
List<String> result = list.stream()
    .filter(s -> s.length() > 5)
    .map(s -> s.toUpperCase())
    .collect(Collectors.toList());
```

### Ecosystem Enablement

```text
Lambda → Functional Interfaces → Streams → Optional → CompletableFuture
```

Without lambdas, none of the major Java 8 APIs would be practical to use.

**Interview Point:**

> Lambdas were introduced to reduce boilerplate, enable functional programming, and power Streams API—not just as syntactic sugar.

</details>

---

# 3. Benefits over anonymous class?

<details>
<summary>Show Answer</summary>

**Answer:**

Lambdas are **shorter, faster, and cleaner** than anonymous inner classes for implementing functional interfaces.

### Comparison Table

| Feature | Lambda | Anonymous Class |
|---------|--------|-----------------|
| **Syntax** | Short, concise | Verbose boilerplate |
| **Lines of code** | 1 line typical | 5+ lines typical |
| **`this` keyword** | Refers to **outer class** | Refers to **anonymous class** |
| **Class file** | No new `.class` file | Creates `Outer$1.class` |
| **Performance** | Better (invokedynamic) | Slightly slower |
| **Target** | Functional interface only | Any interface/abstract class |
| **Constructor** | Cannot have | Can call super constructor |
| **Fields** | Cannot have | Can have fields |

### Code Comparison

```java
// Anonymous class
Runnable r1 = new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello");
    }
};

// Lambda
Runnable r2 = () -> System.out.println("Hello");
```

### `this` Keyword Difference (Important)

```java
class Outer {
    void test() {
        Runnable anonymous = new Runnable() {
            public void run() {
                System.out.println(this); // Anonymous class instance
            }
        };

        Runnable lambda = () -> {
            System.out.println(this); // Outer class instance
        };
    }
}
```

### Memory Benefit

```text
Anonymous class: compiles to separate class file per usage
Lambda: no extra class file — JVM generates implementation at runtime
```

### When Anonymous Class Still Needed

```java
// Multiple methods — NOT functional interface
abstract class Handler {
    abstract void onSuccess();
    abstract void onFailure();
}

// Must use anonymous class or regular class
Handler h = new Handler() {
    void onSuccess() { }
    void onFailure() { }
};

// Abstract class with constructor logic
Thread t = new Thread() {
    @Override
    public void run() {
        super.run(); // call parent
    }
};
```

**Interview Point:**

> Lambda wins on syntax, performance, and memory for **single-method interfaces**. Anonymous class still needed for multiple abstract methods or abstract classes.

</details>

---

### Intermediate

---

# 4. Syntax of Lambda?

<details>
<summary>Show Answer</summary>

**Answer:**

Lambda syntax follows the pattern: **parameters → arrow → body**.

### General Form

```text
(parameters) -> expression
(parameters) -> { statements; }
```

### All Syntax Variations

| Case | Syntax | Example |
|------|--------|---------|
| No params | `() -> body` | `() -> System.out.println("Hi")` |
| One param | `x -> body` | `x -> x * 2` |
| One param (brackets) | `(x) -> body` | `(x) -> x * 2` |
| Multiple params | `(a, b) -> body` | `(a, b) -> a + b` |
| Expression body | `-> expression` | `x -> x * 2` (auto-return) |
| Block body | `-> { stmts; }` | `(a,b) -> { return a+b; }` |
| Typed params | `(int a, int b) ->` | `(int a, int b) -> a + b` |

### No Parameters

```java
Supplier<String> s = () -> "Hello";
Runnable r = () -> System.out.println("Running");
```

### One Parameter

```java
Function<String, Integer> len = s -> s.length();
Consumer<String> print = (s) -> System.out.println(s);
// brackets optional for single param
```

### Multiple Parameters

```java
BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
Comparator<String> cmp = (s1, s2) -> s1.compareTo(s2);
// brackets REQUIRED for multiple params
```

### Expression Body (Implicit Return)

```java
// Last expression is automatically returned
(a, b) -> a + b
x -> x * x
s -> s.toUpperCase()
```

### Block Body (Explicit Return)

```java
(a, b) -> {
    int sum = a + b;
    return sum;  // return required in block body
}

(x) -> {
    System.out.println(x);
    return x * 2;
}
```

### Type Inference

```java
// Types inferred from context
Comparator<String> c = (a, b) -> a.compareTo(b);

// Explicit types (rarely needed)
Comparator<String> c = (String a, String b) -> a.compareTo(b);
```

### Invalid Syntax

```java
// ❌ No target type
(a, b) -> a + b;

// ❌ Multiple statements without block
(a, b) -> a + b; return a; // error

// ❌ Multiple params without brackets
a, b -> a + b; // error

// ✅ Correct
BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
```

**Interview Point:**

> Single param = brackets optional. Multiple params = brackets required. Expression body = implicit return. Block body = explicit `return` needed.

</details>

---

# 5. Variable capture?

<details>
<summary>Show Answer</summary>

**Answer:**

**Variable capture** means a lambda can **access variables from its enclosing scope**—local variables, instance fields, and static fields.

### What Can Be Captured

| Variable Type | Can Capture? | Can Modify? |
|---------------|--------------|-------------|
| Local variable | ✅ Yes | ❌ No (effectively final) |
| Instance field | ✅ Yes | ✅ Yes |
| Static field | ✅ Yes | ✅ Yes |
| Method parameter | ✅ Yes | ❌ No (effectively final) |

### Local Variable Capture

```java
int multiplier = 10; // effectively final

Function<Integer, Integer> times = x -> x * multiplier;
System.out.println(times.apply(5)); // 50

// multiplier = 20; // ❌ if changed, lambda above won't compile
```

### Instance Field Capture

```java
class Calculator {
    int factor = 10;

    void test() {
        Function<Integer, Integer> fn = x -> x * factor;
        factor = 20; // ✅ OK — instance field can change
        System.out.println(fn.apply(5)); // uses current factor
    }
}
```

### Static Field Capture

```java
static int count = 0;

Runnable r = () -> {
    count++; // ✅ OK — static field modifiable
    System.out.println(count);
};
```

### Why Local Variables Cannot Be Modified

```text
Lambda may execute later (another thread, async)
Local variable may no longer exist on stack
Solution: JVM copies value into lambda — must be final snapshot
```

```java
int x = 10;
Runnable r = () -> System.out.println(x);
// JVM effectively stores copy of x = 10 inside lambda
// If x could change, copy would be stale — unsafe
```

### Capture vs No Capture

```java
// Captures local variable
int base = 100;
Supplier<Integer> s = () -> base;

// Does not capture — uses instance field directly
Supplier<Integer> s2 = () -> this.instanceField;
```

**Interview Point:**

> Lambdas capture **effectively final** locals (read-only copy). Instance and static fields are accessed directly and can be modified.

</details>

---

# 6. Effectively final variable?

<details>
<summary>Show Answer</summary>

**Answer:**

An **effectively final** variable is a local variable that is **never reassigned** after initialization—even without the `final` keyword.

### Definition

```text
Variable is assigned once
    ↓
Never modified after that
    ↓
Effectively final — lambda can use it
```

### Valid — Effectively Final

```java
int x = 10;           // assigned once, never changed
String name = "Java"; // effectively final

Runnable r = () -> System.out.println(x + name); // ✅
```

### Invalid — Not Effectively Final

```java
int x = 10;
x = 20; // reassigned — no longer effectively final

Runnable r = () -> System.out.println(x); // ❌ compile error
```

### Explicit final vs Effectively Final

```java
// Both work in lambda
final int a = 10;
int b = 20; // effectively final

Consumer<Void> c = x -> {
    System.out.println(a + b); // both OK
};
```

### Loop Variable Trap

```java
List<Runnable> tasks = new ArrayList<>();

for (int i = 0; i < 3; i++) {
    tasks.add(() -> System.out.println(i)); // ❌ i is not effectively final
}

// Fix — capture in effectively final local
for (int i = 0; i < 3; i++) {
    int copy = i;
    tasks.add(() -> System.out.println(copy)); // ✅
}
```

### Why This Rule Exists

```text
1. Thread safety — lambda may run on another thread
2. Local vars live on stack — may be gone when lambda runs
3. JVM copies value at lambda creation — must be stable snapshot
4. Prevents confusing bugs from mutated shared state
```

### Effectively Final in Other Contexts

```java
// Also required for inner classes (pre-Java 8)
class Outer {
    void test() {
        int x = 10;
        new Thread(new Runnable() {
            public void run() {
                System.out.println(x); // x must be effectively final
            }
        }).start();
    }
}
```

**Interview Point:**

> Effectively final = assigned once, never changed. Required for local variables in lambdas and anonymous classes. Instance/static fields are exempt.

</details>

---

### Advanced

---

# 7. How Lambda works internally?

<details>
<summary>Show Answer</summary>

**Answer:**

At compile time, lambdas are **not converted to anonymous classes** (in modern Java). Instead, the compiler emits an **`invokedynamic`** instruction that bootstraps a lambda factory at runtime.

### Internal Flow

```text
Lambda source code
    ↓
Compiler generates invokedynamic + bootstrap method
    ↓
Runtime: LambdaMetafactory creates functional interface instance
    ↓
Lambda executes when functional interface method is called
```

### Compile Time

```java
// Source
Runnable r = () -> System.out.println("Hello");

// Compiler does NOT create Runnable$1.class (unlike anonymous class)
// Instead generates invokedynamic in bytecode
```

### Bytecode (Conceptual)

```text
invokedynamic run()Ljava/lang/Runnable;
  BootstrapMethods:
    #0: lambda$main$0()  // synthetic bootstrap method
```

### Runtime — LambdaMetafactory

```java
// JVM internally (simplified concept)
LambdaMetafactory.metafactory(
    caller,
    "run",           // functional interface method
    () -> System.out.println("Hello")  // lambda body
);
// Returns Runnable instance — no separate class file
```

### vs Anonymous Class Internals

| | Lambda (Java 8+) | Anonymous Class |
|---|------------------|-----------------|
| Bytecode | `invokedynamic` | `new InnerClass()` |
| Class file | None (or synthetic) | `Outer$1.class` |
| Creation | Lazy at runtime | At object creation |
| Metafactory | `LambdaMetafactory` | Normal class instantiation |

### Serialized Lambdas

```java
// Serializable functional interface
Runnable r = (Runnable & Serializable) () -> System.out.println("Hi");
// Generates different bootstrap — SerializedLambda class used
```

### Bridge Methods

```java
// When lambda targets interface with generics
Comparator<String> cmp = (a, b) -> a.compareTo(b);
// Compiler may generate bridge methods for type erasure compatibility
```

**Interview Point:**

> Modern Java: lambda → **invokedynamic** → **LambdaMetafactory** → functional interface instance. No anonymous class file per lambda usage.

</details>

---

# 8. invokedynamic instruction?

<details>
<summary>Show Answer</summary>

**Answer:**

`invokedynamic` is a **JVM bytecode instruction** (added in Java 7) that **dynamically links a method call at runtime**—used by Java 8 lambdas, Java 7 dynamic languages, and string concatenation (Java 9+).

### What It Does

```text
Traditional invoke*: target method known at compile time
invokedynamic: target method resolved at RUNTIME via bootstrap method
```

### Other invoke Instructions

| Instruction | Purpose |
|-------------|---------|
| `invokevirtual` | Instance method call |
| `invokestatic` | Static method call |
| `invokeinterface` | Interface method call |
| `invokespecial` | Constructor, private, super |
| **`invokedynamic`** | **Runtime-determined call site** |

### Lambda Usage Flow

```text
1. Compiler writes invokedynamic in bytecode
2. Bootstrap method (synthetic) provided in class
3. First execution: JVM calls bootstrap method
4. Bootstrap returns CallSite (linked method handle)
5. Subsequent calls: direct invocation — fast path
```

### Example — What Compiler Generates

```java
// Source
Supplier<String> s = () -> "Hello";

// Bytecode (simplified)
invokedynamic get()Ljava/util/function/Supplier;
  // bootstrap: LambdaMetafactory.metafactory(...)
  // sam method type: ()Ljava/lang/String;
```

### Bootstrap Method

```java
// Synthetic method generated by compiler (not in source)
private static Object lambda$main$0(
    Lookup lookup, String name, Type type,
    MethodType samMethodType, MethodType implMethodType,
  MethodHandle implMethod) {
    return LambdaMetafactory.metafactory(
        lookup, name, samMethodType, implMethodType, implMethod, implMethodType);
}
```

### Why invokedynamic for Lambdas?

| Benefit | Explanation |
|---------|-------------|
| **Lazy linking** | Implementation created only when needed |
| **No class explosion** | No `Outer$1.class`, `Outer$2.class` per lambda |
| **Optimization** | JVM can inline and optimize call site |
| **Flexibility** | Same mechanism for lambdas, method handles, string concat |

### Other Uses of invokedynamic

```text
Java 8  → Lambda expressions
Java 7  → Dynamic language support (JRuby, Nashorn)
Java 9+ → String concatenation (instead of StringBuilder chain)
Java 11+→ Constant dynamic (condy)
```

**Interview Point:**

> `invokedynamic` = runtime method linking. Lambdas use it instead of generating anonymous classes—bootstrap via `LambdaMetafactory.metafactory()`.

</details>

---

# 9. Lambda memory optimization?

<details>
<summary>Show Answer</summary>

**Answer:**

Lambdas are **more memory-efficient** than anonymous inner classes because they avoid generating separate `.class` files and can **share implementations** across lambda instances.

### Anonymous Class Memory Cost

```text
Each anonymous class usage:
  → New class file: Outer$1.class, Outer$2.class, ...
  → Loaded into Metaspace (permanent until unloaded)
  → Each instance = separate object on heap
```

```java
// Creates Outer$1.class in Metaspace
Runnable r1 = new Runnable() { public void run() { } };

// Creates Outer$2.class — another Metaspace entry
Runnable r2 = new Runnable() { public void run() { } };
```

### Lambda Memory Model

```text
No per-usage class file (usually)
  → invokedynamic + LambdaMetafactory
  → Implementation generated at runtime
  → Identical lambdas may SHARE same implementation object
```

```java
// Both may share same lambda implementation
Runnable r1 = () -> System.out.println("Hi");
Runnable r2 = () -> System.out.println("Hi");
```

### Memory Comparison

| Aspect | Anonymous Class | Lambda |
|--------|-----------------|--------|
| Class files | One per usage site | None (or shared synthetic) |
| Metaspace | More classes loaded | Fewer classes |
| Captured vars | Inner class holds refs | Serialized in lambda object |
| Identical lambdas | Separate classes | Can share implementation |

### When Lambda Captures Variables

```java
int x = 10;
Runnable r = () -> System.out.println(x);

// Lambda object stores COPY of x (or reference to synthetic field)
// Still lighter than full inner class with synthetic accessors
```

### Capturing vs Non-Capturing Lambdas

```text
Non-capturing lambda: () -> System.out.println("static")
  → No extra fields — minimal memory
  → Can be singleton — shared across all usages

Capturing lambda: () -> System.out.println(x)
  → Stores captured variable values
  → One object per distinct capture context
```

### Performance + Memory Together

```text
invokedynamic call site:
  → First call: bootstrap (slow once)
  → Later calls: optimized direct call (fast)
  → JVM may inline lambda body — no object allocation at all!
```

### JVM Optimization — Lambda Elision

```java
list.forEach(x -> System.out.println(x));
// JVM may optimize to loop without creating Runnable object
// Especially with non-capturing lambdas
```

**Interview Point:**

> Lambdas save Metaspace (no anonymous class files), can share implementations, and JVM may **inline** non-capturing lambdas eliminating object allocation entirely.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Can lambda access instance variables?

<details>
<summary>Show Answer</summary>

**Answer:** Yes. Instance fields can be read and modified inside lambdas. `this` refers to the enclosing class instance.

</details>

---

### Q: Can lambda throw checked exceptions?

<details>
<summary>Show Answer</summary>

**Answer:** Only if the functional interface method declares it. `Runnable.run()` cannot throw checked exceptions. `Callable.call()` can. Otherwise wrap in try-catch inside lambda body.

</details>

---

### Q: Lambda without functional interface — possible?

<details>
<summary>Show Answer</summary>

**Answer:** No. Lambda must have a **target type** (functional interface). Standalone `(a,b) -> a+b` without assignment causes compile error.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Lambda = compact functional interface implementation. **Effectively final** locals only. Internally: **invokedynamic** + **LambdaMetafactory**—no anonymous class files, better memory and performance than pre-Java 8 anonymous classes.

</details>
