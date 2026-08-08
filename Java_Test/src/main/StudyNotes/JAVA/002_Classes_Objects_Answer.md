# Classes & Objects

# 1. Difference Between Class and Object

<details>
<summary>Show Answer</summary>

| Class                                      | Object                                  |
|--------------------------------------------|-----------------------------------------|
| Blueprint/template for creating objects    | Instance of a class                     |
| Logical entity                             | Physical entity                         |
| No memory allocated for instance variables | Memory allocated when object is created |
| Defines properties and behavior            | Represents actual data                  |

### Example

```java
class Employee {
    String name;
}

// Object creation
Employee emp = new Employee();
emp.name = "John";
```

**Interview Point:**

> A class is a blueprint, whereas an object is a runtime instance of that blueprint.

</details>

---

# 2. What is Constructor?

<details>
<summary>Show Answer</summary>

**Answer:**

A constructor is a special method used to initialize an object when it is created.

### Characteristics

* Same name as class
* No return type (not even void)
* Automatically invoked during object creation

### Example

```java
class Employee {

    Employee() {
        System.out.println("Object Created");
    }
}

public class Test {
    public static void main(String[] args) {
        Employee emp = new Employee(); // Constructor called
    }
}
```

**Output**

```text
Object Created
```

</details>

---

# 3. Default Constructor vs Parameterized Constructor

<details>
<summary>Show Answer</summary>

| Default Constructor                            | Parameterized Constructor               |
|------------------------------------------------|-----------------------------------------|
| No parameters                                  | Accepts parameters                      |
| Initializes object with default values         | Initializes object with provided values |
| Compiler provides one if no constructor exists | Must be explicitly written              |

### Example

```java
class Employee {

    // Default constructor
    Employee() {
        System.out.println("Default Constructor");
    }

    // Parameterized constructor
    Employee(String name) {
        System.out.println("Employee: " + name);
    }
}
```

```java
new Employee();
new Employee("John");
```

**Output**

```text
Default Constructor
Employee: John
```

</details>

---

# 4. Constructor Overloading

<details>
<summary>Show Answer</summary>

**Answer:**

Having multiple constructors with different parameter lists in the same class.

### Example

```java
class Employee {

    Employee() {
        System.out.println("Default");
    }

    Employee(String name) {
        System.out.println("Name: " + name);
    }

    Employee(String name, int age) {
        System.out.println(name + " " + age);
    }
}
```

**Interview Point:**

> Constructor overloading provides multiple ways to create objects.

</details>

---

# 5. Can Constructor Be Private?

<details>
<summary>Show Answer</summary>

**Answer:** ✅ Yes.

A private constructor prevents object creation from outside the class.

### Example

```java
class Singleton {

    private Singleton() {
        System.out.println("Private Constructor");
    }

    static Singleton getInstance() {
        return new Singleton();
    }
}
```

```java
Singleton s = Singleton.getInstance(); // Allowed
// new Singleton(); // Compilation Error
```

### Use Cases

* Singleton Design Pattern
* Utility classes

</details>

---

# 6. What is Copy Constructor?

<details>
<summary>Show Answer</summary>

**Answer:**

Java does not provide a built-in copy constructor like C++, but we can create one manually.

### Example

```java
class Employee {

    String name;

    Employee(String name) {
        this.name = name;
    }

    // Copy Constructor
    Employee(Employee emp) {
        this.name = emp.name;
    }
}
```

```java
Employee e1 = new Employee("John");
Employee e2 = new Employee(e1);

System.out.println(e2.name);
```

**Output**

```text
John
```

**Interview Point:**

> Copy constructor creates a new object by copying values from an existing object.

</details>

---

# 7. Can a Constructor Call Another Constructor?

<details>
<summary>Show Answer</summary>

**Answer:** ✅ Yes.

Using `this()`.

### Example

```java
class Employee {

    Employee() {
        this("John"); // Calls parameterized constructor
        System.out.println("Default Constructor");
    }

    Employee(String name) {
        System.out.println("Name: " + name);
    }
}
```

```java
new Employee();
```

**Output**

```text
Name: John
Default Constructor
```

**Interview Point:**

> `this()` must be the first statement inside a constructor.

</details>

---

# 8. this() vs super()

<details>
<summary>Show Answer</summary>

| this()                                          | super()                                           |
|-------------------------------------------------|---------------------------------------------------|
| Calls current class constructor                 | Calls parent class constructor                    |
| Constructor chaining within same class          | Constructor chaining across inheritance hierarchy |
| Must be first statement                         | Must be first statement                           |
| Cannot be used with super() in same constructor | Cannot be used with this() in same constructor    |

### Example

```java
class Parent {

    Parent() {
        System.out.println("Parent");
    }
}

class Child extends Parent {

    Child() {
        super(); // Calls Parent constructor
        System.out.println("Child");
    }
}
```

