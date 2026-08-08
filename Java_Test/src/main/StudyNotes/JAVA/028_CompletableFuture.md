# 28. CompletableFuture

## 28. CompletableFuture

## Extremely Important (Java 8+)

### Basics

---

# 1. Why CompletableFuture introduced?

<details>
<summary>Show Answer</summary>

**Answer:**

`CompletableFuture` (Java 8) was introduced to fix **`Future` limitations**—enabling **non-blocking callbacks, chaining, combining async tasks**, and declarative error handling for modern async programming.

### What Future Couldn't Do

```text
❌ Blocking get() only way to get result
❌ No completion callbacks
❌ No chaining of async operations
❌ No combining multiple futures
❌ Manual exception handling via catch
```

### What CompletableFuture Adds

```text
✅ Non-blocking callbacks (thenApply, thenAccept)
✅ Chain async operations (thenCompose)
✅ Combine futures (allOf, anyOf, thenCombine)
✅ Declarative error handling (exceptionally, handle)
✅ Manual completion (complete, completeExceptionally)
✅ Async execution control (supplyAsync, custom Executor)
```

### Before vs After

```java
// ❌ Future — blocking, manual chaining
Future<User> userFuture = executor.submit(() -> fetchUser());
User user = userFuture.get(); // BLOCKS
Future<Orders> ordersFuture = executor.submit(() -> fetchOrders(user));
Orders orders = ordersFuture.get(); // BLOCKS again

// ✅ CompletableFuture — non-blocking chain
CompletableFuture<Orders> ordersFuture =
    CompletableFuture.supplyAsync(() -> fetchUser())
        .thenCompose(user -> fetchOrdersAsync(user)); // chain
```

### Implements Future + CompletionStage

```java
public class CompletableFuture<T>
    implements Future<T>, CompletionStage<T> {
    // Future: get(), cancel()
    // CompletionStage: thenApply, thenCompose, etc.
}
```

**Interview Point:**

> CompletableFuture = Future + CompletionStage. Fixes Future's blocking/chaining limitations. Core of modern Java async programming since Java 8.

</details>

---

# 2. Problems with Future?

<details>
<summary>Show Answer</summary>

**Answer:**

`Future` has **six major problems** that make it unsuitable for modern async programming—blocking retrieval, no callbacks, no chaining, no combining, and poor error handling.

### Problem 1 — Blocking get()

```java
Future<String> future = executor.submit(() -> fetchData());
String data = future.get(); // caller BLOCKED — defeats async purpose
```

### Problem 2 — No Callbacks

```java
// ❌ Cannot say "when done, do this"
future.onComplete(result -> process(result)); // doesn't exist

// Must poll:
while (!future.isDone()) { Thread.sleep(100); } // wasteful
```

### Problem 3 — No Chaining

```java
// Sequential blocking — not true async pipeline
User user = fetchUserFuture.get();
Orders orders = fetchOrdersFuture(user).get();
Summary summary = buildSummary(user, orders).get();
```

### Problem 4 — Cannot Combine Futures

```java
// Wait for multiple — manual
Future<String> f1 = executor.submit(task1);
Future<String> f2 = executor.submit(task2);
String r1 = f1.get(); // block
String r2 = f2.get(); // block
// No allOf(), anyOf()
```

### Problem 5 — Poor Exception Handling

```java
try {
    result = future.get();
} catch (ExecutionException e) {
    handle(e.getCause()); // only at get() time
}
```

### Problem 6 — Cannot Complete Manually

```text
Future = result of submitted task only
Cannot complete Future from outside
No way to fulfill a pending Future manually
```

### CompletableFuture Fixes All

| Future Problem | CompletableFuture Solution |
|----------------|---------------------------|
| Blocking get | thenApply, thenAccept callbacks |
| No callbacks | thenApply, whenComplete |
| No chaining | thenCompose, thenApply chain |
| No combining | allOf, anyOf, thenCombine |
| Poor errors | exceptionally, handle |
| No manual complete | complete(), completeExceptionally() |

**Interview Point:**

> Future problems: blocking, no callbacks, no chain, no combine, poor errors. CompletableFuture solves all. Know this comparison for senior interviews.

</details>

---

# 3. Async programming?

<details>
<summary>Show Answer</summary>

**Answer:**

