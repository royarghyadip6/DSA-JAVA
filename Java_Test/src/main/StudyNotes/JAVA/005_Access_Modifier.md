# 1. Difference among `public`, `private`, `protected`, and `default`

| Access Modifier             | Same Class | Same Package | Subclass (Different Package) | Other Classes (Different Package) |
|-----------------------------|------------|--------------|------------------------------|-----------------------------------|
| `private`                   | ✅          | ❌            | ❌                            | ❌                                 |
| `default` (package-private) | ✅          | ✅            | ❌                            | ❌                                 |
| `protected`                 | ✅          | ✅            | ✅                            | ❌                                 |
| `public`                    | ✅          | ✅            | ✅                            | ✅                                 |

### Example

```java
public class Employee {

    public int publicVar;

    protected int protectedVar;

    int defaultVar;      // package-private

    private int privateVar;
}
```

**Interview Shortcut**

```text
private   < default < protected < public
```

---

# 2. Can a Top-Level Class be Private?

**Answer:** ❌ No

Only **public** and **default (package-private)** are allowed for top-level classes.

### Invalid

```java
private class Employee {
}
```

**Compilation Error**

### Valid

```java
public class Employee {
}
```

or

```java
class Employee {
}
```

---

# 3. Can a Top-Level Class be Protected?

**Answer:** ❌ No

`protected` is applicable only to:

* Methods
* Variables
* Inner classes

### Invalid

```java
protected class Employee {
}
```

**Compilation Error**

### Why?

Because `protected` only makes sense in the context of inheritance, and top-level classes cannot participate in inheritance visibility rules.

---

# 4. Access Levels in Same Package

Suppose:

```text
com.company
    Employee
    Manager
```

### Employee

```java
class Employee {

    public int a = 1;
    protected int b = 2;
    int c = 3;
    private int d = 4;
}
```

### Manager

```java
Employee e = new Employee();

System.out.println(e.a); // ✅
System.out.println(e.b); // ✅
System.out.println(e.c); // ✅
// System.out.println(e.d); ❌
```

### Interview Point

> Inside the same package, everything is accessible except `private`.

---

# 5. Access Levels Across Packages

```text
com.company.model
    Employee

com.company.service
    UserService
```

### Employee

```java
public class Employee {

    public int a = 1;
    protected int b = 2;
    int c = 3;
    private int d = 4;
}
```

### UserService (No Inheritance)

```java
Employee e = new Employee();

System.out.println(e.a); // ✅

// e.b ❌
// e.c ❌
// e.d ❌
```

### Interview Point

> Across packages, only `public` members are accessible unless inheritance is involved.

---

# 6. Difference Between `protected` and `default`

| Feature                          | protected | default |
|----------------------------------|-----------|---------|
| Same Class                       | ✅         | ✅       |
| Same Package                     | ✅         | ✅       |
| Child Class in Different Package | ✅         | ❌       |
| Other Package Classes            | ❌         | ❌       |

### Example

```java
package parent;

public class Parent {

    protected void show() {
        System.out.println("Protected Method");
    }
}
```

```java
package child;

public class Child extends Parent {

    void test() {
        show(); // ✅ Allowed
    }
}
```

### Interview Point

> The only difference is that `protected` is accessible in subclasses outside the package, whereas `default` is not.

---

# 7. Real Project Use of `protected`

### Common Example: Spring Template Method Pattern

```java
public abstract class BaseService {

    protected void validate() {
        System.out.println("Common Validation");
    }
}
```

```java
public class EmployeeService extends BaseService {

    public void save() {
        validate(); // Accessible to child class
    }
}
```

### Why `protected`?

* Hide implementation from outside world.
* Allow reuse in subclasses.

### Interview Answer

> In real projects, `protected` is commonly used in base classes where common functionality should be reusable by subclasses but not exposed publicly.

---

# 8. Can Constructor be Private?

**Answer:** ✅ Yes

### Example

```java
class Singleton {

    private Singleton() {
    }

    public static Singleton getInstance() {
        return new Singleton();
    }
}
```

```java
Singleton s = Singleton.getInstance();
```

### Use Cases

* Singleton Pattern
* Utility Classes
* Factory Design Pattern

---

# 9. Explain Encapsulation Using Access Modifiers

**Answer:**

Encapsulation means hiding data and exposing controlled access through methods.

### Example

```java
class Employee {

    private double salary; // Hidden

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {

        if (salary > 0) {
            this.salary = salary;
        }
    }
}
```

### Why Use It?

* Data security
* Validation
* Controlled access
* Maintainability

### Interview Point

> `private` variables + public getters/setters are the most common implementation of encapsulation.

---

# 10. What Happens if Access Level is Reduced While Overriding?

**Answer:**

You cannot reduce the visibility of an overridden method.

### Parent

```java
class Parent {

    public void show() {
        System.out.println("Parent");
    }
}
```

### Child

```java
class Child extends Parent {

    @Override
    private void show() {  // ❌ Compilation Error
        System.out.println("Child");
    }
}
```

### Why?

The child method must be at least as accessible as the parent method.

### Valid Cases

```java
class Parent {

    protected void show() {
    }
}
```

```java
class Child extends Parent {

    @Override
    public void show() {  // ✅ Increased visibility
    }
}
```

### Allowed Visibility Changes

| Parent    | Child                        |
|-----------|------------------------------|
| public    | public                       |
| protected | protected / public           |
| default   | default / protected / public |
| private   | Not overridden               |

### Interview Point

> Visibility can be increased during overriding, but it cannot be reduced because it would violate the parent class contract.
