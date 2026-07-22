# Phase 0 — Prerequisites

**Duration:** 1–2 weeks | **Goal:** Strong base before Java 8 advanced topics

[← Back to Index](README.md) | **Next:** [Phase 1 — Language Foundations →](01-language-foundations.md)

---

## What You Will Learn

- Core Java concepts that Java 8 builds on top of
- Why each topic matters for lambdas, streams, and async code
- How to spot gaps in your knowledge before moving forward

---

## Theory (Easy to Remember)

### 1. Object-Oriented Programming (OOP)

- **Class** = blueprint; **object** = real instance built from that blueprint.
- **Inheritance** = child class gets parent's fields and methods (`extends`).
- **Polymorphism** = same method call, different behavior based on actual object type.
- **Interface** = contract (what methods must exist); **abstract class** = partial implementation + contract.
- **`final`** on class = cannot extend; on method = cannot override; on variable = cannot reassign.
- **Why Java 8 needs this:** A lambda is just a short way to implement an interface's single method.

**Memory trick:** *"Lambda = tiny class that implements one interface method."*

---

### 2. Generics

- Generics let you write type-safe code: `List<String>` instead of raw `List`.
- **`? extends T`** = read-only (producer) — you can get `T` but not add (mostly).
- **`? super T`** = write-only (consumer) — you can add `T` but get only `Object`.
- **Type erasure** = at runtime, `List<String>` becomes just `List` — generics exist only at compile time.
- **Why Java 8 needs this:** `Stream<T>`, `Collector<T,A,R>`, and `Function<T,R>` all depend on generics.

**Memory trick:** *"PECS — Producer Extends, Consumer Super."*

---

### 3. Collections Framework

| Collection | Ordered?      | Duplicates? | Key Use            |
|------------|---------------|-------------|--------------------|
| `List`     | Yes           | Yes         | Index-based access |
| `Set`      | Depends       | No          | Unique elements    |
| `Map`      | Keys: depends | Keys: no    | Key-value lookup   |

- **`Iterator`** = walk elements one by one.
- **`Comparable`** = natural order (`compareTo` inside the class).
- **`Comparator`** = external order (`Comparator.comparing` in Java 8).
- **Why Java 8 needs this:** Streams are created from collections (`list.stream()`).

---

### 4. Exceptions

- **Checked** = compiler forces you to handle (`IOException`).
- **Unchecked** = runtime (`NullPointerException`, `IllegalArgumentException`).
- **try-with-resources** (Java 7) = auto-close (`try (BufferedReader br = ...) {}`).
- **Why Java 8 needs this:** Stream pipelines throw unchecked exceptions; `Optional` is an alternative to null, not a replacement for all exceptions.

---

### 5. Multithreading Basics

