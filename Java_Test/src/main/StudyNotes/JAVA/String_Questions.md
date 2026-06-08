Here’s a structured list of **String interview questions** for a **5+ years Java Developer**.
I’ve grouped them from **basic → intermediate → advanced → tricky → coding patterns → real-world scenarios**.

---

# Java String Interview Questions

## 1. String Basics

1. What is String in Java?
2. Why is String immutable in Java?
3. Difference between `String`, `StringBuilder`, and `StringBuffer`.
4. Difference between `==` and `equals()` in String.
5. What is String Constant Pool?
6. Difference between heap memory and String pool.
7. What happens when we create:

   ```java
   String s = "abc";
   ```
8. What happens internally when we create:

   ```java
   String s = new String("abc");
   ```
9. What is `intern()` method?
10. Why are Strings widely used as keys in HashMap?
11. Can String be subclassed?
12. Why is String class final?
13. Difference between mutable and immutable objects.
14. What is UTF-16 in Java String?
15. How String objects are stored in memory?

---

# Java String Basics — Interview Questions & Answers

---

# 1. What is String in Java?

In Java, a String is an object that represents a sequence of characters.

```java
String s = "Hello";
```

Internally, String is a class in Java:

```java
public final class String
```

Important characteristics:

* Immutable
* Stored in String Constant Pool
* Widely used in Java applications
* Thread-safe because immutable

---

# 2. Why is String immutable in Java?

A String object cannot be changed after creation.

Example:

```java
String s = "Hello";
s.concat("World");

System.out.println(s);
```

Output:

```java
Hello
```

Because `concat()` creates a new object instead of modifying the existing one.

## Reasons for immutability

### A. Security

Strings are used in:

* Database URLs
* File paths
* Network connections
* Class loading

If mutable, hackers could modify references.

Example:

```java
"jdbc:mysql://prod-db"
```

Changing it dynamically would be dangerous.

---

### B. Thread Safety

Immutable objects are naturally thread-safe.

Multiple threads can share the same String safely without synchronization.

---

### C. String Pool Optimization

Because Strings are immutable, JVM can safely reuse objects in the String pool.

```java
String s1 = "Java";
String s2 = "Java";
```

Both point to the same object.

---

### D. HashMap Performance

String caches its hashcode internally.

Because immutable:

```java
hashCode() remains constant
```

This improves HashMap performance.

---

# 3. Difference between String, StringBuilder, and StringBuffer

| Feature         | String                 | StringBuilder | StringBuffer        |
| --------------- | ---------------------- | ------------- | ------------------- |
| Mutable         | No                     | Yes           | Yes                 |
| Thread-safe     | Yes                    | No            | Yes                 |
| Performance     | Slow for modifications | Fast          | Slower than Builder |
| Storage         | String Pool/Heap       | Heap          | Heap                |
| Synchronization | Not needed             | No            | Yes                 |

---

## Example

### String

```java
String s = "Hello";
s.concat("World");
```

Creates new object.

---

### StringBuilder

```java
StringBuilder sb = new StringBuilder("Hello");
sb.append("World");
```

Modifies same object.

---

### StringBuffer

```java
StringBuffer sb = new StringBuffer("Hello");
sb.append("World");
```

Thread-safe version.

---

## When to use what?

| Scenario                               | Recommended   |
| -------------------------------------- | ------------- |
| Read-only text                         | String        |
| Frequent modification (single thread)  | StringBuilder |
| Frequent modification (multi-threaded) | StringBuffer  |

---

# 4. Difference between `==` and `equals()` in String

## `==`

Checks:

```text
Reference equality
```

Meaning:

* Are both references pointing to same object?

---

## `equals()`

Checks:

```text
Content equality
```

---

## Example

```java
String s1 = "Java";
String s2 = "Java";

System.out.println(s1 == s2);       // true
System.out.println(s1.equals(s2));  // true
```

Because both point to same SCP object.

---

## Another Example

```java
String s1 = new String("Java");
String s2 = new String("Java");

System.out.println(s1 == s2);       // false
System.out.println(s1.equals(s2));  // true
```

Different objects but same content.

---

# 5. What is String Constant Pool?

String Constant Pool (SCP) is a special memory area where JVM stores String literals.

Purpose:

* Reduce memory usage
* Reuse existing String objects

Example:

```java
String s1 = "Java";
String s2 = "Java";
```

Only one object is created in SCP.

Both references point to same object.

---

## Internal Flow

```text
Step 1:
Check if "Java" exists in SCP

Step 2:
If exists → reuse object

Step 3:
Else create new object
```

---

# 6. Difference between Heap Memory and String Pool

| Feature             | Heap                       | String Pool     |
| ------------------- | -------------------------- | --------------- |
| Purpose             | General objects            | String literals |
| Reusability         | No                         | Yes             |
| Memory optimization | Less                       | High            |
| Object creation     | Every `new` creates object | Literals reused |

---

## Example

```java
String s1 = new String("Java");
```

Creates:

1. One object in SCP (if absent)
2. One object in Heap

---

# 7. What happens when we create:

```java
String s = "abc";
```

## Internal Steps

### Step 1

JVM checks SCP for `"abc"`.

### Step 2

If absent:

* Create object in SCP.

### Step 3

Reference `s` points to SCP object.

---

## Memory Diagram

```text
String Pool:
-------------
"abc"

s -----> "abc"
```

---

# 8. What happens internally when we create:

```java
String s = new String("abc");
```

## Internal Steps

### Step 1

Check SCP for `"abc"`.

### Step 2

