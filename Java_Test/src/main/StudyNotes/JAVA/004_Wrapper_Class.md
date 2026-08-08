# 1. Why Wrapper Classes are Needed?

<details>
<summary>Show Answer</summary>

**Answer:**

Wrapper classes convert primitive data types into objects.

| Primitive | Wrapper   |
|-----------|-----------|
| int       | Integer   |
| long      | Long      |
| double    | Double    |
| char      | Character |
| boolean   | Boolean   |

### Why Needed?

* Collections store objects, not primitives.
* Utility methods are available.
* Generics work only with objects.

### Example

```java id="wy0rwg"
List<Integer> numbers = new ArrayList<>();

numbers.add(10); // int -> Integer (Autoboxing)
```

**Interview Point:**

> Collections and Generics are the biggest reasons wrapper classes exist.

</details>

---

# 2. What is Autoboxing?

<details>
<summary>Show Answer</summary>

**Answer:**

Automatic conversion of a primitive into its corresponding wrapper object.

### Example

```java id="yqq8r5"
Integer num = 100; // int -> Integer

// Compiler converts roughly to:
Integer num2 = Integer.valueOf(100);
```

**Interview Point:**

> Introduced in Java 5 to reduce manual conversion code.

</details>

---

# 3. What is Unboxing?

<details>
<summary>Show Answer</summary>

**Answer:**

Automatic conversion of a wrapper object into its primitive value.

### Example

```java id="2xpdls"
Integer num = 100;

int value = num; // Integer -> int

// Compiler converts roughly to:
int value2 = num.intValue();
```

</details>

---

# 4. Difference Between `int` and `Integer`

<details>
<summary>Show Answer</summary>

| int                        | Integer                 |
|----------------------------|-------------------------|
| Primitive                  | Wrapper Class           |
| Stores value directly      | Stores object           |
| Cannot be null             | Can be null             |
| Faster                     | Slightly slower         |
| Less memory                | More memory             |
| Cannot be used in Generics | Can be used in Generics |

### Example

```java id="rt4nkh"
int a = 10;

Integer b = 10;
```

### Interview Point

```java id="n0kkie"
Integer age = null;

int salary = null; // Compilation Error
```

Only wrappers can represent absence of value using `null`.

</details>

---

# 5. What is Integer Cache?

<details>
<summary>Show Answer</summary>

**Answer:**

JVM caches Integer objects in the range **-128 to 127**.

### Example

```java id="vszw5q"
Integer a = 100;
Integer b = 100;

System.out.println(a == b);
```

**Output**

```text id="p3wytc"
true
```

Because both references point to the same cached object.

### Memory

```text id="a5r8v5"
Integer Cache
--------------
100
--------------
 ↑   ↑
 a   b
```

**Interview Point:**

> `Integer.valueOf()` uses Integer Cache.

</details>

---

# 6. Output?

<details>
<summary>Show Answer</summary>

```java id="p6s8u6"
Integer a = 127;
Integer b = 127;

System.out.println(a == b);
```

### Output

```text id="5p7g0t"
true
```

### Why?

127 lies within cache range (-128 to 127).

```text id="g62w4j"
a ----\
       > Same Cached Object
b ----/
```

</details>

---

# 7. Output?

<details>
<summary>Show Answer</summary>

```java id="vwj8if"
Integer a = 128;
Integer b = 128;

System.out.println(a == b);
```

### Output

```text id="8qmyow"
false
```

### Why?

128 is outside cache range.

Two separate Integer objects are created.

```text id="z9v7gl"
a ---> Integer(128)

b ---> Integer(128)
```

Different references.

</details>

---

# 8. Why Integer Cache Range is -128 to 127?

<details>
<summary>Show Answer</summary>

**Answer:**

Because this range is frequently used in real-world applications.

### Benefits

* Reduces object creation.
* Improves performance.
* Saves memory.

### Historical Reason

This range matches the signed **1-byte byte range**.

```text id="v62c3l"
-128 to 127
```

### Interview Point

> The range is configurable using JVM options, but the default is -128 to 127.

</details>

---

# 9. Why Integer is Immutable?

<details>
<summary>Show Answer</summary>

### Example

```java id="nmdkg6"
Integer a = 10;

a = 20;
```

Internally:

```text id="8x3fsy"
Integer(10)   // Old object

Integer(20)   // New object
```

The existing object is never modified.

### Reasons

* Thread safety
* Integer Cache consistency
* Reliable hashing

### Interview Point

> If Integer were mutable, Integer Cache would become unsafe.

</details>

---

# 10. Difference Between `parseInt()` and `valueOf()`

<details>
<summary>Show Answer</summary>

| parseInt()                 | valueOf()               |
|----------------------------|-------------------------|
| Returns primitive int      | Returns Integer object  |
| Used when primitive needed | Used when object needed |
| No caching involved        | Uses Integer Cache      |
| Faster                     | Slightly slower         |

### Example

```java id="v8e6z3"
int num = Integer.parseInt("100");

System.out.println(num);
```

---

```java id="3jlwm5"
Integer num = Integer.valueOf("100");

System.out.println(num);
```

### Interview Point

```java id="wjlwm1"
Integer.valueOf("100")
```

internally uses:

```java id="pv1g8i"
IntegerCache
```

when possible.

</details>

---

# 11. Memory Impact of Wrappers

<details>
<summary>Show Answer</summary>

### Primitive

```java id="b36j4h"
int num = 100;
```

Stores only the value.

---

### Wrapper

```java id="oqkh1l"
Integer num = 100;
```

Requires:

* Object header
* Integer value
* Reference

### Memory View

```text id="0pd69z"
Stack
-----
num --->

Heap
-----
Integer Object
value = 100
-----
```

### Impact

```java id="d67b8k"
List<Integer> list = new ArrayList<>();
```

Millions of Integer objects consume significantly more memory than primitive arrays.

### Interview Point

> Wrappers introduce additional memory overhead because every value becomes an object.

</details>

---

# 12. Why Wrappers are Heavily Used in Collections?

<details>
<summary>Show Answer</summary>

### Because Collections Store Objects

❌ Not Allowed

```java id="6w0m6i"
List<int> list = new ArrayList<>();
```

Compilation Error.

---

✅ Allowed

```java id="0cyg5g"
List<Integer> list = new ArrayList<>();
```

### Why?

Collections are built using Generics.

```java id="h0rzg5"
class ArrayList<E>
```

Generics work only with reference types (objects), not primitives.

### Example

```java id="zt2e92"
List<Integer> ages = new ArrayList<>();

ages.add(25); // Autoboxing
ages.add(30);
```

### Interview Answer

> Wrapper classes are heavily used in Collections because Java Generics support only reference types. Since primitives cannot be used directly, wrapper classes act as object representations of primitive values.

</details>
