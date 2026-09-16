# 1.1 — HTTP & Web Basics (Security Lens)

> **Module 1 · Topic 1** · Prerequisites
> Baseline: HTTP/1.1 (RFC 9110/9112), HTTP/2, Spring Security 6.x on Boot 3.x
> New to this? Read **In Plain English** below first, then come back to the Version Matrix.

---

## Version Matrix

| Concern | Spring Security 5.x | **Spring Security 6.x (baseline)** | Spring Security 7.x |
|---|---|---|---|
| Servlet API | `javax.servlet.*` | **`jakarta.servlet.*`** | `jakarta.servlet.*` |
| Default `SameSite` on session cookie | not set by Spring; container default | **not set by Spring; set via `server.servlet.session.cookie.same-site`** | same |
| `requiresChannel()` HTTPS redirect | available | **available, lambda DSL** | available |
| Default security headers | HSTS, X-Frame-Options, X-Content-Type-Options, Cache-Control | **same + `X-XSS-Protection: 0`** (the old `1; mode=block` was itself an XSS vector) | same |

---

## Why This Exists

Every Spring Security concept is a reaction to a property of HTTP. If you do not know
*why* HTTP forces a design, you will memorise configuration instead of reasoning about it.

The single most important property is this:

> **HTTP is stateless.** The server keeps no memory of the previous request. Every request
> must carry, by itself, everything needed to identify the caller.

Everything else follows. Sessions exist because HTTP is stateless. Cookies exist to carry
the session identifier. CSRF exists because cookies are sent automatically. JWTs exist to
avoid server-side session storage. `SecurityContextHolder` is cleared at the end of every
request because the next request is, as far as HTTP is concerned, a stranger.

---

## In Plain English

**The one-line version:** The web works by sending a complete, self-contained letter for every
single interaction, and because nobody on the receiving end remembers the previous letter, every
security mechanism you will ever learn exists to prove who you are all over again.

**An analogy.** Imagine a bank whose teller has perfect amnesia. Every time you walk up to the
counter, the teller has genuinely never seen you before. You cannot say "as I mentioned a moment
ago" — there was no moment ago. So the bank gives you a numbered plastic token when you first
prove your identity, and from then on you must place that token on the counter with *every*
request. The teller does not recognise you; the teller recognises the token and looks up which
customer it belongs to in a ledger under the desk.

That token is the session cookie, and the ledger is the session store. Notice what this design
implies. If someone steals your token, they are you, because the token is all the teller checks.
If the bank has three branches and the ledger only exists at branch one, walking into branch two
with your token gets you nowhere. And if a stranger can somehow trick you into putting your token
on the counter while *they* dictate the request, the bank will happily obey — that last one is
exactly the cross-site request forgery problem.

**How it actually works, step by step.**

A request is just structured text sent over a network connection. It starts with a request line
that names a *method* (the verb describing what you want done, such as `GET` to read something or
`POST` to submit something) and a path such as `/api/articles/1`. After that come *headers*, which
are simple `Name: value` pairs carrying metadata — who you are, what content type you are sending,
which browser you are using. Then a blank line, then an optional *body* holding the actual data.
The response has the same shape: a status line with a numeric code, headers, blank line, body.

The method is not just decoration. The HTTP specification classifies `GET`, `HEAD`, `OPTIONS` and
`TRACE` as *safe*, meaning they are only supposed to read things and never change anything. Spring
Security takes that promise literally and skips its forgery checks on those four methods. So if you
write an endpoint that deletes a user but respond to it with `GET`, you have quietly turned off a
protection the framework would otherwise have given you for free.

Cookies are how the browser carries your token. When the server wants to hand you one it sends a
`Set-Cookie: JSESSIONID=8A1F...` header, and the browser then attaches `Cookie: JSESSIONID=8A1F...`
to every later request to that site — automatically, with no code on your part. That automatic
behaviour is convenient and is also the root of a whole attack class, because "automatic" means it
happens even when the request was triggered by a page the attacker wrote. The extra words you see
on a cookie (`HttpOnly`, `Secure`, `SameSite`) are instructions to the browser that narrow when it
is willing to send the cookie, and each one shuts down a specific attack.

Status codes are the server's one-word verdict, and two of them are the ones you will argue about
for the rest of your career. `401` means "I have no idea who you are, please identify yourself".
`403` means "I know precisely who you are, and the answer is still no". The confusing part, which
the sections below cover in full, is that Spring sometimes converts what looks like a `403`
situation into a login redirect instead, on the reasoning that a visitor who never logged in should
be asked to log in rather than simply refused.

Finally, there is the question of where the memory lives. *Stateful* means the server keeps a
session object and the client holds only a meaningless identifier pointing at it. *Stateless* means
the server keeps nothing and the client carries a signed token that contains the facts themselves.
Stateless scales beautifully because any server can serve any request, and it makes cancelling
access hard, because there is nothing to delete.

**Why should a beginner care?** If you do not understand that HTTP forgets everything, you will
write a login screen that appears to work on your laptop and then logs users out at random the
moment it runs on more than one server. If you do not understand that browsers attach cookies
automatically, you will build an application where a malicious web page can transfer money out of
a logged-in user's account without ever seeing their password. And if you do not understand the
difference between `401` and `403`, you will spend days confused about why your JSON API keeps
answering with an HTML login page.

**Words you will meet in this file**

