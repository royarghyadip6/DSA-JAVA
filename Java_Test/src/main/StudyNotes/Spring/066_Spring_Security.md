# 66. Spring Security

## Basics

---

# 1. What is Spring Security?

<details>
<summary>Show Answer</summary>

**Answer:**

**Spring Security** is a powerful framework that provides **authentication**, **authorization**, and **protection** against common attacks (CSRF, session fixation, etc.) for Spring applications.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults())
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}
```

| Feature | Purpose |
|---------|---------|
| **Authentication** | Verify who the user is (login) |
| **Authorization** | Control what the user can access |
| **Password encoding** | BCrypt, not plain text |
| **Session management** | Control login sessions |
| **CSRF protection** | Block cross-site request forgery |
| **Method security** | `@PreAuthorize` on service methods |

```text
Request → Security Filter Chain → Authentication → Authorization → Controller
                ↑
         intercepts EVERY request before your code runs
```

**Interview Point:**

> Spring Security = filter chain that authenticates and authorizes every request. Replaces custom servlet filters and ad-hoc security code.

</details>

---

# 2. Authentication vs Authorization?

<details>
<summary>Show Answer</summary>

**Answer:**

| | **Authentication** | **Authorization** |
|---|-------------------|-------------------|
| **Question** | Who are you? | What can you do? |
| **When** | Login / token validation | After identity is known |
| **Example** | Username + password check | Role `ADMIN` can delete users |
| **Failure** | 401 Unauthorized | 403 Forbidden |
| **Spring** | `AuthenticationManager`, `UserDetailsService` | `authorizeHttpRequests`, `@PreAuthorize` |

```text
Authentication:
  User sends: username=john, password=secret
  System checks: credentials valid? → YES → identity established

Authorization:
  User john (ROLE_USER) requests DELETE /admin/users
  System checks: does ROLE_USER have permission? → NO → 403 Forbidden
```

```java
// Authentication — who is logged in
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String username = auth.getName();

// Authorization — can this user do this?
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(Long id) { ... }
```

**Interview Point:**

> Authentication = identity verification (login). Authorization = permission check (access control). AuthN first, then AuthZ.

</details>

---

# 3. How Spring Security works?

<details>
<summary>Show Answer</summary>

**Answer:**

Spring Security uses a **chain of servlet filters** that run **before** your controller. Each filter handles one security concern.

```text
HTTP Request
    ↓
SecurityContextPersistenceFilter   (load/save SecurityContext)
    ↓
LogoutFilter
    ↓
UsernamePasswordAuthenticationFilter (form login)
    ↓
BearerTokenAuthenticationFilter    (JWT — if configured)
    ↓
AuthorizationFilter                (access rules: permit/deny)
    ↓
ExceptionTranslationFilter         (401/403 handling)
    ↓
FilterSecurityInterceptor            (final authorization check)
    ↓
DispatcherServlet → Your Controller
```

**Core flow:**

1. **Request arrives** → filter chain intercepts.
2. **Extract credentials** — form login, Basic auth, JWT from header.
3. **Authenticate** — `AuthenticationManager` delegates to `AuthenticationProvider`.
4. **Load user** — `UserDetailsService.loadUserByUsername()`.
5. **Verify password** — `PasswordEncoder.matches()`.
6. **Store identity** — `SecurityContextHolder` (thread-local).
7. **Authorize** — check URL rules / `@PreAuthorize`.
8. **Allow or reject** — proceed to controller or return 401/403.

```java
@Bean
public AuthenticationManager authManager(AuthenticationConfiguration config) {
    return config.getAuthenticationManager();
}

@Bean
public DaoAuthenticationProvider authProvider(UserDetailsService uds, PasswordEncoder encoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(uds);
    provider.setPasswordEncoder(encoder);
    return provider;
}
```

**Interview Point:**

> Spring Security = filter chain. Credentials extracted → authenticated → stored in SecurityContext → authorized → controller or 401/403.

</details>

---

## Frequently Asked

---

# 4. UserDetailsService?

<details>
<summary>Show Answer</summary>

**Answer:**

`UserDetailsService` is a Spring Security interface that **loads user-specific data** during authentication.

```java
public interface UserDetailsService {
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
}
```

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepo.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),          // encoded password from DB
            mapRoles(user.getRoles())   // ROLE_USER, ROLE_ADMIN
        );
    }

    private List<GrantedAuthority> mapRoles(Set<Role> roles) {
        return roles.stream()
            .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()))
            .collect(Collectors.toList());
    }
}
```