**Async programming** executes tasks **without blocking the caller**—the main thread continues while work happens in background threads, with results delivered via callbacks or Futures when ready.

### Sync vs Async

```java
// Synchronous — caller waits
String data = fetchFromAPI(); // blocks 2 seconds
process(data);

// Asynchronous — caller continues
CompletableFuture.supplyAsync(() -> fetchFromAPI())
    .thenAccept(data -> process(data)); // callback when ready
// caller continues immediately
```

### Async Programming Model

```text
1. Submit async task
2. Caller continues (non-blocking)
3. Task runs in background thread
4. Result delivered via callback/Future when done
```

### CompletableFuture Async Pipeline

```java
CompletableFuture.supplyAsync(() -> fetchUser(userId))   // async step 1
    .thenApply(user -> fetchOrders(user))                 // chain step 2
    .thenApply(orders -> calculateTotal(orders))          // chain step 3
    .thenAccept(total -> sendInvoice(total))              // final action
    .exceptionally(e -> { log.error(e); return null; }); // error handler
```

### Benefits

| Benefit | Explanation |
|---------|-------------|
| **Responsiveness** | UI/API doesn't freeze waiting |
| **Throughput** | Handle more requests concurrently |
| **Resource efficiency** | Threads not blocked on IO |
| **Composition** | Chain and combine async operations |

### Common Async Patterns

```text
Parallel API calls:   fetch user + orders + payments simultaneously
Pipeline:             fetch → transform → save → notify
Fan-out/Fan-in:       split work → parallel process → combine results
Event-driven:         onComplete callbacks
```

### Async in Java Stack

```text
CompletableFuture  → async composition (Java 8+)
ExecutorService    → thread pool for async execution
@Async (Spring)    → method-level async
Reactive (WebFlux) → fully reactive stack
```

**Interview Point:**

> Async = non-blocking execution + callback/Future for results. CompletableFuture enables declarative async pipelines. Essential for microservices and IO-bound workloads.

</details>

---

## Creation

---

# 4. supplyAsync()

<details>
<summary>Show Answer</summary>

**Answer:**

`supplyAsync()` creates a `CompletableFuture` that **runs a `Supplier` asynchronously** and completes with the supplier's return value.

### Basic Usage

```java
CompletableFuture<String> future =
    CompletableFuture.supplyAsync(() -> {
        return fetchDataFromAPI(); // runs in background thread
    });

String result = future.get(); // or use thenApply chain
```

### With Custom Executor

```java
ExecutorService executor = Executors.newFixedThreadPool(10);

CompletableFuture<String> future =
    CompletableFuture.supplyAsync(() -> fetchData(), executor);
// Uses specified executor instead of default ForkJoinPool
```

### Default Executor

```text
supplyAsync() without executor:
  Uses ForkJoinPool.commonPool()
  Parallelism = Runtime.getRuntime().availableProcessors() - 1
```

### supplyAsync vs runAsync

| | `supplyAsync(Supplier)` | `runAsync(Runnable)` |
|---|------------------------|----------------------|
| Returns | `CompletableFuture<T>` | `CompletableFuture<Void>` |
| Input | Supplier<T> — returns value | Runnable — no return |
| Use for | Fetch data, compute result | Fire-and-forget task |

### Chaining After supplyAsync

```java
CompletableFuture<Integer> future =
    CompletableFuture.supplyAsync(() -> fetchPrice())
        .thenApply(price -> price * 1.1)  // add 10% tax
        .thenApply(total -> (int) total);
```

**Interview Point:**

> `supplyAsync(Supplier)` = async task with return value. Default: ForkJoinPool.commonPool(). Pass custom Executor for control.

</details>

---

# 5. runAsync()

<details>
<summary>Show Answer</summary>

**Answer:**

`runAsync()` creates a `CompletableFuture<Void>` that runs a **Runnable asynchronously**—for fire-and-forget tasks with no return value.

### Basic Usage

```java
CompletableFuture<Void> future =
    CompletableFuture.runAsync(() -> {
        sendEmailNotification(user); // no return value
        log.info("Email sent");
    });
// CompletableFuture<Void> — no result to retrieve
```

### With Custom Executor

```java
ExecutorService emailExecutor = Executors.newFixedThreadPool(2);

CompletableFuture.runAsync(() -> sendEmail(user), emailExecutor);
```

