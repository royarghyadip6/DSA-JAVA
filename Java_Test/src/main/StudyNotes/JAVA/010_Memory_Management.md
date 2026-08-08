# 1. Stack Memory vs Heap Memory

<details>
<summary>Show Answer</summary>

| Stack Memory                             | Heap Memory                  |
|------------------------------------------|------------------------------|
| Stores local variables and method frames | Stores objects               |
| Thread-specific                          | Shared among threads         |
| Faster access                            | Slower access                |
| Automatically cleared when method ends   | Cleared by Garbage Collector |
| Small size                               | Larger size                  |

### Example

```java
public void test() {

    int age = 25;                // Stack

    Employee emp = new Employee(); // Reference -> Stack
                                  // Object -> Heap
}
```

### Memory View

```text
Stack                     Heap
------                    --------
age = 25

emp ------------------> Employee Object
```

### Interview Point

> Local variables live in Stack, objects live in Heap.

</details>

---

# 2. What Causes Memory Leak in Java?

<details>
<summary>Show Answer</summary>

### Definition

Memory leak occurs when objects are no longer needed but are still reachable, preventing GC from reclaiming them.

### Example

```java
List<Employee> employees =
        new ArrayList<>();

while (true) {

    employees.add(
        new Employee()
    );
}
```

Objects remain referenced by the list.

```text
List
  ↓
Employee Objects
```

GC cannot remove them.

### Common Causes

* Static collections
* Unclosed resources
* Listener registrations
* Caches without eviction
* ThreadLocal misuse

### Interview Point

> Memory leak in Java means unnecessary object retention, not lost memory like C/C++.

</details>

---

# 3. Is Java Completely Free from Memory Leaks?

<details>
<summary>Show Answer</summary>

### Answer

❌ No

Java prevents manual memory leaks but not logical memory leaks.

### Example

```java
static List<Object> cache =
        new ArrayList<>();
```

```java
cache.add(new Object());
```

Objects remain reachable forever.

### Why?

GC removes only unreachable objects.

```text
Reachable Object
       ↓
GC Cannot Remove
```

### Interview Answer

> Java is not completely free from memory leaks because reachable but unused objects can still consume memory.

</details>

---

# 4. What is OutOfMemoryError (OOM)?

<details>
<summary>Show Answer</summary>

### Definition

Occurs when JVM cannot allocate memory for a new object.

### Example

```java
List<Object> list =
        new ArrayList<>();

while (true) {

    list.add(new Object());
}
```

### Output

```text
java.lang.OutOfMemoryError:
Java heap space
```

### Interview Point

> OOM is an Error, not an Exception, because JVM cannot safely recover from it.

</details>

---

# 5. Different Types of OutOfMemoryError

<details>
<summary>Show Answer</summary>

## Java Heap Space

```text
java.lang.OutOfMemoryError:
Java heap space
```

Heap exhausted.

---

## GC Overhead Limit Exceeded

```text
java.lang.OutOfMemoryError:
GC overhead limit exceeded
```

GC spends most of its time collecting but recovers little memory.

---

## Metaspace

```text
java.lang.OutOfMemoryError:
Metaspace
```

Class metadata memory exhausted.

---

## Unable to Create New Native Thread

```text
java.lang.OutOfMemoryError:
unable to create new native thread
```

Too many threads.

---

## Direct Buffer Memory

```text
java.lang.OutOfMemoryError:
Direct buffer memory
```

Off-heap memory exhausted.

### Interview Point

> Most commonly seen OOMs are Heap Space and Metaspace.

</details>

---

# 6. What are Heap Generations?

<details>
<summary>Show Answer</summary>

Heap is divided to optimize GC.

```text
Heap
 │
 ├── Young Generation
 │      ├── Eden
 │      ├── Survivor S0
 │      └── Survivor S1
 │
 └── Old Generation
```

### Why?

Most objects die young.

This is called:

```text
Weak Generational Hypothesis
```

### Interview Point

> Heap generations reduce GC cost by treating short-lived and long-lived objects differently.

</details>

---

# 7. What is Young Generation?

<details>
<summary>Show Answer</summary>

### Purpose

Stores newly created objects.

### Example

```java
Employee emp =
        new Employee();
```

Object initially goes to:

```text
Young Generation
      ↓
Eden Space
```

### Characteristics

* Frequent GC
* Short-lived objects

### Interview Point

> Every newly created object starts in Young Generation.

</details>

---

