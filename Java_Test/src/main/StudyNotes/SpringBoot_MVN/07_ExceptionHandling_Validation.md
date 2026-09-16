# 07. Exception Handling and Bean Validation

## Start here (simple English)

**In one sentence:** If the client sends bad data, return **400** with a **clear JSON error**. If something unexpected blows up, return **500**, **log the real stack**, and **never** dump the stack to the client.

**Everyday picture:** A help desk.

- “You forgot the email field” → polite, specific, 400
- “The printer caught fire” → log it for engineering, tell the user “something went wrong”, 500

**Validation** = check the input **before** business logic:

```java
public record CreateOrderRequest(
    @NotBlank String sku,
    @Min(1) int quantity
) {}

@PostMapping
public OrderResponse create(@Valid @RequestBody CreateOrderRequest req) { ... }
```

`@Valid` on the controller parameter makes Spring run those rules. Fail → `MethodArgumentNotValidException` → you turn it into JSON in one **global** class:

```java
@RestControllerAdvice
public class ApiExceptionHandler { ... }
```

One advice class for the whole API is better than `try/catch` in every controller.

**Limits a fresher misses:** `@ControllerAdvice` **cannot** see errors that happen in a **Security filter** (JWT parse). Those are outside MVC.

Interview Q&A is **5–8 year standard**.

---

APIs fail. You are judged on **consistent error bodies**, **correct status codes**, and **knowing what `@ControllerAdvice` cannot see**.

---

## 1. Goals

- Clients always get the same JSON shape
- 4xx vs 5xx is honest
- Stack traces never leave the process
- Validation errors list **fields**, not a single string
- Unexpected exceptions are logged with correlation id (MDC)

RFC 7807 **Problem Details**:

```json
{
  "type": "https://api.acme.com/errors/validation",
  "title": "Constraint Violation",
  "status": 400,
  "detail": "Request body is invalid",
  "instance": "/v1/orders",
  "errors": [
    { "field": "email", "message": "must be a well-formed email address" }
  ]
}
```

Spring 6 / Boot 3: `ProblemDetail` + `ErrorResponseException`. Use them instead of a random `Map`.

---

## 2. `@Valid` vs `@Validated`

Jakarta Bean Validation (Hibernate Validator is the impl):

```java
public record CreateOrderRequest(
    @NotBlank String sku,
    @Min(1) int quantity,
    @Valid Customer customer
) {}
```

`@Valid` on a nested object **cascades**.

Controller:

```java
@PostMapping
public OrderResponse create(@Valid @RequestBody CreateOrderRequest req) { ... }
```

Failed body validation → `MethodArgumentNotValidException` → you map it to 400.

**`@Validated` (Spring):**

- Supports **groups**: `@Validated(OnCreate.class)`
- On a **class**, enables **method-parameter validation** (AOP interceptor)

```java
@Service
@Validated
public class OrderService {
    public void create(@Valid CreateOrderRequest req) { ... }
}
```

Without `@Validated` on the class, `@Valid` on a service parameter is **ignored**.

`@Valid` on a `@RequestParam` / `@PathVariable` also needs `@Validated` on the controller class.

**Groups:**

```java
public interface OnCreate {}
public interface OnUpdate {}

@NotNull(groups = OnCreate.class)
@Null(groups = OnUpdate.class)
UUID id;
```

---

## 3. Constraint annotations

`@NotNull` `@NotEmpty` `@NotBlank` `@Size` `@Min` `@Max` `@Email` `@Pattern` `@Positive` `@Past` `@Future` `@AssertTrue`.

`@NotNull` vs `@NotBlank`: blank string passes `@NotNull`. For names/emails use `@NotBlank`.