If absent:

* Create `"abc"` in SCP.

### Step 3

Create another object in Heap using `new`.

### Step 4

Reference `s` points to Heap object.

---

## Memory Diagram

```text
SCP:
----
"abc"

Heap:
-----
new String("abc")

s -----> Heap Object
```

---

# 9. What is `intern()` method?

`intern()` moves or references a String into the String Constant Pool.

---

## Example

```java
String s1 = new String("Java");
String s2 = s1.intern();

String s3 = "Java";

System.out.println(s2 == s3);
```

Output:

```java
true
```

Because both point to SCP object.

---

## Why use `intern()`?

Used to:

* Reduce memory
* Reuse duplicate strings

Useful in:

* Large-scale applications
* Parsing systems
* Caching systems

---

# 10. Why are Strings widely used as keys in HashMap?

Because String is:

* Immutable
* Hashcode cached
* Thread-safe

---

## Important Reason

HashMap depends on:

```java
hashCode()
equals()
```

If String were mutable:

```java
Map<String, String> map = new HashMap<>();
```

Changing key content would break retrieval.

---

## Example Problem if Mutable

```java
key = "abc";
hash = 123;

key changed to "xyz";
hash changes
```

HashMap bucket location becomes invalid.

---

# 11. Can String be subclassed?

No.

Because String class is declared as:

```java
public final class String
```

`final` prevents inheritance.

---

## Why final?

To ensure:

* Immutability
* Security
* Performance optimization

---

# 12. Why is String class final?

Main reasons:

| Reason           | Explanation        |
| ---------------- | ------------------ |
| Security         | Prevent tampering  |
| Immutability     | No modification    |
| Thread safety    | Safe sharing       |
| JVM optimization | SCP & hash caching |

---

## Example Risk

If inheritance allowed:

```java
class MyString extends String
```

Someone could override:

```java
hashCode()
equals()
```

This would break Java internals.

---

# 13. Difference between mutable and immutable objects

| Feature           | Mutable                  | Immutable        |
| ----------------- | ------------------------ | ---------------- |
| Can state change? | Yes                      | No               |
| Thread-safe       | Usually No               | Yes              |
| Performance       | Better for modifications | Better for reads |
| Example           | StringBuilder            | String           |

---

## Mutable Example

```java
StringBuilder sb = new StringBuilder("Java");
sb.append("8");
```

Same object modified.

---

## Immutable Example

```java
String s = "Java";
s.concat("8");
```

Creates new object.

---

# 14. What is UTF-16 in Java String?

Java Strings internally use Unicode encoding.

Traditionally Java used:

```text
UTF-16
```

Meaning:

* Each character generally uses 2 bytes.

---

## Example

```java
String s = "A";
```

Stored internally using Unicode representation.

---

## Important Note (Java 9+)

Java introduced:

```text
Compact Strings
```

Uses:

* LATIN-1 (1 byte) when possible
* UTF-16 otherwise

This reduces memory usage.

---

# 15. How String objects are stored in memory?

Strings can exist in:

| Area                 | Purpose                     |
| -------------------- | --------------------------- |
| String Constant Pool | Literals                    |
| Heap                 | Objects created using `new` |

---

## Example

```java
String s1 = "Java";
String s2 = new String("Java");
```

---

## Memory Representation

```text
String Pool:
------------
"Java"

Heap:
------
new String("Java")
```

---

# Most Important Interview Summary

| Question            | Key Point                      |
| ------------------- | ------------------------------ |
| Why immutable?      | Security + SCP + Thread safety |
| `==` vs `equals()`  | Reference vs Content           |
| SCP                 | Reuse String literals          |
| Why final?          | Prevent modification           |
| String vs Builder   | Immutable vs Mutable           |
| Why key in HashMap? | Stable hashcode                |
| `intern()`          | Move/reference to SCP          |


---

# 2. String Pool & Memory Internals

1. Explain String pool with examples.
2. How many objects are created?

   ```java
   String s1 = "Java";
   String s2 = "Java";
   ```
3. How many objects are created?

   ```java
   String s1 = new String("Java");
   ```
4. Difference between:

   ```java
   String s = "abc";
   ```

   and

   ```java
   String s = new String("abc");
   ```
5. What is SCP (String Constant Pool)?
6. Where is String pool located in Java 7 and Java 8?
7. What happens during String concatenation?
8. Compile-time vs runtime String concatenation.
9. Explain:

   ```java
   String s1 = "ab";
   String s2 = "a" + "b";
   ```
10. Why does this return true?

```java
"abc" == "ab" + "c"
```
---

# String Pool & Memory Internals — Interview Questions & Answers

---

# 1. Explain String Pool with examples

String Pool (also called String Constant Pool or SCP) is a special memory area inside JVM where String literals are stored and reused.

Purpose:

* Save memory
* Improve performance
* Avoid duplicate String objects

---

## Example 1

```java id="iw0l5q"
String s1 = "Java";
String s2 = "Java";
```

## What happens internally?

### Step 1

JVM checks SCP:

```text id="x7oc6y"
Does "Java" already exist?
```

### Step 2

If not present:

* Create `"Java"` in SCP.

### Step 3

`s1` points to SCP object.

### Step 4

For `s2`, JVM again checks SCP.

Since `"Java"` already exists:

* No new object created.
* `s2` points to same object.

---

## Memory Diagram

```text id="ee1r4z"
String Pool:
-------------
"Java"

s1 ----\
        ---> "Java"
s2 ----/
```

---

## Example 2

