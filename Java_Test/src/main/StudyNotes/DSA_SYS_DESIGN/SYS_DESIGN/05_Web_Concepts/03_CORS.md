# 3. CORS and Web Security

## Simple idea

A page from `https://shop.com` must not silently read your bank data at `https://bank.com`.

The browser enforces the **Same-Origin Policy**. **CORS** is how an API **opts in** and says, "this other website may call me."

```text
shop.com page  →  wants JSON from api.shop.com
Browser        →  asks api.shop.com: "May shop.com read your response?"
API            →  Access-Control-Allow-Origin: https://shop.com
Browser        →  only then shows the JSON to the page's JavaScript
```

CORS is **enforced by the browser**, not by curl, Postman, or a mobile app. Those clients do not apply it. The server must still authenticate the caller.

---

## Same-origin policy

An **origin** is three parts together:

```text
scheme + host + port
https://shop.com:443
```

These are **different** origins:

| URL | Why it differs from `https://shop.com` |
|-----|----------------------------------------|
| `http://shop.com` | different scheme |
| `https://api.shop.com` | different host |
| `https://shop.com:8443` | different port |

A script on one origin cannot freely read another origin's responses. That blocks a malicious page from using your logged-in session to **read** private APIs.

It exists to protect cookies, tokens, and personal data, and it reduces some cross-site attacks. It is not a complete CSRF fix by itself.

---

## What CORS adds

CORS is a set of **response headers**. The browser looks at them and decides whether JavaScript may see the response.

The main one:

```text
Access-Control-Allow-Origin: https://shop.com
```

Other headers:

| Header | Meaning |
|--------|---------|
| `Access-Control-Allow-Methods` | GET, POST, PUT, DELETE, … |
| `Access-Control-Allow-Headers` | Which request headers are allowed (`Authorization`, `Content-Type`, …) |
| `Access-Control-Allow-Credentials` | May the browser send cookies / auth on this cross-origin call? |
| `Access-Control-Max-Age` | How long the browser may cache the preflight answer |

The **server** decides. The browser obeys. If the header is missing, the browser hides the response from JavaScript. The request may still have reached your server.

---

## Simple request vs preflight

Some calls are "simple." The browser sends them directly.

A request is treated as simple only in limited cases:

- method is **GET, HEAD, or POST**
- no custom headers such as `Authorization`
- content type is a simple one, such as `text/plain` or form data — **not** `application/json`

Anything richer gets a **preflight**:

```text
1. Browser sends OPTIONS
2. Server answers with Allow-Origin, Allow-Methods, Allow-Headers
3. If that matches, the browser sends the real PUT / POST / DELETE
```

Preflight is required for:

- PUT, DELETE, PATCH, and other non-simple methods
- `Authorization` or other custom headers
- `Content-Type: application/json`

That is why a normal JSON REST call from a browser almost always does OPTIONS first.

---

## How you configure it

Return the headers from the API, a filter, Nginx, or the gateway.

Allow one site, not the world, when the data is private:

```text
Access-Control-Allow-Origin: https://shop.com
Access-Control-Allow-Methods: GET, POST, PUT, DELETE
Access-Control-Allow-Headers: Content-Type, Authorization
```

Spring, Express, Django, and Nginx can all set these. The idea is the same. The syntax is framework-specific.

GraphQL uses HTTP too. Queries and mutations with JSON and auth headers also trigger preflight. Configure CORS on that endpoint the same way.

---

## Dangerous settings

| Mistake | Why it hurts |
|---------|----------------|
| `Access-Control-Allow-Origin: *` on a private API | Any website's JavaScript may read the response |
| `Allow-Origin: *` together with credentials | Browsers **reject** this combination. The real bug is reflecting the caller's origin and also allowing credentials |
| Listing every header, including internal ones | You widen what a browser page can send |
| CORS instead of authentication | CORS only controls browsers. Stolen tokens still work in curl |

Safer:

- allow-list exact origins (`https://shop.com`, not `*`)
- turn credentials on only for those origins
- vary the policy by route if public GETs and private APIs differ
- still check JWT / session on the server

---

## Ways to avoid browser CORS

CORS is not the only shape.

**Reverse proxy (best everyday option)**

The page and the API look like one origin. Nginx (or the SPA host) forwards `/api` to the real backend.

```text
Browser  →  https://shop.com/api/orders
Nginx    →  http://order-service:8080/orders
```

The browser never sees a second origin, so it does not apply cross-origin rules. The backend should still not be wide open on the internet if the proxy is the intended door.

**API gateway**

One place sets allowed origins for many services (AWS API Gateway, Kong, and similar). You do not copy CORS config into every Spring app and then drift.

**JSONP**

An old trick: load a `<script>` from the other site instead of using `fetch`. It is obsolete and unsafe. Know the name so you can say "do not use it."

---

## What proxies and gateways improve

- the browser talks to one origin, or one gateway applies one policy
- fewer mistaken `*` configs across services
- you can cache preflight (`Access-Control-Max-Age`) so OPTIONS is not constant
- the gateway still does auth, rate limit, and routing

A proxy does not remove the need for authentication.

---

## Interview lines

- Origin = scheme + host + port. Same-origin policy stops a page from reading another origin.
- CORS is the server opt-in, enforced by the browser.
- JSON, `Authorization`, PUT, and DELETE cause an OPTIONS preflight.
- Never use `*` for a credentialed or private API. Allow-list origins.
- Same-origin via a reverse proxy avoids CORS. The API still checks identity.

---

## Interview Q&A

### 1. What is the Same-Origin Policy, and why does it exist?

<details>
<summary>Show Answer</summary>

**Answer:**

The browser treats two URLs as the same origin only when scheme, host, and port match. A script on one origin cannot freely read another origin's responses.

It exists so a malicious site cannot use the victim's cookies to read private data. It is a browser rule, not a server firewall.

</details>

### 2. How does CORS enable cross-origin requests?

<details>
<summary>Show Answer</summary>

**Answer:**

The server sends headers such as `Access-Control-Allow-Origin: https://shop.com`. The browser allows the page's JavaScript to see the response only if the origin is allowed.

Without that header, the call may still hit the server, but the browser hides the body from the script.

</details>

### 3. What is a preflight request, and when is it required?

<details>
<summary>Show Answer</summary>

**Answer:**

The browser first sends **OPTIONS** to ask which methods and headers are allowed. Then it sends the real request.

It is required for methods other than simple GET/HEAD/POST, for custom headers like `Authorization`, and for content types such as `application/json`.

</details>

### 4. How do you configure CORS headers on a server?

<details>
<summary>Show Answer</summary>

**Answer:**

The response must include:

- `Access-Control-Allow-Origin` — which site
- `Access-Control-Allow-Methods` — which verbs
- `Access-Control-Allow-Headers` — which request headers

Set them in the app (Spring, Express, Django) or in Nginx / the API gateway. Prefer a specific origin over `*`.

</details>

### 5. What are common CORS security risks?

<details>
<summary>Show Answer</summary>

**Answer:**

- `Allow-Origin: *` lets any website read a response that does not use credentials
- reflecting whatever `Origin` the caller sent, plus `Allow-Credentials: true`, lets hostile sites make authenticated browser calls and read the result
- `*` together with credentials is rejected by browsers, but it is still a misconfiguration
- CORS is not authorization. Check the token or session on every private API

Mitigate with an origin allow-list, credentials only for those origins, and real authentication.

</details>

### 6. What are alternatives to CORS?

<details>
<summary>Show Answer</summary>

**Answer:**

- **Reverse proxy** — browser calls `/api` on the same host; Nginx forwards to the backend
- **API gateway** — one CORS policy for many services
- **JSONP** — old script-tag hack. Do not use it

</details>

### 7. How do API gateways and reverse proxies help?

<details>
<summary>Show Answer</summary>

**Answer:**

A reverse proxy makes the API same-origin, so the browser does not treat it as cross-origin.