Custom:

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SkuValidator.class)
public @interface ValidSku {
    String message() default "invalid sku";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

---

## 4. Handler stack

| Mechanism | Scope |
|-----------|--------|
| `@ExceptionHandler` on a `@Controller` | That controller only |
| `@ControllerAdvice` / `@RestControllerAdvice` | Selected controllers (packages / assignableTypes) |
| `HandlerExceptionResolver` | MVC infrastructure |
| `ErrorController` (`/error`) | Fallback for unhandled / filter / 404 depending on config |
| Filter / Security entry point | Outside MVC |

`@RestControllerAdvice` = `@ControllerAdvice` + `@ResponseBody`.

```java
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request");
        pd.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> Map.of("field", fe.getField(), "message", fe.getDefaultMessage()))
            .toList());
        return pd;
    }

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail notFound(NotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail other(Exception ex) {
        // log with MDC; generic message to client
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
    }
}
```

**Order:** more specific handlers beat generic. Multiple advice classes: `@Order` / `Ordered`. Lowest value = highest precedence. Do **not** put `Exception.class` in a `@Order(1)` advice that shadows everything.

**Business exceptions:** a small hierarchy (`NotFoundException`, `ConflictException`) mapped to statuses. Don’t use `@ResponseStatus` on every exception as the only strategy — advice is easier to evolve — but `@ResponseStatus` on `NotFoundException` works for simple apps.

---

## 5. What advice cannot see

```text
Exception in Filter (JWT parse)     → not @ControllerAdvice
Exception in Tomcat before servlet  → container page / Boot ErrorController
AccessDeniedException               → Security AccessDeniedHandler (unless you
                                      rethrow into MVC, which you usually don't)
```

Boot’s `BasicErrorController` serves `/error`. For REST, set:

```yaml
server:
  error:
    include-message: never
    include-stacktrace: never
    include-binding-errors: never
```

Then prefer advice so `/error` is a last resort.

404: by default Spring Boot may **not** throw `NoHandlerFoundException`. To handle unknown URLs in advice:

```yaml
spring:
  mvc:
    throw-exception-if-no-handler-found: true
  web:
    resources:
      add-mappings: false
```

(Only if you don’t serve static resources from the same app.)

---

## 6. Validation of configuration

```java
@Validated
@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(@NotBlank String host, @Min(1) int port) {}
```

Fails **startup** — the right time for “SMTP host missing”.

---

## 7. Production pitfalls

1. Catching `Exception` in the controller and returning 200.
2. `ex.getMessage()` to clients — leaks SQL / path names.
3. One `@ExceptionHandler(Exception.class)` that also eats `AccessDeniedException` and turns 403 into 500.
4. `@Valid` on service without `@Validated` on the class — silent skip.
5. Primitive `@RequestParam int page` missing → 500 (`MissingServletRequestParameterException` vs type mismatch). Use `Integer` + required false + default.
6. Hibernate `ConstraintViolationException` (DB unique) bubbling as 500 instead of 409.

---

# Interview Q&A (5–8 year bar)

A fresher knows `@ExceptionHandler`. A 5–8 year answer splits `@Valid` vs `@Validated`, filter vs MVC, and `ProblemDetail`.

### Q1. How do you handle exceptions globally in Boot?

**Answer:** `@RestControllerAdvice` + `@ExceptionHandler` returning `ProblemDetail` / `ResponseEntity`.

**Counter:** Does that catch filter exceptions?  
**Answer:** No. Security and filters need their own handlers.

---

### Q2. `@ControllerAdvice` vs `@RestControllerAdvice`?

**Answer:** REST advice writes the body directly (`@ResponseBody`). `@ControllerAdvice` may resolve a view unless methods are `@ResponseBody`.

---

### Q3. `@Valid` vs `@Validated`?

**Answer:** `@Valid` = spec, cascade. `@Validated` = Spring groups + method validation when placed on the class.

**Counter:** Why didn’t my service-level `@Valid` run?  
**Answer:** Class missing `@Validated`, so no method-validation proxy.

---

### Q4. What exception is thrown for invalid `@RequestBody` JSON vs invalid fields?

**Answer:** Malformed JSON → `HttpMessageNotReadableException`. Constraint fail → `MethodArgumentNotValidException`. Query/path constraints → `ConstraintViolationException` (sometimes).

**Counter:** `@RequestBody` with missing body?  
**Answer:** `HttpMessageNotReadableException` if required (default).

---

### Q5. How do you pick HTTP status for domain errors?

**Answer:** Map exception types in advice: not found 404, conflict 409, validation 400, auth 401/403. Unexpected 500 with logged cause.

---

### Q6. Multiple advice classes — who wins?

**Answer:** `@Order` — lower value first. Specific exception type still wins over `Exception` **within** a class. Across classes, order matters; a broad handler in a high-precedence advice can steal.

---

### Q7. `@ResponseStatus` on an exception class?

**Answer:** `ResponseStatusExceptionResolver` maps it. Fast for simple cases. Advice is better when you need a body.

---

### Q8. What is `ProblemDetail`?

**Answer:** Spring’s RFC 7807 type: `type`, `title`, `status`, `detail`, extensions via `setProperty`.

---

### Q9. Should the controller catch exceptions?

**Answer:** Prefer throw domain exceptions and let advice translate. Controllers stay thin.

---

### Q10. Validation groups — when?

**Answer:** Same DTO for create vs update with different constraints (`id` null on create, required on update). Don’t invent groups for every field.

---

### Q11. How do you test advice?

**Answer:** `@WebMvcTest` + `MockMvc` performing a request that triggers validation; assert status and JSON path. Or `@SpringBootTest` for full stack.

---

### Q12. `MethodArgumentNotValidException` vs `BindException`?

**Answer:** Both wrap `BindingResult`. `@RequestBody` failures are `MethodArgumentNotValidException`. Form/`@ModelAttribute` often `BindException`. Handle both or a common parent.

---

### Q13. Why 500 on a missing request param?

**Answer:** Unhandled `MissingServletRequestParameterException`. Add a handler → 400.

---

### Q14. Can `@Order` on `@ExceptionHandler` methods?

**Answer:** Ordering is primarily on **advice beans**. Method matching is by exception **specificity** (deepest cause matching rules exist — know that Spring matches the closest exception type).

---

### Q15. Hibernate Validator vs Jakarta Validation?

**Answer:** Jakarta Validation = API (`@NotNull`). Hibernate Validator = implementation on the classpath via `starter-validation`.