### Fire-and-Forget Pattern

```java
// Log async — don't wait for result
CompletableFuture.runAsync(() -> auditLog.log(event));

// Continue main flow immediately
processOrder(order);
```

### runAsync vs supplyAsync

```java
// runAsync — no return
CompletableFuture<Void> f1 =
    CompletableFuture.runAsync(() -> cleanupTempFiles());

// supplyAsync — returns value
CompletableFuture<String> f2 =
    CompletableFuture.supplyAsync(() -> fetchConfig());
```

### Waiting for Completion

```java
CompletableFuture<Void> future = CompletableFuture.runAsync(() -> doWork());

future.join(); // wait for completion (no result)
future.get();  // returns null (Void)
```

**Interview Point:**

> `runAsync(Runnable)` = async fire-and-forget, returns `CompletableFuture<Void>`. Use for logging, notifications, cleanup — no result needed.

</details>

---

## Transformation

---

# 6. thenApply()

<details>
<summary>Show Answer</summary>

**Answer:**

`thenApply()` **transforms the result** of a completed `CompletableFuture` using a `Function`—like `Stream.map()` but for async pipelines.

### Basic Usage

```java
CompletableFuture<String> future =
    CompletableFuture.supplyAsync(() -> "hello")
        .thenApply(s -> s.toUpperCase())   // "HELLO"
        .thenApply(s -> s + " WORLD");     // "HELLO WORLD"
```

### Real Example

```java
CompletableFuture<Double> priceFuture =
    CompletableFuture.supplyAsync(() -> fetchProductPrice(productId))
        .thenApply(price -> price * 0.9)    // apply 10% discount
        .thenApply(discounted -> discounted + calculateTax(discounted));
```

### thenApply vs thenApplyAsync

```java
// thenApply — runs in SAME thread as previous stage (or caller thread)
future.thenApply(x -> transform(x));

// thenApplyAsync — runs in ForkJoinPool (or custom executor)
future.thenApplyAsync(x -> transform(x));
```

### Function Signature

```java
<U> CompletableFuture<U> thenApply(Function<? super T, ? extends U> fn)

// T = input type from previous stage
// U = output type of this stage
// Returns new CompletableFuture<U>
```

### Chain Example

```java
CompletableFuture.supplyAsync(() -> fetchUserId())
    .thenApply(id -> fetchUser(id))       // User
    .thenApply(user -> user.getEmail())   // String
    .thenApply(email -> email.toLowerCase()); // String
```

**Interview Point:**

> `thenApply(Function)` = transform result synchronously in pipeline. Returns new CompletableFuture with transformed value. Like map() for async.

</details>

---

# 7. thenAccept()

<details>
<summary>Show Answer</summary>

**Answer:**

`thenAccept()` **consumes the result** using a `Consumer`—performs an action on the value without returning anything (`CompletableFuture<Void>`).

### Basic Usage

```java
CompletableFuture.supplyAsync(() -> fetchUser())
    .thenAccept(user -> {
        System.out.println("User: " + user.getName()); // consume result
        sendWelcomeEmail(user);
    });
// Returns CompletableFuture<Void> — terminal consumer
```

### vs thenApply

| | `thenApply` | `thenAccept` |
|---|-------------|--------------|
| Input | Function<T, U> | Consumer<T> |
| Returns | `CompletableFuture<U>` | `CompletableFuture<Void>` |
| Purpose | Transform value | Consume value |
| Analogy | Stream.map() | Stream.forEach() |

### Real Example

```java
CompletableFuture.supplyAsync(() -> calculateReport())
    .thenAccept(report -> {
        saveToDatabase(report);
        sendEmailToManager(report);
    }); // consume — no further chaining of value
```

### Terminal Action

```java
// thenAccept is often the last step
CompletableFuture.supplyAsync(() -> fetchOrder())
    .thenApply(order -> validateOrder(order))
    .thenApply(order -> processPayment(order))
    .thenAccept(order -> sendConfirmation(order)); // final action
```

### thenAccept vs thenRun

```java
// thenAccept — receives the result value
future.thenAccept(value -> process(value));

// thenRun — no access to result, just runs after completion
future.thenRun(() -> log.info("Done"));
```

**Interview Point:**

