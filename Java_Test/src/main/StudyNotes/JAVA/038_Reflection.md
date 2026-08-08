# 38. Reflection API

## 38. Reflection API

## Basics

---

# 1. What is Reflection?

<details>
<summary>Show Answer</summary>

**Answer:**

**Reflection** is the ability to **inspect and modify** classes, methods, fields, and constructors **at runtime**—even private members—without knowing them at compile time.

### Simple Idea

```text
Normal code:  you know class name at compile time → Employee emp = new Employee()
Reflection:   discover class/methods at runtime → inspect any class dynamically
```

```java
Class<?> clazz = Employee.class;
System.out.println(clazz.getName());           // Employee
System.out.println(clazz.getDeclaredFields()); // all fields
```

### What You Can Do

```text
✅ Get class metadata (name, methods, fields)
✅ Create objects without new keyword
✅ Invoke methods dynamically
✅ Access/modify private fields (with setAccessible)
✅ Load classes by name string
```

**Interview Point:**

> Reflection = runtime introspection of classes. Inspect and manipulate structure at runtime. Power + performance/security cost.

</details>

---

# 2. Why Reflection used?

<details>
<summary>Show Answer</summary>

**Answer:**

| Use Case | Example |
|----------|---------|
| **Frameworks** | Spring creates beans, Hibernate maps entities |
| **IDEs** | Auto-complete, debugging |
| **Libraries** | Jackson serializes unknown objects |
| **Plugins** | Load classes by name from config |
| **Testing** | Mockito creates mocks |
| **Annotations** | Process @Autowired, @Entity at runtime |

```java
// Framework doesn't know your class at compile time
String className = config.get("handler.class");
Class<?> clazz = Class.forName(className);
Object instance = clazz.getDeclaredConstructor().newInstance();
```

**Interview Point:**

> Reflection powers frameworks (Spring, Hibernate), serialization, testing tools. Trade-off: flexibility vs performance.

</details>

---

# 3. How to get Class object?

<details>
<summary>Show Answer</summary>

**Answer:**

Three ways to get a `Class<?>` object:

```java
// 1. dot class literal — compile time
Class<?> c1 = Employee.class;

// 2. getClass() on instance — runtime
Employee emp = new Employee();
Class<?> c2 = emp.getClass();

// 3. Class.forName() — by string name — dynamic loading
Class<?> c3 = Class.forName("com.app.Employee");
```

| Method | When to Use |
|--------|-------------|
| `.class` | Known at compile time |
| `getClass()` | Have an instance |
| `Class.forName()` | Class name from config/file |

**Interview Point:**

> Three ways: .class, getClass(), Class.forName(). forName loads class dynamically.

</details>

---

## Frequently Asked

---

# 4. Class.forName()?

<details>
<summary>Show Answer</summary>

**Answer:**

`Class.forName(String className)` loads a class by name and returns its `Class` object—also triggers **class initialization** (static blocks).

```java
Class<?> clazz = Class.forName("com.mysql.cj.jdbc.Driver");
// JDBC classic — loads driver, static block registers with DriverManager

Object obj = clazz.getDeclaredConstructor().newInstance();
```

### forName vs loadClass

```text
Class.forName(name)     → loading + linking + initialization
ClassLoader.loadClass() → loading only — no static init yet
```

**Interview Point:**

> Class.forName loads and initializes class. Used in JDBC drivers, dynamic plugins. Throws ClassNotFoundException.

</details>

---

# 5. getMethods()?

<details>
<summary>Show Answer</summary>

**Answer:**

`getMethods()` returns **all public methods** of the class—including **inherited public methods** from parent classes and interfaces.

```java
Method[] methods = Employee.class.getMethods();
for (Method m : methods) {
    System.out.println(m.getName());
}
// includes: getName, toString, equals, hashCode (from Object) + Employee public methods
```

| Method | Returns |
|--------|---------|
| `getMethods()` | All **public** methods (including inherited) |
| `getDeclaredMethods()` | All methods **declared in this class** (any access) |

**Interview Point:**

> getMethods() = public only + inherited. getDeclaredMethods() = all declared in class including private.

</details>

---

# 6. getDeclaredMethods()?

<details>
<summary>Show Answer</summary>

**Answer:**

`getDeclaredMethods()` returns **every method declared in the class**—public, private, protected, static—but **not inherited** methods.

