# 🚀 Core Java Interview Questions (5+ Years Experience)

# 📌 1. OOPs & Core Fundamentals

---

## 🔹 OOPs Concepts

* What are the 4 pillars of OOPs?
* Difference between abstraction and encapsulation?
* Difference between method overloading and overriding?
* Can we override static methods?
* Why Java does not support multiple inheritance with classes?
* Composition vs inheritance?
* IS-A vs HAS-A relationship?
* Runtime polymorphism example?

---

## 🔹 Class/Object

* What happens internally when object is created?
* Difference between stack and heap memory?
* What is constructor chaining?
* Why constructors cannot be final/static?
* Can constructor be private?

---

## 🔹 final/static/this/super

* Difference between final, finally, finalize?
* Why String is final?
* Can final object be modified?
* Difference between static and instance members?
* Why main method is static?
* Use cases of static block?
* Difference between this() and super()?

---

# 📌 2. String Internals

🔥 EXTREMELY IMPORTANT

---

* Why String is immutable?
* What is String Constant Pool?
* Difference between:

```java id="63g8v7"
String s = "abc";
String s = new String("abc");
```

* How many objects created in SCP?
* Difference between StringBuilder and StringBuffer?
* Why StringBuilder faster?
* What happens internally in string concatenation?
* Why String is used as HashMap key?
* Interning in Java?
* How equals() works for String?

---

# 📌 3. equals() & hashCode()

🔥 MUST PREPARE

---

* Contract of equals() and hashCode()?
* Why both should be overridden together?
* What happens if only equals() overridden?
* What happens if only hashCode() overridden?
* Why HashMap depends on hashCode()?
* Difference between == and equals()?
* IdentityHashMap vs HashMap?

---

# 📌 4. Collections Framework

🔥 MOST IMPORTANT AREA

---

# 🔹 ArrayList

* Internal working of ArrayList?
* Capacity growth formula?
* Difference between ArrayList and LinkedList?
* Random access complexity?
* Why ArrayList not synchronized?

---

# 🔹 LinkedList

* Internal structure?
* Why insertion faster?
* Why searching slower?

---

# 🔹 Vector

* Difference between Vector and ArrayList?
* Why Vector legacy?

---

# 🔹 CopyOnWriteArrayList

🔥 Advanced

* Internal working?
* Why read-heavy use cases?
* Why modification expensive?

---

# 📌 5. HashMap Deep Dive

🔥 VERY HIGH PRIORITY

---

* Internal working of HashMap?
* What is bucket?
* Collision handling?
* What is load factor?
* What is rehashing?
* Default capacity?
* Why capacity is power of 2?
* Treeification in Java 8?
* Difference between Java 7 and Java 8 HashMap?
* How put() works internally?
* How get() works internally?
* Why hashCode() important?
* Can HashMap have null key?
* Why HashMap not thread-safe?
* Infinite loop issue in Java 7 HashMap?
* Comparable usage in treeification?

---

# 📌 6. ConcurrentHashMap

🔥 MUST FOR 5+ YEARS

---

* Difference between HashMap and ConcurrentHashMap?
* How ConcurrentHashMap works internally?
* Segment locking in Java 7?
* CAS in Java 8?
* Why ConcurrentHashMap faster than Hashtable?
* Can ConcurrentHashMap have null key/value?
* Difference between synchronizedMap and ConcurrentHashMap?

---

# 📌 7. Set Implementations

---

* Difference between HashSet and TreeSet?
* Internal working of HashSet?
* Why Set removes duplicates?
* LinkedHashSet use case?
* TreeSet sorting mechanism?

---

# 📌 8. TreeMap & Sorting

---

* Internal working of TreeMap?
* Red-Black Tree?
* Comparator vs Comparable?
* What happens if compareTo inconsistent?

---

# 📌 9. Iterator Internals

🔥 IMPORTANT

---

* Iterator vs ListIterator?
* Fail-fast vs fail-safe?
* What is ConcurrentModificationException?
* Why fail-fast iterator?
* How CopyOnWriteArrayList avoids CME?

---

# 📌 10. Java 8 Features

🔥 VERY HIGH PRIORITY

---

# 🔹 Lambda

* What is lambda?
* Why lambda introduced?
* Functional programming?
* Internal working of lambda?
* What is invokedynamic?
* Effectively final variable?

---

# 🔹 Functional Interface

* What is functional interface?
* Why @FunctionalInterface?
* Can it have multiple methods?
* Built-in functional interfaces?
* Predicate vs Function vs Consumer vs Supplier?

---

# 🔹 Method Reference

