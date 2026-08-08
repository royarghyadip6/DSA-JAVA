# 58. REST API

## Basics

---

# 1. What is REST?

<details>
<summary>Show Answer</summary>

**Answer:**

**REST** (Representational State Transfer) is an **architectural style** for designing networked APIs. It treats server data as **resources** identified by **URIs**, manipulated via **HTTP methods**, and exchanged as **representations** (usually JSON).

### Simple Idea

```text
Client  →  GET /orders/101  →  Server
Client  ←  JSON { "id": 101, "status": "SHIPPED" }  ←  Server
```

### REST vs HTTP

| | REST | HTTP |
|---|------|------|
| What | Design rules / style | Transport protocol |
| Relationship | Uses HTTP | Carries REST requests |

```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @GetMapping("/{id}")
    public OrderDto getOrder(@PathVariable Long id) {
        return orderService.findById(id);
    }
}
```

**Interview Point:**

> REST is not a protocol — it is a set of constraints on top of HTTP. Resources + URIs + stateless communication = REST.

</details>

---

# 2. REST principles?

<details>
<summary>Show Answer</summary>

**Answer:**

REST is built on **six architectural constraints** (Richardson Maturity Model):

| # | Principle | Meaning |
|---|-----------|---------|
| 1 | **Client-Server** | UI and business logic are separated |
| 2 | **Stateless** | Each request carries all context needed |
| 3 | **Cacheable** | Responses declare if they can be cached |
| 4 | **Uniform Interface** | Standard HTTP verbs, URIs, representations |
| 5 | **Layered System** | Client may talk to proxy/gateway, not real server |
| 6 | **Code on Demand** *(optional)* | Server can send executable code (rare) |

```text
Stateless example:
  ❌ Server remembers "user is logged in" across requests without token
  ✅ Client sends Authorization: Bearer <JWT> on every request
```

**Interview Point:**

> **Stateless** and **Uniform Interface** are the two most asked. Stateless = easier horizontal scaling.

</details>

---

# 3. REST constraints?

<details>
<summary>Show Answer</summary>

**Answer:**

Constraints are **rules** that make APIs scalable and simple:

| Constraint | Violation Example | Correct Approach |
|------------|-------------------|------------------|
| Stateless | Session stored only on server | JWT / token per request |
| Uniform Interface | `POST /getUserById` | `GET /users/{id}` |
| Cacheable | No `Cache-Control` header | `Cache-Control: max-age=300` |
| Layered | Client hardcodes internal service IP | API Gateway in front |

```java
// Uniform interface — resource-oriented URI
@GetMapping("/users/{id}")          // ✅
@PostMapping("/users/getById")       // ❌ RPC style, not RESTful
```

**Interview Point:**

> Constraints are trade-offs for scalability. Breaking statelessness forces sticky sessions and hurts cloud scaling.

</details>

---

## HTTP Methods

---

# 4. GET

<details>
<summary>Show Answer</summary>

**Answer:**

**GET** retrieves a resource. It is **safe** (no server state change) and **idempotent**.

| Property | Value |
|----------|-------|
| Purpose | Read / fetch |
| Body | Usually none |
| Safe | ✅ Yes |
| Idempotent | ✅ Yes |
| Cacheable | ✅ Yes |

```http
GET /api/v1/products?category=electronics&page=0&size=20 HTTP/1.1
Host: api.example.com
Accept: application/json
```

```java
@GetMapping
public Page<ProductDto> listProducts(
        @RequestParam String category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    return productService.findByCategory(category, PageRequest.of(page, size));
}
```

**Interview Point:**

> GET must not modify data. Never put sensitive data in URL query strings (logged in access logs).

</details>

---

# 5. POST

<details>
<summary>Show Answer</summary>

**Answer:**

**POST** creates a new resource or triggers a **non-idempotent** action.

| Property | Value |
|----------|-------|
| Purpose | Create / submit |
| Safe | ❌ No |
| Idempotent | ❌ No (duplicate calls may create duplicates) |
| Typical response | `201 Created` + `Location` header |

```http
POST /api/v1/orders HTTP/1.1
Content-Type: application/json

{ "productId": 42, "quantity": 2 }
```

```java
@PostMapping
public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request) {
    OrderDto created = orderService.create(request);
    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}").buildAndExpand(created.getId()).toUri();
    return ResponseEntity.created(location).body(created);
}
```

**Interview Point:**

> POST to collection URI (`/orders`) creates. Use idempotency keys in payment APIs to handle duplicate POST retries.

</details>

---

# 6. PUT

<details>
<summary>Show Answer</summary>

**Answer:**

