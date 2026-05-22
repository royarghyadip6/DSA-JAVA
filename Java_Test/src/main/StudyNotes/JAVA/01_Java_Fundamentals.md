# Java Fundamentals Revision Notes

1. [OOPs Deep Dive](#-oops-deep-dive)

    <details>
   * Encapsulation
   * Abstraction
   * Inheritance
   * Polymorphism
   * Composition vs Inheritance
   * Association/Aggregation
   * Method Overriding
   * Runtime Polymorphism
    </details>

2. [Class & Object Internals](#-class--object-internals)
    <details>
   * Memory allocation
   * Stack vs Heap
   * Object creation process
   * Constructor chaining
   * `this` vs `super`
    </details>
3. [Access Modifiers](#-access-modifiers)
    <details>
   * public/private/protected/default
   * package-level visibility
    </details>
4. [Static Keyword](#-static-keyword)
    <details>
   * static block
   * static variable
   * static method
   * static nested class
    </details>
5. [final Keyword](#-final-keyword)
    <details>
   * final variable
   * final method
   * final class
   * immutability
    </details>
6. [equals() & hashCode()](#-equals--hashcode--very-important)
    <details>
   * Contract rules
   * HashMap dependency
   * Common bugs
   * Performance impact
    </details>
7. [String Internals](#-string-internals)
    <details>
   * String Pool
   * Immutable behavior
   * SCP (String Constant Pool)
   * StringBuilder vs StringBuffer
   * String vs new String()
    </details>
---

# 🔹 OOPs Deep Dive

---

# 1. Encapsulation

## ✅ Definition

Encapsulation means **binding data (variables) and methods together into a single unit (class)** and restricting direct access to data.

It is achieved using:

* `private` variables
* `public getter/setter methods`

---

## ✅ Why Encapsulation?

* Data hiding
* Better security
* Controlled access
* Easy maintenance
* Flexible implementation changes

---

## ✅ Example

```java
class Employee {

    private String name;   // hidden data
    private int salary;

    // getter
    public String getName() {
        return name;
    }

    // setter
    public void setName(String name) {
        this.name = name;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {

        // validation logic
        if (salary > 0) {
            this.salary = salary;
        }
    }
}

public class Main {
    public static void main(String[] args) {

        Employee emp = new Employee();

        emp.setName("Arghya");
        emp.setSalary(50000);

        System.out.println(emp.getName());
        System.out.println(emp.getSalary());
    }
}
```

---

## ✅ Real-world Example

ATM Machine:

* User cannot directly access bank balance variable.
* Access only through methods like:

    * withdraw()
    * deposit()
    * checkBalance()

---

## ✅ Interview Points

### Why use private variables?

To prevent unauthorized access and protect object state.

### Is encapsulation only data hiding?

No.
It is:

* Data + methods binding
* Controlled access

---

## ✅ Common Mistakes

❌ Public variables everywhere

```java
public int salary;
```

This breaks encapsulation.

---

# 2. Abstraction

## ✅ Definition

Abstraction means:

> Showing only essential details and hiding internal implementation.

---

## ✅ Achieved Using

* Abstract classes
* Interfaces

---

# ✅ Abstract Class Example

```java
abstract class Vehicle {

    abstract void start();

    void stop() {
        System.out.println("Vehicle stopped");
    }
}

class Car extends Vehicle {

    @Override
    void start() {
        System.out.println("Car starts with key");
    }
}

public class Main {
    public static void main(String[] args) {

        Vehicle v = new Car();

        v.start();
        v.stop();
    }
}
```

---

# ✅ Interface Example

```java
interface Payment {

    void pay();
}

class UpiPayment implements Payment {

    public void pay() {
        System.out.println("Paid using UPI");
    }
}
```

---

## ✅ Real-world Example

Car driving:

* You use steering, brake, accelerator
* Internal engine logic hidden

---

## ✅ Abstract Class vs Interface

| Feature              | Abstract Class             | Interface           |
|----------------------|----------------------------|---------------------|
| Methods              | Abstract + concrete        | Mostly abstract     |
| Variables            | Instance variables allowed | public static final |
| Constructor          | Yes                        | No                  |
| Multiple inheritance | No                         | Yes                 |

---

## ✅ Interview Points

### 100% abstraction possible?

Using interfaces.

### Can abstract class have constructor?

Yes.

---

# 3. Inheritance

## ✅ Definition

Inheritance allows one class to acquire properties and behavior of another class.

---

## ✅ Syntax

```java
class Parent {
}

class Child extends Parent {
}
```

---

## ✅ Example

```java
class Animal {

    void eat() {
        System.out.println("Animal eats");
    }
}

class Dog extends Animal {

    void bark() {
        System.out.println("Dog barks");
    }
}

public class Main {

    public static void main(String[] args) {

        Dog d = new Dog();

        d.eat();
        d.bark();
    }
}
```

---

## ✅ Types of Inheritance

| Type         | Supported in Java? |
|--------------|--------------------|
| Single       | ✅                  |
| Multilevel   | ✅                  |
| Hierarchical | ✅                  |
| Multiple     | ❌ with class       |
| Hybrid       | ❌ with class       |

Java supports multiple inheritance using interfaces.

---

## ✅ Why Java avoids multiple inheritance with classes?

To avoid:

# Diamond Problem

---

# 4. Polymorphism

## ✅ Definition

Polymorphism means:

> One thing behaving in multiple forms.

---

# ✅ Types

| Type         | Achieved By        |
|--------------|--------------------|
| Compile-time | Method Overloading |
| Runtime      | Method Overriding  |

---

# ✅ Method Overloading Example

```java
class Calculator {

    int add(int a, int b) {
        return a + b;
    }

    int add(int a, int b, int c) {
        return a + b + c;
    }
}
```

---

# 5. Method Overriding

## ✅ Definition

Child class provides specific implementation of parent method.

---

## ✅ Rules

* Method name same
* Parameters same
* IS-A relationship required
* Return type same/covariant
* Cannot reduce visibility

---

## ✅ Example

```java
class Animal {

    void sound() {
        System.out.println("Animal sound");
    }
}

class Dog extends Animal {

    @Override
    void sound() {
        System.out.println("Dog barks");
    }
}
```

---

# 6. Runtime Polymorphism

## ✅ Definition

Method call resolved at runtime based on actual object.

---

## ✅ Example

```java
class Animal {

    void sound() {
        System.out.println("Animal sound");
    }
}

class Dog extends Animal {

    @Override
    void sound() {
        System.out.println("Dog barks");
    }
}

public class Main {

    public static void main(String[] args) {

        Animal a = new Dog();

        a.sound();
    }
}
```

Output:

```java
Dog barks
```

---

## ✅ Internal Working

JVM uses:

* Dynamic Method Dispatch
* Method table lookup at runtime

---

## ✅ Important Interview Question

### Which methods cannot be overridden?

* static
* final
* private

---

# 7. Composition vs Inheritance

---

# ✅ Inheritance (IS-A)

```java
class Animal {
}

class Dog extends Animal {
}
```

Dog IS-A Animal.

---

# ✅ Composition (HAS-A)

```java
class Engine {
}

class Car {

    Engine engine = new Engine();
}
```

Car HAS-A Engine.

---

# ✅ Which is better?

Prefer:

# Composition over inheritance

Because:

* Loose coupling
* Better flexibility
* Easier maintenance

---

# ✅ Real-world Example

❌ Bad inheritance:

```java
class Car extends Engine
```

✅ Correct:

```java
class Car has Engine
```

---

# 8. Association & Aggregation

---

# ✅ Association

Relationship between two independent objects.

Example:

* Teacher ↔ Student

```java
class Teacher {
}

class Student {
}
```

---

# ✅ Aggregation

Special type of association with weak ownership.

Example:

* Department HAS Employees

Employees can exist independently.

```java
class Employee {
}

class Department {

    List<Employee> employees;
}
```

---

# ✅ Aggregation vs Composition

| Feature             | Aggregation         | Composition |
|---------------------|---------------------|-------------|
| Ownership           | Weak                | Strong      |
| Lifecycle dependent | No                  | Yes         |
| Example             | Department-Employee | House-Room  |

---

# 🔹 Class & Object Internals

---

# 1. Memory Allocation

Java memory mainly divided into:

| Area        | Stores                           |
|-------------|----------------------------------|
| Stack       | Method calls, local variables    |
| Heap        | Objects                          |
| Method Area | Class metadata, static variables |

---

# 2. Stack vs Heap

| Stack                  | Heap           |
|------------------------|----------------|
| Stores local variables | Stores objects |
| Thread-specific        | Shared memory  |
| Faster                 | Slower         |
| Auto cleanup           | GC cleanup     |

---

## ✅ Example

```java
class Student {

    int id;
}

public class Main {

    public static void main(String[] args) {

        int x = 10;

        Student s = new Student();
    }
}
```

---

## ✅ Memory Breakdown

### Stack

```java
x = 10
s -> reference
```

### Heap

```java
Student object
id = 0
```

---

# 3. Object Creation Process

When:

```java
Student s = new Student();
```

runs internally:

---

## ✅ Step-by-Step

### 1. Class Loading

JVM loads class if not already loaded.

---

### 2. Memory Allocation

Heap memory allocated.

---

### 3. Default Initialization

```java
int = 0
boolean = false
Object = null
```

---

### 4. Constructor Call

Constructor executes.

---

### 5. Reference Assignment

Reference stored in stack.

---

# 4. Constructor Chaining

Calling one constructor from another.

---

# ✅ Using this()

```java
class Employee {

    Employee() {
        this(100);
        System.out.println("Default Constructor");
    }

    Employee(int id) {
        System.out.println("Parameterized Constructor");
    }
}
```

Output:

```java
Parameterized Constructor
Default Constructor
```

---

# ✅ Rules

* `this()` must be first statement.
* Used within same class.

---

# ✅ Using super()

Calls parent constructor.

```java
class Parent {

    Parent() {
        System.out.println("Parent Constructor");
    }
}

class Child extends Parent {

    Child() {
        super();
        System.out.println("Child Constructor");
    }
}
```

---

# 5. this vs super

| Feature                  | this           | super         |
|--------------------------|----------------|---------------|
| Refers to                | Current object | Parent object |
| Calls constructor        | this()         | super()       |
| Access current variables | Yes            | No            |
| Access parent variables  | No             | Yes           |

---

# ✅ Example

```java
class Parent {

    int x = 10;
}

class Child extends Parent {

    int x = 20;

    void show() {

        System.out.println(this.x);
        System.out.println(super.x);
    }
}
```

Output:

```java
20
10
```

---

# ✅ Important Interview Questions

---

## Q1. Why String is immutable?

* Security
* Thread safety
* String pool optimization
* Hashcode caching

---

## Q2. Why objects stored in heap?

Because heap is shared and objects may be accessed from multiple methods/threads.

---

## Q3. Can constructor be overridden?

❌ No

Because constructors are not inherited.

---

## Q4. Difference between overloading and overriding?

| Overloading          | Overriding         |
|----------------------|--------------------|
| Same method name     | Same method name   |
| Different parameters | Same parameters    |
| Compile time         | Runtime            |
| In same class        | Parent-child class |

---

# ✅ Quick Revision Summary

| Concept       | Key Point                 |
|---------------|---------------------------|
| Encapsulation | Data hiding               |
| Abstraction   | Hiding implementation     |
| Inheritance   | Code reuse                |
| Polymorphism  | Multiple behaviors        |
| Overloading   | Compile-time polymorphism |
| Overriding    | Runtime polymorphism      |
| Composition   | HAS-A                     |
| Inheritance   | IS-A                      |
| Stack         | Local variables           |
| Heap          | Objects                   |
| this          | Current object            |
| super         | Parent object             |

# Java Fundamentals Revision Notes

# 🔹 Access Modifiers

Access modifiers control:

* Visibility
* Accessibility
* Scope of classes, methods, variables, constructors

---

# 1. public Access Modifier

## ✅ Accessible From

* Same class
* Same package
* Different package
* Different package child class

---

## ✅ Example

```java id="7xwlxg"
public class Employee {

    public void show() {
        System.out.println("Public Method");
    }
}
```

Can be accessed from anywhere.

---

# 2. private Access Modifier

## ✅ Accessible Only

Inside the same class.

---

## ✅ Example

```java id="g7c4ij"
class Employee {

    private int salary;

    private void display() {
        System.out.println("Private Method");
    }
}
```

---

## ✅ Important Point

Private members are NOT inherited directly.

---

## ✅ Why private?

For:

* Encapsulation
* Security
* Controlled access

---

# 3. protected Access Modifier

## ✅ Accessible From

* Same class
* Same package
* Child classes outside package

---

## ✅ Example

```java id="6e5m91"
class Parent {

    protected void show() {
        System.out.println("Protected Method");
    }
}

class Child extends Parent {

    void test() {
        show();
    }
}
```

---

# 4. Default Access Modifier (Package-Private)

When no modifier is specified.

---

## ✅ Accessible From

Only within same package.

---

## ✅ Example

```java id="s76m24"
class Employee {

    void display() {
        System.out.println("Default Access");
    }
}
```

---

# 5. Access Modifier Visibility Table

| Modifier  | Same Class | Same Package | Child Class Outside Package | Outside Package |
|-----------|------------|--------------|-----------------------------|-----------------|
| private   | ✅          | ❌            | ❌                           | ❌               |
| default   | ✅          | ✅            | ❌                           | ❌               |
| protected | ✅          | ✅            | ✅                           | ❌               |
| public    | ✅          | ✅            | ✅                           | ✅               |

---

# 6. Package-Level Visibility

---

## ✅ What is Package?

A package is a namespace used to organize classes.

Example:

```java id="g0z2hq"
package com.company.service;
```

---

## ✅ Why Packages?

* Avoid class name conflict
* Better organization
* Access control
* Modular design

---

## ✅ Example

### Package A

```java id="8i2j0k"
package pack1;

class Employee {

    void show() {
        System.out.println("Hello");
    }
}
```

Accessible only inside `pack1`.

---

# 🔹 Static Keyword

---

# ✅ What is static?

`static` belongs to:

# Class itself, not object

Only one copy exists.

---

# 1. Static Variable

---

## ✅ Shared Across All Objects

```java id="q4n2l5"
class Employee {

    int id;
    static String company = "TCS";
}
```

---

## ✅ Memory Allocation

Stored in:

# Method Area / Metaspace

Not in heap object.

---

## ✅ Example

```java id="3g42tv"
class Counter {

    static int count = 0;

    Counter() {
        count++;
        System.out.println(count);
    }
}

public class Main {

    public static void main(String[] args) {

        new Counter();
        new Counter();
        new Counter();
    }
}
```

Output:

```java id="1i6f1n"
1
2
3
```

---

# 2. Static Method

---

## ✅ Characteristics

* Called using class name
* Cannot access non-static directly
* Cannot use `this` or `super`

---

## ✅ Example

```java id="m9m0n5"
class MathUtil {

    static int square(int x) {
        return x * x;
    }
}

public class Main {

    public static void main(String[] args) {

        System.out.println(MathUtil.square(5));
    }
}
```

---

## ✅ Why main() is static?

JVM can call it without creating object.

---

# 3. Static Block

---

## ✅ Purpose

Used for:

* Static initialization
* One-time setup

Runs only once when class loads.

---

## ✅ Example

```java id="5h5npx"
class Test {

    static {

        System.out.println("Static Block Executed");
    }

    public static void main(String[] args) {

        System.out.println("Main Method");
    }
}
```

Output:

```java id="6n4h9z"
Static Block Executed
Main Method
```

---

## ✅ Internal Working

When JVM loads class:

1. Static variables initialized
2. Static blocks executed
3. main() executed

---

# 4. Static Nested Class

---

## ✅ Definition

Nested class declared static.

---

## ✅ Example

```java id="5ztrm0"
class Outer {

    static class Inner {

        void display() {
            System.out.println("Static Nested Class");
        }
    }
}

public class Main {

    public static void main(String[] args) {

        Outer.Inner obj = new Outer.Inner();

        obj.display();
    }
}
```

---

## ✅ Important Point

Static nested class:

* Cannot directly access outer non-static members
* Behaves like normal class

---

# 🔹 final Keyword

---

# ✅ Meaning of final

Once assigned:

# Cannot be changed

---

# 1. final Variable

---

## ✅ Constant Variable

```java id="8pqhhf"
class Test {

    final int MAX = 100;
}
```

---

## ❌ Invalid

```java id="jxzzjz"
MAX = 200;
```

Compile-time error.

---

# ✅ Blank Final Variable

Assigned later via constructor.

```java id="m80f7d"
class Student {

    final int rollNo;

    Student(int rollNo) {
        this.rollNo = rollNo;
    }
}
```

---

# ✅ final Reference Variable

Reference cannot change.

```java id="r64p0m"
final StringBuilder sb = new StringBuilder("Hello");

sb.append(" Java"); // allowed
```

Object mutable.
Reference fixed.

---

# 2. final Method

---

## ✅ Cannot be overridden

```java id="3x50pi"
class Parent {

    final void show() {
        System.out.println("Final Method");
    }
}
```

---

## ❌ Invalid

```java id="v6tyaw"
class Child extends Parent {

    void show() {
    }
}
```

Compile-time error.

---

## ✅ Why final methods?

* Security
* Prevent behavior modification
* Optimization opportunity

---

# 3. final Class

---

## ✅ Cannot be inherited

```java id="0q8j8n"
final class Vehicle {
}
```

---

## ❌ Invalid

```java id="99e39p"
class Car extends Vehicle {
}
```

---

# ✅ Real-world Example

Java `String` class is final.

Why?

* Security
* Immutability
* Prevent modification

---

# 4. Immutability

---

# ✅ Definition

Immutable object:

# Object state cannot change after creation

---

# ✅ Example of Immutable Class

```java id="1v2qj4"
final class Employee {

    private final int id;
    private final String name;

    Employee(int id, String name) {

        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
```

---

# ✅ Characteristics of Immutable Class

* Class should be final
* Fields private final
* No setters
* Initialization via constructor

---

# ✅ Why String is Immutable?

---

## ✅ Security

Used in:

* URL
* Database connection
* File path

Prevent modification.

---

## ✅ Thread Safety

No synchronization needed.

---

## ✅ String Pool Optimization

Reusability improves memory efficiency.

---

# ✅ Mutable vs Immutable

| Mutable       | Immutable     |
|---------------|---------------|
| Can change    | Cannot change |
| StringBuilder | String        |
| ArrayList     | LocalDate     |
| HashMap       | Integer       |

---

# ✅ Important Interview Questions

---

## Q1. Can constructor be final?

❌ No

Because constructors are not inherited.

---

## Q2. Can static methods be overridden?

❌ No

They are hidden, not overridden.

---

## Q3. Can private methods be overridden?

❌ No

Because they are not visible to child class.

---

## Q4. Can final variable be uninitialized?

✅ Yes, blank final variable.

Must initialize in constructor.

---

## Q5. Difference between final, finally, finalize?

| Keyword    | Purpose                  |
|------------|--------------------------|
| final      | Restriction              |
| finally    | Exception handling block |
| finalize() | GC cleanup method        |

---

# ✅ Quick Revision Summary

| Concept          | Key Point                      |
|------------------|--------------------------------|
| public           | Accessible everywhere          |
| private          | Same class only                |
| protected        | Package + child class          |
| default          | Same package only              |
| static           | Belongs to class               |
| static variable  | Shared memory                  |
| static block     | Runs once during class loading |
| static method    | No object required             |
| final variable   | Constant                       |
| final method     | Cannot override                |
| final class      | Cannot inherit                 |
| immutable object | State cannot change            |

# Java Fundamentals Revision Notes

# 🔹 equals() & hashCode() — VERY IMPORTANT

This is one of the most important interview topics in Java.

Especially important for:

* HashMap
* HashSet
* Caching
* Collections
* Performance optimization

---

# 1. equals() Method

---

## ✅ Purpose

Used to compare:

# Logical equality (content comparison)

Defined in:
Java `Object` class.

---

# ✅ Default equals() Behavior

Default implementation compares:

# Memory addresses (reference equality)

Equivalent to:

```java id="10skys"
==
```

---

## ✅ Example

```java id="mdt5ja"
class Employee {

    int id;

    Employee(int id) {
        this.id = id;
    }
}

public class Main {

    public static void main(String[] args) {

        Employee e1 = new Employee(101);
        Employee e2 = new Employee(101);

        System.out.println(e1.equals(e2));
    }
}
```

Output:

```java id="jq3i97"
false
```

Because both objects are different in memory.

---

# 2. Overriding equals()

---

## ✅ Example

```java id="0nv5p9"
class Employee {

    int id;

    Employee(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {

        Employee e = (Employee) obj;

        return this.id == e.id;
    }
}

public class Main {

    public static void main(String[] args) {

        Employee e1 = new Employee(101);
        Employee e2 = new Employee(101);

        System.out.println(e1.equals(e2));
    }
}
```

Output:

```java id="q7l6vn"
true
```

---

# 3. hashCode() Method

---

## ✅ Purpose

Returns integer hash value of object.

Used heavily in:

* HashMap
* HashSet
* Hashtable

---

## ✅ Defined In

`Object` class.

---

# ✅ Internal Idea

Hash-based collections use hashCode() to:

1. Find bucket
2. Improve search performance

---

# 4. equals() and hashCode() Contract Rules

---

# ✅ MOST IMPORTANT INTERVIEW TOPIC

---

## ✅ Rule 1

If:

```java id="7x99h0"
a.equals(b) == true
```

Then:

```java id="2m6g3q"
a.hashCode() == b.hashCode()
```

MUST be true.

---

## ✅ Rule 2

If hashcodes are same:

```java id="52m1is"
a.hashCode() == b.hashCode()
```

equals may be:

* true OR false

(Hash collision possible)

---

## ✅ Rule 3

Multiple calls to hashCode() should return same value
(unless object data changes)

---

# 5. Correct equals() + hashCode() Implementation

---

## ✅ Proper Example

```java id="06v0r4"
import java.util.Objects;

class Employee {

    int id;
    String name;

    Employee(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;

        Employee e = (Employee) obj;

        return id == e.id &&
               Objects.equals(name, e.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name);
    }
}
```

---

# 6. Why HashMap Depends on hashCode()

---

# ✅ Internal Working of HashMap

When inserting:

```java id="8blfkc"
map.put(key, value);
```

Steps:

---

## ✅ Step 1

Calls:

```java id="mq0s1n"
key.hashCode()
```

---

## ✅ Step 2

Calculates bucket index.

---

## ✅ Step 3

Stores object in bucket.

---

## ✅ Step 4

If collision occurs:

* uses equals()
* checks actual equality

---

# ✅ Retrieval Process

```java id="kq0a4y"
map.get(key);
```

Again:

1. hashCode()
2. bucket search
3. equals()

---

# 7. Common Bug — VERY IMPORTANT

---

# ❌ Overriding equals() Without hashCode()

---

## ❌ Wrong Example

```java id="13lmrk"
class Employee {

    int id;

    Employee(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {

        Employee e = (Employee) obj;

        return this.id == e.id;
    }
}
```

---

# ❌ Problem

```java id="pj3jvk"
HashSet<Employee> set = new HashSet<>();

set.add(new Employee(101));

System.out.println(set.contains(new Employee(101)));
```

Expected:

```java id="e3cxqr"
true
```

Actual:

```java id="5lkqcc"
false
```

---

# ✅ Why?

Different hashCode().
Different buckets.

equals() never reached.

---

# 8. Performance Impact

---

# ✅ Good hashCode()

Gives:

* Uniform distribution
* Faster lookup
* O(1) average complexity

---

# ❌ Bad hashCode()

Example:

```java id="c5p7zj"
@Override
public int hashCode() {
    return 1;
}
```

All objects go into same bucket.

Performance becomes:

```java id="gqz27o"
O(n)
```

instead of:

```java id="ozmo8d"
O(1)
```

---

# 9. == vs equals()

| ==                   | equals()                |
|----------------------|-------------------------|
| Reference comparison | Content comparison      |
| Checks address       | Checks logical equality |
| Faster               | Customizable            |

---

# ✅ Example

```java id="u6btyf"
String s1 = new String("Java");
String s2 = new String("Java");

System.out.println(s1 == s2);
System.out.println(s1.equals(s2));
```

Output:

```java id="w4gx9z"
false
true
```

---

# 🔹 String Internals

---

# 1. What is String?

---

## ✅ String is:

* Immutable
* Final class
* Stored in String Constant Pool
* Most optimized object in Java

---

# 2. String Constant Pool (SCP)

---

# ✅ Definition

Special memory area inside Heap storing string literals.

Purpose:

* Reusability
* Memory optimization

---

# ✅ Example

```java id="v22hnf"
String s1 = "Java";
String s2 = "Java";
```

Only ONE object created in SCP.

Both references point to same object.

---

# ✅ Memory Diagram

```java id="g6n3h8"
SCP:
"Java"

s1 ----|
s2 ----|
```

---

# 3. String vs new String()

---

# ✅ Case 1

```java id="x1v2c0"
String s1 = "Java";
String s2 = "Java";
```

---

## ✅ Objects Created

Only:

```java id="kvh2ja"
1 object in SCP
```

---

## ✅ Comparison

```java id="lrh8o2"
s1 == s2
```

Result:

```java id="2y0nxy"
true
```

---

# ✅ Case 2

```java id="3quzq2"
String s1 = new String("Java");
```

---

## ✅ Objects Created

1. SCP object (if not already present)
2. Heap object

Total:

```java id="11l12l"
2 objects
```

---

# ✅ Memory Diagram

```java id="7ru2vb"
SCP -> "Java"

Heap -> new String("Java")
```

---

## ✅ Comparison

```java id="mjlwm7"
String s1 = new String("Java");
String s2 = new String("Java");

System.out.println(s1 == s2);
```

Output:

```java id="tn9pb0"
false
```

Because heap objects are different.

---

# 4. Immutable Behavior of String

---

# ✅ What is Immutability?

Once created:

# String content cannot change

---

## ✅ Example

```java id="s0xeyg"
String s = "Java";

s.concat(" World");

System.out.println(s);
```

Output:

```java id="cr4kw7"
Java
```

---

# ✅ Why?

`concat()` creates NEW object.

Original object unchanged.

---

# ✅ Correct Way

```java id="scs5d5"
s = s.concat(" World");
```

---

# 5. Why String is Immutable?

---

# ✅ Security

Used in:

* Database URLs
* File paths
* Network connections

---

# ✅ Thread Safety

Immutable objects are naturally thread-safe.

---

# ✅ String Pool Optimization

Safe reuse possible.

---

# ✅ Hashcode Caching

Hashcode cached because content never changes.

Huge performance benefit.

---

# 6. StringBuilder vs StringBuffer

---

# ✅ StringBuilder

* Mutable
* Faster
* Not thread-safe

---

## ✅ Example

```java id="4i7k9w"
StringBuilder sb = new StringBuilder("Java");

sb.append(" World");

System.out.println(sb);
```

Output:

```java id="3ljq6q"
Java World
```

---

# ✅ StringBuffer

* Mutable
* Thread-safe
* Synchronized
* Slower

---

## ✅ Example

```java id="ngzlc7"
StringBuffer sb = new StringBuffer("Java");

sb.append(" World");
```

---

# 7. String vs StringBuilder vs StringBuffer

| Feature          | String                | StringBuilder           | StringBuffer       |
|------------------|-----------------------|-------------------------|--------------------|
| Mutable          | ❌                     | ✅                       | ✅                  |
| Thread-safe      | ✅                     | ❌                       | ✅                  |
| Performance      | Slow for modification | Fastest                 | Slower             |
| Memory efficient | Less for static data  | Better for modification | Better for threads |

---

# 8. Important String Interview Questions

---

# ✅ Q1. How many objects created?

```java id="0km5av"
String s = new String("Java");
```

Answer:

* 1 in SCP
* 1 in Heap

Total:

```java id="4rfq1g"
2
```

(if SCP object not already existing)

---

# ✅ Q2. Difference between SCP and Heap String?

| SCP               | Heap            |
|-------------------|-----------------|
| Reusable          | Non-reusable    |
| Memory optimized  | Separate object |
| Faster comparison | More memory     |

---

# ✅ Q3. Why StringBuilder faster than String?

Because String creates new object on every modification.

---

# ✅ Q4. Why StringBuffer slower?

Because methods are synchronized.

---

# 9. String Interning

---

# ✅ intern() Method

Moves string into SCP.

---

## ✅ Example

```java id="7nhl3d"
String s1 = new String("Java");

String s2 = s1.intern();

String s3 = "Java";

System.out.println(s2 == s3);
```

Output:

```java id="6t0q4q"
true
```

---

# 10. String Memory Optimization Example

---

# ❌ Bad Practice

```java id="h0t4lr"
String s = "";

for(int i = 0; i < 1000; i++) {
    s = s + i;
}
```

Creates many temporary objects.

---

# ✅ Better Approach

```java id="m3w4uk"
StringBuilder sb = new StringBuilder();

for(int i = 0; i < 1000; i++) {
    sb.append(i);
}
```

---

# ✅ Internal Working of String Concatenation

Compiler converts:

```java id="hck72m"
String s = a + b;
```

into:

```java id="w74hdf"
new StringBuilder()
    .append(a)
    .append(b)
    .toString();
```

---

# ✅ Quick Revision Summary

| Concept                  | Key Point                             |
|--------------------------|---------------------------------------|
| equals()                 | Logical equality                      |
| hashCode()               | Bucket calculation                    |
| equals-hashCode contract | Equal objects must have same hashcode |
| HashMap                  | Uses hashCode + equals                |
| SCP                      | String Constant Pool                  |
| String                   | Immutable                             |
| new String()             | Creates heap object                   |
| StringBuilder            | Mutable + fast                        |
| StringBuffer             | Mutable + thread-safe                 |
| intern()                 | Moves string to SCP                   |