```java id="nd2cxh"
String s1 = new String("Java");
String s2 = new String("Java");
```

Now:

* Heap objects are different.
* SCP object may still be shared.

---

## Memory Diagram

```text id="eh8p0x"
SCP:
----
"Java"

Heap:
------
Object1 -> "Java"
Object2 -> "Java"
```

---

# 2. How many objects are created?

```java id="2e2n7h"
String s1 = "Java";
String s2 = "Java";
```

## Answer:

Only:

```text id="l8w4e0"
1 object
```

---

## Why?

Because:

* `"Java"` is a String literal.
* JVM stores it in SCP.
* Second reference reuses existing object.

---

## Memory Representation

```text id="0q6v9p"
SCP:
-----
"Java"

s1 ---> "Java"
s2 ---> "Java"
```

---

# 3. How many objects are created?

```java id="h4g1i9"
String s1 = new String("Java");
```

## Answer:

Usually:

```text id="o11k6j"
2 objects
```

---

## Object 1

String literal `"Java"` inside SCP.

## Object 2

Heap object created by `new`.

---

## Memory Diagram

```text id="o3vhk9"
SCP:
----
"Java"

Heap:
------
new String("Java")

s1 ---> Heap Object
```

---

## Important Interview Note

If `"Java"` already exists in SCP:

* Only heap object gets created.

Then total new objects:

```text id="4rnw4p"
1 object
```

---

# 4. Difference between

## A.

```java id="e9u8eo"
String s = "abc";
```

## B.

```java id="vlp2ng"
String s = new String("abc");
```

---

# Key Differences

| Feature         | `"abc"`   | `new String("abc")` |
| --------------- | --------- | ------------------- |
| Memory          | SCP       | Heap                |
| Reusability     | Yes       | No                  |
| Performance     | Better    | Slower              |
| Objects created | Usually 1 | Usually 2           |
| Recommended     | Yes       | Rarely              |

---

# Internal Working

---

## Case 1

```java id="9mn1rm"
String s = "abc";
```

### Steps

1. Check SCP.
2. If absent → create object in SCP.
3. Reference points to SCP object.

---

## Case 2

```java id="zuc96m"
String s = new String("abc");
```

### Steps

1. Check SCP.
2. Create `"abc"` in SCP if absent.
3. Create new object in Heap.
4. Reference points to Heap object.

---

# 5. What is SCP (String Constant Pool)?

SCP is a special memory region where JVM stores String literals.

---

# Why SCP exists?

To:

* Reduce duplicate objects
* Save memory
* Improve performance

---

## Example

```java id="7om75y"
String s1 = "Hello";
String s2 = "Hello";
```

Only one object exists in SCP.

---

# Important Property

Because String is immutable:

```text id="nn9wht"
JVM can safely reuse objects.
```

---

# 6. Where is String Pool located in Java 7 and Java 8?

---

# Before Java 7

SCP was stored in:

```text id="9du5u3"
PermGen memory
```

PermGen had fixed size.

Problem:

```text id="rjlwm5"
OutOfMemoryError: PermGen space
```

---

# Java 7 and Java 8

SCP moved to:

```text id="dz3i2t"
Heap memory
```

Benefits:

* Better garbage collection
* Dynamic resizing
* Better scalability

---

# Important Interview Point

| Java Version | SCP Location |
| ------------ | ------------ |
| Java 6       | PermGen      |
| Java 7+      | Heap         |

---

# 7. What happens during String concatenation?

Concatenation means combining Strings.

Example:

```java id="bdzrb4"
String s = "Java" + "World";
```

---

# Internal Working

Compiler converts:

```java id="0p8c1h"
String s = "Java" + "World";
```

into:

```java id="g5pp7n"
String s = new StringBuilder()
              .append("Java")
              .append("World")
              .toString();
```

---

# Why StringBuilder?

Because String is immutable.

Without StringBuilder:

* Multiple temporary objects would be created.

---

# Important Optimization

If concatenation is compile-time constant:

```java id="5a5sc9"
"Java" + "World"
```

Compiler optimizes it directly to:

```java id="h51xho"
"JavaWorld"
```

---

# 8. Compile-time vs Runtime String concatenation

---

# A. Compile-Time Concatenation

Example:

```java id="r9f0ux"
String s = "ab" + "cd";
```

Compiler optimizes:

```java id="8lh8ol"
"abcd"
```

Only one SCP object created.

---

# Memory

```text id="7p7p9m"
SCP:
-----
"abcd"
```

---

# B. Runtime Concatenation

Example:

```java id="n8a0yv"
String a = "ab";
String b = "cd";

String s = a + b;
```

This happens during execution.

Internally:

```java id="jlwmu0"
new StringBuilder()
```

gets used.

Result String created in Heap.

---

# Key Difference

| Feature            | Compile-time | Runtime    |
| ------------------ | ------------ | ---------- |
| When?              | Compilation  | Execution  |
| Optimization       | Yes          | No         |
| Uses SCP           | Yes          | Usually No |
| Uses StringBuilder | No           | Yes        |

---

# 9. Explain

```java id="tq1nqj"
String s1 = "ab";
String s2 = "a" + "b";
```

---

# What happens?

Compiler sees:

```java id="ckd2xn"
"a" + "b"
```

Both are constants.

So compiler optimizes:

```java id="5lg1wa"
"a" + "b" → "ab"
```

Thus:

```java id="w2xk5r"
s1 and s2 point to same SCP object
```

---

# Memory Diagram

```text id="l0kh0p"
SCP:
----
"ab"

s1 ---> "ab"
s2 ---> "ab"
```

