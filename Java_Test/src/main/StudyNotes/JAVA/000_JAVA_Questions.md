For a **5–8 years experienced Java developer**, interviewers usually expect strong knowledge of **Core Java, Java 8+, Collections, Multithreading, JVM, Memory Management, Design Patterns, Exception Handling, OOP, and Spring ecosystem**.

Since one response cannot realistically contain *every question from every topic*, let's start with **Part 1: Core Java + OOP + String + Wrapper Classes + JVM Fundamentals**. These are among the most frequently asked questions.

# 1. OOPS Concepts

### Basic

1. What are the four pillars of OOP?
2. Difference between abstraction and encapsulation?
3. Difference between abstraction and interface?
4. Difference between inheritance and composition?
5. What is polymorphism?
6. Compile-time vs runtime polymorphism?
7. Method overloading vs method overriding?
8. Can we overload main() method?
9. Can we override static methods?
10. Can we override private methods?

### Intermediate

11. Why is composition preferred over inheritance?
12. What is the IS-A relationship?
13. What is the HAS-A relationship?
14. What is tight coupling?
15. What is loose coupling?
16. What is association?
17. Difference between association, aggregation and composition?
18. What is covariant return type?
19. Can constructors be inherited?
20. Why can't constructors be overridden?

### Advanced

21. Explain real-life use of polymorphism in your project.
22. How does dynamic method dispatch work internally?
23. What happens if parent and child contain same field name?
24. Why Java doesn't support multiple inheritance through classes?
25. How does JVM resolve overridden methods?

---

# 2. Classes & Objects

### Basic

1. Difference between class and object?
2. What is constructor?
3. Default constructor vs parameterized constructor?
4. Constructor overloading?
5. Can constructor be private?
6. What is copy constructor?
7. Can a constructor call another constructor?
8. this() vs super()?
9. What is object cloning?

### Intermediate

10. Deep copy vs shallow copy?
11. How Cloneable works?
12. Why clone() is protected?
13. Can object be created without new keyword?
14. Ways to create objects in Java?
15. What happens internally when new keyword is used?

### Advanced

16. Explain object creation process from memory perspective.
17. What happens in JVM when object becomes unreachable?
18. Difference between object reference and object itself?

---

# 3. String Class

### Very Frequently Asked

1. Why String is immutable?
2. Benefits of immutability?
3. Difference between String, StringBuilder, StringBuffer?
4. String pool?
5. What is SCP (String Constant Pool)?
6. Difference between heap and string pool?
7. How many objects are created?

```java
String s="Java";
String s2="Java";
```

8. How many objects are created?

```java
String s=new String("Java");
```

9. equals() vs == ?

### Intermediate

10. intern() method?
11. How String pool works internally?
12. Why String is final?
13. Why String is used as HashMap key?
14. Why StringBuilder is faster?
15. Difference between concat() and + operator?

### Advanced

16. How hashCode() is calculated for String?
17. Why String hashCode is cached?
18. Explain String memory optimization in Java 8.
19. Can immutable class be broken?
20. How would you design your own immutable class?

---

# 4. Wrapper Classes

### Basic

1. Why wrapper classes are needed?
2. What is autoboxing?
3. What is unboxing?
4. Difference between int and Integer?
5. Integer cache?

### Intermediate

6. Output?

```java
Integer a=127;
Integer b=127;
System.out.println(a==b);
```

7. Output?

```java
Integer a=128;
Integer b=128;
System.out.println(a==b);
```

8. Why Integer cache range is -128 to 127?
9. Why Integer is immutable?
10. Difference between parseInt() and valueOf()?

### Advanced

11. Memory impact of wrappers?
12. Why wrappers are heavily used in collections?

---

# 5. Access Modifiers

### Basic

1. Difference among public, private, protected, default?
2. Can top-level class be private?
3. Can top-level class be protected?
4. Access levels in same package?
5. Access levels across packages?

### Intermediate

6. Difference between protected and default?
7. Real project use of protected?
8. Can constructor be private?

### Advanced

9. Explain encapsulation using access modifiers.
10. What happens if access level is reduced while overriding?

---

# 6. Static Keyword

### Frequently Asked

1. What is static variable?
2. What is static method?
3. Why main() is static?
4. Can static method access instance variables?
5. Can static method be overridden?
6. Can static block be overloaded?
7. Static block vs instance block?
8. Order of execution?

### Intermediate

9. Static nested class?
10. Use cases of static nested class?
11. Memory allocation of static members?
12. When static block executes?

### Advanced

13. Class loading process?
14. What happens if static block throws exception?
15. How many times static block executes?

---

# 7. Final Keyword

### Basic

1. final variable?
2. final method?
3. final class?
4. Why String is final?

### Intermediate

5. Can final reference object be modified?
6. Difference between final and immutable?
7. Why local variables used in lambda must be effectively final?

### Advanced

8. How JVM optimizes final variables?
9. Can final fields change through reflection?

---

# 8. Abstract Class vs Interface

### Most Important

1. Difference between abstract class and interface?
2. When would you use interface?
3. When would you use abstract class?
4. Can abstract class have constructor?
5. Can abstract class have static methods?
6. Can interface have static methods?
7. Can interface have private methods?

### Java 8 Questions

8. What are default methods?
9. Why default methods introduced?
10. What are static methods in interface?
11. Multiple inheritance issue with default methods?

### Advanced

12. Diamond problem?
13. If two interfaces contain same default method?
14. Interface segregation principle?
15. Marker interfaces examples?

---

# 9. JVM Architecture

### Most Asked for 5+ Years

1. What happens when Java program starts?
2. Explain JVM architecture.
3. What are JVM memory areas?
4. Heap vs Stack?
5. Method Area?
6. Metaspace?
7. Program Counter Register?
8. Native Method Stack?
9. Class Loader Subsystem?
10. Execution Engine?

### Intermediate

11. Parent delegation model?
12. Types of class loaders?
13. Bootstrap ClassLoader?
14. Extension ClassLoader?
15. Application ClassLoader?

### Advanced

16. Explain complete class loading lifecycle.
17. Loading vs Linking vs Initialization?
18. Verification phase?
19. Resolution phase?
20. JVM execution flow?

