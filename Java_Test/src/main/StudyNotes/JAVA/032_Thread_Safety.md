# 32. Thread Safety

## 32. Thread Safety

## Frequently Asked

---

# 1. What is thread safety?

<details>
<summary>Show Answer</summary>

**Answer:**

**Thread safety** means a class or code behaves **correctly** when **multiple threads use it at the same time**—no corrupted data, no lost updates, no crashes.

### Simple Idea

```text
Thread-safe:   10 threads add to counter → counter = 10 ✅
Not safe:        10 threads add to counter → counter = 7 ❌ (lost updates)
```

### What Goes Wrong Without Thread Safety

| Problem | Example |
|---------|---------|
| **Race condition** | Two threads read same value, both increment, one write lost |
| **Inconsistent state** | Object half-updated when another thread reads |
| **Exception** | `ConcurrentModificationException`, `ArrayIndexOutOfBounds` |
| **Deadlock** | Threads wait forever on each other |

### Broken Example

```java
class Counter {
    private int count = 0;

    public void increment() {
        count++; // NOT atomic — read, add, write in 3 steps
    }
}

// 2 threads both call increment() 1000 times
// Expected: 2000
// Actual:   often less (e.g., 1847) — race condition
```

### Thread-Safe Example

```java
class SafeCounter {
    private AtomicInteger count = new AtomicInteger(0);

    public void increment() {
        count.incrementAndGet(); // atomic — safe
    }
}
```

### Levels of Thread Safety

| Level | Meaning |
|-------|---------|
| **Not thread-safe** | Must sync externally (HashMap, ArrayList) |
| **Conditionally safe** | Safe if used correctly (Iterator) |
| **Thread-safe** | Safe for concurrent use (ConcurrentHashMap) |
| **Immutable** | Always safe — cannot change (String, Integer) |

### Real Production Examples

```text
✅ Thread-safe:  ConcurrentHashMap, AtomicInteger, String
❌ Not safe:     HashMap, ArrayList, SimpleDateFormat
⚠️ Per-request:  HttpServletRequest in Spring (one thread per request)
```

**Interview Point:**

> Thread safety = correct behavior under concurrent access. Race conditions cause lost updates and corruption. Use sync, locks, atomic classes, or immutable design.

</details>

---

# 2. How to make class thread-safe?

<details>
<summary>Show Answer</summary>

**Answer:**

Make a class thread-safe by ensuring **only one thread modifies shared data at a time**, or by **not sharing mutable state** at all.

### 5 Main Approaches

| Approach | How | When to Use |
|----------|-----|-------------|
| **1. Synchronization** | `synchronized` method/block | Simple shared mutable state |
| **2. Locks** | `ReentrantLock`, `ReadWriteLock` | More control than synchronized |
| **3. Atomic classes** | `AtomicInteger`, `AtomicReference` | Single variable updates |
| **4. Immutable objects** | `final` fields, no setters | Best when state doesn't change |
| **5. Thread confinement** | Don't share — each thread has own copy | Per-thread data |

### 1. Synchronization

```java
class SafeCounter {
    private int count = 0;

    public synchronized void increment() {
        count++; // only one thread at a time
    }

    public synchronized int getCount() {
        return count;
    }
}
```

### 2. Locks

```java
class SafeCounter {
    private int count = 0;
    private final Lock lock = new ReentrantLock();

    public void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock(); // always unlock in finally
        }
    }
}
```

### 3. Atomic Classes

```java
class SafeCounter {
    private final AtomicInteger count = new AtomicInteger(0);

    public void increment() {
        count.incrementAndGet(); // CAS — no explicit lock
    }
}
```

### 4. Immutable Class

```java
public final class ImmutableUser {
    private final String name;
    private final int age;

    public ImmutableUser(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() { return name; }
    public int getAge() { return age; }
    // no setters — cannot change after creation
}
```

### 5. Thread Confinement — Don't Share

```java
// Each thread creates its own object — no sharing
void process() {
    List<String> localList = new ArrayList<>(); // thread-local
    localList.add("data");
    // safe — no other thread touches localList
}
```

### Use Concurrent Collections

```java
// Instead of wrapping HashMap manually:
Map<String, String> map = new ConcurrentHashMap<>();
List<String> list = Collections.synchronizedList(new ArrayList<>());
// or CopyOnWriteArrayList for read-heavy
```

### Decision Guide

```text
Single counter/status flag     → AtomicInteger / AtomicBoolean
Shared map/list                → ConcurrentHashMap / synchronized wrapper
Complex business logic         → synchronized or ReentrantLock
Data never changes             → Immutable class
Per-thread context             → ThreadLocal or thread confinement
```