A gateway centralizes allowed origins, methods, and headers, and can sit with auth and rate limits. Both reduce copy-pasted CORS config. They do not replace server-side auth.

</details>

**Q&A recap:** SOP protects reads across origins. CORS is an explicit server allow-list. Preflight for JSON and custom headers. Proxy if you want one origin.

## Interview Q&A — 6 to 8 years

### S1. `Access-Control-Allow-Origin: *` plus cookies. What did you actually allow?

<details>
<summary>Show Answer</summary>

**Answer:**

Browsers refuse to send credentials when the allow-origin is `*`. If I reflect the request's `Origin` and also send `Allow-Credentials: true`, I have allowed any site that can trick a user to call my API with the user's cookie. I allow-list exact origins. I do not reflect an arbitrary origin. A server-to-server call does not use CORS at all. CORS is a browser rule.

**Corner cases:** A null origin from a sandboxed iframe or a `file://` page. Reflecting `null` is a common bug. Regexes that match `https://evil-mybank.com` because they search for `mybank.com`. Preflight caches (`Access-Control-Max-Age`) keep a bad policy for hours. Subdomains of a user-generated site (`foo.myapp.com`) must not inherit the parent allow-list. Different browsers treat wildcard headers differently. I test the real browser, not only curl. Curl never enforces CORS, so "it works in curl" proves nothing.

</details>

### S2. The browser sends an OPTIONS that your gateway does not route. What does the user see?

<details>
<summary>Show Answer</summary>

**Answer:**

The real POST never runs. The console shows a CORS error, which hides the actual status. I route OPTIONS to a handler that returns the allow-list, the allowed methods, and the allowed headers (including `Authorization` and `Idempotency-Key` if I use them). I answer OPTIONS without requiring auth, or the preflight itself fails.

**Corner cases:** A gateway strips headers not on the allow-list, so the browser omits `Idempotency-Key` and the POST creates duplicates. `Content-Type: application/json` triggers a preflight. A "simple" form post does not, which is why CSRF still exists on cookie forms. Redirects during a preflight fail the check. A CDN caches an OPTIONS response from one origin and serves it to another unless `Vary: Origin` is set.

</details>

### S3. When do you put a same-origin proxy in front instead of opening CORS?

<details>
<summary>Show Answer</summary>

**Answer:**

When the browser should see one origin: my site and my API both on `app.example.com`, and the proxy forwards `/api` to the service. Then cookies are first-party and CORS is unnecessary. I do this for my own UI. I do not do it so a third-party site can borrow my user's session. Public APIs for other companies still need a real allow-list and tokens, not my proxy.

**Corner cases:** The proxy forwards `Host` or `X-Forwarded-Host` in a way that breaks routing. It becomes a way to reach internal-only paths I forgot to block (`/api/admin`). Timeouts differ from the browser's, so the proxy returns 504 and the UI blames CORS. WebSocket upgrade through the proxy needs its own config. A service worker cache mixes API responses across users if the cache key ignores cookies.

</details>

### S4. How do you explain that CORS is not an authorization system?

<details>
<summary>Show Answer</summary>

**Answer:**

CORS stops a random website's JavaScript from reading the response in the user's browser. It does not stop that website's server, curl, or a mobile app from calling me. Authorization is still authentication plus a check on every request. If an endpoint is safe only because "CORS will hide it," it is public.

**Corner cases:** An internal admin tool "protected" by CORS and no login. Error messages that leak whether an email exists, readable by any allowed origin. A too-wide allow-list (every preview deploy) plus a powerful cookie. I tie preview domains to short-lived tokens. CORS headers on a 401 still matter, or the browser hides the 401 and the app cannot refresh a token.

</details>

**Senior recap:** never reflect arbitrary origins with credentials, preflight is a real route, same-origin proxy is for your own UI, and CORS is not login.

Back to [00_Section_Overview.md](00_Section_Overview.md). Next course folder: [06_Scalability](../06_Scalability/README.md).
