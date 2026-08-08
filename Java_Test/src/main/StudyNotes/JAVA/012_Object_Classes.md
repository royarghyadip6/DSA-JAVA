# 1. Why Override `equals()`?

<details>
<summary>Show Answer</summary>

### Default Behavior

`Object.equals()` compares memory addresses (references).

```java
Employee e1 = new Employee(101);
Employee e2 = new Employee(101);

System.out.println(e1.equals(e2));
```

**Output**

```text
false
```

because both are different objects.

---

### Override `equals()`

```java
class Employee {

    private int id;

    Employee(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) return true;

        if (!(obj instanceof Employee)) return false;

        Employee other = (Employee) obj;

        return this.id == other.id;
    }
}
```

```java
Employee e1 = new Employee(101);
Employee e2 = new Employee(101);

System.out.println(e1.equals(e2));
```

**Output**

```text
true
```

### Interview Point

> We override equals() when logical equality is more important than reference equality.

</details>

---

# 2. Why Override `hashCode()`?

<details>
<summary>Show Answer</summary>

### Purpose

Hash-based collections use hashCode() to locate objects efficiently.

Examples:

```java
HashMap
HashSet
Hashtable
ConcurrentHashMap
```

### Example

```java
Employee e1 = new Employee(101);
Employee e2 = new Employee(101);
```

If equals() says both are equal:

```java
e1.equals(e2) == true
```

then both must generate the same hashCode.

### Interview Point

> hashCode() improves lookup performance and must be consistent with equals().

</details>

---

# 3. Contract Between `equals()` and `hashCode()`

<details>
<summary>Show Answer</summary>

### Rule 1

If two objects are equal:

```java
a.equals(b) == true
```

then

```java
a.hashCode() == b.hashCode()
```

must be true.

---

### Rule 2

If two objects have same hashCode:

```java
a.hashCode() == b.hashCode()
```

they may or may not be equal.

---

### Correct Implementation

```java
@Override
public boolean equals(Object obj) {
    // compare id
}

@Override
public int hashCode() {
    return Objects.hash(id);
}
```

### Interview Point

> Equal objects must have equal hashCodes, but equal hashCodes do not guarantee equality.

</details>

---

# 4. What Happens if `equals()` is Overridden but `hashCode()` is Not?

<details>
<summary>Show Answer</summary>

### Example

```java
class Employee {

    int id;

    Employee(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object obj) {
        return this.id == ((Employee)obj).id;
    }
}
```

```java
HashSet<Employee> set =
        new HashSet<>();

set.add(new Employee(101));

System.out.println(
    set.contains(new Employee(101))
);
```

Expected:

```text
true
```

Actual:

```text
false
```

### Why?

HashSet first uses:

```java
hashCode()
```

Different hash buckets are searched.

`equals()` is never reached.

### Interview Point

> Overriding equals() without hashCode() breaks HashMap/HashSet behavior.

</details>

---

# 5. Difference Between `==` and `equals()`

<details>
<summary>Show Answer</summary>

| ==                     | equals()                          |
|------------------------|-----------------------------------|
| Compares references    | Compares content/logical equality |
| Operator               | Method                            |
| Cannot be overridden   | Can be overridden                 |
| Fast memory comparison | Custom comparison logic           |

### Example

```java
String s1 = new String("Java");
String s2 = new String("Java");

System.out.println(s1 == s2);
System.out.println(s1.equals(s2));
```

**Output**

```text
false
true
```

### Interview Point

> `==` checks whether two references point to the same object, while equals() checks logical equality.

</details>

---

# 6. Important Methods in Object Class

<details>
<summary>Show Answer</summary>

Every class implicitly extends:

```java
java.lang.Object
```

### Frequently Used Methods

| Method      | Purpose                        |
|-------------|--------------------------------|
| equals()    | Compare objects                |
| hashCode()  | Hash value                     |
| toString()  | String representation          |
| clone()     | Copy object                    |
| getClass()  | Runtime class info             |
| wait()      | Thread waiting                 |
| notify()    | Wake one thread                |
| notifyAll() | Wake all waiting threads       |
| finalize()  | Cleanup before GC (Deprecated) |

### Interview Point

> Object class is the root class of Java's inheritance hierarchy.

</details>

---

# 7. What is `clone()`?

<details>
<summary>Show Answer</summary>

### Purpose

Creates a copy of an object.

