# 1. Why is String Immutable?

**Answer:**

Once a String object is created, its value cannot be changed.

### Example

```java id="8j4c3u"
String s = "Java";

s.concat(" 8");

System.out.println(s);
```

**Output**

```text id="m3l9qn"
Java
```

`concat()` creates a new String object instead of modifying the existing one.

### Why Java Made String Immutable?

* Security (URLs, DB connections, file paths)
* Thread Safety
* String Pool optimization
* Cached `hashCode()` performance

### Interview Point

> If String were mutable, changing its value would break String Pool sharing and HashMap key behavior.

---

# 2. Benefits of Immutability

### Advantages

#### 1. Thread Safe

Multiple threads can safely share the same String object.

```java id="sq0n9e"
String name = "Java";
```

No synchronization required.

---

#### 2. String Pool Optimization

Many references can share the same object.

```java id="2p72ys"
String s1 = "Java";
String s2 = "Java";
```

Both point to the same pooled object.

---

#### 3. Security

Important values cannot be modified accidentally.

```java id="d5wlxy"
String url = "jdbc:mysql://localhost:3306/test";
```

---

#### 4. Efficient Hashing

`hashCode()` can be cached because value never changes.

### Interview Point

> Thread safety, security, String Pool, and hashCode caching are the major reasons for String immutability.

---

# 3. Difference Between String, StringBuilder, and StringBuffer

| Feature         | String                 | StringBuilder                       | StringBuffer                       |
|-----------------|------------------------|-------------------------------------|------------------------------------|
| Mutable         | ❌ No                   | ✅ Yes                               | ✅ Yes                              |
| Thread Safe     | ✅ Yes                  | ❌ No                                | ✅ Yes                              |
| Performance     | Slow for modifications | Fastest                             | Slower than StringBuilder          |
| Synchronization | Not needed             | No                                  | Synchronized                       |
| Use Case        | Read-only text         | Single-threaded string manipulation | Multi-threaded string manipulation |

### Example

```java id="w9s7gi"
StringBuilder sb = new StringBuilder();

sb.append("Java")
  .append(" 8");

System.out.println(sb);
```

**Output**

```text id="u6pn6s"
Java 8
```

### Interview Point

> StringBuilder is generally preferred unless thread safety is required.

---

# 4. What is String Pool?

**Answer:**

String Pool is a special memory area inside Heap where JVM stores String literals.

### Example

```java id="hf6l2g"
String s1 = "Java";
String s2 = "Java";
```

### Memory

```text id="18owcv"
String Pool
-------------
"Java"
-------------
   ↑
 s1
 s2
```

Only one object is created and shared.

### Advantage

* Reduces memory consumption.
* Improves performance.

---

# 5. What is SCP (String Constant Pool)?

**Answer:**

SCP (String Constant Pool) is the same thing as String Pool.

It stores String literals and reuses existing String objects whenever possible.

### Example

```java id="g42sgd"
String s1 = "Java";
String s2 = "Java";
```

Only one object exists in SCP.

### Interview Point

> String Pool and SCP are often used interchangeably in interviews.

---

# 6. Difference Between Heap and String Pool

| Heap                  | String Pool                   |
|-----------------------|-------------------------------|
| Stores all objects    | Stores String literals only   |
| Larger memory area    | Special area inside Heap      |
| Objects may duplicate | Duplicate literals are reused |
| Created using `new`   | Created using string literals |

### Example

```java id="qq3vct"
String s1 = "Java";
String s2 = new String("Java");
```

Memory:

```text id="9ftq74"
Heap
------
new String("Java")
------

String Pool
------
"Java"
------
```

### Interview Point

> String Pool is a special part of Heap dedicated to String literal optimization.

---

# 7. How Many Objects Are Created?

### Code

```java id="7kw4g4"
String s = "Java";
String s2 = "Java";
```

### Answer

✅ Only **1 object**

### Memory

```text id="0j96s0"
String Pool
-------------
"Java"
-------------
   ↑
 s
 s2
```

Both references point to the same pooled String.

### Interview Answer

> Since `"Java"` already exists in the String Pool, the second assignment reuses the same object. Only one object is created.

---

# 8. How Many Objects Are Created?

### Code

```java id="jaf9oq"
String s = new String("Java");
```

### Answer

✅ **2 objects** (Interview Standard Answer)

### Memory

```text id="6zj9ww"
String Pool
-------------
"Java"
-------------

Heap
-------------
new String("Java")
-------------
```

#### Object 1

```text id="n7vhzm"
"Java"
```

created in String Pool (if not already present)

#### Object 2

```text id="rkxlj5"
new String("Java")
```

created in Heap

### Interview Point

> `new String()` always creates a new Heap object even if the literal already exists in the String Pool.

---

# 9. equals() vs ==

| equals()              | ==                    |
|-----------------------|-----------------------|
| Compares content      | Compares references   |
| Overridden in String  | Operator              |
| Checks value equality | Checks memory address |

