# 59. Exception Handling in Spring Boot

## Frequently Asked

---

# 1. @ExceptionHandler

<details>
<summary>Show Answer</summary>

**Answer:**

`@ExceptionHandler` handles exceptions **within a single controller** (or class). It maps exception types to HTTP responses.

```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @GetMapping("/{id}")
    public OrderDto getOrder(@PathVariable Long id) {
        return orderService.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(OrderNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, ex.getMessage()));
    }
}
```

| Scope | Where it works |
|-------|----------------|
| Controller-level | Only that controller's methods |
| Global | Use `@ControllerAdvice` instead |

**Interview Point:**

> `@ExceptionHandler` on a controller = local handling. For app-wide handling, move to `@ControllerAdvice`.

</details>

---

# 2. @ControllerAdvice

<details>
<summary>Show Answer</summary>

**Answer:**

`@ControllerAdvice` provides **global exception handling** across all (or selected) controllers. It is a specialization of `@Component`.

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(404, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(ErrorResponse.of(400, message));
    }
}
```

| Annotation | Returns |
|------------|---------|
| `@ControllerAdvice` | Can return view name or `@ResponseBody` |
| `@RestControllerAdvice` | Always `@ResponseBody` (JSON) |

**Interview Point:**

> One `@ControllerAdvice` class centralizes all exception-to-HTTP mapping. Keeps controllers clean.

</details>

---

# 3. @RestControllerAdvice

<details>
<summary>Show Answer</summary>

**Answer:**

`@RestControllerAdvice` = `@ControllerAdvice` + `@ResponseBody`. Every handler method return value is serialized directly to JSON/XML.

```java
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(new ApiError(ex.getStatus().value(), ex.getMessage(), Instant.now()));
    }
}
```

| | @ControllerAdvice | @RestControllerAdvice |
|---|-------------------|----------------------|
| Return type | View or body (needs @ResponseBody) | Always response body |
| Use case | MVC with Thymeleaf | REST APIs |

**Interview Point:**

> Use `@RestControllerAdvice` for REST APIs. No need for `@ResponseBody` on each handler.

</details>

---

# 4. ResponseEntityExceptionHandler

<details>
<summary>Show Answer</summary>

**Answer:**

`ResponseEntityExceptionHandler` is a Spring base class with **pre-built handlers** for standard Spring MVC exceptions (validation, binding, missing params, etc.).

```java
@RestControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (a, b) -> a));

        ApiError error = new ApiError(400, "Validation failed", fieldErrors);
        return ResponseEntity.badRequest().body(error);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        return ResponseEntity.badRequest()
                .body(new ApiError(400, "Missing parameter: " + ex.getParameterName()));
    }
}
```

| Built-in handlers | Exception |
|-------------------|-----------|
| Validation | `MethodArgumentNotValidException` |
| Type mismatch | `MethodArgumentTypeMismatchException` |
| Missing param | `MissingServletRequestParameterException` |
| No handler | `NoHandlerFoundException` |

**Interview Point:**

> Extend `ResponseEntityExceptionHandler` to override default Spring MVC error responses with your API error format.

</details>

---

## Advanced

---

# 5. Global Exception Handling?

<details>
<summary>Show Answer</summary>

**Answer:**

**Global exception handling** catches exceptions from any controller in one place and returns a **consistent error response**.

### Architecture

```text
Controller throws exception
        ↓
@ControllerAdvice catches it
        ↓
Maps to HTTP status + JSON error body
        ↓
Client receives uniform error format
```

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> notFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> dbError(DataAccessException ex) {
        log.error("Database error", ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Database operation failed");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> fallback(Exception ex) {
        log.error("Unhandled exception", ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred");
    }

    private ResponseEntity<ApiError> buildError(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ApiError(status.value(), message, Instant.now(), MDC.get("traceId")));
    }
}
```

| Layer | Exception Type | HTTP Status |
|-------|---------------|-------------|
| Business | `OrderNotFoundException` | 404 |
| Validation | `MethodArgumentNotValidException` | 400 |
| Security | `AccessDeniedException` | 403 |
| Infrastructure | `DataAccessException` | 500 |

**Interview Point:**

> Order handlers: specific exceptions first, `Exception.class` last as catch-all. Never expose stack traces to clients.

</details>

---

# 6. Standard error response design?

<details>
<summary>Show Answer</summary>

**Answer:**

A **standard error response** gives clients a predictable JSON structure for every failure.

