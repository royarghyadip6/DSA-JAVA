# 11. Spring Security (Boot-focused)

## Start here (simple English)

**In one sentence:** Security answers two questions: **Who are you?** (authentication) and **What may you do?** (authorization).

**Everyday picture:** An office building.

- **Reception** (filter chain) checks your badge **before** you enter any room (`DispatcherServlet` / controllers)
- **401** = you have **no valid badge**
- **403** = badge is valid, but **this floor is forbidden**

Spring Security is a stack of **servlet filters** in front of MVC. JWT parsing happens **here**, not in `@ControllerAdvice`.

**Boot 3 style** (adapter class is gone):

```java
@Bean
SecurityFilterChain api(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/v1/health").permitAll()
            .anyRequest().authenticated());
    return http.build();
}
```

Until you write this, Boot’s default is **lock everything** (a login page for a browser app).

**Two common API styles:**

| | Session cookie | JWT in `Authorization` header |
|--|----------------|-------------------------------|
| CSRF | Keep **on** | Usually **off** |
| Server memory | Session | Token is the session |

Interview Q&A is **5–8 year standard**.

---

Spring Security is a **filter chain** in front of `DispatcherServlet`.

`WebSecurityConfigurerAdapter` is **deleted** in Spring Security 6 / Boot 3. Interviews still mention it. Answer: “adapter is gone; we declare a `SecurityFilterChain` `@Bean`.”

---

## 1. Filter chain (the real architecture)

```text
HTTP request
  → FilterChainProxy (Spring Security’s single servlet filter)
      → SecurityFilterChain (matchers pick a chain)
          → DisableEncodeUrlFilter
          → WebAsyncManagerIntegrationFilter
          → SecurityContextHolderFilter     // restore SecurityContext
          → HeaderWriterFilter
          → CsrfFilter                      // cookie/session apps
          → LogoutFilter
          → UsernamePasswordAuthenticationFilter  // form login
          → BearerTokenAuthenticationFilter       // resource server
          → RequestCacheAwareFilter
          → SecurityContextHolderAwareRequestFilter
          → AnonymousAuthenticationFilter
          → SessionManagementFilter
          → ExceptionTranslationFilter      // 401 vs 403 translation
          → AuthorizationFilter             // authorizeHttpRequests
  → DispatcherServlet
```

You do **not** memorize every class. You **do** remember:

- Security runs **before** MVC
- `ExceptionTranslationFilter` converts auth failures to 401/403
- `@ControllerAdvice` does **not** handle those unless they occur **inside** MVC after the chain allowed the request
- Multiple `SecurityFilterChain` beans: `@Order`, first match wins (`securityMatcher`)

`SecurityContext` is a **ThreadLocal** (`MODE_THREADLOCAL`). `@Async` loses it unless you wrap the executor (`DelegatingSecurityContextAsyncTaskExecutor`).

---

