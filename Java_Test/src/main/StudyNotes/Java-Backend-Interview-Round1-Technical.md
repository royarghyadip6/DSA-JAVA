# 1st Technical Round — Java Backend (5–8 Years)

This file is **only for Round 1** (technical screening / first coding+concepts round).

Round 2 (microservices, Kafka, AWS, system design), Manager round, and HR round are **not** in this file.

Java in this file is sized for a **5 to 8 year** developer: not “what is a class”, and not a JVM-tuning book. If a 5–8 YOE interview stays on Java, they will walk through **internals + trade-offs + one production example**.

**What Round 1 usually is (45–75 minutes)**

1. “Tell me about your project” — 1 to 2 minutes
2. Core Java + OOP + Collections internals (this is the heaviest part)
3. JVM / GC / references (short, but they do ask)
4. Java 8 and later (11 / 17 / 21)
5. Multithreading + `java.util.concurrent`
6. Spring Boot + Hibernate/JPA
7. Spring Security
8. SQL query on a shared screen
9. 1 or 2 coding problems on a shared screen

They are checking: can you explain internals in simple words, can you write correct code, and did you actually use Spring/SQL in a project.

**How to use this file**

- Theory answers are **open**. Read them out loud until you can say them without looking.
- Coding answers are **hidden**. Solve first, then open.
- Speak in simple English. Start with the short answer. If they say “explain internally”, continue.
- Do not dump every point in the first 20 seconds.

**Full study time: about 10–11 hours.** For a fast revision, prioritize the night checklist, HashMap/concurrency, Spring transactions/JPA/security, testing, SQL, and 8 coding problems.

| Time | What to finish |
|---|---|
| 2.5h | Core Java + OOP + Collections (especially HashMap) |
| 1.0h | JVM, GC, generics, serialization, singleton |
| 1.5h | Multithreading + JUC + Java 8/11/17/21 |
| 1.5h | Spring Boot + Hibernate/JPA + Spring Security |
| 1.0h | Testing, patterns, REST/build/logging |
| 0.75h | SQL |
| 1.5h | Coding/DSA (pick at least 8) |
| 0.3h | 90-second project intro |

---

## Table of contents

