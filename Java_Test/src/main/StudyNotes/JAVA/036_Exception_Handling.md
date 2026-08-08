# 36. Exception Handling

## 36. Exception Handling

## Basics

---

# 1. What is Exception?

<details>
<summary>Show Answer</summary>

**Answer:**

An **exception** is an **unexpected event** that disrupts normal program flow—Java creates an exception object and transfers control to a handler (or crashes if unhandled).

### Simple Idea

```text
Normal flow:  read file → process → save
Exception:     read file → file not found → STOP normal flow → handle error
```

### Example

```java
try {
    int result = 10 / 0; // ArithmeticException
} catch (ArithmeticException e) {
    System.out.println("Cannot divide by zero");
}
```

### Without Handling

```java
int result = 10 / 0; // program crashes — stack trace printed
```

### Key Terms

| Term | Meaning |
|------|---------|
| **Throw** | Create and signal exception |
| **Catch** | Handle exception |
| **Stack trace** | Path of method calls when error occurred |

**Interview Point:**

> Exception = runtime problem that breaks normal flow. Use try-catch to handle gracefully instead of crashing.

</details>

---

# 2. Difference between Error and Exception?

<details>
<summary>Show Answer</summary>

**Answer:**

Both extend `Throwable`, but **Errors** are serious system problems you usually **cannot recover from**, while **Exceptions** are application-level problems you **can handle**.

| | Error | Exception |
|---|-------|-----------|
| Severity | Critical / fatal | Recoverable |
| Examples | `OutOfMemoryError`, `StackOverflowError` | `IOException`, `NullPointerException` |
| Should catch? | ❌ Generally no | ✅ Yes |
| Cause | JVM / system | Application / external input |

```text
Throwable
 ├── Error        (OutOfMemoryError, StackOverflowError)
 └── Exception
      ├── IOException (checked)
      └── RuntimeException (unchecked)
```

**Interview Point:**

> Don't catch Error. Catch Exception for recoverable problems. OOM and StackOverflow = fix code/config, not try-catch.

</details>

---

# 3. Difference between Checked and Unchecked Exception?

<details>
<summary>Show Answer</summary>

**Answer:**

| | Checked | Unchecked |
|---|---------|-----------|
| Parent | `Exception` (not RuntimeException) | `RuntimeException` |
| Compile-time check | ✅ Must handle or declare | ❌ No compile check |
| Examples | `IOException`, `SQLException` | `NullPointerException`, `IllegalArgumentException` |
| Meaning | Expected external failure | Programmer bug / logic error |

```java
// Checked — compiler forces handling
public void read() throws IOException {
    Files.readString(Path.of("file.txt"));
}

// Unchecked — no throws required
public void divide(int a, int b) {
    return a / b; // may throw ArithmeticException
}
```

**Interview Point:**

> Checked = compiler enforces handle/throws. Unchecked = RuntimeException subclasses — fix the bug, don't just catch.

</details>

---

# 4. RuntimeException hierarchy?

<details>
<summary>Show Answer</summary>

**Answer:**

`RuntimeException` is the base of **unchecked exceptions**—bugs and invalid API usage at runtime.

```text
RuntimeException
 ├── NullPointerException
 ├── IllegalArgumentException
 ├── IllegalStateException
 ├── ArithmeticException
 ├── IndexOutOfBoundsException
 │    ├── ArrayIndexOutOfBoundsException
 │    └── StringIndexOutOfBoundsException
 ├── ClassCastException
 ├── NumberFormatException
 └── ConcurrentModificationException
```

```java
List<String> list = new ArrayList<>();
list.get(10);           // IndexOutOfBoundsException
String s = null;
s.length();             // NullPointerException
```

**Interview Point:**

> All unchecked exceptions extend RuntimeException. Usually indicate bugs — validate inputs to avoid them.

</details>

---

# 5. Exception hierarchy?

<details>
<summary>Show Answer</summary>

**Answer:**

```text
Object
Throwable
 ├── Error
 │    ├── OutOfMemoryError
 │    └── StackOverflowError
 └── Exception
      ├── IOException (checked)
      ├── SQLException (checked)
      └── RuntimeException (unchecked)
           ├── NullPointerException
           ├── IllegalArgumentException
           └── ...
```

