# 1. What is Garbage Collection (GC)?

### Definition

Garbage Collection is the JVM process of automatically reclaiming memory occupied by objects that are no longer reachable.

### Example

```java id="6pd36v"
Employee emp = new Employee();

emp = null;
```

Object becomes eligible for GC.

```text
Heap
-----
Employee Object
     ↓
Unreachable
     ↓
GC Removes
```

### Benefits

* Automatic memory management
* Prevents manual memory cleanup
* Reduces memory-related bugs

### Interview Point

> Garbage Collection removes unreachable objects from Heap memory automatically.

---

# 2. How Does an Object Become Eligible for GC?

### Case 1: Nullifying Reference

```java id="y4xofm"
Employee emp = new Employee();

emp = null;
```

Object becomes unreachable.

---

### Case 2: Reassigning Reference

```java id="m74j06"
Employee emp = new Employee();

emp = new Employee();
```

First object becomes eligible for GC.

---

### Case 3: Local Reference Goes Out of Scope

```java id="wy9m0l"
public void test() {

    Employee emp =
            new Employee();

} // method ends
```

Object may become eligible.

---

### Case 4: Island of Isolation

```java id="q69s7z"
class Employee {

    Employee ref;
}
```

```java id="9sz4g7"
Employee e1 = new Employee();
Employee e2 = new Employee();

e1.ref = e2;
e2.ref = e1;

e1 = null;
e2 = null;
```

Both objects reference each other but are unreachable from application.

Eligible for GC.

### Interview Point

> Reachability matters, not reference count.

---

# 3. Can We Force GC?

### Answer

❌ No

We can only request GC.

### Example

```java id="7kqf4w"
System.gc();
```

or

```java id="j5z3hg"
Runtime.getRuntime().gc();
```

### Important

```java id="i8w4v7"
System.gc();
```

does NOT guarantee GC execution.

### Interview Answer

> We can request garbage collection using System.gc(), but JVM decides whether and when to perform it.

---

# 4. Difference Between `finalize()` and GC

| GC                              | finalize()                            |
|---------------------------------|---------------------------------------|
| JVM memory cleanup process      | Callback method before object removal |
| Automatic                       | Automatic                             |
| Still exists                    | Deprecated (Java 9+)                  |
| Removes memory                  | Used for cleanup logic                |
| Runs many times during app life | At most once per object               |

### Example

```java id="znod54"
@Override
protected void finalize() {

    System.out.println("Cleanup");
}
```

### Why Deprecated?

* Unpredictable
* Performance issues
* Resource leaks

### Modern Alternative

```java id="m7g3iu"
try-with-resources
```

### Interview Point

> finalize() is deprecated and should not be used for resource cleanup.

---

# 5. Reference Types in Java

Java provides four important reference types:

```text
1. Strong
2. Weak
3. Soft
4. Phantom
```

---

# 6. Strong Reference

### Example

```java id="q8vb0h"
Employee emp =
        new Employee();
```

### Memory

```text
emp ─────► Employee Object
```

GC cannot remove the object.

### Interview Point

> Normal object references are strong references.

---

# 7. Weak Reference

### Example

```java id="o6cc0r"
WeakReference<Employee> ref =
    new WeakReference<>(
        new Employee()
    );
```

### Behavior

After GC:

```java id="7h7b8l"
System.gc();
```

Object can be removed immediately.

### Use Cases

```text
WeakHashMap
Caching
Memory-sensitive data
```

### Interview Point

> Weakly referenced objects are collected during the next GC cycle.

---

# 8. Soft Reference

### Example

```java id="z2d2ic"
SoftReference<Employee> ref =
    new SoftReference<>(
        new Employee()
    );
```

### Behavior

Object remains until JVM experiences memory pressure.

### Use Cases

```text
Image Cache
Application Cache
```

### Interview Point

> Soft references survive normal GC and are removed only when memory becomes scarce.

---

# 9. Phantom Reference

### Example

```java id="j9jzaf"
ReferenceQueue<Employee> queue =
        new ReferenceQueue<>();

PhantomReference<Employee> ref =
        new PhantomReference<>(
            new Employee(),
            queue
        );
```

### Characteristics

```java id="u8k2nq"
ref.get();
```

always returns:

```text
null
```

### Use Cases

```text
Advanced Resource Cleanup
Memory Tracking
Framework Internals
```

### Interview Point

> Phantom references are used to receive notification after an object becomes eligible for reclamation.

---

# 10. System.gc() Guarantee?