# 8. What is Eden Space?

<details>
<summary>Show Answer</summary>

### Purpose

Initial allocation area for new objects.

### Example

```java
new Employee();
```

```text
Heap

Young Gen
---------
Eden
---------
```

Object created here first.

### Flow

```text
New Object
     ↓
Eden Space
```

### Interview Point

> Most objects die in Eden Space before reaching Old Generation.

</details>

---

# 9. What is Survivor Space?

<details>
<summary>Show Answer</summary>

Young Generation contains:

```text
Eden
S0 (Survivor)
S1 (Survivor)
```

### Purpose

Stores objects that survive Minor GC.

### Flow

```text
Eden
 ↓
Survivor S0
 ↓
Survivor S1
 ↓
Old Generation
```

### Interview Point

> Survivor spaces act as temporary holding areas for surviving objects.

</details>

---

# 10. What is Old Generation?

<details>
<summary>Show Answer</summary>

### Purpose

Stores long-lived objects.

### Example

```java
static Employee emp =
        new Employee();
```

Long-lived objects eventually get promoted.

```text
Young Gen
    ↓
Old Gen
```

### Characteristics

* Larger memory
* Less frequent GC
* Longer pause times

### Interview Point

> Objects surviving multiple Minor GCs are promoted to Old Generation.

</details>

---

# 11. Minor GC

<details>
<summary>Show Answer</summary>

### Definition

GC occurring in Young Generation.

### Example Flow

```text
Eden Full
    ↓
Minor GC
    ↓
Dead Objects Removed
Live Objects → Survivor
```

### Characteristics

* Fast
* Frequent
* Young Generation only

### Interview Point

> Minor GC cleans Eden and Survivor spaces.

</details>

---

# 12. Major GC

<details>
<summary>Show Answer</summary>

### Definition

GC focused on Old Generation.

### Example

```text
Old Generation Near Full
          ↓
Major GC
```

### Characteristics

* Slower than Minor GC
* Longer pause time

### Interview Point

> Major GC targets long-lived objects in Old Generation.

</details>

---

# 13. Full GC

<details>
<summary>Show Answer</summary>

### Definition

GC covering the entire heap.

### Areas Involved

```text
Young Generation
Old Generation
Metaspace (sometimes)
```

### Flow

```text
Entire JVM Memory
        ↓
Full GC
```

### Characteristics

* Slowest GC
* Stop-The-World pause
* Affects application performance

### Interview Point

> Full GC is expensive because it scans the whole heap.

---

# GC Flow (Most Asked)

```text
New Object
    ↓
Eden
    ↓ (Minor GC)
Survivor S0
    ↓
Survivor S1
    ↓
Old Generation
    ↓ (Major GC)
Removed

Entire Heap Scan
    ↓
Full GC
```

### 5–8 Year Interview One-Liner

> New objects are created in Eden Space, surviving objects move between Survivor spaces, long-lived objects are promoted to Old Generation, Minor GC cleans Young Generation, Major GC cleans Old Generation, and Full GC scans the entire heap.

These are very common **real-world production interview questions** for 6–8 years experienced Java developers. Interviewers usually expect practical answers rather than textbook definitions.

</details>

---

# 14. Explain Memory Leak from HashMap

<details>
<summary>Show Answer</summary>

### Problem

A HashMap can cause memory leaks when keys cannot be removed because of incorrect `equals()` and `hashCode()` implementation.

### Example

```java
class Employee {

    private int id;

    Employee(int id) {
        this.id = id;
    }

    // equals() and hashCode() NOT overridden
}
```

```java
Map<Employee, String> map = new HashMap<>();

Employee e1 = new Employee(1);

map.put(e1, "John");

// Trying to remove
map.remove(new Employee(1));

System.out.println(map.size());
```

**Output**

```text
1
```

### Why?

```java
new Employee(1)
```

is a different object.

HashMap cannot locate the original key.

Object remains in memory.

### Real Production Examples

```java
Cache
Session Store
User Lookup Maps
```

where entries continuously grow.

### Fix

Always override:

```java
equals()
hashCode()
```

for custom keys.

### Interview Answer

> HashMap memory leaks usually happen when entries are never removed due to improper key design, causing objects to remain strongly referenced and preventing garbage collection.

</details>

---

# 15. Explain Memory Leak in Listener Registration

<details>
<summary>Show Answer</summary>

### Problem

A listener is registered but never unregistered.

### Example

