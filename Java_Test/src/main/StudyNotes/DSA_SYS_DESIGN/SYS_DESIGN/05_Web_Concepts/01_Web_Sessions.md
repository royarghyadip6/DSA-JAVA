# 1. Web Sessions — Managing State

## Simple idea

HTTP **forgets** you after every response. A shopping site still needs to know:

- you are logged in
- what is in the cart
- your language or theme

That memory is **state**. Because HTTP does not keep it, the app must send it again on every call, or look it up with an ID.

```text
Request 1: login          → server learns who you are
Request 2: view cart      → server does NOT remember request 1
                           unless you send a cookie or token
```

---

## Why HTTP is stateless

Each request is a new transaction. The server does not keep a memory of the last one.

That makes servers simpler: any machine can answer if the request carries everything it needs.

The cost is login, carts, and "my account." Something extra must carry identity.

---

## Ways to keep state

| Way | Where the data lives | Everyday picture |
|-----|----------------------|------------------|
| **Cookie** | Browser, sent automatically | Small note attached to every visit |
| **Server session** | Server memory, Redis, or DB. Cookie holds only the ID | Coat-check ticket. The coat stays inside |
| **JWT** | Client (cookie or storage). Token holds the claims | A signed pass. The door reads the pass |
| **localStorage / sessionStorage** | Browser only. JavaScript must send it | A notebook in the browser. Not sent by itself |

`localStorage` stays after the tab closes. `sessionStorage` dies when the tab closes. Neither is sent like a cookie. Apps often put a JWT there and add an `Authorization` header. That is easy for XSS to steal. An **HttpOnly cookie** cannot be read by JavaScript.

---

## Session-based login

```text
1. User sends email + password
2. Server checks them and creates a session
3. Server stores { sessionId → userId, cart, roles }
4. Server sends Set-Cookie: sessionId=abc
5. Browser sends that cookie on later calls
6. Server looks up abc and knows the user
```

The browser never holds the cart or the role list. It holds a **random ID**.

If the server deletes the session, the user is logged out immediately. That is easy revocation.

---

## Token-based login (JWT, OAuth)

A **JWT** is a signed package: who you are, when it expires, maybe roles.

```text
1. Login succeeds
2. Auth service signs a JWT
3. Client sends Authorization: Bearer <jwt>
4. Any service checks the signature
5. No session table is required
```

OAuth / OpenID Connect is the login **flow** (who issues the token). JWT is often the **token format**.

Good when:

- many API servers or microservices must trust the same login
- you do not want a session lookup on every call
- clients are mobile apps and partner APIs

Hard part: you cannot easily "delete" a JWT that is already on the phone. It stays valid until it expires, unless you add a blocklist (which is session storage again). Use **short-lived access tokens** and **refresh tokens**.

---

## Sessions vs tokens

| | Server session | JWT |
|--|----------------|-----|
| Storage | Server (or Redis) | Client |
| Scale | Need shared session store or sticky routing | Any server can verify the signature |
| Logout now | Delete the session | Wait for expiry, or keep a deny-list |
| Lookup | Extra read of session store | No DB read if you only check the signature |
| Data exposure | Session payload stays on the server | Claims are visible to anyone who has the token (it is signed, not secret) |
| Typical fit | Classic website | APIs, mobile, many services |

"JWT is less secure" is only true when the token is stored badly (for example `localStorage` plus XSS). A random session ID in an HttpOnly cookie is a strong default for a browser app.

---

## Cookie flags you must say

| Flag | What it does |
|------|----------------|
| **Secure** | Cookie travels only on HTTPS |
| **HttpOnly** | JavaScript cannot read it. Helps against XSS theft |
| **SameSite=Lax or Strict** | Browser will not send the cookie on most cross-site requests. Helps against CSRF |

---

## Session hijacking

An attacker steals the session ID and becomes you.

Stops:

- HTTPS so the cookie is not readable on the wire
- Secure + HttpOnly + SameSite
- new session ID right after login (session fixation)
- short expiry and re-login for payments or password change

---

## CSRF

**Cross-Site Request Forgery:** you are logged in to the bank. A bad page makes your browser send "transfer money" **with your cookie**, because the browser attaches cookies automatically.

The bank sees a real cookie and may trust it.

Stops:

- CSRF token that the bad site cannot read
- SameSite cookies
- re-enter password for dangerous actions
- do not trust a cookie alone for a state-changing call

