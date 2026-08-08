# 27. Callable and Future

## 27. Callable and Future

## Frequently Asked

---

# 1. Difference between Runnable and Callable?

<details>
<summary>Show Answer</summary>

**Answer:**

`Runnable` runs a task with **no return value** and cannot throw checked exceptions. `Callable` runs a task that **returns a result** and can throw checked exceptions.

### Comparison Table

| | `Runnable` | `Callable<V>` |
|---|------------|---------------|
| Return value | ❌ void | ✅ Returns `V` |
| Checked exceptions | ❌ Cannot throw | ✅ Can throw |
| Method | `void run()` | `V call()` throws Exception |
| Used with | `Thread`, `Executor.execute()` | `ExecutorService.submit()` |
| Result access | Shared variables only | `Future<V>` |

### Runnable

```java
Runnable task = () -> {
    System.out.println("Running"); // no return
};

new Thread(task).start();
executor.execute(task); // void — no Future
```

### Callable

```java
Callable<String> task = () -> {
    return fetchDataFromAPI(); // returns result
};

Future<String> future = executor.submit(task);
String result = future.get();
```

### Exception Handling Difference

```java
// Runnable — cannot throw checked exception
Runnable r = () -> {
    // throw new IOException(); // compile error!
};

// Callable — can throw checked exception
Callable<String> c = () -> {
    return readFile(); // throws IOException — OK
};
```

### When to Use Each

```text
Runnable:  fire-and-forget tasks, logging, notifications
Callable:  need return value, API calls, computations, DB queries
```

**Interview Point:**

> Runnable = void, no checked exceptions. Callable = returns value, throws exceptions, used with submit() + Future. Use Callable when you need a result.

</details>

---

# 2. Why Callable introduced?

<details>
<summary>Show Answer</summary>

**Answer:**

`Callable` was introduced because `Runnable` **cannot return a value** or throw checked exceptions—forcing awkward workarounds like shared mutable state to get results from async tasks.

### Problems with Runnable

```java
// ❌ Runnable cannot return — use shared variable
final String[] result = new String[1]; // awkward!

Runnable task = () -> {
    result[0] = fetchData(); // hack to get return value
};
new Thread(task).start();
// race condition on result[0] — not thread-safe without sync
```

### Runnable Cannot Throw Checked Exceptions

```java
Runnable task = () -> {
    readFile(); // throws IOException — compile error in Runnable!
};
```

### Callable Solves Both

```java
Callable<String> task = () -> {
    return readFile(); // returns value + throws IOException
};

Future<String> future = executor.submit(task);
String data = future.get(); // clean result retrieval
```

### Callable + Future Pattern

```text
Callable:  defines task that RETURNS a result
Future:    handle to retrieve result asynchronously
submit():  connects Callable → Future
```

### Before Callable (Java 1.x)

```text
Options to get async result:
  1. Shared mutable variable + synchronization (error-prone)
  2. Callback interfaces (complex)
  3. Custom result containers
  → All awkward and error-prone
```

### With Callable (Java 5+)

```java
Future<Integer> future = executor.submit(() -> computeSum());
Integer sum = future.get(); // clean, type-safe result
```

**Interview Point:**

> Callable introduced for tasks that **return values** and **throw checked exceptions**. Pairs with Future for async result retrieval. Part of Java 5 Executor Framework.

</details>

---

# 3. Future interface?

<details>
<summary>Show Answer</summary>

**Answer:**

`Future` represents the **result of an asynchronous computation**—a handle returned by `submit()` that lets you check status, get the result, or cancel the task.

### Interface Methods

```java
public interface Future<V> {
    boolean cancel(boolean mayInterruptIfRunning);
    boolean isCancelled();
    boolean isDone();
    V get() throws InterruptedException, ExecutionException;
    V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException;
}
```

### Basic Usage

```java
ExecutorService executor = Executors.newFixedThreadPool(2);

Future<String> future = executor.submit(() -> {
    Thread.sleep(2000);
    return "Hello from async task";
});

// Check status without blocking
System.out.println("Done? " + future.isDone()); // false

String result = future.get(); // blocks until complete
System.out.println(result);   // "Hello from async task"
```

### Future Lifecycle

```text
submit() called
    ↓
Task running → isDone() = false
    ↓
Task completes → isDone() = true
    ↓
get() returns result immediately (already done)
```

### Future States

| State | `isDone()` | `isCancelled()` |
|-------|------------|-------------------|
| Running | false | false |
| Completed | true | false |
| Cancelled | true | true |

### Multiple Futures

```java
Future<String> f1 = executor.submit(() -> fetchUser());
Future<String> f2 = executor.submit(() -> fetchOrders());
Future<String> f3 = executor.submit(() -> fetchPayments());

// Wait for all
String user     = f1.get();
String orders   = f2.get();
String payments = f3.get();
```

### Future Is a Promise

```text
Future = promise of a result that will be available later
  submit() → get Future immediately (result not yet available)
  get()    → wait for and retrieve result
```

**Interview Point:**

> Future = async result handle. `isDone()`, `isCancelled()`, `get()`. Returned by `submit()`. Represents pending/completed computation.

