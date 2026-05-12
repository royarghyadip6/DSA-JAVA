# 🔐 MODULE 2: Spring Security Fundamentals (Clean Deep Dive)

## What is Spring Security?

Spring Security is a framework that handles:

* **Authentication** (who you are)
* **Authorization** (what you can access)
* Protection against common attacks (CSRF, session hijacking, etc.)

👉 Key idea:
It **intercepts every request before it reaches your controller** using filters.

Flow:

```
Request → Security Filters → Controller
```

---

# 🔐 1. What is `spring-boot-starter-security`?

It’s a **starter dependency** that pulls in:

* Spring Security core
* Default configuration
* Auto-configured authentication + authorization

👉 Maven:

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

---

# ⚙️ 2. What happens immediately after adding it?

The moment you add the dependency and start the app:

### ✅ Spring Boot Auto Configuration kicks in

Specifically:

* `SecurityAutoConfiguration`
* `UserDetailsServiceAutoConfiguration`

These classes configure security **without you writing a single line**

---

# 🔄 3. Why does a login page automatically appear?

Because Spring Boot registers a **default SecurityFilterChain**.

### Key class:

👉 `SecurityAutoConfiguration`

Internally imports:

* `SpringBootWebSecurityConfiguration`

---

# 🔗 4. Core Flow (Very Important for Interview)

### Step-by-step:

1. Application starts
2. Spring Boot scans classpath
3. Finds Spring Security dependency
4. Triggers Auto Configuration
5. Creates a **default SecurityFilterChain**
6. Registers **form login**
7. Adds **default login page generator**
8. All endpoints → secured

---

# 🧠 5. The Exact Classes Responsible

## (A) `SecurityAutoConfiguration`

* Entry point for security auto config
* Uses `@Import`

---

## (B) `SpringBootWebSecurityConfiguration`

This is where magic happens.

It defines:

```java
@Bean
SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
```

---

## (C) `HttpSecurity` (CORE BUILDER)

This builds security rules:

```java
http
  .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
  .formLogin(withDefaults())
  .httpBasic(withDefaults());
```

👉 This line is why:

* All endpoints require authentication
* Login form appears

---

# 🎨 6. Who Generates the Login Page?

👉 Class:
`DefaultLoginPageGeneratingFilter`

### What it does:

* Intercepts request to `/login`
* If no custom login page → generates HTML login form dynamically

---

# 🔗 7. Full Filter Chain (Simplified)

When request comes:

```
Request
  ↓
SecurityContextPersistenceFilter
  ↓
UsernamePasswordAuthenticationFilter
  ↓
DefaultLoginPageGeneratingFilter
  ↓
ExceptionTranslationFilter
  ↓
FilterSecurityInterceptor
  ↓
Controller
```

---

# 🔑 8. Default Username & Password – Where from?

👉 Class: `UserDetailsServiceAutoConfiguration`

Creates:

```java
@Bean
UserDetailsService userDetailsService()
```

### Default user:

* Username: `user`
* Password: randomly generated (printed in logs)

Uses:

```java
User.withDefaultPasswordEncoder()
```

---

# 🧩 9. Important Beans Auto-Created

When you add starter:

* `SecurityFilterChain`
* `UserDetailsService`
* `AuthenticationManager`
* `PasswordEncoder` (DelegatingPasswordEncoder)

---

# 🧪 10. How Authentication Actually Works (Deep Flow)

1. User hits `/protected`
2. Redirected to `/login`
3. `DefaultLoginPageGeneratingFilter` shows form
4. User submits credentials
5. `UsernamePasswordAuthenticationFilter` intercepts POST `/login`
6. Creates `Authentication` object
7. Sends to `AuthenticationManager`
8. Delegates to `DaoAuthenticationProvider`
9. Calls `UserDetailsService`
10. Password verified using `PasswordEncoder`
11. Success → stored in `SecurityContext`

---

# 🧬 11. Important Annotations (Deep Dive)

## ✅ `@EnableWebSecurity`

* Enables Spring Security
* Imports `WebSecurityConfiguration`

👉 In Spring Boot → **already enabled automatically**

---

## ✅ `@Configuration`

Marks security config class