---

# 10. Memory Management

### Frequently Asked

1. Stack memory vs Heap memory?
2. What causes memory leak in Java?
3. Is Java completely free from memory leaks?
4. What is OutOfMemoryError?
5. Different types of OOM?

### Intermediate

6. Heap generations?
7. Young Generation?
8. Old Generation?
9. Survivor Space?
10. Eden Space?
11. Minor GC?
12. Major GC?
13. Full GC?

### Advanced

14. Explain memory leak from HashMap.
15. Explain memory leak in listener registration.
16. Explain memory leak in ThreadLocal.
17. Heap dump analysis experience?
18. How did you solve memory issue in production?

---

# 11. Garbage Collection

### Basic

1. What is Garbage Collection?
2. How object becomes eligible for GC?
3. Can we force GC?
4. Difference between finalize() and GC?

### Intermediate

5. Reference types?

   * Strong
   * Weak
   * Soft
   * Phantom

6. System.gc() guarantee?

7. How reachability analysis works?

8. What are GC roots?

### Advanced

9. G1 GC?
10. CMS GC?
11. Parallel GC?
12. Serial GC?
13. ZGC?
14. Shenandoah GC?
15. Which GC did your application use and why?

---

# 12. Equals, HashCode and Object Class

### Most Frequently Asked

1. Why override equals()?
2. Why override hashCode()?
3. Contract between equals() and hashCode()?
4. What happens if equals overridden but hashCode not?
5. Difference between == and equals()?

### Intermediate

6. What methods are in Object class?
7. clone()?
8. finalize()?
9. wait(), notify(), notifyAll()?

### Advanced

10. How HashMap uses hashCode and equals internally?
11. Can two unequal objects have same hashCode?
12. Can two equal objects have different hashCodes?

---

# **Part 2**

---

# Java Interview Questions for 5–8 Years Experience

# Part 2: Collections + Generics + Java 8 + Streams + Functional Programming

These topics are asked in almost every Java interview for 5+ years.

---

# 13. Collections Framework

## Basics

1. What is Collection Framework?
2. Difference between Collection and Collections?
3. Difference between Collection and Map?
4. What are the major interfaces in Collection Framework?
5. Explain List, Set and Queue.
6. Which collection allows duplicates?
7. Which collection maintains insertion order?
8. Which collection stores unique values?
9. Which collection provides sorting?
10. Which collection is thread-safe?

---

## List Interface

### ArrayList

1. Difference between ArrayList and LinkedList?
2. How ArrayList grows internally?
3. Default capacity of ArrayList?
4. Growth formula of ArrayList?
5. Difference between size() and capacity()?
6. Why random access is fast in ArrayList?
7. Why insertion in middle is slow?

### Advanced

8. Internal structure of ArrayList.
9. Time complexity of add(), remove(), get().
10. Fail-fast behavior in ArrayList.

---

### LinkedList

11. Internal structure of LinkedList?
12. Why insertion is faster in LinkedList?
13. Why searching is slower?
14. Doubly LinkedList or Singly LinkedList?
15. Time complexity analysis.

### Scenario

16. When would you choose LinkedList over ArrayList?

---

# Set Interface

### HashSet

1. How HashSet works internally?
2. Does HashSet allow duplicates?
3. Why duplicates are not allowed?
4. Which methods are used internally?
5. How HashSet uses HashMap?

### Intermediate

6. Can HashSet contain null?
7. How many nulls can be stored?
8. Time complexity of add()?

### Advanced

9. How collision impacts HashSet?
10. How equality is determined?

---

### LinkedHashSet

11. Difference between HashSet and LinkedHashSet?
12. How insertion order is maintained?

---

### TreeSet

13. Difference between HashSet and TreeSet?
14. How TreeSet maintains sorting?
15. Can TreeSet store null?
16. Internal data structure?

### Advanced

17. TreeSet backed by which Map?
18. Complexity of operations?

---

# Queue Interface

### PriorityQueue

1. What is PriorityQueue?
2. Internal data structure?
3. Is insertion order maintained?
4. How sorting happens?

### Advanced

5. Difference between Queue and PriorityQueue?
6. Heap implementation details?

---

# Map Interface

## HashMap

### Most Asked Topic

1. How HashMap works internally?
2. What is hashing?
3. What is bucket?
4. How key-value pair stored?
5. Why key should be immutable?
6. Can HashMap have null key?
7. Can HashMap have null values?
8. Time complexity of get()?

### Intermediate

9. What happens during put()?
10. How equals() and hashCode() are used?
11. Collision handling in HashMap?
12. What is load factor?
13. Default load factor?
14. Default initial capacity?
15. What is threshold?

### Advanced

16. HashMap internal structure in Java 7?
17. HashMap internal structure in Java 8?
18. What is Treeification?
19. Why Red Black Tree introduced?
20. TREEIFY_THRESHOLD value?
21. UNTREEIFY_THRESHOLD value?
22. Resize process?
23. Rehashing?
24. How HashMap avoids infinite loop issue from Java 7?

### Production Questions

25. How would you optimize a large HashMap?
26. Why immutable keys are recommended?

---

## ConcurrentHashMap

### Most Important

1. Difference between HashMap and ConcurrentHashMap?
2. Why HashMap is not thread-safe?
3. How ConcurrentHashMap works?

### Java 7

4. What is Segment?

### Java 8

5. How synchronization changed?
6. What is CAS?
7. What is bucket-level locking?

### Advanced

8. putIfAbsent()?
9. computeIfAbsent()?
10. Why null keys are not allowed?

---

## Hashtable

1. Difference between Hashtable and HashMap?
2. Why Hashtable is legacy?
3. Synchronization difference?

---

## LinkedHashMap

1. Difference between HashMap and LinkedHashMap?
2. How insertion order maintained?
3. Access-order mode?
4. LRU Cache implementation using LinkedHashMap?

---

## TreeMap

1. Difference between TreeMap and HashMap?
2. Internal structure?
3. Complexity?
4. Null key support?
5. How sorting works?

---

# Collection Utilities

1. Collections.sort() vs Arrays.sort()
2. Collections.synchronizedList()
3. Collections.unmodifiableList()
4. Difference between synchronized and unmodifiable collections.