### Example 1

```java id="bft3v5"
String s1 = new String("Java");
String s2 = new String("Java");

System.out.println(s1 == s2);
System.out.println(s1.equals(s2));
```

**Output**

```text id="n14rph"
false
true
```

### Why?

```text id="u48t5f"
s1 -> Heap Object 1
s2 -> Heap Object 2
```

Different references.

But contents are identical.

---

### Example 2

```java id="uzg7n4"
String s1 = "Java";
String s2 = "Java";

System.out.println(s1 == s2);
System.out.println(s1.equals(s2));
```

**Output**

```text id="4uxts8"
true
true
```

Because both references point to the same pooled object.

### Interview Point

> For Strings, always use `equals()` when comparing values. `==` should only be used when you intentionally want to compare object references.

---

# 10. What is `intern()` Method?

**Answer:**

`intern()` returns the reference of the String from the String Pool. If the String is not present in the pool, it gets added.

### Example

```java
String s1 = new String("Java"); // Heap + Pool
String s2 = s1.intern();        // Returns Pool reference

String s3 = "Java";

System.out.println(s2 == s3);
```

**Output**

```text
true
```

### Memory

```text
Heap
-----
s1 -> "Java"

String Pool
------------
"Java"
------------
   ↑
 s2
 s3
```

### Interview Point

> `intern()` is used to move/reuse Strings from the String Pool to save memory.

---

# 11. How Does String Pool Work Internally?

### Example

```java
String s1 = "Java";
String s2 = "Java";
String s3 = "Java";
```

### Internal Working

#### First Statement

```java
String s1 = "Java";
```

JVM checks SCP:

```text
Does "Java" exist?
```

❌ No

Create it.

```text
String Pool
------------
"Java"
------------
```

---

#### Second Statement

```java
String s2 = "Java";
```

JVM checks SCP:

```text
Does "Java" exist?
```

✅ Yes

Reuse existing object.

---

### Memory

```text
String Pool
------------
"Java"
------------
 ↑   ↑   ↑
s1  s2  s3
```

### Interview Point

> Before creating a String literal, JVM always checks the String Pool. If found, it reuses the existing object.

---

# 12. Why is String Final?

### Declaration

```java
public final class String
```

### Reasons

#### 1. Security

Prevents malicious subclasses from changing behavior.

```java
String url = "jdbc:mysql://...";
```

The value and behavior remain predictable.

---

#### 2. Immutability Guarantee

If inheritance were allowed:

```java
class MyString extends String {
   // Not allowed
}
```

Subclass could potentially break immutability.

---

#### 3. Thread Safety

Immutability ensures safe sharing across threads.

---

#### 4. String Pool Optimization

Pooling relies on String objects never changing.

### Interview Point

> String is final mainly to preserve immutability, security, thread safety, and String Pool consistency.

---

# 13. Why is String Used as HashMap Key?

### Answer

Because String is:

* Immutable
* Final
* Has cached hashCode()
* Reliable for hashing

### Example

```java
Map<String, Integer> map = new HashMap<>();

map.put("Java", 1);

System.out.println(map.get("Java"));
```

### What If String Were Mutable?

Imagine:

```java
String key = "Java";

map.put(key, 1);

// key changes somehow
key = "Spring";
```

Hash code would change.

HashMap may not find the entry anymore.

---

### Cached hashCode()

String caches hashCode after first calculation.

```java
String str = "Java";

str.hashCode(); // Calculated
str.hashCode(); // Reused
```

### Interview Point

> String is an ideal HashMap key because immutability guarantees that hashCode and equals behavior never changes after insertion.

---

# 14. Why is StringBuilder Faster?

### Example

#### String

```java
String s = "";

for (int i = 0; i < 3; i++) {
    s = s + "A";
}
```

### What Happens?

```text
""      -> New Object
"A"     -> New Object
"AA"    -> New Object
"AAA"   -> New Object
```

Multiple object creations.

---

### StringBuilder

```java
StringBuilder sb = new StringBuilder();

for (int i = 0; i < 3; i++) {
    sb.append("A");
}
```

### What Happens?

```text
Single StringBuilder Object
Buffer gets modified
```

No new String object on every append.

---

### Interview Point

> StringBuilder is faster because it modifies an internal character buffer instead of creating new objects repeatedly.

---

# 15. Difference Between `concat()` and `+` Operator

| concat()                                                | + Operator                         |
|---------------------------------------------------------|------------------------------------|
| Method of String class                                  | Operator                           |
| Accepts only String                                     | Can concatenate any type           |
| Slightly more efficient for simple String concatenation | Compiler converts to StringBuilder |
| Null causes exception                                   | Null converted to `"null"`         |

---

### Example 1

#### concat()

```java
String s = "Java";

System.out.println(s.concat(" 8"));
```

**Output**

```text
Java 8
```

---

### Example 2

#### + Operator

```java
String s = "Java";

System.out.println(s + 8);
```

**Output**

```text
Java8
```

---