**PUT** replaces an entire resource at a known URI. **Idempotent** — calling it multiple times yields the same result.

| Property | Value |
|----------|-------|
| Purpose | Full update / upsert |
| Idempotent | ✅ Yes |
| Body | Complete resource representation |

```http
PUT /api/v1/users/5 HTTP/1.1
Content-Type: application/json

{ "id": 5, "name": "Alice", "email": "alice@example.com", "role": "ADMIN" }
```

```java
@PutMapping("/{id}")
public UserDto replaceUser(@PathVariable Long id, @Valid @RequestBody UserDto user) {
    return userService.replace(id, user); // overwrites all fields
}
```

**Interview Point:**

> PUT = replace whole resource. Missing fields in body may be set to null — prefer PATCH for partial updates.

</details>

---

# 7. PATCH

<details>
<summary>Show Answer</summary>

**Answer:**

**PATCH** applies a **partial update** to a resource. Only changed fields are sent.

| | PUT | PATCH |
|---|-----|-------|
| Scope | Full replacement | Partial update |
| Idempotent | ✅ Yes | ⚠️ Usually yes (depends on semantics) |
| Body size | Entire object | Only changed fields |

```http
PATCH /api/v1/users/5 HTTP/1.1
Content-Type: application/json

{ "email": "newemail@example.com" }
```

```java
@PatchMapping("/{id}")
public UserDto patchUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
    return userService.partialUpdate(id, updates);
}
```

**Interview Point:**

> PATCH is preferred for mobile clients saving bandwidth. Document which fields are patchable.

</details>

---

# 8. DELETE

<details>
<summary>Show Answer</summary>

**Answer:**

**DELETE** removes a resource. **Idempotent** — deleting the same resource twice should still succeed (often `204` or `404`).

```http
DELETE /api/v1/users/5 HTTP/1.1
```

```java
@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void deleteUser(@PathVariable Long id) {
    userService.delete(id);
}
```

| Response | When |
|----------|------|
| `204 No Content` | Successfully deleted |
| `404 Not Found` | Resource already gone |
| `409 Conflict` | Cannot delete (has dependencies) |

**Interview Point:**

> Prefer soft delete (`deleted=true`) in production for audit trails. Expose as DELETE for API consumers.

</details>

---

## Frequently Asked

---

# 9. PUT vs PATCH?

<details>
<summary>Show Answer</summary>

**Answer:**

| | PUT | PATCH |
|---|-----|-------|
| Update type | Full replacement | Partial update |
| Missing fields | May null out omitted fields | Only updates sent fields |
| Use case | Replace entire record | Change one or two fields |
| Idempotent | Always | Typically yes |

```json
// PUT /users/5 — must send ALL fields
{ "name": "Bob", "email": "bob@x.com", "role": "USER" }

// PATCH /users/5 — only what changes
{ "role": "ADMIN" }
```

**Interview Point:**

> PUT overwrites; PATCH merges. In banking/fintech, PATCH is common for status updates.

</details>

---

# 10. POST vs PUT?

<details>
<summary>Show Answer</summary>

**Answer:**

| | POST | PUT |
|---|------|-----|
| URI | Collection (`/users`) | Specific resource (`/users/5`) |
| Idempotent | ❌ No | ✅ Yes |
| Server assigns ID | ✅ Usually | Client may provide ID |
| Purpose | Create | Replace / upsert |

```text
POST /users        → server generates id=101
PUT  /users/101    → client says "store this at id 101"
```

**Interview Point:**

> POST = "create something". PUT = "put this exact resource at this URI". Duplicate POST = two records; duplicate PUT = same result.

</details>

---

# 11. Idempotent methods?

<details>
<summary>Show Answer</summary>

**Answer:**

An operation is **idempotent** if performing it **multiple times** has the **same effect** as performing it once.

| Method | Idempotent? |
|--------|-------------|
| GET | ✅ Yes |
| PUT | ✅ Yes |
| DELETE | ✅ Yes |
| PATCH | ⚠️ Usually |
| POST | ❌ No |

```text
PUT /accounts/1/balance  { "balance": 1000 }
  Call 1 → balance = 1000
  Call 2 → balance = 1000  (same result)

POST /transfers  { "amount": 100 }
  Call 1 → transfer #1 created
  Call 2 → transfer #2 created  (different result!)
```

**Interview Point:**

> Idempotency matters for retries (network timeouts). Use idempotency keys with POST in payment systems.

</details>

---

# 12. Safe methods?

<details>
<summary>Show Answer</summary>

**Answer:**

A **safe** method does **not modify** server state. It is read-only.

