# 3. REST and RESTful API Design

## Simple idea

**REST** (Representational State Transfer) is not a library. It is a **style** for APIs, described by Roy Fielding (2000).

You treat business things as **resources** and use normal HTTP to act on them.

```text
GET    /users/42      read user 42
POST   /orders        create an order
PATCH  /users/42      change part of the user
DELETE /products/9    remove product 9
```

The server does not remember the previous call. The URL + method + body say everything.

---

## Why teams like REST

- uses HTTP that every language already speaks
- easy across web, mobile, and partners
- cache and stateless servers help scale

---

## Six constraints

The course slides list the first five. The interview PDF adds the sixth as **optional**.

| # | Constraint | Plain meaning |
|---|------------|----------------|
| 1 | **Client-server** | UI and API can change separately |
| 2 | **Stateless** | Each request carries auth and data. No server memory required |
| 3 | **Cacheable** | Responses can say "reuse me" |
| 4 | **Layered system** | Client does not care if a gateway, cache, or LB sits in the middle |
| 5 | **Uniform interface** | Same rules everywhere: resource URLs + HTTP verbs |
| 6 | **Code on demand** (optional) | Server may send code such as JavaScript. Rarely the point of a JSON API |

A **REST API** follows some of these ideas. A **RESTful API** follows them properly (the strict word). In interviews, say the difference, then design as if you mean RESTful.

---

## Resources and endpoints

A **resource** is a thing: user, order, product.

An **endpoint** is the URL + method where you touch that thing.

```text
GET    /users/42
POST   /orders
DELETE /products/9
```

The resource is usually a **noun**, not a verb.

---

## URL habits

```text
Good:  GET  /users
Good:  GET  /v1/users/42
Good:  PATCH /users/42     body: { "status": "active" }

Weak:  POST /users/42/activate
Weak:  GET  /getUserById?id=42
```

- plural collections: `/users`, `/orders`
- version in the path when you must not break old apps: `/v1/users`
- do not put the action in the path if an HTTP method already says it

---

## Methods, safety, idempotency

| Method | Typical job | Safe? (no change) | Idempotent? (repeat = same result) |
|--------|-------------|-------------------|-------------------------------------|
| GET, HEAD | Read | Yes | Yes |
| PUT | Full replace | No | Yes |
| DELETE | Remove | No | Yes (second delete still "gone") |
| PATCH | Partial update | No | Not always |
| POST | Create | No | No (two POSTs can create two orders) |

**Why this matters:** clients and gateways **retry**. If POST is retried blindly, you can charge twice. PUT/DELETE retries are safer. For POST, use an **idempotency key**.

**PUT vs PATCH**

- PUT: send the **whole** user. Missing fields can be wiped.
- PATCH: send only `email`.

---

## Status codes you will say

| Code | Meaning |
|------|---------|
| 200 | OK, here is the body |
| 201 | Created |
| 204 | Done, no body (often DELETE) |
| 400 | Bad request |
| 401 | Not authenticated |
| 403 | Authenticated, not allowed |
| 404 | No such resource |
| 500 | Server bug |

---

## JSON vs XML

| | JSON | XML |
|--|------|-----|
| Weight | Light | Heavy |
| Parse | Fast | Slower |
| Readability | Easy | Verbose |
| Use | Almost all new APIs | Old systems, strict schema validation |

Default to **JSON** unless a partner already speaks XML.

---

## Design habits (from the course)

- plural nouns
- correct status codes
- versioning (`/v1/...`)
- pagination: `GET /users?page=2&limit=20`
- rate limit / throttle so one client cannot melt you
- auth: **OAuth 2.0**, **JWT**, API keys for partners

**Blog sketch**

```text
GET    /posts
POST   /posts
GET    /posts/{id}
POST   /posts/{id}/comments
```

---

## HATEOAS

**HATEOAS** means the response includes **links** to what you can do next.

```json
{
  "id": 1,
  "name": "John",
  "links": {
    "self": "/users/1",
    "orders": "/users/1/orders"
  }
}
```

The client discovers URLs instead of hard-coding every path. Many real APIs skip full HATEOAS. Know the name and the idea.

---

## Auth

- **OAuth 2.0** — user login / delegated access
- **JWT** — compact token the API can check
- **API keys** — simple key for a partner or internal caller

Authentication = who you are. Authorization = what you may do (403 if the token is valid but the role is wrong).

---

## Caching, paging, versions

**Cache:** `Cache-Control: max-age=3600`, `ETag`. Cache GET, not payment POST.

**Paging:** `?page=2&limit=10` so you never return 1 million rows.

**Versions**

| Style | Example |
|-------|---------|
| URI | `/v1/users` |
| Header | `Accept-Version: v1` |
| Query | `?version=1` |

URI versioning is the easiest to see in logs.

---

## Performance