---

# Therefore

```java id="rkebv3"
System.out.println(s1 == s2);
```

Output:

```java id="m8gkm6"
true
```

---

# 10. Why does this return true?

```java id="f0l3lz"
"abc" == "ab" + "c"
```

---

# Explanation

Compiler treats:

```java id="n6t8mz"
"ab" + "c"
```

as compile-time constant.

It becomes:

```java id="vf9nmj"
"abc"
```

Thus:

```java id="30ht5j"
"abc" == "abc"
```

Both references point to same SCP object.

---

# Memory Representation

```text id="1l9nl4"
SCP:
----
"abc"

Both references point here.
```

---

# Therefore

Output:

```java id="r4m6s2"
true
```

---

# Important Tricky Case

```java id="rqb3pa"
String s = "ab";

System.out.println("abc" == s + "c");
```

Output:

```java id="8t8efg"
false
```

---

# Why?

Because:

```java id="ij31jr"
s + "c"
```

is runtime concatenation.

A new Heap object gets created.

---

# Interview Summary Table

| Question             | Key Point                       |
|----------------------|---------------------------------|
| SCP                  | Stores reusable String literals |
| `"abc"`              | Uses SCP                        |
| `new String()`       | Creates Heap object             |
| Compile-time concat  | Optimized into SCP              |
| Runtime concat       | Uses StringBuilder              |
| Why immutable?       | Safe SCP reuse                  |
| Java 7+ SCP location | Heap memory                     |
| `"ab"+"c"`           | Compile-time optimization       |
| `s+"c"`              | Runtime concatenation           |


---

# 3. StringBuilder & StringBuffer

1. Difference between `StringBuilder` and `StringBuffer`.
2. Why is `StringBuilder` faster?
3. When should we use `StringBuilder`?
4. Is `StringBuilder` thread-safe?
5. What happens internally in append operation?
6. Capacity vs length in `StringBuilder`.
7. Default capacity of `StringBuilder`.
8. How does resizing happen internally?
9. Difference between:

   ```java
   append()
   insert()
   replace()
   reverse()
   delete()
   ```
10. Why Strings are inefficient in loops?

---

# StringBuilder & StringBuffer — Interview Questions & Answers

---

# 1. Difference between `StringBuilder` and `StringBuffer`

Both are mutable classes used for modifying Strings efficiently.

---

# Comparison Table

| Feature         | StringBuilder        | StringBuffer        |
| --------------- | -------------------- | ------------------- |
| Mutable         | Yes                  | Yes                 |
| Thread-safe     | No                   | Yes                 |
| Synchronization | No                   | Yes                 |
| Performance     | Faster               | Slower              |
| Introduced      | Java 5               | Java 1.0            |
| Best for        | Single-threaded apps | Multi-threaded apps |

---

# Example

## StringBuilder

```java id="7n8wgc"
StringBuilder sb = new StringBuilder("Java");
sb.append("8");

System.out.println(sb);
```

Output:

```java id="k8z7my"
Java8
```

---

## StringBuffer

```java id="a5gk7w"
StringBuffer sb = new StringBuffer("Java");
sb.append("8");

System.out.println(sb);
```

Output:

```java id="4rvfkp"
Java8
```

---

# Main Interview Point

`StringBuffer` methods are synchronized.

Example:

```java id="x4ecxl"
public synchronized StringBuffer append(String str)
```

Synchronization adds overhead.

That is why `StringBuilder` is faster.

---

# 2. Why is `StringBuilder` faster?

Because:

```text id="i7hcfq"
It is not synchronized.
```

No locking mechanism is used.

---

# StringBuffer Internally

```java id="zh4yq4"
public synchronized StringBuffer append(String str)
```

Every operation requires lock acquisition.

---

# StringBuilder Internally

```java id="3m6i7f"
public StringBuilder append(String str)
```

No synchronization.

So:

* Less overhead
* Better performance

---

# Performance Example

```java id="l75r8l"
StringBuilder sb = new StringBuilder();

for(int i=0; i<100000; i++) {
    sb.append(i);
}
```

Much faster than StringBuffer.

---

# 3. When should we use `StringBuilder`?

Use `StringBuilder` when:

* Frequent String modification is needed
* Application is single-threaded
* Performance matters

---

# Common Use Cases

| Scenario                 | Use StringBuilder |
| ------------------------ | ----------------- |
| Building JSON            | Yes               |
| Building SQL queries     | Yes               |
| Large loops              | Yes               |
| String concatenation     | Yes               |
| Log message construction | Yes               |

---

# Example

```java id="lhbxz8"
StringBuilder sb = new StringBuilder();

for(int i=1; i<=5; i++) {
    sb.append(i);
}

System.out.println(sb);
```

Output:

```java id="09wpyv"
12345
```

---

# 4. Is `StringBuilder` thread-safe?

No.

`StringBuilder` is:

```text id="d1l7vz"
NOT thread-safe
```

Because:

* Its methods are not synchronized.
* Multiple threads modifying same object can cause inconsistent results.

---

# Example Problem

```java id="0xvjlwm"
StringBuilder sb = new StringBuilder();

Thread t1 = new Thread(() -> sb.append("A"));
Thread t2 = new Thread(() -> sb.append("B"));
```

Possible issues:

* Data corruption
* Race conditions

---

# If thread safety is needed?

Use:

```java id="75xw4f"
StringBuffer
```

or external synchronization.

---

# 5. What happens internally in append operation?

---

# Example

