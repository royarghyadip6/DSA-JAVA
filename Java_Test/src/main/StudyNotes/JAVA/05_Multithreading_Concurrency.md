# Easy Level

### 1. What is a Thread?

A thread is the smallest unit of execution within a process.

```text
Process
 ├── Thread-1
 ├── Thread-2
 └── Thread-3
```

---

### 2. Difference between Process and Thread?

| Process                    | Thread                |
| -------------------------- | --------------------- |
| Independent execution unit | Part of a process     |
| Own memory                 | Shares process memory |
| Heavyweight                | Lightweight           |
| Expensive creation         | Cheap creation        |

---

### 3. How can you create a thread?

**Method 1: Extend Thread**

```java
class MyThread extends Thread {
    public void run() {
        System.out.println("Running");
    }
}
```

**Method 2: Implement Runnable (Preferred)**

```java
class MyTask implements Runnable {
    public void run() {
        System.out.println("Running");
    }
}
```

---

### 4. Difference between start() and run()?

```java
thread.start();
```

Creates a new thread.

```java
thread.run();
```

Normal method call.

---

### 5. What are Thread States?

```text
NEW
RUNNABLE
BLOCKED
WAITING
TIMED_WAITING
TERMINATED
```

---

### 6. What is Thread Scheduler?

JVM component that decides which thread executes next.

---

### 7. What is sleep()?

Pauses current thread.

```java
Thread.sleep(1000);
```

Thread sleeps for 1 second.

---

### 8. Difference between sleep() and wait()?

| sleep()              | wait()         |
| -------------------- | -------------- |
| Thread class         | Object class   |
| Doesn't release lock | Releases lock  |
| Fixed duration       | Until notified |

---

### 9. What is join()?

Waits for another thread to finish.

```java
thread.join();
```

---

### 10. What is daemon thread?

Background thread.

Examples:

* Garbage Collector
* JVM housekeeping threads

```java
thread.setDaemon(true);
```

---

# Intermediate Level

### 11. What is Race Condition?

Multiple threads modifying shared data simultaneously.

```java
count++;
```

Not thread-safe.

---

### 12. What is Synchronization?

Prevents multiple threads from accessing critical section simultaneously.

```java
synchronized void increment() {
    count++;
}
```

---

### 13. What is Critical Section?

Code accessing shared resources.

```java
count++;
```

---

### 14. What is Monitor?

Every Java object has an intrinsic monitor lock.

```java
synchronized(obj) {
}
```

---

### 15. What is Mutual Exclusion?

Only one thread enters critical section at a time.

---

### 16. What is Deadlock?

Thread-A waits for Thread-B.

Thread-B waits for Thread-A.

Neither proceeds.

```text
Thread A → Lock1 → waiting Lock2
Thread B → Lock2 → waiting Lock1
```

---

### 17. How to avoid Deadlock?

* Consistent lock ordering
* Lock timeout
* Reduce nested locking

---

### 18. What is Volatile?

Guarantees visibility.

```java
volatile boolean running;
```

Changes become visible immediately.

---

### 19. Does volatile provide thread safety?

No.

Wrong:

```java
volatile int count;
count++;
```

Still not atomic.

---

### 20. Difference between synchronized and volatile?

| synchronized           | volatile        |
| ---------------------- | --------------- |
| Visibility + Atomicity | Visibility only |
| Locking                | No locking      |
| Slower                 | Faster          |

---

### 21. What is Atomic Operation?

Operation executed as one indivisible unit.

Example:

```java
AtomicInteger counter =
    new AtomicInteger();

counter.incrementAndGet();
```

---

### 22. What is CAS (Compare-And-Swap)?

Lock-free mechanism used internally by atomic classes.

```java
compare(expected,current)
```

If expected matches current, update succeeds.

---

### 23. What are Atomic Classes?

```java
AtomicInteger
AtomicLong
AtomicBoolean
AtomicReference
```

---

### 24. Difference between synchronized block and synchronized method?

Method:

```java
synchronized void test() {
}
```

Block:

```java
void test() {
    synchronized(this) {
    }
}
```

Block provides finer control.

---

### 25. What is ThreadLocal?

Provides thread-specific variables.

```java
ThreadLocal<String> user =
        new ThreadLocal<>();
```

Each thread gets its own copy.

---

# Advanced Level

### 26. What is Java Memory Model (JMM)?

Defines how threads interact through memory.

Key concepts:

* Visibility
* Ordering
* Atomicity

---

### 27. What is Happens-Before Relationship?

Guarantees visibility between threads.

Example:

```java
Thread.start()
```

Establishes happens-before relationship.

---

### 28. Why is count++ not atomic?

Internally:

```text
Read count
Increment
Write count
```

Three operations.

---

### 29. What is ReentrantLock?

Explicit lock implementation.