</details>

---

# 4. Future.get()?

<details>
<summary>Show Answer</summary>

**Answer:**

`Future.get()` **blocks the calling thread** until the task completes and returns the result—or throws an exception if the task failed.

### get() Variants

```java
Future<String> future = executor.submit(() -> fetchData());

// Blocks indefinitely until done
String result = future.get();

// Blocks up to 5 seconds
String result = future.get(5, TimeUnit.SECONDS);
```

### What get() Does

```text
1. If task complete → return result immediately
2. If task running → caller BLOCKED (WAITING state)
3. Task completes → return result, caller continues
4. Task threw exception → ExecutionException thrown
5. Caller interrupted → InterruptedException thrown
6. Timeout expires → TimeoutException thrown
```

### ExecutionException — Task Failed

```java
Future<String> future = executor.submit(() -> {
    throw new RuntimeException("DB connection failed");
});

try {
    String result = future.get();
} catch (ExecutionException e) {
    // Task failed — get actual cause
    Throwable cause = e.getCause();
    System.out.println("Task failed: " + cause.getMessage());
}
```

### get() Blocks Caller

```java
Future<Integer> future = executor.submit(() -> {
    Thread.sleep(5000); // 5 second task
    return 42;
});

System.out.println("Waiting...");
Integer result = future.get(); // blocks 5 seconds here!
System.out.println("Got: " + result);
```

### Non-Blocking Check Before get()

```java
Future<String> future = executor.submit(() -> fetchData());

// Do other work while task runs
prepareUI();

if (future.isDone()) {
    String data = future.get(); // instant — already done
} else {
    showLoadingSpinner();
    String data = future.get(); // wait now
}
```

### get() vs isDone()

```text
isDone()  → check status, non-blocking
get()     → wait for result, blocking
get(timeout) → wait with timeout, throws TimeoutException
```

**Interview Point:**

> `get()` blocks until result ready. Throws `ExecutionException` if task failed. Use `get(timeout)` to avoid infinite wait. Check `isDone()` before get() for non-blocking.

</details>

---

## Advanced

---

# 5. Cancellation?

<details>
<summary>Show Answer</summary>

**Answer:**

`Future.cancel(mayInterruptIfRunning)` attempts to **stop a running or queued task**—returns `true` if cancellation succeeded, `false` if already done or couldn't cancel.

### cancel() Method

```java
Future<String> future = executor.submit(() -> {
    while (!Thread.currentThread().isInterrupted()) {
        processChunk();
    }
    return "done";
});

// Cancel the task
boolean cancelled = future.cancel(true);
// true = mayInterruptIfRunning
// false = don't interrupt if already running
```

### mayInterruptIfRunning Parameter

| Value | Behavior |
|-------|--------|
| `true` | Interrupt thread if task is running |
| `false` | Don't interrupt — only cancel if not yet started |

### Cancellation States

```java
Future<?> future = executor.submit(longRunningTask);

future.cancel(true);
future.isCancelled(); // true
future.isDone();      // true (cancelled counts as done)

future.get(); // throws CancellationException
```

### Cooperative Cancellation Pattern

```java
Future<?> future = executor.submit(() -> {
    while (!Thread.currentThread().isInterrupted()) {
        try {
            doWork();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // restore flag
            break; // exit on interrupt
        }
    }
});

// Later — cancel cooperatively
future.cancel(true); // sends interrupt to worker thread
```

### Cancel vs shutdownNow()

```text
future.cancel(true):     cancel ONE specific task
executor.shutdownNow():  cancel ALL queued + interrupt all running
```

### Limitations

```text
❌ Cannot force-kill unresponsive task (cooperative only)
❌ cancel(false) on running task — task continues
❌ Already completed task — cancel returns false
✅ Task must respond to interrupt flag
```

**Interview Point:**

> `cancel(true)` sends interrupt to running thread. Cooperative — task must check interrupt flag. `isCancelled()` to verify. Use `shutdownNow()` to cancel all pool tasks.

</details>

---

# 6. Timeout handling?

<details>
<summary>Show Answer</summary>

**Answer:**

`Future.get(timeout, TimeUnit)` waits for the result only up to the specified duration—throws `TimeoutException` if the task doesn't complete within the timeout.

### Timed get()

```java
Future<String> future = executor.submit(() -> {
    Thread.sleep(10000); // 10 second task
    return "result";
});

try {
    String result = future.get(3, TimeUnit.SECONDS); // wait max 3 sec
} catch (TimeoutException e) {
    System.out.println("Task too slow — timed out!");
    future.cancel(true); // cancel slow task
}
```

### Timeout Handling Pattern

```java
Future<OrderResult> future = executor.submit(() -> processOrder(order));

try {
    OrderResult result = future.get(30, TimeUnit.SECONDS);
    return result;
} catch (TimeoutException e) {
    future.cancel(true);
    log.warn("Order processing timed out: {}", order.getId());
    return OrderResult.timeout();
} catch (ExecutionException e) {
    log.error("Order failed: {}", e.getCause().getMessage());
    return OrderResult.failed();
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
    return OrderResult.cancelled();
}
```