```java id="i7nd8x"
StringBuilder sb = new StringBuilder("Java");
sb.append("8");
```

---

# Internal Working

`StringBuilder` internally maintains:

```java id="jlwm3r"
char[] value
```

---

# Steps During append()

### Step 1

Check available capacity.

### Step 2

If enough space:

* Add characters directly to array.

### Step 3

If capacity insufficient:

* Create larger array.
* Copy old data.
* Append new data.

---

# Visualization

Before:

```text id="1v8fcj"
[J][a][v][a][_][_]
```

After append("8"):

```text id="b35a2r"
[J][a][v][a][8][_]
```

---

# Important Point

Unlike String:

```text id="a7v4of"
No new object is created every time.
```

Same object is modified.

---

# 6. Capacity vs Length in `StringBuilder`

---

# Length

Represents:

```text id="q7eyy8"
Actual number of characters
```

Example:

```java id="v8k14l"
StringBuilder sb = new StringBuilder("Java");
System.out.println(sb.length());
```

Output:

```java id="lmjlwm"
4
```

---

# Capacity

Represents:

```text id="9o3d4z"
Total storage available before resizing
```

Example:

```java id="trp4ec"
StringBuilder sb = new StringBuilder("Java");
System.out.println(sb.capacity());
```

Output:

```java id="14egpb"
20
```

---

# Why 20?

Formula:

```text id="5i3pks"
16 + current string length
```

Current length:

```text id="t6lrlw"
4
```

Thus:

```text id="egjlwm"
16 + 4 = 20
```

---

# Summary

| Feature    | Meaning                 |
| ---------- | ----------------------- |
| length()   | Actual used characters  |
| capacity() | Total allocated storage |

---

# 7. Default capacity of `StringBuilder`

Default capacity:

```text id="p1nqsv"
16 characters
```

---

# Example

```java id="f2o83h"
StringBuilder sb = new StringBuilder();

System.out.println(sb.capacity());
```

Output:

```java id="1s6kgl"
16
```

---

# If initialized with String

```java id="f6rjlwm"
StringBuilder sb = new StringBuilder("Java");
```

Capacity becomes:

```text id="jlwmrx"
16 + 4 = 20
```

---

# 8. How does resizing happen internally?

When capacity becomes full:

```text id="yjlwm8"
StringBuilder automatically resizes.
```

---

# Resize Formula

```text id="4fx4e7"
(newCapacity = oldCapacity * 2) + 2
```

---

# Example

Initial capacity:

```text id="18t8ow"
16
```

After full:

```text id="v1yn0v"
(16 * 2) + 2 = 34
```

Next:

```text id="ofjlwm"
(34 * 2) + 2 = 70
```

---

# Internal Steps

### Step 1

Create larger char array.

### Step 2

Copy old characters.

### Step 3

Assign new array.

---

# Visualization

Before:

```text id="jlwmr8"
Capacity = 16
```

After resize:

```text id="3wz1l5"
Capacity = 34
```

---

# Important Interview Point

Resizing is expensive because:

* New array creation
* Memory allocation
* Data copying

---

# Optimization Tip

If size known beforehand:

```java id="ljlwmx"
new StringBuilder(1000);
```

Avoids repeated resizing.

---

# 9. Difference between methods

---

# A. append()

Adds content at end.

```java id="8xx6gm"
StringBuilder sb = new StringBuilder("Java");
sb.append("8");
```

Output:

```java id="jlwm3z"
Java8
```

---

# B. insert()

Adds content at specified index.

```java id="b3i6rb"
StringBuilder sb = new StringBuilder("Jva");
sb.insert(1, "a");
```

Output:

```java id="0sdjlwm"
Java
```

---

# C. replace()

Replaces characters between indexes.

```java id="jlwm1g"
StringBuilder sb = new StringBuilder("Java");
sb.replace(0, 2, "K");
```

Output:

```java id="jlwmr0"
Kva
```

---

# D. reverse()

Reverses characters.

```java id="zjlwm9"
StringBuilder sb = new StringBuilder("Java");
sb.reverse();
```

Output:

```java id="jlwmwq"
avaJ
```

---

# E. delete()

Deletes characters between indexes.

```java id="jlwm6l"
StringBuilder sb = new StringBuilder("Java");
sb.delete(1, 3);
```

Output:

```java id="jlwmes"
Ja
```

---

# Summary Table

| Method    | Purpose         |
| --------- | --------------- |
| append()  | Add at end      |
| insert()  | Add at position |
| replace() | Replace portion |
| reverse() | Reverse content |
| delete()  | Remove portion  |

---

# 10. Why Strings are inefficient in loops?

Because:

```text id="y0mjlwm"
String is immutable.
```

Every modification creates:

```text id="6gh7ml"
new object
```

---

# Bad Example

```java id="vjlwmq"
String s = "";

for(int i=0; i<5; i++) {
    s = s + i;
}
```

---

# What Happens?

Iteration 1:

```text id="5jlwmn"
"" + 0 -> new object
```

Iteration 2:

```text id="jlwmfh"
"0" + 1 -> new object
```

Iteration 3:

```text id="7jlwmk"
"01" + 2 -> new object
```

Multiple temporary objects created.

---

# Problems

| Issue            | Impact                  |
| ---------------- | ----------------------- |
| More objects     | High memory usage       |
| GC overhead      | Performance degradation |
| Repeated copying | Slower execution        |

---

# Efficient Approach

Use:

```java id="jlwm84"
StringBuilder
```

---

# Good Example

