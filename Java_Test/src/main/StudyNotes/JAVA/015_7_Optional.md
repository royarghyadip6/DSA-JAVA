# 15.7 Optional

## Optional

### Basic

---

# 1. Why Optional introduced?

<details>
<summary>Show Answer</summary>

**Answer:**

`Optional` was introduced in **Java 8** to provide a **type-safe container** for values that may be absent—making **null handling explicit** in APIs instead of relying on `null` references.

### Problems Before Optional

```java
// Null hidden in return type — caller may forget check
public User findUser(Long id) {
    return userMap.get(id);  // might return null
}

User user = findUser(1L);
user.getName();  // NullPointerException if not found!
```

### What Optional Solves

| Problem | Optional Solution |
|---------|-------------------|
| Hidden null returns | Return type signals "may be absent" |
| Null checks everywhere | Functional chain: map, filter, orElse |
| NPE at runtime | Explicit handling forced at compile/design level |
| Unclear API contracts | `Optional<User>` = "user might not exist" |

### Design Intent

```text
Optional is for RETURN TYPES
  → "This method may not return a value"
  → Caller must handle absence explicitly
```

### Example — Clear API

```java
public Optional<User> findUser(Long id) {
    return Optional.ofNullable(userRepository.findById(id));
}

// Caller handles absence
findUser(1L)
    .map(User::getName)
    .orElse("Unknown");
```

### What Optional Is NOT

```text
❌ Not a way to eliminate null from Java
❌ Not for fields in entity classes
❌ Not for method parameters (generally)
✅ A wrapper to make absence explicit in return types
```

**Interview Point:**

> Optional introduced to make **missing values explicit** in APIs and reduce NPEs—not to replace all null checks blindly.

</details>

---

# 2. NullPointerException solution?

<details>
<summary>Show Answer</summary>

**Answer:**

`Optional` helps prevent `NullPointerException` by forcing developers to **handle the absent case** before accessing a value—no more silent `null` returns.

### Traditional NPE Risk

```java
String city = user.getAddress().getCity().toUpperCase();
// NPE if user, address, or city is null — any level
```

### Optional Chain — Safe Navigation

```java
Optional<String> city = Optional.ofNullable(user)
    .map(User::getAddress)
    .map(Address::getCity);

city.map(String::toUpperCase)
    .ifPresent(System.out::println);
// No NPE — empty Optional if any step is null
```

### How It Prevents NPE

| Approach | NPE Risk |
|----------|----------|
| `user.getName().toUpperCase()` | High |
| `if (user != null && user.getName() != null)` | Low but verbose |
| `Optional.ofNullable(user).map(User::getName).map(String::toUpperCase)` | Low, clean |

### Safe Value Access Methods

```java
Optional<String> opt = findName();

// ❌ Dangerous — throws if empty
String s = opt.get();

// ✅ Safe alternatives
opt.orElse("default");
opt.orElseGet(() -> computeDefault());
opt.orElseThrow(() -> new NotFoundException());
opt.ifPresent(name -> use(name));
```

### Stream Integration

```java
users.stream()
     .map(User::getEmail)           // might be null
     .map(Optional::ofNullable)     // wrap
     .flatMap(Optional::stream)     // skip nulls safely
     .forEach(sendEmail);
```

### Important Limitation

```text
Optional does NOT eliminate null from Java
It only helps when used correctly in return types
Misuse (get() without check) still causes NPE
```

**Interview Point:**

> Optional prevents NPE through **explicit absence handling**—use `map`/`flatMap` chains instead of `get()`. Never call `get()` without `isPresent()` check.

</details>

---

# 3. Optional.of()?

<details>
<summary>Show Answer</summary>

**Answer:**

`Optional.of(value)` creates an `Optional` containing the value—the value **must NOT be null**. If null is passed, it throws `NullPointerException` immediately.

### Usage

```java
Optional<String> opt = Optional.of("Hello");
// opt contains "Hello"

String name = "Java";
Optional<String> opt2 = Optional.of(name);
```

### Null Throws NPE

```java
Optional<String> opt = Optional.of(null);
// ❌ NullPointerException immediately
```

### When to Use

```text
Use Optional.of() when:
  ✅ Value is GUARANTEED non-null
  ✅ You want fail-fast if null accidentally passed
  ✅ Constant/literal values
```

### Examples

