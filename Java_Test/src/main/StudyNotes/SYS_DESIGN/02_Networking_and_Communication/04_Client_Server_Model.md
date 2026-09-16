# 4. Client-Server Model

## Simple idea

**Client** asks. **Server** answers.

That is the model behind websites, email, REST APIs, and most Spring Boot apps.

```text
Client  --request-->  Server
Client  <--response-- Server
```

- **Client** = browser, mobile app, another API
- **Server** = web server, database, mail server
- **Network** = internet, LAN, Wi-Fi, 5G — the wire in the middle

---

## Why it exists

- Clients stay simple (UI)
- Servers hold logic, data, and CPU
- Many clients can share **one** well-managed server farm

Everyday examples:

| Client | Server |
|--------|--------|
| Chrome | Web server (Nginx / Spring) |
| Gmail app | Mail server (SMTP / IMAP) |
| Netflix app | Streaming servers |
| Game app | Game server |
| `JdbcTemplate` | MySQL server |

---

## How they talk

1. Client sends a request (HTTP, SQL, …)
2. Network carries it
3. Server processes it
4. Server sends a response
5. Client uses the response (shows a page, saves JSON, …)

Two styles:

| Style | Meaning | Example |
|-------|---------|---------|
| **Request / response** | Ask, wait, get answer, often done | REST, HTTP |
| **Persistent connection** | Keep the pipe open | WebSockets, some FTP sessions |

---

## HTTP request-response (webpage)

This is the classic walk-through. Learn it in order:

```text
1. User types https://example.com
2. DNS → IP
3. Browser sends HTTP GET
4. Web server may query DB / call other APIs
5. Server returns status (200 OK) + HTML/CSS/JS
6. Browser paints the page
```

Same idea for a Java API:

```text
Mobile app  POST /orders  →  OrderController  →  DB  →  201 Created + JSON
```

---

## Synchronous vs asynchronous

**Synchronous** — client **waits**.

```text
Submit form → wait → "Order placed"
REST call:  orderService.create() blocks until HTTP response
```

Used in classic web apps and most REST APIs.

**Asynchronous** — client **does not wait** (or waits on a callback / event).

```text
Send chat message → keep typing
AJAX loads comments in the background
WebSocket pushes "driver arrived"
```

Used in chat, live scores, IoT, notifications.

---

## Stateless vs stateful servers

**Stateless** — server forgets you after the response. Each request carries what it needs (token, ids).

```text
REST + JWT:  every call is independent
Easy to add more servers (load balancer can send you anywhere)
Easy to cache
```

**Stateful** — server **remembers** the session.

```text
WebSocket chat, multiplayer game, some banking sessions
Must stick the user to the same server (sticky session) or share session store
```

Rule of thumb for interviews:

> Prefer **stateless** APIs for scale. Use **state** only when the product needs a live session.

---

## Caching in this model (preview)

Caching means: **do not fetch the same thing again**.

| Layer | What it stores |
|-------|----------------|
| Browser cache | CSS, JS, images |
| CDN | Same files near the user |
| Server cache | API responses |
| DB cache | Redis / Memcached query results |

---

## Load balancer in this model (preview)

Many clients → **one VIP** (load balancer) → many app servers.

```text
Clients  →  Load balancer  →  Server A
                           →  Server B
                           →  Server C
```

Strategies you will hear: **Round robin**, **Least connections**, **IP hash**.

---

## Security challenges (names)

- **MITM** — someone in the middle reads traffic → use HTTPS
- **DDoS** — flood of requests → rate limit, CDN, WAF
- **SQL injection** — bad input in queries → parameterised queries
- **XSS** — bad script in a page → encode output

---

## Limits of client-server — and the fix

| Limit | Fix |
|-------|-----|
| One server dies = site down | Load balancer + extra servers |
| Too much traffic | Horizontal scale + cache |
| Central data is a target | TLS, auth, firewalls |

A high-traffic design in one breath:

```text
Clients → LB → many stateless Spring apps
            → cache (Redis / CDN)
            → DB primary + read replicas
            → split into microservices when the monolith hurts
```

---

## Interview lines

- Client asks, server answers, network carries.
- Walk HTTP: DNS → GET → process → status + body → render.
- Sync waits; async does not. Stateless scales; stateful remembers.
- Scale with LB + cache + more servers, not one giant machine.

---

## Interview Q&A

Read the question. Answer out loud. Then open the answer.

### 1. What is the client-server model, and how does it work?

<details>
<summary>Show Answer</summary>

**Answer:**

Clients **request** services. Servers **provide** them.

- Client starts the call
- Server listens, works, responds
- Protocols: HTTP, FTP, DNS, SMTP, etc.

**Example:** Browser asks a web server for a page.

</details>

### 2. How does a client communicate with a server?

<details>
<summary>Show Answer</summary>

**Answer:**

Over a **network** (internet, LAN, Wi-Fi) using standard protocols (**HTTP**, **TCP/IP**, **WebSockets**).

Usually **request-response**. Example: Android app → Spring Boot REST API.

</details>

