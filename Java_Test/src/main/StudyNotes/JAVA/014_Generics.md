# 14. Generics

## Generics

## Basics

---

# 1. What are Generics?

<details>
<summary>Show Answer</summary>

**Answer:**

**Generics** allow classes, interfaces, and methods to operate on **types as parameters**—enabling **type-safe** code without casting.

### Without Generics (Pre-Java 5)

```java
List list = new ArrayList();
list.add("Java");
list.add(123);           // no compile-time check
String s = (String) list.get(1); // ClassCastException at runtime!
```

### With Generics

```java
List<String> list = new ArrayList<>();
list.add("Java");
list.add(123);           // ❌ compile-time error
String s = list.get(0);  // no cast needed
```

### Generic Syntax

```java
// Generic class
class Box<T> {
    private T value;
    void set(T value) { this.value = value; }
    T get() { return value; }
}

// Generic interface
interface Repository<T> {
    T findById(Long id);
}

// Generic method
public <T> T getFirst(List<T> list) {
    return list.isEmpty() ? null : list.get(0);
}
```

### Type Parameters

| Symbol | Common Use |
|--------|------------|
| `T` | Type |
| `E` | Element (collections) |
| `K` | Key (maps) |
| `V` | Value (maps) |
| `N` | Number |
| `R` | Return type |

**Interview Point:**

> Generics = parameterized types. Compiler checks type safety at compile time—catches bugs before runtime.

</details>

---

# 2. Why Generics introduced?

<details>
<summary>Show Answer</summary>

**Answer:**

Generics were introduced in **Java 5** to solve problems with **raw types** in the Collections Framework.

### Problems Before Generics

| Problem | Example |
|---------|---------|
| **No type safety** | Anything could be added to a `List` |
| **Casting required** | `(String) list.get(0)` on every read |
| **Runtime errors** | `ClassCastException` only at runtime |
| **Code duplication** | Separate classes for `IntList`, `StringList`, etc. |

### Example — The Old Problem

```java
List list = new ArrayList();
list.add("Hello");
Integer i = (Integer) list.get(0); // compiles! crashes at runtime
```

### What Generics Solved

```text
Raw types          → Type-safe collections
Runtime casting    → Compile-time checking
ClassCastException → Errors caught by compiler
Duplicate code     → One generic class for all types
```

### Design Goals

1. **Type safety** — catch errors at compile time
2. **Eliminate casts** — cleaner, safer code
3. **Enable generic algorithms** — one method works for all types
4. **Backward compatibility** — raw types still compile (with warnings)

### Real Motivation

```java
// Before: every collection was raw
Map map = new HashMap();
map.put("key", "value");
String val = (String) map.get("key"); // cast everywhere

// After: type-safe from the start
Map<String, String> map = new HashMap<>();
String val = map.get("key"); // no cast
```

**Interview Point:**

> Generics were added primarily for Collections—eliminate casts and catch type errors at compile time instead of `ClassCastException` at runtime.

</details>

---

# 3. Benefits of Generics?

<details>
<summary>Show Answer</summary>

**Answer:**

| Benefit | Description |
|---------|-------------|
| **Type safety** | Compiler prevents invalid type operations |
| **No casting** | Cleaner code, fewer `ClassCastException` |
| **Code reuse** | One generic class/method for all types |
| **Better readability** | Type intent visible in declaration |
| **IDE support** | Autocomplete knows actual types |

### 1. Type Safety

```java
List<Integer> numbers = new ArrayList<>();
numbers.add(10);        // ✅
numbers.add("ten");     // ❌ compile error
```

### 2. Eliminate Casting

```java
// Without generics
List list = new ArrayList();
String s = (String) list.get(0);

// With generics
List<String> list = new ArrayList<>();
String s = list.get(0); // no cast
```

### 3. Code Reuse

```java
// One Pair class works for any two types
Pair<String, Integer> nameAge = new Pair<>("John", 30);
Pair<Integer, Double> score = new Pair<>(95, 98.5);
```

### 4. Generic Algorithms

