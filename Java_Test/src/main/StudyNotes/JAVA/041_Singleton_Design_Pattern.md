# 41. Singleton Design Pattern

## 41. Singleton Design Pattern

## Most Important Pattern

---

# 1. What is Singleton?

<details>
<summary>Show Answer</summary>

**Answer:**

**Singleton** is a design pattern that ensures a class has **only one instance** in the entire application—and provides a **global access point** to that instance.

### Simple Idea

```text
One president for a country
One database connection pool manager
One configuration loader
```

```java
Singleton instance1 = Singleton.getInstance();
Singleton instance2 = Singleton.getInstance();
instance1 == instance2; // true — same object
```

### Key Rules

```text
✅ Private constructor — nobody can create with new
✅ Static method getInstance() — controlled access
✅ One static instance — shared globally
```

**Interview Point:**

> Singleton = one instance only + global access via getInstance(). Private constructor prevents external instantiation.

</details>

---

# 2. Why Singleton used?

<details>
<summary>Show Answer</summary>

**Answer:**

| Use Case | Why One Instance |
|----------|------------------|
| **Configuration** | Single config loaded once |
| **Connection pool** | One pool manages all DB connections |
| **Logger** | Central logging configuration |
| **Cache manager** | Shared cache across app |
| **Thread pool** | One executor for whole app |
| **Spring @Service** | Default scope is singleton |

```text
Benefits:
  Controlled access to shared resource
  Saves memory — no duplicate heavy objects
  Consistent state — everyone uses same instance
```

### Spring Note

```java
@Service // Spring creates ONE bean by default
public class UserService { }
// You rarely write manual Singleton in Spring apps
```

**Interview Point:**

> Use when exactly one shared instance needed — config, pools, caches. Spring singleton scope replaces manual pattern in most apps.

</details>

---

# 3. How to create Singleton?

<details>
<summary>Show Answer</summary>

**Answer:**

Basic structure — **private constructor** + **static instance** + **public getInstance()**.

```java
public class DatabaseConfig {

    private static DatabaseConfig instance;

    private DatabaseConfig() {
        // private — prevent new DatabaseConfig()
    }

    public static DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    public String getUrl() {
        return "jdbc:oracle:thin:@localhost:1521/db";
    }
}
```

**Interview Point:**

> Private ctor + static field + getInstance(). Many implementations — eager, lazy, thread-safe, enum.

</details>

---

## Different Implementations

---

# 4. Eager Initialization

<details>
<summary>Show Answer</summary>

**Answer:**

Instance created **when class is loaded** — simple and thread-safe by JVM class initialization.

```java
public class EagerSingleton {
    private static final EagerSingleton INSTANCE = new EagerSingleton();

    private EagerSingleton() {}

    public static EagerSingleton getInstance() {
        return INSTANCE;
    }
}
```

| Pros | Cons |
|------|------|
| Thread-safe (JVM) | Created even if never used |
| Simple | Wastes memory if unused |
| Fast getInstance() | No lazy loading |

**Interview Point:**

> Eager = static final instance at class load. Thread-safe, simple. Use when instance always needed.

</details>

---

# 5. Lazy Initialization

<details>
<summary>Show Answer</summary>

**Answer:**

Instance created **only when getInstance() is first called** — not at class load time.

```java
public class LazySingleton {
    private static LazySingleton instance;

    private LazySingleton() {}

    public static LazySingleton getInstance() {
        if (instance == null) {
            instance = new LazySingleton(); // created on first call
        }
        return instance;
    }
}
```

| Pros | Cons |
|------|------|
| Created only when needed | **NOT thread-safe** |
| Saves memory if unused | Multiple threads can create multiple instances |

**Interview Point:**

> Lazy = create on first getInstance(). Simple but broken under multithreading — needs sync or holder pattern.

</details>

---

# 6. Thread Safe Singleton

<details>
<summary>Show Answer</summary>

**Answer:**

Add **synchronization** on getInstance() so only one thread creates the instance.

```java
public class ThreadSafeSingleton {
    private static ThreadSafeSingleton instance;

    private ThreadSafeSingleton() {}

    public static synchronized ThreadSafeSingleton getInstance() {
        if (instance == null) {
            instance = new ThreadSafeSingleton();
        }
        return instance;
    }
}
```

| Pros | Cons |
|------|------|
| Thread-safe | Lock on **every** getInstance() call — slow |
| Lazy loading | Contention under high traffic |

**Interview Point:**

> synchronized getInstance() = safe but slow. Lock acquired every call even after instance exists.

</details>

---

# 7. Double Checked Locking

<details>
<summary>Show Answer</summary>

**Answer:**

Check instance **twice** — avoid locking after instance exists. **`volatile`** required on instance field.

```java
public class DCLSingleton {
    private static volatile DCLSingleton instance;

    private DCLSingleton() {}

    public static DCLSingleton getInstance() {
        if (instance == null) {                    // first check — no lock
            synchronized (DCLSingleton.class) {
                if (instance == null) {            // second check — inside lock
                    instance = new DCLSingleton();
                }
            }
        }
        return instance;
    }
}
```

### Why volatile?

```text
Without volatile: another thread may see partially constructed object
volatile ensures visibility and prevents instruction reordering
```

**Interview Point:**

> DCL = two null checks + synchronized block + volatile. Lazy and thread-safe. Know why volatile is mandatory.

</details>

---

# 8. Bill Pugh Singleton

<details>
<summary>Show Answer</summary>

**Answer:**

Uses **inner static holder class** — JVM loads holder only when getInstance() called. Thread-safe without explicit synchronization.