> `thenAccept(Consumer)` = consume result, no return. Terminal action in pipeline. Like forEach() for async. Use when you don't need to transform — just act on result.

</details>

---

# 8. thenRun()

<details>
<summary>Show Answer</summary>

**Answer:**

`thenRun()` runs an action **after the previous stage completes**—without access to the result value. Returns `CompletableFuture<Void>`.

### Basic Usage

```java
CompletableFuture.supplyAsync(() -> fetchData())
    .thenRun(() -> {
        System.out.println("Fetch completed!"); // no access to result
        updateStatus("DONE");
    });
```

### vs thenAccept

```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "result");

// thenAccept — receives result
future.thenAccept(result -> System.out.println(result)); // prints "result"

// thenRun — no result access
future.thenRun(() -> System.out.println("Done")); // just knows it completed
```

### Use Cases

```java
// Cleanup after async operation
CompletableFuture.supplyAsync(() -> processFile())
    .thenRun(() -> cleanupTempFiles());

// Status update after completion
CompletableFuture.runAsync(() -> backupDatabase())
    .thenRun(() -> notifyAdmin("Backup complete"));

// Trigger next step without needing result
CompletableFuture.supplyAsync(() -> fetchConfig())
    .thenRun(() -> startApplication()); // just needs to know config loaded
```

### Comparison

| Method | Access Result? | Returns |
|--------|---------------|---------|
| `thenApply` | ✅ Yes — transforms | `CompletableFuture<U>` |
| `thenAccept` | ✅ Yes — consumes | `CompletableFuture<Void>` |
| `thenRun` | ❌ No | `CompletableFuture<Void>` |

**Interview Point:**

> `thenRun()` = action after completion, no result access. Use for cleanup, status updates, triggering next step when result not needed.

</details>

---

## Combining

---

# 9. thenCompose()

<details>
<summary>Show Answer</summary>

**Answer:**

`thenCompose()` **flattens nested CompletableFutures**—when the next step itself returns a `CompletableFuture`, avoiding `CompletableFuture<CompletableFuture<T>>`.

### The Problem — Nested Futures

```java
// thenApply with async next step — NESTED future!
CompletableFuture<CompletableFuture<Orders>> nested =
    CompletableFuture.supplyAsync(() -> fetchUser())
        .thenApply(user -> fetchOrdersAsync(user));
// CompletableFuture<CompletableFuture<Orders>> — wrong!
```

### thenCompose Flattens

```java
CompletableFuture<Orders> flat =
    CompletableFuture.supplyAsync(() -> fetchUser())
        .thenCompose(user -> fetchOrdersAsync(user));
// CompletableFuture<Orders> — correct!
```

### Real Pipeline

```java
CompletableFuture<OrderSummary> summary =
    CompletableFuture.supplyAsync(() -> fetchUser(userId))
        .thenCompose(user -> fetchOrdersAsync(user))    // async step
        .thenCompose(orders -> buildSummaryAsync(orders)); // another async step
```

### thenApply vs thenCompose

| | `thenApply` | `thenCompose` |
|---|-------------|---------------|
| Function returns | `U` (plain value) | `CompletableFuture<U>` |
| Result type | `CompletableFuture<U>` | `CompletableFuture<U>` (flattened) |
| Use when | Sync transform | Next step is async |
| Analogy | Stream.map() | Stream.flatMap() |

### Dependency Chain

```java
// Each step depends on previous async result
CompletableFuture.supplyAsync(() -> authenticate(token))
    .thenCompose(auth -> authorizeAsync(auth))
    .thenCompose(session -> fetchDataAsync(session))
    .thenAccept(data -> display(data));
```

**Interview Point:**

> `thenCompose` = flatMap for CompletableFuture. Use when next step returns CompletableFuture. Prevents nested futures. Most important chaining method.

</details>

---

# 10. thenCombine()

<details>
<summary>Show Answer</summary>

**Answer:**

`thenCombine()` **combines two independent CompletableFutures**—waits for both to complete and merges their results using a `BiFunction`.

### Basic Usage

```java
CompletableFuture<String> future1 =
    CompletableFuture.supplyAsync(() -> fetchUserName());

CompletableFuture<Integer> future2 =
    CompletableFuture.supplyAsync(() -> fetchUserAge());

CompletableFuture<String> combined = future1.thenCombine(future2,
    (name, age) -> name + " is " + age + " years old"
);

String result = combined.get(); // "John is 30 years old"
```