**Interview Point:**

> Five ways: sync, locks, atomics, immutability, thread confinement. Prefer immutability and concurrent collections. Synchronization is simplest but can hurt performance under contention.

</details>

---

# 3. Immutable class thread-safe?

<details>
<summary>Show Answer</summary>

**Answer:**

**Yes** — immutable classes are **automatically thread-safe** because their state **cannot change** after creation. No thread can corrupt what no thread can modify.

### Why Immutable = Thread-Safe

```text
Thread A reads user.getName() → "John"
Thread B reads user.getName() → "John"
No setter exists → nobody can change "John" to "Jane"
No race condition possible on mutable state
```

### Rules for Immutable Class

```java
public final class ImmutablePerson {
    private final String name;       // 1. final class
    private final List<String> tags; // 2. final fields

    public ImmutablePerson(String name, List<String> tags) {
        this.name = name;
        // 3. defensive copy on input
        this.tags = new ArrayList<>(tags);
    }

    public String getName() { return name; }

    public List<String> getTags() {
        // 4. defensive copy on output
        return new ArrayList<>(tags);
    }
    // 5. no setters
}
```

| Rule | Why |
|------|-----|
| `final` class | Cannot be subclassed to add mutable state |
| `final` fields | Cannot reassign reference |
| No setters | Cannot change values |
| Defensive copy | External list/array cannot mutate internal state |
| Initialize in constructor | All fields set once |

### Built-In Immutable Examples

```java
String s = "hello";
s = "world"; // creates NEW String — original "hello" unchanged

Integer i = 10;
i = 20; // new Integer object — old 10 untouched

List<String> list = List.of("a", "b"); // immutable list
list.add("c"); // UnsupportedOperationException
```

### Caveat — Reference Field Must Be Truly Immutable

```java
// ❌ Looks immutable but NOT safe
public final class Broken {
    private final Date date; // Date is MUTABLE!

    public Broken(Date date) {
        this.date = date; // external code can still change date
    }
}

Date d = new Date();
Broken b = new Broken(d);
d.setTime(0); // mutates internal date! NOT thread-safe

// ✅ Fix — use immutable java.time
private final Instant instant; // immutable
```

**Interview Point:**

> Immutable classes are inherently thread-safe — no mutable state to fight over. Must deep-copy mutable fields (Date, List). String, Integer, `java.time` classes are immutable and safe.

</details>

---

# 4. Stateless class thread-safe?

<details>
<summary>Show Answer</summary>

**Answer:**

**Yes** — a **stateless class** is thread-safe because it holds **no instance variables** (or only constants). Each method call uses only its parameters and local variables—nothing shared between threads.

### Stateless vs Stateful

```text
Stateless:  no instance fields that change
Stateful:   has instance fields multiple threads can access
```

### Stateless — Thread-Safe

```java
@Service
public class TaxCalculator {

    // No mutable instance fields — only constants OK
    private static final double DEFAULT_RATE = 0.18;

    public double calculateTax(double amount, double rate) {
        return amount * rate; // uses only parameters + locals
    }
}

// 100 threads call calculateTax() simultaneously → safe ✅
```

### Stateful — NOT Thread-Safe

```java
@Service
public class OrderService {
    private int orderCount = 0; // SHARED mutable state ❌

    public void processOrder(Order order) {
        orderCount++; // race condition
        save(order);
    }
}
```

### Stateless in Spring — Why It Matters

```text
Spring creates ONE singleton bean per service
  → If bean has mutable fields → all HTTP threads share them
  → Stateless service beans → safe by default

@RequestScope / prototype beans → different lifecycle
```

### Stateless Service Pattern (Production)

```java
@Service
public class UserService {

    private final UserRepository repo; // injected — repo must be thread-safe

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public User findById(Long id) {
        return repo.findById(id); // no shared mutable state in this class
    }
}
```

### When Stateless Is NOT Enough

```text
✅ Stateless class + thread-safe dependencies → safe
❌ Stateless class + non-thread-safe dependency → still broken

Example:
  Stateless service uses HashMap as instance field in another bean
  → still not safe
```

### Constants Are OK

```java
private static final Logger log = LoggerFactory.getLogger(...); // safe
private final UserRepository repo; // safe if repo is thread-safe
private static final Map<String, String> CACHE = Map.of("key", "val"); // immutable — safe
```

**Interview Point:**