### Answer

❌ No Guarantee

### Example

```java id="x5ij6q"
System.gc();
```

### JVM Behavior

```text
Request Sent
      ↓
JVM May Ignore
```

### Why?

JVM decides based on:

```text
Memory Pressure
GC Algorithm
Performance Considerations
```

### Interview Point

> System.gc() is only a suggestion to JVM.

---

# 11. How Reachability Analysis Works?

### Modern GC Algorithm

JVM starts from special root objects and traverses references.

### Example

```java id="m5rsm4"
Employee e1 =
        new Employee();

Employee e2 =
        new Employee();

e1.ref = e2;
```

### Reachability Graph

```text
GC Root
   ↓
e1
   ↓
e2
```

Both reachable.

---

If:

```java id="pl5nse"
e1 = null;
```

Then:

```text
GC Root

(No path to e1 or e2)
```

Both become eligible.

### Interview Point

> JVM uses reachability analysis, not reference counting.

---

# 12. What are GC Roots?

### Definition

GC Roots are starting points used during reachability analysis.

### Common GC Roots

#### 1. Local Variables in Stack

```java id="v2x6db"
Employee emp =
        new Employee();
```

```text
Stack
  ↓
emp
  ↓
Object
```

---

#### 2. Active Threads

```java id="5gnhx9"
Thread t =
        new Thread();
```

Thread references are GC roots.

---

#### 3. Static Variables

```java id="rf4vfp"
class Cache {

    static Employee emp =
            new Employee();
}
```

Static references are GC roots.

---

#### 4. JNI References

```text
Native C/C++ Code
        ↓
Java Object
```

Referenced objects cannot be collected.

---

### Example

```java id="8r6x4l"
class Cache {

    static Employee emp =
            new Employee();
}
```

```text
GC Root
   ↓
Cache.emp
   ↓
Employee Object
```

Object survives GC.

### Interview Answer

> GC Roots are special references such as stack variables, active threads, static fields, and JNI references. During reachability analysis, any object reachable from a GC Root is considered alive.

---

# 5–8 Year Interview Rapid Fire

### Q: Does Java use reference counting?

**Answer:** ❌ No. Java uses reachability analysis.

---

### Q: Is an object with circular references eligible for GC?

```java id="a6c5n4"
e1.ref = e2;
e2.ref = e1;
```

**Answer:** ✅ Yes, if no GC Root can reach them.

---

### Q: Which reference is strongest?

```text
Strong > Soft > Weak > Phantom
```

---

### Q: Which reference is commonly used for caches?

**Answer:** SoftReference

---

### Q: Can System.gc() force GC?

**Answer:** No, it only requests GC.

---

### Q: What replaced finalize()?

**Answer:** `AutoCloseable`, `try-with-resources`, and `Cleaner` API.

---

### Interview One-Liner

> Garbage Collection in Java is based on reachability analysis. Objects not reachable from GC Roots become eligible for collection. JVM automatically reclaims memory using different reference strengths and GC algorithms, while `System.gc()` only requests—not guarantees—garbage collection.

# 13. What is Serial GC?

### Definition

Serial GC uses a **single thread** for garbage collection.

### Working

```text
Application Stops (STW)
         ↓
Single GC Thread
         ↓
Memory Cleanup
```

### JVM Option

```bash
-XX:+UseSerialGC
```

### Advantages

* Simple
* Low memory overhead

### Disadvantages

* Long pause times
* Not suitable for large applications

### Use Cases

```text
Small applications
Single CPU systems
Development environments
```

### Interview Point

> Serial GC uses one GC thread and causes Stop-The-World pauses during collection.

---

# 14. What is Parallel GC?

### Definition

Parallel GC uses multiple threads for Young Generation collection.

Also called:

```text
Throughput Collector
```

### JVM Option

```bash
-XX:+UseParallelGC
```

### Working

```text
Application Stops
         ↓
Multiple GC Threads
         ↓
Faster Collection
```

### Advantages

* High throughput
* Better CPU utilization

### Disadvantages

* Longer pause times compared to G1/ZGC

### Use Cases

```text
Batch Processing
Data Processing Jobs
Applications where throughput matters
```

### Interview Point

> Parallel GC focuses on maximizing throughput, not minimizing pause times.

---

# 15. What is CMS GC (Concurrent Mark Sweep)?

### Definition

CMS performs most GC work concurrently with application threads.

### JVM Option (Java 8)

```bash
-XX:+UseConcMarkSweepGC
```