```java
public static <T> void swap(List<T> list, int i, int j) {
    T temp = list.get(i);
    list.set(i, list.get(j));
    list.set(j, temp);
}

// Works for List<String>, List<Integer>, List<Employee>, etc.
```

### 5. Stronger APIs

```java
// API clearly states what it returns
Optional<Employee> findEmployee(Long id);
Map<String, List<Order>> ordersByCustomer();
```

**Interview Point:**

> Type safety + no casts + reusable algorithms. Generics make APIs self-documenting and safer.

</details>

---

# 4. Compile-time type safety?

<details>
<summary>Show Answer</summary>

**Answer:**

**Compile-time type safety** means the compiler **verifies types before the program runs**—preventing invalid operations and catching bugs early.

### How It Works

```text
Source code with generics
    ↓
Compiler checks all type operations
    ↓
Invalid code → compile error (never runs)
Valid code   → bytecode (with type erasure)
```

### Example — Compile-Time Catch

```java
List<String> names = new ArrayList<>();
names.add("Alice");
names.add(42); // ❌ Compile error: incompatible types int cannot be converted to String
```

### Example — No Unsafe Cast at Runtime

```java
List<String> list = new ArrayList<>();
list.add("Java");

// Without generics this would compile but crash:
// Integer i = (Integer) list.get(0);

String s = list.get(0); // compiler knows it's String
```

### Compile-Time vs Runtime

| | Compile-Time (Generics) | Runtime (Raw Types) |
|---|-------------------------|---------------------|
| Type check | At compilation | At execution |
| Error type | Compile error | `ClassCastException` |
| When caught | Before deploy | In production |

### Bounded Type Safety

```java
List<? extends Number> numbers = new ArrayList<Integer>();
numbers.add(10); // ❌ compile error — can't add (only read safely)
Number n = numbers.get(0); // ✅ safe read
```

### Limitation — Type Erasure

```java
// Compiler catches this:
if (list instanceof List<String>) { } // ❌ compile error

// But erasure means runtime has no generic info:
List<String> strings = new ArrayList<>();
List<Integer> ints = new ArrayList<>();
strings.getClass() == ints.getClass(); // true — both are ArrayList at runtime
```

**Interview Point:**

> Generics provide compile-time safety, not runtime safety. Compiler is your guard; type info is erased before JVM runs the code.

</details>

---

## Intermediate

---

# 5. What is Type Erasure?

<details>
<summary>Show Answer</summary>

**Answer:**

**Type erasure** is the process where the compiler **removes all generic type information** from bytecode—replacing type parameters with their **bounds** or **`Object`**.

### What Happens

```text
Compile time:  List<String>
Bytecode:      List  (String erased)
```

### Example

```java
// Source code
List<String> list = new ArrayList<>();
list.add("Java");
String s = list.get(0);
```

```java
// Effectively what bytecode sees (erased)
List list = new ArrayList();
list.add("Java");           // compiler inserted cast check
String s = (String) list.get(0); // compiler inserted cast
```

### Generic Class Erasure

```java
// Source
class Box<T> {
    T value;
    T get() { return value; }
}

// Erased to
class Box {
    Object value;
    Object get() { return value; }
}
```

### Bounded Type Erasure

```java
// Source
class NumberBox<T extends Number> {
    T value;
}

// Erased to
class NumberBox {
    Number value;  // bound replaces T
}
```

### Why Erasure Exists

* **Backward compatibility** — old Java 1.4 code could run with Java 5 libraries
* **No new runtime types** — `List<String>` and `List<Integer>` share same class

### Implications

```java
// ❌ Cannot do at runtime
new T();
T[] array = new T[10];
if (obj instanceof List<String>) { }

// ✅ Can do
if (obj instanceof List) { } // raw type check only
```

**Interview Point:**

> Type erasure = generics exist only for compiler. Runtime sees raw types + casts. That's why you can't `new T()` or `instanceof List<String>`.

</details>

---

# 6. Why Generic information removed at runtime?

<details>
<summary>Show Answer</summary>

**Answer:**

Generic type information is removed at runtime (**type erasure**) primarily for **backward compatibility** with pre-Java 5 code and libraries.

### Main Reasons