### Parallel API Calls

```java
CompletableFuture<User> userFuture =
    CompletableFuture.supplyAsync(() -> userService.getUser(id));

CompletableFuture<List<Order>> ordersFuture =
    CompletableFuture.supplyAsync(() -> orderService.getOrders(id));

CompletableFuture<Dashboard> dashboard = userFuture.thenCombine(ordersFuture,
    (user, orders) -> new Dashboard(user, orders)
);
// Both API calls run in PARALLEL — faster than sequential
```

### vs thenCompose

```text
thenCompose:  sequential — step 2 depends on step 1 result
thenCombine:  parallel  — both run independently, merge at end
```

### allOf — Wait for All

```java
CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> fetchA());
CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> fetchB());
CompletableFuture<String> f3 = CompletableFuture.supplyAsync(() -> fetchC());

CompletableFuture<Void> all = CompletableFuture.allOf(f1, f2, f3);
all.join(); // wait for ALL to complete
String a = f1.get();
String b = f2.get();
String c = f3.get();
```

**Interview Point:**

> `thenCombine` = merge two parallel futures. Both run concurrently. Use for independent async operations that need combined result.

</details>

---

## Error Handling

---

# 11. exceptionally()

<details>
<summary>Show Answer</summary>

**Answer:**

`exceptionally()` handles failures in the pipeline—if the previous stage throws an exception, it runs a recovery `Function` and continues with a fallback value.

### Basic Usage

```java
CompletableFuture<String> future =
    CompletableFuture.supplyAsync(() -> fetchFromAPI())
        .exceptionally(ex -> {
            log.error("API failed: {}", ex.getMessage());
            return "default-value"; // fallback
        });

String result = future.get(); // "default-value" if API failed
```

### Recovery Pattern

```java
CompletableFuture<User> userFuture =
    CompletableFuture.supplyAsync(() -> primaryUserService.getUser(id))
        .exceptionally(ex -> {
            log.warn("Primary failed, using cache");
            return cacheService.getUser(id); // fallback
        });
```

### Only Handles Exceptions

```text
Previous stage succeeds → exceptionally SKIPPED
Previous stage throws   → exceptionally RUNS with exception as input
Returns normal CompletableFuture with recovery value
```

### exceptionally vs catch

```java
// Declarative — in pipeline
future.thenApply(transform)
      .exceptionally(ex -> fallback);

// vs blocking catch
try {
    result = future.get();
} catch (ExecutionException e) { ... }
```

### Limitation

```text
exceptionally only handles exceptional completion
Normal null results pass through unchanged
Only triggered on exception, not on null
```

**Interview Point:**

> `exceptionally(Function<Throwable, T>)` = async catch block. Returns fallback value on failure. Pipeline continues after recovery.

</details>

---

# 12. handle()

<details>
<summary>Show Answer</summary>

**Answer:**

`handle()` processes **both success and failure**—receives the result OR the exception and always returns a new value, regardless of outcome.

### Basic Usage

```java
CompletableFuture<String> future =
    CompletableFuture.supplyAsync(() -> fetchData())
        .handle((result, ex) -> {
            if (ex != null) {
                log.error("Failed: {}", ex.getMessage());
                return "fallback";
            }
            return result.toUpperCase(); // transform success
        });
```

### handle vs exceptionally

| | `exceptionally` | `handle` |
|---|-----------------|----------|
| Runs on success | ❌ Skipped | ✅ Runs with result |
| Runs on failure | ✅ Runs with exception | ✅ Runs with exception |
| Parameters | `Throwable` only | `(result, exception)` |
| Use for | Fallback only | Transform success OR handle failure |

### Unified Success/Failure Handler

```java
CompletableFuture<Response> response =
    CompletableFuture.supplyAsync(() -> callExternalAPI())
        .handle((data, ex) -> {
            if (ex != null) {
                return Response.error(ex.getMessage());
            }
            return Response.success(data);
        });
// Always returns Response — success or error wrapper
```

### Always Runs

```text
Success: handle(result, null) → transform result
Failure: handle(null, exception) → handle error
Always produces output — pipeline never breaks
```

**Interview Point:**