---

## ✅ `@Bean`

Defines:

* SecurityFilterChain
* PasswordEncoder
* UserDetailsService

---

## ✅ `@ConditionalOnClass`

Used in auto-config

👉 Meaning:
“Only configure if this class exists”

---

## ✅ `@ConditionalOnMissingBean`

👉 Very important

Means:

* “Create default config only if user hasn’t defined their own”

👉 This is how customization works:

* You define your own `SecurityFilterChain`
* Default one disappears

---

## ✅ `@Import`

Used to bring in other configurations

---

# 🔥 12. Why Everything is Secured by Default?

Because of:

```java
.anyRequest().authenticated()
```

👉 This is intentionally strict:

* Secure by default (fail-safe design)

---

# ⚡ 13. How to Override Default Behavior?

Define your own config:

```java
@Bean
SecurityFilterChain custom(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/public/**").permitAll()
            .anyRequest().authenticated()
        )
        .formLogin();

    return http.build();
}
```

👉 This replaces default config completely

---

# 🧠 14. Key Interview Insight (Very Important)

When asked:
👉 “Why login page appears automatically?”

Say this:

> “Spring Boot’s autoconfiguration creates a default SecurityFilterChain using HttpSecurity, enables formLogin(), and registers DefaultLoginPageGeneratingFilter which dynamically generates the login page if no custom page is provided.”

---

---

---

## Features & Capabilities

### Authentication

Verifies user identity using:

* Username/password
* JWT
* OAuth2
* LDAP

Internally uses:

* `AuthenticationManager`
* `AuthenticationProvider`

---

### Authorization

Controls access based on:

* Roles (`ROLE_ADMIN`)
* Authorities
* Method-level annotations (`@PreAuthorize`)

👉 Happens **after authentication**

---

### Security Protections

Built-in defenses:

* CSRF protection
* Session fixation protection
* Security headers (XSS, clickjacking)

---

### Session Management

Supports:

* Stateful (session-based login)
* Stateless (JWT APIs)

Example:

```java
.sessionManagement().sessionCreationPolicy(STATELESS)
```

---

### Password Security

* Uses hashing (BCrypt)
* Supports secure password encoding
* Prevents storing plain passwords

---

### Flexible Configuration

You can control:

* URL-level security
* Method-level security
* Custom filters
* Custom authentication logic

---

### Security Context

Stores current user info:

* Authentication object
* Roles/authorities

Accessed via:

```java
SecurityContextHolder
```

---

## Architecture Overview (Most Important)

### High-level flow

```
Client Request
   ↓
Security Filter Chain
   ↓
Authentication
   ↓
Authorization
   ↓
Controller
```

---

### Core Components

**1. Security Filter Chain**

* Intercepts every request
* Runs multiple filters (auth, authorization, etc.)

---

**2. Authentication**

Main classes:

* `AuthenticationManager` → entry point
* `AuthenticationProvider` → validates user
* `UserDetailsService` → loads user

Flow:

```
Request → Extract credentials → AuthenticationManager
       → AuthenticationProvider → Validate
       → Create Authentication object
```

---

**3. Authorization**

Checks permissions after authentication:

```
Authenticated user → Check roles → Allow/Deny
```

---

**4. SecurityContext**

* Stores Authentication object
* Stored in **ThreadLocal** (per request)

---

**5. Exception Handling**

Handled by:

* `ExceptionTranslationFilter`

Converts:

* Auth failure → 401
* Access denied → 403

---

## End-to-End Flow (Interview Ready)

```
1. Request enters server
2. Security filter chain intercepts
3. Authentication happens
4. Authentication stored in SecurityContext
5. Authorization checks roles
6. If allowed → Controller
7. Else → 401 / 403
```

---

## Final Mental Model

* Spring Security = **Gatekeeper**
* Filters = **Security guards**
* Authentication = **ID check**
* Authorization = **Permission check**

---

## Common Mistakes

* Thinking authentication = authorization
* Not understanding filter chain
* Ignoring SecurityContext
* Wrong role configuration

---

## Interview Gold Line

👉 “Spring Security is a filter-based framework that performs authentication and authorization before the request reaches the controller.”

---

