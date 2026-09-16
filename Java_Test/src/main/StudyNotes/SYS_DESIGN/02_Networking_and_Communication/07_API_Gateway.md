# 7. What is an API Gateway?

## Simple idea

In microservices, you do **not** want the mobile app to know 15 internal URLs.

An **API Gateway** is **one front door for APIs**.

```text
Mobile / Web / Partner
          |
          v
     API Gateway     ←  auth, rate limit, route, cache, log
          |
    +-----+-----+-----+
    v     v     v     v
  User  Order Pay  Catalog   (many Spring Boot services)
```

It is a **reverse proxy with extra brains** for APIs.

---

## Why not call services directly?

If the app talks to each service:

- Security is copied 15 times
- Every service is exposed to the internet
- Versioning and rate limits are messy
- Mobile must change whenever you split a service

The gateway **hides** that mess.

---

## How it works

1. Client sends one request to the gateway (`https://api.shop.com/orders`)
2. Gateway may: check JWT, apply rate limit, rewrite path, add headers
3. Gateway forwards to the right service
4. Gateway can cache, merge, or reshape the response
5. Client gets one answer

Key jobs:

| Job | Meaning |
|-----|---------|
| Routing | `/orders` → Order Service |
| AuthN / AuthZ | Who are you? Are you allowed? |
| Rate limiting | Max N calls per minute |
| Caching | Repeat GET served from cache |
| Transformation | JSON ↔ XML, REST ↔ gRPC at the edge |
| Logging / metrics | One place to watch API health |
| Load balancing | Spread to many instances of a service |
| TLS | HTTPS at the door |

---

## API Gateway vs Load Balancer

This is asked in almost every interview.

| | Load balancer | API Gateway |
|--|---------------|-------------|
| Main job | Spread TCP/HTTP traffic across **same kind** of servers | Manage **APIs**: route, auth, throttle, transform |
| Layer | L4 (IP/port) and/or L7 | Mostly **L7** (HTTP APIs) |
| Knows about | Healthy instances | URL paths, API keys, JWT, quotas |
| Example | AWS ALB / NLB, HAProxy | Kong, AWS API Gateway, Apigee |

A large design often uses **both**:

```text
Internet → Load balancer → several API Gateway nodes → microservices
```

The LB keeps gateways highly available. The gateway understands APIs.

---

## Popular products (from the course)

**Open source:** Kong, NGINX, Traefik

**Cloud:** AWS API Gateway, Google Apigee, Azure API Management

You do not need all features of all of them. Name 2–3 and the idea.

---

## When to use / when to skip

**Use when**

- Microservices
- Many clients (web, mobile, IoT, partners)
- You need auth, quotas, monitoring at the edge

**Skip when**

- Small monolith, few APIs
- Internal-only, low traffic, no public surface

Do not put a heavy gateway in front of a hello-world app.

---

## Auth in one minute

- **Authentication** = prove identity (login)
- **Authorization** = prove permission (can this user refund?)

Common methods at the gateway: **API keys**, **OAuth 2.0 + JWT**, **mTLS**, enterprise **SAML / LDAP**.

Failed token → **401/403**, request never reaches Order Service.

---

## Rate limit vs throttle (simple)

- **Rate limit** — hard cap: 100 req/min, then **reject** (429)
- **Throttle** — slow the extra requests instead of killing them at once
- **Burst** — allow a short spike

Algorithms you can name: **token bucket**, **leaky bucket**, **fixed / sliding window**.

---

## Caching at the gateway

- Cache **GET** product details, not **POST** payment
- TTL so data does not stay stale forever
- Redis / in-memory / CDN edge cache

---

## DDoS at the gateway

Rate limit, IP allow/deny, WAF, bot checks, TLS termination — same family as reverse proxy, but aimed at **API abuse**.

---

## Design for millions of users (sketch)

```text
Clients
   →  LB
   →  many gateway instances (auto-scale, multi-AZ / multi-region)
   →  cache
   →  services
```

Watch latency: every extra hop costs time. Cache and keep gateway logic thin.

---

## Challenges

| Problem | Fix |
|---------|-----|
| Gateway is a single point of failure | Many instances + LB + failover |
| Extra latency | Cache, skip heavy plugins |
| Config complexity | Kong / Apigee style control plane |
| Becomes a bottleneck | Horizontal scale |
| API versions | `/v1`, `/v2` |

---