---

# Iterator

## Basic

1. What is Iterator?
2. Difference between Iterator and Enumeration?
3. Difference between Iterator and ListIterator?

### Advanced

4. What is fail-fast iterator?
5. What is fail-safe iterator?
6. ConcurrentModificationException?
7. How does modCount work?

---

# Comparable and Comparator

## Comparable

1. What is Comparable?
2. compareTo() method?
3. Natural sorting?

## Comparator

4. What is Comparator?
5. compare() method?
6. Custom sorting?

## Frequently Asked

7. Comparable vs Comparator?
8. Multiple sorting strategies?
9. Lambda-based Comparator?

---

# Generics

## Basics

1. What are Generics?
2. Why Generics introduced?
3. Benefits of Generics?
4. Compile-time type safety?

---

## Intermediate

5. What is Type Erasure?
6. Why Generic information removed at runtime?
7. Generic Method?
8. Generic Class?
9. Generic Interface?

---

## Wildcards

10. What is <?> ?
11. What is <? extends T> ?
12. What is <? super T> ?
13. PECS Principle?

### Advanced

14. Difference between extends and super?
15. Producer Extends Consumer Super?
16. Why List<Object> != List<String>?

---

# Java 8 Features

## Java 8 Features List

1. Lambda Expressions
2. Functional Interfaces
3. Streams API
4. Method References
5. Optional
6. Default Methods
7. Static Methods in Interface
8. CompletableFuture
9. Date Time API
10. Nashorn Engine

---

# Functional Interface

### Very Frequently Asked

1. What is Functional Interface?
2. Why only one abstract method?
3. @FunctionalInterface annotation?

### Java Built-in Interfaces

4. Predicate
5. Function
6. Consumer
7. Supplier
8. UnaryOperator
9. BinaryOperator

### Scenario Questions

10. When would you use Predicate?
11. Function vs Consumer?
12. Consumer vs Supplier?

---

# Lambda Expressions

### Basics

1. What is Lambda?
2. Why Lambda introduced?
3. Benefits over anonymous class?

### Intermediate

4. Syntax of Lambda?
5. Variable capture?
6. Effectively final variable?

### Advanced

7. How Lambda works internally?
8. invokedynamic instruction?
9. Lambda memory optimization?

---

# Method References

1. What is Method Reference?
2. Types of Method References?

### Types

3. Static Method Reference
4. Instance Method Reference
5. Constructor Reference

### Advanced

6. Lambda vs Method Reference?

---

# Stream API

## Basics

1. What is Stream?
2. Difference between Stream and Collection?
3. Internal iteration vs external iteration?
4. Lazy evaluation?

---

## Intermediate

5. Intermediate operations?
6. Terminal operations?
7. map() vs flatMap()?
8. filter()?
9. distinct()?
10. sorted()?
11. limit()?
12. skip()?

---

## Advanced Stream Questions

13. How Stream pipeline works?
14. Why Streams are lazy?
15. Can Stream be reused?
16. Why Stream is not a data structure?

---

# map() vs flatMap()

### Most Asked

1. Difference between map and flatMap?
2. Real-life example?
3. Nested collection flattening?

---

# Collectors

1. collect()?
2. Collectors.toList()?
3. groupingBy()?
4. partitioningBy()?
5. counting()?
6. joining()?
7. mapping()?
8. collectingAndThen()?

### Coding Questions

9. Group employees by department.
10. Find highest salary employee.
11. Count employees by department.

---

# Parallel Streams

### Frequently Asked

1. What is Parallel Stream?
2. How parallelStream() works?
3. ForkJoinPool?
4. Advantages?
5. Disadvantages?

### Advanced

6. When should parallel streams be avoided?
7. Thread safety issues?
8. Performance considerations?

---

# Optional

### Basic

1. Why Optional introduced?
2. NullPointerException solution?
3. Optional.of()?
4. Optional.ofNullable()?
5. Optional.empty()?

### Intermediate

6. orElse() vs orElseGet()?
7. map()?
8. flatMap()?
9. filter()?

### Advanced

10. Why Optional should not be used in entity classes?
11. Optional as method parameter?

---

# Date & Time API (Java 8)

### Basic

1. Problems with Date and Calendar?
2. LocalDate?
3. LocalTime?
4. LocalDateTime?
5. ZonedDateTime?

### Intermediate

6. Period vs Duration?
7. Date formatting?
8. Parsing dates?

### Advanced

9. Timezone handling?
10. UTC conversion?

---

# Frequently Asked Coding Questions Using Streams

1. Find duplicate elements.
2. Find first non-repeated character.
3. Find second highest number.
4. Count frequency of elements.
5. Reverse string using Stream.
6. Merge two lists.
7. Convert list to map.
8. Group by department.
9. Find longest string.
10. Find max salary employee.
11. Sort employees by salary.
12. Partition even/odd numbers.
13. Remove duplicates.
14. Find top 3 highest numbers.
15. Find common elements between lists.
16. Count vowels in string.
17. Find occurrence of characters.
18. Convert list of strings to uppercase.
19. Find average salary.
20. Find kth highest salary.

---

# **Part 3**

---

# Java Interview Questions for 5–8 Years Experience

# Part 3: Multithreading, Concurrency, Executor Framework, CompletableFuture

This is one of the most heavily tested areas for **5+ years Java developers**.

---

# 14. Multithreading Fundamentals

## Basics

1. What is a Thread?
2. Process vs Thread?
3. Why multithreading is required?
4. Advantages of multithreading?
5. User thread vs daemon thread?
6. How to create a thread?
7. Thread class vs Runnable interface?
8. Which approach is preferred and why?

### Thread Creation

9. Creating thread using Thread class?
10. Creating thread using Runnable?
11. Creating thread using Lambda?
12. Creating thread using ExecutorService?
13. Creating thread using Callable?

---

# 15. Thread Lifecycle

## Most Asked

1. What are thread states?

```text
NEW
RUNNABLE
BLOCKED
WAITING
TIMED_WAITING
TERMINATED
```

2. Explain each state with example.
3. Difference between RUNNABLE and RUNNING?
4. Difference between WAITING and BLOCKED?
5. Difference between WAITING and TIMED_WAITING?