> `handle(result, ex)` = handles BOTH success and failure in one handler. More flexible than exceptionally. Use for unified response wrapping.

</details>

---

# 13. whenComplete()

<details>
<summary>Show Answer</summary>

**Answer:**

`whenComplete()` runs a **side-effect action** after completion—receives result and exception but **cannot change the result** (returns same CompletableFuture).

### Basic Usage

```java
CompletableFuture.supplyAsync(() -> fetchOrder())
    .whenComplete((order, ex) -> {
        if (ex != null) {
            log.error("Order fetch failed", ex);
        } else {
            log.info("Order fetched: {}", order.getId());
        }
    })
    .thenApply(order -> processOrder(order)); // pipeline continues
```

### whenComplete vs handle

| | `whenComplete` | `handle` |
|---|----------------|----------|
| Can change result | ❌ No — side effect only | ✅ Yes — returns new value |
| Returns | Same CompletableFuture<T> | New CompletableFuture<U> |
| Use for | Logging, metrics, audit | Transform or recover |
| Pipeline | Result passes through unchanged | Result may change |

### Logging / Metrics Pattern

```java
CompletableFuture.supplyAsync(() -> processPayment(order))
    .whenComplete((result, ex) -> {
        metrics.recordLatency(timer.elapsed());
        auditLog.log(order.getId(), ex == null ? "SUCCESS" : "FAILED");
    })
    .thenApply(result -> sendConfirmation(result));
```

### Exception Propagates

```java
CompletableFuture.supplyAsync(() -> { throw new RuntimeException("fail"); })
    .whenComplete((r, ex) -> log.error("Error", ex)) // logs error
    .thenApply(r -> process(r)); // still throws — whenComplete doesn't swallow!
```

**Interview Point:**

> `whenComplete` = side-effect only (logging, metrics). Cannot change result. Exception still propagates downstream. Like finally block in async pipeline.

</details>

---

## Advanced

---

# 14. Difference between thenApply and thenCompose?

<details>
<summary>Show Answer</summary>

**Answer:**

`thenApply()` transforms a result with a **synchronous function** returning a plain value. `thenCompose()` chains when the next step returns a **CompletableFuture**—flattening nested futures.

### thenApply — Sync Transform

```java
CompletableFuture<String> future =
    CompletableFuture.supplyAsync(() -> 42)
        .thenApply(n -> "Number: " + n); // returns String — sync transform
// CompletableFuture<String>
```

### thenCompose — Async Chain

```java
CompletableFuture<Orders> future =
    CompletableFuture.supplyAsync(() -> fetchUser())
        .thenCompose(user -> fetchOrdersAsync(user)); // returns CompletableFuture<Orders>
// CompletableFuture<Orders> — flattened, not nested
```

### Wrong — thenApply with Async

```java
// ❌ WRONG — nested CompletableFuture
CompletableFuture<CompletableFuture<Orders>> wrong =
    CompletableFuture.supplyAsync(() -> fetchUser())
        .thenApply(user -> fetchOrdersAsync(user));
// Type: CompletableFuture<CompletableFuture<Orders>> — nested!

Orders orders = wrong.get().get(); // double get() — ugly!
```

### Correct — thenCompose

```java
// ✅ CORRECT — flat CompletableFuture
CompletableFuture<Orders> correct =
    CompletableFuture.supplyAsync(() -> fetchUser())
        .thenCompose(user -> fetchOrdersAsync(user));
// Type: CompletableFuture<Orders> — flat!

Orders orders = correct.get(); // single get()
```

### Analogy with Streams

| CompletableFuture | Stream Equivalent |
|-------------------|-------------------|
| `thenApply(fn)` | `map(fn)` — transform |
| `thenCompose(fn)` | `flatMap(fn)` — flatten nested |

### Decision Rule

```text
Next step returns plain value T     → thenApply
Next step returns CompletableFuture<T> → thenCompose
```

**Interview Point:**

> thenApply = sync transform (map). thenCompose = async chain (flatMap). Wrong choice → nested CompletableFuture. Most common CompletableFuture interview question.

</details>

---

# 15. Async vs non-async methods?

<details>
<summary>Show Answer</summary>

**Answer:**

CompletableFuture has **sync variants** (`thenApply`) that run in the completing thread, and **async variants** (`thenApplyAsync`) that run in a separate thread from the executor pool.