| Branch | Catch in app? |
|--------|---------------|
| Error | ❌ Rarely |
| Checked Exception | ✅ Handle or throws |
| RuntimeException | ✅ Optional — often let bubble or catch at boundary |

**Interview Point:**

> Throwable → Error (don't catch) + Exception (checked + unchecked via RuntimeException).

</details>

---

## Checked vs Unchecked

---

# 6. Why checked exceptions exist?

<details>
<summary>Show Answer</summary>

**Answer:**

Checked exceptions force the developer to **acknowledge recoverable failures** at compile time—especially I/O, network, and database problems that **will happen** in production.

### Why Java Added Them

```text
File might not exist     → IOException
Network might fail       → SocketException
DB connection might fail → SQLException

Compiler: "You MUST plan for this — handle or declare"
```

### Benefit

```java
// Compiler won't compile without throws or try-catch
public void loadConfig() throws IOException {
    Properties p = new Properties();
    p.load(new FileInputStream("config.properties"));
}
```

### Criticism (Know for Interviews)

```text
Some teams prefer unchecked for all — less boilerplate
Modern trend: unchecked custom exceptions + global handler
Still know checked — legacy APIs and Java standard library use them heavily
```

**Interview Point:**

> Checked exceptions = compile-time reminder for recoverable external failures. Controversial but still everywhere in Java I/O and JDBC.

</details>

---

# 7. Examples of checked exceptions?

<details>
<summary>Show Answer</summary>

**Answer:**

| Exception | When |
|-----------|------|
| `IOException` | File read/write fails |
| `FileNotFoundException` | File doesn't exist |
| `SQLException` | Database error |
| `ClassNotFoundException` | Class not found at runtime |
| `InterruptedException` | Thread interrupted during sleep/wait |

```java
try {
    Connection conn = DriverManager.getConnection(url);
} catch (SQLException e) {
  log.error("DB connection failed", e);
}
```

**Interview Point:**

> Checked = IOException family, SQLException, ClassNotFoundException, InterruptedException.

</details>

---

# 8. Examples of unchecked exceptions?

<details>
<summary>Show Answer</summary>

**Answer:**

| Exception | When |
|-----------|------|
| `NullPointerException` | Null reference used |
| `IllegalArgumentException` | Invalid argument |
| `IllegalStateException` | Invalid object state |
| `ArithmeticException` | Divide by zero |
| `ArrayIndexOutOfBoundsException` | Invalid array index |
| `ClassCastException` | Wrong cast |

```java
public void setAge(int age) {
    if (age < 0) {
        throw new IllegalArgumentException("Age cannot be negative");
    }
}
```

**Interview Point:**

> Unchecked = RuntimeException family. Often programmer mistakes or bad input — validate early.

</details>

---

# 9. When should you create checked exception?

<details>
<summary>Show Answer</summary>

**Answer:**

Create a **checked** custom exception when the caller **can and should recover** from the failure—and you want compile-time enforcement.

### Good Cases

```text
✅ Business rule violation caller can fix (retry, alternate path)
✅ External dependency failure with recovery (retry payment)
✅ Library API where failure is expected and documented
```

```java
public class InsufficientBalanceException extends Exception {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}

public void withdraw(double amount) throws InsufficientBalanceException {
    if (balance < amount) {
        throw new InsufficientBalanceException("Balance too low");
    }
}
```

### Avoid When

```text
❌ Programming bug (use unchecked)
❌ Every layer must wrap in try-catch (use unchecked + global handler)
```

**Interview Point:**

> Checked custom exception when caller must handle recovery. Many modern APIs prefer unchecked + documentation.

</details>

---

# 10. When should you create unchecked exception?

<details>
<summary>Show Answer</summary>

**Answer:**

Create **unchecked** when failure is a **bug, invalid input, or broken invariant**—caller shouldn't be forced to catch at every layer.

### Good Cases

```text
✅ Invalid argument (IllegalArgumentException style)
✅ Object used in wrong state
✅ Business rule that means "reject request" not "retry"
✅ REST API — map to 400/500 at boundary
```

```java
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long id) {
        super("Order not found: " + id);
    }
}

public Order getOrder(Long id) {
    return repo.findById(id)
        .orElseThrow(() -> new OrderNotFoundException(id));
}
```

**Interview Point:**

> Unchecked for bugs and API errors. Spring `@ControllerAdvice` catches at boundary. Don't force try-catch through 10 layers.

</details>

---

## try-catch-finally

---

# 11. Can try exist without catch?

<details>
<summary>Show Answer</summary>

**Answer:**

**Yes** — `try` can exist with **only `finally`**, or with **try-with-resources** (no catch required).

```java
// try + finally only
try {
    openResource();
} finally {
    closeResource();
}

// try-with-resources
try (InputStream in = new FileInputStream("file.txt")) {
    // use stream
} // auto-close — no catch needed
```

**Interview Point:**

> try without catch is valid if paired with finally or try-with-resources.

</details>

---

# 12. Can try exist without finally?

<details>
<summary>Show Answer</summary>

**Answer:**

**Yes** — `try-catch` without `finally` is common. `finally` is **optional**.

```java
try {
    process();
} catch (IOException e) {
    log.error("Failed", e);
}
// no finally — perfectly valid
```

**Interview Point:**

> finally is optional. Use when you must clean up regardless of success or failure.

</details>

---

# 13. Can finally exist without catch?

<details>
<summary>Show Answer</summary>

**Answer:**

**Yes** — `try-finally` without `catch` is valid. Exception propagates after `finally` runs.

```java
try {
    riskyOperation();
} finally {
    cleanup(); // always runs
}
// if riskyOperation throws, cleanup runs THEN exception propagates
```

**Interview Point:**

> try-finally = run cleanup, let exception bubble up. Common for manual resource cleanup (legacy code).

</details>

---

# 14. Is finally always executed?

<details>
<summary>Show Answer</summary>

**Answer:**

**Almost always** — `finally` runs after `try` or `catch`, whether or not an exception occurred.

```java
try {
    return 1;
} catch (Exception e) {
    return 2;
} finally {
    System.out.println("cleanup"); // runs BEFORE return completes
}
```

### Runs When

```text
✅ Normal completion
✅ Exception caught
✅ Exception not caught (then propagates after finally)
✅ return in try or catch
```

**Interview Point:**

> finally runs in almost all cases — cleanup code goes here. Exceptions: see Q15.

</details>

---

# 15. When finally is not executed?

<details>
<summary>Show Answer</summary>

**Answer:**

`finally` does **not** run when:

| Situation | Why |
|-----------|-----|
| `System.exit()` | JVM shuts down immediately |
| Fatal `Error` (e.g. some JVM crashes) | Process dies |
| Infinite loop in try | Never reaches finally |
| Thread killed abruptly | `Thread.stop()` (deprecated) |
| Power failure / kill -9 | OS kills process |

```java
try {
    System.out.println("start");
    System.exit(0); // finally SKIPPED
} finally {
    System.out.println("never printed");
}
```

**Interview Point:**

> finally skipped mainly by System.exit() and process kill. Normal exceptions still run finally.

</details>

---

## Advanced

---

# 16. What happens if exception occurs in finally?

<details>
<summary>Show Answer</summary>

**Answer:**

If `try` threw exception A and `finally` throws exception B, **B propagates** and **A may be lost** (unless suppressed in try-with-resources).

```java
try {
    throw new RuntimeException("from try");
} finally {
    throw new RuntimeException("from finally"); // THIS propagates
}
// Caller sees "from finally" — "from try" is lost
```

### Best Practice

```java
try {
    risky();
} finally {
    try {
        cleanup();
    } catch (Exception e) {
        log.error("Cleanup failed", e); // don't throw from finally
    }
}
```

**Interview Point:**

> Exception in finally can mask original exception. Never throw from finally — log instead.

</details>

---

# 17. What happens if return statement exists in try and finally?

<details>
<summary>Show Answer</summary>

**Answer:**

Both blocks can have `return`, but **`finally` runs before the method actually returns**. If `finally` also has `return`, it **overrides** try's return value.

```java
public int test() {
    try {
        return 1;
    } finally {
        System.out.println("finally runs");
        // return 2; // if uncommented — method returns 2, not 1
    }
}
// Output: "finally runs", returns 1
```

**Interview Point:**

> finally executes before return from try. return in finally overrides try's return — avoid return in finally.

</details>

---

# 18. Which return executes?

<details>
<summary>Show Answer</summary>

**Answer:**

| Scenario | Result |
|----------|--------|
| return only in try | try's value after finally runs |
| return in try and catch | whichever block executes |
| return in try AND finally | **finally's return wins** |
| exception in try, no catch | finally runs, then exception propagates (no return) |

```java
public int getValue() {
    try {
        return 10;
    } finally {
        return 20; // BAD — method returns 20
    }
}
```

**Interview Point:**

> finally return overrides try return. Never put return in finally — confusing and bug-prone.

</details>

---

# 19. Can finally override return value?

<details>
<summary>Show Answer</summary>

**Answer:**

**Yes** — if `finally` has a `return` statement, it **replaces** the return value from `try` or `catch`.

```java
public int calculate() {
    try {
        return 100;
    } finally {
        return 200; // overrides — returns 200
    }
}
```

Without return in finally, try's value is preserved:

```java
try {
    return 100;
} finally {
    cleanup(); // no return — method still returns 100
}
```

**Interview Point:**

> finally can override return — never return from finally. Code smell and interview trap.

</details>

---

## throw vs throws

---

# 20. Difference between throw and throws?

<details>
<summary>Show Answer</summary>

**Answer:**

| | `throw` | `throws` |
|---|---------|----------|
| Purpose | **Create** and throw exception now | **Declare** exceptions method may throw |
| Used in | Method body | Method signature |
| Count | One exception per throw | Multiple exceptions allowed |

```java
// throw — actually throws
public void validate(int age) {
    if (age < 0) {
        throw new IllegalArgumentException("Invalid age");
    }
}

// throws — declares possibility
public void readFile() throws IOException {
    Files.readAllBytes(Path.of("data.txt"));
}
```

**Interview Point:**

> throw = action (throw now). throws = declaration (might throw). throw in body, throws in signature.

</details>

---

# 21. Why throws keyword used?

<details>
<summary>Show Answer</summary>

**Answer:**

`throws` tells callers **which checked exceptions** they must handle—part of Java's compile-time contract.

```java
public void connect() throws SQLException, IOException {
    // caller MUST handle or declare these
}

// Caller option 1: catch
try {
    connect();
} catch (SQLException | IOException e) {
    handle(e);
}

// Caller option 2: declare throws
public void startup() throws SQLException, IOException {
    connect();
}
```

**Interview Point:**

> throws documents checked exceptions for callers. Unchecked exceptions don't need throws declaration.

</details>

---

# 22. Multiple exceptions in throws?

<details>
<summary>Show Answer</summary>

**Answer:**

**Yes** — comma-separated list in method signature.

```java
public void process() throws IOException, SQLException, ParseException {
    readFile();
    queryDb();
    parseDate();
}
```

### Multi-catch (Java 7+)

```java
try {
    process();
} catch (IOException | SQLException e) {
    log.error("IO or DB error", e);
}
```

**Interview Point:**

> Multiple exceptions in throws with commas. Multi-catch catches several types in one block.

</details>

---

## Custom Exceptions

---

# 23. How to create custom exception?

<details>
<summary>Show Answer</summary>

**Answer:**

Extend `Exception` (checked) or `RuntimeException` (unchecked). Provide constructors with message and optional cause.

```java
// Unchecked — common in Spring REST apps
public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message) {
        super(message);
    }

    public PaymentFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Usage
throw new PaymentFailedException("Card declined", rootCause);
```

**Interview Point:**

> Extend RuntimeException for unchecked, Exception for checked. Always support message + cause constructor.

</details>

---

# 24. Checked custom exception?

<details>
<summary>Show Answer</summary>

**Answer:**

Extend `Exception` but **not** `RuntimeException`.

```java
public class DuplicateOrderException extends Exception {
    public DuplicateOrderException(String orderId) {
        super("Duplicate order: " + orderId);
    }
}

public void createOrder(Order order) throws DuplicateOrderException {
    if (exists(order.getId())) {
        throw new DuplicateOrderException(order.getId());
    }
    save(order);
}
```

Caller must catch or declare:

```java
try {
    service.createOrder(order);
} catch (DuplicateOrderException e) {
    return "Order already exists";
}
```

**Interview Point:**

> Checked custom = extends Exception (not RuntimeException). Compiler enforces handling.

</details>

---

# 25. Unchecked custom exception?

<details>
<summary>Show Answer</summary>

**Answer:**

Extend `RuntimeException` — no forced handling.

```java
public class ResourceNotFoundException extends RuntimeException {
    private final String resourceId;

    public ResourceNotFoundException(String resource, String id) {
        super(resource + " not found: " + id);
        this.resourceId = id;
    }
}

public User getUser(Long id) {
    return repo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User", id.toString()));
}
```

Spring maps to HTTP 404 via `@ExceptionHandler`.

**Interview Point:**

> Unchecked custom = extends RuntimeException. Preferred for REST APIs with global exception handler.

</details>

---

## Java 7+

---

# 26. Multi-catch block?

<details>
<summary>Show Answer</summary>

**Answer:**

Catch **multiple exception types** in one block—Java 7+. Exception parameter is **effectively final**.

```java
try {
    process();
} catch (IOException | SQLException e) {
    log.error("External failure", e);
    // same handler for both
}
```

### Rules

```text
✅ IOException | SQLException — siblings OK
❌ IOException | FileNotFoundException — subclass + parent — compile error
✅ catch parameter cannot be reassigned in multi-catch
```

**Interview Point:**

> Multi-catch reduces duplicate catch blocks. No subclass + parent in same multi-catch.

</details>

---

# 27. Try-with-resources?

<details>
<summary>Show Answer</summary>

**Answer:**

Automatically **closes resources** after try block—Java 7+. Replaces manual finally close.

```java
// Old way
InputStream in = null;
try {
    in = new FileInputStream("file.txt");
    // read
} finally {
    if (in != null) in.close();
}

// Try-with-resources
try (InputStream in = new FileInputStream("file.txt")) {
    // read — in.close() called automatically
}
```

### Multiple Resources

```java
try (Connection conn = getConnection();
     PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.executeQuery();
}
```

**Interview Point:**

> try-with-resources auto-closes. Resource must implement AutoCloseable. Closes in reverse order.

</details>

---

# 28. AutoCloseable?

<details>
<summary>Show Answer</summary>

**Answer:**

`AutoCloseable` interface with `close()` method—anything used in try-with-resources must implement it.

```java
public interface AutoCloseable {
    void close() throws Exception;
}
```

```java
public class DatabaseConnection implements AutoCloseable {
    @Override
    public void close() {
        releaseConnection();
    }
}

try (DatabaseConnection conn = pool.borrow()) {
    conn.query("SELECT ...");
} // close() called automatically
```

`Closeable` extends `AutoCloseable` — used by streams and readers.

**Interview Point:**

> AutoCloseable.close() called automatically at end of try-with-resources. Implement for custom resources.

</details>

---

# 29. Suppressed exceptions?

<details>
<summary>Show Answer</summary>

**Answer:**

When try throws A and `close()` throws B, **B is suppressed** and attached to A—you don't lose the original exception.

```java
try (MyResource r = new MyResource()) {
    throw new RuntimeException("from try");
} // close() also throws — suppressed, attached to main exception
```

```java
catch (Exception e) {
    e.printStackTrace();
    for (Throwable suppressed : e.getSuppressed()) {
        System.out.println("Suppressed: " + suppressed.getMessage());
    }
}
```

Only works with **try-with-resources** — manual try-finally can mask exceptions.

**Interview Point:**

> Suppressed exceptions preserve close() failures when try also failed. getSuppressed() to inspect.

</details>

---

## Production Questions

---

# 30. Global exception handling?

<details>
<summary>Show Answer</summary>

**Answer:**

**Global exception handling** catches exceptions at application boundary—one place maps errors to HTTP status, logs, and user-friendly messages.

### Without Global Handler

```text
Every controller has try-catch → duplicate code, inconsistent responses
```

### Spring — @ControllerAdvice

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404)
            .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(400)
            .body(new ErrorResponse("BAD_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(500)
            .body(new ErrorResponse("INTERNAL_ERROR", "Something went wrong"));
    }
}
```

### Standard Error Response

```java
public record ErrorResponse(String code, String message, Instant timestamp) {
    public ErrorResponse(String code, String message) {
        this(code, message, Instant.now());
    }
}
```

**Interview Point:**

> @ControllerAdvice + @ExceptionHandler for centralized REST error handling. Map business exceptions to 4xx, unknown to 500.

</details>

---

# 31. Exception handling in Spring Boot?

<details>
<summary>Show Answer</summary>

**Answer:**

Spring Boot layers exception handling:

| Layer | Mechanism |
|-------|-----------|
| Controller | `@ExceptionHandler` on controller (local) |
| Global | `@RestControllerAdvice` (app-wide) |
| Validation | `@Valid` → `MethodArgumentNotValidException` |
| Security | `AccessDeniedException` → 403 |
| Default | `ErrorController` / Whitelabel for unhandled |

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
        .map(e -> e.getField() + ": " + e.getDefaultMessage())
        .collect(Collectors.joining(", "));
    return ResponseEntity.badRequest().body(new ErrorResponse("VALIDATION_ERROR", message));
}
```