| Reason | Explanation |
|--------|-------------|
| **Backward compatibility** | Java 5 libraries must work with Java 1.4 clients |
| **No new VM types** | JVM doesn't need to know `List<String>` vs `List<Integer>` |
| **Migration path** | Existing bytecode and class files stay valid |
| **Simpler JVM** | No generic-aware runtime type system needed |

### Compatibility Example

```text
Java 1.4 app uses:     ArrayList list = new ArrayList();
Java 5 library returns: List<String> (erased to List at runtime)
Both work together — same bytecode shape
```

### What Would Happen Without Erasure

```text
List<String>  → separate runtime class
List<Integer> → separate runtime class
List<Employee> → separate runtime class
= explosion of classes, breaks binary compatibility
```

### Bridge Methods

Compiler generates **bridge methods** to maintain polymorphism after erasure:

```java
class StringBox extends Box<String> {
    String get() { return "hello"; }
}

// Compiler adds bridge method:
// Object get() { return get(); }  // bridges to String get()
```

### Reified Generics (Contrast)

Languages like **C#** have **reified generics**—type info kept at runtime. Java chose erasure for migration, not capability.

### Limited Runtime Info

Some generic info is retained via **reflection** in specific cases:

```java
class MyClass<T> {
    T field;
}

Field f = MyClass.class.getDeclaredField("field");
Type type = f.getGenericType(); // ParameterizedType — compile-time metadata in class file
```

**Interview Point:**

> Erasure = backward compatibility decision. Java 5 could ship without breaking millions of existing apps. Trade-off: no runtime generic types.

</details>

---

# 7. Generic Method?

<details>
<summary>Show Answer</summary>

**Answer:**

A **generic method** declares its own **type parameter** independent of the class—can be used in non-generic classes too.

### Syntax

```java
public <T> T methodName(T param) {
    return param;
}
// <T> before return type = type parameter declaration
```

### In Non-Generic Class

```java
public class Utils {

    // Generic method — class itself is not generic
    public static <T> T getFirst(List<T> list) {
        return list.isEmpty() ? null : list.get(0);
    }

    public static <T> void swap(List<T> list, int i, int j) {
        T temp = list.get(i);
        list.set(i, list.get(j));
        list.set(j, temp);
    }
}

String first = Utils.getFirst(Arrays.asList("a", "b"));
Integer num = Utils.getFirst(Arrays.asList(1, 2, 3));
```

### Multiple Type Parameters

```java
public static <K, V> V getValue(Map<K, V> map, K key) {
    return map.get(key);
}
```

### Bounded Generic Method

```java
public static <T extends Comparable<T>> T max(T a, T b) {
    return a.compareTo(b) >= 0 ? a : b;
}

max(10, 20);        // Integer
max("a", "b");      // String
```

### Generic Method vs Generic Class

| Generic Class | Generic Method |
|---------------|----------------|
| `class Box<T>` | `<T> T getFirst(List<T>)` |
| All instances share type T | Type per method call |
| `Box<String> box` | `getFirst(strings)` vs `getFirst(numbers)` |

### Inference

```java
// Compiler infers T = String
getFirst(Arrays.asList("a", "b"));

// Explicit type argument (rare)
Utils.<String>getFirst(list);
```

**Interview Point:**

> Generic method = `<T>` before return type. Works in any class. Compiler infers type from arguments—no need to specify usually.

</details>

---

# 8. Generic Class?

<details>
<summary>Show Answer</summary>

**Answer:**

A **generic class** declares one or more **type parameters** that apply to the **entire class**—fields, methods, and constructors use those types.

### Basic Example

```java
public class Box<T> {
    private T content;

    public void set(T content) {
        this.content = content;
    }

    public T get() {
        return content;
    }
}

Box<String> stringBox = new Box<>();
stringBox.set("Hello");
String s = stringBox.get();

Box<Integer> intBox = new Box<>();
intBox.set(42);
```

### Multiple Type Parameters

```java
public class Pair<K, V> {
    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() { return key; }
    public V getValue() { return value; }
}

Pair<String, Integer> pair = new Pair<>("age", 30);
```

### Bounded Generic Class