- **`Thread`** = separate execution path.
- **`Runnable`** = task with no return value (lambda target in Java 8).
- **`synchronized`** = only one thread enters the block at a time.
- **`volatile`** = visibility guarantee (one thread's write is seen by others).
- **Why Java 8 needs this:** Parallel streams and `CompletableFuture` run work on multiple threads.

---

### 6. JVM Memory (Intro)

- **Stack** = method calls, local variables (per thread).
- **Heap** = objects shared across threads.
- **Happens-before** = if action A happens-before B, then B sees A's results.
- **Why Java 8 needs this:** Parallel streams are unsafe if you modify shared mutable state.

---

## Examples

### Generics — PECS in action

```java
// Producer: we only READ from src
public void copy(List<? extends Number> src, List<? super Integer> dest) {
    for (Number n : src) {
        dest.add(n.intValue()); // OK: dest accepts Integer
    }
}
```

### Comparator before Java 8 vs after

```java
// Old way
Collections.sort(names, new Comparator<String>() {
    public int compare(String a, String b) {
        return a.length() - b.length();
    }
});

// Java 8 way (preview for Phase 1)
names.sort((a, b) -> a.length() - b.length());
names.sort(Comparator.comparing(String::length));
```

### Thread safety problem (why streams need care)

```java
List<Integer> list = new ArrayList<>();
IntStream.range(0, 1000).parallel().forEach(list::add); // BUG! Not thread-safe
// Correct: use collect(Collectors.toList()) instead
```

---

## Practice Exercises

| # | Exercise                                                                                          | Difficulty | Hint                                           |
|---|---------------------------------------------------------------------------------------------------|------------|------------------------------------------------|
| 1 | Create a `List<Employee>` and sort by salary using `Comparator` (no lambda yet)                   | Easy       | `Collections.sort` + anonymous class           |
| 2 | Write a method `findMax(List<Integer>)` using a loop                                              | Easy       | Track max in a variable                        |
| 3 | Implement `groupByDepartment(List<Employee>)` returning `Map<String, List<Employee>>` using loops | Medium     | `Map.computeIfAbsent` or manual put            |
| 4 | Explain why `List<Cat>` is NOT a `List<Animal>` (even though Cat extends Animal)                  | Medium     | Think type erasure + add operation             |
| 5 | Write a thread-safe counter using `synchronized`                                                  | Medium     | Increment in synchronized block                |
| 6 | Convert `List<String>` to uppercase using a `for` loop, then prepare to refactor in Phase 2       | Easy       | `toUpperCase()` per element                    |
| 7 | Use try-with-resources to read a file line by line                                                | Medium     | `Files.newBufferedReader` or `BufferedReader`  |
| 8 | Draw (on paper) what happens when two threads modify the same `HashMap` without sync              | Hard       | ConcurrentModificationException / lost updates |

---

## Interview Q&A (5–8 Years Experience)

### Q1. Why is `List<Dog>` not a subtype of `List<Animal>`?

**Answer:** Because of **invariance** due to the `add` operation. If it were allowed, you could write:
```java
List<Animal> animals = new ArrayList<Dog>(); // hypothetical
animals.add(new Cat()); // adds Cat to a List<Dog> — type system broken
```
Generics are compile-time only (type erasure), but the language enforces this rule to keep the type system sound. Use `? extends Animal` for read-only and `? super Dog` for write-only scenarios (PECS).

---

### Q2. What is the difference between `Comparable` and `Comparator`?

**Answer:**
- **`Comparable`** = intrinsic ordering (`class Employee implements Comparable<Employee>`). One natural order per class.
- **`Comparator`** = extrinsic ordering. Multiple sort strategies without changing the class (`Comparator.comparing(Employee::getSalary)`).
- In production, prefer `Comparator` for flexibility. `Comparable` is fine when there is one obvious natural order (e.g., `LocalDate`).

---

### Q3. Explain happens-before in simple terms.

**Answer:** If action A **happens-before** B, then everything A did (writes to memory) is **visible** to B. Common sources:
- `synchronized` unlock → lock on same monitor
- `volatile` write → read
- `Thread.start()` → actions in started thread
- `Thread.join()` → actions after join returns

Without happens-before, CPUs and JIT can reorder instructions — you may see stale values in multi-threaded code.

---

### Q4. When would you use `HashMap` vs `ConcurrentHashMap`?

**Answer:**
- **`HashMap`** = single-threaded or external synchronization. Not thread-safe.
- **`ConcurrentHashMap`** = high-concurrency reads/writes without locking the entire map. Uses segment/node-level locking (Java 8: CAS + synchronized bins).
- Use `ConcurrentHashMap` when multiple threads read/write. Never use `Collections.synchronizedMap` for high contention — it locks the whole map.

---

### Q5. What is type erasure and what problems does it cause?

**Answer:** At runtime, `List<String>` becomes raw `List`. Generic type parameters are erased. Problems:
- Cannot do `new T()` or `T[]` directly
- Cannot use `instanceof` on generic types
- Overloads differing only by type parameter don't work after erasure

Workarounds: `Class<T>` token, factories, or libraries like Guava `TypeToken`.

---

### Q6. Checked vs unchecked exceptions — when to use which?

**Answer:**
- **Checked** = recoverable, caller must handle (IO, SQL). Use when the caller can reasonably recover.
- **Unchecked** = programming bugs or unrecoverable (`IllegalArgumentException`, `NPE`). Use for API validation and invariant violations.

Modern trend (Spring, many libraries): prefer unchecked for cleaner APIs. Java 8 streams only throw unchecked — another reason to understand the difference.

---

### Q7. What is the difference between `==` and `equals()`?

**Answer:**
- **`==`** = reference equality (same object in memory) for objects; value equality for primitives.
- **`equals()`** = logical equality (overridden per class). Always override `hashCode()` when overriding `equals()`.

For `Integer` caching (-128 to 127), `==` may appear to work — never rely on it. Use `equals()` or unbox carefully.

---

### Q8. How does `HashMap` work internally (high level)?

**Answer:** Array of buckets. `hashCode()` → bucket index. Collision handling: linked list (Java 7) or tree (Java 8 when bucket size > 8). `get` is O(1) average; O(log n) worst case per bucket with tree. Resizes when load factor exceeds threshold (default 0.75). Keys should have stable `hashCode` and consistent `equals`.

---

## Self-Check Before Phase 1

- [ ] I can write `Comparator` without looking up syntax
- [ ] I can explain PECS with an example
- [ ] I know why parallel `forEach` on `ArrayList` with `add` is wrong
- [ ] I can use try-with-resources confidently
- [ ] I understand `synchronized` vs `volatile` at a basic level

**Ready?** → [Phase 1 — Language Foundations](01-language-foundations.md)