| Responsibility | Detail |
|----------------|--------|
| **Find user** | By username/email from DB |
| **Return UserDetails** | Username, encoded password, authorities (roles) |
| **Throw exception** | `UsernameNotFoundException` if not found |

```text
Login attempt "john@email.com"
    → UserDetailsService.loadUserByUsername("john@email.com")
    → returns UserDetails(password=encoded, roles=[ROLE_USER])
    → PasswordEncoder.matches(rawPassword, encodedPassword)
```

**Interview Point:**

> UserDetailsService = bridge between your DB and Spring Security. Loads user + roles for authentication.

</details>

---

# 5. PasswordEncoder?

<details>
<summary>Show Answer</summary>

**Answer:**

`PasswordEncoder` is an interface for **encoding and verifying passwords** securely — never store plain text.

```java
public interface PasswordEncoder {
    String encode(CharSequence rawPassword);      // hash for storage
    boolean matches(CharSequence raw, String encoded); // verify at login
}
```

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12); // strength 12
}

// Registration — encode before save
user.setPassword(passwordEncoder.encode(rawPassword));
userRepo.save(user);

// Login — Spring Security calls matches() automatically
passwordEncoder.matches("userTypedPassword", user.getPasswordFromDb());
```

| Rule | Why |
|------|-----|
| **Never store plain text** | DB breach exposes all passwords |
| **Use adaptive hashing** | BCrypt/Argon2 — slow by design |
| **Salt per password** | BCrypt embeds salt in hash |
| **One-way** | Cannot reverse hash to password |

```text
Registration:  "MyPass123" → BCrypt → "$2a$12$N9qo8uLOickgx2ZMRZoMye..."
Login:         "MyPass123" → matches() against stored hash → true/false
```

**Interview Point:**

> PasswordEncoder = secure hash + verify. Always BCrypt (or Argon2) in production. Encode on register, matches() on login.

</details>

---

# 6. BCrypt?

<details>
<summary>Show Answer</summary>

**Answer:**

**BCrypt** is a password hashing algorithm designed to be **slow and salted** — the default choice in Spring Security.

```java
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
String hash = encoder.encode("MyPassword");
// $2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.G2oQdK8xK9vK9e
```

| Property | Detail |
|----------|--------|
| **Salt** | Random salt generated per hash (embedded in output) |
| **Strength** | Cost factor (4–31); `12` is common production value |
| **Format** | `$2a$12$[22-char salt][31-char hash]` |
| **Adaptive** | Increase strength over time as CPUs get faster |

```text
Same password, two encodes → different hashes (different salts):

  encode("hello") → $2a$12$abc...xyz
  encode("hello") → $2a$12$def...uvw

Both match("hello", hash) → true
```

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(); // default strength 10
}
```

**Why not SHA-256?**

```text
SHA-256:  fast → attacker can try billions/sec (GPU)
BCrypt:   slow → attacker limited to thousands/sec
```

**Interview Point:**

> BCrypt = salted + slow hash. Same password → different hashes. Use strength 10–12. Never SHA/MD5 for passwords.

</details>

---

## JWT

---

# 7. What is JWT?

<details>
<summary>Show Answer</summary>

**Answer:**

**JWT (JSON Web Token)** is a compact, self-contained token format for **securely transmitting claims** between parties — commonly used for **stateless API authentication**.

```text
Client login → Server validates → Server issues JWT
Client sends JWT in header on every request → Server validates JWT (no session lookup)
```

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huIiwicm9sZSI6IkFETUlOIn0.signature
```

| Property | Detail |
|----------|--------|
| **Self-contained** | Payload has user id, roles, expiry |
| **Stateless** | Server doesn't store session (just validates signature) |
| **Signed** | Tampering detected via signature verification |
| **Not encrypted by default** | Payload is Base64 — don't put secrets in JWT |

```java
// Issue JWT after login
String token = jwtService.generateToken(user.getEmail(), user.getRoles());

// Validate on each request
if (jwtService.validateToken(token)) {
    String username = jwtService.extractUsername(token);
    // set SecurityContext
}
```

**Interview Point:**

> JWT = signed token carrying claims (user, roles, expiry). Stateless auth — no server session. Send in `Authorization: Bearer` header.

</details>

---

# 8. JWT structure?

<details>
<summary>Show Answer</summary>

**Answer:**

A JWT has **three parts** separated by dots: **Header.Payload.Signature**

```text
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
     HEADER            PAYLOAD              SIGNATURE