### Example

```java
class Employee implements Cloneable {

    int id = 101;

    @Override
    protected Object clone()
            throws CloneNotSupportedException {

        return super.clone();
    }
}
```

```java
Employee e1 = new Employee();
Employee e2 = (Employee) e1.clone();
```

### Interview Point

> clone() creates a field-by-field copy of an object.

</details>

---

# 8. What is `finalize()`?

<details>
<summary>Show Answer</summary>

### Purpose

Historically used for cleanup before GC.

```java
@Override
protected void finalize() {
    System.out.println("Cleanup");
}
```

### Status

❌ Deprecated since Java 9.

### Problems

* Unpredictable
* Performance overhead
* Resource leaks

### Modern Alternative

```java
try-with-resources
AutoCloseable
```

### Interview Point

> finalize() should be avoided in modern Java.

</details>

---

# 9. What are `wait()`, `notify()`, and `notifyAll()`?

<details>
<summary>Show Answer</summary>

Used for thread communication.

### wait()

Releases monitor lock and waits.

```java
synchronized(lock) {
    lock.wait();
}
```

---

### notify()

Wakes one waiting thread.

```java
synchronized(lock) {
    lock.notify();
}
```

---

### notifyAll()

Wakes all waiting threads.

```java
synchronized(lock) {
    lock.notifyAll();
}
```

### Interview Point

> These methods belong to Object because every object can act as a monitor lock.

</details>

---

# 10. How Does HashMap Use `hashCode()` and `equals()` Internally?

<details>
<summary>Show Answer</summary>

### Put Operation

```java
map.put(key, value);
```

### Step 1

Calculate hashCode.

```java
int hash = key.hashCode();
```

### Step 2

Determine bucket.

```text
hashCode
    ↓
Bucket Index
```

### Step 3

Store entry.

---

### Get Operation

```java
map.get(key);
```

### Internal Flow

```text
hashCode()
      ↓
Bucket
      ↓
equals()
      ↓
Match Found
```

### Example

```java
map.put(new Employee(101), "John");

map.get(new Employee(101));
```

HashMap:

```text
1. hashCode()
2. Locate bucket
3. equals() comparison
4. Return value
```

### Interview Point

> HashMap uses hashCode() to find the bucket and equals() to identify the correct key within that bucket.

</details>

---

# 11. Can Two Unequal Objects Have the Same hashCode()?

<details>
<summary>Show Answer</summary>

### Answer

✅ Yes

This is called a collision.

### Example

```java
String s1 = "FB";
String s2 = "Ea";

System.out.println(
    s1.hashCode()
);

System.out.println(
    s2.hashCode()
);
```

Both produce:

```text
2236
2236
```

But:

```java
s1.equals(s2)
```

returns:

```text
false
```

### Interview Point

> Hash collisions are allowed and handled by HashMap using equals().

</details>

---

# 12. Can Two Equal Objects Have Different hashCodes()?

<details>
<summary>Show Answer</summary>

### Answer

❌ No

If:

```java
a.equals(b)
```

returns:

```text
true
```

then:

```java
a.hashCode() == b.hashCode()
```

must also be true.

Otherwise:

```java
HashMap
HashSet
```

will behave incorrectly.

### Interview Point

> Equal objects with different hashCodes violate the equals-hashCode contract.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Which method should be overridden along with equals()?

<details>
<summary>Show Answer</summary>

**Answer:** `hashCode()`

</details>

---

### Q: Does HashMap call equals() first?

<details>
<summary>Show Answer</summary>

**Answer:** No.

```text
hashCode()
     ↓
Bucket Selection
     ↓
equals()
```

</details>

---

### Q: Why does Object class have wait/notify methods?

<details>
<summary>Show Answer</summary>

**Answer:** Because every object can act as a monitor lock.

</details>

---

### Q: Can hashCode values be negative?

<details>
<summary>Show Answer</summary>

**Answer:** Yes.

```java
int hashCode()
```

returns any integer.

</details>

---

### Q: Root class of Java?

<details>
<summary>Show Answer</summary>

**Answer:**

```java
java.lang.Object
```

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> HashMap relies on both hashCode() and equals(). hashCode() determines the bucket, and equals() identifies the exact object within that bucket. Therefore, whenever equals() is overridden, hashCode() must also be overridden to maintain the contract and ensure correct collection behavior.

</details>
