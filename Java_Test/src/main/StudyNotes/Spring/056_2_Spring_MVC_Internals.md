# 56.2 Spring MVC Internals

[← 056_1 Events, SpEL, Resources](056_1_Events_SpEL_Resources.md) | [Course map](00_COURSE_MAP.md) | **Next:** [056_3 JDBC and Transactions →](056_3_Jdbc_and_Transaction_Abstraction.md)

REST *design* (status codes, versioning) lives in [058_REST_API.md](058_REST_API.md). This chapter is **what happens when a URL is hit**.

Teaching is simple first. **Interview Q&A at the end is 5–8 year standard.**

---

## Simple first

A browser (or Postman) sends: `GET /orders/5`.

1. The **servlet container** (Tomcat) receives bytes.
2. **Filters** can look at *every* request (even ones Spring does not know). Like the building security desk.
3. **DispatcherServlet** is Spring’s **front desk**. It does not do business logic. It only routes.
4. It asks: “Which method handles this URL?” → **HandlerMapping**
5. **Interceptors** run (Spring-only, they know *which* controller). Like a receptionist who already knows your appointment.
6. The adapter **calls your method**, filling `@PathVariable`, `@RequestBody`, etc.
7. Your method returns an object. Jackson turns it into **JSON** (`HttpMessageConverter`).
8. If something throws, **`@ControllerAdvice`** turns it into an error JSON.

```text
Filter (building gate)
  → DispatcherServlet (front desk)
      → find the method
      → interceptors
      → OrderController.get(5)
      → JSON out
```

**`@Valid`:** without it, Spring still reads JSON into an object but **does not** run `@NotNull` checks. Always put `@Valid` on `@RequestBody` when you have constraints.

**Filter vs interceptor vs advice (remember this):**

| | Sees 404 with no controller? | Typical job |
|--|------------------------------|-------------|
| Filter | Yes (if mapped) | Auth, CORS, wrap body |
| Interceptor | Only if a Spring handler exists | Log handler name, timing |
| `@ControllerAdvice` | After a controller threw | Exception → JSON |

---

## When you interview (5–8 years)

A senior can draw `doDispatch` without Boot: mapping → interceptors → adapter → converters → exception resolvers. They know why Security CORS is a filter, not an interceptor.

---

## 1. How the servlet gets there (Core, not Boot)

Framework way:

- `web.xml` or `WebApplicationInitializer` (`AbstractAnnotationConfigDispatcherServletInitializer`)
- Root context = services; child servlet context = controllers
- Map `/` or `/api/*` to `DispatcherServlet`

Boot way (for contrast only): embedded Tomcat + `DispatcherServletAutoConfiguration` maps `/`. You still have a `DispatcherServlet` bean.

`@EnableWebMvc` imports `DelegatingWebMvcConfiguration` — registers `RequestMappingHandlerMapping`, `RequestMappingHandlerAdapter`, exception resolvers, converters. Without it (or Boot’s `WebMvcAutoConfiguration`), annotations do nothing.

`WebMvcConfigurer` is the hook to **add** formatters, interceptors, CORS, converters without replacing the whole config. Implementing `WebMvcConfigurationSupport` yourself **turns off** Boot defaults — a classic footgun (leave that story for Boot; in Core, prefer `@EnableWebMvc` + `WebMvcConfigurer`).

---

## 2. DispatcherServlet `doDispatch` (the pipeline)

**Simple:** this is the front desk’s checklist for **one** request. Same as the picture at the top, with official names.

```text
1. Find HandlerExecutionChain
     HandlerMapping.getHandler(request)
     = handler (controller method) + HandlerInterceptor[]

2. Get HandlerAdapter that supports the handler
     RequestMappingHandlerAdapter for @RequestMapping

3. Apply interceptors preHandle
     if any returns false → stop (afterCompletion still runs for started ones)

4. ha.handle(request, response, handler)
     resolve arguments
     invoke controller method
     handle return value

5. interceptors postHandle  (if no exception)

6. render view if a ModelAndView remains
     (REST with @ResponseBody usually has no view)

7. interceptors afterCompletion  (always, including errors)
```

