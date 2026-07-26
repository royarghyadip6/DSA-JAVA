# 1. What are the Four Pillars of OOP?

1. **Encapsulation** – Binding data and methods together and restricting direct access to data.
2. **Abstraction** – Hiding implementation details and showing only essential functionality.
3. **Inheritance** – Acquiring properties and behaviors from a parent class.
4. **Polymorphism** – One interface, multiple implementations.

**Example:**

```java
class Animal {
    void sound() {
        System.out.println("Animal Sound");
    }
}

class Dog extends Animal {
    @Override
    void sound() {
        System.out.println("Bark");
    }
}
```

---

# 2. Difference between Abstraction and Encapsulation

| Abstraction                                    | Encapsulation                                        |
|------------------------------------------------|------------------------------------------------------|
| Hides implementation details                   | Hides data                                           |
| Focuses on "What"                              | Focuses on "How"                                     |
| Achieved using abstract classes and interfaces | Achieved using private variables and getters/setters |
| Improves design                                | Improves security                                    |

**Example:**

**Abstraction**

```java
interface Payment {
    void pay();
}
```

**Encapsulation**

```java
class Employee {
    private int salary;

    public int getSalary() {
        return salary;
    }
}
```

---

# 3. Difference between Abstraction and Interface

| Abstraction                                       | Interface                                   |
|---------------------------------------------------|---------------------------------------------|
| OOP concept                                       | Java implementation of abstraction          |
| Can be achieved using abstract class or interface | Interface is one way to achieve abstraction |
| May contain both abstract and concrete methods    | Java 8+: abstract, default, static methods  |
| Can have state (instance variables)               | Only constants (public static final)        |

**Interview Answer:**

> Abstraction is a concept, whereas Interface is a mechanism provided by Java to implement abstraction.

---

# 4. Difference between Inheritance and Composition

| Inheritance             | Composition             |
|-------------------------|-------------------------|
| IS-A relationship       | HAS-A relationship      |
| Tight coupling          | Loose coupling          |
| Child depends on parent | Classes are independent |
| Less flexible           | More flexible           |

**Inheritance Example**

```java
class Animal {}
class Dog extends Animal {}
```

**Composition Example**

```java
class Engine {}

class Car {
    private Engine engine = new Engine();
}
```

**Interview Point:**

> Composition is generally preferred over inheritance because it provides better flexibility and lower coupling.

---

# 5. What is Polymorphism?

Polymorphism means **one interface, multiple forms**.

A parent reference can point to different child objects and invoke their specific implementations.

```java
Animal a = new Dog();
a.sound();
```

Output:

```text
Bark
```

---

# 6. Compile-Time vs Runtime Polymorphism

| Compile-Time        | Runtime                   |
|---------------------|---------------------------|
| Method Overloading  | Method Overriding         |
| Decided by compiler | Decided by JVM at runtime |
| Static Binding      | Dynamic Binding           |
| Faster              | Slightly slower           |

### Compile-Time

```java
class Test {
    void show(int a) {}
    void show(String s) {}
}
```

### Runtime

```java
Animal a = new Dog();
a.sound();
```

---

# 7. Method Overloading vs Method Overriding

| Overloading                                    | Overriding                         |
|------------------------------------------------|------------------------------------|
| Same method name, different parameters         | Same method signature              |
| Same class                                     | Parent-child relationship          |
| Compile-time polymorphism                      | Runtime polymorphism               |
| Return type may differ (with different params) | Return type must be same/covariant |

### Overloading

```java
class Calculator {

    // Overloaded methods
    int add(int a, int b) {
        return a + b;
    }

    int add(int a, int b, int c) {
        return a + b + c;
    }
}
```

### Overriding

```java
class Animal {
    void sound() {
        System.out.println("Animal Sound");
    }
}

class Dog extends Animal {

    @Override
    void sound() {
        System.out.println("Dog Barks");
    }
}
```

