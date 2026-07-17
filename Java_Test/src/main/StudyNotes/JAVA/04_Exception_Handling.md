# Easy Level

### 1. What is an Exception in Java?

An exception is an event that disrupts the normal flow of program execution.

### 2. What is the difference between Error and Exception?

| Error            | Exception           |
| ---------------- | ------------------- |
| Unrecoverable    | Recoverable         |
| JVM related      | Application related |
| OutOfMemoryError | IOException         |

### 3. What is Exception Handling?

Mechanism to handle runtime errors using:

* try
* catch
* finally
* throw
* throws

### 4. Difference between Checked and Unchecked Exceptions?

**Checked Exception**

* Checked at compile time
* Must be handled

Examples:

```java
IOException
SQLException
ClassNotFoundException
```

**Unchecked Exception**

* Occurs at runtime
* Extends RuntimeException

Examples:

```java
NullPointerException
ArithmeticException
ArrayIndexOutOfBoundsException
```

---

### 5. Can we have multiple catch blocks?

Yes.

```java
try {
    int x = 10 / 0;
}
catch (ArithmeticException e) {
}
catch (Exception e) {
}
```

---

### 6. What happens if no exception occurs in try block?

Catch blocks are skipped.
Finally block executes.

---

### 7. Can we write try without catch?

Yes.

```java
try {
    // code
}
finally {
}
```

---

### 8. Can finally block be skipped?

Yes.

Examples:

```java
System.exit(0);
```

JVM crash.

---

### 9. Difference between throw and throws?

| throw                              | throws             |
| ---------------------------------- | ------------------ |
| Used to explicitly throw exception | Declares exception |
| Inside method body                 | Method signature   |

```java
throw new RuntimeException();
```

```java
public void test() throws IOException
```

---

### 10. Can we catch RuntimeException?

Yes.

```java
try {
}
catch(RuntimeException e) {
}
```

---

# Intermediate Level

### 11. What is Exception Propagation?

Exception moves up the call stack until handled.

```java
m1()
  -> m2()
      -> m3()
```

If m3 throws exception and doesn't handle it, it propagates to m2 then m1.

---

### 12. What is Stack Trace?

Sequence of method calls that led to exception.

```java
e.printStackTrace();
```

---

### 13. Why should specific catch come before generic catch?

Correct:

```java
catch(IOException e)
catch(Exception e)
```

Wrong:

```java
catch(Exception e)
catch(IOException e)
```

Compiler error.

---

### 14. Difference between Final, Finally and Finalize?

| Keyword    | Purpose              |
| ---------- | -------------------- |
| final      | Prevent modification |
| finally    | Cleanup block        |
| finalize() | GC hook (deprecated) |

---

### 15. What is Multi-Catch?

```java
catch(IOException | SQLException e)
{
}
```

Introduced in Java 7.

---

### 16. What is Try-With-Resources?

Automatically closes resources.

```java
try(BufferedReader br =
        new BufferedReader(new FileReader("a.txt")))
{
}
```

---

### 17. Which executes first:

return or finally?

```java
try {
   return 10;
}
finally {
   System.out.println("finally");
}
```

Output:

```java
finally
```

Then method returns 10.

---

### 18. What happens when exception occurs in finally?

It overrides previous exception.

```java
try {
   throw new Exception("A");
}
finally {
   throw new RuntimeException("B");
}
```

"B" is seen.

---

### 19. Can constructors throw exceptions?

Yes.

```java
public Student() throws IOException
{
}
```

---

### 20. What is Custom Exception?

```java
public class InvalidAgeException
        extends Exception {

    public InvalidAgeException(String msg) {
        super(msg);
    }
}
```

---

# Hard Level (5+ Years)

### 21. Why are checked exceptions criticized?

Problems:

* Boilerplate code
* Tight coupling
* Exception leakage across layers

Many modern frameworks prefer RuntimeException.

---

### 22. Explain exception chaining.

```java
catch(SQLException e) {
    throw new ServiceException("DB Error", e);
}
```

Original cause preserved.