```java
public class NumberBox<T extends Number> {
    private T value;

    public double getDoubleValue() {
        return value.doubleValue(); // Number methods available
    }
}

NumberBox<Integer> box = new NumberBox<>();
NumberBox<String> box2 = new NumberBox<>(); // ❌ String doesn't extend Number
```

### Real Examples in Java

```java
ArrayList<E>
HashMap<K, V>
Optional<T>
CompletableFuture<T>
ThreadLocal<T>
```

### Raw Type (Avoid)

```java
Box box = new Box(); // raw type — compiles with warning, no type safety
box.set("hello");
box.set(123); // allowed — defeats generics
```

**Interview Point:**

> Generic class = type parameter on class declaration. One `Box<T>` replaces infinite type-specific classes. Always use parameterized types, never raw.

</details>

---

# 9. Generic Interface?

<details>
<summary>Show Answer</summary>

**Answer:**

A **generic interface** declares type parameters that implementing classes or lambdas must specify or infer.

### Basic Example

```java
public interface Repository<T> {
    T findById(Long id);
    void save(T entity);
    void delete(T entity);
}

public class UserRepository implements Repository<User> {
    @Override
    public User findById(Long id) { /* ... */ }

    @Override
    public void save(User entity) { /* ... */ }

    @Override
    public void delete(User entity) { /* ... */ }
}
```

### Multiple Type Parameters

```java
public interface Map<K, V> {
    V get(K key);
    void put(K key, V value);
}
```

### Generic Interface with Lambda (Java 8+)

```java
interface Transformer<T, R> {
    R transform(T input);
}

Transformer<String, Integer> length = s -> s.length();
Integer len = length.transform("Hello"); // 5
```

### Bounded Generic Interface

```java
interface Comparable<T extends Comparable<T>> {
    int compareTo(T other);
}

class Employee implements Comparable<Employee> {
  @Override
  public int compareTo(Employee other) { /* ... */ }
}
```

### Implementing with Concrete Type

```java
// Specific type — most common
class StringList implements List<String> { }

// Keep generic — for wrapper classes
class GenericList<T> implements List<T> { }
```

### Common Generic Interfaces

| Interface | Type Param | Use |
|-----------|------------|-----|
| `Comparable<T>` | Element type | Natural ordering |
| `Comparator<T>` | Compared type | Custom ordering |
| `Optional<T>` | Value type | Nullable wrapper |
| `Callable<V>` | Return type | Thread task result |
| `Function<T, R>` | Input, output | Lambda transform |

**Interview Point:**

> Generic interface = contract with type parameter. Implementer chooses concrete type (`Repository<User>`) or stays generic (`Repository<T>`).

</details>

---

## Wildcards

---

# 10. What is <?> ?

<details>
<summary>Show Answer</summary>

**Answer:**

`<?>` is an **unbounded wildcard**—represents a collection of **unknown type**. You can read as `Object` but cannot add (except `null`).

### Syntax

```java
List<?> list = new ArrayList<String>();
List<?> list2 = new ArrayList<Integer>();
// Both valid — unknown element type
```

### What You Can Do

```java
List<?> list = Arrays.asList("a", "b");

Object obj = list.get(0);  // ✅ read as Object
int size = list.size();    // ✅ non-type methods work
list.clear();              // ✅

list.add("x");  // ❌ compile error — can't add anything (except null)
list.add(null); // ✅ only null allowed
```

### Use Case — Method Accepting Any List

```java
public static void printList(List<?> list) {
    for (Object item : list) {
        System.out.println(item);
    }
}

printList(Arrays.asList("a", "b"));
printList(Arrays.asList(1, 2, 3));
```

### `<?>` vs `<Object>`

```java
List<Object> objectList = new ArrayList<>();
objectList.add("hello");  // ✅ can add any Object
objectList.add(42);

List<?> wildcardList = new ArrayList<String>();
wildcardList.add("hello"); // ❌ cannot add

// List<String> is NOT a List<Object>
List<Object> lo = new ArrayList<String>(); // ❌ compile error
List<?> lw = new ArrayList<String>();      // ✅ OK
```

### When to Use

