# 40. Immutable Class

## 40. Immutable Class

## Most Asked

---

# 1. What is Immutable Class?

<details>
<summary>Show Answer</summary>

**Answer:**

An **immutable class** is one whose **state cannot change** after the object is created—no setters, final fields, no way to modify internal data.

### Simple Idea

```text
Mutable:   StringBuilder sb = new StringBuilder("hi");
           sb.append("!");  → "hi!"  (changed)

Immutable: String s = "hi";
           s = s + "!";     → new String "hi!" — original "hi" unchanged
```

```java
public final class Money {
    private final String currency;
    private final double amount;

    public Money(String currency, double amount) {
        this.currency = currency;
        this.amount = amount;
    }

    public String getCurrency() { return currency; }
    public double getAmount() { return amount; }
    // no setters — cannot change after creation
}
```

**Interview Point:**

> Immutable = state fixed at creation. No setters. Changes create new objects (like String).

</details>

---

# 2. Why String is immutable?

<details>
<summary>Show Answer</summary>

**Answer:**

`String` is immutable for **security, performance, thread safety, and String pool** reasons.

| Reason | Explanation |
|--------|-------------|
| **String pool** | Same literal shares one object — safe if immutable |
| **Security** | Class names, file paths can't be changed after check |
| **Thread safety** | Shared freely across threads |
| **HashMap keys** | hashCode never changes |
| **Performance** | JVM can optimize, intern, cache hashCode |

```java
String a = "Java";
String b = "Java";
// a == b → true (same pool object) — only safe if immutable

String path = "/etc/config";
// if mutable, attacker could change path after security check
```

**Interview Point:**

> String immutable for pool sharing, security, thread safety, stable hashCode for HashMap keys.

</details>

---

# 3. Benefits of immutability?

<details>
<summary>Show Answer</summary>

**Answer:**

| Benefit | Why |
|---------|-----|
| **Thread-safe** | No sync needed — state never changes |
| **Safe HashMap keys** | hashCode stable forever |
| **Defensive copies** | Pass to other code safely |
| **Easier reasoning** | State won't change unexpectedly |
| **Cache friendly** | Safe to cache and share |
| **Fail-fast validation** | Validate once in constructor |

```java
// Safe to share across threads
private static final List<String> ALLOWED = List.of("GET", "POST");

// Safe as cache key
Map<Money, Rate> rates = new HashMap<>();
rates.put(new Money("USD", 1.0), rate);
```

**Interview Point:**

> Immutable = thread-safe, good keys, safe sharing, simpler debugging. Prefer for value objects (Money, Email, Date).

</details>

---

# 4. How to create immutable class?

<details>
<summary>Show Answer</summary>

**Answer:**

Follow the **5 rules** for a proper immutable class:

```java
public final class ImmutableEmployee {
    private final String name;
    private final int age;
    private final List<String> skills;

    public ImmutableEmployee(String name, int age, List<String> skills) {
        this.name = name;
        this.age = age;
        this.skills = List.copyOf(skills); // defensive copy on input
    }

    public String getName() { return name; }
    public int getAge() { return age; }
    public List<String> getSkills() {
        return List.copyOf(skills); // defensive copy on output
    }
}
```

```text
1. final class
2. final fields
3. no setters
4. defensive copy mutable inputs (List, Date)
5. defensive copy mutable outputs from getters
```

**Interview Point:**

> final class + final fields + no setters + defensive copy for mutable fields. Use List.copyOf / new ArrayList.

</details>

---

## Rules

---

# 5. Make class final?

<details>
<summary>Show Answer</summary>

**Answer:**

Class must be **`final`** so nobody can subclass it and add mutable state or override methods to break immutability.

```java
// ❌ Without final — subclass can break immutability
class MutableBreak extends ImmutableEmployee {
    void hack() { /* add mutable field */ }
}

// ✅ final class — cannot extend
public final class ImmutableEmployee { }
```

**Interview Point:**

> final class prevents subclass attacks on immutability. Required rule for immutable design.

</details>

---

# 6. Make fields private final?

<details>
<summary>Show Answer</summary>

**Answer:**

All instance fields should be **`private final`**—private hides them, final prevents reassignment after constructor.

```java
private final String name;   // ✅
private String name;         // ❌ can be reassigned
public final String name;    // ❌ external code could reassign if not final in practice — still use private
```

```java
private final List<String> tags; // final = reference can't change
// but List content could still change unless defensive copy!
tags.add("x"); // ❌ if same list reference leaked
```

**Interview Point:**

> private final fields — reference immutable. For mutable objects (List), also defensive copy.

</details>

---

# 7. No setters?

<details>
<summary>Show Answer</summary>

**Answer:**

**No setter methods**—once constructed, state cannot be modified through API.

```java
// ❌ Breaks immutability
public void setName(String name) { this.name = name; }

// ✅ Only getters
public String getName() { return name; }

// ✅ "Change" = new object
public ImmutableEmployee withName(String newName) {
    return new ImmutableEmployee(newName, this.age, this.skills);
}
```

Modern pattern: `withX()` methods return **new instance** with one field changed (like records).

**Interview Point:**

> No setters. For changes, return new immutable copy (withName, withAge pattern).

</details>

---

# 8. Defensive copying?

<details>
<summary>Show Answer</summary>

