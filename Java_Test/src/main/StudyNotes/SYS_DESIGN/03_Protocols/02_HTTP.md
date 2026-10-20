# 2. HTTP

## Simple idea

**HTTP** (HyperText Transfer Protocol) is the language of the web.

A client asks for something. A server answers. That is the whole shape.

```text
Browser / app  --HTTP request-->  Server
Browser / app  <--HTTP response-- Server
```

It runs on **TCP** (and modern stacks also use HTTP/2 and HTTP/3).

- **HTTP** default port **80**
- **HTTPS** default port **443**

---

## What HTTP is good at

- text you can read in logs (method, URL, headers)
- **stateless** — the server does not remember the last call by itself
- many **methods** (GET, POST, PUT, PATCH, DELETE)
- pages, images, and JSON APIs use the same idea

---

## Request and response

**Request**

| Part | Example |
|------|---------|
| Method | `GET` |
| URL | `/index.html` |
| Headers | `Host`, `User-Agent`, `Content-Type`, `Authorization` |
| Body | Optional. Used by POST/PUT/PATCH |

**Response**

| Part | Example |
|------|---------|
| Status | `200 OK` |
| Headers | `Content-Type: text/html` |
| Body | HTML, JSON, or empty |

---

## Request-response cycle

```text
1. Client sends the request
2. Server does the work (file, DB, another API)
3. Server sends status + headers + body
4. Browser paints the page, or the app reads the JSON
```

Example:

```text
GET /index.html HTTP/1.1
Host: www.example.com

HTTP/1.1 200 OK
Content-Type: text/html
```

---

## Stateless

HTTP **forgets** you after the response. The next call is a new stranger unless **you** send identity again.

Problems:

- "stay logged in" is not free
- every call must carry what the server needs (token, cookie)

How we fake memory:

| Tool | Where it lives | Simple use |
|------|----------------|------------|
| **Cookie** | Browser; sent automatically | Session id, preferences |
| **Session** | Server map keyed by a session id (often in a cookie) | Classic login |
| **Token (JWT / OAuth)** | Client sends `Authorization` header | APIs, mobile, microservices |

Sessions make the **server** remember. JWTs keep the server closer to stateless: the token itself carries the claims.

---

## Methods

| Method | Job | Example |
|--------|-----|---------|
| **GET** | Read | Load a user |
| **POST** | Create | New order |
| **PUT** | Replace the **whole** resource | Send all profile fields |
| **PATCH** | Change **some** fields | Change only email |
| **DELETE** | Remove | Delete a comment |

**PUT vs PATCH:** PUT is a full overwrite. PATCH is a small edit. If you PUT and forget a field, you can wipe it.

---

## Status codes

| Class | Meaning | Examples |
|-------|---------|----------|
| **1xx** | Still working | 100 Continue |
| **2xx** | Success | **200** OK, **201** Created |
| **3xx** | Go elsewhere / use cache | **301** permanent move, **302** temporary, **304** use cache |
| **4xx** | Client mistake | **400** bad body, **401** not logged in, **403** logged in but not allowed, **404** missing |
| **5xx** | Server broke | **500** bug, **503** overloaded or down |

**401 vs 403:** 401 = "who are you?" 403 = "I know you, and the answer is no."

**301 vs 302**

- **301** — this URL moved forever. Browsers and search engines remember the new one.
- **302** — temporary detour. Do not replace the old bookmark.

---

## HTTPS

HTTPS is HTTP inside **TLS** (people still say SSL).

It gives:

- **confidentiality** — others cannot read the body
- **integrity** — others cannot quietly change it
- **authentication** — the certificate says which server you reached

Use it for login, payments, and any API on the public internet. Port **443**, not 80.

---

## Caching headers

Browsers and CDNs store copies so the origin is not hit every time.

| Header | Meaning |
|--------|---------|
| `Cache-Control: max-age=3600` | Reuse for 1 hour |
| `Cache-Control: no-cache` | Re-check before reuse |
| `ETag` | Fingerprint of this version. If it matches, server can return **304** |
| `Expires` | Old-style expiry date |

**304 Not Modified** = "your copy is still good."

---

## Security risks on plain HTTP