```java id="jlwmtn"
StringBuilder sb = new StringBuilder();

for(int i=0; i<5; i++) {
    sb.append(i);
}
```

Only one object modified.

---

# Interview Summary Table

| Question                | Key Point               |
|-------------------------|-------------------------|
| StringBuilder vs Buffer | Speed vs Thread safety  |
| Why faster?             | No synchronization      |
| Thread-safe?            | No                      |
| append() internal       | Uses mutable char array |
| Default capacity        | 16                      |
| Resize formula          | `(old * 2) + 2`         |
| length vs capacity      | Used vs allocated       |
| Strings in loops        | Creates many objects    |
| Best practice           | Use StringBuilder       |


---

# 4. Frequently Asked Coding Questions

## Easy

# String Coding Interview Questions with Hidden Answers

---

# 1. Reverse a String (Don't just print, Store in a variable)

```java id="2ez4qb"
String str = "Java";
```

<details>
<summary>Show Answer</summary>

## Using Two Pointer Approach

```java id="cjlwm7"
public class Main {
    public static void main(String[] args) {
       String str = "Learning Java String Programs.";
       char[] charArr = str.toCharArray();
       int min = 0;
       int max = charArr.length - 1;

       while ( max > min) {
          char temp = charArr[max];
          charArr[max] = charArr[min];
          charArr[min] = temp;
          min++;
          max--;
       }

        System.out.println(new String(charArr));
    }
}
```

## Using StringBuilder

```java
public class Main {
    public static void main(String[] args) {
        String str = "Learning Java String Programs.";
        StringBuilder sb = new StringBuilder();
        for (int i = str.length() - 1; i >= 0; i--) {
            sb.append(str.charAt(i));
        }
        System.out.println(sb);
    }
}
```

</details>

---

# 2. Reverse each word in a sentence

```java id="jlwmp8"
Input: "Java is fun"
```

<details>
<summary>Show Answer</summary>

```java id="jlwmg5"
public class Main {
    public static void main(String[] args) {
        String str = "Java is fun";
        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();
        for(String word : words) {
            StringBuilder sb = new StringBuilder(word);
            result.append(sb.reverse()).append(" ");
        }
        System.out.println(result.toString().trim());
    }
}
```

Output:

```text id="jlwmq5"
avaJ si nuf
```

```java
public class TestString {
    public static void main(String[] args) {
        String str = "Learning Java String Programs.";

        StringBuilder sb = new StringBuilder();
        String [] strArr = str.split(" ");
        for (String str1 : strArr) {
            char[] charArr = str1.toCharArray();
            int min = 0;
            int max = charArr.length - 1;

            while ( max > min) {
                char temp = charArr[max];
                charArr[max] = charArr[min];
                charArr[min] = temp;
                min++;
                max--;
            }
            sb.append(new String(charArr)).append(" ");
        }
        System.out.println(sb);
    }
}
```

</details>

---

# 3. Check palindrome String

```java id="9v8rmr"
Input: "madam"
```

<details>
<summary>Show Answer</summary>

```java id="jlwm11"
public class Main {
    public static void main(String[] args) {
        String str = "madam";
        int left = 0;
        int right = str.length() - 1;
        boolean isPalindrome = true;
        while(left < right) {
            if(str.charAt(left) != str.charAt(right)) {
                isPalindrome = false;
                break;
            }
            left++;
            right--;
        }
        System.out.println(isPalindrome);
    }
}
```

Output:

```text id="jlwm22"
true
```

## Using Java8

```java
public class TestString {
    public static void main(String[] args) {
        String input = "Madam";
        String str = input.replaceAll("\\s+", "").toLowerCase();
        int len = str.length()-1;
        boolean result = IntStream.range(0, len)
                .allMatch(index -> str.charAt(index) == str.charAt(len-index));
        System.out.println(result);
    }
}
```

## Using StringBuilder

```java
public class TestString {
    public static void main(String[] args) {
        String input = "Madam";
        String str = input.replaceAll("\\s+", "").toLowerCase();
//        boolean result = str.equals(new StringBuilder(str).reverse().toString());
        boolean result = str.contentEquals(new StringBuilder(str).reverse());
        System.out.println(result);
    }
}
```

</details>

---

# 4. Count vowels and consonants

```java id="jlwm33"
Input: "Java"
```

<details>
<summary>Show Answer</summary>

```java id="jlwm44"
public class Main {
    public static void main(String[] args) {
        String str = "Java".toLowerCase();
        int vowels = 0;
        int consonants = 0;
        for(char ch : str.toCharArray()) {
            if(ch >= 'a' && ch <= 'z') {
                if("aeiou".indexOf(ch) != -1) {
                    vowels++;
                } else {
                    consonants++;
                }
            }
        }
        System.out.println("Vowels: " + vowels);
        System.out.println("Consonants: " + consonants);
    }
}
```

Output:

```text id="jlwm55"
Vowels: 2
Consonants: 2
```

</details>

---

# 5. Find duplicate characters

```java id="jlwm66"
Input: "programming"
```

<details>
<summary>Show Answer</summary>

```java id="jlwm77"
import java.util.*;
public class Main {
    public static void main(String[] args) {
        String str = "programming";
        Map<Character, Integer> map = new HashMap<>();
        for(char ch : str.toCharArray()) {
            map.put(ch, map.getOrDefault(ch, 0) + 1);
        }
        for(Map.Entry<Character, Integer> entry : map.entrySet()) {
            if(entry.getValue() > 1) {
                System.out.println(entry.getKey());
            }
        }
    }
}
```

Output:

```text id="jlwm88"
r
g
m
```

Using Java 8

```java
public class TestString {
    public static void main(String[] args) {
        String str = "Learning Java";
        Map<Character,Long> countMap = str.chars()
                .peek(System.out::println)
                .mapToObj(ch -> (char)ch)
                        .collect(Collectors.groupingBy(ch -> ch, Collectors.counting()));
        System.out.println("Count HashMap : "+countMap);
        countMap = countMap.entrySet().stream()
                        .filter(x -> x.getValue() > 1)
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        System.out.println("HashMap with duplicate value: "+countMap);
    }
}
```

```java
public class TestString {
    public static void main(String[] args) {
        String str = "Learning Java";
        HashSet<Object> hashSet = new HashSet<>();
        String result = str.chars().mapToObj(ch -> (char)ch)
                .filter(ch -> !hashSet.add(ch))
                .distinct()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        System.out.println(result);
    }
}
```

</details>

---

# 6. Count frequency of each character

```java id="jlwm99"
Input: "aabbcc"
```

<details>
<summary>Show Answer</summary>

```java id="jlwm00"
import java.util.*;

public class Main {

    public static void main(String[] args) {

        String str = "aabbcc";

        Map<Character, Integer> map = new HashMap<>();

        for(char ch : str.toCharArray()) {
            map.put(ch, map.getOrDefault(ch, 0) + 1);
        }

        System.out.println(map);
    }
}
```

Output:

```text id="jlwm12"
{a=2, b=2, c=2}
```

</details>

---

# 7. Remove duplicate characters

```java id="jlwm13"
Input: "programming"
```

<details>
<summary>Show Answer</summary>

```java id="jlwm14"
import java.util.*;

public class Main {

    public static void main(String[] args) {

        String str = "programming";

        Set<Character> set = new LinkedHashSet<>();

        for(char ch : str.toCharArray()) {
            set.add(ch);
        }

        StringBuilder result = new StringBuilder();

        for(char ch : set) {
            result.append(ch);
        }

        System.out.println(result);
    }
}
```

Output:

```text id="jlwm15"
progamin
```

</details>

---

# 8. Check anagram

```java id="jlwm16"
Input: "listen", "silent"
```

<details>
<summary>Show Answer</summary>

```java id="jlwm17"
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

        String s1 = "listen";
        String s2 = "silent";

        char[] arr1 = s1.toCharArray();
        char[] arr2 = s2.toCharArray();

        Arrays.sort(arr1);
        Arrays.sort(arr2);

        System.out.println(Arrays.equals(arr1, arr2));
    }
}
```

Output:

```text id="jlwm18"
true
```

</details>

---

# 9. Find first non-repeated character

```java id="jlwm19"
Input: "swiss"
```

<details>
<summary>Show Answer</summary>

```java id="jlwm20"
import java.util.*;

public class Main {

    public static void main(String[] args) {

        String str = "swiss";

        Map<Character, Integer> map = new LinkedHashMap<>();

        for(char ch : str.toCharArray()) {
            map.put(ch, map.getOrDefault(ch, 0) + 1);
        }

        for(Map.Entry<Character, Integer> entry : map.entrySet()) {

            if(entry.getValue() == 1) {
                System.out.println(entry.getKey());
                break;
            }
        }
    }
}
```

Output:

```text id="jlwm21"
w
```

</details>

---

# 10. Find first repeated character

```java id="jlwm23"
Input: "swiss"
```

<details>
<summary>Show Answer</summary>

```java id="jlwm24"
import java.util.*;

public class Main {

    public static void main(String[] args) {

        String str = "swiss";

        Set<Character> set = new HashSet<>();

        for(char ch : str.toCharArray()) {

            if(set.contains(ch)) {
                System.out.println(ch);
                break;
            }

            set.add(ch);
        }
    }
}
```

Output:

```text id="jlwm25"
s
```

</details>

---

# 11. Count words in String

```java id="jlwm26"
Input: "Java is fun"
```

<details>
<summary>Show Answer</summary>

```java id="jlwm27"
public class Main {

    public static void main(String[] args) {

        String str = "Java is fun";

        String[] words = str.trim().split("\\s+");

        System.out.println(words.length);
    }
}
```

Output:

```text id="jlwm28"
3
```

</details>

---

# 12. Swap two Strings without third variable

```java id="jlwm29"
Input:
a = "Hello"
b = "World"
```

<details>
<summary>Show Answer</summary>

```java id="jlwm30"
public class Main {

    public static void main(String[] args) {

        String a = "Hello";
        String b = "World";

        a = a + b;

        b = a.substring(0, a.length() - b.length());

        a = a.substring(b.length());

        System.out.println("a = " + a);
        System.out.println("b = " + b);
    }
}
```

Output:

```text id="jlwm31"
a = World
b = Hello
```

</details>

---

# 13. Remove white spaces

```java id="jlwm32"
Input: "Ja va Pro gram"
```

<details>
<summary>Show Answer</summary>

```java id="jlwm34"
public class Main {

    public static void main(String[] args) {

        String str = "Ja va Pro gram";

        str = str.replaceAll("\\s", "");

        System.out.println(str);
    }
}
```

Output:

```text id="jlwm35"
JavaProgram
```

</details>

---

# 14. Convert lowercase to uppercase without built-in methods

```java id="jlwm36"
Input: "java"
```

<details>
<summary>Show Answer</summary>

