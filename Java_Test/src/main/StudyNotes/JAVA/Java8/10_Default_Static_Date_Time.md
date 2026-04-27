Great—these are **very important Java 8 topics** and often combined in interviews.
We’ll do a **clean, deep theory breakdown** (no MCQs yet).

---

# 🔥 1. Default & Static Methods in Interfaces

(Enhancement in Java 8)

---

# 🧠 1.1 Why were they introduced?

### ❌ Problem Before Java 8

* Interfaces could only have **abstract methods**
* If you add a new method → **all implementing classes break**

---

### ✅ Solution

👉 Introduced:

* **Default methods**
* **Static methods**

👉 To support:

* **Backward compatibility**
* **API evolution (very important)**

---

# 🔥 1.2 Default Methods

---

## ✅ Definition

A **default method** is a method inside an interface with a **body**.

---

### Syntax:

```java id="d1"
interface A {
    default void show() {
        System.out.println("Default method");
    }
}
```

---

## 🧠 Key Characteristics

* Has **implementation**
* Can be **overridden**
* Enables **multiple inheritance of behavior**

---

## 🔥 Example

```java id="d2"
interface Vehicle {
    default void start() {
        System.out.println("Vehicle starting");
    }
}

class Car implements Vehicle {}
```

---

# ⚠️ 1.3 Multiple Inheritance Conflict (VERY IMPORTANT)

---

## ❌ Problem

```java id="d3"
interface A {
    default void show() { System.out.println("A"); }
}

interface B {
    default void show() { System.out.println("B"); }
}

class Test implements A, B {}
```

👉 ❌ Compile-time error

---

## ✅ Fix (Must Override)

```java id="d4"
class Test implements A, B {
    public void show() {
        A.super.show(); // or B.super.show()
    }
}
```

---

### 🧠 Rule

```text
Class > Interface > Default Method
```

---

# 🔥 1.4 Static Methods in Interface

---

## ✅ Definition

Static methods belong to the **interface itself**, not instances.

---

### Syntax:

```java id="d5"
interface MathUtil {
    static int add(int a, int b) {
        return a + b;
    }
}
```

---

## 🧠 Key Characteristics

* Cannot be overridden
* Called using interface name

---

### Usage:

```java id="d6"
MathUtil.add(2, 3);
```

---

# ⚠️ Important Difference

| Feature           | Default Method | Static Method |
|-------------------|----------------|---------------|
| Overridable       | ✅ Yes          | ❌ No          |
| Called via object | ✅ Yes          | ❌ No          |
| Inheritance       | Yes            | No            |

---

# 🔥 1.5 Internal Purpose (Interview Level)

* Helps evolve APIs like:

    * Collections framework
* Avoids breaking millions of implementations

---

# 💥 Interview Killer Line

👉 “Default methods enable backward-compatible API evolution, while static methods provide utility behavior directly within interfaces.”

---

---

# 🔥 2. Date & Time API (`java.time`)

---

# 🧠 2.1 Why New API?

### ❌ Problems with old API (`Date`, `Calendar`)

* Mutable
* Not thread-safe
* Confusing
* Poor design

---

### ✅ New API (Java 8)

Package:

```java id="t1"
java.time
```

👉 Inspired by Joda-Time

---

# 🔥 2.2 Core Principles

```text
Immutable + Thread-safe + Fluent API
```

---

# 🔥 2.3 Key Classes

---

## ✅ 1. `LocalDate`

👉 Date only (no time)

```java id="t2"
LocalDate date = LocalDate.now();
```

---

## ✅ 2. `LocalTime`

👉 Time only

```java id="t3"
LocalTime time = LocalTime.now();
```

---

## ✅ 3. `LocalDateTime`

👉 Date + Time

```java id="t4"
LocalDateTime dt = LocalDateTime.now();
```

---

## ✅ 4. `ZonedDateTime`

👉 Date + Time + Timezone

```java id="t5"
ZonedDateTime zdt = ZonedDateTime.now();
```

---

## ✅ 5. `Instant`

👉 Machine timestamp (UTC)

```java id="t6"
Instant now = Instant.now();
```

---

# 🔥 2.4 Creating Dates

```java id="t7"
LocalDate.of(2025, 4, 27);
```

---

# 🔥 2.5 Manipulation (Immutable)

```java id="t8"
date.plusDays(5);
date.minusMonths(2);
```

👉 Returns **new object**

---

# 🔥 2.6 Formatting & Parsing

---

## Formatting

```java id="t9"
DateTimeFormatter f = DateTimeFormatter.ofPattern("dd-MM-yyyy");
String s = date.format(f);
```

---

## Parsing

```java id="t10"
LocalDate d = LocalDate.parse("27-04-2025", f);
```

---

# 🔥 2.7 Time Zones (VERY IMPORTANT)