- cache
- gzip / brotli
- async work for slow jobs (do not block the HTTP thread)
- DB indexes
- pagination

---

## Can REST be stateful?

The style says **no**. Each request should carry what it needs.

Some apps still keep server sessions. That API is then **not** really stateless, and load balancing gets harder (sticky sessions). Prefer tokens.

---

## Security

- SQL injection → parameterised queries
- CSRF → tokens on cookie-based browser flows; bearer tokens are a different model
- snooping → **HTTPS** only

---

## REST vs SOAP (one minute)

| | REST | SOAP |
|--|------|------|
| What it is | Style | Strict protocol |
| Body | Usually JSON | XML |
| Speed | Lighter | Heavier (XML + WS-Security) |
| State | Stateless | Can be stateful |

REST vs GraphQL vs gRPC is the next file: [05_Beyond_REST.md](05_Beyond_REST.md).

---

## Interview lines

- REST = resources + HTTP + stateless.
- Six constraints; code-on-demand is optional.
- POST is not idempotent. PUT and DELETE are.
- Nouns, versions, pages, JWT/OAuth, correct status codes.

---

## Interview Q&A

Read the question. Answer out loud. Then open the answer.

### 1. What is REST, and how does it differ from SOAP?

<details>
<summary>Show Answer</summary>

**Answer:**

REST is an architectural style: resources, HTTP methods, stateless calls.

SOAP is a protocol with a strict XML envelope.

REST is usually JSON and lighter. SOAP is XML, heavier, and can keep state. REST is the default for public HTTP APIs.

</details>

### 2. What are the six constraints of REST?

<details>
<summary>Show Answer</summary>

**Answer:**

1. Client-server
2. Stateless
3. Cacheable
4. Layered system
5. Uniform interface
6. Code on demand (optional)

</details>

### 3. What is the difference between a REST API and a RESTful API?

<details>
<summary>Show Answer</summary>

**Answer:**

REST API = uses HTTP and resource ideas, maybe loosely.

RESTful API = actually follows the REST constraints (stateless, uniform interface, cacheable, and so on).

</details>

### 4. What is a resource, and what is an endpoint?

<details>
<summary>Show Answer</summary>

**Answer:**

A resource is the thing (user, order). It is named by a URI such as `/users/42`.

An endpoint is the URL you call, with a method: `GET /users/42`, `POST /orders`.

</details>

### 5. Explain GET, POST, PUT, PATCH, and DELETE.

<details>
<summary>Show Answer</summary>

**Answer:**

- GET — read, safe, idempotent
- POST — create, not idempotent
- PUT — full replace, idempotent
- PATCH — partial update, not always idempotent
- DELETE — remove, idempotent

</details>

### 6. When would you use PUT vs PATCH?

<details>
<summary>Show Answer</summary>

**Answer:**

PUT when the client sends the **entire** new resource. PATCH when it sends **only the changed fields**.

</details>

### 7. Which status codes should you know?

<details>
<summary>Show Answer</summary>

**Answer:**

200 OK, 201 Created, 204 No Content, 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 500 Internal Server Error.

</details>

### 8. What are best practices for RESTful APIs?

<details>
<summary>Show Answer</summary>

**Answer:**

Plural nouns, right status codes, `/v1` versioning, pagination, rate limits, OAuth or JWT, HTTPS, JSON.

</details>

### 9. How do you design a blogging API?

<details>
<summary>Show Answer</summary>

**Answer:**

```text
GET  /posts
POST /posts
GET  /posts/{id}
POST /posts/{id}/comments
```

Users can be `/users`. Keep nouns. Do not invent `/createPost`.

</details>

### 10. What is HATEOAS?

<details>
<summary>Show Answer</summary>

**Answer:**

Responses include links to related actions (`self`, `orders`) so the client can follow the API instead of memorising every URL.

</details>

### 11. How do you handle authentication and authorization?

<details>
<summary>Show Answer</summary>

**Answer:**

OAuth 2.0 for user login, JWT for the token the API checks, API keys for partners. Reject missing identity with 401. Reject a valid user who lacks permission with 403.

</details>

### 12. What are rate limiting and throttling, and why do they matter?

<details>
<summary>Show Answer</summary>

**Answer:**

They cap how many calls one client can make. That stops abuse, noisy neighbours, and simple DDoS from taking down the API. Return **429** when the limit is hit.

</details>

### 13. How do you keep backward compatibility?

<details>
<summary>Show Answer</summary>

**Answer:**

Version the API (`/v1`, `/v2`), add fields without removing old ones, and keep the old version alive until clients move.

</details>

### 14. How does caching work in REST APIs?

<details>
<summary>Show Answer</summary>

**Answer:**

Use `Cache-Control` (for example `max-age=3600`) and `ETag` on safe GETs. Do not cache personalised or payment responses by accident.

</details>