CORS is a different control. It stops a bad page from **reading** your API response. It does not, by itself, stop the browser from **sending** a cookie.

---

## Scaling sessions

```text
User → Load balancer → Server A   (session lives only on A)
Next request → Server B           (B has no session → looks logged out)
```

Three fixes:

| Fix | Idea | Cost |
|-----|------|------|
| **Sticky sessions** | LB always sends you to server A | A dies → you lose the session. Uneven load |
| **Shared store** | All servers read Redis / Memcached / DB | Extra hop. Redis must be highly available |
| **JWT** | No server session | Harder instant logout |

Large sites mix these: short tokens, refresh tokens, Redis for some session data, SSO so one login works across many apps.

---

## Microservices

Do not make every service store its own login session.

```text
User → API Gateway → checks JWT
                   → Order Service trusts the token
                   → Payment Service trusts the token
```

A central issuer (OAuth / OpenID Connect) signs tokens. Services validate them. Refresh tokens renew access without a full login every few minutes.

---

## Interview lines

- HTTP is stateless. Cookies, server sessions, or tokens add memory.
- Session = ID in the cookie, data on the server. JWT = claims inside a signed token.
- Sticky sessions are a quick fix. Redis or JWT scales cleaner.
- Hijacking: steal the ID. CSRF: trick the browser into sending the cookie.
- Secure, HttpOnly, SameSite.

---

## Interview Q&A

Read the question. Answer out loud. Then open the answer.

### 1. Why is HTTP considered stateless?

<details>
<summary>Show Answer</summary>

**Answer:**

Each request is independent. The server does not remember the previous one. That simplifies servers, but login and carts need cookies, sessions, or tokens.

</details>

### 2. How do web apps maintain state anyway?

<details>
<summary>Show Answer</summary>

**Answer:**

- **Cookies** — small data the browser sends back
- **Server session** — real data on the server, cookie holds the session ID
- **JWT** — signed token carries the claims
- **localStorage / sessionStorage** — browser storage that JavaScript must attach itself

</details>

### 3. Server-side sessions vs client-side tokens?

<details>
<summary>Show Answer</summary>

**Answer:**

| | Session | JWT |
|--|---------|-----|
| Data | On the server | On the client |
| Scale | Shared store or sticky LB | Stateless verification |
| Logout | Delete session | Hard until expiry |
| Lookup | Session read | Signature check |
| Fit | Traditional websites | APIs and microservices |

Protect the token. A JWT in `localStorage` is stealable by XSS.

</details>

### 4. When would you use JWT instead of sessions?

<details>
<summary>Show Answer</summary>

**Answer:**

When many services must accept the same login, you want to skip a session database, or clients are mobile/partners (often via OAuth).

Do not pick JWT when you must revoke access instantly, unless you add a short lifetime plus a blocklist or refresh-token store.

</details>

### 5. How do cookies and sessions work together?

<details>
<summary>Show Answer</summary>

**Answer:**

Login creates a server session (user id, and so on). The server sends only the session ID in a cookie. Later requests bring that cookie back. The server loads the session. If it is expired or deleted, the user must log in again.

</details>

### 6. What is session hijacking, and how do you prevent it?

<details>
<summary>Show Answer</summary>

**Answer:**

The attacker uses a stolen session ID as if they were you.

Prevent it with HTTPS, Secure / HttpOnly / SameSite cookies, a new session ID after login, and short expiry plus re-auth for sensitive actions.

</details>

### 7. How does CSRF exploit sessions?

<details>
<summary>Show Answer</summary>

**Answer:**

The victim is logged in. Another site triggers a request to your site. The browser attaches the session cookie. Your server may treat it as the user.

Mitigate with CSRF tokens, SameSite=Lax or Strict, and re-authentication for payments or password changes.

</details>

### 8. Why Secure, HttpOnly, and SameSite?

<details>
<summary>Show Answer</summary>

**Answer:**

- **Secure** — HTTPS only
- **HttpOnly** — JavaScript cannot read the cookie (XSS)
- **SameSite** — cookie is not sent on most cross-site calls (CSRF)

</details>

### 9. How do you scale session management?

<details>
<summary>Show Answer</summary>

**Answer:**

Sticky sessions, a shared store such as Redis or Memcached, or stateless JWTs so any server can authenticate without shared session memory.

</details>

### 10. How does a load balancer handle sessions?

