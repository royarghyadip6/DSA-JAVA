# 5. Beyond REST — gRPC and GraphQL

## Simple idea

REST is the default public API. It is not always the best inside a big system.

```text
REST problem
  Client needs name + city
  GET /users/1 returns 40 fields          ← over-fetch
  Then GET /users/1/orders                ← extra round trip (under-fetch / chatty)

gRPC     =  small, fast, typed calls between services
GraphQL  =  one query, client picks the fields
```

There is no single winner. You justify the choice.

---

## REST limits (why the other two exist)

- **Over-fetch** — too many fields
- **Under-fetch** — too few fields, so the client calls again
- **Latency** — a screen that needs five resources needs five HTTP calls
- **Real-time** — REST has no built-in stream; you poll or add WebSockets

---

## gRPC

**gRPC** is a high-performance RPC style for **service-to-service** calls.

It uses:

| Piece | Why |
|-------|-----|
| **HTTP/2** | Many calls on **one** connection (multiplexing), plus streaming |
| **Protocol Buffers** | Binary schema. Smaller and faster to parse than JSON |
| **`.proto` file** | Contract. Tools generate client and server code (Java, Go, Python, …) |

```text
Order Service  --protobuf over HTTP/2-->  Payment Service
```

Streaming can be one-way or **full-duplex** (both sides stream).

**Use gRPC when**

- microservices talk to each other
- you need low latency or streaming
- bandwidth is tight (IoT, mobile-to-backend in some designs)
- many languages must share one contract

**Do not use gRPC as the first public browser API**

- browsers do not speak gRPC as easily as JSON
- binary is harder to debug in curl
- partners expect REST

---

## GraphQL

**GraphQL** is a query language. Usually **one HTTP endpoint**. The client describes the shape it wants. A **schema** lists types and links. **Resolvers** fill each field.

```text
POST /graphql
query {
  user(id: 1) {
    name
    city
    orders { id total }
  }
}
```

One round trip can replace `GET /users/1` + `GET /users/1/orders`.

**Use GraphQL when**

- web and mobile screens differ (each asks for its own fields)
- you want fewer calls on a slow network
- one API must stitch several backend services

**Pain at scale**

| Pain | Why | What people do |
|------|-----|----------------|
| Caching | Every query body can differ, so CDN cache is harder than `GET /users/1` | Redis, Apollo cache, persisted queries |
| Heavy queries | Deep nesting becomes a huge join | Depth limits, complexity scores, DataLoader |
| Abuse | A public query can be a DoS | Rate limit, cost analysis, persisted queries |
| More server code | A resolver per field | Federation (split the schema) |

---

## Comparison

| | REST | gRPC | GraphQL |
|--|------|------|---------|
| Protocol | HTTP/1.1 or HTTP/2 | HTTP/2 | HTTP (usually POST) |
| Body | JSON or XML | Protobuf (binary) | JSON query |
| Shape | Fixed endpoints | Methods in a `.proto` | Client picks fields |
| Speed | Fine for public CRUD | Very high | Good if queries are controlled |
| Streaming | Not built in | Built in, both ways | Usually add WebSockets / subscriptions |
| Best for | Public APIs, simple CRUD | Internal microservices | UI-driven flexible reads |
| Weakness | Over/under-fetch | Harder to debug, weak in browsers | Cache, query cost, resolver complexity |

**Memory line:** REST is **simple**. gRPC is **fast**. GraphQL is **flexible**.

---

## gRPC security

Same ideas as HTTPS, plus service identity.

- **TLS** so the bytes are encrypted
- **mTLS** so both sides prove who they are (common inside a mesh)
- **JWT / OAuth** or **API keys** in gRPC **metadata** (the header equivalent)
- RBAC for "this service may call Pay"
- rate limits against floods
- interceptors for logs

If keys leak, rotate them and keep them in a secret store. If someone can sit in the middle, you were missing TLS.

---

## Scaling GraphQL

1. **Federation** — split one giant schema across teams (Apollo Federation style)
2. **Cache** — Redis, CDN, persisted queries for the popular ones
3. **DataLoader** — batch field lookups so you do not query the DB once per item (N+1)
4. **Complexity limits** — max depth, max cost, timeouts
5. **Cursor pagination** — `first: 10, after: cursor` instead of dumping a table
6. **Many servers behind a load balancer** — GraphQL processes are still stateless HTTP if you design them that way

---

## How to choose in an interview

```text
Public mobile/web CRUD, many third parties     → REST
Java service calling another Java/Go service   → gRPC
Screen needs a custom slice of a big graph     → GraphQL
Chat / ticks                                   → WebSocket (or gRPC stream inside)
```

You can mix them: GraphQL or REST at the edge, gRPC between services.

---

## Interview lines

- REST over-fetches and chatters. gRPC fixes speed inside the system. GraphQL fixes "give me only these fields."
- gRPC = HTTP/2 + Protobuf + generated stubs.
- GraphQL caching and deep queries are the traps. Say federation, DataLoader, and query limits.
- Do not put raw gRPC on a public website just to sound modern.

---

## Interview Q&A

Read the question. Answer out loud. Then open the answer.

### 1. How would you compare REST, gRPC, and GraphQL?

<details>
<summary>Show Answer</summary>

**Answer:**

- **REST** — standard public HTTP, JSON, fixed URLs. Simple. Can over-fetch.
- **gRPC** — binary Protobuf on HTTP/2, strong contracts, streaming. Best between microservices. Harder to debug and to call from a browser.
- **GraphQL** — one endpoint, client chooses fields. Best for UI flexibility. Caching and expensive queries need extra work.

</details>

### 2. When would you use gRPC over REST?

<details>
<summary>Show Answer</summary>

**Answer:**

When calls are internal and latency or bandwidth matters: microservice mesh, streaming, IoT, many languages sharing a `.proto`.

Not when the API is a public browser API or when humans must debug JSON in curl every day.

</details>

### 3. What are the trade-offs of GraphQL at large scale?

<details>
<summary>Show Answer</summary>

**Answer:**

You gain flexible queries and fewer round trips.

You pay with harder HTTP caching, risk of huge nested queries, DoS if queries are unlimited, and more resolver code.

Mitigate with Redis/CDN/persisted queries, DataLoader, complexity limits, and federation.

</details>

### 4. How does gRPC handle authentication and security?

<details>
<summary>Show Answer</summary>

**Answer:**

TLS for encryption, mTLS so both services authenticate, JWT/OAuth or API keys in metadata, RBAC, rate limits, and interceptors for audit logs. Store secrets in a vault and rotate them.

</details>

### 5. How do you scale GraphQL APIs?

<details>
<summary>Show Answer</summary>

**Answer:**

Federate the schema, cache hot queries, batch DB access with DataLoader, cap query depth and cost, use cursor pagination, and run many instances behind a load balancer. Watch query timing in your metrics.

</details>

**Q&A recap:** REST simple, gRPC fast and internal, GraphQL flexible for clients. Always name the trade-off.

Back to [00_Section_Overview.md](00_Section_Overview.md). Next course folder: [04_Architectural_Patterns](../04_Architectural_Patterns/README.md).