```java
Optional<String> greeting = Optional.of("Hello World");
Optional<Integer> count = Optional.of(42);

// After validation — value known non-null
if (input != null) {
    return Optional.of(input.trim());
}
```

### of() vs ofNullable()

| | `Optional.of(value)` | `Optional.ofNullable(value)` |
|---|----------------------|------------------------------|
| Null input | **NPE** | Returns `Optional.empty()` |
| Use when | Value guaranteed non-null | Value might be null |

```java
// Wrong — use ofNullable if null possible
User user = repository.find(id);
Optional.of(user);  // ❌ NPE if user is null

// Correct
Optional.ofNullable(user);  // ✅ empty Optional if null
```

**Interview Point:**

> `Optional.of()` = value **must not be null**. Use only when null is impossible. For nullable values use `ofNullable()`.

</details>

---

# 4. Optional.ofNullable()?

<details>
<summary>Show Answer</summary>

**Answer:**

`Optional.ofNullable(value)` creates an `Optional` containing the value if non-null, or **`Optional.empty()`** if the value is null—the most commonly used factory method.

### Usage

```java
Optional<String> opt1 = Optional.ofNullable("Hello");
// contains "Hello"

Optional<String> opt2 = Optional.ofNullable(null);
// empty Optional — no exception
```

### Most Common Pattern — Repository Lookup

```java
public Optional<User> findById(Long id) {
    User user = database.find(id);
    return Optional.ofNullable(user);
    // user found → Optional<User>
    // user null   → Optional.empty()
}
```

### Wrapping Nullable Chain

```java
Optional<String> city = Optional.ofNullable(user)
    .map(User::getAddress)
    .map(Address::getCity);
// any null in chain → empty Optional
```

### Comparison Table

```java
Optional.of("text");        // ✅ Optional["text"]
Optional.of(null);          // ❌ NPE
Optional.ofNullable("text"); // ✅ Optional["text"]
Optional.ofNullable(null);   // ✅ Optional.empty()
Optional.empty();            // ✅ Optional.empty()
```

### Real Production Use

```java
// Service layer
public Optional<Order> findOrder(String orderId) {
    return Optional.ofNullable(orderRepository.findById(orderId));
}

// Controller
return findOrder(id)
    .map(this::toDTO)
    .orElseThrow(() -> new OrderNotFoundException(id));
```

**Interview Point:**

> `ofNullable()` is the **default choice** when value might be null. Returns empty Optional instead of throwing NPE.

</details>

---

# 5. Optional.empty()?

<details>
<summary>Show Answer</summary>

**Answer:**

`Optional.empty()` returns a **singleton empty Optional** with no value—represents an explicitly absent result.

### Usage

```java
Optional<String> empty = Optional.empty();
// No value inside

boolean hasValue = empty.isPresent();  // false
boolean isEmpty = empty.isEmpty();     // true (Java 11+)
```

### When to Use

```java
// Explicitly return "not found" without null
public Optional<User> findUser(String email) {
    if (email == null || email.isBlank()) {
        return Optional.empty();  // explicit absence
    }
    return Optional.ofNullable(repository.findByEmail(email));
}

// Default in cache miss
public Optional<Config> getConfig(String key) {
  return cache.get(key);  // returns Optional.empty() if not cached
}
```

### empty() vs ofNullable(null)

```java
Optional.empty()           // singleton empty instance
Optional.ofNullable(null) // also empty — same effect

// Both are empty:
Optional.empty().equals(Optional.ofNullable(null));  // true
```

### Singleton Pattern

```text
Optional.empty() returns same singleton instance
Memory efficient — one shared empty Optional for all types (via erasure)
```

### Checking Empty

```java
Optional<String> opt = Optional.empty();

opt.isPresent();  // false
opt.isEmpty();    // true (Java 11+)
opt.orElse("default");  // "default"
```

**Interview Point:**

> `Optional.empty()` = explicit "no value". Use in return when result is intentionally absent. Same as `ofNullable(null)`.

</details>

---

### Intermediate

---

# 6. orElse() vs orElseGet()?

<details>
<summary>Show Answer</summary>

**Answer:**

`orElse()` always **evaluates the default value**. `orElseGet()` evaluates the default **only if Optional is empty**—lazy supplier pattern.

### orElse() — Always Evaluates Default

```java
Optional<String> opt = Optional.of("Hello");

String result = opt.orElse(computeExpensiveDefault());
// computeExpensiveDefault() runs EVEN THOUGH opt has value!
```