If the controller throws, `processDispatchResult` / `processHandlerException` runs **`HandlerExceptionResolver`s** (`@ExceptionHandler`, `@ControllerAdvice`, `ResponseEntityExceptionHandler`, default resolvers).

No handler → `NoHandlerFoundException` (if configured) or servlet 404.

---

## 3. HandlerMapping

| Implementation | Maps |
|----------------|------|
| `RequestMappingHandlerMapping` | `@RequestMapping` / `@GetMapping` on `@Controller` |
| `BeanNameUrlHandlerMapping` | Bean name `/foo` → `Controller` bean (legacy) |
| `RouterFunctionMapping` | WebMvc.fn functional endpoints |
| `SimpleUrlHandlerMapping` | Programmatic path → handler |

`RequestMappingHandlerMapping` builds `RequestMappingInfo` from:

- Path (`/` + class-level + method-level)
- HTTP method
- Params, headers, consumes, produces

**Matching:** most specific wins (longer path, more constraints). Ambiguous mappings → **startup failure** (`IllegalStateException: Ambiguous mapping`). Fail-fast is good.

Path patterns: Spring 5.3+ default `PathPattern` (`/orders/{id}`) instead of AntPathMatcher. Subtle differences with trailing slash and `**`.

---

## 4. HandlerAdapter and argument resolvers

`RequestMappingHandlerAdapter` does the real invocation.

**Argument resolvers** (`HandlerMethodArgumentResolver`) fill parameters:

| Annotation / type | Source |
|-------------------|--------|
| `@PathVariable` | URI template |
| `@RequestParam` | Query / form |
| `@RequestHeader` | Header |
| `@CookieValue` | Cookie |
| `@RequestBody` | Message converter on body |
| `@ModelAttribute` | Bind params onto an object (form) |
| `@RequestPart` | Multipart |
| `HttpServletRequest` / `Principal` / `Locale` | Native |
| `@AuthenticationPrincipal` | Security (later) |
| `Optional<T>` | Missing → empty |

**Return value handlers** (`HandlerMethodReturnValueHandler`):

| Return | What happens |
|--------|----------------|
| `@ResponseBody` / `@RestController` | `HttpMessageConverter` → bytes |
| `ResponseEntity<T>` | Status + headers + converters |
| `String` from `@Controller` | View name |
| `ModelAndView` | View + model |
| `void` + `HttpServletResponse` | You wrote the response |
| `CompletableFuture` / `SseEmitter` | Async MVC |

`@RestController` = `@Controller` + `@ResponseBody` on the type, so every method uses converters unless you opt out.

---

## 5. HttpMessageConverter

Converters turn HTTP body ↔ Java.

Typical Boot/MVC stack (order matters — first that `canWrite`/`canRead` wins):

| Converter | Type |
|-----------|------|
| `ByteArrayHttpMessageConverter` | `byte[]` |
| `StringHttpMessageConverter` | `String` |
| `MappingJackson2HttpMessageConverter` | JSON (`application/json`) |
| `MappingJackson2XmlHttpMessageConverter` | XML if jackson-xml present |
| `FormHttpMessageConverter` | `application/x-www-form-urlencoded` |
| `ResourceHttpMessageConverter` | files |

`consumes = "application/json"` + `@RequestBody` → Jackson read. Wrong `Content-Type` → 415. Cannot produce `Accept` → 406.

Custom converter: `WebMvcConfigurer.configureMessageConverters` **replaces** defaults if you are not careful. Prefer `extendMessageConverters` to **add**.

`@JsonIgnore` / Jackson modules are converter configuration, not DispatcherServlet.

---

## 6. Filters vs interceptors vs `@ControllerAdvice`