```java id="t11"
ZonedDateTime zdt =
    ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
```

---

# 🔥 2.8 Duration vs Period

---

## ✅ Duration → Time-based

```java id="t12"
Duration d = Duration.between(t1, t2);
```

---

## ✅ Period → Date-based

```java id="t13"
Period p = Period.between(d1, d2);
```

---

# 🔥 2.9 Comparison

```java id="t14"
date1.isBefore(date2);
date1.isAfter(date2);
```

---

# 🔥 2.10 Thread Safety

👉 All classes are:

```text
Immutable → Thread-safe
```

---

# ⚠️ Common Mistakes

---

## ❌ Mixing old and new API

---

## ❌ Ignoring timezone

---

## ❌ Assuming mutable behavior

---

# 🧠 FINAL MEMORY RULES

```text
Default → override allowed  
Static → utility methods  

java.time → immutable + thread-safe  
LocalDate → date  
LocalTime → time  
ZonedDateTime → timezone
```

---

# 💥 FINAL INTERVIEW LINE

👉 “Java 8 introduced default and static methods to enable interface evolution without breaking implementations, and the java.time API to provide a modern, immutable, and thread-safe replacement for the legacy date-time classes.”

---


---

# 🔥 Tricky MCQs on Default & Static Methods (Very Hard Conflicts)

---

## 🧠 Q1. What happens here?

```java id="q1"
interface A {
    default void show() {
        System.out.println("A");
    }
}

interface B {
    default void show() {
        System.out.println("B");
    }
}

class Test implements A, B {}
```

### Options:

1. Prints A
2. Prints B
3. Compile-time error
4. Runtime exception

<details>
<summary>Show Answer</summary>

**Correct Answer: 3**

👉 Conflict: same default method → must override

</details>

---

## 🧠 Q2. What is the output?

```java id="q2"
interface A {
    default void show() {
        System.out.println("A");
    }
}

class Test implements A {
    public void show() {
        System.out.println("Class");
    }
}

new Test().show();
```

### Options:

1. A
2. Class
3. Compile error
4. Runtime error

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 Class method overrides default method

</details>

---

## 🧠 Q3. What happens here?

```java id="q3"
interface A {
    default void show() {
        System.out.println("A");
    }
}

class Test implements A {
    public static void show() {
        System.out.println("Static");
    }
}
```

### Options:

1. Works fine
2. Compile error
3. Runtime error
4. Both methods exist

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 Static method cannot override default method

</details>

---

## 🧠 Q4. What is the output?

```java id="q4"
interface A {
    default void show() {
        System.out.println("A");
    }
}

interface B extends A {}

class Test implements B {}

new Test().show();
```

### Options:

1. A
2. Compile error
3. Runtime error
4. Nothing

<details>
<summary>Show Answer</summary>

**Correct Answer: 1**

👉 Default method inherited

</details>

---

## 🧠 Q5. What happens here?

```java id="q5"
interface A {
    default void show() {
        System.out.println("A");
    }
}

class Test implements A {
    void show() {
        System.out.println("Test");
    }
}
```

### Options:

1. Works fine
2. Compile error
3. Runtime error
4. Ambiguous

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 Must be public → cannot reduce visibility

</details>

---

## 🧠 Q6. What happens here?

```java id="q6"
interface A {
    static void show() {
        System.out.println("A");
    }
}

class Test implements A {
    public void show() {
        System.out.println("Test");
    }
}
```

### Options:

1. Overrides static method
2. Compile error
3. Works but not overriding
4. Runtime error

<details>
<summary>Show Answer</summary>

**Correct Answer: 3**

👉 Static methods are NOT inherited → no override

</details>

---

## 🧠 Q7. What happens here?

```java id="q7"
interface A {
    static void show() {
        System.out.println("A");
    }
}

class Test implements A {}

new Test().show();
```

### Options:

1. A
2. Compile error
3. Runtime error
4. Works fine

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 Static method must be called using interface name

</details>

---

## 🧠 Q8. What is the output?

```java id="q8"
interface A {
    default void show() {
        System.out.println("A");
    }
}

interface B extends A {
    default void show() {
        System.out.println("B");
    }
}

class Test implements B {}

new Test().show();
```

### Options:

1. A
2. B
3. Compile error
4. Runtime error

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 Child interface overrides default method

</details>

---

## 🧠 Q9. What happens here?

```java id="q9"
interface A {
    default void show() {
        System.out.println("A");
    }
}

class Parent {
    public void show() {
        System.out.println("Parent");
    }
}

class Test extends Parent implements A {}

new Test().show();
```

### Options:

1. A
2. Parent
3. Compile error
4. Runtime error

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 Class wins over interface

</details>

---

## 🧠 Q10. What is the output?

