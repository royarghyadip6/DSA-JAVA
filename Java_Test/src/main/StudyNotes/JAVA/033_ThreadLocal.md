# 33. ThreadLocal

## 33. ThreadLocal

## Important Production Topic

---

# 1. What is ThreadLocal?

<details>
<summary>Show Answer</summary>

**Answer:**

`ThreadLocal` is a Java class that stores a **value per thread**—each thread gets its **own independent copy**. Threads never share the same value, so no synchronization is needed.

### Simple Analogy

```text
Gym locker:
  Each member (thread) has their own locker (ThreadLocal slot)
  You put your bag in YOUR locker
  Other members cannot see or change your bag
```

### Basic API

```java
ThreadLocal<String> holder = ThreadLocal.withInitial(() -> "default");

holder.set("Alice");   // current thread's value
String v = holder.get(); // read current thread's value
holder.remove();         // clear current thread's value
```

### Visual — Two Threads

```text
Thread-1:  holder.set("User-A")  →  Thread-1's slot = "User-A"
Thread-2:  holder.set("User-B")  →  Thread-2's slot = "User-B"

Thread-1.get() → "User-A"   (not "User-B")
Thread-2.get() → "User-B"   (not "User-A")
```

### ThreadLocal vs Normal Shared Field

```java
// ❌ Shared — needs synchronization
private String currentUser; // all threads fight over this

// ✅ ThreadLocal — no sync needed
private static final ThreadLocal<String> currentUser = new ThreadLocal<>();
// each thread has own currentUser
```

### Static ThreadLocal Is Common

```java
// ThreadLocal is usually static
private static final ThreadLocal<User> CONTEXT = new ThreadLocal<>();

// static = one ThreadLocal object globally
// but each thread still has its OWN value inside it
```

### Real Production Uses

```text
Current logged-in user in a request
Correlation ID for logging (MDC)
Spring transaction binding
Locale / timezone per request
Tenant ID in multi-tenant SaaS
```

**Interview Point:**

> ThreadLocal = per-thread variable storage. One ThreadLocal object, many thread-specific values. No sharing = no sync. Always static in practice.

</details>

---

# 2. How ThreadLocal works internally?

<details>
<summary>Show Answer</summary>

**Answer:**

Internally, each `Thread` object holds a `ThreadLocalMap`—a hash map where **keys are ThreadLocal instances** and **values are the per-thread data**.

### Internal Structure (Simplified)

```text
Thread-1 object
  └── threadLocals (ThreadLocalMap)
        ├── ThreadLocal@A → Value for A on Thread-1
        └── ThreadLocal@B → Value for B on Thread-1

Thread-2 object
  └── threadLocals (ThreadLocalMap)
        ├── ThreadLocal@A → Value for A on Thread-2  (different!)
        └── ThreadLocal@B → Value for B on Thread-2
```

### What Happens on set()

```java
threadLocal.set("hello");

// Internally:
// 1. Get current Thread object
// 2. Get thread.threadLocals (ThreadLocalMap)
// 3. If map null → create new ThreadLocalMap
// 4. map.set(this ThreadLocal, "hello")
```

### What Happens on get()

```java
String v = threadLocal.get();

// Internally:
// 1. Get current Thread
// 2. Get thread.threadLocals
// 3. entry = map.getEntry(this ThreadLocal)
// 4. If found → return entry.value
// 5. If not → call initialValue() and set it
```

### ThreadLocalMap Entry — Weak Reference Key

```text
Entry extends WeakReference<ThreadLocal>
  Key:   WeakReference to ThreadLocal object
  Value: Strong reference to actual data

Why weak key?
  When ThreadLocal variable goes out of scope (no strong ref),
  GC can collect ThreadLocal → key becomes null
  BUT value may still be held → memory leak risk!
```

### Memory Leak Path

```text
Thread (strong ref from pool) → ThreadLocalMap → Entry
  Entry.key = null (ThreadLocal GC'd)
  Entry.value = User object (STILL HELD!) ← leak

Thread pool reuses thread → old User never released
```

### Diagram