| Use `<?>` | Use concrete type |
|-----------|-------------------|
| Read-only operations | Need to add elements |
| `printList`, `size`, `isEmpty` | `list.add(item)` |
| Maximum flexibility | Type-specific logic |

**Interview Point:**

> `<?>` = unknown type. Read as Object, write nothing (except null). Use for APIs that work with any List regardless of element type.

</details>

---

# 11. What is <? extends T> ?

<details>
<summary>Show Answer</summary>

**Answer:**

`<? extends T>` is an **upper-bounded wildcard**—accepts `T` or any **subtype of T**. Used when you **read** from the structure (producer).

### Syntax

```java
List<? extends Number> numbers;
numbers = new ArrayList<Integer>();  // ✅ Integer extends Number
numbers = new ArrayList<Double>();   // ✅
numbers = new ArrayList<String>(); // ❌ String is not a Number
```

### Read-Only (Mostly)

```java
List<? extends Number> list = new ArrayList<Integer>();
list.add(10);     // ❌ compile error — can't add (except null)
Number n = list.get(0); // ✅ read as Number
```

### Why Can't Add?

```text
List<? extends Number> could be List<Integer>
Adding Double to List<Integer> would break type safety
Compiler blocks all adds to be safe
```

### Example — Find Max

```java
public static double sum(List<? extends Number> numbers) {
    double total = 0;
    for (Number n : numbers) {
        total += n.doubleValue();
    }
    return total;
}

sum(Arrays.asList(1, 2, 3));       // List<Integer>
sum(Arrays.asList(1.5, 2.5));    // List<Double>
```

### extends vs implements

```java
// extends works for classes AND interfaces in generics
List<? extends Comparable<String>> list1;
List<? extends Runnable> list2;
```

### Common Pattern

```java
public static <T extends Comparable<T>> T max(List<T> list) {
    T max = list.get(0);
    for (T item : list) {
        if (item.compareTo(max) > 0) max = item;
    }
    return max;
}
```

**Interview Point:**

> `<? extends T>` = upper bound. Accepts T and subclasses. Read safely as T; cannot add (except null). **Producer extends** — source of data.

</details>

---

# 12. What is <? super T> ?

<details>
<summary>Show Answer</summary>

**Answer:**

`<? super T>` is a **lower-bounded wildcard**—accepts `T` or any **supertype of T**. Used when you **write** to the structure (consumer).

### Syntax

```java
List<? super Integer> list;
list = new ArrayList<Integer>();  // ✅
list = new ArrayList<Number>();   // ✅ Number is supertype of Integer
list = new ArrayList<Object>();   // ✅
list = new ArrayList<String>();  // ❌ String is not supertype of Integer
```

### Write-Friendly

```java
List<? super Integer> list = new ArrayList<Number>();
list.add(10);        // ✅ can add Integer
list.add(20);        // ✅
// list.add(1.5);   // ❌ can't add Double

Object obj = list.get(0); // ✅ read only as Object
Integer i = list.get(0);  // ❌ can't read as Integer safely
```

### Why Read Is Limited

```text
List<? super Integer> could be List<Number> or List<Object>
get() returns unknown — only Object is safe
```

### Example — Copy to List

```java
public static void copy(
        List<? extends Number> source,
        List<? super Number> dest) {
    for (Number n : source) {
        dest.add(n);
    }
}

List<Number> numbers = new ArrayList<>();
List<Integer> ints = Arrays.asList(1, 2, 3);
copy(ints, numbers); // source=extends, dest=super
```

### Collections.copy() Signature

```java
public static <T> void copy(List<? super T> dest, List<? extends T> src)
```

**Interview Point:**

> `<? super T>` = lower bound. Accepts T and supertypes. Write T safely; read only as Object. **Consumer super** — destination for data.

</details>

---

# 13. PECS Principle?

<details>
<summary>Show Answer</summary>

**Answer:**

**PECS** = **Producer Extends, Consumer Super**—a mnemonic for choosing `extends` vs `super` wildcards.

### The Rule

| Role | Wildcard | Mnemonic |
|------|----------|----------|
| **Producer** (you **get** from it) | `<? extends T>` | **Extends** |
| **Consumer** (you **put** into it) | `<? super T>` | **Super** |