> Stateless = no mutable instance state → automatically thread-safe. Spring singleton services should be stateless. Injected dependencies must also be thread-safe.

</details>

---

## Advanced

---

# 5. Thread confinement?

<details>
<summary>Show Answer</summary>

**Answer:**

**Thread confinement** means data is **used by only one thread**—never shared. Since no other thread can access it, **no synchronization is needed**.

### Simple Idea

```text
Don't share → no conflict → automatically safe
Like each person has their own notebook — nobody fights over it
```

### Types of Thread Confinement

| Type | How |
|------|-----|
| **Ad-hoc** | Developer ensures no sharing (fragile) |
| **Stack confinement** | Local variables — on thread's stack |
| **ThreadLocal** | Per-thread copy of shared variable |
| **Single-threaded executor** | Only one thread runs tasks |

### 1. Stack Confinement — Local Variables

```java
public void processOrders(List<Order> orders) {
  List<Order> failed = new ArrayList<>(); // local — only THIS thread sees it

    for (Order o : orders) {
        if (!validate(o)) {
            failed.add(o); // safe — no other thread touches failed
        }
    }
    report(failed);
}
// failed dies when method ends — never shared
```

### 2. ThreadLocal Confinement

```java
private static final ThreadLocal<UserContext> context =
    ThreadLocal.withInitial(() -> new UserContext());

// Thread A sets its own context
context.set(userA); // only Thread A sees userA

// Thread B sets its own context
context.set(userB); // only Thread B sees userB — no conflict
```

### 3. Single-Threaded Executor

```java
ExecutorService singleThread =
    Executors.newSingleThreadExecutor();

// All tasks run on ONE thread — no concurrent access to shared state
singleThread.submit(() -> updateState());
singleThread.submit(() -> updateState()); // queued — runs one at a time
```

### Thread Confinement vs Synchronization

| | Thread Confinement | Synchronization |
|---|-------------------|-----------------|
| Sharing | No sharing | Shared data |
| Performance | Best — no lock overhead | Lock contention possible |
| Complexity | Simple if achievable | More complex |
| Risk | Easy to accidentally share | Deadlock risk |

### Production Example — Servlet Request

```text
Each HTTP request = one thread (typically)
  Request object passed as parameter → confined to that thread
  No other thread reads same request object
  → request-scoped data is naturally thread-confined
```

**Interview Point:**

> Thread confinement = don't share mutable data. Local variables are stack-confined. ThreadLocal gives per-thread copies. Cheapest thread-safety — no locks needed.

</details>

---

# 6. ThreadLocal?

<details>
<summary>Show Answer</summary>

**Answer:**

`ThreadLocal` gives **each thread its own separate copy** of a variable. Threads don't share the value—they each have their own isolated copy.

### Simple Analogy

```text
Hotel room key card:
  Each guest (thread) gets their own card
  Your card opens YOUR room only
  You don't share keys with other guests
```

### Basic Usage

```java
private static final ThreadLocal<String> userIdHolder =
    ThreadLocal.withInitial(() -> "unknown");

// Thread A (handling user 101)
userIdHolder.set("101");
System.out.println(userIdHolder.get()); // "101"

// Thread B (handling user 202) — at same time
userIdHolder.set("202");
System.out.println(userIdHolder.get()); // "202"

// Thread A still sees "101" — not affected by Thread B
```

### Common Pattern — User Context

```java
public class UserContextHolder {
    private static final ThreadLocal<User> context = new ThreadLocal<>();

    public static void set(User user) {
        context.set(user);
    }

    public static User get() {
        return context.get();
    }

    public static void clear() {
        context.remove(); // MUST clear — prevents leak in thread pools
    }
}

// In filter/interceptor (start of request):
UserContextHolder.set(currentUser);

// Anywhere in same request thread:
User user = UserContextHolder.get(); // no need to pass user everywhere

// End of request:
UserContextHolder.clear();
```

### Internal Idea (Simple)

```text
Each Thread object has a Map<ThreadLocal, Value>
  Thread-1 → { userContext → UserA, locale → "en" }
  Thread-2 → { userContext → UserB, locale → "fr" }

ThreadLocal.get() → looks up current thread's map
```

### Always Remove in Thread Pools

```java
// ❌ Dangerous with thread pool
try {
    context.set(user);
    doWork();
} finally {
    context.remove(); // ✅ MUST — pooled thread reused for next request
}
// Without remove: next request on same thread sees OLD user data!
```

### ThreadLocal vs Shared Variable