```java id="jlwm37"
public class Main {

    public static void main(String[] args) {

        String str = "java";

        StringBuilder result = new StringBuilder();

        for(char ch : str.toCharArray()) {

            if(ch >= 'a' && ch <= 'z') {
                ch = (char)(ch - 32);
            }

            result.append(ch);
        }

        System.out.println(result);
    }
}
```

Output:

```text id="jlwm38"
JAVA
```

</details>

---

# 15. Find length without using length()

```java id="jlwm39"
Input: "Java"
```

<details>
<summary>Show Answer</summary>

```java id="jlwm40"
public class Main {

    public static void main(String[] args) {

        String str = "Java";

        int count = 0;

        try {

            while(true) {

                str.charAt(count);

                count++;
            }

        } catch(Exception e) {

            System.out.println(count);
        }
    }
}
```

Output:

```text id="jlwm41"
4
```

</details>

---

## Medium

1. Longest substring without repeating characters.
2. Longest palindrome substring.
3. String compression problem.
4. Group anagrams.
5. Check rotation of String.
6. Check if one String is subsequence of another.
7. Find all permutations of String.
8. Generate all substrings.
9. Character with maximum occurrence.
10. Minimum window substring.
11. Zigzag conversion.
12. Implement `atoi()`.
13. Roman number to integer.
14. Integer to Roman.
15. Valid parentheses problem.

---

## Advanced

1. KMP String matching algorithm.
2. Rabin-Karp algorithm.
3. Trie-based word search.
4. Suffix array basics.
5. Pattern matching problems.
6. Regex-based String parsing.
7. Implement String compression efficiently.
8. Streaming String problems.
9. Design autocomplete system.
10. Find repeated DNA sequences.

---

# 5. Tricky Output Questions

## Question 1

```java
String s1 = "Java";
String s2 = "Java";
System.out.println(s1 == s2);
```

## Question 2

```java
String s1 = new String("Java");
String s2 = new String("Java");
System.out.println(s1 == s2);
```

## Question 3

```java
String s1 = "Java";
String s2 = new String("Java");
System.out.println(s1 == s2);
```

## Question 4

```java
String s1 = "ab";
String s2 = "a" + "b";
System.out.println(s1 == s2);
```

## Question 5

```java
String s1 = "ab";
String s2 = new String("a") + new String("b");
System.out.println(s1 == s2);
```

## Question 6

```java
final String s1 = "ab";
String s2 = s1 + "c";
String s3 = "abc";
System.out.println(s2 == s3);
```

## Question 7

```java
String s = null;
System.out.println(s.length());
```

## Question 8

```java
String s = "Java";
s.concat("World");
System.out.println(s);
```

---

# 6. Scenario-Based Questions (Important for 5+ Years)

1. Why should String be immutable?
2. What problems occur if String becomes mutable?
3. Why is String heavily used in frameworks?
4. How does immutability help in multithreading?
5. Why is String safe for caching?
6. Why are credentials stored as `char[]` instead of String?
7. Performance issue with String concatenation in loops.
8. How JVM optimizes String memory?
9. What is String deduplication in JVM?
10. Explain memory leak possibilities with Strings.
11. How would you optimize large-scale text processing?
12. What happens internally during:

```java
String result = a + b + c;
```

---

# 7. String Methods Interview Questions

## Important Methods

1. `charAt()`
2. `substring()`
3. `indexOf()`
4. `lastIndexOf()`
5. `contains()`
6. `split()`
7. `replace()`
8. `replaceAll()`
9. `trim()`
10. `strip()`
11. `equalsIgnoreCase()`
12. `compareTo()`
13. `startsWith()`
14. `endsWith()`
15. `matches()`
16. `join()`
17. `format()`
18. `repeat()`
19. `isBlank()`
20. `lines()`

---

# 8. Java 8+ String Questions

1. Explain `StringJoiner`.
2. Difference between `String.join()` and `Collectors.joining()`.
3. Reverse String using Streams.
4. Count character frequency using Streams.
5. Remove duplicates using Streams.
6. Find duplicate characters using Streams.
7. Sort characters in String using Streams.
8. Convert String to List using Streams.
9. Check palindrome using Streams.
10. String processing with parallel streams.

---

# 9. Real Interview Coding Patterns

## Pattern 1 — Frequency Map

Used in:

* Anagram
* Duplicate chars
* Character counting

## Pattern 2 — Two Pointers

Used in:

* Palindrome
* Reverse String
* Substring problems

## Pattern 3 — Sliding Window

Used in:

* Longest substring
* Minimum window substring

## Pattern 4 — Stack

Used in:

* Valid parentheses
* Expression parsing

## Pattern 5 — HashSet

Used in:

* Duplicate removal
* Unique character problems

---

# 10. Most Important Questions for Experienced Developers

These are asked very frequently:

1. Why String is immutable?
2. Difference between StringBuilder and StringBuffer.
3. Explain String pool internally.
4. `==` vs `equals()`.
5. How many objects are created?
6. Runtime vs compile-time concatenation.
7. Why String is thread-safe?
8. Why String used in HashMap keys?
9. Reverse String without built-in methods.
10. Longest substring without repeating characters.
11. Find first non-repeated character.
12. Check anagram efficiently.
13. Performance optimization in String handling.
14. Explain String memory optimization in JVM.
15. StringBuilder internal resizing.

If you want, next I can start:

* **Mock Interview Round on Strings**
* **Top 50 String Coding Problems with solutions**
* **String Internals Deep Dive**
* **StringBuilder Internal Working**
* **Tricky String Output Questions with explanations**
* **FAANG-level String Problems**
* **Java String interview answers for 5+ years experience**