**Answer:**

**Defensive copy** = clone mutable objects on **input** (constructor) and **output** (getters) so external code cannot modify internal state.

```java
public ImmutablePerson(List<String> hobbies) {
    this.hobbies = new ArrayList<>(hobbies); // copy on input
}

public List<String> getHobbies() {
    return new ArrayList<>(hobbies); // copy on output
}

// Java 10+
this.hobbies = List.copyOf(hobbies);   // immutable copy on input
return List.copyOf(hobbies);           // immutable list on output
```

### Without Defensive Copy — Broken

```java
List<String> list = new ArrayList<>();
list.add("reading");
ImmutablePerson p = new ImmutablePerson(list);
list.add("hacking"); // mutates internal list! ❌
```

**Interview Point:**

> Defensive copy on constructor and getter for any mutable field (List, Date, array). Use List.copyOf in modern Java.

</details>

---

## Advanced

---

# 9. Why immutable objects are thread-safe?

<details>
<summary>Show Answer</summary>

**Answer:**

Immutable objects are **automatically thread-safe** because their state **never changes**—no thread can corrupt what no thread can modify.

```text
Thread A reads getName() → "John"
Thread B reads getName() → "John"
No setter exists → no race condition on state
```

No need for:
```text
synchronized, volatile, locks, Atomic*
```

**Interview Point:**

> Immutable = inherently thread-safe. No shared mutable state = no synchronization needed.

</details>

---

# 10. Why immutable objects are used as HashMap keys?

<details>
<summary>Show Answer</summary>

**Answer:**

`HashMap` uses **hashCode** and **equals** to find keys. If key changes after insert, **hashCode changes** → key lost in map → cannot retrieve value.

```java
// ❌ Mutable key — broken
Map<List<String>, String> map = new HashMap<>();
List<String> key = new ArrayList<>();
key.add("a");
map.put(key, "value");
key.add("b"); // hashCode changed!
map.get(key); // null — lost! ❌

// ✅ Immutable key
Map<String, String> map = new HashMap<>();
map.put("user-123", "data");
// String key never changes — always findable
```

**Interview Point:**

> HashMap keys must have stable hashCode. Immutable keys never change → safe. String, Integer, LocalDate are ideal keys.

</details>

---

# 11. Can immutability be broken through Reflection?

<details>
<summary>Show Answer</summary>

**Answer:**

**Yes** — reflection with `setAccessible(true)` can modify `final` fields in theory—breaks immutability guarantee.

```java
Field f = String.class.getDeclaredField("value");
f.setAccessible(true);
// strip FINAL modifier (fragile, JVM-specific)
f.set(stringInstance, new char[]{'h','a','c','k'});
```

### Reality

```text
Harder on modern JVMs (Java 12+)
Module system restricts access
String still relies on immutability — don't break in production
```

### Defense

```text
Don't expose internal mutable state
Use List.copyOf for collections
Avoid reflection on your immutable classes in production code
```

**Interview Point:**

> Reflection can break final fields theoretically. Know for interview. Real immutability = design + no leaked mutable refs.

</details>

---

## Scenario

---

# 12. Design your own immutable Employee class.

<details>
<summary>Show Answer</summary>

**Answer:**

```java
import java.util.ArrayList;
import java.util.List;

public final class ImmutableEmployee {
    private final Long id;
    private final String name;
    private final String department;
    private final List<String> skills;
    private final Address address; // immutable Address class too

    public ImmutableEmployee(Long id, String name, String department,
                             List<String> skills, Address address) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.skills = List.copyOf(skills);
        this.address = address; // Address must be immutable too
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDepartment() { return department; }
    public List<String> getSkills() { return List.copyOf(skills); }
    public Address getAddress() { return address; }

    // "Update" = new instance
    public ImmutableEmployee withDepartment(String newDept) {
        return new ImmutableEmployee(id, name, newDept, skills, address);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImmutableEmployee)) return false;
        ImmutableEmployee that = (ImmutableEmployee) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}

public final class Address {
    private final String city;
    private final String country;

    public Address(String city, String country) {
        this.city = city;
        this.country = country;
    }

    public String getCity() { return city; }
    public String getCountry() { return country; }
}
```

### Checklist

```text
✅ final class
✅ final fields
✅ no setters
✅ List.copyOf defensive copy
✅ nested Address also immutable
✅ withDepartment for "updates"
✅ equals/hashCode for use as key
```

**Interview Point:**

> Full immutable Employee: final class, final fields, defensive copy, immutable nested objects, withX for changes, equals/hashCode.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Java record vs immutable class?

<details>
<summary>Show Answer</summary>

**Answer:**

Java 16+ `record` is shorthand immutable data carrier — final class, final fields, constructor, getters, equals, hashCode auto-generated.

```java
public record Employee(Long id, String name) { }
// immutable by default
```

</details>

---

### Q: Immutable vs unmodifiable?

<details>
<summary>Show Answer</summary>

**Answer:**

**Immutable** = object state never changes (String). **Unmodifiable** = view that can't be modified via that reference, but backing collection may still change (Collections.unmodifiableList).

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Immutable = final class, final fields, no setters, defensive copy. Thread-safe, great HashMap keys. String immutable for pool and security. Reflection can break final — design without leaked mutable refs. Use records in modern Java.

</details>