| | Servlet `Filter` | `HandlerInterceptor` | `@ControllerAdvice` |
|--|------------------|----------------------|---------------------|
| API | Servlet spec | Spring MVC | Spring MVC |
| Sees | Every request mapped to the servlet (and you can map `/*`) | Only requests that **found a Spring handler** | After controller throw / selected handlers |
| Can wrap streams | Yes (`ContentCachingRequestWrapper`) | Awkward | No |
| CORS preflight | Often here | Possible | No |
| Spring injection | Need `@Bean` + `FilterRegistrationBean` (Boot) or `DelegatingFilterProxy` | Yes, it is a bean | Yes |
| `afterCompletion` | `finally` in `doFilter` | Yes | N/A |

**Rule:**

- **Filter:** authentication gate that must run even for 404 static paths, wrapping body, CORS at container level, `OncePerRequestFilter`
- **Interceptor:** logging handler name, tenant from already-authenticated principal, `StopWatch` around controller, `LocaleChangeInterceptor`
- **`@ControllerAdvice`:** exception → JSON, `@InitBinder`, `@ModelAttribute` for all controllers

`DelegatingFilterProxy` is how Spring Security’s filter bean is hooked into the servlet container. Name it in interviews even though Security is a later course.

Interceptor contract:

```java
public class TraceInterceptor implements HandlerInterceptor {
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        return true; // false → skip controller
    }
    public void postHandle(..., ModelAndView mav) { }          // view not rendered yet
    public void afterCompletion(..., Exception ex) { }         // cleanup ThreadLocal
}
```

REST `@ResponseBody`: `postHandle` still runs; `ModelAndView` may be null. Cleanup belongs in `afterCompletion`.

---

## 7. Exception handling (MVC layer)

Order of resolvers (typical):

1. `ExceptionHandlerExceptionResolver` — `@ExceptionHandler` on the controller, then `@ControllerAdvice`
2. `ResponseStatusExceptionResolver` — `@ResponseStatus` on the exception class
3. `DefaultHandlerExceptionResolver` — Spring’s 400/405/415/503 for known MVC exceptions

`@RestControllerAdvice` = `@ControllerAdvice` + `@ResponseBody`.

Controller-local `@ExceptionHandler` **beats** advice for that type (more specific). Advice can be limited: `@ControllerAdvice(assignableTypes = ...)`, `annotations = RestController.class`, `basePackages`.

`ResponseEntityExceptionHandler` — extend in advice to reuse Spring’s handlers for `MethodArgumentNotValidException`, `HttpMessageNotReadableException`, etc., and override to your JSON shape.

RFC 7807 `ProblemDetail` is Spring 6’s built-in error body (`ErrorResponse`). Mention it; product teams often still use a custom `ErrorResponse` DTO.

Full Boot-oriented notes: [059](059_Exception_Handling_Spring_Boot.md). Here you only need the **resolver chain**.

---

## 8. Bean Validation on the web

Jakarta Validation (`@NotNull`, `@Size`, `@Valid`) is **not** Spring. Spring **triggers** it.

| What you write | What runs |
|----------------|-----------|
| `@Valid` / `@Validated` on `@RequestBody` | `RequestResponseBodyMethodProcessor` → `LocalValidatorFactoryBean` |
| `@Valid` on `@ModelAttribute` | DataBinder |
| `@Validated` on a **class** + method constraints | `MethodValidationPostProcessor` (AOP) — also for service methods |
| `@RequestParam @Min(1) int page` | Needs method validation enabled |

Failure → `MethodArgumentNotValidException` (body/form) or `ConstraintViolationException` (method validation) → 400 if you handle them.

`@Valid` vs `@Validated`: `@Validated` is Spring’s; supports **validation groups**. `@Valid` is Jakarta; cascades into nested objects.

```java
@PostMapping("/orders")
public ResponseEntity<Void> create(@Valid @RequestBody CreateOrderRequest body) {
    ...
}
```

Without `@Valid`, Jackson still deserializes; **constraints are skipped**.

`BindingResult` immediately after the `@Valid` argument lets the controller handle errors without an exception. If you omit `BindingResult` and validation fails, exception resolver runs.