```text
  ThreadLocal (static, strong ref)
       │
       ▼ set/get uses current thread
  ┌─────────┐
  │ Thread  │──► ThreadLocalMap
  └─────────┘       │
                    ├── [WeakRef→TL] → User@101
                    └── [WeakRef→TL] → Locale@en
```

**Interview Point:**

> Each Thread has ThreadLocalMap. Keys are weak refs to ThreadLocal; values are strong. Weak key + pooled threads + no remove() = classic memory leak.

</details>

---

# 3. Use cases?

<details>
<summary>Show Answer</summary>

**Answer:**

`ThreadLocal` is used when **each thread needs its own context** that should not be shared—and you want to avoid passing that context through every method.

### Top Production Use Cases

| Use Case | What Is Stored | Framework |
|----------|----------------|-----------|
| **Request user context** | Current user, roles | Custom filter + ThreadLocal |
| **Logging MDC** | correlationId, traceId | SLF4J MDC |
| **Transaction** | DB connection, transaction | Spring `@Transactional` |
| **Multi-tenant** | tenantId | SaaS apps |
| **Locale** | Language, timezone | i18n formatting |
| **Security** | Authentication token | Custom security context |

### 1. User Context — Full Pattern

```java
public final class RequestContext {
    private static final ThreadLocal<String> userId = new ThreadLocal<>();
    private static final ThreadLocal<String> tenantId = new ThreadLocal<>();

    public static void set(String uid, String tid) {
        userId.set(uid);
        tenantId.set(tid);
    }

    public static String getUserId() { return userId.get(); }
    public static String getTenantId() { return tenantId.get(); }

    public static void clear() {
        userId.remove();
        tenantId.remove();
    }
}

// Servlet filter — start of every HTTP request
public void doFilter(...) {
    try {
        RequestContext.set(extractUserId(request), extractTenant(request));
        chain.doFilter(request, response);
    } finally {
        RequestContext.clear(); // always clean up
    }
}
```

### 2. MDC Logging — Trace Every Request

```java
// At request entry
MDC.put("traceId", UUID.randomUUID().toString());
MDC.put("userId", user.getId());

log.info("Order created"); // log: [traceId=abc-123 userId=42] Order created

// At request end
MDC.clear();
```

### 3. Spring Transaction Management

```text
@Service method with @Transactional:
  Spring binds Connection to current thread via ThreadLocal
  All repo calls in same thread → same connection → same transaction
  Different thread → different connection → separate transaction
```

### 4. Multi-Tenant SaaS

```java
// Every DB query filters by tenant
String tenant = TenantContext.get();
repo.findAllByTenant(tenant);

// Tenant set once per request — not passed to every repo method
```

### When NOT to Use

```text
❌ Caching shared data (use ConcurrentHashMap)
❌ Counters across threads (use AtomicInteger)
❌ Async child threads (ThreadLocal not inherited)
❌ Passing data 1–2 method levels (just use parameter)
```

**Interview Point:**

> ThreadLocal for per-request context: user, tenant, traceId, transaction. Set at request start, clear at end. Spring transactions and SLF4J MDC use it internally.

</details>

---

## Advanced

---

# 4. Memory leak with ThreadLocal?

<details>
<summary>Show Answer</summary>

**Answer:**

`ThreadLocal` can cause **memory leaks** when a **long-lived thread** (thread pool) holds a reference to a **large object** after the `ThreadLocal` itself is no longer needed—and **`remove()` was never called**.

### How the Leak Happens

```text
1. Request arrives → pool thread T1 handles it
2. threadLocal.set(bigUserObject)  → stored in T1's ThreadLocalMap
3. Request ends → but remove() NOT called
4. T1 returns to pool (thread NOT destroyed — reused)
5. T1's ThreadLocalMap STILL holds bigUserObject
6. 1000 requests → 1000 leaked objects on pooled threads
7. Old Generation fills → OutOfMemoryError
```

### Why Thread Doesn't Die

```text
Normal thread:  request ends → thread dies → map garbage collected ✅
Thread pool:    request ends → thread REUSED → map survives ❌
```

