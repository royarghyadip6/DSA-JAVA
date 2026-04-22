## 🔥 1. Lambda Expressions (Core Foundation)

* Syntax: `(params) -> expression`
* Functional style coding
* Replacing anonymous classes

👉 Must Know:

* Effectively final variables
* Difference vs anonymous class

---

## 🔥🔥 2. Functional Interfaces (Very Important)

* Interface with **one abstract method**

### Built-in Functional Interfaces:

* `Predicate<T>` → boolean condition
* `Function<T, R>` → input → output
* `Consumer<T>` → takes input, no return
* `Supplier<T>` → returns output

👉 Must Know:

* `@FunctionalInterface`
* Custom functional interfaces

---

## 🔥🔥🔥 3. Streams API (MOST IMPORTANT)

### 🔹 Stream Creation

* From collections, arrays, `Stream.of()`

### 🔹 Intermediate Operations (lazy)

* `filter()`
* `map()`
* `flatMap()`
* `sorted()`
* `distinct()`

### 🔹 Terminal Operations

* `forEach()`
* `collect()`
* `reduce()`
* `count()`
* `anyMatch()`, `allMatch()`

---

### 🔹 Collectors (VERY IMPORTANT)

* `Collectors.toList()`
* `groupingBy()`
* `partitioningBy()`
* `joining()`

---

### 🔹 Parallel Streams

* `parallelStream()`
* When to use / not use

👉 Interview Focus:

* Internal iteration vs external
* Lazy evaluation
* Performance considerations

---

## 🔥 4. Method References

Shortcut for lambda

Types:

* `Class::staticMethod`
* `object::instanceMethod`
* `Class::new` (constructor reference)

---

## 🔥 5. Optional Class (Null Handling)

* `Optional.of()`
* `Optional.ofNullable()`
* `Optional.empty()`

### Important Methods:

* `isPresent()`
* `ifPresent()`
* `orElse()`
* `orElseGet()`
* `orElseThrow()`
* `map()`, `flatMap()`

👉 Must Know:

* Difference: `orElse()` vs `orElseGet()`

---

## 🔥 6. Default & Static Methods in Interface

* `default` methods (with implementation)
* `static` methods

👉 Interview Focus:

* Multiple inheritance problem resolution

---

## 🔥 7. Date & Time API (java.time)

* `LocalDate`
* `LocalTime`
* `LocalDateTime`
* `ZonedDateTime`

👉 Important:

* Immutability
* Thread-safe vs old `Date`

---

## 🔥 8. Nashorn JavaScript Engine (Less Important)

* Execute JS in Java
* Mostly deprecated now → low priority

---

## 🔥 9. Spliterator (Advanced, Optional)

* Used internally in Streams
* Parallel processing support

👉 Only basic idea is enough

---

# ⚡ Java 9+ (Quick Add-ons for Interviews)

## 🔹 Java 9

* `List.of()`, `Set.of()`, `Map.of()`
* Private methods in interfaces

## 🔹 Java 10

* `var` keyword (local variable type inference)

## 🔹 Java 11

* String methods:

    * `isBlank()`
    * `lines()`
    * `strip()`

---

# 🎯 What Interviewers Focus MOST On

👉 80% questions come from:

* Streams API
* Functional Interfaces
* Lambda Expressions
* Optional

---

# 🧠 Pro Tip (Important)

Don’t just learn syntax—be ready to:

* Convert **for-loop → stream**
* Write **real problems using streams**
* Explain **internal working**

---