---

## 9. CORS at MVC level

```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("https://app.example.com")
                .allowedMethods("GET", "POST")
                .allowCredentials(true);
    }
}
```

MVC CORS uses `CorsInterceptor` / `DefaultCorsProcessor` **inside** DispatcherServlet.

If a **filter** (Security) rejects OPTIONS before the servlet, MVC CORS never runs. That is why Security has its own CORS config. For Core interviews: “CORS can be MVC mappings or a filter; the first layer that handles OPTIONS wins.”

---

## 10. Other pieces worth naming

| Piece | Role |
|-------|------|
| `ViewResolver` | Logical name → JSP/Thymeleaf (HTML apps) |
| `LocaleResolver` | i18n locale per request |
| `ThemeResolver` | Legacy theming |
| `MultipartResolver` | File uploads (`StandardServletMultipartResolver` on Servlet 3) |
| `FlashMapManager` | Redirect attributes |
| `MvcUriComponentsBuilder` | Build links from mapping names |
| `AsyncSupport` | `WebAsyncTask`, timeout, `Callable` return |

`HiddenHttpMethodFilter` — HTML forms spoof PUT/DELETE via `_method`. REST JSON APIs do not need it.

---

## Production pitfalls

1. **Filter vs interceptor for auth** — unauthenticated 404s and static resources need a filter if they must be protected uniformly.
2. **ThreadLocal in interceptor `preHandle` without `afterCompletion`** — leak on thread pool (including after exceptions).
3. **`configureMessageConverters` wiping Jackson defaults.**
4. **No `@Valid`** — open to empty payloads.
5. **Huge `@RequestBody` with no size limit** — servlet/container max post size.
6. **Returning entities from `@RestController`** — lazy Hibernate proxies, Jackson blows up (OSIV). Keep MVC ignorant of entities — that’s JPA’s chapter, but MVC is where it explodes.
7. **Ambiguous mapping** only on a second controller added months later — startup fail in prod deploy.
8. **`@EnableWebMvc` in Boot** — kills Boot MVC auto-config. In Core it’s required; in Boot prefer `WebMvcConfigurer` only.

---

## Interview Ready Q&A (5–8 year standard)

The notes used a front desk. **Here, name HandlerMapping/Adapter, 415 vs 406, `@EnableWebMvc` in Boot, and filter exceptions bypassing `@ExceptionHandler`.**

### Q1. What is DispatcherServlet?

**Answer:** Spring MVC’s front controller servlet. Every MVC request is delegated to handler mappings, adapters, exception resolvers, and view/message conversion. It is a servlet, not a replacement for Tomcat.

**Counter:** Is there one per application?

**Counter-answer:** Usually one mapped to `/`. You can have two (e.g. `/api` and `/admin`) with two child contexts. Each has its own mappings.

---

### Q2. Walk through a GET `/orders/5` that returns JSON.

**Answer:** Filters → DispatcherServlet → `RequestMappingHandlerMapping` finds `get(id)` → interceptors `preHandle` → `RequestMappingHandlerAdapter` resolves `@PathVariable` → controller → `@ResponseBody` + Jackson converter → `postHandle` / `afterCompletion` → bytes on the response.

**Counter:** Where does Jackson get involved?

**Counter-answer:** Not in the mapping step. In the adapter’s return-value handler, `RequestResponseBodyMethodProcessor` picks `MappingJackson2HttpMessageConverter` based on `Accept` and return type.

---

### Q3. Filter vs interceptor?

**Answer:** Filter = servlet chain, can wrap streams, runs even when no Spring handler exists (depending on mapping). Interceptor = Spring, has access to the handler (`HandlerMethod`), `preHandle`/`postHandle`/`afterCompletion`.

**Counter:** Where do you put CORS?

**Counter-answer:** Simple apps: `WebMvcConfigurer.addCorsMappings`. Apps with Spring Security: Security CORS filter, because OPTIONS must pass the security chain. Interceptor is usually the wrong layer for CORS.