### Service Layer Pattern

```text
Service: throw business exceptions (unchecked)
Controller: no try-catch — let advice handle
Don't catch Exception and return null — swallowing
```

**Interview Point:**

> Services throw domain exceptions. @RestControllerAdvice maps to HTTP. Handle validation errors separately. Never expose stack trace to client.

</details>

---

# 32. How do you log exceptions?

<details>
<summary>Show Answer</summary>

**Answer:**

Log exceptions with **full stack trace** at the point you handle or cannot recover—include context (userId, orderId, correlationId).

### Good Logging

```java
try {
    processPayment(order);
} catch (PaymentException e) {
    log.error("Payment failed for orderId={}, userId={}", order.getId(), order.getUserId(), e);
    throw e; // or wrap and rethrow
}
```

### Rules

| Do | Don't |
|----|-------|
| `log.error("msg", e)` — pass exception as last arg | `log.error(e.getMessage())` — no stack trace |
| Include business context IDs | Log same exception at every layer |
| Use MDC for traceId | `printStackTrace()` in production |
| Log once at boundary | Swallow without log |

### SLF4J Pattern

```java
private static final Logger log = LoggerFactory.getLogger(OrderService.class);

log.warn("Retry attempt {} for order {}", attempt, orderId);
log.error("Order processing failed", exception);
```