### Sync Methods (Default)

```java
CompletableFuture.supplyAsync(() -> fetchData()) // runs in ForkJoinPool
    .thenApply(data -> transform(data))          // runs in SAME thread as supplyAsync
    .thenApply(data -> enrich(data));             // same thread chain
```

### Async Methods

```java
CompletableFuture.supplyAsync(() -> fetchData())       // ForkJoinPool thread
    .thenApplyAsync(data -> transform(data))          // different ForkJoinPool thread
    .thenApplyAsync(data -> enrich(data));             // another thread
```

### Method Pairs

| Sync (same thread) | Async (executor thread) |
|--------------------|--------------------------|
| `thenApply` | `thenApplyAsync` |
| `thenAccept` | `thenAcceptAsync` |
| `thenRun` | `thenRunAsync` |
| `thenCompose` | `thenComposeAsync` |
| `thenCombine` | `thenCombineAsync` |
| `whenComplete` | `whenCompleteAsync` |
| `handle` | `handleAsync` |

### When to Use Async Variants

```text
✅ CPU-intensive transform after IO fetch
✅ Don't want to block completing thread
✅ Long-running transform step
✅ Parallel processing of result

❌ Simple/fast transforms — sync is fine
❌ Already in async context — sync avoids thread switch overhead
```

### Custom Executor for Async Methods

```java
ExecutorService cpuPool = Executors.newFixedThreadPool(4);

CompletableFuture.supplyAsync(() -> fetchFromDB())     // IO pool
    .thenApplyAsync(data -> heavyCompute(data), cpuPool); // CPU pool
```

**Interview Point:**

> Sync methods run in completing thread. Async methods (*Async suffix) run in executor pool. Use Async for CPU-heavy transforms. Pass custom Executor to control thread pool.

</details>

---

# 16. Custom Executor with CompletableFuture?

<details>
<summary>Show Answer</summary>

**Answer:**

Pass a custom `Executor` to `supplyAsync`, `runAsync`, and all `*Async` methods to control **which thread pool** runs the task—isolating IO, CPU, and background workloads.

### Default vs Custom

```java
// Default — ForkJoinPool.commonPool()
CompletableFuture.supplyAsync(() -> fetchData());

// Custom executor
ExecutorService ioPool = Executors.newFixedThreadPool(20);
CompletableFuture.supplyAsync(() -> fetchData(), ioPool);
```

### Separate Pools by Workload

```java
ExecutorService ioPool    = Executors.newFixedThreadPool(20);  // DB/API calls
ExecutorService cpuPool   = Executors.newFixedThreadPool(4);   // computation
ExecutorService emailPool = Executors.newFixedThreadPool(2);   // notifications

// IO-bound fetch
CompletableFuture.supplyAsync(() -> fetchFromDB(), ioPool)
    .thenApplyAsync(data -> heavyAnalysis(data), cpuPool)
    .thenAcceptAsync(result -> sendEmail(result), emailPool);
```

### Spring @Async Integration

```java
@Bean
public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(20);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("async-");
    executor.initialize();
    return executor;
}

// Use in CompletableFuture
CompletableFuture.supplyAsync(() -> process(), taskExecutor);
```

### Why Custom Executor Matters

```text
ForkJoinPool.commonPool():
  Shared across all CompletableFuture default calls
  Parallelism = CPU cores - 1
  Blocking IO on common pool → starves other async tasks

Custom executor:
  Isolated pool for your workload
  Sized appropriately (IO vs CPU)
  Named threads for debugging
  Independent monitoring
```

### Production Pattern

```java
class AsyncConfig {
    static final ExecutorService IO_EXECUTOR =
        new ThreadPoolExecutor(10, 50, 60L, SECONDS,
            new ArrayBlockingQueue<>(200),
            new ThreadPoolExecutor.CallerRunsPolicy());
}

CompletableFuture.supplyAsync(() -> apiCall(), AsyncConfig.IO_EXECUTOR);
```

**Interview Point:**

> Always pass custom Executor for production CompletableFuture. Don't block ForkJoinPool.commonPool() with IO. Separate pools for IO, CPU, background tasks.

</details>

---

# 17. Parallel API calls use case?

<details>
<summary>Show Answer</summary>

**Answer:**