---

## State Transition Questions

6. What causes BLOCKED state?
7. What causes WAITING state?
8. What causes TIMED_WAITING state?
9. How thread moves from NEW to RUNNABLE?
10. How thread becomes TERMINATED?

---

# 16. Thread Methods

## Frequently Asked

1. start() vs run()?
2. Why should we call start() instead of run()?
3. What happens internally when start() is called?
4. Can start() be called twice?
5. What happens if start() called twice?

---

## sleep()

6. What is Thread.sleep()?
7. Does sleep release lock?
8. Checked exception in sleep?
9. Difference between sleep() and wait()?

---

## join()

10. What is join()?
11. Why join() is used?
12. join(long millis)?

---

## yield()

13. What is yield()?
14. Is yield guaranteed?

---

## interrupt()

15. What is interruption?
16. interrupt() vs interrupted() vs isInterrupted()?
17. How interruption works internally?

---

# 17. Synchronization

## Basics

1. What is synchronization?
2. Why synchronization needed?
3. Race condition?
4. Critical section?
5. Thread interference?

---

## Synchronized Keyword

6. Synchronized method?
7. Synchronized block?
8. Object-level lock?
9. Class-level lock?

---

## Locking Questions

10. How many threads can enter synchronized block?
11. Which object acts as monitor?
12. What is monitor lock?
13. Reentrant lock behavior?

---

## Advanced

14. What happens internally when thread enters synchronized block?
15. How JVM implements synchronization?
16. Lock escalation?
17. Lock optimization in JVM?

---

# 18. Deadlock

## Very Common

1. What is Deadlock?
2. Conditions for Deadlock?

### Coffman Conditions

3. Mutual Exclusion
4. Hold and Wait
5. No Preemption
6. Circular Wait

---

## Advanced

7. How to detect Deadlock?
8. How to avoid Deadlock?
9. Real production Deadlock example?
10. How thread dump helps?

---

# 19. Inter-Thread Communication

## Most Asked

1. wait()?
2. notify()?
3. notifyAll()?

---

## Frequently Asked

4. Why wait(), notify() belong to Object class?
5. Why not Thread class?
6. Difference between sleep() and wait()?
7. Does wait() release lock?
8. Does sleep() release lock?

---

## Advanced

9. Lost notification problem?
10. Spurious wakeup?
11. Why wait() should be inside loop?

---

# 20. Volatile Keyword

## Most Asked

1. What is volatile?
2. Why volatile introduced?
3. Visibility issue?
4. What is cache memory problem?

---

## Intermediate

5. Does volatile guarantee thread safety?
6. Does volatile guarantee atomicity?
7. Volatile vs synchronized?

---

## Advanced

8. Java Memory Model and volatile?
9. Happens-before relationship?
10. Double Checked Locking with volatile?

---

# 21. Java Memory Model (JMM)

## Senior-Level Topic

1. What is JMM?
2. Why JMM introduced?
3. Visibility problem?
4. Reordering problem?

---

## Advanced

5. Happens-before rule?
6. Memory barriers?
7. Instruction reordering?
8. CPU cache effects?
9. Compiler optimizations?

---

# 22. Atomic Classes

## Frequently Asked

1. Why Atomic classes introduced?
2. AtomicInteger?
3. AtomicLong?
4. AtomicBoolean?

---

## Advanced

5. CAS (Compare And Swap)?
6. How AtomicInteger works internally?
7. Difference between AtomicInteger and synchronized?
8. Lock-free programming?

---

## Methods

9. incrementAndGet()
10. getAndIncrement()
11. compareAndSet()
12. lazySet()

---

# 23. Locks Framework

## ReentrantLock

1. Difference between synchronized and ReentrantLock?
2. Why ReentrantLock?
3. Fair lock?
4. Non-fair lock?

---

## Advanced

5. tryLock()?
6. lockInterruptibly()?
7. Condition object?
8. Multiple conditions?

---

## ReadWriteLock

9. What is ReadWriteLock?
10. Read lock?
11. Write lock?

---

## StampedLock

12. What is StampedLock?
13. Optimistic locking?
14. When to use StampedLock?

---

# 24. Executor Framework

## Most Important for 5+ Years

1. Why Executor Framework introduced?
2. Problems with manual thread creation?
3. What is Executor?
4. What is ExecutorService?

---

## Thread Pools

5. What is Thread Pool?
6. Why Thread Pool?
7. Benefits?

---

## Types of Executors

8. FixedThreadPool
9. CachedThreadPool
10. SingleThreadExecutor
11. ScheduledThreadPool

---

## Advanced

12. How ThreadPoolExecutor works?
13. Core Pool Size?
14. Maximum Pool Size?
15. Queue Capacity?
16. Rejection Policy?

---

## Rejection Policies

17. AbortPolicy
18. CallerRunsPolicy
19. DiscardPolicy
20. DiscardOldestPolicy

---

# 25. Callable and Future

## Frequently Asked

1. Difference between Runnable and Callable?
2. Why Callable introduced?
3. Future interface?
4. Future.get()?

---

## Advanced

5. Cancellation?
6. Timeout handling?
7. Future limitations?

---

# 26. CompletableFuture

## Extremely Important (Java 8+)

### Basics

1. Why CompletableFuture introduced?
2. Problems with Future?
3. Async programming?

---

## Creation

4. supplyAsync()
5. runAsync()

---

## Transformation

6. thenApply()
7. thenAccept()
8. thenRun()

---

## Combining

9. thenCompose()
10. thenCombine()

---

## Error Handling

11. exceptionally()
12. handle()
13. whenComplete()

---

## Advanced

14. Difference between thenApply and thenCompose?
15. Async vs non-async methods?
16. Custom Executor with CompletableFuture?
17. Parallel API calls use case?

---

# 27. Fork Join Framework

## Frequently Asked

1. What is ForkJoinPool?
2. Why ForkJoinPool?
3. Work stealing algorithm?

---

## Classes

4. RecursiveTask
5. RecursiveAction

---

## Advanced

6. How work stealing works?
7. Parallel Stream relationship with ForkJoinPool?

---

# 28. Concurrent Collections

## ConcurrentHashMap

