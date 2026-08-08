# 1. Difference Between Abstract Class and Interface

<details>
<summary>Show Answer</summary>

## Before Java 8

| Feature                    | Abstract Class          | Interface                  |
|----------------------------|-------------------------|----------------------------|
| Methods                    | Abstract + Concrete     | Only Abstract              |
| Variables                  | Instance, Static, Final | Only `public static final` |
| Constructor                | ✅ Yes                   | ❌ No                       |
| Multiple Inheritance       | ❌ No                    | ✅ Yes                      |
| State (Instance Variables) | ✅ Yes                   | ❌ No                       |
| Access Modifiers           | Any                     | Only `public abstract`     |

---

## Java 8+

| Feature              | Abstract Class          | Interface                   |
|----------------------|-------------------------|-----------------------------|
| Methods              | Abstract + Concrete     | Abstract + Default + Static |
| Variables            | Instance, Static, Final | Only `public static final`  |
| Constructor          | ✅ Yes                   | ❌ No                        |
| Multiple Inheritance | ❌ No                    | ✅ Yes                       |
| State                | ✅ Yes                   | ❌ No                        |
| Private Methods      | ✅ Yes                   | ✅ Yes (Java 9+)             |

### Interview Answer

> Use an Interface for a contract and multiple implementations. Use an Abstract Class when classes share common state and behavior.

</details>

---

# 2. When Would You Use an Interface?

<details>
<summary>Show Answer</summary>

### Use When

* Defining a contract
* Supporting multiple implementations
* Achieving loose coupling
* Dependency Injection

### Example

```java
interface PaymentService {
    void pay(double amount);
}

class UPIPaymentService implements PaymentService {
    public void pay(double amount) {
        System.out.println("UPI Payment");
    }
}
```

### Real Project Example

```java
@Autowired
private PaymentService paymentService;
```

Different implementations can be injected without changing client code.

**Interview Point:**

> Interfaces are heavily used in Spring applications for loose coupling.

</details>

---

# 3. When Would You Use an Abstract Class?

<details>
<summary>Show Answer</summary>

### Use When

* Common code is shared
* Common state exists
* Constructor is needed
* Partial implementation is required

### Example

```java
abstract class Employee {

    protected String company;

    Employee(String company) {
        this.company = company;
    }

    void login() {
        System.out.println("Common Login Logic");
    }

    abstract void work();
}
```

### Interview Point

> Abstract classes are preferred when multiple subclasses share common implementation and state.

</details>

---

# 4. Can an Abstract Class Have a Constructor?

<details>
<summary>Show Answer</summary>

### Answer

✅ Yes

### Example

```java
abstract class Employee {

    Employee() {
        System.out.println("Employee Constructor");
    }
}

class Developer extends Employee {
}
```

```java
new Developer();
```

**Output**

```text
Employee Constructor
```

### Why?

Constructors initialize common state of child objects.

</details>

---

# 5. Can an Abstract Class Have Static Methods?

<details>
<summary>Show Answer</summary>

### Answer

✅ Yes

### Example

```java
abstract class Employee {

    static void companyInfo() {
        System.out.println("ABC Ltd");
    }
}
```

```java
Employee.companyInfo();
```

### Interview Point

> Static methods belong to the class, not the object, so abstract classes can contain them.

</details>

---

# 6. Can an Interface Have Static Methods?

<details>
<summary>Show Answer</summary>

### Answer

✅ Yes (Java 8+)

### Example

```java
interface Utility {

    static void print() {
        System.out.println("Utility Method");
    }
}
```

```java
Utility.print();
```

### Interview Point

> Interface static methods are called using the interface name and are not inherited by implementing classes.

</details>

---

# 7. Can an Interface Have Private Methods?

<details>
<summary>Show Answer</summary>

### Answer

✅ Yes (Java 9+)

### Why?

To avoid duplicating logic across default methods.

### Example

```java
interface Vehicle {

    default void start() {
        validate();
        System.out.println("Started");
    }

    private void validate() {
        System.out.println("Validation");
    }
}
```

### Interview Point

> Private interface methods improve code reuse within the interface itself.

</details>

---

# 8. What are Default Methods?

<details>
<summary>Show Answer</summary>

### Answer

Default methods are interface methods with implementation.

### Example

```java
interface Vehicle {

    default void start() {
        System.out.println("Vehicle Started");
    }
}
```