**Output**

```text
Parent
Child
```

</details>

---

# 9. What is Object Cloning?

<details>
<summary>Show Answer</summary>

**Answer:**

Object cloning creates a copy of an existing object using the `clone()` method.

### Example

```java
class Employee implements Cloneable {

    String name = "John";

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
```

```java
Employee e1 = new Employee();

Employee e2 = (Employee) e1.clone();

System.out.println(e1 == e2);      // Different objects
System.out.println(e1.name);       // John
System.out.println(e2.name);       // John
```

**Output**

```text
false
John
John
```

### Interview Point

> `clone()` performs a shallow copy by default. For objects containing nested mutable objects, a deep copy must be implemented manually.

### Common Follow-up

**Q: Difference between Shallow Copy and Deep Copy?**

* **Shallow Copy:** Copies references.
* **Deep Copy:** Creates copies of referenced objects as well.

```java
Original Object
      |
      v
 Address Object

Shallow Copy
Both objects point to same Address

Deep Copy
Each object has its own Address
```

</details>

---

# 10. Deep Copy vs Shallow Copy

<details>
<summary>Show Answer</summary>

| Shallow Copy                                  | Deep Copy                                                      |
|-----------------------------------------------|----------------------------------------------------------------|
| Copies primitive values and object references | Copies primitive values and creates new objects for references |
| Nested objects are shared                     | Nested objects are independently copied                        |
| Faster                                        | Slightly slower                                                |
| Default behavior of `clone()`                 | Must be implemented manually                                   |

### Shallow Copy

```java
class Address {
    String city;

    Address(String city) {
        this.city = city;
    }
}

class Employee implements Cloneable {
    String name;
    Address address;

    Employee(String name, Address address) {
        this.name = name;
        this.address = address;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone(); // Shallow Copy
    }
}
```

```java
Employee e1 = new Employee("John", new Address("Bangalore"));
Employee e2 = (Employee) e1.clone();

e2.address.city = "Pune";

System.out.println(e1.address.city); // Pune
```

**Interview Point:**

> In shallow copy, both objects share the same nested object reference.

</details>

---

# 11. How Cloneable Works?

<details>
<summary>Show Answer</summary>

**Answer:**

`Cloneable` is a **marker interface** (empty interface).

```java
public interface Cloneable {
}
```

When `Object.clone()` is called:

* JVM checks whether the class implements `Cloneable`.
* If yes, cloning proceeds.
* If not, `CloneNotSupportedException` is thrown.

### Example

```java
class Employee implements Cloneable {

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
```

**Interview Point:**

> `Cloneable` doesn't provide methods. It simply tells JVM that cloning is allowed.

</details>

---

# 12. Why clone() is Protected?

<details>
<summary>Show Answer</summary>

**Answer:**

`clone()` is declared as protected in `Object` class to prevent arbitrary cloning of every object.

```java
protected native Object clone()
        throws CloneNotSupportedException;
```

To make cloning available outside the class, we typically override it as `public`.

### Example

```java
class Employee implements Cloneable {

    @Override
    public Employee clone() throws CloneNotSupportedException {
        return (Employee) super.clone();
    }
}
```

**Interview Point:**

> Java forces developers to explicitly decide whether cloning should be supported.

</details>

---

# 13. Can Object Be Created Without `new` Keyword?

<details>
<summary>Show Answer</summary>

**Answer:** ✅ Yes.

Several ways exist to create objects without directly using `new`.

### Example using Reflection

```java
Class<Employee> clazz = Employee.class;

Employee emp =
        clazz.getDeclaredConstructor().newInstance();
```

---

### Example using clone()

```java
Employee e2 = (Employee) e1.clone();
```

---

### Example using Deserialization

```java
ObjectInputStream in =
        new ObjectInputStream(new FileInputStream("emp.ser"));

Employee emp = (Employee) in.readObject();
```

**Interview Point:**

> Even when you don't explicitly write `new`, JVM eventually allocates memory for the object.

</details>

---

# 14. Ways to Create Objects in Java

<details>
<summary>Show Answer</summary>

### 1. Using `new` Keyword

```java
Employee emp = new Employee();
```

---

### 2. Using Reflection

```java
Employee emp =
        Employee.class.getDeclaredConstructor()
                      .newInstance();
```

---

### 3. Using clone()

```java
Employee copy = (Employee) emp.clone();
```

---

### 4. Using Deserialization

```java
Employee emp =
        (Employee) objectInputStream.readObject();
```

---

### 5. Using Factory Methods

```java
List<String> list = List.of("A", "B");
```

Internally the factory method creates and returns an object.

---

### Interview Answer

> Common ways are: `new`, Reflection, Cloning, Deserialization, and Factory Methods.

</details>

---

# 15. What Happens Internally When `new` Keyword is Used?

<details>
<summary>Show Answer</summary>