```java
Animal animal = new Dog();
animal.sound(); // Runtime decides which method to call
```

---

# 8. Can We Overload main() Method?

**Yes.**

The JVM calls only:

```java
public static void main(String[] args)
```

but we can define additional overloaded versions.

```java
public class Test {

    public static void main(String[] args) {

        System.out.println("JVM calls this method");

        // Calling overloaded version manually
        main(100);
    }

    public static void main(int num) {
        System.out.println("Overloaded main(): " + num);
    }
}
```

Output:

```text
JVM calls this method
Overloaded main(): 100
```

---

# 9. Can We Override Static Methods?

**No.**

Static methods belong to the class, not the object.

If a child defines the same static method, it is called **method hiding**, not overriding.

```java
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

public class Test {
    public static void main(String[] args) {

        Parent p = new Child();

        // Method call resolved using reference type (Parent)
        p.show();
    }
}
```

Output:

```text
Parent
```

**Interview Point:**

> Static methods are resolved at compile time, so they cannot participate in runtime polymorphism.

---

# 10. Can We Override Private Methods?

**No.**

Private methods are not inherited by child classes.

```java
class Parent {
    private void show() {
        System.out.println("Parent");
    }
}

class Child extends Parent {
    void show() {
        System.out.println("Child");
    }
}
```

This is a new method in `Child`, not an overridden method.

**Interview Point:**

> Since private methods are not visible outside their class, they are not inherited and therefore cannot be overridden.

---

# 11. Why is Composition Preferred Over Inheritance?

**Answer:**
Composition is preferred because it provides **loose coupling** and **better flexibility**. With composition, behavior can be changed at runtime by replacing dependent objects, whereas inheritance creates a strong dependency between parent and child.

### Example

```java
class Engine {
    void start() {
        System.out.println("Engine Started");
    }
}

class Car {

    // HAS-A relationship
    private Engine engine = new Engine();

    void startCar() {
        engine.start();
    }
}
```

**Interview Point:**

> "Favor Composition over Inheritance" is a common design principle because it improves maintainability and flexibility.

---

# 12. What is the IS-A Relationship?

**Answer:**
IS-A represents **Inheritance**. A child class is a specialized version of a parent class.

### Example

```java
class Animal {
}

class Dog extends Animal {
}
```

**Interpretation:**

```text
Dog IS-A Animal
```

**Interview Point:**

> Use inheritance only when a true IS-A relationship exists.

---

# 13. What is the HAS-A Relationship?

**Answer:**
HAS-A represents **Composition/Aggregation**, where one class contains another class as a member.

### Example

```java
class Engine {
}

class Car {

    // Car HAS-A Engine
    private Engine engine = new Engine();
}
```

**Interpretation:**

```text
Car HAS-A Engine
```

---

# 14. What is Tight Coupling?

**Answer:**
Tight coupling means one class is heavily dependent on another class's implementation. Changes in one class may require changes in another.

### Example

```java
class MySQLDatabase {
    void connect() {
        System.out.println("Connected to MySQL");
    }
}

class UserService {

    // Direct dependency
    private MySQLDatabase db = new MySQLDatabase();

    void saveUser() {
        db.connect();
    }
}
```

### Disadvantage

* Difficult to test
* Difficult to replace implementation
* Less maintainable

---

# 15. What is Loose Coupling?

**Answer:**
Loose coupling means classes depend on abstractions (interfaces) rather than concrete implementations.

### Example

```java
interface Database {
    void connect();
}

class MySQLDatabase implements Database {
    public void connect() {
        System.out.println("Connected to MySQL");
    }
}

class UserService {

    private Database db;

    UserService(Database db) {
        this.db = db;
    }

    void saveUser() {
        db.connect();
    }
}
```

### Advantages

* Easy to test
* Easy to replace implementation
* Better maintainability

**Interview Point:**