1. How ConcurrentHashMap works?
2. Bucket locking?
3. CAS?

---

## CopyOnWriteArrayList

4. What is CopyOnWriteArrayList?
5. Advantages?
6. Disadvantages?

---

## BlockingQueue

7. What is BlockingQueue?
8. Producer Consumer problem?
9. ArrayBlockingQueue?
10. LinkedBlockingQueue?

---

## Concurrent Collections Comparison

11. ConcurrentHashMap vs Hashtable?
12. CopyOnWriteArrayList vs synchronizedList?

---

# 29. Producer Consumer

## Most Asked Scenario

1. Explain Producer Consumer Problem.
2. How to solve using wait/notify?
3. How to solve using BlockingQueue?

---

## Advanced

4. Which approach preferred in production?
5. Why BlockingQueue is better?

---

# 30. Thread Safety

## Frequently Asked

1. What is thread safety?
2. How to make class thread-safe?
3. Immutable class thread-safe?
4. Stateless class thread-safe?

---

## Advanced

5. Thread confinement?
6. ThreadLocal?
7. Why ThreadLocal used?

---

# 31. ThreadLocal

## Important Production Topic

1. What is ThreadLocal?
2. How ThreadLocal works internally?
3. Use cases?

---

## Advanced

4. Memory leak with ThreadLocal?
5. Thread pools and ThreadLocal issue?
6. How to clean ThreadLocal?

---

# 32. Concurrency Design Questions

### Frequently Asked in Product Companies

1. Design a thread-safe Singleton.
2. Design a rate limiter.
3. Design a cache.
4. Design producer-consumer system.
5. Design file processing system using ExecutorService.
6. Design asynchronous notification service.
7. Design parallel API aggregator.

---

# High-Frequency Multithreading Questions (Asked Repeatedly)

1. start() vs run()
2. sleep() vs wait()
3. notify() vs notifyAll()
4. synchronized vs ReentrantLock
5. HashMap vs ConcurrentHashMap
6. volatile vs synchronized
7. Callable vs Runnable
8. Future vs CompletableFuture
9. wait() vs join()
10. AtomicInteger vs synchronized
11. ConcurrentHashMap internal working
12. Deadlock detection and prevention
13. CAS operation
14. Java Memory Model
15. Happens-before relationship
16. ThreadLocal memory leak
17. Producer Consumer implementation
18. ExecutorService lifecycle
19. CompletableFuture chaining
20. ForkJoinPool work stealing

---

# **Part 4**

---

# Java Interview Questions for 5–8 Years Experience

# Part 4: Exception Handling, Serialization, Reflection, Annotations, Design Patterns, SOLID, Immutable Classes, Java 11/17/21

These topics are heavily asked in **L2, L3, Lead Developer, Senior Java Developer** interviews.

---

# 33. Exception Handling

## Basics

1. What is Exception?
2. Difference between Error and Exception?
3. Difference between Checked and Unchecked Exception?
4. RuntimeException hierarchy?
5. Exception hierarchy?

---

## Checked vs Unchecked

6. Why checked exceptions exist?
7. Examples of checked exceptions?
8. Examples of unchecked exceptions?
9. When should you create checked exception?
10. When should you create unchecked exception?

---

## try-catch-finally

11. Can try exist without catch?
12. Can try exist without finally?
13. Can finally exist without catch?
14. Is finally always executed?
15. When finally is not executed?

---

## Advanced

16. What happens if exception occurs in finally?
17. What happens if return statement exists in try and finally?
18. Which return executes?
19. Can finally override return value?

---

## throw vs throws

20. Difference between throw and throws?
21. Why throws keyword used?
22. Multiple exceptions in throws?

---

## Custom Exceptions

23. How to create custom exception?
24. Checked custom exception?
25. Unchecked custom exception?

---

## Java 7+

26. Multi-catch block?
27. Try-with-resources?
28. AutoCloseable?
29. Suppressed exceptions?

---

## Production Questions

30. Global exception handling?
31. Exception handling in Spring Boot?
32. How do you log exceptions?
33. Why should exceptions not be swallowed?

---

# 34. Serialization & Deserialization

## Basics

1. What is Serialization?
2. Why Serialization used?
3. What is Deserialization?
4. Serializable interface?
5. Why Serializable is marker interface?

---

## Frequently Asked

6. serialVersionUID?
7. Why serialVersionUID important?
8. What happens if serialVersionUID changes?

---

## transient Keyword

9. What is transient?
10. Why transient used?
11. Can transient field be serialized?

---

## static and Serialization

12. Are static variables serialized?
13. Why not?

---

## Advanced

14. writeObject()?
15. readObject()?
16. Externalizable?
17. Serializable vs Externalizable?

---

## Security Questions

18. Serialization vulnerabilities?
19. Why serialization discouraged in microservices?
20. Alternatives to serialization?

---

# 35. Reflection API

## Basics

1. What is Reflection?
2. Why Reflection used?
3. How to get Class object?

---

## Frequently Asked

4. Class.forName()?
5. getMethods()?
6. getDeclaredMethods()?
7. getFields()?
8. getConstructors()?

---

## Advanced

9. Can private methods be invoked?
10. setAccessible(true)?
11. Can final field be modified?
12. Reflection performance impact?

---

## Framework Questions

13. How Spring uses Reflection?
14. How Hibernate uses Reflection?
15. How Dependency Injection uses Reflection?

---

# 36. Annotations

## Basics

1. What are annotations?
2. Why annotations introduced?
3. Built-in annotations?

---

## Common Annotations

4. @Override
5. @Deprecated
6. @SuppressWarnings
7. @FunctionalInterface

---

## Meta Annotations

8. @Target
9. @Retention
10. @Inherited
11. @Documented
12. @Repeatable

---

## Advanced

13. RetentionPolicy values?
14. SOURCE vs CLASS vs RUNTIME?
15. How custom annotations are created?

---

## Framework Questions

16. How Spring processes annotations?
17. How @Autowired works internally?
18. How @ComponentScan works?

---

# 37. Immutable Class

## Most Asked

1. What is Immutable Class?
2. Why String is immutable?
3. Benefits of immutability?
4. How to create immutable class?

---

## Rules