| Term | In plain words |
|---|---|
| Stateless | The server keeps no memory between requests; each request must prove itself from scratch. |
| HTTP method | The verb of a request — `GET` to read, `POST` to submit, `PUT` to replace, `DELETE` to remove. |
| Safe method | A method that is only meant to read data and never change it, so nothing bad happens if it repeats. |
| Idempotent | Doing it twice leaves the system in the same state as doing it once. |
| Header | A `Name: value` line of metadata attached to a request or response. |
| Cookie | A small piece of text the server asks the browser to store and send back on every later request. |
| `JSESSIONID` | The default name of the cookie holding the session identifier — the numbered token from the analogy. |
| Session | A bundle of data the server stores about one logged-in user, looked up by the identifier in the cookie. |
| Base64 | A way of rewriting bytes as safe text. It is not secret in any way; anyone can reverse it instantly. |
| TLS / HTTPS | Encryption of the network connection itself, so nobody in between can read or alter the traffic. |
| CSRF | An attack where a page you did not write makes your browser send a request you did not intend. |
| XSS | An attack where the attacker's JavaScript runs inside your page and can act as you. |
| Servlet filter | A piece of code that sees every request before your application code does and may block it. |
| `SecurityContextHolder` | The place Spring parks "who is making this request" for the duration of one request. |
| `CsrfFilter` | The filter that checks the anti-forgery token, but only on the state-changing methods. |
| `ExceptionTranslationFilter` | The filter that turns a security failure into a `401`, a `403`, or a redirect to the login page. |

**If you remember only one thing:** HTTP has no memory, so every request must carry its own proof
of identity — and everything Spring Security does is a consequence of that one fact.

---

## Core Concepts

### 1. HTTP Methods — Safety and Idempotency

**In simple terms:** The verb you choose for an endpoint decides whether Spring Security bothers
protecting it, so using `GET` for something that changes data silently switches that protection off.

These two properties are *not* trivia. Spring Security's CSRF protection keys off them directly.

| Method | Safe (no state change) | Idempotent (repeat = same result) | CSRF-protected by Spring by default |
|---|---|---|---|
| `GET` | Yes | Yes | **No** |
| `HEAD` | Yes | Yes | **No** |
| `OPTIONS` | Yes | Yes | **No** |
| `TRACE` | Yes | Yes | **No** |
| `POST` | No | No | **Yes** |
| `PUT` | No | Yes | **Yes** |
| `PATCH` | No | No | **Yes** |
| `DELETE` | No | Yes | **Yes** |

Spring Security's `CsrfFilter` holds this exact set:

```java
// org.springframework.security.web.csrf.CsrfFilter
private RequestMatcher requireCsrfProtectionMatcher = new DefaultRequiresCsrfMatcher();

private static final class DefaultRequiresCsrfMatcher implements RequestMatcher {
    private final HashSet<String> allowedMethods =
        new HashSet<>(Arrays.asList("GET", "HEAD", "TRACE", "OPTIONS"));

    @Override
    public boolean matches(HttpServletRequest request) {
        return !this.allowedMethods.contains(request.getMethod());
    }
}
```

**The security consequence:** if you write a state-changing endpoint behind `GET`
(`GET /account/delete?id=5`), you have silently opted out of CSRF protection. This is one of
the most common real-world vulnerabilities in legacy Spring apps.

### 2. The Request/Response Lifecycle

**In simple terms:** This is the route every request travels from the browser to your code, and
security sits early on that route so it can turn a request away long before your controller exists.

```
TCP connect → TLS handshake → HTTP request line → headers → blank line → body
                                                                            ↓
                                                                    server processes
                                                                            ↓
       status line ← headers ← blank line ← body ← HTTP response
```

In a Spring Boot app the server-side portion expands to:

```mermaid
flowchart TD
    Client[Client] --> Connector["Tomcat Connector<br/>parses bytes into HttpServletRequest"]
    Connector --> ContainerFilters["Container filter chain"]
    ContainerFilters --> DFP["DelegatingFilterProxy<br/>springSecurityFilterChain"]
    DFP --> FCP["FilterChainProxy"]
    FCP --> SecFilters["Spring Security filters<br/>authn then authz"]
    SecFilters --> DS["DispatcherServlet"]
    DS --> Interceptors["HandlerInterceptors"]
    Interceptors --> Controller["Controller"]
    Controller --> Response["Response written back<br/>through the same chain in reverse"]
```

The critical detail: **Spring Security runs in the servlet filter layer, before
`DispatcherServlet`.** It can reject a request before any Spring MVC machinery exists. That
is why a `@ControllerAdvice` cannot catch an `AccessDeniedException` thrown by
`AuthorizationFilter` — the exception never reaches MVC.

### 3. Headers That Matter For Security

**In simple terms:** A handful of these metadata lines carry your credentials and instruct the
browser on how carefully to handle them, so getting them right is most of your day-to-day security.

#### `Authorization`