### 15. JSON or XML?

<details>
<summary>Show Answer</summary>

**Answer:**

JSON: small, fast, readable. XML: legacy partners and strict validation. New APIs should default to JSON.

</details>

### 16. How do you paginate?

<details>
<summary>Show Answer</summary>

**Answer:**

`GET /users?page=2&limit=10` (offset style). For very large or changing lists, cursor style (`?cursor=abc&limit=10`) is more stable. The course example is page + limit.

</details>

### 17. How does versioning work?

<details>
<summary>Show Answer</summary>

**Answer:**

Path `/v1/resource`, header `Accept-Version: v1`, or query `?version=1`. Path versions are the most obvious.

</details>

### 18. How would you improve REST performance?

<details>
<summary>Show Answer</summary>

**Answer:**

Cache, compress (gzip), do slow work asynchronously, index the database, paginate, and avoid chatty calls when one response can carry what the client needs.

</details>

### 19. Can a REST API be stateful?

<details>
<summary>Show Answer</summary>

**Answer:**

It **should not**. Stateless is a core constraint. Session stickiness makes it stateful and harder to scale. Prefer tokens.

</details>

### 20. How does REST handle SQL injection and CSRF?

<details>
<summary>Show Answer</summary>

**Answer:**

SQL injection: parameterised queries, never glue user text into SQL.

CSRF: anti-CSRF tokens when the browser auto-sends cookies. HTTPS always. Auth tokens in headers are not sent automatically the way cookies are.

</details>

**Q&A recap:** Six constraints, nouns + verbs, idempotency, status codes, versioning, JWT, cache, paging.

## Interview Q&A — 6 to 8 years

### S1. Offset pagination works in the demo and corrupts the product at scale. How?

<details>
<summary>Show Answer</summary>

**Answer:**

`OFFSET 100000` still makes the database walk and throw away 100,000 rows. New rows inserted at the top shift the pages, so the user sees duplicates or skips a row. I use a cursor: "give me 50 rows after id X" on an indexed column. I also set a max page size so a client cannot ask for a million rows.

**Corner cases:** The cursor is a raw offset in disguise. Ties on the sort key (two rows with the same timestamp) skip or repeat unless I add a unique id as a second key. A cursor that encodes a deleted id should return the next live row, not an empty page. Exposing an internal primary key in the cursor leaks a guessable sequence. I sign the cursor or I accept that ids are visible. A filter change in the middle of paging needs a new cursor, not the old one.

</details>

### S2. How do you version a public REST API that mobile apps will call for years?

<details>
<summary>Show Answer</summary>

**Answer:**

I add fields without changing the meaning of old ones, and I keep old routes alive until usage is near zero. `/v2` is a new resource when the meaning changes, not when I feel like a cleanup. I measure calls per version. I do not reuse a field for a new meaning. Sunset is a date, a response header, and a log, not a surprise deploy.

**Corner cases:** A "default version" that moves under apps still in the store. Enum values the old app treats as errors. A breaking change hidden in a query parameter. Internal services pinned to v1 so I can never delete it. Idempotency and auth must work the same on every version I still serve, or retries across a version switch double-write.

</details>

### S3. A "REST" endpoint does ten writes across four services. What should a senior say?

<details>
<summary>Show Answer</summary>

**Answer:**

That is a workflow, not a resource update. I make one service own the outcome. It writes its own row first (order = PENDING), calls the others with idempotency keys, and moves to CONFIRMED or FAILED. Callers retry the same request. I do not pretend four independent commits are one ACID transaction. If the business truly needs one atomic commit, those data live in one database.

**Corner cases:** The owner crashes after payment succeeds and before it stores the payment id. Reconciliation by idempotency key, not a second charge. Timeouts mark the order FAILED while the other service later succeeds. I need an outbox or a reconcile job. Returning 200 only when all four finished makes the client wait through the slowest hop and retry the whole chain. Partial success must be visible in the resource (`status=PENDING`).

</details>

### S4. How do you design list filters so they stay fast and safe?

<details>
<summary>Show Answer</summary>

**Answer:**

I allow a fixed set of filter and sort fields that match an index. Unknown sort fields are 400, not a full table scan. I cap page size. I authorize the rows (a user cannot filter someone else's orders by guessing a query). Search text goes to a search index, not to `LIKE %foo%` on the primary table.

**Corner cases:** An optional filter that is missing in production hits the unindexed path and melts the primary. A client sends a huge `in` list. I cap it. Combining filters with OR defeats the index. Time ranges that are open-ended (`created < now` with no start) scan history. I require a bound. GraphQL-style field picking on a REST API can still over-fetch if I join every table "just in case."

</details>

**Senior recap:** cursor pages, additive versions, one owner for a multi-service write, and filters that match an index.

Next: [04_Real_Time_Communication.md](04_Real_Time_Communication.md)