* Types of method references?
* Difference between lambda and method reference?

---

# 🔹 Stream API

🔥 MOST ASKED

* What is Stream API?
* Why streams introduced?
* Internal working of streams?
* Lazy evaluation?
* Difference between intermediate and terminal operations?
* map vs flatMap?
* reduce() working?
* Why streams single-use?
* Parallel stream?
* When not to use parallel stream?
* Difference between findFirst and findAny?
* Why streams not thread-safe?
* groupingBy vs partitioningBy?
* Collectors deep dive?
* Primitive streams?
* Stateful vs stateless operations?

---

# 🔹 Optional

* Why Optional introduced?
* Optional best practices?
* Why Optional.get() risky?

---

# 🔹 Date & Time API

* Difference between Date and LocalDate?
* Period vs Duration?
* ZonedDateTime use case?

---

# 📌 11. Exception Handling

---

* Checked vs unchecked exception?
* Difference between throw and throws?
* finally block execution cases?
* Can finally block be skipped?
* try-with-resources?
* Suppressed exceptions?
* Custom exception creation?

---

# 📌 12. Multithreading & Concurrency

🔥 CRITICAL FOR 5+ YEARS

---

# 🔹 Basics

* Thread lifecycle?
* Runnable vs Callable?
* wait vs sleep?
* wait vs notify vs notifyAll?
* join() use case?

---

# 🔹 Synchronization

* synchronized method vs block?
* Object-level lock vs class-level lock?
* Can constructor be synchronized?
* What is intrinsic lock?

---

# 🔹 volatile

* What problem volatile solves?
* Visibility vs atomicity?
* Happens-before relationship?

---

# 🔹 Atomic Classes

* What is CAS?
* How AtomicInteger works internally?

---

# 🔹 Executor Framework

🔥 MUST KNOW

* Why Executor framework introduced?
* ThreadPoolExecutor internals?
* Fixed vs cached thread pool?
* Callable vs Runnable?
* Future vs CompletableFuture?

---

# 🔹 Locks

* ReentrantLock advantages?
* synchronized vs ReentrantLock?
* ReadWriteLock use case?

---

# 🔹 Deadlock

🔥 Mandatory

* What is deadlock?
* Deadlock prevention?
* Deadlock detection?

---

# 🔹 Producer Consumer

* How implement using wait/notify?
* BlockingQueue advantages?

---

# 📌 13. CompletableFuture

🔥 Advanced topic

---

* Why CompletableFuture?
* supplyAsync vs runAsync?
* thenApply vs thenCompose?
* Exception handling?
* Combining futures?

---

# 📌 14. JVM Internals

🔥 HIGH PRIORITY

---

* JVM architecture?
* Heap vs Stack?
* Metaspace?
* Class loading process?
* Parent delegation model?
* JIT compiler?
* Escape analysis?

---

# 📌 15. Garbage Collection

🔥 MUST PREPARE

---

* Minor GC vs Major GC?
* Young generation vs Old generation?
* G1GC?
* Stop-the-world event?
* Memory leak in Java?
* OutOfMemoryError types?
* How to analyze heap dump?

---

# 📌 16. Serialization

---

* Serializable vs Externalizable?
* serialVersionUID?
* transient keyword?
* Singleton serialization issue?

---

# 📌 17. Generics

🔥 VERY IMPORTANT

---

* Why generics introduced?
* Type erasure?
* Wildcards?
* PECS principle?
* <? extends T> vs <? super T>


---

# 📌 18. Reflection API

---

* Reflection use cases?
* Performance impact?
* Dynamic class loading?

---

# 📌 19. Annotations

---

* Built-in annotations?
* Custom annotations?
* Retention policies?

---

# 📌 20. Design Patterns

🔥 5+ YEARS MUST KNOW

---

# 🔹 Creational

* Singleton
* Factory
* Builder

# 🔹 Structural

* Adapter
* Proxy
* Decorator

# 🔹 Behavioral

* Strategy
* Observer
* Template Method

---

# 📌 21. Real Scenario Questions

🔥 VERY IMPORTANT

---

* Why String immutable?
* Why HashMap not thread-safe?
* Why ConcurrentHashMap better?
* How to make class immutable?
* How to design thread-safe singleton?
* How to avoid deadlock?
* Why streams are lazy?
* Why fail-fast iterator?
* Why Optional introduced?
* How to optimize memory?

---

# 📌 22. Coding + Practical Questions

---

* Frequency map problems
* LRU cache
* Producer-consumer
* Thread-safe cache
* Stream grouping problems
* Custom collector
* Parallel stream bug fixing

---
