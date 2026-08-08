# 43. Builder Design Pattern

## 43. Builder Design Pattern

## Most Asked in Modern Java

---

# 1. Why Builder Pattern?

<details>
<summary>Show Answer</summary>

**Answer:**

The **Builder Pattern** builds complex objects **step by step** with a clean, readable API—especially when an object has **many optional fields** or **complex construction logic**.

### Simple Idea

```text
Building a house:
  Step 1: add walls
  Step 2: add roof
  Step 3: add garage (optional)
  Builder assembles final house — not one giant constructor
```

```java
Order order = Order.builder()
    .customerId("C101")
    .productId("P55")
    .quantity(2)
    .discountCode("SAVE10")
    .build();
```

**Interview Point:**

> Builder = step-by-step object construction. Readable, flexible, avoids telescoping constructors.

</details>

---

# 2. Problems solved by Builder?

<details>
<summary>Show Answer</summary>

**Answer:**

| Problem | Builder Solution |
|---------|------------------|
| Too many constructor parameters | Set fields one at a time |
| Many optional fields | Only set what you need |
| Unreadable constructor calls | Fluent API with names |
| Immutable objects | Build once, no setters after |
| Validation at build time | `build()` validates all fields together |

```java
// ❌ Hard to read — which param is what?
new User("John", null, null, "john@email.com", null, true, false);

// ✅ Clear
User user = User.builder()
    .name("John")
    .email("john@email.com")
    .active(true)
    .build();
```

**Interview Point:**

> Builder solves telescoping constructors, optional params, readability, and build-time validation.

</details>

---

# 3. Telescoping Constructor Problem?

<details>
<summary>Show Answer</summary>

**Answer:**

**Telescoping constructors** = many overloaded constructors chaining defaults—hard to maintain and use.

```java
// Telescoping — grows forever
public Pizza(String size) { ... }
public Pizza(String size, String crust) { ... }
public Pizza(String size, String crust, List<String> toppings) { ... }
public Pizza(String size, String crust, List<String> toppings, boolean extraCheese) { ... }
// 10 optional fields = explosion of constructors
```

### Problems

```text
❌ Duplicate code across constructors
❌ Easy to pass wrong argument order
❌ Can't skip middle optional params without null
❌ Unreadable at call site
```

### Builder Fix

```java
Pizza pizza = Pizza.builder()
    .size("large")
    .crust("thin")
    .toppings(List.of("olive", "corn"))
    .extraCheese(true)
    .build();
```

**Interview Point:**

> Telescoping = many constructors for optional fields. Builder replaces with fluent step-by-step API.

</details>

---

## Advanced

---

# 4. Immutable objects with Builder?

<details>
<summary>Show Answer</summary>

**Answer:**

Builder is ideal for **immutable objects**—set all fields via builder, `build()` creates final immutable instance with no setters.

```java
public final class ImmutableOrder {
    private final String id;
    private final String customerId;
    private final int quantity;

    private ImmutableOrder(Builder builder) {
        this.id = builder.id;
        this.customerId = builder.customerId;
        this.quantity = builder.quantity;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String id;
        private String customerId;
        private int quantity;

        public Builder id(String id) { this.id = id; return this; }
        public Builder customerId(String cid) { this.customerId = cid; return this; }
        public Builder quantity(int q) { this.quantity = q; return this; }

        public ImmutableOrder build() {
            if (id == null) throw new IllegalStateException("id required");
            return new ImmutableOrder(this);
        }
    }
}
```

```text
✅ Object immutable after build()
✅ Validation in build() before creating
✅ Builder can be reused — create new builder each time
```

**Interview Point:**

> Private ctor + static builder + build() validation = immutable object pattern. Lombok @Builder does this automatically.

</details>

---

# 5. Lombok Builder?

<details>
<summary>Show Answer</summary>

**Answer:**

Lombok `@Builder` generates builder code at compile time—no manual Builder class.

```java
@Builder
@Getter
public class Order {
    private final String orderId;
    private final String customerId;
    private final int quantity;
    @Builder.Default
    private final boolean express = false;
}

// Usage
Order order = Order.builder()
    .orderId("O123")
    .customerId("C99")
    .quantity(5)
    .express(true)
    .build();
```

### Common Lombok Combos

```java
@Builder
@Value          // immutable — all fields final
public class Address {
    String city;
    String zip;
}

@Builder(toBuilder = true)  // copy existing object to builder
public class User { ... }
```

**Interview Point:**

> Lombok @Builder = auto-generated fluent builder. @Builder.Default for optional defaults. toBuilder for copies.

</details>

---

# 6. Real-world Builder examples?

<details>
<summary>Show Answer</summary>

**Answer:**

| Example | Usage |
|---------|--------|
| **OkHttp Request** | `new Request.Builder().url(...).header(...).build()` |
| **Retrofit** | Client builder configuration |
| **Spring Security** | `HttpSecurity` fluent config |
| **JUnit** | Test builders |
| **StringBuilder** | Append steps, build string |
| **Hibernate Criteria** | Query building |
| **AWS SDK** | Client builders |

```java
// OkHttp
Request request = new Request.Builder()
    .url("https://api.example.com/orders")
    .header("Authorization", "Bearer token")
    .build();

// Spring Security
http.securityMatcher("/api/**")
    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
    .build();
```

**Interview Point:**

> OkHttp Request.Builder, StringBuilder, Spring Security config — builder everywhere in modern Java APIs.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Builder vs Factory?

<details>
<summary>Show Answer</summary>

**Answer:**

**Factory** = which **type** to create (Email vs SMS). **Builder** = how to **assemble one complex object** with many fields. Different problems.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Builder = fluent step-by-step construction. Fixes telescoping constructors. Immutable + validation in build(). Lombok @Builder in production. OkHttp, StringBuilder are real examples.

</details>