| | Shared Variable | ThreadLocal |
|---|----------------|-------------|
| Value | One for all threads | One per thread |
| Sync needed | Yes | No |
| Use case | Global counter | Per-request user/locale |
| Leak risk | No | Yes if not removed |

**Interview Point:**

> ThreadLocal = per-thread copy of a variable. Used for user context, locale, DB connection (old pattern). **Always `remove()`** in thread pools to avoid leaks and wrong data.

</details>

---

# 7. Why ThreadLocal used?

<details>
<summary>Show Answer</summary>

**Answer:**

`ThreadLocal` is used when you need **per-thread data** that should **not be shared**—without passing it through every method call.

### Main Use Cases

| Use Case | Why ThreadLocal |
|----------|-----------------|
| **User/request context** | Current user, tenant ID in multi-tenant app |
| **Locale/timezone** | Per-request language formatting |
| **Transaction context** | Which DB transaction this thread is in |
| **Logging MDC** | Correlation ID per request in logs |
| **Avoid parameter passing** | Don't pass user through 10 method layers |

### 1. Security — Current User (Most Common)

```java
// Without ThreadLocal — pass user everywhere
service.process(order, user);
  → helper.validate(order, user);
    → audit.log(order, user); // user passed 3 levels deep

// With ThreadLocal — set once, read anywhere
UserContextHolder.set(user);
service.process(order);
  → helper.validate(order);
    → audit.log(order); // reads from ThreadLocal internally
```

### 2. Logging — MDC (Mapped Diagnostic Context)

```java
// Set correlation ID at request start
MDC.put("correlationId", requestId);

log.info("Processing order"); // log includes correlationId automatically
// All logs in this thread tagged with same ID — easy to trace in production

MDC.clear(); // end of request
```

### 3. Spring `@Transactional`

```text
Spring stores current transaction in ThreadLocal
  → same thread's DB calls use same connection/transaction
  → different threads have separate transactions
```

### 4. SimpleDateFormat (Old Pattern — Avoid Now)

```java
// SimpleDateFormat is NOT thread-safe
// Old fix: one per thread
private static final ThreadLocal<SimpleDateFormat> formatter =
    ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

// Modern fix: use java.time — immutable and thread-safe ✅
LocalDate.parse("2024-03-15"); // no ThreadLocal needed
```

### When NOT to Use ThreadLocal

```text
❌ Global cache (use ConcurrentHashMap)
❌ Data that must be shared across threads
❌ Entity class fields
❌ When you can pass parameter cleanly (2 levels deep)
❌ Async code — child thread does NOT inherit ThreadLocal (use Reactor Context)
```

### ThreadLocal in Async — Warning

```java
// Parent thread sets context
UserContextHolder.set(userA);

executor.submit(() -> {
    UserContextHolder.get(); // null or wrong! — child thread is different
});

// Fix: pass explicitly or use framework support (Spring @Async with TaskDecorator)
```

### Memory Leak Risk

```text
Thread pool reuses threads → ThreadLocal value survives after request
  → old User object held in memory → leak
  → wrong user on next request

Fix: always context.remove() in finally block or servlet filter
```

**Interview Point:**

> ThreadLocal for per-thread context (user, MDC, transaction) without passing through every method. **Must remove()** in pools. Doesn't propagate to async child threads. Prefer `java.time` over ThreadLocal-wrapped SimpleDateFormat.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Thread-safe vs synchronized?

<details>
<summary>Show Answer</summary>

**Answer:**

**Thread-safe** is a **property** — code works correctly with multiple threads. **Synchronized** is one **mechanism** to achieve thread safety. You can be thread-safe via atomics, immutability, or concurrent collections without `synchronized`.

</details>

---

### Q: Is HashMap thread-safe?

<details>
<summary>Show Answer</summary>

**Answer:**

**No.** `HashMap` is not thread-safe. Concurrent `put()` from multiple threads can corrupt internal structure or cause infinite loops (Java 7). Use `ConcurrentHashMap` or wrap with `Collections.synchronizedMap()`.

</details>

---

### Q: Spring singleton bean thread-safe?

<details>
<summary>Show Answer</summary>

**Answer:**

**Only if stateless** or uses thread-safe dependencies. Singleton = one instance shared by all request threads. Mutable instance fields on singleton bean = **not safe**. Keep services stateless; use `@RequestScope` for per-request state.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Thread safety = correct under concurrent access. Achieve via sync, locks, atomics, immutability, or thread confinement. **Immutable** and **stateless** classes are automatically safe. **ThreadLocal** = per-thread copy — use for context, always `remove()` in pools.

</details>