```
Authorization: Basic dXNlcjpwYXNzd29yZA==
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

The format is `Authorization: <scheme> <credentials>`. Two schemes dominate:

- **`Basic`** — `base64(username + ":" + password)`. This is **encoding, not encryption**.
  Anyone who sees the header sees the password. Only ever acceptable over TLS, and even then
  it sends the password on every single request, which is a much larger exposure surface
  than a session cookie. Handled by `BasicAuthenticationFilter`.
- **`Bearer`** — "whoever bears this token gets the access". No proof of possession. If the
  token leaks, it is fully usable by the thief until expiry. Handled by
  `BearerTokenAuthenticationFilter` in a resource server.

#### `Cookie` / `Set-Cookie`

```
Set-Cookie: JSESSIONID=8A1F...; Path=/; HttpOnly; Secure; SameSite=Lax
```

The attributes are the security surface, and each one blocks a specific attack:

| Attribute | What it does | Attack it blocks |
|---|---|---|
| `HttpOnly` | JavaScript cannot read the cookie via `document.cookie` | XSS session theft |
| `Secure` | Cookie only sent over HTTPS | Network sniffing / SSL stripping |
| `SameSite=Strict` | Never sent on cross-site requests | CSRF (but breaks inbound links from other sites) |
| `SameSite=Lax` | Sent on top-level cross-site **GET** navigations only | Most CSRF; the browser default since Chrome 80 |
| `SameSite=None` | Always sent cross-site; **requires `Secure`** | Nothing — needed for legitimate third-party contexts |
| `Domain` | Widens the cookie to subdomains | Narrowing it limits subdomain takeover blast radius |
| `Path` | Restricts by path prefix | Weak isolation; not a real boundary |
| `__Host-` prefix | Browser enforces `Secure` + `Path=/` + no `Domain` | Cookie injection from a subdomain |

In Spring Boot:

```properties
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.same-site=lax
server.servlet.session.cookie.name=__Host-SESSION
```

#### Other headers Spring Security writes by default

`HeaderWriterFilter` emits these on every response unless you disable them:

```
Strict-Transport-Security: max-age=31536000 ; includeSubDomains   (HTTPS requests only)
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-XSS-Protection: 0
```

Note `X-XSS-Protection: 0`. Spring Security 6 deliberately **disables** the legacy browser
XSS auditor, because the auditor itself could be abused to selectively suppress scripts on a
page and create vulnerabilities. Modern defence is Content-Security-Policy, which Spring does
*not* enable by default because a correct CSP is application-specific.

### 4. Status Codes — The Ones That Matter

**In simple terms:** These numbers are how the server says yes or no, and the difference between
"I do not know you" and "I know you and you still cannot" is the distinction people get wrong most.

| Code | Meaning | Who produces it in Spring Security |
|---|---|---|
| `200` | OK | Your controller |
| `201` | Created | Your controller |
| `302` | Found (redirect) | `LoginUrlAuthenticationEntryPoint` redirecting to `/login` |
| `400` | Bad Request | Malformed input, missing required parameter |
| **`401`** | **Unauthorized** — *you are not authenticated* (misnamed in the RFC) | `ExceptionTranslationFilter` → `AuthenticationEntryPoint` |
| **`403`** | **Forbidden** — *you are authenticated but not permitted* | `ExceptionTranslationFilter` → `AccessDeniedHandler` |
| `404` | Not Found | Also used deliberately to hide the existence of resources from unauthorised users |
| `405` | Method Not Allowed | Dispatcher |
| `419` / `403` | CSRF token missing or invalid | `CsrfFilter` → `AccessDeniedHandler` (Spring uses 403) |
| `429` | Too Many Requests | Your rate limiter |
| `500` | Server Error | Unhandled exception — **never leak stack traces here** |

**The 401 vs 403 rule, stated precisely:**

- **401** = "I do not know who you are. Authenticate and try again." The response **must**
  include a `WWW-Authenticate` header per RFC 9110. Spring's `BasicAuthenticationEntryPoint`
  sends `WWW-Authenticate: Basic realm="Realm"`.
- **403** = "I know exactly who you are, and you still cannot do this. Re-authenticating will
  not help."

Spring decides between them in `ExceptionTranslationFilter`:

```java
// Simplified from ExceptionTranslationFilter.handleSpringSecurityException
if (exception instanceof AuthenticationException) {
    sendStartAuthentication(request, response, chain, (AuthenticationException) exception);
}
else if (exception instanceof AccessDeniedException) {
    Authentication authentication = this.securityContextHolderStrategy.getContext().getAuthentication();
    boolean isAnonymous = this.authenticationTrustResolver.isAnonymous(authentication);
    if (isAnonymous || this.authenticationTrustResolver.isRememberMe(authentication)) {
        // anonymous user hit a protected resource -> ask them to log in (401 / redirect)
        sendStartAuthentication(...);
    }
    else {
        // a real, fully authenticated user lacks the authority -> 403
        this.accessDeniedHandler.handle(request, response, (AccessDeniedException) exception);
    }
}
```

**This is the single most misunderstood piece of the framework.** An `AccessDeniedException`
does **not** always become a 403. If the current authentication is anonymous or remember-me,
Spring upgrades it to an authentication challenge instead, because it is reasonable to ask
that user to log in properly first. This is exactly why "my API returns 302 to /login instead
of 401" happens on unauthenticated REST calls.

### 5. Stateless vs Stateful

**In simple terms:** Either the server remembers you and the client holds a ticket, or the server
remembers nothing and the client carries all the facts itself — and you trade easy cancellation
for easy scaling.

| | Stateful (session) | Stateless (token) |
|---|---|---|
| Server stores | Session object keyed by ID | Nothing |
| Client carries | Opaque session ID in a cookie | Self-contained signed token |
| Revocation | Instant — delete the session | Hard — token valid until expiry unless you add a denylist |
| Horizontal scaling | Needs sticky sessions or a shared store (Redis) | Trivially scalable |
| Payload size | Tiny cookie (~32 bytes) | Large header (500 B – 2 KB) on every request |
| CSRF exposure | **Yes** — cookies are sent automatically | **No**, *if* the token is in a header and not a cookie |
| XSS exposure | Low with `HttpOnly` | **High** if stored in `localStorage` |
| Typical use | Server-rendered web apps, BFF | Public APIs, mobile, microservices |

The honest summary an interviewer wants: **stateless buys you scale and costs you
revocation.** Every "JWT logout" design is an attempt to buy revocation back, and every one
of them reintroduces server-side state in some form.

---

## Working Code

A Boot 3 / Security 6 configuration that exercises the HTTP-level concerns above:

```java
package com.example.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

@Configuration
@EnableWebSecurity
public class HttpLevelSecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Force HTTPS for every request; emits a 302 to the https:// URL.
            .requiresChannel(channel -> channel.anyRequest().requiresSecure())

            .authorizeHttpRequests(auth -> auth
                // Method matters: a safe GET is public, the mutating verbs are not.
                .requestMatchers(HttpMethod.GET, "/api/articles/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/articles/**").hasRole("AUTHOR")
                .requestMatchers(HttpMethod.DELETE, "/api/articles/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )

            .headers(headers -> headers
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .preload(true)
                    .maxAgeInSeconds(63072000)          // 2 years, required for preload
                )
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; frame-ancestors 'none'; object-src 'none'")
                )
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
                .xssProtection(xss -> xss
                    .headerValue(XXssProtectionHeaderWriter.HeaderValue.DISABLED)
                )
                .frameOptions(frame -> frame.deny())
            )

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )

            .formLogin(Customizer.withDefaults());

        return http.build();
    }
}
```

Cookie hardening lives in configuration, not in the filter chain:

```yaml
server:
  servlet:
    session:
      cookie:
        name: __Host-SESSION     # browser-enforced: Secure + Path=/ + no Domain
        http-only: true
        secure: true
        same-site: lax
      timeout: 30m
  # We are behind a load balancer that terminates TLS.
  forward-headers-strategy: framework