```java
class EventSource {

    private List<Listener> listeners =
            new ArrayList<>();

    public void register(Listener l) {
        listeners.add(l);
    }
}
```

```java
Listener listener =
        new UserListener();

eventSource.register(listener);
```

Later:

```java
listener = null;
```

Developer thinks object can be GC'ed.

### Reality

```text
EventSource
     ↓
listeners
     ↓
UserListener
```

Still reachable.

GC cannot remove it.

### Real Project Examples

```text
Swing Event Listeners
Spring Application Events
Kafka Consumers
WebSocket Subscribers
```

### Fix

```java
eventSource.unregister(listener);
```

or

```java
WeakReference
```

### Interview Answer

> Listener leaks occur when publishers keep references to listeners after they are no longer needed, preventing garbage collection.

</details>

---

# 16. Explain Memory Leak in ThreadLocal

<details>
<summary>Show Answer</summary>

### Very Important Interview Question

### Example

```java
private static final ThreadLocal<User>
        USER_CONTEXT = new ThreadLocal<>();
```

```java
USER_CONTEXT.set(user);
```

Request finishes.

But:

```java
USER_CONTEXT.remove();
```

is forgotten.

### Problem

In thread pools:

```text
Worker Thread
      ↓
ThreadLocalMap
      ↓
User Object
```

Thread stays alive.

Object remains alive.

Memory usage keeps increasing.

### Real Spring Boot Example

```java
Filter
Interceptor
Security Context
Transaction Context
```

### Correct Pattern

```java
try {

    USER_CONTEXT.set(user);

    // business logic

} finally {

    USER_CONTEXT.remove(); // Mandatory
}
```

### Interview Point

> ThreadLocal leaks are common in thread pools because worker threads live much longer than individual requests.

</details>

---

# 17. Heap Dump Analysis Experience?

<details>
<summary>Show Answer</summary>

### Interview-Ready Answer

> In one production issue, application memory usage kept growing and eventually resulted in `OutOfMemoryError: Java heap space`. We generated a heap dump using `jmap` and analyzed it using Eclipse MAT. The dominator tree showed that a cache map contained millions of entries and was retaining most of the heap. We identified missing eviction logic, implemented size-based expiration, redeployed the application, and memory consumption became stable.

---

### Common Tools

| Tool                       | Purpose              |
|----------------------------|----------------------|
| jmap                       | Generate heap dump   |
| jcmd                       | JVM diagnostics      |
| Eclipse MAT                | Heap analysis        |
| VisualVM                   | Monitoring           |
| JConsole                   | JVM monitoring       |
| Java Flight Recorder (JFR) | Production profiling |

### Common MAT Investigation Steps

```text
Heap Dump
     ↓
Histogram
     ↓
Largest Objects
     ↓
Dominator Tree
     ↓
GC Roots
     ↓
Leak Source
```

### Keywords Interviewers Like

```text
Heap Dump
Histogram
Dominator Tree
Retained Heap
GC Roots
MAT
```

</details>

---

# 18. How Did You Solve a Memory Issue in Production?

<details>
<summary>Show Answer</summary>

### Sample Answer (Very Strong)

> We observed increasing heap utilization and frequent Full GCs in production. Using Grafana and JVM metrics, we confirmed that memory was not being released after requests completed. We captured a heap dump and analyzed it in Eclipse MAT. The analysis showed a static cache retaining large amounts of data without eviction. We implemented a bounded cache with expiry, performed load testing, and verified that heap usage stabilized and Full GC frequency dropped significantly.

---

### Alternative Answer (ThreadLocal Leak)

> In a Spring Boot application, memory usage gradually increased over several days. Heap dump analysis revealed objects retained through ThreadLocal variables in a thread pool. The cleanup code was missing. We added `ThreadLocal.remove()` in a finally block and validated the fix using heap analysis and long-duration testing.

---

# Production Troubleshooting Flow (Very Frequently Asked)

```text
High Memory Usage
        ↓
GC Logs Analysis
        ↓
Heap Dump Capture
        ↓
MAT Analysis
        ↓
Find Largest Retained Objects
        ↓
Check GC Roots
        ↓
Fix Leak
        ↓
Load Test
        ↓
Production Verification
```

### One-Liner for Interview

> When diagnosing memory issues, I usually start with JVM metrics and GC behavior, then capture a heap dump, analyze retained objects and GC roots using Eclipse MAT, identify the retaining reference chain, implement the fix, and validate it through load testing before deployment.

</details>