## Interview lines

- API Gateway = one API door: route + security + quota + observability.
- LB spreads clones. Gateway understands **API meaning**.
- Use it for microservices and public APIs. Skip it for a tiny monolith.
- Always mention failure and latency of the gateway itself.

---

## Interview Q&A

Read the question. Answer out loud. Then open the answer.

### 1. What is an API Gateway, and why is it used?

<details>
<summary>Show Answer</summary>

**Answer:**

An **API Gateway** sits between clients and backend services.

It routes requests, checks **auth**, **rate limits**, **caches**, and can **transform** bodies/headers.

Why: one door for API traffic, better security, less junk on services, easier to scale the edge.

</details>

### 2. How does an API Gateway differ from a Load Balancer?

<details>
<summary>Show Answer</summary>

**Answer:**

| Load balancer | API Gateway |
|---------------|-------------|
| Spread traffic across **similar** servers | Understands **APIs** |
| L4 and/or L7 | Mainly **L7** |
| Health + distribution | Auth, quota, cache, compose, transform |

You often deploy **both**: LB in front of several gateway instances.

</details>

### 3. What are the key benefits of using an API Gateway?

<details>
<summary>Show Answer</summary>

**Answer:**

- **Security** — authn / authz at the door
- **Rate limit / throttle** — stop abuse
- **Load balancing** — fan-out to instances
- **Caching** — fewer backend calls
- **Transformation** — JSON/XML, header rewrites
- **Monitoring / logging** — one place for API metrics

</details>

### 4. How does an API Gateway handle authentication and authorization?

<details>
<summary>Show Answer</summary>

**Answer:**

- **Authentication** = who are you?
- **Authorization** = what may you do?

Methods: **API keys**, **OAuth 2.0 + JWT**, **mTLS**, enterprise **LDAP / SAML**.

Gateway checks token **before** forwarding. Bad token → stop (401/403). Services stay simpler.

</details>

### 5. Explain rate limiting and throttling in API Gateways.

<details>
<summary>Show Answer</summary>

**Answer:**

- **Rate limiting** — hard cap, then **reject** (HTTP 429). Example: 100 req/min.
- **Throttling** — extra calls are **slowed**, not always dropped.
- **Burst** — allow a short spike.

Algorithms: **token bucket**, **leaky bucket**, **fixed window**, **sliding window**.

</details>

### 6. What caching strategies can be implemented in an API Gateway?

<details>
<summary>Show Answer</summary>

**Answer:**

- In-memory / **Redis** / Memcached
- Full **response cache** for repeated GETs
- **CDN / edge** cache
- **Per-route** rules (cache GET `/products/1`, never cache POST `/pay`)
- **TTL** so data goes stale on purpose, not forever

Example: product details cached 5 minutes.

</details>

### 7. How does an API Gateway improve security against DDoS attacks?

<details>
<summary>Show Answer</summary>

**Answer:**

Rate limit / throttle, IP allow/deny, **WAF**, bot checks / CAPTCHA, **TLS termination**.

A flood hits the gateway fabric first, not every microservice.

</details>

### 8. When should you use an API Gateway in a microservices architecture?

<details>
<summary>Show Answer</summary>

**Answer:**

Use when you need one URL for many services, central auth, quotas, cache, protocol translation, API logs.

**E-commerce:** mobile hits gateway; gateway routes to Auth, Catalog, Payment.

Skip for a tiny internal monolith.

</details>

### 9. How would you design an API Gateway for a large-scale system with millions of users?

<details>
<summary>Show Answer</summary>

**Answer:**

```text
Clients → Load balancer → many gateway nodes (auto-scale, multi-AZ / region)
                        → cache
                        → services
```

Also: rate limits, HA/failover, logging/metrics.

</details>

### 10. What challenges might arise when implementing an API Gateway, and how would you address them?

<details>
<summary>Show Answer</summary>

**Answer:**

| Challenge | Fix |
|-----------|-----|
| Single point of failure | Several instances + LB + failover |
| Extra latency | Thin plugins, cache |
| Config sprawl | Kong / Apigee style management |
| Gateway becomes bottleneck | Horizontal scale (K8s) |
| Versioning | `/v1`, `/v2` |

</details>

**Q&A recap:** Gateway vs LB, auth, token bucket, cache GET not POST, DDoS, microservices door, HA, failure modes.

Next: [08_CDN.md](08_CDN.md)