```java
public class BillPughSingleton {

    private BillPughSingleton() {}

    private static class SingletonHolder {
        private static final BillPughSingleton INSTANCE = new BillPughSingleton();
    }

    public static BillPughSingleton getInstance() {
        return SingletonHolder.INSTANCE;
    }
}
```

### Why It Works

```text
JVM guarantees class initialization is thread-safe
SingletonHolder loads only when getInstance() first called
Lazy + thread-safe + no synchronized keyword
```

**Interview Point:**

> Bill Pugh = static inner holder class. Lazy, thread-safe, no sync. Preferred class-based singleton.

</details>

---

# 9. Enum Singleton

<details>
<summary>Show Answer</summary>

**Answer:**

**Best approach** — Joshua Bloch recommends enum singleton. JVM guarantees one instance, serialization-safe, reflection-safe.

```java
public enum AppConfig {
    INSTANCE;

    private String appName = "OrderService";

    public String getAppName() {
        return appName;
    }

    public void setAppName(String name) {
        this.appName = name;
    }
}

// Usage
AppConfig.INSTANCE.getAppName();
```

| Advantage | Detail |
|-----------|--------|
| Thread-safe | JVM enum guarantee |
| Serialization | No duplicate on deserialize |
| Reflection | Cannot create second instance via reflection |
| Simple | Minimal code |

**Interview Point:**

> Enum singleton = best practice. One line INSTANCE. Safe against reflection and serialization attacks.

</details>

---

## Advanced

---

# 10. Which Singleton is best?

<details>
<summary>Show Answer</summary>

**Answer:**

| Scenario | Best Choice |
|----------|-------------|
| **General Java** | **Enum singleton** |
| **Class-based (no enum)** | **Bill Pugh holder** |
| **Spring app** | `@Service` / `@Component` — don't write manual singleton |
| **Legacy / interview** | Know DCL with volatile |

```text
Ranking:
  1. Enum singleton        — safest, simplest
  2. Bill Pugh holder      — lazy + thread-safe
  3. Eager static final    — when always needed
  4. DCL + volatile        — know for interviews
  5. synchronized lazy     — simple but slow
  6. Lazy (no sync)        — broken — never use
```

**Interview Point:**

> Enum best. Bill Pugh for class-based. Spring bean for apps. Never unsynchronized lazy.

</details>

---

# 11. Reflection attack on Singleton?

<details>
<summary>Show Answer</summary>

**Answer:**

Reflection can call **private constructor** and create a **second instance** — breaks singleton.

```java
Singleton original = Singleton.getInstance();

Constructor<Singleton> ctor = Singleton.class.getDeclaredConstructor();
ctor.setAccessible(true);
Singleton hacked = ctor.newInstance(); // second instance! ❌

original == hacked; // false — singleton broken
```

### Prevention

```java
// In private constructor — throw if instance already exists
private Singleton() {
    if (Singleton.instance != null) {
        throw new IllegalStateException("Singleton already created");
    }
}

// Or use enum — reflection cannot create second enum constant
```

**Interview Point:**

> Reflection bypasses private constructor. Prevent with check in constructor or use enum singleton.

</details>

---

# 12. Serialization attack on Singleton?

<details>
<summary>Show Answer</summary>

**Answer:**

When singleton is deserialized, Java creates a **new object** — two instances exist.

```java
Singleton s1 = Singleton.getInstance();
// serialize s1 to file
// deserialize → new object s2
s1 == s2; // false ❌
```

### Fix — readResolve()

```java
public class Singleton implements Serializable {
    private static final Singleton INSTANCE = new Singleton();

    private Singleton() {}

    public static Singleton getInstance() { return INSTANCE; }

    protected Object readResolve() {
        return INSTANCE; // always return same instance on deserialize
    }
}
```

Enum singleton is automatically safe — JVM handles it.

**Interview Point:**

> Deserialization creates duplicate. Fix with readResolve() returning INSTANCE. Enum is safe by default.

</details>

---

# 13. How to prevent Singleton breaking?

<details>
<summary>Show Answer</summary>

**Answer:**

| Attack | Prevention |
|--------|------------|
| **Multiple threads** | Bill Pugh, enum, DCL+volatile, synchronized |
| **Reflection** | Throw in constructor if instance exists; use **enum** |
| **Serialization** | `readResolve()` returns INSTANCE; use **enum** |
| **Clone** | Override `clone()` throw exception |

```java
// Full protection (class-based)
public class SafeSingleton implements Serializable {
    private static final SafeSingleton INSTANCE = new SafeSingleton();

    private SafeSingleton() {
        if (INSTANCE != null) {
            throw new IllegalStateException("Use getInstance()");
        }
    }

    public static SafeSingleton getInstance() { return INSTANCE; }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    protected Object readResolve() { return INSTANCE; }
}

// Best: just use enum
public enum SafeEnumSingleton { INSTANCE; }
```

**Interview Point:**

> Enum = best defense (reflection + serialization). Class-based: guard constructor + readResolve() + no clone.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Singleton in Spring — need manual pattern?

<details>
<summary>Show Answer</summary>

**Answer:**

**No** — `@Service`, `@Component` beans are **singleton scope by default**. Spring container manages one instance. Manual singleton pattern rarely needed.

</details>

---

### Q: Singleton vs static class?

<details>
<summary>Show Answer</summary>

**Answer:**

Static class = all static methods, cannot inherit/implement interfaces, lazy loading harder. Singleton = real object, can implement interfaces, polymorphism, lazy init patterns.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Singleton = one instance, private ctor, getInstance(). **Enum best**. Bill Pugh for class-based. DCL needs volatile. Guard against reflection, serialization, clone. Spring @Service replaces manual singleton.

</details>
