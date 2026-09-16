# 06. Spring MVC and REST APIs

## Start here (simple English)

**In one sentence:** A **REST API** is a way for programs to talk over HTTP. The client sends a request to a URL. Your Java method runs. JSON comes back.

**REST** is a **style of design** (resources, URLs, HTTP verbs). **HTTP** is the **protocol**. **Spring MVC** is the **library** that maps URLs to methods.

**Everyday picture:** A waiter (Tomcat) takes the order, a manager (`DispatcherServlet`) decides which chef (controller method) should cook it, the kitchen (service) cooks, and the plate (JSON) goes back.

**Tiny example:**

```java
@RestController
@RequestMapping("/v1/orders")
public class OrderController {
    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable long id) {
        return orders.get(id);
    }
}
```

`GET /v1/orders/5` → `get(5)` → JSON.

**HTTP verbs in one line:** GET = read, POST = create, PUT = replace all, PATCH = change some fields, DELETE = remove.

**Status codes in one line:** 2xx success, 4xx your fault (bad request, not logged in, not found), 5xx server fault.

The rest of this chapter is the **pipeline**, converters, CORS, clients. Interview Q&A is **5–8 year standard**.

---

REST is an **architectural style**. HTTP is the **protocol**. Spring MVC is the **implementation** that maps HTTP onto Java methods via `DispatcherServlet`.

A 5–8 year round will spend more time on the **servlet pipeline**, **converters**, and **API design** than on “GET vs POST”.

---

## 1. REST in one page

- Resources identified by URIs: `/orders`, `/orders/42`
- Representations: usually JSON
- Uniform interface: HTTP methods + status codes
- **Stateless**: each request carries auth (JWT/session cookie). The server does not need the previous request’s memory to understand this one.
- Cacheable, layered (gateway, load balancer)

REST is not “JSON over HTTP”. SOAP is a protocol; REST is constraints.

---

## 2. HTTP methods and idempotency

| Method | Typical use | Safe | Idempotent |
|--------|-------------|------|------------|
| GET | Read | Yes | Yes |
| HEAD | Headers only | Yes | Yes |
| POST | Create / non-idempotent action | No | **No** |
| PUT | Replace **entire** resource | No | **Yes** |
| PATCH | Partial update | No | **Not guaranteed** |
| DELETE | Remove | No | Yes (deleting missing → 404 or 204; repeating delete should not create new work) |

**Safe** = no server-side state change. **Idempotent** = N identical requests leave the same resource state as 1 request.

**PUT vs PATCH:**

```http
PUT /users/1
{ "name": "Ana", "email": "ana@x.com" }   # full replacement; omitted fields reset

PATCH /users/1
{ "email": "new@x.com" }                 # only email changes
```

POST create: two clicks → two rows unless you send an **Idempotency-Key** (store key → resource id).

---

## 3. Status codes you should actually use

| Code | When |
|------|------|
| 200 | GET/PATCH/PUT success with body |
| 201 | POST created; `Location` header |
| 204 | Success, no body (DELETE) |
| 400 | Validation / malformed JSON |
| 401 | Not authenticated |
| 403 | Authenticated but not allowed |
| 404 | Resource id not found |
| 409 | Conflict (duplicate, version) |
| 412 | Precondition failed (If-Match / ETag) |
| 422 | Semantic validation (if you distinguish from 400) |
| 429 | Rate limit |
| 500 | Unexpected |
| 502/503 | Downstream / overloaded |

401 vs 403: **who are you** vs **you may not**. Security chapter 11.

Prefer **RFC 7807 Problem Details** for error bodies (chapter 07).

---

## 4. URI design

Good:

```text
GET    /v1/orders
POST   /v1/orders
GET    /v1/orders/{id}
POST   /v1/orders/{id}/cancel     # action-as-subresource if cancel is a process
GET    /v1/customers/{id}/orders
```

Bad: `/getOrder`, `/api/doCreate`, verbs in paths, leaking `/v1/orders?userId=` when a nested resource is clearer.

Versioning:

| Strategy | Example | Trade-off |
|----------|---------|-----------|
| URI | `/v1/orders` | Visible, easy to route |
| Header | `Accept: application/vnd.acme.v1+json` | Cleaner URLs, harder to debug |
| Query | `?version=1` | Easy to misuse |

URI versioning is the usual enterprise choice.

---

## 5. `DispatcherServlet` pipeline (memorize this)

**Simple version:** After Security filters, one Spring class named `DispatcherServlet` is the **traffic cop**. It finds the controller method, reads JSON in, calls your method, writes JSON out.

Boot registers `DispatcherServlet` mapped to `/` (by default). Every HTTP request that passes filters hits it.

```text
Request
  → Servlet Filters  (Security, CORS, encoding)     // NOT MVC
    → DispatcherServlet.doDispatch()
        1. HandlerMapping          which controller method?
        2. HandlerAdapter          how to invoke it?
        3. Interceptors preHandle
        4. Argument resolvers      @PathVariable, @RequestBody, …
        5. Controller method
        6. Return value handlers   @ResponseBody → HttpMessageConverter
        7. Interceptors postHandle / afterCompletion
        8. View resolution         (skipped for @ResponseBody)
  → Filters on the way out
```