5. Make class final?
6. Make fields private final?
7. No setters?
8. Defensive copying?

---

## Advanced

9. Why immutable objects are thread-safe?
10. Why immutable objects are used as HashMap keys?
11. Can immutability be broken through Reflection?

---

## Scenario

12. Design your own immutable Employee class.

---

# 38. Singleton Design Pattern

## Most Important Pattern

1. What is Singleton?
2. Why Singleton used?
3. How to create Singleton?

---

## Different Implementations

4. Eager Initialization
5. Lazy Initialization
6. Thread Safe Singleton
7. Double Checked Locking
8. Bill Pugh Singleton
9. Enum Singleton

---

## Advanced

10. Which Singleton is best?
11. Reflection attack on Singleton?
12. Serialization attack on Singleton?
13. How to prevent Singleton breaking?

---

# 39. Factory Design Pattern

## Frequently Asked

1. What is Factory Pattern?
2. Why Factory Pattern used?
3. Real-world examples?

---

## Advanced

4. Factory vs Constructor?
5. Factory vs Abstract Factory?
6. Spring BeanFactory relation?

---

# 40. Builder Design Pattern

## Most Asked in Modern Java

1. Why Builder Pattern?
2. Problems solved by Builder?
3. Telescoping Constructor Problem?

---

## Advanced

4. Immutable objects with Builder?
5. Lombok Builder?
6. Real-world Builder examples?

---

# 41. Strategy Pattern

## Frequently Asked

1. What is Strategy Pattern?
2. Why use Strategy Pattern?
3. Open Closed Principle relation?

---

## Scenario Questions

4. Payment system design?
5. Notification system design?
6. Sorting strategy example?

---

# 42. Observer Pattern

## Commonly Asked

1. What is Observer Pattern?
2. Publisher Subscriber model?
3. Real-world examples?

---

## Framework Questions

4. Spring Event mechanism?
5. Kafka/Event Driven Architecture relation?

---

# 43. Other Important Design Patterns

## Creational

1. Singleton
2. Factory
3. Abstract Factory
4. Builder
5. Prototype

---

## Structural

6. Adapter
7. Decorator
8. Facade
9. Proxy
10. Bridge

---

## Behavioral

11. Strategy
12. Observer
13. Command
14. State
15. Template Method
16. Chain of Responsibility

---

## Interview Questions

17. Adapter vs Facade?
18. Decorator vs Proxy?
19. Strategy vs State?
20. Template Method vs Strategy?

---

# 44. SOLID Principles

## Extremely Important

### S - Single Responsibility Principle

1. What is SRP?
2. Violation examples?

---

### O - Open Closed Principle

3. What is OCP?
4. How Strategy Pattern supports OCP?

---

### L - Liskov Substitution Principle

5. What is LSP?
6. Real-world violation example?

---

### I - Interface Segregation Principle

7. What is ISP?
8. Fat interface problem?

---

### D - Dependency Inversion Principle

9. What is DIP?
10. Dependency Injection relation?

---

## Advanced

11. Explain SOLID using project examples.
12. Which SOLID principle is most violated?

---

# 45. JVM Advanced Questions

## Class Loading

1. Explain complete class loading process.
2. Loading?
3. Linking?
4. Initialization?

---

## Advanced

5. Verification?
6. Preparation?
7. Resolution?

---

## Runtime Questions

8. JIT Compiler?
9. Interpreter vs JIT?
10. HotSpot JVM?
11. Tiered Compilation?

---

## Memory Questions

12. Metaspace vs PermGen?
13. Memory leaks?
14. Heap Dump?
15. Thread Dump?

---

# 46. Java 11 Interview Questions

## Features

1. What new features introduced in Java 11?
2. New String methods?
3. isBlank()?
4. lines()?
5. repeat()?
6. strip()?

---

## HTTP Client

7. New HttpClient API?
8. Difference from HttpURLConnection?

---

## Collections

9. toArray(IntFunction)?
10. var in lambda?

---

# 47. Java 17 Interview Questions

## Frequently Asked

1. Why Java 17 is LTS?
2. Sealed Classes?
3. Records?
4. Pattern Matching?

---

## Advanced

5. Record vs POJO?
6. Record vs Lombok?
7. Sealed Class use cases?
8. Permitted subclasses?

---

# 48. Java 21 Interview Questions

## Modern Interviews

1. Virtual Threads?
2. Project Loom?
3. Structured Concurrency?
4. Scoped Values?

---

## Frequently Asked

5. Difference between Platform Thread and Virtual Thread?
6. Why Virtual Threads introduced?
7. Millions of threads possible?

---

## Advanced

8. Virtual Thread vs ExecutorService?
9. Performance impact?
10. Migration strategy?

---

# 49. Coding & Scenario-Based Questions (5–8 Years)

## Collections

1. Implement custom HashMap.
2. Implement LRU Cache.
3. Find duplicates efficiently.
4. Top K frequent elements.
5. Group employees by department.

---

## Multithreading

6. Producer Consumer.
7. Deadlock example and solution.
8. Thread-safe Singleton.
9. Rate Limiter.
10. Parallel file processing.

---

## Java 8

11. Find second highest salary.
12. Find nth highest salary.
13. Count word frequency.
14. Find duplicate characters.
15. Flatten nested collections.

---

## Design

16. Design Parking Lot.
17. Design Notification Service.
18. Design Payment Gateway.
19. Design URL Shortener.
20. Design Inventory System.

---

# Top 50 Questions Every 5+ Year Java Developer Must Master

1. HashMap internal working
2. ConcurrentHashMap internal working
3. ArrayList internal structure
4. String immutability
5. equals() and hashCode()
6. Comparable vs Comparator
7. Generics and Type Erasure
8. Lambda internals
9. Stream API internals
10. map vs flatMap
11. Optional
12. JVM architecture
13. Class loading
14. Heap vs Stack
15. Garbage Collection
16. G1 GC
17. Memory leaks
18. Thread lifecycle
19. Synchronization
20. volatile
21. Java Memory Model
22. CAS
23. AtomicInteger
24. ReentrantLock
25. ExecutorService
26. ThreadPoolExecutor
27. Future
28. CompletableFuture
29. ForkJoinPool
30. Concurrent Collections
31. Producer Consumer
32. ThreadLocal
33. Exception handling
34. Custom exceptions
35. Serialization
36. Reflection
37. Annotations
38. Immutable class
39. Singleton
40. Factory Pattern
41. Builder Pattern
42. Strategy Pattern
43. Observer Pattern
44. SOLID Principles
45. Spring IoC concepts
46. Spring Bean lifecycle
47. Spring Boot internals
48. Microservices basics
49. REST API design
50. Production troubleshooting (heap dump, thread dump, GC logs)