1. [Opening — project intro](#1-opening--project-intro-90-seconds)
2. [Core Java](#2-core-java)
3. [OOP and language (5–8 YOE)](#3-oop-and-language-58-yoe)
4. [Collections (they ask this the most)](#4-collections-they-ask-this-the-most)
5. [Java 8, 11, 17, 21](#5-java-8-11-17-21)
6. [Multithreading and JUC](#6-multithreading-and-juc)
7. [JVM, GC, references](#7-jvm-gc-references)
8. [Generics, serialization, singleton](#8-generics-serialization-singleton)
9. [Spring Boot](#9-spring-boot)
10. [Hibernate and JPA](#10-hibernate-and-jpa)
11. [Spring Security](#11-spring-security)
12. [SQL](#12-sql)
13. [Trick / output questions](#13-trick--output-questions)
14. [Coding problems](#14-coding-problems-try-first)
15. [Unit and integration testing](#15-unit-testing-and-integration-testing)
16. [Design patterns](#16-design-patterns-commonly-asked)
17. [Spring ecosystem, REST, build, logging](#17-spring-ecosystem-rest-build-and-logging)
18. [Additional coding problems](#18-additional-high-frequency-coding-problems)
19. [Night-before checklist](#night-before-checklist-round-1)
20. [How to talk in Round 1](#how-to-talk-in-round-1)

**Java topics covered for 5–8 years (Round 1)**

OOP (4 pillars, abstract vs interface, diamond, `this`/`super`, static, access, composition, marker) · String/SCP/immutability · equals/hashCode + Integer cache · overloading/overriding · pass-by-value · enum · inner classes · exceptions · try-with-resources · `java.time` · HashMap/HashSet/CHM/ArrayList/LinkedHashMap/TreeMap/WeakHashMap/IdentityHashMap/PriorityQueue/ArrayDeque · Java 7 HashMap concurrent-resize bug · Java 8 streams (map/flatMap, collect, parallel pitfalls) · Java 11/17/21 (records, sealed, virtual threads) · threads, JMM/volatile, ThreadPoolExecutor, DCL singleton, Latch/Barrier/Semaphore, BlockingQueue, ForkJoin, CompletableFuture · JVM/classloaders/G1/ZGC/references/heap dump · generics erasure + PECS · serialization/`readResolve`/Cloneable · reflection/annotations/SOLID

---

## 1. Opening — project intro (90 seconds)

They will start with this. Round 1 wants a **short** story. Do not explain Kafka request-reply in this round unless they ask.

**What you should say**

> I work on **ESM — Ethernet Service Management** — inside Nokia **WS-NOC**. It is a Java backend service used by operators to create and manage carrier Ethernet services on the network, like EPL and EVPL.
>
> Northbound, the UI/API hits our REST layer. We persist the service model in **Oracle**. When we actually need to configure a device, we do not call the network element directly. We send a message through **Kafka** to an adapter service, and that adapter talks to the device.
>
> The stack is Java, Spring, JPA, Oracle, Kafka. The code is split into modules: common DTOs, DAO, business service, and a web module for REST.
>
> In day-to-day work I pick up features and defects on service create/deploy, list screens, and southbound failures — things like timeouts, sync mismatch between NMS and the device, and slow list queries.

**If they only ask “what is your role?”**

> I am a backend developer. I write the service logic, REST APIs, database changes, and I debug production issues using logs, SQL, and Kafka traces. I also review PRs and help test the flow on the lab.

**Stop here.** If they want architecture, they will ask. That is more Round 2.

---

## 2. Core Java

---

### Q. Why is main written as `public static void main(String[] args)`?

**Answer**

The JVM starts the program by calling `main`. It is **public** so the JVM can call it from outside the class. It is **static** so the JVM does not need to create an object first — there is no object yet at startup. It is **void** because the JVM does not use a return value from `main`. If you want an exit code, you use `System.exit`. `String[] args` is how command-line arguments are passed.

In Spring Boot, `main` only boots the application:

```java
public static void main(String[] args) {
    SpringApplication.run(MyApp.class, args);
}
```

**If they go deeper**

- Can we overload `main`? Yes. Extra `main` methods are normal methods. The JVM still looks only for `public static void main(String[])`.
- Can `main` be private? Then the JVM cannot start that class as the entry point.
- Can we change the return type? Not for the JVM entry point.

---

### Q. Why is String immutable?

**Answer**

String is immutable because Java shares String objects in many places. The **String pool** reuses the same object for the same literal. Strings are also used as HashMap keys, class names, file paths, and in security checks.

If String were mutable, changing it through one reference would change it for every other reference pointing at the same object. The pool would break. A HashMap key could change after insert, and you would never find that entry again because the hash bucket would be wrong.

So the character data inside a String cannot change. Methods like `concat`, `replace`, `toUpperCase` look like they modify the String, but they **create a new String** and return it.

**If they go deeper**

- How is immutability implemented? The class is `final`. The backing `byte[]`/`char[]` is private. There is no setter.
- Why is this also faster? The same object can be shared. `hashCode` can be cached on the String object.
- Other immutable classes: `Integer`, `Long`, `LocalDate`, and any class you write with `final` fields and no setters.

---

### Q. What is the String Constant Pool?

**Answer**

The String pool is a special area on the **heap** (since Java 7 it is not in PermGen) that stores unique String literals.

When you write `String a = "hello";`, the JVM checks the pool. If `"hello"` is already there, `a` points to that object. If not, it creates it and puts it in the pool.

```java
String a = "hello";
String b = "hello";
String c = new String("hello");

a == b;        // true  — same pool object
a == c;        // false — c is a new heap object
a.equals(c);   // true  — same characters
```

`new String("hello")` always creates a new object. The literal `"hello"` still sits in the pool. `c.intern()` returns the pooled object.

**If they go deeper**

Why was the pool moved to the heap? PermGen had a fixed size. Too many interned strings caused `OutOfMemoryError: PermGen`. On the heap the pool can grow and is garbage collected when nothing references those strings (for strings that are interned and no longer used — literals from loaded classes stay as long as the class is loaded).

---

### Q. String vs StringBuilder vs StringBuffer?

**Answer**

| | String | StringBuilder | StringBuffer |
|---|---|---|---|
| Mutable? | No | Yes | Yes |
| Safe to share across threads? | Yes—no internal mutation | No | Yes (synchronized) |
| Speed in a loop | Slow (new object every time) | Fast | Slower than Builder |
| When to use | Fixed text, keys, messages | Building text in one thread | Almost never today |

If you write this in a loop:

```java
String s = "";
for (int i = 0; i < n; i++) {
    s = s + i;   // new String every time
}
```

you create many objects. Use `StringBuilder` instead.

I do not use `StringBuffer` in new code. Synchronization on every append is extra cost. If I need a string built across threads, I do not share a builder — I build locally and then publish the final String.

---

### Q. What is the contract between `equals()` and `hashCode()`?

**Answer**

The contract is simple:

1. If `a.equals(b)` is **true**, then `a.hashCode()` and `b.hashCode()` **must** be the same.
2. If hash codes are the same, objects are **not** necessarily equal. That is a collision. Then `equals` decides.

If you override `equals`, you **must** override `hashCode`, and you must use the **same fields** in both.

**Full `equals` contract** (they ask this at 5–8 YOE):

- **Reflexive:** `x.equals(x)` is true
- **Symmetric:** `x.equals(y)` iff `y.equals(x)` (broken if you mix `equals` with a subclass that adds a field)
- **Transitive:** if x=y and y=z then x=z
- **Consistent:** no randomness; same inputs, same result
- **Non-null:** `x.equals(null)` is false, never NPE

**Why HashMap cares**

HashMap first uses `hashCode` to find the **bucket** (array index). Inside that bucket it uses `equals` to find the exact key.

If two equal objects have different hash codes, you put with one key and get with an “equal” key and you get `null`. The object is sitting in another bucket.

**Rules I follow**

- Use `Objects.equals` and `Objects.hash`.
- Prefer immutable keys.
- Never use a field in `hashCode` that you later change while the object is in a HashMap. If you change it, the object is in the old bucket and is “lost”.

```java
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Employee e)) return false;
    return id == e.id;
}
@Override
public int hashCode() {
    return Long.hashCode(id);
}
```

---

### Q. Overloading vs overriding?

**Answer**

**Overloading** is the same method name, different parameters, in the same class (or parent/child). The compiler picks the method at **compile time** from the reference type and argument types. Return type can be different, but parameters **must** be different. This is compile-time polymorphism.

**Overriding** is the same method signature in a child class. The JVM picks the method at **runtime** from the **actual object**. This is runtime polymorphism. `@Override` helps the compiler catch mistakes.

**Exception rule (they like this)**

In overriding:

- You cannot throw a **new or wider checked** exception than the parent method.
- You can throw a narrower checked exception.
- You can throw any **unchecked** exception (`RuntimeException`) even if the parent does not declare it.

In overloading there is no such restriction, because they are different methods.

**If they go deeper**

Can we reduce visibility while overriding? No. `public` in parent cannot become `protected` in child. We can keep it the same or make it more open (protected → public).

Can we change return type while overriding? Yes, to a subtype (covariant return). `Object` in parent, `String` in child is allowed.

Can we override a static method? No. We hide it. Static binding is compile time.

---

### Q. Can we overload `main`? Can constructor be final? Can class be private?

**Answer**

- Overload `main`: **Yes**. Only the standard signature is the entry point.
- Final constructor: **No**. `final` on a method means “cannot override”. Constructors are not inherited, so `final` on a constructor is not allowed.
- Private class: A top-level class cannot be `private` or `protected`. It can be `public` or package-private. A **nested** class can be private.
- Final class: Yes. That is how String works. No subclass.
- Abstract class instance: You cannot `new` an abstract class. You can `new` a concrete subclass, or use an anonymous class.

---

### Q. How do you make a class immutable, like String?

**Answer**

I follow these rules:

1. Make the class `final` so nobody extends it and adds setters.
2. Make all fields `private final`.
3. No setter methods.
4. Initialize everything in the constructor.
5. If a field is mutable (`Date`, `List`, array), **copy** it in the constructor and **copy** it again in the getter. Do not return the internal list.

```java
public final class Employee {
    private final String name;
    private final List<String> skills;

    public Employee(String name, List<String> skills) {
        this.name = name;
        this.skills = List.copyOf(skills); // defensive copy, unmodifiable
    }

    public String getName() { return name; }

    public List<String> getSkills() {
        return skills; // already unmodifiable
    }
}
```

If I return `this.skills` and it is a normal `ArrayList`, the caller can do `emp.getSkills().add("x")` and break immutability.

---

### Q. Shallow copy vs deep copy?

**Answer**

A **shallow copy** copies the outer object, but nested objects are still **shared**. Change a nested object, and both copies see the change.

A **deep copy** copies the outer object **and** the nested objects. The two graphs are independent.

`t2 = t1` is not a copy at all. Both variables point to the same object.

Default `clone()` is shallow. I rarely use `Cloneable` in production. I use a copy constructor or a mapper, and I copy nested mutable fields myself.

---

### Q. What is `final`, `finally`, `finalize`?

**Answer**

- `final`: variable cannot be reassigned, method cannot be overridden, class cannot be extended.
- `finally`: block that runs after try/catch, even if there is a return, used to close resources. Today I prefer try-with-resources.
- `finalize()`: method the GC might call before collecting an object. **Do not use it.** It is deprecated, slow, and not reliable. Use `Cleaner` or try-with-resources.

---

### Q. Checked vs unchecked exceptions? Can try exist without catch?

**Answer**

**Checked** exceptions must be declared or caught (`IOException`, `SQLException`). The compiler enforces this. They are for recoverable conditions you expect the caller to handle.

**Unchecked** are `RuntimeException` and `Error` (`NullPointerException`, `IllegalArgumentException`). Compiler does not force you to catch them.

I wrap SQL exceptions at the repository boundary and throw a business/runtime exception that the API layer maps to HTTP 4xx/5xx.

**Try without catch:** Yes.

```java
try {
    // work
} finally {
    // cleanup
}
```

Or try-with-resources with no catch. You cannot have try with neither catch nor finally (unless it is try-with-resources, which has an implicit finally).

---

### Q. Heap vs stack? How is memory handled?

**Answer**

**Stack** is per thread. It stores method frames: local variables and partial results. When a method returns, the frame is gone. Primitive locals live here. Object **references** live here, but the objects themselves live on the heap. Too much recursion → `StackOverflowError`.

**Heap** is shared by all threads. All objects (`new`) live here. Young generation (Eden + Survivor) for short-lived objects. Old generation for objects that survive GC. Full heap → `OutOfMemoryError: Java heap space`.

**Metaspace** holds class metadata (replaced PermGen). Classloader leaks fill metaspace.

**In a Spring app**, a typical leak is: a static Map that only grows, a cache without eviction, `ThreadLocal` not removed on a thread pool, or a Hibernate session holding a huge graph.

I do not tune GC in Round 1 unless they ask. If they ask: default is **G1** on modern JDKs. It works in regions and tries to keep pause times down. For a normal Spring Boot service we start with G1 and measure. Full GC details are in the [JVM section](#7-jvm-gc-references).

---

## 3. OOP and language (5–8 YOE)

These are still asked in Round 1. At 5–8 years they want a **clear definition + when you use it in a service**, not a school definition.

---

### Q. Explain OOP in Java with a project example.

**Answer**

Four pillars:

**Encapsulation** — keep fields private, expose behavior. In a service, `Employee` does not expose a mutable list; it exposes `getSkills()` as an unmodifiable copy. Spring beans encapsulate a repository behind a service.

**Abstraction** — show what, hide how. `List` is an abstraction. `PaymentGateway` interface with `StripeGateway` and `MockGateway` implementations. Callers do not care how payment is done.

**Inheritance** — child reuses parent. I use it for **is-a** that is stable (`RuntimeException` → `NotFoundException`). I do not build deep entity hierarchies. Prefer composition.

**Polymorphism** — same call, different behavior. Overloading is compile-time. Overriding is runtime. `service.process(request)` may run `EplProcessor` or `EvplProcessor` depending on the actual object.

**Interview line.** “I use inheritance for exceptions and a few base types. For business behavior I prefer interface + composition, because service types change and deep class trees become unreadable.”

---

### Q. Abstract class vs interface? When do you use which?

**Answer**

| | Abstract class | Interface |
|---|---|---|
| Fields | Instance fields, constructors | Constants (`public static final`). Java 8+ can have `default`/`static` methods. Java 9+ `private` methods |
| Methods | Abstract + concrete | Abstract + default + static |
| Multiple | A class extends **one** abstract class | A class can implement **many** interfaces |
| Constructor | Yes | No |
| Access | Any | Abstract methods are public (legacy). Private helpers allowed from Java 9 |

**When I pick interface:** capability (`Comparable`, `AutoCloseable`, `Runnable`), or a contract between layers (`OrderRepository`). Multiple unrelated classes can share it.

**When I pick abstract class:** shared state + shared partial implementation for a tight family (`AbstractHandler` with a template method). If I only need a contract, I do not start with an abstract class.

**Java 8+:** interfaces can have default methods, so “abstract class for default behavior” is weaker than before. I still use abstract class when I need **fields and a constructor**.

---

### Q. Why no multiple inheritance of classes? Diamond problem?

**Answer**

If class `C` extends `A` and `B`, and both have `foo()`, the JVM would not know which `foo` to call. That is the diamond problem.

Java allows multiple **interfaces**. If two interfaces have the **same default method**, the class **must override** it (or pick `InterfaceName.super.foo()`). The compiler forces a choice. That is how Java solved diamond for default methods.

---

### Q. `this` vs `super`? Constructor chaining?

**Answer**

`this` is the current object. `this()` calls another constructor **in the same class**. `this.field` disambiguates a field from a parameter.

`super` is the parent. `super()` calls the parent constructor. `super.method()` calls the parent method.

Rules:

- `this()` or `super()` must be the **first** statement in a constructor.
- You cannot use both `this()` and `super()` in the same constructor.
- If you write no constructor, Java adds a default no-arg constructor that calls `super()`.
- If the parent has only `Parent(int x)` and no no-arg constructor, the child **must** call `super(x)` or it will not compile.

Static things do not use `this` / `super`. There is no object.

---

### Q. `static` keyword — variable, method, block, nested class?

**Answer**

**Static variable** — one copy per **class**, shared by all instances. Use for constants (`private static final`) or true shared state (careful with threads).

**Static method** — belongs to the class. Cannot use instance fields/`this`. `Math.max`, factory methods.

**Static block** — runs once when the class is initialized, before constructors. Used for initializing static maps.

**Instance block** `{ }` — runs every time an object is created, before the constructor body, after `super()`.

**Order:** parent static → child static → parent instance block → parent constructor → child instance block → child constructor.

**Static nested class** — like a normal class that happens to be nested. It does **not** hold a hidden reference to the outer instance. A non-static inner class **does**, and that can leak memory if you keep inner instances.

**Can a constructor be static?** No. A constructor initializes an instance.

**When I use static:** utilities with no state, constants. I do not put business services in static methods — they are hard to mock and hard to replace.

---

### Q. Access modifiers?

**Answer**

| Modifier | Same class | Same package | Subclass (other pkg) | World |
|---|---|---|---|---|
| `private` | yes | no | no | no |
| default (no modifier) | yes | yes | no | no |
| `protected` | yes | yes | yes | no |
| `public` | yes | yes | yes | yes |

Top-level class: only `public` or package-private. Nested class: any.

I keep fields `private`. I keep service methods that are only for the class `private`. API methods `public`. `protected` is for “subclasses in other packages may need this” — rare in Spring services.

---

### Q. Composition vs inheritance?

**Answer**

Inheritance is **is-a**. Composition is **has-a**.

A `Car` **has an** `Engine`. If I extend `Engine` to make `Car`, that is the wrong model.

Composition is more flexible: I can swap the engine. Inheritance is a compile-time coupling. The fragile base class problem: a change in the parent breaks children.

**Interview line.** “I inherit when the relationship is true and stable. I compose when I want to reuse behavior. Spring DI is composition.”

---

### Q. Marker interface? Functional interface we already covered — what about marker?

**Answer**

A marker interface has **no methods**. It marks a type so the JVM or library can treat it specially. `Serializable`, `Cloneable`, `RandomAccess` (`ArrayList` has it so algorithms can jump by index).

Today we often use **annotations** (`@Transactional`) instead of marker interfaces. Marker interfaces still help with `instanceof` and compile-time type checks.

---

### Q. Is Java pass-by-value or pass-by-reference?

**Answer**

Java is **always pass-by-value**.

For primitives, the value is copied. For objects, the **reference value** is copied. The method gets a copy of the pointer. It can change the object’s fields. It cannot make the caller’s variable point to a new object.

```java
void reassign(List<String> list) {
    list.add("x");          // caller sees this
    list = new ArrayList<>(); // caller does NOT see this
}
```

---

### Q. Autoboxing? Why is `Integer a = 127; Integer b = 127; a == b` true, but 128 false?

**Answer**

Autoboxing converts `int` ↔ `Integer` automatically.

`Integer` caches values from **-128 to 127** (`Integer.valueOf`). So `valueOf(127)` returns the same cached object. `==` is true.

`valueOf(128)` creates a **new** object (unless you changed the cache). `==` is false. `equals` is true.

**Interview line.** “I compare wrapper objects with `equals`, not `==`. For money I do not use `double`. I use `BigDecimal` and `compareTo`, because `equals` also checks scale (`1.0` vs `1.00`).”

Same cache idea exists for `Boolean`, `Byte`, `Short`, `Character` (0–127), some `Long`.

---

### Q. Why `char[]` for passwords, not String?

**Answer**

String is immutable and may stay in the pool / heap until GC. You cannot wipe it. A `char[]` you can fill with zeros after use. Also logs and memory dumps are slightly less likely to keep a long-lived String password. This is a security hygiene question. In Spring I still use `char[]` or a Secret type if the API allows it.

---

### Q. Enum internals?

**Answer**

An `enum` is a class that extends `java.lang.Enum`. Constants are **public static final** instances created once. You cannot `new` them. `==` works for enum comparison because there is one instance per constant.

Enums can have fields, methods, constructors (private). `values()`, `valueOf(String)`.

**Why enum singleton is safe:** JVM class initialization is thread-safe. Serialization of enums does not create a new instance (the spec restores the constant). That is why Effective Java prefers enum singleton.

Switch on enum is common. From Java 17, sealed types + switch is the modern version of a closed set of types.

---

### Q. Inner class vs static nested vs anonymous vs lambda?

**Answer**

- **Static nested:** no outer-instance pointer. Use when the nested type does not need the outer object.
- **Inner (non-static):** hidden reference to outer `this`. Can leak the outer object.
- **Local class:** declared inside a method.
- **Anonymous class:** one-off implementation of an interface/class. Verbose. Can have state.
- **Lambda:** only for **functional interfaces**. No new named type. Does not introduce `this` of its own — `this` is the enclosing class. Cannot hold mutable fields like an anonymous class can.

If I need a one-method callback: lambda. If I need extra fields or multiple methods: anonymous class or a real class.

---

### Q. `throw` vs `throws` vs custom exception design?

**Answer**

`throws` is on the method signature — “this method might throw these checked exceptions.” `throw` is the statement that actually throws.

For APIs I prefer **unchecked** domain exceptions (`NotFoundException extends RuntimeException`) so service signatures stay clean. I do not swallow exceptions empty. I wrap with cause: `throw new ServiceException("deploy failed", e)` so the stack is not lost.

Never use exceptions for normal control flow (not found in a tight loop). Optional or a result type is cleaner for “maybe”.

---

### Q. try-with-resources?

**Answer**

If a class implements `AutoCloseable`, I can write:

```java
try (Connection c = ds.getConnection();
     PreparedStatement ps = c.prepareStatement(sql)) {
    // use
} // close is called in reverse order, even on exception
```

`close()` exceptions are suppressed and attached to the main exception (`getSuppressed()`). This replaced most `finally` close blocks.

---

### Q. `java.time` vs `Date`?

**Answer**

`Date` and `Calendar` are mutable, confusing (`month` is 0-based), and not thread-safe.

I use `java.time`:

- `Instant` — a point on the UTC timeline (store in DB as timestamp)
- `LocalDate` / `LocalDateTime` — no zone (a birthday, a local appointment)
- `ZonedDateTime` — date-time with a zone
- `Duration` / `Period`

I convert at the edges. I do not keep `Date` in new domain models.

---

## 4. Collections (they ask this the most)

---

### Q. Internal working of HashMap? (must know)

**Answer — speak this slowly**

HashMap stores key-value pairs. Internally it is an **array of buckets**. Each bucket is a linked list of nodes, and from Java 8, a long list can become a **balanced tree**.

Default capacity is **16**. Capacity is always a **power of 2**. Default load factor is **0.75**. So the map resizes when size reaches `16 * 0.75 = 12`.

Each node stores:

- `hash` — the mixed hash of the key
- `key`
- `value`
- `next` — next node in the same bucket (or tree links after treeify)

**How `put(key, value)` works**

1. If the key is **null**, HashMap does not call `hashCode` on it (that would NPE). Null key goes to **bucket 0**. Only one null key is allowed. If a null key already exists, the value is replaced.
2. If the key is not null, it calls `key.hashCode()`, then mixes the bits (so similar hashes spread out).
3. The bucket index is:

   `index = (n - 1) & hash`

   This is why size is a power of 2. `(n - 1)` is a bit mask. It is faster than `hash % n`.
4. If that bucket is empty, a new node is placed there.
5. If the bucket is not empty, Java walks the list (or tree):
   - If it finds the **same key** (same hash and `equals` true), it **replaces the value** and returns the old value.
   - If not, it adds a new node. Collision is handled by **chaining** — another node in the same bucket.
6. After insert, if `size > capacity * loadFactor`, the array **doubles** (16 → 32 → 64…). Nodes are moved to a new index. Because capacity is power of 2, each node either stays at the same index or moves by the old capacity. That is cheaper than recomputing everything from scratch.

**How `get(key)` works**

1. Null key → look at bucket 0.
2. Else compute hash, find index, walk the list/tree.
3. Match hash + `equals` → return value. Else return null.

**Collision**

Two different keys can have the same hash, or different hashes that map to the same index. Both sit in the same bucket as a list.

**Java 8 treeify (they will ask)**

If a bucket’s list gets **8 or more** nodes, and the table size is at least **64**, that bucket becomes a red-black tree. Lookup in that bucket becomes O(log n) instead of O(n).

If the table is smaller than 64, HashMap **resizes** instead of treeifying. If the tree shrinks to **6** nodes, it becomes a list again.

**What HashMap is not**

- Not thread-safe.
- Does not keep insertion order (`LinkedHashMap` does).
- Allows one null key and many null values.
- Iterator is fail-fast. If you structurally change the map while iterating, you can get `ConcurrentModificationException`.

**When I use it**

Request-local maps, grouping data in a service method, building a lookup from a DB list. If the map is **shared by many threads**, I use `ConcurrentHashMap`, not HashMap.

---

### Q. Internal working of HashSet?

**Answer**

HashSet stores unique elements. It allows one null. It does not keep order. It is not thread-safe.

Internally HashSet is a **HashMap**. The set element is the **key**. The value is a dummy static object called `PRESENT`.

```java
public HashSet() {
    map = new HashMap<>();
}

private static final Object PRESENT = new Object();

public boolean add(E e) {
    return map.put(e, PRESENT) == null;
}
```

If `put` returns null, the key was new → `add` returns true. If `put` returns the old dummy value, the element was already there → `add` returns false.

Uniqueness is the same as HashMap: `hashCode` + `equals`.

---

### Q. HashMap vs ConcurrentHashMap vs Hashtable vs synchronizedMap?

**Answer**

**HashMap**

- Not thread-safe.
- One null key, many null values.
- Fast for single-threaded use.
- Fail-fast iterator.

**ConcurrentHashMap**

- Thread-safe without locking the **whole** map.
- **No null key, no null value.** If you put null you get `NullPointerException`.
- Iterator is weakly consistent. It does not throw `ConcurrentModificationException`. It may or may not show a write that happens during iteration.
- In Java 8 it does **not** use the old “16 segments” model. The table is still an array of bins. If a bin is empty, `put` uses **CAS** to attach the first node. If the bin already has nodes, it locks **only that bin’s head**. Reads are usually without lock.
- `size()` uses a LongAdder-style counter so it does not lock the whole map.

**Hashtable**

- Old. Every method is `synchronized` on the whole object. Slow. No nulls. I do not use it.

**Collections.synchronizedMap(map)**

- Wraps a map with one lock on every call. Safer than HashMap, but coarse. Worse than ConcurrentHashMap for concurrent reads.

**What I say when they ask “which one?”**

> For a local map in one request, HashMap. For a cache or a map shared across threads in the service, ConcurrentHashMap. I do not use Hashtable.

**Why ConcurrentHashMap disallows null**

If `get` returns null, you cannot tell: is the key missing, or is the value null, while another thread is removing it? The API avoids that confusion.

---

### Q. ArrayList vs LinkedList vs Vector? Which is faster for search / insert / delete?

**Answer**

**ArrayList** is an array that grows (usually 1.5x).

- Get by index: **O(1)** — this is why we use it.
- Add at the end: amortized **O(1)**.
- Add/remove in the middle: **O(n)** because later elements must shift.
- Search by value: **O(n)** unless you know the index.

**LinkedList** is nodes with prev/next.

- Get by index: **O(n)** — it walks from the start.
- Add/remove at the **ends**: O(1).
- Add/remove in the middle **if you already have the node**: O(1), but finding that node is O(n).
- More memory per element (pointers).

**Vector** is a synchronized ArrayList. I do not use it. If I need a concurrent list, I pick a concurrent collection or I synchronize at a higher level.

**Which is best?**

- Frequent get / iterate / append → **ArrayList**. This is the default.
- Frequent search by index → **ArrayList**.
- Queue behavior (add/remove ends) → **ArrayDeque**, not LinkedList.
- I almost never choose LinkedList in production code. People think middle insert is free. It is not, because you must find the index first.

---

### Q. LinkedHashMap? TreeMap?

**Answer**

**LinkedHashMap** is a HashMap plus a doubly linked list of entries. It can keep **insertion order**, or **access order** (that is how a simple LRU is built — see coding section).

**TreeMap** is a red-black tree. Keys are sorted. `get`/`put` are O(log n). Keys must be `Comparable` or you pass a `Comparator`. No null key (in natural order). I use it when I need sorted keys, not as a general HashMap replacement.

---

### Q. Comparable vs Comparator?

**Answer**

When we call `Collections.sort(list)` with one argument, the element type must implement **Comparable**. That is the **natural order**. One class, one `compareTo`.

```java
public int compareTo(Movie other) {
    return Integer.compare(this.year, other.year);
}
```

If I also want to sort by rating, then by name, I cannot do that with one `compareTo`. I use **Comparator**, which is **outside** the class.

```java
movies.sort(Comparator.comparing(Movie::getRating)
                      .thenComparing(Movie::getName));
```

Comparator is a functional interface (`compare`). I can write many comparators for the same type.

`Collections.sort(list, comparator)` and `list.sort(comparator)` use Comparator. The class does not need to implement Comparable.

---

### Q. Iterator vs Enumeration? Fail-fast vs fail-safe?

**Answer**

**Enumeration** is the old cursor (Vector, Hashtable). It only reads. **Iterator** is the modern one. It has `hasNext`, `next`, and `remove`.

**Fail-fast** (ArrayList, HashMap): the collection has a `modCount`. The iterator copies it. If the collection is structurally changed during iteration (except `iterator.remove()`), the next `next()` can throw `ConcurrentModificationException`. It is best-effort, not a guarantee.

**Fail-safe / weakly consistent** (ConcurrentHashMap, CopyOnWriteArrayList): they do not throw. CopyOnWrite iterates a snapshot of the array. ConcurrentHashMap may see some new writes and miss others.

I do not modify a list inside foreach. I use `removeIf`, or I collect items to delete and then remove them.

---

### Q. CopyOnWriteArrayList?

**Answer**

It is a thread-safe List. Every **write** (`add`, `set`, `remove`) copies the **whole array**, applies the change, then swaps the reference. Readers keep iterating the old array. They never see a partial update and never get `ConcurrentModificationException`.

Writes are expensive. Reads are cheap and lock-free.

I use it for listener lists, plugin lists — **many reads, rare writes**. I do not use it as a normal ArrayList for request data.

CopyOnWriteArraySet is the same idea for a Set.

---

### Q. Can we use a custom object as a HashMap key? What changed in Java 8?

**Answer**

Yes. The object **must** implement `equals` and `hashCode` correctly, and it should be **immutable** while it is used as a key.

Java 8 change people mention: collision lists **treeify** after 8 nodes (if table ≥ 64). Worst-case get is O(log n) instead of O(n). Also `Key.hashCode` is mixed more carefully.

If they ask “Employee as key, I change the name after put” — the entry is lost. That is why keys should be immutable.

---

### Q. Why is HashMap capacity always a power of 2?

**Answer**

Index is `(n - 1) & hash`. If `n` is 16, `n-1` is 15, which is `1111` in binary. AND with hash keeps only the last 4 bits — a cheap modulo. If `n` were 15, this bit mask would be uneven and keys would cluster.

On resize, each node either stays or moves by **oldCap** (the extra bit). That is another reason for power of 2.

---

### Q. Java 7 HashMap could infinite-loop. What happened?

**Answer**

In Java 7, concurrent `put` on a HashMap (no lock) could corrupt a bucket into a **circular linked list** during resize. A `get` then looped forever and pinned a CPU core. Java 8 rewrote resize and treeified buckets, so that specific infinite loop is gone — but HashMap is **still not thread-safe**. You can still lose updates. Shared maps must be `ConcurrentHashMap` or fully synchronized.

---

### Q. WeakHashMap, IdentityHashMap, EnumMap, PriorityQueue, ArrayDeque?

**Answer**

**WeakHashMap** — keys are **weak references**. If nothing else points to the key, GC can remove the entry. Used for caches where you do not want keys to pin objects. Values are strong — if a value points back to the key, the entry will not be collected (common bug). Not for a general cache; use Caffeine/Guava with size/TTL.

**IdentityHashMap** — keys compared with `==`, not `equals`. Hash is `System.identityHashCode`. Used in serialization / cycle detection, not in business maps.

**EnumMap** — array indexed by enum ordinal. Fast and compact if all keys are one enum type.

**PriorityQueue** — binary heap. Offer/poll O(log n). Peek O(1). Not fully sorted if you iterate. Use for “next highest priority”. Comparator or Comparable required.

**ArrayDeque** — resizable array, efficient as stack or queue. **Faster than Stack and LinkedList** for queue/stack. No capacity waste of node pointers. I use `ArrayDeque`, not `Stack` (which is synchronized like Vector).

**TreeSet** — backed by TreeMap. Sorted, unique, O(log n). No HashMap hashing. `null` not allowed with natural order.

---

### Q. `List.of` vs `Collections.unmodifiableList` vs `new ArrayList`?

**Answer**

`Collections.unmodifiableList(list)` is a **view**. If someone still has the original list and mutates it, the “unmodifiable” view changes. `add` on the view throws `UnsupportedOperationException`.

`List.of(a, b)` (Java 9) is a truly immutable list. Nulls are not allowed. Changes to original variables later do not affect it (the references inside are still the same objects — immutability of the list is not deep immutability of elements).

For an API I return `List.copyOf(internal)` so the caller cannot change my internals.

---

### Q. How does `Collections.sort` work?

**Answer**

`List.sort` / `Collections.sort` uses **TimSort** (a stable merge sort tuned for real data). Average O(n log n). It is stable: equal elements keep order. For primitives, `Arrays.sort` uses dual-pivot quicksort (not stable). I mention this only if they ask “is sort stable?”

---

## 5. Java 8, 11, 17, 21

---

### Q. What did you use from Java 8?

**Answer**

In real projects I use:

- Lambda expressions
- Stream API (`filter`, `map`, `flatMap`, `collect`, `groupingBy`)
- Optional
- Method references (`Employee::getName`)
- `default` methods on interfaces
- `java.time` (`LocalDate`, `LocalDateTime`) instead of `Date`
- CompletableFuture (if they stay on Java 8 topics)

I also know later versions at a speaking level: Java 11 HTTP client / `var`; Java 17 records and text blocks (needed for Spring Boot 3); Java 21 virtual threads. In Round 1 they mostly stay on 8.

---

### Q. Intermediate vs terminal Stream operations?

**Answer**

A Stream pipeline is **lazy**. Nothing runs until a **terminal** operation.

**Intermediate** (return another Stream): `filter`, `map`, `flatMap`, `distinct`, `sorted`, `limit`, `peek`. They are not executed immediately. They can be chained.

**Terminal** (trigger work, return a non-stream): `collect`, `forEach`, `reduce`, `count`, `findFirst`, `anyMatch`. After a terminal operation the stream is **consumed**. You cannot reuse it.

```java
List<String> names = employees.stream()
    .filter(e -> e.getDept().equals("AV"))
    .map(Employee::getName)
    .sorted()
    .collect(Collectors.toList());
```

---

### Q. `map` vs `flatMap`?

**Answer**

`map` is one-to-one. Each input element becomes **one** output element. `Stream<Employee>` + `map(Employee::getName)` → `Stream<String>`.

`flatMap` is one-to-many, then **flatten**. Each input becomes a Stream, and those streams are merged into one.

Example: each Order has a list of Items. I want all items:

```java
List<Item> items = orders.stream()
    .flatMap(order -> order.getItems().stream())
    .collect(Collectors.toList());
```

If I used `map`, I would get `Stream<List<Item>>` — a stream of lists, not a stream of items.

Optional also has `flatMap` to avoid `Optional<Optional<T>>`.

---

### Q. Optional — how do you use it?

**Answer**

Optional is a box that may hold a value. I use it as a **return type** when “not found” is normal, instead of returning null.

```java
public Optional<Employee> findById(Long id) { ... }

Employee e = findById(id)
    .orElseThrow(() -> new NotFoundException("employee " + id));
```

Rules I follow:

- Do not use Optional as a **field**.
- Do not use Optional as a **method parameter**.
- Do not call `get()` without checking.
- `Optional.of(x)` throws if x is null. Use `Optional.ofNullable(x)`.
- Prefer `map` / `flatMap` / `orElse` / `orElseGet` / `orElseThrow`.

`orElseGet` is better when the default is expensive, because `orElse(compute())` always computes, even when the value is present.

---

### Q. Method references?

**Answer**

A method reference is a short lambda. `::` means “use this method”.

```java
list.sort(Comparator.comparing(Employee::getName));
list.forEach(System.out::println);
```

Types: `Class::staticMethod`, `instance::method`, `Class::instanceMethod`, `Class::new`.

I use them when the lambda would only call one method. If there is extra logic, I keep a lambda or a private method.

---

### Q. Functional interface? `default` method on interface?

**Answer**

A functional interface has **one abstract method**. `@FunctionalInterface` is optional but useful. Examples: `Runnable`, `Callable`, `Comparator`, `Function`, `Predicate`, `Supplier`. Lambdas target these.

`default` methods on interfaces let you add a method with a body without breaking every implementor. Stream API uses this. If two interfaces have the same default method, the class must override it (diamond problem).

---

### Q. Predicate, Function, Consumer, Supplier, BiFunction?

**Answer**

These are the built-in functional interfaces I actually name in interviews:

| Interface | Method | Meaning |
|---|---|---|
| `Predicate<T>` | `boolean test(T t)` | filter |
| `Function<T,R>` | `R apply(T t)` | map |
| `Consumer<T>` | `void accept(T t)` | forEach |
| `Supplier<T>` | `T get()` | factory, lazy default |
| `BiFunction<T,U,R>` | `R apply(T t, U u)` | merge two values |
| `UnaryOperator<T>` | `T apply(T t)` | Function where in and out are same type |

`Predicate.and` / `or` / `negate` compose filters. I do not invent new functional interfaces when these fit.

---

### Q. `groupingBy`, `partitioningBy`, `toMap` merge function?

**Answer**

```java
Map<String, List<Employee>> byDept =
    list.stream().collect(Collectors.groupingBy(Employee::getDept));

Map<String, Long> count =
    list.stream().collect(Collectors.groupingBy(Employee::getDept, Collectors.counting()));

Map<Boolean, List<Employee>> split =
    list.stream().collect(Collectors.partitioningBy(e -> e.getSalary() > 100000));
```

`partitioningBy` always has two buckets: true and false.

`toMap` **throws** if two keys collide, unless you give a merge function:

```java
.collect(Collectors.toMap(Employee::getId, e -> e, (old, dup) -> old));
```

That is how I remove duplicates by id.

---

### Q. `reduce` vs `collect`? `findFirst` vs `findAny`? `peek`?

**Answer**

**reduce** combines elements into one value (`sum`, `max`, custom). Identity + accumulator. For a mutable result (StringBuilder, List), **collect** is the right tool. `collect` is a mutable reduction with supplier, accumulator, combiner.

**findFirst** — first element in encounter order. **findAny** — any element; better for parallel streams when you do not care which match.

**peek** — see elements for debugging. I do not use peek for business logic; it is easy to skip if the pipeline is optimized. Use `map` if you need a transformation.

**Short-circuit:** `findFirst`, `anyMatch`, and `limit` can stop based on values. `collect` normally consumes the pipeline. `count()` is not value-short-circuiting, although an implementation may derive the count directly for a SIZED stream and skip traversal.

A stream is **one-shot**. Calling two terminals on the same stream throws `IllegalStateException`.

---

### Q. Parallel streams — when do you NOT use them?

**Answer**

`list.parallelStream()` splits work on the **common ForkJoinPool**. That pool is shared by the whole JVM.

I do **not** use parallel streams when:

- The list is small (overhead > gain)
- The lambda does blocking I/O (JDBC, HTTP) — it will stall the common pool
- I need a dedicated pool / timeout / naming
- Order matters and I forgot `forEachOrdered`
- There is shared mutable state in the lambda (race)

For CPU-heavy work on a large in-memory list, parallel can help. For a Spring request thread doing DB calls, I use an **ExecutorService** I own, not `parallelStream()`.

---

### Q. Checked exceptions inside lambdas?

**Answer**

Functional interfaces like `Function` do not declare checked exceptions. `map(this::readFile)` will not compile if `readFile` throws `IOException`. I wrap:

```java
.map(id -> {
    try {
        return read(id);
    } catch (IOException e) {
        throw new UncheckedIOException(e);
    }
})
```

Or I extract a helper. I do not swallow the exception.

---

### Q. Java 11 vs 17 vs 21 — what a 5–8 YOE should say

**Answer**

I do not recite every JEP. I say what changed for **backend work**.

**Java 11 (LTS)**

- `var` for local variables (Java 10). Type is still static; I use it when the right side is obvious.
- New HTTP Client (`java.net.http`).
- `String.isBlank`, `strip`, `repeat`, `files.readString`.
- Java EE modules removed from the JDK (need separate deps).
- Run a single `.java` file without compile step.

**Java 17 (LTS) — current corporate default for Spring Boot 3**

- **Records:** immutable data carriers. Compiler generates constructor, accessors (`name()`, not `getName()`), `equals`, `hashCode`, `toString`. Good for DTOs. Avoid them as ordinary JPA entities; entity lifecycle/proxy requirements do not fit record semantics, though providers may support records for projections/embeddables in specific versions.
- **Text blocks:** `""" ... """` for JSON/SQL.
- **Sealed classes:** I control which types may extend me. Good for domain modeling + exhaustive switch.
- **Pattern matching for instanceof:** `if (o instanceof String s) { ... }`
- Stronger encapsulation of JDK internals (`--add-opens` pain on old libraries).

**Java 21 (LTS)**

- **Virtual threads (Loom):** cheap threads scheduled by the JVM. Good for high numbers of **blocking** I/O tasks (JDBC, HTTP) without switching the whole app to reactive. I would not claim I rewrote a system on virtual threads unless I did. On **Java 21**, long blocking work inside `synchronized` or native/foreign calls can pin a carrier; newer JDKs reduce synchronized pinning, so state the JDK version.
- Sequenced collections (`getFirst` / `getLast`).
- Pattern matching for switch, record patterns.

**Interview line.** “Production services I have seen are mostly 8, 11, or 17. Boot 3 needs 17. I write streams like Java 8 because that is still the language of most business code.”

---

### Q. Records vs Lombok `@Value` vs normal immutable class?

**Answer**

A record is a transparent immutable tuple. I use it for API DTOs and return types.

I use records for DTOs/projections rather than normal JPA entities. Verify provider/version support before using a record for an embeddable or specialized mapping.

Lombok `@Value` generates similar code on a normal class. Records are language-level; no extra processor. If the team already uses Lombok everywhere, I do not start a holy war in Round 1 — I explain both.

---

## 6. Multithreading and JUC

---

### Q. Thread vs Runnable vs Callable? Why ExecutorService?

**Answer**

**Thread** is the worker. If I `new Thread()` for every task, I can create too many threads and kill the JVM.

**Runnable** is a task: `void run()`. No return value. Cannot throw checked exceptions.

**Callable** is a task: `V call()`. Returns a value. Can throw checked exceptions.

**ExecutorService** is a thread pool. I submit tasks. Threads are reused. I can bound the pool size and the queue.

```java
ExecutorService pool = Executors.newFixedThreadPool(10);
Future<Integer> f = pool.submit(() -> compute());
Integer result = f.get(2, TimeUnit.SECONDS);
pool.shutdown();
```

I do not use `newCachedThreadPool` in production without a limit — it can create unbounded threads. I prefer a **fixed** or **bounded** pool, a bounded queue, and a rejection policy (`CallerRunsPolicy` or fail). I name threads. I always shut down the pool.

---

### Q. Future vs CompletableFuture?

**Answer**

`Future` is the old handle. `get()` **blocks** until the result is ready. You cannot easily chain two async calls.

`CompletableFuture` can run async, then `thenApply` (transform), `thenCompose` (chain another async), `thenCombine` (wait for two), `exceptionally` (handle error), `allOf` (wait for many).

```java
CompletableFuture<Price> p = CompletableFuture.supplyAsync(() -> priceClient.get(id), ioPool);
CompletableFuture<Stock> s = CompletableFuture.supplyAsync(() -> stockClient.get(id), ioPool);
PriceQuote q = p.thenCombine(s, this::merge).get(3, TimeUnit.SECONDS);
```

Always pass **your own executor**. The common ForkJoinPool is shared and can starve the rest of the app.

---

### Q. synchronized vs ReentrantLock vs volatile vs AtomicInteger?

**Answer**

**synchronized** — only one thread at a time in that block/method, for that lock object. Simple. The lock is released even if an exception is thrown. You cannot try to lock with a timeout. Use it for short critical sections.

**ReentrantLock** — same idea, but you can `tryLock(1, SECONDS)`, interrupt waiting threads, and have multiple `Condition` objects. You **must** `unlock()` in `finally`. If you forget, you deadlock.

**volatile** — makes a write **visible** to other threads. It does **not** make `count++` atomic. `count++` is read, add, write — two threads can lose an increment. I use volatile for flags like `running = false`.

**AtomicInteger** — `incrementAndGet()` uses CAS (compare-and-swap). Good for counters without a full lock. Under very high contention, `LongAdder` is better.

---

### Q. What is deadlock? How do you avoid it?

**Answer**

Deadlock is when two threads wait for each other forever.

Example: Thread-1 locks Account A, then wants Account B. Thread-2 locks Account B, then wants Account A. Both wait.

**How I avoid it**

1. Always take locks in the **same order** (always smaller account id first).
2. Use `tryLock` with timeout and back off.
3. Do not call unknown code (listeners) while holding a lock.
4. Prefer concurrent collections over nested synchronized blocks.

**How I detect it**

Thread dump (`jstack`). You see “waiting to lock” cycles. `ThreadMXBean.findDeadlockedThreads()` can also report it.

**Related words they may use**

- **Starvation:** a thread never gets the lock because others keep winning.
- **Livelock:** threads keep releasing and retrying and never make progress.
- **Race condition:** result depends on lucky timing (lost update on `count++`).

---

### Q. wait / notify vs sleep?

**Answer**

`Thread.sleep` pauses the thread. It does **not** release a monitor/lock you hold. It just waits for time.

`wait()` must be called inside `synchronized` on that object. It **releases** the monitor and waits until `notify`/`notifyAll`. Always wait in a **loop** because of spurious wakeup:

```java
synchronized (lock) {
    while (!ready) {
        lock.wait();
    }
    // work
}
```

`notify` wakes one waiter. `notifyAll` wakes all. I usually use `notifyAll` unless I am sure only one waiter should run.

`join()` waits for **another thread to finish**. If they say “odd thread must run completely before even thread”, that is `join`, not wait/notify.

---

### Q. ThreadLocal?

**Answer**

ThreadLocal stores a value **per thread**. Useful for a request id / MDC correlation id / tenant id.

Danger: in a **thread pool**, the same thread handles the next request. If I do not `remove()` in a `finally`, the next request can see the old user id. That is a data leak. It can also leak memory.

```java
try {
    TENANT.set(tenantId);
    chain.doFilter(req, res);
} finally {
    TENANT.remove();
}
```

---

### Q. How do you print even and odd with two threads? (they ask this a lot)

**Answer (concept — code is in the coding section)**

Two threads share a lock and a boolean `oddTurn`. The odd thread waits until `oddTurn` is true, prints, flips the flag, notifies. The even thread does the opposite. Use `while` + `wait`, not `if`.

If they only want “odd first, then even”, start odd, and even calls `oddThread.join()` before printing — that is a different question.

---

### Q. Thread life cycle? Daemon vs user thread? interrupt?

**Answer**

States: **NEW** (created, not started) → **RUNNABLE** (running or ready) → **BLOCKED** (waiting for a monitor) / **WAITING** (`wait`, `join` without timeout) / **TIMED_WAITING** (`sleep`, `wait(timeout)`) → **TERMINATED**.

You cannot restart a terminated thread.

**User threads** keep the JVM alive. **Daemon threads** (GC, some timers) do not. When the last user thread ends, the JVM exits and daemon threads are killed. Tomcat worker threads are user threads. I do not run important business work as daemon unless I accept sudden death.

**interrupt:** does not stop a thread by force. It sets a flag. If the thread is in `wait`/`sleep`/`join`, it throws `InterruptedException`. The correct pattern:

```java
} catch (InterruptedException e) {
    Thread.currentThread().interrupt(); // restore the flag
    return; // stop work
}
```

Swallowing `InterruptedException` without restoring the flag is a bug. `Thread.stop()` is deprecated and unsafe.

---

### Q. `synchronized` on instance vs static vs a private lock object?

**Answer**

- Instance method `synchronized`: lock is `this`. Two threads can run it on **different** instances at the same time.
- Static `synchronized`: lock is the **Class** object. One lock for the whole class.
- `synchronized(lock)` block: I use a **private final Object lock**. I do not lock on `this` if I can avoid it — callers could also lock on my public instance and deadlock with me. I never lock on a `String` literal (it is interned and shared).

**Reentrant:** the same thread can take the same lock again (synchronized methods calling each other). Count is incremented. That is why `ReentrantLock` has that name.

---

### Q. Java Memory Model? happens-before? Why volatile is not enough for `count++`?

**Answer**

The JMM defines when a write by one thread becomes visible to another.

**Happens-before** (what I name):

- Unlock of monitor **happens-before** a later lock of the same monitor.
- Write to `volatile` **happens-before** a later read of that same variable.
- Thread `start` happens-before the first action in that thread.
- Last action in a thread happens-before `join` returns in another thread.

Without these, a thread can keep a field in a CPU cache/register and never see another thread’s write.

`volatile` gives **visibility** and **ordering** for that variable. `count++` is still three steps: read, add, write. Two threads can both read 5 and both write 6. Use `AtomicInteger` or a lock for a counter.

---

### Q. Singleton — all the ways, and which one you pick?

**Answer**

1. **Eager:** `private static final Foo INSTANCE = new Foo();` — simple, thread-safe, created even if unused.
2. **Synchronized method `getInstance`:** correct, slow after startup.
3. **Double-checked locking:** must use `private static volatile Foo instance`. Without `volatile`, a thread can see a half-constructed object (safe publication). Java 5+ volatile fixes this.
4. **Holder (Bill Pugh):** nested static class loaded on first `getInstance`. JVM class-init lock makes it thread-safe. No explicit synchronized. I like this.
5. **Enum:** `INSTANCE;` — thread-safe, serialization-safe. Best if I need a true singleton.

**How singleton breaks:** reflection can call a private constructor (guard with a flag). Serialization creates a new object unless `readResolve()` returns `INSTANCE`. Clone can copy it unless you throw in `clone()`.

**Spring:** a Spring bean default **scope singleton** is “one instance per container”, not the Gang of Four pattern. I do not write enum singletons for services. I let Spring create one bean.

---

### Q. ThreadPoolExecutor — core, max, queue, rejection? Factory methods to avoid?

**Answer**

`ThreadPoolExecutor(core, max, keepAlive, unit, queue, factory, handler)`

- **corePoolSize:** threads kept even if idle (unless allowCoreThreadTimeOut).
- **maxPoolSize:** hard cap.
- **workQueue:** if all core threads are busy, new tasks go to the queue. **Only when the queue is full** are extra threads created up to max. This surprises people: an **unbounded** `LinkedBlockingQueue` means max is never used.
- **keepAlive:** extra threads above core die after idle time.
- **handler:** what to do when max + queue are full.

Rejection policies:

- `AbortPolicy` (default) — throw `RejectedExecutionException`
- `CallerRunsPolicy` — the submitting thread runs the task (backpressure)
- `DiscardPolicy` — drop
- `DiscardOldestPolicy` — drop oldest in queue

**I avoid:** `Executors.newCachedThreadPool()` (unbounded threads), `newFixedThreadPool` with an unbounded queue if I need failure visibility. I prefer a bounded queue + `CallerRunsPolicy` or abort + metrics.

`shutdown()` waits for queued tasks. `shutdownNow()` interrupts workers and returns unfinished tasks. I call `awaitTermination`.

---

### Q. CountDownLatch vs CyclicBarrier vs Semaphore vs Phaser?

**Answer**

**CountDownLatch** — one or more threads wait until N events happen. Count goes down only. Cannot reset. Example: wait for 3 services to start, then accept traffic. Main thread `latch.await()`, each starter `countDown()`.

**CyclicBarrier** — N threads wait **for each other**, then all continue. Can be reused (cyclic). Optional barrier action. Example: all workers finish phase 1, then phase 2.

**Semaphore** — N permits. `acquire` / `release`. Limit concurrent calls to a slow API (bulkhead). Fair vs unfair.

**Phaser** — like a more flexible barrier. Parties can register/deregister. I mention it; I rarely use it in business code.

**Exchanger** — two threads swap an object. Rare in interviews after you name it.

**ReadWriteLock / StampedLock:** many readers, one writer. Good for a read-heavy in-memory structure. If reads are very short, a simple `synchronized` or ConcurrentHashMap is often better. StampedLock is optimistic read; easy to get wrong — I only mention it.

---

### Q. BlockingQueue and producer–consumer?

**Answer**

`BlockingQueue` is the standard in-process producer–consumer:

- `put` waits if full (backpressure)
- `take` waits if empty

**ArrayBlockingQueue** — bounded array, optional fairness.

**LinkedBlockingQueue** — linked nodes, optionally bounded (default huge).

**PriorityBlockingQueue** — unbounded, ordered by priority.

**SynchronousQueue** — no storage; `put` waits for a `take`. Cached thread pools use this.

**DelayQueue** — elements become available after a delay.

I use a **bounded** `ArrayBlockingQueue` so producers slow down instead of eating RAM. Across processes I use Kafka, not a JVM queue.

---

### Q. ForkJoinPool vs normal Executor? RecursiveTask?

**Answer**

ForkJoinPool is for **CPU** work that **splits** (divide and conquer): sort, tree walk, parallel streams. Workers can **steal** tasks from other workers’ queues.

`RecursiveTask<V>` returns a value; `RecursiveAction` does not. `fork()` schedules, `join()` waits. For I/O I do not use ForkJoin — I use a normal thread pool.

Parallel streams use the **common** ForkJoinPool. I can pass a custom pool by running inside `pool.submit(() -> list.parallelStream()...)`.

---

### Q. CompletableFuture: `get` vs `join`, `thenApply` vs `thenCompose`, `handle` vs `exceptionally`?

**Answer**

- `get()` throws checked `ExecutionException` / `InterruptedException`.
- `join()` throws unchecked `CompletionException`. Easier inside lambdas.
- `thenApply` — map the result (like `map`).
- `thenCompose` — flatMap when the next step already returns a CompletableFuture (avoid `CompletableFuture<CompletableFuture<T>>`).
- `thenCombine` — wait for two independent futures.
- `allOf` — wait for many; then `join` each.
- `exceptionally` — recover from error, return a fallback.
- `handle` — always called with (result, exception); one of them is null.

Always pass an executor for I/O. Always set a timeout (`orTimeout` / `get(3, SECONDS)`).

---

## 7. JVM, GC, references

---

### Q. JVM architecture in one minute?

**Answer**

- **Class loader** loads `.class` bytes.
- **Runtime data:**
  - **Method area / Metaspace** — class metadata, static variables, constant pool of the class
  - **Heap** — objects
  - **Java stacks** — per thread, frames
  - **PC register** — per thread, current instruction
  - **Native method stack** — JNI
- **Execution engine:** interpreter + **JIT** (HotSpot compiles hot methods to native). C1 (client, faster compile) and C2 (server, better optimization).
- **GC** reclaims heap.

I do not draw every box unless they ask. I can talk heap vs stack vs metaspace clearly.

---

### Q. Class loading? Bootstrap vs platform vs application? `Class.forName` vs `loadClass`?

**Answer**

Delegation model: child asks **parent first**.

1. **Bootstrap** — `java.base` / rt-like modules. Native, no Java `ClassLoader` object.
2. **Platform** (old “extension”) — other JDK modules.
3. **Application / system** — classpath / module path.

A class is unique by **name + classloader**. Two copies of `com.foo.Bar` in two loaders are two types. That is the classic “cannot cast” in web apps.

**`Class.forName("x")`** — loads **and initializes** the class (static blocks run). JDBC drivers used this.

**`classLoader.loadClass("x")`** — loads but may **not** initialize until first active use.

**`new` / method call / access static** trigger initialization.

Parent-first prevents you from replacing `java.lang.String`. Child-first (some web loaders) lets a WAR ship its own library version.

---

### Q. Young vs Old generation? Minor vs Major vs Mixed GC? G1 vs ZGC?

**Answer**

New objects go to **Eden**. When Eden fills, a **minor GC** copies live objects to a **Survivor** space. After surviving several cycles (age threshold), they are **promoted** to Old.

**Old / Tenured** holds long-lived objects. Collecting it is more expensive.

**G1 (default, Java 9+):** heap is split into **regions**. It can mix young and a bit of old in one pause (**mixed GC**). You give a pause target (`-XX:MaxGCPauseMillis`). Good default for Spring services.

**Parallel GC:** throughput, longer pauses. Batch jobs.

**Serial GC:** one thread, tiny heaps / containers.

**CMS:** old, removed. Do not propose it.

**ZGC / Shenandoah:** concurrent, very short pauses, large heaps. Use when pause time is the product requirement and you have measured G1 is not enough.

**Follow-up:** `OutOfMemoryError: Java heap space` — heap full. `GC overhead limit exceeded` — GC running constantly, little progress. `Metaspace` — too many classes / classloader leak. `Unable to create native thread` — OS thread limit, not heap.

---

### Q. GC roots? How do you find a memory leak?

**Answer**

GC roots: local variables on stacks, static fields, JNI references, something the JVM must keep.

An object is live if a chain from a root reaches it.

**Leak in Java** is “objects still reachable that we do not need”: static Map, unbounded cache, ThreadLocal on a pool, listeners not removed, unterminated threads holding a huge graph.

**How I investigate:** heap dump (`jcmd` / `jmap`) → Eclipse MAT / VisualVM → Dominator tree. Look for unexpected retained size. `jstat` for GC frequency. `jstack` for stuck threads. I mention the tools; I do not pretend I memorize every MAT button.

---

### Q. Strong vs Soft vs Weak vs Phantom references?

**Answer**

- **Strong** — normal `Employee e = ...`. GC will not collect while a strong ref exists.
- **Soft** — collected when the JVM really needs memory. Old “cache” idea. Today I use a sized cache, not SoftReference.
- **Weak** — collected at next GC when only weak refs remain. `WeakHashMap` keys. `ThreadLocal` internals use weak keys for the `ThreadLocal` object itself.
- **Phantom** — enqueued after the object becomes phantom-reachable; `get()` always returns null. It supports post-mortem cleanup coordination through a `ReferenceQueue`. `Cleaner` (Java 9) is the safer modern API. Finalization is deprecated and is not a required step.

---

### Q. JIT, escape analysis, why “interpreted vs compiled” matters?

**Answer**

Hot methods get compiled to native code. That is why a microbenchmark without warmup lies.

**Escape analysis:** if an object never leaves a method, the JIT may allocate it on the stack or **scalar-replace** it (keep fields in registers). That is why “I allocated fewer objects” is not always visible.

I mention this only if they ask why production is faster than a unit test loop.

---

### Q. Direct memory / NIO `ByteBuffer`?

**Answer**

`ByteBuffer.allocateDirect` uses memory **off-heap**. GC does not manage it the same way. Useful for I/O (less copy to the kernel). You can OOM native memory (`OutOfMemoryError: Direct buffer memory`) while the heap looks fine. Netty / Kafka clients use this. I do not allocate huge direct buffers in business code without a cap.

**IO vs NIO:** classic IO is stream + blocking. NIO is channels + buffers + selectors (one thread many sockets). For a normal Spring REST service I still use servlet Tomcat threads. NIO matters in gateways and high-connection servers.

---

## 8. Generics, serialization, singleton

---

### Q. What is type erasure? Why not `new T()`? Why not `List<String>[]`?

**Answer**

Generics are **compile-time**. At runtime, `List<String>` and `List<Integer>` are both `List`. The compiler inserts casts. That is **erasure**.

So you cannot:

- `new T()`
- `new T[]`
- `instanceof List<String>` (you can `instanceof List`)

You **can** `new ArrayList<String>()` because the constructor does not need T at runtime.

**Heap pollution:** mixing raw types (`List`) with generics, then a `ClassCastException` later at a cast the compiler inserted. That is why raw types are a warning.

**PECS:** Producer Extends, Consumer Super.

- I **produce** (read) from `List<? extends Number>` — I can get `Number`, I cannot add (except null).
- I **consume** (write) to `List<? super Integer>` — I can add `Integer`, get returns `Object`.

`Collections.copy(dest, src)` is the textbook example.

---

### Q. Bounded types? `T extends Comparable<? super T>`?

**Answer**

`T extends Number` — T must be Number or subclass.

`T extends Comparable<T>` is too tight (`java.sql.Timestamp` extends `Date` which is `Comparable<Date>`). The library uses `T extends Comparable<? super T>` so a type can be compared as its parent type. I mention it if they ask about `Collections.sort` signature.

---

### Q. Serializable vs Externalizable? `serialVersionUID`? `transient`?

**Answer**

**Serializable** is a marker. The JVM writes the object graph. Slow, fragile, a security risk if you deserialize untrusted bytes (gadget attacks). I do **not** use Java serialization for REST or Kafka. I use JSON / Avro / protobuf.

They still ask it.

- **`serialVersionUID`:** if missing, the compiler generates one from the class shape. Change a field, the UID changes, old bytes throw `InvalidClassException`. I set it explicitly if I must serialize.
- **`transient`:** field is skipped. Useful for passwords, derived cache, ThreadLocal-like state.
- Custom `writeObject` / `readObject` for extra control.
- **Externalizable:** you write `writeExternal` / `readExternal` yourself. Full control, more code.

**JSON vs Java serialization:** JSON is text, versioning is fields, no `ObjectInputStream` exploits. Spring uses Jackson. That is what I use.

---

### Q. How does serialization break singleton? `readResolve`?

**Answer**

`ObjectInputStream` can create an object **without** the normal constructor path and return a **second** instance.

Fix:

```java
private Object readResolve() {
    return INSTANCE;
}
```

Enum singletons do not have this problem. Reflection: in the constructor, `if (INSTANCE != null) throw ...`.

---

### Q. Cloneable problems?

**Answer**

`clone()` is not in `Cloneable` — it is on `Object` and is `protected`. Cloneable is a marker that changes `clone` from throwing `CloneNotSupportedException` to doing a **shallow** field copy.

Problems: shallow copy of mutable fields, constructors are skipped (final fields / invariants), easy to forget to clone children. I prefer a **copy constructor** or a mapper.

---

### Q. Reflection and annotations (what they expect at 5–8 YOE)?

**Answer**

Reflection: `Class.forName`, `getDeclaredField`, `setAccessible(true)`, `invoke`. Spring uses it heavily (proxy, injection). Cost: slower, breaks encapsulation, skipped constructors sometimes, modules (Java 17) may deny access.

I use reflection in libraries and tests, not in a hot request path.

**Annotation:** metadata. `@Retention(RUNTIME)` is visible via reflection (Spring). `@Retention(SOURCE)` is for the compiler (Lombok-style, `@Override`). `@Retention(CLASS)` is in the class file but not runtime by default.

**Custom annotation:** `@interface`, elements with defaults. Spring’s `@Transactional` is a runtime annotation processed by a proxy, not by the JVM itself.

---

### Q. SOLID in one minute (they sometimes mix this into Java round)?

**Answer**

- **S**ingle responsibility — a class has one reason to change.
- **O**pen/closed — extend with new types, not by editing a 500-line `if`.
- **L**iskov — subclass must be usable as the parent (no `UnsupportedOperationException` surprise).
- **I**nterface segregation — many small interfaces, not one fat one.
- **D**ependency inversion — depend on `PaymentPort`, not `StripeClient` directly. Spring constructor injection is this.

I give one example from my service layer and stop.

---

## 9. Spring Boot

---

### Q. Difference between Spring and Spring Boot?

**Answer**

**Spring** is the framework: IoC container, MVC, Security, Data, AOP. You configure a lot yourself (XML or Java config). You provide a server, pick versions, wire DataSource, DispatcherServlet, etc.

**Spring Boot** sits on top of Spring. It is opinionated. You add a **starter** (`spring-boot-starter-web`), Boot sees Tomcat and Spring MVC on the classpath, and it **auto-configures** an embedded server, JSON converters, default logging. You get `main` + `application.yml` + Actuator.

Boot does not replace Spring. A Boot app is still a Spring app. It just starts faster with less XML.

**Advantages I mention**

- Starters (dependency set that works together)
- Auto-configuration
- Embedded Tomcat / Jetty
- Actuator (health, metrics)
- Easy external config (`application.yml`, env, profiles)

---

### Q. What is IoC? What is a Spring Bean? How do you inject?

**Answer**

**IoC (Inversion of Control)** means I do not `new` my dependencies. The **container** creates objects, injects other beans, and manages lifecycle (create → inject → `@PostConstruct` → use → `@PreDestroy`).

A **Spring Bean** is any object the container manages.

**How I register beans**

1. Stereotype annotations: `@Component`, `@Service`, `@Repository`, `@Controller`
2. `@Bean` methods on a `@Configuration` class
3. XML (legacy)

**How I inject**

Constructor injection is what I use:

```java
@Service
public class OrderService {
    private final OrderRepository repo;
    public OrderService(OrderRepository repo) {
        this.repo = repo;
    }
}
```

Why constructor: required dependencies are clear, fields can be `final`, tests are easy (`new OrderService(mockRepo)`), and Spring can wire even without `@Autowired` on a single constructor.

Field `@Autowired` works but hides required deps and makes tests harder. Setter injection is for optional deps.

Spring injects **by type**. If two beans have the same type, I use `@Qualifier("beanName")` or `@Primary`.

---

### Q. Bean scopes? Is singleton thread-safe?

**Answer**

Default scope is **singleton** — one instance per Spring context. Controllers and services are singletons.

**prototype** — a new instance every time the bean is requested from the container.

Web scopes: **request** (one per HTTP request), **session**.

**Important trap:** if a singleton bean has a prototype dependency injected in the constructor, that prototype is created **once** and held forever. To get a new prototype each time, inject `ObjectFactory<T>` or use `@Lookup`.

**Singleton and threads:** the bean is shared by all requests. I must not store request data in instance fields (`currentUser` as a field is a bug). Keep state in method parameters, or use a thread-safe store (DB, ConcurrentHashMap with care, or request attributes).

---

### Q. @Component vs @Service vs @Repository vs @Controller — can we swap them?

**Answer**

All of them are `@Component`. Component scan will pick them up.

- `@Controller` / `@RestController` — web layer. `@RequestMapping` methods become HTTP endpoints. **You cannot swap this with `@Service`.** If you put only `@Service` on a controller class, URLs will not map. If you put `@Controller` on a service, it becomes a web handler by mistake.
- `@Service` — business logic. Documentation + AOP pointcuts.
- `@Repository` — persistence. Extra: Spring translates DB exceptions into `DataAccessException`.
- `@Component` — generic bean (filters, helpers).

**Can we swap `@Service` and `@Repository`?** Technically the app may still start. It is not recommended. Exception translation and meaning are lost.

This is a real interview question (CitiusTech-style). Answer clearly: Controller stays Controller. Service/Repository should not be swapped even if scan still works.

---

### Q. How does `@Transactional` work? What is the self-invocation problem?

**Answer**

`@Transactional` is **not** magic inside the method. Spring puts a **proxy** around the bean. When another bean calls `orderService.placeOrder()`, the call hits the proxy first. The proxy starts a transaction (or joins one), calls the real method, then commits. If a **runtime** exception is thrown, it rolls back. Checked exceptions do **not** roll back unless you set `rollbackFor`.

**Self-invocation:** if `placeOrder()` calls `this.saveAudit()` in the **same class**, that call does **not** go through the proxy. `@Transactional` on `saveAudit` is ignored. Fix: move `saveAudit` to another bean, or inject self.

**Propagation I actually mention**

- `REQUIRED` (default): join the current transaction, or start one.
- `REQUIRES_NEW`: pause the current one, start a new one. Audit that must save even if the parent fails.
- `NOT_SUPPORTED`: run with no transaction.

**Isolation:** database isolation. Default is usually whatever the DB uses (Oracle: READ COMMITTED). I do not change isolation unless I have a reason.

**Read-only:** `readOnly = true` hints the provider; Hibernate can skip flushes.

**Proxy + `@Async`:** the async method runs on another thread. The transaction ThreadLocal does not follow. The async method needs its own transaction.

---

### Q. @ControllerAdvice and @ExceptionHandler?

**Answer**

`@ExceptionHandler` on a controller handles exceptions from **that** controller.

`@ControllerAdvice` is **global**. One class maps exceptions to HTTP responses for the whole app.

```java
@ControllerAdvice
public class ApiErrors {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorBody> notFound(NotFoundException ex) {
        return ResponseEntity.status(404).body(new ErrorBody(ex.getMessage()));
    }
}
```

I return a simple body: message, error code, trace id. I log the stack trace. I do not send the stack trace to the client.

---

### Q. Spring profiles? Actuator? default port?

**Answer**

Profiles: `dev`, `qa`, `prod`. I set `spring.profiles.active=prod`. Beans with `@Profile("prod")` load only then. Files: `application.yml` + `application-prod.yml`.

Default port is **8080**. Change with `server.port=9090`.

**Actuator** gives production endpoints: `/actuator/health`, `/actuator/metrics`, `/actuator/info`. I expose health to the load balancer. I **protect** `env`, `heapdump`, `beans`. Health can include DB down = not ready.

---

### Q. Auto-configuration in simple words?

**Answer**

Boot looks at the **classpath**. If it sees `DataSource` classes and you did not define a DataSource bean, it creates one from `spring.datasource.*`. If it sees Spring MVC, it sets up DispatcherServlet and embedded Tomcat. Conditions like `@ConditionalOnClass` and `@ConditionalOnMissingBean` drive this. If I define my own bean, Boot backs off.

---

### Q. RestTemplate vs WebClient? Filter vs Interceptor?

**Answer**

**RestTemplate** is blocking HTTP. Very common in Spring Boot 2 projects. It is in maintenance mode.

**WebClient** is the newer API (can be non-blocking). Spring Boot 3.2+ also has **RestClient** as a modern blocking API.

For Round 1: “I have used RestTemplate with a connection pool and timeouts. I always set connect and read timeout. I do not use the default `SimpleClientHttpRequestFactory` without timeouts.”

**Filter** (servlet): runs around the whole request, before Spring MVC. CORS, authentication, gzip.

**Interceptor** (`HandlerInterceptor`): Spring MVC, around the controller. `preHandle` / `postHandle` / `afterCompletion`. Logging, intercepting controller calls.

Filter is lower; Interceptor is closer to the controller.

---

### Q. PUT vs POST vs PATCH vs DELETE? Can we update with POST?

**Answer**

- **GET** — read. No body required. Should not change state.
- **POST** — create, or an action that is not idempotent.
- **PUT** — replace the whole resource. Calling it twice with the same body should end in the same state (idempotent).
- **PATCH** — partial update.
- **DELETE** — remove. Idempotent: deleting twice is still “gone”.

Can we update or delete with POST? HTTP will allow it. Many old APIs do only POST. REST style says use PUT/PATCH/DELETE so retries and intent are clear. I say: “It works. I still prefer PUT/PATCH for updates.”

---

### Q. JPA: Lazy vs Eager? What is N+1?

**Answer**

**LAZY:** related data is loaded when you first touch it (`order.getItems()`). Default for `@OneToMany`.

**EAGER:** loaded together with the parent. Default for `@ManyToOne` — this often surprises people.

**N+1 problem:** you load 50 orders (1 query). Then in a loop you call `order.getItems()` and Hibernate runs **50 more queries**. Total 51. That kills the DB.

**How I fix it**

- JPQL `join fetch o.items`
- `@EntityGraph`
- `hibernate.default_batch_fetch_size` so it loads items in batches, not one-by-one
- Do not return entities to the API if you only need three fields — use a DTO query

I do not set everything EAGER. That makes other screens slower.

**Open Session In View:** Boot often keeps the session open until the HTTP response is written, so lazy load “works” in the controller. It hides N+1 until production. I prefer OSIV off and explicit fetch.

---

### Q. If the service makes too many DB calls, how do you optimize?

**Answer**

This came up in real interviews. I talk in this order:

1. **Find N+1** — turn on SQL logging. If I see a query per row, fix fetch.
2. **Batch** — one `WHERE id IN (...)` instead of a query per id. For inserts, `hibernate.jdbc.batch_size` and `entityManager.flush(); clear();` in a loop so the persistence context does not hold 100k entities.
3. **Index** the columns in the WHERE/JOIN.
4. **Cache** data that rarely changes (config, country list) with a TTL.
5. **Connection pool** — do not open a connection per tiny call; reuse Hikari/c3p0. In my current ESM project the pool is **c3p0**, not Hikari. Default Boot is Hikari.
6. Pagination — do not load 50,000 rows into memory.

EntityManager is the persistence context: first-level cache of entities in that transaction. It is not a replacement for fixing the query.

---

### Q. @PostConstruct? Bean lifecycle in one line?

**Answer**

Container creates the object → injects dependencies → calls `@PostConstruct` → bean is ready → on shutdown `@PreDestroy`.

I use `@PostConstruct` to validate config. I do not call other microservices there; I use `ApplicationReadyEvent` if I need the whole context up.

---

### Q. How does a Spring MVC request flow?

**Answer**

Request hits servlet filters → `DispatcherServlet` → HandlerMapping finds the controller method → arguments are converted (`@RequestBody` JSON → object) → controller → service → repository → DB → return `ResponseEntity` → JSON converter → filters on the way out.

If an exception is thrown, `@ControllerAdvice` handles it.

---

## 10. Hibernate and JPA

Hibernate is the usual JPA **implementation**. JPA is the **API** (`EntityManager`, annotations). Spring Data JPA sits on top and gives repositories. At 5–8 years they want **entity states, persistence context, fetch, locking**, not “JPA is for CRUD”.

---

### Q. JPA vs Hibernate vs Spring Data JPA?

**Answer**

**JPA** is the specification: `EntityManager`, `@Entity`, JPQL.

**Hibernate** implements JPA and adds extras: `@BatchSize`, `@DynamicUpdate`, Hibernate Types, own Session API.

**Spring Data JPA** gives `JpaRepository`, derived query names, `@Query`. Under the hood it still uses EntityManager/Hibernate.

I say “we use Spring Data JPA with Hibernate on Oracle.” I do not say they are three unrelated tools.

---

### Q. Entity states: transient, persistent, detached, removed?

**Answer**

- **Transient:** `new Employee()` — not known to Hibernate, no row yet.
- **Persistent (managed):** Hibernate tracks it in the **persistence context**. Changes are flushed to DB at flush/commit. `persist` / `find` / query results in a session.
- **Detached:** it **has** an id, but the session is closed (or we `evict`). Changes are **not** saved unless we `merge`.
- **Removed:** marked for delete; deleted on flush.

**Interview line.** “The persistence context is the first-level cache. One EntityManager / Session per transaction in a typical Spring app. Same id loaded twice in one TX returns the **same object**.”

---

### Q. `persist` vs `merge` vs `save` vs `update` vs `saveOrUpdate`? `find` vs `getReference`?

**Answer**

JPA:

- **`persist`:** for **new** entities (transient → persistent). Id may be assigned on persist or on flush (depends on generator). If you persist a detached entity, you get an exception.
- **`merge`:** copy state onto a **persistent** instance. Returns the managed instance. Use for detached updates. The object you passed in may still be detached — use the **return value**.
- **`remove`:** delete.
- **`find(id)`:** hits persistence context, then DB. Returns null if missing. Initializes the entity.
- **`getReference(id)`** (Hibernate `load`): returns a **proxy** without hitting DB until you access a field. If the row does not exist, you fail later (`EntityNotFoundException`). Useful to set an association without loading the whole graph.

Hibernate-only (legacy Session): `save`, `update`, `saveOrUpdate`. In new code I stick to JPA `persist`/`merge`. Spring Data `save()` uses the repository’s `isNew` detection (normally version first, then id, or custom `Persistable`) to choose persist vs merge. “Id null means persist” is a common simplification, not the complete rule.

---

### Q. First-level cache, second-level cache, query cache?

**Answer**

**First-level:** the persistence context. Always on. Per session/transaction. Guarantees identity (`==` for same id in one session).

**Second-level:** shared across sessions (Ehcache, Infinispan, Redis via provider). Entity cache by id. I use it only for **read-mostly** reference data, with a clear invalidation story. Wrong cache = stale money/inventory.

**Query cache:** caches query **result ids**, then loads entities (often from L2). Easy to get stale. I rarely turn it on.

**Interview line.** “L1 is not optional. L2 is a product decision. I do not cache rapidly changing service rows in ESM.”

---

### Q. Dirty checking and flush? FlushMode?

**Answer**

Hibernate **snapshots** entity state when it loads. At **flush**, it compares current fields to the snapshot and emits SQL UPDATEs. You do not call `update()` on a managed entity — you change fields and flush/commit.

Flush happens: before query (so the query sees your changes), at commit, or `entityManager.flush()`.

`FlushMode.AUTO` (default) vs `COMMIT` (flush only at commit — faster, query may not see unflushed writes).

**`@DynamicUpdate`:** UPDATE only dirty columns. Helps wide tables. Default Hibernate updates all columns.

---

### Q. LazyInitializationException? Open Session in View?

**Answer**

Session is closed after the `@Transactional` service method. If the controller then touches a lazy collection, Hibernate throws **LazyInitializationException**.

Fixes: fetch in the service (`join fetch` / entity graph), map to DTO **inside** the transaction, or (worse) OSIV.

**OSIV** (`spring.jpa.open-in-view=true` is Boot default): session stays open for the whole HTTP request. Lazy load “works” in Jackson serialization and **hides N+1** until production load. I prefer OSIV **off** and explicit fetch.

---

### Q. Owning side? `mappedBy`? `@JoinColumn` vs join table? Cascade vs orphanRemoval?

**Answer**

In a **bidirectional** association, one side **owns** the foreign key. The other is inverse: `mappedBy = "order"`.

Hibernate **only persists the owning side**. If I add a child to `order.getItems()` but do not set `item.setOrder(order)`, the FK may stay null.

`@ManyToOne` + `@JoinColumn` = FK on the many table (usual).

`@ManyToMany` / extra columns → join table. For many-to-many I often use an explicit link entity instead of `@ManyToMany`.

**Cascade** (`PERSIST`, `MERGE`, `REMOVE`, `ALL`): operations on parent apply to children. `CascadeType.ALL` on a `@ManyToOne` to a shared parent is dangerous (deleting an order deletes the customer).

**orphanRemoval = true:** remove a child from the collection → DELETE that child. Different from cascade REMOVE (delete parent deletes children). Use on true composition (order lines), not on shared references.

---

### Q. JPA inheritance: SINGLE_TABLE vs JOINED vs TABLE_PER_CLASS?

**Answer**

- **SINGLE_TABLE:** one table, discriminator column. Fast reads, many nullable columns.
- **JOINED:** base table + subclass tables. Normalized, more joins.
- **TABLE_PER_CLASS:** one table per concrete class, union queries. Rarely my default.

I pick SINGLE_TABLE for a small closed hierarchy. I pick JOINED if subclasses have many exclusive fields.

---

### Q. ID generators: IDENTITY vs SEQUENCE? Why batch insert fails with IDENTITY?

**Answer**

**IDENTITY:** DB autoincrement. Hibernate must INSERT to get the id. That **disables JDBC batching**.

**SEQUENCE** (Oracle, Postgres): Hibernate can pre-allocate ids (`allocationSize`) and **batch** inserts. Preferred.

**TABLE:** extra table as sequence — extra locks, avoid.

**UUID:** no DB round trip, larger indexes.

ESM/Oracle: sequences are the natural story. `allocationSize` and the DB sequence increment must be configured consistently with Hibernate’s sequence optimizer. Gaps are normal with pooled allocation and rollback; the real concern is avoiding duplicate allocation or wasted/incorrect ranges after a misconfiguration.

---

### Q. Optimistic vs pessimistic locking? Why `@Version`?

**Answer**

**Optimistic:** `@Version` column (number or timestamp). Update includes `WHERE version=?`. If another TX committed, count=0 → `OptimisticLockException`. Good for low conflict (edit a service).

**Pessimistic:** `SELECT ... FOR UPDATE` (`LockModeType.PESSIMISTIC_WRITE`). Blocks others. Good for short “allocate this port now”. Long pessimistic locks kill concurrency.

ESM entities have **no `@Version`**. Concurrent save can overwrite. I can say that as a real gap.

**Interview line.** “I do not use pessimistic on a user-facing form that stays open for minutes.”

---

### Q. `equals`/`hashCode` on JPA entities?

**Answer**

Do **not** use the generated database id if it is null before persist (Set/HashMap break when id gets assigned). Prefer a **business key** (natural unique field) or a UUID assigned in the constructor. Include only stable fields. Be careful with lazy associations inside `equals` (can trigger loads / LIE).

---

### Q. Cartesian product with two `join fetch`? How do you paginate?

**Answer**

`join fetch` two collections in one query (`order.items` and `order.payments`) duplicates rows. Hibernate may return duplicate parents or a huge result. Fix: two queries, or `@BatchSize`, or entity graph for one collection only.

**Pagination + join fetch collection:** `LIMIT` applies to SQL rows, not unique parents — page size is wrong. Pattern: query **ids** with pagination, then `WHERE id IN (...)` with fetch.

Spring Data `Pageable` on a join-fetch query is a common bug. I mention it.

---

### Q. JPQL vs Criteria vs native vs Spring Data derived query?

**Answer**

- **Derived:** `findByStatusAndName` — fast to write, bad for dynamic filters.
- **`@Query` JPQL:** portable, named params, DTO constructor expressions.
- **Criteria / Specification:** dynamic filters (operator search screens).
- **Native SQL:** reports, hints, Oracle-specific. Tie to DB. Map with `@SqlResultSetMapping` or interface projections.

`@Modifying` on update/delete JPQL: set `clearAutomatically = true` or the persistence context is stale.

---

### Q. Hibernate vs JDBC — which is faster?

**Answer**

JDBC is fewer layers for bulk jobs. Hibernate is faster to write for entity graphs and dirty checking. For 100k inserts I use JDBC batch or `jdbctemplate.batchUpdate`, or `StatelessSession`, not a giant persistence context. “Hibernate is always slower” is too blunt — N+1 is the usual reason it looks slow.

---

## 11. Spring Security

Theory is **open**. Round 1 wants: filter chain, Authentication vs Authorization, 401 vs 403, CSRF, password encoding, method security. JWT/OAuth details also appear here; Round 2 goes deeper on OAuth2 resource server.

---

### Q. What is Spring Security? How does a request get authenticated?

**Answer**

Spring Security is a **filter chain** in front of the servlet. A `DelegatingFilterProxy` named `springSecurityFilterChain` sits in the servlet container and delegates to Spring beans.

Typical flow:

1. Filters run (`SecurityContextPersistence` / `SecurityContextHolderFilter` → authentication filters → `ExceptionTranslationFilter` → `AuthorizationFilter` / `FilterSecurityInterceptor`).
2. An authentication filter extracts credentials (form, Basic, Bearer JWT).
3. **`AuthenticationManager`** (`ProviderManager`) asks **`AuthenticationProvider`s**.
4. A provider uses **`UserDetailsService`** (and **`PasswordEncoder`**) or a JWT decoder.
5. Success: `SecurityContextHolder` stores an **`Authentication`** (ThreadLocal by default).
6. Authorization checks URL matchers and/or method annotations.

Boot 3 / Security 6: you declare a **`SecurityFilterChain`** `@Bean`. `WebSecurityConfigurerAdapter` is **gone**. I do not write Adapter code in a new interview answer.

---

### Q. Authentication vs authorization? 401 vs 403?

**Answer**

**Authentication:** who are you? Failed/missing login → **401 Unauthorized**.

**Authorization:** are you allowed to do this? Authenticated but not allowed → **403 Forbidden**.

`ExceptionTranslationFilter` maps `AuthenticationException` → 401 (and may redirect to login) and `AccessDeniedException` → 403.

---

### Q. `SecurityContextHolder`? Is it thread-safe across `@Async`?

**Answer**

Default strategy is **`MODE_THREADLOCAL`**. The next request on the same Tomcat thread must clear it (the framework does).

`@Async` / new thread: the child does **not** see the Authentication unless you use `MODE_INHERITABLETHREADLOCAL` or pass the token yourself. I do not enable inheritable globally without thinking (thread pools leak the previous user — same bug as ThreadLocal).

---

### Q. `UserDetailsService`, `UserDetails`, `PasswordEncoder`?

**Answer**

`UserDetailsService.loadUserByUsername` loads username, **encoded** password, and authorities.

`UserDetails` is the adapter (account expired/locked flags).

**PasswordEncoder:** I use **BCrypt** (`BCryptPasswordEncoder`). Never store plain text. Never use MD5/SHA1 for passwords. `{noop}` is only for local demos.

`DelegatingPasswordEncoder` can read `{bcrypt}...` prefixes so you can migrate algorithms.

---

### Q. CSRF? When do we disable it?

**Answer**

CSRF: a browser on another site submits a form to your site using the user’s cookie. Spring Security enables CSRF by default for **cookie session** apps. The page includes a CSRF token; state-changing POST must send it.

**Stateless JWT APIs** (Authorization header, no cookie session): there is no cookie to abuse that way. We typically **`csrf.disable()`** for those APIs. If we use cookies for JWT, CSRF is back — then we need tokens or SameSite.

I do not disable CSRF on a server-rendered form app just because “it is an API”.

---

### Q. Session vs Basic vs form vs JWT in Spring Security?

**Answer**

- **httpBasic:** every request sends `Authorization: Basic`. Simple, no XSS-stored token; passwords go every time (use TLS). Rare for browsers.
- **formLogin:** session cookie. CSRF on. Easy logout (invalidate session).
- **JWT resource server:** `oauth2ResourceServer(jwt -> ...)` in Security 6. Stateless. Scale horizontally without session store. Revoke is hard (short TTL + denylist).

`SessionCreationPolicy.STATELESS` for JWT APIs so Spring does not create an HTTP session.

---

### Q. Method security: `@PreAuthorize`, `@Secured`, `hasRole` vs `hasAuthority`?

**Answer**

URL security is coarse. Method security is “this service method only for `SERVICE_WRITE`”.

Enable: `@EnableMethodSecurity` (old: `@EnableGlobalMethodSecurity(prePostEnabled = true)`).

```java
@PreAuthorize("hasAuthority('ESM_SERVICE_WRITE')")
public void deploy(String id) { ... }
```

**`hasRole("ADMIN")`** looks for authority `ROLE_ADMIN` (prefix). **`hasAuthority("ADMIN")`** looks for exactly `ADMIN`. Mixing these is a common bug.

SpEL: `hasPermission(#id, 'DEPLOY')` with a custom `PermissionEvaluator` for object-level (IDOR) checks.

Same **proxy** limit as `@Transactional`: self-invocation skips the check.

---

### Q. Filter order? Custom filter? `OncePerRequestFilter`?

**Answer**

Order matters: CORS filter early, then authentication, then authorization. I add a custom JWT filter with `http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)` in older setups. In Security 6 resource server, I prefer the built-in JWT filter.

`OncePerRequestFilter` avoids running twice on forwards. I do not read the body twice without a wrapper.

---

### Q. `permitAll` vs `authenticated` vs `denyAll`? CORS with Security?

**Answer**

`/actuator/health` often `permitAll`. APIs `authenticated`. Admin matchers `hasRole`. Default in new Security: **any unmatched request is denied** if we use `anyRequest().authenticated()` — I always set a catch-all explicitly.

**CORS:** browser preflight is OPTIONS. Spring Security must **not** reject OPTIONS before the CORS filter. `http.cors(Customizer.withDefaults())` + a `CorsConfigurationSource` bean. CORS is not authz for Feign calls (Round 2 also says this).

---

### Q. How would you secure a Spring Boot REST API with JWT? (speak 2 minutes)

**Answer**

1. Client gets a token from an auth server (or login endpoint that signs JWT).
2. `SecurityFilterChain`: CSRF off, session stateless, `oauth2ResourceServer().jwt()`.
3. Set issuer-uri so Boot downloads JWKS and validates signature, `exp`, `iss`.
4. Map JWT claims to `GrantedAuthority`.
5. `@PreAuthorize` on write APIs.
6. HTTPS only. Short `exp`. Refresh token in httpOnly cookie or auth server.

**ESM honesty:** we do **not** use this Boot JWT tutorial. Security Java config is commented out; XML + `@Authorizer` + NAD/VPD. I explain **both**: industry default vs what my project actually does.

---

### Q. Password hashing? Timing attacks? Security headers?

**Answer**

BCrypt is slow by design (work factor). `PasswordEncoder.matches` is constant-time enough — do not write your own `==` on hashes.

Headers (Spring Security default): X-Content-Type-Options, X-Frame-Options, cache control. Add HSTS at the load balancer. CSP if we serve HTML.

---

## 12. SQL

---

### Q. What is an index? When does it not help?

**Answer**

An index is a B-tree (usually) that stores column values in sorted order plus a pointer to the row. The database can find a row without reading the whole table.

**Helps:** `WHERE id = 10`, `WHERE status = 'OPEN' AND created_at > ...` (if the index matches), joins on FK columns, unique constraints.

**Does not help:**

- `WHERE UPPER(name) = 'RAM'` — function on the column (unless a function-based index)
- Column with only 2 values (gender) — scanning can be cheaper
- Query that returns most of the table
- Too many indexes — every INSERT/UPDATE must maintain them

**Composite index** `(status, created_at)` helps `WHERE status = ? ORDER BY created_at`. It may **not** help `WHERE created_at = ?` alone. The leftmost column should be in the filter. That is the **leftmost prefix** rule.

**Covering index:** the index itself has all columns the query needs, so the DB does not touch the table.

---

### Q. INNER JOIN vs LEFT JOIN?

**Answer**

**INNER JOIN** — only rows that match on both sides.

**LEFT JOIN** — all rows from the left table. If there is no match on the right, right columns are NULL.

If I need employees who have no manager, I **must** use LEFT JOIN. INNER JOIN drops them.

---

### Q. Write: EmpName and ManagerName from one Employee table

Table:

| EmpID | EmpName | DeptName | ManagerID |
|---|---|---|---|
| 1 | Hitesh | AV | 3 |
| 2 | Ramesh | DV | 1 |
| 3 | Jitesh | NV | NULL |

Expected:

| EmpName | ManagerName |
|---|---|
| Hitesh | Jitesh |
| Ramesh | Hitesh |
| Jitesh | NULL |

**Answer (this is a self-join)**

```sql
SELECT e.EmpName AS EmpName,
       m.EmpName AS ManagerName
FROM   Employee e
LEFT JOIN Employee m ON e.ManagerID = m.EmpID;
```

We join the table to **itself**. `e` is the employee. `m` is the manager row. LEFT JOIN keeps Jitesh, whose `ManagerID` is NULL.

If they say INNER JOIN, Jitesh disappears. Mention that.

---

### Q. Second highest salary? Nth highest?

**Answer**

```sql
-- simple second highest
SELECT MAX(salary) AS second_highest
FROM   employee
WHERE  salary < (SELECT MAX(salary) FROM employee);

-- nth with window function (preferred)
SELECT salary
FROM (
    SELECT salary,
           DENSE_RANK() OVER (ORDER BY salary DESC) AS rnk
    FROM   employee
) t
WHERE rnk = 2;
```

If two people have 20, and the next is 10:

- `RANK` gives 1, 1, 3 — “2nd” might return empty
- `DENSE_RANK` gives 1, 1, 2 — 2nd is 10

For “second highest salary” I use **DENSE_RANK** unless they want unique ranks.

---

### Q. GROUP BY vs window functions? Aggregate vs composite?

**Answer**

`GROUP BY` **collapses** rows. `SELECT dept, COUNT(*) FROM emp GROUP BY dept` — one row per dept.

A **window function** keeps every row and adds extra columns:

```sql
SELECT name,
       dept,
       salary,
       AVG(salary) OVER (PARTITION BY dept) AS dept_avg
FROM   employee;
```

**Aggregate functions:** `COUNT`, `SUM`, `AVG`, `MIN`, `MAX` — work on a set.

People say “composite function” meaning functions used together, or composite keys/indexes. In SQL interviews I clarify: “If you mean aggregate functions, they are COUNT/SUM/… If you mean composite index, that is an index on more than one column.”

---

### Q. EXISTS vs IN?

**Answer**

`EXISTS` stops at the **first** matching row. Good for “does this employee have at least one order?”

```sql
SELECT e.name
FROM   employee e
WHERE  EXISTS (SELECT 1 FROM orders o WHERE o.emp_id = e.id);
```

`IN` compares to a list. Fine for small lists. `NOT IN` is dangerous if the subquery can return **NULL** — the whole condition becomes unknown and you get no rows. I prefer `NOT EXISTS` for anti-joins.

---

### Q. DELETE vs TRUNCATE vs DROP? UNION vs UNION ALL?

**Answer**

- **DELETE** — removes rows. Can have WHERE. Logged. Can rollback. Triggers fire.
- **TRUNCATE** — empties the table fast. Usually cannot WHERE. Resets identity on some databases.
- **DROP** — removes the table itself.

**UNION** concatenates result sets and **removes duplicates** (extra sort/hash).

**UNION ALL** keeps duplicates and is cheaper. I use UNION ALL unless I truly need distinct.

---

### Q. How do you debug a slow query? Pagination?

**Answer**

I run **EXPLAIN** (Oracle: `EXPLAIN PLAN`, Postgres: `EXPLAIN ANALYZE`). I look for a full table scan on a large table, a bad join order, or a function on an indexed column.

Then: add the right index, rewrite the query, update statistics, or stop selecting columns I do not need.

**OFFSET 100000 LIMIT 20** is slow — the DB still walks 100000 rows. Better: keyset pagination:

```sql
SELECT *
FROM   orders
WHERE  (created_at, id) < (:last_ts, :last_id)
ORDER BY created_at DESC, id DESC
FETCH FIRST 20 ROWS ONLY;
```

---

### Q. ACID properties?

**Answer**

- **Atomicity:** all statements commit or none.
- **Consistency:** constraints/invariants remain valid.
- **Isolation:** concurrent transactions behave according to the isolation level.
- **Durability:** committed data survives a crash.

ACID is a database guarantee plus correct application transaction boundaries; it does not make a multi-service workflow atomic.

---

### Q. Isolation levels, anomalies, MVCC, deadlocks?

**Answer**

- **READ UNCOMMITTED:** dirty reads possible.
- **READ COMMITTED:** no dirty reads; the same row may change between two reads.
- **REPEATABLE READ:** stable rows in the transaction; phantom behavior is database-specific/MVCC-dependent.
- **SERIALIZABLE:** strongest; behaves like transactions ran one by one, with lower concurrency/retry risk.

Anomalies: dirty read, non-repeatable read, phantom row, and **lost update**. MVCC lets readers see snapshots instead of blocking writers, but it does not automatically prevent every lost update—use `@Version`, locking, or an atomic update.

Deadlock: transactions lock resources in opposite order. Keep transactions short, lock in a consistent order, and retry the whole idempotent transaction on the database’s deadlock/serialization exception with a small bounded backoff.

---

### Q. Database normalization: 1NF, 2NF, 3NF? When denormalize?

**Answer**

- **1NF:** atomic values; no repeating column groups.
- **2NF:** 1NF + non-key columns depend on the whole composite key.
- **3NF:** 2NF + no transitive dependency between non-key columns.

Normalize the write/source-of-truth model to avoid anomalies. Denormalize a measured read path (report/list/search) when joins are the bottleneck, with an ownership and update strategy.

---

### Q. Primary key, unique key, foreign key, candidate/composite key?

**Answer**

Primary key uniquely identifies a row and is non-null. A table can have several unique/candidate keys but one chosen primary key. Foreign key enforces referential integrity. Composite key contains multiple columns. Index foreign keys used in joins/deletes, depending on the DB and workload.

---

### Q. CTE and recursive CTE?

**Answer**

A CTE (`WITH`) names a subquery for readability and reuse in one statement. A **recursive CTE** handles hierarchy such as employee → manager or category trees, with an anchor query and recursive query. It is not automatically faster; inspect the plan.

---

### Q. View vs materialized view vs stored procedure?

**Answer**

A **view** stores a query, not data. A **materialized view** stores results and needs refresh—fast reads, possible staleness. A **stored procedure** runs server-side logic and can reduce round trips, but couples business logic to the DB and is harder to version/test across databases.

Use each deliberately; do not move all service logic into procedures for “performance”.

---

### Q. How do SQL NULL and three-valued logic work?

**Answer**

`NULL` means unknown/missing. `col = NULL` is never true; use `IS NULL`. Comparisons can be TRUE, FALSE, or UNKNOWN. This is why `NOT IN (subquery)` can return no rows when the subquery contains NULL; prefer `NOT EXISTS`.

---

## 13. Trick / output questions

---

### Q. What does this print?

```java
Map<String, Integer> aMap = new HashMap<>();
String a = "blumeglobal";
String b = new String("blumeglobal");
aMap.put(a, 14);
aMap.put(b, 12);

System.out.println(aMap.get("blumeglobal"));
System.out.println(aMap.get(new String("blumeglobal")));
System.out.println(aMap.get(b));
System.out.println(aMap.get(a));
```

**Answer**

All four print **12**.

`a` and `b` have different references (`==` is false) but the **same characters**, so `equals` is true and `hashCode` is the same. HashMap treats them as **one key**. The second `put` **replaces** 14 with 12. After that, any String with value `"blumeglobal"` gets 12.

---

### Q. What does this print? `method(null)`

```java
public static void method(Object o) {
    System.out.println("Object method");
}
public static void method(String s) {
    System.out.println("String method");
}
public static void main(String[] args) {
    method(null);
}
```

**Answer**

It prints **String method**.

Overload resolution picks the **most specific** type. `null` fits both `String` and `Object`. `String` is more specific than `Object`, so `method(String)` is chosen.

If you add `method(Integer i)`, it **does not compile** — `null` is ambiguous between String and Integer.

---

### Q. HashMap put the same key twice?

**Answer**

The size stays the same. The value is replaced. `put` returns the **previous** value (or null if there was none).

---

### Q. What does this print? Integer cache

```java
Integer a = 127;
Integer b = 127;
Integer c = 128;
Integer d = 128;
System.out.println(a == b);
System.out.println(c == d);
System.out.println(c.equals(d));
```

**Answer**

`true`, `false`, `true`.

`Integer.valueOf` (used by autoboxing) caches **-128 to 127**. `127` is the same object. `128` is two objects. Always compare wrappers with `equals`.

---

### Q. What does this print? String concat vs intern

```java
String s1 = "ja" + "va";
String s2 = "java";
String s3 = new String("java");
System.out.println(s1 == s2);
System.out.println(s2 == s3);
System.out.println(s2 == s3.intern());
```

**Answer**

`true`, `false`, `true`.

Compile-time constant `"ja" + "va"` is interned as `"java"`. `new String` is a heap copy. `intern()` returns the pool object.

---

## 14. Coding problems (try first)

Write on paper or in an empty editor. Then open the solution. Say time and space complexity out loud.

---

### C1. First non-repeated character in a String

Example: `"swiss"` → `'w'`

**Hint.** Count frequency, then scan again from the left for count == 1.

<details>
<summary>Click to see solution</summary>

```java
public static Character firstNonRepeated(String s) {
    if (s == null || s.isEmpty()) {
        return null;
    }
    int[] freq = new int[256];
    for (int i = 0; i < s.length(); i++) {
        freq[s.charAt(i)]++;
    }
    for (int i = 0; i < s.length(); i++) {
        if (freq[s.charAt(i)] == 1) {
            return s.charAt(i);
        }
    }
    return null;
}
```

Time O(n), space O(1) for ASCII. For full Unicode, iterate `s.codePoints()` and count `Integer` code points; `Character` only represents one UTF-16 code unit and can split supplementary characters.

</details>

---

### C2. Frequency of numbers

Input: `2,3,5,6,2,3,2` → `{2=3, 3=2, 5=1, 6=1}`

**Hint.** `HashMap` and `merge`, or `groupingBy`.

<details>
<summary>Click to see solution</summary>

```java
public static Map<Integer, Integer> freq(int[] a) {
    Map<Integer, Integer> map = new HashMap<>();
    for (int x : a) {
        map.merge(x, 1, Integer::sum);
    }
    return map;
}
```

Stream version:

```java
Map<Integer, Long> freq = list.stream()
    .collect(Collectors.groupingBy(i -> i, Collectors.counting()));
```

Time O(n), space O(k) unique numbers.

</details>

---

### C3. Second largest in an array (duplicates allowed)

Input: `8, 5, 10, 20, 7, 20, 9` → `10` (not 20)

**Hint.** One loop. Track `max` and `second`. Skip values equal to max.

<details>
<summary>Click to see solution</summary>

```java
public static Integer secondLargest(int[] a) {
    Integer max = null;
    Integer second = null;
    for (int x : a) {
        if (max == null || x > max) {
            second = max;
            max = x;
        } else if (x < max && (second == null || x > second)) {
            second = x;
        }
    }
    return second; // null if all values are equal
}
```

Time O(n), space O(1). Do not sort unless they allow O(n log n).

</details>

---

### C4. Reverse a linked list

**Hint.** Three pointers: prev, curr, next. Draw boxes.

<details>
<summary>Click to see solution</summary>

```java
class ListNode {
    int val;
    ListNode next;
    ListNode(int val) { this.val = val; }
}

public static ListNode reverse(ListNode head) {
    ListNode prev = null;
    ListNode curr = head;
    while (curr != null) {
        ListNode next = curr.next;
        curr.next = prev;
        prev = curr;
        curr = next;
    }
    return prev;
}
```

Time O(n), space O(1).

</details>

---

### C5. Merge two sorted linked lists

`1→4→6→9` and `2→3→5→7→8→11`

**Hint.** Dummy node. Do not copy into ArrayList and sort — they want O(n+m) merge.

<details>
<summary>Click to see solution</summary>

```java
public static ListNode merge(ListNode a, ListNode b) {
    ListNode dummy = new ListNode(0);
    ListNode tail = dummy;
    while (a != null && b != null) {
        if (a.val <= b.val) {
            tail.next = a;
            a = a.next;
        } else {
            tail.next = b;
            b = b.next;
        }
        tail = tail.next;
    }
    tail.next = (a != null) ? a : b;
    return dummy.next;
}
```

Time O(n+m), space O(1).

</details>

---

### C6. Two threads: odd and even 1 to 10, alternating

**Hint.** Shared lock, `oddTurn` flag, `wait`/`notifyAll`, `while` not `if`.

<details>
<summary>Click to see solution</summary>

```java
public class EvenOdd {
    private final Object lock = new Object();
    private boolean oddTurn = true;

    public void printOdd() {
        for (int i = 1; i <= 9; i += 2) {
            print(i, true);
        }
    }

    public void printEven() {
        for (int i = 2; i <= 10; i += 2) {
            print(i, false);
        }
    }

    private void print(int number, boolean odd) {
        synchronized (lock) {
            while (oddTurn != odd) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            System.out.println(Thread.currentThread().getName() + " " + number);
            oddTurn = !oddTurn;
            lock.notifyAll();
        }
    }

    public static void main(String[] args) {
        EvenOdd eo = new EvenOdd();
        Thread t1 = new Thread(eo::printOdd, "ODD");
        Thread t2 = new Thread(eo::printEven, "EVEN");
        t1.start();
        t2.start();
    }
}
```

If they say “odd thread fully, then even thread”: `t1.start(); t1.join(); t2.start();`

</details>

---

### C7. Set matrix zeroes

If any cell is 0, set that whole row and column to 0. Do it in place.

**Hint.** Use first row and first column as markers. Extra booleans for row 0 and column 0.

<details>
<summary>Click to see solution</summary>

```java
public static void setZeroes(int[][] m) {
    int rows = m.length;
    int cols = m[0].length;
    boolean firstRowZero = false;
    boolean firstColZero = false;

    for (int j = 0; j < cols; j++) {
        if (m[0][j] == 0) firstRowZero = true;
    }
    for (int i = 0; i < rows; i++) {
        if (m[i][0] == 0) firstColZero = true;
    }
    for (int i = 1; i < rows; i++) {
        for (int j = 1; j < cols; j++) {
            if (m[i][j] == 0) {
                m[i][0] = 0;
                m[0][j] = 0;
            }
        }
    }
    for (int i = 1; i < rows; i++) {
        for (int j = 1; j < cols; j++) {
            if (m[i][0] == 0 || m[0][j] == 0) {
                m[i][j] = 0;
            }
        }
    }
    if (firstRowZero) {
        for (int j = 0; j < cols; j++) m[0][j] = 0;
    }
    if (firstColZero) {
        for (int i = 0; i < rows; i++) m[i][0] = 0;
    }
}
```

Time O(rows * cols), space O(1).

</details>

---

### C8. Remove duplicate employees and group by department (Stream)

**Hint.** Distinct by `id` using `toMap`, then `groupingBy` dept.

<details>
<summary>Click to see solution</summary>

```java
public final class Employee {
    private final long id;
    private final String name;
    private final String dept;

    public Employee(long id, String name, String dept) {
        this.id = id;
        this.name = name;
        this.dept = dept;
    }
    public long getId() { return id; }
    public String getName() { return name; }
    public String getDept() { return dept; }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Employee e) && e.id == id;
    }
    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}

Map<String, List<Employee>> byDept = employees.stream()
        .collect(Collectors.toMap(Employee::getId, e -> e, (first, dup) -> first))
        .values()
        .stream()
        .collect(Collectors.groupingBy(Employee::getDept));
```

</details>

---

### C9. Two sum — return indices

**Hint.** One pass HashMap: value → index. Look for `target - nums[i]`.

<details>
<summary>Click to see solution</summary>

```java
public static int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> seen = new HashMap<>();
    for (int i = 0; i < nums.length; i++) {
        Integer j = seen.get(target - nums[i]);
        if (j != null) {
            return new int[] { j, i };
        }
        seen.put(nums[i], i);
    }
    throw new IllegalArgumentException("No pair");
}
```

Time O(n), space O(n).

</details>

---

### C10. Detect cycle in a linked list

**Hint.** Slow pointer +1, fast pointer +2. If they meet, there is a cycle.

<details>
<summary>Click to see solution</summary>

```java
public static boolean hasCycle(ListNode head) {
    ListNode slow = head;
    ListNode fast = head;
    while (fast != null && fast.next != null) {
        slow = slow.next;
        fast = fast.next.next;
        if (slow == fast) {
            return true;
        }
    }
    return false;
}
```

Time O(n), space O(1). Extra: after they meet, put one pointer at head; move both one step; they meet at the cycle start.

</details>

---

### C11. LRU Cache

**Hint.** `LinkedHashMap` with access-order true, override `removeEldestEntry`.

<details>
<summary>Click to see solution</summary>

```java
public class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;

    public LRUCache(int capacity) {
        super(capacity, 0.75f, true); // true = access order
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}
```

Get and put are O(1) average. If they want you to implement from scratch: HashMap + doubly linked list. Head = most recently used, tail = least recently used. On get, move node to head. On put when full, remove tail.

</details>

---

### C12. Custom equals/hashCode for a map key

**Hint.** Same fields in both. Immutable. `Objects.hash`.

<details>
<summary>Click to see solution</summary>

```java
public final class Pair {
    private final String left;
    private final String right;

    public Pair(String left, String right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair p)) return false;
        return Objects.equals(left, p.left)
            && Objects.equals(right, p.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }
}
```

Do not add setters if this is a HashMap key.

</details>

---

### C13. Thread-safe lazy Singleton (holder class)

**Hint.** Nested static class. JVM initializes it on first access. Mention `volatile` DCL as the alternative.

<details>
<summary>Click to see solution</summary>

```java
public final class HolderSingleton {
    private HolderSingleton() {}

    private static class Holder {
        private static final HolderSingleton INSTANCE = new HolderSingleton();
    }

    public static HolderSingleton getInstance() {
        return Holder.INSTANCE;
    }
}
```

Enum version (serialization-safe):

```java
public enum EnumSingleton {
    INSTANCE;
    public void work() { }
}
```

Double-checked locking must use `private static volatile HolderSingleton instance`.

</details>

---

### C14. Producer–consumer with BlockingQueue

**Hint.** Bounded `ArrayBlockingQueue`. `put` / `take`. Poison pill to stop.

<details>
<summary>Click to see solution</summary>

```java
BlockingQueue<Integer> q = new ArrayBlockingQueue<>(10);

Runnable producer = () -> {
    try {
        for (int i = 0; i < 50; i++) {
            q.put(i);
        }
        q.put(-1); // poison
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
};

Runnable consumer = () -> {
    try {
        while (true) {
            int v = q.take();
            if (v == -1) {
                break;
            }
            // process v
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
};
```

This is in-process backpressure. Across services I use Kafka.

</details>

---

### C15. Implement `BlockingQueue.put` wait/notify (they sometimes ask “without BlockingQueue”)

**Hint.** Array + `synchronized` + `wait` while full / empty. Always wait in a `while`.

<details>
<summary>Click to see solution</summary>

```java
public class TinyBlockingQueue<T> {
    private final Object[] a;
    private int head, tail, size;
    public TinyBlockingQueue(int cap) { a = new Object[cap]; }

    public synchronized void put(T x) throws InterruptedException {
        while (size == a.length) wait();
        a[tail] = x;
        tail = (tail + 1) % a.length;
        size++;
        notifyAll();
    }

    @SuppressWarnings("unchecked")
    public synchronized T take() throws InterruptedException {
        while (size == 0) wait();
        T x = (T) a[head];
        a[head] = null;
        head = (head + 1) % a.length;
        size--;
        notifyAll();
        return x;
    }
}
```

Time of put/take O(1) aside from waiting.

</details>

---

## 15. Unit testing and integration testing

This was the largest missing area. For 5–8 years, expect “How did you test it?” after almost every project answer.

---

### Q. Unit test vs integration test vs component test vs end-to-end?

**Answer**

- **Unit:** one class, dependencies mocked, milliseconds. JUnit 5 + Mockito.
- **Integration:** real collaboration with infrastructure, such as repository + real PostgreSQL/Oracle-compatible container.
- **Component:** start the Spring service and test its API while external services are stubbed.
- **End-to-end:** real deployed services, Kafka, DB, and device/simulator. Slowest and most fragile.

I use a test pyramid: many unit tests, fewer integration tests, and a small set of critical E2E tests. A mocked repository cannot prove that my JPQL works.

---

### Q. Important JUnit 5 annotations and assertions?

**Answer**

`@Test`, `@BeforeEach`, `@AfterEach`, `@BeforeAll`, `@ParameterizedTest`, `@ValueSource`, `@CsvSource`, `@MethodSource`, `@Nested`, and `@DisplayName`.

Use `assertEquals`, `assertThrows`, `assertAll`, and `assertTimeout`. I use parameterized tests for boundary cases instead of copying five nearly identical tests.

---

### Q. Mockito: mock vs spy vs `@InjectMocks`? `when` vs `doReturn`?

**Answer**

- **Mock:** fake object; methods return defaults until stubbed.
- **Spy:** wraps a real object; real methods run unless stubbed. Use rarely.
- `@InjectMocks`: creates the class under test and injects mocks.
- `when(mock.call()).thenReturn(x)` for mocks.
- `doReturn(x).when(spy).call()` for a spy, so the real method is not executed during stubbing.

I verify important interactions (`verify(kafka).send(...)`), not every getter. Over-verifying implementation details makes refactoring painful. Use `ArgumentCaptor` when the important assertion is the event/request object sent to a dependency, not merely that the method was called.

---

### Q. `@Mock` vs `@MockBean` / `@MockitoBean`?

**Answer**

`@Mock` is pure Mockito; no Spring context. Fast.

`@MockBean` replaces a bean in the Spring test context (deprecated in newer Spring Boot in favor of Spring Framework’s `@MockitoBean`). Use it only when I need Spring wiring. Starting Spring just to test one service class is wasteful.

---

### Q. `@SpringBootTest`, `@WebMvcTest`, `@DataJpaTest`?

**Answer**

- `@SpringBootTest`: whole application context. Use for a small number of integration tests.
- `@WebMvcTest(Controller.class)`: MVC slice, controller, validation, JSON, `MockMvc`; mock the service.
- `@DataJpaTest`: repository/JPA slice; usually rolls back. Use a real DB container when SQL dialect matters.

`MockMvc` tests status, headers, JSON, validation, and exception mapping without opening a real network port.

---

### Q. Testcontainers, WireMock, Embedded Kafka?

**Answer**

**Testcontainers** starts a real disposable DB/Kafka in Docker. It catches dialect, migration, constraint, and serialization issues that H2 misses.

**WireMock** stubs HTTP dependencies and lets me verify timeouts, 500s, and request bodies.

**Embedded Kafka** is useful, but a Kafka Testcontainer is closer to production. For ESM, unit tests mock the adapter proxy; a bench/simulator test proves the real southbound path.

---

### Q. How do you test `@Transactional`, retries, async code, and time?

**Answer**

- Transaction: integration test the repository/constraint; unit tests cannot prove rollback.
- Retry: stub failure twice then success; verify exactly three attempts and no retry for non-retryable errors.
- Async: avoid `Thread.sleep`. Await a condition with Awaitility or join a returned future with a timeout.
- Time: inject `Clock`, not `LocalDateTime.now()` everywhere.

Flaky tests usually come from real time, random ports/data, shared state, or test-order dependency.

---

### Q. Code coverage: what percentage is good?

**Answer**

Coverage is a signal, not proof. I prefer a gate on **new code** (often 70–80%, team dependent) and mutation/failure-path thinking. A test that executes a line but asserts nothing is not useful. I exclude generated DTOs/config where appropriate and never inflate a report.

---

### Q. What is the testing strategy for a Kafka consumer?

**Answer**

1. Unit-test handler with a normal event object.
2. Test idempotency: same event twice produces one business effect.
3. Test retryable vs non-retryable failures and DLT publishing.
4. Integration-test serialization, topic, and offset behavior with Kafka/Testcontainers.
5. Verify DB commit and offset ordering; do not acknowledge before the business transaction succeeds.

---

## 16. Design patterns commonly asked

Do not only define them. Say where the pattern appears in Java/Spring or your project.

---

### Q. Which design patterns have you used?

**Answer**

> In Spring I use Proxy (`@Transactional`, security), Factory (bean creation and adapter proxy factory), Template Method (common workflow with overridable steps), Strategy (select an implementation behind an interface), Observer (application events), Builder (complex immutable request), and Adapter (wrap a vendor/client API). ESM specifically uses **Command + CommandManager + Task Chain + Kafka Adapter Proxy**; I do not rename its task chain as Strategy.

---

### Q. Factory vs Abstract Factory vs Builder?

**Answer**

- **Factory Method:** choose/create one product (`PaymentProcessorFactory.create(type)`).
- **Abstract Factory:** create a related family of objects (AWS client family vs Azure family).
- **Builder:** construct one complex object step by step, especially immutable objects with optional fields.

Builder solves construction readability; Factory hides which concrete class is created.

---

### Q. Strategy vs Template Method?

**Answer**

**Strategy** uses composition: inject/swap an algorithm at runtime (`PricingStrategy`).  
**Template Method** uses inheritance: base method fixes the workflow and subclasses override selected steps.

I prefer Strategy when behavior changes independently. Template Method is useful for a stable skeleton.

---

### Q. Adapter vs Facade vs Decorator vs Proxy?

**Answer**

- **Adapter:** convert one interface into another (`VendorClient` → our `InventoryPort`).
- **Facade:** simple entry point over a complex subsystem.
- **Decorator:** add behavior while keeping the same interface (metrics, compression).
- **Proxy:** control access to the real object (transactions, lazy JPA proxy, remote proxy).

Decorator and Proxy look similar structurally; their intent differs.

---

### Q. Observer and Command?

**Answer**

**Observer:** publisher notifies subscribers. Spring `ApplicationEvent`, but use carefully—hidden synchronous side effects can make transactions confusing.

**Command:** represent an action as an object so it can be queued, logged, retried, or composed. ESM uses Command/CommandManager and task chains.

---

### Q. Anti-patterns: God class, service locator, anemic model?

**Answer**

God class owns unrelated responsibilities and changes for every feature. Service Locator hides dependencies; constructor injection is clearer. Anemic domain models are data-only entities with all behavior in services—sometimes acceptable in CRUD, but workflow rules should not become a 3000-line service.

---

## 17. Spring ecosystem, REST, build, and logging

---

### Q. Bean Validation: `@Valid` vs `@Validated`? Custom validator?

**Answer**

`@Valid` validates nested request objects. `@Validated` enables Spring method validation and validation groups.

Use `@NotNull`, `@NotBlank`, `@Size`, `@Pattern`, `@Positive`. For a cross-field rule (end date after start date), create a class-level constraint + `ConstraintValidator`.

Validation errors should return stable 400 responses through `@ControllerAdvice`. Never rely only on UI validation.

---

### Q. Jackson questions: unknown fields, nulls, dates, recursion?

**Answer**

Use DTOs, not JPA entities, at the API boundary. Configure unknown-field behavior deliberately. `@JsonInclude(NON_NULL)` can reduce output; `@JsonProperty` maps names; `@JsonIgnore` hides fields (not a security boundary by itself). Use ISO-8601 `java.time`.

Bidirectional JPA relations can recurse during JSON serialization; another reason to map to DTOs. Custom serializer/deserializer only when the wire format truly differs.

---

### Q. How do you design REST errors and pagination?

**Answer**

Error body: stable `code`, readable `message`, `traceId`, optional field violations. Correct status: 400 validation, 401 unauthenticated, 403 forbidden, 404 absent, 409 conflict/idempotency/optimistic lock, 429 rate limit, 500 unexpected, 503 unavailable.

Pagination uses `page/size/sort` for normal admin APIs; keyset/cursor pagination for deep/high-volume pages. Put limits on `size`.

---

### Q. `@RestController` vs `@Controller`; `@PathVariable` vs `@RequestParam` vs `@RequestBody`?

**Answer**

`@RestController` = `@Controller` + `@ResponseBody`; return values are serialized. `@Controller` is also used for MVC views.

Use `@PathVariable` for resource identity (`/orders/{id}`), `@RequestParam` for filtering/paging (`?status=OPEN`), and `@RequestBody` for JSON command/resource data. Validate the body with `@Valid`; do not put sensitive or large structured data in query strings.

---

### Q. Why `PreparedStatement` instead of SQL string concatenation?

**Answer**

It binds values separately from SQL syntax, preventing SQL injection and handling quoting/types correctly. It can also improve statement/plan reuse depending on the database/driver. Dynamic table/column names cannot be parameterized—allowlist them rather than accepting arbitrary input.

---

### Q. OpenAPI/Swagger? API-first vs code-first?

**Answer**

OpenAPI documents paths, schemas, errors, and examples; it can generate clients and run contract checks. API-first is useful across teams; code-first is faster for one team. The important point is that the spec and implementation stay in CI sync. Never expose Swagger UI publicly in production without controls.

---

### Q. Feign vs RestTemplate vs WebClient/RestClient?

**Answer**

Feign is declarative and integrates with Spring Cloud; easy, but remote calls can become invisible and chatty. `RestClient` is the modern blocking Spring API, `WebClient` supports non-blocking/reactive, and RestTemplate is maintenance-mode legacy.

Whichever client I choose, configure connection/read/response timeouts, pool limits, observability, and error mapping.

---

### Q. `@Cacheable`, `@CachePut`, `@CacheEvict` traps?

**Answer**

`@Cacheable` returns cached result and skips method execution. `@CachePut` always executes then updates. `@CacheEvict` removes.

Like transactions, caching is proxy-based: self-invocation is skipped. Cache keys must include tenant/permissions where relevant. Do not cache null/errors forever. Decide TTL and invalidation before enabling it.

---

### Q. `@Scheduled`, Quartz, distributed scheduling?

**Answer**

`@Scheduled` runs on **every application instance** unless coordinated. For one execution across pods, use a DB/Redis lock such as ShedLock, Kubernetes CronJob, or Quartz clustered mode. Jobs must be idempotent and record last successful run. Configure a scheduler thread pool; one long job must not block all schedules.

---

### Q. Maven lifecycle, dependency conflicts, scopes?

**Answer**

Lifecycle: `validate → compile → test → package → verify → install → deploy`. `mvn verify` runs integration checks before install/deploy.

Scopes: compile (default), test, runtime, provided. `dependencyManagement` controls versions; it does not add a dependency. Diagnose conflicts with `mvn dependency:tree`; Maven uses nearest-wins, so use BOMs and avoid random exclusions.

---

### Q. SLF4J/Logback and good production logging?

**Answer**

SLF4J is the facade; Logback/Log4j2 is the implementation. Use parameterized logging (`log.info("serviceId={}", id)`), not string concatenation. Put `traceId`, `serviceId`, tenant in MDC and clear MDC on pooled threads. Never log passwords/tokens/large payloads. Log an exception once at the boundary that handles it—not at every layer.

---

### Q. Git questions: merge vs rebase, revert vs reset?

**Answer**

Merge preserves branch history; rebase rewrites commits onto a new base. Rebase local/unshared work, not a branch teammates already use. `git revert` creates a safe inverse commit for shared history. `reset` moves local history and can discard work; do not use hard reset on shared work.

---

### Q. Spring AOP terms and JDK proxy vs CGLIB?

**Answer**

Aspect = cross-cutting concern; advice = code before/after/around; pointcut = which methods; join point = method execution in Spring AOP.

JDK dynamic proxy requires an interface. CGLIB creates a subclass and cannot advise final classes/methods. Calls must pass through the proxy; self-invocation skips `@Transactional`, `@Cacheable`, security, and custom aspects. Use AOP for transactions/metrics/audit—not hidden business workflow.

---

### Q. Circular dependency in Spring? How do you fix it?

**Answer**

Constructor cycle (`A → B → A`) fails at startup and usually exposes a design problem. Fix by moving shared behavior to a third service, publishing an event, or changing direction. `@Lazy` can break creation temporarily but hides the coupling; setter injection is not a design fix. Modern Spring Boot disallows circular references by default.

---

### Q. `@Value` vs `@ConfigurationProperties`?

**Answer**

`@Value` is fine for one property. `@ConfigurationProperties(prefix="client")` binds a typed group, supports validation, metadata, and immutable records/constructor binding. Use it for client URL/timeouts/pool config. Secrets still come from a secret store/environment, not committed YAML.

---

## 18. Additional high-frequency coding problems

These close the missing tree, binary-search, interval, and sliding-window coverage.

---

### C16. Binary search (sorted array)

<details>
<summary>Click to see solution</summary>

```java
static int binarySearch(int[] a, int target) {
    int lo = 0, hi = a.length - 1;
    while (lo <= hi) {
        int mid = lo + (hi - lo) / 2;
        if (a[mid] == target) return mid;
        if (a[mid] < target) lo = mid + 1;
        else hi = mid - 1;
    }
    return -1;
}
```

Time O(log n), space O(1). Mention overflow-safe midpoint.
</details>

---

### C17. Validate balanced brackets

<details>
<summary>Click to see solution</summary>

```java
static boolean balanced(String s) {
    Deque<Character> stack = new ArrayDeque<>();
    Map<Character, Character> closeToOpen =
            Map.of(')', '(', ']', '[', '}', '{');
    for (char ch : s.toCharArray()) {
        if (closeToOpen.containsValue(ch)) stack.push(ch);
        else if (closeToOpen.containsKey(ch)
                && (stack.isEmpty() || stack.pop() != closeToOpen.get(ch))) {
            return false;
        }
    }
    return stack.isEmpty();
}
```

Time O(n), space O(n).
</details>

---

### C18. Binary tree level-order traversal (BFS)

<details>
<summary>Click to see solution</summary>

```java
static List<List<Integer>> levelOrder(TreeNode root) {
    List<List<Integer>> result = new ArrayList<>();
    if (root == null) return result;
    Queue<TreeNode> q = new ArrayDeque<>();
    q.offer(root);
    while (!q.isEmpty()) {
        int size = q.size();
        List<Integer> level = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            TreeNode n = q.poll();
            level.add(n.val);
            if (n.left != null) q.offer(n.left);
            if (n.right != null) q.offer(n.right);
        }
        result.add(level);
    }
    return result;
}
```

Time O(n), space O(width).
</details>

---

### C19. Maximum depth of binary tree (DFS)

<details>
<summary>Click to see solution</summary>

```java
static int maxDepth(TreeNode root) {
    if (root == null) return 0;
    return 1 + Math.max(maxDepth(root.left), maxDepth(root.right));
}
```

Time O(n), recursion space O(height). For a highly skewed tree, use iterative DFS to avoid stack overflow.
</details>

---

### C20. Merge overlapping intervals

<details>
<summary>Click to see solution</summary>

```java
static int[][] mergeIntervals(int[][] intervals) {
    Arrays.sort(intervals, Comparator.comparingInt(x -> x[0]));
    List<int[]> out = new ArrayList<>();
    for (int[] current : intervals) {
        if (out.isEmpty() || out.get(out.size() - 1)[1] < current[0]) {
            out.add(current.clone());
        } else {
            int[] last = out.get(out.size() - 1);
            last[1] = Math.max(last[1], current[1]);
        }
    }
    return out.toArray(new int[0][]);
}
```

Time O(n log n), space O(n).
</details>

---

### C21. Longest substring without repeating characters

<details>
<summary>Click to see solution</summary>

```java
static int longestUnique(String s) {
    Map<Character, Integer> last = new HashMap<>();
    int left = 0, best = 0;
    for (int right = 0; right < s.length(); right++) {
        char ch = s.charAt(right);
        left = Math.max(left, last.getOrDefault(ch, -1) + 1);
        last.put(ch, right);
        best = Math.max(best, right - left + 1);
    }
    return best;
}
```

Time O(n), space O(character set).
</details>

---

### C22. Top K frequent elements

<details>
<summary>Click to see solution</summary>

```java
static List<Integer> topK(int[] nums, int k) {
    Map<Integer, Integer> freq = new HashMap<>();
    for (int n : nums) freq.merge(n, 1, Integer::sum);
    PriorityQueue<Integer> heap =
            new PriorityQueue<>(Comparator.comparingInt(freq::get));
    for (int n : freq.keySet()) {
        heap.offer(n);
        if (heap.size() > k) heap.poll();
    }
    return new ArrayList<>(heap);
}
```

Time O(n log k), space O(number of unique values).
</details>

---

### C23. Graph BFS and shortest path in an unweighted graph

<details>
<summary>Click to see solution</summary>

```java
static int shortestPath(List<List<Integer>> graph, int source, int target) {
    int[] distance = new int[graph.size()];
    Arrays.fill(distance, -1);
    Queue<Integer> queue = new ArrayDeque<>();
    queue.offer(source);
    distance[source] = 0;

    while (!queue.isEmpty()) {
        int node = queue.poll();
        if (node == target) return distance[node];
        for (int next : graph.get(node)) {
            if (distance[next] == -1) {
                distance[next] = distance[node] + 1;
                queue.offer(next);
            }
        }
    }
    return -1;
}
```

BFS gives shortest number of edges in an **unweighted** graph. Time O(V + E), space O(V). DFS is for traversal/connectivity/backtracking, not shortest unweighted path.
</details>

---

### C24. Climbing stairs (basic dynamic programming)

You can climb 1 or 2 steps. How many ways reach step `n`?

<details>
<summary>Click to see solution</summary>

```java
static int climbStairs(int n) {
    if (n <= 2) return n;
    int oneBack = 2, twoBack = 1;
    for (int step = 3; step <= n; step++) {
        int current = oneBack + twoBack;
        twoBack = oneBack;
        oneBack = current;
    }
    return oneBack;
}
```

State: `dp[i] = dp[i-1] + dp[i-2]`. Time O(n), space O(1). Explain recursion → memoization → bottom-up optimization.
</details>

---

## Night-before checklist (Round 1)

Say these out loud without notes:

1. HashMap: array, `(n-1) & hash`, list then tree at 8 / table 64, load factor 0.75, not thread-safe. Java 7 concurrent resize could infinite-loop.
2. equals true ⇒ hashCode same. Reverse is not required. Integer cache −128..127.
3. HashSet is HashMap with dummy value `PRESENT`.
4. ConcurrentHashMap: CAS + per-bin lock, no nulls, weakly consistent iterators.
5. ArrayList for get-by-index. ArrayDeque for queue/stack. LinkedList almost never.
6. String is immutable because of pool + HashMap keys + safety. Java is pass-by-value.
7. Abstract class vs interface: state/constructor vs multiple contracts + default methods. Diamond = override the default.
8. `map` one-to-one, `flatMap` flatten. Do not parallelStream blocking I/O.
9. Java 17 records/sealed; Java 21 virtual threads; Boot 3 needs 17.
10. `@Transactional` is a proxy. `this.method()` skips it. Rollback on runtime exceptions.
11. `@Controller` cannot be swapped with `@Service`.
12. N+1: one query plus one per row. Fix with join fetch / DTO. OSIV off if I can. persist vs merge. SEQUENCE batches, IDENTITY does not. `@Version` optimistic lock. Owning side = FK.
13. Spring Security: filter chain, 401 vs 403, BCrypt, CSRF on for cookie sessions / off for stateless JWT, `@PreAuthorize` is a proxy. ESM is XML + `@Authorizer`, not Boot JWT.
14. Employee–manager: self LEFT JOIN.
15. Deadlock: lock order. CountDownLatch vs CyclicBarrier vs Semaphore.
16. Singleton: holder class or enum. DCL needs volatile. Spring singleton ≠ GOF singleton.
17. Generics: erasure, PECS. Serialization: avoid for APIs; `serialVersionUID`, `readResolve`.
18. G1 is default. Weak vs Soft vs Phantom. Heap dump for leaks.
19. Reverse list: prev / curr / next. ThreadPoolExecutor: unbounded queue ignores max pool.
20. 90-second ESM intro — no Kafka deep dive unless they ask.

---

## How to talk in Round 1

1. Repeat the question in half a sentence (“You want HashMap internals…”).
2. Give the short answer first.
3. If they nod, stop. If they say “internally?”, go to the long version.
4. For code: brute force first, then improve, then complexity.
5. If you do not know: say so, then reason. Do not invent APIs.

Use Round 2 for distributed systems and architecture; Round 3 for manager questions; Round 4 for HR.