> Spring Framework promotes loose coupling through Dependency Injection.

---

# 16. What is Association?

**Answer:**
Association is a relationship where two independent objects are connected and can interact with each other.

### Example

```java
class Teacher {
}

class Student {

    // Association
    private Teacher teacher;
}
```

**Interpretation:**

```text
Teacher and Student are associated.
```

**Interview Point:**

> Association is the broadest relationship; Aggregation and Composition are specialized forms of Association.

---

# 17. Difference Between Association, Aggregation and Composition

| Feature               | Association  | Aggregation    | Composition      |
| --------------------- | ------------ | -------------- | ---------------- |
| Relationship          | Uses         | HAS-A          | Strong HAS-A     |
| Ownership             | No ownership | Weak ownership | Strong ownership |
| Independent Lifecycle | Yes          | Yes            | No               |
| Strength              | Weak         | Medium         | Strong           |

---

### Association

```java
class Teacher {}
class Student {
    Teacher teacher;
}
```

```text
Student uses Teacher
```

---

### Aggregation

```java
class Department {
}

class Employee {

    // Employee can exist independently
    private Department department;
}
```

```text
Employee HAS-A Department
```

If Department is removed, Employee can still exist.

---

### Composition

```java
class Engine {
}

class Car {

    // Strong ownership
    private Engine engine = new Engine();
}
```

```text
Car HAS-A Engine
```

If Car is destroyed, Engine typically has no meaning independently.

**Interview Shortcut:**

```text
Association → Uses
Aggregation → Weak HAS-A
Composition → Strong HAS-A
```

---

# 18. What is Covariant Return Type?

**Answer:**
Covariant return type allows an overridden method to return a subtype of the original return type.

### Example

```java
class Animal {
}

class Dog extends Animal {
}

class Parent {

    Animal getObject() {
        return new Animal();
    }
}

class Child extends Parent {

    @Override
    Dog getObject() {     // Subclass return type
        return new Dog();
    }
}
```

**Interview Point:**

> Introduced in Java 5 to make overriding more flexible.

---

# 19. Can Constructors Be Inherited?

**Answer:**
No.

Constructors belong to the class itself and are used to initialize that class's objects. Therefore, they are not inherited by child classes.

### Example

```java
class Parent {

    Parent() {
        System.out.println("Parent Constructor");
    }
}

class Child extends Parent {

    Child() {
        System.out.println("Child Constructor");
    }
}
```

Even though Parent's constructor executes, it is **not inherited**.

---

# 20. Why Can't Constructors Be Overridden?

**Answer:**
Constructors cannot be overridden because:

1. Constructors are not inherited.
2. Overriding requires inheritance.
3. Constructors have the same name as the class, and child/parent class names are different.

### Example

```java
class Parent {

    Parent() {
        System.out.println("Parent");
    }
}

class Child extends Parent {

    Child() {
        System.out.println("Child");
    }
}
```

### What Happens Internally?

```java
Child child = new Child();
```

Execution order:

```text
Parent Constructor
Child Constructor
```

**Interview Point:**

> Constructors participate in constructor chaining using `super()`, but they do not participate in method overriding.

---

# 21. Explain Real-Life Use of Polymorphism in Your Project

**Answer:**

In a Spring Boot application, we often program against an interface and inject different implementations.

### Example: Payment Service

```java id="4cx8rl"
public interface PaymentService {
    void pay(double amount);
}

@Service
public class CreditCardPaymentService implements PaymentService {

    @Override
    public void pay(double amount) {
        System.out.println("Paid using Credit Card");
    }
}

@Service
public class UPIPaymentService implements PaymentService {

    @Override
    public void pay(double amount) {
        System.out.println("Paid using UPI");
    }
}
```

```java id="3if8mh"
@Autowired
private PaymentService paymentService;
```

**Interview Answer:**