```

**1. Header** (Base64URL):

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

**2. Payload** (Base64URL) — claims:

```json
{
  "sub": "john@email.com",
  "roles": ["USER", "ADMIN"],
  "iat": 1704067200,
  "exp": 1704070800
}
```

| Claim | Meaning |
|-------|---------|
| `sub` | Subject (user id/email) |
| `iat` | Issued at |
| `exp` | Expiration time |
| `roles` | Custom claim |

**3. Signature:**

```text
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret-key
)
```

```text
Tamper attempt:
  Change payload role USER → ADMIN
  Signature no longer matches → rejected
```

**Interview Point:**

> JWT = Header (alg) + Payload (claims) + Signature (HMAC/RSA). Signature proves integrity. Payload is readable — don't store passwords.

</details>

---

# 9. JWT advantages?

<details>
<summary>Show Answer</summary>

**Answer:**

| Advantage | Explanation |
|-------------|-------------|
| **Stateless** | No server-side session store — scales horizontally |
| **Microservices** | Any service can validate token with shared secret/public key |
| **Mobile/SPA friendly** | No cookie dependency — header-based |
| **Self-contained** | Roles/permissions in token — fewer DB lookups |
| **Cross-domain** | Works across APIs, no same-origin cookie issues |
| **CDN/API Gateway** | Gateway can validate before reaching backend |

```text
Session-based:
  Client → Server A (session in Redis) → must hit same store

JWT-based:
  Client → Server A, B, C → each validates JWT independently
```

**Disadvantages (know for interviews):**

| Disadvantage | Mitigation |
|--------------|------------|
| Can't revoke instantly | Short expiry + refresh token |
| Token size larger than session id | Don't stuff too much in payload |
| Payload readable | Don't put sensitive data |
| Secret/key management | Use RS256, rotate keys |

**Interview Point:**

> JWT pros = stateless, scalable, microservice-friendly. Cons = revocation harder, payload visible. Pair with short expiry + refresh tokens.

</details>

---

## OAuth

---

# 10. OAuth2?

<details>
<summary>Show Answer</summary>

**Answer:**

**OAuth 2.0** is an **authorization framework** that lets a **third-party application** access a user's resources **without sharing the user's password**.

```text
User wants App to access their Google Drive

OAuth2 flow:
  1. App redirects user to Google login
  2. User grants permission to App
  3. Google redirects back with authorization code
  4. App exchanges code for Access Token
  5. App uses Access Token to call Google API
```

| Role | Example |
|------|---------|
| **Resource Owner** | User |
| **Client** | Your app |
| **Authorization Server** | Google, GitHub, Keycloak |
| **Resource Server** | API that holds user data |

**Common flows:**

| Flow | Use case |
|------|----------|
| **Authorization Code** | Web apps (most secure) |
| **Client Credentials** | Service-to-service (no user) |
| **Refresh Token** | Get new access token without re-login |

```java
// Spring Security OAuth2 Client — "Login with Google"
http.oauth2Login(oauth2 -> oauth2
    .loginPage("/login")
    .defaultSuccessUrl("/dashboard")
);
```

**Interview Point:**

> OAuth2 = delegated authorization. User grants app limited access via tokens — password never shared with third-party app.

</details>

---

# 11. OAuth vs JWT?

<details>
<summary>Show Answer</summary>

**Answer:**

They solve **different problems** — often used **together**, not as alternatives.

| | **OAuth 2.0** | **JWT** |
|---|--------------|---------|
| **Type** | Authorization **framework** | Token **format** |
| **Purpose** | Delegate access to resources | Carry claims in signed token |
| **Question** | "Can this app access my data?" | "Here's a signed packet of user info" |
| **Example** | Login with Google | API auth after login |

```text
Often combined:

  OAuth2 Authorization Code flow
       ↓
  Authorization Server issues JWT as Access Token
       ↓
  Client sends JWT to Resource Server
       ↓
  Resource Server validates JWT signature
```

```text
OAuth2 alone:  defines HOW to get a token (flows, grants)
JWT alone:     defines WHAT the token looks like (header.payload.sig)