## 2. Boot 3 configuration shape

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain api(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/v1/**")
            .csrf(csrf -> csrf.disable())  // JWT / stateless API
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/v1/health").permitAll()
                .requestMatchers("/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
            .cors(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    SecurityFilterChain actuator(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/actuator/**")
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .anyRequest().hasRole("OPS")
            )
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
```

**Order:** more specific chains first (`@Order(0)` actuator, `@Order(1)` API). A chain without `securityMatcher` matches **everything** and swallows the rest.

---

## 3. Authentication mechanisms

| Mechanism | When |
|-----------|------|
| Form login + session cookie | Server-rendered / same-site SPA with cookies |
| HTTP Basic | Actuator, internal tools (always HTTPS) |
| DAO `UserDetailsService` + `PasswordEncoder` | Homegrown users table |
| JWT resource server (`oauth2ResourceServer().jwt()`) | Stateless APIs, tokens from an Authorization Server |
| OAuth2 login | “Login with Google/Okta” (browser redirect) |
| mTLS | Service-to-service |

**JWT API vs session app is the fork:**

| | Session cookie | JWT Bearer |
|--|----------------|------------|
| CSRF | **On** | Usually **off** (no cookie for browser auto-send of access token) |
| Session | `IF_REQUIRED` | `STATELESS` |
| Storage | Server session (or Spring Session Redis) | Token is the session |
| Logout | Invalidate session | Client drops token; need a denylist if you must revoke |

If the SPA stores JWT in **localStorage**, XSS steals it. If it stores in an **httpOnly cookie**, you are back to **CSRF**. There is no free lunch.

---

## 4. CSRF

CSRF = browser automatically sends cookies on a forged third-party POST.

- Cookie/session apps: **keep CSRF** (Boot default on). SPA reads `XSRF-TOKEN` cookie and echoes `X-XSRF-TOKEN`.
- Pure Bearer header APIs: **disable CSRF** (no cookie credential).
- Mixing cookie session + disable CSRF = **vulnerable**.

---

## 5. CORS vs Security

CORS is a **browser** rule. Security **authorization** is a **server** rule.

You need both:

```java
http.cors(Customizer.withDefaults());
```

and a `CorsConfigurationSource` bean (chapter 06). Preflight `OPTIONS` must be `permitAll()` or CORS filter must run correctly inside the chain. Symptom: MVC `@CrossOrigin` works in curl, fails in the browser with Security on.

---

## 6. Password encoding

```java
@Bean
PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
}
```

Delegating encoder stores `{bcrypt}...`. **Never** `NoOpPasswordEncoder` in prod. **Never** log passwords.

`UserDetailsService.loadUserByUsername` → `UserDetails` (username, hash, authorities, flags: locked, expired).

---

## 7. Authorization

**HTTP (filter):** `authorizeHttpRequests` — coarse (path + method + role).

**Method (AOP):** `@PreAuthorize("hasRole('ADMIN')")`, `@PostAuthorize`, `@PreFilter`. Needs `@EnableMethodSecurity`. Same **proxy** rules as `@Transactional` (self-invocation skips security!).

`hasRole("ADMIN")` looks for authority `ROLE_ADMIN`. `hasAuthority("ORDER_WRITE")` is raw. Pick one convention.

401 vs 403:

| | Meaning | Typical class |
|--|---------|----------------|
| 401 Unauthorized | No valid authentication | `AuthenticationEntryPoint` |
| 403 Forbidden | Authenticated, missing permission | `AccessDeniedHandler` |

Anonymous user hitting a protected path: 401. User `ROLE_USER` hitting admin: 403.

For REST, replace the default HTML login redirect:

```java
http.exceptionHandling(e -> e
    .authenticationEntryPoint((req, res, ex) -> {
        res.setStatus(401);
        res.setContentType("application/problem+json");
        // write ProblemDetail
    })
    .accessDeniedHandler((req, res, ex) -> res.setStatus(403)));
```

---

## 8. JWT resource server (common Boot interview)

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://auth.acme.com/realms/payments
```

Boot auto-configures `JwtDecoder` from the issuer’s JWKS. You validate signature, expiry, issuer. Map claims to `JwtAuthenticationToken` authorities (`scope` / `roles` converters).

Custom converter when Keycloak puts roles under `realm_access.roles`.

**Don’t** write your own HS256 parser in a filter unless you enjoy CVE lists. Use `NimbusJwtDecoder`.

---

## 9. Method security and domain objects

```java
@PreAuthorize("hasRole('ADMIN') or @orderAuth.owns(#id, principal)")
@GetMapping("/{id}")
public OrderResponse get(@PathVariable long id) { ... }
```

`@PostAuthorize("returnObject.ownerId == principal.id")` after the method — don’t use if the method already did the expensive work; check first.

---

## 10. Production pitfalls

1. `anyRequest().permitAll()` left from a demo.
2. CSRF disabled on a cookie session SPA.
3. Actuator `health` public is fine; `env` / `heapdump` public is a **incident**.
4. CORS `allowedOrigins("*")` + `allowCredentials(true)` — illegal and broken.
5. `@PreAuthorize` on a private method.
6. Security context lost in `@Async` / WebFlux (different model: `ReactiveSecurityContextHolder`).
7. Mixing `authenticated()` with anonymous actuator without a separate chain.
8. Logging JWT / Authorization headers.

---

# Interview Q&A (5–8 year bar)

A fresher knows login and roles. A 5–8 year answer explains filter order, CSRF vs JWT, 401 vs 403, and method-security proxies.

### Q1. How does Spring Security work in a Boot app?

**Answer:** Boot registers `FilterChainProxy`. A `SecurityFilterChain` bean defines matchers, CSRF, session, authN, authZ. Filters run before `DispatcherServlet`. Default is authenticated-all until you customize.

**Counter:** Adapter class?  
**Answer:** Removed in Security 6. Use `SecurityFilterChain` `@Bean`.

---

### Q2. Authentication vs authorization?

**Answer:** AuthN = identity (JWT, form, basic). AuthZ = permissions (`authorizeHttpRequests`, `@PreAuthorize`).

---

### Q3. 401 vs 403?

**Answer:** 401 = not authenticated (or bad token). 403 = authenticated, not allowed.

**Counter:** Why did the browser get a 302 to `/login` on an API?  
**Answer:** Default form-login entry point. Set a JSON `AuthenticationEntryPoint`.

---

### Q4. Why disable CSRF for JWT APIs?

**Answer:** CSRF exploits **cookie** credentials sent automatically. Bearer tokens in `Authorization` are not auto-sent by foreign forms. If you put JWT in a cookie, CSRF is back.

**Counter:** SPA with session cookie?  
**Answer:** Keep CSRF on.

---

### Q5. Filter vs `@PreAuthorize`?

**Answer:** Filter = path-level, before controller. Method security = AOP on the bean, can use parameters and Spring beans in SpEL. Use both: filter for coarse, method for domain.

**Counter:** `@PreAuthorize` on the same class calling `this.delete()`?  
**Answer:** Skipped. Proxy rules.

---

### Q6. Where do you put JWT validation?

**Answer:** Resource-server filter (`BearerTokenAuthenticationFilter`), not `@ControllerAdvice`. Invalid token → 401 via `AuthenticationEntryPoint`.

---

### Q7. `hasRole` vs `hasAuthority`?

**Answer:** `hasRole("X")` ⇒ authority `ROLE_X`. `hasAuthority` is literal. Mixing both without the prefix is the classic bug.

---

### Q8. How do you expose `/actuator/health` but lock `/actuator/env`?

**Answer:** `requestMatchers("/actuator/health").permitAll()` and `anyRequest().hasRole("OPS")` on an actuator-matched chain. Also `management.endpoints.web.exposure.include`.

---

### Q9. CORS failed only after adding Security?

**Answer:** Enable CORS on `HttpSecurity` and permit preflight. MVC-only CORS is too late if Security rejects OPTIONS.

---

### Q10. How is `SecurityContext` stored?

**Answer:** `SecurityContextHolder` ThreadLocal (default). Request finishes → cleared. `@Async` needs a delegating executor.

---

### Q11. Password storage?

**Answer:** BCrypt (or Argon2) via `PasswordEncoder`. Delegating encoder for algorithm evolution. Never reversible encryption for passwords.

---

### Q12. Multiple `SecurityFilterChain`s?

**Answer:** Yes. `@Order` + `securityMatcher`. First matching chain handles the request exclusively.

---

### Q13. What is a resource server vs authorization server?

**Answer:** Resource server **validates** tokens and serves APIs (your Boot app). Authorization server **issues** tokens (Keycloak, Auth0, Spring Authorization Server). Don’t build a toy issuer in the API app.

---

### Q14. `authenticated()` vs `permitAll()` vs `anonymous()`?

**Answer:** `permitAll` = no auth required. `authenticated` = must have a non-anonymous Authentication. `anonymous` = specifically the anonymous token Security installs.

---

### Q15. How do you test security?

**Answer:** `@WebMvcTest` + `SecurityMockMvcRequestPostProcessors.jwt()` / `user("u").roles("ADMIN")`. `@WithMockUser`. Full `@SpringBootTest` with Testcontainers for integration. Don’t `permitAll` in tests unless that is the production rule.