### Example

```java
Employee emp = new Employee();
```

### Internal Steps

#### Step 1: Class Loading

JVM loads the `Employee` class if not already loaded.

```text
Class Loader
      ↓
Employee.class loaded
```

---

#### Step 2: Memory Allocation

Memory is allocated in the Heap.

```text
Heap
 ┌─────────────┐
 │ Employee    │
 │ name = null │
 │ age = 0     │
 └─────────────┘
```

---

#### Step 3: Default Initialization

Fields get default values.

```java
String name = null;
int age = 0;
boolean active = false;
```

---

#### Step 4: Constructor Invocation

Constructor initializes the object.

```java
Employee() {
    name = "John";
}
```

---

#### Step 5: Reference Assignment

Reference variable stores the object's address.

```java
Employee emp = new Employee();
```

```text
Stack                Heap
-----                -----
emp  ─────────────► Employee Object
```

### Interview Answer

> When `new` is used, JVM loads the class (if needed), allocates memory in the heap, initializes fields with default values, invokes the constructor, and finally assigns the object's reference to the variable.

### Follow-up Question

**Q: Where is the object stored?**

**Answer:**

* Object → Heap Memory
* Reference Variable (`emp`) → Stack (local variable case)

**Interview Point:**

> The most important steps are: **Class Loading → Memory Allocation → Default Initialization → Constructor Execution → Reference Assignment**.

</details>

---

# 16. Explain Object Creation Process from Memory Perspective

<details>
<summary>Show Answer</summary>

### Example

```java
Employee emp = new Employee();
```

### What Happens Internally?

#### 1. Class Loading (Method Area / Metaspace)

If `Employee` class is not already loaded, JVM loads its metadata into **Metaspace**.

```text
Metaspace
-----------
Employee Class Metadata
Methods
Fields
Constructor Info
```

---

#### 2. Memory Allocation in Heap

JVM allocates memory for the object in the Heap.

```text
Heap
------------------
Employee Object
name = null
age  = 0
------------------
```

---

#### 3. Default Initialization

All instance variables get default values.

```java
String name = null;
int age = 0;
boolean active = false;
```

---

#### 4. Constructor Execution

Constructor initializes object state.

```java
Employee() {
    name = "John";
}
```

---

#### 5. Reference Stored in Stack

```java
Employee emp = new Employee();
```

```text
Stack                    Heap
-----                    -----
emp  ------------------> Employee Object
```

### Memory View

```text
Metaspace
----------
Employee.class

Stack
----------
emp

Heap
----------
Employee Object
```

### Interview Point

> The object lives in the Heap, the reference variable lives in the Stack (for local variables), and class metadata lives in Metaspace.

</details>

---

# 17. What Happens in JVM When an Object Becomes Unreachable?

<details>
<summary>Show Answer</summary>

### Example

```java
Employee emp = new Employee();

emp = null; // Object becomes unreachable
```

### Step 1: Object Becomes Eligible for GC

```text
Before

emp ------> Employee Object
```

```text
After

emp = null

Employee Object (No Reference)
```

Since no live reference points to the object, it becomes **eligible for Garbage Collection**.

---

### Step 2: Garbage Collector Detects It

GC periodically checks for unreachable objects using reachability analysis.

```text
GC Roots
   |
   |---- No path to Employee Object
```

Object is marked as garbage.

---

### Step 3: Memory Reclaimed

GC frees the heap memory occupied by the object.

```text
Before GC

Heap
---------
Employee Object
---------
```

```text
After GC

Heap
---------
Free Space
---------
```

### Important Interview Point

> Eligible for GC does not mean immediately removed. Garbage collection happens when JVM decides it is necessary.

### Common Follow-up

**Q: How can an object become unreachable?**

```java
obj = null;
obj = new Employee();
method scope ends;
```

</details>

---

# 18. Difference Between Object Reference and Object Itself

<details>
<summary>Show Answer</summary>

| Object Reference                      | Object                       |
|---------------------------------------|------------------------------|
| Variable holding object's address     | Actual data stored in memory |
| Stored in Stack (local variable case) | Stored in Heap               |
| Can be reassigned                     | Created/destroyed by JVM     |
| Does not contain object data          | Contains fields and state    |

### Example

```java
Employee emp = new Employee();
```

### Memory Representation

```text
Stack                    Heap
-----                    -----
emp  ------------------> Employee Object
                          name = "John"
```

Here:

```java
emp
```

is the **reference**

and

```java
new Employee()
```

is the **actual object**

---

### Another Example

```java
Employee e1 = new Employee();
Employee e2 = e1;
```

```text
Stack

e1 --------\
            \
             ---> Employee Object
            /
e2 --------/
```

Both references point to the same object.

---

### Interview Point

> A reference is like a house address, while the object is the actual house. Multiple references can point to the same object, but the object exists only once in memory.

</details>