```java
Lock lock =
    new ReentrantLock();

lock.lock();

try {
}
finally {
    lock.unlock();
}
```

Advantages:

* Fair lock
* Try lock
* Interruptible lock

---

### 30. Difference between synchronized and ReentrantLock?

| synchronized | ReentrantLock    |
| ------------ | ---------------- |
| JVM managed  | API managed      |
| No timeout   | tryLock()        |
| No fairness  | Fairness support |
| Simpler      | More flexible    |

---

### 31. What is ReadWriteLock?

Multiple readers allowed.

Single writer allowed.

```java
ReadWriteLock lock =
    new ReentrantReadWriteLock();
```

Useful for read-heavy systems.

---

### 32. What is StampedLock?

Introduced in Java 8.

Supports:

```text
Read Lock
Write Lock
Optimistic Read
```

Better performance for read-heavy workloads.

---

### 33. Difference between wait(), notify(), notifyAll()?

```java
wait();
```

Thread waits.

```java
notify();
```

Wakes one waiting thread.

```java
notifyAll();
```

Wakes all waiting threads.

---

### 34. Why should wait() be used inside a loop?

Wrong:

```java
if(queue.isEmpty())
    wait();
```

Correct:

```java
while(queue.isEmpty())
    wait();
```

Handles spurious wakeups.

---

### 35. What is Producer-Consumer Problem?

One thread produces data.

Another consumes data.

Common solution:

```java
BlockingQueue
```

---

### 36. What is BlockingQueue?

Thread-safe queue.

Examples:

```java
ArrayBlockingQueue
LinkedBlockingQueue
PriorityBlockingQueue
```

---

### 37. What is ConcurrentHashMap?

Thread-safe HashMap.

```java
ConcurrentHashMap<String,String>
```

Allows concurrent access.

---

### 38. Difference between HashMap and ConcurrentHashMap?

| HashMap            | ConcurrentHashMap          |
| ------------------ | -------------------------- |
| Not thread-safe    | Thread-safe                |
| Fast single thread | Better concurrency         |
| Fail-fast iterator | Weakly consistent iterator |

---

### 39. Why not use Hashtable?

Hashtable locks entire table.

```java
ConcurrentHashMap
```

offers much better scalability.

---

### 40. What is CopyOnWriteArrayList?

Every write creates new copy.

Best for:

* Many reads
* Few writes

---

# Expert / Architect Level

### 41. What is Executor Framework?

Manages thread creation and lifecycle.

```java
ExecutorService executor =
    Executors.newFixedThreadPool(5);
```

---

### 42. Why is creating threads manually bad?

Problems:

* Expensive
* Resource leakage
* Poor scalability

Executor framework solves these.

---

### 43. Types of Thread Pools?

```java
newFixedThreadPool()
newCachedThreadPool()
newSingleThreadExecutor()
newScheduledThreadPool()
```

---

### 44. Difference between submit() and execute()?

**execute()**

```java
executor.execute(task);
```

No return value.

**submit()**

```java
Future<?> future =
        executor.submit(task);
```

Returns Future.

---

### 45. What is Future?

Represents asynchronous result.

```java
Future<String> future;
```

---

### 46. Limitations of Future?

Cannot:

* Chain operations
* Combine tasks
* Handle async pipelines easily

Solved by CompletableFuture.

---

### 47. What is CompletableFuture?

Java 8 asynchronous programming API.

```java
CompletableFuture
    .supplyAsync(...)
    .thenApply(...)
    .thenAccept(...);
```

---

### 48. Difference between Future and CompletableFuture?

| Future        | CompletableFuture |
| ------------- | ----------------- |
| Blocking      | Non-blocking      |
| Simple result | Async pipeline    |
| No chaining   | Supports chaining |

---

### 49. What is ForkJoinPool?

Work-stealing thread pool.

```java
ForkJoinPool
```

Used internally by:

```java
parallelStream()
CompletableFuture
```

---

### 50. Explain ConcurrentHashMap internals (Java 8).

Very common 5+ years question.

Java 7:

```text
Segment based locking
```

Java 8:

```text
CAS
+
synchronized
+
Node buckets
+
Tree bins
```

Features:

* Lock-free reads
* Fine-grained locking
* Better scalability
* Red-Black Tree after collisions exceed threshold

---

# Most Asked Real Interview Questions (5+ Years)

1. Why is `volatile` not enough for `count++`?
2. Explain Java Memory Model.
3. Difference between `synchronized`, `ReentrantLock`, and `ReadWriteLock`.
4. Explain `ConcurrentHashMap` internals in Java 8.
5. Explain CAS and AtomicInteger.
6. Difference between `wait()` and `sleep()`.
7. How does `CompletableFuture` work?
8. How would you design a thread-safe cache?
9. What causes deadlock and how do you detect it?
10. Explain thread pools and how to size them for production systems.