### orElseGet() — Lazy Evaluation

```java
Optional<String> opt = Optional.of("Hello");

String result = opt.orElseGet(() -> computeExpensiveDefault());
// computeExpensiveDefault() does NOT run — opt has value
// Returns "Hello" immediately
```

### Comparison Table

| | `orElse(T default)` | `orElseGet(Supplier<T>)` |
|---|---------------------|--------------------------|
| Default evaluation | **Always** | **Only if empty** |
| Parameter | Value | Supplier (lambda) |
| Use when | Cheap default | Expensive default |
| Performance | May waste work | Lazy — efficient |

### Expensive Default Example

```java
// ❌ Bad — DB call even when value present
String config = optionalConfig.orElse(loadFromDatabase());

// ✅ Good — DB call only when empty
String config = optionalConfig.orElseGet(() -> loadFromDatabase());
```

### Cheap Default — Either OK

```java
// Both fine for simple defaults
opt.orElse("default");
opt.orElseGet(() -> "default");
```

### orElseThrow() — Third Option

```java
// Throw if empty
User user = findUser(id)
    .orElseThrow(() -> new UserNotFoundException(id));

// Java 10+ — no-arg throws NoSuchElementException
User user = findUser(id).orElseThrow();
```

### Decision Guide

```text
Value present likely + expensive default → orElseGet()
Simple literal default                  → orElse()
Must exist or fail                        → orElseThrow()
```

**Interview Point:**

> `orElse` = default **always** computed. `orElseGet` = default **only if empty**. Use `orElseGet` for expensive defaults—classic interview trap.

</details>

---

# 7. map()?

<details>
<summary>Show Answer</summary>

**Answer:**

`Optional.map()` transforms the value inside Optional if present—returns `Optional<R>` with transformed value, or empty if original was empty.

### Syntax

```java
Optional<U> map(Function<? super T, ? extends U> mapper)
```

### Basic Example

```java
Optional<String> opt = Optional.of("hello");

Optional<String> upper = opt.map(String::toUpperCase);
// Optional["HELLO"]

Optional<String> empty = Optional.empty();
Optional<String> result = empty.map(String::toUpperCase);
// still Optional.empty()
```

### Null-Safe Chain

```java
Optional<String> cityUpper = Optional.ofNullable(user)
    .map(User::getAddress)      // if user null → empty
    .map(Address::getCity)      // if address null → empty
    .map(String::toUpperCase);  // if city null → empty
// No NPE at any step
```

### map() Only Runs If Present

```java
Optional<Integer> length = Optional.of("Java")
    .map(s -> {
        System.out.println("mapping");  // prints
        return s.length();
    });
// Optional[4]

Optional<Integer> noLength = Optional.empty()
    .map(s -> s.length());  // mapper NOT called
// Optional.empty()
```

### map() vs if Present Check

```java
// Old way
if (opt.isPresent()) {
    return opt.get().toUpperCase();
}

// Optional map
return opt.map(String::toUpperCase).orElse("");
```

### map() Trap — Nested Optional

```java
Optional<Optional<String>> nested = opt.map(s -> Optional.of(s.toUpperCase()));
// ❌ Optional<Optional<String>> — awkward

// Use flatMap instead
Optional<String> flat = opt.flatMap(s -> Optional.of(s.toUpperCase()));
```

**Interview Point:**

> `map()` transforms value if present, returns empty if not. Use for null-safe chains. If mapper returns Optional, use `flatMap`.

</details>

---

# 8. flatMap()?

<details>
<summary>Show Answer</summary>

**Answer:**

`Optional.flatMap()` transforms the value using a function that returns an `Optional`—**flattens** nested `Optional<Optional<T>>` into `Optional<T>`.

### Why flatMap Is Needed

```java
Optional<String> opt = Optional.of("hello");

// map — returns nested Optional
Optional<Optional<String>> nested = opt.map(s -> Optional.of(s.toUpperCase()));
// Optional[Optional["HELLO"]] — awkward!

// flatMap — flattens
Optional<String> flat = opt.flatMap(s -> Optional.of(s.toUpperCase()));
// Optional["HELLO"] — clean
```

### Syntax

```java
Optional<U> flatMap(Function<? super T, Optional<U>> mapper)
// Mapper must return Optional<U>, not U
```

### Real Example — Optional Chaining