The next logical **Part 5** would be a complete **Spring, Spring Boot, Hibernate/JPA, Microservices, Kafka, REST API, Security, Docker, Kubernetes, System Design, and Production Support interview question bank**, which is typically expected for 5–8 year Java backend developers.

# Java Backend Developer (5–8 Years)

# Part 5: Spring, Spring Boot, JPA/Hibernate, REST, Microservices, Kafka, Security, Docker, Kubernetes, System Design

This section covers the majority of questions asked in **service-based companies, product companies, fintech, banking, e-commerce, and startup interviews**.

---

# 50. Spring Framework Fundamentals

## Spring Core

1. What is Spring Framework?
2. Why Spring became popular?
3. Advantages of Spring?
4. What are Spring modules?
5. What is IoC?
6. What is Dependency Injection?
7. Why DI is important?
8. Types of Dependency Injection?

---

## Dependency Injection

9. Constructor Injection vs Setter Injection?
10. Which one is preferred and why?
11. What happens if multiple constructors exist?
12. Why constructor injection is recommended?

---

## Spring Container

13. BeanFactory vs ApplicationContext?
14. What is Spring Container?
15. How beans are managed?

---

## Bean Scope

16. Singleton scope?
17. Prototype scope?
18. Request scope?
19. Session scope?
20. Application scope?

---

## Bean Lifecycle

21. Explain Bean lifecycle.
22. Bean initialization?
23. Bean destruction?
24. @PostConstruct?
25. @PreDestroy?
26. InitializingBean?
27. DisposableBean?

---

## Bean Creation

28. @Component
29. @Service
30. @Repository
31. @Controller
32. Difference among them?

---

# 51. Spring Annotations

## Frequently Asked

1. @Autowired
2. @Qualifier
3. @Primary
4. @Value
5. @Bean
6. @Configuration

---

## Advanced

7. @Lazy
8. @DependsOn
9. @Profile
10. @PropertySource

---

## Scenario

11. What happens when multiple beans of same type exist?
12. How Spring resolves dependency ambiguity?

---

# 52. Spring AOP

## Very Frequently Asked

1. What is AOP?
2. Why AOP?
3. Cross-cutting concerns?

---

## Terminologies

4. Aspect
5. Advice
6. Join Point
7. Pointcut
8. Weaving

---

## Types of Advice

9. Before
10. After
11. AfterReturning
12. AfterThrowing
13. Around

---

## Advanced

14. How Spring AOP works internally?
15. JDK Dynamic Proxy vs CGLIB?
16. Which proxy gets created?

---

## Real World

17. Logging using AOP?
18. Transaction management using AOP?
19. Security using AOP?

---

# 53. Spring Boot

## Basics

1. What is Spring Boot?
2. Why Spring Boot?
3. Difference between Spring and Spring Boot?
4. Advantages of Spring Boot?

---

## Auto Configuration

5. What is auto-configuration?
6. How auto-configuration works?
7. @EnableAutoConfiguration?
8. Spring Factories mechanism?

---

## Starter Dependencies

9. What are starters?
10. Why starters introduced?

---

## Frequently Asked

11. @SpringBootApplication?
12. Components inside @SpringBootApplication?
13. Embedded Tomcat?
14. Can Spring Boot run without Tomcat?

---

## Profiles

15. What are Profiles?
16. application.properties vs application.yml?
17. Environment specific configuration?

---

## Actuator

18. What is Actuator?
19. Health endpoint?
20. Metrics endpoint?

---

## Advanced

21. How Spring Boot starts internally?
22. What happens when SpringApplication.run() executes?

---

# 54. REST API

## Basics

1. What is REST?
2. REST principles?
3. REST constraints?

---

## HTTP Methods

4. GET
5. POST
6. PUT
7. PATCH
8. DELETE

---

## Frequently Asked

9. PUT vs PATCH?
10. POST vs PUT?
11. Idempotent methods?
12. Safe methods?

---

## Status Codes

13. 200
14. 201
15. 204
16. 400
17. 401
18. 403
19. 404
20. 409
21. 500

---

## API Design

22. REST API naming conventions?
23. Versioning strategies?
24. URI design best practices?

---

## Advanced

25. Pagination?
26. Sorting?
27. Filtering?
28. HATEOAS?
29. API Gateway?

---

# 55. Exception Handling in Spring Boot

## Frequently Asked

1. @ExceptionHandler
2. @ControllerAdvice
3. @RestControllerAdvice
4. ResponseEntityExceptionHandler

---

## Advanced

5. Global Exception Handling?
6. Standard error response design?
7. Custom exception hierarchy?

---

# 56. Spring Data JPA

## Basics

1. What is JPA?
2. What is Hibernate?
3. Difference between JPA and Hibernate?
4. Why JPA?

---

## Entity

5. @Entity
6. @Table
7. @Id
8. @GeneratedValue

---

## Relationships

9. OneToOne
10. OneToMany
11. ManyToOne
12. ManyToMany

---

## Frequently Asked

13. mappedBy?
14. Owning side?
15. Cascade types?
16. orphanRemoval?

---

# 57. Hibernate Internals

## Most Asked

1. Hibernate architecture?
2. Session?
3. SessionFactory?
4. EntityManager?
5. Persistence Context?

---

## Entity States

6. Transient
7. Persistent
8. Detached
9. Removed

---

## Fetching

10. Lazy Loading
11. Eager Loading
12. FetchType.LAZY
13. FetchType.EAGER

---

## Advanced

14. N+1 Query Problem?
15. How to solve N+1 issue?
16. Fetch Join?
17. Entity Graph?

---

## Caching

18. First Level Cache?
19. Second Level Cache?
20. Query Cache?