```

A test that pins the behaviour:

```java
@WebMvcTest
@Import(HttpLevelSecurityConfig.class)
class HttpLevelSecurityConfigTests {

    @Autowired MockMvc mvc;

    @Test
    void anonymousGetIsPublic() throws Exception {
        mvc.perform(get("/api/articles/1").secure(true))
           .andExpect(status().isOk());
    }

    @Test
    void anonymousDeleteIsChallengedNotForbidden() throws Exception {
        // Anonymous + AccessDeniedException -> authentication challenge, NOT 403.
        mvc.perform(delete("/api/articles/1").secure(true).with(csrf()))
           .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "AUTHOR")
    void authenticatedButWrongRoleIsForbidden() throws Exception {
        // Fully authenticated + AccessDeniedException -> 403.
        mvc.perform(delete("/api/articles/1").secure(true).with(csrf()))
           .andExpect(status().isForbidden());
    }

    @Test
    void plainHttpIsRedirectedToHttps() throws Exception {
        mvc.perform(get("/api/articles/1"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrlPattern("https://**"));
    }
}
```

---

## Internals

### How Tomcat hands the request to Spring Security

1. `Http11Processor` parses the raw bytes into a `Request`/`Response` pair.
2. `CoyoteAdapter` wraps them as `HttpServletRequest`/`HttpServletResponse`.
3. `StandardWrapperValve` builds an `ApplicationFilterChain` from the container's filter
   registrations, ordered by their registration order.
4. Boot's `SecurityFilterAutoConfiguration` registered a `DelegatingFilterProxyRegistrationBean`
   named `springSecurityFilterChain` at order `-100`
   (`SecurityProperties.DEFAULT_FILTER_ORDER`), so it runs near the front.
5. `DelegatingFilterProxy.doFilter` looks up the `FilterChainProxy` bean from the
   `WebApplicationContext` (lazily, on first request) and delegates to it.

### Why the security filter order is `-100`

```java
// org.springframework.boot.autoconfigure.security.SecurityProperties
public static final int DEFAULT_FILTER_ORDER = OrderedFilter.REQUEST_WRAPPER_FILTER_MAX_ORDER - 100;
// REQUEST_WRAPPER_FILTER_MAX_ORDER = Ordered.HIGHEST_PRECEDENCE + 50
```

It deliberately sits **after** request-wrapping filters (like `OrderedCharacterEncodingFilter`
and `OrderedFormContentFilter`) so that the request is fully decoded before security reads
parameters, but **before** everything application-level. If security ran before form content
filtering, `request.getParameter("username")` on a `PUT` would return `null`.

### `forward-headers-strategy` and why it is a security setting

Behind a reverse proxy that terminates TLS, Tomcat sees plain HTTP. `request.isSecure()`
returns `false`, `requiresChannel().requiresSecure()` redirects, and you get an infinite
redirect loop. Setting `server.forward-headers-strategy=framework` installs
`ForwardedHeaderFilter`, which rewrites the request from `X-Forwarded-Proto`,
`X-Forwarded-Host`, and `X-Forwarded-For`.

**This is only safe if the proxy strips client-supplied `X-Forwarded-*` headers.** Otherwise
an attacker sends `X-Forwarded-Proto: https` over plain HTTP and defeats your channel
security, or spoofs `X-Forwarded-For` to defeat IP-based rules.

---

## Configuration Reference

| Setting | Effect | Default (Boot 3.x) |
|---|---|---|
| `server.servlet.session.cookie.http-only` | Blocks JS access to the session cookie | `true` (Tomcat default) |
| `server.servlet.session.cookie.secure` | Cookie only over HTTPS | unset — **set it explicitly** |
| `server.servlet.session.cookie.same-site` | Cross-site send policy | unset — browser applies `Lax` |
| `server.servlet.session.timeout` | Idle session expiry | `30m` |
| `server.forward-headers-strategy` | Honour `X-Forwarded-*` | `none` |
| `spring.security.filter.order` | Position of the security filter in the container chain | `-100` |
| `spring.security.filter.dispatcher-types` | Which dispatch types the chain runs on | `ASYNC, ERROR, REQUEST` |
| `http.requiresChannel()` | Forces HTTPS via redirect | off |
| `headers.httpStrictTransportSecurity()` | HSTS header on HTTPS responses | on, `max-age=31536000`, `includeSubDomains` |
| `headers.contentSecurityPolicy()` | CSP header | **off** — must be configured |
| `headers.frameOptions()` | `X-Frame-Options` | `DENY` |

---

## Production Concerns & Anti-Patterns

**State-changing `GET` endpoints.** `GET /users/5/delete` bypasses CSRF protection entirely,
gets prefetched by browsers and link scanners, and ends up in access logs and `Referer`
headers. Use the correct verb.

**Secrets in the query string.** `GET /reset?token=abc123` puts the token in server access
logs, proxy logs, browser history, and the `Referer` header of every outbound link on the
resulting page. Put secrets in headers or the body.

**`SameSite=None` without understanding it.** Teams set it to "fix" a cross-origin problem
and silently restore full CSRF exposure. If you genuinely need cross-site cookies, you must
keep CSRF tokens enabled.

**Trusting `X-Forwarded-For` for security decisions.** Unless your edge proxy overwrites it,
it is entirely attacker-controlled. IP allowlists built on it are decorative.

**Returning 403 where 404 is correct.** `GET /documents/99` returning 403 tells the attacker
that document 99 exists. For tenant-scoped or ownership-scoped resources, 404 is often the
right answer — it leaks nothing.

**Verbose error responses.** A 500 carrying a stack trace reveals your framework versions,
package structure, and sometimes SQL. Map everything to a generic body and log the detail
server-side with a correlation ID.

---

## Debugging Playbook

| Symptom | Likely root cause | Fix |
|---|---|---|
| REST API returns `302` to `/login` instead of `401` | Default `LoginUrlAuthenticationEntryPoint` is active because `formLogin()` is configured | Set a custom `AuthenticationEntryPoint` that sends 401, or scope `formLogin` to a separate filter chain |
| Infinite redirect loop after enabling `requiresChannel` | TLS terminated at the proxy; Tomcat sees HTTP | `server.forward-headers-strategy=framework` and ensure the proxy sets `X-Forwarded-Proto` |
| Cookie not sent by the browser at all | `Secure` set but page served over HTTP, or `SameSite=None` without `Secure` | Serve over HTTPS; pair `None` with `Secure` |
| Login works in Postman, fails in the browser | Postman does not enforce `SameSite`/CORS; the browser does | Check `SameSite`, origin, and CSRF token |
| `403` on every `POST` from your SPA | CSRF token not sent, or sent under the wrong header name | Use `CookieCsrfTokenRepository.withHttpOnlyFalse()` and send `X-XSRF-TOKEN` |
| `request.getParameter()` is null on `PUT`/`PATCH` | Form content filter not applied to those methods | Rely on `@RequestBody`, or enable `OrderedFormContentFilter` |
| HSTS header missing | HSTS is only written on secure requests by default | Verify `request.isSecure()` is true (forwarded headers) |

---

## Interview Q&A

### Q1. HTTP is stateless. So how does a Spring web application remember a logged-in user across requests?

<details>
<summary>Show answer</summary>

It does not rely on HTTP at all — it layers state on top. On successful authentication the
container creates an `HttpSession` and returns its identifier in a `Set-Cookie: JSESSIONID=...`
header. The browser automatically attaches that cookie to every subsequent request to the
same origin. Spring Security's `SecurityContextHolderFilter` (6.x) reads the
`SecurityContext` out of the session through `HttpSessionSecurityContextRepository` and puts
it into the `SecurityContextHolder` for the duration of the request, then clears it in a
`finally` block.

So the statelessness of HTTP is unchanged. The *server* holds state, and the client holds
only an opaque pointer to it.

**Counter-question: if the server holds the state, what breaks when you scale to three instances behind a load balancer?**

The session lives in the memory of whichever instance created it. A follow-up request routed
elsewhere finds no session and the user appears logged out. Three fixes, in increasing
quality:

1. Sticky sessions at the load balancer — works, but a single instance restart logs out
   every user pinned to it, and it defeats even load distribution.
2. Session replication between instances — chatty, and replication lag causes intermittent
   failures at exactly the moment of failover.
3. An external session store (Spring Session with Redis or JDBC) — instances become truly
   stateless, any node can serve any request, and a restart loses nothing. This is the
   standard answer.

**Counter-question: you said `SecurityContextHolder` is cleared in a `finally` block. Why is that so important?**

Because servlet containers pool threads. If the context were left behind, the *next* request
to be dispatched onto that thread would begin life already authenticated as the previous
user. That is a cross-user data leak, and it is catastrophic and very hard to reproduce in
testing because it depends on thread reuse under load. `FilterChainProxy` clears the context
in `finally`, which is why you must never call `SecurityContextHolder.setContext()` in a
filter without ensuring cleanup.

**Counter-question: does the same risk exist with virtual threads in Boot 3.2+?**

The specific pooling risk goes away because a virtual thread is not reused across requests —
it is created per task and discarded. But `ThreadLocal` still works correctly with virtual
threads, and the clearing logic is unchanged and still correct. The real change is a
performance one: with millions of virtual threads, `ThreadLocal` memory footprint matters
more, which is part of why Spring Security 6 introduced the pluggable
`SecurityContextHolderStrategy` rather than static access.
</details>

### Q2. Explain the difference between 401 and 403, and then tell me why Spring sometimes returns a 302 when you expected a 401.

<details>
<summary>Show answer</summary>

**401 Unauthorized** actually means *unauthenticated* — the RFC name is a historical mistake.
It says "I do not know who you are". Per RFC 9110 the response must carry a
`WWW-Authenticate` header telling the client how to authenticate.

**403 Forbidden** means *authenticated but not permitted*. The server knows exactly who you
are and the answer is still no. Retrying with the same credentials will never work.

The 302 happens because of how `ExceptionTranslationFilter` resolves an `AccessDeniedException`.
It asks `AuthenticationTrustResolver` whether the current authentication is anonymous or
remember-me. If it is, Spring reasons that the user has not really tried to authenticate
yet, so instead of a flat 403 it *starts authentication* by invoking the configured
`AuthenticationEntryPoint`. With `formLogin()` enabled, that entry point is
`LoginUrlAuthenticationEntryPoint`, which redirects to `/login` — hence the 302.

**Counter-question: so how do you make a REST API return a clean 401 instead of that redirect?**

Three options, best first:

1. Separate the chains. Put the API on its own `SecurityFilterChain` with
   `securityMatcher("/api/**")` and no `formLogin`, so the default entry point is
   `Http403ForbiddenEntryPoint` or whatever you set. The browser-facing chain keeps form login.
2. Set an explicit entry point on the API chain:
   `.exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))`.
3. Use `DelegatingAuthenticationEntryPoint` keyed on a `RequestMatcher` (for example, does the
   request `Accept` JSON?) if you must serve both from one chain.

Option 1 is the cleanest because it makes the two audiences explicit in configuration rather
than hiding the branching inside a handler.

**Counter-question: when would you deliberately return 404 instead of 403?**

When the existence of the resource is itself sensitive. If `GET /documents/4711` returns 403,
the attacker has learned that document 4711 exists and belongs to someone else — they can
enumerate the whole ID space and map your data. For tenant-scoped or ownership-scoped
resources, returning 404 for "exists but not yours" and "does not exist" makes the two cases
indistinguishable. The trade-off is debuggability: your own support team will also see 404s,
so log the real reason server-side with a correlation ID.

**Counter-question: your 403 handler writes a JSON body. Why doesn't your `@RestControllerAdvice` catch that exception?**

Because `AuthorizationFilter` throws it in the servlet filter layer, before `DispatcherServlet`
ever runs. `@ControllerAdvice` is Spring MVC machinery; it only sees exceptions thrown from
within the dispatch. Filter-layer exceptions must be handled by `AccessDeniedHandler` and
`AuthenticationEntryPoint`. The exception is method security — `@PreAuthorize` on a controller
throws *inside* the dispatch, so that one *is* catchable by `@ControllerAdvice`, which is why
the same logical failure can produce two different response bodies in the same application if
you are not careful.
</details>

### Q3. Why does Spring Security skip CSRF protection for GET, HEAD, OPTIONS and TRACE?

<details>
<summary>Show answer</summary>

Because CSRF protection is only needed for requests that change state, and those four methods
are defined by the HTTP specification as **safe** — they must not have side effects. The cost
of protecting them would be high (every link on your site would need a token) and the benefit
zero, assuming your application honours the spec.

The set is hard-coded in `CsrfFilter.DefaultRequiresCsrfMatcher`.

**Counter-question: so if I implement `GET /transfer?to=bob&amount=1000`, what exactly happens?**

You have created a CSRF vulnerability with no warning from the framework. An attacker puts
`<img src="https://yourbank.com/transfer?to=attacker&amount=1000">` on any page. The victim's
browser loads the image, automatically attaching the session cookie, and the transfer
executes. No JavaScript, no CORS involvement — image loading is not subject to the
same-origin policy for *sending* the request. `SameSite=Lax` would block this particular
vector because an image load is not a top-level navigation, but you should never rely on a
browser default as your only control.

There is more: browsers, link prefetchers, corporate security scanners, and chat-app link
previews all issue speculative GETs. A state-changing GET will eventually be triggered by a
machine with no user involved at all.

**Counter-question: TRACE is in the safe list. Is that a problem?**

It is safe in the idempotency sense, but `TRACE` enables Cross-Site Tracing (XST): the server
echoes the entire request including headers, which historically let an attacker read an
`HttpOnly` cookie via `XMLHttpRequest`. Modern browsers block `TRACE` from script, and Tomcat
disables it by default (`allowTrace=false`), so it is largely historical. The correct posture
is to disable `TRACE` at the container or proxy rather than rely on CSRF logic.

**Counter-question: I have a stateless JWT API with tokens in the `Authorization` header. Can I disable CSRF?**

Yes, and here is the precise reason, which matters more than the answer. CSRF exploits
**ambient authority** — credentials the browser attaches automatically, which means cookies
and HTTP Basic. An `Authorization: Bearer` header is not ambient; the attacker's page cannot
make the victim's browser add it, because setting that header from script requires a CORS
preflight that your server will refuse.

The trap: if you store the JWT in a cookie "for convenience", it becomes ambient again and
CSRF is fully back. The rule is not "JWT means no CSRF", it is **"no cookie-based
credentials means no CSRF"**.
</details>

### Q4. A colleague says "we use HTTPS, so HTTP Basic is fine". Respond.

<details>
<summary>Show answer</summary>

TLS fixes the confidentiality of the wire, which is necessary but not sufficient. The
remaining problems with Basic are structural:

1. **The raw password is transmitted on every single request.** A session ID or token is a
   revocable, low-value bearer credential. A password is a high-value, long-lived,
   often-reused secret. Sending it a thousand times a day multiplies every opportunity for it
   to be captured — a debug proxy, an over-eager APM agent that logs headers, a misconfigured
   access log, a heap dump.
2. **No revocation, no expiry, no session semantics.** You cannot log out. You cannot expire
   an idle session. You cannot invalidate one device.
3. **The server must be able to verify a raw password on every request**, which means running
   bcrypt (deliberately ~100 ms) per request or caching the verification — and caching
   password verifications is its own hazard.
4. **Browser UX is a native dialog** you cannot style, cannot add MFA to, and cannot log out of
   without closing the browser.
5. **TLS termination is rarely end-to-end.** The password arrives in plaintext at your load
   balancer, your WAF, your service mesh sidecar, and any of those can log it.

Basic auth is acceptable for machine-to-machine calls with a generated, rotatable secret that
is not a human's password, and for internal health endpoints. It is not acceptable for user
login.

**Counter-question: if it's so bad, why does Spring Security still ship `httpBasic()` and why is it in nearly every tutorial?**

Because it is the simplest thing that demonstrates the authentication pipeline without
requiring a login page, a session, or a token issuer — it is a teaching tool and a
machine-to-machine tool. It is also genuinely useful for securing Actuator endpoints scraped
by Prometheus, or for a curl-able internal admin API. The mistake is generalising from the
tutorial to production user login.

**Counter-question: you mentioned bcrypt costs ~100ms per request. Doesn't that make Basic auth a DoS vector?**

Yes, and it is a real one. An unauthenticated attacker can send requests with garbage
credentials, and each one forces your server to burn ~100 ms of CPU on a deliberately slow
KDF. A few hundred concurrent requests will saturate your CPU. Mitigations: rate-limit
unauthenticated requests at the edge before they reach the application, cap the length of the
submitted password (bcrypt on a 1 MB string is far worse), and prefer an
authenticate-once-then-use-a-token model so the KDF runs once per session rather than once per
request.
</details>

### Q5. Walk me through every security-relevant thing that happens between the browser sending a request and your `@GetMapping` method executing.

<details>
<summary>Show answer</summary>

1. **TLS handshake.** Certificate validated by the browser; cipher suite negotiated. If HSTS
   was previously sent, the browser upgraded `http://` to `https://` before any request left
   the machine.