| Method | Safe? |
|--------|-------|
| GET | ✅ Yes |
| HEAD | ✅ Yes |
| OPTIONS | ✅ Yes |
| POST | ❌ No |
| PUT | ❌ No |
| PATCH | ❌ No |
| DELETE | ❌ No |

```text
Safe:     GET /reports/sales     → only reads data
Not safe: POST /reports/generate → triggers report creation
```

**Interview Point:**

> Safe ≠ idempotent. GET is both safe and idempotent. DELETE is idempotent but not safe.

</details>

---

## Status Codes

---

# 13. 200

<details>
<summary>Show Answer</summary>

**Answer:**

**200 OK** — request succeeded. Standard response for successful GET, PUT, PATCH.

```http
HTTP/1.1 200 OK
Content-Type: application/json

{ "id": 1, "name": "Alice" }
```

```java
@GetMapping("/{id}")
public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
    return ResponseEntity.ok(userService.findById(id));
}
```

**Interview Point:**

> 200 for successful reads and updates. Don't use 200 for resource creation — use 201.

</details>

---

# 14. 201

<details>
<summary>Show Answer</summary>

**Answer:**

**201 Created** — a new resource was successfully created. Include `Location` header pointing to the new resource.

```http
HTTP/1.1 201 Created
Location: /api/v1/orders/5501
Content-Type: application/json

{ "id": 5501, "status": "PENDING" }
```

```java
return ResponseEntity.created(location).body(createdOrder);
```

**Interview Point:**

> Always return 201 + Location header on POST create. Helps clients discover the new resource URI.

</details>

---

# 15. 204

<details>
<summary>Show Answer</summary>

**Answer:**

**204 No Content** — success with **no response body**. Common for DELETE and updates where no data is returned.

```http
HTTP/1.1 204 No Content
```

```java
@DeleteMapping("/{id}")
@ResponseStatus(HttpStatus.NO_CONTENT)
public void delete(@PathVariable Long id) {
    service.delete(id);
}
```

**Interview Point:**

> 204 = "done, nothing to return". Don't send `{}` body with 204.

</details>

---

# 16. 400

<details>
<summary>Show Answer</summary>

**Answer:**

**400 Bad Request** — client sent invalid input (validation failure, malformed JSON, missing required field).

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "email must be a valid email address",
  "timestamp": "2026-08-08T10:00:00Z"
}
```

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    return ResponseEntity.badRequest().body(buildError(ex));
}
```

**Interview Point:**

> 400 = client's fault (bad input). Distinguish from 404 (resource not found) and 422 (semantic validation).

</details>

---

# 17. 401

<details>
<summary>Show Answer</summary>

**Answer:**

**401 Unauthorized** — authentication failed or missing. Client must provide valid credentials.

```http
HTTP/1.1 401 Unauthorized
WWW-Authenticate: Bearer realm="api"
```

| Code | Meaning |
|------|---------|
| 401 | Who are you? (not authenticated) |
| 403 | I know you, but you can't do this (not authorized) |

**Interview Point:**

> 401 = not logged in / bad token. 403 = logged in but insufficient permissions. Don't confuse them.

</details>

---

# 18. 403

<details>
<summary>Show Answer</summary>

**Answer:**

**403 Forbidden** — client is authenticated but **lacks permission** for the requested action.

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "ROLE_USER cannot delete orders"
}
```

```java
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/{id}")
public void deleteOrder(@PathVariable Long id) { ... }
```

**Interview Point:**

> 403 after successful auth when RBAC/ABAC denies access. Return generic message — don't leak resource existence.

</details>

---

# 19. 404

<details>
<summary>Show Answer</summary>

**Answer:**

**404 Not Found** — resource does not exist at the requested URI.

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Order 9999 not found"
}
```

```java
@GetMapping("/{id}")
public OrderDto getOrder(@PathVariable Long id) {
    return orderService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order", id));
}
```

**Interview Point:**

> 404 for missing resources. Some APIs return 404 instead of 403 to avoid revealing that a resource exists.

</details>

---

# 20. 409

<details>
<summary>Show Answer</summary>

**Answer:**

**409 Conflict** — request conflicts with current server state (duplicate email, optimistic lock failure, version mismatch).

```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Email already registered"
}
```

```java
// Optimistic locking with @Version
@Entity
public class Account {
    @Version
    private Long version;
}
// StaleObjectStateException → map to 409
```

**Interview Point:**

> 409 for business rule conflicts and concurrent update collisions. Common with `@Version` in JPA.

</details>

---

# 21. 500

<details>
<summary>Show Answer</summary>

**Answer:**