### Recommended Structure

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "timestamp": "2026-08-08T14:30:00Z",
  "path": "/api/v1/users",
  "traceId": "a1b2c3d4-e5f6-7890",
  "fieldErrors": {
    "email": "must be a valid email",
    "age": "must be greater than 0"
  }
}
```

```java
public record ApiError(
        int status,
        String error,
        String message,
        Instant timestamp,
        String path,
        String traceId,
        Map<String, String> fieldErrors
) {
    public static ApiError of(HttpStatus status, String message, WebRequest request) {
        return new ApiError(
                status.value(),
                status.getReasonPhrase(),
                message,
                Instant.now(),
                request.getDescription(false).replace("uri=", ""),
                MDC.get("traceId"),
                null
        );
    }
}
```

| Field | Purpose |
|-------|---------|
| `status` | HTTP status code |
| `message` | Human-readable description |
| `timestamp` | When error occurred |
| `traceId` | Correlate with server logs |
| `fieldErrors` | Per-field validation details |

### RFC 7807 (Problem Details)

```json
{
  "type": "https://api.example.com/errors/insufficient-funds",
  "title": "Insufficient Funds",
  "status": 422,
  "detail": "Account balance is 50, requested transfer is 100",
  "instance": "/api/v1/transfers/tx-991"
}
```

```java
// Spring 6+ built-in support
@ExceptionHandler(InsufficientFundsException.class)
public ProblemDetail handleInsufficientFunds(InsufficientFundsException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    problem.setTitle("Insufficient Funds");
    problem.setType(URI.create("https://api.example.com/errors/insufficient-funds"));
    return problem;
}
```

**Interview Point:**

> Consistent error format across all endpoints. Include traceId for production debugging. RFC 7807 is the industry standard for problem details.

</details>

---

# 7. Custom exception hierarchy?

<details>
<summary>Show Answer</summary>

**Answer:**

Design a **custom exception hierarchy** to separate business errors from infrastructure failures and map each to the right HTTP status.

### Hierarchy Design

```text
RuntimeException
 └── ApiException (base — carries HttpStatus)
      ├── ResourceNotFoundException     → 404
      ├── ValidationException           → 400
      ├── BusinessRuleException         → 422
      ├── ConflictException             → 409
      └── UnauthorizedException         → 401
```

```java
public abstract class ApiException extends RuntimeException {
    private final HttpStatus status;

    protected ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}

public class OrderNotFoundException extends ApiException {
    public OrderNotFoundException(Long orderId) {
        super(HttpStatus.NOT_FOUND, "Order not found: " + orderId);
    }
}

public class InsufficientStockException extends ApiException {
    public InsufficientStockException(String productId) {
        super(HttpStatus.UNPROCESSABLE_ENTITY,
                "Insufficient stock for product: " + productId);
    }
}
```

```java
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException ex, WebRequest request) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiError.of(ex.getStatus(), ex.getMessage(), request));
    }
}
```

| Exception | HTTP | When |
|-----------|------|------|
| `ResourceNotFoundException` | 404 | Entity not in DB |
| `ValidationException` | 400 | Business validation failed |
| `ConflictException` | 409 | Duplicate / version conflict |
| `BusinessRuleException` | 422 | Valid input but rule violated |

### Production Tips

```java
// Service layer — throw domain exceptions, not HTTP concepts
public Order getOrder(Long id) {
    return orderRepo.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));
}

// Don't catch and swallow
try {
    paymentGateway.charge(amount);
} catch (PaymentException e) {
    throw new PaymentFailedException("Charge failed for order " + orderId, e);
}
```

**Interview Point:**

> Business exceptions in service layer. `@ControllerAdvice` maps them to HTTP. Never put `ResponseEntity` logic in services.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: @ExceptionHandler vs @ControllerAdvice?

<details>
<summary>Show Answer</summary>

**Answer:**

`@ExceptionHandler` handles exceptions in **one controller**. `@ControllerAdvice` applies handlers **globally** across controllers.

</details>

---

### Q: @ControllerAdvice vs @RestControllerAdvice?

<details>
<summary>Show Answer</summary>

**Answer:**

`@RestControllerAdvice` adds `@ResponseBody` to all handler methods — designed for REST APIs returning JSON.

</details>

---

### Q: Why extend ResponseEntityExceptionHandler?

<details>
<summary>Show Answer</summary>

**Answer:**

It provides default handlers for Spring MVC exceptions (validation, type mismatch, missing params). Override methods to customize the error response format.

</details>

---

### Q: Should you expose stack traces in API errors?

<details>
<summary>Show Answer</summary>

**Answer:**

**No** in production. Log full stack trace server-side. Return generic message + traceId to the client.

</details>

---

### Q: Where should exceptions be thrown — controller or service?

<details>
<summary>Show Answer</summary>

**Answer:**

**Service layer** throws domain/business exceptions. Controller (or `@ControllerAdvice`) maps them to HTTP responses.

</details>

---

### Q: What is RFC 7807?

<details>
<summary>Show Answer</summary>

**Answer:**

Standard format for HTTP API problem details (`type`, `title`, `status`, `detail`, `instance`). Spring 6+ supports `ProblemDetail` class natively.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> `@ExceptionHandler` = local. `@ControllerAdvice` / `@RestControllerAdvice` = global. Extend `ResponseEntityExceptionHandler` for Spring MVC defaults. Custom exception hierarchy with HttpStatus. Consistent JSON error body with status, message, timestamp, traceId. RFC 7807 ProblemDetail. Throw in service, map in advice. Never expose stack traces.

</details>