If mapping fails → `NoHandlerFoundException` (often Boot’s default is Tomcat 404 before that; `spring.mvc.throw-exception-if-no-handler-found` + `spring.web.resources.add-mappings=false` to handle 404 in `@ControllerAdvice`).

### Filters vs interceptors vs AOP vs `@ControllerAdvice`

| | Filter | HandlerInterceptor | Controller `@Aspect` | `@ControllerAdvice` |
|--|--------|--------------------|----------------------|---------------------|
| Sees | All servlets, including static / actuator if mapped | Only requests that got a **handler** | Service/controller method calls | MVC exceptions / binding |
| Spring bean | Yes (`OncePerRequestFilter`) | Yes | Yes | Yes |
| Has `HandlerMethod` | No | Yes | n/a | After controller threw |
| Typical | Auth, CORS, MDC, gzip | Logging handler name, timing | TX, metrics on services | JSON error body |

**Rule:** authentication is a **filter**. “Which controller ran” is an **interceptor**. Business transactions are **AOP on services**. API errors are **advice**.

```java
@Component
public class MdcFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        MDC.put("corrId", Optional.ofNullable(req.getHeader("X-Correlation-Id"))
                .orElse(UUID.randomUUID().toString()));
        try { chain.doFilter(req, res); }
        finally { MDC.remove("corrId"); }
    }
}
```

```java
@Component
public class TimingInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        req.setAttribute("t0", System.nanoTime());
        return true;
    }
}
```

`preHandle` returning `false` aborts the controller.

---

## 6. Message conversion and content negotiation

`@RequestBody User u` → `HttpMessageConverter` (usually Jackson `MappingJackson2HttpMessageConverter`).

`Content-Type: application/json` selects the reader. `Accept` selects the writer.

```java
@PostMapping(path = "/orders", consumes = "application/json", produces = "application/json")
```

If JSON is malformed → `HttpMessageNotReadableException` → 400 (if you handle it).

Customize Jackson via `Jackson2ObjectMapperBuilderCustomizer`, not a raw `ObjectMapper` bean (chapter 05).

**DTO vs entity:** controllers speak DTOs. Entities have lazy proxies and persistence annotations. Map in the service or with MapStruct.

---

## 7. Controller shape

```java
@RestController
@RequestMapping("/v1/orders")
public class OrderController {
    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable UUID id) {
        return orders.get(id);
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest req) {
        OrderResponse created = orders.create(req);
        return ResponseEntity
            .created(URI.create("/v1/orders/" + created.id()))
            .body(created);
    }
}
```

`ResponseEntity` when you need headers/status. `@ResponseStatus(HttpStatus.CREATED)` is shorter but weaker for `Location`.

Pagination: `Pageable` + Spring Data (`?page=0&size=20&sort=createdAt,desc`). Return `Page<T>` or a stable envelope `{ content, totalElements, … }` — don’t leak the Spring `Page` type if you want a stable public API.

---

## 8. CORS

Browser calls from `https://app.acme.com` to `https://api.acme.com` are cross-origin. The **browser** enforces CORS. curl does not.

Global (preferred over `@CrossOrigin` on every controller):

```java
@Bean
CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration c = new CorsConfiguration();
    c.setAllowedOrigins(List.of("https://app.acme.com"));
    c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
    c.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    c.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/v1/**", c);
    return source;
}
```

If Spring Security is on the classpath, CORS must be **enabled in the security filter chain** too (`cors(Customizer.withDefaults())`), otherwise the preflight dies at security. Chapter 11.

`allowedOrigins("*")` **cannot** combine with `allowCredentials(true)`.

---

## 9. Calling other HTTP APIs

| Client | Model | Status |
|--------|--------|--------|
| `RestTemplate` | Blocking, servlet-thread | Maintenance mode; fine in existing MVC apps |
| `WebClient` | Reactive; can `block()` in MVC if you must | Preferred for new clients |
| JDK `HttpClient` | Blocking or async | Fine, fewer Boot integrations |
| OpenFeign / HTTP interfaces (Boot 3.2 / Spring 6.1) | Interface proxy | Nice for many service clients |

Boot: inject `RestTemplateBuilder` / `WebClient.Builder`. Set connect/read timeouts. Never use the no-arg `new RestTemplate()` in production.

```java
@Bean
WebClient paymentsWebClient(WebClient.Builder builder) {
    return builder.baseUrl("http://payments")
        .build();
}
```

Timeouts, retries, circuit breakers: Resilience4j (not Boot core). At least set **timeouts**. An infinite client call exhausts Tomcat threads.

---

## 10. Caching HTTP

```http
Cache-Control: max-age=60
ETag: "abc"
If-None-Match: "abc"   → 304
```

Spring: `ShallowEtagHeaderFilter`, or set headers on `ResponseEntity`. GET-only. Authenticated personalized data is usually `Cache-Control: private, no-store`.