<details>
<summary>Show Answer</summary>

**Answer:**

If the session is only in one server's RAM, the next server will not know the user.

Use stickiness, put sessions in Redis (or a database), or stop storing server sessions and use JWTs.

</details>

### 11. How do microservices share authentication?

<details>
<summary>Show Answer</summary>

**Answer:**

One issuer (OAuth / OpenID Connect) signs JWTs. Each service checks the signature. It does not call the login service on every request. Refresh tokens renew access. The gateway can reject bad tokens before they reach services.

</details>

### 12. How do large apps handle sessions?

<details>
<summary>Show Answer</summary>

**Answer:**

They combine short-lived tokens, refresh tokens, SSO, and distributed stores such as Redis. They avoid keeping login state only in one machine's memory.

</details>

**Q&A recap:** stateless HTTP, cookie + server session vs JWT, hijack vs CSRF, sticky vs Redis vs token.

## Interview Q&A — 6 to 8 years

### S1. You put the session in a JWT so any server can serve the user. What can you no longer do?

<details>
<summary>Show Answer</summary>

**Answer:**

I cannot easily revoke it. Logout, a password change, and a stolen laptop keep working until `exp`. I keep JWTs short (minutes) and use a refresh token I can revoke in a store. The access token stays small and is not the place I hide roles that change every minute. If I need instant revoke, a server-side session id is the honest design.

**Corner cases:** A huge JWT in a header blows past proxy limits and gets logged. Putting it in `localStorage` makes any XSS a full account takeover. An `httpOnly` `Secure` `SameSite` cookie is safer for a browser. Clock skew makes a token look expired or not-yet-valid. I allow a small skew. Two tabs refreshing at once rotate the refresh token twice and log the user out. Rotation must tolerate a short overlap. A kid's "none" algorithm token must be rejected.

</details>

### S2. How does CSRF show up, and when does a bearer token not save you?

<details>
<summary>Show Answer</summary>

**Answer:**

A cookie is sent automatically on a cross-site request. Another site can trigger a POST the browser will authenticate. `SameSite=Lax` or `Strict`, plus a CSRF token on state-changing requests, closes that. A bearer token in an `Authorization` header is not sent automatically, so classic CSRF does not apply. If I store that token where script can read it, XSS is the worse bug.

**Corner cases:** `SameSite=Lax` still sends the cookie on a top-level GET. A state change on GET is CSRF-able. Subdomains can set cookies on the parent if I am careless, and one weak subdomain steals the session. A reverse proxy that strips `Origin` breaks my check. Mobile apps do not use cookies the same way. I do not bolt a CSRF token on a pure bearer API and call it done, and I do not skip it on a cookie session.

</details>

### S3. Sticky sessions fixed a bug and created an outage. What happened?

<details>
<summary>Show Answer</summary>

**Answer:**

Session memory lived on one app node. That node died, or a deploy drained it, and every user pinned to it had to log in again. The hot user (or a NAT) made one node much hotter than the rest. I move the session to a shared store with a TTL, or I use a short token. The app node can die without taking logins with it.

**Corner cases:** The shared store is one Redis with no replica. I moved the single point. Session writes on every request hammer Redis. I touch the TTL only sometimes. A failover of Redis that loses data logs everyone out. I decide if that is acceptable. Two data centers with two session stores mean a user pinned to a region. I must not bounce them every request. A session key that includes a role copied at login stays stale after an admin demotes the user.

</details>

### S4. How do you run login across web, mobile, and a partner API without three security models?

<details>
<summary>Show Answer</summary>

**Answer:**

One identity provider. The browser gets a cookie session or a short cookie-backed token. The mobile app gets a refresh token in secure storage and a short access token. The partner gets a separate client credential with its own rate limit and scopes, not a user's password. Each client can be revoked on its own. Scopes are the authorization, not "logged in means admin."

**Corner cases:** A partner token that never expires and is checked into a repo. A refresh token copied onto a second phone unless I detect reuse and revoke the family. SSO logout that kills the website and leaves the mobile refresh token alive. I define what single logout means. Clock and audience (`aud`) checks so a token for the mobile app is not accepted at the admin API.

</details>

**Senior recap:** short-lived tokens plus a revocable refresh, cookies need CSRF defense, sticky sessions are a hidden single node, and each client has its own credential.

Next: [02_Serialization.md](02_Serialization.md)