### 3. What are some real-world examples of the client-server model?

<details>
<summary>Show Answer</summary>

**Answer:**

- **Web** — browser ↔ web server
- **Email** — Outlook/Gmail app ↔ SMTP/IMAP/POP3 servers
- **Streaming** — Netflix app ↔ media servers
- **Gaming** — game client ↔ game server
- **Databases** — SQL client / JDBC ↔ MySQL

</details>

### 4. What is the difference between a client and a server?

<details>
<summary>Show Answer</summary>

**Answer:**

| Client | Server |
|--------|--------|
| Asks for data or a job | Does the job and replies |
| User-facing (browser, app) | Holds logic and data |
| Many clients | Fewer, more powerful processes |

One machine can be **both** (your API is a server to mobile, and a client to MySQL).

</details>

### 5. Explain the HTTP request-response cycle with an example.

<details>
<summary>Show Answer</summary>

**Answer:**

1. Browser sends `GET /index.html`
2. Request travels over the network
3. Server fetches files / may query DB
4. Server returns **status + body** (`200 OK` + HTML)
5. Browser **renders**

Visiting `https://example.com` is this cycle for HTML, then extra cycles for CSS, JS, images.

</details>

### 6. What are the key differences between synchronous and asynchronous communication?

<details>
<summary>Show Answer</summary>

**Answer:**

| Sync | Async |
|------|--------|
| Client **waits** for the answer | Client **continues**; answer later / push |
| Typical REST form submit | WebSockets, AJAX background fetch |
| Simple to reason about | Better for chat, live updates, IoT |

</details>

### 7. How does a browser load a webpage? Walk me through the steps.

<details>
<summary>Show Answer</summary>

**Answer:**

1. User enters URL
2. **DNS** → IP
3. **HTTP GET** to that IP (after TLS if HTTPS)
4. Server gathers HTML/CSS/JS/images (maybe DB)
5. **HTTP response** + status
6. Browser **parses and paints**

Mention DNS. Many candidates forget it.

</details>

### 8. What is the difference between stateless and stateful servers?

<details>
<summary>Show Answer</summary>

**Answer:**

| Stateless | Stateful |
|----------|----------|
| No memory of previous calls | Keeps session |
| REST, HTTP APIs + JWT | WebSockets, some bank sessions, games |
| Easy **scale** and **LB** (any server) | Needs sticky sessions or shared session store |
| Easy **cache** | Personalised, continuous feel |

Prefer stateless public APIs unless the product needs a live session.

</details>

### 9. How does caching improve performance in a client-server model?

<details>
<summary>Show Answer</summary>

**Answer:**

Skip repeat work. Faster UX, less CPU and DB.

| Layer | Example |
|-------|---------|
| Browser | CSS, JS, images |
| CDN | Same files near the user |
| Server | Cached API JSON |
| Database | Redis / Memcached query results |

</details>

### 10. How do load balancers work in a client-server architecture?

<details>
<summary>Show Answer</summary>

**Answer:**

LB sits in front of **many** servers so no one box melts.

- **Round robin** — take turns
- **Least connections** — quietest server
- **IP hashing** — same client IP → same server

Improves reliability and scale.

</details>

### 11. What are some security challenges in the client-server model?

<details>
<summary>Show Answer</summary>

**Answer:**

| Attack | Simple meaning | Defence |
|--------|----------------|---------|
| MITM | Spy in the middle | HTTPS / TLS |
| DDoS | Flood | Rate limit, CDN, WAF |
| SQL injection | Poisoned query | Parameterised queries / bind variables |
| XSS | Poisoned script in a page | Encode output, CSP |

</details>

### 12. How does WebSockets differ from traditional request-response communication?

<details>
<summary>Show Answer</summary>

**Answer:**

| HTTP request-response | WebSocket |
|-----------------------|-----------|
| Ask, get, often close | **Stay open**, both sides send anytime |
| Good for pages, REST CRUD | Chat, live prices, multiplayer |
| New request for each poll | No polling needed |

WebSocket is still client-server — the **conversation style** changes.

</details>

### 13. What are some limitations of the client-server model? How can they be addressed?

<details>
<summary>Show Answer</summary>

**Answer:**

| Limit | Fix |
|-------|-----|
| Single server = single point of failure | LB, replicas, failover |
| Scale ceiling | Horizontal scale + cache |
| Central honey-pot for attackers | TLS, auth, firewalls |

</details>

### 14. How would you design a scalable client-server system for a high-traffic application?

<details>
<summary>Show Answer</summary>

**Answer:**

```text
Clients
  → CDN (static)
  → Load balancer
  → Many stateless app servers (Spring Boot)
  → Redis cache
  → DB + read replicas + indexes
  → Split to microservices when the monolith hurts
```

Mention: cache first, scale out, do not start with 50 services.

</details>

**Q&A recap:** Client vs server, HTTP walk, sync/async, state, cache, LB algorithms, security, WebSocket, scale blueprint.

Next: [05_Forward_vs_Reverse_Proxy.md](05_Forward_vs_Reverse_Proxy.md)