---

## 11. OpenAPI

`springdoc-openapi` reads mappings and produces Swagger UI. Keep annotations (`@Operation`, `@Schema`) on **public** APIs. Internal admin controllers: exclude.

---

## 12. HATEOAS

Links in the body (`_links.self`). Useful for hypermedia clients; most company SPAs ignore it. Know the acronym; don’t force it.

---

## 13. Production pitfalls

1. Returning JPA entities → lazy `ByteBuddyInterceptor` JSON errors, or huge graphs.
2. `@Transactional` on the controller → connection held while writing JSON.
3. No timeouts on `RestTemplate` → thread pool death.
4. PUT used as PATCH (partial body wiping fields with null).
5. POST without idempotency key for payments.
6. CORS only on MVC, not on Security.
7. Swallowing exceptions into 200 `{ "error": "..." }` — clients cannot branch on status.
8. `throws Exception` on controllers instead of advice.

---

# Interview Q&A (5–8 year bar)

A fresher maps GET/POST. A 5–8 year answer walks filters → `DispatcherServlet` → converters, and why `@ControllerAdvice` missed a JWT error.

### Q1. What is REST? Is it HTTP?

**Answer:** REST is an architectural style (resources, uniform interface, statelessness, cache). HTTP is the usual protocol that implements it.

**Counter:** Can REST be over something else?  
**Answer:** The original thesis was not HTTP-only. In industry, REST means HTTP APIs.

---

### Q2. PUT vs PATCH vs POST?

**Answer:** POST create/action (not idempotent). PUT full replace (idempotent). PATCH partial (idempotency depends on your semantics).

**Counter:** Is PATCH always non-idempotent?  
**Answer:** JSON Patch with the same document can be idempotent. “Not always” is the safe answer.

---

### Q3. What is idempotency? Why do payments care?

**Answer:** Repeating the request does not change the result beyond the first application. Clients retry on timeouts. Without keys, retries double-charge.

**Counter:** Is GET idempotent?  
**Answer:** Yes. Safe and idempotent. Logging a GET server-side does not break the HTTP meaning.

---

### Q4. Walk a request through Spring MVC.

**Answer:** Filters → `DispatcherServlet` → `HandlerMapping` → interceptors `preHandle` → argument resolvers → controller → return value / `HttpMessageConverter` → interceptors → filters.

**Counter:** Where does JWT auth run?  
**Answer:** A **servlet filter**, before the dispatcher (Spring Security filter chain).

**Counter:** Will `@ControllerAdvice` catch a filter exception?  
**Answer:** No. Filters are outside MVC. Use `AuthenticationEntryPoint` / `AccessDeniedHandler` or a filter-level handler.

---

### Q5. Filter vs interceptor?

**Answer:** Filter = servlet API, whole chain. Interceptor = Spring MVC, only mapped handlers, has access to `handler`.

**Trap:** “They are the same.”

---

### Q6. How does `@RequestBody` work?

**Answer:** `RequestResponseBodyMethodProcessor` + `HttpMessageConverter` (Jackson for JSON) using `Content-Type`.

**Counter:** How do you support XML too?  
**Answer:** Add Jackson XML or JAXB converter; `consumes`/`produces` or content negotiation.

---

### Q7. How do you version an API?

**Answer:** URI `/v1` is the usual. Headers are purist. Don’t mix three strategies.

---

### Q8. How do you secure a REST API?

**Answer:** HTTPS, authn (JWT / session / mTLS), authz (method security / filter), input validation, rate limit at gateway, no entities in JSON, CSRF only if cookie session (chapter 11).

---

### Q9. `RestTemplate` vs `WebClient`?

**Answer:** `RestTemplate` blocking, maintenance mode. `WebClient` reactive, preferred for new code. In MVC you may still block on `WebClient` — then you are not magically non-blocking.

**Counter:** Why `RestTemplateBuilder`?  
**Answer:** Boot-applied timeouts, message converters, Metrics.

---

### Q10. How do you design for scale?

**Answer:** Stateless app, cache GETs, pagination, async for long work, timeouts, idempotent writes, horizontal pods behind a load balancer. REST constraints enable that.

---

### Q11. 401 vs 403 vs 404 for a hidden resource?

**Answer:** Unauthenticated → 401. Authenticated but not owner: some teams return **404** (don’t leak existence), others 403. Be consistent and document.

---

### Q12. What is HATEOAS?

**Answer:** Responses include links to next actions. Richardson maturity level 3. Rare in internal APIs.

---

### Q13. CORS preflight?

**Answer:** Browser sends `OPTIONS` with `Access-Control-Request-Method`. Server must allow origin/method/headers. Security filter must not block OPTIONS.

---

### Q14. Why `ResponseEntity`?

**Answer:** Explicit status, headers (`Location`, `ETag`), body. Cleaner than `@ResponseStatus` for branches (201 vs 200).

---

### Q15. What is content negotiation?

**Answer:** Picking converter from `Accept` / `Content-Type` / path extension (extension is often disabled). Same handler, different representation.
