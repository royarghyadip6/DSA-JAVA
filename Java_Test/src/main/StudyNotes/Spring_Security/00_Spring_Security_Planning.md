# Spring Security — Master Index (Senior Backend Engineer Track)

> **Audience:** Java backend engineers with 5–8 years of experience.
> **Baseline:** Spring Security **6.x** on Spring Boot **3.x** (Jakarta namespace, Java 17+).
> **Deltas covered in every file:** what changed coming from 5.x, and what breaks in 7.x.

---

## New To Spring Security? Start Here Instead

If you are a beginner, **do not start with this index.** Read
[`00A_Start_Here_Beginner_Primer.md`](00A_Start_Here_Beginner_Primer.md) first. It explains the
whole subject in plain language with no prior knowledge assumed, defines every recurring term
in a glossary, and gives you a six-week learning path. Come back here once it makes sense.

---

## How To Use These Notes

Each file is self-contained and follows the same structure, so you can jump straight to a
topic without reading linearly. But if you are studying end-to-end, follow the module order
below — later modules assume the vocabulary established earlier.

**Every file is written for two audiences at once.** The explanatory sections are pitched so a
newcomer can follow them; the interview sections are pitched at five to eight years of
experience and deliberately do not get easier.

| Reading for | Read these sections, in this order | Skip on the first pass |
|---|---|---|
| **Learning the subject** | In Plain English, then Core Concepts (read the "In simple terms" lead of every subsection first, then go back for detail), then Working Code, then Debugging Playbook | Version Matrix, Internals, Interview Q&A |
| **Interview preparation** | Quick Recall, then Interview Q&A (answer out loud *before* expanding each block), then Internals, then Version Matrix | In Plain English |

Every file contains four things you will not find in typical notes:

1. **An "In Plain English" section** — an everyday analogy, a jargon-free walkthrough, and a
   table translating every term used in that file into plain words.
2. **"In simple terms" leads** on each Core Concepts subsection, so you get the idea before
   the detail.
3. **Interview Q&A with counter-questions.** Each answer is followed by the follow-up probes
   a senior interviewer actually asks next, with answers. Answers are inside collapsible
   `<details>` blocks so you can self-test. These are **not** simplified for beginners — treat
   them as a target to work towards.
4. **A debugging playbook** — symptom → root cause → fix — because at this level you are
   judged on how fast you diagnose, not on whether you can recite definitions.

---

## The Standard File Structure

| Section | What it gives you |
|---|---|
| Version Matrix | 6.x baseline, 5.x delta, 7.x delta for that topic |
| Why This Exists | The problem the component solves, and where it sits in the request flow |
| Core Concepts | Real interface signatures, not paraphrases |
| Flow Diagram | Mermaid diagram of the runtime flow or class relationships |
| Working Code | Config bean + component + test, written to be compilable |
| Internals | What the framework actually does, with named classes and methods |
| Configuration Reference | Option → effect → default |
| Production Concerns | Pitfalls and anti-patterns seen in real systems |
| Debugging Playbook | Symptom → root cause → fix |
| Interview Q&A | Senior-level questions, each with nested counter-questions |
| Design Scenario | An architect-level open-ended question |
| Quick Recall | A compressed cheat block for revision |

---

## Version Baseline (Read This Once)

These notes teach Spring Security 6.x. The three versions you will meet in the wild:

| | Spring Security 5.7/5.8 | **Spring Security 6.x** | Spring Security 7.0 |
|---|---|---|---|
| Ships with | Boot 2.7 | **Boot 3.x** | Boot 4.0 (Nov 2025) |
| Namespace | `javax.*` | **`jakarta.*`** | `jakarta.*` |
| Config style | `WebSecurityConfigurerAdapter` (deprecated 5.7) | **`SecurityFilterChain` bean** | `SecurityFilterChain` bean only |
| URL matching | `antMatchers()` / `mvcMatchers()` | **`requestMatchers()`** | `requestMatchers()` + `PathPatternRequestMatcher` |
| Authorization API | `authorizeRequests()` + `AccessDecisionManager` | **`authorizeHttpRequests()` + `AuthorizationManager`** | `authorizeHttpRequests()` only; `AccessDecisionManager` moved out to `spring-security-access` |
| DSL chaining | `.and()` chaining | **Lambda DSL (preferred)** | Lambda DSL only — `.and()` removed |
| Context persistence | `SecurityContextPersistenceFilter` | **`SecurityContextHolderFilter` + explicit `SecurityContextRepository`** | same |
| Custom DSL | `.apply(...)` | `.apply(...)` (deprecated) / `.with(...)` | `.with(...)` only |

**The seven hard removals in 7.0** that stop an app from starting:

1. `.and()` chaining — every configurer must use its own lambda.
2. `authorizeRequests()` — use `authorizeHttpRequests()`.
3. `AntPathRequestMatcher` / `MvcRequestMatcher` — use `PathPatternRequestMatcher`.
4. `AccessDecisionManager` / `AccessDecisionVoter` — moved to the `spring-security-access` module.
5. `AuthorizationManager#check` — renamed to `AuthorizationManager#authorize`.
6. `HttpSecurity.apply(...)` — use `HttpSecurity.with(...)`.
7. OAuth2 Resource Owner Password Credentials grant — deleted outright.

**New in 7.0 worth knowing:** first-class multi-factor authentication support,
`Authentication.Builder` for mutating/merging authentications, `AuthorizationManagerFactory`,
`AllAuthoritiesAuthorizationManager`, and SPA-friendly CSRF configuration.

---

## Module Map

### Module 0 — Orientation

- [`00A_Start_Here_Beginner_Primer.md`](00A_Start_Here_Beginner_Primer.md) — the whole subject in plain English: what problem security solves, why HTTP's lack of memory causes everything else, the six objects you will meet everywhere, passwords, roles, 401 vs 403, CSRF vs CORS, a first working configuration explained line by line, a full glossary, and a six-week learning path

### Module 1 — Prerequisites

You cannot reason about Spring Security without these. Interviewers use them to separate
people who memorised config from people who understand the protocol.

- [`01_M1_T1_HTTP_Web_Basics.md`](01_M1_T1_HTTP_Web_Basics.md) — HTTP methods, safety/idempotency, the `Authorization` header, cookie attributes (`HttpOnly`, `Secure`, `SameSite`), 401 vs 403 vs 419, statelessness
- [`02_M1_T2_Authentication_Authorization.md`](02_M1_T2_Authentication_Authorization.md) — AuthN vs AuthZ vs accounting, identity/principal/subject, roles vs authorities vs permissions, the confused-deputy problem
- [`03_M1_T3_Cryptography.md`](03_M1_T3_Cryptography.md) — hashing vs encryption vs encoding, symmetric/asymmetric, salt vs pepper, KDFs (bcrypt/scrypt/Argon2), HMAC vs digital signature, constant-time comparison
- [`04_M1_T4_Servlet_Basics.md`](04_M1_T4_Servlet_Basics.md) — servlet lifecycle and threading, `Filter` vs `HandlerInterceptor` vs AOP, `OncePerRequestFilter`, `DispatcherServlet`, async dispatch and `ERROR` dispatch

### Module 2 — Spring Security Fundamentals

- [`05_M2_T1_Spring_Security_Introduction.md`](05_M2_T1_Spring_Security_Introduction.md) — what the framework is, what auto-configuration actually registers, the architecture in one picture
- [`06_M2_T2_Core_Components.md`](06_M2_T2_Core_Components.md) — `SecurityContext`, `SecurityContextHolder`, `Authentication`, `GrantedAuthority`, `UserDetails`, `UserDetailsService`
- [`07_M2_T3_Security_Filter_Chain.md`](07_M2_T3_Security_Filter_Chain.md) — `DelegatingFilterProxy`, `FilterChainProxy`, `VirtualFilterChain`, the ordered default filter list, chain selection

### Module 3 — Authentication

- [`08_M3_T1_Authentication_Mechanism.md`](08_M3_T1_Authentication_Mechanism.md) — `AuthenticationManager`, `ProviderManager`, `AuthenticationProvider`, token lifecycle, `AuthenticationEventPublisher`
- [`09_M3_T2_InMemory_Authentication.md`](09_M3_T2_InMemory_Authentication.md) — `InMemoryUserDetailsManager`, when it is legitimate, when it is a liability
- [`10_M3_T3_JDBC_Authentication.md`](10_M3_T3_JDBC_Authentication.md) — `JdbcUserDetailsManager`, the default schema, custom queries, group-based authorities
- [`11_M3_T4_Custom_Authentication.md`](11_M3_T4_Custom_Authentication.md) — custom `UserDetailsService` vs custom `AuthenticationProvider`, and how to choose
- [`12_M3_T5_Password_Encoding.md`](12_M3_T5_Password_Encoding.md) — `PasswordEncoder`, `DelegatingPasswordEncoder`, `{id}` prefixes, cost tuning, upgrade-on-login

### Module 4 — Authorization

