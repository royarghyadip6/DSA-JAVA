# 1. What is a Static Variable?

<details>
<summary>Show Answer</summary>

**Answer:**

A static variable belongs to the **class**, not to individual objects.

* Only one copy exists per class.
* Shared among all objects.

### Example

```java id="e8v7dn"
class Employee {

    static String company = "ABC"; // Shared

    String name; // Per object
}

public class Test {
    public static void main(String[] args) {

        Employee e1 = new Employee();
        Employee e2 = new Employee();

        System.out.println(e1.company);
        System.out.println(e2.company);
    }
}
```

### Memory

```text id="z9bhdh"
Method Area / Metaspace
-----------------------
company = "ABC"
-----------------------

Heap
------
e1
e2
------
```

**Interview Point:**

> Static variables are commonly used for constants, counters, and shared configuration.

</details>

---

# 2. What is a Static Method?

<details>
<summary>Show Answer</summary>

**Answer:**

A static method belongs to the class and can be called without creating an object.

### Example

```java id="mxjgtu"
class MathUtil {

    static int add(int a, int b) {
        return a + b;
    }
}

public class Test {
    public static void main(String[] args) {

        // Called using class name
        System.out.println(MathUtil.add(10, 20));
    }
}
```

**Output**

```text id="ngn3wr"
30
```

### Interview Point

> Static methods are used for utility/helper methods because they don't depend on object state.

</details>

---

# 3. Why is `main()` Static?

<details>
<summary>Show Answer</summary>

### Signature

```java id="76rwy0"
public static void main(String[] args)
```

### Why?

JVM starts execution by calling `main()`.

If `main()` were non-static:

```java id="4spgh4"
public void main(String[] args)
```

JVM would first need to create an object.

But which object should it create?

To avoid this dependency, `main()` is made static.

### Interview Answer

> `main()` is static so that JVM can invoke it directly using the class name without creating an object.

</details>

---

# 4. Can a Static Method Access Instance Variables?

<details>
<summary>Show Answer</summary>

**Answer:** ❌ Directly No

### Example

```java id="jx9j8k"
class Employee {

    int salary = 50000;

    static void show() {

        // System.out.println(salary);
        // Compilation Error
    }
}
```

### Why?

Instance variables belong to objects.

Static methods belong to the class.

At compile time, JVM does not know which object's variable should be accessed.

---

### Correct Way

```java id="zzmmbk"
class Employee {

    int salary = 50000;

    static void show() {

        Employee emp = new Employee();

        System.out.println(emp.salary);
    }
}
```

### Interview Point

> A static method can access instance members only through an object reference.

</details>

---

# 5. Can a Static Method be Overridden?

<details>
<summary>Show Answer</summary>

**Answer:** ❌ No

Static methods are **hidden**, not overridden.

### Example

```java id="6kmqlz"
class Parent {

    static void show() {
        System.out.println("Parent");
    }
}

class Child extends Parent {

    static void show() {
        System.out.println("Child");
    }
}
```

```java id="l3fc4i"
Parent p = new Child();

p.show();
```

**Output**

```text id="j8ix0h"
Parent
```

### Why?

Method resolution happens using the reference type.

```java id="0t5rzn"
Parent p
```

Compiler chooses `Parent.show()`.

### Interview Point

> Static methods use static binding (compile-time binding), so runtime polymorphism does not apply.

</details>

---

# 6. Can a Static Block be Overloaded?

<details>
<summary>Show Answer</summary>

**Answer:** ❌ No

Overloading applies to methods, not blocks.

### Valid

```java id="f1b4ik"
class Test {

    static {
        System.out.println("Block 1");
    }

    static {
        System.out.println("Block 2");
    }
}
```

These are multiple static blocks, not overloaded blocks.

### Output

```text id="ozp2ny"
Block 1
Block 2
```

### Interview Point

> A class can have multiple static blocks, executed in the order they appear.

</details>

---

# 7. Static Block vs Instance Block

<details>
<summary>Show Answer</summary>

| Static Block                       | Instance Block                         |
|------------------------------------|----------------------------------------|
| Executes when class loads          | Executes when object is created        |
| Runs only once                     | Runs every time an object is created   |
| Uses `static {}`                   | Uses `{}`                              |
| Can access static members directly | Can access static and instance members |

### Example

```java id="mjlwmr"
class Test {

    static {
        System.out.println("Static Block");
    }

    {
        System.out.println("Instance Block");
    }

    Test() {
        System.out.println("Constructor");
    }
}
```

```java id="dvw00r"
new Test();
new Test();
```

**Output**

```text id="y10a8m"
Static Block
Instance Block
Constructor
Instance Block
Constructor
```

### Interview Point

> Static block executes once per class loading, instance block executes for every object creation.

</details>

---

# 8. Order of Execution

<details>
<summary>Show Answer</summary>

### Example

```java id="8lsvh8"
class Parent {

    static {
        System.out.println("Parent Static");
    }

    {
        System.out.println("Parent Instance");
    }

    Parent() {
        System.out.println("Parent Constructor");
    }
}

class Child extends Parent {

    static {
        System.out.println("Child Static");
    }

    {
        System.out.println("Child Instance");
    }

    Child() {
        System.out.println("Child Constructor");
    }
}
```

```java id="pocxw4"
public class Test {

    public static void main(String[] args) {

        new Child();
    }
}
```

### Output

```text id="3on1cg"
Parent Static
Child Static

Parent Instance
Parent Constructor

Child Instance
Child Constructor
```

### Complete Order