### Phases

```text
Initial Mark (STW)
      ↓
Concurrent Mark
      ↓
Remark (STW)
      ↓
Concurrent Sweep
```

### Advantages

* Lower pause times than Parallel GC

### Disadvantages

* Fragmentation issues
* More CPU consumption
* Deprecated in Java 9
* Removed in Java 14

### Interview Point

> CMS reduced pause times but suffered from memory fragmentation and was eventually replaced by G1.

---

# 16. What is G1 GC?

### Definition

G1 (Garbage First) is the default GC in modern Java.

### JVM Option

```bash
-XX:+UseG1GC
```

### Why "Garbage First"?

It cleans regions containing the most garbage first.

### Heap Structure

Traditional Heap:

```text
Young Gen
Old Gen
```

G1 Heap:

```text
Region 1
Region 2
Region 3
...
Region N
```

Heap is divided into many equal-sized regions.

### Working

```text
Find regions with maximum garbage
             ↓
Collect those regions first
```

### Advantages

* Predictable pause times
* Handles large heaps well
* Reduced Full GC occurrences

### Disadvantages

* Slightly lower throughput than Parallel GC

### Interview Point

> G1 balances throughput and pause time, which is why it became the default GC since Java 9.

---

# 17. What is ZGC?

### Definition

ZGC (Z Garbage Collector) is a low-latency collector designed for very large heaps.

### JVM Option

```bash
-XX:+UseZGC
```

### Key Feature

Pause times remain extremely low even for huge heaps.

```text
Heap = 8 GB
Heap = 64 GB
Heap = 1 TB

Pause ≈ few milliseconds
```

### Working

Most work happens concurrently with application threads.

### Advantages

* Ultra-low pause times
* Scales to TB-sized heaps
* Excellent for latency-sensitive systems

### Disadvantages

* Slightly higher CPU usage
* Not needed for smaller applications

### Use Cases

```text
Trading Systems
Large Microservices
Real-time Platforms
Large Enterprise Systems
```

### Interview Point

> ZGC prioritizes low latency over maximum throughput.

---

# 18. What is Shenandoah GC?

### Definition

Shenandoah is another low-pause-time garbage collector developed by Red Hat.

### JVM Option

```bash
-XX:+UseShenandoahGC
```

### Goal

```text
Pause Time ≈ Constant
Regardless of Heap Size
```

### Working

Moves objects concurrently while application threads continue running.

### Advantages

* Very low pause times
* Better responsiveness

### Disadvantages

* Higher CPU overhead
* Not available in all JDK distributions

### Interview Point

> Like ZGC, Shenandoah focuses on minimizing pause times rather than maximizing throughput.

---

# 19. Comparison of Major GCs

| GC         | Pause Time | Throughput | Heap Size    | Status    |
|------------|------------|------------|--------------|-----------|
| Serial     | High       | Low        | Small        | Supported |
| Parallel   | High       | Very High  | Medium/Large | Supported |
| CMS        | Medium     | Good       | Large        | Removed   |
| G1         | Low-Medium | Good       | Large        | Default   |
| ZGC        | Very Low   | Good       | Very Large   | Supported |
| Shenandoah | Very Low   | Good       | Large        | Supported |

---

# 20. Which GC Did Your Application Use and Why?

### Interview-Ready Answer (Most Common)

> Our Spring Boot microservices run on Java 17 and use **G1 GC**, which is the default collector. We chose it because it provides a good balance between throughput and pause time. Our heap sizes are typically between 2–8 GB, and G1 handles such workloads efficiently while avoiding long Full GC pauses.

### Follow-up: How Did You Verify?

```bash
java -XX:+PrintCommandLineFlags -version
```

or

```bash
jcmd <pid> VM.flags
```

or check startup logs.

---

# Real-World Selection Guide (6–8 Years)

| Scenario                                     | Preferred GC       |
|----------------------------------------------|--------------------|
| Small Utility App                            | Serial GC          |
| Batch Processing                             | Parallel GC        |
| Legacy Java 8 System                         | CMS (historically) |
| Most Spring Boot Apps                        | G1 GC              |
| Low Latency Trading System                   | ZGC                |
| Very Large Heap with Low Latency Requirement | ZGC / Shenandoah   |

### Interview One-Liner

> For most modern Java enterprise applications, G1 GC is the preferred choice because it provides predictable pause times, good throughput, and efficient handling of multi-GB heaps. For ultra-low-latency systems, ZGC or Shenandoah are typically considered.