**500 Internal Server Error** — unexpected server failure. Never expose stack traces to clients in production.

```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "traceId": "abc-123-def"
}
```

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
    log.error("Unhandled exception", ex);
    return ResponseEntity.status(500)
            .body(new ErrorResponse("Internal error", traceId));
}
```

**Interview Point:**

> Log full stack trace server-side. Return generic message + traceId to client for support correlation.

</details>

---

## API Design

---

# 22. REST API naming conventions?

<details>
<summary>Show Answer</summary>

**Answer:**

| Rule | Good ✅ | Bad ❌ |
|------|---------|--------|
| Use nouns, not verbs | `/users` | `/getUsers` |
| Plural resources | `/orders` | `/order` |
| Lowercase + hyphens | `/order-items` | `/OrderItems` |
| Hierarchy for relations | `/users/5/orders` | `/getUserOrders?userId=5` |
| No file extensions | `/users/1` | `/users/1.json` |

```text
GET    /api/v1/products           → list
GET    /api/v1/products/42         → get one
POST   /api/v1/products           → create
PUT    /api/v1/products/42        → replace
DELETE /api/v1/products/42        → delete
```

**Interview Point:**

> URIs identify resources, HTTP verbs define actions. Keep URIs short, predictable, and versioned.

</details>

---

# 23. Versioning strategies?

<details>
<summary>Show Answer</summary>

**Answer:**

| Strategy | Example | Pros | Cons |
|----------|---------|------|------|
| URI path | `/api/v1/users` | Simple, visible | URI changes |
| Header | `Accept: application/vnd.myapi.v2+json` | Clean URIs | Harder to test in browser |
| Query param | `/users?version=2` | Easy to add | Messy, rarely used |
| Media type | Content negotiation | RESTful purist | Complex |

```java
// URI versioning — most common in Spring Boot
@RestController
@RequestMapping("/api/v1/users")
public class UserControllerV1 { ... }

@RestController
@RequestMapping("/api/v2/users")
public class UserControllerV2 { ... }
```

**Interview Point:**

> URI versioning (`/v1/`, `/v2/`) is most common in industry. Support old version for a deprecation window.

</details>

---

# 24. URI design best practices?

<details>
<summary>Show Answer</summary>

**Answer:**

| Practice | Detail |
|----------|--------|
| Limit nesting depth | Max 2–3 levels: `/users/{id}/orders/{orderId}` |
| Filtering via query params | `/products?status=active&sort=price,asc` |
| Pagination | `?page=0&size=20` or cursor-based `?cursor=abc` |
| No CRUD verbs in URI | ❌ `/createUser` ✅ `POST /users` |
| Consistent error format | Same JSON structure for all errors |

```text
GET /api/v1/orders?status=SHIPPED&page=0&size=20&sort=createdAt,desc
```

**Interview Point:**

> Collections = plural nouns. Sub-resources only when strong parent-child relationship exists.

</details>

---

## Advanced

---

# 25. Pagination?

<details>
<summary>Show Answer</summary>

**Answer:**

Pagination splits large result sets into pages to reduce memory and improve response time.

| Style | How | Best For |
|-------|-----|----------|
| Offset-based | `?page=0&size=20` | Small/medium datasets |
| Cursor-based | `?cursor=eyJpZCI6MTAwfQ` | Large feeds, real-time data |

```java
@GetMapping
public Page<OrderDto> listOrders(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) {
    return orderService.findAll(PageRequest.of(page, size));
}
```

```json
{
  "content": [ ... ],
  "totalElements": 1500,
  "totalPages": 75,
  "number": 0,
  "size": 20
}
```

**Interview Point:**

> Offset pagination degrades on deep pages (`OFFSET 100000`). Use cursor pagination for infinite scroll / social feeds.

</details>

---

# 26. Sorting?

<details>
<summary>Show Answer</summary>

**Answer:**

Sorting is passed via query parameters. Spring Data supports `sort=field,direction`.

```http
GET /api/v1/employees?sort=salary,desc&sort=name,asc
```

```java
@GetMapping
public Page<EmployeeDto> list(
        @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
    return employeeService.findAll(pageable);
}
```

| Rule | Why |
|------|-----|
| Whitelist sortable fields | Prevent SQL injection via sort param |
| Default sort | Consistent UX when client omits sort |
| Index sorted columns | Avoid full table scans |

**Interview Point:**

> Never pass raw `sort` param to SQL without validation. Whitelist allowed fields in service layer.

</details>

---

# 27. Filtering?

<details>
<summary>Show Answer</summary>

**Answer:**

Filtering narrows results using query parameters or a search endpoint.

```http
GET /api/v1/products?category=electronics&minPrice=100&maxPrice=500&inStock=true
```

```java
@GetMapping
public List<ProductDto> search(
        @RequestParam(required = false) String category,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice) {
    return productService.search(category, minPrice, maxPrice);
}