---

# 58. Transactions

## Extremely Important

1. What is transaction?
2. ACID properties?

---

## Spring Transactions

3. @Transactional
4. Propagation types?
5. Isolation levels?

---

## Propagation

6. REQUIRED
7. REQUIRES_NEW
8. SUPPORTS
9. MANDATORY
10. NEVER

---

## Isolation

11. READ_UNCOMMITTED
12. READ_COMMITTED
13. REPEATABLE_READ
14. SERIALIZABLE

---

## Advanced

15. Dirty Read?
16. Non-repeatable Read?
17. Phantom Read?

---

# 59. SQL & Database Questions

## Frequently Asked

1. Primary Key?
2. Foreign Key?
3. Unique Key?
4. Composite Key?

---

## Joins

5. Inner Join
6. Left Join
7. Right Join
8. Full Join
9. Cross Join
10. Self Join

---

## Advanced

11. Index?
12. Clustered Index?
13. Non-clustered Index?
14. Composite Index?

---

## Query Optimization

15. Explain query optimization techniques.
16. Why index not used?
17. How to analyze slow query?

---

# 60. Microservices

## Basics

1. What are Microservices?
2. Monolith vs Microservices?
3. Advantages?
4. Challenges?

---

## Service Communication

5. REST communication?
6. Feign Client?
7. WebClient?

---

## Service Discovery

8. Eureka?
9. Service Registry?
10. Client-side Discovery?

---

## API Gateway

11. Why API Gateway?
12. Spring Cloud Gateway?
13. Gateway benefits?

---

## Distributed Systems

14. Distributed transaction?
15. Saga Pattern?
16. Choreography Saga?
17. Orchestration Saga?

---

# 61. Kafka

## Extremely Important

### Basics

1. What is Kafka?
2. Why Kafka?
3. Kafka architecture?

---

## Components

4. Producer
5. Consumer
6. Broker
7. Topic
8. Partition
9. Offset

---

## Frequently Asked

10. Why partitioning?
11. Consumer Group?
12. Consumer Group Rebalancing?

---

## Advanced

13. How ordering works?
14. How Kafka achieves high throughput?
15. Retention policy?
16. Replication factor?

---

## Reliability

17. ISR?
18. Leader election?
19. Producer ACKS?

---

## Exactly Once

20. At-most-once?
21. At-least-once?
22. Exactly-once delivery?

---

## Production

23. How to handle duplicate messages?
24. Idempotent consumer?
25. Dead Letter Queue?

---

# 62. Spring Security

## Basics

1. What is Spring Security?
2. Authentication vs Authorization?
3. How Spring Security works?

---

## Frequently Asked

4. UserDetailsService?
5. PasswordEncoder?
6. BCrypt?

---

## JWT

7. What is JWT?
8. JWT structure?
9. JWT advantages?

---

## OAuth

10. OAuth2?
11. OAuth vs JWT?
12. Access Token?
13. Refresh Token?

---

## Advanced

14. Security filter chain?
15. How JWT validation works?
16. CSRF?
17. CORS?

---

# 63. Docker

## Basics

1. What is Docker?
2. Why Docker?
3. VM vs Docker?

---

## Frequently Asked

4. Image?
5. Container?
6. Dockerfile?

---

## Commands

7. build
8. run
9. ps
10. logs
11. exec

---

## Advanced

12. Multi-stage build?
13. Docker networking?
14. Docker volumes?

---

# 64. Kubernetes

## Basics

1. What is Kubernetes?
2. Why Kubernetes?
3. Container orchestration?

---

## Components

4. Pod
5. Deployment
6. ReplicaSet
7. Service
8. ConfigMap
9. Secret

---

## Advanced

10. Ingress?
11. Load Balancer?
12. Horizontal Pod Autoscaler?

---

## Production

13. Rolling update?
14. Blue-Green deployment?
15. Canary deployment?

---

# 65. System Design (Backend Developer)

## Frequently Asked

1. Design URL Shortener.
2. Design Notification Service.
3. Design Parking Lot.
4. Design Rate Limiter.
5. Design Food Delivery System.
6. Design E-Commerce Cart.

---

## Scalability

7. Horizontal Scaling?
8. Vertical Scaling?
9. Load Balancer?

---

## Database

10. SQL vs NoSQL?
11. Sharding?
12. Replication?

---

## Caching

13. Redis?
14. Cache Aside Pattern?
15. Write Through?
16. Write Back?

---

## Messaging

17. Kafka vs RabbitMQ?
18. Async communication benefits?

---

# 66. Production Support Questions

## Real Experience Based

1. How do you investigate a slow application?
2. How do you analyze high CPU usage?
3. How do you analyze memory leak?
4. How do you collect thread dump?
5. How do you collect heap dump?

---

## JVM

6. GC log analysis?
7. Full GC issue troubleshooting?
8. OutOfMemoryError troubleshooting?

---

## Database

9. Slow SQL troubleshooting?
10. Deadlock troubleshooting?

---

## Microservices

11. Service-to-service timeout troubleshooting?
12. Circuit breaker use cases?
13. Retry strategy?

---

# Top 100 Must-Prepare Topics for 5–8 Years Java Backend Interviews

* Core Java Internals
* Collections Internals
* HashMap Internals
* ConcurrentHashMap
* JVM Architecture
* Garbage Collection
* Java Memory Model
* Multithreading
* Executor Framework
* CompletableFuture
* Design Patterns
* SOLID Principles
* Spring Core
* Spring Boot
* Spring Security
* REST APIs
* JPA/Hibernate
* Transactions
* SQL Optimization
* Microservices
* Kafka
* Redis
* Docker
* Kubernetes
* System Design
* Production Support
* Performance Tuning

After mastering Parts 1–5, you'll have coverage of roughly **90–95% of Java backend interview questions typically asked for 5–8 years experience** across companies such as TCS, Infosys, Wipro, Accenture, Cognizant, Capgemini, IBM, Deloitte, HCL, LTIMindtree, as well as many product companies.

A useful next step would be **Part 6: 300+ scenario-based, tricky, output-based, and real production interview questions with answers**, because those are what usually differentiate senior candidates from mid-level candidates.