### Multiple Futures with Timeout

```java
List<Future<String>> futures = executor.invokeAll(
    tasks,
    10, TimeUnit.SECONDS // all tasks timeout after 10s
);
// Completed futures return result
// Timed-out futures: isCancelled() = true
```

### Timeout vs No Timeout

| | `get()` | `get(timeout, unit)` |
|---|---------|----------------------|
| Wait | Forever | Max specified time |
| On timeout | Never | `TimeoutException` |
| Task state after timeout | Still running | Still running (must cancel) |
| Use for | Must wait | Responsive systems |

### Important — Cancel After Timeout

```java
try {
    result = future.get(5, TimeUnit.SECONDS);
} catch (TimeoutException e) {
    future.cancel(true); // IMPORTANT — task still running!
    // Without cancel, task continues consuming resources
}
```

**Interview Point:**

> `get(timeout, unit)` prevents infinite blocking. On `TimeoutException` — task still running, call `cancel(true)`. Essential for responsive production systems.

</details>

---

# 7. Future limitations?

<details>
<summary>Show Answer</summary>

**Answer:**

`Future` has several limitations—**blocking get()**, **no manual completion**, **no chaining**, and **single result only**—addressed by `CompletableFuture` in Java 8.

### Limitation 1 — get() Is Blocking

```java
// Future.get() blocks caller thread
String result = future.get(); // WAITING state — no other work

// Cannot do: "notify me when done" without blocking
```

### Limitation 2 — No Completion Callback

```java
// ❌ Cannot register callback on Future
future.onComplete(result -> process(result)); // doesn't exist!

// Must poll or block:
while (!future.isDone()) { Thread.sleep(100); } // polling — bad
String r = future.get(); // blocking
```

### Limitation 3 — No Chaining

```java
// ❌ Cannot chain: fetch user → fetch orders → process
Future<User> userFuture = executor.submit(() -> fetchUser());
User user = userFuture.get(); // block
Future<Orders> ordersFuture = executor.submit(() -> fetchOrders(user));
Orders orders = ordersFuture.get(); // block again

// CompletableFuture allows:
// fetchUser().thenCompose(u -> fetchOrders(u)).thenApply(process)
```

### Limitation 4 — No Combining Multiple Futures

```java
// ❌ No built-in way to wait for ALL or ANY futures
Future<String> f1 = executor.submit(task1);
Future<String> f2 = executor.submit(task2);
// Must manually get() each — no allOf/anyOf
```

### Limitation 5 — Single Execution

```text
Future represents ONE task result
Cannot be reused or reset
New task → new Future
```

### Limitation 6 — Exception Handling

```java
// Only via catch on get()
try {
    result = future.get();
} catch (ExecutionException e) {
    handle(e.getCause());
}
// No declarative exception handling
```

### CompletableFuture Fixes These (Java 8)

```java
CompletableFuture.supplyAsync(() -> fetchUser())
    .thenApply(user -> fetchOrders(user))    // chaining
    .thenAccept(orders -> process(orders))   // callback
    .exceptionally(e -> handleError(e));     // error handling

CompletableFuture.allOf(f1, f2, f3);        // combine futures
```

### Summary Table

| Limitation | Future | CompletableFuture |
|------------|--------|-------------------|
| Blocking get | ✅ Only option | ✅ + callbacks |
| Callbacks | ❌ | ✅ thenApply/Accept |
| Chaining | ❌ | ✅ thenCompose |
| Combine futures | ❌ Manual | ✅ allOf/anyOf |
| Exception handling | catch on get() | exceptionally() |

**Interview Point:**

> Future limitations: blocking get(), no callbacks, no chaining, no combine. CompletableFuture (Java 8) fixes all. Know when to upgrade from Future to CompletableFuture.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: submit(Runnable) returns Future<?> — how to get result?

<details>
<summary>Show Answer</summary>

**Answer:**

`submit(Runnable)` returns `Future<?>` — `get()` returns `null`. To get a result, use `submit(Callable)` or `submit(Runnable, result)` which returns the provided result object on completion.

```java
Future<String> f = executor.submit(() -> doWork(), "completed");
String result = f.get(); // "completed"
```

</details>

---

### Q: invokeAll() vs multiple submit()?

<details>
<summary>Show Answer</summary>

**Answer:**

`invokeAll(tasks)` submits all tasks and **waits for ALL to complete** (or timeout). Returns list of Futures all done. Multiple `submit()` + `get()` each — more control but more code.

```java
List<Future<String>> results = executor.invokeAll(callables);
// All futures are done when invokeAll returns
```

</details>

---

### Q: Future.get() after cancel()?

<details>
<summary>Show Answer</summary>

**Answer:**

Throws **`CancellationException`** if task was cancelled before completing. Always check `isCancelled()` or catch `CancellationException` before calling `get()`.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Callable = Runnable with return value + exceptions. Future = async result handle. `get()` blocks. `cancel(true)` for cooperative stop. `get(timeout)` for timeout. Limitations → use CompletableFuture for callbacks and chaining.

</details>