### Why Important?

Before Java 8:

```java
interface Vehicle {
    void start();
}
```

Adding a new method would break all implementations.

Default methods solved this issue.

</details>

---

# 9. Why Were Default Methods Introduced?

<details>
<summary>Show Answer</summary>

### Problem Before Java 8

```java
interface Vehicle {

    void start();

    void stop(); // Newly added
}
```

All implementing classes would fail compilation.

### Solution

```java
interface Vehicle {

    void start();

    default void stop() {
        System.out.println("Stopped");
    }
}
```

### Interview Answer

> Default methods were introduced to add new functionality to interfaces without breaking existing implementations.

</details>

---

# 10. What are Static Methods in Interfaces?

<details>
<summary>Show Answer</summary>

### Example

```java
interface MathUtil {

    static int add(int a, int b) {
        return a + b;
    }
}
```

```java
System.out.println(MathUtil.add(10, 20));
```

**Output**

```text
30
```

### Interview Point

> Static methods are utility/helper methods related to the interface.

</details>

---

# 11. Multiple Inheritance Issue with Default Methods?

<details>
<summary>Show Answer</summary>

### Example

```java
interface A {

    default void show() {
        System.out.println("A");
    }
}

interface B {

    default void show() {
        System.out.println("B");
    }
}
```

```java
class Test implements A, B {
}
```

❌ Compilation Error

Because JVM doesn't know which implementation to choose.

</details>

---

# 12. What is the Diamond Problem?

<details>
<summary>Show Answer</summary>

### Problem

```java
interface A {

    default void show() {
        System.out.println("A");
    }
}

interface B extends A {
}

interface C extends A {
}
```

```java
class D implements B, C {
}
```

If multiple paths provide the same method, ambiguity may occur.

### Java's Solution

The implementing class must explicitly override the method.

```java
class D implements B, C {

    @Override
    public void show() {
        System.out.println("Resolved");
    }
}
```

### Interview Point

> Java allows multiple inheritance through interfaces but forces the class to resolve ambiguity.

</details>

---

# 13. If Two Interfaces Contain the Same Default Method?

<details>
<summary>Show Answer</summary>

### Example

```java
interface A {

    default void show() {
        System.out.println("A");
    }
}

interface B {

    default void show() {
        System.out.println("B");
    }
}
```

```java
class Test implements A, B {

    @Override
    public void show() {
        System.out.println("Custom");
    }
}
```

### Output

```text
Custom
```

---

### Calling Specific Interface Method

```java
class Test implements A, B {

    @Override
    public void show() {

        A.super.show();

        B.super.show();
    }
}
```

**Output**

```text
A
B
```

### Interview Point

> The implementing class must resolve the conflict by overriding the method.

</details>

---

# 14. What is Interface Segregation Principle (ISP)?

<details>
<summary>Show Answer</summary>

One of the SOLID principles.

### Definition

> Clients should not be forced to depend on methods they do not use.

### Bad Design

```java
interface Worker {

    void work();

    void eat();
}
```

```java
class Robot implements Worker {

    public void work() {
    }

    public void eat() {
        // Not applicable
    }
}
```

Problem: Robot doesn't eat.

---

### Good Design

```java
interface Workable {
    void work();
}

interface Eatable {
    void eat();
}
```

```java
class Robot implements Workable {
}

class Human implements Workable, Eatable {
}
```

### Interview Point

> ISP promotes smaller, focused interfaces instead of one large "god interface".

</details>

---

# 15. Marker Interfaces Examples

<details>
<summary>Show Answer</summary>

### What is a Marker Interface?

An interface with no methods.

```java
interface Marker {
}
```

Used to provide metadata to JVM/frameworks.

---

### Common Examples

| Marker Interface                 | Purpose                   |
|----------------------------------|---------------------------|
| `Serializable`                   | Object serialization      |
| `Cloneable`                      | Enables cloning           |
| `Remote`                         | RMI support               |
| `SingleThreadModel` (Deprecated) | Servlet threading control |

### Example

```java
class Employee implements Serializable {
}
```

```java
ObjectOutputStream out =
        new ObjectOutputStream(...);

out.writeObject(employee);
```

Without `Serializable`:

```text
NotSerializableException
```

### Interview Point

> Marker interfaces don't define behavior; they convey metadata or capabilities to the JVM/framework.

</details>