| Risk | What happens | Fix |
|------|----------------|-----|
| MITM | Someone reads or edits traffic | **HTTPS** |
| Tampering | Response is changed | TLS |
| Session hijack | Stolen cookie | `HttpOnly` + `Secure` cookies |
| XSS | Bad script in the page | CSP, encode output |
| Injection | Bad input reaches SQL | Parameterised queries |
| Phishing | Fake site | HTTPS + user care; certs do not stop every fake brand |

---

## Interview lines

- HTTP = request + response on the web. Stateless unless you add cookie, session, or token.
- PUT replaces. PATCH patches.
- 2xx success, 3xx redirect, 4xx your bug, 5xx my bug.
- HTTPS = HTTP + TLS on 443.

---

## Interview Q&A

Read the question. Answer out loud. Then open the answer.

### 1. What is HTTP, and how does it work?

<details>
<summary>Show Answer</summary>

**Answer:**

HTTP is the web's request/response protocol for pages, images, and APIs.

Client sends a request. Server returns a status, headers, and optional body. The browser renders HTML or the app reads JSON.

Example: open `https://www.example.com` → GET → `200` + HTML → page on screen.

</details>

### 2. Why is HTTP considered a stateless protocol?

<details>
<summary>Show Answer</summary>

**Answer:**

The server does not keep a memory of past requests. Each call must stand alone.

That makes login and "my cart" harder. We add cookies, server sessions, or tokens (JWT, OAuth) so the next call can prove who you are.

</details>

### 3. What are the key differences between HTTP and HTTPS?

<details>
<summary>Show Answer</summary>

**Answer:**

| | HTTP | HTTPS |
|--|------|--------|
| Body | Plain text | Encrypted with TLS |
| Port | 80 | 443 |
| Integrity | Can be changed on the wire | Protected |
| Use | Non-sensitive only | Login, bank, APIs |

</details>

### 4. Explain the HTTP request-response cycle with an example.

<details>
<summary>Show Answer</summary>

**Answer:**

1. Client sends `GET /index.html` with `Host: www.example.com`
2. Server finds the page (or runs app code)
3. Server replies `200 OK` plus `Content-Type` and the body
4. Client renders it

</details>

### 5. What are HTTP methods? When would you use PUT vs PATCH?

<details>
<summary>Show Answer</summary>

**Answer:**

GET read, POST create, PUT full replace, PATCH partial change, DELETE remove.

PUT a user when you send the whole profile. PATCH when you change only the email and must not wipe the name.

</details>

### 6. What are HTTP status codes? Give 2xx, 3xx, 4xx, and 5xx examples.

<details>
<summary>Show Answer</summary>

**Answer:**

- 1xx informational
- 2xx success: **200** OK, **201** Created
- 3xx redirect: **301** moved forever, **304** use cache
- 4xx client: **400** bad request, **401** auth required, **404** missing
- 5xx server: **500** crash, **503** unavailable / overloaded

</details>

### 7. How do cookies, sessions, and tokens help maintain state?

<details>
<summary>Show Answer</summary>

**Answer:**

- **Cookie** — small data in the browser, sent on later requests
- **Session** — server stores the user; browser only keeps a session id cookie
- **JWT / OAuth token** — client sends the token (often in a header). Server can validate it without a big session map

</details>

### 8. What is the difference between 301 and 302?

<details>
<summary>Show Answer</summary>

**Answer:**

- **301** permanent. Search engines and browsers update the URL (`http://old` → `https://new`).
- **302** temporary. Keep the old URL as the real one.

</details>

### 9. How does caching work in HTTP, and which headers control it?

<details>
<summary>Show Answer</summary>

**Answer:**

Clients and proxies store responses to cut load and latency.

- `Cache-Control: max-age=3600` — fresh for one hour
- `Cache-Control: no-cache` — revalidate
- `ETag` — version id; match → **304**
- `Expires` — absolute expiry

</details>

### 10. What security risks are associated with HTTP, and how can they be mitigated?

<details>
<summary>Show Answer</summary>

**Answer:**

MITM, tampering, phishing, session hijack.

Fixes: HTTPS, `HttpOnly` and `Secure` cookies, Content-Security-Policy, JWT/OAuth, validate input (stops injection).

</details>

**Q&A recap:** Stateless + cookie/session/token, PUT vs PATCH, status families, 301 vs 302, cache headers, HTTPS.

Next: [03_REST.md](03_REST.md)
