# 1. What is a `final` Variable?

<details>
<summary>Show Answer</summary>

**Answer:**

A `final` variable can be assigned a value only once.

### Example

```java
final int AGE = 25;

// AGE = 30;  // ❌ Compilation Error
```

### For Instance Variables

```java
class Employee {

    final int id;

    Employee(int id) {
        this.id = id; // Allowed: assigned once
    }
}
```

**Interview Point:**

> A final variable must be initialized exactly once—either during declaration, instance initialization, or constructor execution.

</details>

---

# 2. What is a `final` Method?

<details>
<summary>Show Answer</summary>

**Answer:**

A `final` method cannot be overridden by subclasses.

### Example

```java
class Parent {

    final void show() {
        System.out.println("Parent Method");
    }
}

class Child extends Parent {

    // ❌ Compilation Error
    // void show() {}
}
```

### Why Use It?

* Prevent modification of critical logic.
* Maintain business rules.

**Interview Point:**

> Final methods are inherited but cannot be overridden.

</details>

---

# 3. What is a `final` Class?

<details>
<summary>Show Answer</summary>

**Answer:**

A final class cannot be extended.

### Example

```java
final class Employee {
}

// ❌ Compilation Error
// class Manager extends Employee {}
```

### Common Examples

```java
String
Integer
Long
Character
```

**Interview Point:**

> Final classes are often used when inheritance could break security or immutability.

</details>

---

# 4. Why is String Final?

<details>
<summary>Show Answer</summary>

### Declaration

```java
public final class String
```

### Reasons

#### 1. Preserve Immutability

If inheritance were allowed:

```java
// Not allowed
class MyString extends String {
}
```

A subclass could break immutability.

---

#### 2. Security

Strings are used in:

```java
Database URLs
File Paths
Network Connections
Class Loading
```

Their behavior must remain predictable.

---

#### 3. String Pool Consistency

```java
String s1 = "Java";
String s2 = "Java";
```

Both can safely share the same pooled object because String cannot be modified.

### Interview Point

> String is final mainly to guarantee immutability, security, and String Pool optimization.

</details>

---

# 5. Can a Final Reference Object Be Modified?

<details>
<summary>Show Answer</summary>

**Answer:** ✅ Yes

A final reference cannot point to another object, but the object's state can still change.

### Example

```java
final StringBuilder sb = new StringBuilder("Java");

// Allowed: object state changes
sb.append(" 8");

System.out.println(sb);
```

**Output**

```text
Java 8
```

---

### Not Allowed

```java
final StringBuilder sb = new StringBuilder("Java");

// ❌ Compilation Error
// sb = new StringBuilder("Spring");
```

### Interview Point

> Final restricts reference reassignment, not object mutation.

</details>

---

# 6. Difference Between `final` and Immutable

<details>
<summary>Show Answer</summary>

| final                              | Immutable                  |
|------------------------------------|----------------------------|
| Keyword                            | Design concept             |
| Prevents reassignment              | Prevents state change      |
| Object may still be mutable        | Object state never changes |
| Applied to variable, method, class | Applied to class design    |

### Example

```java
final StringBuilder sb =
        new StringBuilder("Java");

sb.append(" 8"); // Allowed
```

Object changed ⇒ Not immutable.

---

### Immutable Example

```java
String str = "Java";

str.concat(" 8");

System.out.println(str);
```

Output:

```text
Java
```

### Interview Point

> `final` reference ≠ immutable object.

</details>

---

# 7. Why Must Local Variables Used in Lambda Be Effectively Final?

<details>
<summary>Show Answer</summary>

### Example

```java
int num = 10;

Runnable r = () ->
        System.out.println(num);

r.run();
```

Valid because `num` is not modified.

---

### Invalid Example

```java
int num = 10;

Runnable r = () ->
        System.out.println(num);

num++; // ❌ Compilation Error
```

### Why?

Local variables are stored in the stack frame.

When lambda executes later, the method may already have completed.

Java captures a copy of the variable value.

```text
Method Stack Frame
      ↓
num = 10

Lambda captures value 10
```

If modifications were allowed, consistency issues would occur.

### Interview Point

> Lambdas capture values, not local variables themselves. Therefore local variables must be final or effectively final.

</details>

---

# 8. How Does JVM Optimize Final Variables?

<details>
<summary>Show Answer</summary>

### Example

```java
final int x = 10;
final int y = 20;

int z = x + y;
```

### Compiler Optimization

Compiler may replace:

```java
int z = x + y;
```

with:

```java
int z = 30;
```

This is called **Constant Folding**.

---

### Example

```java
final String APP = "MyApp";

System.out.println(APP);
```

Compiler may inline the value directly.

### Benefits

* Reduced memory access
* Faster execution
* Better JIT optimizations

### Interview Point

> Final constants allow compile-time optimizations such as constant folding and inlining.

</details>

---

# 9. Can Final Fields Change Through Reflection?

<details>
<summary>Show Answer</summary>

**Answer:** ⚠️ Yes, in some cases.

### Example

```java
class Employee {

    private final String name = "John";
}
```

Using reflection:

```java
Field field =
        Employee.class.getDeclaredField("name");

field.setAccessible(true);

field.set(emp, "David");
```

The field value may change.

---

### Why Is This Dangerous?

It breaks assumptions about:

* Immutability
* Thread safety
* Security

---

### Modern Java

Recent Java versions have stronger restrictions around reflective access, especially with the module system, but reflective modification of final fields is still possible in certain scenarios.

### Interview Point

> Although `final` prevents normal code from modifying a field, reflection can bypass access checks and potentially modify it, which is one reason reflection should be used carefully.

</details>