```java
Method[] declared = Employee.class.getDeclaredMethods();
for (Method m : declared) {
    System.out.println(m.getName() + " — " + m.getModifiers());
}
// includes private helper methods — NOT Object.toString unless overridden
```

```java
// Invoke private method
Method privateMethod = clazz.getDeclaredMethod("secret");
privateMethod.setAccessible(true);
privateMethod.invoke(instance);
```

**Interview Point:**

> getDeclaredMethods = all access levels, only this class. Use setAccessible for private invocation.

</details>

---

# 7. getFields()?

<details>
<summary>Show Answer</summary>

**Answer:**

`getFields()` returns **all public fields**—including inherited public fields from parent.

```java
Field[] fields = Employee.class.getFields();
// public fields only — inherited + own

Field[] allFields = Employee.class.getDeclaredFields();
// all fields declared in Employee — private, protected, public
```

| Method | Scope |
|--------|-------|
| `getFields()` | Public fields + inherited public |
| `getDeclaredFields()` | All fields in this class only |

```java
Field salary = clazz.getDeclaredField("salary");
salary.setAccessible(true);
salary.set(emp, 50000);
```

**Interview Point:**

> getFields = public + inherited. getDeclaredFields = all in class. setAccessible for private fields.

</details>

---

# 8. getConstructors()?

<details>
<summary>Show Answer</summary>

**Answer:**

`getConstructors()` returns **public constructors**. `getDeclaredConstructors()` returns **all constructors** including private.

```java
Constructor<?>[] publicCtors = Employee.class.getConstructors();
Constructor<?> privateCtor = Employee.class.getDeclaredConstructor(String.class);
privateCtor.setAccessible(true);
Employee emp = (Employee) privateCtor.newInstance("John");
```

```java
// Create instance without new
Class<?> clazz = Employee.class;
Object obj = clazz.getDeclaredConstructor().newInstance();
```

**Interview Point:**

> getConstructors = public only. getDeclaredConstructors = all. newInstance() creates object via reflection.

</details>

---

## Advanced

---

# 9. Can private methods be invoked?

<details>
<summary>Show Answer</summary>

**Answer:**

**Yes** — with reflection and `setAccessible(true)`, you can invoke private methods (breaks encapsulation).

```java
class Secret {
    private String hidden() {
        return "secret data";
    }
}

Secret s = new Secret();
Method m = Secret.class.getDeclaredMethod("hidden");
m.setAccessible(true); // bypass Java access control
String result = (String) m.invoke(s); // "secret data"
```

### Warning

```text
❌ Breaks encapsulation
❌ May fail with strong module system (Java 9+)
❌ SecurityManager can block
✅ Used in frameworks and tests — not normal business code
```

**Interview Point:**

> Yes via getDeclaredMethod + setAccessible(true) + invoke. Framework/testing use only.

</details>

---

# 10. setAccessible(true)?

<details>
<summary>Show Answer</summary>

**Answer:**

`setAccessible(true)` tells JVM to **skip access checks**—allows reading/writing private fields and calling private methods via reflection.

```java
Field field = clazz.getDeclaredField("password");
field.setAccessible(true);  // disable access check
field.set(user, "newPassword");
```

### On Method, Field, Constructor

```java
method.setAccessible(true);
field.setAccessible(true);
constructor.setAccessible(true);
```

### Java 9+ Modules

```text
Strong encapsulation may block setAccessible on JDK internal classes
--add-opens for frameworks that need deep access
```

**Interview Point:**

> setAccessible(true) bypasses private/protected checks. Required for private reflection access. Module system may restrict.

</details>

---

# 11. Can final field be modified?

<details>
<summary>Show Answer</summary>

**Answer:**

**Technically yes** via reflection—but **strongly discouraged** and may fail on modern JVMs for true finals.

```java
class Immutable {
    private final String name = "original";
}

Field f = Immutable.class.getDeclaredField("name");
f.setAccessible(true);

// Remove final modifier (older technique — fragile)
Field modifiers = Field.class.getDeclaredField("modifiers");
modifiers.setAccessible(true);
modifiers.setInt(f, f.getModifiers() & ~Modifier.FINAL);

f.set(instance, "hacked"); // may work on some JVMs
```

### Reality

```text
JVM may optimize assuming final never changes
String intern pool relies on String immutability
Java 12+ harder to strip FINAL modifier
Don't do this in production — breaks guarantees
```

**Interview Point:**