OAuth2 token can be:
  - JWT (common)
  - opaque random string (validated by auth server introspection)
```

**Interview Point:**

> OAuth2 = protocol for obtaining tokens. JWT = token format. Access tokens are often JWTs. OAuth2 is not authentication (use OpenID Connect for that).

</details>

---

# 12. Access Token?

<details>
<summary>Show Answer</summary>

**Answer:**

An **Access Token** is a credential that grants **temporary access** to protected resources (APIs).

```http
GET /api/orders
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

| Property | Typical value |
|----------|---------------|
| **Lifetime** | Short — 15 min to 1 hour |
| **Purpose** | Access APIs on behalf of user |
| **Storage** | Memory (SPA), secure header — avoid localStorage if XSS risk |
| **Format** | JWT or opaque string |
| **Contains** | User id, scopes, roles, expiry |

```text
Login → Access Token (expires 15 min)
Every API call → send token
Token expires → use Refresh Token to get new Access Token
```

```java
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) {
        String token = extractBearerToken(req);
        if (token != null && jwtService.validate(token)) {
            Authentication auth = jwtService.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(req, res);
    }
}
```

**Interview Point:**

> Access token = short-lived credential for API access. Send as Bearer token. Short expiry limits damage if stolen.

</details>

---

# 13. Refresh Token?

<details>
<summary>Show Answer</summary>

**Answer:**

A **Refresh Token** is a long-lived credential used to **obtain new Access Tokens** without re-authenticating the user.

```text
Login:
  → Access Token  (15 min)
  → Refresh Token (7 days, stored securely server-side or httpOnly cookie)

Access Token expires:
  POST /auth/refresh  { refreshToken: "..." }
  → New Access Token (15 min)
  → Optionally rotate Refresh Token

Refresh Token expires:
  → User must login again
```

| Token | Lifetime | Usage |
|-------|----------|-------|
| **Access** | Short (minutes) | Every API request |
| **Refresh** | Long (days) | Only to `/refresh` endpoint |

**Security practices:**

```text
✓ Store refresh token in httpOnly + Secure cookie (not localStorage)
✓ Rotate refresh token on each use (detect theft)
✓ Store refresh tokens server-side with revocation list
✓ Bind to device/client id
✓ Short access token + long refresh = balance security and UX
```

```java
@PostMapping("/refresh")
public TokenResponse refresh(@CookieValue("refreshToken") String refreshToken) {
    if (!refreshTokenService.isValid(refreshToken)) {
        throw new UnauthorizedException();
    }
    String newAccess = jwtService.generateAccessToken(user);
    String newRefresh = refreshTokenService.rotate(refreshToken);
    return new TokenResponse(newAccess, newRefresh);
}
```

**Interview Point:**

> Refresh token = long-lived, used only to get new access tokens. Enables logout/revocation. Rotate on use. Never send refresh token to every API call.

</details>

---

## Advanced

---

# 14. Security filter chain?

<details>
<summary>Show Answer</summary>

**Answer:**

The **Security Filter Chain** is an ordered list of **filters** that Spring Security applies to every HTTP request.

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())                    // API — often disabled
        .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**").permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
```

**Key filters (order matters):**

```text
1. SecurityContextHolderFilter
2. CorsFilter
3. LogoutFilter
4. JwtAuthenticationFilter        ← custom, before username/password
5. UsernamePasswordAuthenticationFilter
6. AuthorizationFilter            ← URL access rules
7. ExceptionTranslationFilter     ← 401 EntryPoint, 403 Handler
8. FilterSecurityInterceptor
```

| Config method | Effect |
|---------------|--------|
| `permitAll()` | No auth required |
| `authenticated()` | Any logged-in user |
| `hasRole("ADMIN")` | Specific role |
| `addFilterBefore()` | Insert custom filter |

```text
Request /api/orders + valid JWT
  → JwtFilter validates → sets SecurityContext
  → AuthorizationFilter checks authenticated() → pass
  → Controller runs

Request /api/orders + no JWT
  → JwtFilter skips
  → AuthorizationFilter → 401 Unauthorized