The classic `CompletableFuture` use case: **fetch multiple independent API/data sources in parallel** and combine results—much faster than sequential calls.

### Problem — Sequential (Slow)

```java
// ❌ Sequential — 3 seconds total (1s each)
User user     = userService.getUser(id);     // 1 second
List<Order> orders = orderService.getOrders(id); // 1 second
PaymentInfo payment = paymentService.getPayment(id); // 1 second
// Total: 3 seconds
```

### Solution — Parallel with CompletableFuture

```java
CompletableFuture<User> userFuture =
    CompletableFuture.supplyAsync(() -> userService.getUser(id));

CompletableFuture<List<Order>> ordersFuture =
    CompletableFuture.supplyAsync(() -> orderService.getOrders(id));

CompletableFuture<PaymentInfo> paymentFuture =
    CompletableFuture.supplyAsync(() -> paymentService.getPayment(id));

// All 3 run in PARALLEL — ~1 second total!
CompletableFuture<Void> all = CompletableFuture.allOf(
    userFuture, ordersFuture, paymentFuture);
all.join();

Dashboard dashboard = new Dashboard(
    userFuture.get(),
    ordersFuture.get(),
    paymentFuture.get()
);
```

### thenCombine Pattern

```java
CompletableFuture<Dashboard> dashboard =
    userFuture.thenCombine(ordersFuture,
        (user, orders) -> new Dashboard(user, orders))
    .thenCombine(paymentFuture,
        (dashboard, payment) -> {
            dashboard.setPayment(payment);
            return dashboard;
        });
```

### With Timeout

```java
CompletableFuture<User> userFuture =
    CompletableFuture.supplyAsync(() -> userService.getUser(id))
        .orTimeout(2, TimeUnit.SECONDS)        // Java 9+
        .exceptionally(ex -> getCachedUser(id)); // fallback
```

### Microservice Aggregation (BFF Pattern)

```java
// Backend-for-Frontend — aggregate multiple microservices
public CompletableFuture<OrderDetails> getOrderDetails(String orderId) {
    CompletableFuture<Order> orderF  = supplyAsync(() -> orderService.get(orderId));
    CompletableFuture<User> userF  = supplyAsync(() -> userService.getByOrder(orderId));
    CompletableFuture<Product> prodF = supplyAsync(() -> productService.getByOrder(orderId));

    return orderF.thenCombine(userF, (order, user) -> new OrderDetails(order, user))
                 .thenCombine(prodF, (details, product) -> {
                     details.setProduct(product);
                     return details;
                 });
}
```

### Performance Gain

```text
Sequential:  sum of all call times (1+1+1 = 3s)
Parallel:    max of all call times (max(1,1,1) = 1s)
3x faster for 3 independent calls
```

**Interview Point:**

> Parallel API calls = classic CompletableFuture use case. `supplyAsync` for each call + `allOf` or `thenCombine` to merge. Cuts latency from sum to max of call times.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: CompletableFuture.get() vs join()?

<details>
<summary>Show Answer</summary>

**Answer:**

Both wait for result. `get()` throws checked `InterruptedException` + `ExecutionException`. `join()` throws unchecked `CompletionException`. Prefer `join()` in lambda/stream contexts.

```java
String result = future.join(); // unchecked — cleaner in pipelines
```

</details>

---

### Q: allOf() vs anyOf()?

<details>
<summary>Show Answer</summary>

**Answer:**

`allOf(futures)` — waits for **ALL** to complete. `anyOf(futures)` — completes when **ANY ONE** completes. Both return `CompletableFuture<Void>` / `CompletableFuture<Object>`.

```java
CompletableFuture.anyOf(f1, f2, f3).join(); // first to complete
```

</details>

---

### Q: CompletableFuture vs @Async (Spring)?

<details>
<summary>Show Answer</summary>

**Answer:**

`@Async` = Spring annotation for async method execution, uses Spring's task executor. CompletableFuture = programmatic async composition with chaining. Often used together: `@Async` methods return CompletableFuture.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> CompletableFuture = non-blocking async pipelines. `supplyAsync`/`runAsync` to start. `thenApply` (map) vs `thenCompose` (flatMap). `thenCombine` for parallel merge. `exceptionally`/`handle` for errors. Custom Executor for production. Parallel API calls = killer use case.

</details>