> Reflection can break final in theory — fragile, JVM-dependent. String immutability can be broken but shouldn't. Know for interview, never do in code.

</details>

---

# 12. Reflection performance impact?

<details>
<summary>Show Answer</summary>

**Answer:**

Reflection is **slower** than direct calls—JVM cannot optimize as well.

| Factor | Impact |
|--------|--------|
| Method lookup | Slower than direct call |
| invoke() overhead | Boxing, access checks |
| No inlining | JIT can't optimize hot reflective paths |
| setAccessible | One-time cost after first call |

```text
Direct call:     ~1 ns
Reflection call: ~10-100x slower (varies)
```

### Mitigations

```text
✅ Cache Method/Field objects — don't look up every time
✅ setAccessible once
✅ Use direct code in hot paths
✅ Frameworks cache reflection metadata (Spring caches bean definitions)
```

**Interview Point:**

> Reflection is slow — method lookup + invoke overhead. Frameworks cache metadata. Avoid in performance-critical loops.

</details>

---

## Framework Questions

---

# 13. How Spring uses Reflection?

<details>
<summary>Show Answer</summary>

**Answer:**

Spring uses reflection extensively at startup and runtime:

| Feature | Reflection Use |
|---------|----------------|
| **Bean creation** | `getDeclaredConstructor().newInstance()` |
| **@Autowired** | Find fields/setters, inject dependencies |
| **@PostConstruct** | Find and invoke lifecycle methods |
| **@RequestMapping** | Map HTTP paths to controller methods |
| **AOP proxies** | Invoke methods on target objects |
| **@Value** | Set field values from properties |

```text
Startup:
  1. Component scan finds @Service, @Controller classes
  2. Reflection reads class metadata, annotations
  3. Creates instances, wires dependencies into fields
  4. Caches bean definitions — not reflect every request
```

**Interview Point:**

> Spring reflects at startup to create beans, read annotations, inject fields. Metadata cached — not per-request reflection.

</details>

---

# 14. How Hibernate uses Reflection?

<details>
<summary>Show Answer</summary>

**Answer:**

Hibernate uses reflection to map Java objects to database tables without hardcoded mappings for every entity.

| Use | How |
|-----|-----|
| **Entity mapping** | Read `@Entity`, `@Column`, `@Id` on fields |
| **Property access** | get/set field values for INSERT/UPDATE |
| **Lazy loading** | Proxy classes override getters via reflection |
| **Constructor** | Create entity instances from query results |

```java
@Entity
class Employee {
    @Id
    private Long id;
    @Column(name = "emp_name")
    private String name;
}

// Hibernate reflects: which fields map to which columns
// Reads/writes field values via reflection or bytecode enhancement
```

**Interview Point:**

> Hibernate reflects entity fields/annotations for ORM mapping. JPA annotations + reflection = no manual SQL per field.

</details>

---

# 15. How Dependency Injection uses Reflection?

<details>
<summary>Show Answer</summary>

**Answer:**

DI containers use reflection to **create objects** and **inject dependencies** into fields/constructors without you calling `new` or setters manually.

```text
Without DI:
  UserService service = new UserService(new UserRepository(), new EmailClient());

With Spring DI + Reflection:
  @Service class UserService {
      @Autowired UserRepository repo;  // Spring reflects, finds field, injects bean
  }
```

### Steps

```text
1. Scan for @Component / @Service classes
2. Reflection: find @Autowired fields or constructor params
3. Resolve bean types from container
4. setAccessible + field.set() or constructor.newInstance(args)
5. Return fully wired object
```

**Interview Point:**

> DI = container creates objects and injects deps via reflection + annotations. Constructor injection preferred over field injection.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Reflection vs normal instantiation?

<details>
<summary>Show Answer</summary>

**Answer:**

`new Employee()` — compile-time known, fast, type-safe. Reflection `newInstance()` — runtime dynamic, slower, flexible for frameworks.

</details>

---

### Q: getMethod vs getDeclaredMethod?

<details>
<summary>Show Answer</summary>

**Answer:**

`getMethod(name, params)` — public method, includes inherited. `getDeclaredMethod(name, params)` — any access level, only declared in class.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Reflection = runtime class inspection. Class.forName, getDeclaredMethods/Fields, setAccessible for private. Slow — frameworks cache metadata. Powers Spring DI, Hibernate ORM, testing mocks.

</details>