```text id="5cvlnc"
1. Parent Static Block
2. Child Static Block

3. Parent Instance Block
4. Parent Constructor

5. Child Instance Block
6. Child Constructor
```

### Interview Shortcut

```text id="g2hk91"
Static Blocks
      ↓
Instance Blocks
      ↓
Constructors
```

**Most Asked Interview Question:**

> For inheritance, remember:
>
> **Parent Static → Child Static → Parent Instance → Parent Constructor → Child Instance → Child Constructor**.

</details>

# 9. What is a Static Nested Class?

<details>
<summary>Show Answer</summary>

**Answer:**

A static nested class is a class declared with the `static` keyword inside another class.

### Example

```java
class Outer {

    static class Inner {

        void show() {
            System.out.println("Static Nested Class");
        }
    }
}

public class Test {

    public static void main(String[] args) {

        // No need to create Outer object
        Outer.Inner obj = new Outer.Inner();

        obj.show();
    }
}
```

**Output**

```text
Static Nested Class
```

### Interview Point

> A static nested class behaves like a normal class logically grouped inside another class.

</details>

---

# 10. Use Cases of Static Nested Class

<details>
<summary>Show Answer</summary>

### 1. Builder Pattern (Very Common)

```java
public class Employee {

    private String name;

    static class Builder {

        Employee build() {
            return new Employee();
        }
    }
}
```

Example:

```java
Employee.Builder builder =
        new Employee.Builder();
```

---

### 2. Grouping Related Classes

```java
class Database {

    static class ConnectionConfig {
    }
}
```

Keeps related classes together.

---

### Why Static?

Without `static`:

```java
Outer.Inner inner =
        new Outer().new Inner();
```

Requires Outer object.

With `static`:

```java
Outer.Inner inner =
        new Outer.Inner();
```

No Outer object required.

### Interview Point

> Static nested classes are commonly used in Builder Design Pattern and for logically grouping helper classes.

</details>

---

# 11. Memory Allocation of Static Members

<details>
<summary>Show Answer</summary>

### Example

```java
class Employee {

    static String company = "OpenAI";

    static void show() {
    }
}
```

### Memory Layout

```text
Metaspace (Class Metadata)
--------------------------
Employee Class
Static Variable: company
Static Method: show()
--------------------------

Heap
-----
Employee Objects
-----
```

### Important

```java
Employee e1 = new Employee();
Employee e2 = new Employee();
```

Only one copy exists:

```text
company = "OpenAI"
```

shared by all objects.

### Interview Point

> Static members are loaded when the class is loaded and shared by all instances.

</details>

---

# 12. When Does a Static Block Execute?

<details>
<summary>Show Answer</summary>

### Answer

A static block executes when the class is initialized by the JVM.

### Example

```java
class Test {

    static {
        System.out.println("Static Block");
    }
}
```

```java
public class Main {

    public static void main(String[] args) {

        new Test();
    }
}
```

**Output**

```text
Static Block
```

---

### Class Initialization Triggers

```java
new Test();
```

or

```java
Test.show();
```

or

```java
Class.forName("Test");
```

### Interview Point

> Static blocks execute during class initialization, not object creation.

</details>

---

# 13. Explain Class Loading Process

<details>
<summary>Show Answer</summary>

### Example

```java
Employee emp = new Employee();
```

### JVM Class Loading Phases

#### 1. Loading

Class loader loads `.class` file into JVM.

```text
Employee.class
      ↓
JVM Memory
```

---

#### 2. Linking

Consists of:

##### Verification

Checks bytecode validity.

```text
Is bytecode valid?
```

##### Preparation

Allocates memory for static variables.

```java
static int count;
```

Default value assigned:

```java
count = 0;
```

##### Resolution

Symbolic references converted to actual references.

---

#### 3. Initialization

Static variables and static blocks execute.

```java
static int count = 10;

static {
    System.out.println("Loaded");
}
```

### Interview Shortcut

```text
Loading
   ↓
Linking
   ↓
Initialization
```

### Interview Point

> Static blocks execute in the Initialization phase of class loading.

</details>

---

# 14. What Happens if a Static Block Throws Exception?

<details>
<summary>Show Answer</summary>

### Example

```java
class Test {

    static {

        if (true) {
            throw new RuntimeException("Error");
        }
    }
}
```

```java
new Test();
```

### Output

```text
ExceptionInInitializerError
```

### Why?

JVM wraps the original exception inside:

```java
ExceptionInInitializerError
```

### Important

Once initialization fails:

```java
new Test();
```

again results in:

```text
NoClassDefFoundError
```

because JVM marks the class initialization as failed.

### Interview Point

> Any unchecked exception from a static block causes `ExceptionInInitializerError`.

</details>

---

# 15. How Many Times Does a Static Block Execute?

<details>
<summary>Show Answer</summary>

### Answer

✅ Only once per class loader.

### Example

```java
class Test {

    static {
        System.out.println("Static Block");
    }
}
```

```java
new Test();
new Test();
new Test();
```

**Output**

```text
Static Block
```

Only once.

---

### Why?

Because the class is loaded only once by a particular ClassLoader.

```text
Class Load
     ↓
Static Block Executes
     ↓
Objects Created Multiple Times
```

### Example

```java
Test t1 = new Test();
Test t2 = new Test();
Test t3 = new Test();
```

Static block executes once, constructors execute three times.

### Interview Point

> A static block executes once per class loading. If a different ClassLoader loads the same class, the static block can execute again for that ClassLoader.

</details>