```java
public Optional<String> getCityName(User user) {
    return Optional.ofNullable(user)
        .flatMap(User::getAddress)     // Optional<Address>
        .flatMap(Address::getCity);    // Optional<String>
}

// If User has Optional-returning methods:
public Optional<String> getManagerEmail(Employee emp) {
    return Optional.ofNullable(emp)
        .flatMap(Employee::getManager)   // Optional<Employee>
        .flatMap(Employee::getEmail);    // Optional<String>
}
```

### flatMap with Service Calls

```java
public Optional<Order> findLatestOrder(Long userId) {
    return findUser(userId)
        .flatMap(user -> orderService.findLatestByUser(user));
    // findUser returns Optional<User>
    // findLatestByUser returns Optional<Order>
}
```

### flatMap vs map

| | `map(fn)` | `flatMap(fn)` |
|---|-----------|---------------|
| Mapper returns | `R` | `Optional<R>` |
| Result | `Optional<R>` | `Optional<R>` (flattened) |
| Nested Optional | Yes (problem) | No (flattened) |

### Stream Integration

```java
// flatMap Optional::stream — unwrap in stream
users.stream()
     .map(User::getEmail)           // String might be null
     .map(Optional::ofNullable)
     .flatMap(Optional::stream)     // skip empty Optionals
     .forEach(sendEmail);
```

**Interview Point:**

> `flatMap` when mapper returns `Optional`. Avoids `Optional<Optional<T>>`. Same concept as Stream flatMap.

</details>

---

# 9. filter()?

<details>
<summary>Show Answer</summary>

**Answer:**

`Optional.filter()` returns the Optional if the value **matches the predicate**, or **`Optional.empty()`** if empty or predicate fails.

### Basic Usage

```java
Optional<Integer> opt = Optional.of(42);

Optional<Integer> filtered = opt.filter(n -> n > 10);
// Optional[42] — predicate passed

Optional<Integer> failed = opt.filter(n -> n > 100);
// Optional.empty() — predicate failed
```

### Empty Optional — filter Does Nothing

```java
Optional<String> empty = Optional.empty();
Optional<String> result = empty.filter(s -> s.length() > 5);
// still Optional.empty() — predicate not evaluated
```

### Real Examples

```java
// Filter adult users
Optional<User> adult = findUser(id)
    .filter(user -> user.getAge() >= 18);

// Filter non-empty strings
Optional<String> validName = Optional.ofNullable(name)
    .filter(s -> !s.isBlank());

// Chain filter with map
Optional<String> upperAdultCity = findUser(id)
    .filter(user -> user.getAge() >= 18)
    .map(User::getAddress)
    .map(Address::getCity)
    .map(String::toUpperCase);
```

### filter() vs if Present

```java
// Old way
if (opt.isPresent() && opt.get().length() > 5) {
    use(opt.get());
}

// Optional filter
opt.filter(s -> s.length() > 5)
   .ifPresent(use);
```

### Multiple Conditions

```java
Optional<Employee> eligible = findEmployee(id)
    .filter(Employee::isActive)
    .filter(e -> e.getSalary() > 50000)
    .filter(e -> e.getDepartment().equals("IT"));
```

### Properties

| Property | Detail |
|----------|--------|
| Empty input | Returns empty — predicate not run |
| Predicate fails | Returns empty |
| Predicate passes | Returns same Optional |
| Lazy | Predicate only evaluated if value present |

**Interview Point:**

> `filter(Predicate)` keeps value only if condition true. Empty stays empty. Chain with `map` for conditional processing.

</details>

---

### Advanced

---

# 10. Why Optional should not be used in entity classes?

<details>
<summary>Show Answer</summary>

**Answer:**

`Optional` should **not** be used as **fields** in entity/domain classes because it breaks **serialization**, adds **memory overhead**, and conflicts with **JPA/Hibernate** and **JSON** frameworks.

### Problems with Optional Fields

```java
// ❌ Bad — Optional as entity field
@Entity
class User {
    private Long id;
    private Optional<String> middleName;  // DON'T
    private Optional<Address> address;    // DON'T
}
```

### Why It's Wrong

| Issue | Explanation |
|-------|-------------|
| **Serialization** | Jackson/Gson don't handle Optional fields well by default |
| **JPA/Hibernate** | JPA doesn't support Optional as persistent field type |
| **Memory overhead** | Extra Optional wrapper object per field |
| **equals/hashCode** | Complicated with Optional fields |
| **Not serializable** | Optional itself is not Serializable (traditionally) |
| **API confusion** | Field always "present" as Optional object—even when empty |