2. **Cookie selection.** The browser decides which cookies to attach based on origin, `Path`,
   `Secure`, and `SameSite` relative to the initiating context. This is where CSRF is won or
   lost.
3. **Request arrives at the edge.** Load balancer / WAF terminates TLS, hopefully strips
   client-supplied `X-Forwarded-*` and sets its own.
4. **Tomcat** parses bytes into `HttpServletRequest` and builds the container filter chain.
5. **`ForwardedHeaderFilter`** (if enabled) rewrites scheme/host/port so `request.isSecure()`
   is truthful.
6. **`DelegatingFilterProxy`** (`springSecurityFilterChain`, order `-100`) delegates to
   `FilterChainProxy`.
7. **`FilterChainProxy`** iterates its `SecurityFilterChain` list and picks the **first**
   whose `RequestMatcher` matches. Only that chain runs.
8. **`DisableEncodeUrlFilter`** prevents the session ID being encoded into URLs.
9. **`SecurityContextHolderFilter`** loads the `SecurityContext` from the
   `SecurityContextRepository` (deferred/lazy in 6.x) and sets it on the holder; clears it in
   `finally`.
10. **`HeaderWriterFilter`** registers the response wrapper that will write HSTS, `nosniff`,
    `X-Frame-Options`, CSP, and cache headers.