### Producer Extends — Read From

```java
// List PRODUCES Numbers for us to read
public static double sum(List<? extends Number> numbers) {
    for (Number n : numbers) {  // GET from list
        // use n
    }
}
```

### Consumer Super — Write To

```java
// List CONSUMES Integers we add
public static void addNumbers(List<? super Integer> list) {
    list.add(10);  // PUT into list
    list.add(20);
}
```

### Both Together — Collections.copy()

```java
public static <T> void copy(
    List<? super T> dest,    // consumer — super
    List<? extends T> src) { // producer — extends
    for (T item : src) {
        dest.add(item);
    }
}
```

### Decision Flowchart

```text
Need to READ from parameter?  → <? extends T>
Need to WRITE to parameter?   → <? super T>
Both read and write exact T?  → use T (no wildcard)
Neither?                      → <?>
```

### Wrong Wildcard = Compile Error

```java
// ❌ Want to add — need super, not extends
void addInteger(List<? extends Integer> list) {
    list.add(1); // compile error
}

// ✅
void addInteger(List<? super Integer> list) {
    list.add(1); // OK
}
```

**Interview Point:**

> PECS: if parameter **produces** T → `extends`. If it **consumes** T → `super`. Memorize this for every wildcard interview question.

</details>

---

### Advanced

---

# 14. Difference between extends and super?

<details>
<summary>Show Answer</summary>

**Answer:**

In generics, `extends` and `super` define **wildcard bounds** with opposite directions in the type hierarchy.

### Comparison

| | `<? extends T>` | `<? super T>` |
|---|----------------|---------------|
| Direction | **Upper** bound (T and subtypes) | **Lower** bound (T and supertypes) |
| Accepts | `T`, child classes | `T`, parent classes |
| Read | As `T` (safe) | As `Object` only |
| Write | Cannot add (except null) | Can add `T` |
| Role | **Producer** | **Consumer** |
| PECS | **Extends** | **Super** |

### extends — Upper Bound

```java
List<? extends Number> list = new ArrayList<Integer>();
Number n = list.get(0);  // ✅ read as Number
list.add(10);            // ❌ cannot add
```

### super — Lower Bound

```java
List<? super Integer> list = new ArrayList<Number>();
list.add(10);            // ✅ write Integer
Object o = list.get(0);  // ✅ read as Object only
```

### Type Hierarchy Example

```text
Object
  └── Number
        └── Integer

<? extends Number>  → Number, Integer, Double, etc.
<? super Integer>   → Integer, Number, Object
```

### extends in Generic Declaration (Different!)

```java
// Class bound — T must be Number or subclass
class Box<T extends Number> { }

// Wildcard — list of unknown subtype of Number
List<? extends Number> list;
```

### super Only in Wildcards

```java
// ✅ wildcard
List<? super Integer> list;

// ❌ cannot use super in class declaration
class Box<T super Integer> { } // compile error
```

**Interview Point:**

> `extends` = ceiling (subtypes OK, read T). `super` = floor (supertypes OK, write T). Opposite directions—PECS tells you which to pick.

</details>

---

# 15. Producer Extends Consumer Super?

<details>
<summary>Show Answer</summary>

**Answer:**

**Producer Extends, Consumer Super (PECS)** is the standard rule for designing generic method signatures with wildcards.

### Definitions

| Term | Meaning | Wildcard |
|------|---------|----------|
| **Producer** | Structure **gives** you items | `<? extends T>` |
| **Consumer** | Structure **receives** your items | `<? super T>` |

### Visual Model

```text
Producer (src)  --extends-->  [read T]
Consumer (dest) --super-->    [write T]

copy(dest, src):
  src  = producer → <? extends T>
  dest = consumer → <? super T>
```

### Real API Examples

```java
// Collections.copy — textbook PECS
public static <T> void copy(
    List<? super T> dest,      // consumer
    List<? extends T> src)     // producer

// Collections.addAll — producer
public static <T> boolean addAll(
    Collection<? super T> c,   // consumer
    T... elements)             // elements are T

// Comparator — often producer
public static <T> void sort(
    List<T> list,
    Comparator<? super T> c)  // consumer of T for comparison
```