```java id="q10"
interface A {
    default void show() {
        System.out.println("A");
    }
}

interface B {
    default void show() {
        System.out.println("B");
    }
}

class Test implements A, B {
    public void show() {
        A.super.show();
    }
}

new Test().show();
```

### Options:

1. A
2. B
3. Compile error
4. Runtime error

<details>
<summary>Show Answer</summary>

**Correct Answer: 1**

👉 Explicitly choosing A’s default

</details>

---

## 🧠 Q11. What happens here?

```java id="q11"
interface A {
    default void show() {
        System.out.println("A");
    }
}

interface B extends A {}

interface C extends A {}

class Test implements B, C {}

new Test().show();
```

### Options:

1. A
2. Compile error
3. Runtime error
4. Ambiguous

<details>
<summary>Show Answer</summary>

**Correct Answer: 1**

👉 Same inherited method → no conflict

</details>

---

## 🧠 Q12. What happens here?

```java id="q12"
interface A {
    default void show() {
        System.out.println("A");
    }
}

interface B {
    void show();
}

class Test implements A, B {}

new Test().show();
```

### Options:

1. A
2. Compile error
3. Runtime error
4. Nothing

<details>
<summary>Show Answer</summary>

**Correct Answer: 1**

👉 Default satisfies abstract method

</details>

---

## 🧠 Q13. What happens here?

```java id="q13"
interface A {
    default void show() {
        System.out.println("A");
    }
}

interface B {
    static void show() {
        System.out.println("B");
    }
}

class Test implements A, B {}

new Test().show();
```

### Options:

1. A
2. B
3. Compile error
4. Runtime error

<details>
<summary>Show Answer</summary>

**Correct Answer: 1**

👉 Static method not inherited → no conflict

</details>

---

## 🧠 Q14. What happens here?

```java id="q14"
interface A {
    default void show() {
        System.out.println("A");
    }
}

abstract class B implements A {
    public abstract void show();
}

class Test extends B {
    public void show() {
        System.out.println("Test");
    }
}

new Test().show();
```

### Options:

1. A
2. Test
3. Compile error
4. Runtime error

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 Abstract class forces override

</details>

---

# 🧠 FINAL CONFLICT RULES (VERY IMPORTANT)

<details>
<summary>Show Rules</summary>

```text id="rules"
1. Class > Interface
2. Child Interface > Parent Interface
3. Multiple default conflict → must override
4. Static methods → NOT inherited
5. Cannot reduce visibility when overriding
6. Static cannot override default
```

</details>

---

# 💥 INTERVIEW KILLER INSIGHT

<details>
<summary>Show Answer</summary>

👉 “The most critical rule in default method conflicts is that class methods always take precedence, followed by the most specific interface, and any ambiguity must be resolved explicitly using InterfaceName.super.method().”

</details>

---

---

# 🔥 Tricky MCQs on `java.time` (Timezone + Immutability Traps)

---

## 🧠 Q1. What is the output?

```java id="q1"
LocalDate date = LocalDate.of(2025, 4, 27);
date.plusDays(5);
System.out.println(date);
```

### Options:

1. 2025-05-02
2. 2025-04-27
3. Compile error
4. Runtime error

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 `LocalDate` is immutable → original object unchanged

</details>

---

## 🧠 Q2. What is the output?

```java id="q2"
LocalDate date = LocalDate.of(2025, 4, 27);
date = date.plusDays(5);
System.out.println(date);
```

### Options:

1. 2025-05-02
2. 2025-04-27
3. Compile error
4. Runtime error

<details>
<summary>Show Answer</summary>

**Correct Answer: 1**

👉 Must reassign → new object returned

</details>

---

## 🧠 Q3. What happens here?

```java id="q3"
LocalDateTime dt = LocalDateTime.now();
ZonedDateTime zdt = ZonedDateTime.from(dt);
```

### Options:

1. Works fine
2. Compile error
3. Runtime exception
4. Returns UTC time

<details>
<summary>Show Answer</summary>

**Correct Answer: 3**

👉 `LocalDateTime` has no zone → cannot convert directly

</details>

---

## 🧠 Q4. What is the output?

```java id="q4"
ZonedDateTime z1 =
    ZonedDateTime.now(ZoneId.of("UTC"));

ZonedDateTime z2 =
    ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

System.out.println(z1.isEqual(z2));
```

### Options:

1. true
2. false
3. Compile error
4. Runtime error

<details>
<summary>Show Answer</summary>

**Correct Answer: 1**

👉 Same instant, different zones → still equal

</details>

---

## 🧠 Q5. What happens here?

```java id="q5"
LocalDate date = LocalDate.now();
date.minusDays(1);
date.plusDays(1);
System.out.println(date);
```

### Options:

1. Yesterday
2. Tomorrow
3. Today
4. Compile error

<details>
<summary>Show Answer</summary>

**Correct Answer: 3**