// Complex filters — Specification pattern (Spring Data JPA)
public Page<Product> findBySpec(ProductFilter filter, Pageable pageable) {
    return productRepo.findAll(ProductSpecifications.from(filter), pageable);
}
```

**Interview Point:**

> Simple filters = query params. Complex dynamic filters = Specification/Criteria API or dedicated search service (Elasticsearch).

</details>

---

# 28. HATEOAS?

<details>
<summary>Show Answer</summary>

**Answer:**

**HATEOAS** (Hypermedia As The Engine Of Application State) — responses include **links** to related actions/resources so the client discovers the API dynamically.

```json
{
  "id": 101,
  "status": "PENDING",
  "_links": {
    "self": { "href": "/api/v1/orders/101" },
    "cancel": { "href": "/api/v1/orders/101/cancel", "method": "POST" },
    "payment": { "href": "/api/v1/orders/101/payment", "method": "POST" }
  }
}
```

```java
// Spring HATEOAS
@EntityModel<OrderDto> model = EntityModel.of(order);
model.add(linkTo(methodOn(OrderController.class).getOrder(id)).withSelfRel());
model.add(linkTo(methodOn(OrderController.class).cancel(id)).withRel("cancel"));
```

**Interview Point:**

> HATEOAS = Level 3 Richardson Maturity. Rare in mobile/SPA apps (they hardcode routes). More common in public hypermedia APIs.

</details>

---

# 29. API Gateway?

<details>
<summary>Show Answer</summary>

**Answer:**

An **API Gateway** is a single entry point that routes, secures, and manages traffic to backend microservices.

| Feature | Benefit |
|---------|---------|
| Routing | `/orders` → order-service, `/users` → user-service |
| Authentication | Centralized JWT validation |
| Rate limiting | Protect backends from abuse |
| Load balancing | Distribute traffic |
| SSL termination | HTTPS at edge |

```text
Client → API Gateway (Spring Cloud Gateway / Kong / AWS API GW)
              ├── user-service
              ├── order-service
              └── payment-service
```

```yaml
# Spring Cloud Gateway route example
spring:
  cloud:
    gateway:
      routes:
        - id: order-service
          uri: lb://ORDER-SERVICE
          predicates:
            - Path=/api/v1/orders/**
```

**Interview Point:**

> Gateway = cross-cutting concerns (auth, routing, throttling). Business logic stays in microservices.

</details>

---

# 5–8 Year Interview Rapid Fire

### Q: Is REST the same as HTTP?

<details>
<summary>Show Answer</summary>

**Answer:**

No. HTTP is the **protocol**; REST is an **architectural style** that uses HTTP methods, URIs, and stateless communication.

</details>

---

### Q: Which HTTP method is both safe and idempotent?

<details>
<summary>Show Answer</summary>

**Answer:**

**GET** and **HEAD**. They don't change server state and repeated calls have the same effect.

</details>

---

### Q: 401 vs 403?

<details>
<summary>Show Answer</summary>

**Answer:**

**401** = not authenticated (missing/invalid credentials). **403** = authenticated but not authorized for the action.

</details>

---

### Q: When to use 201 vs 200?

<details>
<summary>Show Answer</summary>

**Answer:**

**201 Created** when a new resource is created (POST). **200 OK** for successful reads and updates.

</details>

---

### Q: POST vs PUT idempotency?

<details>
<summary>Show Answer</summary>

**Answer:**

**PUT** is idempotent (same result on retry). **POST** is not (retries may create duplicates). Use idempotency keys for POST in critical flows.

</details>

---

### Q: Best versioning approach?

<details>
<summary>Show Answer</summary>

**Answer:**

**URI versioning** (`/api/v1/`) is most common — simple, visible, easy to route in gateway and document.

</details>

---

<details>
<summary>Show Answer</summary>

### Interview One-Liner

> REST = stateless, resource-oriented APIs over HTTP. GET=read, POST=create, PUT=replace, PATCH=partial update, DELETE=remove. Safe methods don't change state. Idempotent methods = same effect on retry. 200=OK, 201=Created, 204=No Content, 400=bad input, 401=unauthenticated, 403=forbidden, 404=not found, 409=conflict, 500=server error. Version via URI. Paginate/filter/sort via query params. API Gateway for routing, auth, rate limiting.

</details>