### Correct Entity Design

```java
// ✅ Use nullable fields — null means absent
@Entity
class User {
    private Long id;
    private String middleName;  // null if not set
    private Address address;    // null if not set

    // Optional only in getter for API convenience (debated)
    public Optional<String> getMiddleName() {
        return Optional.ofNullable(middleName);
    }
}
```

### Optional Where It Belongs

```text
✅ Method RETURN types     → Optional<User> findById(id)
✅ Stream operations       → flatMap(Optional::stream)
❌ Entity fields           → use null
❌ DTO fields (usually)    → use null or validation
❌ Constructor parameters  → use null
```

### Joshua Bloch / Java Guidelines

```text
"Optional was designed for return types"
"Do not use Optional for fields"
"Do not use Optional in collections"
```

### JPA Example

```java
// Hibernate will fail or behave unexpectedly
@Entity
class Order {
    @Column
    private Optional<String> notes;  // ❌ not supported
}
```

**Interview Point:**

> Optional for **return types only**. Entity fields use `null` for absence. JPA, JSON, serialization don't support Optional fields well.

</details>

---

# 11. Optional as method parameter?

<details>
<summary>Show Answer</summary>

**Answer:**

Using `Optional` as a **method parameter** is generally **discouraged**—it adds overhead, unclear semantics, and doesn't improve null safety over simple nullable parameters.

### Why Avoid Optional Parameters

```java
// ❌ Discouraged
public void processUser(Optional<String> name) {
    name.ifPresent(n -> ...);
}

// ✅ Preferred — nullable parameter with clear contract
public void processUser(String name) {
    if (name != null) {
        ...
    }
}
```

### Problems with Optional Parameters

| Problem | Detail |
|---------|--------|
| **Caller burden** | Caller must wrap: `processUser(Optional.of(name))` |
| **Three states** | null param, empty Optional, present Optional — confusing |
| **No real safety** | Caller can pass `Optional.of(null)` — NPE in of() |
| **Overhead** | Unnecessary Optional object creation |
| **Readability** | `processUser(name)` cleaner than `processUser(Optional.ofNullable(name))` |

### Three-State Confusion

```java
void save(Optional<String> value) {
    // value == null           → caller passed null (bug?)
    // value.isEmpty()         → intentionally no value
    // value.isPresent()       → has value
}
// Too many cases — error prone
```

### Better Alternatives

```java
// Nullable parameter — document in comment/JavaDoc
public void updateEmail(String email) {
    if (email != null) {
        user.setEmail(email);
    }
}

// Overloaded methods
public void search(String name) { search(name, null); }
public void search(String name, String department) { ... }

// Builder pattern for many optional params
UserSearch.builder()
    .name("John")
    .department("IT")
    .build();
```

### When Optional Parameter Might Be OK

```text
Rare cases in functional/Stream APIs internally
Framework code (Optional already in pipeline)
Generally avoid in public application APIs
```

### Return vs Parameter

```text
Optional RETURN  → ✅ "result might not exist"
Optional PARAM   → ❌ "argument might not be passed" — use null or overload
```

**Interview Point:**

> Optional as **parameter** = discouraged. Use nullable param or overloads. Optional designed for **return types** where absence is part of the result contract.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Optional.get() without check?

<details>
<summary>Show Answer</summary>

**Answer:** Throws `NoSuchElementException` if empty. Never use `get()` without `isPresent()`—use `orElse`, `orElseGet`, or `ifPresent` instead.

</details>

---

### Q: Optional in JSON response?

<details>
<summary>Show Answer</summary>

**Answer:** Jackson serializes Optional fields awkwardly (object wrapper). Return plain null or use `@JsonInclude(NON_NULL)` on DTO fields—not Optional fields.

</details>

---

### Q: isPresent() vs isEmpty()?

<details>
<summary>Show Answer</summary>

**Answer:** `isPresent()` returns true if value exists. `isEmpty()` (Java 11+) returns true if no value. Prefer `ifPresent()` over `if (isPresent()) get()`.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Optional = explicit "maybe absent" wrapper for **return types**. Use `ofNullable` for nullable values, `orElseGet` for lazy defaults, `map`/`flatMap` for safe chains. Never fields, rarely parameters, never `get()` without check.

</details>