### Weak Key Doesn't Save Value

```text
ThreadLocal key = WeakReference
  → ThreadLocal object can be GC'd when no strong ref
  → Entry.key becomes null
  → Entry.value STILL strongly referenced!
  → Value cannot be GC'd until thread dies or remove() called
```

### Leak Example

```java
// ❌ Leak pattern
private static final ThreadLocal<byte[]> BUFFER =
    ThreadLocal.withInitial(() -> new byte[1024 * 1024]); // 1 MB per thread

public void handleRequest() {
    byte[] buf = BUFFER.get(); // 1 MB attached to pool thread forever
    process(buf);
    // forgot BUFFER.remove() → 1 MB leaked per pool thread
}

// 50 pool threads × 1 MB = 50 MB never released
```

### Real Production Incident Pattern

```text
Symptom:     Heap grows over days, Full GC doesn't help
Tool:        heap dump → dominator tree shows Thread objects
Root cause:  ThreadLocalMap entries on pool threads
Fix:         remove() in filter finally block
```

### Safe Pattern

```java
try {
    context.set(user);
    doWork();
} finally {
    context.remove(); // ✅ releases value from ThreadLocalMap
}
```

**Interview Point:**

> ThreadLocal leak = pooled thread + strong value ref + no remove(). Weak key only GC's ThreadLocal object, not the value. Always remove() in finally. Classic production OOM cause.

</details>

---

# 5. Thread pools and ThreadLocal issue?

<details>
<summary>Show Answer</summary>

**Answer:**

Thread pools **reuse threads**, so `ThreadLocal` values from a **previous request can leak into the next request** on the same thread—wrong user data, security breach, or memory leak.

### The Problem — Stale Data

```text
Request 1 (Thread T1):  set user = Admin
Request 1 ends:           remove() NOT called
Request 2 (Thread T1):  get() → still Admin! ❌
  User B's request runs with Admin privileges → security bug
```

### Code Demonstrating Bug

```java
private static final ThreadLocal<String> ROLE = new ThreadLocal<>();

// Request 1 — admin
ROLE.set("ADMIN");
processRequest(); // ends without remove()

// Later — same pool thread, different user
String role = ROLE.get(); // "ADMIN" — WRONG! should be null or "USER"
```

### Why Pools Make It Worse

| | New Thread Each Request | Thread Pool |
|---|------------------------|-------------|
| Thread dies after request | Yes | No — reused |
| ThreadLocal auto-cleaned | Yes (thread GC) | No |
| Stale data risk | Low | **High** |
| Memory leak risk | Low | **High** |

### Spring / Servlet — Built-In Risk

```text
Tomcat thread pool: 200 threads handle ALL requests
  Thread-5 handles User-A request → sets ThreadLocal
  Thread-5 handles User-B request → old value still there if not cleared

Fix: Filter/Interceptor with finally { context.remove() }
```

### Async Makes It Worse

```java
// Parent request thread
UserContext.set(adminUser);

executor.submit(() -> {
    // Child thread — DIFFERENT thread
    UserContext.get(); // null! ThreadLocal NOT inherited by default
});

// @Async in Spring — same problem unless TaskDecorator configured
```

### Solutions

```text
1. Always remove() in finally block (mandatory)
2. Servlet Filter cleans ThreadLocal at end of every request
3. Spring TaskDecorator copies ThreadLocal to async child threads
4. Prefer framework abstractions (SecurityContextHolder manages cleanup)
```

### Spring SecurityContextHolder

```java
// Spring stores security context in ThreadLocal
// SecurityContextPersistenceFilter automatically clears after request
// You should follow same pattern for custom ThreadLocals
```

**Interview Point:**

> Thread pools reuse threads → stale ThreadLocal data crosses requests. Security risk + memory leak. Mandatory filter with `finally { remove() }`. Async threads don't inherit ThreadLocal.

</details>

---

# 6. How to clean ThreadLocal?

<details>
<summary>Show Answer</summary>

**Answer:**

Clean `ThreadLocal` by calling **`remove()`** on the current thread's value—typically in a **`finally` block** at the end of every request or task.