- [`13_M4_T1_Authorization_URL_Based.md`](13_M4_T1_Authorization_URL_Based.md) — `authorizeHttpRequests`, matcher ordering, `AuthorizationFilter`, `permitAll` vs `ignoring()`
- [`14_M4_T2_Method_Level_Security.md`](14_M4_T2_Method_Level_Security.md) — `@EnableMethodSecurity`, `@PreAuthorize`/`@PostAuthorize`/`@PreFilter`/`@PostFilter`, `@Secured`, JSR-250
- [`15_M4_T3_Expression_Based_Access_Control.md`](15_M4_T3_Expression_Based_Access_Control.md) — SpEL in security, `MethodSecurityExpressionRoot`, custom expression beans, `@P` and `#root`
- [`16_M4_T4_Role_Authority.md`](16_M4_T4_Role_Authority.md) — the `ROLE_` prefix contract, `hasRole` vs `hasAuthority`, `RoleHierarchy`, scope-vs-role in OAuth2

### Module 5 — Modern Configuration

- [`17_M5_T1_SecurityFilterChain_Bean.md`](17_M5_T1_SecurityFilterChain_Bean.md) — the bean-based model, multiple chains, `@Order`, `securityMatcher`
- [`18_M5_T2_HttpSecurity_DSL.md`](18_M5_T2_HttpSecurity_DSL.md) — the DSL as a builder of configurers, every major configurer, `SharedObject`s, custom DSL with `.with()`
- [`19_M5_T3_Custom_Login_And_Handlers.md`](19_M5_T3_Custom_Login_And_Handlers.md) — custom login pages, `AuthenticationSuccessHandler`/`FailureHandler`, `LogoutHandler`, JSON login

### Module 6 — Session, CSRF, CORS

- [`20_M6_T1_Session_Management.md`](20_M6_T1_Session_Management.md) — `SessionCreationPolicy`, session fixation protection, concurrent session control, Spring Session
- [`21_M6_T2_CSRF_Protection.md`](21_M6_T2_CSRF_Protection.md) — the attack, token repositories, the BREACH-safe handler, SPA patterns, when disabling is actually safe
- [`22_M6_T3_CORS.md`](22_M6_T3_CORS.md) — preflight mechanics, `CorsConfigurationSource`, why CORS is not a security control

### Module 7 — JWT

- [`23_M7_T1_JWT_Fundamentals.md`](23_M7_T1_JWT_Fundamentals.md) — JOSE, JWS vs JWE, registered claims, HS256 vs RS256 vs ES256, `alg: none` and confusion attacks
- [`24_M7_T2_JWT_Spring_Integration.md`](24_M7_T2_JWT_Spring_Integration.md) — resource server vs hand-rolled filter, `JwtDecoder`, `JwtAuthenticationConverter`, `OncePerRequestFilter`
- [`25_M7_T3_Token_Strategies.md`](25_M7_T3_Token_Strategies.md) — access/refresh split, rotation and reuse detection, revocation, storage on the client

### Module 8 — Filters Deep Dive

- [`26_M8_T1_Default_Filters.md`](26_M8_T1_Default_Filters.md) — the full ordered filter catalogue with the job of each
- [`27_M8_T2_Custom_Filters.md`](27_M8_T2_Custom_Filters.md) — writing filters, `addFilterBefore/After/At`, the double-registration trap, debugging the chain

### Module 9 — Exception Handling

- [`28_M9_T1_Security_Exception_Handling.md`](28_M9_T1_Security_Exception_Handling.md) — `ExceptionTranslationFilter`, `AuthenticationEntryPoint`, `AccessDeniedHandler`, RFC 7807 problem details, why `@ControllerAdvice` does not catch filter exceptions

### Module 10 — OAuth2 & OpenID Connect

- [`29_M10_T1_OAuth2_OIDC_Fundamentals.md`](29_M10_T1_OAuth2_OIDC_Fundamentals.md) — roles, grants, PKCE, OIDC on top of OAuth2, ID token vs access token
- [`30_M10_T2_OAuth2_Client.md`](30_M10_T2_OAuth2_Client.md) — `oauth2Login`, `ClientRegistration`, `OAuth2AuthorizedClient`, mapping the provider's claims to authorities
- [`31_M10_T3_Resource_Server.md`](31_M10_T3_Resource_Server.md) — JWT validation, JWKS caching, opaque token introspection, audience and issuer validation

### Module 11 — Advanced

- [`32_M11_T1_SecurityContext_Internals.md`](32_M11_T1_SecurityContext_Internals.md) — `SecurityContextHolderStrategy`, ThreadLocal, async/`@Async`/`CompletableFuture` propagation, `DelegatingSecurityContext*`
- [`33_M11_T2_Multi_Step_Authentication.md`](33_M11_T2_Multi_Step_Authentication.md) — OTP/2FA/MFA flows, partial authentication tokens, step-up authentication
- [`34_M11_T3_Multi_Tenancy.md`](34_M11_T3_Multi_Tenancy.md) — tenant resolution, per-tenant issuers, tenant-scoped authorization, data isolation
- [`35_M11_T4_Method_Security_Internals_ACL.md`](35_M11_T4_Method_Security_Internals_ACL.md) — how the AOP interceptors are wired, `AuthorizationManagerBeforeMethodInterceptor`, domain-object ACLs