### Internal Working of `+`

Compiler converts:

```java
String result = s + " World";
```

into roughly:

```java
String result =
        new StringBuilder()
                .append(s)
                .append(" World")
                .toString();
```

### Interview Point

> For a few concatenations, `+` is fine. For loops or large string manipulations, prefer `StringBuilder` because repeated `+` creates many temporary objects.

---

# 16. How is `hashCode()` Calculated for String?

### Formula Used Internally

```java
s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
```

### Example

```java
String str = "ABC";
```

ASCII Values:

```text
A = 65
B = 66
C = 67
```

Calculation:

```text
65 * 31² + 66 * 31¹ + 67

= 65 * 961 + 66 * 31 + 67

= 62465 + 2046 + 67

= 64578
```

### Actual JDK Logic (Simplified)

```java
int hash = 0;

for (char ch : value) {
    hash = 31 * hash + ch;
}
```

### Why 31?

* Prime number
* Good distribution of hash values
* Multiplication by 31 is optimized by JVM

```java
31 * x == (x << 5) - x
```

### Interview Point

> String hashCode is content-based. Two Strings with the same content always produce the same hashCode.

---

# 17. Why is String hashCode Cached?

### Problem

Calculating hashCode every time is expensive for long Strings.

```java
String str = "VeryLongString...";
```

Imagine this String is used millions of times as a HashMap key.

---

### Internal Implementation (Simplified)

```java
public final class String {

    private int hash;

    public int hashCode() {

        int h = hash;

        if (h == 0 && value.length > 0) {

            for (char c : value) {
                h = 31 * h + c;
            }

            hash = h;
        }

        return h;
    }
}
```

### How It Works

#### First Call

```java
str.hashCode();
```

* Hash calculated
* Stored in `hash` field

#### Subsequent Calls

```java
str.hashCode();
```

* Returned directly from cache

### Why Possible?

Because String is immutable.

```java
String str = "Java";
```

Its content can never change.

### Interview Point

> If String were mutable, cached hashCode could become invalid after modification.

---

# 18. Explain String Memory Optimization in Java 8

### 1. String Pool Reuse

```java
String s1 = "Java";
String s2 = "Java";
```

Only one object in the String Pool.

```text
String Pool
------------
"Java"
------------
 ↑      ↑
s1     s2
```

---

### 2. HashCode Caching

Hash calculated only once and reused.

---

### 3. Compact Strings (Java 9+)

**Important Interview Note**

Many candidates incorrectly say Java 8.

Actually:

```text
Compact Strings → Java 9
```

Before Java 9:

```java
char[] value;
```

Each character consumed 2 bytes.

---

Java 9+:

```java
byte[] value;
```

Latin characters consume less memory.

---

### Java 8 Optimization Summary

* String Pool
* Immutable Sharing
* HashCode Caching
* Reuse through `intern()`

### Interview Point

> The biggest String memory optimization in Java 8 is String Pool reuse.

---

# 19. Can an Immutable Class Be Broken?

### Answer

✅ Yes, if not designed properly.

### Example (Wrong Design)

```java
class Employee {

    private String name;

    private Address address;

    public Employee(String name, Address address) {
        this.name = name;
        this.address = address;
    }

    public Address getAddress() {
        return address; // Dangerous
    }
}
```

```java
Address addr = new Address("Bangalore");

Employee emp =
        new Employee("John", addr);

emp.getAddress().setCity("Pune");
```

Object state changed.

### Why?

Because mutable object reference was exposed.

---

### How to Prevent?

Return defensive copies.

```java
public Address getAddress() {
    return new Address(address);
}
```

### Other Ways Immutability Can Be Broken

* Reflection
* Serialization attacks
* Exposing mutable collections
* Returning mutable references

### Interview Point

> Immutable classes are only truly immutable when all mutable state is protected through defensive copying.

---

# 20. How Would You Design Your Own Immutable Class?

### Rules

1. Make class `final`
2. Make fields `private final`
3. No setters
4. Initialize through constructor
5. Defensive copy for mutable fields
6. Defensive copy in getters

---

### Example

```java
final class Employee {

    private final String name;

    private final Address address;

    public Employee(String name, Address address) {

        this.name = name;

        // Defensive copy
        this.address =
                new Address(address.getCity());
    }

    public String getName() {
        return name;
    }

    public Address getAddress() {

        // Return copy instead of actual object
        return new Address(address.getCity());
    }
}
```

### Mutable Class

```java
class Address {

    private String city;

    public Address(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
```

### Verification

```java
Address addr =
        new Address("Bangalore");

Employee emp =
        new Employee("John", addr);

addr.setCity("Pune");

System.out.println(
        emp.getAddress().getCity()
);
```

**Output**

```text
Bangalore
```

### Interview Answer (Short)

> To create an immutable class, make the class final, fields private final, initialize them through the constructor, avoid setters, and use defensive copying for mutable objects in constructors and getters. This ensures the object's state cannot be modified after creation.