11. **`CorsFilter`** short-circuits `OPTIONS` preflights and adds `Access-Control-*` headers.
12. **`CsrfFilter`** loads/generates the token and, for unsafe methods, compares the submitted
    token; mismatch throws `AccessDeniedException` (a `CsrfException`).
13. **`LogoutFilter`** matches `POST /logout` and, if it matches, ends the request here.
14. **Authentication filters** — `UsernamePasswordAuthenticationFilter` for `POST /login`,
    `BasicAuthenticationFilter` for the `Authorization: Basic` header,
    `BearerTokenAuthenticationFilter` for `Bearer`. Each builds an unauthenticated token,
    hands it to `AuthenticationManager`, and on success stores the result via
    `SecurityContextRepository`.
15. **`RequestCacheAwareFilter`** restores the pre-login request after a successful
    authentication redirect.
16. **`AnonymousAuthenticationFilter`** — if nothing authenticated, installs an
    `AnonymousAuthenticationToken` with `ROLE_ANONYMOUS`, so downstream code never sees `null`.
17. **`ExceptionTranslationFilter`** wraps the rest of the chain in a try/catch. It does
    nothing on the way in; it exists to catch `AuthenticationException` and
    `AccessDeniedException` on the way out and convert them to 401/403/redirect.
18. **`AuthorizationFilter`** evaluates `authorizeHttpRequests` rules via `AuthorizationManager`.
    Denial throws `AccessDeniedException`, caught by step 17.