### Module 12 — Production Hardening

- [`36_M12_T1_Security_Headers_HTTPS.md`](36_M12_T1_Security_Headers_HTTPS.md) — HSTS, CSP, `X-Frame-Options`, `Referrer-Policy`, `requiresChannel`, TLS termination behind a proxy
- [`37_M12_T2_Brute_Force_Protection.md`](37_M12_T2_Brute_Force_Protection.md) — lockout, exponential backoff, rate limiting, credential stuffing, user enumeration
- [`38_M12_T3_Auditing_And_Monitoring.md`](38_M12_T3_Auditing_And_Monitoring.md) — authentication events, Spring Data auditing, what to log and what never to log

### Module 13 — Microservices Security

- [`39_M13_T1_Gateway_Security.md`](39_M13_T1_Gateway_Security.md) — centralised authentication at the edge, token translation, trust boundaries
- [`40_M13_T2_Service_To_Service.md`](40_M13_T2_Service_To_Service.md) — token propagation, client credentials, token exchange, mTLS

### Module 14 — Testing

- [`41_M14_T1_Testing_Spring_Security.md`](41_M14_T1_Testing_Spring_Security.md) — `spring-security-test`, `@WithMockUser`, `@WithUserDetails`, custom annotations, request post-processors, testing the filter chain

### Module 15 — Real-World Patterns

- [`42_M15_T1_RBAC_ABAC_Patterns.md`](42_M15_T1_RBAC_ABAC_Patterns.md) — RBAC vs ABAC vs ReBAC, permission modelling, `PermissionEvaluator`, externalised policy (OPA/Cedar)

### Module 16 — Performance

- [`43_M16_T1_Performance_Optimization.md`](43_M16_T1_Performance_Optimization.md) — filter chain cost, static-resource bypass, caching `UserDetails`, bcrypt strength vs latency, JWKS and introspection caching

### Module 17 — Interview Deep Dive

- [`44_M17_T1_Interview_Deep_Dive.md`](44_M17_T1_Interview_Deep_Dive.md) — cross-topic questions, whiteboard flows, and full system-design answers

### Module 18 — Modern & Adjacent (added beyond the original syllabus)

- [`45_M18_T1_Reactive_WebFlux_Security.md`](45_M18_T1_Reactive_WebFlux_Security.md) — `SecurityWebFilterChain`, `ReactiveSecurityContextHolder`, why ThreadLocal reasoning collapses
- [`46_M18_T2_Spring_Authorization_Server.md`](46_M18_T2_Spring_Authorization_Server.md) — running your own OAuth2/OIDC provider
- [`47_M18_T3_Modern_Auth_Mechanisms.md`](47_M18_T3_Modern_Auth_Mechanisms.md) — passkeys/WebAuthn, one-time-token login, remember-me, LDAP, SAML2, Actuator endpoint security

---

## The Ten Things You Must Be Able To Whiteboard

If you can do these from memory, you are interview-ready:

1. The full request path from socket to controller, naming every Spring Security component.
2. The authentication lifecycle: unauthenticated token → `ProviderManager` → authenticated token → `SecurityContextRepository`.
3. Why 401 and 403 are produced by different code paths, and by which classes.
4. CSRF vs CORS — different problems, different solutions, commonly conflated.
5. Role vs authority, and exactly where the `ROLE_` prefix is added and stripped.
6. Session-based vs token-based authentication, with the revocation trade-off stated honestly.
7. JWT structure, what the signature does and does not protect, and the `alg` confusion attack.
8. Where method security sits relative to the filter chain, and what that means for `@PostAuthorize`.
9. How you would secure a microservices estate: edge, service-to-service, and data layer.
10. How you would migrate a Boot 2.7 / Security 5.7 app to Boot 3 / Security 6 without downtime.

---

## Practice Projects

Build these in order. Each one forces you to hit the failure modes described in the notes.

1. **Form login + in-memory users.** Add a custom login page, success handler, and logout. Then break it on purpose: wrong `PasswordEncoder`, matcher order inverted, CSRF token missing.
2. **Database-backed authentication with roles.** Custom `UserDetailsService`, bcrypt, role hierarchy, method security on the service layer.
3. **Stateless JWT API.** Issue and validate tokens, add refresh-token rotation with reuse detection, and wire `AuthenticationEntryPoint` + `AccessDeniedHandler` to return RFC 7807 JSON.
4. **Microservices estate.** Gateway performs OIDC login, exchanges for an internal token, propagates it downstream; downstream services run as resource servers with audience validation and mTLS between them.