> In my projects, polymorphism is commonly used through interfaces and Spring Dependency Injection. The application depends on the interface, and Spring injects the required implementation at runtime, making the code loosely coupled and easy to maintain.

---

# 22. How Does Dynamic Method Dispatch Work Internally?

**Answer:**

Dynamic Method Dispatch is the mechanism by which JVM decides which overridden method to call **at runtime**, based on the actual object type.

### Example

```java id="hlxqhi"
class Animal {
    void sound() {
        System.out.println("Animal Sound");
    }
}

class Dog extends Animal {

    @Override
    void sound() {
        System.out.println("Dog Barks");
    }
}
```

```java id="iv3nyk"
Animal animal = new Dog();

animal.sound();
```

**Output**

```text id="j7itvp"
Dog Barks
```

### Internally

1. Compiler checks that `sound()` exists in `Animal`.
2. JVM sees actual object is `Dog`.
3. JVM invokes `Dog.sound()`.

**Interview Point:**

> Method call is determined at runtime using the actual object type, not the reference type.

---

# 23. What Happens if Parent and Child Contain Same Field Name?

**Answer:**

Fields are **hidden**, not overridden.

Field access depends on the **reference type**, whereas method calls depend on the **object type**.

### Example

```java id="y89qqq"
class Parent {
    String name = "Parent";
}

class Child extends Parent {
    String name = "Child";
}
```

```java id="z14sre"
Parent p = new Child();

System.out.println(p.name);
```

**Output**

```text id="k1iqvo"
Parent
```

### Compare with Method Overriding

```java id="mfiy95"
class Parent {
    void show() {
        System.out.println("Parent");
    }
}

class Child extends Parent {

    @Override
    void show() {
        System.out.println("Child");
    }
}
```

```java id="w27ydk"
Parent p = new Child();

p.show();
```

**Output**

```text id="7fg2te"
Child
```

**Interview Point:**

> Variables follow reference type (static binding), methods follow object type (dynamic binding).

---

# 24. Why Java Doesn't Support Multiple Inheritance Through Classes?

**Answer:**

Java avoids multiple inheritance through classes to prevent ambiguity and complexity.

### Diamond Problem

```java id="jsmw3l"
class A {
    void show() {
        System.out.println("A");
    }
}

class B extends A {
}

class C extends A {
}
```

Suppose Java allowed:

```java id="kzc8lr"
class D extends B, C {
}
```

Now if:

```java id="fwm20g"
D d = new D();
d.show();
```

The JVM would not know whether to use B's version or C's version if both override `show()`.

### Solution in Java

Java allows:

```java id="xg7cfp"
interface A {}
interface B {}

class C implements A, B {
}
```

**Interview Point:**

> Java supports multiple inheritance through interfaces but not through classes to avoid the Diamond Problem.

---

# 25. How Does JVM Resolve Overridden Methods?

**Answer:**

For overridden methods, JVM uses **runtime polymorphism (dynamic binding)**.

### Example

```java id="r0omgj"
class Parent {
    void show() {
        System.out.println("Parent");
    }
}

class Child extends Parent {

    @Override
    void show() {
        System.out.println("Child");
    }
}
```

```java id="swg2va"
Parent p = new Child();

p.show();
```

**Output**

```text id="fkkk8k"
Child
```

### JVM Resolution Process

1. Compiler verifies `show()` exists in `Parent`.
2. Object created is `Child`.
3. JVM looks at actual object's method table (v-table concept).
4. JVM finds overridden method in `Child`.
5. `Child.show()` gets executed.

### Static Binding vs Dynamic Binding

| Type               | Resolved By |
| ------------------ | ----------- |
| Variables          | Compiler    |
| Static Methods     | Compiler    |
| Private Methods    | Compiler    |
| Overloaded Methods | Compiler    |
| Overridden Methods | JVM Runtime |

**Interview Point:**

> Overridden methods are resolved using dynamic binding at runtime, which is the foundation of runtime polymorphism.