### 1. Basic — Always finally

```java
private static final ThreadLocal<User> CONTEXT = new ThreadLocal<>();

public void process(User user) {
    try {
        CONTEXT.set(user);
        doBusinessLogic();
    } finally {
        CONTEXT.remove(); // ✅ always runs — even on exception
    }
}
```

### 2. Servlet Filter — Production Pattern

```java
@Component
public class RequestContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        try {
            HttpServletRequest httpReq = (HttpServletRequest) req;
            String userId = extractUserId(httpReq);
            String tenantId = extractTenantId(httpReq);
            RequestContext.set(userId, tenantId);
            chain.doFilter(req, res);
        } finally {
            RequestContext.clear(); // remove ALL ThreadLocals
        }
    }
}
```

### 3. Centralized Holder with clear()

```java
public final class RequestContext {
    private static final ThreadLocal<String> userId = new ThreadLocal<>();
    private static final ThreadLocal<String> tenantId = new ThreadLocal<>();

    public static void set(String uid, String tid) {
        userId.set(uid);
        tenantId.set(tid);
    }

    public static void clear() {
        userId.remove();
        tenantId.remove();
        // remove EVERY ThreadLocal in this holder
    }
}
```

### 4. MDC — Logging Cleanup

```java
try {
    MDC.put("traceId", traceId);
    log.info("Starting job");
    runJob();
} finally {
    MDC.clear(); // removes all MDC keys for this thread
}
```

### 5. Thread Pool Task Wrapper

```java
public class ContextAwareRunnable implements Runnable {
    private final Runnable task;
    private final String userId;

    public ContextAwareRunnable(Runnable task, String userId) {
        this.task = task;
        this.userId = userId;
    }

    @Override
    public void run() {
        try {
            RequestContext.set(userId, null);
            task.run();
        } finally {
            RequestContext.clear();
        }
    }
}

executor.submit(new ContextAwareRunnable(() -> doWork(), currentUserId));
```

### remove() vs set(null)

| | `remove()` | `set(null)` |
|---|-----------|-------------|
| Entry removed from map | Yes | No — entry stays with null value |
| Memory released | Yes | Map slot still occupied |
| get() after | calls initialValue() | returns null |
| **Preferred** | ✅ | ❌ |

### Checklist for Production

```text
✅ remove() in finally block
✅ One filter/interceptor per app entry point
✅ clear() method removes ALL ThreadLocals in holder
✅ Async tasks: copy context + clean in child thread
✅ Never rely on thread death for cleanup in pools
✅ Monitor heap for Thread → ThreadLocalMap in dumps
```

**Interview Point:**

> Clean with `remove()` in `finally` — not `set(null)`. Servlet filter is standard pattern. MDC.clear() for logging. Wrap pool tasks to set and clear context in child thread.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: ThreadLocal inherited by child thread?

<details>
<summary>Show Answer</summary>

**Answer:**

**No** — child threads do **not** inherit parent's ThreadLocal values. `InheritableThreadLocal` can pass to child threads created directly, but **not** to pool threads. Use explicit context passing or Spring `TaskDecorator` for async.

</details>

---

### Q: ThreadLocal vs synchronized?

<details>
<summary>Show Answer</summary>

**Answer:**

**ThreadLocal** = each thread has own copy — no sharing, no lock. **Synchronized** = threads share one value — lock controls access. ThreadLocal for per-thread context; synchronized for shared mutable state.

</details>

---

### Q: How did you debug ThreadLocal leak?

<details>
<summary>Show Answer</summary>

**Answer:**

Heap dump → Eclipse MAT / VisualVM → dominator tree → `Thread` objects holding large `ThreadLocalMap` → inspect entries → find leaked User/Connection objects → trace to missing `remove()` in filter or pool task → add `finally { context.clear() }`.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> ThreadLocal = per-thread map on each Thread object. Use for user context, MDC, transactions. **Pooled threads + no remove() = memory leak + stale data.** Always `remove()` in filter `finally`. Async threads don't inherit — pass explicitly.

</details>