19. **`DispatcherServlet`** — now, finally, Spring MVC begins.
20. **`HandlerInterceptor.preHandle`** runs.
21. **Method security AOP proxy** evaluates `@PreAuthorize` before the target method body.
22. **Your controller method executes.**
23. On the way out, `@PostAuthorize` / `@PostFilter`, then `postHandle`, then the response
    passes back up through every filter, where headers get written and the context is cleared.

**Counter-question: at step 7 you said only the first matching chain runs. Why does that trip people up?**

Because people expect matching to accumulate, like `@RequestMapping`. It does not. If you
declare a chain with `securityMatcher("/**")` at `@Order(1)` and a chain for `/api/**` at
`@Order(2)`, the `/api/**` chain is dead code — the catch-all swallows everything. The rule
is: **most specific matcher first, catch-all last**, and the catch-all chain is the only one
allowed to have no `securityMatcher`. Spring Boot logs a warning for an unreachable chain in
recent versions, but it is easy to miss.

**Counter-question: `ExceptionTranslationFilter` sits at 17, before `AuthorizationFilter` at 18. Why that order and not the reverse?**

Because the filter chain is a call stack, not a pipeline. `ExceptionTranslationFilter` calls
`chain.doFilter(...)` inside a `try` block, so everything *after* it — including
`AuthorizationFilter` and the entire MVC dispatch — executes within that `try`. Being
"before" in the list means it is *outside* in the stack, which is exactly what you need to
catch things thrown later. If it were after `AuthorizationFilter`, it would never see the
exception that filter throws.

**Counter-question: where would you insert a JWT filter, and why there?**

Before `UsernamePasswordAuthenticationFilter`
(`http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)`). The precise
requirement is only that it runs **after** `SecurityContextHolderFilter` (so the holder is
initialised and will be cleaned up) and **before** `AuthorizationFilter` (so the
authentication exists when rules are evaluated). `UsernamePasswordAuthenticationFilter` is a
convenient, well-known landmark inside that window. Putting it after `AuthorizationFilter` is
the classic bug: every request is evaluated as anonymous and you get a 403 with a perfectly
valid token.
</details>

### Q6. What is the difference between encoding, encryption, and hashing, and where does Base64 in HTTP Basic fit?

<details>
<summary>Show answer</summary>

- **Encoding** transforms data into another representation for *transport compatibility*.
  It is fully reversible by anyone, requires no key, and provides **zero** security.
  Base64, URL-encoding, and HTML entities are encodings.
- **Encryption** transforms data so only a key-holder can reverse it. Reversible *with the
  key*. Provides confidentiality.
- **Hashing** is a one-way function producing a fixed-size digest. Not reversible by design.
  Provides integrity, and — with a slow, salted KDF — password storage.

HTTP Basic uses **Base64, which is encoding**. `dXNlcjpwYXNz` decodes to `user:pass` with one
shell command. Its purpose is purely to make arbitrary bytes safe to put in an HTTP header,
not to hide anything. This is why Basic is meaningless without TLS.

**Counter-question: if hashing is one-way, how does login verification work?**

You never reverse the hash. You hash the submitted password with the *same* salt and
parameters and compare digests. With bcrypt the salt and cost factor are embedded in the
stored string itself — `$2a$10$N9qo8uLOickgx2ZMRZoMye...` encodes algorithm `2a`, cost `10`,
then the 22-char salt, then the digest — so `matches(raw, encoded)` can extract everything it
needs from the stored value.

**Counter-question: so is a JWT encrypted?**

Not by default. The standard JWT you see is a **JWS** — signed, not encrypted. The header and
payload are `base64url`-encoded plaintext that anyone can read by pasting the token into
jwt.io. The signature proves *integrity and authenticity* (it has not been altered and it came
from the holder of the key), not confidentiality.

If you need the payload hidden you need **JWE**, which is a different, five-part structure and
is comparatively rare. The practical rule: **never put anything in a JWT payload you would not
put in a log file.** No PII beyond an identifier, no internal hostnames, no secrets.