### Step-by-Step Application

```java
// Design: merge source list into destination
public static <T> void merge(
    List<? extends T> source,  // 1. we READ from source → extends
    List<? super T> dest) {    // 2. we WRITE to dest → super
    for (T item : source) {
        dest.add(item);
    }
}
```

### When NOT to Use Wildcards

```java
// Simple case — exact type is fine
public static <T> T getFirst(List<T> list) {
    return list.get(0);  // read AND return T — use T, not wildcard
}
```

### Interview Trap

```java
// ❌ Wrong — both extends, can't write to dest
void copy(List<? extends T> dest, List<? extends T> src)

// ✅ Correct PECS
void copy(List<? super T> dest, List<? extends T> src)
```

**Interview Point:**

> PECS is the senior-level answer for wildcard questions. Producer=extends (read), Consumer=super (write). `Collections.copy` is the canonical example.

</details>

---

# 16. Why List<Object> != List<String>?

<details>
<summary>Show Answer</summary>

**Answer:**

`List<String>` is **not** a subtype of `List<Object>` because generics are **invariant**—changing the type parameter does not create a subtype relationship, even if the type argument is a subtype.

### The Problem

```java
List<Object> objects = new ArrayList<>();
objects.add("hello");
objects.add(42);        // OK for List<Object>

// If this were allowed:
List<Object> objects = new ArrayList<String>(); // ❌ compile error
objects.add(42);      // would put Integer into String list!
```

### Invariance

```text
String extends Object     ✅
List<String> extends List<Object>  ❌ NOT true in Java
```

### Compile-Time Proof

```java
List<String> strings = new ArrayList<>();
List<Object> objects = strings; // ❌ compile error

// incompatible types: List<String> cannot be converted to List<Object>
```

### What Works Instead

```java
// Unbounded wildcard — read only
List<?> list = strings; // ✅

// Upper bound — read as String
List<? extends String> list2 = strings; // ✅

// Lower bound
List<? super String> list3 = strings; // ✅
```

### Arrays vs Generics (Contrast)

```java
// Arrays ARE covariant — dangerous!
Object[] objects = new String[10];
objects[0] = "hello";  // ✅
objects[1] = 42;       // compiles! ArrayStoreException at runtime

// Generics prevent this at compile time
```

### Why Invariance Is Correct

| If covariant | Problem |
|--------------|---------|
| `List<String>` ⊆ `List<Object>` | Add non-String to String list |
| Type safety broken | Heap pollution |
| Runtime crashes | Generics goal defeated |

### Practical Impact

```java
// Can't pass List<String> to method expecting List<Object>
void process(List<Object> list) { }

List<String> names = new ArrayList<>();
process(names); // ❌

// Fix with wildcard
void process(List<?> list) { }
process(names); // ✅
```

**Interview Point:**

> Generics are invariant—`List<String>` is NOT `List<Object>`. Use `List<? extends Object>` or `List<?>` for flexibility. Arrays are covariant (and unsafe)—generics fix that.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Can you create an array of generic type?

<details>
<summary>Show Answer</summary>

**Answer:** No—`new T[10]` is illegal. Use `(T[]) new Object[10]` with caution, or `ArrayList<T>`. Type erasure prevents real generic arrays.

</details>

---

### Q: What is heap pollution?

<details>
<summary>Show Answer</summary>

**Answer:** When a variable of parameterized type refers to an object not of that type—e.g., raw type assignment. Can cause `ClassCastException`. `@SafeVarargs` and proper generics prevent it.

</details>

---

### Q: Generic type vs wildcard — when to use?

<details>
<summary>Show Answer</summary>

**Answer:** Use **type parameter `T`** when same type appears in input and output. Use **wildcard `?`** when you only need flexibility on one side (PECS) and don't need to relate types.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Generics give compile-time type safety via type parameters; erasure removes type info at runtime for backward compatibility. Use PECS—`extends` for producers (read), `super` for consumers (write). `List<String>` is not `List<Object>` because generics are invariant.

</details>