---

### Q4. What if `preHandle` returns false?

**Answer:** Controller is not invoked. Remaining interceptors’ `preHandle` skipped. `afterCompletion` still runs for interceptors that already started. You must write the response yourself (status/body) or the client hangs/empty.

**Counter:** Exception in `preHandle`?

**Counter-answer:** `afterCompletion` with that exception. Exception resolvers may not run the same way as controller exceptions — be careful; often the servlet error page runs.

---

### Q5. `@Controller` vs `@RestController`?

**Answer:** `@RestController` = `@Controller` + class-level `@ResponseBody`. Methods write the body via converters. `@Controller` methods typically return view names unless a method is annotated `@ResponseBody`.

**Counter:** Can a `@Controller` return JSON?

**Counter-answer:** Yes — put `@ResponseBody` on the method or return `ResponseEntity`.

---

### Q6. 415 vs 406 vs 400 vs 404 vs 405?

**Answer:** 404 no mapping. 405 method not allowed (path exists, verb wrong). 415 `Content-Type` not in `consumes` / no converter. 406 `Accept` cannot be produced. 400 bind/validation/malformed JSON.

**Counter:** Malformed JSON — 400 or 415?

**Counter-answer:** Usually 400 `HttpMessageNotReadableException`. 415 is media type, not parse error.

---

### Q7. How does `@RequestBody` get its object?

**Answer:** Argument resolver reads `InputStream` with a converter that `canRead` the type and content type. Typically Jackson `ObjectMapper.readValue`.

**Counter:** Empty body for a required `@RequestBody`?

**Counter-answer:** `HttpMessageNotReadableException` / 400. `required = false` allows null.

---

### Q8. `@Valid` vs `@Validated`?

**Answer:** `@Valid` = Jakarta, cascade to nested. `@Validated` = Spring, supports groups, used on classes for method validation.

**Counter:** You put constraints on `@RequestParam` and they never run. Why?

**Counter-answer:** Need `MethodValidationPostProcessor` / `@Validated` on the controller class. `@Valid` on a simple int param is not the body-validation path.

---

### Q9. What is `HandlerExceptionResolver`?

**Answer:** Strategy to turn an exception during handler execution into a `ModelAndView` or completed response. `@ExceptionHandler` is implemented by `ExceptionHandlerExceptionResolver`.

**Counter:** Exception in a **filter**?

**Counter-answer:** DispatcherServlet resolvers do **not** see it. Filter must catch or the container error page / Boot `ErrorController` handles it. Another reason auth failures in Security filters are not `@ExceptionHandler` unless forwarded.

---

### Q10. `@ExceptionHandler` on controller vs `@ControllerAdvice`?

**Answer:** Local handler is more specific to that controller. Advice is global (optionally narrowed by package/annotation). Both are the same resolver.

**Counter:** Two advices handle the same exception?

**Counter-answer:** Spring picks the **closest** exception type match, then looks at advice order (`@Order`). Prefer one global handler per exception type.

---

### Q11. What does `@EnableWebMvc` register?

**Answer:** The MVC infrastructure beans: handler mapping, adapter, converters, exception resolvers, view resolvers defaults. Via `DelegatingWebMvcConfiguration`.

**Counter:** Why is `@EnableWebMvc` dangerous in Boot?

**Counter-answer:** It switches off `WebMvcAutoConfiguration` (because `@ConditionalOnMissingBean` of `WebMvcConfigurationSupport`). You lose Boot’s sensible converter/message defaults unless you rebuild them. In **Core** you need `@EnableWebMvc`. In Boot, implement `WebMvcConfigurer` only.

---

### Q12. Ambiguous mapping — when is it detected?

**Answer:** At mapping registration (startup), not on first request. Two methods with the same path+method+params fail the context.

**Counter:** Same path GET vs POST?

**Counter-answer:** Fine — HTTP method is part of the key.

---

### Q13. How do interceptors get registered?