```

**Interview Point:**

> Filter chain = ordered security filters per request. Configure via SecurityFilterChain bean. Custom JWT filter goes before UsernamePasswordAuthenticationFilter.

</details>

---

# 15. How JWT validation works?

<details>
<summary>Show Answer</summary>

**Answer:**

JWT validation verifies the token is **authentic**, **not expired**, and **trusted** before granting access.

**Steps:**

```text
1. Extract token from Authorization: Bearer <token>
2. Parse header → check alg (reject "none", unexpected alg)
3. Verify signature with secret (HS256) or public key (RS256)
4. Validate claims:
   - exp (not expired)
   - iss (expected issuer)
   - aud (expected audience)
5. Extract sub, roles → build Authentication object
6. Set SecurityContextHolder.getContext().setAuthentication(auth)
7. Proceed to authorization check
```

```java
public boolean validateToken(String token) {
    try {
        Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .requireIssuer("my-app")
            .build()
            .parseClaimsJws(token);  // throws if invalid signature or expired
        return true;
    } catch (ExpiredJwtException e) {
        log.warn("JWT expired");
    } catch (JwtException e) {
        log.warn("JWT invalid: {}", e.getMessage());
    }
    return false;
}

public Authentication getAuthentication(String token) {
    Claims claims = extractClaims(token);
    String username = claims.getSubject();
    List<GrantedAuthority> authorities = extractRoles(claims);
    return new UsernamePasswordAuthenticationToken(username, null, authorities);
}
```

| Check | Failure result |
|-------|----------------|
| Missing token | 401 |
| Bad signature | 401 |
| Expired `exp` | 401 |
| Wrong `iss`/`aud` | 401 |
| Valid but insufficient role | 403 |

**Interview Point:**

> JWT validation = extract → verify signature → check exp/iss → build Authentication → SecurityContext. Invalid/expired = 401 before controller.

</details>

---

# 16. CSRF?

<details>
<summary>Show Answer</summary>

**Answer:**

**CSRF (Cross-Site Request Forgery)** is an attack where a malicious site tricks a user's browser into sending **authenticated requests** to your app (using the user's cookies).

```text
1. User logged into bank.com (session cookie in browser)
2. User visits evil.com
3. evil.com page: <form action="https://bank.com/transfer" method="POST">
                     <input name="amount" value="10000">
                   </form> + auto-submit
4. Browser sends request WITH bank.com cookie → unauthorized transfer
```

**Spring Security CSRF protection:**

```text
Server generates CSRF token → embedded in form / header
Client sends token with state-changing requests (POST, PUT, DELETE)
Server validates token matches session → reject if missing/wrong
```

```html
<!-- Thymeleaf form -->
<form method="post">
  <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
</form>
```

```java
// REST API with JWT (no cookies) — CSRF not needed
http.csrf(csrf -> csrf.disable());  // stateless token auth = immune to CSRF
```

| Scenario | CSRF risk | Action |
|----------|-----------|--------|
| Session cookie auth + forms | **High** | Enable CSRF (default) |
| JWT in Authorization header | **Low** | Disable CSRF OK |
| SPA + cookie session | **High** | CSRF token or SameSite cookie |

**Interview Point:**

> CSRF = forged request using victim's session cookie. Spring adds CSRF token by default. Disable only for stateless JWT APIs (token in header, not cookie).

</details>

---

# 17. CORS?

<details>
<summary>Show Answer</summary>

**Answer:**

**CORS (Cross-Origin Resource Sharing)** is a browser security mechanism that controls whether a **web page from one origin** can access resources from **another origin**.

```text
Origin = protocol + domain + port
https://app.example.com  ≠  https://api.example.com  → different origin
```

**Without CORS headers:**

```text
Browser: SPA at localhost:3000 calls API at localhost:8080
Server responds with data but NO CORS headers
Browser BLOCKS JavaScript from reading response (CORS error)
```

**Spring Security CORS config:**

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
    return http.build();
}

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("https://app.example.com"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
}
```

| Header | Purpose |
|--------|---------|
| `Access-Control-Allow-Origin` | Which origins can access |
| `Access-Control-Allow-Methods` | Allowed HTTP methods |
| `Access-Control-Allow-Headers` | Allowed request headers |
| `Access-Control-Allow-Credentials` | Allow cookies |

```text
CORS vs CSRF:
  CORS  = browser blocks cross-origin READ by JavaScript (server opts in)
  CSRF  = attacker tricks browser into unwanted WRITE with cookies
```

**Interview Point:**

> CORS = browser cross-origin policy. Server must send Allow-Origin headers. Configure allowed origins explicitly — never `*` with credentials. CORS is not a substitute for authentication.

</details>

---