**Counter-question: comparing digests — is `String.equals` acceptable there?**

For comparing a password hash, in practice yes, because the attacker does not control the
stored hash and cannot observe the comparison directly. But for comparing **secrets the
attacker supplies and can vary** — API keys, HMAC signatures, CSRF tokens — `equals` is a
timing side channel: it returns at the first differing byte, so response time leaks how many
leading bytes were correct, and the secret can be recovered byte by byte. Use a constant-time
comparison: `MessageDigest.isEqual(byte[], byte[])`, or Spring Security's own
`CsrfTokenRepository` logic which uses exactly that. Spring's `BCryptPasswordEncoder` also
uses a constant-time check internally.
</details>

### Q7. Design question — you are asked to choose between cookie-session and JWT for a new product. Walk me through your decision.

<details>
<summary>Show answer</summary>

I would refuse to answer until I know four things, because the answer is entirely determined
by them:

1. **Who are the clients?** A single first-party web SPA, or also mobile apps and third-party
   API consumers?
2. **What is the revocation requirement?** Does "log out everywhere" or "disable a compromised
   account" need to take effect in milliseconds, or is a few minutes acceptable?
3. **What is the scale and topology?** One service, or dozens that need to verify identity
   without calling back to an auth service on every request?
4. **What is the regulatory posture?** Finance and healthcare usually mandate immediate
   session termination.

Given those, my defaults:

**First-party web app, single backend → cookie session.** It is the boring, correct answer.
`HttpOnly` + `Secure` + `SameSite=Lax` gives XSS-resistant credential storage that JWT in
`localStorage` cannot match. Revocation is a `DELETE` from Redis. Cost is one Redis lookup per
request, which is sub-millisecond. The only real work is enabling CSRF protection properly.

**Public API, mobile clients, or many services → JWT access tokens.** Self-contained
validation means a downstream service verifies a signature locally instead of a network call.
Keep access tokens **short** (5–15 minutes) so the revocation window is bounded, and pair them
with opaque, rotating refresh tokens stored server-side — the refresh token is where you get
revocation back.

**Web SPA plus mobile → Backend-for-Frontend.** The BFF holds a cookie session with the
browser and holds the OAuth2 tokens server-side, exchanging them when calling downstream. The
browser never sees a token. Mobile talks OAuth2 directly. This is the pattern the OAuth2
browser-based-apps BCP now recommends, and it is what I would push for.

What I would *not* do is put a JWT in `localStorage` for a first-party web app. It trades a
solved problem (CSRF, which has a standard mitigation) for an unsolved one (XSS token theft,
which has no mitigation once script runs).

**Counter-question: you said short-lived access tokens bound the revocation window. The security team demands instant revocation. Now what?**

Then stateless validation is off the table for that decision, and I would say so plainly
rather than pretend otherwise. Options in order of preference:

1. **Shorten the access token to ~60 seconds** and make refresh cheap. The window is now
   smaller than the time it takes a human to act on the revocation anyway. Often this satisfies
   "instant" in practice.
2. **Denylist only revoked JTIs**, replicated to every service via Redis or a pub/sub topic.
   The list stays tiny because entries expire when the token would have expired naturally. This
   is stateful, but it is *bounded* state, not a session store.
3. **Token introspection** on every request (RFC 7662). Fully revocable, but you have
   reinvented the session lookup with more moving parts and worse latency — at that point a
   session is the simpler design.
4. **Bind sessions to a `session_id` claim** and check its validity from a shared cache. A
   hybrid: self-contained claims for authorization data, one cheap cache hit for liveness.

I would present 1 and 2 together as the recommendation, and be explicit that 3 means we should
have chosen sessions.

**Counter-question: the frontend team insists on `localStorage` because "cookies are complicated". How do you handle that conversation?**

I would reframe it as a threat comparison rather than a preference. With `HttpOnly` cookies,
an XSS flaw lets the attacker make requests *as* the user while the page is open — bad, but
bounded and observable. With `localStorage`, the same XSS flaw lets them **exfiltrate the
token** and use it from their own machine, from anywhere, until it expires — unbounded and
invisible to us. The cookie complexity they are avoiding is one `CookieCsrfTokenRepository`
bean and one header in their HTTP client; the risk they are accepting is silent full account
takeover. If they still push back, `sessionStorage` with a very short token and refresh
rotation is a compromise, but I would record the decision and its rationale rather than let it
be made implicitly.
</details>

---

## Quick Recall

```
STATELESSNESS
  HTTP remembers nothing -> sessions/cookies/tokens exist to fake memory
  SecurityContextHolder cleared in finally -> thread pools would leak identity otherwise

METHODS
  safe: GET HEAD OPTIONS TRACE   -> no CSRF check
  unsafe: POST PUT PATCH DELETE  -> CSRF checked
  state-changing GET = silent CSRF hole

STATUS
  401 = who are you?        -> AuthenticationEntryPoint, must send WWW-Authenticate
  403 = I know, still no    -> AccessDeniedHandler
  anonymous + AccessDenied  -> upgraded to 401/302, NOT 403
  404 instead of 403        -> when existence itself is sensitive

COOKIE ATTRIBUTES
  HttpOnly -> blocks XSS theft
  Secure   -> HTTPS only
  SameSite Lax (default) -> blocks most CSRF
  SameSite None -> requires Secure, restores CSRF risk
  __Host- prefix -> browser-enforced hardening

CREDENTIAL AMBIENCE (the real CSRF rule)
  ambient (cookie, Basic) -> CSRF possible
  explicit (Authorization: Bearer header) -> CSRF not possible
  JWT in a cookie -> ambient again -> CSRF is back

ENCODING vs ENCRYPTION vs HASHING
  Base64 = encoding = zero security
  JWT default = JWS = signed, NOT encrypted, payload is world-readable

FILTER POSITION
  Spring Security order = -100, before DispatcherServlet
  => @ControllerAdvice cannot catch filter-layer security exceptions
```

---

**Next:** [`02_M1_T2_Authentication_Authorization.md`](02_M1_T2_Authentication_Authorization.md)