```java
e.getCause();
```

---

### 23. What is suppressed exception?

Java 7 feature.

```java
try(Resource r = new Resource())
{
}
```

If both:

* try throws exception
* close() throws exception

close exception becomes suppressed.

```java
e.getSuppressed();
```

---

### 24. Why should we not catch Exception?

Bad practice:

```java
catch(Exception e)
```

Because:

* Hides actual problems
* Difficult debugging
* May swallow programming bugs

---

### 25. Why should we not use exceptions for control flow?

Bad:

```java
try {
   int value = list.get(i);
}
catch(IndexOutOfBoundsException e) {
}
```

Exception creation is expensive.

---

### 26. Difference between:

```java
throw e;
```

and

```java
throw new Exception(e);
```

**throw e**

* Preserves original exception

**throw new Exception(e)**

* Wraps exception

---

### 27. Explain exception hierarchy.

```text
Throwable
├── Error
│   ├── OutOfMemoryError
│   └── StackOverflowError
│
└── Exception
    ├── RuntimeException
    │   ├── NullPointerException
    │   ├── ArithmeticException
    │   └── IllegalArgumentException
    │
    └── Checked Exceptions
        ├── IOException
        ├── SQLException
        └── ClassNotFoundException
```

---

### 28. Why is NullPointerException so common?

Reasons:

* Uninitialized objects
* Returning null
* Missing validations

Solutions:

* Optional
* Objects.requireNonNull()
* Defensive coding

---

### 29. What is best practice for exception logging?

Bad:

```java
catch(Exception e){
   System.out.println(e);
}
```

Good:

```java
logger.error("Order processing failed", e);
```

Always log stack trace.

---

### 30. Why should we avoid empty catch blocks?

Bad:

```java
catch(Exception e){
}
```

Exception is silently swallowed.

---

# Advanced Level

### 31. Explain try-with-resources internals.

Compiler converts:

```java
try(Resource r = new Resource())
{
}
```

Into roughly:

```java
Resource r = new Resource();
try {
}
finally {
   r.close();
}
```

With suppressed exception handling.

---

### 32. What happens if both catch and finally have return statements?

```java
try {
    return 1;
}
finally {
    return 2;
}
```

Output:

```java
2
```

Finally overrides.

---

### 33. Can we create generic exception classes?

No.

Invalid:

```java
class MyException<T> extends Exception {
}
```

Compiler error.

---

### 34. Explain exception handling in Streams.

```java
list.stream()
    .map(x -> {
        try {
            return convert(x);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    });
```

Checked exceptions aren't directly supported.

---

### 35. Exception handling in CompletableFuture?

```java
future.exceptionally(ex -> {
    return "default";
});
```

or

```java
future.handle((result, ex) -> {
});
```

---

### 36. Difference between Future.get() and CompletableFuture.join()?

| get()                     | join()                               |
| ------------------------- | ------------------------------------ |
| Throws checked exceptions | Throws unchecked CompletionException |
| Must handle               | No mandatory handling                |

---

### 37. What is CompletionException?

Wrapper exception used by CompletableFuture.

```java
CompletionException
    -> Actual Exception
```

---

### 38. Can JVM recover from OutOfMemoryError?

Generally No.

Possible actions:

* Log
* Graceful shutdown

Application state may be inconsistent.

---

### 39. What is the best exception strategy in Spring Boot?

Controller Layer:

```java
@RestControllerAdvice
```

Service Layer:

```java
throw BusinessException(...)
```

Repository Layer:

```java
DataAccessException
```

Global handling preferred.

---

### 40. Design a production-grade exception framework.

Common approach:

```java
AppException
   |
   +-- ValidationException
   +-- BusinessException
   +-- ResourceNotFoundException
   +-- UnauthorizedException
```

Global Handler:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
}
```

Error Response:

```java
{
  "timestamp":"2026-07-17",
  "code":"USER_404",
  "message":"User not found"
}
```

This type of exception architecture is a very common **Spring Boot 5+ years interview topic** and is frequently asked in service-based/backend developer interviews.