**Interview Point:**

> log.error(message, exception) for stack trace. Add context IDs. Log once at handler boundary. MDC for correlation.

</details>

---

# 33. Why should exceptions not be swallowed?

<details>
<summary>Show Answer</summary>

**Answer:**

**Swallowing** = catching exception and doing nothing (or only logging weakly). Hides bugs, loses data, makes production debugging impossible.

### Bad — Swallowed

```java
try {
    saveToDatabase(order);
} catch (Exception e) {
    // empty — SILENT FAILURE ❌
}

try {
    sendEmail(user);
} catch (Exception e) {
    e.printStackTrace(); // logged nowhere useful ❌
}
```

### Problems

```text
Order not saved — user thinks success
No alert — team discovers days later
Stack trace lost — cannot debug
Tests pass — failure hidden
```

### Good Patterns

```java
// Rethrow
catch (SQLException e) {
    throw new DataAccessException("Save failed", e);
}

// Handle meaningfully
catch (IOException e) {
    log.error("Config load failed", e);
    throw new IllegalStateException("Cannot start without config", e);
}

// Recover if truly recoverable
catch (TimeoutException e) {
    return retryWithBackoff();
}
```

**Interview Point:**

> Never empty catch blocks. Rethrow, wrap, or recover with clear intent. Swallowing = silent production bugs.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Can we catch Error?

<details>
<summary>Show Answer</summary>

**Answer:**

Technically yes, but **don't** in normal code. Fix OOM/StackOverflow at source (memory, recursion). Catching Error masks fatal problems.

</details>

---

### Q: Exception vs Exception in catch order?

<details>
<summary>Show Answer</summary>

**Answer:**

Catch **subclass before superclass**. `catch (FileNotFoundException)` before `catch (IOException)`. Compiler error if parent comes first.

</details>

---

### Q: Is finally needed with try-with-resources?

<details>
<summary>Show Answer</summary>

**Answer:**

**No** — close happens automatically. You can add finally for other cleanup, but resource close is handled by try-with-resources.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> Error = don't catch. Checked = handle/throws. Unchecked = RuntimeException. try-finally runs cleanup. Never return from finally. throw vs throws. try-with-resources + suppressed exceptions. @ControllerAdvice for global handling. Never swallow exceptions.

</details>