👉 No reassignment → no change

</details>

---

## 🧠 Q6. What is the output?

```java id="q6"
LocalTime t1 = LocalTime.of(10, 0);
LocalTime t2 = t1.plusHours(2);

System.out.println(t1);
```

### Options:

1. 10:00
2. 12:00
3. Compile error
4. Runtime error

<details>
<summary>Show Answer</summary>

**Correct Answer: 1**

👉 Immutable → original unchanged

</details>

---

## 🧠 Q7. What is the issue here?

```java id="q7"
ZonedDateTime zdt =
    ZonedDateTime.now();

LocalDateTime ldt = zdt.toLocalDateTime();

ZonedDateTime newZdt =
    ZonedDateTime.from(ldt);
```

### Options:

1. Works fine
2. Compile error
3. Runtime exception
4. Same value

<details>
<summary>Show Answer</summary>

**Correct Answer: 3**

👉 Zone info lost → cannot reconstruct

</details>

---

## 🧠 Q8. What is the output?

```java id="q8"
ZonedDateTime zdt =
    ZonedDateTime.now(ZoneId.of("UTC"));

ZonedDateTime changed =
    zdt.withZoneSameInstant(ZoneId.of("Asia/Kolkata"));

System.out.println(zdt.equals(changed));
```

### Options:

1. true
2. false
3. Compile error
4. Runtime error

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 Same instant, different zone → not equal

</details>

---

## 🧠 Q9. What happens here?

```java id="q9"
ZonedDateTime zdt =
    ZonedDateTime.now(ZoneId.of("UTC"));

ZonedDateTime changed =
    zdt.withZoneSameLocal(ZoneId.of("Asia/Kolkata"));

System.out.println(zdt.isEqual(changed));
```

### Options:

1. true
2. false
3. Compile error
4. Runtime error

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 Same local time, different instant → NOT equal

</details>

---

## 🧠 Q10. What is the output?

```java id="q10"
Instant i1 = Instant.now();
Instant i2 = i1.plusSeconds(60);

System.out.println(i1);
```

### Options:

1. Original time
2. +60 seconds
3. Compile error
4. Runtime error

<details>
<summary>Show Answer</summary>

**Correct Answer: 1**

👉 Immutable → original unchanged

</details>

---

## 🧠 Q11. What happens here?

```java id="q11"
LocalDate date = LocalDate.parse("2025-13-01");
```

### Options:

1. Works fine
2. Compile error
3. DateTimeParseException
4. Adjusts month automatically

<details>
<summary>Show Answer</summary>

**Correct Answer: 3**

👉 Invalid month → exception

</details>

---

## 🧠 Q12. What is the output?

```java id="q12"
LocalDate d1 = LocalDate.of(2025, 4, 27);
LocalDate d2 = LocalDate.of(2025, 4, 27);

System.out.println(d1 == d2);
```

### Options:

1. true
2. false
3. Compile error
4. Runtime error

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 Different objects → use `.equals()`

</details>

---

## 🧠 Q13. What happens here?

```java id="q13"
ZonedDateTime zdt =
    ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));

Instant i = zdt.toInstant();

System.out.println(i);
```

### Options:

1. Compile error
2. Local time printed
3. UTC time
4. Runtime error

<details>
<summary>Show Answer</summary>

**Correct Answer: 3**

👉 Instant is always UTC

</details>

---

## 🧠 Q14. What is the issue?

```java id="q14"
LocalDateTime ldt =
    LocalDateTime.now(ZoneId.of("UTC"));
```

### Options:

1. Works fine
2. Compile error
3. Runtime error
4. Returns UTC time

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 LocalDateTime has no zone → cannot pass ZoneId

</details>

---

## 🧠 Q15. What is the output?

```java id="q15"
ZonedDateTime z1 =
    ZonedDateTime.now(ZoneId.of("UTC"));

ZonedDateTime z2 =
    z1.plusHours(5);

System.out.println(z1.getHour() == z2.getHour());
```

### Options:

1. true
2. false
3. Compile error
4. Runtime error

<details>
<summary>Show Answer</summary>

**Correct Answer: 2**

👉 New object → different time

</details>

---

# 🧠 FINAL TRAP RULES (VERY IMPORTANT)

<details>
<summary>Show Rules</summary>

```text
1. All java.time classes are immutable
2. Must reassign after modification
3. LocalDateTime → no timezone
4. ZonedDateTime → timezone aware
5. Instant → always UTC
6. withZoneSameInstant → same moment
7. withZoneSameLocal → same local time
8. equals() vs isEqual() → different meaning
```

</details>

---

# 💥 INTERVIEW KILLER INSIGHT

<details>
<summary>Show Answer</summary>

👉 “Most production bugs in java.time come from misunderstanding immutability and confusing local time with actual instant across time zones.”

</details>

---