**Answer:** `WebMvcConfigurer.addInterceptors(InterceptorRegistry)`. Can `addPathPatterns` / `excludePathPatterns`.

**Counter:** Interceptor bean vs mapped interceptor?

**Counter-answer:** Just `@Component` on an interceptor does **not** always register it. You must add it to the registry (or use older XML). This is a frequent “why isn’t it called” bug.

---

### Q14. `ResponseEntity` vs `@ResponseStatus` vs `ResponseEntityExceptionHandler`?

**Answer:** `ResponseEntity` = full control per method. `@ResponseStatus` on exception = static status. `ResponseEntityExceptionHandler` = base class for advice to handle MVC exceptions with consistent bodies.

**Counter:** `@ResponseStatus` on a successful controller method?

**Counter-answer:** Allowed — sets status when the method returns normally (e.g. 201). Prefer `ResponseEntity` when status depends on logic.

---

### Q15. What is `HandlerMethod`?

**Answer:** The handler object for `@RequestMapping`: bean + method. Interceptors receive it as `handler` (sometimes wrapped). You can inspect annotations for metrics.

**Counter:** Functional `RouterFunction` — still `HandlerMethod`?

**Counter-answer:** No. Handler is a `HandlerFunction`. Adapters differ (`HandlerFunctionAdapter`).

---

### Q16. Multipart — who parses the file?

**Answer:** Servlet 3 multipart (`@MultipartConfig` / container config) + `StandardServletMultipartResolver`. `@RequestPart` / `MultipartFile` arguments.

**Counter:** JSON + file in one request?

**Counter-answer:** `multipart/form-data` with a part that is JSON (`@RequestPart("meta") Meta meta`) plus `@RequestPart("file") MultipartFile file`. Not a single `@RequestBody`.

---

### Q17. Async MVC (`Callable` / `DeferredResult`) vs `@Async`?

**Answer:** MVC async **releases the servlet thread** while the work runs on a task executor, then **dispatches** back to complete the request. `@Async` is AOP on a service method; the HTTP thread may still block if the controller waits on the future incorrectly.

**Counter:** Transaction on `@Async` from a controller?

**Counter-answer:** Different thread — new transaction if annotated. The request thread may already have left. Do not expect request-scoped beans on the worker without extra context copy.

---

### Q18. How would you add a custom `HandlerMethodArgumentResolver`?

**Answer:** `WebMvcConfigurer.addArgumentResolvers`. Implement `supportsParameter` + `resolveArgument`. Use for tenant-from-header as a typed `TenantId`.

**Counter:** Why not a filter that sets a ThreadLocal?

**Counter-answer:** ThreadLocal leaks and is invisible in method signatures. A resolver makes the dependency explicit. Filters still OK for wrapping/logging.

---

### Q19. `produces` on `@GetMapping` — what does it do?

**Answer:** Adds a constraint: mapping matches only if `Accept` is compatible. Also sets the conversion type for the converter.

**Counter:** Two methods, same path, `produces = json` vs `xml`?

**Counter-answer:** Content negotiation picks one. That is valid, not ambiguous.

---

### Q20. Why did `@ControllerAdvice` not handle my exception?

**Answer:** Thrown from a filter; thrown in a different servlet; advice package doesn’t include the controller (`assignableTypes`); exception type doesn’t match (wrapped in `UndeclaredThrowableException` / `NestedServletException`); already committed response.

**Counter:** `NestedServletException`?

**Counter-answer:** Container wrap. Unwrap in a resolver or throw from the controller so MVC sees the cause. DispatcherServlet usually unwraps nested servlet exceptions for resolvers — still check the cause chain in logs.

---

### Interview one-liner

> DispatcherServlet is the front controller: HandlerMapping → interceptors → HandlerAdapter (argument resolvers + message converters) → return handlers → exception resolvers. Filters wrap the servlet; interceptors wrap the handler; `@ControllerAdvice` maps exceptions to bodies. `@Valid` is required to run Bean Validation on `@RequestBody`. CORS and auth often belong in filters, not interceptors.
